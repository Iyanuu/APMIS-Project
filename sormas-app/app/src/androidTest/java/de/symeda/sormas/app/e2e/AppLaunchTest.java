package de.symeda.sormas.app.e2e;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;

import android.Manifest;

import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.login.LoginActivity;

/**
 * Smoke test: the app starts on an emulator and shows the login screen.
 *
 * <p>
 * This test does not need a server. It proves that the emulator, the app build and the test build work together. The
 * journey tests in this package use the same setup.
 */
@RunWith(AndroidJUnit4.class)
public class AppLaunchTest {

	@Rule
	public GrantPermissionRule permissions = GrantPermissionRule.grant(
		Manifest.permission.ACCESS_FINE_LOCATION,
		Manifest.permission.ACCESS_COARSE_LOCATION,
		Manifest.permission.CAMERA,
		Manifest.permission.READ_PHONE_STATE,
		Manifest.permission.READ_EXTERNAL_STORAGE,
		Manifest.permission.WRITE_EXTERNAL_STORAGE);

	/** Starts the activity only after the test sets the server. */
	@Rule
	public ActivityTestRule<LoginActivity> loginScreen = new ActivityTestRule<>(LoginActivity.class, false, false);

	@Before
	public void useTestServer() {
		E2eServer.useTestServer();
	}

	@Test
	public void appDoesNotUseTheProductionServer() {
		assertFalse(E2eServer.isProduction(ConfigProvider.getServerRestUrl()));
	}

	@Test
	public void loginScreenShowsTheSignInFields() {
		loginScreen.launchActivity(null);

		onView(withId(R.id.login_username)).check(matches(isDisplayed()));
		onView(withId(R.id.login_password)).check(matches(isDisplayed()));
		onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
	}
}
