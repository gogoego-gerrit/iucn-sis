package org.iucn.sis.shared.structures;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.util.extjs.client.WindowUtils;

public class SISRangeError {

	LayoutContainer table;
	public static final Window s = WindowUtils.getWindow(true, true, "");

	public SISRangeError() {

		s.setHeading("INVALID DATA");
		LayoutContainer content = s;

		// content.setLayout(new FlowLayout(15));
		makeTable();
		content.add(table);

		s.setSize(500, 300);
		s.show();
		s.center();

	}

	private void makeTable() {
		table = new LayoutContainer();

		table.add(new HTML("Invalid Range.  The range must fit one of the following forms:"));
		Grid grid = new Grid(5, 2);
		grid.setWidget(0, 0, new HTML("<b>VALID RANGE</b>"));
		grid.setWidget(0, 1, new HTML("<b>RANGE MEANING</b>"));
		grid.setWidth("450px");
		grid.setText(1, 0, "0");
		grid.setText(1, 1, "The value is definitely 0");
		grid.setText(2, 0, "0-1");
		grid.setText(2, 1, "The value is definitely between 0 and 1");
		grid.setText(3, 0, "0-1,.5");
		grid.setText(3, 1, "The value is definitely 0-1 but my best guess is .5");
		grid.setText(4, 0, "0-1,.25-.5");
		grid.setText(4, 1, "The value is definitely 0-1 but my best guess is between .25 and .5");
		grid.getColumnFormatter().setWidth(0, "100px");
		grid.getColumnFormatter().setWidth(1, "350px");
		grid.setBorderWidth(1);

		table.add(new HTML("<br />"));
		table.add(grid);

		Button closeButton = new Button("OK");
		closeButton.addListener(Events.OnMouseUp, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				s.hide();
				s.removeAll();
			}

		});
		// closeButton.addStyleName(style)
		table.add(new HTML("<br />"));
		table.add(closeButton);

	}

}
