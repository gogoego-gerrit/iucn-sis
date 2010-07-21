package com.solertium.util.querybuilder.struct;

import java.util.HashMap;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNamedNodeMap;
import com.solertium.lwxml.shared.NativeNode;

public class QBColumn extends HashMap<String, String> {
	private static final long serialVersionUID = 1L;

	public QBColumn() {
		super();
	}

	public QBColumn(NativeElement node) {
		super();
		load(node);
	}

	public void load(NativeElement node) {
		NativeNamedNodeMap map = node.getAttributes();
		for (int i = 0; i < map.getLength(); i++) {
			NativeNode cur = map.item(i);
			put(cur.getNodeName(), cur.getTextContent());
		}
	}

	public String getName() {
		return get("name");
	}

	public String getFriendlyName() {
		return containsKey("friendly") ? (String)get("friendly") : getName();
	}

	public boolean hasFriendlyName() {
		return containsKey("friendly");
	}

	public String getType() {
		return get("type");
	}

	public boolean isKey() {
		return get("key") != null && (get("key")).equals("true");
	}

	public String getRelatedTable() {
		return get("relatedTable");
	}

	public String getRelatedColumn() {
		return get("relatedColumn");
	}

	public String getProperty(String key) {
		return get(key);
	}

}
