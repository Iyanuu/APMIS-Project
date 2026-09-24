package de.symeda.sormas.api.campaign;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.FieldConstraints;

public class CampaignTreeGridDto {
	
	
	private List<CampaignTreeGridDto> regionData = new ArrayList<>();
	private List<CampaignTreeGridDto> districtData = new ArrayList<>();
	private List<CampaignTreeGridDto> clusterData = new ArrayList<>();


	
    private String name;
    private Long id;
    private String parentUuid;
    private String uuid;
    private Long isClicked; //let leave this to a Long type
    private String levelAssessed;
    private String savedData;
    private String districtModality;
    private String districtStatus;
    private String floatStatus;
    private String ageGroup;

    private Boolean selected;
    
    
    
    private Long populationData;
    private Long populationData5_10;
    private Long populationData4_23M;
    private Long populationData4_59M;

    
    
    public CampaignTreeGridDto(String name, Long id, String parentUuid, String uuid, String levelAssessed) {
        this.name = name;
        this.id = id;
        this.parentUuid = parentUuid;
        this.uuid = uuid;
        this.levelAssessed = levelAssessed;
    }
    
    public CampaignTreeGridDto(String name, Long id, String parentUuid, String uuid, String levelAssessed, boolean isSelected) {
        this.name = name;
        this.id = id;
        this.parentUuid = parentUuid;
        this.uuid = uuid;
        this.levelAssessed = levelAssessed;
        this.selected = isSelected;
    }
    
    public CampaignTreeGridDto(String name, Long id, String parentUuid, String uuid, String levelAssessed, String districtModality, String districtStatus) {
        this.name = name;
        this.id = id;
        this.parentUuid = parentUuid;
        this.uuid = uuid;
        this.levelAssessed = levelAssessed;
        this.districtModality = districtModality;
        this.districtStatus = districtStatus;
//        this.ageGroup = ageGroup;
    }
    
    public CampaignTreeGridDto(String name, Long id, String parentUuid, String uuid, String levelAssessed, String districtModality, String districtStatus, String floatStatus) {
        this.name = name;
        this.id = id;
        this.parentUuid = parentUuid;
        this.uuid = uuid;
        this.levelAssessed = levelAssessed;
        this.districtModality = districtModality;
        this.districtStatus = districtStatus;
//        this.ageGroup = ageGroup;
        this.floatStatus = floatStatus;

    }
    
    public CampaignTreeGridDto(String name, Long id, String parentUuid, String uuid, String levelAssessed, boolean isSelected, String districtModality, String districtStatus, String floatStatus) {
        this.name = name;
        this.id = id;
        this.parentUuid = parentUuid;
        this.uuid = uuid;
        this.levelAssessed = levelAssessed;
        this.districtModality = districtModality;
        this.selected = isSelected;
        this.districtStatus = districtStatus;
        this.floatStatus = floatStatus;

    }
    
    

    public CampaignTreeGridDto(String name, Long id, String parentUuid, String uuid) {
        this.name = name;
        this.id = id;
        this.parentUuid = parentUuid;
        this.uuid = uuid;
        }

    public CampaignTreeGridDto() {
		// TODO Auto-generated constructor stub
	}

	public String getName() {
        return name;
    }

    public List<CampaignTreeGridDto> getRegionData() {
        return regionData;
    }

    public void setRegionData(List<CampaignTreeGridDto> regionData) {
        this.regionData = regionData;
    }
    
    public List<CampaignTreeGridDto> getDistrictData() {
        return districtData;
    }

    public void setDistrictData(List<CampaignTreeGridDto> districtData) {
        this.districtData = districtData;
    }	
    
    public List<CampaignTreeGridDto> getClusterData() {
        return clusterData;
    }

    public void setClusterData(List<CampaignTreeGridDto> clusterData) {
        this.clusterData = clusterData;
    }	
    
    
    public void addRegionData(CampaignTreeGridDto regionData_sub) {
    	regionData.add(regionData_sub);
    }
    
    public void addDistrictData(CampaignTreeGridDto districtData_sub) {
    	districtData.add(districtData_sub);
    }
    
    public void addClusterData(CampaignTreeGridDto clusterData_sub) {
    	clusterData.add(clusterData_sub);
    }

    public void setPopulationData(Long populationData) {
		this.populationData = populationData;
	}

	public void setPopulationData5_10(Long populationData5_10) {
		this.populationData5_10 = populationData5_10;
	}

	public void setPopulationData4_23M(Long populationData4_23M) {
		this.populationData4_23M = populationData4_23M;
	}
	
	public void setPopulationData4_59M(Long populationData4_59M) {
		this.populationData4_59M = populationData4_59M;
	}
	
	public Long getPopulationData() {
	    return populationData;
	}
    	
	public Long getPopulationData5_10() {
	    return populationData5_10;
	}
	
	public Long getPopulationData4_23M() {
	    return populationData4_23M;
	}
	
	public Long getPopulationData4_59M() {
	    return populationData4_59M;
	}    
    
    public Long getPopulationDataTotal() {
        return getRegionData().stream()
                .mapToLong(region -> (region.getPopulationData5_10() != null ? region.getPopulationData5_10() : 0) 
                		+ (region.getPopulationData() != null ? region.getPopulationData() : 0)
            	+ (region.getPopulationData4_23M() != null ? region.getPopulationData4_23M() : 0)
            	+ (region.getPopulationData4_59M() != null ? region.getPopulationData4_59M() : 0))
                .reduce(Long::sum)
                .orElse(0L);
    }

        
	public String getSavedData() {
		return savedData;
	}

	public void setSavedData(String savedData) {
		this.savedData = savedData;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentUuid() {
		return parentUuid;
	}

	public void setParentUuid(String parentUuid) {
		this.parentUuid = parentUuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Long getIsClicked() {
		return isClicked;
	}

	public void setIsClicked(Long isClicked) {
		this.isClicked = isClicked;
	}

	public String getLevelAssessed() {
		return levelAssessed;
	}

	public void setLevelAssessed(String levelAssessed) {
		this.levelAssessed = levelAssessed;
	}

	public String getDistrictModality() {
		return districtModality;
	}

	public void setDistrictModality(String districtModality) {
		this.districtModality = districtModality;
	}
	
	public String getDistrictStatus() {
		return districtStatus;
	}
	
	public void setDistrictStatus(String districtStatus) {
		this.districtStatus = districtStatus;
	}
	
	public String getFloatStatus() {
		return floatStatus;
	}

	public void setFloatStatus(String floatStatus) {
		this.floatStatus = floatStatus;
	}

	public String getAgeGroup() {
		return ageGroup;
	}

	public void setAgeGroup(String ageGroup) {
		this.ageGroup = ageGroup;
	}

	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}
    
}

