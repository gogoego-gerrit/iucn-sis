package org.iucn.sis.server.extensions.offline;

import java.io.File;

import org.hibernate.Session;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.OfflineMetadata;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

public class OfflineMetaDataRestlet extends BaseServiceRestlet {
	
	public OfflineMetaDataRestlet(Context context) {
		super(context);
	}
	
	@Override
	public void definePaths() {
		paths.add("/offline/metadata");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		OfflineMetadata metadata = OfflineBackupWorker.get();
		if (metadata == null)
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "SIS Configuration error.");
		
		return new StringRepresentation(metadata.toXML(), MediaType.TEXT_XML);		
	}
	
	public String getDbNameFromUri(String uri){		
		File file = new File(uri);
		return file.getName();
	}
	
	public String removeJDBCPrefix(String uri){		
		String path = uri.substring(uri.indexOf("file:")+5,uri.length());
		return path;
	}
}
