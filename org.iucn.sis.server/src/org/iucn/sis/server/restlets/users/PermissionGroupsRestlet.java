package org.iucn.sis.server.restlets.users;

import java.io.IOException;
import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.PermissionIO;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
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
			PermissionIO permissionIO = new PermissionIO(session);
			for (String curGroup : groupName.split(",") ) {
				PermissionGroup group;
				try {
					group = permissionIO.getPermissionGroup(curGroup);
					
					if (group == null)
						throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
					
					SISPersistentManager.instance().deleteObject(session, group);
				} catch (PersistentException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
				
				//FIXME: remove the permission group
//				/*NodeList list = permissionsDoc.getElementsByTagName(curGroup);
//				if( list == null || list.getLength() == 0 )
//					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//				else {
//					Element el = (Element)list.item(0);
//					ArrayList<Element> els = new ArrayList<Element>();
//					els.add(el);
//					operateOnDocument(els, DELETE_ELEMENT);
//				}*/
				
				/*response.setEntity("Remove successful.", MediaType.TEXT_PLAIN);
				response.setStatus(Status.SUCCESS_OK);*/
			}
		} else {
			//FIXME: remove the permission group
//			NodeList list = permissionsDoc.getElementsByTagName(groupName);
//			if( list == null || list.getLength() == 0 )
//				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//			else {
//				Element el = (Element)list.item(0);
//				ArrayList<Element> els = new ArrayList<Element>();
//				els.add(el);
//				operateOnDocument(els, DELETE_ELEMENT);
//				
//				response.setEntity(DocumentUtils.serializeNodeToString(el), MediaType.TEXT_XML);
//				response.setStatus(Status.SUCCESS_OK);
//			}
			
			response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		}
	}
	
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		UserIO userIO = new UserIO(session);
		PermissionIO permissionIO = new PermissionIO(session);
		
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
				return new StringRepresentation(permissionIO.getPermissionGroupsXML(), MediaType.TEXT_XML);
			} catch (DBException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final NativeDocument document = new JavaNativeDocument();
		try {
			document.parse(entity.getText());
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final StringBuilder builder = new StringBuilder();
		builder.append("<groups>");
		final NativeNodeList nodes = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeNode node = nodes.item(i);
			if (PermissionGroup.ROOT_TAG.equals(node.getNodeName())) {
				PermissionGroup group = PermissionGroup.fromXML(node);
				try {
					SISPersistentManager.instance().saveObject(session, group);
				} catch (PersistentException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
				builder.append(group.toXML());
			}
		}
		builder.append("</groups>");
	}

	@SuppressWarnings("unused")
	private void handlePost(Request request, Response response) throws IOException {
//		String payload = request.getEntity().getText();
//		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
//		ndoc.parse(payload);
//
//		//Overriding processInherits so it doesn't add empty inherited groups automagically
//		PermissionParser parser = new PermissionParser(ndoc) {
//			@Override
//			protected void processInheritsTag(NativeElement el, PermissionGroup group) {
//				String name = el.getTextContent();
//				group.addInheritence(new PermissionGroup(name));
//			}
//		};
//		ArrayList<Element> els = new ArrayList<Element>();
//		for( Entry<String, PermissionGroup> entry : parser.getGroups().entrySet() ) {
//			Document doc = DocumentUtils.createDocumentFromString(entry.getValue().toXML());
//			els.add((Element)permissionsDoc.adoptNode(doc.getDocumentElement().cloneNode(true)));
//		}
		
//		Document doc = new DomRepresentation(request.getEntity()).getDocument();
//		
//		ArrayList<Element> els = new ArrayList<Element>();
//		NodeList list = doc.getDocumentElement().getChildNodes();
//		
//		for( int i = 0; i < list.getLength(); i++ ) {
//			if( list.item(i).getNodeType() == Node.ELEMENT_NODE ) {
//				els.add( (Element) permissionsDoc.adoptNode(list.item(i).cloneNode(true) ) );
//			}
//		}
//		
//		if( els.size() > 0 )
//			operateOnDocument(els, ADD_ELEMENT);
		
		response.setStatus(Status.SUCCESS_OK);
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
