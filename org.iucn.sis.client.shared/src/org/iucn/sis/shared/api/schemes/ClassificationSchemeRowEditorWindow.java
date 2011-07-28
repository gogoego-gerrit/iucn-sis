package org.iucn.sis.shared.api.schemes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class ClassificationSchemeRowEditorWindow extends BasicWindow implements DrawsLazily {
	
	public enum EditMode {
		NEW, EXISTING
	}
	
	protected final String description;
	protected final TreeData treeData;
	protected final ClassificationSchemeViewer parent;
	
	protected final EditMode mode;
	protected final boolean isViewOnly;
	
	protected String saveButtonText = "Save Selection";
	protected ClassificationSchemeRowEditor editor;
	
	protected ComplexListener<ClassificationSchemeModelData> saveListener;
	protected SimpleListener cancelListener;
	
	public ClassificationSchemeRowEditorWindow(ClassificationSchemeViewer parent, TreeData treeData, 
			String description, ClassificationSchemeModelData model, EditMode mode, boolean isViewOnly) {
		super((EditMode.NEW.equals(mode) ? "New " : "Edit ") + description);
		
		this.parent = parent;
		this.description = description;
		this.treeData = treeData;
		this.mode = mode;
		this.isViewOnly = isViewOnly;
		
		setModel(model);
		
		setLayout(new FillLayout());
		setLayoutOnChange(true);
		setClosable(false);
		setSize(800, 700);
		setScrollMode(Scroll.AUTO);
	}
	
	public void setSaveListener(ComplexListener<ClassificationSchemeModelData> saveListener) {
		this.saveListener = saveListener;
	}
	
	public void setCancelListener(SimpleListener cancelListener) {
		this.cancelListener = cancelListener;
	}
	
	public void show() {
		draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback() {
			public void isDrawn() {
				open();
			}
		});
	}
	
	protected void drawButtons(final ComboBox<CodingOption> box, final ClassificationSchemeModelData model) {
		addButton(new Button(isEditing() ? "Done" : "Save Selection", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (EditMode.NEW.equals(mode)) {
					if (box.getValue() == null) {
						WindowUtils.errorAlert("Please select a coding option from the drop-down.");
						return;
					}
					if (parent.containsRow(box.getValue().getRow())) {
						WindowUtils.errorAlert("A row with this coding option has already been selected.");
						return;
					}
					model.setSelectedRow(box.getValue().getRow());
				}
				
				saveSelection(model);
			}
		}));
		if (!isEditing())
			addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					cancel(model);
				}
			}));
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		removeAll();
		getButtonBar().removeAll();
		
		final ClassificationSchemeModelData model = getModel();
		
		editor = createRowEditor(model, isViewOnly);
		editor.setModel(model);
		editor.draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				final ComboBox<CodingOption> box = createClassificationOptions(model.getSelectedRow());
				box.setEnabled(EditMode.NEW.equals(mode));
				
				final ToolBar top = new ToolBar();
				top.add(new LabelToolItem(description+":"));
				top.add(box);
				
				final LayoutContainer container = new LayoutContainer(new BorderLayout());
				container.add(top, new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
				container.add(editor, new BorderLayoutData(LayoutRegion.CENTER));
				
				add(container);
				
				if (isViewOnly) {
					addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
						public void componentSelected(ButtonEvent ce) {
							hide();
						}
					}));
				}
				else {
					drawButtons(box, model);
				}
				
				callback.isDrawn();
			}
		});
	}
	
	private void open() {
		super.show();
	}
	
	protected ClassificationSchemeRowEditor createRowEditor(ClassificationSchemeModelData model, boolean isViewOnly) {
		return new ClassificationSchemeRowEditor(isViewOnly);
	}
	
	protected void saveSelection(ClassificationSchemeModelData model) {
		if (saveListener != null)
			saveListener.handleEvent(model);
		hide();
	}
	
	protected void cancel(ClassificationSchemeModelData model) {
		if (cancelListener != null)
			cancelListener.handleEvent();
		hide();
	}
	
	protected boolean isEditing() {
		return EditMode.EXISTING.equals(mode);
	}
	
	protected ComboBox<CodingOption> createClassificationOptions(TreeDataRow selected) {
		int topLevel = treeData.getTopLevelDisplay();
		
		/*
		 * Flatten the tree into a list...
		 */
		final List<TreeDataRow> list = new ArrayList<TreeDataRow>(treeData.flattenTree().values());
		Collections.sort(list, new BasicClassificationSchemeViewer.TreeDataRowComparator(topLevel));
		
		final ListStore<CodingOption> store = new ListStore<CodingOption>();
		
		CodingOption selectedOption = null;
		for (TreeDataRow row : list) {
			/*
			 * Weed out legacy data
			 */
			if ("true".equals(row.getCodeable())) {
				final CodingOption option = new CodingOption(row, topLevel);
				if (isLegacyOption((String)option.get("text")))
					continue;
				
				store.add(option);
				if (selected != null && row.getDisplayId().equals(selected.getDisplayId()))
					selectedOption = option;
			}
		}
		
		final ComboBox<CodingOption> box = new ComboBox<CodingOption>();
		box.setStore(store);
		box.setForceSelection(true);
		box.setTriggerAction(TriggerAction.ALL);
		box.setWidth(675);
		
		if (selectedOption != null)
			box.setValue(selectedOption);
		
		return box;
	}
	
	private boolean isLegacyOption(String text) {
		StringBuilder num = new StringBuilder();
		for (char c : text.toCharArray()) {
			if (Character.isDigit(c))
				num.append(c);
			else
				break;
		}
		boolean isLegacy = false;
		try {
			isLegacy = Integer.parseInt(num.toString()) >= 100;
		} catch (Exception e) {
			//Nothing to do
		}
		return isLegacy;
	}

	protected static class CodingOption extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		private final TreeDataRow row;
		
		public CodingOption(TreeDataRow row, int topLevel) {
			super();
			this.row = row;
			set("text", row.getFullLineage(topLevel));
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
		
	}
}
