package org.iucn.sis.client.components.panels.workingsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.client.components.panels.PanelManager;
import org.iucn.sis.shared.BaseAssessment;

import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.extjs.gxt.ui.client.widget.treetable.TreeTable;
import com.extjs.gxt.ui.client.widget.treetable.TreeTableColumn;
import com.extjs.gxt.ui.client.widget.treetable.TreeTableColumnModel;
import com.google.gwt.user.client.ui.Image;
import com.solertium.lwxml.gwt.debug.SysDebugger;

public class AsyncTree {
	static class AssessmentViewer extends BaseModelData {

		public AssessmentViewer() {

		}

		public AssessmentViewer(String name, String created, String modified, String status, String evaluated, String id) {

			if (BaseAssessment.DRAFT_ASSESSMENT_STATUS.equalsIgnoreCase(name))
				set("name", "Draft Assessment");
			else if (BaseAssessment.USER_ASSESSMENT_STATUS.equalsIgnoreCase(name))
				set("name", "User Assessment");
			else
				set("name", "Published Assessment");
			if (created == null || created.trim().equals(""))
				set("created", "N/A");
			else
				set("created", created);
			if (modified == null || modified.trim().equals(""))
				set("modified", "N/A");
			else
				set("modified", modified);
			if (status == null || status.trim().equals(""))
				set("status", "N/A");
			else
				set("status", status);
			if (evaluated == null || evaluated.trim().equals(""))
				set("evaluated", "N/A");
			else
				set("evaluated", evaluated);

			set("id", id);
		}

		public String getCreated() {
			return get("created");
		}

		public String getEvaluated() {
			return get("evaluated");
		}

		public String getModified() {
			return get("modified");
		}

		public String getStatus() {
			return get("status");
		}

	}

	class Folder extends BaseModelData {

		public Folder() {
		}

		public Folder(String name, String id) {
			set("name", name);
			set("id", id);
		}

		public Folder(String name, String id, ModelData[] children) {
			this(name, id);
			for (int i = 0; i < children.length; i++)
				addChild(children[i]);
		}

		public void addChild(ModelData child) {
			addChild(child);
		}

		public void addChildren(ModelData[] children) {
			for (int i = 0; i < children.length; i++)
				addChild(children[i]);
		}

		public Image getIcon() {
			return (Image) get("icon");
		}

		public String getName() {
			return get("name");
		}

	}

	class Message extends BaseModelData {
		public Message(String name) {
			set("name", name);
		}

		public String getName() {
			return get("name");
		}

	}

	private final TreeTable tree;
	private final TreeBinder<ModelData> binder;
	private final TreeStore<ModelData> store;

	private String mode;

	private final PanelManager manager;

	private HashMap taxonNameToTaxonID = null;

