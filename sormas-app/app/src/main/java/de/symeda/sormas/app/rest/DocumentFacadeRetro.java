package de.symeda.sormas.app.rest;

import de.symeda.sormas.api.document.DocumentDto;
import de.symeda.sormas.api.document.DocumentUploadRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DocumentFacadeRetro {

	@POST("documents/push")
	Call<DocumentDto> pushDocument(@Body DocumentUploadRequest request);
}
