
package de.symeda.sormas.app.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.app.component.Item;

public class DataUtilsTest {

	@Test
	public void getEnumItems() {

		List<Item> enumItems = DataUtils.getEnumItems(YesNoUnknown.class, true);
		assertThat(enumItems.size(), is(YesNoUnknown.values().length + 1));

		enumItems = DataUtils.getEnumItems(YesNoUnknown.class, false);
		assertThat(enumItems.size(), is(YesNoUnknown.values().length));
	}

	@Test(expected = NullPointerException.class)
	public void getEnumItemsNullPointer() {
		DataUtils.getEnumItems(null, false);
	}

	@Test
	public void toItemsToleratesAMissingList() {

		assertThat(DataUtils.toItems(null, false).size(), is(0));
	}

	@Test
	public void toItemsToleratesNullEntries() {

		// toItems builds its label with String.valueOf, so a null entry becomes the label "null" rather than
		// an error. addItems below does the same job and does not agree with this.
		List<Item> items = DataUtils.toItems(Arrays.asList("a", null), false);

		assertThat(items.size(), is(2));
	}

	@Test
	public void addItemsToleratesNullEntries() {

		// FAILS TODAY. addItems calls listInEntry.toString() directly, so one null entry throws, while
		// toItems handles the same input. Two methods on the same class, doing the same conversion,
		// disagreeing about null is the kind of difference that only shows up on a device.
		List<Item> items = DataUtils.addItems(new ArrayList<Item>(), Arrays.asList("a", null));

		assertThat(items.size(), is(2));
	}

	@Test
	public void addItemsToleratesAMissingList() {

		// FAILS TODAY, for the same reason: no null guard on the incoming list, where toItems has one.
		List<Item> items = DataUtils.addItems(new ArrayList<Item>(), null);

		assertThat(items.size(), is(0));
	}

	@Test
	public void addEmptyItemToleratesAnItemWithNoKey() {

		// FAILS TODAY. addEmptyItem scans with items.get(i).getKey().equals(""), which throws as soon as any
		// item was built with a null key. Item allows a null key, so nothing prevents this.
		List<Item> items = new ArrayList<>();
		items.add(new Item<String>(null, "value"));

		DataUtils.addEmptyItem(items);

		assertThat(items.size(), is(2));
	}

	@Test
	public void addEmptyItemDoesNotAddASecondEmptyItem() {

		List<Item> items = DataUtils.toItems(Arrays.asList("a"), true);

		DataUtils.addEmptyItem(items);

		assertThat(items.size(), is(2));
	}
}
