package org.iucn.sis.server.extensions.integrity;

import java.util.Collection;

/**
 * IntegrityValidationResponse.java
 * 
 * The validation results. If validation failed, an error message will be made
 * available.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public class IntegrityValidationResponse {

	private final boolean isSuccess;
	private final Collection<String> errorMessages;
	
	private String successMessage;

	public IntegrityValidationResponse() {
		isSuccess = true;
		this.successMessage = "This assessment is valid.";
		this.errorMessages = null;
	}

	public IntegrityValidationResponse(Collection<String> errorMessages) {
		isSuccess = false;
		this.errorMessages = errorMessages;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public Collection<String> getErrorMessages() {
		return errorMessages;
	}

	public void append(Collection<String> errorMessages) {
		this.errorMessages.addAll(errorMessages);
	}
	
	public void setSuccessMessage(String successMessage) {
		this.successMessage = successMessage;
	}
	
	public String getSuccessMessage() {
		return successMessage;
	}

}
