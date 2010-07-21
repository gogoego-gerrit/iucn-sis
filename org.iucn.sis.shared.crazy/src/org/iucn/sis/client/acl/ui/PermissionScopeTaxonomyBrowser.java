package org.iucn.sis.client.acl.ui;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.components.panels.TaxonomyBrowserPanel;
import org.iucn.sis.client.components.panels.TaxonomyBrowserPanel.TaxonListElement;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class PermissionScopeTaxonomyBrowser extends ContentPanel {

	private TaxonomyBrowserPanel scopeBrowser;
	private Button select;
	private TaxonNode currentlySelected;
	
	/**
	 * Create a PermissionScopeTaxonomyBrowser Window. You must supply the PermissionEditor this
	 * Window will be used by, so when a scope is chosen it can callback and update the editor.
	 * 
	 * @param editor the editor using this taxonomy browser
	 */
	public PermissionScopeTaxonomyBrowser(final PermissionGroupEditor editor) {
		setLayout(new FitLayout());
		setHeaderVisible(false);
		currentlySelected = null;
		
		scopeBrowser = new TaxonomyBrowserPanel() {
			@Override
			protected void addViewButtonToFootprint() {
				//Don't add anything
			}
			
			@Override
			protected void onChangedTaxon() {
				if( footprints.length > 0 ) {
					String display = footprints[footprints.length - 1];
					updateCurrent(TaxonomyCache.impl.getNode(display));
				} else
					updateCurrent(null);
			}
		};
		scopeBrowser.getBinder().setStyleProvider(new ModelStringProvider<TaxonListElement>() {
			public String getStringValue(TaxonListElement model, String property) {
				if( !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.GRANT, model.getNode()) )
					return "deleted";
				else
					return "";
			}
		});
		
		select = new Button("No Taxon Selected", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				editor.updateScope(currentlySelected);
			}
		});
		select.setEnabled(false);
		
		add(scopeBrowser);
		getButtonBar().add(select);
		
		addListener(Events.Show, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				scopeBrowser.update();
				layout();
			};
		});
	}
	
	protected void updateCurrent(TaxonNode taxon) {
		this.currentlySelected = taxon;
		
		if( taxon == null ) {
			select.setEnabled(false);
			select.setText("No Taxon Selected");
		} else if( !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.GRANT, taxon) ) {
			select.setEnabled(false);
			select.setText(taxon.getFullName());
		} else {
			select.setEnabled(true);
			select.setText("Use " + taxon.getFullName());
		}
	}
}
