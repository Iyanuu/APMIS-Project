package de.symeda.sormas.app.backend.common;

import java.util.List;

import de.symeda.sormas.api.PushResult;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.app.backend.region.Area;
import de.symeda.sormas.app.rest.NoConnectionException;
import retrofit2.Call;

/**
 * A real helper, compiled by the test build, whose mapping is known by construction.
 *
 * <p>
 * Asserting the harness against production helpers only proves it agrees with whoever wrote the test.
 * This class exists so the expected answer is decided here, in code, and the harness has to match it.
 *
 * <p>
 * {@link Area} declares exactly two fields, {@code name} and {@code externalId}, which keeps the
 * expected sets small enough to state exhaustively. The copying below is deliberately lopsided:
 *
 * <ul>
 * <li>server to phone copies <b>name only</b> - so {@code externalId} must be reported as a gap
 * <li>phone to server copies <b>externalId only</b> - so {@code name} must be reported as a gap
 * <li>each direction also contains a call written onto the wrong object, which must <b>not</b> be
 * credited
 * </ul>
 *
 * If the harness reports anything other than that, it is wrong.
 */
public class KnownMappingHelper extends AdoDtoHelper<Area, AreaDto> {

	@Override
	protected Class<Area> getAdoClass() {
		return Area.class;
	}

	@Override
	protected Class<AreaDto> getDtoClass() {
		return AreaDto.class;
	}

	@Override
	protected Call<List<AreaDto>> pullAllSince(long since) throws NoConnectionException {
		throw new UnsupportedOperationException("fixture");
	}

	@Override
	protected Call<List<AreaDto>> pullByUuids(List<String> uuids) throws NoConnectionException {
		throw new UnsupportedOperationException("fixture");
	}

	@Override
	protected Call<List<PushResult>> pushAll(List<AreaDto> dtos) throws NoConnectionException {
		throw new UnsupportedOperationException("fixture");
	}

	@Override
	protected void fillInnerFromDto(Area area, AreaDto dto) {

		area.setName(dto.getName());

		// Written onto the source rather than the target. A real mistake, and the harness must not
		// count it as externalId having reached the entity.
		dto.setExternalId(area.getExternalId());
	}

	@Override
	protected void fillInnerFromAdo(AreaDto dto, Area area) {

		dto.setExternalId(area.getExternalId());

		// Same mistake in the other direction: this does not mean name reached the DTO.
		area.setName(dto.getName());
	}
}
