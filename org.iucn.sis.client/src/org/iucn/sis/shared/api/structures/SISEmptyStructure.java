package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.Widget;

/**
 * An empty, place-holder structure, chiefly for classification schemes.
 * 
 * @author adam.schwartz
 */
public class SISEmptyStructure extends Structure {
	public SISEmptyStructure(String struct, String descript, String structID, Object data) {
		super(struct, descript, structID, data);
		buildContentPanel(Orientation.VERTICAL);
	}
	
	@Override
	public void save(Field field) {
		//Nothing to do.
	}
	
	@Override
	public boolean hasChanged() {
		return false;
	}
	
	@Override
	public void clearData() {
	}

	@Override
	protected Widget createLabel() {
		return displayPanel;
	}

	@Override
	protected Widget createViewOnlyLabel() {
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
		//ret.add(description);
		return ret;
	}
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		return new ArrayList<ClassificationInfo>();
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
		return offset;
	}
	
	@Override
	public void setData(Field field) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEnabled(boolean isEnabled) {
	}

	@Override
	public String toXML() {
		return "";
	}

}
