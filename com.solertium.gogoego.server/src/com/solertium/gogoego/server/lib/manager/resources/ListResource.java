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
package com.solertium.gogoego.server.lib.manager.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

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
import org.w3c.dom.Node;

import com.solertium.gogoego.server.lib.manager.container.ManagerApplication;
import com.solertium.util.NodeCollection;

/**
 * ListResource.java
 * 
 * Lists all sites that are running in the component.
 * 
 * @author carl.scott
 * 
 */
public class ListResource extends Resource {

	public ListResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(false);
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	public Representation represent(Variant variant) throws ResourceException {
		ArrayList<HashMap<String, String>> list = getSites();
		Document document = DocumentUtils.impl.newDocument();
		Element root = document.createElement("root");
		for (HashMap<String, String> site : list) {
			Element element = document.createElement("site");
			element.setAttribute("name", site.get("name"));
			element.setAttribute("match", site.get("match"));
			element.setAttribute("httpsHost", site.get("httpsHost"));
			root.appendChild(element);
		}
		document.appendChild(root);
		
		return new DomRepresentation(variant.getMediaType(), document);
	}

	
	private ArrayList<HashMap<String, String>> getSites(){
		ArrayList<HashMap<String, String>> sites = new ArrayList<HashMap<String, String>>();
		String vmroot = ((ManagerApplication)ManagerApplication.getCurrent()).getVMRoot();
		Document hostDoc = SiteBuilder.getHostDocument(new File(vmroot + "/hosts.xml"));
		
		NodeCollection nodes = new NodeCollection(hostDoc.getElementsByTagName("host"));
		for (Node node : nodes) {
			HashMap<String, String> site = new HashMap<String, String>();
			site.put("name", ((Element)node).getAttribute("name"));
			site.put("match",((Element)node).getAttribute("match"));
			site.put("httpsHost", ((Element)node).getAttribute("httpsHost"));
			sites.add(site);
		
		}
		return sites;
	}
}
