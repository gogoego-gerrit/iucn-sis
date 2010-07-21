package org.iucn.sis.server.extensions.references;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.mapping.Column;
import org.iucn.sis.server.ServerApplication;
import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.server.api.utils.ServerPaths;
import org.iucn.sis.server.ref.ReferenceLabels.LabelMappings;
import org.iucn.sis.server.taxa.TaxonomyDocUtils;
import org.iucn.sis.shared.api.models.Taxon;
import org.restlet.Uniform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.StringLiteral;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.ClasspathResources;
import com.solertium.util.ElementCollection;
import com.solertium.util.NodeCollection;
import com.solertium.util.TagFilter;
import com.solertium.util.TagFilter.Tag;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;
import com.sun.rowset.internal.Row;

public class ReferenceDBBuilder implements Runnable {

	private class TrivialAssessment {

		private String asm_type;

		/**
		 * For draft assessments...
		 * 
		 * @param taxId
		 */
		public TrivialAssessment(final int taxId) throws NotFoundException {
			try {
				VFSPath draftRoot = VFSUtils.parseVFSPath(ServerPaths.getDraftAssessmentRootURL("" + taxId));
				for (VFSPathToken draftURL : ServerApplication.getStaticVFS().list(draftRoot)) {
					final Document doc = DocumentUtils.getVFSFileAsDocument(draftRoot.child(draftURL).toString(),
							ServerApplication.getStaticVFS());
					String id = doc.getElementsByTagName("assessmentID").item(0).getTextContent();
					asm_type = BaseAssessment.DRAFT_ASSESSMENT_STATUS;
					process(id, taxId, doc);
				}
			} catch (final DBException ix) {
				System.err.println("DB exception parsing assessment " + taxId);
			} catch (final VFSPathParseException e) {
				e.printStackTrace();
				// Whatever
			}
		}

		/**
		 * For published assessments...
		 * 
		 * @param id
		 * @param taxId
		 */
		public TrivialAssessment(final String id, final int taxId) {
			try {
				final String fn = "/browse/assessments/" + FilenameStriper.getIDAsStripedPath(id) + ".xml";
				final Document d = DocumentUtils.getVFSFileAsDocument(fn, ServerApplication.getStaticVFS());
				if (d == null) {
					System.err.println("Could not open assessment " + fn);
				} else {
					asm_type = BaseAssessment.PUBLISHED_ASSESSMENT_STATUS;
					process(id, taxId, d);
				}
			} catch (final DBException ix) {
				System.err.println("DB exception parsing assessment " + id);
			}
		}

		private void addBibliographyEntry(final Element reference) {
			try {
				final Document responseDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				final Element rootEl = responseDoc.createElement("references");
				responseDoc.appendChild(rootEl);

				final String id = reference.getAttribute("id");
				final Row r = ec.getRow("bibliography");
				String type = reference.getAttribute("type");
				LabelMappings lm = ReferenceLabels.getInstance().get(type);
				r.get("Publication_Type").setObject(type);

				final NodeList fields = reference.getElementsByTagName("field");
				for (int j = 0; j < fields.getLength(); j++) {
					final Element field = (Element) fields.item(j);
					String name = field.getAttribute("name");
					String normalized = LabelMappings.normalize(name);
					if (normalized != null)
						name = normalized;

					final String value = field.getTextContent();

					Column c = r.get(name);

					if (c != null) {
						if (value == null || value.equals(""))
							c.setObject(null);
						else
							c.setString(value);
					}
				}

				String newid = r.getMD5Hash();
				r.get("Bib_hash").setObject(newid);

				InsertQuery query = new InsertQuery("bibliography", r);
				try {
					ec.doUpdate(query);
				} catch (DBException dbx) {
					// dbx.printStackTrace();
					System.out.println("Reference " + newid + " already exists.");

					// TODO: COLLISION DETECTED -- NEED TO DO AN UPDATE FOR
					// TRANSIENT FIELDS, BUT SHOULD DETECT TO ENSURE IT'S
					// BODILY
					// THE SAME REFERENCE, AND NOT AN ACTUAL HASH COLLISION
				}
				// re-query, and glue the canonical representation of this ref
				// into the response document.
				SelectQuery sq = new SelectQuery();
				sq.select("bibliography", "*");
				sq.constrain(new CanonicalColumnName("bibliography", "Bib_hash"), QConstraint.CT_EQUALS, newid);
				ec.doQuery(sq, new ReferenceRowProcessor(responseDoc, rootEl, ReferenceLabels.getInstance()));
			} catch (final DBException dbx) {
				dbx.printStackTrace();
			} catch (final ParserConfigurationException pcx) {
				pcx.printStackTrace();
			}
		}

