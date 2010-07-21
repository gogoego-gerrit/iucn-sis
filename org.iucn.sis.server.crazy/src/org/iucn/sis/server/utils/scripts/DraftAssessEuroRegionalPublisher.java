package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.ref.ReferenceApplication;
import org.iucn.sis.server.ref.ReferenceLabels;
import org.iucn.sis.server.ref.ReferenceRowProcessor;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.FormattedDate;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.VFS;
import com.solertium.vfs.utils.VFSUtils;

public class DraftAssessEuroRegionalPublisher extends BaseDraftAssessmentModder {

	public static class DraftAssessEuroRegionalPublisherResource extends Resource {

		public DraftAssessEuroRegionalPublisherResource() {
		}

		public DraftAssessEuroRegionalPublisherResource(final Context context, final Request request, final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BaseDraftAssessmentModder.running) {
				String wsID = (String) getRequest().getAttributes().get("wsID");
				String wsURL = null;

				if (wsID != null) {
					if( wsID.equals("86") || wsID.equals("156") || wsID.equals("157") || 
							wsID.equals("158") || wsID.equals("159") || wsID.equals("170") ) {
						wsURL = "/workingsets/" + wsID + ".xml";
						new Thread(new DraftAssessEuroRegionalPublisher(SISContainerApp.getStaticVFS(), wsURL, getContext())).run();
						System.out.println("Started a new Euro regional draft publisher!!");
					} else
						System.out.println("Invalid WorkingSetID supplied.");
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
			sb.append("Draft script is running...");
			sb.append("</body></html>");

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	private Context context;

	private String uriPrefix = "riap://component";

	public DraftAssessEuroRegionalPublisher(File vfsRoot, String workingSetURL, Context context) {
		super(vfsRoot, workingSetURL, false);
		this.context = context;
	}

	public DraftAssessEuroRegionalPublisher(VFS vfs, String workingSetURL, Context context) {
		super(vfs, workingSetURL, false);
		this.context = context;
	}

	@Override
	protected void workOnAssessment(AssessmentData data, TaxonNode node) {
		if (data.getType().equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS)) {
			data.setType(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS);
			data.setDateFinalized(FormattedDate.impl.getDate());

			boolean doit = false;
			
			if( workingSetURL.contains("156.xml")) {
				doit = euroMammalAssessment(data);
			} else if( workingSetURL.contains("157.xml")) {
				doit = euroAmphibianAssessment(data);
			} else if( workingSetURL.contains("158.xml")) {
				doit = euroReptileRegionalAssessment(data);
			} else if( workingSetURL.contains("159.xml")) {
				doit = euroReptileGlobalAssessment(data);
			} else if( workingSetURL.contains("86.xml")) {
				doit = euroTortoises(data);
			} else if( workingSetURL.contains("170.xml")) {
				doit = caucasus(data);
			}
			
			if( doit ) {

				System.out.println("***GOING to publish assessment " + data.getAssessmentID());

				// It's ready to be published!!!
				data.setAssessmentID("new");
				Response response = context.getServerDispatcher().put(uriPrefix + "/assessments", new StringRepresentation(data.toXML(), MediaType.TEXT_XML, null,
								CharacterSet.UTF_8));

				if (response.getStatus().isSuccess()) {
					try {
						vfs.delete(VFSUtils.parseVFSPath(ServerPaths.getDraftAssessmentURL(data.getAssessmentID())));
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("*** UNABLE TO REMOVE DRAFT ASSESSMENT " + data.getAssessmentID());
					}
				} else
					System.out.println("Failure to publish draft assessment " + data.getAssessmentID());
			}
		}
	}
	
	protected boolean caucasus(AssessmentData data) {
		if( !data.isGlobal() )
			return false;
		
		putAuthorsIfNotNull(data);
		
		if( !data.getDataMap().containsKey(CanonicalNames.RedListPublication) )
			data.getDataMap().put(CanonicalNames.RedListPublication, new ArrayList<String>());
		data.addReference(getReference("A5994895F9A01570FAB5E021A858EE2C"), CanonicalNames.RedListPublication);
		
		return true;
	}
	
	protected boolean euroTortoises(AssessmentData data) {
		return data.isRegional();
	}
	
	protected boolean euroReptileRegionalAssessment(AssessmentData data) {
		if( !data.isRegional() )
			return false;
		
		String evals = "Cox, N. and Temple, H.J. (Global Reptile Assessment)";
		ArrayList<String> arr = new ArrayList<String>();
		arr.add(evals);
		data.getDataMap().put(CanonicalNames.RedListEvaluators, arr);
		
		putAuthorsIfNotNull(data);
		
		if( data.getDateAssessed() == null || data.getDateAssessed().equals("") )
			data.setDateAssessed("2008-12-14");
		
		if( !data.getDataMap().containsKey(CanonicalNames.RedListPublication) )
			data.getDataMap().put(CanonicalNames.RedListPublication, new ArrayList<String>());
		data.addReference(getReference("BC3104B0864151C4A05AB766BB5CDD29"), CanonicalNames.RedListPublication);
		
		return true;
	}
	
	protected boolean euroReptileGlobalAssessment(AssessmentData data) {
		if( !data.isGlobal() )
			return false;
		
		String evals = "Cox, N. and Temple, H.J. (Global Reptile Assessment)";
		ArrayList<String> arr = new ArrayList<String>();
		arr.add(evals);
		data.getDataMap().put(CanonicalNames.RedListEvaluators, arr);
		
		putAuthorsIfNotNull(data);
		
		if( data.getDateAssessed() == null || data.getDateAssessed().equals("") )
			data.setDateAssessed("2008-12-14");
		
		arr = new ArrayList<String>();
		arr.add("true");
		arr.add("2009-04-01");
		arr.add("1");
		data.getDataMap().put(CanonicalNames.RedListEvaluated, arr);
		
		arr = new ArrayList<String>();
		arr.add("2009-04-01");
		data.getDataMap().put(CanonicalNames.RedListEvaluationDate, arr);

		if( !data.getDataMap().containsKey(CanonicalNames.RedListPublication) )
			data.getDataMap().put(CanonicalNames.RedListPublication, new ArrayList<String>());
		data.addReference(getReference("A5994895F9A01570FAB5E021A858EE2C"), CanonicalNames.RedListPublication);
		
		return true;
	}
	
	protected boolean euroMammalAssessment(AssessmentData data) {
		if( !data.isRegional() )
			return false;
		
		putAuthorsIfNotNull(data);
		
		if( !data.getDataMap().containsKey(CanonicalNames.RedListPublication) )
			data.getDataMap().put(CanonicalNames.RedListPublication, new ArrayList<String>());
		data.addReference(getReference("A72F9506600996E2670CCE6B3FE7A254"), CanonicalNames.RedListPublication);
		
		return true;
	}
	
	protected boolean euroAmphibianAssessment(AssessmentData data) {
		String evals = "Cox, N. and Temple, H.J. (Global Amphibian Assessment)";
		ArrayList<String> arr = new ArrayList<String>();
		arr.add(evals);
		data.getDataMap().put(CanonicalNames.RedListEvaluators, arr);
		
		putAuthorsIfNotNull(data);
		
		if( data.isGlobal() ) {
			arr = new ArrayList<String>();
			arr.add("true");
			arr.add("2009-01-01");
			arr.add("1");
			data.getDataMap().put(CanonicalNames.RedListEvaluated, arr);
			
			arr = new ArrayList<String>();
			arr.add("2009-01-01");
			data.getDataMap().put(CanonicalNames.RedListEvaluationDate, arr);
			
			if( !data.getDataMap().containsKey(CanonicalNames.RedListPublication) )
				data.getDataMap().put(CanonicalNames.RedListPublication, new ArrayList<String>());		
			data.addReference(getReference("A5994895F9A01570FAB5E021A858EE2C"), CanonicalNames.RedListPublication);
		} else {
			if( !data.getDataMap().containsKey(CanonicalNames.RedListPublication) )
				data.getDataMap().put(CanonicalNames.RedListPublication, new ArrayList<String>());
			data.addReference(getReference("BC3104B0864151C4A05AB766BB5CDD29"), CanonicalNames.RedListPublication);
		}
		
		return true;
	}

	private void putAuthorsIfNotNull(AssessmentData data) {
		ArrayList<String> arr;
		String curAuthors = data.getFirstDataPiece(CanonicalNames.RedListAssessmentAuthors, "");
		if( curAuthors.equals("") ) {
			arr = new ArrayList<String>();
			arr.add(data.getFirstDataPiece(CanonicalNames.RedListAssessors, ""));
			data.getDataMap().put(CanonicalNames.RedListAssessmentAuthors, arr);
		}
	}
	
	
	private ReferenceUI getReference(String refID) {
		try {
			final ExecutionContext ec = new SystemExecutionContext(ReferenceApplication.DBNAME);
			final SelectQuery sq = new SelectQuery();
			sq.select("bibliography", "*");
			sq.constrain(new CanonicalColumnName("bibliography", "Bib_hash"), QConstraint.CT_EQUALS, refID);
			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			final Element rootEl = doc.createElement("references");
			doc.appendChild(rootEl);

			ec.doQuery(sq, new ReferenceRowProcessor(doc, rootEl, ReferenceLabels.getInstance()));
			String refXML = DocumentUtils.serializeNodeToString(doc.getDocumentElement());
			NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
			ndoc.parse(refXML);
			ReferenceUI ref = new ReferenceUI(ndoc.getDocumentElement().getElementByTagName("reference"));
			return ref;
		} catch (final DBException dbx) {
			dbx.printStackTrace();
			return null;
		} catch (final NamingException nx) {
			nx.printStackTrace();
			return null;
		} catch (final ParserConfigurationException px) {
			px.printStackTrace();
			return null;
		}
	}
}
