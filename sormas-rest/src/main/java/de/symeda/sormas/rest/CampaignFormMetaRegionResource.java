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
import de.symeda.sormas.api.campaign.form.CampaignFormMetaRegionDto;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.infrastructure.PopulationDataReferenceDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;

@Path("/campaignforms_region")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RolesAllowed({ "USER", "REST_USER" })
public class CampaignFormMetaRegionResource extends EntityDtoResource {

	@GET
	@Path("/all/{since}")
	public List<CampaignFormMetaRegionDto> getAllCampaignFormMeta(@PathParam("since") long since) {
		AreaReferenceDto rdto = FacadeProvider.getUserFacade().getCurrentUser().getArea();

//		List<CampaignFormMetaRegionDto> formsByRegion = new ArrayList<>();

		List<String> result = new ArrayList<>();

		if (rdto != null) {

			result.add(rdto.getUuid());
		}
		return FacadeProvider.getCampaignFormMetaRegionFacade().getCampaignFormsByUserRegion(result);

	}

	@POST
	@Path("/query")
	public List<CampaignFormMetaRegionDto> getByUuids(List<String> uuids) {

		List<CampaignFormMetaRegionDto> result = FacadeProvider.getCampaignFormMetaRegionFacade()
				.getCampaignFormsByUserRegion(uuids);
		return result;
	}

	@POST
	@Path("/getByUuids")
	public List<CampaignFormMetaRegionDto> getByUuidsAlias(List<String> uuids) {
		return getByUuids(uuids);
	}

	@GET
	@Path("/uuids")
	public List<String> getAllUuids() {
		AreaReferenceDto rdto = FacadeProvider.getUserFacade().getCurrentUser().getArea();

//		List<CampaignFormMetaRegionDto> formsByRegion = new ArrayList<>();

		List<String> result = new ArrayList<>();

		if (rdto != null) {

			result.add(rdto.getUuid());
		} else {

//			retListx.add(rdtox.getUuid());

			
		}
//		return FacadeProvider.getCampaignFormMetaRegionFacade().getCampaignFormsByUserRegion(result);
		return result;

	}

	@GET
	@Path("/selectedCampaignFormMetaByRegion")
	public List<CampaignFormMetaRegionDto> fetchCampaignFormMetaByRegion() {
		AreaReferenceDto rdto = FacadeProvider.getUserFacade().getCurrentUser().getArea();


		System.out.println(FacadeProvider.getUserFacade().getCurrentUser()
				+ "Request Hits Form Meta With Expiry  population data by district  ==========================111111111"
				+ rdto.getUuid());

		List<String> resultx = new ArrayList<>();
		System.out.println("  ==========================111111111cccc");

		if (rdto != null) {
				resultx.add(rdto.getUuid());
			
			List<CampaignFormMetaRegionDto> result = FacadeProvider.getCampaignFormMetaRegionFacade().getCampaignFormsByUserRegion(resultx);

			System.out.println("  ==========================111111111ccccvv" + result);

			return result;

		} else {
			
			List<CampaignFormMetaRegionDto> result = new ArrayList<>();
//			resultx.add(rdtox.getUuid());
//
//			List<PopulationDataDto> result = FacadeProvider.getPopulationDataFacade()
//					.fetchPopulationDataSelectionByUserDistricts(resultx);
//			System.out.println("  ==========================111111111ccccvv" + result);

			return result;
		}

	}
}
