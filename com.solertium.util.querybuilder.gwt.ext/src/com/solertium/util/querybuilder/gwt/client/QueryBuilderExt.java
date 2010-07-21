package com.solertium.util.querybuilder.gwt.client;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.struct.DBStructure;

public class QueryBuilderExt implements EntryPoint {

	public QueryBuilderExt() {

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
				final Viewport vp = new Viewport();
				vp.setLayout(new FitLayout());
				
				vp.add(new QueryDesigner(new GWTQBQuery()));
				
				RootPanel.get().add(vp);
				
				vp.layout();
			}
			public void onFailure(Throwable caught) {  }
		});


	}

}
