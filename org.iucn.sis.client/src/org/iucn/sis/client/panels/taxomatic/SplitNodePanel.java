package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class SplitNodePanel extends LayoutContainer {

	private DataList children;
	private HashMap<String, ArrayList<String>> parentToChildList;
	private Taxon  currentNode;

	private static final int HEADER_HEIGHT = 65;

	public static final int PANEL_HEIGHT = 410;
	public static final int PANEL_WIDTH = 590;

	public SplitNodePanel() {
		parentToChildList = new HashMap<String, ArrayList<String>>();
		currentNode = TaxonomyCache.impl.getCurrentTaxon();
		load();
	}

	private HTML getInstructions() {
		return new HTML("<b>Instructions:</b> Click \"Create New Taxa\" to create a new "
				+ "taxonomic taxon (taxa), then move the children below to those "
				+ "new groups.  Use CTRL+Click to select multiple children at "
				+ "the same time.  You should move all the children to a new " + "taxonomic concept as "
				+ currentNode.getFullName() + " will become deprecated.");
	}

	private LayoutContainer getLeftSide() {
		// BorderLayout layout = new BorderLayout();
		RowLayout layout = new RowLayout(Orientation.VERTICAL);
		// layout.setMargin(0);
		// layout.setSpacing(0);

		LayoutContainer container = new LayoutContainer();
		container.setLayout(layout);
		container.setLayoutOnChange(true);
		container.setSize(PANEL_WIDTH / 2, PANEL_HEIGHT);

		children = new DataList();
		children.setSelectionMode(SelectionMode.MULTI);

		TaxonomyCache.impl.getTaxonChildren(currentNode.getId() + "", new GenericCallback<List<Taxon >>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Error", "Could not fetch children, please try again later.");
			}

			public void onSuccess(List<Taxon > result) {
				Iterator it = ((ArrayList) result).listIterator();
				while (it.hasNext()) {
					Taxon  next = (Taxon ) it.next();
					DataListItem li = new DataListItem(next.getFullName());
					li.setData("node", next);
					children.add(li);
				}
			}
		});

		container.add(new HTML("Children currently in " + currentNode.getFullName() + ":"), new RowData(1d, 25));
		container.add(children, new RowData(1d, 1d));

		return container;
	}

	public LayoutContainer getRightSide() {
		RowLayout layout = new RowLayout();
		// layout.setMargin(0);
		// layout.setSpacing(0);

		ButtonBar south = new ButtonBar();
		south.setAlignment(HorizontalAlignment.RIGHT);
		final Button complete = new Button("Complete Split", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				onClose();
			}
		});
		south.add(complete);
		complete.setEnabled(false);

		LayoutContainer container = new LayoutContainer();
		container.setLayout(layout);
		container.setLayoutOnChange(true);

		final DataList moveList = new DataList();
		final ListBox listBox = new ListBox();
		listBox.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				moveList.removeAll();
				ArrayList list = parentToChildList.get(listBox.getValue(listBox.getSelectedIndex()));
				if (list == null)
					return;
				Iterator iterator = list.listIterator();
				while (iterator.hasNext()) {
					Taxon  cur = (Taxon ) iterator.next();
					DataListItem li = new DataListItem(cur.getFullName());
					li.setData("node", cur);
					moveList.add(li);
				}
				layout();
			}
		});

		Menu m = new Menu();
		MenuItem item = new MenuItem();
		item.setText("Remove");
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				DataListItem item = (DataListItem) ce.getSource();
				ArrayList list = parentToChildList.get(listBox.getValue(listBox.getSelectedIndex()));
				if (list != null)
					list.remove(item.getData("node"));

				moveList.remove(item);
				children.add(item);
				layout();
			}
		});
		m.add(item);
		moveList.setContextMenu(m);

		VerticalPanel table = new VerticalPanel();
		table.add(new HTML("Current Taxonomic Group: "));
		table.add(listBox);
		listBox.setWidth("100%");
		listBox.setEnabled(false);

		final ButtonBar bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.RIGHT);
		final Button addChild = new Button("Add Child", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<DataListItem> sel = children.getSelectedItems();
				if (sel == null)
					return;
				ArrayList list = parentToChildList.get(listBox.getValue(listBox.getSelectedIndex()));
				for (DataListItem selected : sel) {
					children.remove(selected);
					if (!list.contains(selected.getData("node"))) {
						list.add(selected.getData("node"));
						DataListItem item = new DataListItem(selected.getText());
						item.setData("node", selected.getData("node"));
						moveList.add(item);
					}
				}
				layout();
			}
		});
		addChild.setEnabled(false);
		bar.add(addChild);
		bar.add(new Button("Create New Taxon", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				final Listener listener = new Listener() {
					public void handleEvent(BaseEvent be) {
						SysDebugger.getInstance().println("Handling Event...");
						CreateNewTaxonPanel.impl.removeListener(Events.StateChange, this);
						Taxon  newNode = (Taxon ) be.getSource();
						parentToChildList.put(newNode.getId() + "", new ArrayList());
						listBox.addItem(newNode.getFullName(), newNode.getId() + "");
						listBox.setSelectedIndex(listBox.getItemCount() - 1);
						listBox.setEnabled(true);
						moveList.removeAll();
						addChild.setEnabled(true);
					}
				};
				TaxonomyCache.impl.fetchTaxon(currentNode.getParentId(), false, new GenericCallback<Taxon >() {
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Error", "Could not "
								+ "find parent level to attach new taxonomic concept to.");
					}

					public void onSuccess(Taxon  result) {
						CreateNewTaxonPanel.impl.setRefresh(false);
						CreateNewTaxonPanel.impl.addListener(Events.StateChange, listener);
						CreateNewTaxonPanel.impl.open(result);
						complete.setEnabled(true);
					}
				});
			}
		}));

		container.add(table, new RowData(1d, 25));
		container.add(bar, new RowData(1d, 25));
		container.add(new HTML("Children to add to new taxon:"), new RowData(1d, 25));
		container.add(moveList, new RowData(1d, 1d));
		container.add(south, new RowData(1d, 25));

		return container;
	}

	public void load() {

		BorderLayout layout = new BorderLayout();
		// layout.setMargin(2);

		LayoutContainer full = new LayoutContainer();
		full.setLayout(layout);
		full.setLayoutOnChange(true);

		full.add(getInstructions(), new BorderLayoutData(LayoutRegion.NORTH, HEADER_HEIGHT));
		full.add(getLeftSide(), new BorderLayoutData(LayoutRegion.WEST, PANEL_WIDTH / 2 - 5));
		full.add(getRightSide(), new BorderLayoutData(LayoutRegion.CENTER, PANEL_WIDTH / 2 - 5));
		full.setSize(PANEL_WIDTH + 10, PANEL_HEIGHT + 5);
		add(full);
	}

	public void onClose() {

		final BaseEvent be = new BaseEvent(this);
		be.setCancelled(false);

		String errorMessage = null;

		if (currentNode.getLevel() < TaxonLevel.SPECIES) {
			if (parentToChildList.size() < 1) {
				errorMessage = "You must create at least one new taxon to split " + currentNode.getFullName()
						+ " into.";
			}

		}

		else {
			if (parentToChildList.size() < 2) {
				errorMessage = "You must create at least two new taxon to split " + currentNode.getFullName()
						+ " into.";
			} else if (children.getItems().size() != 0) {
				errorMessage = "You must remove all children from " + currentNode.getFullName() + ".";
			}

		}

		if (errorMessage == null) {
			TaxomaticUtils.impl.performSplit(currentNode, parentToChildList, new GenericCallback<String>() {

				public void onFailure(Throwable arg0) {
					WindowUtils.infoAlert("Error", "An error occurred while " + "splitting the nodes.  The new nodes "
							+ "have been created, but the split has not occurred. "
							+ "Check to make sure that no one else" + " is currently using the taxa, and retry.");

				}

				public void onSuccess(String arg0) {

					if (currentNode.getLevel() < TaxonLevel.SPECIES) {
						WindowUtils.confirmAlert("Saved", "The split was successful.  However, the status of "
								+ currentNode.getFullName()
								+ " was not modified.  Would you like to edit the status of "
								+ currentNode.getFullName() + "?", new WindowUtils.MessageBoxListener() {

							@Override
							public void onNo() {
								// TODO Auto-generated method stub

							}

							@Override
							public void onYes() {
								final Window shell = WindowUtils.getWindow(false, false,
										"Basic Taxon Information Editor");
								TaxonBasicEditor editor = new TaxonBasicEditor(
										ClientUIContainer.bodyContainer.tabManager.panelManager);
								editor.addListener(Events.Close, new Listener() {
									public void handleEvent(BaseEvent be) {
										shell.hide();
									}
								});
								shell.setLayout(new FillLayout());
								shell.add(editor);
								shell.show();
								shell.setSize(500, 300);
								shell.center();

							}
						}, "Yes, edit", "No, all finished");
					} else {
						WindowUtils.infoAlert("Saved", "Changes saved.");
					}
					fireEvent(Events.Close, be);

				}

			});
		}

		else {
			WindowUtils.errorAlert("Error", errorMessage);
		}

	}

}
