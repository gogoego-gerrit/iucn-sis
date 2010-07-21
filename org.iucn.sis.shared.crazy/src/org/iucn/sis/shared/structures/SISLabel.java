package org.iucn.sis.shared.structures;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;

public class SISLabel extends Structure {

	public static final String LABEL = "label";

	public SISLabel(String struct, String descript) {
		super(struct, descript);
		// displayPanel = new ContentPanel();
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	public void clearData() {

	}

	@Override
	public Widget createLabel() {
		displayPanel.add(this.descriptionLabel);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		return createLabel();
	}

	@Override
	public void createWidget() {
		SysDebugger.getInstance().println("Created a label with description " + description);
		this.descriptionLabel = new HTML(this.description);
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();
		ret.add(description);
		return ret;
	}

	@Override
	public Object getData() {
		return description;
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
		super.setData(dataList, dataOffset);
		return dataOffset;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		// Nothing to do here
	}

	@Override
	public String toXML() {
		return "";
	}

}
