package org.iucn.sis.shared.api.integrity;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;

/**
 * HelpWindow.java
 * 
 * Hope to provide some guidance!
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public class HelpWindow extends Window {

	public HelpWindow() {
		super();
		setIconStyle("icon-integrity");
		setClosable(true);
		setModal(true);
		setHeading("Using the Assessment Integrity Validator Builder");
		setSize(300, 300);
		setScrollMode(Scroll.AUTO);
		setLayout(new FillLayout());
// setAlignment(HorizontalAlignment.CENTER);

		add(new HtmlContainer(
				"<div style=\"font-size:14px !important;\">First, click \"Tables\" to add any "
						+ "tables you'll be using to define conditions.  Then, click \"Conditions\" to add "
						+ "conditions that would make an assessment be invalid. <br/><br/> You can add simple field "
						+ "comparisons, with support for grouping and \"AND\"/\"OR\" logic.</div>"));

		addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				close();
			}
		}));
	}

}
