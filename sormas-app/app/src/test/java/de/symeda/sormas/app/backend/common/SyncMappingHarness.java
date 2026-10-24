package de.symeda.sormas.app.backend.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Works out which fields a DtoHelper actually copies, in each direction, by reading the compiled
 * class.
 *
 * <p>
 * The app converts between server DTOs and its own local entities by hand, in two separate methods
 * per entity:
 *
 * <ul>
 * <li>{@code fillInnerFromDto(ADO ado, DTO dto)} - server to phone
 * <li>{@code fillInnerFromAdo(DTO dto, ADO ado)} - phone to server
 * </ul>
 *
 * Each is a list of {@code set...(get...())} lines. A field missing from one loses data silently in
 * that direction, and nothing in the build catches it.
 *
 * <p>
 * <b>Why bytecode rather than source.</b> This reads the compiled output of sormas-app and
 * sormas-api - the same classes that ship - so the answer cannot drift because of how the source
 * happens to be formatted, indented or wrapped, and there is no need to locate source files relative
 * to a working directory. Running the helpers is not an option:
 * {@code CampaignFormDataDtoHelper.fillInnerFromDto} makes seven {@code DatabaseHelper.getXDao()}
 * calls, so it needs a live Android context and SQLite database, and reflection cannot see inside a
 * method body.
 *
 * <p>
 * <b>How direction is determined.</b> A setter call in bytecode records the type it was called on. In
 * {@code fillInnerFromDto(Area area, AreaDto dto)} a copy into the entity appears as a call to
 * {@code Area.setName}, while the same line written the wrong way round appears as
 * {@code AreaDto.setName}. The owner type alone therefore separates the two directions, and a copy
 * written onto the wrong object is not credited. No local variable tracking is needed, which is what
 * made an earlier source-reading version fragile: parameter names vary between helpers and even
 * between the two methods of one helper.
 *
 * <p>
 * <b>Three states, not two.</b> Seven of the sixteen synced entities are pull only - their
 * {@code fillInnerFromAdo} throws {@code UnsupportedOperationException}, because the app receives
 * that reference data and never sends it back. Reporting every field as a gap for those would produce
 * around forty false positives. A third case exists too: {@code UserDtoHelper} implements the push
 * direction but copies only {@code token}, deliberately, because users are managed on the server. So
 * the harness reports facts and leaves intent to an explicit expected list in each per-entity test.
 *
 * <p>
 * Anything it cannot interpret throws. Skipping quietly is how an earlier attempt produced a wrong
 * answer that looked clean.
 */
public final class SyncMappingHarness {

	/** Copied by AdoDtoHelper itself in both directions, not by the per-entity helpers. */
	private static final Set<String> HANDLED_BY_BASE_CLASS = new LinkedHashSet<>(Arrays.asList("uuid", "creationDate", "changeDate"));

	static final String FROM_DTO = "fillInnerFromDto";
	static final String FROM_ADO = "fillInnerFromAdo";

	private SyncMappingHarness() {
	}

	public static SyncMapping analyse(Class<? extends AdoDtoHelper<?, ?>> helperClass) {

		Class<?>[] types = readGenericTypes(helperClass);
		Class<?> entityClass = types[0];
		Class<?> dtoClass = types[1];

		Bytecode bytecode = read(helperClass);

		Set<String> entityFields = declaredDataFields(entityClass);
		Set<String> dtoFields = declaredDataFields(dtoClass);

		Set<String> shared = new LinkedHashSet<>(entityFields);
		shared.retainAll(dtoFields);

		return new SyncMapping(
			entityClass,
			dtoClass,
			entityFields,
			dtoFields,
			bytecode.settersCalledOn(FROM_DTO, entityClass, shared),
			bytecode.settersCalledOn(FROM_ADO, dtoClass, shared),
			!bytecode.throwsUnsupported(FROM_ADO));
	}

	/**
	 * Takes the entity and DTO types from {@code extends AdoDtoHelper<Entity, Dto>}. Reflection on the
	 * generic superclass is exact, unlike reading a method signature, where parameter order and naming
	 * differ between helpers.
	 */
	private static Class<?>[] readGenericTypes(Class<?> helperClass) {

		Type superType = helperClass.getGenericSuperclass();
		if (!(superType instanceof ParameterizedType)) {
			throw new IllegalStateException(
				helperClass.getName() + " does not extend AdoDtoHelper with type arguments, so its entity and DTO cannot be determined. "
					+ "Either it is not a standard helper, or the harness needs extending to cover it. Do not skip it.");
		}

		Type[] args = ((ParameterizedType) superType).getActualTypeArguments();
		if (args.length != 2 || !(args[0] instanceof Class) || !(args[1] instanceof Class)) {
			throw new IllegalStateException(helperClass.getName() + " has unexpected AdoDtoHelper type arguments: " + Arrays.toString(args));
		}

		return new Class<?>[] {
			(Class<?>) args[0],
			(Class<?>) args[1] };
	}

