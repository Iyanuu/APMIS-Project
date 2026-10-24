package de.symeda.sormas.api.campaign.data;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;

/**
 * Covers {@link CampaignFormDataEntry}, one value of a campaign form: the id of the form element and its value.
 *
 * <p>
 * The server calls {@link CampaignFormDataEntry#removeNullValueEntries} when it saves campaign form data, also the data
 * that the phones push (CampaignFormDataFacadeEjb). {@link CampaignFormDataEntry#toString} gives the value as text.
 *
 * <p>
 * The test with {@code @Ignore("#104 ...")} shows a wrong result today. When #104 is fixed, remove {@code @Ignore}.
 */
public class CampaignFormDataEntryTest {

	private static List<String> idsLeftAfterTheCleanup(CampaignFormDataEntry... entries) {
		List<CampaignFormDataEntry> list = new ArrayList<>(Arrays.asList(entries));
		CampaignFormDataEntry.removeNullValueEntries(list);
		return list.stream().map(CampaignFormDataEntry::getId).collect(Collectors.toList());
	}

	@Test
	public void cleanupRemovesEntriesWithNoValue() {
		assertThat(
			idsLeftAfterTheCleanup(new CampaignFormDataEntry("a", null), new CampaignFormDataEntry("b", "1"), new CampaignFormDataEntry("c", null)),
			contains("b"));
	}

	@Test
	public void cleanupKeepsEmptyTextZeroAndFalse() {
		assertThat(
			idsLeftAfterTheCleanup(
				new CampaignFormDataEntry("empty", ""),
				new CampaignFormDataEntry("zero", "0"),
				new CampaignFormDataEntry("no", Boolean.FALSE)),
			contains("empty", "zero", "no"));
	}

	@Ignore("#104: the cleanup compares the text \"null\" by reference, so it keeps the text that comes from JSON")
	@Test
	public void cleanupRemovesTheTextNullFromJson() {
		// Jackson and Gson make a new String for each value. It is equal to "null", but it is not the same object.
		String nullTextFromJson = new String("null");
		assertThat(idsLeftAfterTheCleanup(new CampaignFormDataEntry("a", nullTextFromJson), new CampaignFormDataEntry("b", "1")), contains("b"));
	}

	@Test
	public void noValueIsShownAsEmptyText() {
		assertThat(new CampaignFormDataEntry("a", null).toString(), is(""));
	}

	@Test
	public void textAndNumbersAreShownAsTheyAre() {
		assertThat(new CampaignFormDataEntry("a", "Kabul").toString(), is("Kabul"));
		assertThat(new CampaignFormDataEntry("a", "12").toString(), is("12"));
		assertThat(new CampaignFormDataEntry("a", 2.5).toString(), is("2.5"));
	}

	@Test
	public void trueAndFalseAreShownAsYesAndNo() {
		assertThat(new CampaignFormDataEntry("a", Boolean.TRUE).toString(), is(I18nProperties.getString(Strings.yes)));
		assertThat(new CampaignFormDataEntry("a", Boolean.FALSE).toString(), is(I18nProperties.getString(Strings.no)));
	}

	@Test
	public void digitsLongerThanElevenCharactersAreShownAsADate() {
		// The value is read as milliseconds since 1970, in the time zone of the server.
		TimeZone before = TimeZone.getDefault();
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			assertThat(new CampaignFormDataEntry("a", "1761264000000").toString(), is("2025/10/24"));
			// 12 digits are also shown as a date, for example a phone number with the country code and 00 before it.
			assertThat(new CampaignFormDataEntry("a", "009370123456").toString(), not(is("009370123456")));
			// 11 digits or fewer stay as they are.
			assertThat(new CampaignFormDataEntry("a", "93701234567").toString(), is("93701234567"));
		} finally {
			TimeZone.setDefault(before);
		}
	}

	@Test
	public void entriesWithTheSameIdAndValueAreEqual() {
		assertThat(new CampaignFormDataEntry("a", "1"), is(new CampaignFormDataEntry("a", "1")));
		assertThat(new CampaignFormDataEntry("a", "1").hashCode(), is(new CampaignFormDataEntry("a", "1").hashCode()));
		assertThat(new CampaignFormDataEntry("a", "1"), not(is(new CampaignFormDataEntry("a", "2"))));
		assertThat(new CampaignFormDataEntry("a", "1"), not(is(new CampaignFormDataEntry("b", "1"))));
	}
}
