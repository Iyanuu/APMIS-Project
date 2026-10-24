package de.symeda.sormas.app;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Gate check for #7. This test fails on purpose. This pull request is a draft and will be closed without a merge.
 */
public class GateCheckFailingTest {

	@Test
	public void failsOnPurpose() {
		fail("This test fails on purpose, to check that the android gate fails.");
	}
}
