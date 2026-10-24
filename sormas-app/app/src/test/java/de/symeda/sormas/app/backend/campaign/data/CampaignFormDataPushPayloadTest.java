package de.symeda.sormas.app.backend.campaign.data;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMeta;
import de.symeda.sormas.app.rest.RetroProvider;

/**
 * Tests that each campaign form value saved on the phone is in the JSON that the push sends to the server.
 *
 * <p>
 * The save and the push are two different pieces of code. If the push leaves out a value, the field worker sees the value
 * on the phone, but the server never gets it.
 *
 * <p>
 * Each test saves a record, loads it back, converts it with {@link CampaignFormDataDtoHelper#adoToDto} as the push does,
 * and writes the DTO with the Gson configuration of {@link RetroProvider#initGson()}. Then the test compares the JSON with
 * the saved values.
 */
public class CampaignFormDataPushPayloadTest {

	private static final Gson PUSH_GSON = RetroProvider.initGson();

	private static final String RECORD_UUID = "RECORD-UUID-0000000000001";
	private static final String FORM_META_UUID = "FORM-META-UUID-000000001";

	/**
	 * Saves the entries in the same way as the database, then loads them into a new record that has a form meta.
	 */
	private static CampaignFormData savedRecord(CampaignFormDataEntry... entries) {
		CampaignFormData saved = new CampaignFormData();
		saved.setFormValues(new ArrayList<>(Arrays.asList(entries)));

		CampaignFormMeta formMeta = new CampaignFormMeta();
		formMeta.setUuid(FORM_META_UUID);

		CampaignFormData loaded = new CampaignFormData();
		loaded.setUuid(RECORD_UUID);
		loaded.setCreationDate(new Date(1_700_000_000_000L));
		loaded.setChangeDate(new Date(1_700_000_100_000L));
		loaded.setCampaignFormMeta(formMeta);
		// The New and Edit activities set these flags before the save. The push fails when they are null.
		loaded.setIsverified(false);
		loaded.setIspublished(false);
		loaded.setFormValuesJson(saved.getFormValuesJson());
		return loaded;
	}

	/**
	 * Builds the JSON that the push sends for one record.
	 */
	private static JsonObject pushPayload(CampaignFormData record) {
		CampaignFormDataDto dto = new CampaignFormDataDtoHelper().adoToDto(record);
		return PUSH_GSON.toJsonTree(dto).getAsJsonObject();
	}

	/**
	 * Gives the form values of the JSON, by id, in their order. A value that is not in the JSON is JSON null.
	 */
	private static Map<String, JsonElement> formValuesOf(JsonObject payload) {
		Map<String, JsonElement> values = new LinkedHashMap<>();
		for (JsonElement element : payload.getAsJsonArray("formValues")) {
			JsonObject entry = element.getAsJsonObject();
			JsonElement value = entry.has("value") ? entry.get("value") : JsonNull.INSTANCE;
			values.put(entry.get("id").getAsString(), value);
		}
		return values;
	}

	/**
	 * Fails if a saved entry is not in the JSON, or if its value in the JSON is different.
	 */
	private static void assertEachSavedValueIsInThePayload(CampaignFormData record, JsonObject payload) {
		Map<String, JsonElement> sent = formValuesOf(payload);

		for (CampaignFormDataEntry entry : record.getFormValues()) {
			assertTrue("The push does not send the entry \"" + entry.getId() + "\"", sent.containsKey(entry.getId()));
			assertThat("The push sends a different value for \"" + entry.getId() + "\"", sent.get(entry.getId()), is(PUSH_GSON.toJsonTree(entry.getValue())));
		}
		assertThat("The push sends a different number of entries", sent.size(), is(record.getFormValues().size()));
	}

	@Test
	public void eachSavedValueIsInThePayload() {
		CampaignFormData record = savedRecord(
			new CampaignFormDataEntry("text", "Kabul"),
			new CampaignFormDataEntry("zero", "0"),
			new CampaignFormDataEntry("leadingZeros", "007"),
			new CampaignFormDataEntry("decimal", "2.50"),
			new CampaignFormDataEntry("negative", "-1"),
			new CampaignFormDataEntry("date", "24-10-2026"),
			new CampaignFormDataEntry("time", "13:45"),
			new CampaignFormDataEntry("dari", "واکسین"),
			new CampaignFormDataEntry("pashto", "ټول ماشومان"),
			new CampaignFormDataEntry("empty", ""),
			new CampaignFormDataEntry("checkBoxOn", Boolean.TRUE),
			new CampaignFormDataEntry("checkBoxOff", Boolean.FALSE),
			new CampaignFormDataEntry("yesNo", "YES"));

		assertEachSavedValueIsInThePayload(record, pushPayload(record));
	}

	@Test
	public void numbersAndDatesAreSentAsTextWithoutChanges() {
		CampaignFormData record = savedRecord(
			new CampaignFormDataEntry("leadingZeros", "007"),
			new CampaignFormDataEntry("decimal", "2.50"),
			new CampaignFormDataEntry("date", "24-10-2026"));

		Map<String, JsonElement> sent = formValuesOf(pushPayload(record));

		assertThat(sent.get("leadingZeros").getAsString(), is("007"));
		assertThat(sent.get("decimal").getAsString(), is("2.50"));
		assertThat(sent.get("date").getAsString(), is("24-10-2026"));
	}

	@Test
	public void emptyTextIsSentAndIsNotTheSameAsNotAnswered() {
		CampaignFormData record = savedRecord(new CampaignFormDataEntry("empty", ""), new CampaignFormDataEntry("notAnswered", null));

		Map<String, JsonElement> sent = formValuesOf(pushPayload(record));

		assertThat(sent.get("empty").getAsString(), is(""));
		assertThat(sent.get("notAnswered").isJsonNull(), is(true));
	}

	@Test
	public void longTextIsSentWithoutChanges() {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < 500; i++) {
			text.append("line ").append(i).append("\t\"quoted\" \\ <tag> & end\n");
		}
		CampaignFormData record = savedRecord(new CampaignFormDataEntry("comment", text.toString()));

		assertThat(formValuesOf(pushPayload(record)).get("comment").getAsString(), is(text.toString()));
	}

	@Test
	public void payloadIdentifiesTheRecordAndItsForm() {
		CampaignFormData record = savedRecord(new CampaignFormDataEntry("text", "Kabul"));
		record.setFormDate(new Date(1_729_728_000_000L));

		JsonObject payload = pushPayload(record);

		assertThat(payload.get("uuid").getAsString(), is(RECORD_UUID));
		assertThat(payload.getAsJsonObject("campaignFormMeta"), notNullValue());
		assertThat(payload.getAsJsonObject("campaignFormMeta").get("uuid").getAsString(), is(FORM_META_UUID));
		// RetroProvider writes dates as milliseconds.
		assertThat(payload.get("formDate").getAsLong(), is(1_729_728_000_000L));
	}

	@Test
	public void missingValueFailsTheCheck() {
		// Proves that the check can fail: remove one entry from the JSON, as a faulty push would.
		CampaignFormData record = savedRecord(new CampaignFormDataEntry("kept", "1"), new CampaignFormDataEntry("dropped", "2"));
		JsonObject payload = pushPayload(record);
		payload.getAsJsonArray("formValues").remove(1);

		try {
			assertEachSavedValueIsInThePayload(record, payload);
		} catch (AssertionError expected) {
			assertTrue(expected.getMessage().contains("dropped"));
			return;
		}
		throw new AssertionError("The check did not find the missing entry");
	}
}
