package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.server.io.WorkingSetIO;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.MostRecentFlagger;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.w3c.dom.Document;

import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class PublishedAssessmentRLCatAndCritModder extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentRLCatAndCritModderResource extends Resource {

		public PublishedAssessmentRLCatAndCritModderResource() {
		}

		public PublishedAssessmentRLCatAndCritModderResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			boolean writeback = false;
			String wb = (String)getRequest().getAttributes().get("writeback");
			if( wb != null && wb.equalsIgnoreCase("true") )
				writeback = true;
			
			System.out.println("Writeback is " + writeback);
			try {
				if (!BasePublishedAssessmentModder.running) {
					new Thread(new PublishedAssessmentRLCatAndCritModder(SISContainerApp.getStaticVFS(), writeback)).run();
					System.out.println("Started a new historian!");
				} else
					System.out.println("A published assessment script is already running!");

				StringBuilder sb = new StringBuilder();
				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append(BasePublishedAssessmentModder.results.toString());
				sb.append("</body></html>");

				return new StringRepresentation(sb, MediaType.TEXT_HTML);
			} catch (Exception e) {
				
				StringBuilder sb = new StringBuilder();
				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append(DocumentUtils.getStackTraceAsString(e));
				sb.append("</body></html>");
				return new StringRepresentation(sb.toString(), MediaType.TEXT_HTML);
			}
		}
	}

	private boolean writeback = false;

	public PublishedAssessmentRLCatAndCritModder(File vfsRoot, boolean writeback) throws Exception {
		super(vfsRoot);
		this.writeback = writeback;
	}

	public PublishedAssessmentRLCatAndCritModder(VFS vfs, boolean writeback) throws Exception {
		super(vfs);
		this.writeback = writeback;
	}

	@Override
	protected void workOnFullList(List<AssessmentData> assessments) {
		for( AssessmentData data : assessments ) {
			boolean writeBackAssessment = false;

			List<String> catCrit = (List<String>)data.getDataMap().get(CanonicalNames.RedListCriteria);
			if( catCrit == null )
				System.out.println("Um. Assessment " + data.getUID() + " has no red listing?????");
			else {
				String version = catCrit.get(1);
				if( version.trim().equals("2.3") )
					catCrit.set(1, "1");
				else if( version.trim().equals("3.1") )
					catCrit.set(1, "0");
			}
			
			if( catCrit.size() == 10 )
				catCrit.add("");
			
			catCrit.add("");
			
			if (writeback && writeBackAssessment)
				writeBackPublishedAssessment(data);
		}
	}
	
	@Override
	protected void workOnHistorical(AssessmentData data) {
		//Nothing to do.
	}

	@Override
	protected void workOnMostRecent(AssessmentData data) {
		//Nothing to do.
	}
}
