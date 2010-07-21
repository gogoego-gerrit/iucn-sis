package com.solertium.util.querybuilder.query;

import java.util.ArrayList;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class QBQuery {

	protected static final int INPUT_TYPE_FREE_TEXT = 1;
	protected static final int INPUT_TYPE_LOOKUP_VALUE = 2;

	protected ArrayList<SelectedField> fields; //SelectedField
	protected ArrayList<String> tables; //String

	protected QBConstraintGroup conditions;

	public QBQuery() {
		fields = new ArrayList<SelectedField>();
		tables = new ArrayList<String>();
		conditions = new QBConstraintGroup();
	}

	public void load(NativeDocument doc) {
		if (doc != null && doc.getPeer() != null && doc.getDocumentElement() != null) {
			load(doc.getDocumentElement());
		}
	}

	public void load(NativeNode element) {
		NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeElement cur = nodes.elementAt(i);
			if (cur.getNodeName().equals("select")) {
				addField(new SelectedField(cur));
			}
			//Skip the tables, they'll get automatically added based
			//on what's selected
			else if (cur.getNodeName().equals("constraint")) {
				conditions.loadConfig(cur);
			}
		}
	}

	public String toXML() {
		String xml = "<query version=\"1.1\">\r\n";
		for (int i = 0; i < fields.size(); i++) {
			SelectedField field = fields.get(i);
			xml += field.toXML() + "\r\n";
		}
		for (int i = 0; i < tables.size(); i++) {
			xml += "<table>" + tables.get(i) + "</table>\r\n";
		}
		//TODO: save arbitrary joins...
		xml += conditions.saveConfig();
		xml += "\r\n</query>";
		return xml;
	}

	public ArrayList<SelectedField> getFields() {
		return fields;
	}

	public void addField(SelectedField field) {
		fields.add(field);
		if (!tables.contains(field.getTableName()))
			tables.add(field.getTableName());
	}

	/**
	 * Use this to add a table simply to satisfy the
	 * autojoiner.  You should not use this function
	 * in normal use, and should use addField, which
	 * will add the table for you.
	 * @param table the table name
	 */
	public void addTable(String table) {
		if (!tables.contains(table))
			tables.add(table);
	}

	public void removeField(SelectedField field) {
		fields.remove(field);
		boolean keepTable = false;
		for (int i = 0; i < fields.size(); i++) {
			SelectedField f = fields.get(i);
			if (f.getTableName().equals(field.getTableName())) {
				keepTable = true;
				break;
			}
		}
		if (!keepTable)
			tables.remove(field.getTableName());
	}

	public boolean isSelected(String table, String field) {
		for (int i = 0; i < fields.size(); i++) {
			SelectedField cur = fields.get(i);
			if (cur.getTableName().equals(table) &&
				cur.getColumnName().equals(field))
					return true;
		}
		return false;
	}

	public void swapOrder(int index, int otherIndex) {
		SelectedField field = fields.get(index);
		SelectedField other = fields.get(otherIndex);
		fields.set(index, other);
		fields.set(otherIndex, field);
	}

	public QBConstraintGroup getConditions() {
		return conditions;
	}

	public ArrayList<String> getTables() {
		return tables;
	}

}
