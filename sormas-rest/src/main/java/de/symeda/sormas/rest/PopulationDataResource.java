package de.symeda.sormas.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaExpiryDto;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.infrastructure.PopulationDataReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.user.UserRole;

@Path("/populationData")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RolesAllowed({ "USER", "REST_USER" })
public class PopulationDataResource  extends EntityDtoResource {

	@GET
	@Path("/all/{since}")
	public List<PopulationDataDto> getAllPopulationData(@PathParam("since") long since) {
		final Set<DistrictReferenceDto> rdto = FacadeProvider.getUserFacade().getCurrentUser().getDistricts();
		final DistrictReferenceDto rdtox = FacadeProvider.getUserFacade().getCurrentUser().getDistrict();
		List<String> result = new ArrayList<>();
			if(rdto != null && rdto.size() < 1) {
			result.add(rdtox.getUuid());
			
	
		
		}
			return FacadeProvider.getPopulationDataFacade()
					.fetchPopulationDataSelectionByUserDistricts(result);
	}

	@POST
	@Path("/query")
	public List<PopulationDataDto> getByUuids(List<String> uuids) {

		List<PopulationDataDto> result = FacadeProvider.getPopulationDataFacade()
				.fetchPopulationDataSelectionByUserDistricts(uuids);
		return result;
	}

	@POST
	@Path("/getByUuids")
	public List<PopulationDataDto> getByUuidsAlias(List<String> uuids) {
		return getByUuids(uuids);
	}

	@GET
	@Path("/uuids")
	public List<String> getAllUuids() {
		final Set<DistrictReferenceDto> rdto = FacadeProvider.getUserFacade().getCurrentUser().getDistricts();
		final DistrictReferenceDto rdtox = FacadeProvider.getUserFacade().getCurrentUser().getDistrict();

		if(rdto != null && rdto.size() > 0) {
			return FacadeProvider.getDistrictFacade().getAllUuids().stream()
					.filter(e -> rdto.stream().anyMatch(ee -> e.equals(ee.getUuid()))).collect(Collectors.toList());
		} else {
			
			List<String> retListx = new ArrayList<>();
			System.out.println(retListx + "retListxretListxretListxretListxbefore------");
			
			if(rdtox != null) {
				System.out.println(rdtox.getCaption() + "retListxretListxretListxretListxbefore------");
				retListx.add(rdtox.getUuid());
			}else {
				

				retListx.add(rdtox.getUuid());
				System.out.println(retListx + "retListxretListxretListxretListxafter ------");
			}
			


			return retListx;
		
		}
	}
	
	@GET
	@Path("/selectedDistricts")
	public List<PopulationDataDto> fetchPopulationDataSelectionByUserDistricts() {
		List<String> retListx = getAllUuids();
		System.out.println(retListx  +  "retListxretListxretListx _-----------------------");
		
		
		final Set<DistrictReferenceDto> rdto = FacadeProvider.getUserFacade().getCurrentUser().getDistricts();
		final DistrictReferenceDto rdtox = FacadeProvider.getUserFacade().getCurrentUser().getDistrict();

		System.out.println(FacadeProvider.getUserFacade().getCurrentUser() + "Request Hits Form Meta With Expiry  population data by district  ==========================111111111");
		
		
		System.out.println(FacadeProvider.getUserFacade().getCurrentUser() + "Request Hits Form Meta With Expiry  population data by c  ==========================111111111");

		List<String> resultx = new ArrayList<>();
//		System.out.println("  ==========================111111111cccc"+ rdto.size());
//		System.out.println("  ==========================111111111cccc"+ rdtox.getCaption());
//

		if(retListx != null && retListx.size() > 1) {
			for(String district : retListx) {
				resultx.add(district);
			}
			List<PopulationDataDto> result = FacadeProvider.getPopulationDataFacade()
					.fetchPopulationDataSelectionByUserDistricts(resultx);


			return result;
			

		} else if(retListx != null && retListx.size() == 1) {
			
			Set<UserRole> roles = FacadeProvider.getUserFacade().getCurrentUser().getUserRoles();
if(roles.contains(UserRole.COMMUNITY_OFFICER)) {
	System.out.println("  ==========================111111111cccccommunity officer "+ rdto.size());

	if(rdtox != null) {
		resultx.add(rdtox.getUuid());
	}
	
	System.out.println("  ==========================111111111cccccommunity resultx "+ resultx);

}else if(roles.contains(UserRole.SURVEILLANCE_OFFICER)) {
	for(String districtUUid : retListx) {
		resultx.add(districtUUid);
	}
}

System.out.println("  ==========================111111111cccccommunity resultx before "+ resultx);

	List<PopulationDataDto> result = FacadeProvider.getPopulationDataFacade()
					.fetchPopulationDataSelectionByUserDistricts(resultx);

	System.out.println("  ==========================111111111cccccommunity resultx result "+ result);

			return result;
			

		}else {
			
			resultx.add(rdtox.getUuid());
			
			List<PopulationDataDto> result = FacadeProvider.getPopulationDataFacade()
					.fetchPopulationDataSelectionByUserDistricts(resultx);
			System.out.println("  ==========================333331111ccccvv" + result);

			return result;
		}
		
	}
}
