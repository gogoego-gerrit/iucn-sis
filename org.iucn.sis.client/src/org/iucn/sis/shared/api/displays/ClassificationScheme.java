package org.iucn.sis.shared.api.displays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.DefinitionCache;
import org.iucn.sis.client.api.caches.NotesCache;
import org.iucn.sis.client.api.caches.ReferenceCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.shared.api.acl.InsufficientRightsException;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.data.DisplayDataProcessor;
import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.structures.ClassificationInfo;
import org.iucn.sis.shared.api.structures.Structure;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.extjs.client.GenericPagingLoader;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.portable.PortableAlphanumericComparator;

/**
 * Represents a classification scheme field.
 * 
 * @author adam.schwartz
 */
public class ClassificationScheme extends Display {
	
	public class ClassificationSchemeEntry extends BaseModel implements Referenceable {
		private static final long serialVersionUID = -3863528937477858112L;
		
		private String entryCanonicalName;
		//final Entry curEntry;
		private Structure struct;
		private Object key;
		private ArrayList<String> pretty;
		
		public ClassificationSchemeEntry(String key, Structure struct) {
			//super(struct.extractModelData().getProperties());
			super();
			this.struct = struct;
			
			setKey(key);
			synchronizeWithModel();
		}

		public void synchronizeWithModel(){
			pretty = new ArrayList<String>();
			
			/*String xml = "<root>"+struct.toXML()+"</root>";

			NativeDocument doc = NativeDocumentFactory.newNativeDocument();
			doc.parse(xml);

			NativeNodeList structs = doc.getDocumentElement().getElementsByTagName("structure");
			 */
			
			ArrayList<String> raw = new ArrayList<String>();
			for (ClassificationInfo info : struct.getClassificationInfo()) {
			//for(int i=0;i<struct.extractDescriptions().size();i++){
				//set((String)struct.extractDescriptions().get(i), structs.elementAt(i).getTextContent());
				//raw.add(structs.elementAt(i).getTextContent());
				set(info.getDescription(), info.getData());
				raw.add(info.getData());
			}
			try{
				struct.getDisplayableData(raw, pretty, 0);
			} catch (Exception e) {
				// ignore. Used to catch dependant structures that we do not care about.
			}
		}

		public Object getKey(){
			return key;
		}

		public void setKey(String key){
			this.key = key;
			entryCanonicalName = canonicalName + "." + key;
			set(description.replaceAll("\\s", ""), (String) codeToSelectedDesc.get(key));
		}

		public Widget getDetailsWidget(){
			VerticalPanel vp = new VerticalPanel();

			if (!inViewOnlyMode) {
				Widget structWidget = struct.generate();
				structWidget.addStyleName("leftMargin15");
				vp.add(structWidget);
			} else {
				Widget structWidget = struct.generateViewOnly();
				structWidget.addStyleName("leftMargin15");
				vp.add(structWidget);
			}

			return vp;
		}

		public void addReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
			ReferenceCache.getInstance().addReferences(AssessmentCache.impl.getCurrentAssessment().getId(),
					references);

			ReferenceCache.getInstance().addReferencesToAssessmentAndSave(references, entryCanonicalName, callback);
		}

		public Set<Reference> getReferencesAsList() {
			return AssessmentCache.impl.getCurrentAssessment().getField(entryCanonicalName).getReference();
		}

		public void onReferenceChanged(GenericCallback<Object> callback) {
			try {
				AssessmentClientSaveUtils.saveAssessment(null, AssessmentCache.impl.getCurrentAssessment(), callback);
			} catch (InsufficientRightsException e) {
				WindowUtils.errorAlert("Insufficient Permissions", "You do not have "
						+ "permission to modify this assessment. The changes you " + "just made will not be saved.");
			}
		}

		public void removeReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
			int removed = 0;
			for (int i = 0; i < references.size(); i++)
				if (AssessmentCache.impl.getCurrentAssessment().getField(
						entryCanonicalName).getReference().remove(references.get(i)) );
					removed++;

