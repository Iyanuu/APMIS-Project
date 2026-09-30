package de.symeda.sormas.app.backend.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Works out which fields a DtoHelper actually copies, in each direction.
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
 * <b>Why this reads source rather than running the code.</b> Executing a helper is not an option:
 * {@code CampaignFormDataDtoHelper.fillInnerFromDto} makes seven {@code DatabaseHelper.getXDao()}
 * calls, so it needs a live Android context and SQLite database. Reflection cannot see inside a
 * method body either. So the copied fields are read from the source file, while the field lists come
 * from reflection, which is exact.
 *
 * <p>
 * <b>Why it asks per field rather than extracting every assignment.</b> An earlier attempt tried to
 * pull all {@code target.set...} calls out of each method and silently missed 19 of 59 helpers,
 * because the parameter names vary: {@code ado}/{@code dto}, {@code area}/{@code dto},
 * {@code target}/{@code source}. This reads the actual first parameter name from the signature and
 * then asks, for each field it cares about, whether that parameter is assigned. Fewer moving parts,
 * and nothing can be missed without the test noticing.
 *
 * <p>
 * Anything it cannot interpret throws. Skipping quietly is how the earlier attempt produced a wrong
 * answer that looked clean.
 *
 * <p>
 * <b>Three states, not two.</b> Seven of the sixteen synced entities are pull only - their
 * {@code fillInnerFromAdo} throws {@code UnsupportedOperationException}, because the app receives
 * that reference data and never sends it. Reporting every field as a gap for those would produce
 * around forty false positives. A third case exists too: {@code UserDtoHelper} implements the push
 * direction but copies only {@code token}, deliberately, because users are managed on the server. The
 * harness therefore reports facts - what is copied, and whether a direction is supported at all - and
 * leaves intent to an explicit expected list in each per-entity test.
 */
public final class SyncMappingHarness {

	/** Handled by AdoDtoHelper itself in both directions, not by the per-entity helpers. */
	private static final Set<String> HANDLED_BY_BASE_CLASS = new LinkedHashSet<>(java.util.Arrays.asList("uuid", "creationDate", "changeDate"));

	private static final String[] CANDIDATE_SOURCE_ROOTS = {
		"src/main/java",
		"app/src/main/java",
		"sormas-app/app/src/main/java",
		"../sormas-app/app/src/main/java" };

	private SyncMappingHarness() {
	}

	public static SyncMapping analyse(Class<? extends AdoDtoHelper<?, ?>> helperClass) {

		Class<?>[] types = readGenericTypes(helperClass);
		Class<?> entityClass = types[0];
		Class<?> dtoClass = types[1];

		String source = readSource(helperClass);

		Method fromDto = readMethod(helperClass, source, "fillInnerFromDto");
		Method fromAdo = readMethod(helperClass, source, "fillInnerFromAdo");

		Set<String> entityFields = declaredDataFields(entityClass);
		Set<String> dtoFields = declaredDataFields(dtoClass);

		Set<String> shared = new LinkedHashSet<>(entityFields);
		shared.retainAll(dtoFields);

		boolean pushSupported = !fromAdo.body.contains("UnsupportedOperationException");

		return new SyncMapping(
			entityClass,
			dtoClass,
			entityFields,
			dtoFields,
			assignedFields(fromDto, shared),
			assignedFields(fromAdo, shared),
			pushSupported);
	}

