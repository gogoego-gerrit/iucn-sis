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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.CardPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;

/**
 * A wizard window intended to display wizard cards.</br></br>
 * 
 * // setup an array of WizardCards</br> ArrayList<WizardCard> cards = new
 * ArrayList<WizardCard>();</br> </br> // 1st card - a welcome</br> WizardCard
 * wc = new WizardCard("Welcome");</br> wc.setHtmlText(
 * "Welcome to the example for <strong>ext.ux.WizardWindow</strong>, "</br> +
 * "a ExtGWT user extension for creating wizards.<br/>
 * <br/>
 * "</br> +
 * "Please click the \"next\"-button and fill out all form values.");</br>
 * cards.add(wc);</br> </br> // 2nd or more cards...</br> // wc = new
 * WizardCard("More cards...");</br> // cards.add(wc);</br> // ...</br> </br>
 * WizardWindow wizwin = new WizardWindow(cards);</br>
 * wizwin.setHeading("A simple example for a wizard");</br>
 * wizwin.setHeaderTitle("Simple Wizard Example");</br> </br>
 * wizwin.show();</br>
 * 
 */

public class WizardWindow extends Window {

	protected class Header extends VerticalPanel {
		private HorizontalPanel indicatorPanel;
		private ProgressBar indicatorBar;
		private Html stepHTML;
		private Html titleHTML;

		protected Header() {
			super();
			setTableWidth("100%");
			setTableHeight("100%");
			setStyleName("ext-ux-wiz-Header");
			setBorders(true);

			titleHTML = new Html("");
			titleHTML.setStyleName("ext-ux-wiz-Header-title");
			add(titleHTML);

			if (progressIndicator == Indicator.DOT) {

				stepHTML = new Html("");
				stepHTML.setStyleName("ext-ux-wiz-Header-step");
				add(stepHTML);

				indicatorPanel = new HorizontalPanel();
				indicatorPanel.setStyleName("ext-ux-wiz-Header-stepIndicator-container");
				for (int i = 0; i < cards.size(); i++) {
					Image img = new Image("ext-ux-wiz-stepIndicator-off.png");
					img.setStyleName("ext-ux-wiz-Header-stepIndicator");
					indicatorPanel.add(img);
				}
				TableData td = new TableData();
				td.setHorizontalAlign(HorizontalAlignment.RIGHT);
				add(indicatorPanel, td);
			}
			if (progressIndicator == Indicator.PROGRESSBAR) {
				indicatorBar = new ProgressBar();
				LayoutContainer lc = new LayoutContainer();
				lc.add(indicatorBar);
				lc.setWidth("50%");
				TableData td = new TableData();
				td.setHorizontalAlign(HorizontalAlignment.RIGHT);
				td.setPadding(5);
				add(lc, td);
			}
		}

		@Override
		protected void onRender(Element parent, int pos) {
			super.onRender(parent, pos);
			setStyleAttribute("borderLeft", "none");
			setStyleAttribute("borderRight", "none");
			setStyleAttribute("borderTop", "none");
		}

		protected void updateIndicatorStep(String cardtitle) {

			final String stepStr = indicateStepText + (1 + currentStep) + indicateOfText + cards.size() + " : "
					+ cardtitle;
			final double stepRatio = (double) (1 + currentStep) / (double) cards.size();
			titleHTML.setHtml(headerTitle);

			if (progressIndicator == Indicator.DOT) {
				stepHTML.setHtml(stepStr);
				indicatorPanel.removeAll();
				for (int i = 0; i < cards.size(); i++) {

					Image img = new Image("ext-ux-wiz-stepIndicator-off.png");
					img.setStyleName("ext-ux-wiz-Header-stepIndicator");
					if (i == currentStep) {
						img.setUrl("ext-ux-wiz-stepIndicator-on.png");
					}
					indicatorPanel.add(img);
				}
				indicatorPanel.layout();
			}
			if (progressIndicator == Indicator.PROGRESSBAR) {
				DeferredCommand.addCommand(new Command() {
					public void execute() {
						indicatorBar.updateProgress(stepRatio, stepStr);
					}
				});
			}
		}
	}

	/**
	 * Indicator type enumeration.
	 */
	public enum Indicator {
		NONE, DOT, PROGRESSBAR
	}

	private String previousButtonText = "< Previous";
	private String nextButtonText = "Next >";
	private String cancelButtonText = "Cancel";
	private String finishButtonText = "Finish";
	private String indicateStepText = "Step ";

	private String indicateOfText = " of ";
	private int currentStep = 0;

	protected final ArrayList<WizardCard> cards;
	private String headerTitle;
	private Header headerPanel;
	private CardPanel cardPanel;
	private Button prevBtn;
	private Button nextBtn;
	private Button cancelBtn;
	private Indicator progressIndicator = Indicator.DOT;

	private String wizMainImg = "ext-ux-wiz-default-pic.png";

	/**
	 * Creates a new wizard window.
	 * 
	 * @param cards
	 *            an ArrayList of WizardCard/s
	 */
	public WizardWindow(ArrayList<WizardCard> cards) {
		super();
		this.cards = cards;
		setSize(540, 400);
		setClosable(true);
		setResizable(false);
		setModal(true);
	}
	
	/**
	 * @return the cancelButtonText
	 */
	public String getCancelButtonText() {
		return cancelButtonText;
	}

	/**
	 * @return the finishButtonText
	 */
	public String getFinishButtonText() {
		return finishButtonText;
	}

	/**
	 * Returns the currently set header title
	 * 
	 * @return the header title
	 */
	public String getHeaderTitle() {
		return headerTitle;
	}

	/**
	 * @return the indicateOfText
	 */
	public String getIndicateOfText() {
		return indicateOfText;
	}

