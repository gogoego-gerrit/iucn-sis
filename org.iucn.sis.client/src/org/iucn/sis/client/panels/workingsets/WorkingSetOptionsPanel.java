package org.iucn.sis.client.panels.workingsets;

import java.util.List;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
import org.iucn.sis.client.panels.workingsets.WorkingSetTaxaList.TaxaData;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.CheckChangedEvent;
import com.extjs.gxt.ui.client.event.CheckChangedListener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.util.extjs.client.CardLayoutContainer;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * Panel that edits taxa from a working set
 * 
 * @author liz.schwartz
 * 
 */
@SuppressWarnings("deprecation")
public class WorkingSetOptionsPanel extends RefreshLayoutContainer {

	public static final int MOVE = 2;
	public static final int DELETE = 1;
	public static final int ADDSEARCH = 4;
	public static final int ADDBROWSE = 5;
	
	private Button title;
	private WorkingSet ws;
	private Button addSearch;
	private Button addBrowse;
	private Button delete;
	private Button move;
	private Button titleHeader;
	private int mode;
	private WorkingSetTaxaList taxaList = null;
	private WorkingSetTaxaList checkedTaxaList = null;
	private WorkingSetTaxaList deleteTaxaList = null;

	private VerticalPanel imagePanel = null;

	private CardLayoutContainer taxaListHolder = null;
	private CardLayoutContainer infoPanel = null;

	private WorkingSetAddTaxaBrowserPanel browserPanel = null;
	private WorkingSetAddTaxaSearchPanel addTaxonPanel = null;
	private WorkingSetDeleteTaxa deletePanel = null;
	private WorkingSetMoveTaxaPanel movePanel = null;
	private LayoutContainer blankPanel = null;

	/**
	 * variables that determine whether you need to refresh the panels
	 */
	boolean refreshNeededDelete = true;
	boolean refreshNeededAdd = true;
	boolean refreshNeededMove = true;
	boolean anyChanges = false;

	public WorkingSetOptionsPanel() {
		mode = -1;
		build();
	}

	private void addBrowseTaxa() {
		setMode(ADDBROWSE);
		if (WorkingSetCache.impl.getCurrentWorkingSet() != null)
			refreshInfoPanel(taxaList.getFilter());
	}

	private void addContent(RowData data) {

		LayoutContainer content = new LayoutContainer();
		BorderLayout layout = new BorderLayout();
		BorderLayoutData west = new BorderLayoutData(LayoutRegion.WEST, .48f);
		BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER, 20);
		BorderLayoutData east = new BorderLayoutData(LayoutRegion.EAST, .48f);

		content.setLayout(layout);

