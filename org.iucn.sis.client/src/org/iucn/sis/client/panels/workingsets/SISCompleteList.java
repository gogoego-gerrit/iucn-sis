package org.iucn.sis.client.panels.workingsets;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.util.extjs.client.ViewerFilterTextBox;

public class SISCompleteList extends VerticalPanel {

	class MasterListUI extends Dialog {

		ListStore<ModelData> viewer = null;
		DataList largeList = null;
		ViewerFilterTextBox<ModelData> masterTextbox = null;

		public MasterListUI() {
			super();
			setButtons(OKCANCEL);
			setClosable(true);
			setResizable(true);
			largeList = new DataList();
			largeList.setCheckable(true);
			viewer = new ListStore<ModelData>();

			masterTextbox = new ViewerFilterTextBox<ModelData>();
			build();
		}

		private void build() {
			setHideOnButtonClick(true);
			addStyleName("my-shell-plain");
			setHeading("Master List");
			setMinHeight(200);
			setMinWidth(200);
			setSize(300, 450);

			largeList.addStyleName("gwt-background");
			// viewer.setContentProvider( new ModelContentProvider());
			// viewer.setLabelProvider(new ModelLabelProvider());
			StoreFilter<ModelData> filter = new StoreFilter<ModelData>() {
				public boolean select(Store store, ModelData parent, ModelData item, String property) {
					String text = masterTextbox.getText();
					if (text != null && !text.equals("")) {
						return ((String) item.get("name")).toLowerCase().startsWith(text.toLowerCase());
					}
					return true;
				}

			};
			viewer.addFilter(filter);
			masterTextbox.bind(viewer);

			((Button)getButtonBar().getItemByItemId(Dialog.OK)).setText("Add");
			((Button)getButtonBar().getItemByItemId(Dialog.OK)).addSelectionListener(new SelectionListener<ButtonEvent>() {

				@Override
				public void componentSelected(ButtonEvent ce) {
					Button btn = ce.getButton();
					if (btn == getButtonBar().getItemByItemId(Dialog.OK) )
						getItemsToAdd();

				}

			});

			setLayout(new RowLayout());

			HTML instructions = new HTML("<b>Instructions:</b> Select the items "
					+ "which you would like to add to your list from the master list below. You"
					+ " can filter the results using the textbox below.");

			add(instructions, new RowData(1d, 25));
			add(masterTextbox, new RowData(1d, 25));
			add(largeList, new RowData(1d, 1d));

		}

		private void getItemsToAdd() {
			List<DataListItem> checked = largeList.getChecked();
			for (int i = 0; i < checked.size(); i++) {
				DataListItem item = checked.get(i);
				if (item.isEnabled()) {
					addItemInList(item.getText());
				}
			}
		}

		public void refresh() {
			for (int i = 0; i < masterList.size(); i++) {
				String text = (String) masterList.get(i);
				BaseModelData item = new BaseModelData();

				if (!listText.contains(text)) {
					item.set("name", text);
					viewer.add(item);
				}
			}
		}

	}

	public final boolean ENABLED = true;

	public final boolean DISABLED = false;
	protected VerticalPanel list = null;
	protected ScrollPanel scroll = null;
	protected TextBox textbox = null;
	protected Button addButton = null;
	protected Button removeButton = null;
	private Image image = null;
	private ArrayList selected = null;

	private MasterListUI myMasterListUI = null;
	/**
	 * Text of everything that was typed
	 */
	private ArrayList masterList = null;
	private ArrayList listText = null;
	private String highlightStyle = null;
	// private boolean resizable;

	/**
	 * either enabled or disabled
	 */
	private boolean style = ENABLED;

	public SISCompleteList() {

		list = new VerticalPanel();
		textbox = new TextBox();
		image = new Image("images/icon-book-add.png");
		addButton = new Button();
		removeButton = new Button();
		myMasterListUI = new MasterListUI();
		selected = new ArrayList();
		masterList = new ArrayList();
		listText = new ArrayList();
		highlightStyle = "redFont";
		// resizable = false;
		
		scroll = new ScrollPanel(list);
		scroll.setAlwaysShowScrollBars(true);
		scroll.setSize("275px", "300px");
		scroll.addStyleName("expert-border");
		scroll.setAlwaysShowScrollBars(false);
		setSize("300px", "340px");
		setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

		build();
	}

	/**
	 * needed for working set summary panel ... probably shouldn't use unless
	 * you want to do highlighting different way...
	 * 
	 * @param width
	 */
	public SISCompleteList(WorkingSetNewWSPanel panel, int width) {
		list = new VerticalPanel();
		textbox = new TextBox();
		image = new Image("images/icon-book-add.png");
		addButton = new Button();
		removeButton = new Button();
		myMasterListUI = new MasterListUI();
		selected = new ArrayList();
		masterList = new ArrayList();
		listText = new ArrayList();
		highlightStyle = "white-background";
		// resizable = true;
		
		scroll = new ScrollPanel(list);
		scroll.setAlwaysShowScrollBars(true);
		scroll.setWidth(width + "px");
		scroll.addStyleName("expert-border");
		scroll.setAlwaysShowScrollBars(false);
		setWidth(width + "px");
		setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

		build();
	}

