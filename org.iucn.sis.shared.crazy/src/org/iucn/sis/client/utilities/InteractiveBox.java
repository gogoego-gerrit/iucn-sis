package org.iucn.sis.client.utilities;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.TextBox;

public class InteractiveBox extends Window {

	private Grid table;
	private HTML html;
	private TextBox textBox;
	private Button button = null;

	public InteractiveBox(Button myButton) {
		super();
		setClosable(true);
		setModal(true);

		table = new Grid(2, 2);
		html = new HTML();
		textBox = new TextBox();
		button = myButton;

		table.setWidget(0, 0, html);
		table.setWidget(0, 1, textBox);
		table.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_MIDDLE);
		table.setWidget(1, 1, button);
		table.getRowFormatter().setVerticalAlign(1, HasVerticalAlignment.ALIGN_BOTTOM);
		table.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		table.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		table.setCellSpacing(8);
		table.getCellFormatter().setWidth(0, 0, "275px");
		table.getCellFormatter().setWidth(1, 1, "125px");
		table.getCellFormatter().setWidth(0, 1, "125px");

		add(table);
		setWidth(400);
		setHeight(100);

	}

	/**
	 * Need to have the button that you pass in call this function in the
	 * clicklistener
	 * 
	 * @return
	 */
	public String closeMe() {
		hide();
		return textBox.getText();
	}

	public void setHTML(String html) {
		this.html.setHTML(html);
		this.html.addStyleName("bold");
	}

}
