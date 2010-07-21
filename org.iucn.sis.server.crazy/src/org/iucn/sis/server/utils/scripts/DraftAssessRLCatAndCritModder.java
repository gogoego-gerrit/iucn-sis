package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.solertium.db.ExecutionContext;
import com.solertium.vfs.VFS;

public class DraftAssessRLCatAndCritModder extends BaseDraftAssessmentModder {

	public static class DraftAssessRLCatAndCritModderResource extends Resource {

		public DraftAssessRLCatAndCritModderResource() {
		}

		public DraftAssessRLCatAndCritModderResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BaseDraftAssessmentModder.running) {
				String wsID = (String) getRequest().getAttributes().get("wsID");
				String wsURL = null;

				if (wsID != null)
					wsURL = "/workingsets/" + wsID + ".xml";

				new Thread(new DraftAssessRLCatAndCritModder(SISContainerApp.getStaticVFS(), wsURL)).run();
				System.out.println("Started a new RL cat and crit data fixer!");
			} else
				System.out.println("A draft assessment script is already running!");

			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
			sb.append("DraftAssessFinalCarolineMods is running...");
			sb.append("</body></html>");

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	public DraftAssessRLCatAndCritModder(File vfsRoot, String workingSetURL) {
		super(vfsRoot, workingSetURL, false);
	}

	public DraftAssessRLCatAndCritModder(VFS vfs, String workingSetURL) {
		super(vfs, workingSetURL, false);
	}

	@Override
	protected void workOnAssessment(AssessmentData data, TaxonNode node) {
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
		
		if( catCrit.size() == 10 ) {
			catCrit.add("");
			catCrit.add("");
			writeBackDraftAssessment(data);
		} else if( catCrit.size() == 11 ) {
			catCrit.add("");
			writeBackDraftAssessment(data);
		}
	}
}
