package de.symeda.sormas.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.PushResult;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataHistoryExtractDto;
import de.symeda.sormas.api.campaign.data.CampaignFormImageValue;
import de.symeda.sormas.api.campaign.data.ImageUploadRequest;
import de.symeda.sormas.api.infrastructure.area.AreaHistoryExtractDto;

import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import java.util.Base64;
import de.symeda.sormas.api.campaign.data.ImageUploadRequest;

@Path("/campaignFormData")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RolesAllowed({
	"USER",
	"REST_USER" })
public class CampaignFormDataResource extends EntityDtoResource {


	@GET
	@Path("/all/{since}")
	public List<CampaignFormDataDto> getAllCampaignFormData(@PathParam("since") long since) {
		return FacadeProvider.getCampaignFormDataFacade().getAllActiveAfter(new Date(since));
	}

	@POST
	@Path("/query")
	public List<CampaignFormDataDto> getByUuids(List<String> uuids) {
		return FacadeProvider.getCampaignFormDataFacade().getByUuids(uuids);
	}

	@POST
	@Path("/push")
	public List<PushResult> postCampaignFormData(@Valid List<CampaignFormDataDto> dtos) {
		System.out.println("FORMDATE FROM MOBILE ");     // extract the name field
		dtos.stream().map(CampaignFormDataDto::getFormDate).forEach(System.out::println);
		return savePushedDto(dtos, FacadeProvider.getCampaignFormDataFacade()::saveCampaignFormDataMobile);
	}

	@GET
	@Path("/uuids")
	public List<String> getAllUuids() {
		return FacadeProvider.getCampaignFormDataFacade().getAllActiveUuids();
	}
	
	@GET
	@Path("/formDataHistory/{since}")
	public List<CampaignFormDataHistoryExtractDto> getRecordsHistory(@PathParam("since") long since, @QueryParam("getRecordHistory") List<String> uuid) {
		System.out.println(uuid + "UUUID from area resource");
		List<CampaignFormDataHistoryExtractDto> ref = new ArrayList<CampaignFormDataHistoryExtractDto>();
//		return ref;
		return FacadeProvider.getCampaignFormDataFacade().getAllActiveAfter(new Date(since), uuid);
	}
	
	@POST
	@Path("/image/upload")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public CampaignFormImageValue uploadImage(ImageUploadRequest request) {
	    if (request == null) {
	        throw new WebApplicationException(Response.Status.BAD_REQUEST);
	    }
	    try {
	        byte[] bytes = Base64.getDecoder().decode(request.getBase64Content());
	        return FacadeProvider.getCampaignFormImageFacade()
	            .uploadImage(request.getCampaignFormDataUuid(), request.getImageValue(), bytes);
	    } catch (Exception e) {
	    	System.out.println("Image upload failed: " + e.getMessage());
//	        logger.error("Image upload failed", e);
	        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
	    }
	}
}