	/**
	 * Takes the entity and DTO types from {@code extends AdoDtoHelper<Entity, Dto>}. Reflection on the
	 * generic superclass is exact, unlike reading the method signature, where parameter order and
	 * naming differ between helpers.
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
			throw new IllegalStateException(helperClass.getName() + " has unexpected AdoDtoHelper type arguments: " + java.util.Arrays.toString(args));
		}

		return new Class<?>[] {
			(Class<?>) args[0],
			(Class<?>) args[1] };
	}

	/**
	 * Fields declared on the class itself. Inherited fields are excluded deliberately: uuid,
	 * creationDate and changeDate are copied by AdoDtoHelper, not by the per-entity helper, so
	 * counting them would report gaps that are not gaps.
	 */
	private static Set<String> declaredDataFields(Class<?> type) {

		Set<String> names = new LinkedHashSet<>();
		for (Field field : type.getDeclaredFields()) {
			if (field.isSynthetic()) {
				continue;
			}
			if (Modifier.isStatic(field.getModifiers())) {
				continue; // serialVersionUID and constants are not data
			}
			if (HANDLED_BY_BASE_CLASS.contains(field.getName())) {
				continue;
			}
			names.add(field.getName());
		}
		return names;
	}

	private static String readSource(Class<?> helperClass) {

		String relative = helperClass.getName().replace('.', '/') + ".java";
		for (String root : CANDIDATE_SOURCE_ROOTS) {
			Path candidate = Paths.get(root, relative);
			if (Files.isReadable(candidate)) {
				try {
					return new String(Files.readAllBytes(candidate), StandardCharsets.UTF_8);
				} catch (IOException e) {
					throw new UncheckedIOException("Could not read " + candidate, e);
				}
			}
		}
		throw new IllegalStateException(
			"Could not find the source of " + helperClass.getName() + ". Looked for " + relative + " under " + java.util.Arrays.toString(CANDIDATE_SOURCE_ROOTS)
				+ " relative to " + Paths.get("").toAbsolutePath() + ". The harness reads source because helper methods cannot be executed without a database.");
	}

	/** Pulls out one method's body and the name of its first parameter, which is always the target. */
	private static Method readMethod(Class<?> helperClass, String source, String methodName) {

		Matcher signature = Pattern.compile("void\\s+" + Pattern.quote(methodName) + "\\s*\\(([^)]*)\\)\\s*\\{").matcher(source);
		if (!signature.find()) {
			throw new IllegalStateException(
				helperClass.getName() + " has no " + methodName + " that the harness can read. It may be formatted unusually. "
					+ "Fix the harness rather than excluding this helper.");
		}

		String firstParameter = firstParameterName(helperClass, methodName, signature.group(1));
		return new Method(firstParameter, bodyFrom(helperClass, methodName, source, signature.end() - 1));
	}

	private static String firstParameterName(Class<?> helperClass, String methodName, String parameterList) {

		String first = parameterList.split(",")[0].trim();
		String[] parts = first.split("\\s+");
		if (parts.length < 2) {
			throw new IllegalStateException(helperClass.getName() + '.' + methodName + " has an unreadable parameter list: " + parameterList);
		}
		return parts[parts.length - 1];
	}

	private static String bodyFrom(Class<?> helperClass, String methodName, String source, int openingBrace) {

		int depth = 0;
		for (int i = openingBrace; i < source.length(); i++) {
			char c = source.charAt(i);
			if (c == '{') {
				depth++;
			} else if (c == '}') {
				depth--;
				if (depth == 0) {
					return source.substring(openingBrace, i + 1);
				}
			}
		}
		throw new IllegalStateException(helperClass.getName() + '.' + methodName + " has unbalanced braces, so its body cannot be read.");
	}

	/** Of the fields given, those the method assigns on its target parameter. */
	private static Set<String> assignedFields(Method method, Set<String> candidates) {

		Set<String> assigned = new LinkedHashSet<>();
		for (String field : candidates) {
			String setter = Pattern.quote(method.target) + "\\s*\\.\\s*set" + Pattern.quote(capitalise(field)) + "\\s*\\(";
			if (Pattern.compile(setter).matcher(method.body).find()) {
				assigned.add(field);
			}
		}
		return assigned;
	}

	private static String capitalise(String field) {
		return Character.toUpperCase(field.charAt(0)) + field.substring(1);
	}

	private static final class Method {

		private final String target;
		final String body;

		private Method(String target, String body) {
			this.target = target;
			this.body = body;
		}
	}
}
