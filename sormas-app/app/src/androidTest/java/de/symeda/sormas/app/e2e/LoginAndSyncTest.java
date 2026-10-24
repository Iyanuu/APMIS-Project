package de.symeda.sormas.app.e2e;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import android.Manifest;
import android.app.Activity;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.backend.user.User;
import de.symeda.sormas.app.login.LoginActivity;

/**
 * Journey step 1: the test user logs in on the login screen, and the first sync gets the campaign data from the server.
 *
 * <p>
 * This test needs a real server and a test user (see {@link E2eServer#hasServerLogin()}). Without them, it is skipped.
 */
@RunWith(AndroidJUnit4.class)
public class LoginAndSyncTest {

	/** The first sync gets all infrastructure data, so it can take some minutes. */
	private static final long LOGIN_AND_SYNC_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(10);
	private static final long POLL_MILLIS = 2000;

	@Rule
	public GrantPermissionRule permissions = GrantPermissionRule.grant(
		Manifest.permission.ACCESS_FINE_LOCATION,
		Manifest.permission.ACCESS_COARSE_LOCATION,
		Manifest.permission.CAMERA,
		Manifest.permission.READ_PHONE_STATE,
		Manifest.permission.READ_EXTERNAL_STORAGE,
		Manifest.permission.WRITE_EXTERNAL_STORAGE);

	@Rule
	public ActivityTestRule<LoginActivity> loginScreen = new ActivityTestRule<>(LoginActivity.class, false, false);

	@Before
	public void useTestServer() {
		assumeTrue("This test needs a server and a test user. The workflow app_e2e.yml gives them.", E2eServer.hasServerLogin());
		E2eServer.useTestServer();
	}

	@Test
	public void testUserLogsInAndTheFirstSyncGetsCampaignForms() throws InterruptedException {
		LoginActivity activity = loginScreen.launchActivity(null);

		onView(allOf(isDescendantOfA(withId(R.id.login_username)), isAssignableFrom(EditText.class)))
			.perform(replaceText(E2eServer.username()));
		onView(allOf(isDescendantOfA(withId(R.id.login_password)), isAssignableFrom(EditText.class)))
			.perform(replaceText(E2eServer.password()), closeSoftKeyboard());

		// Not an Espresso click: Espresso waits until the app is idle, and the first sync keeps the app busy for minutes.
		InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> activity.findViewById(R.id.btn_login).performClick());

		waitUntilLoggedInAndOffTheLoginScreen();

		User user = ConfigProvider.getUser();
		assertThat(user.getUserName(), equalToIgnoringCase(E2eServer.username()));

		long campaigns = DatabaseHelper.getCampaignDao().countOf();
		long campaignForms = DatabaseHelper.getCampaignFormMetaDao().countOf();
		System.out.println("E2E: logged in as " + user.getUserName() + ". Campaigns: " + campaigns + ". Campaign forms: " + campaignForms + ".");

		assertTrue("The first sync got no campaign. The test user must see at least one campaign.", campaigns > 0);
		assertTrue("The first sync got no campaign form. The test user must see at least one campaign form.", campaignForms > 0);
	}

	/**
	 * Waits until the app has the user and shows a screen that is not the login screen. Fails with the last known state
	 * when the time is over.
	 */
	private static void waitUntilLoggedInAndOffTheLoginScreen() throws InterruptedException {
		long end = System.currentTimeMillis() + LOGIN_AND_SYNC_TIMEOUT_MILLIS;
		while (System.currentTimeMillis() < end) {
			Activity resumed = resumedActivity();
			if (ConfigProvider.getUser() != null && resumed != null && !(resumed instanceof LoginActivity)) {
				System.out.println("E2E: the login and the first sync are complete. Screen: " + resumed.getClass().getSimpleName() + ".");
				return;
			}
			Thread.sleep(POLL_MILLIS);
		}

		Activity resumed = resumedActivity();
		fail(
			"The login and the first sync did not complete in " + TimeUnit.MILLISECONDS.toMinutes(LOGIN_AND_SYNC_TIMEOUT_MILLIS)
				+ " minutes. User: " + (ConfigProvider.getUser() == null ? "none" : "present") + ". Screen: "
				+ (resumed == null ? "none" : resumed.getClass().getSimpleName())
				+ ". Look in logcat.txt for the connection or sync error.");
	}

	private static Activity resumedActivity() {
		AtomicReference<Activity> resumed = new AtomicReference<>();
		InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
			Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
			if (!activities.isEmpty()) {
				resumed.set(activities.iterator().next());
			}
		});
		return resumed.get();
	}
}
