package org.iucn.sis.client.fieldmanager.container;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.util.gwt.ui.DrawsLazily;

public class FieldManager implements EntryPoint {

	@Override
	public void onModuleLoad() {
		final FieldLoader panel = new FieldLoader();
		panel.draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				RootPanel.get().add(panel);
			}
		});
		
	}

}
