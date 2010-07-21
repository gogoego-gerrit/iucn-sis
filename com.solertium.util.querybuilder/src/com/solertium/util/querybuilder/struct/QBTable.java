package com.solertium.util.querybuilder.struct;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;


public class QBTable {
	
	private boolean isLoaded = false;
	private QBColumnSet columns;
	private String tableName;
	private String friendlyName;
	private String description;
	
	private NativeElement node;
	
	public QBTable(NativeElement node) {
		this.node = node;
		columns = new QBColumnSet();
		tableName = node.getAttribute("name");
		friendlyName = node.getAttribute("friendly");
		if (friendlyName == null)
			friendlyName = tableName;
	}
	
	public void load() {
		NativeNodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			NativeElement curChild = children.elementAt(i);
			if (curChild.getNodeName().equals("column")) {
				columns.addColumn(curChild);
			}
			else if (curChild.getNodeName().equals("description")) {
				description = curChild.getTextContent();
			}
		}
		isLoaded = true;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean hasDescription() {
		return description != null;
	}
	
	public boolean hasFriendlyName() {
		return node.getAttribute("friendly") != null;
	}
	
	public QBColumnSet getColumns() {
		return columns;
	}
	
	public boolean isLoaded() {
		return isLoaded;
	}

}
