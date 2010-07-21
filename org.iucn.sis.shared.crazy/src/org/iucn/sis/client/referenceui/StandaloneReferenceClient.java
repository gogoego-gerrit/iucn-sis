package org.iucn.sis.client.referenceui;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.core.client.EntryPoint;

public class StandaloneReferenceClient implements EntryPoint {
	public void onModuleLoad() {
		final Viewport vp = new Viewport();
		vp.setLayout(new FillLayout());

		ReferenceViewPanel rvp = new ReferenceViewPanel();
		vp.add(rvp);
		vp.layout();
	}

}
