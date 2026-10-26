package de.symeda.sormas.api.user;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public enum FormAccess {
	
	ICM("ICM", "ICM"),
    PCA("PCA", "PCA"),
    ARCHIVE("ARCHIVE", "ARCHIVE"),
    FLW("FLW", "FLW"),
    ADMIN("ADMIN", "ADMIN"),
    FMS("FMS", "FMS"),	
    LQAS("LQAS", "LQAS"),
    TRAINING("TRAINING", "TRAINING"),
    EAG("EAG", "EAG"),
    EAG_ADMIN("EAG-ADMIN", "EAG-ADMIN"),
    EAG_ICM("EAG-ICM", "EAG-ICM"),
    EAG_PCA("EAG-PCA", "EAG-PCA"),
    EAG_FMS("EAG-FMS", "EAG-FMS"),
    EAG_LQAS("EAG-LQAS", "EAG-LQAS"),
    MODALITY_PRE("MODALITY_PRE"),
    MODALITY_POST("MODALITY_POST"),
	VALIDATION("VALIDATION", "VALIDATION"),
	MONITORING("MONITORING", "MONITORING"),
	MICROPLANNING("MICROPLANNING", "MICROPLAN"),
	DISTRICT_READINESS("DISTRICT_READINESS", "DISTRICT READINESS"),
	TF_REPORT("TF_REPORT", "TF REPORT"),
	EVENING_REVIEW("EVENING_REVIEW", "EVENING REVIEW");

    private String displayName;
    private String formAccessValue;


    FormAccess(String displayName) {
        this.displayName = displayName;
    }

    FormAccess(String formAccessValue, String displayName) {
        this.formAccessValue = formAccessValue;
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }

	public void addAssignableForms(Collection<FormAccess> collection) {

		for (FormAccess form : FormAccess.values()) {
			collection.add(form);
		}
	}

	public static Set<FormAccess> getAssignableForms() {
		Set<FormAccess> result = EnumSet.allOf(FormAccess.class);
		return result;
	}

}
