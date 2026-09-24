package de.symeda.sormas.api.campaign;

import java.io.Serializable;

@SuppressWarnings("serial")

 public class CampaignTreeGridDtoImpl extends CampaignTreeGridDto implements Serializable{
    private static final long serialVersionUID = 1L;


	private Long populationData;
	private Long populationData5_10;
	private Long populationData4_23M;
	private Long populationData4_59M;
	private Long populationDataTotal;
	
	private String savedSelectionData;
	
	public CampaignTreeGridDtoImpl(String name, Long populationData, Long id, String parentUuid, String uuid, String levelAssessed, String savedSelectionData) {
        super(name, id, parentUuid, uuid, levelAssessed);
        this.populationData = populationData;
        this.savedSelectionData = savedSelectionData;
    }
	
	public CampaignTreeGridDtoImpl(String name, Long populationData, Long id, String parentUuid, String uuid, String levelAssessed, String savedSelectionData, String districtModality, String districtStatus, String ageGroup) {
        super(name, id, parentUuid, uuid, levelAssessed, districtModality, districtStatus);
        this.populationData = populationData;
        this.savedSelectionData = savedSelectionData;
    }
	public CampaignTreeGridDtoImpl(String name, Long populationData, Long populationData5_10, Long id, String parentUuid, String uuid, String levelAssessed, String savedSelectionData, String districtModality, String districtStatus) {
        super(name, id, parentUuid, uuid, levelAssessed, districtModality, districtStatus);
        this.populationData = populationData;
        this.populationData5_10 = populationData5_10;

        this.savedSelectionData = savedSelectionData;
    }
	
	public CampaignTreeGridDtoImpl(String name, Long populationData, Long populationData5_10, Long id, String parentUuid, String uuid, String levelAssessed, String savedSelectionData, String districtModality, String districtStatus, Long populationDataTotal) {
        super(name, id, parentUuid, uuid, levelAssessed, districtModality, districtStatus);
        this.populationData = populationData;
        this.populationData5_10 = populationData5_10;
        this.savedSelectionData = savedSelectionData;
        this.populationDataTotal = populationData + populationData5_10;
    }
	
	public CampaignTreeGridDtoImpl(String name, Long populationData, Long populationData5_10, Long id, String parentUuid, String uuid, String levelAssessed, String savedSelectionData, String districtModality, String districtStatus, String floatStatus, Long populationDataTotal) {
        super(name, id, parentUuid, uuid, levelAssessed, districtModality, districtStatus, floatStatus);
        this.populationData = populationData;
        this.populationData5_10 = populationData5_10;
        this.savedSelectionData = savedSelectionData;
        this.populationDataTotal = populationData + populationData5_10;
    }
	
	public CampaignTreeGridDtoImpl(String name, Long populationData, Long populationData5_10, Long populationData4_23M, Long id, String parentUuid, String uuid, String levelAssessed, String savedSelectionData, String districtModality, String districtStatus, String floatStatus, Long populationDataTotal) {
        super(name, id, parentUuid, uuid, levelAssessed, districtModality, districtStatus, floatStatus);
        this.populationData = populationData;
        this.populationData5_10 = populationData5_10;
        this.populationData4_23M = populationData4_23M;
        this.savedSelectionData = savedSelectionData;
        this.populationDataTotal = populationData + populationData5_10;
    }
	
	public CampaignTreeGridDtoImpl(String name, Long populationData, Long populationData5_10, Long populationData4_23M, Long id, String parentUuid, String uuid, String levelAssessed, boolean isSelected, String districtModality, String districtStatus, String floatStatus, Long populationDataTotal) {
        super(name, id, parentUuid, uuid, levelAssessed, isSelected, districtModality, districtStatus, floatStatus);
        this.populationData = populationData;
        this.populationData5_10 = populationData5_10;
        this.populationData4_23M = populationData4_23M;
        this.populationDataTotal = populationData + populationData5_10;
    }
	
	public CampaignTreeGridDtoImpl(String name, Long populationData, Long populationData5_10, Long populationData4_23M, Long populationData4_59M, Long id, String parentUuid, String uuid, String levelAssessed, boolean isSelected, String districtModality, String districtStatus, String floatStatus, Long populationDataTotal) {
        super(name, id, parentUuid, uuid, levelAssessed, isSelected, districtModality, districtStatus, floatStatus);
        this.populationData = populationData;
        this.populationData5_10 = populationData5_10;
        this.populationData4_23M = populationData4_23M;
        this.populationData4_59M = populationData4_59M;
        this.populationDataTotal = populationData + populationData5_10 + populationData4_23M + populationData4_59M;
    }

	
	
	 @Override
     public Long getPopulationData() {
         return populationData;
     }
	 
	 @Override
     public Long getPopulationData5_10() {
         return populationData5_10;
     }
	 
	 @Override
     public Long getPopulationData4_23M() {
         return populationData4_23M;
     }
	 
	 @Override
     public Long getPopulationData4_59M() {
         return populationData4_59M;
     }
	 
	 public Long getPopulationDataTotal() {
         return populationDataTotal;
     }
	 
	 @Override
     public String getSavedData() {
         return savedSelectionData;
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

	 public void setPopulationDataTotal(Long populationDataTotal) {
		 this.populationDataTotal = populationDataTotal;
	 }
	 
}
