package de.symeda.sormas.app.backend.campaign.data;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.app.util.YesNo;

/**
 * Tests that campaign form values come back unchanged after a save and a reload.
 *
 * <p>
 * The local database keeps all form values of a record in one column, as JSON text. {@link CampaignFormData#setFormValues}
 * writes the JSON with Gson, and {@link CampaignFormData#getFormValues} reads it back after the record loads. The database
 * stores the text without changes. Thus, this conversion is the part that can change a value.
 *
 * <p>
 * Text, number, decimal, range, time and dropdown fields give text. Check boxes give a Boolean. The save path changes dates
 * to text before the save. These values must come back unchanged.
 *
 * <p>
 * The value of an entry is an Object. When Gson reads the JSON, it does not know the original type, so some other types
 * change. The last tests in this class record these changes. If one of those tests fails, the conversion changed. Make
 * sure that the change is intended.
 */
public class CampaignFormDataRoundTripTest {

	/**
	 * Saves the entries in the same way as the database, then loads them into a new record.
	 */
	private static List<CampaignFormDataEntry> saveAndReload(CampaignFormDataEntry... entries) {
		CampaignFormData saved = new CampaignFormData();
		saved.setFormValues(new ArrayList<>(Arrays.asList(entries)));

		CampaignFormData loaded = new CampaignFormData();
		loaded.setFormValuesJson(saved.getFormValuesJson());
		return loaded.getFormValues();
	}

	private static Object saveAndReloadValue(Object value) {
		return saveAndReload(new CampaignFormDataEntry("field", value)).get(0).getValue();
	}

	@Test
	public void textComesBackUnchanged() {
		assertThat(saveAndReloadValue("Kabul"), is((Object) "Kabul"));
		assertThat(saveAndReloadValue("  spaces before and after  "), is((Object) "  spaces before and after  "));
	}

	@Test
	public void numbersEnteredAsTextKeepTheirExactForm() {
		// Number, decimal and range fields give text. Leading zeros, trailing zeros and signs must stay.
		for (String number : Arrays.asList("0", "007", "2.50", "-1", "1000000")) {
			Object reloaded = saveAndReloadValue(number);
			assertThat(reloaded, instanceOf(String.class));
			assertThat(reloaded, is((Object) number));
		}
	}

	@Test
	public void dateAndTimeTextComesBackUnchanged() {
		// CampaignFormDataEditActivity changes date values to dd-MM-yyyy text before the save.
		assertThat(saveAndReloadValue("24-10-2026"), is((Object) "24-10-2026"));
		assertThat(saveAndReloadValue("13:45"), is((Object) "13:45"));
	}

	@Test
	public void zeroEmptyTextAndNullStayDifferent() {
		List<CampaignFormDataEntry> reloaded = saveAndReload(
			new CampaignFormDataEntry("zero", "0"),
			new CampaignFormDataEntry("empty", ""),
			new CampaignFormDataEntry("notAnswered", null));

		assertThat(reloaded.get(0).getValue(), is((Object) "0"));
		assertThat(reloaded.get(1).getValue(), is((Object) ""));
		assertThat(reloaded.get(2).getValue(), is(nullValue()));
	}

	@Test
	public void dariAndPashtoTextComesBackUnchanged() {
		// Dari "واکسین" (vaccine) and Pashto "ټول ماشومان" (all children). The Pashto text contains the letter ټ, which Dari does not use.
		String dari = "واکسین";
		String pashto = "ټول ماشومان";

		assertThat(saveAndReloadValue(dari), is((Object) dari));
		assertThat(saveAndReloadValue(pashto), is((Object) pashto));
	}

	@Test
	public void longTextWithSpecialCharactersComesBackUnchanged() {
		// Quotes, backslashes, tabs and line breaks need escapes in JSON.
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < 500; i++) {
			text.append("line ").append(i).append("\t\"quoted\" \\ <tag> & end\n");
		}

		assertThat(saveAndReloadValue(text.toString()), is((Object) text.toString()));
	}

	@Test
	public void checkBoxValuesComeBackUnchanged() {
		assertThat(saveAndReloadValue(Boolean.TRUE), is((Object) Boolean.TRUE));
		assertThat(saveAndReloadValue(Boolean.FALSE), is((Object) Boolean.FALSE));
	}

	@Test
	public void entriesKeepTheirIdsAndOrder() {
		List<CampaignFormDataEntry> reloaded = saveAndReload(
			new CampaignFormDataEntry("c", "3"),
			new CampaignFormDataEntry("a", "1"),
			new CampaignFormDataEntry("b", "2"));

		List<String> ids = new ArrayList<>();
		for (CampaignFormDataEntry entry : reloaded) {
			ids.add(entry.getId());
		}
		assertThat(ids, contains("c", "a", "b"));
	}

	@Test
	public void recordWithoutValuesGivesAnEmptyList() {
		CampaignFormData neverSaved = new CampaignFormData();
		neverSaved.setFormValuesJson(null);
		assertThat(neverSaved.getFormValues(), is(empty()));

		assertThat(saveAndReload(), is(empty()));
	}

	// The tests below record values that change type. The form fields do not give these types today.

	@Test
	public void wholeNumberComesBackAsDecimal() {
		// Gson reads each JSON number into an Object field as a Double.
		assertThat(saveAndReloadValue(5), is((Object) 5.0d));
		assertThat(saveAndReloadValue(0), is((Object) 0.0d));
	}

	@Test
	public void yesNoComesBackAsText() {
		// ControlSwitchField.setValue reads "YES" and "NO" as text, so the switch still shows the correct answer.
		Object reloaded = saveAndReloadValue(YesNo.YES);
		assertThat(reloaded, is((Object) "YES"));
		assertThat(reloaded, not(instanceOf(YesNo.class)));
	}

	@Test
	public void dateObjectComesBackAsText() {
		// The text format depends on the device locale. The save path changes dates to dd-MM-yyyy text first, so
		// a Date object does not usually reach this conversion.
		Object reloaded = saveAndReloadValue(new Date(0));
		assertThat(reloaded, instanceOf(String.class));
	}
}
