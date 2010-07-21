package org.iucn.sis.server.ref;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.ref.ReferenceLabels.LabelMappings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SilentExecutionContext;
import com.solertium.util.ElementCollection;

public class Reference extends Row {

	public Reference() {
		createColumns();
	}

	public Reference(Element el) {
		this();
		loadFromElement(el);
	}

	public Reference(ReferenceUI ref) {
		this();
		loadFromElement(ref);
	}

	public Reference(String s) throws SAXException {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					new org.xml.sax.InputSource(new StringReader(s)));
			loadFromElement(doc.getDocumentElement());
		} catch (ParserConfigurationException px) { // config problem,
			// unexpected
			throw new RuntimeException(px);
		} catch (IOException ix) { // should not occur on a string
			throw new RuntimeException(ix);
		}
	}

	private void createColumns() {
		try {
			ExecutionContext ec = new SilentExecutionContext(ReferenceApplication.DBNAME);
			if (!ec.getDBSession().hasDefinitionMinimum(ReferenceApplication.DBDEFINITION, 2)) {
				ReferenceApplication.initializeDatabase();
			}
			Row prototype = ec.getRow("bibliography");
			for (Column c : prototype.getColumns()) {
				add(c);
			}
		} catch (NamingException nx) {
			throw new RuntimeException("Database is not setup, cannot create references", nx);
		} catch (DBException dx) {
			throw new RuntimeException("Database is not setup, cannot create references", dx);
		}
	}

	public Element createElement(Document doc) {
		Element reference = doc.createElement("reference");
		reference.setAttribute("id", getId());
		final String type = get("Publication_Type").getString();
		reference.setAttribute("type", type);
		ReferenceLabels labels = ReferenceLabels.getInstance();
		for (final Column c : getColumns()) {
			LabelMappings lm = labels.get(type);
			final String ltext;
			if (lm == null) {
				ltext = c.getLocalName();
			} else {
				ltext = lm.get(c.getLocalName());
			}
			if (ltext != null) {
				String lcfn = LabelMappings.normalize(c.getLocalName());
				if (!"publication_type".equals(lcfn)) {
					final String lvalue = c.getString();
					if (lvalue != null) {
						final Element field = doc.createElement("field");
						field.setAttribute("name", lcfn);
						field.setAttribute("label", ltext);
						field.appendChild(doc.createTextNode(lvalue));
						reference.appendChild(field);
					}
				}
			}
		}
		return reference;
	}

	public String getId() {
		String id = getMD5Hash();
		set("Bib_Hash", id);
		return id;
	}

	public Map<String, String> getMap() {
		HashMap<String, String> ret = new HashMap<String, String>();
		for (final Column c : getColumns()) {
			String lcfn = LabelMappings.normalize(c.getLocalName());
			if (!"publication_type".equals(lcfn)) {
				final String lvalue = c.getString();
				if (lvalue != null) {
					ret.put(lcfn, lvalue);
				}
			}
		}
		return ret;
	}

	public String getType() {
		return get("Publication_Type").getString();
	}

	private void loadFromElement(Element el) {
		setReferenceType(el.getAttribute("type"));
		ElementCollection fields = new ElementCollection(el.getElementsByTagName("field"));
		for (Element field : fields) {
			String name = field.getAttribute("name");
			String value = field.getTextContent();
			if (name != null && value != null)
				set(name, value);
		}
	}

	private void loadFromElement(ReferenceUI ref) {
		setReferenceType(ref.getReferenceType());
		for (Entry<String, String> entry : ref.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			if (name != null && value != null)
				set(name, value);
		}
	}

	private void set(String columnName, String value) {
		Column c = get(columnName);
		if( c != null )
			c.setString(value);
		else
			System.out.println("*** Unable to set reference column " + columnName);
	}

	public void setField(String field, String value) {
		Column pt = get(field);
		pt.setString(value);
	}

	public void setReferenceType(String type) {
		setField("Publication_Type", type);
	}

}
