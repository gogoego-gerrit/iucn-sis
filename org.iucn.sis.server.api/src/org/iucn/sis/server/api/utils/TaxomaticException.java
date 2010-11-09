package org.iucn.sis.server.api.utils;

import org.w3c.dom.Document;

import com.solertium.util.BaseDocumentUtils;

public class TaxomaticException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private final boolean clientError;
	
	public TaxomaticException(String message) {
		this(message, true);
	}
	
	public TaxomaticException(String message, boolean isClientError) {
		super(message);
		this.clientError = isClientError;
	}
	
	public TaxomaticException(Throwable e) {
		this("Unexpected server error occured.", e);
	}
	
	public TaxomaticException(String message, Throwable e) {
		super(message, e);
		this.clientError = false;
	}
	
	public boolean isClientError() {
		return clientError;
	}
	
	public Document getErrorAsDocument() {
		return BaseDocumentUtils.impl.createErrorDocument(getMessage());
	}

}
