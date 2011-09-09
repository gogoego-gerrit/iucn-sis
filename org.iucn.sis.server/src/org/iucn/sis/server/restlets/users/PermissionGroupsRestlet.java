package org.iucn.sis.server.restlets.users;

import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.PermissionGroupIO;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Element;

import com.solertium.db.DBException;
import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class PermissionGroupsRestlet extends BaseServiceRestlet {

	private final int DELETE_ELEMENT = 0;
	private final int ADD_ELEMENT = 1;
	
	public PermissionGroupsRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/acl/user/{username}");
		paths.add("/acl/groups");
		paths.add("/acl/group/{groupName}");
	}

	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
		String groupName = (String)request.getAttributes().get("groupName");
		if (groupName == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		else if (groupName.contains(",")) {
			PermissionGroupIO permissionIO = new PermissionGroupIO(session);
			for (String curGroup : groupName.split(",") ) {
				PermissionGroup group;
				try {
					group = permissionIO.getPermissionGroup(curGroup);
					
					if (group == null)
						throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
					
					permissionIO.deletePermissionGroup(group);
				} catch (PersistentException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
		} else {
			PermissionGroup group;
			PermissionGroupIO permissionIO = new PermissionGroupIO(session);
			try{
				group = permissionIO.getPermissionGroup(groupName);
				permissionIO.deletePermissionGroup(group);
				response.setStatus(Status.SUCCESS_OK);
			}catch(PersistentException e){
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
	}
	
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		UserIO userIO = new UserIO(session);
		PermissionGroupIO permissionIO = new PermissionGroupIO(session);
		
		String username = (String) request.getAttributes().get("username");
		if (username != null) {
			User user = userIO.getUserFromUsername(username);
			if (user == null)
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
			
			StringBuilder xml = new StringBuilder("<permissions>");
			for (PermissionGroup group : user.getPermissionGroups())
				xml.append(group.toBasicXML());
			xml.append("</permissions>");
			
			return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
		} else {
			try {
				final StringBuilder out = new StringBuilder();
				out.append("<permissions>");
				
				for (PermissionGroup group : permissionIO.getPermissionGroups())
					out.append(group.toXML());
				
				out.append("</permissions>");
				return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
	}
	
	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final NativeDocument document = new JavaNativeDocument();
		try {
			document.parse(entity.getText());
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		PermissionGroupIO groupIO = new PermissionGroupIO(session);
		final StringBuilder builder = new StringBuilder();
		builder.append("<groups>");
		final NativeNodeList nodes = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeNode node = nodes.item(i);
			if (PermissionGroup.ROOT_TAG.equals(node.getNodeName())) {
				PermissionGroup group = PermissionGroup.fromXML(node);
				try {
					groupIO.savePermissionGroup(group);					
				} catch (PersistentException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				} catch (ResourceException e){
					throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
				}
				builder.append(group.toXML());
			}
		}
		builder.append("</groups>");
		response.setEntity(new StringRepresentation(builder.toString(), MediaType.TEXT_XML));
	}	
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final NativeDocument document = new JavaNativeDocument();
		try {
			document.parse(entity.getText());
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		PermissionGroupIO groupIO = new PermissionGroupIO(session);
		final StringBuilder builder = new StringBuilder();
		builder.append("<groups>");
		final NativeNodeList nodes = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeNode node = nodes.item(i);
			if (PermissionGroup.ROOT_TAG.equals(node.getNodeName())) {
				PermissionGroup group = PermissionGroup.fromXML(node);
				try {
					groupIO.updatePermissionGroup(group);					
				} catch (PersistentException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				} 
				builder.append(group.toXML());
			}
		}
		builder.append("</groups>");
		response.setEntity(new StringRepresentation(builder.toString(), MediaType.TEXT_XML));
	}


	@SuppressWarnings("unused")
	private synchronized void operateOnDocument(List<Element> els, int operationCode) {
		if( operationCode == DELETE_ELEMENT ) {
			for( Element el : els )
				el.getParentNode().removeChild(el);
		} else if( operationCode == ADD_ELEMENT ) {
			for( Element el : els ) {
//				NodeList list = permissionsDoc.getElementsByTagName(el.getNodeName());
//				if(list.getLength() == 1)
//					list.item(0).getParentNode().removeChild(list.item(0));
//					
//				permissionsDoc.getDocumentElement().appendChild(el);
			}
		}
		
	}
	
}
