package org.iucn.sis.shared.structures;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.Widget;

/**
 * An empty, place-holder structure, chiefly for classification schemes.
 * 
 * @author adam.schwartz
 */
public class SISEmptyStructure extends Structure {
	public SISEmptyStructure(String struct, String descript, Object data) {
		super(struct, descript, data);
		buildContentPanel(Orientation.VERTICAL);
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
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();
		//ret.add(description);
		return ret;
	}

	@Override
	public Object getData() {
		return null;
	}

	/**
	 * Pass in the raw data from an AssessmentData object, and this will return
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
	public int setData(ArrayList dataList, int dataOffset) {
		return dataOffset;
	}

	
	@Override
	protected void setEnabled(boolean isEnabled) {
	}

	@Override
	public String toXML() {
		return "";
	}

}
