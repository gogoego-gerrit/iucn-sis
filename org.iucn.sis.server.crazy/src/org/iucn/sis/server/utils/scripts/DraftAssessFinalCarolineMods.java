package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

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

public class DraftAssessFinalCarolineMods extends BaseDraftAssessmentModder {

	public static class DraftAssessFinalCarolineModsResource extends Resource {

		public DraftAssessFinalCarolineModsResource() {
		}

		public DraftAssessFinalCarolineModsResource(final Context context, final Request request,
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

				new Thread(new DraftAssessFinalCarolineMods(SISContainerApp.getStaticVFS(), wsURL)).run();
				System.out.println("Started a new Caroline's data fixer!");
			} else
				System.out.println("A draft assessment script is already running!");

			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
			sb.append("DraftAssessFinalCarolineMods is running...");
			sb.append("</body></html>");

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	private ExecutionContext ec;

	private HashMap<String, Boolean> ids;

	public DraftAssessFinalCarolineMods(File vfsRoot, String workingSetURL) {
		super(vfsRoot, workingSetURL, false);
	}

	public DraftAssessFinalCarolineMods(VFS vfs, String workingSetURL) {
		super(vfs, workingSetURL, false);
	}

	@Override
	protected void workOnAssessment(AssessmentData data, TaxonNode node) {
		boolean writeBackAssessment = false;

		if (data.getDataMap().containsKey(CanonicalNames.RedListAssessors)) {
			String assessors = data.getFirstDataPiece(CanonicalNames.RedListAssessors, null);

			if (assessors != null && !assessors.equals("")) {
				if (data.getDataMap().containsKey(CanonicalNames.RedListAssessmentAuthors)) {
					String authors = data.getFirstDataPiece(CanonicalNames.RedListAssessmentAuthors, null);

					if (authors == null || authors.equals("")) {
						data.getDataMap().put(CanonicalNames.RedListAssessmentAuthors, wrapInArray(assessors));
						writeBackAssessment = true;
					}
					// else
					// System.out.println("Assessment " + data.getAssessmentID()
					// + " contains non-empty authors.");
				} else {
					data.getDataMap().put(CanonicalNames.RedListAssessmentAuthors, wrapInArray(assessors));
					writeBackAssessment = true;
				}
			}
			// else
			// System.out.println("Assessment " + data.getAssessmentID() +
			// " has empty assessors.");
		}
		// else
		// System.out.println("Assessment " + data.getAssessmentID() +
		// " contains no assessors.");

		if (writeBackAssessment) {
			// System.out.println("Writing back assessment " +
			// data.getAssessmentID());
			writeBackDraftAssessment(data);
		}

		if (node.getStatus().equalsIgnoreCase("New") || node.getStatus().equalsIgnoreCase("N")) {
			node.setStatus("A");
			writeBackTaxon(node);
		}
	}

	private ArrayList<String> wrapInArray(String data) {
		ArrayList<String> arr = new ArrayList<String>();
		arr.add(data);
		return arr;
	}
}