	/**
	 * @return the indicateStepText
	 */
	public String getIndicateStepText() {
		return indicateStepText;
	}

	/**
	 * @return the nextButtonText
	 */
	public String getNextButtonText() {
		return nextButtonText;
	}

	/**
	 * @return the previousButtonText
	 */
	public String getPreviousButtonText() {
		return previousButtonText;
	}

	private void onButtonPressed(Button button) {
		if (button == cancelBtn) {
			hide(button);
			return;
		}
		else if (button == prevBtn) {
			if (this.currentStep > 0) {
				currentStep--;
				updateWizard();
			}
		}
		else if (button == nextBtn) {
			if (!cards.get(currentStep).isValid())
				return;
			
			cards.get(currentStep).doServerValidation(new AsyncCallback<Object>() {
				public void onFailure(Throwable caught) { }
				public void onSuccess(Object result) {
					if (currentStep + 1 == cards.size()) {
						if (cards.get(currentStep).notifyFinishListeners()) {}
							hide();
					} else {
						currentStep++;
						if (cards.get(currentStep).canSkip() && cards.get(currentStep).isValid() && ((currentStep + 1) < cards.size()))
							onButtonPressed(nextBtn);
						updateWizard();
					}	
				}
			});
		}
	}

	@Override
	protected void onRender(Element parent, int pos) {
		setLayout(new BorderLayout());

		prevBtn = new Button(previousButtonText);
		nextBtn = new Button(nextButtonText);
		cancelBtn = new Button(cancelButtonText);

		ButtonBar buttonBar = getButtonBar();
		buttonBar.add(prevBtn);
		buttonBar.add(nextBtn);
		buttonBar.add(cancelBtn);
		buttonBar.addListener(Events.Select, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent bbe) {
				onButtonPressed(bbe.getButton());
			}
		});
		
		super.onRender(parent, pos);

		headerPanel = new Header();
		add(headerPanel, new BorderLayoutData(LayoutRegion.NORTH, 60));
		cardPanel = new CardPanel();
		cardPanel.setStyleAttribute("padding", "40px 15px 5px 5px");
		cardPanel.setStyleAttribute("backgroundColor", "#F6F6F6");
		LayoutContainer lc = new LayoutContainer();
		lc.setStyleAttribute("backgroundColor", "#F6F6F6");
		int width = 100; // min width allowed
		if (wizMainImg != null) {
			Image leftimage = new Image(wizMainImg);
			lc.add(leftimage);
			width = Math.max(100, leftimage.getWidth());
		}
		add(lc, new BorderLayoutData(LayoutRegion.WEST, width));
		add(cardPanel, new BorderLayoutData(LayoutRegion.CENTER));
		for (WizardCard wizardCard : cards) {
			cardPanel.add(wizardCard);
		}

		if (cards.size() > 0) {
			updateWizard();
		}
	}

	/**
	 * @param cancelButtonText
	 *            the cancelButtonText to set. Defaults to "Cancel".
	 */
	public void setCancelButtonText(String cancelButtonText) {
		this.cancelButtonText = cancelButtonText;
	}

	/**
	 * @param finishButtonText
	 *            the finishButtonText to set. Defaults to "Finish".
	 */
	public void setFinishButtonText(String finishButtonText) {
		this.finishButtonText = finishButtonText;
	}

	/**
	 * Sets the title located in the top header
	 * 
	 * @param hdrtitle
	 *            string value
	 */
	public void setHeaderTitle(String hdrtitle) {
		this.headerTitle = hdrtitle;
	}

	/**
	 * @param indicateOfText
	 *            the indicateOfText to set. Defaults to " of ".
	 */
	public void setIndicateOfText(String indicateOfText) {
		this.indicateOfText = indicateOfText;
	}

	/**
	 * @param indicateStepText
	 *            the indicateStepText to set. Defaults to "Step ".
	 */
	public void setIndicateStepText(String indicateStepText) {
		this.indicateStepText = indicateStepText;
	}

	/**
	 * Sets the wizard image picture, or set to null if you don't wish to
	 * display an image. Defaults to "ext-ux-wiz-default-pic.png"
	 * 
	 * @param String
	 *            url path
	 */
	public void setMainImg(String url) {
		wizMainImg = url;
	}

	/**
	 * @param nextButtonText
	 *            the nextButtonText to set. Defaults to "Next >".
	 */
	public void setNextButtonText(String nextButtonText) {
		this.nextButtonText = nextButtonText;
	}

	/**
	 * @param previousButtonText
	 *            the previousButtonText to set. Defaults to "< Previous".
	 */
	public void setPreviousButtonText(String previousButtonText) {
		this.previousButtonText = previousButtonText;
	}

	/**
	 * Sets the progress indicator type. Defaults to DOT
	 * 
	 * @param Indicator
	 *            value
	 */
	public void setProgressIndicator(Indicator value) {
		progressIndicator = value;
	}

	private void updateWizard() {
		final WizardCard wc = cards.get(currentStep);		
		wc.beforeDraw(new SuccessCallback() {
			public void onSuccess(Object result) {
				headerPanel.updateIndicatorStep(wc.getCardTitle());	
				WizardWindow.this.cardPanel.setActiveItem(wc);
				wc.layout();

				if (currentStep + 1 == cards.size()) {
					nextBtn.setText(finishButtonText);
				} else {
					nextBtn.setText(nextButtonText);
				}

				if (currentStep == 0) {
					prevBtn.setEnabled(false);
				} else {
					prevBtn.setEnabled(true);
				}
			}
		});	
	}
	
	private static abstract class SuccessCallback implements AsyncCallback<Object> {
		public void onFailure(Throwable caught) {
			onSuccess(null);
		}
	}
}
