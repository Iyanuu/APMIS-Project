package de.symeda.sormas.app.backend.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.app.backend.campaign.Campaign;
import de.symeda.sormas.app.backend.campaign.CampaignDtoHelper;
import de.symeda.sormas.app.backend.campaign.data.CampaignFormDataDtoHelper;
import de.symeda.sormas.app.backend.region.Area;
import de.symeda.sormas.app.backend.region.AreaDtoHelper;
import de.symeda.sormas.app.backend.user.UserDtoHelper;

/**
 * Proves the harness reads compiled helpers correctly before any per-entity test relies on it.
 *
 * <p>
 * The tests that carry the weight are those against {@link KnownMappingHelper}, a real helper compiled
 * by the test build whose mapping is decided here rather than inferred from production code. Anything
 * asserted against a production helper only shows the harness agrees with whoever wrote the test; the
 * fixture shows it reports the right answer.
 */
public class SyncMappingHarnessTest {

	private static Set<String> setOf(String... values) {
		return new TreeSet<>(Arrays.asList(values));
	}

	// --- Against a compiled fixture with a known answer -------------------------------------------

	@Test
	public void reportsExactlyWhatTheCompiledClassCopies() {

		SyncMapping mapping = SyncMappingHarness.analyse(KnownMappingHelper.class);

		assertEquals("Area declares name and externalId; nothing else is shared with AreaDto", setOf("externalId", "name"), mapping.getSharedFields());

		// The fixture copies name downwards and externalId upwards, and nothing else.
		assertEquals(setOf("name"), mapping.getCopiedToEntity());
		assertEquals(setOf("externalId"), mapping.getNotCopiedToEntity());

		assertEquals(setOf("externalId"), mapping.getCopiedToDto());
		assertEquals(setOf("name"), mapping.getNotCopiedToDto());
	}

	@Test
	public void doesNotCreditACopyWrittenOntoTheWrongObject() {

		// The fixture's fillInnerFromDto also contains dto.setExternalId(area.getExternalId()) - the
		// right field, assigned to the wrong object. If the harness counted that, externalId would
		// appear as having reached the entity, and a real mistake would read as success.
		SyncMapping mapping = SyncMappingHarness.analyse(KnownMappingHelper.class);

		assertFalse("externalId was assigned on the DTO, not the entity", mapping.getCopiedToEntity().contains("externalId"));
		assertFalse("name was assigned on the entity, not the DTO", mapping.getCopiedToDto().contains("name"));
	}

	@Test
	public void readsTheEntityAndDtoFromTheCompiledClassDeclaration() {

		SyncMapping fixture = SyncMappingHarness.analyse(KnownMappingHelper.class);
		assertEquals(Area.class, fixture.getEntityClass());
		assertEquals(AreaDto.class, fixture.getDtoClass());

		SyncMapping campaign = SyncMappingHarness.analyse(CampaignDtoHelper.class);
		assertEquals(Campaign.class, campaign.getEntityClass());
		assertEquals(CampaignDto.class, campaign.getDtoClass());
	}

	@Test
	public void excludesFieldsTheBaseClassCopies() {

		SyncMapping mapping = SyncMappingHarness.analyse(KnownMappingHelper.class);

		// AdoDtoHelper copies these in both directions, so counting them would report gaps that are
		// not gaps.
		assertFalse(mapping.getEntityFields().contains("uuid"));
		assertFalse(mapping.getEntityFields().contains("creationDate"));
		assertFalse(mapping.getEntityFields().contains("changeDate"));
	}

	@Test
	public void excludesConstantsAndSerialVersionUid() {

		SyncMapping mapping = SyncMappingHarness.analyse(KnownMappingHelper.class);

		assertFalse(mapping.getDtoFields().contains("serialVersionUID"));
		for (String field : mapping.getDtoFields()) {
			assertFalse("constants are not data: " + field, field.equals(field.toUpperCase()));
		}
	}

