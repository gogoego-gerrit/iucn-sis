package org.iucn.sis.client.panels.taxa;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class TaxonHomeWorkingSetsTab extends LayoutContainer implements DrawsLazily {

	public TaxonHomeWorkingSetsTab() {
		super(new FillLayout());
		setLayoutOnChange(true);
	}
	
	@Override
	public void draw(DoneDrawingCallback callback) {
		loadWorkingSetInformationPanel(new DoneDrawingCallbackWithParam<Component>() {
			public void isDrawn(Component parameter) {
				removeAll();
				add(parameter);
			}
		});
	}
	
	/**
	 * Loads the working set information panel.  If this 
	 * panel is not applicable for the current taxon, it 
	 * returns null.
	 * @return
	 */
	private void loadWorkingSetInformationPanel(final DrawsLazily.DoneDrawingCallbackWithParam<Component> callback) {
		TaxonomyCache.impl.fetchWorkingSetsForTaxon(TaxonomyCache.impl.getCurrentTaxon(), new GenericCallback<List<WorkingSet>>() {
			public void onSuccess(List<WorkingSet> workingSets) {
				/*final ContentPanel workingSetInformation = new ContentPanel(new FillLayout());
				workingSetInformation.setStyleName("x-panel");
				workingSetInformation.setWidth(350);
				workingSetInformation.setHeight(200);
				workingSetInformation.setHeading("Related Working Sets");
				workingSetInformation.setLayoutOnChange(true);
				workingSetInformation.setScrollMode(Scroll.AUTO);
				
				if (workingSets.size() > 0) {*/

					final ListStore<BaseModelData> store = new ListStore<BaseModelData>();
					
					for (final WorkingSet data : workingSets) {
						BaseModelData model = new BaseModelData();
						model.set("wsname", data.getName());
						model.set("creator", data.getCreatorUsername());
						model.set("id", data.getId());
						store.add(model);
					}

					List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

					columns.add(new ColumnConfig("wsname", "Working Set Name", 200));
					columns.add(new ColumnConfig("creator", "Owner", 150));
					
					final Grid<BaseModelData> tbl = new Grid<BaseModelData>(store, new ColumnModel(columns));
					
					tbl.setBorders(false);
					tbl.removeAllListeners();
					tbl.addListener(Events.RowClick, new Listener<GridEvent<BaseModelData>>() {
						public void handleEvent(GridEvent<BaseModelData> be) {
							
							if (be.getModel() == null)
								return;

							BaseModelData model = be.getModel();
							final Integer id = model.get("id");

							//WorkingSetCache.impl.setCurrentWorkingSet(id, true);
							StateManager.impl.setState(WorkingSetCache.impl.getWorkingSet(id), null, null);
							//ClientUIContainer.bodyContainer.setSelection(ClientUIContainer.bodyContainer.tabManager.workingSetPage);

						}
					});
					
					//ClientUIContainer.headerContainer.update();
					tbl.getStore().sort("wsname", SortDir.ASC);
					/*workingSetInformation.add(tbl);
				}else*/
					//workingSetInformation.add(new HTML("There are no working sets for this taxon."));
					
					if (workingSets.isEmpty())
						Info.display("Info", "There are no working sets for this taxon.");
					
				
				callback.isDrawn(tbl);
			}
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Could not load working sets, please try again later.");
			}
		});
	}

}
