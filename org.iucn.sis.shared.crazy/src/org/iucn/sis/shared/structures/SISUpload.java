package org.iucn.sis.shared.structures;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class SISUpload extends Structure {

	private FileUpload fileUpload;

	public SISUpload(String struct, String descript) {
		super(struct, descript);
		// displayPanel = new HorizontalPanel();
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	public void clearData() {
		// TODO: ?
	}

	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(fileUpload);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(new HTML("Uploaded:" + fileUpload.getFilename()));
		return displayPanel;
	}

	@Override
	public void createWidget() {
		descriptionLabel = new HTML(this.description);
		fileUpload = new FileUpload();
		fileUpload.setName("uploadFormElement_" + description);
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
		prettyData.add(offset, rawData.get(offset));
		return ++offset;
	}

	public FileUpload getFileUpload() {
		return fileUpload;
	}

	@Override
	public int setData(ArrayList dataList, int dataOffset) {
		super.setData(dataList, dataOffset);
		// TODO: ?
		return ++dataOffset;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.fileUpload.setVisible(isEnabled);
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}

}
