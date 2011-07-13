//package org.iucn.sis.server.extensions.export.access.exported;
//
//import org.gogoego.api.plugins.GoGoEgo;
//import org.restlet.Context;
//import org.restlet.data.MediaType;
//import org.restlet.data.Request;
//import org.restlet.data.Response;
//import org.restlet.data.Status;
//import org.restlet.representation.Representation;
//import org.restlet.representation.StringRepresentation;
//import org.restlet.representation.Variant;
//import org.restlet.resource.Resource;
//import org.restlet.resource.ResourceException;
//
//public class AccessExportResource extends Resource {
//
//	public AccessExportResource(final Context context, final Request request, final Response response) {
//		super(context, request, response);
//		getVariants().add(new Variant(MediaType.TEXT_HTML));
//	}
//
//	@Override
//	public void acceptRepresentation(Representation entity) throws ResourceException {
//		String id = (String) getRequest().getAttributes().get("workingsetid");
//		if (!AccessExport.isRunning()) {
//			// String id =
//			// (String)getRequest().getEntityAsForm().getFirstValue("wsid");
//			if (id != null && !id.equals("")) {
//				System.out.println("WDID:    " + id);
//				new Thread(new AccessExport(getContext().getClientDispatcher(), id, getContext())).start();
//			} else
//				new Thread(new AccessExport(getContext().getClientDispatcher(), getContext())).start();
//		}
//		final StringBuilder sb = new StringBuilder();
//		sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>"
//				+ "The Access database image is being generated.  "
//				+ "Please wait. <p/> <a href='javascript:window.location.reload()'>Reload</a></body></html>");
//		getResponse().setStatus(Status.SUCCESS_OK);
//		getResponse().setEntity(new StringRepresentation(sb, MediaType.TEXT_HTML));
//	}
//
//	@Override
//	public boolean allowPost() {
//		return true;
//	}
//
//	@Override
//	public Representation represent(final Variant variant) {
//		final StringBuilder sb = new StringBuilder();
//
//		String workingsetid = (String) getRequest().getAttributes().get("workingsetid");
//		String actionURL = "/export/access" + (workingsetid == null ? "" : ("/" + workingsetid));
//
//		if (AccessExport.isRunning()) {
//			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>"
//					+ "The Access database image is presently being generated.  "
//					+ "Please wait. <p/> <a href='javascript:window.location.reload()'>Reload</a></body></html>");
//		} else {
//			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
//
//			String mdbFile;
//			String zipFile;
//			if (workingsetid != null) {
//				mdbFile = "export_" + workingsetid + ".mdb";
//				zipFile = "export_" + workingsetid + ".zip";
//			} else {
//				mdbFile = GoGoEgo.getInitProperties().getProperty("INSTANCE") + ".mdb";
//				zipFile = GoGoEgo.getInitProperties().getProperty("INSTANCE") + ".zip";
//			}
//			sb
//					.append("<a href=\"/export/download/zip/"
//							+ zipFile
//							+ "\" target=\"_blank\">Download latest SIS Access image, zipped, small file size (highly recommended).</a><p/>");
////			sb
////					.append("<a href=\"/export/download/"
////							+ mdbFile
////							+ "\" target=\"_blank\">Download latest SIS Access image, uncompressed, very large file size.</a><p/>");
//			sb.append("<form method=\"post\" action=\"" + actionURL + "\">");
//			if (workingsetid != null)
//				sb.append("The access database will contain all published assessments as well as " +
//						"draft assessments that match the working set's scope. Please ensure the " +
//						"scope is set to include the regions you wish to capture in this export.");
//			sb.append("Rebuild Access database image from scratch: ");
//			// if(workingsetid!=null){
//			// sb.append("<input type=\"hidden\" name=\"wsid\"value=\""+workingsetid+"\" />");
//			// }
//			sb.append("<p><input value=\"Generate\" type=\"submit\" style='font-family:Verdana; font-size:x-small'/>");
//			sb.append("</form>");
//			sb.append("</body></html>");
//		}
//
//		return new StringRepresentation(sb, MediaType.TEXT_HTML);
//	}
//}
