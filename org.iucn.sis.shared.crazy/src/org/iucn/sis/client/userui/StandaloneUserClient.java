package org.iucn.sis.client.userui;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class StandaloneUserClient implements EntryPoint {
	public void onModuleLoad() {
		final Viewport vp = new Viewport();
		vp.setLayout(new FillLayout());
		vp.setLoadingPanelId("loading");

		RootPanel.get().add(vp);

		vp.add(new UserModelTabPanel());
		vp.layout();
	}

}
