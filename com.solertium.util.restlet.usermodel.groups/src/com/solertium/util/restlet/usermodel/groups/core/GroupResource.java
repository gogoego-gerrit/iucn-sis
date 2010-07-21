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
package com.solertium.util.restlet.usermodel.groups.core;

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
import com.solertium.util.restlet.usermodel.groups.managers.GroupManager;

/**
 * GroupResource.java
 * 
 * Resource to handle CRUD operations for user groups and forward 
 * them to the appropriate manager.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GroupResource extends Resource {
	
	public static final String ATTR_GROUP_ID = "groupID";
	public static final String ATTR_USER_ID = "userID";
	
	private final String groupID;
	private final String userID;
	private final GroupManager manager;

	public GroupResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		setModifiable(true);
		
		HasInstanceId app = (HasInstanceId)context.getAttributes().get(GroupManager.GROUP_MANAGER_INIT_KEY);
		if (app == null) app = (HasInstanceId) Application.getCurrent();
		manager = GroupManagerFactory.impl.getGroupManager(app.getInstanceId());
		
		groupID = (String)request.getAttributes().get(ATTR_GROUP_ID);
		userID = (String)request.getAttributes().get(ATTR_USER_ID);
		
		getVariants().add(new Variant(MediaType.TEXT_XML));	
	}
	
	/**
	 * - /groups/{groupID}: remove a group
	 * - /groups/{groupID}/{userID}: remove a user
	 */
	public void removeRepresentations() throws ResourceException {
		if (groupID == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		else if (userID == null)
			manager.removeGroup(groupID);
		else
			manager.removeUser(groupID, userID);
	}
	
	public void handlePut() {
		try {
			storeRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}
	
	/**
	 * - /groups: Add a new group (or groups)
	 *  <root>
	 * 		<row>
	 * 			<field name="NAME">name</field>
	 * 		</row>
	 *  </root>
	 *  
	 * - /groups/{groupID}: Add a new user (or users)
	 * 	<root>
	 * 		<row>
	 * 			<field name="USER_ID">user id</field>
	 * 		</row>
	 * 	</root>
	 * 
	 * The user ID should come from the profile table.
	 */
	public void storeRepresentation(Representation entity) throws ResourceException {
		if (groupID == null)
			addGroups(entity);
		else if (userID == null)
			addUsers(entity);
	}
	
	private Document getDocumentFromEntity(final Representation entity) throws ResourceException {
		try {
			return new DomRepresentation(entity).getDocument(); 
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
	}
	
	private void addUsers(final Representation entity) throws ResourceException{
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
			
			String userID = null;
			final NodeCollection children = new NodeCollection(node.getChildNodes());
			for (Node curChild : children) {
				String attr = BaseDocumentUtils.impl.getAttribute(curChild, "name");
				if ("USER_ID".equals(attr))
					userID = curChild.getTextContent();
			}
			if (userID == null)
				continue;
			
			String entryID;
			try {
				entryID = manager.addUser(groupID, userID);
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
	
	private void addGroups(final Representation entity) throws ResourceException {
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
				entryID = manager.addGroup(name);
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
	 * - /groups/{groupID} - update a group
	 *	<root>
	 *		<field name="NAME">group name</field>
	 *	</root> 
	 *
	 * 
	 * There is nothing to update at the user level
	 */
	public void acceptRepresentation(Representation entity) throws ResourceException {
		final Document document = getDocumentFromEntity(entity);
		
		final ElementCollection fields = new ElementCollection(
			document.getDocumentElement().getElementsByTagName("field")	
		);
		if (fields.isEmpty())
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		if (!"NAME".equals(fields.get(0).getAttribute("name")))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		manager.updateGroup(userID, fields.get(0).getTextContent());
	}
	
	/**
	 * - /groups: list all groups
	 * - /groups/{groupID}: list all users in a group
	 */
	public Representation represent(Variant variant) throws ResourceException {
		if (groupID == null) {
			final Document document = BaseDocumentUtils.impl.newDocument();
			final Element root = document.createElement("root");
			
			final Map<String, String> mapping = manager.getGroupMapping();
			for (Map.Entry<String, String> entry : mapping.entrySet()) {
				final Element element = document.createElement("group");
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
					manager.getGroup(groupID).toXML()
				)
			);
		}
	}
	
	public void handlePropfind(){
		Map<String, String> profiles;
		try {
			profiles = manager.getGroupMapping();
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
			return;
		}
		String xml = "<root>";
		for(String pid: profiles.keySet())
			xml+="<group id=\""+pid+"\">"+profiles.get(pid)+"</group>";
		xml+="</root>";
		Document doc= BaseDocumentUtils.impl.createDocumentFromString(xml);
		getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, doc));
	}
	
	public boolean allowPropfind(){
		return true;
	}
	
}
