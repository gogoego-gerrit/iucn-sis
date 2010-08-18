package org.iucn.sis.client.panels.references;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class StandaloneReferenceClient implements EntryPoint {
	public void onModuleLoad() {
		final Viewport vp = new Viewport();
		vp.setLayout(new FillLayout());

		//ReferenceViewPanel rvp = new ReferenceViewPanel();
		ReferenceViewTabPanel rvp = new ReferenceViewTabPanel();
		vp.add(rvp);
		vp.layout();
		
		RootPanel.get().add(vp);
	}

}
