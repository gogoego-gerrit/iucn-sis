package org.iucn.sis.client.panels.redlist;

import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class RedlistPanel extends LayoutContainer implements DrawsLazily {

	public RedlistPanel() {
		super();
		setLayout(new FillLayout());
	}

	public void draw(DoneDrawingCallback callback) {
		removeAll();
		VerticalPanel vp = new VerticalPanel();
		final Button publishImages = new Button("Publish Images to Redlist", new SelectionListener<ButtonEvent>() {
			public void componentSelected(final ButtonEvent ce) {
				ce.getButton().setEnabled(false);
				ce.getButton().setText("Publishing: please wait...");
				
				NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
				doc.put(UriBase.getInstance().getRedlistBase() +"/redlist/publish","", new GenericCallback<String>() {
					public void onSuccess(String result) {
						WindowUtils.infoAlert("Images Published");
						ce.getButton().setEnabled(true);
						ce.getButton().setText("Publish Images to Redlist");
					}

					public void onFailure(Throwable caught) {
						caught.printStackTrace();
						ce.getButton().setEnabled(true);
						ce.getButton().setText("Publish Images to Redlist");
					}
				});
			};
		});
		vp.add(publishImages);
		add(vp);
		callback.isDrawn();
		layout();


	}


}
