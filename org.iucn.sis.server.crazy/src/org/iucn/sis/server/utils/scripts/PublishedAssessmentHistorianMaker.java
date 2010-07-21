package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.iucn.sis.server.simple.SISContainerApp;
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

import com.solertium.vfs.VFS;

public class PublishedAssessmentHistorianMaker extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentHistorianResource extends Resource {

		public PublishedAssessmentHistorianResource() {
		}

		public PublishedAssessmentHistorianResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BasePublishedAssessmentModder.running) {
				new Thread(new PublishedAssessmentHistorianMaker(SISContainerApp.getStaticVFS())).run();
				System.out.println("Started a new historian!");
			} else
				System.out.println("A published assessment script is already running!");

			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
			sb.append(BasePublishedAssessmentModder.results.toString());
			sb.append("</body></html>");

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	public PublishedAssessmentHistorianMaker(File vfsRoot) {
		super(vfsRoot);
	}

	public PublishedAssessmentHistorianMaker(VFS vfs) {
		super(vfs);
	}

	private void addDataIfNotNull(HashMap source, HashMap dest, String canonicalName) {
		if (source.containsKey(canonicalName))
			dest.put(canonicalName, source.get(canonicalName));
	}

	private boolean removeData(AssessmentData data) {
		HashMap source = data.getDataMap();
		HashMap dest = new HashMap();

		ArrayList rlPub = new ArrayList();
		rlPub.add("");
		ArrayList otherPub = new ArrayList();
		otherPub.add("");

		dest.put(CanonicalNames.RedListPublication, rlPub);
		dest.put(CanonicalNames.OtherPublication, otherPub); // Have to
		// safeguard
		// these

		addDataIfNotNull(source, dest, CanonicalNames.RedListCriteria);
		addDataIfNotNull(source, dest, CanonicalNames.RedListReasonsForChange);
		addDataIfNotNull(source, dest, CanonicalNames.RedListAssessmentDate);
		addDataIfNotNull(source, dest, CanonicalNames.RedListAssessors);
		addDataIfNotNull(source, dest, CanonicalNames.RedListEvaluators);
		addDataIfNotNull(source, dest, CanonicalNames.RedListNotes);
		addDataIfNotNull(source, dest, CanonicalNames.RedListPetition);
		addDataIfNotNull(source, dest, CanonicalNames.RedListCaveat);
		addDataIfNotNull(source, dest, CanonicalNames.RedListRationale);

		addDataIfNotNull(source, dest, CanonicalNames.TaxonomicNotes);
		addDataIfNotNull(source, dest, CanonicalNames.PossiblyExtinct);
		addDataIfNotNull(source, dest, CanonicalNames.PossiblyExtinctCandidate);

		data.setData(dest);

		if (source.size() != dest.size())
			return true;
		else
			return false;
	}

	private boolean removeReferences(AssessmentData data) {
		ArrayList<String> refsToRemove = new ArrayList<String>();

		for (Object fieldName : data.getReferences().keySet()) {
			if (!fieldName.toString().equalsIgnoreCase(CanonicalNames.RedListPublication)
					&& !fieldName.toString().equalsIgnoreCase(CanonicalNames.OtherPublication))
				refsToRemove.add(fieldName.toString());
		}

		for (String fieldName : refsToRemove)
			data.getReferences().remove(fieldName);

		if (refsToRemove.size() > 0)
			return true;
		else
			return false;
	}

	protected void setHistoric(final AssessmentData data) {
		boolean changed = false;

		if (!data.isHistorical()) {
			data.setHistorical(true);
			changed = true;
		}

		if (removeReferences(data))
			changed = true;

		if (removeData(data))
			changed = true;

		if (changed)
			writeBackPublishedAssessment(data);
	}

	@Override
	protected void workOnHistorical(AssessmentData data) {
		results.append("Modified " + data.getDateFinalized() + "<br/>");
		setHistoric(data);
	}

	@Override
	protected void workOnMostRecent(AssessmentData data) {
		results.append("Latest assessment date is: " + data.getDateFinalized());
		results.append("<br/>-------------------------------" + "<br/>");
	}
}
