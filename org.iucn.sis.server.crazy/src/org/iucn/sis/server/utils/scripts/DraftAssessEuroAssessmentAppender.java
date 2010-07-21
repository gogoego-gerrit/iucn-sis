package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.XMLUtils;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.restlet.Context;
import org.restlet.Uniform;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.solertium.vfs.VFS;

public class DraftAssessEuroAssessmentAppender extends BaseDraftAssessmentModder {

	public static class DraftAssessEuroAssessmentAppenderResource extends Resource {

		public DraftAssessEuroAssessmentAppenderResource() {
		}

		public DraftAssessEuroAssessmentAppenderResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BaseDraftAssessmentModder.running) {
				String wsID = (String) getRequest().getAttributes().get("wsID");
				String wsURL = null;

				if (wsID != null) {
					wsURL = "/workingsets/" + wsID + ".xml";
					new Thread(new DraftAssessEuroAssessmentAppender(SISContainerApp.getStaticVFS(), wsURL,
							getContext().getClientDispatcher())).run();
					System.out.println("Started a new draft publisher!!");
				} else {
					StringBuilder sb = new StringBuilder();
					sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
					sb.append("You must supply a working set ID.");
					sb.append("</body></html>");

					return new StringRepresentation(sb, MediaType.TEXT_HTML);
				}

			} else
				System.out.println("A draft assessment script is already running!");

			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
			sb.append("DraftAssessFinalCarolineMods is running...");
			sb.append("</body></html>");

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	private Uniform uniform;

	private String uriPrefix = "riap://host";

	public DraftAssessEuroAssessmentAppender(File vfsRoot, String workingSetURL, Uniform uniform) {
		super(vfsRoot, workingSetURL, false);
		this.uniform = uniform;
	}

	public DraftAssessEuroAssessmentAppender(VFS vfs, String workingSetURL, Uniform uniform) {
		super(vfs, workingSetURL, false);
		this.uniform = uniform;
	}

	@Override
	protected void workOnAssessment(AssessmentData data, TaxonNode node) {

		if (data.isGlobal() || !data.getRegionIDs().contains("0"))
			return;

		List<String> ut;
		if (data.getDataMap().containsKey(CanonicalNames.UseTradeDocumentation))
			ut = (ArrayList<String>) data.getDataMap().get(CanonicalNames.UseTradeDocumentation);
		else {
			ut = new ArrayList<String>();
			ut.add("");
		}

		String contents = ut.get(0);
		if (!contents.equals(""))
			contents += XMLUtils.clean("<br><br>");
		contents += "Saproxylic Coleoptera tend to be popular with beetle "
				+ "collectors although trade is rarely an issue, the only exceptions "
				+ "being a few larger species of more dramatic form or colour.";

		ut.clear();
		ut.add(contents);

		writeBackDraftAssessment(data);
	}
}
