package org.iucn.sis.server.extensions.offline;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;

import com.solertium.util.BaseDocumentUtils;

public class OfflineBackupRestlet extends Restlet {
	
	public OfflineBackupRestlet(Context context) {
		super(context);
	}
	
	@Override
	public void handle(Request request, Response response) {
		String location = OfflineBackupWorker.backup();
		if (location != null) {
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, 
				BaseDocumentUtils.impl.createConfirmDocument("Backup Successful! Location: "+location)));
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, 
				BaseDocumentUtils.impl.createErrorDocument("Backup Error.")));
		}
	}
			
}