	/**
	 * Adds item to the list
	 * 
	 * @param item
	 */
	public void addItemInList(String text) {

		if (!listText.contains(text) && !text.trim().equalsIgnoreCase("")) {
			listText.add(text);
			HTML html = buildItemInList(text);
			list.add(html);

			if (!masterList.contains(text)) {
				masterList.add(text);
			}
		}
	}

	/**
	 * adds the items that is given in ArrayList to the global list
	 * 
	 * @param csvItems
	 */
	public void addItemsInGlobalList(ArrayList items) {
		for (int i = 0; i < items.size(); i++) {
			if (!masterList.contains(items.get(i)))
				masterList.add(items.get(i));
		}
	}

	/**
	 * adds the items that is given in csv form to the global list
	 * 
	 * @param csvItems
	 */
	public void addItemsInGlobalList(String csvItems) {
		String[] items = csvItems.split(",");
		for (int i = 0; i < items.length; i++) {
			if (!masterList.contains(items[i]))
				masterList.add(items[i]);
		}
	}

	/**
	 * adds the items that is given in csv form to the listbox
	 * 
	 * @param csvItems
	 */
	public void addItemsInList(String csvItems) {
		String[] items = csvItems.split(",");
		for (int i = 0; i < items.length; i++) {
			addItemInList(items[i]);
		}
	}

	private void build() {

		addButton.setText("Add");
		addButton.setToolTip("Adds the text in the text box to the list.");
		addButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				updateNewItemInList();
			}

		});

		removeButton.setText("Delete");
		removeButton.setToolTip("Deletes items which are selected in the list.");
		removeButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				updateDeleteItemsInList();
			}

		});
		removeButton.disable();

		image.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				showMasterList();
			}

		});
		image.addStyleName("pointerCursor");

		setSpacing(4);

		HorizontalPanel hp = new HorizontalPanel();
		hp.add(image);
		hp.add(textbox);
		hp.add(addButton);
		hp.add(removeButton);
		add(hp);
		add(scroll);
	}

	private HTML buildItemInList(String text) {
		final HTML html = new HTML(text);
		html.setWidth("200px");
		html.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		html.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				if (style) {

					if (!selected.contains(html)) {
						removeButton.enable();
						selected.add(html);
						html.addStyleName(highlightStyle);
					} else {
						SysDebugger.getInstance().println("selected did contain html");
						selected.remove(html);
						html.removeStyleName(highlightStyle);
						if (selected.isEmpty()) {
							removeButton.disable();
							SysDebugger.getInstance().println("I am in disable");
						}
					}
				}
			}

		});
		html.addStyleName("pointerCursor");
		return html;
	}

	public void clearGlobalList() {
		masterList.clear();
	}

	public void clearItemsInList() {
		selected.clear();
		list.clear();
		listText.clear();
		SysDebugger.getInstance().println("I cleared the list");
	}

	public void disable() {
		textbox.setEnabled(false);
		addButton.disable();
		style = DISABLED;
	}

	public void enable() {
		textbox.setEnabled(true);
		addButton.enable();
		style = ENABLED;
	}

	/**
	 * Returns in csv form the list of items currently in the global list.
	 */
	public String getItemsInGlobalList() {
		StringBuffer csv = new StringBuffer();
		for (int i = 0; i < masterList.size(); i++) {
			csv.append((String) masterList.get(i) + ",");
		}

		if (csv.length() > 0) {
			return csv.substring(0, csv.length() - 1);
		} else
			return csv.toString();
	}

	public ArrayList getItemsInList() {
		return listText;
	}

	/**
	 * Returns in csv form the list of items currently in the listbox.
	 */
	public String getItemsInListAsCSV() {
		StringBuffer csv = new StringBuffer();
		for (int i = 0; i < listText.size(); i++) {
			csv.append((String) listText.get(i) + ",");
		}

		if (csv.length() > 0) {
			return csv.substring(0, csv.length() - 1);
		} else
			return csv.toString();
	}

	/**
	 * accepts a list of items in csv form, and places it in the global list
	 * 
	 * @param csvItems
	 */
	public void setGlobalList(String csvItems) {
		masterList.clear();
		addItemsInGlobalList(csvItems);
	}

	/**
	 * removes whatever is currently in the listbox, and loads it with the items
	 * that is given in csv form
	 * 
	 * @param csvItems
	 */
	public void setItemsInList(String csvItems) {
		clearItemsInList();
		addItemsInList(csvItems);
	}

	private void showMasterList() {
		myMasterListUI.refresh();
		myMasterListUI.show();
	}

	private void updateDeleteItemsInList() {

		for (int i = 0; i < selected.size(); i++) {
			HTML html = (HTML) selected.get(i);
			list.remove(html);
			listText.remove(html.getText());
		}
		selected.clear();
		removeButton.disable();
	}

	private void updateNewItemInList() {
		String text = textbox.getText();
		if (!text.trim().equalsIgnoreCase("")) {
			textbox.setText("");
			addItemInList(text.replace(',', ' '));
		}
	}
}
