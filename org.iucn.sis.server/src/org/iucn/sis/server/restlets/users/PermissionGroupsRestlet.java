package org.iucn.sis.server.restlets.users;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.query.ExperimentalSelectQuery;
import com.solertium.db.query.QRelationConstraint;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

public class PermissionGroupsRestlet extends ServiceRestlet {

	private long lastModified = 0;

	private final int DELETE_ELEMENT = 0;
	private final int ADD_ELEMENT = 1;
	
	public PermissionGroupsRestlet(VFS vfs, Context context) {
		super(vfs, context);
	}

	public PermissionGroupsRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/acl/user/{username}");
		paths.add("/acl/groups");
		paths.add("/acl/group/{groupName}");
	}

	@Override
	public void performService(Request request, Response response) {
		try {
			if( request.getMethod().equals(Method.DELETE) )
				handleDelete(request, response);
			else if( request.getMethod().equals(Method.GET) )
				handleGet(request, response);
			else if( request.getMethod().equals(Method.POST) )
				handlePost(request, response);
			else
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (Throwable e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private void handleDelete(Request request, Response response) throws IOException {
		String groupName = (String)request.getAttributes().get("groupName");
		if( groupName == null ) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else if( groupName.contains(",") ) {
			for( String curGroup : groupName.split(",") ) {
//				/*NodeList list = permissionsDoc.getElementsByTagName(curGroup);
//				if( list == null || list.getLength() == 0 )
//					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//				else {
//					Element el = (Element)list.item(0);
//					ArrayList<Element> els = new ArrayList<Element>();
//					els.add(el);
//					operateOnDocument(els, DELETE_ELEMENT);
//				}*/
				
				response.setEntity("Remove successful.", MediaType.TEXT_PLAIN);
				response.setStatus(Status.SUCCESS_OK);
			}
		} else {
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
		}
	}

	private void handleGet(Request request, Response response) {
		
		String username = (String) request.getAttributes().get("username");
		if (username != null) {
			User user = SIS.get().getUserIO().getUserFromUsername(username);
			if (user != null) {
				StringBuilder xml = new StringBuilder("<permissions>");
				for (PermissionGroup group : user.getPermissionGroups()) {
					xml.append(group.toBasicXML());
				}
				xml.append("</permissions>");
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(xml.toString(), MediaType.TEXT_XML);
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
			
		} else {
			try {
				response.setEntity(SIS.get().getPermissionIO().getPermissionGroupsXML(), MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			} catch (DBException e) {
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		}
		
		
	}

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
