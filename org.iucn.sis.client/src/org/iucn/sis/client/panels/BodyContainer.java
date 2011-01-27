package org.iucn.sis.client.panels;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.panels.dem.DEMPanel;
import org.iucn.sis.client.tabs.FeaturedItemContainer;
import org.iucn.sis.client.tabs.HomePageTab;
import org.iucn.sis.client.tabs.TabManager;
import org.iucn.sis.client.tabs.TaxonHomePageTab;
import org.iucn.sis.client.tabs.WorkingSetPage;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class BodyContainer extends LayoutContainer {

	public TabManager tabManager = null;
	
	private LayoutContainer homePage;
	private FeaturedItemContainer<WorkingSet> workingSetPage;
	private FeaturedItemContainer<Taxon> taxonHomePage;
	private FeaturedItemContainer<Assessment> assessmentPage;
	
	private LayoutContainer current;

	public BodyContainer() {
		super(new FillLayout());
		setLayoutOnChange(true);
		addStyleName("gwt-background");
		addStyleName("sis_bodyContainer");

		homePage = new HomePageTab();
		workingSetPage = new WorkingSetPage();
		taxonHomePage = new TaxonHomePageTab();
		assessmentPage = new DEMPanel();
	}
	
	public void openWorkingSet(final boolean updateNavigation) {
		workingSetPage.setItems(new ArrayList<WorkingSet>(WorkingSetCache.impl.getWorkingSets().values()));
		workingSetPage.setSelectedItem(StateManager.impl.getWorkingSet());
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
	
	public void openTaxon(final boolean updateNavigation) {
		final GenericCallback<List<Taxon>> callback = new GenericCallback<List<Taxon>>() {
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}
			public void onSuccess(List<Taxon> result) {
				taxonHomePage.setItems(result);
				taxonHomePage.setSelectedItem(StateManager.impl.getTaxon());
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
		};
		
		WorkingSet ws = StateManager.impl.getWorkingSet();
		if (ws == null)
			callback.onSuccess(new ArrayList<Taxon>(TaxonomyCache.impl.getRecentlyAccessed()));
		else {
			WorkingSetCache.impl.fetchTaxaForWorkingSet(ws, callback);
		}
		
	}
	
	public void openAssessment(final boolean updateNavigation) {
		AssessmentFetchRequest request = new AssessmentFetchRequest(null, StateManager.impl.getTaxon().getId());
		AssessmentCache.impl.fetchAssessments(request, new GenericCallback<String>() {
			public void onSuccess(String result) {
				List<Assessment> assessments = new ArrayList<Assessment>();
				assessments.addAll(AssessmentCache.impl.getDraftAssessmentsForTaxon(StateManager.impl.getTaxon().getId()));
				assessments.addAll(AssessmentCache.impl.getPublishedAssessmentsForTaxon(StateManager.impl.getTaxon().getId()));
				
				assessmentPage.setItems(assessments);
				assessmentPage.setSelectedItem(StateManager.impl.getAssessment());
				
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
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Could not load assessment.");
			}
		});
	}
	
	public void openHomePage(boolean updateNavigation) {
		removeAll();
		
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

}
