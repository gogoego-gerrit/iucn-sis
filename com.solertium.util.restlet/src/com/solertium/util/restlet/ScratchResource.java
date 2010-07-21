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
package com.solertium.util.restlet;

import java.io.Serializable;
import java.util.Date;

import org.restlet.data.Reference;

public class ScratchResource implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Reference reference;
	private String owner;
	private Date expires;
	private Object resource;
	
	public ScratchResource(
			Reference reference,
			String owner,
			Date expires,
			Object resource
	){
		this.reference = reference;
		this.owner = owner;
		this.expires = expires;
		this.resource = resource;
	}
	
	public Reference getReference() {
		return reference;
	}
	public void setReference(Reference reference) {
		this.reference = reference;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public Date getExpires() {
		return expires;
	}
	public void setExpires(Date expires) {
		this.expires = expires;
	}
	public Object getResource() {
		return resource;
	}
	public void setResource(Object resource) {
		this.resource = resource;
	}

}
