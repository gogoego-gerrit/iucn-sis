package org.iucn.sis.client.panels;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.MarkedCache;
import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.utils.AssessmentFormatter;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class AssessmentMonkeyNavigatorPanel extends GridPagingMonkeyNavigatorPanel<Assessment> {
	
	private WorkingSet curNavWorkingSet;
	private Taxon curNavTaxon;
	private Assessment curNavAssessment;
	
	public AssessmentMonkeyNavigatorPanel() {
		super();
		
		setHeading("Assessments");
	}
	
	public void refresh(WorkingSet curNavWorkingSet, Taxon curNavTaxon, Assessment curNavAssessment) {
		this.curNavWorkingSet = curNavWorkingSet;
		this.curNavTaxon = curNavTaxon;
		this.curNavAssessment = curNavAssessment;
		
		refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
	}
	
	@Override
	protected GridView getView() {
		GroupingView view = new GroupingView();
		view.setEmptyText("No assessments for this taxon");
		view.setGroupRenderer(new GridGroupRenderer() {
			public String render(GroupColumnData data) {
				if ("draft".equals(data.group))
					return "Draft Assessments";
				else
					return "Published Assessments";
			}
		});
		
		return view;
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		
		final NavigationModelData<Assessment> selection;
		if (curNavAssessment != null)
			selection = getProxy().getStore().findModel("" + curNavAssessment.getId());
		else
			selection = null;
		
		Debug.println("Selected assessment from nav is {0}, found {1}", curNavAssessment, selection);
		if (selection != null) {
			((NavigationGridSelectionModel<Assessment>)grid.getSelectionModel()).
				highlight(selection);
			
			DeferredCommand.addPause();
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					grid.getView().focusRow(grid.getStore().indexOf(selection));
				}
			});
		}
	}
	
	@Override
	protected ListStore<NavigationModelData<Assessment>> getStoreInstance() {
		GroupingStore<NavigationModelData<Assessment>> store = 
			new GroupingStore<NavigationModelData<Assessment>>(getLoader());
		store.groupBy("status");
		
		return store;
	}
	
	@Override
	protected ColumnModel getColumnModel() {
		final List<ColumnConfig> list = new ArrayList<ColumnConfig>();
		
		/**
		 * TODO: add an icon column; if this assessment is locked, 
		 * display the lock icon.
		 */
		
		list.add(new ColumnConfig("name", "Name", 100));
		list.add(new ColumnConfig("status", "Status", 10));
		
		return new ColumnModel(list);
	}
	
	@Override
	protected void getStore(final GenericCallback<ListStore<org.iucn.sis.client.panels.PagingMonkeyNavigatorPanel.NavigationModelData<Assessment>>> callback) {
		final GroupingStore<NavigationModelData<Assessment>> store = 
			new GroupingStore<NavigationModelData<Assessment>>();
		store.setStoreSorter(new AssessmentStoreSorter());
		store.setKeyProvider(new ModelKeyProvider<NavigationModelData<Assessment>>() {
			public String getKey(NavigationModelData<Assessment> model) {
				if (model.getModel() == null)
					return "-1";
				else
					return Integer.toString(model.getModel().getId());
			}
		});
		
		if (curNavTaxon == null)
			callback.onSuccess(store);
		else if (curNavWorkingSet == null) {
			AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, curNavTaxon.getId()), new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					callback.onSuccess(store);
				}
				public void onSuccess(String result) {
					// TODO Auto-generated method stub
					for (Assessment current : AssessmentCache.impl.getDraftAssessmentsForTaxon(curNavTaxon.getId())) {
						String displayable;
						if (current.getDateAssessed() != null )
							displayable = FormattedDate.impl.getDate();
						else
							displayable = "";
						
						NavigationModelData<Assessment> model = new NavigationModelData<Assessment>(current);
						
						if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, 
								current)) {
							if (displayable == null || displayable.equals(""))
								if (current.getLastEdit() == null)
									displayable = "New";
								else
									displayable = FormattedDate.impl.getDate(current.getLastEdit().getCreatedDate());

							if (current.isRegional())
								displayable += " --- " + RegionCache.impl.getRegionName(current.getRegionIDs());
							else
								displayable += " --- " + "Global";
							
							model.set("name", displayable);
							model.set("locked", Boolean.FALSE);
							model.set("status", "draft");
						} else {
							if (current.isRegional())
								model.set("name", "Regional Draft Assessment");
							else
								model.set("name", "Global Draft Assessment");
							model.set("locked", Boolean.TRUE);
							model.set("status", "draft");
						}
						
						store.add(model);
					}
					
					for (Assessment current : AssessmentCache.impl.getPublishedAssessmentsForTaxon(curNavTaxon.getId())) {
						if (current == null)
							continue;
						
						NavigationModelData<Assessment> model = new NavigationModelData<Assessment>(current);
						
						String displayable;
						if (current.getDateAssessed() != null)
							displayable = FormattedDate.impl.getDate(current.getDateAssessed());
						else
							displayable = "";

						if (current.isRegional())
							displayable += " --- " + RegionCache.impl.getRegionName(current.getRegionIDs());
						else
							displayable += " --- " + "Global";

						displayable += " --- " + AssessmentFormatter.getProperCategoryAbbreviation(current);

						model.set("name", displayable);
						model.set("type", "published");

						store.add(model);
					}
					store.sort("name", SortDir.DESC);
					callback.onSuccess(store);
				}
			});
		}
		else {
			WorkingSetCache.impl.getAssessmentsForWorkingSet(curNavWorkingSet, curNavTaxon, new GenericCallback<List<Assessment>>() {
				public void onSuccess(List<Assessment> result) {
					for (Assessment current : result) {
						NavigationModelData<Assessment> model = new NavigationModelData<Assessment>(current);
						if (AssessmentType.DRAFT_ASSESSMENT_STATUS_ID == (current.getAssessmentType().getId())) {
							String displayable;
							if (current.getDateAssessed() != null )
								displayable = FormattedDate.impl.getDate();
							else
								displayable = "";
							
							if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, 
									current)) {
								if (displayable == null || displayable.equals(""))
									if (current.getLastEdit() == null)
										displayable = "New";
									else
										displayable = FormattedDate.impl.getDate(current.getLastEdit().getCreatedDate());

								if (current.isRegional())
									displayable += " --- " + RegionCache.impl.getRegionName(current.getRegionIDs());
								else
									displayable += " --- " + "Global";
								
								model.set("name", displayable);
								model.set("locked", Boolean.FALSE);
								model.set("status", "draft");
							} else {
								if (current.isRegional())
									model.set("name", "Regional Draft Assessment");
								else
									model.set("name", "Global Draft Assessment");
								model.set("locked", Boolean.TRUE);
								model.set("status", "draft");
							}
						}
						else {
							String displayable;
							if (current.getDateAssessed() != null)
								displayable = FormattedDate.impl.getDate(current.getDateAssessed());
							else
								displayable = "";

							if (current.isRegional())
								displayable += " --- " + RegionCache.impl.getRegionName(current.getRegionIDs());
							else
								displayable += " --- " + "Global";

							displayable += " --- " + AssessmentFormatter.getProperCategoryAbbreviation(current);

							model.set("name", displayable);
							model.set("type", "published");

							store.add(model);
						}
						
						store.add(model);
						callback.onSuccess(store);
					}
				}
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Could not load assessments for the taxon in this working set.");
				}
			});
		}
	}
	
	@Override
	protected void mark(NavigationModelData<Assessment> model, String color) {
		if (!hasSelection())
			return;
		
		final Integer assessmentID = model.getModel().getId();
		
		MarkedCache.impl.markAssement(assessmentID, color);
		
		refreshView();
	}
	
	@Override
	protected void onSelectionChanged(NavigationModelData<Assessment> model) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void open(NavigationModelData<Assessment> model) {
		if (hasSelection())
			navigate(curNavWorkingSet, curNavTaxon, getSelected());
	}
	
	@Override
	protected void setupToolbar() {
		final Button jump = new Button();
		jump.setIconStyle("icon-go-jump");
		jump.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				AssessmentClientSaveUtils.saveIfNecessary(new SimpleListener() {
					public void handleEvent() {
						open(grid.getSelectionModel().getSelectedItem());
					}
				});
			}
		});

		addTool(jump);	
	}
	
	public static class AssessmentStoreSorter extends StoreSorter<NavigationModelData<Assessment>> {
		
		private final AssessmentDateComparator comparator = 
			new AssessmentDateComparator();
		
		@Override
		public int compare(Store<NavigationModelData<Assessment>> store,
				NavigationModelData<Assessment> m1,
				NavigationModelData<Assessment> m2, String property) {
			return comparator.compare(m1.getModel(), m2.getModel());
		}
		
	}
	
	public static class AssessmentDateComparator implements Comparator<Assessment> {
		
		public int compare(Assessment o1, Assessment o2) {
			Date date1 = o1.getDateAssessed();
			Date date2 = o2.getDateAssessed();
			
			if (date1 == null && date2 == null)
				return 0;
			else if (date1 == null)
				return 1;
			else if (date2 == null)
				return -1;
			else
				return date1.compareTo(date2) * -1;
		}		
		
	}

}
