package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.solertium.vfs.VFS;

public class PublishedAssessmentReferenceFixer extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentReferenceFixerResource extends Resource {

		public PublishedAssessmentReferenceFixerResource() {
		}

		public PublishedAssessmentReferenceFixerResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BasePublishedAssessmentModder.running) {
				new Thread(new PublishedAssessmentReferenceFixer(SISContainerApp.getStaticVFS())).run();
				System.out.println("Started a new reference fixer!");
			} else
				System.out.println("A published assessment script is already running!");

			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
			sb.append(BasePublishedAssessmentModder.results.toString());
			sb.append("</body></html>");

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	public PublishedAssessmentReferenceFixer(File vfsRoot) {
		super(vfsRoot);
	}

	public PublishedAssessmentReferenceFixer(VFS vfs) {
		super(vfs);
	}

	protected boolean fixReferences(AssessmentData data) {
		boolean changed = false;

		if (data.getReferences().size() != 0) {
			for (Object cur : data.getReferences().entrySet()) {
				Entry curEntry = (Entry) cur;

				for (Object curRefObject : (ArrayList) curEntry.getValue()) {
					ReferenceUI curRef = (ReferenceUI) curRefObject;

					if (curRef.getReferenceType() == null || curRef.getReferenceType().equalsIgnoreCase("null")) {
						curRef.setReferenceType("rldb");

						if (curRef.containsKey("secondary_title") && !curRef.containsKey("citation")) {
							curRef.addField("citation", curRef.getField("secondary_title"));
							curRef.addField("citation_complete", "y");

							curRef.remove("secondary_title");
						}

						changed = true;
					} else if (curRef.getReferenceType().equalsIgnoreCase("rldb")) {
						if (curRef.containsKey("secondary_title") && !curRef.containsKey("citation")) {
							curRef.addField("citation", curRef.getField("secondary_title"));
							curRef.addField("citation_complete", "y");

							curRef.remove("secondary_title");
							changed = true;
						}
					} else if (curRef.getReferenceType().equalsIgnoreCase("redlist")) {
						if (curRef.containsKey("external_bib_code") && !curRef.containsKey("citation")) {
							curRef.addField("citation", curRef.getField("external_bib_code"));
							curRef.addField("citation_complete", "y");

							curRef.remove("external_bib_code");
							changed = true;
						}
					}
				}
			}
		}

		return changed;
	}

	@Override
	protected void workOnHistorical(AssessmentData data) {
		if (fixReferences(data))
			writeBackPublishedAssessment(data);
	}

	@Override
	protected void workOnMostRecent(AssessmentData data) {
		if (fixReferences(data))
			writeBackPublishedAssessment(data);
	}
}
