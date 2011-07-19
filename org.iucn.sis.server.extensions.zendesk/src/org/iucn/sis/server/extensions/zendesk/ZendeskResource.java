package org.iucn.sis.server.extensions.zendesk;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.gogoego.api.mail.InstanceMailer;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.mail.Mailer;
import com.solertium.util.NodeCollection;
import com.solertium.util.restlet.RestletUtils;

public class ZendeskResource extends BaseServiceRestlet {
	
	private final Client client;
	
	public ZendeskResource(Context context) {
		super(context);
		client = new Client(Protocol.HTTP);
	}
	
	@Override
	public void definePaths() {
		paths.add("/assembla/mail");
		paths.add("/zendesk/{action}");
		paths.add("/zendesk/{action}/{id}");
		
	}
	
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		final String action = (String)request.getAttributes().get("action");
		final String id = (String)request.getAttributes().get("id");
		
		final String url;
		if ("users".equals(action))
			url = "/user.xml";
		else if ("rules".equals(action)) {
			if (id == null)
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			url = "/rules/"+id+".xml";
		}
		else
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		final Request req = newRequest(Method.GET, url);
		
		Response res = client.handle(req);
		
		if (res.getStatus().isSuccess())
			return res.getEntity();
		else
			throw new ResourceException(res.getStatus());
	}
	
	private void sendMail(Representation entity, Request request, Response response, Session session) throws ResourceException {
		User user = getUser(request, session);
		String subject = null, body = null, reporter = null;
		
		Document document = getEntityAsDocument(entity);
		for (Node node : new NodeCollection(document.getDocumentElement().getChildNodes())) {
			if ("subject".equals(node.getNodeName()))
				subject = node.getTextContent();
			else if ("body".equals(node.getNodeName()))
				body = node.getTextContent();
			else if ("reporter".equals(node.getNodeName()))
				reporter = node.getTextContent();
		}
		
		if (subject == null || body == null || reporter == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a subject and a body.");
		
		final StringBuilder out = new StringBuilder();
		out.append("Reporter Name: " + reporter + "\n");
		out.append("Reporter Username: " + user.getUsername() + "\n");
		out.append("Affiliation: " + user.getAffiliation() + "\n");
		out.append("Component: Support\n");
		out.append("Description:\n" + body + "\n.");
		
		Mailer mailer = InstanceMailer.getInstance().getMailer();
		mailer.setTo("sis@support.assembla.com");
		mailer.setSubject(subject);
		mailer.setBody(out.toString());
		
		try {
			mailer.background_send();
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		if (request.getResourceRef().getPath().endsWith("mail")) {
			sendMail(entity, request, response, session);
			return;
		}
			
		final String action = (String)request.getAttributes().get("action");
		if ("login".equals(action)) {
			validateLogin(entity, request, response);
		}
		else if ("remove".equals(action)) {
			removeUser(entity, request, response);
		}
		else if (action.equals("logout")) {
			logout(entity, request, response);
		}
		else {
			final String url;
		
			if ("users".equals(action)) 
				url = "/user.xml";
			else if ("tickets".equals(action)) 
				url = "/tickets.xml";
			else
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
			Request req = newRequest(Method.POST, url);
			req.setEntity(entity);
			RestletUtils.setHeader(req, "Content-Length", String.valueOf(entity.getSize()));
			
			Response res = client.handle(req);
			
			response.setStatus(res.getStatus());
			response.setEntity(res.getEntity());
		}
	}

	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
		String id = (String)request.getAttributes().get("id");
		if (id == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		deleteUser(id, response);
	}
	
	private void logout(Representation entity, Request request, Response response) throws ResourceException {
		Document doc = getEntityAsDocument(entity);
		
		Element el = (Element)doc.getDocumentElement().getElementsByTagName("user").item(0);
	 
		String email;
		try {
			email = URLEncoder.encode(el.getAttribute("email"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		if (email.equals("admin"))
			email = SIS.get().getSettings(getContext()).getProperty("org.iucn.sis.server.extension.zendesk.user", "sisproject@solertium.com");
	
		Request req = newRequest(Method.GET, "/access/logout");
			
		Response res = client.handle(req);
		
		response.setStatus(res.getStatus());
		response.setEntity(res.getEntity());
	}
	
	private void removeUser(Representation entity, Request request, Response response) throws ResourceException { 
		Document doc = getEntityAsDocument(entity);
			  
		Element el = (Element)doc.getDocumentElement().getElementsByTagName("user").item(0);
	 
		String email = el.getAttribute("email");;
		if (email.equals("admin"))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
				
		Request req = newRequest(Method.GET, "/users.xml?role=0");
		
		Response res = client.handle(req);
		
		if (!res.getStatus().isSuccess()) {
			response.setStatus(res.getStatus());
			response.setEntity(res.getEntity());
		}
		else {
			Document userDoc;
			try {
				userDoc = new DomRepresentation(res.getEntity()).getDocument();
			} catch (Exception e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			NodeList list = userDoc.getDocumentElement().getElementsByTagName("user");
			for(int i=0;i<list.getLength();i++){
				Element uel = (Element)list.item(i);
				String uemail = uel.getElementsByTagName("email").item(0).getTextContent();
				String id = uel.getElementsByTagName("id").item(0).getTextContent();
				if(uemail. equals(email)){
					deleteUser(id, response);
				}
			}
			  
			response.setStatus(Status.SUCCESS_OK);  
		}
	}
	
	private void deleteUser(String id, Response response) throws ResourceException {
		String url = "/users/"+id+".xml";
			
		Request req = newRequest(Method.DELETE, url);
		
		Response res = client.handle(req);
		
		if (!res.getStatus().isSuccess())
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
	}
	
	private void validateLogin(Representation entity, Request request, Response response) throws ResourceException {
		Document doc = getEntityAsDocument(entity);
		
		Element el = (Element)doc.getDocumentElement().getElementsByTagName("user").item(0);
		String authToken = "rHHYpql22FzsudXglJL0FdsdVdcTbCOZV75yJ0w4cgoYhnuT";
		String name =  el.getAttribute("name");
		 
		String email = el.getAttribute("email");
		if (email.equals("admin")) {
			  name = "Solertium";
			  email = SIS.get().getSettings(getContext()).getProperty("org.iucn.sis.server.extension.zendesk.user", "sisproject@solertium.com");
		}
		  
		String timestamp =String.valueOf((long)new Date().getTime());
		String organization = "SIS Community";
		
		final MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		m.update((name+email+organization+authToken+timestamp).getBytes());
		byte s[] = m.digest();
		
		String result = "";
		for (int i = 0; i < s.length; i++)
			result += Integer.toHexString((0x000000ff & s[i]) | 0xffffff00).substring(6);
		
		try { 
			String eEmail = URLEncoder.encode(email, "UTF-8");
			String eName = URLEncoder.encode(name, "UTF-8");
			String eOrg = URLEncoder.encode(organization, "UTF-8");
			 
			response.setEntity("?name="+eName+"&email="+eEmail
				 +"&timestamp="+timestamp+"&organization="+ eOrg +"&hash="+result, MediaType.TEXT_PLAIN);
		} catch (UnsupportedEncodingException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		response.setStatus(Status.SUCCESS_OK);
	}
	
	private Request newRequest(Method method, String url) {
		Request req = new Request(Method.GET, SIS.get().getSettings(getContext()).getProperty("org.iucn.sis.server.extension.zendesk.url", "http://support.iucnsis.org") + url);
		req.setChallengeResponse(new ChallengeResponse(
			ChallengeScheme.HTTP_BASIC, 
			SIS.get().getSettings(getContext()).getProperty("org.iucn.sis.server.extension.zendesk.user", "sisproject@solertium.com"), 
			SIS.get().getSettings(getContext()).getProperty("org.iucn.sis.server.extension.zendesk.password", "s3cr3t")
		));
		return req;
	}
}
