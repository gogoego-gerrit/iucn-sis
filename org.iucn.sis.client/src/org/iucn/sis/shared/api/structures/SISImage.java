package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Map;

import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SISImage extends SISPrimitiveStructure<String> {

	public SimplePanel imagePanel;
	public Image image;

	public SISImage(String struct, String descript, String structID, Object data) {
		// descript: URL of the image
		// data: style
		super(struct, descript, structID, data);
		// displayPanel = new ContentPanel();
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	protected PrimitiveField<String> getNewPrimitiveField() {
		return new StringPrimitiveField(getId(), null);
	}
	
	@Override
	public void clearData() {
		image.setUrl("");
	}

	@Override
	public Widget createLabel() {
		displayPanel.add(image);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		return createLabel();
	}

	@Override
	public void createWidget() {
		image = new Image();
		image.setUrl(this.description);

		String style = getDataValue();
		if (style != null)
			image.addStyleName(style);
	}

	@Override
	public String getData() {
		return image.getUrl();
	}
	
	@SuppressWarnings("unchecked")
	private String getDataValue() {
		try {
			return (String)((Map<String, String>)data).get("value");
		} catch (ClassCastException e) {
			return null;
		}
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

	public Image getImage() {
		return image;
	}
	
	@Override
	public void setData(PrimitiveField<String> field) {
		String datum = field != null ? field.getValue() : "";
		image.setUrl(datum);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		// Nothing to do here
	}

}
