package de.symeda.sormas.app.util;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Specification for {@link PercentageUtils}, which converts a portion and a total into a percentage for the
 * dashboard summary charts.
 *
 * <p>
 * These tests state what the method is expected to do, not what it currently does. Several of them fail today.
 * That is deliberate: each failure corresponds to an input that makes a deployed app throw
 * {@link NumberFormatException} rather than return a number.
 *
 * <p>
 * All three failures share one cause. The implementation computes the percentage as a float, formats it into a
 * string with a default-locale {@link java.text.DecimalFormat}, and then parses that string back with
 * {@code Float.valueOf}, which only accepts plain ASCII decimals. Any locale or magnitude that makes
 * {@code DecimalFormat} emit something other than {@code 123.45} therefore blows up on the way back.
 *
 * <p>
 * The locale case is the one that matters most here. The app ships {@code values-fa-rAF} and
 * {@code values-ps-rAF}, so Dari and Pashto are expected device languages, and both format numbers with
 * Eastern Arabic digits.
 *
 * <p>
 * The live caller is {@code TaskSummaryFragment}, which calls this while binding the dashboard pie chart.
 */
public class PercentageUtilsTest {

	private static final double TOLERANCE = 0.005;

	private Locale originalLocale;

	@Before
	public void rememberLocale() {
		originalLocale = Locale.getDefault();
	}

	@After
	public void restoreLocale() {
		// The implementation reads the default locale, so these tests have to change it. Restoring afterwards
		// keeps that from leaking into whatever test runs next.
		Locale.setDefault(originalLocale);
	}

	@Test
	public void calculatesAPercentageOfATotal() {

		assertThat((double) PercentageUtils.percentageOf(50f, 200f), closeTo(25d, TOLERANCE));
	}

	@Test
	public void calculatesAPercentageOfAList() {

		assertThat((double) PercentageUtils.percentageOf(25f, Arrays.asList(25f, 75f)), closeTo(25d, TOLERANCE));
	}

	@Test
	public void calculatesAPercentageOfAnArray() {

		assertThat((double) PercentageUtils.percentageOf(25f, new float[] {
			25f,
			75f }), closeTo(25d, TOLERANCE));
	}

	@Test
	public void rejectsANullList() {

		try {
			PercentageUtils.percentageOf(1f, (List<Float>) null);
			fail("expected IllegalArgumentException for a null list");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	@Test
	public void worksWhenTheDeviceIsSetToDari() {

		// FAILS TODAY. Dari formats 33.33 as ۳۳٫۳۳ (Eastern Arabic digits, U+066B as the decimal separator),
		// which Float.valueOf cannot parse. A device set to Dari throws instead of drawing the chart.
		Locale.setDefault(new Locale("fa", "AF"));

		assertThat((double) PercentageUtils.percentageOf(1f, 3f), closeTo(33.33d, TOLERANCE));
	}

	@Test
	public void worksWhenTheDeviceIsSetToPashto() {

		// FAILS TODAY, for the same reason as the Dari case. Both are official languages of Afghanistan and
		// both are shipped as app resources, so both are expected device configurations.
		Locale.setDefault(new Locale("ps", "AF"));

		assertThat((double) PercentageUtils.percentageOf(1f, 3f), closeTo(33.33d, TOLERANCE));
	}

	@Test
	public void isUnaffectedByTheDeviceLocale() {

		// A percentage is a number, so the same inputs should produce the same number everywhere. Only the
		// display layer should care about locale.
		Locale.setDefault(Locale.UK);
		float inUk = PercentageUtils.percentageOf(1f, 8f);

		Locale.setDefault(Locale.GERMANY);
		float inGermany = PercentageUtils.percentageOf(1f, 8f);

		assertThat((double) inGermany, closeTo(inUk, TOLERANCE));
	}

	@Test
	public void handlesAnEmptyTotalWithoutThrowing() {

		// FAILS TODAY. Dividing by zero yields Infinity, DecimalFormat renders that as "∞", and Float.valueOf
		// rejects it. This is reachable whenever a summary is rendered before any data exists, which is the
		// normal state of a fresh install.
		Locale.setDefault(Locale.UK);

		float result = PercentageUtils.percentageOf(5f, 0f);

		assertThat(Float.isFinite(result), is(false));
	}

	@Test
	public void handlesPercentagesAboveOneThousand() {

		// FAILS TODAY. Above 1000 the default format inserts grouping separators ("2,500,000"), which
		// Float.valueOf also rejects. Reachable from bad or partial data where the portion exceeds the total.
		Locale.setDefault(Locale.UK);

		assertThat((double) PercentageUtils.percentageOf(50000f, 2f), closeTo(2500000d, 1d));
	}
}
