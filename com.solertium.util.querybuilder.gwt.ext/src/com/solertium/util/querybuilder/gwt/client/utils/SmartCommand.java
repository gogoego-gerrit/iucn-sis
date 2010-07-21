package com.solertium.util.querybuilder.gwt.client.utils;

import com.google.gwt.user.client.Command;

public abstract class SmartCommand implements Command {
	
	private CloseListener closeListener; 
	
	public abstract void doAction();
	
	public void setCloseListener(CloseListener closeListener) {
		this.closeListener = closeListener;
	}
	
	public void execute() {
		doAction();
		if (closeListener != null)
			closeListener.onClose();
	}
	
	public static interface CloseListener {
		
		public void onClose();
		
	}
}
