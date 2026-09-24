package de.symeda.sormas.api.document;

import java.io.Serializable;

public class DocumentUploadRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private DocumentDto document;
	private String base64Content;

	public DocumentDto getDocument() {
		return document;
	}

	public void setDocument(DocumentDto document) {
		this.document = document;
	}

	public String getBase64Content() {
		return base64Content;
	}

	public void setBase64Content(String base64Content) {
		this.base64Content = base64Content;
	}
}
