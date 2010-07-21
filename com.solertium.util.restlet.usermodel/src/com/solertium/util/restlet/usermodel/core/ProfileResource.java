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
package com.solertium.util.restlet.usermodel.core;

import java.util.Map;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.ElementCollection;
import com.solertium.util.NodeCollection;
import com.solertium.util.restlet.HasInstanceId;
import com.solertium.util.restlet.usermodel.managers.ProfileManager;

/**
 * ProfileResource.java
 * 
 * Exposes a restful means of performing CRUD operations on 
 * the user collection.
 * 
 * @author david.fritz
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class ProfileResource extends Resource {
	
	public static final String ATTR_USER_ID = "userID";
	public static final String ATTR_FIELD_ID = "fieldID";

	private final String userID;
	private final String fieldID;
			
	public ProfileResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		setModifiable(true);
		
		userID = (String)request.getAttributes().get(ATTR_USER_ID);
		fieldID = (String)request.getAttributes().get(ATTR_FIELD_ID);
		
		getVariants().add(new Variant(MediaType.TEXT_XML));	
	}
	
	public ProfileManager getManager() {
		return ProfileManagerFactory.impl.getProfileManager(
			((HasInstanceId)Application.getCurrent()).getInstanceId()
		);
	}
	
	/**
	 * - /users/{userID}: delete a user
	 * - /users/{userID}/{fieldID}: delete a field
	 */
	public void removeRepresentations() throws ResourceException {
		if (userID == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		else if (fieldID == null)
			getManager().removeProfile(userID);
		else
			getManager().removeField(userID, fieldID);
	}
	
	public void handlePut() {
		try {
			storeRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}
	
	/**
	 *  - /users: Add a new user (or users)
	 *  <root>
	 *  	<row>
	 *  		<field name="NAME">name</field>
	 *  	</row>
	 *  </root>
	 *  
	 *  - /users/{userID}: Add a new profile field
	 *  <root>
	 *  	<row>
	 *  		<field name="KEY">field name</field>
	 *  		<field name="VALUE">field value</field>
	 *  	</row> 
	 *  </root>
	 */
	public void storeRepresentation(Representation entity) throws ResourceException {
		if (userID == null) {
			addUsers(entity);
		} else if (fieldID == null) {
			addFields(entity);
		}
	}
	
	private Document getDocumentFromEntity(final Representation entity) throws ResourceException {
		try {
			return new DomRepresentation(entity).getDocument(); 
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
	}
	
	private void addFields(final Representation entity) throws ResourceException {
		final Document document = getDocumentFromEntity(entity);
		
		final Document results = BaseDocumentUtils.impl.newDocument();
		results.appendChild(results.createElement("root"));
		
		int counter = 1;
		final NodeCollection nodes = new NodeCollection(
			document.getDocumentElement().getElementsByTagName("row")	
		);
		for (Node node : nodes) {
			String id = BaseDocumentUtils.impl.getAttribute(node, "id");
			if ("".equals(id))
				id = "" + counter++;
			
			String key = null, value = null;
			final NodeCollection children = new NodeCollection(node.getChildNodes());
			for (Node curChild : children) {
				String attr = BaseDocumentUtils.impl.getAttribute(curChild, "name");
				if ("KEY".equals(attr))
					key = curChild.getTextContent();
				else if ("VALUE".equals(attr))
					value = curChild.getTextContent();
			}
			if (key == null || value == null)
				continue;
			
			String entryID;
			try {
				entryID = getManager().addField(userID, key, value);
			} catch (ResourceException e) {
				continue;
			}
			
			final Element result = results.createElement("row");
			result.setAttribute("id", id);
			result.setTextContent(entryID);
			
			results.getDocumentElement().appendChild(result);
		}
		
		getResponse().setEntity(new DomRepresentation(
			MediaType.TEXT_XML, results	
		));	
	}
	
	private void addUsers(Representation entity) throws ResourceException {
		final Document document = getDocumentFromEntity(entity);
		
		final Document results = BaseDocumentUtils.impl.newDocument();
		results.appendChild(results.createElement("root"));
		
		int counter = 1;
		final NodeCollection nodes = new NodeCollection(
			document.getDocumentElement().getElementsByTagName("row")	
		);
		for (Node node : nodes) {
			String id = BaseDocumentUtils.impl.getAttribute(node, "id");
			if ("".equals(id))
				id = "" + counter++;
			
			String name = null;
			final NodeCollection children = new NodeCollection(node.getChildNodes());
			for (Node curChild : children) {
				if ("NAME".equals(BaseDocumentUtils.impl.getAttribute(curChild, "name")))
					name = curChild.getTextContent();
			}
			if (name == null)
				continue;
			
			String entryID;
			try {
				entryID = getManager().addUser(name);
			} catch (ResourceException e) {
				continue;
			}
			
			final Element result = results.createElement("row");
			result.setAttribute("id", id);
			result.setTextContent(entryID);
			
			results.getDocumentElement().appendChild(result);
		}
		
		getResponse().setEntity(new DomRepresentation(
			MediaType.TEXT_XML, results	
		));		
	}
	
	/**
	 * - /users/{userID}: update username
	 *  <root>
	 * 		<field name="NAME"> new name</field>
	 *  </root>
	 * 
	 * - /users/{userID}/{fieldID}: update field
	 *  <root>
	 * 		<field name="VALUE">new value</field>
	 *  </root>
	 */
	public void acceptRepresentation(Representation entity) throws ResourceException {
		if (userID == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		else if (fieldID == null) 
			updateUser(entity);
		else
			updateField(entity);
	}
	
	private void updateField(final Representation entity) throws ResourceException {
		final Document document = getDocumentFromEntity(entity);
		
		final ElementCollection fields = new ElementCollection(
			document.getDocumentElement().getElementsByTagName("field")	
		);
		if (fields.isEmpty())
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		if (!"VALUE".equals(fields.get(0).getAttribute("name")))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		getManager().updateField(userID, fieldID, fields.get(0).getTextContent());
	}
	
	private void updateUser(final Representation entity) throws ResourceException {
		final Document document = getDocumentFromEntity(entity);
		
		final ElementCollection fields = new ElementCollection(
			document.getDocumentElement().getElementsByTagName("field")	
		);
		if (fields.isEmpty())
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		if (!"NAME".equals(fields.get(0).getAttribute("name")))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		getManager().updateUser(userID, fields.get(0).getTextContent());
	}
	
	/**
	 * - /users: uri-list of users
	 * - /users/{userID}: profile info for user
	 */
	public Representation represent(Variant variant) throws ResourceException {
		if (userID == null) {
			final Document document = BaseDocumentUtils.impl.newDocument();
			final Element root = document.createElement("root");
			
			final Map<String, String> mapping = getManager().getUserMapping();
			for (Map.Entry<String, String> entry : mapping.entrySet()) {
				final Element element = document.createElement("user");
				element.setAttribute("ID", entry.getKey());
				element.setAttribute("NAME", entry.getValue());
				root.appendChild(element);
			}
			
			document.appendChild(root);
			
			return new DomRepresentation(variant.getMediaType(), document);
		}
		else {
			return new DomRepresentation(variant.getMediaType(), 
				BaseDocumentUtils.impl.createDocumentFromString(
					getManager().getProfile(userID).toXML()
				)
			);
		}
	}
	
	public void handlePropfind() {
		final Map<String, String> profiles;
		try {
			profiles = getManager().getUserMapping();
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
			return;
		}
	
		String xml = "<root>";
		for (String pid: profiles.keySet())
			xml+="<profile id=\""+pid+"\">"+profiles.get(pid)+"</profile>";
		xml+="</root>";
		Document doc= BaseDocumentUtils.impl.createDocumentFromString(xml);
		getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, doc));
	}
	
	public boolean allowPropfind() {
		return true;
	}

}
