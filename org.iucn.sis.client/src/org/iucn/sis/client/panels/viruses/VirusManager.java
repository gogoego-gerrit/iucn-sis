package org.iucn.sis.client.panels.viruses;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.VirusCache;
import org.iucn.sis.client.api.utils.PagingPanel;
import org.iucn.sis.shared.api.models.Virus;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
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
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class VirusManager extends PagingPanel<VirusModelData> implements DrawsLazily {
	
	private boolean isDrawn;
	
	private Grid<VirusModelData> grid;
	
	public VirusManager() {
		super();
		setLayout(new FillLayout());
		isDrawn = false;
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		if (!isDrawn) {
			grid = new Grid<VirusModelData>(getStoreInstance(), getColumnModel());
			grid.setAutoExpandColumn("comments");
			grid.setBorders(false);
			grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<VirusModelData>>() {
				public void handleEvent(GridEvent<VirusModelData> be) {
					if (be.getModel() != null)
						editVirus(be.getModel());
				}
			});
			
			final LayoutContainer container = new LayoutContainer(new BorderLayout());
			container.add(createToolbar(), new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
			container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
			container.add(getPagingToolbar(), new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
			
			removeAll();
			add(container);
			
			isDrawn = true;
		}
		
		refresh(new DoneDrawingCallback() {
			public void isDrawn() {
				callback.isDrawn();
			}
		});
	}
	
	@Override
	protected void getStore(final GenericCallback<ListStore<VirusModelData>> callback) {
		VirusCache.impl.list(new ComplexListener<List<Virus>>() {
			public void handleEvent(List<Virus> eventData) {
				final ListStore<VirusModelData> store = new ListStore<VirusModelData>();
				for (Virus virus : eventData)
					store.add(new VirusModelData(virus));
				callback.onSuccess(store);
			}
		});
	}
	
	@Override
	protected void refreshView() {
		grid.getView().refresh(false);
	}
	
	private ColumnModel getColumnModel() {
		final List<ColumnConfig> list = new ArrayList<ColumnConfig>();
		list.add(new ColumnConfig("name", "Virus Name", 200));
		list.add(new ColumnConfig("added", "Date Added", 150));
		list.add(new ColumnConfig("user", "Added By", 150));
		list.add(new ColumnConfig("comments", "Comments", 200));
		
		return new ColumnModel(list);
	}
	
	private ToolBar createToolbar() {
		final ToolBar bar = new ToolBar();
		bar.add(new Button("New Virus", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				VirusEditor editor = new VirusEditor(null);
				editor.setHeading("Create New Virus");
				editor.setSaveListener(new SimpleListener() {
					public void handleEvent() {
						refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
					}
				});
				editor.show();
			}
		}));
		bar.add(new SeparatorToolItem());
		bar.add(new Button("Edit Virus", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				VirusModelData model = grid.getSelectionModel().getSelectedItem();
				if (model != null) {
					editVirus(model);
				}
			}
		}));
		bar.add(new Button("Remove Virus", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final VirusModelData model = grid.getSelectionModel().getSelectedItem();
				if (model != null) {
					WindowUtils.confirmAlert("Confirm", "Are you sure you want to remove this virus?", new WindowUtils.SimpleMessageBoxListener() {
						public void onYes() {
							VirusCache.impl.remove(model.getVirus(), new GenericCallback<Virus>() {
								public void onSuccess(Virus result) {
									Info.display("Success", "Virus removed.");
									refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
								}
								public void onFailure(Throwable caught) {
									//Nothing to do, error popped already.
								}
							});
						}
					});
				}
			}
		}));
		
		return bar;
	}
	
	private void editVirus(VirusModelData virus) {
		VirusEditor editor = new VirusEditor(virus);
		editor.setHeading("Edit Virus");
		editor.setSaveListener(new SimpleListener() {
			public void handleEvent() {
				refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
			}
		});
		editor.show();
	}
	

}
