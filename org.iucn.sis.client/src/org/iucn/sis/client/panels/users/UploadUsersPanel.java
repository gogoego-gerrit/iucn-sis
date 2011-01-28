package org.iucn.sis.client.panels.users;

import org.iucn.sis.client.api.utils.UriBase;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;

public class UploadUsersPanel extends LayoutContainer {

	public UploadUsersPanel() {
		super(new FillLayout());
		
		draw();
	}
	
	public void draw() {
		final LayoutContainer panel = new LayoutContainer(new FlowLayout());
		panel.add(new Html("Please select a spreadsheet to upload to create new users and profiles."));
		panel.add(new Html("&nbsp;"));
		panel.add(new Html("The columns are required to be in the following order (* = required):"));
		panel.add(new Html("email*,type* (\"u\" for user, \"p\" for profile only),first_name,last_name*,mid_initial,nickname,affiliation"));
		panel.add(new Html("&nbsp;"));
		panel.add(new Html("Click the button below to begin.  You may be required to enter your username and password."));
		panel.add(new Html("&nbsp;"));
		panel.add(new Button("Begin Upload", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				Window window = new Window();
				window.setModal(true);
				window.setClosable(true);
				window.setSize(500, 500);
				window.setHeading("Bulk Upload Users from Spreadsheet");
				window.setUrl(UriBase.getInstance().getUserBase() + "/import/csv");
				
				window.show();
			}
		}));
		
		add(panel);
	}
	

}
