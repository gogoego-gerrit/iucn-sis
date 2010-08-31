package org.iucn.sis.shared.api.schemes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;

public class TreePanelBuilder {
	
	private static final String DATA_KEY = "org.iucn.sis.shared.api.schemes.builder.key";

	public static LayoutContainer build(final ComplexListener<Set<TreeDataRow>> saveListener, final SimpleListener cancelListener, final TreeData treeData) {
		return build(saveListener, cancelListener, treeData, new ArrayList<TreeDataRow>());
	}
	
	public static LayoutContainer build(final ComplexListener<Set<TreeDataRow>> saveListener, final SimpleListener cancelListener, final TreeData treeData, Collection<TreeDataRow> selected) {
		final List<CodingOption> checked = new ArrayList<CodingOption>();
		for (TreeDataRow row : selected)
			checked.add(new CodingOption(row));
		
		final TreePanel<CodingOption> tree = new TreePanel<CodingOption>(createTreeStore(treeData));
		tree.setCheckable(true);
		tree.setCheckedSelection(checked);
		tree.addListener(Events.BeforeCheckChange, new Listener<TreePanelEvent<CodingOption>>() {
			public void handleEvent(TreePanelEvent<CodingOption> be) {
				if (be.getItem() != null)
					be.setCancelled(!be.getItem().isCodeable());					
			}
		});
		tree.setIconProvider(new ModelIconProvider<CodingOption>() {
			public AbstractImagePrototype getIcon(CodingOption model) {
				if (model.isCodeable()) {
					//TODO: show the icon-accept image
					/*return new AbstractImagePrototype() {
						public void applyTo(Image image) {
							// TODO Auto-generated method stub
							
						}
						public Image createImage() {
							// TODO Auto-generated method stub
							return null;
						}
						public String getHTML() {
							// TODO Auto-generated method stub
							return null;
						}
					};*/
					return null;
				}
				else
					return null;
			}
		});
		
		//final Tree tree = new Tree();
		//tree.setCheckable(true);

		/*for (TreeDataRow curRow : treeData.getTreeRoots()) {
			String curCode = curRow.getDisplayId();
			String curDesc = curRow.getDescription();
			String curLevelID = curRow.getRowNumber();

			try {
				if (curLevelID.indexOf(".") < 0) {
					if (Integer.parseInt(curLevelID) >= 100)
						continue;
				} else if (Integer.parseInt(curLevelID.split("\\.")[0]) >= 100)
					continue;
			} catch (NumberFormatException ignored) {
				continue;
			}
			
			String displayableDesc = (curLevelID.equals("0") ? "" : curLevelID) + " " + curDesc;

			if (!curRow.getChildren().isEmpty())
				displayableDesc += " (" + curRow.getChildren().size() + ")";

			final TreeItem curItem;
			if (!"true".equals(curRow.getCodeable())) {
				curItem = new TreeItem(displayableDesc) {
					public void setChecked(boolean checked) {
					}
				};
				// curItem.setStyleName("tree-folder");
				// curItem.setIconStyle("icon-folder");
			} else {
				curItem = new TreeItem(displayableDesc);
				curItem.setIconStyle("icon-accept");
			}

			curItem.setId(curCode);
			curItem.setChecked(selected.contains(curRow));

			tree.getRootItem().add(curItem);
			
			addMyChildren(curItem, curRow, selected);

			if (curItem.getData(DATA_KEY) != null)
				curItem.setText(curItem.getText() + " (Items ticked: " + curItem.getData(DATA_KEY).toString() + ")");
		}*/
		
		Button saveSelections = new Button("Save Selections");
		saveSelections.setIconStyle("icon-save");
		saveSelections.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				/*treePath = tree.getRootItem().getPath();
				window.close();
				updateSelected();*/
				final HashSet<TreeDataRow> set = new HashSet<TreeDataRow>();
				for (CodingOption item : tree.getCheckedSelection())
					set.add(item.getRow());
					//if (item.isChecked())
				
				saveListener.handleEvent(set);
			}
		});

		Button cancel = new Button("Cancel");
		cancel.setIconStyle("icon-cancel");
		cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				/*tree.expandPath(treePath);
				window.close();*/
				
				cancelListener.handleEvent();
			}
		});
		
		Button expandAll = new Button("Expand All", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				tree.expandAll();
			}
		});
//		expandAll.setIconStyle("");
		
		Button collapseAll = new Button("Collapse All", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				tree.collapseAll();
			}
		});
