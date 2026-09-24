package de.symeda.sormas.api.campaign.data;

import java.io.Serializable;

public class ImageUploadRequest implements Serializable {

    private String campaignFormDataUuid;
    private CampaignFormImageValue imageValue;
    private String base64Content;

    public String getCampaignFormDataUuid() {
        return campaignFormDataUuid;
    }

    public void setCampaignFormDataUuid(String campaignFormDataUuid) {
        this.campaignFormDataUuid = campaignFormDataUuid;
    }

    public CampaignFormImageValue getImageValue() {
        return imageValue;
    }

    public void setImageValue(CampaignFormImageValue imageValue) {
        this.imageValue = imageValue;
    }

    public String getBase64Content() {
        return base64Content;
    }

    public void setBase64Content(String base64Content) {
        this.base64Content = base64Content;
    }
}