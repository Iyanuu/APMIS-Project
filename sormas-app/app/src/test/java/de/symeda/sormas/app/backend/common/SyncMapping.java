package de.symeda.sormas.app.backend.common;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * What {@link SyncMappingHarness} found out about one entity's sync mapping.
 *
 * <p>
 * Fields are compared in both directions because the copying is done by two separate hand written
 * methods. A field with a line in only one of them loses data silently in the other direction.
 */
public final class SyncMapping {

	private final Class<?> entityClass;
	private final Class<?> dtoClass;
	private final Set<String> entityFields;
	private final Set<String> dtoFields;
	private final Set<String> copiedToEntity;
	private final Set<String> copiedToDto;
	private final boolean pushSupported;

	SyncMapping(
		Class<?> entityClass,
		Class<?> dtoClass,
		Set<String> entityFields,
		Set<String> dtoFields,
		Set<String> copiedToEntity,
		Set<String> copiedToDto,
		boolean pushSupported) {

		this.entityClass = entityClass;
		this.dtoClass = dtoClass;
		this.entityFields = Collections.unmodifiableSet(new LinkedHashSet<>(entityFields));
		this.dtoFields = Collections.unmodifiableSet(new LinkedHashSet<>(dtoFields));
		this.copiedToEntity = Collections.unmodifiableSet(new LinkedHashSet<>(copiedToEntity));
		this.copiedToDto = Collections.unmodifiableSet(new LinkedHashSet<>(copiedToDto));
		this.pushSupported = pushSupported;
	}

	/**
	 * Whether the phone ever sends this entity back. Seven of the sixteen synced entities are pull
	 * only - reference data such as regions, districts and campaigns - and their fillInnerFromAdo
	 * throws UnsupportedOperationException. For those, a field "not reaching the server" is correct
	 * behaviour rather than a gap.
	 */
	public boolean isPushSupported() {
		return pushSupported;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public Class<?> getDtoClass() {
		return dtoClass;
	}

	/** Fields declared on the phone entity itself, excluding anything inherited. */
	public Set<String> getEntityFields() {
		return entityFields;
	}

	/** Fields declared on the server DTO itself, excluding anything inherited. */
	public Set<String> getDtoFields() {
		return dtoFields;
	}

	/**
	 * Fields present on both sides. A field on the DTO but not the entity is not a problem - the app
	 * simply does not model it. These are the ones where a missing copy line means lost data.
	 */
	public Set<String> getSharedFields() {
		Set<String> shared = new TreeSet<>(entityFields);
		shared.retainAll(dtoFields);
		return Collections.unmodifiableSet(shared);
	}

	/** Shared fields that fillInnerFromDto copies, so server changes reach the phone. */
	public Set<String> getCopiedToEntity() {
		return copiedToEntity;
	}

	/** Shared fields that fillInnerFromAdo copies, so phone edits reach the server. */
	public Set<String> getCopiedToDto() {
		return copiedToDto;
	}

	/** Shared fields the server sends but the phone never stores. */
	public Set<String> getNotCopiedToEntity() {
		Set<String> gap = new TreeSet<>(getSharedFields());
		gap.removeAll(copiedToEntity);
		return Collections.unmodifiableSet(gap);
	}

	/**
	 * Shared fields the phone holds but never sends back. Empty for a pull-only entity, where not
	 * sending is the intended behaviour rather than a defect.
	 */
	public Set<String> getNotCopiedToDto() {
		if (!pushSupported) {
			return Collections.emptySet();
		}
		Set<String> gap = new TreeSet<>(getSharedFields());
		gap.removeAll(copiedToDto);
		return Collections.unmodifiableSet(gap);
	}

	/**
	 * A description suitable for a test failure message. Names the direction, because "field not
	 * copied" is not actionable without knowing which way round.
	 */
	public String describeGaps() {
		StringBuilder sb = new StringBuilder();
		sb.append(entityClass.getSimpleName()).append(" <-> ").append(dtoClass.getSimpleName()).append('\n');
		sb.append("  fields on both sides: ").append(getSharedFields().size()).append('\n');
		if (!pushSupported) {
			sb.append("  pull only - the phone never sends this entity, so no phone -> server gaps apply\n");
		}
		if (!getNotCopiedToEntity().isEmpty()) {
			sb.append("  server -> phone, NOT copied by fillInnerFromDto: ").append(getNotCopiedToEntity()).append('\n');
		}
		if (!getNotCopiedToDto().isEmpty()) {
			sb.append("  phone -> server, NOT copied by fillInnerFromAdo: ").append(getNotCopiedToDto()).append('\n');
		}
		if (getNotCopiedToEntity().isEmpty() && getNotCopiedToDto().isEmpty()) {
			sb.append("  no gaps\n");
		}
		return sb.toString();
	}
}
