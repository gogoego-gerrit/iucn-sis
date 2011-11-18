package org.iucn.sis.client.panels.publication;

import java.util.ArrayList;
import java.util.Collection;

import org.iucn.sis.client.api.caches.FieldWidgetCache;
import org.iucn.sis.client.api.caches.PublicationCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.HasCache;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.structures.WidgetGenerator;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class PublicationEntryPoint extends SISClientBase {
	
	private ClientUIContainer container;

	@Override
	public void loadModule() {
		instance = this;
		
		FieldWidgetCache.impl.registerWidgetGenerator(new WidgetGenerator());
		
		String u = com.google.gwt.user.client.Window.Location.getParameter("u");
		if ("".equals(u))
			u = null;
		String p = com.google.gwt.user.client.Window.Location.getParameter("p");
		if ("".equals(p))
			p = null;
		
		container = new ClientUIContainer();
		
		RootPanel.get().add(container);
		
		if (u != null && p != null)
			SimpleSupport.doLogin(u, p);
		else
			container.buildLogin(null);
	}
	
	@Override
	public void buildLogin(String message) {
		container.buildLogin(message);
	}

	@Override
	public void buildPostLogin() {
		WindowUtils.hideLoadingAlert();
		
		container.removeAll();
		
		final PublicationPanel panel = new PublicationPanel();
		panel.draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				Window window = WindowUtils.newWindow("Publication");
				window.setSize(800, 600);
				window.setLayout(new FillLayout());		
				window.add(panel);
				window.addButton(new Button("Submit Assessment", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						Assessment assessment = new Assessment();
						assessment.setId(336660);
						PublicationCache.impl.submit(assessment, new GenericCallback<Object>() {
							public void onSuccess(Object result) {
								WindowUtils.infoAlert("Submitted assessment.");
							}
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Did not submit assessment.");
							}
						});
					}
				}));
				window.show();
			}
		});
	}

	@Override
	protected Collection<HasCache> getCachesToInitialize() {
		return new ArrayList<HasCache>();
	}

	@Override
	public void onAssessmentChanged() {
		notImplemented();
	}

	@Override
	public void onLogout() {
		notImplemented();
	}

	@Override
	public void onShowReferenceEditor(String title,
			Referenceable referenceable, GenericCallback<Object> onAddCallback,
			GenericCallback<Object> onRemoveCallback) {
		notImplemented();
	}

	@Override
	public void onTaxonChanged() {
		notImplemented();
	}

	@Override
	public void onWorkingSetChanged() {
		notImplemented();
	}
	
	private void notImplemented() {
		WindowUtils.infoAlert("This feature is not implemented.");
	}

}
