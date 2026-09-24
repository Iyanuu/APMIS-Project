package de.symeda.sormas.api.campaign.form;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public enum CampaignFormMetaGeographyLevel {
	
	REGION("REGION"),
    PROVINCE("PROVINCE"),
    DISTRICT("DISTRICT"),
    CLUSTER("CLUSTER");


    private String displayName;

    CampaignFormMetaGeographyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

	public void addAssignableForms(Collection<CampaignFormMetaGeographyLevel> collection) {

		for (CampaignFormMetaGeographyLevel form : CampaignFormMetaGeographyLevel.values()) {
			collection.add(form);
		}
	}

	public static Set<CampaignFormMetaGeographyLevel> getAssignableForms() {
		Set<CampaignFormMetaGeographyLevel> result = EnumSet.allOf(CampaignFormMetaGeographyLevel.class);

		return result;
	}

}