	public AsyncTree(final PanelManager manager) {
		this.manager = manager;
		// root = new Folder("root", null);
		taxonNameToTaxonID = new HashMap();

		ArrayList<TreeTableColumn> columns = new ArrayList<TreeTableColumn>();
		columns.add(new TreeTableColumn("Taxon Name", 0.35f));
		columns.get(0).setMinWidth(200);
		columns.add(new TreeTableColumn("Date Assessed", 0.15f));
		columns.add(new TreeTableColumn("Date Modified", 0.15f));
		columns.add(new TreeTableColumn("Evaluator", 0.25f));
		columns.add(new TreeTableColumn("Status", 0.1f));

		TreeTableColumnModel cm = new TreeTableColumnModel(columns);

		tree = new TreeTable(cm) {
			@Override
			public boolean expandPath(String path) {
				SysDebugger.getInstance().println("This is the path to be expanded " + path);
				return super.expandPath(path);
			}

			@Override
			public void setSize(String width, String height) {
				mode = manager.workingSetFullPanel.getMode();
				if (mode.equals(WorkingSetFullPanel.READ)) {
					width = (manager.workingSetFullPanel.content.getWidth() - 10) + "px";
					height = (manager.workingSetFullPanel.content.getHeight()
							- manager.workingSetFullPanel.basicInfo.getOffsetHeight() - 90)
							+ "px";

					super.setSize(width, height);
				} else {
					width = (manager.workingSetFullPanel.content.getWidth() - 10) + "px";
					height = (manager.workingSetFullPanel.content.getHeight()
							- manager.workingSetFullPanel.basicInfo.getOffsetHeight() - 140)
							+ "px";
					super.setSize(width, height);
				}
			}
		};
		tree.setItemIconStyle("icon-editDocument");

		store = new TreeStore<ModelData>();

		binder = new TreeBinder<ModelData>(tree, store) {
			// public Object getParent(Object element) {
			// return ((Model) element).getParent();
			// }

			// public boolean hasChildren(Object element) {
			// if (element instanceof Folder &&
			// !((Folder)element).getAsString("name").equals(
			// "No assessments to view.")){
			// return true;
			// }
			// return false;
			//			
			// }
			//			
			// public void getChildren(Object parent,
			// IAsyncContentCallback callback) {
			//				
			// Folder folder = (Folder) parent;
			// SysDebugger.getInstance().println(
			// "This is the folder's information id" + folder.getAsString("id")
			// + " and name " + folder.getAsString("name"));
			// if (!folder.equals(root))
			// addAssessmentsToFolder(folder, callback, viewer, this);
			// else{
			// addTaxonToRoot(callback);
			// }
			//				
			// }

			@Override
			protected void onDataChanged(StoreEvent se) {
				// super.onDataChanged(se);
			}

			// public void inputChanged(Viewer viewer, Object oldInput, Object
			// newInput) { }

		};

		tree.addListener(Events.CellDoubleClick, new Listener() {
			public void handleEvent(BaseEvent be) {
				tree.getSelectionModel().select((TreeItem) be.getSource(), false);
				((TreeItem) be.getSource()).fireEvent(Events.Expand);
			}
		});

		binder.setDisplayProperty("name");
		binder.addSelectionChangedListener(new SelectionChangedListener<ModelData>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<ModelData> se) {
				ModelData selection = se.getSelectedItem();
				if (selection != null) {
					String name = selection.get("name");
					if (selection instanceof Folder) {
						SysDebugger.getInstance().println("I am in the viewer selection changed listener thingy");
						store.getChildren(selection);
						// cp.getChildren(selection, new
						// IAsyncContentCallback(){
						// public void setElements(Object[] elements) {}
						// });
					}
				}
			}
		});

	}

	private Message failureMessage() {
		return (new Message("Unable to load assessments."));
	}

	public TreeBinder<ModelData> getCp() {
		return binder;
	}

	public HashMap getTaxonNameToTaxonID() {
		return taxonNameToTaxonID;
	}

	public TreeTable getTree() {
		return tree;
	}

	public TreeStore<ModelData> getViewer() {
		return store;
	}

	public void resize() {
		tree.setSize(1, 1);
	}

	// public Folder getRoot() {
	// return root;
	// }

	public void update() {
		for (ModelData root : store.getRootItems())
			update(root);
	}

	public void update(ModelData parent) {
		// List<ModelData> children = viewer.getChildren(parent);
		store.update(parent);
		manager.workingSetFullPanel.content.layout();
		// cp.getChildren(parent, new IAsyncContentCallback(){
		// public void setElements(Object[] elements) {
		// SysDebugger.getInstance().println("I set " + elements.length +
		// " elements ");
		// viewer.refresh();
		// manager.workingSetFullPanel.content.layout();
		// }
		// });
	}

	public void update(String csvIDS) {
		if (csvIDS == null) {
			update();
		} else {
			String[] ids = csvIDS.split(",");
			List<ModelData> list = store.getRootItems();
			for (int i = 0; i < ids.length; i++) {
				boolean cont = true;
				for (int j = 0; j < list.size() && cont; j++) {
					String id = ((Folder) list.get(j)).get("id");
					if (id.equals(ids[i])) {
						cont = false;
						update(list.get(j));
					}
				}
			}
		}
	}

}
