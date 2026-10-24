package de.symeda.sormas.api.utils;

import static de.symeda.sormas.api.utils.CompatibilityCheckResponse.COMPATIBLE;
import static de.symeda.sormas.api.utils.CompatibilityCheckResponse.TOO_NEW;
import static de.symeda.sormas.api.utils.CompatibilityCheckResponse.TOO_OLD;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * The rule under test: a phone may sync only when
 *
 * <pre>
 *   MINIMUM_REQUIRED_VERSION  &lt;=  app version  &lt;=  server version
 * </pre>
 *
 * Versions are compared part by part (major, then minor, then patch) as numbers.
 * Below the minimum is TOO_OLD, above the server version is TOO_NEW, anything else is COMPATIBLE.
 * An app version that does not contain three numeric parts is rejected with an IllegalArgumentException.
 *
 * The minimum is the real production value (1.0.14), so changing the bound fails these tests on purpose.
 * Most tests use a pinned server version (1.0.20) so they do not change with the build version.
 * Two tests (oneAboveRealServerVersion_isTooNew and realServerVersion_isCompatibleWithItself) use the real
 * server version from InfoProvider.get(), with "too new" worked out as that version plus one patch.
 *
 * Note: the InfoProvider constructor reads /version.txt from the classpath, so that resource must be
 * on the test classpath (Maven writes it during the build).
 */
public class InfoProviderTest {

	/** Pinned server version. Any value above the minimum would do; 1.0.20 leaves room to test "between". */
	private static final String PINNED_SERVER_VERSION = "1.0.20";
	private final InfoProvider real = InfoProvider.get();

	private final InfoProvider infoProvider = new InfoProvider() {

		@Override
		public String getVersion() {
			return PINNED_SERVER_VERSION;
		}
	};

	// ---------------------------------------------------------------- the bounds themselves

	@Test
	public void minimumRequiredVersion_is_1_0_14() {
		assertEquals("1.0.14", infoProvider.getMinimumRequiredVersion());
	}

	// ---------------------------------------------------------------- lower bound: minimum 1.0.14

	@Test
	public void versionBelowMinimum_isTooOld() {
		assertEquals(TOO_OLD, infoProvider.isCompatibleToApi("1.0.0"));
	}

	@Test
	public void oneBelowMinimum_1_0_13_isTooOld() {
		assertEquals(TOO_OLD, infoProvider.isCompatibleToApi("1.0.13"));
	}

	@Test
	public void exactlyMinimum_1_0_14_isCompatible() {
		assertEquals(COMPATIBLE, infoProvider.isCompatibleToApi("1.0.14"));
	}

	@Test
	public void oneAboveMinimum_1_0_15_isCompatible() {
		assertEquals(COMPATIBLE, infoProvider.isCompatibleToApi("1.0.15"));
	}

	// ---------------------------------------------------------------- between and upper bound

	@Test
	public void betweenMinimumAndServerVersion_isCompatible() {
		assertEquals(COMPATIBLE, infoProvider.isCompatibleToApi("1.0.17"));
	}

	@Test
	public void exactlyServerVersion_isCompatible() {
		assertEquals(COMPATIBLE, infoProvider.isCompatibleToApi(PINNED_SERVER_VERSION));
	}

	@Test
	public void oneAboveRealServerVersion_isTooNew() {
		int[] v = VersionHelper.extractVersion(real.getVersion());
		String oneAbove = v[0] + "." + v[1] + "." + (v[2] + 1);

		assertEquals(TOO_NEW, real.isCompatibleToApi(oneAbove));
	}

	// ---------------------------------------------------------------- parts compare as numbers, most significant first

	@Test
	public void patch9_isBelowPatch14_becauseNumbersAreComparedNotText() {
		assertEquals(TOO_OLD, infoProvider.isCompatibleToApi("1.0.9"));
	}

	@Test
	public void patch100_isAbovePatch20_becauseNumbersAreComparedNotText() {
		assertEquals(TOO_NEW, infoProvider.isCompatibleToApi("1.0.100"));
	}

	@Test
	public void lowerMajorWithHighPatch_isStillTooOld() {
		assertEquals(TOO_OLD, infoProvider.isCompatibleToApi("0.9.99"));
	}

	@Test
	public void higherMinorWithLowPatch_isTooNew() {
		assertEquals(TOO_NEW, infoProvider.isCompatibleToApi("1.1.0"));
	}

	@Test
	public void higherMajor_isTooNew() {
		assertEquals(TOO_NEW, infoProvider.isCompatibleToApi("2.0.0"));
	}

	// ---------------------------------------------------------------- malformed input is rejected
	// (each of these calls exactly one method, so "expected" cannot hide a failure from another line;
	// it also works on every JUnit 4 version, unlike assertThrows which needs 4.13+)

	@Test(expected = IllegalArgumentException.class)
	public void emptyString_throwsIllegalArgumentException() {
		infoProvider.isCompatibleToApi("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void nonNumericVersion_throwsIllegalArgumentException() {
		infoProvider.isCompatibleToApi("abc");
	}

	@Test(expected = IllegalArgumentException.class)
	public void twoPartVersion_throwsIllegalArgumentException() {
		infoProvider.isCompatibleToApi("1.0");
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullVersion_throwsIllegalArgumentException() {
		infoProvider.isCompatibleToApi((String) null);
	}

	@Test
	public void realServerVersion_isCompatibleWithItself() {
		assertEquals(COMPATIBLE, real.isCompatibleToApi(real.getVersion()));
	}
}