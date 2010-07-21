package org.iucn.sis.client.api.panels.integrity;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;

/**
 * ValidationResultsWindow.java
 * 
 * Displays the validation results.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public class ValidationResultsWindow extends Window {

	public ValidationResultsWindow(Integer assessmentID, String results) {
		super();
		setIconStyle("icon-integrity");
		setClosable(true);
		setModal(true);
		setHeading("Validation Results for " + assessmentID);
		setSize(400, 300);
		setScrollMode(Scroll.AUTO);
		setLayout(new FillLayout());
// setAlignment(HorizontalAlignment.CENTER);

		add(new HtmlContainer(results));

		addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				close();
			}
		}));
	}

}
