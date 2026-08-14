package de.symeda.sormas.app.component;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Specification for {@link Item}, the key/value pair behind every spinner and dropdown in the app.
 *
 * <p>
 * Everything here passes. Item is worth pinning down anyway because its equals and hashCode decide which entry
 * a spinner considers already selected: if they stopped agreeing, forms would silently reopen showing no
 * selection, which is very hard to trace back from a bug report.
 *
 * <p>
 * The last test records that a null key is allowed. That is the input which makes
 * {@code DataUtils.addEmptyItem} throw, so the two behaviours need to be read together.
 */
public class ItemTest {

	@Test
	public void exposesKeyAndValue() {

		Item<String> item = new Item<String>("Label", "value");

		assertThat(item.getKey(), is("Label"));
		assertThat(item.getValue(), is("value"));
	}

	@Test
	public void rendersItsKeyAsItsLabel() {

		// Spinners display items via toString, so this is what the user actually reads on screen.
		assertThat(new Item<String>("Label", "value").toString(), is("Label"));
	}

	@Test
	public void treatsSameKeyAndValueAsEqual() {

		assertThat(new Item<String>("Label", "value"), is(new Item<String>("Label", "value")));
	}

	@Test
	public void agreesBetweenEqualsAndHashCode() {

		assertThat(new Item<String>("Label", "value").hashCode(), is(new Item<String>("Label", "value").hashCode()));
	}

	@Test
	public void distinguishesADifferentValueUnderTheSameLabel() {

		// Two entries can share a display label while meaning different things, so value has to count.
		assertThat(new Item<String>("Label", "one"), is(not(new Item<String>("Label", "two"))));
	}

	@Test
	public void isNotEqualToNullOrAnotherType() {

		Item<String> item = new Item<String>("Label", "value");

		assertThat(item.equals(null), is(false));
		assertThat(item.equals("Label"), is(false));
	}

	@Test
	public void allowsANullKeyAndValue() {

		Item<String> item = new Item<>(null, null);

		assertThat(item.getKey(), is((String) null));
		assertThat(item.getValue(), is((String) null));
		assertThat(item, is(new Item<String>(null, null)));
	}
}
