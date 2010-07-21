package com.solertium.util.querybuilder.query;

import java.util.HashMap;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNamedNodeMap;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.util.querybuilder.struct.DBStructure;
import com.solertium.util.querybuilder.struct.QBColumn;

public class SelectedField {

	private String table;
	private String column;

	private HashMap<String, String> attributes;

	private boolean allowLabels = true;

	public SelectedField(String table, String column) {
		this.table = table;
		this.column = column;

		attributes = new HashMap<String, String>();
	}

	public SelectedField(NativeElement element) {
		String cName = element.getTextContent();
		if (cName != null) {
			int index = cName.indexOf(".");
			if (index != -1) {
				this.table = cName.substring(0, index);
				this.column = cName.substring(index + 1);
			}
		}

		attributes = new HashMap<String, String>();

		NativeNamedNodeMap map = element.getAttributes();
		for (int i = 0; i < map.getLength(); i++) {
			NativeNode cur = map.item(i);
			setAttribute(cur.getNodeName(), cur.getTextContent());
		}
	}

	public void setAllowLabels(boolean allowLabels) {
		this.allowLabels = allowLabels;
	}

	public String getCanonicalName() {
		return table + "." + column;
	}

	public String toXML() {
		return "<select" + getAttrXML() + ">" + getCanonicalName() + "</select>";
	}

	private String getAttrXML() {
		String xml = "";
		if (getAttribute("sort") != null)
			xml += " sort=\"" + getAttribute("sort") + "\"";
		if (allowLabels) {
			if (getAttribute("label") != null)
				xml += " label=\"" + getAttribute("label") + "\"";
			else if (DBStructure.getInstance().getChooserType().equals(DBStructure.TABLE_CHOOSER_FRIENDLY)) {
				try {
					QBColumn col = DBStructure.getInstance().getTable(table).
						getColumns().getColumn(column);
					if (col.hasFriendlyName())
						xml += " label=\"" + col.getFriendlyName() + "\"";
				} catch (Throwable e) {
					//It's ok.
				}
			}
		}
		if (getAttribute("header") != null)
			xml += " header=\"" + getAttribute("header") + "\"";
		if (getAttribute("range") != null)
			xml += " range=\"" + getAttribute("range") + "\"";
		if (getAttribute("outer") != null)
			xml += " outer=\"" + getAttribute("outer") + "\"";
		if (getAttribute("writable") != null)
			xml += " writable=\"" + getAttribute("writable") + "\"";

		return xml;
	}

	public String getTableName() {
		return table;
	}

	public String getColumnName() {
		return column;
	}

	public String getAttribute(String key) {
		return attributes.get(key);
	}

	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public String toString() {
		return toXML();
	}

	public String getDisplay(boolean friendly) {
		return (friendly ? DBStructure.getInstance().getTable(table).
			getColumns().getColumn(column).getFriendlyName() : column) +
			" <span class=\"fontSize60\">(" + getCanonicalName() + ")</span>" +
			(getAttribute("sort") == null ? "" : " (sort " + getAttribute("sort") + ")");
	}

}
