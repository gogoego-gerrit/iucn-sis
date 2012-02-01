package org.iucn.sis.client.panels.search;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.utils.PagingPanel;
import org.iucn.sis.client.panels.utils.SearchPanel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

/**
 * Simple search results page that displays taxon information 
 * directly from the server in a grid.  Single-clicks on the 
 * rows are fired as select events.
 * 
 * Events:
 * 
 * Select: SearchPanel.SearchEvent<TaxonSearchResult>(model)
 * 
 * Fires when a row is clicked.
 *
 */
public class SearchResultPage extends PagingPanel<SearchResultPage.TaxonSearchResult> implements DrawsLazily {
	
	protected final SearchQuery query;
	protected final Grid<TaxonSearchResult> grid;
	protected final GridSelectionModel<TaxonSearchResult> sm;
	
	private int length;

	public SearchResultPage(SearchQuery query) {
		super();
		this.query = query;
		
		setLayout(new FitLayout());
		setLayoutOnChange(true);
		
		sm = createSelectionModel();
		
		grid = new Grid<TaxonSearchResult>(getStoreInstance(), getColumnModel());
		grid.setAutoExpandMin(100);
		grid.setAutoExpandColumn("name");
		grid.setSelectionModel(sm);
		grid.addListener(Events.RowClick, new Listener<GridEvent<TaxonSearchResult>>() {
			public void handleEvent(GridEvent<TaxonSearchResult> be) {
				fireEvent(Events.Select, new SearchPanel.SearchEvent<TaxonSearchResult>(grid, be.getModel()));
			}
		});
		grid.getView().setEmptyText(getEmptyText());
	}
	
	protected String getEmptyText() {
		return "No taxa found for your query.";
	}
	
	protected GridSelectionModel<TaxonSearchResult> createSelectionModel() {
		GridSelectionModel<TaxonSearchResult> model = new GridSelectionModel<TaxonSearchResult>();
		model.setSelectionMode(SelectionMode.SINGLE);
		
		return model;
	}
	
	protected ColumnModel getColumnModel() {
		final List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		final ColumnConfig name = new ColumnConfig("name", "Scientific Name", 175);
		name.setRenderer(new GridCellRenderer<TaxonSearchResult>() {
			public Object render(TaxonSearchResult model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<TaxonSearchResult> store,
					Grid<TaxonSearchResult> grid) {
				return model.toHtml((String)model.get(property));
			}
		});
		
		columns.add(name);
		columns.add(newColumnConfig("commonName", "Common Name", 175, HorizontalAlignment.LEFT));
		columns.add(new ColumnConfig("level", "Level", 75));
		//columns.add(newColumnConfig("category", "Category", 75, HorizontalAlignment.RIGHT));
		columns.add(newColumnConfig("family", "Family", 100, HorizontalAlignment.RIGHT));
		columns.add(newColumnConfig("genus", "Genus", 100, HorizontalAlignment.RIGHT));
		
		return new ColumnModel(columns);
	}
	
	protected ColumnConfig newColumnConfig(String id, String name, int width, HorizontalAlignment alignment) {
		ColumnConfig columnConfig = new ColumnConfig(id, name, width);
		columnConfig.setAlignment(alignment);
		
		return columnConfig;
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		WindowUtils.showLoadingAlert("Loading search results...");
		
		refresh(new DoneDrawingCallback() {
			public void isDrawn() {
				final LayoutContainer container = new LayoutContainer(new BorderLayout());
				container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
				container.add(getPagingToolbar(), new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
				
				add(container);
				
				callback.isDrawn();
			}
		});
		
	}
	
	@Override
	protected void getStore(final GenericCallback<ListStore<TaxonSearchResult>> callback) {
		SearchCache.impl.search(query, new GenericCallback<NativeDocument>() {
			public void onSuccess(NativeDocument result) {
				final ListStore<TaxonSearchResult> store = new ListStore<TaxonSearchResult>();
				
				final NativeNodeList nodes = result.getDocumentElement().getElementsByTagName("result");
				for (int i = 0; i < nodes.getLength(); i++) {
					TaxonSearchResult model = buildModel(nodes.elementAt(i));
					if (model != null)
						store.add(model);
				}
				
				length = store.getModels().size();
				
				WindowUtils.hideLoadingAlert();
				
				callback.onSuccess(store);
			}
			public void onFailure(Throwable caught) {
				WindowUtils.hideLoadingAlert();
				WindowUtils.errorAlert("Error performing search.");
			}
		});
	}
	
	protected TaxonSearchResult buildModel(NativeElement el) {
		return new TaxonSearchResult(el);
	}
	
	@Override
	protected void refreshView() {
		grid.getView().refresh(false);
	}
	
	public int getLength() {
		return length;
	}
	
	public static class TaxonSearchResult extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		private final int taxonID;
		
		public TaxonSearchResult(NativeElement result) {
			super();
			this.taxonID = Integer.valueOf(result.getAttribute("id"));
			
			NativeNodeList children = result.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				NativeNode node = children.item(i);
				if (NativeNode.TEXT_NODE  != node.getNodeType())
					set(node.getNodeName(), node.getTextContent());
			}
		}
		
		public int getTaxonID() {
			return taxonID;
		}
		
		public String toHtml(String name) {
			String status = get("status");
			String styleName = "taxon_status_" + status;
			
			return "<span class=\"" + styleName + "\">" + name + "</span>";
		}
	}
	
}
