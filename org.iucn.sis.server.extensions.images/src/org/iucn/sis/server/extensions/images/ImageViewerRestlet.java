package org.iucn.sis.server.extensions.images;

import java.io.IOException;

import org.gogoego.api.plugins.GoGoEgo;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.debug.Debug;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.ElementCollection;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

public class ImageViewerRestlet extends BaseServiceRestlet {
	
	private final VFS vfs;

	public ImageViewerRestlet(Context context) {
		super(context);
		vfs = SIS.get().getVFS();
	}

	@Override
	public void definePaths() {
		paths.add("/images/view/{mode}/{taxonid}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		final String mode = (String) request.getAttributes().get("mode");
		final Integer taxonID = getTaxonID(request);
		final String uri = request.getResourceRef().getRemainingPart();
		
		
		final VFSPath stripedPath = 
			new VFSPath("/images/" + FilenameStriper.getIDAsStripedPath(taxonID) + ".xml");
		
		if (!vfs.exists(stripedPath)) {
			return notAvailable();
		}
		
		final Document document;
		try {
			document = vfs.getDocument(stripedPath);
		} catch (IOException e) {
			return notAvailable();
		}
		
		final ElementCollection nodes = 
			new ElementCollection(document.getDocumentElement().getElementsByTagName("image"));
		for (Element node : nodes) {
			String encoding = node.getAttribute("encoding");
			
			String extension;
			MediaType mt;
			if ("image/jpeg".equals(encoding)) {
				extension = "jpg";
				mt = MediaType.IMAGE_JPEG;
			}
			else if ("image/gif".equals(encoding)) {
				extension = "gif";
				mt = MediaType.IMAGE_GIF;
			}
			else if ("image/png".equals(encoding)) {
				extension = "png";
				mt = MediaType.IMAGE_PNG;
			}
			else {
				Debug.println("Invalid file encoding {0} was found for taxon {1}, skipping...", encoding, taxonID);
				continue;
			}
			
			String currentItemUri = "/" + node.getAttribute("id") + "." + extension;
			boolean isPrimary = "true".equals(node.getAttribute("primary"));
			
			if (uri.equals(currentItemUri) || (uri.equals("primary") && isPrimary)) {
				final String path = "/images/bin" + currentItemUri;
				if ("thumb".equals(mode)) {
					String sizeStr = request.getResourceRef().getQueryAsForm().getFirstValue("size", "100");
					int size;
					try {
						size = Integer.parseInt(sizeStr);
					} catch (Exception e) {
						size = 100;
					}
					
					try {
						return represent(GoGoEgo.get().getImageManipulatorHelper(getContext()).
								getResizedURI(path, size), mt);
					} catch (Throwable e) {
						Debug.println("Failed to resize image, returning default instead.");
						return represent(path, mt);
					}
				}
				else {
					return represent(path, mt);
				}
			}
		}
		
		return notAvailable();
	}
	
	private Representation represent(String path, MediaType mt) throws ResourceException {
		try {
			return new InputRepresentation(
				vfs.getInputStream(new VFSPath(path)), mt
			);
		} catch (IOException e) {
			return notAvailable();
		}
	}
	
	private Representation notAvailable() {
		return new InputRepresentation(
			getClass().getResourceAsStream("unavailable.png"), 
			MediaType.IMAGE_PNG
		);
	}
	
	private Integer getTaxonID(Request request) throws ResourceException {
		try {
			return Integer.valueOf((String)request.getAttributes().get("taxonid"));
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid taxon ID supplied.", e);
		}
	}

}
