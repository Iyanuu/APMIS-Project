package de.symeda.sormas.app.campaign;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.app.util.YesNo;

/**
 * Tests the calculated fields of campaign forms, and the helpers that find linked range fields.
 *
 * <p>
 * A form element can have an expression, for example {@code a + b}. {@link CampaignFormDataFragmentUtils#getExpressionValue}
 * evaluates it with SpEL against the other form values. {@link CampaignFormDataFragmentUtils#applyExpressionToEntry}
 * stores the result in the form values, and {@link CampaignFormDataFragmentUtils#recalculateAllExpressions} does this for
 * all elements before a save. These stored values go to the server.
 *
 * <p>
 * The tests marked with {@code @Ignore("#102 ...")} show the cases that store a wrong value. When #102 is fixed, remove
 * {@code @Ignore}.
 */
public class CampaignFormDataFragmentUtilsTest {

	private final ExpressionParser parser = new SpelExpressionParser();

	private static CampaignFormElement element(String id, String type, String expression, String... constraints) {
		CampaignFormElement element = new CampaignFormElement();
		element.setId(id);
		element.setType(type);
		element.setExpression(expression);
		if (constraints.length > 0) {
			element.setConstraints(constraints);
		}
		return element;
	}

	/**
	 * Makes a list of form values from id and value pairs.
	 */
	private static List<CampaignFormDataEntry> values(Object... idsAndValues) {
		List<CampaignFormDataEntry> values = new ArrayList<>();
		for (int i = 0; i < idsAndValues.length; i += 2) {
			values.add(new CampaignFormDataEntry((String) idsAndValues[i], idsAndValues[i + 1]));
		}
		return values;
	}

	private static Object valueOf(List<CampaignFormDataEntry> values, String id) {
		for (CampaignFormDataEntry entry : values) {
			if (id.equals(entry.getId())) {
				return entry.getValue();
			}
		}
		throw new AssertionError("The form values have no entry \"" + id + "\"");
	}

	/**
	 * Applies the expression of a new element with the id "result", and gives the stored value.
	 */
	private Object storedResult(String type, String expression, List<CampaignFormDataEntry> values) {
		CampaignFormDataFragmentUtils.applyExpressionToEntry(parser, values, element("result", type, expression));
		return valueOf(values, "result");
	}

	/**
	 * True if the value is no value, or the text of a finite number.
	 */
	private static boolean isNumberOrNoValue(Object value) {
		if (value == null) {
			return true;
		}
		try {
			return Double.isFinite(Double.parseDouble(value.toString()));
		} catch (NumberFormatException e) {
			return false;
		}
	}

	// Evaluation of an expression

	@Test
	public void numberTextIsReadAsANumber() {
		// Number fields store their value as text. The expression must add the numbers, not join the text.
		Object value = CampaignFormDataFragmentUtils.getExpressionValue(parser, values("a", "12", "b", "3"), "a + b");
		assertThat(value, is((Object) 15.0));
	}

	@Test
	public void integerValuesAreReadAsNumbers() {
		Object value = CampaignFormDataFragmentUtils.getExpressionValue(parser, values("a", 12, "b", "3"), "a + b");
		assertThat(value, is((Object) 15.0));
	}

	@Test
	public void decimalTextIsReadAsADecimal() {
		Object value = CampaignFormDataFragmentUtils.getExpressionValue(parser, values("a", "2.5", "b", "1.25"), "a + b");
		assertThat(value, is((Object) 3.75));
	}

	@Test
	public void conditionGivesTheValueOfTheMatchingBranch() {
		assertThat(CampaignFormDataFragmentUtils.getExpressionValue(parser, values("a", "7"), "a > 5 ? 1 : 0"), is((Object) 1));
		assertThat(CampaignFormDataFragmentUtils.getExpressionValue(parser, values("a", "3"), "a > 5 ? 1 : 0"), is((Object) 0));
	}

	@Test
	public void yesNoSwitchValuesAreReadAsTrueAndFalse() {
		// A yes-no switch stores YesNo.YES or YesNo.NO, but the expressions in the form definition use true and false.
		assertThat(CampaignFormDataFragmentUtils.getExpressionValue(parser, values("v", YesNo.YES), "v == true"), is((Object) true));
		assertThat(CampaignFormDataFragmentUtils.getExpressionValue(parser, values("v", YesNo.NO), "v ? 1 : 0"), is((Object) 0));
	}

	@Test
	public void evaluationDoesNotChangeTheStoredYesNoValues() {
		List<CampaignFormDataEntry> values = values("v", YesNo.YES);
		CampaignFormDataFragmentUtils.getExpressionValue(parser, values, "v == true");
		assertThat(valueOf(values, "v"), is((Object) YesNo.YES));
	}

	@Test(expected = SpelEvaluationException.class)
	public void fieldWithNoEntryStopsTheEvaluation() {
		CampaignFormDataFragmentUtils.getExpressionValue(parser, values("a", "12"), "a + missing");
	}

