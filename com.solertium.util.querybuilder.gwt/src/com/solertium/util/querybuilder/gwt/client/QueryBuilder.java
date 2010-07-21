package com.solertium.util.querybuilder.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.struct.DBStructure;

public class QueryBuilder implements EntryPoint {

	public QueryBuilder() {

	}

	public void onModuleLoad() {
		if (GWT.getModuleName().indexOf("QueryBuilder") != -1) {
			drawTest();
		}
	}

	public void drawTest() {
		DBStructure.getInstance().setURL("test/struct.xml");
		DBStructure.getInstance().load(new GenericCallback<Object>() {
			public void onSuccess(Object result) {
				RootPanel.get().add(new QueryDesigner(new GWTQBQuery()));
			}
			public void onFailure(Throwable caught) {  }
		});


	}

}
