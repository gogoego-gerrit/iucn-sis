package org.iucn.sis.shared.structures;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.shared.acl.User;
import org.iucn.sis.shared.xml.XMLUtils;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class SISOptionsList extends Structure {
	public static final int CURRENT_WORKING_SET_USERS = 0;

	private SISCompleteListTextArea theList;

	public SISOptionsList(String struct, String descript, Object data) {
		super(struct, descript, data);
		buildContentPanel(Orientation.VERTICAL);
	}

	@Override
	public void clearData() {
		theList = new SISCompleteListTextArea();
	}

	@Override
	protected Widget createLabel() {
		displayPanel.clear();
		HTML display = new HTML(description);
		display.setWidth("90%");
		display.setWordWrap(true);
		displayPanel.add(display);
		// displayPanel.add( hideList );
		displayPanel.add(theList);
		return displayPanel;
	}

	@Override
	protected Widget createViewOnlyLabel() {
		displayPanel.clear();
		displayPanel.add(descriptionLabel);
		// displayPanel.add( new HTML( theList.getItemsInListAsCSV() ) );
		displayPanel.add(new HTML(theList.getText()));
		return displayPanel;
	}

	@Override
	public void createWidget() {
		descriptionLabel = new HTML(description);
		theList = new SISCompleteListTextArea();
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
		return XMLUtils.clean(theList.getText());
	}

	/**
	 * Pass in the raw data from an AssessmentData object, and this will return
	 * it in happy, displayable String form
	 * 
	 * @return ArrayList of Strings, having converted the rawData to nicely
	 *         displayable String data. Happy days!
	 */
	@Override
	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData,
			int offset) {
		prettyData.add(offset, rawData.get(0));
		return ++offset;
	}

	@Override
	public int setData(ArrayList dataList, int dataOffset) {
		super.setData(dataList, dataOffset);
		int amountOfOffset = 1;
		theList.clearItemsInList();
		
		if( dataList.size() > dataOffset+1 ) {
			// FOR FORWARDS COMPATIBILITY =)
			if( !dataList.get(1).equals("0") && !dataList.get(1).equals("") ) {
				theList.setTextAreaEnabled(false);

				List<String> userIDs = new ArrayList<String>();
				int numberOfUserIds = 0;
				if (dataList.size() > dataOffset + amountOfOffset) {
					numberOfUserIds = Integer.parseInt((String) dataList
							.get(dataOffset + amountOfOffset));
				}
				for (int i = 0; i < numberOfUserIds; i++) {
					userIDs.add((String) dataList.get(dataOffset + amountOfOffset
							+ i + 1));
				}

				theList.setUsersId(userIDs);
				amountOfOffset += numberOfUserIds;
			} else {
				amountOfOffset++; //Account for "0" count
				
				// FOR BACKWARDS COMPATIBILITY
				if (!"".equals(((String) dataList.get(dataOffset)).trim())) {
					theList.setTextAreaEnabled(true);
					theList.setUserText(XMLUtils.cleanFromXML((String) dataList
							.get(dataOffset)));
				}
			}
		} else {
			// FOR BACKWARDS COMPATIBILITY
			if (!"".equals(((String) dataList.get(dataOffset)).trim())) {
				theList.setTextAreaEnabled(true);
				theList.setUserText(XMLUtils.cleanFromXML((String) dataList
						.get(dataOffset)));
			}	
		}
		
		return dataOffset + amountOfOffset;
	}

	@Override
	protected void setEnabled(boolean isEnabled) {
	}

	@Override
	public String toXML() {
		String ret = "";
		if (theList.hasOldText())
			ret += "<structure>" + XMLUtils.clean(theList.getText()) + "</structure>";
		else
			ret += "<structure>" + "</structure>";

		ret += "<structure>" + theList.getSelectedUsers().size()
				+ "</structure>\r\n";

		for (User user : theList.getSelectedUsers())
			ret += "<structure>" + user.getId() + "</structure>";

		return ret;
	}

}
