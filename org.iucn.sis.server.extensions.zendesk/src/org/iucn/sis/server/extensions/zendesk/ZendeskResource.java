package org.iucn.sis.server.extensions.zendesk;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
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
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.util.restlet.RestletUtils;

public class ZendeskResource extends ServiceRestlet{
	
	private final static int NEW     = 0;
	private final static int OPEN    = 1;
	private final static int PENDING = 2;
	private final static int SOLVED  = 3;
	private final static int CLOSED  = 4;
	
	private final String baseUrl = "http://support.iucnsis.org";
	
	public ZendeskResource(Context context) {
		super(context);
		
	}
	
	@Override
	public void definePaths() {
		paths.add("/zendesk/{action}");
		paths.add("/zendesk/{action}/{id}");
		
	}
	
	@Override
	public void performService(Request request, Response response) {
		if(request.getMethod().equals(Method.POST)) handlePost(request, response);
		if(request.getMethod().equals(Method.GET))  handleGet(request, response);
		if(request.getMethod().equals(Method.PUT))  handlePut(request, response);
		if(request.getMethod().equals(Method.DELETE))  handleDelete(request, response);
	}
	
	private void handleGet(Request request, Response response){
		String action = (String)request.getAttributes().get("action");
		String url="";
		if(action.equals("users")) url = "/user.xml";
		if(action.equals("tickets")) url = "/ticket.xml";
		if(action.equals("rules")){
			String id = (String)request.getAttributes().get("id");
			url = "/rules/"+id+".xml";
		}
		Request req = new Request(Method.GET, baseUrl+url);
		req.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "sisproject@solertium.com", "s3cr3t"));
		
		Client client = new Client(Protocol.HTTP);
		
		Response res = client.handle(req);
		StringRepresentation ent = new StringRepresentation(res.getEntityAsText());
		ent.setMediaType(MediaType.TEXT_XML);
		response.setEntity(ent);
	}
	
	private void handlePut(Request request, Response response){
		
	}
	
	private void handlePost(Request request, Response response){
		try{
			String url="";
			String action = (String)request.getAttributes().get("action");
			if(action.equals("authn")) {
				validateLogin(request, response);
				return;
			}
			if(action.equals("remove")) {
				System.out.println("removing user");
				removeUser(request, response);
				return;
			}
			if(action.equals("logout")) {
				logout(request, response);
				return;
			}
			if(action.equals("users")) url = "/user.xml";
			if(action.equals("tickets")) url = "/tickets.xml";
			
			Request req = new Request(Method.POST, baseUrl+url);
			
			String ent = request.getEntityAsText();
			StringRepresentation rep = new StringRepresentation(ent);
			rep.setMediaType(MediaType.TEXT_XML);
			
			req.setEntity(rep);
			
			RestletUtils.setHeader(req, "Content-Length", String.valueOf(ent.length()));
			Client client = new Client(Protocol.HTTP);
			
			System.out.println(ent);
			Response res = client.handle(req);
			System.out.println(res.getStatus());
			System.out.println(res.getEntityAsText());
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private void handleDelete(Request request, Response response){
		
			String url="";
			String action = (String)request.getAttributes().get("action");
			String id = (String)request.getAttributes().get("id");
			deleteUser(id, response);
			
	}


	private String createUser(){
		String xml = "<user>"+
		  "<email>aljohson@yourcompany.dk</email>"+
		  "<name>Al Johnson</name>"+
		  "<roles>4</roles>"+
		  "<restriction-id>1</restriction-id>"+
		  "<groups type='array'>"+
		  "  <group>2</group>"+
		  "  <group>3</group>"+
		  "</groups>"+
		  "</user>";
		return xml;
	}
	
	private String createTicket(){
		String xml = "<ticket>"+
		"  <description>My printer is not always working</description>"+
		"  <priority-id>4</priority-id>  "+
		"  <requester-name>Mike Newson</requester-name>"+   
		"  <requester-email>mike@nowhere.com</requester-email>"+   
   	    "  <description>My printer is not working</description>"+
		"  <priority-id>4</priority-id>"+
	 	"</ticket>";
		return xml;
	}
	
	private Document getTicketsByRule(){
		//GET /rules/#{view-id}.xml
		return null;
		
	}
	
	private void logout(Request request, Response response){
		String xml = request.getEntityAsText();
		try{
			  Document doc = DocumentUtils.createDocumentFromString(xml);
			  Element el = (Element)doc.getDocumentElement().getElementsByTagName("user").item(0);
	 
			  String email = URLEncoder.encode(el.getAttribute("email"), "UTF-8");
			  if(email.equals("admin")){
				  email="sisproject@solertium.com";
			  }
			  			  
			  Request req = new Request(Method.GET, baseUrl+"/access/logout");
			  Client client = new Client(Protocol.HTTP);
			  Response res = client.handle(req);
			  System.out.println(res.getStatus());
			  System.out.println(res.getEntityAsText());
			  response.setStatus(Status.SUCCESS_OK);
			
			  
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	private void removeUser(Request request, Response response){
		String xml = request.getEntityAsText();
		try{
			  Document doc = DocumentUtils.createDocumentFromString(xml);
			  Element el = (Element)doc.getDocumentElement().getElementsByTagName("user").item(0);
	 
			  String email = el.getAttribute("email");;
			  System.out.println(email);
			  if(email.equals("admin")){
				  response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			  }
			  			  
			  Request req = new Request(Method.GET, baseUrl+"/users.xml?role=0");
			  req.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "sisproject@solertium.com", "s3cr3t"));
			  Client client = new Client(Protocol.HTTP);
			  Response res = client.handle(req);
			  DomRepresentation dr = new DomRepresentation(res.getEntity());
			  Document userDoc = dr.getDocument();
			  
			  NodeList list= userDoc.getDocumentElement().getElementsByTagName("user");
			  for(int i=0;i<list.getLength();i++){
				 Element uel = (Element)list.item(i);
				 String uemail = uel.getElementsByTagName("email").item(0).getTextContent();
				 String id = uel.getElementsByTagName("id").item(0).getTextContent();
				 if(uemail. equals(email)){
					 deleteUser(id, response);
				 }
			  }
			  
			  
			  System.out.println(res.getStatus());
			  System.out.println(res.getEntityAsText());
			  response.setStatus(Status.SUCCESS_OK);
			
			  
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void deleteUser(String id, Response response){
		try{
			String url = "/users/"+id+".xml";
			
			Request req = new Request(Method.DELETE, baseUrl+url);
			req.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "sisproject@solertium.com", "s3cr3t"));
			Client client = new Client(Protocol.HTTP);
			
			
			Response res = client.handle(req);
			if(res.getStatus()!=Status.SUCCESS_OK) response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void validateLogin(Request request, Response response){
		String xml = request.getEntityAsText();
		try{
		  Document doc = DocumentUtils.createDocumentFromString(xml);
		  Element el = (Element)doc.getDocumentElement().getElementsByTagName("user").item(0);
		  String authToken = "rHHYpql22FzsudXglJL0FdsdVdcTbCOZV75yJ0w4cgoYhnuT";
		  String name =  el.getAttribute("name");
		 
		  String email = el.getAttribute("email");
		  if(email.equals("admin")){
			  name = "Solertium";
			  email="sisproject@solertium.com";
		  }
		  
		  String timestamp =String.valueOf((long)new Date().getTime());
		  String organization = "SIS Community";//URLEncoder.encode(el.getAttribute("organization"), "UTF-8");;
		//organization ="Solertium";
		  MessageDigest m = MessageDigest.getInstance("MD5");
		  	        m.update((name+email+organization+authToken+timestamp).getBytes());
		  	        byte s[] = m.digest();
		  	        String result = "";
		  	        for (int i = 0; i < s.length; i++) {
		  	          result += Integer.toHexString((0x000000ff & s[i]) | 0xffffff00).substring(6);
		  	        }
		  	       
		 String eEmail = URLEncoder.encode(email, "UTF-8");
		 String eName = URLEncoder.encode(name, "UTF-8");
		 String eOrg = URLEncoder.encode(organization, "UTF-8");
		 
		 response.setEntity("?name="+eName+"&email="+eEmail
			 +"&timestamp="+timestamp+"&organization="+ eOrg +"&hash="+result, MediaType.TEXT_PLAIN);

		 response.setStatus(Status.SUCCESS_OK);
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}

		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		    
			
	}

}
