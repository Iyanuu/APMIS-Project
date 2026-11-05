package de.symeda.sormas.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.junit.Test;

/**
 * Verifies that every HTTP endpoint called by the mobile app is declared and supported on the server
 * with a matching HTTP method and a compatible return type.
 *
 * <p>Fulfills Issue #66: "Fail the build when the app calls an endpoint the server does not have"
 */
public class AppEndpointCompatibilityTest {

	public static class AppEndpoint {

		public final String httpMethod;
		public final String path;
		public final String returnType;
		public final String facadeInterface;
		public final String javaMethodName;

		public AppEndpoint(String httpMethod, String path, String returnType, String facadeInterface, String javaMethodName) {
			this.httpMethod = httpMethod;
			this.path = normalizePath(path);
			this.returnType = returnType;
			this.facadeInterface = facadeInterface;
			this.javaMethodName = javaMethodName;
		}

		@Override
		public String toString() {
			return String.format("[%s.%s] %s /%s -> %s", facadeInterface, javaMethodName, httpMethod, path, returnType);
		}
	}

	public static class ServerEndpoint {

		public final String httpMethod;
		public final String path;
		public final String returnType;
		public final Class<?> resourceClass;
		public final Method method;

		public ServerEndpoint(String httpMethod, String path, String returnType, Class<?> resourceClass, Method method) {
			this.httpMethod = httpMethod;
			this.path = normalizePath(path);
			this.returnType = returnType;
			this.resourceClass = resourceClass;
			this.method = method;
		}

		@Override
		public String toString() {
			return String.format("[%s.%s] %s /%s -> %s", resourceClass.getSimpleName(), method.getName(), httpMethod, path, returnType);
		}
	}

