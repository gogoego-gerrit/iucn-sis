package org.iucn.sis.server.simple;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.utils.DocumentUtils;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.restlet.StandardServerComponent;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

public class RedlistRestlet extends ServiceRestlet {

	private String destinationURL = "https://rl2009.gogoego.com/admin/files/images/published";
	private Document aggregate;
	
	public RedlistRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
		
		String pubDestination = StandardServerComponent.getInitProperties().getProperty("IMAGE_PUBLISH_URL");
		if( pubDestination != null && !pubDestination.equals(""))
			destinationURL = pubDestination;
		
		System.out.println("--- Red List image publish destination is: " + destinationURL);
	}

	@Override
	public void definePaths() {
		paths.add("/redlist/{action}");
	}

	private void handleGet(Request request, Response response) {
		aggregate = BaseDocumentUtils.getInstance().createDocumentFromString("<images/>");
		try{
			buildXML(request, VFSUtils.parseVFSPath("/images"), false);
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, aggregate));
			response.setStatus(Status.SUCCESS_OK);
		}
		catch (VFSPathParseException e) {
			e.printStackTrace();
		}
	}

	private void handlePut(Request request, Response response) {
		try{
			VFSPath path =VFSUtils.parseVFSPath("/images");
//			putFiles(request, path, url);
			buildXML(request, path, true);
		}
		catch (VFSPathParseException e) {
			e.printStackTrace();
		}



	}

	private void putFiles(Request request, VFSPath directory, String footprint){
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
	}

	@Override
	public void performService(Request request, Response response) {
		if(request.getMethod()==Method.GET) handleGet(request, response);
		else if(request.getMethod()==Method.PUT) handlePut(request, response);
		else response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);


	}

	private void buildXML(Request request, VFSPath path, boolean put){
		System.out.println("building xml " + path.toString());
		try{
			VFSPathToken[] listing = vfs.list(path);
			for(int i=0;i<listing.length; i++){
				VFSPath cur = path.child(listing[i]);
				if(vfs.isCollection(cur)){
					buildXML(request, cur, put);
				}
				else{
					try{
						if(cur.getName().endsWith(".xml")){
							Document image = vfs.getDocument(cur);
							NodeList list = image.getDocumentElement().getElementsByTagName("image");
							String sp_id = image.getDocumentElement().getAttribute("id");
							Document doc = BaseDocumentUtils.getInstance().createDocumentFromString("<images/>");
							
							List<Element> elList = new ArrayList<Element>();
							for(int t=0;t<list.getLength();t++){
								Map<Long, String> alreadySeen = new HashMap<Long, String>(); 
								Node curNode = list.item(t);
								if(curNode.getAttributes().getNamedItem("showRedlist")!=null && curNode.getAttributes().getNamedItem("showRedlist").getTextContent().equals("true")){
									Element el = DocumentUtils.createElementWithText(doc, "image", "");
									
//									el.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(doc, "image_id", 
//											"http://"+request.getResourceRef().getHostDomain()+":"+
//											request.getResourceRef().getHostPort()+"/raw/images/bin/"+
//											curNode.getAttributes().getNamedItem("id").getTextContent()+
//											ManagedImageData.getExtensionFromEncoding(curNode
//													.getAttributes().getNamedItem("encoding").getTextContent())));
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
									System.out.println("putting " + destinationURL + "/" + cur.toString().replace("/images/", ""));
									Request req = new Request(Method.PUT, destinationURL + "/" + cur.toString().replace("/images/", ""));
									ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, request.getChallengeResponse().getCredentials());
									req.setChallengeResponse(cr);
									req.setEntity(new DomRepresentation(MediaType.TEXT_XML, doc));
									Response res = client.handle(req);
									System.out.println(res.getStatus());
								} else {
									aggregate.getDocumentElement().appendChild(aggregate.adoptNode(doc.getDocumentElement()));
								}
							}
						}
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
