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

import com.solertium.util.BaseDocumentUtils;

/**
 * GoGoEgoDomRepresentation.java
 * 
 * String implementation of a DOM representation that automatically 
 * parses the document to string once (and only once), as well as 
 * immediately caching the document.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class GoGoEgoDomRepresentation extends GoGoEgoStringRepresentation {
		
	public GoGoEgoDomRepresentation(Document document) {
		this(MediaType.TEXT_XML, document);
	}
	
	public GoGoEgoDomRepresentation(MediaType mediaType, Document document) {
		super(BaseDocumentUtils.impl.serializeDocumentToString(document), mediaType);
		this.document = document;
	}	

}
