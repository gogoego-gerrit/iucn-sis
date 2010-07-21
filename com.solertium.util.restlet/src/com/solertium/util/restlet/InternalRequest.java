/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.util.restlet;

import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.representation.Representation;

public class InternalRequest extends Request {

	private final Request parentRequest;

	public InternalRequest(final Request parentRequest) {
		super();
		this.parentRequest = parentRequest;
		setChallengeResponse(parentRequest.getChallengeResponse());
	}
	
	public InternalRequest(final Request parentRequest, final Method method,
			final String resourceUri) {
		this(parentRequest, method, new Reference(resourceUri));
	}

	public InternalRequest(final Request parentRequest, final Method method,
			final String resourceUri, final Representation entity) {
		this(parentRequest, method, new Reference(resourceUri), entity);
	}

	public InternalRequest(final Request parentRequest, final Method method,
			final Reference resourceRef) {
		this(parentRequest, method, resourceRef, null);
	}

	public InternalRequest(final Request parentRequest, final Method method,
			final Reference resourceRef, final Representation entity) {
		super(method, resourceRef, entity);
		this.parentRequest = parentRequest;
		setChallengeResponse(parentRequest.getChallengeResponse());
	}

	/**
	 * Returns the original, external request that first invoked
	 * the current chain of internal requests.
	 */
	public final Request getFirstRequest() {
		final Request parent = getParentRequest();
		if (parent instanceof InternalRequest)
			return ((InternalRequest) parent).getFirstRequest();
		return parent;
	}

	/**
	 * Returns the immediate parent of the current internal request.
	 */
	public final Request getParentRequest() {
		return parentRequest;
	}

}
