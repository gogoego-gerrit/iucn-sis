package org.iucn.sis.server.extensions.references;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.naming.NamingException;

import org.iucn.sis.server.extensions.references.ReferenceLabels.LabelMappings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.Column;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.SystemExecutionContext;
import com.solertium.util.TrivialExceptionHandler;

public class ReferenceRowProcessor extends RowProcessor {

	private final Document doc;
	private final ReferenceLabels labels;
	private final Element rootEl;

	public ReferenceRowProcessor(final Document doc, final Element rootEl, final ReferenceLabels labels) {
		this.doc = doc;
		this.rootEl = rootEl;
		this.labels = labels;
	}

	/*
	 * SELECT FROM (SELECT BIB_HASH, COUNT() AS "COUNT" FROM (SELECT FROM
	 * BIBLIOGRAPHY JOIN ASSESSMENT_REFERENCE ON
	 * ASSESSMENT_REFERENCE.REF_ID=BIBLIOGRAPHY.BIB_HASH WHERE UPPER(AUTHOR)
	 * LIKE UPPER('%Galt%') AND YEAR='2009') GROUP BY BIB_HASH) T1 JOIN
	 * BIBLIOGRAPHY WHERE BIBLIOGRAPHY.BIB_HASH=T1.BIB_HASH
	 */

	@Override
	public void process(ResultSet rs, ExecutionContext ec) throws Exception {
		try {
			if (!rs.isBeforeFirst())
				return; // no rows, do NOTHING.
		} catch (final SQLException ignored) { // possibly operation not
			// supported
			TrivialExceptionHandler.ignore(this, ignored);
		}
		setExecutionContext(ec);
		final ResultSetMetaData rsmd = rs.getMetaData();
		start();
		while (rs.next()) {
			final Row row = ec.getDBSession().rsToRow(rs, rsmd);
			process(row);
		}
		finish();
	}

	@Override
	public void process(final Row r) {
		try {
			final ExecutionContext ec = new SystemExecutionContext(ReferenceApplication.DBNAME);
			ec.setExecutionLevel(ExecutionContext.ADMIN);
			ec.setAPILevel(ExecutionContext.SQL_ALLOWED);

			final Element reference = doc.createElement("reference");
			rootEl.appendChild(reference);
			
			try {
				reference.setAttribute("rowID", r.get("id").toString());
			} catch (NullPointerException e) {
				reference.setAttribute("rowID", "0");
			}
			reference.setAttribute("id", r.get("Bib_hash").getString());
			
			final String type = r.get("Publication_Type").getString();
			reference.setAttribute("type", type);
			for (final Column c : r.getColumns()) {
				LabelMappings lm = labels.get(type);
				final String ltext;
				if (lm == null || "count".equalsIgnoreCase(c.getLocalName())) {
					ltext = c.getLocalName();
				} else {
					ltext = lm.get(c.getLocalName());
				}
				if (ltext != null) {
					if (!"count".equalsIgnoreCase(ltext)) {
						String lcfn = LabelMappings.normalize(c.getLocalName());
						if (!"publication_type".equals(lcfn)) {
							final Element field = doc.createElement("field");
							field.setAttribute("name", lcfn);
							field.setAttribute("label", ltext);
							final String lvalue = c.getString();
							if (lvalue != null)
								field.appendChild(doc.createTextNode(lvalue));
							reference.appendChild(field);
						}
					} else {
						reference.setAttribute("count", c.getString());
					}
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
}
