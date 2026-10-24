package de.symeda.sormas.app.backend.campaign.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import de.symeda.sormas.app.backend.common.SyncMapping;
import de.symeda.sormas.app.backend.common.SyncMappingHarness;

/**
 * Checks that {@link CampaignFormDataDtoHelper} copies each field that {@code CampaignFormDataDto} and {@link CampaignFormData} share, in each
 * direction of the sync (#47).
 *
 * <p>
 * If a test fails, a field is not copied. Add the copy line to the helper. If the field must not be copied, add it to the
 * list below with the reason.
 *
 * <p>
 * The DTO field {@code source} and the entity field {@code soruce} have different names, so the harness does not
 * compare them. The helper does not copy them in either direction. On a push, the server sets {@code source} to
 * MOBILE (CampaignFormDataFacadeEjb.saveCampaignFormDataMobile).
 */
public class CampaignFormDataSyncMappingTest {

	private static final SyncMapping MAPPING = SyncMappingHarness.analyse(CampaignFormDataDtoHelper.class);

	/** Shared fields that fillInnerFromDto does not copy, with the reason for each. */
	private static final Map<String, String> NOT_COPIED_TO_PHONE = Map.of(
		"archived",
		"The app does not use this field. No query or screen reads it.");

	/** Shared fields that fillInnerFromAdo does not copy, with the reason for each. */
	private static final Map<String, String> NOT_COPIED_TO_SERVER = Map.of(
		"archived",
		"The server ignores this field on a push. "
			+ "CampaignFormDataFacadeEjb.fromDtoMobile does not copy it.");

	@Test
	public void pushIsSupported() {
		assertTrue(MAPPING.isPushSupported());
	}

	@Test
	public void pullCopiesEachSharedField() {
		assertEquals(MAPPING.describeGaps(), NOT_COPIED_TO_PHONE.keySet(), MAPPING.getNotCopiedToEntity());
	}

	@Test
	public void pushCopiesEachSharedField() {
		assertEquals(MAPPING.describeGaps(), NOT_COPIED_TO_SERVER.keySet(), MAPPING.getNotCopiedToDto());
	}
}
