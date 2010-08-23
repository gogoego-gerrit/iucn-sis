package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Map;

import org.iucn.sis.shared.api.models.PrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class SISUpload extends Structure {

	private FileUpload fileUpload;

	public SISUpload(String struct, String descript, String structID) {
		super(struct, descript, structID);
		// displayPanel = new HorizontalPanel();
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	protected PrimitiveField getNewPrimitiveField() {
		return null;
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
		prettyData.add(offset, rawData.get(offset));
		return ++offset;
	}

	public FileUpload getFileUpload() {
		return fileUpload;
	}

	@Override
	public void setData(Map<String, PrimitiveField> data) {
		super.setData(data);
		// TODO: ?
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
