package org.iucn.sis.shared.api.integrity;

import org.iucn.sis.client.api.panels.integrity.IntegrityApplicationPanel;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.util.gwt.ui.DrawsLazily;

/**
 * IntegrityEntryPoint.java
 * 
 * The entry point of the application. Note the DrawsLazily.s
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public class IntegrityEntryPoint implements EntryPoint {

	public void onModuleLoad() {
		final IntegrityApplicationPanel p = new IntegrityApplicationPanel();
		p.draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				final Viewport vp = new Viewport();
				vp.setLayout(new FitLayout());

				vp.add(p);

				RootPanel.get().add(vp);
			}
		});
	}

}
