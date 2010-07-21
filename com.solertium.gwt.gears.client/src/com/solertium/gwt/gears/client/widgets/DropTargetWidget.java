package com.solertium.gwt.gears.client.widgets;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;


public class DropTargetWidget extends AbsolutePanel{
	
	
	
	public DropTargetWidget() {
		setSize("500px", "500px");
		setStyleName("dropzone");
		getElement().setAttribute("id", "dropZone");
	}
	
	@Override
	public void onBrowserEvent(Event event) {
		super.onBrowserEvent(event);
		System.out.println(event.getType());
	}
	
	
	
		
		
	

}
