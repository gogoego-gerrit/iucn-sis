package org.iucn.sis.shared.api.displays.threats;

import java.util.ArrayList;
import java.util.Collection;

import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeModelData;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeRowEditor;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeRowEditorWindow;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeViewer;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.gwt.ui.DrawsLazily;

public class GroupedThreatRowEditorWindow extends ClassificationSchemeRowEditorWindow {
	
	public GroupedThreatRowEditorWindow(ClassificationSchemeViewer parent, TreeData treeData, String description,
			ClassificationSchemeModelData model, EditMode mode, boolean isViewOnly) {
		super(parent, treeData, description, model, mode, isViewOnly);
		saveButtonText = "Save All and Close";
	}
	
	/**
	 * Overriden to provide a GroupedRowEditor when necessary, 
	 * only for the Named Taxa sections -- 8.5.2 is a special 
	 * case that is grouped, but for viruses instead of taxa. 
	 * The rest of the Named Taxa sections are, indeed, taxa, 
	 * and can use the IAS grouping.
	 * 
	 * All others are singular, not grouped, and can have the 
	 * standard row editor, with the threat viewer used being 
	 * determined by the row number.
	 */
	protected ClassificationSchemeRowEditor createRowEditor(ClassificationSchemeModelData model, boolean isViewOnly) {
		if (model.getSelectedRow() == null)
			return super.createRowEditor(model, isViewOnly); 
		
		if ("8.5.2".equals(model.getSelectedRow().getRowNumber())) {
			final Collection<ClassificationSchemeModelData> models = 
				new ArrayList<ClassificationSchemeModelData>();
			for (ClassificationSchemeModelData current : parent.getModels())
				if (current.getSelectedRow().getDisplayId().equals(model.getSelectedRow().getDisplayId()))
					models.add(current);
			
			ViralThreatRowEditor editor = new ViralThreatRowEditor(models, treeData, model.getSelectedRow(), isViewOnly);
			appendListeners(editor);
			
			return editor;
		}
		else if ("Named taxa".equals(model.getSelectedRow().getDescription())) {
			final Collection<ClassificationSchemeModelData> models = 
				new ArrayList<ClassificationSchemeModelData>();
			for (ClassificationSchemeModelData current : parent.getModels())
				if (current.getSelectedRow().getDisplayId().equals(model.getSelectedRow().getDisplayId()))
					models.add(current);
			
			IASThreatRowEditor editor = new IASThreatRowEditor(models, treeData, model.getSelectedRow(), isViewOnly);
			appendListeners(editor);
			
			return editor;
		}
		else
			return super.createRowEditor(model, isViewOnly);
	}
	
	private void appendListeners(GroupedThreatRowEditor editor) {
		editor.setRemoveListener(new ComplexListener<ClassificationSchemeModelData>() {
			public void handleEvent(ClassificationSchemeModelData model) {
				parent.removeModel(model);
			}
		});
		editor.setAddListener(new ComplexListener<ClassificationSchemeModelData>() {
			public void handleEvent(ClassificationSchemeModelData model) {
				if (parent.getModels().contains(model))
					parent.updateModel(model);
				else
					parent.addModel(model);
			}
		});
		editor.addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<ClassificationSchemeModelData>>() {
			public void handleEvent(SelectionChangedEvent<ClassificationSchemeModelData> be) {
				updateButtons(be);
			}
		});
	}
	
	protected ComboBox<CodingOption> createClassificationOptions(TreeDataRow selected) {
		ComboBox<CodingOption> box = super.createClassificationOptions(selected);
		box.addSelectionChangedListener(new SelectionChangedListener<CodingOption>() {
			public void selectionChanged(SelectionChangedEvent<CodingOption> se) {
				//TODO: create the right structure based on the selection...
				CodingOption selection = se.getSelectedItem();
				if (selection != null) {
					final ClassificationSchemeModelData model = 
						parent.newInstance(parent.generateDefaultStructure(selection.getRow()));
					model.setSelectedRow(selection.getRow());
				
					setModel(model);
					draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
				}
			}
		});
		return box;
	}
	
	protected void updateButtons(final SelectionChangedEvent<ClassificationSchemeModelData> event) {
		getButtonBar().removeAll();
		addButton(new Button("Done", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (saveListener != null)
					for (ClassificationSchemeModelData model : ((GroupedThreatRowEditor)editor).getModelsFromList())
						saveListener.handleEvent(model);
				hide();
			}
		}));
		/*if (event.getSelection().isEmpty()) {
			addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					cancel(null);
				}
			}));
		}
		else {
			addButton(new Button("Save Selection", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					((GroupedThreatRowEditor)editor).save(event.getSelectedItem());
					if (saveListener != null)
						saveListener.handleEvent(event.getSelectedItem());
				}
			}));
		}*/
	}
	
	@Override
	protected void drawButtons(final ComboBox<CodingOption> box, final ClassificationSchemeModelData model) {
		if (!(editor instanceof GroupedThreatRowEditor)) {
			super.drawButtons(box, model);
		}
		else {
			SelectionChangedEvent<ClassificationSchemeModelData> fauxEvent = 
				new SelectionChangedEvent<ClassificationSchemeModelData>(null, model);
			if (EditMode.NEW.equals(mode))
				fauxEvent.getSelection().clear();
			
			updateButtons(fauxEvent);
		}
	}

}
