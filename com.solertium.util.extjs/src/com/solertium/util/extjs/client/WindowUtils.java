/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */

package com.solertium.util.extjs.client;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;

public class WindowUtils {

	/**
	 * A listener that, if a button with "yes" or "ok" as its text is clicked,
	 * will invoke the onYes() function. Otherwise, it will invoke onNo().
	 * 
	 * DO NOT override the handleEvent function, if you want to use the internal
	 * onYes() and onNo() routing functionality.
	 * 
	 * @author adam
	 * 
	 */
	public static abstract class MessageBoxListener implements Listener<MessageBoxEvent> {

		public final void handleEvent(MessageBoxEvent be) {
			if (be.getButtonClicked() != null) {
				if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
					onYes();
				} else if (be.getButtonClicked().getItemId().equals(Dialog.NO)) {
					onNo();
				}
			}
		};

		public abstract void onNo();

		public abstract void onYes();
	}
	
	public static abstract class SimpleMessageBoxListener extends MessageBoxListener {
	
		public void onNo() {
			//Nothing to do.
		}
		
	}

	public static MessageBox loadingBox;

	/**
	 * Shows a Confirm popup
	 * 
	 * @param message
	 */
	public static MessageBox confirmAlert(final String title, final String message, final Listener<MessageBoxEvent> listener) {
		return confirmAlert(title, message, listener, 300, 300);
	}

	/**
	 * Shows a Confirm popup with sizing applied
	 * 
	 * @param message
	 */
	public static MessageBox confirmAlert(final String title, final String message, final Listener<MessageBoxEvent> listener,
			final int width, final int height) {
		return MessageBox.confirm(title, message, listener);
	}

	public static MessageBox confirmAlert(final String title, final String message, final MessageBoxListener listener,
			final String yesText, final String noText) {
		final MessageBox box = new MessageBox();
		box.setMessage(message);
		box.setTitle(title);
		box.setMinWidth(300);
		final Button yesButton = new Button(yesText);
		final Button noButton = new Button(noText);
		noButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				listener.onNo();	
				box.close();
			}
		});
		yesButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
		
			@Override
			public void componentSelected(ButtonEvent ce) {
				listener.onYes();
				box.close();
			}
		});
		box.getDialog().getButtonBar().removeAll();
		box.getDialog().getButtonBar().add(yesButton);
		box.getDialog().getButtonBar().add(noButton);
		box.show();
		
		return box;
	}

	public static void confirmModalAlert(final String title, final String message, final MessageBoxListener listener,
			final String yesText, final String noText) {
		MessageBox box = new MessageBox();
		box.setModal(true);
		box.setMessage(message);
		box.setTitle(title);
		box.setMinWidth(300);
		final Button yesButton = new Button(yesText);
		final Button noButton = new Button(noText);
		Listener<MessageBoxEvent> boxListener = new Listener<MessageBoxEvent>() {

			public void handleEvent(MessageBoxEvent be) {
				if (be.getButtonClicked().equals(yesButton))
					listener.onYes();
				else
					listener.onNo();

			}
		};
		box.addCallback(boxListener);
		box.getDialog().getButtonBar().removeAll();
		box.getDialog().getButtonBar().add(yesButton);
		box.getDialog().getButtonBar().add(noButton);
		box.show();

	}

	/**
	 * Shows an Error popup with the default title "Error"
	 * 
	 * @param message
	 */
	public static void errorAlert(final String message) {
		errorAlert("Error", message);
	}

	/**
	 * Shows an Error popup
	 * 
	 * @param message
	 */
	public static void errorAlert(final String title, final String message) {
		MessageBox.alert(title, message, new Listener<MessageBoxEvent>() {
			public void handleEvent(MessageBoxEvent be) {
				be.getWindow().hide();
			}
		});

	}
	
	public static Window newWindow(String heading) {
		return newWindow(heading, null);
	}
	
	public static Window newWindow(String heading, String iconStyle) {
		return newWindow(heading, iconStyle, true);
	}
	
	public static Window newWindow(String heading, String iconStyle, boolean modal) {
		return newWindow(heading, iconStyle, modal, true);
	}
	
	/**
	 * Returns the (formerly Shell) Ext-GWT Window using the args.
	 * @param heading
	 * @param iconStyle TODO
	 * @param modal
	 * @param resizable
	 * 
	 * @return the Window
	 */
	public static Window newWindow(String heading, String iconStyle, boolean modal, boolean resizable) {
		Window window = new Window();
		window.setConstrain(true);
		window.setMaximizable(true);
		window.setModal(modal);
		window.setResizable(resizable);
		window.setHeading(heading);
		window.setButtonAlign(HorizontalAlignment.CENTER);
		if (iconStyle != null)
			window.setIconStyle(iconStyle);
		
		return window;
	}

	/**
	 * Hides the default loading panel.
	 */
	public static void hideLoadingAlert() {
		if (loadingBox != null)
			loadingBox.close();
	}

	/**
	 * Shows an Info popup with the default title "Info"
	 * 
	 * @param message
	 */
	public static void infoAlert(final String message) {
		infoAlert("Info", message);
	}

	/**
	 * Shows an Info popup
	 * 
	 * @param message
	 */
	public static void infoAlert(final String title, final String message) {
		// WindowManager.get().hideAll();

		MessageBox.alert(title, message, new Listener<MessageBoxEvent>() {
			public void handleEvent(MessageBoxEvent be) {
				be.getWindow().hide();
			}
		});

		// final MessageBox messageBox = new MessageBox();
		// messageBox.show();
		// messageBox.setIcon( MessageBox.INFO );
		// messageBox.setButtons(MessageBox.OK);
		// messageBox.setMessage(message);
		// messageBox.setTitle(title);
		// messageBox.getDialog().setSize(300, 300);
		// messageBox.show();
	}

	/**
	 * Shows the "loading" box with a default loading title, and default
	 * progress bar text.
	 * 
	 * @param message
	 *            to show in the box
	 * @return wait MessageBox - BE SURE TO CLOSE IT WHEN YOU'RE DONE...
	 */
	public static void showLoadingAlert(String message) {
		if (loadingBox != null)
			loadingBox.close();

		loadingBox = MessageBox.wait("Please wait...", message, "");
	}

}
