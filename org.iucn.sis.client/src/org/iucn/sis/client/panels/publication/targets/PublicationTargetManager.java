package org.iucn.sis.client.panels.publication.targets;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.PublicationCache;
import org.iucn.sis.client.api.utils.PagingPanel;
import org.iucn.sis.shared.api.models.PublicationTarget;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class PublicationTargetManager extends PagingPanel<PublicationTargetModelData> implements DrawsLazily {
	
	private Grid<PublicationTargetModelData> grid;
	
	public PublicationTargetManager() {
		super();
		setLayout(new FillLayout());
	}
	
	@Override
	public void draw(DoneDrawingCallback callback) {
		GridSelectionModel<PublicationTargetModelData> sm = new GridSelectionModel<PublicationTargetModelData>();
		sm.setSelectionMode(SelectionMode.SINGLE);
		
		grid = new Grid<PublicationTargetModelData>(getStoreInstance(), getColumnModel());
		grid.setSelectionModel(sm);
		grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<PublicationTargetModelData>>() {
			public void handleEvent(GridEvent<PublicationTargetModelData> be) {
				if (be.getModel() != null)
					openEditor(be.getModel());
			}
		});
		
		final ToolBar bar = new ToolBar();
		bar.add(new Button("Create New Target", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				openEditor(null);
			}
		}));
		bar.add(new Button("Edit Target", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				PublicationTargetModelData model = grid.getSelectionModel().getSelectedItem();
				if (model == null)
					WindowUtils.errorAlert("Please select a target below first, or click \"Create New Target\" to add one.");
				else
					openEditor(model);
			}
		}));
		bar.add(new SeparatorToolItem());
		bar.add(new Button("Delete Target", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final PublicationTargetModelData model = grid.getSelectionModel().getSelectedItem();
				if (model == null)
					WindowUtils.errorAlert("Please select a target below first, or click \"Create New Target\" to add one.");
				else {
					WindowUtils.confirmAlert("Confirm", "Are you sure you want to delete " +
							"this publication target? No assessments will be deleted.", 
							new WindowUtils.SimpleMessageBoxListener() {
						public void onYes() {
							PublicationCache.impl.deleteTarget(model.getModel(), new GenericCallback<Object>() {
								public void onSuccess(Object result) {
									Info.display("Success", "Target deleted successfully.");
									refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
								}
								public void onFailure(Throwable caught) {
								}
							});
						}
					});
				}
			}
		}));
		
		final BorderLayoutData north = new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25);
		north.setSplit(false);
		
		final BorderLayoutData south = new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25);
		south.setSplit(false);
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(bar, north);
		container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(getPagingToolbar(), south);
		
		add(container);
		
		refresh(callback);
	}
	
	private void openEditor(PublicationTargetModelData model) {
		PublicationTargetEditor editor = new PublicationTargetEditor(model);
		editor.addListener(Events.Update, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
			}
		});
		editor.show();
	}
	
	protected void getStore(final GenericCallback<ListStore<PublicationTargetModelData>> callback) {
		PublicationCache.impl.listTargets(new ComplexListener<List<PublicationTarget>>() {
			public void handleEvent(List<PublicationTarget> eventData) {
				ListStore<PublicationTargetModelData> store = new ListStore<PublicationTargetModelData>();
				for (PublicationTarget target : eventData)
					store.add(new PublicationTargetModelData(target));
				
				callback.onSuccess(store);
			}
		});
	}
	
	@Override
	protected void refreshView() {
		grid.getView().refresh(false);
	}
	
	private ColumnModel getColumnModel() {
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		columns.add(new ColumnConfig("name", "Name", 250));
		columns.add(new ColumnConfig("date", "Target Date", 100));
		columns.add(new ColumnConfig("reference", "Publication Reference", 300));
		
		return new ColumnModel(columns);
	}
	
}
