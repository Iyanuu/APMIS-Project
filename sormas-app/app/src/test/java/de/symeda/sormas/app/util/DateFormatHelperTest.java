package de.symeda.sormas.app.util;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

/**
 * Specification for {@link DateFormatHelper#formatDateInterval(Date, Date)}, which builds the date range shown
 * on campaign and report screens.
 *
 * <p>
 * The assertions deliberately avoid hard-coding a date pattern, because the helper formats through the user's
 * configured language and that is not fixed by the test. They check the structure of the result instead, which
 * is what the surrounding code and the reader actually rely on.
 */
public class DateFormatHelperTest {

	private static Date dateOf(int year, int month, int day) {

		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(year, month - 1, day);
		return calendar.getTime();
	}

	@Test
	public void joinsAStartAndEndDate() {

		String interval = DateFormatHelper.formatDateInterval(dateOf(2026, 3, 1), dateOf(2026, 3, 5));

		assertThat(interval, containsString(" - "));
		assertThat(interval, containsString(DateFormatHelper.formatLocalDate(dateOf(2026, 3, 1))));
		assertThat(interval, containsString(DateFormatHelper.formatLocalDate(dateOf(2026, 3, 5))));
	}

	@Test
	public void showsOnlyTheStartWhenThereIsNoEnd() {

		// An open-ended campaign is normal, and it should read as a single date rather than a broken range.
		String interval = DateFormatHelper.formatDateInterval(dateOf(2026, 3, 1), null);

		assertThat(interval, is(DateFormatHelper.formatLocalDate(dateOf(2026, 3, 1))));
	}

	@Test
	public void showsOnlyTheEndWhenThereIsNoStart() {

		// FAILS TODAY. A missing start date formats as the empty string but the separator is still added, so
		// the screen shows " - 05/03/2026" with a dangling dash. Only the end date is known, so only the end
		// date should be shown.
		String interval = DateFormatHelper.formatDateInterval(null, dateOf(2026, 3, 5));

		assertThat(interval, is(DateFormatHelper.formatLocalDate(dateOf(2026, 3, 5))));
	}

	@Test
	public void producesNothingWhenNeitherDateIsKnown() {

		assertThat(DateFormatHelper.formatDateInterval(null, null), is(""));
	}
}
