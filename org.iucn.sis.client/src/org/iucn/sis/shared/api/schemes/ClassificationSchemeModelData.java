package org.iucn.sis.shared.api.schemes;

import java.util.ArrayList;

import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.structures.ClassificationInfo;
import org.iucn.sis.shared.api.structures.DisplayStructure;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ClassificationSchemeModelData extends BaseModelData {
	
	private static final long serialVersionUID = 1L;
	
	private final DisplayStructure structure;
	private final Field field;
	
	private TreeDataRow selectedRow;
	
	public ClassificationSchemeModelData(DisplayStructure structure) {
		this(structure, null);
	}
	
	public ClassificationSchemeModelData(DisplayStructure structure, Field field) {
		super();
		this.field = field;
		this.structure = structure;
		
		getDisplayableData();
	}
	
	public TreeDataRow getSelectedRow() {
		return selectedRow;
	}
	
	public void setSelectedRow(TreeDataRow selectedRow) {
		this.selectedRow = selectedRow;
		if (selectedRow != null)
			set("text", selectedRow.getDescription());
	}
	
	public ArrayList<String> getDisplayableData() {
		ArrayList<String> pretty = new ArrayList<String>();
		ArrayList<String> raw = new ArrayList<String>();
		
		if (structure == null)
			return pretty;
		
		for (Object obj : structure.getClassificationInfo()) {
			ClassificationInfo info = (ClassificationInfo)obj;
			set(info.getDescription(), info.getData());
			raw.add(info.getData());
		}
		
		try {
			structure.getDisplayableData(raw, pretty, 0);
		} catch (Exception e) {
			// ignore. Used to catch dependant structures that we do not care about.
		}
		
		return pretty;
	}
	
	public Widget getDetailsWidget(boolean isViewOnly) {
		SimplePanel container = new SimplePanel();
		if (!isViewOnly) {
			Widget structWidget = structure.generate();
			structWidget.addStyleName("leftMargin15");
			container.setWidget(structWidget);
		} else {
			Widget structWidget = structure.generateViewOnly();
			structWidget.addStyleName("leftMargin15");
			container.setWidget(structWidget);
		}
		return container;
	}
	
	public void save(Field parent, Field field) {
		if (structure != null) {
			Debug.println("Saving a " + structure.getClass().getName());
			if (structure.isPrimitive())
				structure.save(field, field.getPrimitiveField(structure.getId()));
			else
				structure.save(field, field.getField(structure.getId()));
		}
	}
	
	public Field getField() {
		return field;
	}

}
