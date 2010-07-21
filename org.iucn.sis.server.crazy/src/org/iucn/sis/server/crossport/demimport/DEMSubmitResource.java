package org.iucn.sis.server.crossport.demimport;

import java.io.File;
import java.io.IOException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.solertium.util.RandString;
import com.solertium.util.SysDebugger;

public class DEMSubmitResource extends Resource {

	public DEMSubmitResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	@Override
	public void acceptRepresentation(Representation entity) throws ResourceException {
		String user = (String) getRequest().getAttributes().get("user");
		System.out.println("DEMImport request came from " + user);

		DiskFileItemFactory factory = new DiskFileItemFactory();
		RestletFileUpload upload = new RestletFileUpload(factory);
		try {
			//if (DEMImport.isRunning() || NewGAADEMImport.isRunning() || GAADEMImport.isRunning()
			//		|| GMADEMImport.isRunning()) {
			if (DEMImport.isRunning()) {
				throw new IOException("A DEM Import is currently running.  "
						+ "Only one DEM can be imported into SIS at a time.  "
						+ "Please click the DEM Import tab to refresh the status of your import.");
			}
			for (FileItem item : upload.parseRequest(getRequest())) {
				File temp = File.createTempFile("dem-import-" + RandString.getString(8), ".mdb");
				SysDebugger.getInstance().println("Writing DEM to " + temp.getPath());
				try {
					item.write(temp);
					SysDebugger.getInstance().println("Done writing DEM to " + temp.getPath());
				} catch (Exception x) {
					throw new IOException("Could not write item to " + temp.getPath());
				}

				Thread t;

//				if (item.getName().equals("gaa.mdb")) {
//					System.out.println("Starting a GAA DEM Import!");
//					// GAADEMImport di = new GAADEMImport(temp,
//					// getContext().getClientDispatcher(), item.getName(),
//					// user == null ? "admin" : user);
//					// t = new Thread(di);
//					StringBuilder sb = new StringBuilder();
//					sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
//					sb.append("Re-importing GAA data is currently not supported.");
//					sb.append("</body></html>");
//					getResponse().setStatus(Status.SUCCESS_OK);
//					getResponse().setEntity(new StringRepresentation(sb, MediaType.TEXT_HTML));
//				} else if (item.getName().equals("gaa_new.mdb")) {
//					System.out.println("Starting a New GAA DEM Import!");
//					NewGAADEMImport di = new NewGAADEMImport(temp, getContext().getClientDispatcher(), item.getName(),
//							user == null ? "admin" : user);
//					t = new Thread(di);
//					t.start();
//
//					StringBuilder sb = new StringBuilder();
//					sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
//					sb.append("Importing new GAA data...");
//					sb.append("</body></html>");
//					getResponse().setStatus(Status.SUCCESS_OK);
//					getResponse().setEntity(new StringRepresentation(sb, MediaType.TEXT_HTML));
//				} else if (item.getName().equals("gma.mdb")) {
//					System.out.println("Starting a GMA DEM Import!");
//					// GMADEMImport di = new GMADEMImport(temp,
//					// getContext().getClientDispatcher(), item.getName(),
//					// user == null ? "admin" : user);
//					// t = new Thread(di);
//					StringBuilder sb = new StringBuilder();
//					sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
//					sb.append("Re-importing GMA data is currently not supported.");
//					sb.append("</body></html>");
//					getResponse().setStatus(Status.SUCCESS_OK);
//					getResponse().setEntity(new StringRepresentation(sb, MediaType.TEXT_HTML));
//				} 
//				else {
					System.out.println("Starting a regular DEM Import!");
					DEMImport di = new DEMImport(temp, getContext(), item.getName(),
							getRequest().getChallengeResponse().getIdentifier(), 
							String.valueOf(getRequest().getChallengeResponse().getSecret()));
					t = new Thread(di);
					t.start();

					StringBuilder sb = new StringBuilder();
					sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
					sb.append("DEM file was received and is being processed.");
					sb.append("</body></html>");
					getResponse().setStatus(Status.SUCCESS_OK);
					getResponse().setEntity(new StringRepresentation(sb, MediaType.TEXT_HTML));
//				}
			}
		} catch (FileUploadException fx) {
			fx.printStackTrace();
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			getResponse()
					.setEntity(new StringRepresentation("Upload failed: " + fx.getMessage(), MediaType.TEXT_PLAIN));
		} catch (IOException ix) {
			ix.printStackTrace();
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			getResponse().setEntity(
					new StringRepresentation("Storage failed: " + ix.getMessage(), MediaType.TEXT_PLAIN));
		}
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	private String generatePriorImportsStatus() {
		String ret = "<span><p><b>DEMImport Logging Information</b> -- ";
		ret += "<a target=\"_blank\" href=\"/raw" + DEMImportInformation.logURL
				+ "\">(Download full status log)</a><br>";

		String htmlLog = DocumentUtils.getVFSFileAsString(DEMImportInformation.htmlLogURL, SISContainerApp
				.getStaticVFS());

		if (htmlLog == null || htmlLog.equals(""))
			ret += "There is no DEMImport logging information available.";
		else
			ret += htmlLog;

		ret += "</p></span>";

		return ret;
	}

	@Override
	public Representation represent(final Variant variant) {
		StringBuilder sb = new StringBuilder();
//		if (DEMImport.isRunning() || NewGAADEMImport.isRunning() || GAADEMImport.isRunning()
//				|| GMADEMImport.isRunning()) {
		if (DEMImport.isRunning()) {
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>A DEM Import is currently running.  "
					+ "Only one DEM can be imported into SIS at a time.  " + "Please wait.</body></html>");
		} else {
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'><form method=\"post\" enctype=\"multipart/form-data\">");
			sb.append("Attach DEM database: ");
			sb.append("<input type=\"file\" name=\"dem\" size=\"60\" style='font-family:Verdana; font-size:x-small'/>");
			sb.append("<p><input type=\"submit\" onclick=\"this.disabled=true;\" style='font-family:Verdana; font-size:x-small'/>");
			sb.append("</form>");
			sb.append(generatePriorImportsStatus());
			sb.append("</body></html>");
		}

		return new StringRepresentation(sb, MediaType.TEXT_HTML);
	}
}
