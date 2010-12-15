package org.iucn.sis.client.container;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.FieldWidgetCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.ViewCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.extensions.birdlife.structures.BirdlifeWidgetGenerator;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.structures.WidgetGenerator;
import org.iucn.sis.shared.api.utils.FieldParser;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class SimpleSISClient extends SISClientBase {
	
	public static ClientUIContainer clientContainer;
	
	public void loadModule() {
		instance = this;

		clientContainer = new ClientUIContainer();

		RootPanel.get().add(clientContainer);
	}
	
	@Override
	protected void initializeCaches() {
		FieldWidgetCache.impl.setFieldParser(new FieldParser());
		FieldWidgetCache.impl.registerWidgetGenerator(new WidgetGenerator());
		FieldWidgetCache.impl.registerWidgetGenerator(new BirdlifeWidgetGenerator());
		super.initializeCaches();
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
		if( AssessmentCache.impl.getCurrentAssessment() != null ) {
			//ClientUIContainer.bodyContainer.tabManager.panelManager.DEM.updateWorkflowStatus();
			ClientUIContainer.headerContainer.assessmentChanged();
		} else
			ClientUIContainer.bodyContainer.tabManager.panelManager.DEM.clearDEM();
		
		ClientUIContainer.bodyContainer.tabManager.panelManager.recentAssessmentsPanel.refresh();
	}
	
	@Override
	public void onTaxonChanged() {
		// TODO Auto-generated method stub
		ClientUIContainer.bodyContainer.tabManager.taxonHomePage.setAppropriateRights(TaxonomyCache.impl.getCurrentTaxon());
		ClientUIContainer.headerContainer.taxonChanged();
	}
	
	@Override
	public void onWorkingSetChanged() {
		// TODO Auto-generated method stub
		//ClientUIContainer.bodyContainer.tabManager.panelManager.DEM.updateWorkflowStatus();
		ClientUIContainer.headerContainer.workingSetChanged();
	}
	
	@Override
	public void onShowReferenceEditor(String title,
			Referenceable referenceable, GenericCallback<Object> onAddCallback,
			GenericCallback<Object> onRemoveCallback) {

		ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel.setReferences(referenceable, onAddCallback, onRemoveCallback);
		/*ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel.setOnAddSelected(onAddCallback);
		ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel.setOnRemoveSelected(onRemoveCallback);*/

		Window s = WindowUtils.getWindow(false, true, title);
		s.setLayout(new FillLayout());
		s.setIconStyle("icon-book");
		s.add(ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel);
		s.setSize(850, 550);
		s.show();
		s.center();
	}
}
