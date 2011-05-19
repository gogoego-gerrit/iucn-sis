package org.iucn.sis.shared.api.structures;

import org.iucn.sis.client.api.utils.BasicWindow;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;

public class SISRangeError extends BasicWindow {

	LayoutContainer table;

	public SISRangeError() {
		super("INVALID DATA");
		setSize(500, 300);
		
		add(makeTable());

		addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
		
		show();
	}

	private LayoutContainer makeTable() {
		LayoutContainer table = new LayoutContainer();

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

		return table;
	}

}
