package com.solertium.util.querybuilder.struct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.PortableAlphanumericComparator;

/**
 * DBStructure.java
 *
 * Represents the struct.xml file for a database
 *
 * @author carl.scott
 *
 */
public class DBStructure {
	
	public static final String TABLE_CHOOSER_FRIENDLY = "friendly";
	public static final String TABLE_CHOOSER_OFFICIAL = "official";

	private String url;
	private HashMap<String, QBTable> allTables;
	private HashMap<String, QBLookupTable> lookupTables;

	private ArrayList<String> privateTables;
	private String chooserType = TABLE_CHOOSER_FRIENDLY;

	private static DBStructure instance;

	public static DBStructure getInstance() {
		if (instance == null)
			instance = new DBStructure();
		return instance;
	}

	/**
	 * Constructor.
	 * @param url the url where the struct.xml file can be accessed.
	 */
	private DBStructure() {
		allTables = new HashMap<String, QBTable>();
		lookupTables = new HashMap<String, QBLookupTable>();
		privateTables = new ArrayList<String>();
	}

	public void setURL(String url) {
		this.url = url;
	}

	public void addPrivateTable(String table) {
		privateTables.add(table);
	}

	public void load(final GenericCallback<Object> callback) {
		final NativeDocument doc = NativeDocumentFactory.newNativeDocument();
		doc.get(url, new GenericCallback<String>() {
			public void onSuccess(String result) {
				parse(doc);
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	private void parse(NativeDocument doc) {
		NativeNodeList nodes = doc.getDocumentElement().getElementsByTagName("table");
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeElement cur = nodes.elementAt(i);
			if (cur.getNodeName().equals("table")) {
				QBTable table = new QBTable(cur);
				if (!privateTables.contains(table.getTableName())) {
					table.load();
					allTables.put(table.getTableName(), table);
				}
			}
		}
	}

	public QBTable getTable(String name) {
		return allTables.get(name);
	}

	public ArrayList<String> getTableNames() {
		ArrayList<String> names = new ArrayList<String>(allTables.keySet());
		Collections.sort(names, new PortableAlphanumericComparator());
		return names;
	}

	public void loadLookupTables(final String lookupRoute, final NativeDocument doc) {
		NativeNodeList nodes = doc.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeElement cur = nodes.elementAt(i);
			if (cur.getNodeName().equals("column")) {
				final String name = cur.getAttribute("canonicalName");
				NativeNodeList children = cur.getChildNodes();
				for (int k = 0; k < children.getLength(); k++) {
					NativeElement curChild = children.elementAt(k);
					if (curChild.getNodeName().equals("lookup")) {
						lookupTables.put(name, new QBLookupTable(curChild, lookupRoute));
						break;
					}
				}
			}
		}
	}

	public boolean hasLookupTable(String canonicalColumnName) {
		return lookupTables.containsKey(canonicalColumnName);
	}

	public QBLookupTable getLookupTable(String canonicalColumnName) {
		return (lookupTables.get(canonicalColumnName));
	}

	public String getChooserType() {
		return chooserType;
	}

	public void setChooserType(String type) {
		this.chooserType = type;
	}

}
