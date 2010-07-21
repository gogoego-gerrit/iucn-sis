package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.ref.ReferenceApplication;
import org.iucn.sis.server.ref.ReferenceLabels;
import org.iucn.sis.server.ref.ReferenceRowProcessor;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.MostRecentFlagger;
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

public class PublishedAssessmentOct2009Fixer extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentOct2009FixerResource extends Resource {

		public PublishedAssessmentOct2009FixerResource() {
		}

		public PublishedAssessmentOct2009FixerResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			boolean writeback = false;
			String wb = (String)getRequest().getAttributes().get("writeback");
			if( wb != null && wb.equalsIgnoreCase("true") )
				writeback = true;
			
			System.out.println("Writeback is " + writeback);
			try {
				if (!BasePublishedAssessmentModder.running) {
					new Thread(new PublishedAssessmentOct2009Fixer(SISContainerApp.getStaticVFS(), writeback)).run();
					System.out.println("Started a new historian!");
				} else
					System.out.println("A published assessment script is already running!");

				StringBuilder sb = new StringBuilder();
				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append(BasePublishedAssessmentModder.results.toString());
				sb.append("</body></html>");

				return new StringRepresentation(sb, MediaType.TEXT_HTML);
			} catch (Exception e) {
				
				StringBuilder sb = new StringBuilder();
				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append(DocumentUtils.getStackTraceAsString(e));
				sb.append("</body></html>");
				return new StringRepresentation(sb.toString(), MediaType.TEXT_HTML);
			}
		}
	}

	private int addedCaveat = 0;
	private int nullRegionFixed = 0;
	private int globalAndEndemicFixed = 0;
	private int addedRedListPub = 0;
	private boolean writeback = false;

	private ReferenceUI pubRef;
	private Date jan1;
	
	public PublishedAssessmentOct2009Fixer(File vfsRoot, boolean writeback) throws Exception {
		super(vfsRoot);
		this.writeback = writeback;
		
		jan1 = new SimpleDateFormat("yyyy-MM-dd").parse("1999-01-01");
	}

	public PublishedAssessmentOct2009Fixer(VFS vfs, boolean writeback) throws Exception {
		super(vfs);
		this.writeback = writeback;
		
		jan1 = new SimpleDateFormat("yyyy-MM-dd").parse("1999-01-01");
	}

	@Override
	public void run() {
//		pubRef = getReference("2AF8F61E1B165E9F916E1A27D5788688");
		pubRef = getReference("A14284EE6B7A37F9D7546C83B3C68E16");
		if( pubRef == null ) {
			System.out.println("Could not get the 2008 Red List Publication reference. Bailing.");
			return;
		}
		
		super.run();
		
		String results = "";
		results += "Added caveat to " + addedCaveat + " assessments.\n";
		results += "IsRegional was unset on " + nullRegionFixed + " assessments.\n";
		results += "Ticked endemic on " + globalAndEndemicFixed + " global assessments.\n";
		results += "Added 2008 RedList Publication reference to " + addedRedListPub + " assessments.";
		
		System.out.println(results);
		
		DocumentUtils.writeVFSFile("/oct2009ModScriptresults.txt", vfs, results);
	}
	
	private boolean addCaveat(AssessmentData data) {
		if( data.isHistorical() )
			return false;
		
		String assessedDate = data.getDateAssessed();
		
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(assessedDate.substring(0, 10));

			if( date.before(jan1)) {
				List<String> caveat = new ArrayList<String>();
				caveat.add("true");	
				data.getDataMap().put(CanonicalNames.RedListCaveat, caveat);
				addedCaveat++;
				return true;
			}
		} catch (ParseException e) {
			try {
				Date date = new SimpleDateFormat("yyyy/MM/dd").parse(assessedDate.substring(0, 10));

				if( date.before(jan1)) {
					List<String> caveat = new ArrayList<String>();
					caveat.add("true");	
					data.getDataMap().put(CanonicalNames.RedListCaveat, caveat);
					addedCaveat++;
					return true;
				}
			} catch (ParseException e1) {
				System.out.println("COULD NOT PARSE ASSESSMENT DATE " + assessedDate + " for assessment " + data.getAssessmentID());
				return false;
			}
		}
		
		return false;
		
	}
	private boolean fixRegional(AssessmentData data) {
		boolean ret = false;
		
		String isRegional = data.getFirstDataPiece(CanonicalNames.RegionInformation, null);
		if( isRegional == null || isRegional.equals("") || data.getRegionIDs() == null ) {
			List<String> regionData = new ArrayList<String>();
			regionData.add("false");
			regionData.add("");
			regionData.add("true");
			
			data.getDataMap().put(CanonicalNames.RegionInformation, regionData);
			nullRegionFixed++;
			ret = true;
		} 
		
		if( data.isGlobal() && !data.isEndemic() ) {
			data.setEndemic(true);
			globalAndEndemicFixed++;
			ret = true;
		}
		
		return ret;
	}
	private boolean addRedListPublication(AssessmentData data) {
		String assessedDate = data.getDateAssessed();
		if( assessedDate.contains("2008") ) {
			if( data.getReferences(CanonicalNames.RedListPublication).size() == 0 ) {
				data.addReference(pubRef, CanonicalNames.RedListPublication);
				
				addedRedListPub++;
				return true;
			}
		}
		
		return false;
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
	
	@Override
	protected void workOnFullList(List<AssessmentData> assessments) {
		
		List<AssessmentData> updatedHistorical = MostRecentFlagger.flagMostRecentInList(assessments);
		
		if( updatedHistorical.size() > 0 ) {
			System.out.println("Changed " + updatedHistorical.size() + " historical flags for " +
					"taxon " + updatedHistorical.get(0).getSpeciesID());
		}
		
		for( AssessmentData data : assessments ) {
			boolean changed = false;
			
			if( fixRegional(data) )
				changed = true;
			if( addCaveat(data) )
				changed = true;
			if( addRedListPublication(data) )
				changed = true;

			if( writeback )
				if( changed || updatedHistorical.contains(data) )
					writeBackPublishedAssessment(data);
		}
	}
	
	@Override
	protected void workOnHistorical(AssessmentData data) {
		//Nothing to do.
	}

	@Override
	protected void workOnMostRecent(AssessmentData data) {
		//Nothing to do.
	}
}
