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
package org.gogoego.api.representations;

import org.restlet.data.MediaType;
import org.w3c.dom.Document;

/**
 * GoGoEgoRepresentationTrap.java
 * 
 * The simplest implementation of the Trap for a basic resource.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class GoGoEgoRepresentationTrap extends BaseRepresentationTrap<GoGoEgoBaseRepresentation> {
	
	public GoGoEgoRepresentationTrap(GoGoEgoBaseRepresentation wrapped) {
		super(wrapped);
	}

	public String getContentType() {
		return getWrapped().getContentType();
	}

	public String getContent() {
		return getWrapped().getContent();
	}

	public String getContentById(final String id) {
		return getWrapped().getContentById(id);
	}

	public String getContentByTagName(final String name) {
		return getWrapped().getContentById(name);
	}

	public Document getDocument() {
		return getWrapped().getDocument();
	}

	public MediaType getPreferredMediaType() {
		return getWrapped().getPreferredMediaType();
	}

	public String resolveEL(String key) {
		return getWrapped().resolveEL(key);
	}

}
