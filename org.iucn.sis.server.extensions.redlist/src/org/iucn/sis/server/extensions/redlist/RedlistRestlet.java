package org.iucn.sis.server.extensions.redlist;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
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
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class RedlistRestlet extends BaseServiceRestlet {

	public RedlistRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/redlist/{action}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response) throws ResourceException {
		Document document = BaseDocumentUtils.getInstance().createDocumentFromString("<images/>");

		try {
			buildXML(document, request.getChallengeResponse(), getDestinationURL(getContext()), new VFSPath("/images"), false);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
		
		return new DomRepresentation(MediaType.TEXT_XML, document);
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response) throws ResourceException { 
		Document document = BaseDocumentUtils.getInstance().createDocumentFromString("<images/>");
		
		try {
			buildXML(document, request.getChallengeResponse(), getDestinationURL(getContext()), new VFSPath("/images"), true);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		response.setStatus(Status.SUCCESS_CREATED);
	}

	/*private void putFiles(Request request, VFSPath directory, String footprint){
		try{
			VFSPathToken[] listing = vfs.list(directory);
			for(int i=0;i<listing.length; i++){
				VFSPath cur = directory.child(listing[i]);
				if(vfs.isCollection(cur)){
					Client client = new Client(Protocol.HTTPS);
					Request req = new Request(Method.MKCOL, footprint+"/"+cur.getName());
					ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, request.getChallengeResponse().getCredentials());
					req.setChallengeResponse(cr);
					client.handle(req);

					putFiles(request, cur, footprint+"/"+listing[i]);

				}
				else{
					try{
						if(cur.getName().endsWith(".xml")){
							Document image = vfs.getDocument(cur);
							Client client = new Client(Protocol.HTTPS);
							System.out.println("putting " + footprint+"/"+cur.getName());
							Request req = new Request(Method.PUT, footprint+"/"+cur.getName());
							ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, request.getChallengeResponse().getCredentials());
							req.setChallengeResponse(cr);
							req.setEntity(new DomRepresentation(MediaType.TEXT_XML, image));
							Response res = client.handle(req);
							System.out.println(res.getStatus());
						}
					}
					catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		}
		catch (NotFoundException e) {
			e.printStackTrace();
		}
	}*/

	/*
	 * FIXME: stop try/catching everything. 
	 * 
	 */
	private void buildXML(Document document, ChallengeResponse challenge, String destinationURL, VFSPath path, boolean put) throws IOException, ResourceException {
		VFSPathToken[] listing = vfs.list(path);
		for(int i=0;i<listing.length; i++){
			VFSPath cur = path.child(listing[i]);
			if (vfs.isCollection(cur))
				buildXML(document, challenge, destinationURL, cur, put);
			else {
				if (cur.getName().endsWith(".xml")) {
					final Document image = vfs.getDocument(cur);
					
					NodeList list = image.getDocumentElement().getElementsByTagName("image");
					String sp_id = image.getDocumentElement().getAttribute("id");
					
					Document doc = BaseDocumentUtils.getInstance().createDocumentFromString("<images/>");
						
					List<Element> elList = new ArrayList<Element>();
					for(int t=0;t<list.getLength();t++){
						Map<Long, String> alreadySeen = new HashMap<Long, String>(); 
						Node curNode = list.item(t);
						if(curNode.getAttributes().getNamedItem("showRedlist")!=null && curNode.getAttributes().getNamedItem("showRedlist").getTextContent().equals("true")){
							Element el = DocumentUtils.createElementWithText(doc, "image", "");
							
							String isPrimary = curNode.getAttributes().getNamedItem("primary").getTextContent();
							String imageID = curNode.getAttributes().getNamedItem("id").getTextContent();
							String credit = curNode.getAttributes().getNamedItem("credit") == null ? "" : curNode.getAttributes().getNamedItem("credit").getTextContent();
							Long size = Long.valueOf(vfs.getLength(new VFSPath("/images/bin/" + imageID + ".jpg")));
									
							//If this image
							if( alreadySeen.containsKey(size) && alreadySeen.get(size).equals(credit) ) {
								System.out.println("Skipping dupe image " + imageID + " for taxon " + sp_id);
							} else {
								alreadySeen.put(size, credit);
								el.setAttribute("image_id", imageID);
								el.setAttribute("sp_id", sp_id);
								el.setAttribute("primary", isPrimary);

								if(curNode.getAttributes().getNamedItem("credit")!=null)
									el.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(doc, "credit", credit));
										//										el.setAttribute("credit", );
								if(curNode.getAttributes().getNamedItem("source")!=null) 
									el.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(doc,
											"source", curNode.getAttributes().getNamedItem("source").getTextContent()));
								if(curNode.getAttributes().getNamedItem("caption")!=null) 
									el.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(doc,
											"caption", curNode.getAttributes().getNamedItem("caption").getTextContent()));

								if( Boolean.valueOf(isPrimary) ) {
									if( elList.size() > 0 )
										elList.add(0, el);
									else
										elList.add(el);
								} else {
									elList.add(el);
								}
							}
						}
					}
							
					if( elList.size() > 0 ) {
						for( Element curEl : elList )
							doc.getDocumentElement().appendChild(curEl);
							
						if( put ) {
							Client client = new Client(Protocol.HTTPS);
							Request req = new Request(Method.PUT, destinationURL + "/" + cur.toString().replace("/images/", ""));
							ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, challenge.getCredentials());
							req.setChallengeResponse(cr);
							req.setEntity(new DomRepresentation(MediaType.TEXT_XML, doc));
							
							Response resp = client.handle(req);
							if (!resp.getStatus().isSuccess())
								throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Request failed due to error: " + resp.getStatus().getCode());
						} else {
							document.getDocumentElement().appendChild(document.adoptNode(doc.getDocumentElement()));
						}
					}
				}
			}
		}
	}
	
	/**
	 * this will ensure that on-the-fly settings changes are picked 
	 * up with each request.
	 * @param context
	 * @return
	 */
	private String getDestinationURL(Context context) {
		return SIS.get().getSettings(context).
			getProperty("org.iucn.sis.server.extensions.redlist.imagePublishURL", 
				"https://rl2009.gogoego.com/admin/files/images/published");
	}

}
