package de.symeda.sormas.rest;

import java.util.Base64;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.document.DocumentDto;
import de.symeda.sormas.api.document.DocumentUploadRequest;

@Path("/documents")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RolesAllowed({ "USER", "REST_USER" })
public class DocumentResource {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@POST
	@Path("/push")
	@Consumes(MediaType.APPLICATION_JSON)
	public DocumentDto pushDocument(DocumentUploadRequest request) {
		if (request == null || request.getDocument() == null || request.getBase64Content() == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		try {
			byte[] bytes = Base64.getDecoder().decode(request.getBase64Content());
			return FacadeProvider.getDocumentFacade().saveDocument(request.getDocument(), bytes);
		} catch (Exception e) {
			logger.error("Document upload failed", e);
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