			if (removed > 0) {
				try {
					AssessmentClientSaveUtils.saveAssessment(null, AssessmentCache.impl.getCurrentAssessment(), callback);
				} catch (InsufficientRightsException e) {
					WindowUtils
					.errorAlert("Insufficient Permissions", "You do not have "
							+ "permission to modify this assessment. The changes you "
							+ "just made will not be saved.");
				}
			}
		}
	}
	

	/**
	 * HashMap(String, Structure)
	 */
	private HashMap<String, Structure> selected = null;
	private Tree tree = null;

	private String treePath = "";

	/*
	 * ArrayList<String, String>
	 */
	private HashMap<String, String> codeToLevelID = null;

	/*
	 * ArrayList<String, String>
	 */
	private HashMap<String, String> codeToDesc = null;

	/*
	 * ArrayList<String, String>
	 */
	private HashMap<String, String> codeToSelectedDesc = null;

	/*
	 * ArrayList<String, String>
	 */
	private HashMap<String, String> codeToParentCode = null;
	
	/*
	 * ArrayList<String, Boolean>
	 */
	private HashMap<String, Boolean> codeToCodeable = null;

	/*
	 * ArrayList<String, String>
	 */
	private HashMap<String, String> sortOnMe = null;

	private TreeData treeData = null;


	private Button modifyButton = null;
	private Window window = null;

	private LayoutContainer container = null;

	private String definition = "";

	private boolean inViewOnlyMode = false;

	private Set<Reference> curRefs = null;
	private List<Notes> curNotes = null;

	private LayoutContainer innerContainer = null;
	private LayoutContainer hp;

	private ListStore<ClassificationSchemeEntry> store;
	private GenericPagingLoader<ClassificationSchemeEntry> pagingLoader;
	private PagingToolBar pagingBar;

	private Grid<ClassificationSchemeEntry> grid;
	
	private final String DATA_KEY = "classSchemeDataKey";

	public ClassificationScheme(TreeData displayData) {
		super(displayData);

		treeData = displayData;
		selected = new HashMap<String, Structure>();
		codeToDesc = new HashMap<String, String>();
		codeToLevelID = new HashMap<String, String>();
		codeToSelectedDesc = new HashMap<String, String>();
		codeToParentCode = new HashMap<String, String>();
		codeToCodeable = new HashMap<String, Boolean>();
		sortOnMe = new HashMap<String, String>();


		for (Iterator iter = treeData.getTreeRoots().listIterator(); iter.hasNext();)
			buildMaps((TreeDataRow) iter.next(), null, "");

		buildDefinition();
		DefinitionCache.impl.setDefinition(description.toLowerCase(), definition);
	}

	public Grid<ClassificationSchemeEntry> getGrid() {
		return grid;
	}

	private void addMyChildren(TreeItem item, TreeDataRow data) {
		for (Iterator iter = data.getChildren().listIterator(); iter.hasNext();) {
			TreeDataRow curRow = (TreeDataRow) iter.next();

			String curCode = (String) curRow.getDisplayId();
			String curDesc = (String) codeToDesc.get(curCode);
			String curLevelID = (String) codeToLevelID.get(curCode);

			try {
				if (curLevelID.indexOf(".") < 0) {
					if (Integer.parseInt(curLevelID) >= 100)
						continue;
				} else if (Integer.parseInt(curLevelID.split("\\.")[0]) >= 100)
					continue;
			} catch (NumberFormatException ignored) {
			}

			TreeItem curItem = null;

			String displayableDesc = curLevelID + " " + curDesc;

			if (curRow.getChildren().size() > 0)
				displayableDesc += " (" + curRow.getChildren().size() + ")";

			if (!((Boolean) codeToCodeable.get(curCode)).booleanValue()) {
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

			if (selected.containsKey(curCode)) {
				curItem.setChecked(true);
				if (item.getData(DATA_KEY) == null)
					item.setData(DATA_KEY, new Integer(1));
				else
					item.setData(DATA_KEY, new Integer(((Integer) item.getData(DATA_KEY)).intValue() + 1));
			} else
				curItem.setChecked(false);

			item.add(curItem);
			addMyChildren(curItem, curRow);

			if (curItem.getData(DATA_KEY) != null)
				curItem.setText(curItem.getText() + " (Items ticked: " + curItem.getData(DATA_KEY).toString() + ")");
		}
	}

	private void buildChildrenDefinition(TreeDataRow curParent) {
		for (Iterator iter = curParent.getChildren().listIterator(); iter.hasNext();) {
			TreeDataRow curRow = (TreeDataRow) iter.next();

			String curCode = (String) curRow.getDisplayId();
			String curDesc = (String) codeToDesc.get(curCode);
			String curLevelID = (String) codeToLevelID.get(curCode);

			try {
				if (curLevelID.indexOf(".") < 0) {
					if (Integer.parseInt(curLevelID) >= 100)
						continue;
				} else if (Integer.parseInt(curLevelID.split("\\.")[0]) >= 100)
					continue;
			} catch (NumberFormatException ignored) {
			}

			try {
				int depth = Integer.parseInt(curRow.getDepth());
				for (int i = 0; i < depth; i++)
					definition += "&nbsp;&nbsp;";
			} catch (Exception e) {
			}

			definition += curLevelID + " - " + curDesc + "<br />";

			buildChildrenDefinition(curRow);
		}
	}

	private void buildDefinition() {
		for (Iterator iter = treeData.getTreeRoots().listIterator(); iter.hasNext();) {
			TreeDataRow curRow = (TreeDataRow) iter.next();

			String curCode = (String) curRow.getDisplayId();
			String curDesc = (String) codeToDesc.get(curCode);
			String curLevelID = (String) codeToLevelID.get(curCode);

			definition += curLevelID + " - " + curDesc + " (" + curRow.getChildren().size() + ")" + "<br />";

			buildChildrenDefinition(curRow);
		}
	}

	private void buildMaps(TreeDataRow curRow, TreeDataRow parent, String parentDesc) {
		String code = curRow.getDisplayId();
		String levelID = curRow.getRowNumber();
		String description = curRow.getDescription();
		boolean codeable = curRow.getCodeable().equalsIgnoreCase("true");

		String selectedDescription = "";

		if (levelID.charAt(0) >= '0' && levelID.charAt(0) <= '9')
			selectedDescription += levelID + ". ";

		selectedDescription += parentDesc + description;

		codeToDesc.put(code, description);
		codeToLevelID.put(code, levelID);
		codeToSelectedDesc.put(code, selectedDescription);
		codeToCodeable.put(code, new Boolean(codeable));
		if( parent != null )
			codeToParentCode.put(code, parent.getDisplayId());

		if (levelID.charAt(0) >= '0' && levelID.charAt(0) <= '9')
			sortOnMe.put(code, levelID);
		else
			sortOnMe.put(code, description);

		for (Iterator iter = curRow.getChildren().listIterator(); iter.hasNext();)
			buildMaps((TreeDataRow) iter.next(), curRow, levelID.equals("0") ? "" : parentDesc + description + " -> ");
	}

	private LayoutContainer buildTreePanel() {
		container = new LayoutContainer();

		Button saveSelections = new Button("Save Selections");
		saveSelections.setIconStyle("icon-save");
		saveSelections.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				treePath = tree.getRootItem().getPath();
				window.close();
				updateSelected();
			}
		});

		Button cancel = new Button("Cancel");
		cancel.setIconStyle("icon-cancel");
		cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				tree.expandPath(treePath);
				window.close();
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

		ButtonBar buttonBar = new ButtonBar();
		buttonBar.setAlignment(HorizontalAlignment.RIGHT);
		buttonBar.add(saveSelections);
		buttonBar.add(cancel);
		
		buttonBar.setAlignment(HorizontalAlignment.LEFT);
		buttonBar.add(expandAll);
		buttonBar.add(collapseAll);

		tree = new Tree();
		tree.setCheckable(true);

		for (Iterator iter = treeData.getTreeRoots().listIterator(); iter.hasNext();) {
			TreeDataRow curRow = (TreeDataRow) iter.next();

			String curCode = (String) curRow.getDisplayId();
			String curDesc = (String) codeToDesc.get(curCode);
			String curLevelID = (String) codeToLevelID.get(curCode);

			try {
				if (curLevelID.indexOf(".") < 0) {
					if (Integer.parseInt(curLevelID) >= 100)
						continue;
				} else if (Integer.parseInt(curLevelID.split("\\.")[0]) >= 100)
					continue;
			} catch (NumberFormatException ignored) {
			}

			TreeItem curItem = null;
			String displayableDesc = (curLevelID.equals("0") ? "" : curLevelID) + " " + curDesc;

			if (curRow.getChildren().size() > 0)
				displayableDesc += " (" + curRow.getChildren().size() + ")";

			if (!((Boolean) codeToCodeable.get(curCode)).booleanValue()) {
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

			if (selected.containsKey(curCode))
				curItem.setChecked(true);
			else
				curItem.setChecked(false);

			tree.getRootItem().add(curItem);
			addMyChildren(curItem, curRow);

			if (curItem.getData(DATA_KEY) != null)
				curItem.setText(curItem.getText() + " (Items ticked: " + curItem.getData(DATA_KEY).toString() + ")");
		}

		container.add(buttonBar);
		container.add(new HTML("&nbsp<u>Only selections <i>with a check icon</i> " + "will be saved.</u>"));
		container.add(tree);
		return container;
	}

	public Widget getProtectedWidgetContent(boolean viewOnly) {
		return generateContent(viewOnly);
	}

	protected Widget generateContent(boolean viewOnly) {
		inViewOnlyMode = viewOnly;

		if (displayPanel == null) {
			displayPanel = new VerticalPanel();
			((VerticalPanel)displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
			
			innerContainer = new LayoutContainer();
			pagingLoader = new GenericPagingLoader<ClassificationSchemeEntry>();
			store = new ListStore<ClassificationSchemeEntry>(pagingLoader.getPagingLoader());
			pagingBar = new PagingToolBar(15);
			pagingLoader.getPagingLoader().addLoadListener(new LoadListener() {
				public void loaderLoad(LoadEvent le) {
					innerContainer.removeAll();
				}
			});
			pagingBar.bind(pagingLoader.getPagingLoader());

			/**
			 * This listener is a hack that fixes some horrible behavioral issues with the grid's
			 * sizing. Essentially, if a page on the assessment data browser was left open to a 
			 * classification scheme and an assessment was set as current from a different tab, 
			 * e.g. from the recent assessment panel, *without* first triggering a switch to the 
			 * assessment data browser tab (as happens with the monkey navigator), the grid will 
			 * render itself properly at first, but if you page/refresh the grid's data it redraws 
			 * itself with a height only large enough for the headers even though it has the proper 
			 * number of entries.
			 * 
			 * TODO: Low priority as it may be impossible -figure out why the grid does not resize
			 *  its height properly. That is the only misbehaving Widget - the hp and display panel
			 *  retain proper sizing, though the grid is attached to the hp in a RowLayout, so there
			 *  may be something to that combination that contributes.
			 */
			pagingLoader.getPagingLoader().addLoadListener(new LoadListener() {
				protected int gridH = 0;

				@Override
				public void loaderBeforeLoad(LoadEvent le) {
					if( grid.isRendered() )
						gridH = grid.getHeight();
				}

				@Override
				public void loaderLoad(LoadEvent le) {
					if( grid.isRendered() ) {
						if( grid.getHeight() <= 30 && grid.getStore().getCount() > 0 ) {
							grid.setHeight(gridH);
							grid.getView().refresh(true);
							hp.layout();
						}
					}
				}
			});
		}

		displayPanel.clear();
		((VerticalPanel) displayPanel).setSpacing(3);
		displayPanel.addStyleName("thinFrameBorder");
		((VerticalPanel) displayPanel).setBorderWidth(1);

		innerContainer.removeAll();

		ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		Object[] entries = getSortedEntries();
		configs = buildColumnConfig(generateDefaultStructure());

		ColumnModel cm = new ColumnModel(configs);
		grid = new Grid<ClassificationSchemeEntry>(store, cm);
		grid.getView().setFiresEvents(false);

		final ToolBar addBar = new ToolBar();
		SelectionListener<ButtonEvent> addListener = new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				innerContainer.removeAll();
				final ListBox box = getClassificationPulldown(null);
				innerContainer.add(new HTML(description+":"));
				innerContainer.add(box);


				final ClassificationSchemeEntry clsch = new ClassificationSchemeEntry("0", generateDefaultStructure());
				final Widget details = clsch.getDetailsWidget();
				innerContainer.add(details);
				final HorizontalPanel buttonPanel = new HorizontalPanel();
				innerContainer.add(buttonPanel);
				Button ok = new Button("OK", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						//						if(!((Boolean)box.getSelection().get(0).get("enabled")).booleanValue() || selected.containsKey(box.getSelection().get(0).get("key"))){
						if( box.getValue(box.getSelectedIndex()).equals("") || selected.containsKey(box.getValue(box.getSelectedIndex())) ){
							WindowUtils.errorAlert("This is not a selectable option. Please try again.");
							return;
						}
						clsch.setKey(box.getValue(box.getSelectedIndex()));
						clsch.synchronizeWithModel();

						selected.put((String)clsch.getKey(), clsch.struct);
						((Structure) selected.get(clsch.getKey())).setId((String)clsch.getKey());
						
						String parentCode = codeToParentCode.get(clsch.getKey());
						if( parentCode != null && codeToCodeable.get(parentCode).booleanValue() && !selected.containsKey(parentCode) ) {
							ClassificationSchemeEntry parent = new ClassificationSchemeEntry(parentCode, generateDefaultStructure());
							parent.synchronizeWithModel();
							selected.put(parentCode, parent.struct);
							parent.struct.setId(parentCode);
							pagingLoader.getFullList().add(0, parent);
							int h = recalculateHeightAfterSingleAdd();
							
							WindowUtils.infoAlert("Entry Added", "The entry " + codeToDesc.get(parentCode)
									+ " was automatically added, as you selected one of its child entries. "
									+ "Please select and fill in the appropriate data for this entry.");
						}

						pagingLoader.getFullList().add(0, clsch);
						pagingLoader.getPagingLoader().load();

						int h = recalculateHeightAfterSingleAdd();

						innerContainer.removeAll();
						hp.layout();
					}
				});

				Button cancel = new Button("Cancel");
				cancel.addListener(Events.OnClick, new Listener<BaseEvent>() {
					public void handleEvent(BaseEvent be) {
						innerContainer.removeAll();
						hp.layout();
					}
				});
				buttonPanel.add(ok);
				buttonPanel.add(cancel);

				hp.layout();
			}
		};
		Button addClassification = new Button("Quick Add to " + description, addListener);
		addClassification.setIconStyle("icon-add");

		SelectionListener<ButtonEvent> listener = new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				if (window == null) {
					window = WindowUtils.getWindow(true, false, "Add " + description);
					window.setScrollMode(Scroll.AUTO);

					if (description != null && !description.equals(""))
						window.setHeading(description);
				} else
					window.removeAll();

				window.add(showTreePanel());
				window.setSize(500, 500);
				window.show();
				window.center();


			}
		};
		Button modClassification = new Button("Add " + description, listener);
		modClassification.setIconStyle("icon-browse-add");

		addBar.add(addClassification);
		addBar.add(modClassification);

		final ToolBar gridBar = getLabelPanel(grid);
		hp = new LayoutContainer(new RowLayout(Orientation.VERTICAL));

		hp.add(addBar, new RowData(1, 30));
		hp.add(gridBar, new RowData(1, 30));
		hp.add(grid, new RowData(1, -1));
		hp.add(pagingBar, new RowData(1, 40));
		hp.add(innerContainer, new RowData(1, -1));

		grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		grid.getSelectionModel().addListener(Events.SelectionChange,  
				new Listener<SelectionEvent<ClassificationSchemeEntry>>() {  
			public void handleEvent(SelectionEvent<ClassificationSchemeEntry> be) {  
				if(grid.getSelectionModel().getSelectedItem()!=null) {
					gridBar.fireEvent(Events.Change);
					innerContainer.removeAll();
					final ListBox box = getClassificationPulldown(grid.getSelectionModel().getSelectedItem().getKey());

					innerContainer.add(new HTML(description+":"));
					innerContainer.add(box);

					innerContainer.add(grid.getSelectionModel().getSelectedItem().getDetailsWidget());

					final HorizontalPanel buttonPanel = new HorizontalPanel();
					innerContainer.add(buttonPanel);
					Button ok = new Button("OK", new SelectionListener<ButtonEvent>() {
						final String init = (String)grid.getSelectionModel().getSelectedItem().getKey(); 
						public void componentSelected(ButtonEvent ce) {

							//									if(!((Boolean)box.getSelection().get(0).get("enabled")).booleanValue() || (!init.equals(box.getSelection().get(0).get("key")) && selected.containsKey(box.getSelection().get(0).get("key")))){
							if( (box.getValue(box.getSelectedIndex()).equals("") || (!init.equals(box.getValue(box.getSelectedIndex())) && selected.containsKey(box.getValue(box.getSelectedIndex())) ))){
								WindowUtils.errorAlert("This is not a selectable option. Please try again.");
								return;
							}

							selected.remove(grid.getSelectionModel().getSelectedItem().getKey());
							grid.getSelectionModel().getSelectedItem().setKey(box.getValue(box.getSelectedIndex()));
							grid.getSelectionModel().getSelectedItem().synchronizeWithModel();

							selected.put((String)grid.getSelectionModel().getSelectedItem().getKey(), grid.getSelectionModel().getSelectedItem().struct);
							((Structure) selected.get(grid.getSelectionModel().getSelectedItem().getKey())).setId((String)grid.getSelectionModel().getSelectedItem().getKey());
							store.update(grid.getSelectionModel().getSelectedItem());

							innerContainer.removeAll();
							//									innerContainer.layout();
							hp.layout();
						}
					});

					Button cancel = new Button("Cancel");
					cancel.addListener(Events.OnClick, new Listener<BaseEvent>() {
						public void handleEvent(BaseEvent be) {
							innerContainer.removeAll();
							innerContainer.layout();
						}
					});
					buttonPanel.add(ok);
					buttonPanel.add(cancel);

					hp.layout();
					//							innerContainer.layout();
				}
				else{
					gridBar.setVisible(false);
				}
			}
		}); 

		grid.setBorders(true);
		grid.setWidth(900);

		pagingLoader.getFullList().clear();
		for (int i = 0; i < entries.length; i++) {
			final Entry curEntry = (Entry) entries[i];
			if (!codeToSelectedDesc.containsKey(curEntry.getKey())) {
				WindowUtils.errorAlert("Invalid " + description + " Entry!", "The " + description
						+ " entry with id " + curEntry.getKey()
						+ " is not accounted for in the structural definition of SIS "
						+ "(this will most likely happen " + "if a DEMImport contained data that is not valid). "
						+ "Consequently, saving this assessment will eliminate this "
						+ "invalid entry from the assessment document.");
				selected.remove(curEntry.getKey());
				continue;
			}

			//pagingLoader.getFullList().add(new ClassificationSchemeEntry(curEntry));
			try {
				pagingLoader.getFullList().add(new ClassificationSchemeEntry(
					(String)curEntry.getKey(), (Structure)curEntry.getValue()
				));
			} catch (ClassCastException e) {
				System.out.println("Unhandled class cast exception.");
				e.printStackTrace();
			}
		}

		pagingLoader.getPagingLoader().setLimit(15);
		pagingLoader.getPagingLoader().setOffset(0);
		pagingLoader.getPagingLoader().load(0, 15);

		if( grid.isRendered() )
			pagingBar.first();

		displayPanel.add(hp);
		return displayPanel;
	}

	public int recalculateHeightAfterSingleAdd() {
		if( grid.isRendered() ) {
			grid.setHeight(grid.getHeight()+25);
			grid.getView().refresh(true);
			return grid.getHeight();
		} else
			return -1;
	}

	private ListBox getClassificationPulldown(Object selected){
		final PortableAlphanumericComparator c = new PortableAlphanumericComparator();
		List<Entry<String, String>> set = new ArrayList<Entry<String, String>>(codeToSelectedDesc.entrySet());
		ArrayUtils.quicksort(set, new Comparator<Entry<String, String>>() {

			public int compare(Entry<String, String> o1, Entry<String, String> o2) {
				return c.compare(o1.getValue(), o2.getValue());
			}
		});

		ListBox listbox = new ListBox();
		int count = 0;
		for(Entry<String, String> curEntry: set) {
			String key = curEntry.getKey();
			String curLevelID = codeToLevelID.get(key);
			try {
				if (curLevelID.indexOf(".") < 0) {
					if (Integer.parseInt(curLevelID) >= 100)
						continue;
				} else if (Integer.parseInt(curLevelID.split("\\.")[0]) >= 100)
					continue;
			} catch (NumberFormatException ignored) {
			}
			
			if( codeToCodeable.get(key).booleanValue() ) {
				listbox.addItem(curEntry.getValue(), key);
				if(selected!=null && key.equals((String)selected)) listbox.setSelectedIndex(count);
				count++;
			}
		}

		return listbox;
	}

	private void clearInnerContainer(){
		innerContainer.removeAll();
		innerContainer.layout();	
	}

	public ToolBar getLabelPanel(final Grid grid){

		final ToolBar toolBar = new ToolBar(); 
		toolBar.setVisible(false);

		final IconButton notesImage = new IconButton("images/icon-note-grey.png");
		final IconButton refImage = new IconButton("images/icon-book-grey.png");
		final IconButton remove = new IconButton("images/icon-cross.png");
		
		toolBar.addListener(Events.Change, new Listener<BaseEvent>(){
			public void handleEvent(BaseEvent be) {
				if(!toolBar.isVisible())toolBar.setVisible(true);
				curRefs = AssessmentCache.impl.getCurrentAssessment().getField(
						canonicalName + "." + ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).getKey()).getReference();

				//if(refIcon==null){
				if (curRefs == null || curRefs.size() == 0)
					refImage.changeStyle("images/icon-book-grey.png");
				else
					refImage.changeStyle("images/icon-book.png");
				//}

				curNotes = NotesCache.impl.getNotesForCurrentAssessment(canonicalName + ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).getKey());

				if (curNotes == null || curNotes.size() == 0) {
					notesImage.changeStyle("images/icon-note-grey.png");
				} else {
					notesImage.changeStyle("images/icon-note.png");
				}
			};

		});


		//entryCanonicalName = canonicalName + "." + ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).curEntry.getKey();



		remove.addStyleName("pointerCursor");
		remove.setSize("18px", "18px");
		remove.addSelectionListener(new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				WindowUtils.confirmAlert("Delete Confirm", "Are you sure you want to delete this data?",
						new Listener<MessageBoxEvent>() {
					public void handleEvent(MessageBoxEvent be) {
						if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
							ClassificationSchemeEntry curEntry = (ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem();
							selected.remove(curEntry.getKey());
							store.remove(curEntry);
							pagingLoader.getFullList().remove(curEntry);
							clearInnerContainer();
							toolBar.setVisible(false);
							//removeFromParent();
						}
					}
				});
			}
		});

		if (!inViewOnlyMode){
			toolBar.add(remove);
			toolBar.add(new SeparatorToolItem());
		}


		final IconButton refIcon = new IconButton("images/icon-book-grey.png");
		refIcon.setStyleName("SIS_iconPanelIcon");

		refIcon.addSelectionListener(new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				SISClientBase.getInstance().onShowReferenceEditor("Add a references to " + canonicalName + " "
						+ ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).getKey(), 
						(ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem(), 
				new GenericCallback<Object>() {
					public void onFailure(Throwable caught) {
					};

					public void onSuccess(Object result) {
						ArrayList references = (ArrayList) result;
						ReferenceCache.getInstance().addReferences(
								AssessmentCache.impl.getCurrentAssessment().getId(), references);

						ReferenceCache.getInstance().addReferencesToAssessmentAndSave(references,
								canonicalName + "." + ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).getKey(), new GenericCallback<Object>() {
							public void onFailure(Throwable caught) {
							}

							public void onSuccess(Object result) {
								refIcon.changeStyle("images/icon-book.png");
							}
						});
					};
				}, 
				new GenericCallback<Object>() {
					public void onFailure(Throwable caught) {};

					public void onSuccess(Object result) {
						ArrayList list = (ArrayList) result;
						for (int i = 0; i < list.size(); i++) {
							AssessmentCache.impl.getCurrentAssessment().removeReference(
									(Reference) list.get(i), canonicalName + "." + ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).getKey());
						}

						if (AssessmentCache.impl.getCurrentAssessment().getReferences(
								canonicalName + "." + ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).getKey()).size() == 0)
							refIcon.changeStyle("images/icon-book-grey.png");
						// rebuildIconPanel();
					};
				});
			}
		});

		toolBar.add(refIcon);
		toolBar.add(new SeparatorToolItem());

