/*
 * Copyright 2008 Grant Slender <gslender@iinet.com.au>
 * 
 * Ideas/concepts borrowed from Thorsten Suckow-Homberg <ts@siteartwork.de>
 * http://www.siteartwork.de/wizardcomponent
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ext.ux.wizard.client;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Part of wizard window, WizardCard is used to show & validate a step in the
 * wizard process.</br></br>
 * 
 * WizardCard wc = new WizardCard("Welcome");</br> wc.setHtmlText("Welcome to
 * the example for <strong>ext.ux.WizardWindow</strong>, "</br> + "a ExtGWT user
 * extension for creating wizards.<br/>
 * <br/>
 * "</br> + "Please click the
 * \"next\"-button and fill out all form values.");</br> cards.add(wc);</br>
 * 
 */
public class WizardCard extends LayoutContainer {

	private ArrayList<Listener<BaseEvent>> finishListeners;
	
	private String cardtitle;
	protected FormPanel panel;

	private boolean showCancelButton = true;
	private boolean showFinishButton = true;
	private boolean showPreviousButton = true;

	/**
	 * Creates a new wizard card.
	 * 
	 * @param cardtitle
	 *            title string of this card
	 */
	public WizardCard(String cardtitle) {
		super();
		this.cardtitle = cardtitle;
		setLayout(new RowLayout(Orientation.VERTICAL));
	}

	/**
	 * Adds a Listener<BaseEvent> to the list of listeners that will be notified
	 * when the card is finished.</br> Note: this event is only called on the
	 * last card and when the finish button is clicked.
	 * 
	 * @param listener
	 *            the Listener<BaseEvent> to be added
	 */
	public void addFinishListener(Listener<BaseEvent> listener) {
		if (finishListeners == null)
			finishListeners = new ArrayList<Listener<BaseEvent>>();
		finishListeners.add(listener);
	}

	/**
	 * Returns the currently set title.
	 * 
	 * @returns the current title of this card
	 */
	public String getCardTitle() {
		return cardtitle;
	}
	
	protected void beforeDraw(final AsyncCallback<Object> callback) {
		callback.onSuccess(null);
	}
	
	public boolean canSkip() {
		return false;
	}

	public boolean isShowCancelButton() {
		return showCancelButton;
	}

	public boolean isShowFinishButton() {
		return showFinishButton;
	}

	public boolean isShowPreviousButton() {
		return showPreviousButton;
	}

	/**
	 * Calls the isValid of the form (if set) and returns the result.
	 * 
	 * @returns the result of the form isValid(), or true if no form set
	 */
	public boolean isValid() {
		if (panel == null)
			return true;
		return panel.isValid();
	}
	
	protected void doServerValidation(final AsyncCallback<Object> callback) {
		callback.onSuccess(null);
	}

	/**
	 * Notifies the finish listeners, if applicable.
	 * 
	 * @return doit - continue with finish
	 */
	protected boolean notifyFinishListeners() {
		boolean ret = true;

		if (finishListeners != null) {
			for (Listener<BaseEvent> listener : finishListeners) {
				BaseEvent be = new BaseEvent(this);
				listener.handleEvent(be);

				if (be.isCancelled())
					ret = false;
			}
		}

		return ret;
	}

	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
	}

	/**
	 * Sets the FormPanel associated with this card.</br></br> Note: this panel
	 * will set height = 300, and the following to false;
	 * frame,borders,bodyborder,headervisible
	 * 
	 * @param panel
	 *            FormPanel to set
	 */
	public void setFormPanel(FormPanel panel) {
		this.panel = panel;
		panel.setHeight(300);
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setBodyBorder(false);
		panel.setHeaderVisible(false);
		add(panel);
	}

	/**
	 * Sets the HTML text associated with this card.
	 * 
	 * @param htmltext
	 *            HTML string to set
	 */
	public void setHtmlText(String htmltext) {
		final HtmlContainer container = new HtmlContainer(htmltext);
		container.addStyleName("wizard-htmltext");
		add(container);
	}

	public void setShowCancelButton(boolean showCancelButton) {
		this.showCancelButton = showCancelButton;
	}

	public void setShowFinishButton(boolean showFinishButton) {
		this.showFinishButton = showFinishButton;
	}

	public void setShowPreviousButton(boolean showPreviousButton) {
		this.showPreviousButton = showPreviousButton;
	}
}