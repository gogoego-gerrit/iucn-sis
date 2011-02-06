package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.shared.api.models.TaxomaticOperation;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class TaxomaticHistoryPanel extends Window implements DrawsLazily {

	private final Taxon taxon;
	
	public TaxomaticHistoryPanel(Taxon taxon) {
		super();
		setLayout(new FillLayout());
		setScrollMode(Scroll.AUTO);
		setHeading("Taxomatic History");
		setSize(800, 600);
		setModal(false);
		setClosable(true);
		
		this.taxon = taxon;
	}
	
	@Override
	public void show() {
		draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				open();
			}
		});
	}
	
	private void open() {
		super.show();
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		TaxonomyCache.impl.fetchTaxomaticHistory(taxon, new GenericCallback<List<TaxomaticOperation>>() {
			public void onSuccess(List<TaxomaticOperation> result) {
				if (result.isEmpty()) {
					WindowUtils.infoAlert("No history is available for this taxon.");
				}
				else {
					final ListStore<HistoryModelData> store = new ListStore<HistoryModelData>();
					for (TaxomaticOperation operation : result)
						store.add(new HistoryModelData(operation));
					
					final Grid<HistoryModelData> grid = new Grid<HistoryModelData>(store, getColumnModel());
					grid.setAutoExpandColumn("details");
					
					add(grid);
					
					callback.isDrawn();
				}
			}
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Error fetching information, please try again later.");	
			}
		});
	}
	
	private ColumnModel getColumnModel() {
		final List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		columns.add(new ColumnConfig("operation", "Operation", 100));
		
		ColumnConfig date = new ColumnConfig("date", "Date", 175);
		date.setDateTimeFormat(FormattedDate.FULL.getDateTimeFormat());
		
		columns.add(date);
		
		columns.add(new ColumnConfig("user", "User", 150));
		
		columns.add(new ColumnConfig("details", "Details", 300));
		
		return new ColumnModel(columns);
	}

	private static class HistoryModelData extends BaseModelData {
		private static final long serialVersionUID = 1L;
		
		public HistoryModelData(TaxomaticOperation operation) {
			super();
			set("user", operation.getUser().getDisplayableName());
			set("date", operation.getDate());
			set("operation", operation.getOperation());
			set("details", operation.getDetails());
		}
		
		
	}
	
}
