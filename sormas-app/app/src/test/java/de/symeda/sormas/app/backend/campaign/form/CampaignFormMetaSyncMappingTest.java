package de.symeda.sormas.app.backend.campaign.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;

import org.junit.Test;

import de.symeda.sormas.app.backend.common.SyncMapping;
import de.symeda.sormas.app.backend.common.SyncMappingHarness;

/**
 * Checks that {@link CampaignFormMetaDtoHelper} copies each field that {@code CampaignFormMetaDto} and {@link CampaignFormMeta} share, in each
 * direction of the sync (#48).
 *
 * <p>
 * If a test fails, a field is not copied. Add the copy line to the helper. If the field must not be copied, add it to the
 * list below with the reason.
 *
 * <p>
 * Form metas are pull only. The phone receives them and never sends them back.
 * The DTO field {@code geographyLevel} and the entity field {@code geographylevel} have different names, so the
 * harness does not compare them. The helper copies them.
 */
public class CampaignFormMetaSyncMappingTest {

	private static final SyncMapping MAPPING = SyncMappingHarness.analyse(CampaignFormMetaDtoHelper.class);

	/** Shared fields that fillInnerFromDto does not copy, with the reason for each. */
	private static final Map<String, String> NOT_COPIED_TO_PHONE = Map.of();

	@Test
	public void isPullOnly() {
		assertFalse(MAPPING.isPushSupported());
	}

	@Test
	public void pullCopiesEachSharedField() {
		assertEquals(MAPPING.describeGaps(), NOT_COPIED_TO_PHONE.keySet(), MAPPING.getNotCopiedToEntity());
	}
}
