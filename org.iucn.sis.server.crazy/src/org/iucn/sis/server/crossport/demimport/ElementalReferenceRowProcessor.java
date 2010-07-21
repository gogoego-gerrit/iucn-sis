package org.iucn.sis.server.crossport.demimport;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;

public class ElementalReferenceRowProcessor extends RowProcessor {

	private final Document doc;
	private final Element rootEl;

	public ElementalReferenceRowProcessor(final Document doc, final Element rootEl) {
		this.doc = doc;
		this.rootEl = rootEl;
	}

	@Override
	public void process(final Row ri) {
		try {
			Row r = ri.getStructuredRow(getExecutionContext(), "bibliography");
			final Element reference = doc.createElement("reference");
			rootEl.appendChild(reference);
			reference.setAttribute("id", r.get("Bib_hash").getString());
			final String type = r.get("Publication_Type").getString();
			reference.setAttribute("type", type);
			for (final Column c : r.getColumns()) {
				final Element field = doc.createElement("field");
				field.setAttribute("name", c.getLocalName());
				final String lvalue = c.getString();
				if (lvalue != null)
					field.appendChild(doc.createTextNode(lvalue));
				reference.appendChild(field);
			}
		} catch (DBException dbx) {
			dbx.printStackTrace();
		}
	}

}
