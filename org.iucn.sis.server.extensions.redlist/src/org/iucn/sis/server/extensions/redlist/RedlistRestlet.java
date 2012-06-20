package org.iucn.sis.server.extensions.redlist;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.models.TaxonImage;
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
import org.restlet.util.Triple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.vfs.VFS;
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
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		Triple<String, String, String> props = null;
		
		boolean publish = "true".equals(request.getResourceRef().getQueryAsForm().getFirstValue("publish", "false"));
		if (publish)
			props = getPublicationProperties();
		
		Document document; //BaseDocumentUtils.getInstance().createDocumentFromString("<images/>");

		try {
			document = generateXML(session, props);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
		
		/*
		try {
			buildXML(document, request.getChallengeResponse(), getDestinationURL(getContext()), new VFSPath("/images"), false);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}*/
		
		return new DomRepresentation(MediaType.TEXT_XML, document);
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException { 
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
	
	@SuppressWarnings("unchecked")
	private Document generateXML(Session session, Triple<String, String, String> publishProps) throws PersistentException, ResourceException {
		final List<TaxonImage> images = session.createCriteria(TaxonImage.class)
			.add(Restrictions.eq("showRedList", true))
			.addOrder(Order.asc("Taxon.id"))
			.addOrder(Order.desc("primary"))
			.createAlias("taxon", "Taxon")
			.list();
		
		final Document master = BaseDocumentUtils.impl.newDocument();
		final Element root = master.createElement("root");
		master.appendChild(root);
		
		Document current = null;
		Integer taxon = null;
		
		for (TaxonImage image : images) {
			if (taxon == null || !taxon.equals(image.getTaxon().getId())) {
				if (current != null) {
					publish(taxon, current, publishProps);
					
					root.appendChild(master.importNode(current.getDocumentElement(), true));
				}
				
				current = BaseDocumentUtils.impl.newDocument();
				current.appendChild(current.createElement("images"));
			}
			
			taxon = image.getTaxon().getId();
			
			String encoding = image.getEncoding();
			
			String extension;
			if ("image/jpeg".equals(encoding)) {
				extension = "jpg";
			}
			else if ("image/gif".equals(encoding)) {
				extension = "gif";
			}
			else if ("image/png".equals(encoding)) {
				extension = "png";
			}
			else {
				extension = "jpg";
			}
			
			String currentItemUri = image.getIdentifier() + "." + extension;
			
			String sp_id = Integer.toString(taxon);
			String isPrimary = Boolean.toString(image.getPrimary());
			String imageID = currentItemUri; //Integer.toString(image.getId());
			
			//If this image
			Element el = current.createElement("image");
			el.setAttribute("image_id", imageID);
			el.setAttribute("sp_id", sp_id);
			el.setAttribute("primary", isPrimary);
			
			if (!isBlank(image.getCredit()))
				el.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(current, "credit", image.getCredit()));
			
			if (!isBlank(image.getSource()))
				el.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(current, "source", image.getSource()));
			
			if (!isBlank(image.getCaption()))
				el.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(current, "caption", image.getCaption()));

			current.getDocumentElement().appendChild(el);
		}
		
		return master;
	}
	
	private void publish(Integer taxonID, Document document, Triple<String, String, String> publishProps) throws ResourceException {
		if (publishProps != null && document != null && taxonID != null) {
			Client client = new Client(Protocol.HTTPS);
			Request req = new Request(Method.PUT, publishProps.getFirst() + "/" + taxonID + ".xml");
			ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, publishProps.getSecond(), publishProps.getThird());
			req.setChallengeResponse(cr);
			req.setEntity(new DomRepresentation(MediaType.TEXT_XML, document));
			
			Response resp = client.handle(req);
			if (!resp.getStatus().isSuccess())
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Request failed due to error: " + resp.getStatus().getCode());
		}
		else {
			final VFSPath path = new VFSPath("/redlist/published");
			final VFS vfs = SIS.get().getVFS();
			
			if (!vfs.exists(path)) {
				try {
					vfs.makeCollections(path);
				} catch (Exception e) {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Failed to create directory.");
				}
			}
			
			DocumentUtils.writeVFSFile(path + "/" + FilenameStriper.getIDAsStripedPath(taxonID+"") + ".xml", vfs, document);
		}
	}
	
	private Triple<String, String, String> getPublicationProperties() {
		Triple<String, String, String> props = null;
		
		Properties properties = SIS.get().getSettings(getContext());
			
		String destinationURL = properties.getProperty("org.iucn.sis.server.extensions.redlist.imagePublishURL", 
					"https://rl2009.gogoego.com/admin/files/images/published");
		String identifier = properties.getProperty("org.iucn.sis.server.extensions.redlist.publish.identifier");
		String secret = properties.getProperty("org.iucn.sis.server.extensions.redlist.publish.secret");
		
		if (identifier != null && secret != null)
			props = new Triple<String, String, String>(destinationURL, identifier, secret);
		
		return props;
	}

	private boolean isBlank(String value) {
		return value == null || "".equals(value);
	}
	
	/*
	 * FIXME: stop try/catching everything. 
	 * 
	 */
	private void buildXML(Document document, ChallengeResponse challenge, String destinationURL, VFSPath path, boolean put) throws IOException, ResourceException {
		VFS vfs = SIS.get().getVFS();
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
