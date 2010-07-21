package com.solertium.util.querybuilder.struct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class QBColumnSet extends LinkedHashMap<String, QBColumn> {
	private static final long serialVersionUID = 1L;

	public QBColumnSet() {
		super();
	}

	public void addColumn(NativeElement node) {
		QBColumn col = new QBColumn(node);
		put(col.getName(), col);
	}

	public ArrayList<String> getColumnNames() {
		ArrayList<String> names = new ArrayList<String>(keySet());
		Collections.sort(names, new PortableAlphanumericComparator());
		return names;
	}

	public ArrayList<QBColumn> getColumns() {
		return new ArrayList<QBColumn>(values());
	}

	public QBColumn getColumn(String name) {
		return get(name);
	}

}
