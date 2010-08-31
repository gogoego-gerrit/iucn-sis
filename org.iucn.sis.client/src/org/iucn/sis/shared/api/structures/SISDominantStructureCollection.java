package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.shared.api.models.Field;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;

public class SISDominantStructureCollection extends Structure<Field> implements DominantStructure<Field> {

	private ArrayList<DominantStructure<Object>> structures;
	private boolean activeOnAny = true;

	@SuppressWarnings("unchecked")
	public SISDominantStructureCollection(String structure, String description, String structID, Object structures) {
		super(structure, description, structID, structures);

		// displayPanel = new ContentPanel();
		buildContentPanel(Orientation.HORIZONTAL);
		this.structures = (ArrayList) data;
	}
	
	@Override
	public void save(Field parent, Field field) {
		if (field == null) {
			field = new Field();
			field.setName(getId());
			field.setParent(parent);
		}
		
		for (DominantStructure<Object> cur : structures)
			if (cur.isPrimitive())
				cur.save(field, field.getPrimitiveField(cur.getId()));
			else
				cur.save(field, field.getField(cur.getId()));
	}
	
	@Override
	public void addListenerToActiveStructure(ChangeListener changeListener, ClickHandler clickListener,
			KeyboardListener keyboardListener) {
		for (DominantStructure<Object> structure : structures) {
			structure.addListenerToActiveStructure(changeListener, clickListener,
					keyboardListener);
		}
	}

	@Override
	public void clearData() {
		/*
		 * FIXME: should this do something?? 
		 */
	}

	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		for (int i = 0; i < structures.size(); i++) {
			displayPanel.add((structures.get(i)).generate());
		}
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();
		for (int i = 0; i < structures.size(); i++) {
			displayPanel.add((structures.get(i)).generateViewOnly());
		}
		return displayPanel;
	}

	@Override
	public void createWidget() {
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList<String> extractDescriptions() {
		ArrayList<String> ret = new ArrayList<String>();

		for (DominantStructure<Object> structure : structures)
			ret.addAll(structure.extractDescriptions());

		return ret;
	}
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		final ArrayList<ClassificationInfo> list = new ArrayList<ClassificationInfo>();
		for (DominantStructure<Object> structure : structures)
			list.addAll(structure.getClassificationInfo());
		return list;
	}

	@Override
	public String getData() {
		return null;
	}

	/**
	 * Pass in the raw data from an Assessment object, and this will return
	 * it in happy, displayable String form
	 * 
	 * @return ArrayList of Strings, having converted the rawData to nicely
	 *         displayable String data. Happy days!
	 */
	@Override
	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset) {
		for (DominantStructure<Object> curStruct : structures)
			offset = (curStruct).getDisplayableData(rawData, prettyData, offset);

		return offset;
	}

	public ArrayList<DominantStructure<Object>> getStructures() {
		return structures;
	}
	
	@Override
	public boolean hasChanged(Field field) {
		for (DominantStructure<Object> structure : structures) {
			boolean hasChanged;
			if (structure.isPrimitive())
				hasChanged = structure.hasChanged(field.getPrimitiveField(structure.getId()));
			else
				hasChanged = structure.hasChanged(field.getField(structure.getId()));
			
			if (hasChanged)
				return true;
		}
		return false;
	}

	@Override
	public boolean isActive(Rule activityRule) {
		int count = 0;
		for (int i = 0; i < structures.size(); i++) {
			if ((structures.get(i)).isActive(activityRule)) {
				count++;
			}
		}
		if (count == 0) {
			return false;
		}
		if (activeOnAny) {
			return true;
		}
		if (!activeOnAny && count == structures.size()) {
			return true;
		} else {
			return false;
		}
	}

	public void setActiveOnAny(boolean rule) {
		activeOnAny = rule;
	}
	
	@Override
	public void setData(Field field) {
		// TODO Auto-generated method stub
		/*
		 * FIXME: is there something we should be doing here??
		 */
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		for (int i = 0; i < structures.size(); i++) {
			(structures.get(i)).setEnabled(isEnabled);
		}
	}

	public String toXML() {
		return StructureSerializer.toXML(this);
	}
}
