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

package org.gogoego.api.collections;

import org.w3c.dom.Document;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.MD5Hash;

/**
 * GenericCollection.java
 * 
 * Implemenation of a generic collection.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GenericCollection extends GoGoEgoCollection {
	
	private GenericCollectionParser parser;

	public GenericCollection(final Document document) {
		super(null);
		convertFromXMLDocument(document);
	}

	public GenericCollection(final String collectionID, final String name) {
		super(collectionID, name);
		parser = GenericCollectionParserFactory.getParser(this, "1");
	}

	public void addItem(final GoGoEgoItem item) {
		if (item.getItemID() != null)
			items.put(item.getItemID(), item);
		else {
			items.put("$RI$" + new MD5Hash(((ReferenceItem) item).getItemURI()).toString(), item);
		}
	}

	/**
	 * Builds the item & collection listing. EVERYTHING WILL BE NULL, IF YOU
	 * WANT SOMETHING YOU MUST PULL IT FROM THE VFS. But you'll know its there.
	 */
	public void convertFromXMLDocument(final Document document) {
		parser = GenericCollectionParserFactory.getParser(this, 
			BaseDocumentUtils.impl.getAttribute(document.getDocumentElement(), "version"));
		if (parser.isValid(document)) {
			subCollections.clear();
			items.clear();
			data.clear();
			options.clear();
			
			parser.parse(document);
		}
	}
	
	public void addCustomFieldData(CustomFieldData cfd) {
		if (cfd != null)
			data.put(cfd.getName(), cfd);
	}
	
	public void addOption(String optionName, String optionValue) {
		options.put(optionName, optionValue);
	}

	@Override
	public String toXML() {
		return parser.write();
	}

}
