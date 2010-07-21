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
package com.solertium.gogoego.server.lib.app.exporter.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.exporter.container.ExporterApplication;
import com.solertium.gogoego.server.lib.app.exporter.utils.ExportInstance;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorkerMetadata;
import com.solertium.util.AlphanumericComparator;

/**
 * ExporterListResource.java
 * 
 * Provides a listing of all installed implementation of ExporterWorkers.
 * 
 * @author carl.scott
 * 
 */
public class ExporterListResource extends Resource {

	private final ExportInstance exportInstance;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	public ExporterListResource(Context context, Request request, Response response) {
		super(context, request, response);
		this.exportInstance = ((ExporterApplication)ServerApplication.getFromContext(getContext(), ExporterApplication.REGISTRATION)).getExportInstance();
		setModifiable(false);
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	public Representation represent(Variant variant) throws ResourceException {
		Document document = DocumentUtils.impl.newDocument();

		Element root = document.createElement("root");
		final List<String> workers = new ArrayList<String>(exportInstance.getWorkerIDs());

		Collections.sort(workers, new AlphanumericComparator());
		
		for (String key : workers) {
			ExporterWorkerMetadata md;
			try {
				md = ExporterApplication.broker.getPlugin(key).getMetadata();
			} catch (NullPointerException e) {
				continue;
			}
			Element element = document.createElement("exporter");
			element.setAttribute("id", key);
			element.setAttribute("name", md.getName());
			root.appendChild(element);
		}

		document.appendChild(root);

		return new DomRepresentation(variant.getMediaType(), document);
	}

}