		private void addReference(final String id, final String fieldName, final Element reference) throws DBException {
			if (fieldName == null) {
				System.out.println("Refusing to add null id and/or field name.");
				return;
			}

			final String referenceHash = reference.getAttribute("id");
			Integer numericReferenceId = seenReferences.get(referenceHash);
			if (numericReferenceId == null) { // not already seen
				numericReferenceId = ++highReferenceId;
				seenReferences.put(referenceHash, numericReferenceId);

				addBibliographyEntry(reference);
			}
			final Row asmRefRow = ec.getRow("assessment_reference");
			asmRefRow.get("asm_id").setString(id);
			asmRefRow.get("field").setString(fieldName);
			asmRefRow.get("asm_type").setString(asm_type);
			asmRefRow.get("user").setObject(null);
			asmRefRow.get("ref_id").setString(referenceHash);
			try {
				final InsertQuery iqr = new InsertQuery("assessment_reference", asmRefRow);
				ec.doUpdate(iqr);
			} catch (final DBException dbx) {
				dbx.printStackTrace();
			}
		}

		private void process(final String id, final int taxId, final Document doc) throws DBException {
			for (final Element e : ElementCollection.childElements(doc.getDocumentElement())) {
				if ("field".equals(e.getNodeName())) {
					// fields
					final String fieldName = e.getAttribute("id");
					// look for references
					for (final Element reference : ElementCollection.childElementsByTagName(e, "reference"))
						addReference(id, fieldName, reference);
				} else if ("globalReferences".equals(e.getNodeName())) {
					for (final Element reference : ElementCollection.childElementsByTagName(e, "reference"))
						addReference(id, "Global", reference);
				}
			}
		}
	}

	/**
	 * This is a small-memory representation of a taxonomic node used during the
	 * export. Creating it triggers the parse of its own taxon file and,
	 * recursively, the associated assessments, plus the insertion in the
	 * database of relevant rows.
	 */
	private class TrivialNode implements TagFilter.Listener {
		TagFilter tf;
		StringWriter taxw;
		StringWriter asmw;
		private final int id;
		private boolean export;

		public TrivialNode(final int id, final String name, final TrivialNode parent, final int depth, boolean export) {
			this.id = id;
			this.export = export;

			try {
				final Reader r = ServerApplication.getStaticVFS().getReader(ServerPaths.getURLForTaxa("" + id));
				tf = new TagFilter(r);
				tf.shortCircuitClosingTags = false;
				tf.registerListener(this);
				tf.parse();

				// Do draft assessment
				if (export && exportDraftAssessments) {
					try {
						if (hasDraftAssessment()) {
							new TrivialAssessment(id);
						}
					} catch (NotFoundException e) {
						System.out.println("Draft assessment not found for " + id);
					}
				}
			} catch (final NotFoundException nf) {
				System.err.println("No taxon file found for " + id);
			} catch (final IOException ix) {
				System.err.println("IO exception parsing " + id);
			}
		}

		private String formatLiteral(String prop) {
			return ec.formatLiteral(new StringLiteral(prop == null ? "" : prop));
		}

		public int getId() {
			return id;
		}

		public boolean hasDraftAssessment() {
			return false;
		}

		public List<String> interestingTagNames() {
			if (nodeTagNames != null)
				return nodeTagNames;

			nodeTagNames = new ArrayList<String>();

			// If we're going to export it, do the interesting stuff
			// if( export ) {
			// System.out.println("Adding others...");
			nodeTagNames.add("assessments");
			nodeTagNames.add("/assessments");
			// }

			for (int i = 0; i < Taxon.getDisplayableLevelCount(); i++) {
				String curLevel = Taxon.getDisplayableLevel(i);
				nodeTagNames.add(curLevel.toLowerCase());
			}

			return nodeTagNames;
		}

