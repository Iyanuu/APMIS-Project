package de.symeda.sormas.api.campaign.form;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Covers {@link CampaignFormElementType}, the element types in a campaign form definition.
 *
 * <p>
 * The form definition gives the type as text, for example {@code "yes-no"}. The server and the mobile app change this text
 * to the enum with {@link CampaignFormElementType#fromString}, and the enum gives the text back with
 * {@link CampaignFormElementType#toString}.
 */
public class CampaignFormElementTypeTest {

	@Test
	public void eachTypeComesBackFromItsText() {
		for (CampaignFormElementType type : CampaignFormElementType.values()) {
			assertThat(type.name(), CampaignFormElementType.fromString(type.toString()), is(type));
		}
	}

	@Test
	public void textIsLowerCaseWithAHyphen() {
		assertThat(CampaignFormElementType.YES_NO.toString(), is("yes-no"));
		assertThat(CampaignFormElementType.NUMBER.toString(), is("number"));
		assertThat(CampaignFormElementType.VALIDATEDTEXT.toString(), is("validatedtext"));
	}

	@Test
	public void upperCaseAndUnderscoreAreAlsoRead() {
		assertThat(CampaignFormElementType.fromString("NUMBER"), is(CampaignFormElementType.NUMBER));
		assertThat(CampaignFormElementType.fromString("yes_no"), is(CampaignFormElementType.YES_NO));
		assertThat(CampaignFormElementType.fromString("Yes-No"), is(CampaignFormElementType.YES_NO));
	}

	@Test(expected = IllegalArgumentException.class)
	public void unknownTypeStopsWithAnError() {
		CampaignFormElementType.fromString("slider");
	}

	@Test
	public void onlyYesNoHasAllowedValues() {
		assertThat(CampaignFormElementType.YES_NO.getAllowedValues(), arrayContaining("yes", "no", "true", "false"));
		for (CampaignFormElementType type : CampaignFormElementType.values()) {
			if (type != CampaignFormElementType.YES_NO) {
				assertThat(type.name(), type.getAllowedValues(), is(emptyArray()));
			}
		}
	}
}
