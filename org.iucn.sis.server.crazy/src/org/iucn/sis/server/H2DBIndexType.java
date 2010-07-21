package org.iucn.sis.server;

import java.io.IOException;
import java.util.ArrayList;

import javax.naming.NamingException;

import org.iucn.sis.server.simple.BrowseTaxonomyRestlet;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.FilenameStriper;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.ExistenceProcessor;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QColumn;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.QRelationConstraint;
import com.solertium.db.query.Query;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;
import com.solertium.util.SysDebugger;
import com.solertium.vfs.VFS;

public class H2DBIndexType implements SearchIndexType {

	private class LastModifiedRowProcessor extends RowProcessor {
		private long lastMod;

		public LastModifiedRowProcessor() {
			super();
			lastMod = 0;
		}

		long getLastModified() {
			return lastMod;
		}

		@Override
		public void process(final Row row) {
			lastMod = Long.valueOf(row.get("LAST_MODIFIED").getLong());
		}

	}

	private VFS vfs;

	private ExecutionContext ec;

	public H2DBIndexType() throws DBException {

		SysDebugger.getInstance().println("Injecting/creating search database structure");

		try {
			Document structDoc = DocumentUtils.newDocumentBuilder().parse(
					H2DBIndexType.class.getResourceAsStream("searchstruct.xml"));
			ec = new SystemExecutionContext("default");
			ec.setExecutionLevel(ExecutionContext.ADMIN);
			ec.createStructure(structDoc);
		} catch (IOException iox) {
			throw new DBException("Database could not be initialized", iox);
		} catch (NamingException nx) {
			throw new DBException("Database could not be initialized", nx);
		} catch (SAXException sax) {
			throw new RuntimeException("Database structure could not be parsed; is your build corrupt?", sax);
		}

	}

