package org.iucn.sis.server.acl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.utils.DocumentUtils;
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

import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

public class PermissionGroupsRestlet extends ServiceRestlet {

	private Document permissionsDoc;
	private long lastModified = 0;
	private VFSPath uri = new VFSPath("/acl/groups.xml");

	private final int DELETE_ELEMENT = 0;
	private final int ADD_ELEMENT = 1;
	
	public PermissionGroupsRestlet(VFS vfs, Context context) {
		super(vfs, context);
		readGroupsDocument();
	}

	public PermissionGroupsRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
		readGroupsDocument();
	}

	@Override
	public void definePaths() {
		paths.add("/acl/groups");
		paths.add("/acl/group/{groupName}");
	}

	@Override
	public void performService(Request request, Response response) {
		checkTimestamp();

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
				NodeList list = permissionsDoc.getElementsByTagName(curGroup);
				if( list == null || list.getLength() == 0 )
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				else {
					Element el = (Element)list.item(0);
					ArrayList<Element> els = new ArrayList<Element>();
					els.add(el);
					operateOnDocument(els, DELETE_ELEMENT);
				}
				
				response.setEntity("Remove successful.", MediaType.TEXT_PLAIN);
				response.setStatus(Status.SUCCESS_OK);
			}
		} else {
			NodeList list = permissionsDoc.getElementsByTagName(groupName);
			if( list == null || list.getLength() == 0 )
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			else {
				Element el = (Element)list.item(0);
				ArrayList<Element> els = new ArrayList<Element>();
				els.add(el);
				operateOnDocument(els, DELETE_ELEMENT);
				
				response.setEntity(DocumentUtils.serializeNodeToString(el), MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			}
		}
	}

	private void handleGet(Request request, Response response) {
		response.setEntity(new DomRepresentation(MediaType.TEXT_XML, permissionsDoc));
		response.setStatus(Status.SUCCESS_OK);
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
		
		Document doc = new DomRepresentation(request.getEntity()).getDocument();
		
		ArrayList<Element> els = new ArrayList<Element>();
		NodeList list = doc.getDocumentElement().getChildNodes();
		
		for( int i = 0; i < list.getLength(); i++ ) {
			if( list.item(i).getNodeType() == Node.ELEMENT_NODE ) {
				els.add( (Element) permissionsDoc.adoptNode(list.item(i).cloneNode(true) ) );
			}
		}
		
		if( els.size() > 0 )
			operateOnDocument(els, ADD_ELEMENT);
		
		response.setStatus(Status.SUCCESS_OK);
	}

	private synchronized void operateOnDocument(List<Element> els, int operationCode) {
		if( operationCode == DELETE_ELEMENT ) {
			for( Element el : els )
				el.getParentNode().removeChild(el);
		} else if( operationCode == ADD_ELEMENT ) {
			for( Element el : els ) {
				NodeList list = permissionsDoc.getElementsByTagName(el.getNodeName());
				if(list.getLength() == 1)
					list.item(0).getParentNode().removeChild(list.item(0));
					
				permissionsDoc.getDocumentElement().appendChild(el);
			}
		}
		
		writeback();
	}
	
	/**
	 * Checks to see if the file on the file system had been modified by hand, in
	 * which case we want to read it in and use it.
	 */
	private void checkTimestamp() {
		try {
			if( lastModified > 0 ) {
				if( lastModified < vfs.getLastModified(uri))
					readGroupsDocument();
			} else if( vfs.exists(uri) )
				readGroupsDocument();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readGroupsDocument() {
		try {
			permissionsDoc = vfs.getMutableDocument(uri);
			lastModified = vfs.getLastModified(uri);
		} catch (IOException e) {
			System.out.println("Application specific permission group definitions not found. Using default groups.");
			try {
				permissionsDoc = DocumentUtils.newDocumentBuilder().parse(new InputSource(new InputStreamReader(
						PermissionGroupsRestlet.class.getResourceAsStream("groups.xml") )));
				lastModified = -1;
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new RuntimeException(e1);
			}
		} 
	}
	
	private boolean writeback() {
		return DocumentUtils.writeVFSFile(uri.toString(), vfs, permissionsDoc);
	}
}
