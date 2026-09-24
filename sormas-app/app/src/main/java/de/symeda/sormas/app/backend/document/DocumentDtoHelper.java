package de.symeda.sormas.app.backend.document;

import java.util.List;

import android.util.Base64;
import android.util.Log;

import de.symeda.sormas.api.PushResult;
import de.symeda.sormas.api.document.DocumentDto;
import de.symeda.sormas.api.document.DocumentRelatedEntityType;
import de.symeda.sormas.api.document.DocumentUploadRequest;
import de.symeda.sormas.app.backend.common.AbstractAdoDao;
import de.symeda.sormas.app.backend.common.AbstractDomainObject;
import de.symeda.sormas.app.backend.common.AdoDtoHelper;
import de.symeda.sormas.app.backend.common.DaoException;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.user.UserDtoHelper;
import de.symeda.sormas.app.rest.NoConnectionException;
import de.symeda.sormas.app.rest.RetroProvider;
import de.symeda.sormas.app.rest.ServerCommunicationException;
import de.symeda.sormas.app.rest.ServerConnectionException;
import retrofit2.Call;
import retrofit2.Response;

public class DocumentDtoHelper extends AdoDtoHelper<Document, DocumentDto> {

	@Override
	protected Class<Document> getAdoClass() {
		return Document.class;
	}

	@Override
	protected Class<DocumentDto> getDtoClass() {
		return DocumentDto.class;
	}

	@Override
	protected Call<List<DocumentDto>> pullAllSince(long since) throws NoConnectionException {
		// Documents are only pushed from the device; pulling is not needed for campaign images.
		return null;
	}

	@Override
	protected Call<List<DocumentDto>> pullByUuids(List<String> uuids) throws NoConnectionException {
		return null;
	}

	@Override
	protected Call<List<PushResult>> pushAll(List<DocumentDto> dtos) throws NoConnectionException {
		// Not used; documents are pushed one-by-one in pushEntities because each carries its own content.
		return null;
	}

	@Override
	public boolean pushEntities(boolean onlyNewEntities)
		throws DaoException, ServerConnectionException, ServerCommunicationException, NoConnectionException {
		final AbstractAdoDao<Document> dao = DatabaseHelper.getAdoDao(getAdoClass());

		// Documents are created locally with modified=true and accept() clears it after a successful push.
		final List<Document> modified = dao.queryForEq(AbstractDomainObject.MODIFIED, true);

		if (modified == null || modified.isEmpty()) {
			return false;
		}

		boolean pushed = false;
		for (Document doc : modified) {
			if (doc.getContent() == null || doc.getContent().length == 0) {
				continue;
			}

			try {
				DocumentDto dto = adoToDto(doc);

				DocumentUploadRequest request = new DocumentUploadRequest();
				request.setDocument(dto);
				request.setBase64Content(Base64.encodeToString(doc.getContent(), Base64.NO_WRAP));

				Response<DocumentDto> response = RetroProvider.getDocumentFacade().pushDocument(request).execute();
				if (response.isSuccessful() && response.body() != null) {
					dao.accept(doc);           // mark as synchronized (keep the BLOB so the image stays viewable)
					pushed = true;
				} else {
					RetroProvider.throwException(response);
				}
			} catch (Exception e) {
				Log.e("DocumentPush", "Failed to push document " + doc.getUuid(), e);
			}
		}

		return pushed;
	}

	@Override
	protected void fillInnerFromDto(Document target, DocumentDto source) {
		target.setUploadingUser(DatabaseHelper.getUserDao().getByReferenceDto(source.getUploadingUser()));
		target.setRelatedEntityUuid(source.getRelatedEntityUuid());
		target.setRelatedEntityType(source.getRelatedEntityType() != null ? source.getRelatedEntityType().name() : null);
		target.setName(source.getName());
		target.setMimeType(source.getMimeType());
		target.setSize(source.getSize());
	}

	@Override
	protected void fillInnerFromAdo(DocumentDto target, Document source) {
		target.setUploadingUser(UserDtoHelper.toReferenceDto(source.getUploadingUser()));
		target.setRelatedEntityUuid(source.getRelatedEntityUuid());
		target.setRelatedEntityType(source.getRelatedEntityType() != null ? DocumentRelatedEntityType.valueOf(source.getRelatedEntityType()) : null);
		target.setName(source.getName());
		target.setMimeType(source.getMimeType());
		target.setSize(source.getSize());
	}
}
