package org.iucn.sis.server.utils.scripts;

import java.io.File;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.OccurrenceMigratorUtils;
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

public class DraftAssessOccurrenceMigrator extends BaseDraftAssessmentModder {

	public static class DraftAssessOccurrenceMigratorResource extends Resource {

		public DraftAssessOccurrenceMigratorResource() {
		}

		public DraftAssessOccurrenceMigratorResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BaseDraftAssessmentModder.running) {
				new Thread(new DraftAssessOccurrenceMigrator(SISContainerApp.getStaticVFS())).run();
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

	public DraftAssessOccurrenceMigrator(File vfsRoot) {
		super(vfsRoot, null, false);
	}

	public DraftAssessOccurrenceMigrator(VFS vfs) {
		super(vfs, null, false);
	}

	@Override
	protected boolean ignoreThisAssessment(String id) {
		return false;
	}

	@Override
	protected void workOnAssessment(AssessmentData data, TaxonNode node) {
		if (OccurrenceMigratorUtils.migrateOccurrenceData(data)) {
			// System.out.println("Writing back assessment " +
			// data.getAssessmentID());
			writeBackDraftAssessment(data);
		}
	}
}
