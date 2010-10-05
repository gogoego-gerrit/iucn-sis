package org.iucn.sis.client.api.utils.autocomplete;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;

public class AutoCompleteTextBox extends TextBox implements KeyboardListener, ChangeListener, ClickHandler {

	private static final int MAX_MATCHES_TO_DISPLAY = 50;

	private PopupPanel choicesPopup;
	private ListBox choices;
	private CompletionItems items;
	private boolean popupAdded = false;
	private boolean visible = false;

	/**
	 * Default Constructor
	 * 
	 */
	public AutoCompleteTextBox() {
		super();

		this.addKeyboardListener(this);
		// this.setStyleName("AutoCompleteTextBox");

		choices = new ListBox();
		choices.addChangeListener(this);
		choices.addClickHandler(this);
		// choices.setStyleName("list");

		choicesPopup = new PopupPanel(true);
		choicesPopup.add(choices);
		choicesPopup.addStyleName("SIS_AutoCompleteChoices");
		choices.addKeyboardListener(new KeyboardListener() {
			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if (keyCode == KEY_ESCAPE) {
					choices.clear();
					choicesPopup.hide();
				}
			}

			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
			}
		});

		items = new SimpleAutoCompletionItems(new String[] {});
	}

	// add selected item to textbox
	protected void complete() {
		if (choices.getItemCount() > 0) {
			this.setText(choices.getItemText(choices.getSelectedIndex()));
		}

		choices.clear();
		choicesPopup.hide();
	}

	/**
	 * Returns the used CompletionItems object
	 * 
	 * @return CompletionItems implementation
	 */
	public CompletionItems getCompletionItems() {
		return this.items;
	}

	/**
	 * A mouseclick in the list of items
	 */
	public void onChange(Widget arg0) {
		complete();
	}

	public void onClick(ClickEvent event) {
		complete();
	}

	/**
	 * Not used at all
	 */
	public void onKeyDown(Widget arg0, char arg1, int arg2) {
	}

	public void onKeyPress(Widget arg0, char arg1, int arg2) {
	}

	/**
	 * A key was released, start autocompletion
	 */
	public void onKeyUp(Widget arg0, char arg1, int arg2) {
		if (arg1 == KEY_ESCAPE) {
			visible = false;
			choicesPopup.hide();
		}

		if (arg1 == KEY_DOWN) {
			int selectedIndex = choices.getSelectedIndex();

			if (choices.getItemCount() != 0)
				selectedIndex++;

			if (selectedIndex >= choices.getItemCount()) {
				selectedIndex = 0;
			}

			if (choices.getItemCount() != 0) {
				choices.setSelectedIndex(selectedIndex);
				return;
			}
		}

		if (arg1 == KEY_UP) {
			int selectedIndex = choices.getSelectedIndex();

			if (choices.getItemCount() != 0)
				selectedIndex--;

			if (selectedIndex < 0) {
				selectedIndex = choices.getItemCount() - 1;
			}

			if (choices.getItemCount() != 0) {
				choices.setSelectedIndex(selectedIndex);
				return;
			}
		}

		if (arg1 == KEY_ENTER) {
			if (visible) {
				complete();
			}
			return;
		}

		if (arg1 == KEY_ESCAPE) {
			choices.clear();
			choicesPopup.hide();
			visible = false;
			return;
		}

		String text = this.getText();
		String[] matches = new String[] {};

		// If the box is not empty
		// if(text.length() > 0) {
		matches = items.getCompletionItems(text);
		// }

		// If there are matches & matches is of good size
		if (matches.length > 0 && matches.length <= MAX_MATCHES_TO_DISPLAY) {
			choices.clear();

			for (int i = 0; i < matches.length; i++) {
				choices.addItem(matches[i]);
			}

			// if there is only one match and it is what is in the
			// text field anyways there is no need to show autocompletion
			if (matches.length == 1 && matches[0].compareTo(text) == 0) {
				choicesPopup.hide();
			} else {
				choices.setSelectedIndex(0);
				choices.setVisibleItemCount(matches.length + 1);

				if (!popupAdded) {
					RootPanel.get().add(choicesPopup);
					popupAdded = true;
				}

				choicesPopup.show();
				visible = true;

				int leftPosition = 0;
				int topPosition = 0;

				// Find a good top position
				if (getAbsoluteTop() + getOffsetHeight() + 15 > Window.getClientHeight()) {
					topPosition = getAbsoluteTop() - getOffsetHeight() - 30;
				} else {
					topPosition = getAbsoluteTop() + getOffsetHeight();
				}

				// Find a good Left position
				if (getAbsoluteLeft() + getOffsetWidth() + 15 > Window.getClientWidth()) {
					leftPosition = getAbsoluteLeft() - getOffsetWidth();
				} else {
					leftPosition = getAbsoluteLeft();
				}

				choicesPopup.setPopupPosition(leftPosition, topPosition);
				choices.setWidth(this.getOffsetWidth() + "px");
			}
		}

		// There are no maches
		else {
			visible = false;
			choicesPopup.hide();
		}
	}

	/**
	 * Sets an "algorithm" returning completion items You can define your own
	 * way how the textbox retrieves autocompletion items by implementing the
	 * CompletionItems interface and setting the according object
	 * 
	 * @see SimpleAutoCompletionItem
	 * @param items
	 *            CompletionItem implementation
	 */
	public void setCompletionItems(CompletionItems items) {
		this.items = items;
	}

}// class AutoCompleteTextBox
