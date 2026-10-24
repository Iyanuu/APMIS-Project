package de.symeda.sormas.api.campaign;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;

/**
 * Covers {@link ExpressionProcessorUtils}, which gives the form values to the expressions of calculated campaign fields.
 *
 * <p>
 * The mobile app (CampaignFormDataFragmentUtils) and the web app (apmis-flow, ExpressionProcessor and CampaignFormBuilder)
 * both use this class. Thus, a change here changes the calculated values in both apps.
 *
 * <p>
 * Number fields store their value as text. This class changes number text to a Double, so that {@code a + b} adds and
 * does not join text.
 *
 * <p>
 * The test with {@code @Ignore("#102 ...")} shows a wrong result today. When #102 is fixed, remove {@code @Ignore}.
 */
public class ExpressionProcessorUtilsTest {

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

	/**
	 * Gives the value that the expressions see for one form value.
	 */
	private static Object valueSeenByExpressions(Object storedValue) {
		EvaluationContext context = ExpressionProcessorUtils.refreshEvaluationContext(values("field", storedValue));
		return ((Map<?, ?>) context.getRootObject().getValue()).get("field");
	}

	private static Object evaluate(String expression, List<CampaignFormDataEntry> values) {
		return new SpelExpressionParser().parseExpression(expression).getValue(ExpressionProcessorUtils.refreshEvaluationContext(values));
	}

	@Test
	public void numberTextBecomesADouble() {
		assertThat(valueSeenByExpressions("12"), is((Object) 12.0));
		assertThat(valueSeenByExpressions("2.5"), is((Object) 2.5));
		assertThat(valueSeenByExpressions("-3"), is((Object) (-3.0)));
	}

	@Test
	public void spacesAroundANumberAreIgnored() {
		assertThat(valueSeenByExpressions(" 12 "), is((Object) 12.0));
	}

	@Test
	public void integerBecomesADouble() {
		assertThat(valueSeenByExpressions(12), is((Object) 12.0));
	}

	@Test
	public void textThatIsNotANumberStaysText() {
		assertThat(valueSeenByExpressions("Kabul"), is((Object) "Kabul"));
		assertThat(valueSeenByExpressions("12a"), is((Object) "12a"));
	}

	@Test
	public void otherValuesAreNotChanged() {
		assertThat(valueSeenByExpressions(Boolean.TRUE), is((Object) Boolean.TRUE));
		assertThat(valueSeenByExpressions(2.5), is((Object) 2.5));
		assertThat(valueSeenByExpressions(12L), is((Object) 12L));
		assertThat(valueSeenByExpressions(null), is(nullValue()));
	}

	@Test
	public void emptyTextStaysEmptyText() {
		// This is the cause of #102: a sum with an empty field joins text.
		assertThat(valueSeenByExpressions(""), is((Object) ""));
	}

	@Test
	public void textNaNAndInfinityBecomeNumbers() {
		// A calculated field can store "NaN" today (#102). When an expression reads it again, it is a number.
		assertThat(Double.isNaN((Double) valueSeenByExpressions("NaN")), is(true));
		assertThat(valueSeenByExpressions("Infinity"), is((Object) Double.POSITIVE_INFINITY));
	}

	@Test
	public void expressionsReadTheFieldsByTheirIds() {
		assertThat(evaluate("children_under_5 + children_over_5", values("children_under_5", "12", "children_over_5", "3")), is((Object) 15.0));
		assertThat(evaluate("visited > 10 ? 'many' : 'few'", values("visited", "12")), is((Object) "many"));
	}

	@Test
	public void theLastValueWinsWhenAnIdIsTwiceInTheList() {
		assertThat(evaluate("a", values("a", "1", "a", "2")), is((Object) 2.0));
	}

	@Test(expected = SpelEvaluationException.class)
	public void anIdWithNoValueInTheListStopsTheEvaluation() {
		evaluate("a + missing", values("a", "1"));
	}

	@Test
	public void eachCallMakesANewContextWithTheCurrentValues() {
		List<CampaignFormDataEntry> values = values("a", "1");
		EvaluationContext before = ExpressionProcessorUtils.refreshEvaluationContext(values);
		values.get(0).setValue("5");

		assertThat(evaluate("a", values), is((Object) 5.0));
		assertThat(new SpelExpressionParser().parseExpression("a").getValue(before), is((Object) 1.0));
	}

	@Ignore("#102: an empty field in a sum joins text and gives \"5.02.0\"")
	@Test
	public void sumWithAnEmptyFieldDoesNotGiveText() {
		Object result;
		try {
			result = evaluate("a + b + c", values("a", "5", "b", "", "c", "2"));
		} catch (SpelEvaluationException e) {
			// An error is also correct: the app then keeps the old value of the calculated field.
			return;
		}
		assertThat(result, not(instanceOf(String.class)));
	}
}
