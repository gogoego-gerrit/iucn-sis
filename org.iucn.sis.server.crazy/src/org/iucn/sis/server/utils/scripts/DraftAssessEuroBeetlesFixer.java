package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.simple.SISContainerApp;
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

public class DraftAssessEuroBeetlesFixer extends BaseDraftAssessmentModder {

	public static class DraftAssessEuroBeetlesFixerResource extends Resource {

		public DraftAssessEuroBeetlesFixerResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BaseDraftAssessmentModder.running) {
				String wsID = (String) getRequest().getAttributes().get("wsID");
				String writeback = (String) getRequest().getAttributes().get("writeback");
				String wsURL = null;

				if (wsID != null) {
					wsURL = "/workingsets/" + wsID + ".xml";
					new Thread(new DraftAssessEuroBeetlesFixer(SISContainerApp.getStaticVFS(), wsURL,
							getContext().getClientDispatcher(), writeback)).run();
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
	private boolean doWriteback = false;
	private int wrote = 0;
	
	public DraftAssessEuroBeetlesFixer(File vfsRoot, String workingSetURL, Uniform uniform, boolean writeback) {
		super(vfsRoot, workingSetURL, true);
		this.uniform = uniform;
	}

	public DraftAssessEuroBeetlesFixer(VFS vfs, String workingSetURL, Uniform uniform, String writeback) {
		super(vfs, workingSetURL, true);
		this.uniform = uniform;
		if( writeback != null )
			this.doWriteback = Boolean.parseBoolean(writeback);
		
		System.out.println("doWriteback is " + this.doWriteback);
	}

	@Override
	public void run() {
		super.run();
		System.out.println("Modified " + wrote + " assessments.");
	}
	
	@Override
	protected void workOnAssessment(AssessmentData data, TaxonNode node) {

		if (data.isGlobal() || !data.getRegionIDs().contains("0"))
			return;
		
//		{
//			List<String> ut;
//			if (data.getDataMap().containsKey(CanonicalNames.RedListAssessmentDate))
//				ut = (ArrayList<String>) data.getDataMap().get(CanonicalNames.RedListAssessmentDate);
//			else {
//				ut = new ArrayList<String>();
//				ut.add("");
//			}
//			ut.set(0, "2009-06-05");
//		}

		
		{
			List<String> eval = (List<String>)data.getDataMap().get(CanonicalNames.RedListEvaluated);
			if( eval == null ) {
				eval = new ArrayList<String>();
				data.getDataMap().put(CanonicalNames.RedListEvaluated, eval);
			}
			eval.set(1, "2009-05-06");
			
//			eval.clear();
//			eval.add("true");
//			eval.add("");
//			eval.add("1");
//			eval.add("");
//			eval.add("");
		}
		
		{
			List<String> evaluators = (List<String>)data.getDataMap().get(CanonicalNames.RedListEvaluators);
			if( evaluators == null ) {
				evaluators = new ArrayList<String>();
				data.getDataMap().put(CanonicalNames.RedListEvaluators, evaluators);
			}
			
			evaluators.set(0, "");
			evaluators.set(1, "2");
			evaluators.add("186");
			evaluators.add("212");
			
//			if( evaluators.size() > 0 && !evaluators.get(0).equals("")) {
//				String extant = evaluators.get(0);
//				if( !extant.contains("Nieto") && !extant.contains("Alexander, K") )
//					evaluators.set(0, extant.trim() + ", Alexander, K.N.A & Nieto, A.");
//				else {
//					evaluators.set(0, "");
//					evaluators.set(1, "2");
//					evaluators.add("186");
//					evaluators.add("212");
//				}
//			} else {
//				int numEvals = Integer.valueOf(evaluators.get(1));
//				evaluators.set(1, String.valueOf((numEvals+2)));
//				evaluators.add("186");
//				evaluators.add("212");
//			}
//			
//			if( wrote % 25 == 0 )
//				System.out.println("Random evals sample for " + data.getAssessmentID() + 
//						" is now " + Arrays.toString(evaluators.toArray()));
		}
		
//		{
//			List<String> checked = (List<String>)data.getDataMap().get(CanonicalNames.RedListConsistencyCheck);
//			if( checked == null ) {
//				checked = new ArrayList<String>();
//				data.getDataMap().put(CanonicalNames.RedListConsistencyCheck, checked);
//			}
//			checked.clear();
//			checked.add("2");
//			checked.add("2009-11-26");
//			checked.add("1");
//			checked.add("");
//			checked.add("");
//		}

		if( doWriteback ) {
			if( !writeBackDraftAssessment(data) ) 
				System.out.println("ERROR WRITING BACK " + data.getAssessmentID());
			else
				System.out.println("Wrote back assessment " + data.getAssessmentID());
		} else
			System.out.println("modified assessment " + data.getAssessmentID());
		
		wrote++;
	}
}
