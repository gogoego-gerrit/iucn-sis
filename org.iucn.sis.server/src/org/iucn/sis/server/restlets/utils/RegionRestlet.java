package org.iucn.sis.server.restlets.utils;

import java.util.HashMap;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
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

import com.solertium.lwxml.java.JavaNativeDocument;
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

	public static HashMap<String, String> nameToID;

	public RegionRestlet(Context context) {
		super(context);
		nameToID = new HashMap<String, String>();
	}

	@Override
	public void definePaths() {
		paths.add("/regions");
		paths.add("/regions/{regionID}");
	}

	public Representation handleGet(Request request, Response response) throws ResourceException {
		StringBuilder ret = new StringBuilder("<regions>");
		List<Region> regions;
		try {
			regions = SIS.get().getRegionIO().getRegions();
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		for (Region reg : regions)
			ret.append(reg.toXML());
		ret.append("</regions>");
		
		return new StringRepresentation(ret.toString(), MediaType.TEXT_XML);	
	}

	@Override
	public void handlePost(Representation entity, Request request, Response response) throws ResourceException {
		NativeDocument ndoc = new JavaNativeDocument();
		try {
			ndoc.parse(entity.getText());
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		NativeNodeList list = ndoc.getDocumentElement().getElementsByTagName(Region.ROOT_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			Region regionUpdated = Region.fromXML(list.elementAt(i));
			try {
				SIS.get().getRegionIO().saveRegion(regionUpdated);
			} catch (PersistentException e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		}
		response.setEntity(handleGet(request, response));
	}
	
	/*
	 * FIXME: I don't think this does what it is supposed to do...
	 * Should write to the database, not the file system...
	 */
	public void handlePut(Representation entity, Request request, Response response) throws ResourceException {
		NativeDocument ndoc = new JavaNativeDocument();
		try {
			ndoc.parse(entity.getText());
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}

		try {
			SIS.get().getRegionIO().saveRegion(Region.fromXML(ndoc.getDocumentElement().getElementByTagName("region")));
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
}
