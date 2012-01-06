package org.iucn.sis.client.tabs.home;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.panels.utils.RefreshPortlet;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;

public class WhatsNewPortlet extends RefreshPortlet {
	
	public WhatsNewPortlet() {
		super("x-panel");
		setHeading("SIS Updates");
		setLayout(new FlowLayout());
		setLayoutOnChange(true);
		setHeight(120);
		setScrollMode(Scroll.AUTO);
		
		configureThisAsPortlet();
		
		refresh();
	}

	@Override
	public void refresh() {
		removeAll();
		
		final String uri = "/updates.html";
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.getAsText(uri, new GenericCallback<String>() {
			public void onSuccess(String result) {
				setUrl(uri);
			}
			public void onFailure(Throwable caught) {
				add(new Html("No updates found."));
			}
		});
	}

}
