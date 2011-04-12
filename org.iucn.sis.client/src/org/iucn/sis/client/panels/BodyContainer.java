package org.iucn.sis.client.panels;

import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.FieldWidgetCache;
import org.iucn.sis.client.api.caches.AssessmentCache.FetchMode;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.panels.dem.DEMPanel;
import org.iucn.sis.client.panels.references.ReferenceViewTabPanel;
import org.iucn.sis.client.panels.search.SearchQuery;
import org.iucn.sis.client.panels.utils.BasicSearchPanel;
import org.iucn.sis.client.tabs.FeaturedItemContainer;
import org.iucn.sis.client.tabs.HomePageTab;
import org.iucn.sis.client.tabs.TaxonHomePageTab;
import org.iucn.sis.client.tabs.WorkingSetPage;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.Assessment;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class BodyContainer extends LayoutContainer {

	private LayoutContainer homePage;
	private FeaturedItemContainer<Integer> workingSetPage;
	private FeaturedItemContainer<Integer> taxonHomePage;
	private FeaturedItemContainer<Integer> assessmentPage;
	
	private LayoutContainer current;
	
	private ReferenceViewTabPanel refViewPanel;
	private BasicSearchPanel searchPanel;

	public BodyContainer() {
		super(new FillLayout());
		setLayoutOnChange(true);
		addStyleName("gwt-background");
		addStyleName("sis_bodyContainer");

		workingSetPage = new WorkingSetPage();
		taxonHomePage = new TaxonHomePageTab();
		assessmentPage = new DEMPanel();
		
		refViewPanel = new ReferenceViewTabPanel();
		searchPanel = new BasicSearchPanel();
	}
	
	public void openWorkingSet(final String url, final boolean updateNavigation) {
		MonkeyNavigator.getSortedWorkingSetIDs(new ComplexListener<List<Integer>>() {
			public void handleEvent(List<Integer> items) {
				workingSetPage.setUrl(url);
				workingSetPage.setItems(items);
				workingSetPage.setSelectedItem(StateManager.impl.getWorkingSet().getId());
				workingSetPage.draw(new DrawsLazily.DoneDrawingCallback() {
					public void isDrawn() {
						removeAll();
						add(workingSetPage);
						
						current = workingSetPage;
						
						if (updateNavigation)
							updateNavigation();
					}
				});
			}
		});
	}
	
	public void openTaxon(final String url, final boolean updateNavigation) {
		MonkeyNavigator.getSortedTaxaIDs(new ComplexListener<List<Integer>>() {
			public void handleEvent(List<Integer> items) {
				taxonHomePage.setUrl(url);
				taxonHomePage.setItems(items);
				taxonHomePage.setSelectedItem(StateManager.impl.getTaxon().getId());
				taxonHomePage.draw(new DrawsLazily.DoneDrawingCallback() {
					public void isDrawn() {
						removeAll();
						add(taxonHomePage);
						
						current = taxonHomePage;
						
						if (updateNavigation)
							updateNavigation();
					}
				});	
			}
		});
	}
	
	public void openAssessment(final String url, final boolean updateNavigation) {
		FieldWidgetCache.impl.resetWidgetContents();
		
		MonkeyNavigator.getSortedAssessmentIDs(new ComplexListener<List<Integer>>() {
			public void handleEvent(final List<Integer> items) {
				AssessmentCache.impl.fetchAssessment(StateManager.impl.getAssessment().getId(), FetchMode.FULL, new GenericCallback<Assessment>() {
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Could not load this assessment, please try again later.");
					}
					public void onSuccess(Assessment assessment) {
						assessmentPage.setUrl(url);
						assessmentPage.setItems(items);
						assessmentPage.setSelectedItem(StateManager.impl.getAssessment().getId());
						
						assessmentPage.draw(new DrawsLazily.DoneDrawingCallback() {
							public void isDrawn() {
								removeAll();
								add(assessmentPage);
								
								current = assessmentPage;
								
								if (updateNavigation)
									updateNavigation();
							}
						});
					}
				});
			}
		});
	}
	
	public void openHomePage(boolean updateNavigation) {
		removeAll();
		
		if (homePage == null)
			homePage = new HomePageTab();
		add(homePage);
		
		if (updateNavigation)
			updateNavigation();
	}
	
	private void updateNavigation() {
		ClientUIContainer.headerContainer.update();
	}
	
	public void refreshTaxonPage() {
		if (taxonHomePage == current)
			refreshBody();
	}
	
	public void refreshBody() {
		if (workingSetPage == current)
			workingSetPage.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
		else if (taxonHomePage == current)
			taxonHomePage.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
		else if (assessmentPage == current)
			assessmentPage.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
	}
	
	public boolean isAssessmentEditor() {
		return assessmentPage == current;
	}
	
	public void openReferenceManager() {
		openReferenceManager(null);
	}
	
	public void openReferenceManager(Referenceable referenceable) {
		openReferenceManager(referenceable, "Manage References");
	}
	
	public void openReferenceManager(Referenceable referenceable, String windowTitle) {
		openReferenceManager(referenceable, windowTitle, null, null);
	}
	
	public void openReferenceManager(Referenceable referenceable, String windowTitle, GenericCallback<Object> addCallback, GenericCallback<Object> removeCallback) {
		refViewPanel.setReferences(referenceable, addCallback, removeCallback);
		
		final Window s = WindowUtils.getWindow(true, true, windowTitle);
		s.setIconStyle("icon-book");
		s.setLayout(new FitLayout());
		s.add(refViewPanel);
		s.setSize(850, 550);
		s.show();
	}
	
	public void openSearch() {
		openSearch(null, false, false);
	}
	
	public void openSearch(SearchQuery query) {
		openSearch(query, false, true);
	}

	public void openSearch(final SearchQuery query, final boolean advanced, final boolean search) {
		searchPanel.setSearchText("", false);
		
		Window window = WindowUtils.getWindow(true, true, "Search Taxonomy");
		window.addListener(Events.Show, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				DeferredCommand.addCommand(new Command() {
					public void execute() {
						if (advanced)
							searchPanel.setSearchText(query == null ? "" : query.getQuery(), true);
						else if (query != null) {
							searchPanel.setSearchText(query.getQuery(), false);
							if (search)
								searchPanel.search(query);
						}
						else
							searchPanel.setSearchText("", false);
					}
				});
			}
		});
		window.setSize(800, 600);
		window.setLayout(new FillLayout());
		window.add(searchPanel);
		window.show();
	}
}