		public void process(final Tag t) throws IOException {
			try {
				if ("assessments".equals(t.name)) {
					if (!export)
						return;

					asmw = new StringWriter();
					t.newTagText = "";
					tf.divert(asmw);
				} else if ("/assessments".equals(t.name)) {
					if (!export)
						return;

					tf.stopDiverting();
					final String assessments = asmw.toString();
					final String[] asmids = assessments.split(",");
					for (final String asmid : asmids)
						new TrivialAssessment(asmid, id);
				}
			} catch (final RuntimeException x) {
				x.printStackTrace();
			}
		}

		public void setTagFilter(final TagFilter tf) {
			// TODO Auto-generated method stub
		}
	}

	private static AtomicBoolean running = new AtomicBoolean(false);

	public static boolean isRunning() {
		return running.get();
	}

	private final HashMap<String, Integer> seenReferences = new HashMap<String, Integer>();
	private static ArrayList<String> nodeTagNames = null;

	private int highReferenceId = 0;

	private ExecutionContext ec = null;

	private int taxonCount = 0;
	private String workingsetId = null;

	private boolean exportDraftAssessments = false;

	private static final String DS = "ref_lookup";

	public ReferenceDBBuilder() {
		super();
		exportDraftAssessments = true;
	}

	public ReferenceDBBuilder(Uniform uniform) {
		super();
		exportDraftAssessments = true;
	}

	private void createAndConnect() throws IOException, NamingException, DBException {
		DBSessionFactory.getDBSession(DS);

		ec = new SystemExecutionContext(DS);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);

		try {
			ec.dropTable("assessment_reference");
//			ec.dropTable("changed_references");

			ec.createStructure(ClasspathResources.getDocument(ReferenceDBBuilder.class, "reflookup-struct.xml"));
		} catch (final DBException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void export() throws IOException, NamingException, DBException {
		createAndConnect();
		populateDB();
	}

	private void populateDB() {
		final long time = System.currentTimeMillis();
		final Document taxdoc = TaxonomyDocUtils.getTaxonomyDocByID();

		final NodeCollection nodes = new NodeCollection(taxdoc.getDocumentElement().getChildNodes());
		taxonCount = 0;
		traverseNodes(null, nodes, 0, null);
		System.out.println("Taxon count: " + taxonCount);
		System.out.println("Overall exported in " + (System.currentTimeMillis() - time) + " ms");
	}

	public void run() {
		if (!running.compareAndSet(false, true))
			return;
		try {
			export();
		} catch (final Throwable t) {
			t.printStackTrace();
		} finally {
			DBSessionFactory.unregisterDataSource(DS);
			running.set(false);
		}
	}

	private void traverseNodes(final TrivialNode parent, final NodeCollection nodes, final int depth,
			ArrayList<String> ids) {
		for (final Node node : nodes) {
			if (!(node instanceof Element))
				continue;
			final Element element = (Element) node;
			final String elementName = element.getNodeName();
			if (!elementName.startsWith("node"))
				continue;

			final int nodeID = Integer.valueOf(elementName.substring(4));
			final String taxonName = element.getAttribute("name");
			taxonCount++;
			if (taxonCount % 100 == 0) {
				System.out.println(taxonCount);
			}

			if (ids == null) {
				final TrivialNode tn = new TrivialNode(nodeID, taxonName, parent, depth, true);
				if (node.hasChildNodes()) {
					traverseNodes(tn, new NodeCollection(node.getChildNodes()), depth + 1, ids);
				}
			} else {
				boolean export = ids.contains(String.valueOf(nodeID));
				final TrivialNode tn = new TrivialNode(nodeID, taxonName, parent, depth, export);
				if (node.hasChildNodes()) {
					traverseNodes(tn, new NodeCollection(node.getChildNodes()), depth + 1, ids);
				}
			}
		}
	}
}
