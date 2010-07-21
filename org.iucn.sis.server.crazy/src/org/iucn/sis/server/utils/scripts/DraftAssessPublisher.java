package org.iucn.sis.server.utils.scripts;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.ref.ReferenceApplication;
import org.iucn.sis.server.ref.ReferenceLabels;
import org.iucn.sis.server.ref.ReferenceRowProcessor;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.AssessmentPublisher;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
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

public class DraftAssessPublisher extends BaseDraftAssessmentModder {

	public static class DraftAssessPublisherResource extends Resource {

		public DraftAssessPublisherResource() {
		}

		public DraftAssessPublisherResource(final Context context, final Request request, final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BaseDraftAssessmentModder.running) {
				String wsID = (String) getRequest().getAttributes().get("wsID");
				String regionToPublish = (String) getRequest().getAttributes().get("region");
				String refID = (String) getRequest().getAttributes().get("refID");
				String wsURL = null;

				if (wsID != null) {
					wsURL = "/workingsets/" + wsID + ".xml";

					try {
						new Thread(new DraftAssessPublisher(SISContainerApp.getStaticVFS(), wsURL, getContext(), regionToPublish, refID)).run();
					} catch (Exception e) {
						return new StringRepresentation(e.getMessage(), MediaType.TEXT_ALL);
					}
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

	private Context context;

	private int published = 0;
	private int wsUsed = 0;
	
	private String uriPrefix = "riap://application";
//	private HashMap<String, String> philippineIDs = new HashMap<String, String>();
	private final String regionToPublish;
	private final String publicationReference;
	private final ReferenceUI pubRef;
	
	private final AssessmentPublisher publisher;

	public DraftAssessPublisher(VFS vfs, String workingSetURL, Context context, String regionToPublish, String publicationReference)
			throws NamingException {
		super(vfs, workingSetURL, false);
		this.context = context;
		this.regionToPublish = regionToPublish;
		this.publicationReference = publicationReference;
		this.pubRef = getReference(publicationReference);
		this.publisher = new AssessmentPublisher();
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
		} catch (final NullPointerException npe) {
			npe.printStackTrace();
			return null;
		}
	}
	
	
	@Override
	public void run() {
		published = 0;
		
		if( pubRef != null ) {
			super.run();
		} else
			System.out.println("Reference " + this.publicationReference + " could not be found");
		
		System.out.println("Published " + published + " assessments.");
	}

	@Override
	protected void workOnAssessment(AssessmentData data, TaxonNode node) {
		if( (regionToPublish.equals("global") && data.isRegional()) ||
			(!regionToPublish.equals("global") && data.isGlobal()) || 
				(data.isRegional() && !data.getRegionIDsCSV().equals(regionToPublish)) )
			return;
		
		if( publisher.publishAssessment(data, pubRef)) {
			if( writeback ) {
//				System.out.println("***GOING to publish assessment for species " + data.getSpeciesID());

				// It's ready to be published!!!
				data.setAssessmentID("new");
				Request request = new Request(Method.PUT, uriPrefix + "/assessments", new StringRepresentation(data.toXML(), MediaType.TEXT_XML, null,
								CharacterSet.UTF_8));
				Response response = context.getClientDispatcher().handle(request);
			
				if (response.getStatus().isSuccess()) {
					try {
						published++;
						vfs.delete(VFSUtils.parseVFSPath(ServerPaths.getDraftAssessmentURL(data
									.getAssessmentID())));
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("*** UNABLE TO REMOVE DRAFT ASSESSMENT " + data.getAssessmentID());
					}
				} else
					System.out.println("Failure to publish draft assessment " + data.getAssessmentID() + 
							" with status " + response.getStatus().toString());
			} else 
				System.out.println("Would have published species " + data.getSpeciesID() + 
						(data.isGlobal() ? " global draft." : " regional draft for " + data.getRegionIDsCSV()) + "." );
		}
	}

}
