package org.iucn.sis.client.container;

import org.iucn.sis.client.api.caches.FieldWidgetCache;
import org.iucn.sis.client.api.caches.RecentlyAccessedCache;
import org.iucn.sis.client.api.caches.ViewCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.container.StateChangeEvent;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.api.container.StateManager.StateChangeEventType;
import org.iucn.sis.client.extensions.birdlife.structures.BirdlifeWidgetGenerator;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.MonkeyNavigator;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.RecentlyAccessed;
import org.iucn.sis.shared.api.structures.WidgetGenerator;
import org.iucn.sis.shared.api.utils.FieldParser;

import com.extjs.gxt.themes.client.Slate;
import com.extjs.gxt.ui.client.util.Theme;
import com.extjs.gxt.ui.client.util.ThemeManager;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;

import ext.ux.theme.black.client.Black;
import ext.ux.theme.darkgray.client.DarkGray;
import ext.ux.theme.olive.client.Olive;
import ext.ux.theme.purple.client.Purple;
import ext.ux.theme.slickness.client.Slickness;

public class SimpleSISClient extends SISClientBase {
	
	public static ClientUIContainer clientContainer;
	
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

		clientContainer = new ClientUIContainer();
		
		
		Window.addWindowClosingHandler(new Window.ClosingHandler() {
			public void onWindowClosing(ClosingEvent event) {
				if (clientContainer.isLoggedIn())
					event.setMessage("This action will close SIS Toolkit - all unsaved changes will be lost.");
			}
		});

		RootPanel.get().add(clientContainer);
	}
	
	@Override
	protected void initializeCaches(SimpleListener listener) {
		FieldWidgetCache.impl.setFieldParser(new FieldParser());
		FieldWidgetCache.impl.registerWidgetGenerator(new WidgetGenerator());
		FieldWidgetCache.impl.registerWidgetGenerator(new BirdlifeWidgetGenerator());
		RecentlyAccessedCache.impl.load(RecentlyAccessed.USER);
		
		/*StateManager.impl.addStateChangeListener(StateChangeEventType.WorkingSetChanged, new ComplexListener<StateChangeEvent>() {
			public void handleEvent(StateChangeEvent event) {
				ClientUIContainer.bodyContainer.openWorkingSet();
			}
		});
		StateManager.impl.addStateChangeListener(StateChangeEventType.TaxonChanged, new ComplexListener<StateChangeEvent>() {
			public void handleEvent(StateChangeEvent eventData) {
				ClientUIContainer.bodyContainer.openTaxon();
			}
		});
		StateManager.impl.addStateChangeListener(StateChangeEventType.AssessmentChanged, new ComplexListener<StateChangeEvent>() {
			public void handleEvent(StateChangeEvent eventData) {
				ClientUIContainer.bodyContainer.openAssessment();
			}
		});*/
		StateManager.impl.addStateChangeListener(StateChangeEventType.StateChanged, new ComplexListener<StateChangeEvent>() {
			public void handleEvent(StateChangeEvent eventData) {
				boolean updateNavigation = !ClientUIContainer.headerContainer.centerPanel.equals(eventData.getSource());
				if (eventData.getAssessment() != null)
					ClientUIContainer.bodyContainer.openAssessment(updateNavigation);
				else if (eventData.getTaxon() != null)
					ClientUIContainer.bodyContainer.openTaxon(updateNavigation);
				else if (eventData.getWorkingSet() != null)
					ClientUIContainer.bodyContainer.openWorkingSet(updateNavigation);
				else
					ClientUIContainer.bodyContainer.openHomePage(true);
			}
		});
		
		super.initializeCaches(listener);
	}
	
	@Override
	public void buildLogin(String message) {
		clientContainer.buildLogin(message);
	}
	
	@Override
	public void buildPostLogin() {
		clientContainer.buildPostLogin(currentUser.getFirstName(), currentUser.getLastName(), currentUser
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
	
	@Override
	public void onShowReferenceEditor(String title,
			Referenceable referenceable, GenericCallback<Object> onAddCallback,
			GenericCallback<Object> onRemoveCallback) {

		ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel.setReferences(referenceable, onAddCallback, onRemoveCallback);
		/*ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel.setOnAddSelected(onAddCallback);
		ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel.setOnRemoveSelected(onRemoveCallback);*/

		com.extjs.gxt.ui.client.widget.Window s = WindowUtils.getWindow(false, true, title);
		s.setLayout(new FillLayout());
		s.setIconStyle("icon-book");
		s.add(ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel);
		s.setSize(850, 550);
		s.show();
		s.center();
	}
}
