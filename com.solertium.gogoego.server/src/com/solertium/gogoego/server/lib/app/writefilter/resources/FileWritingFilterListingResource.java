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
package com.solertium.gogoego.server.lib.app.writefilter.resources;

import java.util.Map;

import org.gogoego.api.filters.FileWritingFilterFactory;
import org.gogoego.api.filters.FileWritingFilterMetadata;
import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.util.BaseDocumentUtils;

public class FileWritingFilterListingResource extends Resource {

	public FileWritingFilterListingResource(Context context, Request request, Response response) {
		super(context, request, response);
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		final Document document = BaseDocumentUtils.impl.newDocument();
		final Element root = document.createElement("root");
		
		Map<String, Map<String, String>> metadata = PluginAgent.getFileWritingFilterBroker().getMetadata();
		
		for (Map.Entry<String, FileWritingFilterFactory> entry : PluginAgent.getFileWritingFilterBroker().getPlugins().entrySet()) {
			final FileWritingFilterMetadata md;
			try {
				md = entry.getValue().getMetadata();
			} catch (Throwable e) {
				continue;
			}
			
			final Element el = document.createElement("filter");
			el.setAttribute("id", entry.getKey());
			el.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(document, "name", md.getName()));
			el.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(document, "description", md.getDescription()));
			
			final Element bundleMetadata = document.createElement("metadata");
			final Map<String, String> map = metadata.get(entry.getKey());
			if (map != null) {
				for (Map.Entry<String, String> bundleMD : map.entrySet()) {
					final Element field = BaseDocumentUtils.impl.createCDATAElementWithText(document, "field", bundleMD.getValue());
					field.setAttribute("name", bundleMD.getKey());
					bundleMetadata.appendChild(field);
				}
				el.appendChild(bundleMetadata);
			}
			
			root.appendChild(el);
		}
		
		document.appendChild(root);
		
		return new GoGoEgoDomRepresentation(document);
	}

}
