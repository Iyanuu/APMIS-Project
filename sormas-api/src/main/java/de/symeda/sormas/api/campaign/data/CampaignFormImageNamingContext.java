package de.symeda.sormas.api.campaign.data;

import java.io.Serializable;

public class CampaignFormImageNamingContext implements Serializable {

	private static final long serialVersionUID = 4860861643966972209L;

	private String region;
	private String province;
	private String district;
	private String clusterNumber;
	private String clusterName;

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getClusterNumber() {
		return clusterNumber;
	}

	public void setClusterNumber(String clusterNumber) {
		this.clusterNumber = clusterNumber;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
}
