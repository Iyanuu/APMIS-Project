package de.symeda.sormas.app.e2e;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import android.os.Bundle;

import androidx.test.platform.app.InstrumentationRegistry;

import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.util.SormasProperties;

/**
 * Sets the server that the end-to-end tests use, and makes sure that it is never production.
 *
 * <p>
 * The default server of the app is production ({@code server.url.default} in {@code sormas-app.properties}). If a test
 * does not set a server, it logs in to the live system and writes test data there. Thus, each end-to-end test calls
 * {@link #useTestServer()} before it opens a screen.
 *
 * <p>
 * The workflow gives the server as an instrumentation argument:
 * {@code -Pandroid.testInstrumentationRunnerArguments.serverUrl=...}. Without the argument, the tests use an address on
 * the emulator itself that has no server. Then a test that tries to connect fails, and nothing leaves the emulator.
 */
public final class E2eServer {

	/** The name of the instrumentation argument that gives the server URL. */
	public static final String SERVER_URL_ARGUMENT = "serverUrl";

	/** The name of the instrumentation argument that gives the user name of the test user. */
	public static final String USERNAME_ARGUMENT = "username";

	/**
	 * The name of the instrumentation argument that gives the password of the test user, encoded in base64. The device
	 * shell can change special characters in an argument, but not the characters of base64.
	 */
	public static final String PASSWORD_BASE64_ARGUMENT = "passwordBase64";

	/** An address on the emulator that has no server. Port 9 is the discard port. */
	public static final String NO_SERVER = "http://127.0.0.1:9/sormas-rest/";

	private E2eServer() {
	}

	/**
	 * The server URL from the instrumentation argument, or {@link #NO_SERVER}.
	 */
	public static String url() {
		Bundle arguments = InstrumentationRegistry.getArguments();
		String url = arguments.getString(SERVER_URL_ARGUMENT);
		return url == null || url.trim().isEmpty() ? NO_SERVER : url.trim();
	}

	/**
	 * True when the run has a server and a test user. The journey tests need a real server, so they are skipped without
	 * these arguments.
	 */
	public static boolean hasServerLogin() {
		return !NO_SERVER.equals(url()) && !username().isEmpty() && !password().isEmpty();
	}

	/** The user name of the test user, or an empty string. */
	public static String username() {
		return argument(USERNAME_ARGUMENT);
	}

	/** The password of the test user, or an empty string. Never write it to the output. */
	public static String password() {
		String encoded = argument(PASSWORD_BASE64_ARGUMENT);
		return encoded.isEmpty() ? "" : new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
	}

	private static String argument(String name) {
		String value = InstrumentationRegistry.getArguments().getString(name);
		return value == null ? "" : value.trim();
	}

	/**
	 * Points the app at the test server and logs out, so that each test starts on the login screen. Fails before any
	 * screen opens if the URL is the production default.
	 */
	public static void useTestServer() {
		String url = url();
		if (isProduction(url)) {
			throw new IllegalStateException("The end-to-end tests must not use the production server: " + url);
		}

		ConfigProvider.setServerRestUrl(url);

		String used = ConfigProvider.getServerRestUrl();
		if (isProduction(used)) {
			throw new IllegalStateException("The app still uses the production server after the change: " + used);
		}

		// The tests run in one app process. A test that logged in must not change the start of the next test.
		ConfigProvider.clearUserLogin();
	}

	static boolean isProduction(String url) {
		return normalize(url).equals(normalize(SormasProperties.getServerUrlDefault()));
	}

	private static String normalize(String url) {
		if (url == null) {
			return "";
		}
		String normalized = url.trim().toLowerCase();
		while (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}
}