		taxaListHolder = new CardLayoutContainer();
		taxaListHolder.setBorders(true);
		taxaList = new WorkingSetTaxaList(false);
		taxaList.setFilterVisible(true);
		taxaList.addListener(Events.Change, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				refreshInfoPanel(taxaList.getFilter());
			}
		});

		checkedTaxaList = new WorkingSetTaxaList(true);
		checkedTaxaList.setFilterVisible(true);

		CheckChangedListener<TaxaData> checkListener = new CheckChangedListener<TaxaData>() {
			@Override
			public void checkChanged(CheckChangedEvent<TaxaData> event) {
				deletePanel.refreshTaxa(getChecked());
			}

		};

		deleteTaxaList = new WorkingSetTaxaList(true, checkListener);
		deleteTaxaList.setFilterVisible(true);
		deleteTaxaList.addListener(Events.Change, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				refreshInfoPanel(deleteTaxaList.getFilter());
			};
		});

		content.add(taxaListHolder, west);

		imagePanel = new VerticalPanel();
		imagePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		content.add(imagePanel, center);

		browserPanel = new WorkingSetAddTaxaBrowserPanel();
		addTaxonPanel = new WorkingSetAddTaxaSearchPanel();
		deletePanel = new WorkingSetDeleteTaxa();
		movePanel = new WorkingSetMoveTaxaPanel();
		blankPanel = new LayoutContainer();
		blankPanel.addStyleName("gwt-background");

		infoPanel = new CardLayoutContainer();
		infoPanel.setBorders(true);
		content.add(infoPanel, east);
		add(content, data);
	}

	private void addSearchTaxa() {
		setMode(ADDSEARCH);
		if (WorkingSetCache.impl.getCurrentWorkingSet() != null)
			refreshInfoPanel(taxaList.getFilter());

	}

	private void addToolBar(RowData data) {
		ToolBar buttons = new ToolBar();

		addSearch = new Button();
		addSearch.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (ws != null && checkPermissions())
					addSearchTaxa();
				else
					WindowUtils.errorAlert("Insufficient Permissions",
							"You cannot modify a public working set you do not own.");
			}
		});
		addSearch.setIconStyle("icon-search-add");
		addSearch.setTitle("Search for taxa to add");
		addSearch.addStyleName("float-left");

		addBrowse = new Button();
		addBrowse.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (ws != null && checkPermissions())
					addBrowseTaxa();
				else
					WindowUtils.errorAlert("Insufficient Permissions",
							"You cannot modify a public working set you do not own.");
			}
		});
		addBrowse.setIconStyle("icon-browse-add");
		addBrowse.setTitle("Browse for taxa to add");
		addBrowse.addStyleName("float-left");

		delete = new Button();
		delete.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (ws != null && checkPermissions())
					deleteTaxa(ce);
				else
					WindowUtils.errorAlert("Insufficient Permissions",
							"You cannot modify a public working set you do not own.");
			}
		});
		delete.setIconStyle("icon-remove");
		delete.setTitle("Delete taxa from current working set");
		delete.addStyleName("float-left");

		move = new Button();
		move.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				moveTaxa(ce);
			};
		});
		move.setIconStyle("icon-copy");
		move.setTitle("Move selected taxa to another working set");
		move.addStyleName("float-left");

		titleHeader = new Button();
		titleHeader.setText("Currently Modifying: ");
		title = new Button();

		buttons.add(new SeparatorToolItem());
		buttons.add(addSearch);
		buttons.add(new SeparatorToolItem());
		buttons.add(addBrowse);
		buttons.add(new SeparatorToolItem());
		buttons.add(delete);
		buttons.add(new SeparatorToolItem());
		buttons.add(move);
		buttons.add(new SeparatorToolItem());
		buttons.add(titleHeader);
		buttons.add(title);

		add(buttons, data);
	}

	private void build() {

		addStyleName("gwt-background");
		RowLayout layout = new RowLayout();
		setLayout(layout);
		RowData north = new RowData(1d, 25);
		RowData center = new RowData(1d, 1d);

		addToolBar(north);
		addContent(center);

	}

	boolean checkPermissions() {
		if (ws == null) {
			WindowUtils.infoAlert("You must first select a working set to edit...");
			disableButtons();
			return false;
		} else if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, ws)) {
			enableButtons();
			return true;
		} else {
			disableButtons();
			return false;
		}
	}

	public void clearImagePanel() {
		imagePanel.clear();
	}

	private void deleteTaxa(BaseEvent be) {
		setMode(DELETE);
		if (WorkingSetCache.impl.getCurrentWorkingSet() != null) {
			refreshInfoPanel(taxaList.getFilter());
		}
	}

	protected void disableButtons() {
		addBrowse.disable();
		addSearch.disable();
		delete.disable();
		move.disable();
	}

	public void enableButtons() {
		addBrowse.enable();
		addSearch.enable();
		delete.enable();
		move.enable();
	}

	public void forceRefreshTaxaList() {
		setTopTaxaWidget();
		if ((mode == ADDSEARCH || mode == ADDBROWSE)) {
			refreshNeededAdd = false;
			taxaList.setFilter(TaxaData.FAMILY);
			taxaList.forcedRefresh();
		} else if (mode == DELETE) {
			refreshNeededDelete = false;
			deleteTaxaList.setFilter(TaxaData.FAMILY);
			deleteTaxaList.forcedRefresh();
			removeChecks(deleteTaxaList);
		} else if (mode == MOVE) {
			refreshNeededMove = false;
			checkedTaxaList.setFilter(TaxaData.FAMILY);
			checkedTaxaList.forcedRefresh();
			removeChecks(checkedTaxaList);
		}

	}

	public void forceRefreshTaxaList(int newMode) {
		mode = newMode;

		if (checkPermissions()) {
			forceRefreshTaxaList();
			refreshInfoPanel("has permissions");
		}

	}

	public List<TaxaData> getChecked() {
		if (mode == MOVE)
			return checkedTaxaList.getChecked();
		else if (mode == DELETE)
			return deleteTaxaList.getChecked();
		else
			return null;
	}

	public void listChanged() {
		refreshNeededAdd = true;
		refreshNeededDelete = true;
		refreshNeededMove = true;
		anyChanges = true;
		forceRefreshTaxaList();
	}

	private void moveTaxa(BaseEvent be) {
		setMode(MOVE);
		if (WorkingSetCache.impl.getCurrentWorkingSet() != null)
			refreshInfoPanel(taxaList.getFilter());
	}

	@Override
	public void refresh() {
		ws = WorkingSetCache.impl.getCurrentWorkingSet();
		boolean editable = checkPermissions();
		refreshTitle(editable);
		refreshTaxaList(editable);
		if (editable) {
			refreshInfoPanel("not null");
		} else
			refreshInfoPanel(null);
	}

	private void refreshAndAddPanel(RefreshLayoutContainer panel) {
		if (infoPanel.getLayout().getActiveItem() == null || !infoPanel.getLayout().getActiveItem().equals(panel)) {
			infoPanel.switchToComponent(panel);
		}

		panel.refresh();
		panel.layout();
	}

	private void refreshImagePanel() {
		clearImagePanel();
		if (mode == ADDBROWSE || mode == ADDSEARCH) {
			imagePanel.add(new Image("tango/actions/go-previous.png"));
			imagePanel.add(new Image("tango/actions/go-previous.png"));
			imagePanel.add(new Image("tango/actions/go-previous.png"));
		} else if (mode == MOVE) {
			imagePanel.add(new Image("tango/actions/go-next.png"));
			imagePanel.add(new Image("tango/actions/go-next.png"));
			imagePanel.add(new Image("tango/actions/go-next.png"));
		}
	}

	private void refreshInfoPanel(String filter) {
		try {
			if (filter != null) {
				refreshImagePanel();

				if (mode == ADDSEARCH) {
					//FIXME refreshAndAddPanel(manager.addTaxonPanel);
					if (refreshNeededAdd)
						forceRefreshTaxaList();
					else
						refreshTaxaListHolder();

				} else if (mode == ADDBROWSE) {
					refreshAndAddPanel(browserPanel);
					if (refreshNeededAdd)
						forceRefreshTaxaList();
					else
						refreshTaxaListHolder();
				} else if (mode == DELETE) {
					refreshAndAddPanel(deletePanel);
					if (refreshNeededDelete)
						forceRefreshTaxaList();
					else
						refreshTaxaListHolder();
				} else if (mode == MOVE) {
					refreshAndAddPanel(movePanel);
					if (refreshNeededMove)
						forceRefreshTaxaList();
					else
						refreshTaxaListHolder();
				}
				
			} else {
				mode = -1;
				clearImagePanel();
				infoPanel.switchToComponent(blankPanel);
				taxaListHolder.switchToComponent(blankPanel);
			}

			layout();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void refreshTaxaList(final boolean editable) {

		if (editable) {

			if (mode == MOVE) {
				if (refreshNeededMove) {
					checkedTaxaList.setFilter(TaxaData.ORDER);
					checkedTaxaList.forcedRefresh();
					refreshNeededMove = false;
				} else {
					checkedTaxaList.refresh();
				}
			} else if (mode == DELETE) {
				if (refreshNeededDelete) {
					deleteTaxaList.setFilter(TaxaData.ORDER);
					deleteTaxaList.forcedRefresh();
					refreshNeededDelete = false;
				} else {
					deleteTaxaList.refresh();
				}

			} else if (mode == ADDBROWSE || mode == ADDSEARCH) {
				if (refreshNeededAdd) {
					taxaList.setFilter(TaxaData.ORDER);
					taxaList.forcedRefresh();
					refreshNeededAdd = false;
				} else
					taxaList.refresh();
			}
		}

	}

	private void refreshTaxaListHolder() {
		setTopTaxaWidget();
		if (mode == MOVE) {
			removeChecks(checkedTaxaList);
			checkedTaxaList.refreshWithFilter(TaxaData.ORDER);
		}

		else if (mode == ADDBROWSE || mode == ADDSEARCH) {
			taxaList.refreshWithFilter(TaxaData.FAMILY);
		}

		else {
			removeChecks(deleteTaxaList);
			deleteTaxaList.refreshWithFilter(TaxaData.ORDER);

		}
	}

	private void refreshTitle(boolean editable) {
		if (ws == null) {
			title.setText("Please select a working set.");
		} else if (!editable)
			title.setText("Please select a different set.");
		else
			title.setText(ws.getWorkingSetName());
	}

	private void removeChecks(WorkingSetTaxaList list) {
		list.deselectAll();
		List<DataListItem> checked = list.getListChecked();
		for (DataListItem item : checked) {
			item.setChecked(false);
		}

	}

	private void setMode(int newMode) {
		mode = newMode;
	}

	private void setTopTaxaWidget() {
		if ((mode == MOVE)
				&& ((taxaListHolder.getLayout().getActiveItem() == null || !taxaListHolder.getLayout().getActiveItem()
						.equals(checkedTaxaList)))) {
			taxaListHolder.switchToComponent(checkedTaxaList);
			taxaListHolder.layout();
		}

		else if ((mode == ADDBROWSE || mode == ADDSEARCH)
				&& (taxaListHolder.getLayout().getActiveItem() == null || !taxaListHolder.getLayout().getActiveItem()
						.equals(taxaList))) {
			taxaListHolder.switchToComponent(taxaList);
			taxaListHolder.layout();
		}

		else if ((mode == DELETE)
				&& (taxaListHolder.getLayout().getActiveItem() == null || !taxaListHolder.getLayout().getActiveItem()
						.equals(deleteTaxaList))) {
			taxaListHolder.switchToComponent(deleteTaxaList);
			taxaListHolder.layout();
		}
	}

}
