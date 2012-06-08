package org.iucn.sis.client.panels.workingsets;

import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.models.NameValueModelData;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
import org.iucn.sis.client.panels.workingsets.WorkingSetTaxaList.TaxaData;
import org.iucn.sis.client.tabs.WorkingSetPage;
import org.iucn.sis.shared.api.models.TaxonStatus;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class WorkingSetTaxaBatchChangePanel extends RefreshLayoutContainer {

	private final WorkingSetPage parent;

	private WorkingSetTaxaList taxaList;
	private ComboBox<NameValueModelData> status;
	private ListStore<NameValueModelData> statusStore;

	public WorkingSetTaxaBatchChangePanel(WorkingSetPage parent) {
		super();
		this.parent = parent;
		
		addStyleName("gwt-background");
		setScrollMode(Scroll.AUTO);
		
		build();
	}

	private void build() {
		Html instructions = 
			new Html("<b>Instructions:</b> This operation will override the " +
					"current taxon status of the selected Taxa.<br/><br/>");
		
		taxaList = new WorkingSetTaxaList(true);
		taxaList.setFilterVisible(false);
		taxaList.setBorders(true);
		taxaList.setJumpToToolbar();
		
		taxaList.setFilter(TaxaData.FULLNAME);
		
		final LayoutContainer bottomContent = new LayoutContainer();
		bottomContent.setLayout(new RowLayout(Orientation.HORIZONTAL));
		bottomContent.add(taxaList, new RowData(.6f, 1d));
		
		statusStore = new ListStore<NameValueModelData>();
		
		for (Entry<String, String> entry : TaxonStatus.displayableStatus.entrySet())
			statusStore.add(new NameValueModelData(entry.getValue(), entry.getKey()));
		
		status = new ComboBox<NameValueModelData>();
		status.setTriggerAction(TriggerAction.ALL);
		status.setFieldLabel("Status");
		status.setForceSelection(true);
		status.setEditable(false);
		status.setStore(statusStore);
		status.setEmptyText("-- Select --");
		
		FormLayout fmLayout = new FormLayout();
		fmLayout.setLabelPad(20);
		
		FormPanel formPanel = new FormPanel();
		formPanel.setBorders(false);
		formPanel.setBodyBorder(false);
		formPanel.setHeaderVisible(false);
		formPanel.add(status);

		final LayoutContainer content = new LayoutContainer(new RowLayout());
		content.add(instructions, newRowData(1d, -1));
		content.add(formPanel, newRowData(1d, .2));
		content.add(bottomContent, newRowData(1d, .8));
		
		ToolBar buttons = new ToolBar();
		buttons.setSpacing(5);
		buttons.add(newButton("Update Status", "icon-save-and-exit", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				updateTaxaListStatus();
			}
		}));
		buttons.add(newButton("Cancel", "icon-cancel", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				close();
			}
		}));
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(buttons, new BorderLayoutData(LayoutRegion.NORTH, 30, 30, 30));
		container.add(content, new BorderLayoutData(LayoutRegion.CENTER));
		
		setLayout(new FillLayout());
		
		add(container);
	}
	
	private void updateTaxaListStatus() {
		final int count = taxaList.getChecked().size();
		final String selectedStatus = status.getValue() == null ? null : status.getValue().getValue();
		
		if (count <= 0)
			WindowUtils.errorAlert("Please select at least one taxa to update.");
		else if (selectedStatus == null)
			WindowUtils.errorAlert("Please select the taxon status.");
		else {
			List<TaxaData> selectedList = taxaList.getChecked();
			
			StringBuilder builder = new StringBuilder();
			for (TaxaData data : selectedList)
				builder.append(data.getID()+",");
			
			String taxaIDs = builder.toString();
			taxaIDs = taxaIDs.substring(0, taxaIDs.length()-1);
			
			WorkingSetCache.impl.batchUpdateTaxonStatus(taxaIDs, selectedStatus, new GenericCallback<String>(){
				public void onFailure(Throwable caught) {
					WindowUtils.hideLoadingAlert();
					WindowUtils.errorAlert("Error Occurred", "Batch update failed, please try again later.");
				}
				public void onSuccess(String result) {
					String[] resultSet = (result == null || result.equals("")) ? new String[0] : result.split(",");
					
					for (String taxonID : resultSet) 
						TaxonomyCache.impl.getTaxon(Integer.valueOf(taxonID)).setStatus(selectedStatus);
					
					WindowUtils.hideLoadingAlert();
					WindowUtils.infoAlert("Success", "Taxa updated successfully.");
					
					close();
				}
			});	
		}
	}
	
	private void close() {
		status.reset();
		parent.setManagerTab();
	}
	
	@Override
	public void refresh() {
		status.reset();
		taxaList.forcedRefresh();
		layout();
	}

	private Button newButton(String text, String icon, SelectionListener<ButtonEvent> listener) {
		Button button = new Button(text, listener);
		button.setIconStyle(icon);
		return button;
	}
	
	private RowData newRowData(double width, double height) {
		return new RowData(width, height, new Margins(10, 0, 10, 0));
	}
	
}
