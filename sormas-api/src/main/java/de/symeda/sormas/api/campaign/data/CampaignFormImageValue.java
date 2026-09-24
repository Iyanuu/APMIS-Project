package de.symeda.sormas.api.campaign.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CampaignFormImageValue implements Serializable {

	private static final long serialVersionUID = 6742326286672516777L;

	public static final String IMAGE_ID = "imageId";
	public static final String LOCAL_ID = "localId";
	public static final String ORIGINAL_FILE_NAME = "originalFileName";
	public static final String GENERATED_FILE_NAME = "generatedFileName";
	public static final String MIME_TYPE = "mimeType";
	public static final String SOURCE = "source";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String ORIGINAL_SIZE_BYTES = "originalSizeBytes";
	public static final String COMPRESSED_SIZE_BYTES = "compressedSizeBytes";
	public static final String CAPTURED_AT = "capturedAt";
	public static final String DEVICE_TYPE = "deviceType";
	public static final String OS_VERSION = "osVersion";

	private String imageId;
	private String localId;
	private String originalFileName;
	private String generatedFileName;
	private String mimeType;
	private CampaignFormImageSource source;
	private Integer width;
	private Integer height;
	private Long originalSizeBytes;
	private Long compressedSizeBytes;
	private Long capturedAt;
	private String deviceType;
	private String osVersion;

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getLocalId() {
		return localId;
	}

	public void setLocalId(String localId) {
		this.localId = localId;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public String getGeneratedFileName() {
		return generatedFileName;
	}

	public void setGeneratedFileName(String generatedFileName) {
		this.generatedFileName = generatedFileName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public CampaignFormImageSource getSource() {
		return source;
	}

	public void setSource(CampaignFormImageSource source) {
		this.source = source;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Long getOriginalSizeBytes() {
		return originalSizeBytes;
	}

	public void setOriginalSizeBytes(Long originalSizeBytes) {
		this.originalSizeBytes = originalSizeBytes;
	}

	public Long getCompressedSizeBytes() {
		return compressedSizeBytes;
	}

	public void setCompressedSizeBytes(Long compressedSizeBytes) {
		this.compressedSizeBytes = compressedSizeBytes;
	}

	public Long getCapturedAt() {
		return capturedAt;
	}

	public void setCapturedAt(Long capturedAt) {
		this.capturedAt = capturedAt;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public boolean hasIdentifier() {
		return !isBlank(imageId) || !isBlank(localId);
	}

	public boolean isUploaded() {
		return !isBlank(imageId);
	}

	public boolean isPendingUpload() {
		return isBlank(imageId) && !isBlank(localId);
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		putIfNotNull(map, IMAGE_ID, imageId);
		putIfNotNull(map, LOCAL_ID, localId);
		putIfNotNull(map, ORIGINAL_FILE_NAME, originalFileName);
		putIfNotNull(map, GENERATED_FILE_NAME, generatedFileName);
		putIfNotNull(map, MIME_TYPE, mimeType);
		putIfNotNull(map, SOURCE, source != null ? source.name() : null);
		putIfNotNull(map, WIDTH, width);
		putIfNotNull(map, HEIGHT, height);
		putIfNotNull(map, ORIGINAL_SIZE_BYTES, originalSizeBytes);
		putIfNotNull(map, COMPRESSED_SIZE_BYTES, compressedSizeBytes);
		putIfNotNull(map, CAPTURED_AT, capturedAt);
		putIfNotNull(map, DEVICE_TYPE, deviceType);
		putIfNotNull(map, OS_VERSION, osVersion);
		return map;
	}

	public static CampaignFormImageValue fromMap(Map<?, ?> map) {
		if (map == null) {
			return null;
		}

		CampaignFormImageValue imageValue = new CampaignFormImageValue();
		imageValue.setImageId(asString(map.get(IMAGE_ID)));
		imageValue.setLocalId(asString(map.get(LOCAL_ID)));
		imageValue.setOriginalFileName(asString(map.get(ORIGINAL_FILE_NAME)));
		imageValue.setGeneratedFileName(asString(map.get(GENERATED_FILE_NAME)));
		imageValue.setMimeType(asString(map.get(MIME_TYPE)));
		imageValue.setSource(parseSource(map.get(SOURCE)));
		imageValue.setWidth(asInteger(map.get(WIDTH)));
		imageValue.setHeight(asInteger(map.get(HEIGHT)));
		imageValue.setOriginalSizeBytes(asLong(map.get(ORIGINAL_SIZE_BYTES)));
		imageValue.setCompressedSizeBytes(asLong(map.get(COMPRESSED_SIZE_BYTES)));
		imageValue.setCapturedAt(asLong(map.get(CAPTURED_AT)));
		imageValue.setDeviceType(asString(map.get(DEVICE_TYPE)));
		imageValue.setOsVersion(asString(map.get(OS_VERSION)));
		return imageValue;
	}

	private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
		if (value != null) {
			map.put(key, value);
		}
	}

	private static String asString(Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}

	private static Integer asInteger(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return Integer.valueOf(String.valueOf(value).trim());
	}

	private static Long asLong(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		return Long.valueOf(String.valueOf(value).trim());
	}

	private static CampaignFormImageSource parseSource(Object value) {
		String source = asString(value);
		if (source == null || source.isEmpty()) {
			return null;
		}
		return CampaignFormImageSource.valueOf(source.toUpperCase(Locale.ROOT).replace('-', '_'));
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
