package org.iucn.sis.client.panels.workingsets;

import java.util.Collection;

import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.panels.search.SearchQuery;
import org.iucn.sis.client.panels.search.SearchResultPage;
import org.iucn.sis.client.panels.search.WorkingSetSearchResultsPage;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
import org.iucn.sis.client.panels.utils.SearchPanel;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * Panel that allows users to add taxon to their working set
 * 
 * Events:
 * 
 * Change - fired when taxa are added.
 */
public class WorkingSetAddTaxaSearchPanel extends RefreshLayoutContainer {

	class MySearchPanel extends SearchPanel {

		public MySearchPanel() {
			super();
		}

		public void updateTable() {
			if (resultsPage != null)
				resultsPage.removeFromParent();
			resetSearchBox();
		}
		
		@Override
		protected SearchResultPage createSearchResultsPage(SearchQuery query) {
			return new WorkingSetSearchResultsPage(workingSet, query);
		}
		
		public void getSelection(ComplexListener<Collection<Taxon>> callback) {
			((WorkingSetSearchResultsPage)resultsPage).loadSelection(callback);
		}

	}

	private HorizontalPanel buttons;
	private HTML instruct;
	private WorkingSet workingSet;
	private MySearchPanel searchPanel;
	private BorderLayoutData north;

	private BorderLayoutData center;

	public WorkingSetAddTaxaSearchPanel() {
		super();
		north = new BorderLayoutData(LayoutRegion.NORTH, 70);
		center = new BorderLayoutData(LayoutRegion.CENTER);
		setLayout(new BorderLayout());
		addInstructions();
		searchPanel = new MySearchPanel();
		add(searchPanel, center);

	}

	private void addInstructions() {
		VerticalPanel instructions = new VerticalPanel();

		instruct = new HTML();
		instruct.setWordWrap(true);

		buttons = new HorizontalPanel();
		buttons.setSpacing(2);

		final Button add = new Button("Add");
		add.addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(final ButtonEvent ce) {
				add.setEnabled(false);
				
				searchPanel.getSelection(new ComplexListener<Collection<Taxon>>() {
					public void handleEvent(Collection<Taxon> taxaToAdd) {
						// NEED TO ADD THINGS TO WORKINGSET
						if (!taxaToAdd.isEmpty()) {
							WorkingSetCache.impl.editTaxaInWorkingSet(workingSet, taxaToAdd, null, new GenericCallback<String>() {
								@Override
								public void onSuccess(String result) {
									WindowUtils.infoAlert("Taxon successfully added " + "to working set "
											+ workingSet.getName());
									add.setEnabled(true);
									fireEvent(Events.Change);
								}
							
								@Override
								public void onFailure(Throwable caught) {
									WindowUtils.errorAlert("Error adding taxon to " + "working set " + workingSet.getName());
									add.setEnabled(true);
							
								}
							});
							
						}

						// NOTHING TO ADD
						else {
							add.setEnabled(true);
							WindowUtils.errorAlert("No taxon to add to working set " + workingSet.getWorkingSetName());
						}
					}
				});

			}
		});

		buttons.add(add);

		instructions.add(instruct);
		instructions.add(buttons);
		add(instructions, north);
	}

	@Override
	public void refresh() {
		workingSet = WorkingSetCache.impl.getCurrentWorkingSet();
		searchPanel.updateTable();
		if (workingSet != null) {
			instruct.setHTML("<b>Instructions:</b> Search for taxa to add to the " + workingSet.getWorkingSetName()
					+ " working set.  Notice: pressing select all will only select taxa currently in the table.");
		} else {
			instruct.setHTML("<b>Instructions:</b> Please select a working set to add taxa to.");
		}
		layout();

	}

}