//		collapseAll.setIconStyle("");

		final ButtonBar buttonBar = new ButtonBar();
		buttonBar.setAlignment(HorizontalAlignment.RIGHT);
		buttonBar.add(saveSelections);
		buttonBar.add(cancel);
		
		buttonBar.setAlignment(HorizontalAlignment.LEFT);
		buttonBar.add(expandAll);
		buttonBar.add(collapseAll);

		final LayoutContainer container = new LayoutContainer();
		container.add(buttonBar);
		container.add(new HTML("&nbsp<u>Only selections <i>with a check icon</i> " + "will be saved.</u>"));
		container.add(tree);
		
		return container;
	}
	
	private static void addMyChildren(TreeItem item, TreeDataRow data, Collection<TreeDataRow> selected) {
		for (TreeDataRow curRow : data.getChildren()) {
			String curCode = curRow.getDisplayId();
			String curDesc = curRow.getDescription();
			String curLevelID = curRow.getRowNumber();

			try {
				if (curLevelID.indexOf(".") < 0) {
					if (Integer.parseInt(curLevelID) >= 100)
						continue;
				} else if (Integer.parseInt(curLevelID.split("\\.")[0]) >= 100)
					continue;
			} catch (NumberFormatException ignored) {
				continue;
			}

			String displayableDesc = curLevelID + " " + curDesc;			
			if (!curRow.getChildren().isEmpty())
				displayableDesc += " (" + curRow.getChildren().size() + ")";

			final TreeItem curItem;
			if (!"true".equals(curRow.getCodeable())) {
				curItem = new TreeItem(displayableDesc) {
					public void setChecked(boolean checked) {
					}
				};
				// curItem.setStyleName("tree-folder");
			} else {
				curItem = new TreeItem(displayableDesc);
				curItem.setIconStyle("icon-accept");
			}

			curItem.setId(curCode);

			if (selected.contains(curRow)) {
				curItem.setChecked(true);
				if (item.getData(DATA_KEY) == null)
					item.setData(DATA_KEY, new Integer(1));
				else
					item.setData(DATA_KEY, new Integer(((Integer) item.getData(DATA_KEY)).intValue() + 1));
			} else
				curItem.setChecked(false);

			item.add(curItem);
			
			addMyChildren(curItem, curRow, selected);

			if (curItem.getData(DATA_KEY) != null)
				curItem.setText(curItem.getText() + " (Items ticked: " + curItem.getData(DATA_KEY).toString() + ")");
		}
	}
	
	private static TreeStore<CodingOption> createTreeStore(TreeData treeData) {
		TreeStore<CodingOption> store = new TreeStore<CodingOption>();
		for (TreeDataRow row : treeData.getTreeRoots())
			flattenTree(store, new CodingOption(row));
		
		return store;
	}
	
	private static void flattenTree(TreeStore<CodingOption> store, CodingOption parent) {
		if (parent.isValid()) {
			store.add(parent, false);
			for (TreeDataRow child : parent.getRow().getChildren())
				flattenTree(store, new CodingOption(child));
		}
	}
	
	private static class CodingOption extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		private final TreeDataRow row;
		
		public CodingOption(TreeDataRow row) {
			super();
			this.row = row;
			
			set("text", getDescription());
			set("value", row.getDisplayId());
		}
		
		public String getValue() {
			return get("value");
		}
		
		public TreeDataRow getRow() {
			return row;
		}
		
		public boolean isCodeable() {
			return "true".equals(row.getCodeable());
		} 
		
		private String getDescription() {
			String curDesc = row.getDescription();
			String curLevelID = row.getRowNumber();
			
			String displayableDesc = (curLevelID.equals("0") ? "" : curLevelID) + " " + curDesc;

			if (!row.getChildren().isEmpty())
				displayableDesc += " (" + row.getChildren().size() + ")";
			
			return displayableDesc;
		}
		
		private boolean isValid() {
			String curLevelID = row.getRowNumber();

			try {
				if (curLevelID.indexOf(".") < 0) {
					if (Integer.parseInt(curLevelID) >= 100)
						return false;
				} else if (Integer.parseInt(curLevelID.split("\\.")[0]) >= 100)
					return false;
			} catch (NumberFormatException ignored) {
				return false;
			}
			
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((row == null) ? 0 : row.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CodingOption other = (CodingOption) obj;
			if (row == null) {
				if (other.row != null)
					return false;
			} else if (!row.equals(other.row))
				return false;
			return true;
		}
		
	}
}
