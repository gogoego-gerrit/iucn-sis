package org.iucn.sis.client.utilities;

import com.google.gwt.user.client.ui.Button;

/**
 * IndexButton
 * 
 * A button implementation that stores its index relative to other buttons in
 * its group, and some String data
 * 
 * @author carl.scott
 */
public class IndexButton extends Button {
	private int index;
	private String data;

	public IndexButton(String name, int index) {
		super(name);
		this.index = index;
	}

	public String getData() {
		return data;
	}

	public int getIndex() {
		return index;
	}

	public void setData(String data) {
		this.data = data;
	}
}
