package org.iucn.sis.client.api.utils;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.Window;

public class BasicWindow extends Window {
	
	public BasicWindow() {
		this(null);
	}
	
	public BasicWindow(String heading) {
		this(heading, null);
	}
	
	public BasicWindow(String heading, String iconStyle) {
		this(heading, iconStyle, true);
	}
	
	public BasicWindow(String heading, String iconStyle, boolean modal) {
		this(heading, iconStyle, modal, true);
	}
	
	public BasicWindow(String heading, String iconStyle, boolean modal, boolean resizable) {
		super();
		setConstrain(true);
		setMaximizable(true);
		setModal(modal);
		setResizable(resizable);
		setButtonAlign(HorizontalAlignment.CENTER);
		if (heading != null)
			setHeading(heading);
		if (iconStyle != null)
			setIconStyle(iconStyle);
	}

}
