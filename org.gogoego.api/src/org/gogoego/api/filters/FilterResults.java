/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package org.gogoego.api.filters;

import org.restlet.representation.Representation;

/**
 * FilterResults.java
 * 
 * Holds the results of a FileWritingFilter filter operation.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class FilterResults {
	
	private final boolean success;
	private String errorMessage;
	
	private Representation updatedRepresentation;
	
	/**
	 * Create a filter results with a simple true or false for success 
	 * @param success 
	 */
	public FilterResults(boolean success) {
		this.success = success;
	}
	
	/**
	 * Filter results with an updated representation.
	 * 
	 * @param updatedRepresentation
	 */
	public FilterResults(boolean success, Representation updatedRepresentation) {
		this(success);
		this.updatedRepresentation = updatedRepresentation;
	}
	
	/**
	 * Filter with an error message, meaning that this filter 
	 * automatically failed
	 * @param errorMessage
	 */
	public FilterResults(String errorMessage) {
		this(false);
		this.errorMessage = errorMessage;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public Representation getUpdatedRepresentation() {
		return updatedRepresentation;
	}
	
	/**
	 * Set an updated representation
	 * @param updatedRepresentation
	 */
	public void setUpdatedRepresentation(Representation updatedRepresentation) {
		this.updatedRepresentation = updatedRepresentation;
	}

}
