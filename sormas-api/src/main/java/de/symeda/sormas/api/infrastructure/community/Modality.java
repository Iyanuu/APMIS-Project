package de.symeda.sormas.api.infrastructure.community;

public enum Modality {

	H2H("H2H"), M2M("M2M"), S2S("S2S"), HF2HF("HF2HF"), Mixed("Mixed"), M2MS2S("M2M S2S"), GENERAL("General");

	private String displayName;
	
	Modality(String displayName) {
        this.displayName = displayName;
    }

	public String toString() {
		return displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public static Modality fromValue(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		for (Modality modality : Modality.values()) {
			if (modality.name().equalsIgnoreCase(value)
				|| modality.getDisplayName().equalsIgnoreCase(value)) {
				return modality;
			}
		}

		throw new IllegalArgumentException("Unknown modality: " + value);
	}

}
