package org.iucn.sis.shared.structures;

import java.util.ArrayList;

import org.iucn.sis.client.ui.richtexteditor.RichTextToolbar;
import org.iucn.sis.client.utilities.clipboard.Clipboard;
import org.iucn.sis.client.utilities.clipboard.UsesClipboard;
import org.iucn.sis.shared.xml.XMLUtils;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;

public class SISRichTextArea extends Structure implements UsesClipboard {

	// private FCKEditor textarea;

	private Grid areaPanel;
	private RichTextArea area;
	private RichTextToolbar tb;

	private String viewOnlyData;

	public SISRichTextArea(String struct, String descript) {
		super(struct, descript);
		buildContentPanel(Orientation.VERTICAL);
	}

	@Override
	public void clearData() {
		viewOnlyData = "";
		// textarea = new FCKEditor("", description.hashCode()+"", "600px",
		// "300px", "Default");
		area.setHTML("");
	}

	public void copyToClipboard() {
		Clipboard.getInstance().add(area.getHTML(), description);
	}

	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		displayPanel.setSize("100%", "100%");
		displayPanel.add(descriptionLabel);
		// displayPanel.add(textarea);
		displayPanel.add(areaPanel);
		return displayPanel;
	}

	private Grid createRichText() {
		area = new RichTextArea();
		area.ensureDebugId("cwRichText-area");
		tb = new RichTextToolbar(area);
		tb.ensureDebugId("cwRichText-toolbar");

		Grid p = new Grid(2, 1);
		p.setStyleName("cw-RichText");
		p.setWidget(0, 0, tb);
		p.setWidget(1, 0, area);

		area.setHeight("450px");
		area.setWidth("100%");
		tb.setWidth("100%");
		p.setWidth("100%");
		p.setHeight("95%");

		// DOM.setStyleAttribute(p.getElement(), "margin-right", "4px");

		return p;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(new HTML(viewOnlyData));
		return displayPanel;
	}

	@Override
	public void createWidget() {
		descriptionLabel = new HTML(description);
		areaPanel = createRichText();
		// textarea = new FCKEditor("", description.hashCode()+"", "600px",
		// "300px", "Default");
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
		try {
			viewOnlyData = area.getHTML();
			// STRIP OF NON_RICHTEXT FORMATTING
			viewOnlyData = FormattingStripper.stripText(viewOnlyData);
			return XMLUtils.clean(viewOnlyData).replaceAll("[\\n\\r]", " ").trim();
		} catch (Throwable e) {
			e.printStackTrace();
			SysDebugger.getInstance().println("Error getting rich text data...");
			return "";
		}
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

	
	public void pasteFromClipboard(ArrayList items) {
		String text = area.getHTML();
		for (int i = 0; i < items.size(); i++)
			text += (String) items.get(i) + "<br/>";
		area.setHTML(text);
	}

	@Override
	public int setData(final ArrayList dataList, int dataOffset) {
		super.setData(dataList, dataOffset);
		viewOnlyData = dataList.get(dataOffset).toString();
		viewOnlyData = XMLUtils.cleanFromXML(viewOnlyData).replaceAll("[\\n\\r]", " ").trim();
		viewOnlyData = FormattingStripper.stripText(viewOnlyData);
		area.setHTML(viewOnlyData);
		return ++dataOffset;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		// textarea.setVisible(isEnabled);
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}
}
