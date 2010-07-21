package com.solertium.util.querybuilder.gwt.client;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.solertium.util.querybuilder.gwt.client.tree.QBConditionGroupRootItem;
import com.solertium.util.querybuilder.gwt.client.tree.QBTreeItem;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.query.QBQuery;

public class QueryDesigner extends LayoutContainer {

	private GWTQBQuery query;

	public QueryDesigner(GWTQBQuery query) {
		this.query = query;
		draw();
	}

	private void draw() {
		QBTreeItem item = new QBTreeItem(0, true) {
			public GWTQBQuery getQuery() {
				return query;
			}
		};
		add(item);
		item.show("Fields");
		item.expandTree();

		QBConditionGroupRootItem cond = new QBConditionGroupRootItem() {
			public GWTQBQuery getQuery() {
				return query;
			}
		};
		add(cond);
		cond.show("Conditions");
		cond.expandTree();
	}

	public QBQuery getQuery() {
		return query;
	}
}
