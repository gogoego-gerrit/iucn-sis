package org.iucn.sis.client.panels.taxa;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.Image;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.GenericPagingLoader;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * Class that holds a panel with the UI that allows a user to search for taxon
 * based on status.
 * 
 * @author liz.schwartz
 * 
 */
public class TaxonFinderPanel extends ContentPanel {

	class TaxonListElement extends BaseModel {
		private static final long serialVersionUID = 1L;
		public TaxonListElement(final String name, final String id) {
			Image goTo = new Image("tango/actions/go-jump.png");
			goTo.setTitle(name);
			goTo.addStyleName("pointerCursor");

			set("name", name);
			set("id", id);
			set("goto", goTo);
		}

		@Override
		public String toString() {
			return get("name") + " : " + get("id");
		}

	}

	final public static String NEW_STATUS = "new";
	final protected PagingToolBar pagingToolBar;

	final protected ListStore<TaxonListElement> store;
	final protected GenericPagingLoader<TaxonListElement> loader;


	public TaxonFinderPanel() {
		super();
		setHeaderVisible(false);
		setLayout(new FitLayout());
		
		loader = new GenericPagingLoader<TaxonListElement>();
		pagingToolBar = new PagingToolBar(50);
		pagingToolBar.bind(loader.getPagingLoader());
		store = new ListStore<TaxonListElement>(loader.getPagingLoader());

		build();
	}

	private void build() {
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		columns.add(new ColumnConfig("name", "Name", 200));
		columns.add(new ColumnConfig("id", "ID", 150));
		columns.add(new ColumnConfig("goto", "Visit", 50));
		ColumnModel cm = new ColumnModel(columns);

		Grid<TaxonListElement> grid = new Grid<TaxonListElement>(store, cm);
		grid.setLoadMask(true);
		grid.setBorders(true);
		grid.setAutoExpandColumn("name");
		grid.addListener(Events.CellClick, new Listener<GridEvent<TaxonListElement>>() {
			public void handleEvent(GridEvent<TaxonListElement> be) {
				
				if (be.getColIndex() == 2) {
					Integer id = be.getGrid().getStore().getAt(be.getRowIndex()).get("id");
					TaxonomyCache.impl.fetchTaxon(id, true, new GenericCallback<Taxon >() {

						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub

						}

						public void onSuccess(Taxon result) {
							/*ClientUIContainer.bodyContainer
										.setSelection(ClientUIContainer.bodyContainer.tabManager.taxonHomePage);
*/
						}
					});
				}

			}
		});

		ToolBar toolbar = new ToolBar();
		Button item = new Button("Create Working Set");
		item.setIconStyle("icon-folder-add");
		item.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				WindowUtils.confirmAlert("Create Working Set", "Do you wish to create a public "
						+ "working set using the most current \"new\" taxon?",
						new WindowUtils.MessageBoxListener() {

							@Override
							public void onNo() {
								// TODO Auto-generated method stub

							}

							@Override
							public void onYes() {
								final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
								ndoc.post(UriBase.getInstance().getSISBase() +"/taxaFinder/workingSet", "<xml></xml>", new GenericCallback<String>() {

									public void onFailure(Throwable caught) {
										WindowUtils
												.errorAlert("The new taxa working set was unable to be created.");

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
												WindowUtils
														.infoAlert("The new taxa working set has been created and added to your working sets.");

											}
										});

									}
								});

							}
						});

			}
		});
		item.setTitle("Create Working Set");
		toolbar.add(item);

		
		add(grid);
		setTopComponent(toolbar);
		setBottomComponent(pagingToolBar);
	}

	public void load() {
		loader.getPagingLoader().setOffset(0);
		loader.getPagingLoader().load();
	}

}
