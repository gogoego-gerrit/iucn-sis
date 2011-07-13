//package org.iucn.sis.server.extensions.export.access.exported;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//
//import org.restlet.Context;
//import org.restlet.data.MediaType;
//import org.restlet.data.Request;
//import org.restlet.data.Response;
//import org.restlet.data.Status;
//import org.restlet.representation.InputRepresentation;
//import org.restlet.representation.Representation;
//import org.restlet.representation.StringRepresentation;
//import org.restlet.representation.Variant;
//import org.restlet.resource.Resource;
//
//public class AccessDownloadResource extends Resource {
//
//	public AccessDownloadResource(final Context context, final Request request, final Response response) {
//		super(context, request, response);
//		getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
//	}
//
//	@Override
//	public Representation represent(final Variant variant) {
//		String id = (String) getRequest().getAttributes().get("file");
//		String working;
//		if (id == null)
//			working = AccessExport.getWorking();
//		else
//			working = "/usr/data/" + id;
//
//		try {
//			return new InputRepresentation(new FileInputStream(new File(working)), MediaType.APPLICATION_OCTET_STREAM);
//		} catch (final FileNotFoundException fnf) {
//			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
//			return new StringRepresentation("No latest Access database image found to download", MediaType.TEXT_PLAIN);
//		}
//	}
//
//}
