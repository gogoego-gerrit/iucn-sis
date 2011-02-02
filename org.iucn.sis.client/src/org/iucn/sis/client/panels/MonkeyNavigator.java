package org.iucn.sis.client.panels;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.util.gwt.ui.DrawsLazily;

public class MonkeyNavigator extends LayoutContainer implements DrawsLazily {
	
	private final WorkingSetMonkeyNavigatorPanel workingSetContainer;
	private final TaxonMonkeyNavigatorPanel taxonContainer;
	private final AssessmentMonkeyNavigatorPanel assessmentContainer;
	
	private WorkingSet curNavWorkingSet;
	private Taxon curNavTaxon;
	private Assessment curNavAssessment;
	
	private boolean isDrawn;
	
	public MonkeyNavigator() {
		super();
		setBorders(false);
		setStyleName("navigator");
		setLayout(new FillLayout());
		setLayoutOnChange(true);
		
		isDrawn = false;
		
		workingSetContainer = new WorkingSetMonkeyNavigatorPanel();
		taxonContainer = new TaxonMonkeyNavigatorPanel();
		assessmentContainer = new AssessmentMonkeyNavigatorPanel();
	}
	
	
	public void draw(final DrawsLazily.DoneDrawingCallback callback) {
		curNavWorkingSet = WorkingSetCache.impl.getCurrentWorkingSet();
		curNavTaxon = TaxonomyCache.impl.getCurrentTaxon();
		curNavAssessment = AssessmentCache.impl.getCurrentAssessment();
		
		workingSetContainer.refresh(curNavWorkingSet);
		taxonContainer.refresh(curNavWorkingSet, curNavTaxon);
		assessmentContainer.refresh(curNavWorkingSet, curNavTaxon, curNavAssessment);
		
		if (!isDrawn) {
			addListeners();
						
			final LayoutContainer container = new LayoutContainer(new BorderLayout());
			container.add(workingSetContainer, new BorderLayoutData(LayoutRegion.WEST, .30f, 5, 4000));
			container.add(taxonContainer, new BorderLayoutData(LayoutRegion.CENTER, .37f, 5, 4000));
			container.add(assessmentContainer, new BorderLayoutData(LayoutRegion.EAST, .33f, 5, 4000));
						
			removeAll();
			add(container);
		}
						
		isDrawn = true;
						
		callback.isDrawn();
	}
	
	public void addListeners() {	
		workingSetContainer.addListener(Events.SelectionChange, new Listener<NavigationChangeEvent<WorkingSet>>() {
			public void handleEvent(NavigationChangeEvent<WorkingSet> be) {
				curNavWorkingSet = be.getModel();
				curNavTaxon = null;
				curNavAssessment = null;
				
				taxonContainer.refresh(curNavWorkingSet, curNavTaxon);
				assessmentContainer.refresh(curNavWorkingSet, curNavTaxon, curNavAssessment);
			}
		});
		
		taxonContainer.addListener(Events.SelectionChange, new Listener<NavigationChangeEvent<Taxon>>() {
			public void handleEvent(NavigationChangeEvent<Taxon> be) {
				if (be.getModel() == null)
					return;
				
				curNavTaxon = be.getModel();
				curNavAssessment = null;
				
				assessmentContainer.refresh(curNavWorkingSet, curNavTaxon, curNavAssessment);
			}
		});	
	}
	
	public static class NavigationChangeEvent<T> extends BaseEvent {
	
		private T model;
		
		public NavigationChangeEvent(T model) {
			super(null);
			this.model = model;
		}
		
		public T getModel() {
			return model;
		}
		
	}

}
