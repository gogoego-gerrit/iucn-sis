package org.iucn.sis.shared.api.schemes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class TreePanelBuilder {
	
	public static LayoutContainer build(final ComplexListener<Set<TreeDataRow>> saveListener, final SimpleListener cancelListener, final TreeData treeData) {
		return build(saveListener, cancelListener, treeData, new ArrayList<TreeDataRow>());
	}
	
	public static LayoutContainer build(final ComplexListener<Set<TreeDataRow>> saveListener, final SimpleListener cancelListener, final TreeData treeData, Collection<TreeDataRow> selected) {
		final Map<String, CodingOption> checked = new HashMap<String, CodingOption>();
		for (TreeDataRow row : selected) 
			checked.put(row.getDisplayId(), new CodingOption(row));
		
		final TreePanel<CodingOption> tree = new TreePanel<CodingOption>(createTreeStore(treeData, checked));
		tree.setAutoLoad(true);
		tree.setCheckable(true);
		
		tree.setDisplayProperty("text");
		tree.addListener(Events.BeforeCheckChange, new Listener<TreePanelEvent<CodingOption>>() {
			public void handleEvent(TreePanelEvent<CodingOption> be) {
				if (be.getItem() != null)
					be.setCancelled(!be.getItem().isCodeable());					
			}
		});
		
		Button saveSelections = new Button("Save Selections");
		saveSelections.setIconStyle("icon-save");
		saveSelections.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final HashSet<TreeDataRow> set = new HashSet<TreeDataRow>();
				for (CodingOption item : tree.getCheckedSelection())
					set.add(item.getRow());
				
				saveListener.handleEvent(set);
			}
		});

		Button cancel = new Button("Cancel");
		cancel.setIconStyle("icon-cancel");
		cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				cancelListener.handleEvent();
			}
		});
		
		Button expandAll = new Button("Expand All", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				tree.expandAll();
			}
		});
		
		Button collapseAll = new Button("Collapse All", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				tree.collapseAll();
			}
		});

		final ButtonBar buttonBar = new ButtonBar();
		buttonBar.setAlignment(HorizontalAlignment.RIGHT);
		buttonBar.add(saveSelections);
		buttonBar.add(cancel);
		
		buttonBar.setAlignment(HorizontalAlignment.LEFT);
		buttonBar.add(expandAll);
		buttonBar.add(collapseAll);

		final LayoutContainer container = new LayoutContainer() {
			protected void afterRender() {
				super.afterRender();
				Timer t = new Timer() {
					public void run() {
						tree.setCheckedSelection(new ArrayList<CodingOption>(checked.values()));
					}
				};
				t.schedule(1000);
			}
		};
		container.add(buttonBar);
		container.add(new HTML("&nbsp<u>Only selections <i>with a check icon</i> " + "will be saved.</u>"));
		container.add(tree);
		
		return container;
	}

	private static TreeStore<CodingOption> createTreeStore(TreeData treeData, Map<String, CodingOption> selection) {
		TreeStore<CodingOption> store = new TreeStore<CodingOption>();
		store.setStoreSorter(new StoreSorter<CodingOption>(new PortableAlphanumericComparator()));
		store.setKeyProvider(new ModelKeyProvider<CodingOption>() {
			public String getKey(CodingOption model) {
				return model.getValue();
			}
		});
		for (TreeDataRow row : treeData.getTreeRoots()) {
			CodingOption option;
			if (selection.containsKey(row.getDisplayId()))
				option = selection.get(row.getDisplayId());
			else
				option = new CodingOption(row);
			
			if (option.isValid()) {
				flattenTree(store, selection, option);
				store.add(option, true);
			}
		}
		store.sort("text", SortDir.ASC);
		return store;
	}
	
	private static void flattenTree(TreeStore<CodingOption> store, Map<String, CodingOption> selection, CodingOption parent) {
		if (parent.isValid()) {
			for (TreeDataRow current : parent.getRow().getChildren()) {
				CodingOption child;
				if (selection.containsKey(current.getDisplayId())) {
					child = selection.get(current.getDisplayId());
					parent.incrementNumChildrenSelected();
				}
				else
					child = new CodingOption(current);
				parent.add(child);
				
				flattenTree(store, selection, child);
			}
		}
	}
	
	private static class CodingOption extends BaseTreeModel {
		
		private static final long serialVersionUID = 1L;
		
		private final TreeDataRow row;
		private final String rowID;
		
		private int numChildrenSelected;
		
		public CodingOption(TreeDataRow row) {
			super();
			this.row = row;
			this.rowID = row.getDisplayId();
			this.numChildrenSelected = 0;
			
			set("text", getDescription());
			set("value", row.getDisplayId());
		}
		
		public void incrementNumChildrenSelected() {
			numChildrenSelected++;
			set("text", getDescription());
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
			
			StringBuilder displayableDesc = new StringBuilder();
			displayableDesc.append(curLevelID.equals("0") ? "" : curLevelID);
			displayableDesc.append(". ");
			displayableDesc.append(curDesc);

			if (!row.getChildren().isEmpty()) {
				displayableDesc.append(" (" + row.getChildren().size());
				if (numChildrenSelected > 0) {
					displayableDesc.append(", ");
					displayableDesc.append(numChildrenSelected);
					displayableDesc.append(" selected");
				}
				displayableDesc.append(')');
			}
			
			return displayableDesc.toString();
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
			result = prime * result + ((rowID == null) ? 0 : rowID.hashCode());
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
			if (rowID == null) {
				if (other.rowID != null)
					return false;
			} else if (!rowID.equals(other.rowID))
				return false;
			return true;
		}
		
	}
}
