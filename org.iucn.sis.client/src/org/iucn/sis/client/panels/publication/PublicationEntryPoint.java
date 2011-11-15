package org.iucn.sis.client.panels.publication;

import java.util.ArrayList;
import java.util.Collection;

import org.iucn.sis.client.api.caches.FieldWidgetCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.HasCache;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.structures.WidgetGenerator;

import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

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
		
		WindowUtils.infoAlert("Hello");
		
		container.removeAll();
		
		
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
