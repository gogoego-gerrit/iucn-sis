package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

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

public class DraftAssessCoralsOccurrenceFixer extends BaseDraftAssessmentModder {

	public static class DraftAssessCoralsOccurrenceFixerResource extends Resource {

		public DraftAssessCoralsOccurrenceFixerResource() {
		}

		public DraftAssessCoralsOccurrenceFixerResource(final Context context, final Request request,
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

				new Thread(new DraftAssessCoralsOccurrenceFixer(SISContainerApp.getStaticVFS(), wsURL)).run();
				System.out.println("Started a new Corals occurrence fixer!");
			} else
				System.out.println("A draft assessment script is already running!");

			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
			sb.append("DraftAssessGrouperThreatsFixer is running...");
			sb.append("</body></html>");

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	private ExecutionContext ec;

	private HashMap<String, Boolean> ids;

	public DraftAssessCoralsOccurrenceFixer(File vfsRoot, String workingSetURL) {
		super(vfsRoot, workingSetURL, false);
	}

	public DraftAssessCoralsOccurrenceFixer(VFS vfs, String workingSetURL) {
		super(vfs, workingSetURL, false);
	}

	@Override
	protected boolean ignoreThisAssessment(String id) {
		if (ids == null)
			return false;

		return !ids.containsKey(id);
	}

	private boolean setOrigins(HashMap<String, ArrayList<String>> selected) {
		boolean writeBack = false;

		for (Entry<String, ArrayList<String>> entry : selected.entrySet()) {
			ArrayList<String> values = entry.getValue();

			if (!values.get(2).equals("1")) {
				values.set(2, "1");
				writeBack = true;
			}
		}

		return writeBack;
	}

	@Override
	protected void workOnAssessment(AssessmentData data, TaxonNode node) {
		boolean writeBackAssessment = false;

		if (data.getDataMap().containsKey(CanonicalNames.CountryOccurrence))
			if (setOrigins((HashMap<String, ArrayList<String>>) data.getDataMap().get(CanonicalNames.CountryOccurrence)))
				writeBackAssessment = true;
		if (data.getDataMap().containsKey(CanonicalNames.FAOOccurrence))
			if (setOrigins((HashMap<String, ArrayList<String>>) data.getDataMap().get(CanonicalNames.FAOOccurrence)))
				writeBackAssessment = true;
		if (data.getDataMap().containsKey(CanonicalNames.LargeMarineEcosystems))
			if (setOrigins((HashMap<String, ArrayList<String>>) data.getDataMap().get(
					CanonicalNames.LargeMarineEcosystems)))
				writeBackAssessment = true;

		if (writeBackAssessment) {
			System.out.println("Writing back assessment " + data.getAssessmentID());
			writeBackDraftAssessment(data);
		}
	}
}
