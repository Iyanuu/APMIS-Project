/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.backend.infrastructure.community;

import static de.symeda.sormas.api.utils.FieldConstraints.CHARACTER_LIMIT_DEFAULT;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import de.symeda.sormas.api.infrastructure.community.Modality;
import de.symeda.sormas.api.infrastructure.community.Status;
import de.symeda.sormas.backend.common.InfrastructureAdo;
import de.symeda.sormas.backend.infrastructure.district.District;

@Entity
public class Community extends InfrastructureAdo {

	private static final long serialVersionUID = 1971053920357795693L;

	public static final String TABLE_NAME = "community";

	public static final String NAME = "name";
	public static final String FA_AF = "fa_af";
	public static final String PS_AF = "ps_af";
	public static final String DISTRICT = "district";
	public static final String GROWTH_RATE = "growthRate";
	public static final String EXTERNAL_ID = "externalId";
	public static final String CLUSTER_NUMBER = "clusterNumber";
	public static final String FLOATING_STATUS = "floating";
	public static final String INTERNATIONAL_BORDER = "internationalborder";
	public static final String POPULATIONDATA_0_4 = "populationdata_0_4";
	public static final String POPULATIONDATA_5_10 = "populationdata_5_10";
	public static final String POPULATIONDATA_4_23M = "populationdata_4_23M";
	public static final String POPULATIONDATA_4_59M = "populationdata_4_59M";
	public static final String MODALITY = "modality";
	public static final String STATUS = "status";



	private String name;
	private String fa_af;
	private String ps_af;
	private District district;
	private Float growthRate;
	private Long externalId;
	private Integer clusterNumber;
	private String floating;
	private Modality modality;
	private Status status;
	
	private boolean internationalBorder;
	
	private Long populationdata_0_4;
	private Long populationdata_5_10;
	private Long populationdata_4_23M;
	private Long populationdata_4_59M;



	public String getName() {
			return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getFa_af() {
		return fa_af;
	}

	public void setFa_af(String fa_af) {
		this.fa_af = fa_af;
	}

	public String getPs_af() {
		return ps_af;
	}

	public void setPs_af(String ps_af) {
		this.ps_af = ps_af;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, optional = false)
	@JoinColumn(nullable = false)
	public District getDistrict() {
		return district;
	}

	public void setDistrict(District district) {
		this.district = district;
	}

	public Float getGrowthRate() {
		return growthRate;
	}

	public void setGrowthRate(Float growthRate) {
		this.growthRate = growthRate;
	}

	//@Column(length = CHARACTER_LIMIT_DEFAULT)
	public Long getExternalId() {
		return externalId;
	}

	public void setExternalId(Long externalId) {
		this.externalId = externalId;
	}

	public Integer getClusterNumber() {
		return clusterNumber;
	}

	public void setClusterNumber(Integer clusterNumber) {
		this.clusterNumber = clusterNumber;
	}
	
	

	public String getFloating() {
		return floating;
	}

	public void setFloating(String floating) {
		this.floating = floating;
	}
	
	

	public boolean isInternationalBorder() {
		return internationalBorder;
	}

	public void setInternationalBorder(boolean internationalBorder) {
		this.internationalBorder = internationalBorder;
	}
	

	public Long getPopulationdata_0_4() {
		return populationdata_0_4;
	}

	public void setPopulationdata_0_4(Long populationdata_0_4) {
		this.populationdata_0_4 = populationdata_0_4;
	}

	public Long getPopulationdata_5_10() {
		return populationdata_5_10;
	}

	public void setPopulationdata_5_10(Long populationdata_5_10) {
		this.populationdata_5_10 = populationdata_5_10;
	}
	
	public Long getPopulationdata_4_23M() {
		return populationdata_4_23M;
	}

	public void setPopulationdata_4_23M(Long populationdata_4_23M) {
		this.populationdata_4_23M = populationdata_4_23M;
	}
	
	public Long getPopulationdata_4_59M() {
		return populationdata_4_59M;
	}

	public void setPopulationdata_4_59M(Long populationdata_4_59M) {
		this.populationdata_4_59M = populationdata_4_59M;
	}

	
	@Enumerated(EnumType.STRING)
	public Modality getModality() {
		return modality;
	}
	
	public void setModality(Modality modality) {
		this.modality = modality;
	}
	
	@Enumerated(EnumType.STRING)
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}