//		final ClickHandler noteListener = new ClickHandler() {
//			public void onClick(ClickEvent event) {
//				String temp = canonicalName + ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).getKey();
//				openEditViewNotesPopup(temp, notesImage);
//			}
//		};

		notesImage.setStyleName("SIS_iconPanelIcon");
		notesImage.addSelectionListener(new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				String temp = canonicalName + ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).getKey();
				openEditViewNotesPopup(temp, new AsyncCallback<String>() {
					public void onSuccess(String result) {
						notesImage.changeStyle("images/icon-book.png");
					}
					public void onFailure(Throwable caught) {
					}
				});
			}
		});

		toolBar.add(notesImage);
		toolBar.add(new SeparatorToolItem());

		return toolBar;

	}

	private ArrayList<ColumnConfig> buildColumnConfig(Structure str){
		ArrayList<ColumnConfig> cc = new ArrayList<ColumnConfig>();

		GridCellRenderer<ClassificationSchemeEntry> renderer = new GridCellRenderer<ClassificationSchemeEntry>() {
			public Object render(ClassificationSchemeEntry model, String property, ColumnData config,
					int rowIndex, int colIndex, ListStore<ClassificationSchemeEntry> store, Grid<ClassificationSchemeEntry> grid) {

				try{
					return ((ClassificationSchemeEntry)model).pretty.get(colIndex-1);
				}
				catch (Exception e) {
					return model.get(property);
				}
			}
		};

		ColumnConfig column = new ColumnConfig();  
		column.setId(description.replaceAll("\\s", ""));
		column.setHeader(description);
		column.setWidth(450);
		cc.add(column);

		ArrayList<String> des = str.extractDescriptions();
		for (String d : des) {
			column = new ColumnConfig();
			column.setRenderer(renderer);
			String id = d.replaceAll("\\s", "");
			column.setId(id);
			column.setHeader(d);
			column.setWidth(80);
			cc.add(column);
		}
		
		return cc;
	}

	public Structure generateDefaultStructure() {
		return DisplayDataProcessor.processDisplayStructure(treeData.getDefaultStructure());
	}

	// //TODO: Integrate this if MyGWT setChecked() ever works when the treeItem
	// //has been rendered before but isn't currently on the screen.
	// private void setChecked()
	// {
	// for( int i = 0; i < tree.getAllItems().length; i++ )
	// {
	// TreeItem curItem = tree.getAllItems()[i];

	// if( selected.containsKey( curItem.getId() ) )
	// curItem.setChecked(true);
	// else
	// curItem.setChecked(false);
	// }
	// }

	public HashMap getCodeToDesc() {
		return codeToDesc;
	}

	public HashMap getCodeToSelectedDesc() {
		return codeToSelectedDesc;
	}

	public HashMap getCodeToLevelID() {
		return codeToLevelID;
	}

	public HashMap<String, Structure> getSelected() {
		return selected;
	}

	public Object[] getSortedEntries() {
		// First get the map's entries as array (or List):
		Object[] entries = selected.entrySet().toArray();

		// Sort the entries with your own comparator for the values:
		Arrays.sort(entries, new Comparator() {

			private PortableAlphanumericComparator comparator = new PortableAlphanumericComparator();

			public int compare(Object lhs, Object rhs) {
				Entry le = (Entry) lhs;
				Entry re = (Entry) rhs;

				if (!codeToSelectedDesc.containsKey(le.getKey()) || !codeToSelectedDesc.containsKey(re.getKey()))
					return 0;

				return comparator.compare(((String) codeToSelectedDesc.get(le.getKey())), ((String) codeToSelectedDesc
						.get(re.getKey())));
				// return ((String)codeToSelectedDesc.get( le.getKey()
				// )).compareTo(
				// ((String)codeToSelectedDesc.get( re.getKey() )));
			}
		});

		return entries;
	}

	public TreeData getTreeData() {
		return treeData;
	}

	public void revert() {

	}

	public void save() {
		for( Entry<String, Structure> curSelected : selected.entrySet() ) {
			Field subfield = new Field(canonicalName + "Subfield", field.getAssessment());
			subfield.getPrimitiveField().add(new ForeignKeyPrimitiveField(canonicalName, subfield, 
					Integer.valueOf(curSelected.getKey()), canonicalName + "_lookup"));
			curSelected.getValue().save(subfield);
		}
	}
	
	@Override
	public void setData(Field field) {
		this.field = field;
		if( field != null ) {
			
		} else {
			selected.clear();
		}
	}

	/**
	 * Sets the selected schemes, typically from a previously saved Assessment.
	 * 
	 * @param newData
	 *            - HashMap<String(label), Structure>
	 */
	public void setSelectedAsStructures(Map<String, Structure> newData) {
		selected.clear();

		if (newData != null) {
			try {
				selected.putAll(newData);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
//
//	/**
//	 * Sets the selected schemes, typically from a previously saved Assessment.
//	 * 
//	 * @param newData
//	 *            - HashMap<String(label), ArrayList(data)>
//	 */
//	public void setSelectedFromData(HashMap<String, ArrayList<String>> newData) {
//		selected.clear();
//
//		if (newData != null) {
//			for (Entry<String, ArrayList<String>> curSelected : newData.entrySet()) {
//				try {
//					String curKey = curSelected.getKey();
//					Structure curStruct = generateDefaultStructure();
//
//					curStruct.setData(newData.get(curKey));
//
//					selected.put(curKey, curStruct);
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}

	@Override
	public Widget showDisplay(boolean viewOnly) {
		VerticalPanel outer = new VerticalPanel();
		outer.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		outer.setSize("100%", "100%");

		try {
			setupIconPanel();

			VerticalPanel vert = new VerticalPanel();
			vert.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
			vert.setSpacing(5);

			vert.add(iconPanel);
			vert.add(generateContent(viewOnly));
			outer.add(vert);
		} catch (Exception e) {
			e.printStackTrace();
			// Window.alert("Error in Display.show(). " + e.getMessage() );
		}

		dockPanel = outer;
		return dockPanel;
	}

	private LayoutContainer showTreePanel() {
		buildTreePanel();
		treePath = tree.getRootItem().getPath();
		return container;
	}

	public String toThinXML() {
		return toXML();
	}

	public String toXML() {
		String ret = "<classificationScheme id=\"" + canonicalName + "\">\r\n";

		for (Iterator iter = selected.keySet().iterator(); iter.hasNext();) {
			String curKey = (String) iter.next();

			ret += "<selected id=\"" + curKey + "\">\r\n";
			ret += ((Structure) selected.get(curKey)).toXML();
			ret += "</selected>\r\n";
		}

		ret += "</classificationScheme>\r\n";
		return ret;
	}

	private void updateSelected() {
		TreeItem[] items = tree.getChecked().toArray(new TreeItem[0]);
		for (int i = 0; i < items.length; i++) {
			TreeItem item = items[i];

			if (!selected.containsKey(item.getId())) {
				selected.put(item.getId(), generateDefaultStructure());
				((Structure) selected.get(item.getId())).setId(item.getId());
			}
		}

		generateContent(false);
	}
	
	@Override
	public boolean hasChanged() {
		//TODO: IMPLEMENT!!
		return false;
	}
}
