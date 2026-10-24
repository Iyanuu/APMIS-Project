package de.symeda.sormas.app.backend.campaign.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import de.symeda.sormas.app.backend.common.SyncMapping;
import de.symeda.sormas.app.backend.common.SyncMappingHarness;

/**
 * Checks that {@link CampaignFormMetaWithExpDtoHelper} copies each field that {@code CampaignFormMetaExpiryDto} and {@link CampaignFormMetaWithExp} share, in each
 * direction of the sync (#50).
 *
 * <p>
 * If a test fails, a field is not copied. Add the copy line to the helper. If the field must not be copied, add it to the
 * list below with the reason.
 *
 * <p>
 * The DTO field {@code enddate} and the entity field {@code expiryDate} have different names, so the harness
 * does not compare them. The helper copies them in both directions.
 */
public class CampaignFormMetaWithExpSyncMappingTest {

	private static final SyncMapping MAPPING = SyncMappingHarness.analyse(CampaignFormMetaWithExpDtoHelper.class);

	/** Shared fields that fillInnerFromDto does not copy, with the reason for each. */
	private static final Map<String, String> NOT_COPIED_TO_PHONE = Map.of();

	/** Shared fields that fillInnerFromAdo does not copy, with the reason for each. */
	private static final Map<String, String> NOT_COPIED_TO_SERVER = Map.of();

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
