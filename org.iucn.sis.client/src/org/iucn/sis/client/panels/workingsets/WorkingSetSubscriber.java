package org.iucn.sis.client.panels.workingsets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.ui.models.workingset.WSModel;
import org.iucn.sis.client.api.ui.models.workingset.WSStore;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.api.utils.PagingPanel;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class WorkingSetSubscriber extends PagingPanel<WSModel> {

	private final TextBox textBox;
	
	private Grid<WSModel> grid;
	private Button actionButton = null;
	
	public WorkingSetSubscriber() {
		super();
		setLayout(new BorderLayout());
		addStyleName("gwt-background");

		textBox = new TextBox();

		BorderLayoutData north = new BorderLayoutData(LayoutRegion.NORTH, 70f);
		BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER);

		buildInstructions(north);
		buildList(center);
	}

	private void buildInstructions(BorderLayoutData data) {
		HTML bar = new HTML("<b>Instructions:</b> Select any working set that you would like "
				+ "to add to your your working sets, and click on the subscribe button below. "
				+ "This will allow you to view the public working sets, however "
				+ "you can only edit working sets which you create.  You can filter the possible "
				+ "working sets by name, " + "creator or date by clicking on the applicable button.", true);
		add(bar, data);
	}
	
	private ColumnModel getColumnModel() {
		List<ColumnConfig> list = new ArrayList<ColumnConfig>();
		
		list.add(new ColumnConfig("name", "Working Set Name", 300));
		
		ColumnConfig creatorConfig = new ColumnConfig("creator", "Creator", 200);
		creatorConfig.setRenderer(new GridCellRenderer<WSModel>() {
			public Object render(WSModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<WSModel> store, Grid<WSModel> grid) {
				User user = model.get(property);
				return user.getUsername();
			}
		});
		list.add(creatorConfig);
		
		ColumnConfig dateConfig = new ColumnConfig("date", "Date Created", 100);
		dateConfig.setRenderer(new GridCellRenderer<WSModel>() {
			public Object render(WSModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<WSModel> store, Grid<WSModel> grid) {
				Date date = model.get(property);
				return FormattedDate.impl.getDate(date);
			}
		});
		list.add(dateConfig);
		
		return new ColumnModel(list);
	}
	
	public void refresh() {
		refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
	}
	
	@Override
	protected void refreshView() {
		grid.getView().refresh(false);
	}

	private void buildList(BorderLayoutData data) {
		grid = new Grid<WSModel>(getStoreInstance(), getColumnModel());
		
		final SelectionListener<ButtonEvent> event = new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				getProxy().getStore().applyFilters(ce.getButton().getText());
				/*pagingLoader.applyFilter(ce.getButton().getText());
				pagingBar.setActivePage(1);
				pagingLoader.getPagingLoader().load();*/
			}
		};
		
		final Button filterByDateButton = new Button("date", event);
		final Button filterByCreatorButton = new Button("creator", event);
		final Button filterByNameButton = new Button("name", event);

		actionButton = new Button("Subscribe", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				WSModel model = grid.getSelectionModel().getSelectedItem();
				if (model != null)
					subscribe(model);
			}
		});
		actionButton.setToolTip("Subscribe to checked working sets");
		actionButton.setWidth("70px");

		com.extjs.gxt.ui.client.widget.HorizontalPanel hp = 
			new com.extjs.gxt.ui.client.widget.HorizontalPanel();
		hp.add(new HTML("<b>Filter by:</b> "));
		hp.add(textBox);
		hp.setSpacing(2);
		hp.add(filterByNameButton);
		hp.add(filterByCreatorButton);
		hp.add(filterByDateButton);
		
		final ToolBar bar = new ToolBar();
		bar.add(actionButton);
		bar.add(new SeparatorToolItem());
		bar.add(new SeparatorToolItem());
		bar.add(hp);
		

		LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(bar, new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
		container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(getPagingToolbar(), new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		//container.add(actionButton);

//		store.addFilter(filterByName);
//		filterByNameButton.disable();
		add(container, data);
	}
	
	@Override
	protected void getStore(final GenericCallback<ListStore<WSModel>> callback) {
		//actionButton.disable();
		WorkingSetCache.impl.getAllSubscribableWorkingSets(new GenericCallback<List<WorkingSet>>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Could not load working sets, please try again later.");
			}
			public void onSuccess(List<WorkingSet> workingsets) {
				final ListStore<WSModel> store = new ListStore<WSModel>();
				store.addFilter(new StoreFilter<WSModel>() {
					public boolean select(Store<WSModel> store, WSModel parent, WSModel item, String property) {
						if (textBox.getText().length() == 0)
							return true;

						String compareWith = property.startsWith("name") ? item.getName().toLowerCase() : 
							property.startsWith("date") ? item.getDate().toLowerCase() :
							item.getCreator().toLowerCase();
						String text = textBox.getText().toLowerCase();
						
						return compareWith.startsWith(text);
					}
				});
				for (WorkingSet workingSet : workingsets)
					store.add(new WSModel(workingSet));
				
				callback.onSuccess(store);
			}
		});
	}

	private void subscribe(final WSModel model) {
		actionButton.disable();
		WorkingSetCache.impl.subscribeToWorkingSet(model.getID().toString(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert(model.getName()
						+ " was unable to be added to your working sets.  Please try again.");
				actionButton.enable();
			}

			public void onSuccess(String arg0) {
				WindowUtils.infoAlert("Working Set Added", model.getName()
						+ " was successfully added to your working sets.  To view this " + model.getName()
						+ " click on the quick summary tab.");
				actionButton.enable();
				// refreshList();
				WSStore.getStore().update();
			}
		});
	}

}
