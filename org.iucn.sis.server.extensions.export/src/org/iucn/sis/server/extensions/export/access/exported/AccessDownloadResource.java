package org.iucn.sis.server.extensions.export.access.exported;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

@SuppressWarnings("deprecation")
public class AccessDownloadResource extends Resource {

	public AccessDownloadResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	}

	@Override
	public Representation represent(Variant variant) throws ResourceException {
		String file = (String) getRequest().getAttributes().get("file");
		if (file == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a filename.");
		
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