	@Test(expected = SpelEvaluationException.class)
	public void resultWithNoValueStopsTheEvaluation() {
		CampaignFormDataFragmentUtils.getExpressionValue(parser, values("a", null), "a");
	}

	// The value that a calculated field stores

	@Test
	public void numberFieldStoresAWholeNumberWithoutDecimals() {
		assertThat(storedResult("number", "a + b", values("a", "12", "b", "3")), is((Object) "15"));
	}

	@Test
	public void numberFieldStoresTwoDecimals() {
		assertThat(storedResult("number", "a / b", values("a", "1", "b", "3")), is((Object) "0.33"));
	}

	@Test
	public void percentageIsStoredAsAWholeNumber() {
		assertThat(storedResult("number", "a / b * 100", values("a", "45", "b", "50")), is((Object) "90"));
	}

	@Test
	public void decimalFieldStoresTwoDecimals() {
		assertThat(storedResult("decimal", "a / b", values("a", "1", "b", "3")), is((Object) "0.33"));
		assertThat(storedResult("decimal", "a + b", values("a", "1.5", "b", "0.5")), is((Object) "2"));
	}

	@Test
	public void rangeFieldStoresTheSameFormatAsANumberField() {
		assertThat(storedResult("range", "a + b", values("a", "12", "b", "3")), is((Object) "15"));
		assertThat(storedResult("range", "a / b", values("a", "1", "b", "3")), is((Object) "0.33"));
	}

	@Test
	public void numberFieldStoresNoValueWhenTheExpressionGivesTrueOrFalse() {
		assertThat(storedResult("number", "a > 5", values("a", "7")), is(nullValue()));
	}

	@Test
	public void yesNoFieldStoresTrueOrFalseAsText() {
		assertThat(storedResult("yes-no", "a > 5", values("a", "7")), is((Object) "true"));
	}

	@Test
	public void decimalsUseAPointInAllPhoneLanguages() {
		// The app has Dari (fa-AF) and Pashto (ps-AF). The stored value must not use the digits or the decimal mark of the
		// phone language.
		Locale before = Locale.getDefault();
		try {
			Locale.setDefault(new Locale("fa", "AF"));
			assertThat(storedResult("number", "a / b", values("a", "1", "b", "3")), is((Object) "0.33"));
		} finally {
			Locale.setDefault(before);
		}
	}

	@Test
	public void entryIsAddedWhenTheFieldHasNoValueYet() {
		List<CampaignFormDataEntry> values = values("a", "12", "b", "3");
		storedResult("number", "a + b", values);
		assertThat(values, hasSize(3));
	}

	@Test
	public void existingEntryIsUpdatedAndNotAddedAgain() {
		List<CampaignFormDataEntry> values = values("a", "12", "b", "3", "result", "1");
		assertThat(storedResult("number", "a + b", values), is((Object) "15"));
		assertThat(values, hasSize(3));
	}

	@Ignore("#102: an empty field in a sum stores the text 5.02.0")
	@Test
	public void emptyFieldInASumStoresANumberOrNoValue() {
		Object value = storedResult("number", "a + b + c", values("a", "5", "b", "", "c", "2"));
		assertTrue("Stored value: " + value, isNumberOrNoValue(value));
	}

	@Ignore("#102: a division by zero stores 2147483647")
	@Test
	public void divisionByZeroDoesNotStoreTheLargestInteger() {
		Object value = storedResult("number", "a / b", values("a", "12", "b", "0"));
		assertThat(value, not((Object) "2147483647"));
		assertTrue("Stored value: " + value, isNumberOrNoValue(value));
	}

	@Ignore("#102: zero divided by zero stores NaN")
	@Test
	public void zeroDividedByZeroStoresANumberOrNoValue() {
		Object value = storedResult("number", "a / b", values("a", "0", "b", "0"));
		assertTrue("Stored value: " + value, isNumberOrNoValue(value));
	}

	@Ignore("#102: a result above 2147483647 stores 2147483647")
	@Test
	public void largeResultIsStoredWithoutChange() {
		assertThat(storedResult("number", "a * b", values("a", "100000", "b", "100000")), is((Object) "10000000000"));
	}

	// Recalculation of all fields before a save

	@Test
	public void calculatedFieldThatUsesAnotherCalculatedFieldGetsItsValue() {
		List<CampaignFormDataEntry> values = values("a", "12", "b", "3");
		List<CampaignFormElement> elements =
			Arrays.asList(element("total", "number", "a + b"), element("doubled", "number", "total * 2"));

		CampaignFormDataFragmentUtils.recalculateAllExpressions(parser, values, elements, 3);

		assertThat(valueOf(values, "total"), is((Object) "15"));
		assertThat(valueOf(values, "doubled"), is((Object) "30"));
	}

