package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.ref.ReferenceApplication;
import org.iucn.sis.server.ref.ReferenceLabels;
import org.iucn.sis.server.ref.ReferenceRowProcessor;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;
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
import org.w3c.dom.NodeList;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.VFS;

public class PublishedAssessmentOct2009FixerCOOAndCR extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentOct2009FixerCOOAndCRResource extends Resource {

		public PublishedAssessmentOct2009FixerCOOAndCRResource() {
		}

		public PublishedAssessmentOct2009FixerCOOAndCRResource(final Context context, final Request request,
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
					new Thread(new PublishedAssessmentOct2009FixerCOOAndCR(SISContainerApp.getStaticVFS(), writeback)).run();
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

	private int missingPresenceCode = 0;
	private int presenceWrong = 0;
	private int possiblyExtinctWrong = 0;
	private int birdRefAdded = 0;
	private ReferenceUI ref = null;
	
	private boolean isBird = false;
	private boolean writeback = false;

	public PublishedAssessmentOct2009FixerCOOAndCR(File vfsRoot, boolean writeback) throws Exception {
		super(vfsRoot);
		this.writeback = writeback;
	}

	public PublishedAssessmentOct2009FixerCOOAndCR(VFS vfs, boolean writeback) throws Exception {
		super(vfs);
		this.writeback = writeback;
	}

	private boolean addBirdRef(AssessmentData data) {
		if( ref != null  && isBird ) {
			data.addReference(ref, "Global");
			birdRefAdded++;
			return true;
		} else
			return false;
	}
	
	private boolean fixPossiblyExtinct(AssessmentData data) {
		if( !data.getProperCategoryAbbreviation().equals("CR") && data.isPossiblyExtinct() ) {
			possiblyExtinctWrong++;
			data.setDataPiece(7, CanonicalNames.RedListCriteria, "false");
			return true;
		} else
			return false;
	}
	
	private boolean fixPresenceCode(AssessmentData data) {
		boolean changed = false;
		HashMap<String, ArrayList<String>> coo = (HashMap<String, ArrayList<String>>)data.getDataMap().get(CanonicalNames.CountryOccurrence);
		if( coo == null )
			return false;
		
		for( Entry<String, ArrayList<String>> cur : coo.entrySet() ) {
			String presence = cur.getValue().get(0);
			String origin = cur.getValue().get(2);
			if( presence.equals("0") || presence.equals("") ) {
				missingPresenceCode++;
				cur.getValue().set(0, "1");
				changed = true;
			}
			if( origin.equals("0") || origin.equals("") ) {
				missingPresenceCode++;
				cur.getValue().set(2, "1");
				changed = true;
			}
		}

		if( data.getProperCategoryAbbreviation().equals("CR") && data.isPossiblyExtinct() ) {
			for( Entry<String, ArrayList<String>> cur : coo.entrySet() ) {
				String presence = cur.getValue().get(0);
				String origin = cur.getValue().get(2);
				if( presence.equals("1") && origin.equals("1") ) {
					presenceWrong++;
					cur.getValue().set(0, "2");
					changed = true;
				} else if( presence.equals("4") && origin.equals("1") ) {
					presenceWrong++;
					cur.getValue().set(0, "2");
					changed = true;
				}
			}
		}
		
		return changed;
	}
	
	private ReferenceUI getReferenceByField(String fieldName, String title) {
		try {
			final ExecutionContext ec = new SystemExecutionContext(ReferenceApplication.DBNAME);
			final SelectQuery sq = new SelectQuery();
			sq.select("bibliography", "*");
			sq.constrain(new CanonicalColumnName("bibliography", fieldName), QConstraint.CT_EQUALS, title);
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
		} catch (final NullPointerException np) {
			np.printStackTrace();
			return null;
		}
	}
	
	@Override
	public void run() {
		ref = getReferenceByField("Author", "Bird Reference Citations");

		if( ref != null ) {
			super.run();

			String results = "";
			results += "Added presence/origin code to " + missingPresenceCode + " assessments.\n";
			results += "Changed presence for CR/possibly extinct species on " + presenceWrong + " assessments.\n";
			results += "Changed possibly extinct on " + possiblyExtinctWrong + " non-CR assessments.\n";
			results += "Added Bird Citation Reference to " + birdRefAdded + " assessments.\n";

			System.out.println(results);
		} else 
			System.out.println("COULD NOT FIND Bird Reference Citations REFERENCE!");
	}
	
	protected void parseNode(VFS vfs, Document node) {
		NodeList fpList = node.getDocumentElement().getElementsByTagName("footprint");
		if( fpList != null && fpList.getLength() > 0 ) {
			String fp = fpList.item(0).getTextContent();
			isBird = fp.contains(",AVES,");
		} else
			isBird = false;
		
		if( count % 10000 == 0 ) {
			System.out.println("Added presence/origin code to " + missingPresenceCode + " assessments.");
			System.out.println("Changed presence for CR/possibly extinct species on " + presenceWrong + " assessments.");
			System.out.println("Changed possibly extinct on " + possiblyExtinctWrong + " non-CR assessments.");
			System.out.println("Added Bird Citation Reference to " + birdRefAdded + " assessments.");
		}
		
		super.parseNode(vfs, node);
	}
	
	@Override
	protected void workOnFullList(List<AssessmentData> assessments) {
		for( AssessmentData data : assessments ) {
			boolean changed = false;
			
			if( fixPossiblyExtinct(data))
				changed = true;
			if( fixPresenceCode(data) )
				changed = true;
			if( addBirdRef(data))
				changed = true;

			if( writeback )
				if( changed )
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
