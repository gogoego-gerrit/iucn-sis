package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.structures.FormattingStripper;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.solertium.vfs.VFS;

public class DraftAssessRTAFormatter extends BaseDraftAssessmentModder {

	public static class DraftAssessRTAFormatterResource extends Resource {

		public DraftAssessRTAFormatterResource() {
		}

		public DraftAssessRTAFormatterResource(final Context context, final Request request, final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BaseDraftAssessmentModder.running) {
				new Thread(new DraftAssessRTAFormatter(SISContainerApp.getStaticVFS())).run();
				System.out.println("Started a new Draft assessment COO fixer!");
			} else
				System.out.println("A draft assessment script is already running!");

			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
			sb.append("DraftAssessGrouperThreatsFixer is running...");
			sb.append("</body></html>");

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	public DraftAssessRTAFormatter(File vfsRoot) {
		super(vfsRoot, null, false);
	}

	public DraftAssessRTAFormatter(VFS vfs) {
		super(vfs, null, false);
	}

	@Override
	protected boolean ignoreThisAssessment(String id) {
		return false;
	}

	@Override
	protected void workOnAssessment(AssessmentData data, TaxonNode node) {
		boolean doWriteBack = true;

		String[] todo = new String[] { CanonicalNames.ConservationActionsDocumentation,
				CanonicalNames.HabitatDocumentation, CanonicalNames.PopulationDocumentation,
				CanonicalNames.RangeDocumentation, CanonicalNames.ThreatsDocumentation,
				CanonicalNames.UseTradeDocumentation, CanonicalNames.RedListRationale };

		for (String cur : todo) {
			if (data.getDataMap().containsKey(cur)) {
				ArrayList<String> text = (ArrayList<String>) data.getDataMap().get(cur);
				String repaired = FormattingStripper.stripText(text.get(0).trim()).trim();
				if (!repaired.trim().equals(text.get(0).trim())) {
					// System.out.println("Repaired: " + repaired +
					// " \nOriginal: " + text.get(0));
					((ArrayList<String>) data.getDataMap().get(cur)).set(0, repaired);
					// doWriteBack = true;
				}
			}
		}

		if (doWriteBack) {
			System.out.println("Fixed formatting for RTAs in " + data.getAssessmentID());
			writeBackDraftAssessment(data);
		}
	}
}
