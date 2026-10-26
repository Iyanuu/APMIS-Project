package de.symeda.sormas.app.backend.campaign;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;

import org.junit.Test;

import de.symeda.sormas.app.backend.common.SyncMapping;
import de.symeda.sormas.app.backend.common.SyncMappingHarness;

/**
 * Checks that {@link CampaignDtoHelper} copies each field that {@code CampaignDto} and {@link Campaign} share, in each
 * direction of the sync (#46).
 *
 * <p>
 * If a test fails, a field is not copied. Add the copy line to the helper. If the field must not be copied, add it to the
 * list below with the reason.
 *
 * <p>
 * Campaigns are pull only. The phone receives them and never sends them back.
 */
public class CampaignSyncMappingTest {

	private static final SyncMapping MAPPING = SyncMappingHarness.analyse(CampaignDtoHelper.class);

	/** Shared fields that fillInnerFromDto does not copy, with the reason for each. */
	private static final Map<String, String> NOT_COPIED_TO_PHONE = Map.of(
		"archived",
		"The phone removes a campaign when the server archives it. The sync calls deleteInvalid with "
			+ "the uuid list from the server, which contains only active campaigns "
			+ "(CampaignService.createActiveCampaignsFilterForMobile).");

	@Test
	public void isPullOnly() {
		assertFalse(MAPPING.isPushSupported());
	}

	@Test
	public void pullCopiesEachSharedField() {
		assertEquals(MAPPING.describeGaps(), NOT_COPIED_TO_PHONE.keySet(), MAPPING.getNotCopiedToEntity());
	}
}
