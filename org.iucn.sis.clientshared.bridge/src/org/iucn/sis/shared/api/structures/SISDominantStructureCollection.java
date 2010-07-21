package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;

public class SISDominantStructureCollection extends DominantStructure {

	private ArrayList<Structure> structures;
	private boolean activeOnAny = true;

	public SISDominantStructureCollection(String structure, String description, String structID, Object structures) {
		super(structure, description, structID, structures);

		// displayPanel = new ContentPanel();
		buildContentPanel(Orientation.HORIZONTAL);
		this.structures = (ArrayList) data;
	}

	@Override
	protected PrimitiveField getNewPrimitiveField() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void save(Field field) {
		for( Structure cur : structures )
			cur.save(field);
	}
	
	@Override
	public void addListenerToActiveStructure(ChangeListener changeListener, ClickHandler clickListener,
			KeyboardListener keyboardListener) {
		for (int i = 0; i < structures.size(); i++) {
			((DominantStructure) structures.get(i)).addListenerToActiveStructure(changeListener, clickListener,
					keyboardListener);
		}
	}

	@Override
	public void clearData() {

	}

	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		for (int i = 0; i < structures.size(); i++) {
			displayPanel.add(((DominantStructure) structures.get(i)).generate());
		}
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();
		for (int i = 0; i < structures.size(); i++) {
			displayPanel.add(((DominantStructure) structures.get(i)).generateViewOnly());
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
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();

		for (Iterator iter = structures.iterator(); iter.hasNext();)
			ret.addAll(((Structure) iter.next()).extractDescriptions());

		return ret;
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
		for (Object curStruct : structures)
			offset = ((Structure) curStruct).getDisplayableData(rawData, prettyData, offset);

		return offset;
	}

	public ArrayList getStructures() {
		return structures;
	}

	@Override
	public boolean isActive(Rule activityRule) {
		int count = 0;
		for (int i = 0; i < structures.size(); i++) {
			if (((DominantStructure) structures.get(i)).isActive(activityRule)) {
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
	public void setData(Map<String, PrimitiveField> data) {
		super.setData(data);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		for (int i = 0; i < structures.size(); i++) {
			((DominantStructure) structures.get(i)).setEnabled(isEnabled);
		}
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}
}
