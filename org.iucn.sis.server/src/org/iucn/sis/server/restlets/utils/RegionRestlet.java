package org.iucn.sis.server.restlets.utils;

import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.RegionIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.Region;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;

/**
 * Serves Region information. POST can handle multiple Region update/creation
 * requests and returns the entire list of Regions, PUT can handle one Region at
 * a time, and returns the new/existing ID. Incoming payload for these should be
 * an XML document with any document element name then serialized Region
 * object(s) as children.
 * 
 * @author adam.schwartz
 * 
 */
public class RegionRestlet extends BaseServiceRestlet {

	public RegionRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/regions");
		paths.add("/regions/{regionID}");
	}

	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		RegionIO regionIO = new RegionIO(session);
		StringBuilder ret = new StringBuilder("<regions>");
		List<Region> regions;
		try {
			regions = regionIO.getRegions();
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}		
		for (Region reg : regions)
			ret.append(reg.toXML());
		ret.append("</regions>");
		
		return new StringRepresentation(ret.toString(), MediaType.TEXT_XML);	
	}

	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		NativeDocument ndoc = getEntityAsNativeDocument(entity);
		RegionIO regionIO = new RegionIO(session);
		NativeNodeList list = ndoc.getDocumentElement().getElementsByTagName(Region.ROOT_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			Region regionUpdated = Region.fromXML(list.elementAt(i));
			try {	
				regionIO.saveRegion(regionUpdated);
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			} catch (ResourceException e){
				throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
			}
		}
		response.setEntity(handleGet(request, response, session));
	}
	
}
