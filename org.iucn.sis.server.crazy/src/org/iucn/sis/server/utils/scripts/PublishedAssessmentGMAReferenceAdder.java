package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.crossport.demimport.ElementalReferenceRowProcessor;
import org.iucn.sis.server.ref.Reference;
import org.iucn.sis.server.ref.ReferenceApplication;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.QRelationConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.ElementCollection;
import com.solertium.util.SysDebugger;
import com.solertium.vfs.VFS;

public class PublishedAssessmentGMAReferenceAdder extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentGMAReferenceAdderResource extends Resource {

		public PublishedAssessmentGMAReferenceAdderResource() {
		}

		public PublishedAssessmentGMAReferenceAdderResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BasePublishedAssessmentModder.running) {
				new Thread(new PublishedAssessmentGMAReferenceAdder(SISContainerApp.getStaticVFS())).run();
				System.out.println("Started a new published data verifier!");
			} else
				System.out.println("A published assessment script is already running!");

			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
			sb.append(BasePublishedAssessmentModder.results.toString());
			sb.append("</body></html>");

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	private ExecutionContext ec;
	private int changedCount = 0;

	public PublishedAssessmentGMAReferenceAdder(File vfsRoot) {
		super(vfsRoot);
	}

	public PublishedAssessmentGMAReferenceAdder(VFS vfs) {
		super(vfs);
	}

	private boolean addReferences(AssessmentData curAssessment) throws Exception {
		boolean changed = false;

		SelectQuery select = new SelectQuery();
		select = new SelectQuery();
		select.select("bibliographic_original_records", "*");
		select.join("bibliography_link", new QRelationConstraint(new CanonicalColumnName("bibliography_link",
				"Bibliography_number"), new CanonicalColumnName("bibliographic_original_records", "Bib_Code")));
		select.constrain(new CanonicalColumnName("bibliography_link", "Sp_code"), QConstraint.CT_EQUALS, curAssessment.getSpeciesID());
		try {
			// build a single document containing all the references for this
			// assessment
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element el = doc.createElement("references");
			doc.appendChild(el);
			ElementalReferenceRowProcessor errp = new ElementalReferenceRowProcessor(doc, el);
			ec.doQuery(select, errp);
			for (Element refEl : new ElementCollection(el.getElementsByTagName("reference"))) {
				StringWriter refStringWriter = new StringWriter();
				Transformer t = TransformerFactory.newInstance().newTransformer();
				t.setOutputProperty("omit-xml-declaration", "yes");
				t.transform(new DOMSource(refEl), new StreamResult(refStringWriter));

				String result = refStringWriter.toString();
				// result =
				// result.replaceAll(
				// "<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>",
				// "");
				result = result.substring(result.indexOf('>', 0) + 1, result.indexOf("</reference>"));
				// result = result.replaceAll("</reference>", "");

				SysDebugger.getInstance().println("Insert reference based on: [" + result + "]");

				Reference ref = new Reference(refEl);
				curAssessment.addReference(new ReferenceUI(ref.getMap(), ref.getId(), ref.getType()), "Global");
				changed = true;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		return changed;
	}

	@Override
	protected void parseNode(VFS vfs, Document node) {
		String id = node.getDocumentElement().getAttribute("id");
		
		SelectQuery select = new SelectQuery();
		select.select("bibliography_link", "Sp_code");
		select.constrain(new CanonicalColumnName("bibliography_link", "Sp_code"), QConstraint.CT_EQUALS, id);

		Row.Set rowLoader = new Row.Set();
		try {
			ec.doQuery(select, rowLoader);
		} catch (DBException e) {
			e.printStackTrace();
			return;
		}
		
		if( rowLoader.getSet().size() > 0 )
			super.parseNode(vfs, node);
		else
			return;
	}
	
	private void registerDatasource(String sessionName, String URL, String driver, String username, String pass)
			throws Exception {
		DBSessionFactory.registerDataSource(sessionName, URL, driver, username, pass);
	}

	@Override
	public void run() {
		try {
			registerDatasource("gmaRefs", "jdbc:access:////usr/data/GMA/gmaRefs.mdb",
					"com.hxtt.sql.access.AccessDriver", "", "");
			switchToDBSession("gmaRefs");

			try {
				Document structDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
						ReferenceApplication.class.getResourceAsStream("refstruct.xml"));
				new SystemExecutionContext("gmaRefs").setStructure(structDoc);
			} catch (Exception ugly) {
				ugly.printStackTrace();
			}
			
			super.run();
		} catch (Exception e) {
			e.printStackTrace();
			results.append("Failure to connect to ref database.");
		}
	}

	private void switchToDBSession(String sessionName) throws Exception {
		ec = new SystemExecutionContext(sessionName);
		ec.setExecutionLevel(ExecutionContext.READ_WRITE);
	}

	@Override
	protected void workOnHistorical(AssessmentData data) {
		//Nothing to do.
	}

	@Override
	protected void workOnMostRecent(AssessmentData data) {
		
		if( data.isRegional() && !data.isEndemic() ) {
			System.out.println("Regional non-endemic reported as most recent for taxon " + data.getSpeciesID());
			return;
		}
		
		try {
			boolean changed = addReferences(data);

			if (changed) {
				writeBackPublishedAssessment(data);
				changedCount++;
				if( changedCount % 500 == 0 )
					System.out.println("Changed " + changedCount + " assessments.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error trying to add refs to assessment " + data.getAssessmentID());
		}
	}
}
