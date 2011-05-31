package org.iucn.sis.client.panels.taxa;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.api.utils.PagingPanel;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.search.SearchResultPage.TaxonSearchResult;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonStatus;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
import com.extjs.gxt.ui.client.widget.grid.filters.NumericFilter;
import com.extjs.gxt.ui.client.widget.grid.filters.StringFilter;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

/**
 * Class that holds a panel with the UI that allows a user to search for taxon
 * based on status.
 * 
 * @author liz.schwartz
 * 
 */
public class TaxonFinderPanel extends PagingPanel<TaxonSearchResult> implements DrawsLazily {

	private boolean isDrawn;
	private Grid<TaxonSearchResult> grid;
	
	public TaxonFinderPanel() {
		super();
		setLayout(new FillLayout());
		setPageCount(50);
		getProxy().setSort(false);
	}
	
	private ColumnModel getColumnModel() {
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		columns.add(new ColumnConfig("fullname", "Name", 200));
		columns.add(new ColumnConfig("id", "ID", 150));
		
		ColumnConfig jump = new ColumnConfig("goto", "Visit", 50);
		jump.setRenderer(new GridCellRenderer<TaxonSearchResult>() {
			public Object render(final TaxonSearchResult model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<TaxonSearchResult> store,
					Grid<TaxonSearchResult> grid) {
				IconButton icon = new IconButton("icon-go-jump");
				icon.addSelectionListener(new SelectionListener<IconButtonEvent>() {
					public void componentSelected(IconButtonEvent ce) {
						TaxonomyCache.impl.fetchTaxon(model.getTaxonID(), true, new GenericCallback<Taxon>() {
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Could not load this taxon, please try again later.");
							}
							public void onSuccess(Taxon result) {
								StateManager.impl.setTaxon(result);
								WindowManager.get().hideAll();
							}
						});
					}
				});
				
				return icon;
			}
		});
		columns.add(jump);
		
		return new ColumnModel(columns);
	}
	
	protected void getStore(final GenericCallback<ListStore<TaxonSearchResult>> callback) {
		WindowUtils.showLoadingAlert("Loading...");
		
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getSISBase() + "/taxaFinder/" + TaxonStatus.STATUS_NEW, new GenericCallback<String>() {
			public void onSuccess(String result) {
				final ListStore<TaxonSearchResult> store = new ListStore<TaxonSearchResult>();
								
				TaxonByStatusParser parser = new TaxonByStatusParser(document, store);
				parser.setListener(new SimpleListener() {
					public void handleEvent() {
						callback.onSuccess(store);
					}
				});
				
				DeferredCommand.addPause();
				DeferredCommand.addCommand(parser);
			}
			public void onFailure(Throwable caught) {
				
				WindowUtils.errorAlert("Could not load, please try again later.");
			}
		});
	}

	@Override
	public void draw(final DoneDrawingCallback callback) {
		if (isDrawn) {
			callback.isDrawn();
			return;
		}
		
		isDrawn = true;
		
		ToolBar toolbar = new ToolBar();
		
		Button item = new Button("Create Working Set");
		item.setIconStyle("icon-folder-add");
		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				WindowUtils.confirmAlert("Create Working Set", "Do you wish to create a public "
						+ "working set using the most current \"new\" taxon?",
						new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
						ndoc.post(UriBase.getInstance().getSISBase() +"/taxaFinder/workingSet", "<xml></xml>", new GenericCallback<String>() {
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("The new taxa working set was unable to be created.");
							}
							public void onSuccess(String result) {
								WorkingSetCache.impl.update(new GenericCallback<String>() {
									public void onFailure(Throwable caught) {
										WindowUtils
										.infoAlert("The new taxa working set has been created and added to your working sets, but SIS was "
												+ "unable to refresh your working sets.  Please logout and log back in to see the new working set.");
									}
									public void onSuccess(String result) {
										ClientUIContainer.bodyContainer.refreshBody();
										WindowUtils.infoAlert("The new taxa working set has been created and added to your working sets.");
									}
								});
							}
						});

					}
				});
			}
		});
		toolbar.add(item);
		
		Button refresh = new Button("Refresh");
		refresh.setIconStyle("icon-refresh");
		refresh.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				refresh(new DrawsLazily.DoneDrawingCallback() {
					public void isDrawn() {
						layout();
					}
				});
			}
		});
		toolbar.add(new FillToolItem());
		toolbar.add(refresh);
		
		grid = new Grid<TaxonSearchResult>(getStoreInstance(), getColumnModel());
		grid.setAutoExpandColumn("fullname");
		grid.setBorders(false);
		grid.addPlugin(getGridFilters());

		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(toolbar, new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
		container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(getPagingToolbar(), new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		
		add(container);
		
		refresh(callback);
	}
	
	@Override
	protected void refreshView() {
		grid.getView().refresh(false);
	}
	
	private GridFilters getGridFilters() {
		final GridFilters filters = new GridFilters();
		filters.setLocal(true);
		
		filters.addFilter(new NumericFilter("id"));
		filters.addFilter(new StringFilter("fullname"));
		
		return filters;
	}
	
	public static class TaxonByStatusParser implements IncrementalCommand {
		
		private static final int NUM_TO_PARSE = 2000;
		
		private final NativeNodeList nodes;
		private final ListStore<TaxonSearchResult> store;
		
		private SimpleListener listener;
		
		private int current = 0;
		private int size;
		
		public TaxonByStatusParser(NativeDocument document, ListStore<TaxonSearchResult> store) {
			this.store = store;
			this.nodes = document.getDocumentElement().getElementsByTagName(Taxon.ROOT_TAG);
			this.size = nodes.getLength();
		}
		
		@Override
		public boolean execute() {
			if (current >= size) {
				WindowUtils.hideLoadingAlert();
				
				if (listener != null)
					listener.handleEvent();
				
				return false;
			}
			
			int max = current + NUM_TO_PARSE;
			if (max > size)
				max = size;
			
			WindowUtils.showLoadingAlert("Loading Taxa " + (current+1) + "-" + (max) + " of " + size);
			
			for (int i = current; i < current + NUM_TO_PARSE && i < size; i++) {
				TaxonSearchResult result = new TaxonSearchResult(nodes.elementAt(i));
				result.set("id", result.getTaxonID());
				
				store.add(result);
			}
			
			current += NUM_TO_PARSE;
			
			return true;
		}
		
		public void setListener(SimpleListener listener) {
			this.listener = listener;
		}
		
	}

}
