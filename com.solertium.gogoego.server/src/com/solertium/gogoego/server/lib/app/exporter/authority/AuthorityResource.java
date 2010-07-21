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
package com.solertium.gogoego.server.lib.app.exporter.authority;

import java.util.Map;

import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.gogoego.api.utils.MagicDisablingFilter;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.gogoego.server.lib.app.exporter.container.ExporterApplication;
import com.solertium.gogoego.server.lib.app.exporter.container.ExporterWorkerFactory;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorkerMetadata;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.restlet.MediaTypeManager;

/**
 * AuthorityResource.java
 * 
 * Get authority information for a particular worker.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public final class AuthorityResource extends Resource {

	private final String filename;

	public AuthorityResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(false);

		this.filename = (String) request.getAttributes().get("filename");

		getVariants().add(
				new Variant(filename != null ? MediaTypeManager.getMediaType(filename)
						: MediaType.APPLICATION_OCTET_STREAM));
	}

	public Representation represent(Variant variant) throws ResourceException {
		getRequest().getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);

		if (filename == null || filename.equals("") || filename.indexOf("/") != -1)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		if ("Exporters.xml".equals(filename)) {
			final Document document = BaseDocumentUtils.impl.newDocument();
			final Element root = document.createElement("root");
			
			for (Map.Entry<String, ExporterWorkerFactory> entry : ExporterApplication.broker.getPlugins().entrySet()) {
				final ExporterWorkerMetadata md = entry.getValue().getMetadata();
				final Element el = document.createElement("exporter");
				el.setAttribute("id", entry.getKey());
				el.appendChild(BaseDocumentUtils.impl.
						createCDATAElementWithText(document, "name", md.getName()));
				el.appendChild(BaseDocumentUtils.impl.
						createCDATAElementWithText(document, "logo", "/admin/images/g64.png"));
				el.appendChild(BaseDocumentUtils.impl.
						createCDATAElementWithText(document, "description", md.getDescription()));
				if (entry.getValue().getManagement().getSettingsAuthorityUI() != null) {
					el.appendChild(BaseDocumentUtils.impl.
							createCDATAElementWithText(document, "authority", "/admin/apps/" + ExporterApplication.REGISTRATION + "/ui/authority/" + entry.getKey()));
				}
				root.appendChild(el);
			}
			
			document.appendChild(root);
			
			return new GoGoEgoDomRepresentation(document);
		}
		else {
			ExporterWorkerFactory factory = ExporterApplication.broker.getPlugin(filename);
			if (factory == null)
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
			
			Document doc = factory.getManagement().getSettingsAuthorityUI();
			if (doc == null)
				throw new ResourceException(Status.CLIENT_ERROR_GONE);
			
			return new GoGoEgoDomRepresentation(doc);
		}
	}

}
