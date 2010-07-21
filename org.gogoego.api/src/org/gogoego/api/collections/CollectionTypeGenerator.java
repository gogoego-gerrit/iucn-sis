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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.ElementCollection;
import com.solertium.util.SchemaValidator;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.portable.IDValidator;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.VFSPath;

/**
 * Use this to programatically create collection types (views) 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, 
 * <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class CollectionTypeGenerator {
	
	public static void main(String[] args) {
		final VFS vfs;
		try {
			vfs = VFSFactory.getVersionedVFS(new File("/var/gge/lima/newway/vfs"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		final Document document;
		try {
			document = vfs.getMutableDocument(new VFSPath("/(SYSTEM)/views/test.xml"));
		} catch (IOException e) {
			throw new RuntimeException(e);			
		}
		
		document.getDocumentElement().setAttribute("id", "test2");
		
		try {
			create(vfs, document);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (ConflictException impossible) {
			TrivialExceptionHandler.impossible(vfs, impossible);
		}
	}
	
	/**
	 * Create a new collection type.  Will validate your XML document 
	 * against our schema, and if it passes, will be registered.
	 * 
	 * @param vfs
	 * @param document the view document
	 * @throws IllegalArgumentException if the document fails schema validation
	 * 
	 * @see views.xsd for schema information
	 */
	public static void create(VFS vfs, Document document) throws IllegalArgumentException, ConflictException {
		final InputStream viewXSD = CollectionTypeGenerator.class.getResourceAsStream("views.xsd");
		final Reader docReader = new StringReader(BaseDocumentUtils.impl.serializeDocumentToString(document));
		if (SchemaValidator.isValid(viewXSD, docReader)) {
			final String viewID = document.getDocumentElement().getAttribute("id");
			final String viewTitle = document.getDocumentElement().getAttribute("title");
			
			final IDValidator validator = new IDValidator();
			validator.setLowercaseRestriction(false);
			validator.setMustBeAlphaNumeric(true);
			validator.setMustBeginWithLetter(true);
			validator.setNoWhiteSpace(true);
			
			final IDValidator.ValidationInfo response = validator.validate(viewID);
			if (!response.isValid()) {
				final StringBuilder builder = new StringBuilder();
				builder.append("The following errors were found: ");
				
				for (Iterator<Integer> iter = response.getErrors().iterator(); iter.hasNext(); ) {
					builder.append(validator.getStandardErrorMessage(iter.next()));
					if (iter.hasNext())
						builder.append(", ");
				}
				
				if (response.getSuggestedID() != null)
					builder.append("The suggested ID is " + response.getSuggestedID());
				
				throw new IllegalArgumentException(builder.toString());
			}
			
			//Check to see if this is already a registered type...
			Document registry;
			try {
				registry = vfs.getMutableDocument(new VFSPath("/(SYSTEM)/registry/collectiontypes.xml"));
			} catch (IOException e) {
				return;
			}
			
			boolean found = false;
			final ElementCollection nodes = new ElementCollection(registry.getDocumentElement().getElementsByTagName("content"));
			for (Element node : nodes)
				if (found = viewID.equals(node.getAttribute("class")))
					break;
			
			if (found)
				throw new ConflictException("The view " + viewID + " is already registered.");
					
			//Write the document to file
			if (DocumentUtils.writeVFSFile("/(SYSTEM)/views/" + viewID + ".xml", vfs, document)) {
				//Update the registry
				final Element defaultView = registry.createElement("defaultview");
				defaultView.setAttribute("uri", "/admin/files/(SYSTEM)/views/" + viewID + ".xml");
				
				final Element newView = registry.createElement("content");
				newView.setAttribute("class", viewID);
				newView.setAttribute("title", viewTitle);
				newView.appendChild(defaultView);
				
				registry.getDocumentElement().appendChild(newView);
				
				DocumentUtils.writeVFSFile("/(SYSTEM)/registry/collectiontypes.xml", vfs, registry);
			}
		}
		else
			throw new IllegalArgumentException("Document does not validate to schema.");
	}

}