	public ArrayList<String> get(final Document keys) {
		final ArrayList<String> results = new ArrayList<String>();

		if ((keys.getDocumentElement().getElementsByTagName("assessor").getLength() == 0 && keys.getDocumentElement()
				.getElementsByTagName("country").getLength() == 0))
			if (keys.getDocumentElement().getElementsByTagName("commonName").getLength() == 0
					&& keys.getDocumentElement().getElementsByTagName("sciName").getLength() == 0)
				return results;

		final SelectQuery select = new SelectQuery();
		// final ExperimentalSelectQuery select = new ExperimentalSelectQuery();
		select.select("taxonKeys", "NODE_ID");
		select.select(new CanonicalColumnName("taxonKeys", "SCI_NAME"), "ASC");
		select.select("taxonKeys", "LEVEL");
		select.select("taxonKeys", "COUNTRY");
		select.select("taxonKeys", "ASSESSOR");
		select.select(new CanonicalColumnName("commonNames", "COMMON_NAME"), "ASC");

		QColumn qColCommonNames = new QColumn();
		qColCommonNames.setName(new CanonicalColumnName("commonNames", "NODE_ID"));
		qColCommonNames.setOuter(true);

		QColumn qColTaxonKeys = new QColumn();
		qColTaxonKeys.setName(new CanonicalColumnName("taxonKeys", "NODE_ID"));
		qColTaxonKeys.setOuter(true);

		// try this, and if it doesn't work try reversing the order here and in
		// the join call

		select.select(qColCommonNames);
		select.select(qColTaxonKeys);

		select.join("commonNames", new QRelationConstraint(new CanonicalColumnName("taxonKeys", "NODE_ID"),
				new CanonicalColumnName("commonNames", "NODE_ID")));

		boolean qcAND = false;

		if (keys.getDocumentElement().getElementsByTagName("commonName").getLength() > 0) {
			select.constrain(new QComparisonConstraint(new CanonicalColumnName("commonNames", "COMMON_NAME"),
					QConstraint.CT_CONTAINS, keys.getDocumentElement().getElementsByTagName("commonName").item(0)
							.getTextContent().toLowerCase()));
			qcAND = true;
		}

		if (keys.getDocumentElement().getElementsByTagName("country").getLength() > 0) {
			if (qcAND) {
				select.constrain(QConstraint.CG_AND, new QComparisonConstraint(new CanonicalColumnName("taxonKeys",
						"COUNTRY"), QConstraint.CT_CONTAINS, keys.getDocumentElement().getElementsByTagName("country")
						.item(0).getTextContent().toLowerCase()));
			}
		}
		if (keys.getDocumentElement().getElementsByTagName("assessor").getLength() > 0) {
			if (qcAND) {
				select.constrain(QConstraint.CG_AND, new QComparisonConstraint(new CanonicalColumnName("taxonKeys",
						"ASSESSOR"), QConstraint.CT_CONTAINS, keys.getDocumentElement()
						.getElementsByTagName("assessor").item(0).getTextContent().toLowerCase()));
			}
		}

		if (keys.getDocumentElement().getElementsByTagName("sciName").getLength() > 0) {
			if (qcAND) {
				select.constrain(QConstraint.CG_OR, new QComparisonConstraint(new CanonicalColumnName("taxonKeys",
						"SCI_NAME"), QConstraint.CT_CONTAINS, keys.getDocumentElement().getElementsByTagName("sciName")
						.item(0).getTextContent().toLowerCase()));
			} else {
				select.constrain(new QComparisonConstraint(new CanonicalColumnName("taxonKeys", "SCI_NAME"),
						QConstraint.CT_CONTAINS, keys.getDocumentElement().getElementsByTagName("sciName").item(0)
								.getTextContent().toLowerCase()));
				qcAND = true;
			}
			if (keys.getDocumentElement().getElementsByTagName("country").getLength() > 0) {
				if (qcAND) {
					select.constrain(QConstraint.CG_AND, new QComparisonConstraint(new CanonicalColumnName("taxonKeys",
							"COUNTRY"), QConstraint.CT_CONTAINS, keys.getDocumentElement().getElementsByTagName(
							"country").item(0).getTextContent().toLowerCase()));
				}
			}
			if (keys.getDocumentElement().getElementsByTagName("assessor").getLength() > 0) {
				if (qcAND) {
					select.constrain(QConstraint.CG_AND, new QComparisonConstraint(new CanonicalColumnName("taxonKeys",
							"ASSESSOR"), QConstraint.CT_CONTAINS, keys.getDocumentElement().getElementsByTagName(
							"assessor").item(0).getTextContent().toLowerCase()));
				}
			}
		}
		if (keys.getDocumentElement().getElementsByTagName("country").getLength() > 0) {
			if (!qcAND) {
				select.constrain(new QComparisonConstraint(new CanonicalColumnName("taxonKeys", "COUNTRY"),
						QConstraint.CT_CONTAINS, keys.getDocumentElement().getElementsByTagName("country").item(0)
								.getTextContent().toLowerCase()));
			}
		}
		if (keys.getDocumentElement().getElementsByTagName("assessor").getLength() > 0) {
			if (!qcAND) {
				select.constrain(new QComparisonConstraint(new CanonicalColumnName("taxonKeys", "ASSESSOR"),
						QConstraint.CT_CONTAINS, keys.getDocumentElement().getElementsByTagName("assessor").item(0)
								.getTextContent().toLowerCase()));
			}
		}

		SysDebugger.getInstance().println(select.getSQL(ec.getDBSession()));

		try {
			System.out.println("DOING QUERY...");
			ec.doQuery(select, new RowProcessor() {
				@Override
				public void process(final Row row) {
					// SysDebugger.getInstance().println("x " +
					// row.get("NODE_ID").getString());
					// LIMIT RESULTS TO SPECIES AND BELOW
					if (Integer.valueOf(row.get("LEVEL").getString()) >= TaxonNode.SPECIES) {
						if (!results.contains(row.get("NODE_ID").getString()))
							results.add(row.get("NODE_ID").getString());
					}
				}
			});
			System.out.println("QUERY RETURNED...");
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	public long getLastModified(final String key) {
		// TODO Auto-generated method stub

		final SelectQuery select = new SelectQuery();
		select.select("taxonKeys", "NODE_ID");
		select.select("taxonKeys", "LAST_MODIFIED");
		select.constrain(new QComparisonConstraint(new CanonicalColumnName("taxonKeys", "NODE_ID"),
				QConstraint.CT_EQUALS, key));

		final LastModifiedRowProcessor rProc = new LastModifiedRowProcessor();

		try {
			ec.doQuery(select, rProc);

		} catch (final Exception e) {
			e.printStackTrace();
			// return 0;
		}

		return rProc.getLastModified();
	}

	public void index(final Document doc, final long lastModified) {
		if (doc == null)
			return;

		final Element root = doc.getDocumentElement();

		if (root.getElementsByTagName("speciesID").getLength() > 0) {
			// Re-index node which owns this assessment
			Element sid = (Element) root.getElementsByTagName("speciesID").item(0);
			SysDebugger.getInstance().println(
					"reindexing ... " + FilenameStriper.getIDAsStripedPath(sid.getTextContent()));
			String uri = FilenameStriper.getIDAsStripedPath(sid.getTextContent());
			Document node = DocumentUtils.getVFSFileAsDocument("/browse/nodes/" + uri + ".xml", vfs);
			try {
				index(node, vfs.getLastModified("/" + uri));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		if (root.getAttribute("level").equals(""))
			return;

		final Row row = new Row();

		final String id = root.getAttribute("id");

		// final DeleteQuery remove = new DeleteQuery();
		// remove.setTable("taxonKeys");
		// remove.constrain(new QComparisonConstraint(new
		// CanonicalColumnName("taxonKeys", "NODE_ID"),
		// QConstraint.CT_EQUALS, id));
		// try {
		// System.out.println(remove.getSQL(ec.getDBSession()));
		// ec.doUpdate(remove);
		// System.out.println("NODE "+id+" removed from search database");
		// } catch (DBException dbx) {
		// }
		//		

		final Column idc = new CInteger("NODE_ID", 0);
		idc.setString(id);
		row.add(idc);

		row.add(new CString("LAST_MODIFIED", String.valueOf(lastModified)));

		final String level = doc.getDocumentElement().getAttribute("level");
		row.add(new CString("LEVEL", level.toLowerCase()));

		String sciName = "";
		if (!BrowseTaxonomyRestlet.KINGDOM.equalsIgnoreCase(level)) {
			String[] footprint = doc.getDocumentElement().getElementsByTagName("footprint").item(0).getTextContent()
					.split(",");
			for (int i = 5; i < footprint.length; i++)
				sciName += footprint[i] + " ";
		}

		final String name = sciName + doc.getDocumentElement().getAttribute("name");
		row.add(new CString("SCI_NAME", name.toLowerCase()));

		final String status = doc.getDocumentElement().getAttribute("status");
		row.add(new CString("STATUS", status.toUpperCase()));

		if (doc.getElementsByTagName("assessments").getLength() > 0) {
			String[] assessments = doc.getElementsByTagName("assessments").item(0).getTextContent().split(",");
			if (!assessments[0].equals("")) {
				String uri = FilenameStriper.getIDAsStripedPath(assessments[0]);
				// SysDebugger.getInstance().println(uri);
				Document current = DocumentUtils.getVFSFileAsDocument("/browse/assessments/" + uri + ".xml", vfs);
				if (current != null) {
					NodeList schemes = current.getDocumentElement().getElementsByTagName("classificationScheme");
					for (int i = 0; i < schemes.getLength(); i++) {
						if (((Element) schemes.item(i)).getAttribute("id").equals("CountryOccurrence")) {
							NodeList countries = ((Element) schemes.item(i)).getElementsByTagName("label");
							String countryList = "";
							for (int t = 0; t < countries.getLength(); i++)
								countryList += countries.item(t).getTextContent() + ",";
							row.add(new CString("COUNTRY", countryList.toLowerCase()));
						}
					}
					NodeList eval = current.getDocumentElement().getElementsByTagName("evaluators");
					if (eval != null && eval.getLength() > 0) {
						Element a = (Element) eval.item(0);
						if (a != null) {
							String s = a.getTextContent();
							if (s != null)
								row.add(new CString("ASSESSOR", s.toLowerCase()));
						}
					}
				}

			}
		}

		final SelectQuery select = new SelectQuery();
		select.select("taxonKeys", "NODE_ID");
		select.constrain(new QComparisonConstraint(new CanonicalColumnName("taxonKeys", "NODE_ID"),
				QConstraint.CT_EQUALS, id));

		final ExistenceProcessor check = new ExistenceProcessor();

		try {
			ec.doQuery(select, check);
		} catch (DBException dbx) {
			SysDebugger.getInstance().println("Not updating anything; could not check existence");
			dbx.printStackTrace();
			return;
		}

		final Query sql;
		if (check.exists()) {
			UpdateQuery usql = new UpdateQuery();
			usql.setRow(row);
			usql.setTable("taxonKeys");
			usql.constrain(new CanonicalColumnName("taxonKeys", "NODE_ID"), QConstraint.CT_EQUALS, row.get("NODE_ID")
					.getString());
			sql = usql;
		} else {
			InsertQuery isql = new InsertQuery();
			isql.setRow(row);
			isql.setTable("taxonKeys");
			sql = isql;
		}

		try {
			ec.doUpdate(sql);
		} catch (final DBException e) {
			e.printStackTrace();
		}

		DeleteQuery dsql = new DeleteQuery();
		dsql.setTable("commonNames");
		dsql.constrain(new CanonicalColumnName("commonNames", "NODE_ID"), QConstraint.CT_EQUALS, row.get("NODE_ID")
				.getString());
		try {
			ec.doUpdate(dsql);
		} catch (final DBException e) {
			e.printStackTrace();
		}

		final NodeList commonNames = doc.getElementsByTagName("commonName");
		for (int i = 0; i < commonNames.getLength(); i++) {
			final Row commonNamerow = new Row();
			commonNamerow.add(idc);
			commonNamerow.add(new CString("COMMON_NAME", (commonNames.item(i).getAttributes().getNamedItem("name")
					.getTextContent()).toLowerCase()));
			InsertQuery isql = new InsertQuery();
			isql.setRow(commonNamerow);
			isql.setTable("commonNames");
			try {
				ec.doUpdate(isql);
			} catch (final DBException e) {
				e.printStackTrace();
			}
		}

	}

	public void setVFS(VFS vfs) {
		this.vfs = vfs;
	}

}
