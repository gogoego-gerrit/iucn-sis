package org.iucn.sis.server.extensions.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.hibernate.Session;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

public class ReportDownloadRestlet extends BaseServiceRestlet {
	
	public ReportDownloadRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/download/{file}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		String file = (String)request.getAttributes().get("file");
		if (file == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		final InputRepresentation dl;
		try {
			dl = new InputRepresentation(new FileInputStream(open(file)));
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		dl.setDownloadable(true);
		dl.setDownloadName(file);
			
		return dl;
	}
	
	private File open(String fileName) throws ResourceException {
		File tmp;
		try {
			tmp = File.createTempFile("toDelete", "tmp");
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INSUFFICIENT_STORAGE, e);
		}
		
		try {
			String folder = fileName.split("\\.")[0];
			File tmpFolder = new File(tmp.getParentFile(), folder);
			File tmpFile = new File(tmpFolder, fileName);
			
			if (!tmpFile.exists())
				throw new ResourceException(Status.CLIENT_ERROR_GONE);
			
			return tmpFile;
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_GONE, e);
		} finally {
			tmp.delete();
		}
	}

}
