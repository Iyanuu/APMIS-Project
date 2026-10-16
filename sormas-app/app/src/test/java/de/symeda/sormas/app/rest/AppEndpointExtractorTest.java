package de.symeda.sormas.app.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.HTTP;
import retrofit2.http.OPTIONS;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public class AppEndpointExtractorTest {

	/**
	 * Canonical list of the 19 Retrofit interfaces actively used by APMIS mobile sync and startup connectivity.
	 * Legacy disease surveillance interfaces (cases, contacts, visits, clinical events, samples, etc.) are excluded
	 * because their synchronization in SynchronizeDataAsync is deactivated/commented out in APMIS.
	 */
	public static final List<Class<?>> IN_USE_INTERFACES = Collections.unmodifiableList(Arrays.asList(
		AreaFacadeRetro.class,
		CampaignFacadeRetro.class,
		CampaignFormDataFacadeRetro.class,
		CampaignFormMetaFacadeRetro.class,
		CampaignFormMetaRegionFacadeRetro.class,
		CampaignFormMetaWithExpiryFacadeRetro.class,
		CommunityFacadeRetro.class,
		DeviceErrorFacadeRetro.class,
		DeviceInfoFacadeRetro.class,
		DistrictFacadeRetro.class,
		DocumentFacadeRetro.class,
		FCMTokenFacadeRetro.class,
		FeatureConfigurationFacadeRetro.class,
		InfoFacadeRetro.class,
		InfrastructureFacadeRetro.class,
		PopulationDataFacadeRetro.class,
		RegionFacadeRetro.class,
		UserFacadeRetro.class,
		UserRoleConfigFacadeRetro.class
	));

	public static class EndpointRecord {

		public final String httpMethod;
		public final String path;
		public final String returnType;
		public final String facadeInterface;
		public final String javaMethodName;

		public EndpointRecord(String httpMethod, String path, String returnType, String facadeInterface, String javaMethodName) {
			this.httpMethod = httpMethod;
			// Normalize leading slash if present
			this.path = path.startsWith("/") ? path.substring(1) : path;
			this.returnType = returnType;
			this.facadeInterface = facadeInterface;
			this.javaMethodName = javaMethodName;
		}

		public String toTsvLine() {
			return String.format("%s\t%s\t%s\t%s\t%s", httpMethod, path, returnType, facadeInterface, javaMethodName);
		}
	}

	public static List<EndpointRecord> extractEndpoints(List<Class<?>> interfaces) {
		List<EndpointRecord> records = new ArrayList<>();

		for (Class<?> clazz : interfaces) {
			for (Method method : clazz.getDeclaredMethods()) {
				String httpMethod = null;
				String path = null;

				for (Annotation annotation : method.getAnnotations()) {
					if (annotation instanceof GET) {
						httpMethod = "GET";
						path = ((GET) annotation).value();
					} else if (annotation instanceof POST) {
						httpMethod = "POST";
						path = ((POST) annotation).value();
					} else if (annotation instanceof PUT) {
						httpMethod = "PUT";
						path = ((PUT) annotation).value();
					} else if (annotation instanceof DELETE) {
						httpMethod = "DELETE";
						path = ((DELETE) annotation).value();
					} else if (annotation instanceof PATCH) {
						httpMethod = "PATCH";
						path = ((PATCH) annotation).value();
					} else if (annotation instanceof HEAD) {
						httpMethod = "HEAD";
						path = ((HEAD) annotation).value();
					} else if (annotation instanceof OPTIONS) {
						httpMethod = "OPTIONS";
						path = ((OPTIONS) annotation).value();
					} else if (annotation instanceof HTTP) {
						httpMethod = ((HTTP) annotation).method();
						path = ((HTTP) annotation).path();
					}
				}

				if (httpMethod != null && path != null) {
					String returnType = formatReturnType(method.getGenericReturnType());
					records.add(new EndpointRecord(
						httpMethod,
						path,
						returnType,
						clazz.getSimpleName(),
						method.getName()
					));
				}
			}
		}

		// Ensure deterministic, stable ordering across runs and platforms
		records.sort(Comparator
			.comparing((EndpointRecord e) -> e.httpMethod)
			.thenComparing(e -> e.path)
			.thenComparing(e -> e.facadeInterface)
			.thenComparing(e -> e.javaMethodName));

		return records;
	}

	private static String formatReturnType(Type returnType) {
		if (returnType instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) returnType;
			Type rawType = pt.getRawType();
			if (rawType instanceof Class && ((Class<?>) rawType).equals(Call.class)) {
				return cleanTypeName(pt.getActualTypeArguments()[0].toString());
			}
		}
		return cleanTypeName(returnType.toString());
	}

	private static String cleanTypeName(String typeStr) {
		return typeStr
			.replace("class ", "")
			.replace("interface ", "")
			.replaceAll("de\\.symeda\\.sormas\\.api\\.[a-z0-9_.]+\\.", "");
	}

	@Test
	public void extractEndpointsAndVerifyDeterministicOutput() throws IOException {
		List<EndpointRecord> endpoints = extractEndpoints(IN_USE_INTERFACES);
		assertFalse("Expected endpoints to be extracted", endpoints.isEmpty());

		StringBuilder tsvBuilder = new StringBuilder();
		tsvBuilder.append("METHOD\tPATH\tRETURN_TYPE\tINTERFACE\tMETHOD_NAME\n");
		for (EndpointRecord ep : endpoints) {
			tsvBuilder.append(ep.toTsvLine()).append("\n");
		}
		String currentContent = tsvBuilder.toString();

		// Write to build directory report artifact
		File reportDir = new File("build/reports/endpoints");
		reportDir.mkdirs();
		File reportFile = new File(reportDir, "app_in_use_endpoints.tsv");
		try (FileWriter writer = new FileWriter(reportFile)) {
			writer.write(currentContent);
		}

		// Baseline resource file
		File resourceDir = new File("src/test/resources");
		File baselineFile = new File(resourceDir, "app_in_use_endpoints.tsv");

		if (!baselineFile.exists()) {
			resourceDir.mkdirs();
			try (FileWriter writer = new FileWriter(baselineFile)) {
				writer.write(currentContent);
			}
		} else {
			String baselineContent = new String(Files.readAllBytes(baselineFile.toPath()), StandardCharsets.UTF_8);
			assertEquals(
				"App in-use endpoints drifted from committed baseline. "
					+ "If this was an intentional change, update src/test/resources/app_in_use_endpoints.tsv",
				baselineContent,
				currentContent
			);
		}
	}
}