	/**
	 * Fields declared on the class itself. Inherited fields are excluded deliberately: uuid,
	 * creationDate and changeDate are copied by AdoDtoHelper, not by the per-entity helper, so counting
	 * them would report gaps that are not gaps.
	 */
	private static Set<String> declaredDataFields(Class<?> type) {

		Set<String> names = new LinkedHashSet<>();
		for (Field field : type.getDeclaredFields()) {
			if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
				continue; // serialVersionUID and constants are not data
			}
			if (HANDLED_BY_BASE_CLASS.contains(field.getName())) {
				continue;
			}
			names.add(field.getName());
		}
		return names;
	}

	private static Bytecode read(Class<?> helperClass) {

		String resource = '/' + helperClass.getName().replace('.', '/') + ".class";
		try (InputStream in = helperClass.getResourceAsStream(resource)) {
			if (in == null) {
				throw new IllegalStateException(
					"Could not find the compiled class for " + helperClass.getName() + " at " + resource
						+ ". The harness reads build output, so the module must be compiled before these tests run.");
			}
			Bytecode bytecode = new Bytecode(helperClass);
			new ClassReader(in).accept(bytecode, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			bytecode.verifyBothDirectionsArePresent();
			return bytecode;
		} catch (IOException e) {
			throw new UncheckedIOException("Could not read the compiled class for " + helperClass.getName(), e);
		}
	}

	/** Collects, per direction, which setters were called and on what type. */
	private static final class Bytecode extends ClassVisitor {

		private final Class<?> helperClass;
		private final Map<String, Set<String>> callsByMethod = new LinkedHashMap<>();
		private final Set<String> throwsUnsupported = new LinkedHashSet<>();

		private Bytecode(Class<?> helperClass) {
			super(Opcodes.ASM9);
			this.helperClass = helperClass;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

			if (!FROM_DTO.equals(name) && !FROM_ADO.equals(name)) {
				return null;
			}

			final String method = name;
			final Set<String> calls = callsByMethod.computeIfAbsent(method, key -> new LinkedHashSet<>());

			return new MethodVisitor(Opcodes.ASM9) {

				@Override
				public void visitMethodInsn(int opcode, String owner, String callee, String methodDescriptor, boolean isInterface) {
					if (callee.startsWith("set")) {
						// owner is the type the setter was called on, which is what separates the
						// two directions and rejects a copy written onto the wrong object.
						calls.add(owner + '#' + callee);
					}
				}

				@Override
				public void visitTypeInsn(int opcode, String type) {
					if (opcode == Opcodes.NEW && "java/lang/UnsupportedOperationException".equals(type)) {
						throwsUnsupported.add(method);
					}
				}
			};
		}

		/**
		 * Both directions must be declared on the helper itself. A compiled class with only one of them
		 * cannot be analysed, and must fail loudly rather than be reported as having no gaps.
		 */
		private void verifyBothDirectionsArePresent() {
			for (String required : new String[] {
				FROM_DTO,
				FROM_ADO }) {
				if (!callsByMethod.containsKey(required)) {
					throw new IllegalStateException(
						helperClass.getName() + " has no " + required + " of its own in the compiled class, so it cannot be analysed. "
							+ "Extend the harness rather than excluding it.");
				}
			}
		}

		private boolean throwsUnsupported(String method) {
			return throwsUnsupported.contains(method);
		}

		/**
		 * Of the fields given, those the method assigns on the target type. A setter inherited from a
		 * superclass still counts, because the call is made on the target object either way.
		 */
		private Set<String> settersCalledOn(String method, Class<?> targetType, Set<String> candidates) {

			Set<String> calls = callsByMethod.getOrDefault(method, Collections.emptySet());
			Set<String> assigned = new LinkedHashSet<>();

			for (String field : candidates) {
				String setter = "set" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
				for (String call : calls) {
					int split = call.indexOf('#');
					if (!call.substring(split + 1).equals(setter)) {
						continue;
					}
					if (isTargetOrSuperclassOf(call.substring(0, split).replace('/', '.'), targetType)) {
						assigned.add(field);
						break;
					}
				}
			}
			return assigned;
		}

		private boolean isTargetOrSuperclassOf(String owner, Class<?> targetType) {
			for (Class<?> type = targetType; type != null; type = type.getSuperclass()) {
				if (type.getName().equals(owner)) {
					return true;
				}
			}
			return false;
		}
	}
}
