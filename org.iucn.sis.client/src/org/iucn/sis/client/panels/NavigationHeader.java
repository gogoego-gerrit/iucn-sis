package org.iucn.sis.client.panels;

import java.util.Iterator;
import java.util.Set;

import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.MarkedCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;

public class NavigationHeader extends LayoutContainer {

	private ToolBar bar = null;

	private Button nextTaxa;
	private Button prevTaxa;
	private Button currentTaxa;
	
	private HeaderSummaryPanel summaryPanel;

	private Button monkeyToolItem;

	public NavigationHeader() {
		setBorders(true);
		setLayout(new RowLayout(Orientation.VERTICAL));
		MarkedCache.impl.update();

		bar = new ToolBar();
		bar.setHeight(25);

		monkeyToolItem = new Button();
		monkeyToolItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				update();
				
				new MonkeyNavigator().show();
				/*if (!navigator.isRendered())
					update();

				refreshSets(new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}

					public void onSuccess(String result) {
						setListSelected();
						
						monkeyToolItem.fireEvent(Events.OnMouseOut);

						// navPopup.showAt(getAbsoluteLeft() - 30, getAbsoluteTop());
						navPopup.showAt(10, 0);
						// navPopup.setSize(getHeaderWidth() + 60, NAVIGATOR_SIZE);
						// navigator.setSize(getHeaderWidth() + 60, NAVIGATOR_SIZE);
						navPopup.setSize(Window.getClientWidth() - 20, NAVIGATOR_SIZE);
						navigator.setSize(Window.getClientWidth() - 20, NAVIGATOR_SIZE);
						navigator.layout();
						navPopup.layout();
					}
				});*/
			};
		});

		monkeyToolItem.setText("Navigate");
		monkeyToolItem.setIconStyle("icon-monkey-face");
		bar.add(monkeyToolItem);

		nextTaxa = new Button();
		nextTaxa.setIconStyle("icon-arrow-right");
		nextTaxa.setToolTip("Next Taxon");
		nextTaxa.addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				AssessmentClientSaveUtils.saveIfNecessary(new SimpleListener() {
					public void handleEvent() {
						//doMoveNext();
					}
				});
			}
		});

		currentTaxa = new Button();
		currentTaxa.setText("Quick Taxon Navigation");
		currentTaxa.setToolTip(new ToolTipConfig("Quick Taxon Navigation",
				"These arrows will allow you to navigate,<br>" + "in order, through the taxa in your current<br>"
				+ "working set, selecting the global draft assessment<br>" + "by default, should one exist."));
		currentTaxa.addStyleName("bold");

		prevTaxa = new Button();
		prevTaxa.setIconStyle("icon-arrow-left");
		prevTaxa.setToolTip("Previous Taxon");
		prevTaxa.addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				AssessmentClientSaveUtils.saveIfNecessary(new SimpleListener() {
					public void handleEvent() {
						//doMovePrev();
					}
				});
			}
		});

		bar.add(new SeparatorToolItem());

		Button blah = new Button();
		blah.setText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		blah.setEnabled(false);
		bar.add(blah);

		bar.add(prevTaxa);
		bar.add(currentTaxa);
		bar.add(nextTaxa);

		summaryPanel = new HeaderSummaryPanel();
		summaryPanel.setHeight(175);
		
		add(bar, new RowData(1, 25));
		add(summaryPanel, new RowData(1, .8));

		layout();
	}

	/*private void doMoveNext() {
		final WorkingSet curSelectedWorkingSet = WorkingSetCache.impl.getCurrentWorkingSet();
		
		if (curSelectedWorkingSet == null)
			Info.display(new InfoConfig("No Working Set", "Please select a working set."));
		else if (curSelectedWorkingSet.getSpeciesIDs().isEmpty())
			Info.display(new InfoConfig("Empty Working Set", "Your current working set {0} contains no species.",
					new Params(curSelectedWorkingSet.getWorkingSetName())));
		else if (TaxonomyCache.impl.getCurrentTaxon() == null)
			fetchTaxon(curSelectedWorkingSet.getSpeciesIDs().get(0));
		else {
			Integer curID = TaxonomyCache.impl.getCurrentTaxon().getId();
			boolean found = false;
			for (Iterator<Integer> iter = curSelectedWorkingSet.getSpeciesIDs().listIterator(); iter.hasNext() && !found;)
				if (found = iter.next().equals(curID))
					fetchTaxon(iter.hasNext()? iter.next() : curSelectedWorkingSet.getSpeciesIDs().get(0));
		}
	}
	
	private void fetchTaxon(final int speciesID) {
		TaxonomyCache.impl.fetchTaxon(speciesID, true, false, new GenericCallback<Taxon>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Could not fetch species, please try again later.");
			}
			public void onSuccess(Taxon  result) {
				AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, speciesID), new GenericCallback<String>() {
					public void onFailure(Throwable caught) {};
					public void onSuccess(String result) {
						if (AssessmentCache.impl.getDraftAssessment(speciesID, false) != null) {
							AssessmentCache.impl.getDraftAssessment(speciesID, true);
						} else {
							Set<Assessment> draftAssessments = 
								AssessmentCache.impl.getDraftAssessmentsForTaxon(speciesID);
							if (!draftAssessments.isEmpty())
								AssessmentCache.impl.setCurrentAssessment(draftAssessments.iterator().next(), false);
						}
						ClientUIContainer.bodyContainer.refreshBody();
					}
				});
			}
		});
	}

	private void doMovePrev() {
		final WorkingSet curSelectedWorkingSet = WorkingSetCache.impl.getCurrentWorkingSet();
		
		if (curSelectedWorkingSet == null)
			Info.display(new InfoConfig("No Working Set", "Please select a working set."));
		else if (curSelectedWorkingSet.getSpeciesIDs().isEmpty())
			Info.display(new InfoConfig("Empty Working Set", "Your current working set {0} contains no species.",
					new Params(curSelectedWorkingSet.getWorkingSetName())));
		else if (TaxonomyCache.impl.getCurrentTaxon() == null)
			fetchTaxon(curSelectedWorkingSet.getSpeciesIDs().get(0));
		else {
			Integer curID = TaxonomyCache.impl.getCurrentTaxon().getId();

			// Initialize to the end of the list, in case the first item is
			// current
			Integer newCurrent = 
				curSelectedWorkingSet.getSpeciesIDs().get(curSelectedWorkingSet.getSpeciesIDs().size() - 1);

			for (Iterator<Integer> iter = curSelectedWorkingSet.getSpeciesIDs().listIterator(); iter.hasNext();) {
				Integer cur = iter.next();
				if (cur.equals(curID))
					break;
				else
					newCurrent = cur;
			}

			fetchTaxon(newCurrent);
		}
	}*/

	/*private void setListSelected() {
		taxonListBinder.removeAllListeners();

		if (setToSelect != -1 && setToSelect < workingSetList.getItemCount() && workingSetList.isRendered())
			workingSetList.getSelectionModel().select(setToSelect, false);
		else
			workingSetList.getSelectionModel().select(0, false);

		if (taxonToSelect != null && taxonList.isRendered()) {
			if (taxonToSelect instanceof DataListItem)
				taxonList.getSelectionModel().select((DataListItem) taxonToSelect, false);
			else if (taxonToSelect instanceof TaxonListElement) {
				int elIndex = taxonPagingLoader.getFullList().indexOf((TaxonListElement) taxonToSelect);
				int activePage = ((elIndex + 1) / taxonPagingToolBar.getPageSize()) + 1;
				taxonPagingToolBar.setActivePage(activePage);

				DataListItem item = (DataListItem) taxonListBinder.findItem((TaxonListElement) taxonToSelect);
				taxonList.scrollIntoView(item);
				taxonList.getSelectionModel().select(item, false);
			}
		} else
			taxonList.getSelectionModel().deselectAll();

		if (assessmentToSelect != -1 && assessmentToSelect < assessmentList.getItemCount()
				&& assessmentList.isRendered())
			assessmentList.getSelectionModel().select(assessmentToSelect, false);
		else
			assessmentList.getSelectionModel().deselectAll();

		taxonListBinder.addSelectionChangedListener(new SelectionChangedListener<TaxonListElement>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<TaxonListElement> se) {
				if (se.getSelectedItem() != null && se.getSelectedItem().getNode() != null) {
					curNavTaxon = se.getSelectedItem().getNode();
					curNavAssessment = null;

					refreshAssessments(null);
				}
			}
		});
	}*/

	public void update() {
		/*curSelectedWorkingSet = WorkingSetCache.impl.getCurrentWorkingSet();
		curNavTaxon = TaxonomyCache.impl.getCurrentTaxon();
		curNavAssessment = AssessmentCache.impl.getCurrentAssessment();

		// refreshSets();
		if (curSelectedWorkingSet == null) {
			// nextTaxa.setEnabled(false);
			// prevTaxa.setEnabled(false);
			currentTaxa.setText("Quick Taxon Navigation");
		} else {
			nextTaxa.setEnabled(true);
			prevTaxa.setEnabled(true);
			currentTaxa.setText(curNavTaxon == null ? "Quick Taxon Navigation" : curNavTaxon.getFullName());
		}*/

		summaryPanel.update();
		ClientUIContainer.bodyContainer.tabManager.update();
	}
}