	public static String normalizePath(String path) {
		if (path == null) {
			return "";
		}
		String normalized = path.trim().replaceAll("/+", "/");
		if (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}
		if (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}

	public static String combinePaths(String classPath, String methodPath) {
		return normalizePath(classPath + "/" + methodPath);
	}

	public static List<AppEndpoint> loadAppEndpoints() throws IOException {
		InputStream in = AppEndpointCompatibilityTest.class.getResourceAsStream("/app_in_use_endpoints.tsv");
		if (in == null) {
			// Fallback: check relative paths from sormas-rest or repo root
			File file = new File("src/test/resources/app_in_use_endpoints.tsv");
			if (!file.exists()) {
				file = new File("../sormas-app/app/src/test/resources/app_in_use_endpoints.tsv");
			}
			if (file.exists()) {
				in = new FileInputStream(file);
			}
		}

		assertNotNull("Could not find app_in_use_endpoints.tsv on classpath or filesystem", in);

		List<AppEndpoint> endpoints = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			boolean firstLine = true;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				if (firstLine) {
					firstLine = false;
					if (line.startsWith("METHOD\tPATH")) {
						continue;
					}
				}
				String[] parts = line.split("\t");
				if (parts.length >= 5) {
					endpoints.add(new AppEndpoint(parts[0], parts[1], parts[2], parts[3], parts[4]));
				}
			}
		}
		return endpoints;
	}

	public static List<Class<?>> getResourceClasses() {
		Set<Class<?>> classes = new HashSet<>();

		// 1. Try scanning de.symeda.sormas.rest package
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Enumeration<URL> resources = cl.getResources("de/symeda/sormas/rest");
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				File dir = new File(url.getFile().replace("%20", " "));
				if (dir.exists() && dir.isDirectory()) {
					File[] files = dir.listFiles((d, name) -> name.endsWith("Resource.class"));
					if (files != null) {
						for (File f : files) {
							String simpleName = f.getName().substring(0, f.getName().length() - 6);
							try {
								Class<?> c = Class.forName("de.symeda.sormas.rest." + simpleName);
								if (c.isAnnotationPresent(Path.class)) {
									classes.add(c);
								}
							} catch (ClassNotFoundException | NoClassDefFoundError ignored) {
							}
						}
					}
				}
			}
		} catch (Exception ignored) {
		}

		// 2. Comprehensive explicit fallback ensuring reliable execution in all test environments
		List<Class<?>> knownResources = Arrays.asList(
			ActionResource.class,
			AdditionalTestResource.class,
			AggregateReportResource.class,
			ApmisCampaignResource.class,
			AreaResource.class,
			BAGExportResource.class,
			CampaignFormDataResource.class,
			CampaignFormMetaRegionResource.class,
			CampaignFormMetaResource.class,
			CampaignFormMetaWithExpResource.class,
			CampaignResource.class,
			CaseResource.class,
			ClassificationResource.class,
			ClinicalVisitResource.class,
			CommunityResource.class,
			ContactResource.class,
			ContinentResource.class,
			CountryResource.class,
			CustomizableEnumValueResource.class,
			DeviceInfoResource.class,
			DevicesErrorResource.class,
			DiseaseConfigurationResource.class,
			DistrictResource.class,
			DocumentResource.class,
			EntityDtoResource.class,
			EventParticipantResource.class,
			EventResource.class,
			ExternalSurveillanceToolGatewayResource.class,
			FacilityResource.class,
			FCMTokenResource.class,
			FeatureConfigurationResource.class,
			ImmunizationResource.class,
			InfoResource.class,
			InfrastructureResource.class,
			OutbreakResource.class,
			PathogenTestResource.class,
			PersonResource.class,
			PointOfEntryResource.class,
			PopulationDataResource.class,
			PrescriptionResource.class,
			RegionResource.class,
			SampleResource.class,
			SormasToSormasResource.class,
			SubcontinentResource.class,
			SurveillanceReportResource.class,
			TaskResource.class,
			TreatmentResource.class,
			UserResource.class,
			UserRoleConfigResource.class,
			VisitResource.class,
			WeeklyReportResource.class
		);

		for (Class<?> res : knownResources) {
			if (res.isAnnotationPresent(Path.class)) {
				classes.add(res);
			}
		}

		return new ArrayList<>(classes);
	}

	public static Map<String, List<ServerEndpoint>> extractServerEndpoints() {
		Map<String, List<ServerEndpoint>> endpoints = new HashMap<>();
		List<Class<?>> resources = getResourceClasses();

		for (Class<?> clazz : resources) {
			Path classPathAnnotation = clazz.getAnnotation(Path.class);
			if (classPathAnnotation == null) {
				continue;
			}
			String classPath = classPathAnnotation.value();

			for (Method method : clazz.getDeclaredMethods()) {
				String httpMethod = getHttpMethod(method);
				if (httpMethod == null) {
					continue;
				}

				Path methodPathAnnotation = method.getAnnotation(Path.class);
				String methodPath = methodPathAnnotation != null ? methodPathAnnotation.value() : "";
				String combinedPath = combinePaths(classPath, methodPath);

				String returnType = formatReturnType(method.getGenericReturnType());
				ServerEndpoint endpoint = new ServerEndpoint(httpMethod, combinedPath, returnType, clazz, method);

				String key = httpMethod + " " + combinedPath;
				endpoints.computeIfAbsent(key, k -> new ArrayList<>()).add(endpoint);
			}
		}

		return endpoints;
	}

	private static String getHttpMethod(Method method) {
		if (method.isAnnotationPresent(GET.class)) return "GET";
		if (method.isAnnotationPresent(POST.class)) return "POST";
		if (method.isAnnotationPresent(PUT.class)) return "PUT";
		if (method.isAnnotationPresent(DELETE.class)) return "DELETE";
		if (method.isAnnotationPresent(PATCH.class)) return "PATCH";
		if (method.isAnnotationPresent(HEAD.class)) return "HEAD";
		if (method.isAnnotationPresent(OPTIONS.class)) return "OPTIONS";
		return null;
	}

	private static String formatReturnType(Type returnType) {
		return cleanTypeName(returnType.toString());
	}

	private static String cleanTypeName(String typeStr) {
		return typeStr
			.replace("class ", "")
			.replace("interface ", "")
			.replaceAll("de\\.symeda\\.sormas\\.api\\.[a-z0-9_.]+\\.", "");
	}

	public static boolean isTypeCompatible(String serverType, String appType) {
		if (serverType.equals(appType)) {
			return true;
		}
		// JAX-RS Response is dynamically typed and compatible with any expected return
		if (serverType.equals("javax.ws.rs.core.Response") || serverType.equals("Response")) {
			return true;
		}
		String cleanServer = cleanTypeName(serverType);
		String cleanApp = cleanTypeName(appType);
		return cleanServer.equals(cleanApp);
	}

	@Test
	public void assertAllAppEndpointsExistOnServer() throws IOException {
		List<AppEndpoint> appEndpoints = loadAppEndpoints();
		assertFalse("Expected non-empty list of app endpoints", appEndpoints.isEmpty());

		Map<String, List<ServerEndpoint>> serverEndpoints = extractServerEndpoints();
		assertFalse("Expected non-empty map of server endpoints", serverEndpoints.isEmpty());

		List<String> failures = new ArrayList<>();

		for (AppEndpoint appEp : appEndpoints) {
			String key = appEp.httpMethod + " " + appEp.path;
			List<ServerEndpoint> matchingServers = serverEndpoints.get(key);

			if (matchingServers == null || matchingServers.isEmpty()) {
				failures.add(String.format(
					"Missing server endpoint for app call: [%s.%s]\n"
						+ "  Expected: %s /%s\n"
						+ "  Returning: %s\n"
						+ "  No matching JAX-RS @Path found on any resource in sormas-rest.",
					appEp.facadeInterface,
					appEp.javaMethodName,
					appEp.httpMethod,
					appEp.path,
					appEp.returnType
				));
				continue;
			}

			boolean hasCompatibleType = false;
			for (ServerEndpoint serverEp : matchingServers) {
				if (isTypeCompatible(serverEp.returnType, appEp.returnType)) {
					hasCompatibleType = true;
					break;
				}
			}

			if (!hasCompatibleType) {
				failures.add(String.format(
					"Return type mismatch for app call: [%s.%s]\n"
						+ "  Endpoint: %s /%s\n"
						+ "  App expects: %s\n"
						+ "  Server returns: %s (in %s.%s)",
					appEp.facadeInterface,
					appEp.javaMethodName,
					appEp.httpMethod,
					appEp.path,
					appEp.returnType,
					matchingServers.get(0).returnType,
					matchingServers.get(0).resourceClass.getSimpleName(),
					matchingServers.get(0).method.getName()
				));
			}
		}

		if (!failures.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("\nFound %d endpoint mismatch(es) between mobile app and server:\n\n", failures.size()));
			for (int i = 0; i < failures.size(); i++) {
				sb.append(String.format("%d) %s\n\n", i + 1, failures.get(i)));
			}
			fail(sb.toString());
		}
	}
}

