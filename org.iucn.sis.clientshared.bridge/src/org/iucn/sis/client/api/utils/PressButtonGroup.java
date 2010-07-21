/**
 * PressButtonGroup.java
 * 
 * Holds a group of buttons that react to each other.  When one of the members 
 * is clicked, it has a pressed style.
 * 
 * @author carl.scott
 */

package org.iucn.sis.client.api.utils;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PressButtonGroup {
	private ArrayList buttons; // All buttons in button group
	private String defaultStyle; // Unclicked buttons
	private String pressedStyle; // Clicked button

	private ClickHandler pressListener; // Does the swap

	private Panel displayPanel; // Adds them to a nice displayable panel of
	// The user's choice

	private int currentIndex = 0; // The currently clicked button

	public PressButtonGroup(String defaultStyle, String pressedStyle) {
		this(defaultStyle, pressedStyle, new VerticalPanel());
	}

	/**
	 * Creates a new button group
	 * 
	 * @param defaultStyle
	 *            the unclicked button style
	 * @param pressedStyle
	 *            the clicked button style
	 * @param displayPanel
	 *            panel to show the buttons in
	 */
	public PressButtonGroup(String defaultStyle, String pressedStyle, Panel displayPanel) {
		this.defaultStyle = defaultStyle;
		this.pressedStyle = pressedStyle;
		this.buttons = new ArrayList();
		this.displayPanel = displayPanel;
		pressListener = new ClickHandler() {
			public void onClick(ClickEvent event) {
				buttonPressed((IndexButton) event.getSource());
			}
		};
	}

	public void addNewButton(IndexButton button, String data, ClickHandler listener) {
		button.setStyleName(defaultStyle);
		button.setData(data);
		button.addClickHandler(pressListener);
		if (listener != null)
			button.addClickHandler(listener);
		buttons.add(button);
	}

	/** ADD A SINGLE NEW BUTTON **/

	public void addNewButton(String buttonName) {
		addNewButton(buttonName, "", null);
	}

	public void addNewButton(String buttonName, String data) {
		addNewButton(buttonName, data, null);
	}

	/**
	 * Adds a new button to the group
	 * 
	 * @param buttonName
	 *            display name of the button
	 * @param data
	 *            extra data to store with the button
	 * @param listener
	 *            any listener you want to attach
	 */
	public void addNewButton(String buttonName, String data, ClickHandler listener) {
		IndexButton button = new IndexButton(buttonName, buttons.size());
		button.setStyleName(defaultStyle);
		button.setData(data);
		button.addClickHandler(pressListener);
		if (listener != null)
			button.addClickHandler(listener);
		buttons.add(button);
	}

	/** ADD A GROUP OF BUTTONS **/

	public void addNewButtonGroup(ArrayList buttonNames) {
		addNewButtonGroup(buttonNames, null, null);
	}

	public void addNewButtonGroup(ArrayList buttonNames, ArrayList data) {
		addNewButtonGroup(buttonNames, data, null);
	}

	/**
	 * Adds a group of new buttons
	 * 
	 * @param buttonNames
	 *            set of strings of button names
	 * @param data
	 *            set of strings of button data
	 * @param listener
	 *            any listener you want for the button (all buttons have the
	 *            same listener)
	 */
	public void addNewButtonGroup(ArrayList buttonNames, ArrayList data, ClickHandler listener) {
		for (int i = 0; i < buttonNames.size(); i++) {
			try {
				addNewButton((String) buttonNames.get(i), (String) data.get(i), listener);
			} catch (Exception e) {
				try {
					addNewButton((String) buttonNames.get(i), "", listener);
				} catch (Exception f) {
				}
			}
		}
	}

	/**
	 * Private helper method to swap the styles. Buttons are smart enough so you
	 * dont have to look through them
	 * 
	 * @param sender
	 *            the clicked button
	 */
	private void buttonPressed(IndexButton sender) {
		((IndexButton) buttons.get(currentIndex)).setStyleName(defaultStyle);
		((IndexButton) buttons.get(currentIndex = sender.getIndex())).setStyleName(pressedStyle);
	}

	/**
	 * Clicks the button given its index
	 * 
	 * @param index
	 *            the index
	 */
	public void click(int index) {
		try {
			((IndexButton) buttons.get(index)).click();
		} catch (IndexOutOfBoundsException e) {
		}
	}

	/**
	 * Clicks the button given its button text
	 * 
	 * @param buttonText
	 *            the button text
	 */
	public void click(String buttonText) {
		for (int i = 0; i < buttons.size(); i++) {
			if (((IndexButton) buttons.get(i)).getText().equalsIgnoreCase(buttonText)) {
				click(i);
			}
		}
	}

	/**
	 * Get the data of the currently clicked button
	 * 
	 * @return the data
	 */
	public String getCurrentData() {
		return ((IndexButton) buttons.get(currentIndex)).getData();
	}

	/**
	 * Get the index of the currently clicked button
	 * 
	 * @return the index
	 */
	public int getCurrentIndex() {
		return currentIndex;
	}

	/**
	 * Generates a display panel and returns it
	 * 
	 * @return the display panel with buttons
	 */
	public Panel getDisplayPanel() {
		displayPanel.clear();
		for (int i = 0; i < buttons.size(); i++) {
			displayPanel.add((IndexButton) buttons.get(i));
		}
		return displayPanel;
	}

	/**
	 * Set the display panel of your choice
	 * 
	 * @param panel
	 *            the panel
	 */
	public void setDisplayPanel(Panel panel) {
		this.displayPanel = panel;
	}

}// class ButtonGroup
