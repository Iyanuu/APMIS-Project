package de.symeda.sormas.backend.location;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;

@Entity
public class Location extends AbstractDomainObject {
	
	private static final long serialVersionUID = 392776645668778670L;

	private String address;
	private String details;
	
	private Region region;
	private District district;
	private Community community;
	
	private Float latitude;
	private Float longitude;

	
	@Column(length = 255)
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

	@Column(length = 255)
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}

	@ManyToOne(cascade = {})
	public Region getRegion() {
		return region;
	}
	public void setRegion(Region region) {
		this.region = region;
	}

	@ManyToOne(cascade = {})
	public District getDistrict() {
		return district;
	}
	public void setDistrict(District district) {
		this.district = district;
	}

	@ManyToOne(cascade = {})
	public Community getCommunity() {
		return community;
	}
	public void setCommunity(Community community) {
		this.community = community;
	}
	
	@Column(columnDefinition = "float8")
	public Float getLatitude() {
		return latitude;
	}
	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}
	
	@Column(columnDefinition = "float8")
	public Float getLongitude() {
		return longitude;
	}
	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}
}
