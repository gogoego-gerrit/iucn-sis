package org.iucn.sis.client.panels.dem;

import java.util.Arrays;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.SchemaCache;
import org.iucn.sis.client.api.caches.ViewCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.ui.views.SISPageHolder;
import org.iucn.sis.client.api.ui.views.SISView;
import org.iucn.sis.client.api.ui.views.ViewDisplay.PageChangeRequest;
import org.iucn.sis.shared.api.acl.UserPreferences;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.Assessment;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.ListViewSelectionModel;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.gwt.ui.DrawsLazily;

public class DEMLayoutChooser extends LayoutContainer { 

	private ComplexListener<PageChangeRequest> pageChangeListener;
	
	public DEMLayoutChooser() {
		super(new FillLayout());
	}
	
	public void setPageChangeListener(
			ComplexListener<PageChangeRequest> pageChangeListener) {
		this.pageChangeListener = pageChangeListener;
	}
	
	public void draw(final DrawsLazily.DoneDrawingCallbackWithParam<String> callback) {
		Assessment assessment = AssessmentCache.impl.getCurrentAssessment();
		String schema = assessment.getSchema(SchemaCache.impl.getDefaultSchema());
		
		ViewCache.impl.fetchViews(schema, new GenericCallback<String>() {
			public void onSuccess(String result) {
				Integer currentPage = buildContainer();
				
				callback.isDrawn(currentPage == null ? null : currentPage.toString());
			}
			public void onFailure(Throwable caught) {
				callback.isDrawn(null);
			}
		});
	}
	
	private Integer buildContainer() {
		removeAll();
		
		String prefs = SISClientBase.currentUser.getPreference(UserPreferences.VIEW_CHOICES, null);
		List<String> viewsToShow = null;
		if (prefs != null && !"".equals(prefs))
			viewsToShow = Arrays.asList(prefs.split(","));
		
		final ListStore<ViewModelData> listing = new ListStore<ViewModelData>();
		listing.setKeyProvider(new ModelKeyProvider<ViewModelData>() {
			public String getKey(ViewModelData model) {
				return model.get("value");
			}
		});
		
		final ListStore<SectionModelData> sections = new ListStore<SectionModelData>();
		sections.setKeyProvider(new ModelKeyProvider<SectionModelData>() {
			public String getKey(SectionModelData model) {
				return model.getPage().getPageID(); 
			}
		});
		sections.addFilter(new StoreFilter<SectionModelData>() {
			public boolean select(Store<SectionModelData> store, SectionModelData parent, 
					SectionModelData item, String property) {
				return property == null || property.equals(item.get("parent"));
			}
		});
		
		for (final SISView curView : ViewCache.impl.getAvailableViews()) {
			if ((viewsToShow == null || viewsToShow.contains(curView.getId())) && hasPermission(curView)) {
				listing.add(new ViewModelData(curView.getDisplayableTitle(), curView.getId()));		
				
				for (SISPageHolder curPage : curView.getPages()) {
					SectionModelData model = new SectionModelData(curPage.getPageTitle(), curView.getId());
					model.set("page", curPage);
					
					sections.add(model);
				}
			}
		}
		
		final ComboBox<ViewModelData> box = new ComboBox<ViewModelData>();
		box.setWidth(200);
		box.setEmptyText("--- Select View ---");
		box.setTriggerAction(TriggerAction.ALL);
		box.setEditable(false);
		box.setForceSelection(true);
		box.setStore(listing);
		box.addSelectionChangedListener(new SelectionChangedListener<ViewModelData>() {
			public void selectionChanged(SelectionChangedEvent<ViewModelData> se) {
				ViewModelData selected = se.getSelectedItem();
				if (selected != null) {
					String id = selected.get("value");
					sections.filter(id);
				}
				else
					sections.filter("none");
				
				SISClientBase.currentUser.setPreference(UserPreferences.DEFAULT_LAYOUT, selected.getId());
			}
		});
		
		final SectionSelectionModel sm = new SectionSelectionModel();
		
		final ListView<SectionModelData> view = new ListView<SectionModelData>();
		view.setSelectionModel(sm);
		view.setStore(sections);
		view.addListener(Events.Select, new Listener<ListViewEvent<SectionModelData>>() {
			public void handleEvent(ListViewEvent<SectionModelData> be) {
				SectionModelData selection = be.getModel();
				if (selection != null && pageChangeListener != null) {
					String parentID = selection.get("parent");
					SISView curView = ViewCache.impl.getView(parentID);
					SISPageHolder page = selection.getPage(); 
					pageChangeListener.handleEvent(
						new PageChangeRequest(curView, page));
				}
			}
		});
		
		final BorderLayoutData north = new BorderLayoutData(LayoutRegion.NORTH);
		north.setSplit(false);
		north.setSize(25);
		
		final LayoutContainer boxWrapper = new LayoutContainer();
		boxWrapper.add(box);
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(boxWrapper, north);
		container.add(view, new BorderLayoutData(LayoutRegion.CENTER));
		
		SISView current = ViewCache.impl.getCurrentView();
		if (current == null) {
			String defaultViewID = SISClientBase.currentUser.getPreference(UserPreferences.DEFAULT_LAYOUT, null);
			
			ViewModelData defaultSelection = null;
			if (defaultViewID != null)
				defaultSelection = listing.findModel(defaultViewID);
			
			if (defaultSelection == null && listing.getCount() > 0)
				defaultSelection = listing.getAt(0);
			
			if (defaultSelection != null)
				box.setValue(defaultSelection);	
			else
				sections.filter("none");
			add(container);
			return null;
		}
		else {
			box.setValue(listing.findModel(current.getId()));
			sm.setValue(sections.findModel(current.getCurPage().getPageID()));
			add(container);
			return ViewCache.impl.getLastPageViewed(current.getId());
		}
	}
	
	private boolean hasPermission(SISView view) {
		return AuthorizationCache.impl.hasRight(AuthorizableObject.READ, view);
	}
	
	private static class SectionSelectionModel extends ListViewSelectionModel<SectionModelData> {
		
		public SectionSelectionModel() {
			super();
			setSelectionMode(SelectionMode.SINGLE);
		}
		
		public void setValue(SectionModelData value) {
			doSingleSelect(value, false);
		}
		
	}
	
	private static class ViewModelData extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		public ViewModelData(String name, String id) {
			super();
			set("text", name);
			set("value", id);
		}
		
		public String getId() {
			return get("value");
		}
		
	}
	
	private static class SectionModelData extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		public SectionModelData(String name, String parent) {
			super();
			set("text", name);
			set("value", name);
			set("parent", parent);
		}
		
		public SISPageHolder getPage() {
			return get("page");
		}
		
	}

}
