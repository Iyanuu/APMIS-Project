package de.symeda.sormas.app.e2e;

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
	 * Points the app at the test server. Fails before any screen opens if the URL is the production default.
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