	// --- Against the real helpers -----------------------------------------------------------------

	@Test
	public void readsEveryHelperRegardlessOfHowItsParametersAreNamed() {

		// Parameter names differ between helpers and even between the two methods of one helper -
		// target/source, ado/dto, area/dto. Reading bytecode removes that problem, because the owner
		// type identifies the target. These assertions guard against a regression to any approach
		// that depends on the names.
		for (Class<?> helper : new Class<?>[] {
			CampaignDtoHelper.class,
			AreaDtoHelper.class,
			UserDtoHelper.class,
			CampaignFormDataDtoHelper.class }) {

			@SuppressWarnings("unchecked")
			SyncMapping mapping = SyncMappingHarness.analyse((Class<? extends AdoDtoHelper<?, ?>>) helper);

			assertFalse(
				helper.getSimpleName() + ": no copied fields found at all, which means the target type was not matched",
				mapping.getCopiedToEntity().isEmpty() && mapping.getCopiedToDto().isEmpty());
		}
	}

	@Test
	public void detectsAPullOnlyEntity() {

		// CampaignDtoHelper.fillInnerFromAdo throws UnsupportedOperationException: campaigns come
		// down from the server and are never sent back. Without this, the harness reports all its
		// shared fields as failing to reach the server, which is the opposite of true.
		SyncMapping campaign = SyncMappingHarness.analyse(CampaignDtoHelper.class);

		assertFalse("Campaign is pull only", campaign.isPushSupported());
		assertTrue("a pull-only entity has no phone -> server gaps by definition", campaign.getNotCopiedToDto().isEmpty());
		assertTrue(campaign.describeGaps().contains("pull only"));
	}

	@Test
	public void stillChecksThePushDirectionWhereItIsSupported() {

		// CampaignFormData is the one entity field workers create, so it must push.
		assertTrue("CampaignFormData must support push", SyncMappingHarness.analyse(CampaignFormDataDtoHelper.class).isPushSupported());

		// The fixture implements both directions, so it must not be mistaken for pull only.
		assertTrue(SyncMappingHarness.analyse(KnownMappingHelper.class).isPushSupported());
	}

	@Test
	public void reportsBothDirectionsSeparately() {

		SyncMapping mapping = SyncMappingHarness.analyse(KnownMappingHelper.class);

		assertEquals(mapping.getSharedFields().size(), mapping.getCopiedToEntity().size() + mapping.getNotCopiedToEntity().size());

		// The push invariant only holds where push is supported. For a pull-only entity the gap set is
		// deliberately empty, so copied + gap does not account for every shared field.
		if (mapping.isPushSupported()) {
			assertEquals(mapping.getSharedFields().size(), mapping.getCopiedToDto().size() + mapping.getNotCopiedToDto().size());
		} else {
			assertTrue(mapping.getNotCopiedToDto().isEmpty());
		}
	}

	// --- Failing loudly ---------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	private static Class<? extends AdoDtoHelper<?, ?>> notAHelper() {
		return (Class<? extends AdoDtoHelper<?, ?>>) (Class<?>) String.class;
	}

	@Test
	public void throwsWhenTheClassIsNotAHelper() {

		try {
			SyncMappingHarness.analyse(notAHelper());
			fail("expected the harness to throw, because skipping a helper it cannot read is how gaps get missed");
		} catch (IllegalStateException expected) {
			assertTrue(
				"the message should say what to do about it: " + expected.getMessage(),
				expected.getMessage().contains("Do not skip it") || expected.getMessage().contains("cannot be determined"));
		}
	}

	@Test
	public void describesGapsInAWayThatNamesTheDirection() {

		String description = SyncMappingHarness.analyse(KnownMappingHelper.class).describeGaps();

		assertTrue(description.contains("Area"));
		assertTrue("a gap report is not actionable without the direction", description.contains("->"));
		assertTrue("both gaps should be named", description.contains("externalId") && description.contains("name"));
	}
}
