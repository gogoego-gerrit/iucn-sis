package org.iucn.sis.shared.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.iucn.sis.client.displays.ClassificationScheme;
import org.iucn.sis.shared.TreeData;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.Widget;

public class SISClassificationSchemeStructure extends Structure {
	private ClassificationScheme scheme;

	public SISClassificationSchemeStructure(String struct, String descript, Object data) {
		super(struct, descript, data);
		buildContentPanel(Orientation.VERTICAL);
	}

	@Override
	public void clearData() {
		scheme.setSelectedFromData(new HashMap());
	}

	@Override
	protected Widget createLabel() {
		clearDisplayPanel();
		displayPanel.setSize("60%", "100%");
		Widget p = scheme.getProtectedWidgetContent(false);
		int h = scheme.recalculateHeightAfterSingleAdd();
		if( h > -1 )
			p.setHeight(h+"px");
		else
			p.setHeight( ((scheme.getSelected().size() * 25) + 20)+"px");
		scheme.getGrid().setWidth(500);
		p.setWidth("500px");
		displayPanel.add(p);
		return displayPanel;
	}
	
	@Override
	protected Widget createViewOnlyLabel() {
		clearDisplayPanel();
		displayPanel.add(scheme.showViewOnly());
		return displayPanel;
	}

	@Override
	public void createWidget() {
		scheme = new ClassificationScheme((TreeData) data);
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();
		ret.add("No. of " + scheme.getDescription());
		return ret;
	}

	@Override
	public Object getData() {
		return scheme.getSelected();
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
		int num = ((String) rawData.get(offset)).matches("\\d") ? Integer.parseInt((String) rawData.get(offset)) : 0;
		prettyData.add(offset, num+"");

		offset++;

		for (int i = 0; i < num; i++) {
			String cur = (String) rawData.get(offset);
			if (scheme.getCodeToDesc().containsKey(cur))
				prettyData.add(offset, scheme.getCodeToDesc().get(cur)+"");
			else
				prettyData.add(offset, cur);

			offset++;
		}

		return offset;
	}

	public ClassificationScheme getScheme() {
		return scheme;
	}

	
	@Override
	public int setData(ArrayList dataList, int dataOffset) {
		super.setData(dataList, dataOffset);
		if (dataList.size() <= dataOffset)
			return dataOffset;

		HashMap selected = new HashMap();
		int numSelected = Integer.parseInt(dataList.get(dataOffset).toString());
		dataOffset++;

		for (int i = 0; i < numSelected; i++) {
			String id = dataList.get(dataOffset).toString();
			dataOffset++;
			// DUPLICATE WORK BEING DONE IN SET SELECTED...
			Structure struct = scheme.generateDefaultStructure();
			dataOffset = struct.setData(dataList, dataOffset);

			selected.put(id, struct);
		}

		scheme.setSelectedAsStructures(selected);
		return ++dataOffset;
	}

	@Override
	protected void setEnabled(boolean isEnabled) {
	}

	@Override
	public String toXML() {
		String ret = "<!-- This tag is for the classification scheme, noting how many selections it has -->\r\n";
		ret += "<structure>" + scheme.getSelected().size() + "</structure>\r\n";

		for (Iterator iter = scheme.getSelected().keySet().iterator(); iter.hasNext();) {
			String curKey = (String) iter.next();

			ret += "<structure>" + curKey + "</structure>\r\n";
			ret += ((Structure) scheme.getSelected().get(curKey)).toXML();
		}

		return ret;
	}
	


}