	@Test
	public void moreThanOnePassGivesTheRightValueWhenTheOrderIsReversed() {
		// "doubled" comes before "total", so the first pass uses the old value of "total".
		List<CampaignFormDataEntry> values = values("a", "12", "b", "3", "total", "0");
		List<CampaignFormElement> elements =
			Arrays.asList(element("doubled", "number", "total * 2"), element("total", "number", "a + b"));

		CampaignFormDataFragmentUtils.recalculateAllExpressions(parser, values, elements, 3);

		assertThat(valueOf(values, "doubled"), is((Object) "30"));
	}

	@Test
	public void onePassKeepsTheOldValueWhenTheOrderIsReversed() {
		// This shows why the save uses more than one pass.
		List<CampaignFormDataEntry> values = values("a", "12", "b", "3", "total", "0");
		List<CampaignFormElement> elements =
			Arrays.asList(element("doubled", "number", "total * 2"), element("total", "number", "a + b"));

		CampaignFormDataFragmentUtils.recalculateAllExpressions(parser, values, elements, 1);

		assertThat(valueOf(values, "doubled"), is((Object) "0"));
	}

	@Test
	public void fieldsWithoutAnExpressionKeepTheValueThatTheUserEntered() {
		List<CampaignFormDataEntry> values = values("a", "12", "b", "3");
		List<CampaignFormElement> elements = Arrays.asList(element("a", "number", null), element("total", "number", "a + b"));

		CampaignFormDataFragmentUtils.recalculateAllExpressions(parser, values, elements, 3);

		assertThat(valueOf(values, "a"), is((Object) "12"));
	}

	// Linked range fields

	@Test
	public void rangeFieldWithAMinimumOrMaximumAndNoExpressionIsATrigger() {
		List<CampaignFormElement> elements = Arrays.asList(
			element("withLimits", "range", null, "min=0", "max=10"),
			element("upperCaseLimit", "range", null, "MAX=5"),
			element("withExpression", "range", "withLimits * 2", "min=0"),
			element("noLimits", "range", null),
			element("numberWithLimits", "number", null, "min=0"));

		Set<String> triggers = CampaignFormDataFragmentUtils.getRangeMinMaxTriggerIds(elements);

		assertThat(triggers, contains("withLimits", "upperCaseLimit"));
	}

	@Test
	public void idsInAnExpressionAreFoundWithoutKeywordsAndNumbers() {
		Set<String> ids = CampaignFormDataFragmentUtils.extractIdsFromExpression("a + b_2 > 10 AND NOT c OR d == null");
		assertThat(ids, contains("a", "b_2", "c", "d"));
	}

	@Test
	public void rangeFieldWhoseExpressionUsesATriggerIsAnEffect() {
		List<CampaignFormElement> elements = Arrays.asList(
			element("usesTrigger", "range", "trigger * 2"),
			element("usesOtherField", "range", "other + 1"),
			element("numberUsesTrigger", "number", "trigger"));

		Set<String> effects =
			CampaignFormDataFragmentUtils.getRangeExpressionEffectIds(elements, Collections.singleton("trigger"));

		assertThat(effects, contains("usesTrigger"));
	}

	@Test
	public void linkedRangeHelpersAcceptNoInput() {
		assertThat(CampaignFormDataFragmentUtils.getRangeMinMaxTriggerIds(null), is(empty()));
		assertThat(CampaignFormDataFragmentUtils.extractIdsFromExpression("  "), is(empty()));
		assertThat(CampaignFormDataFragmentUtils.getRangeExpressionEffectIds(null, Collections.singleton("trigger")), is(empty()));
	}

	// Captions and hints in the user language

	@Test
	public void captionInTheUserLanguageIsUsedWhenItExists() {
		CampaignFormElement element = element("children", "number", null);
		element.setCaption("Children vaccinated");
		Map<String, String> translations = new HashMap<>();
		translations.put("children", "Caption in Pashto");

		assertThat(CampaignFormDataFragmentUtils.getUserLanguageCaption(translations, element), is("Caption in Pashto"));
		assertThat(CampaignFormDataFragmentUtils.getUserLanguageCaption(new HashMap<>(), element), is("Children vaccinated"));
		assertThat(CampaignFormDataFragmentUtils.getUserLanguageCaption(null, element), is("Children vaccinated"));
	}

	@Test
	public void hintInTheUserLanguageIsUsedWhenItExists() {
		CampaignFormElement element = element("children", "number", null);
		element.setHint("Count children under 5");
		Map<String, String> hints = new HashMap<>();
		hints.put("children", "Hint in Pashto");

		assertThat(CampaignFormDataFragmentUtils.getUserLanguageHint(hints, element), is("Hint in Pashto"));
		assertThat(CampaignFormDataFragmentUtils.getUserLanguageHint(null, element), is("Count children under 5"));
	}

	// Form values

	@Test
	public void clearFormValueRemovesOnlyThatField() {
		List<CampaignFormDataEntry> values = values("a", "1", "b", "2");
		CampaignFormDataFragmentUtils.clearFormValue("a", values);
		assertThat(values, hasSize(1));
		assertThat(valueOf(values, "b"), is((Object) "2"));
	}
}
