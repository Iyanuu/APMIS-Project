package de.symeda.sormas.app.backend.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.app.backend.campaign.Campaign;
import de.symeda.sormas.app.backend.campaign.CampaignDtoHelper;
import de.symeda.sormas.app.backend.campaign.data.CampaignFormDataDtoHelper;
import de.symeda.sormas.app.backend.region.AreaDtoHelper;
import de.symeda.sormas.app.backend.user.UserDtoHelper;

/**
 * Proves the harness reads helpers correctly before any per-entity test relies on it.
 *
 * <p>
 * The cases that matter are the ones where an earlier attempt went wrong: helpers whose parameters
 * are named differently, and helpers the harness cannot interpret. The second must throw rather than
 * return an empty result, because a silent skip looks identical to a clean pass.
 */
public class SyncMappingHarnessTest {

	@Test
	public void readsTheEntityAndDtoFromTheClassDeclaration() {

		SyncMapping mapping = SyncMappingHarness.analyse(CampaignDtoHelper.class);

		assertEquals(Campaign.class, mapping.getEntityClass());
		assertEquals(CampaignDto.class, mapping.getDtoClass());
	}

	@Test
	public void findsFieldsOnBothSides() {

		SyncMapping mapping = SyncMappingHarness.analyse(CampaignDtoHelper.class);

		assertFalse("expected the entity to declare fields", mapping.getEntityFields().isEmpty());
		assertFalse("expected the DTO to declare fields", mapping.getDtoFields().isEmpty());
		assertFalse("expected some fields on both sides", mapping.getSharedFields().isEmpty());

		for (String shared : mapping.getSharedFields()) {
			assertTrue(shared + " should be on the entity", mapping.getEntityFields().contains(shared));
			assertTrue(shared + " should be on the DTO", mapping.getDtoFields().contains(shared));
		}
	}

	@Test
	public void excludesFieldsTheBaseClassCopies() {

		SyncMapping mapping = SyncMappingHarness.analyse(CampaignDtoHelper.class);

		// AdoDtoHelper copies these in both directions, so counting them would report gaps that
		// are not gaps.
		assertFalse(mapping.getEntityFields().contains("uuid"));
		assertFalse(mapping.getEntityFields().contains("creationDate"));
		assertFalse(mapping.getEntityFields().contains("changeDate"));
	}

	@Test
	public void excludesConstantsAndSerialVersionUid() {

		SyncMapping mapping = SyncMappingHarness.analyse(CampaignDtoHelper.class);

		assertFalse(mapping.getDtoFields().contains("serialVersionUID"));
		for (String field : mapping.getDtoFields()) {
			assertFalse("constants are not data: " + field, field.equals(field.toUpperCase()));
		}
	}

	@Test
	public void readsHelpersWhoseParametersAreNamedDifferently() {

		// CampaignDtoHelper uses (target, source); AreaDtoHelper uses (area, dto); UserDtoHelper
		// differs again. An earlier attempt assumed "target" and silently missed 19 of 59 helpers.
		for (Class<?> helper : new Class<?>[] {
			CampaignDtoHelper.class,
			AreaDtoHelper.class,
			UserDtoHelper.class }) {

			@SuppressWarnings("unchecked")
			SyncMapping mapping = SyncMappingHarness.analyse((Class<? extends AdoDtoHelper<?, ?>>) helper);

			assertFalse(
				helper.getSimpleName() + ": found no copied fields at all, which means the parameter name was not read correctly",
				mapping.getCopiedToEntity().isEmpty() && mapping.getCopiedToDto().isEmpty());
		}
	}

	@Test
	public void reportsBothDirectionsSeparately() {

		SyncMapping mapping = SyncMappingHarness.analyse(CampaignDtoHelper.class);

		// The two directions are separate hand written methods, so they can disagree. The harness
		// must not collapse them into one answer.
		assertEquals(mapping.getSharedFields().size(), mapping.getCopiedToEntity().size() + mapping.getNotCopiedToEntity().size());
		assertEquals(mapping.getSharedFields().size(), mapping.getCopiedToDto().size() + mapping.getNotCopiedToDto().size());
	}

	@Test
	public void throwsRatherThanSkippingWhenItCannotReadAHelper() {

		try {
			// Not a helper at all, so it has no AdoDtoHelper type arguments to read.
			@SuppressWarnings("unchecked")
			Class<? extends AdoDtoHelper<?, ?>> notAHelper = (Class<? extends AdoDtoHelper<?, ?>>) (Class<?>) String.class;
			SyncMappingHarness.analyse(notAHelper);
			fail("expected the harness to throw, because skipping a helper it cannot read is how gaps get missed");
		} catch (IllegalStateException expected) {
			assertTrue(
				"the message should say what to do about it, not just that it failed",
				expected.getMessage().contains("Do not skip it") || expected.getMessage().contains("cannot be determined"));
		}
	}

	@Test
	public void detectsAPullOnlyEntity() {

		// CampaignDtoHelper.fillInnerFromAdo throws UnsupportedOperationException: campaigns come
		// down from the server and are never sent back. Without this, the harness reports all 11
		// shared fields as failing to reach the server, which is the opposite of true.
		SyncMapping campaign = SyncMappingHarness.analyse(CampaignDtoHelper.class);

		assertFalse("Campaign is pull only", campaign.isPushSupported());
		assertTrue("a pull-only entity has no phone -> server gaps by definition", campaign.getNotCopiedToDto().isEmpty());
		assertTrue(campaign.describeGaps().contains("pull only"));
	}

	@Test
	public void stillChecksThePushDirectionWhereItIsSupported() {

		SyncMapping formData = SyncMappingHarness.analyse(CampaignFormDataDtoHelper.class);

		// CampaignFormData is the one entity field workers create, so it must push.
		assertTrue("CampaignFormData must support push", formData.isPushSupported());
	}

	@Test
	public void describesGapsInAWayThatNamesTheDirection() {

		String description = SyncMappingHarness.analyse(CampaignDtoHelper.class).describeGaps();

		assertTrue(description.contains("Campaign"));
		assertTrue("a gap report is not actionable without the direction", description.contains("no gaps") || description.contains("->"));
	}
}
