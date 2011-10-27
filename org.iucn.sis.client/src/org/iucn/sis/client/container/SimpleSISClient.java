package org.iucn.sis.client.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.iucn.sis.client.api.caches.BookmarkCache;
import org.iucn.sis.client.api.caches.FieldWidgetCache;
import org.iucn.sis.client.api.caches.RecentlyAccessedCache;
import org.iucn.sis.client.api.caches.ViewCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.HasCache;
import org.iucn.sis.client.extensions.birdlife.structures.BirdlifeWidgetGenerator;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.RecentlyAccessed;
import org.iucn.sis.shared.api.structures.WidgetGenerator;
import org.iucn.sis.shared.api.utils.FieldParser;

import com.extjs.gxt.themes.client.Slate;
import com.extjs.gxt.ui.client.util.Theme;
import com.extjs.gxt.ui.client.util.ThemeManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.lwxml.shared.GenericCallback;

import ext.ux.theme.black.client.Black;
import ext.ux.theme.darkgray.client.DarkGray;
import ext.ux.theme.olive.client.Olive;
import ext.ux.theme.purple.client.Purple;
import ext.ux.theme.slickness.client.Slickness;

public class SimpleSISClient extends SISClientBase {
	
	private ClientUIContainer container;
	
	public void loadModule() {
		instance = this;
		
		/*
		 * This must happen before any Ext widget 
		 * is initialized.
		 */
		if (!ThemeManager.getThemes().contains(Slate.SLATE)) {
			ThemeManager.register(Slate.SLATE);
			ThemeManager.register(new Theme(Black.BLACK.getId(), Black.BLACK.getName(), "css/" + Black.BLACK.getFile()));
			ThemeManager.register(new Theme(DarkGray.DARKGRAY.getId(), DarkGray.DARKGRAY.getName(), "css/" + DarkGray.DARKGRAY.getFile()));
			ThemeManager.register(new Theme(Olive.OLIVE.getId(), Olive.OLIVE.getName(), "css/" + Olive.OLIVE.getFile()));
			ThemeManager.register(new Theme(Purple.PURPLE.getId(), Purple.PURPLE.getName(), "css/" + Purple.PURPLE.getFile()));
			ThemeManager.register(new Theme(Slickness.SLICKNESS.getId(), Slickness.SLICKNESS.getName(), "css/" + Slickness.SLICKNESS.getFile()));
		}
		
		Window.addWindowClosingHandler(new Window.ClosingHandler() {
			public void onWindowClosing(ClosingEvent event) {
				if (container.isLoggedIn())
					event.setMessage("This action will close SIS Toolkit - all unsaved changes will be lost.");
			}
		});

		container = new ClientUIContainer();
		container.buildLogin(null);
		
		RootPanel.get().add(container);
		
		//No server contact, just setup
		FieldWidgetCache.impl.setFieldParser(new FieldParser());
		FieldWidgetCache.impl.registerWidgetGenerator(new WidgetGenerator());
		FieldWidgetCache.impl.registerWidgetGenerator(new BirdlifeWidgetGenerator());
	}
	
	@Override
	protected Collection<HasCache> getCachesToInitialize() {
		//This runs async
		RecentlyAccessedCache.impl.load(RecentlyAccessed.USER);
		
		List<HasCache> list = new ArrayList<HasCache>();
		list.add(BookmarkCache.impl);
		
		return list;
	}
	
	@Override
	public void buildLogin(String message) {
		container.buildLogin(message);
	}
	
	@Override
	public void buildPostLogin() {
		container.buildPostLogin(currentUser.getFirstName(), currentUser.getLastName(), currentUser
				.getAffiliation());
	}
	
	@Override
	public void onLogout() {
		ViewCache.impl.doLogout();
	}
	
	@Override
	public void onAssessmentChanged() {
		/*if( AssessmentCache.impl.getCurrentAssessment() != null ) {
			//ClientUIContainer.bodyContainer.tabManager.panelManager.DEM.updateWorkflowStatus();
			ClientUIContainer.headerContainer.assessmentChanged();
		} else
			ClientUIContainer.bodyContainer.tabManager.panelManager.DEM.clearDEM();
		
		ClientUIContainer.bodyContainer.tabManager.panelManager.recentAssessmentsPanel.refresh();*/
	}
	
	@Override
	public void onTaxonChanged() {
		// TODO Auto-generated method stub
		/*ClientUIContainer.bodyContainer.tabManager.taxonHomePage.setAppropriateRights(TaxonomyCache.impl.getCurrentTaxon());
		ClientUIContainer.headerContainer.taxonChanged();*/
	}
	
	@Override
	public void onWorkingSetChanged() {
		// TODO Auto-generated method stub
		//ClientUIContainer.bodyContainer.tabManager.panelManager.DEM.updateWorkflowStatus();
		//ClientUIContainer.headerContainer.workingSetChanged();
	}
	
	/**
	 * @deprecated use ClientUIContainer.bodyContainer.openReferenceManager instead.
	 */
	public void onShowReferenceEditor(String title,
			Referenceable referenceable, GenericCallback<Object> onAddCallback,
			GenericCallback<Object> onRemoveCallback) {

		ClientUIContainer.bodyContainer.openReferenceManager(referenceable, title, onAddCallback, onRemoveCallback);
	}
}
