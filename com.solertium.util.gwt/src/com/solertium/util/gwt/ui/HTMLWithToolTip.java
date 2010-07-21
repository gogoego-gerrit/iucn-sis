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
package com.solertium.util.gwt.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class HTMLWithToolTip extends HTML {
	private PopupPanel popupPanel;
	private boolean isPanelShowing = false;
	private String toolTip;
	private Timer timer;

	private int schedule = 1500;

	private int left;
	private int top;

	public String text;

	public HTMLWithToolTip(final String text, final int cutoff) {
		super(text);
		this.text = text;

		if (text != null && text.length() > cutoff) {
			setHTML((toolTip = text).substring(0, cutoff) + "...");
		} else {
			toolTip = null;
		}

		if (toolTip != null) {
			popupPanel = new PopupPanel(true, false) {
				public void hide() {
					super.hide();
					isPanelShowing = false;
				}
			};
			sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
		}
	}
	
	public HTMLWithToolTip(final String text, final String toolTip) {
		super(text);
		this.toolTip = toolTip;

		popupPanel = new PopupPanel(true, false) {
			public void hide() {
				super.hide();
				isPanelShowing = false;
			}
		};
		sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
	}

	public String getTooltipText() {
		return toolTip;
	}

	public void onBrowserEvent(final Event evt) {
		switch (DOM.eventGetType(evt)) {
		case Event.ONMOUSEOVER: {
			left = DOM.eventGetClientX(evt);
			top = DOM.eventGetClientY(evt);
			timer = new Timer() {
				public void run() {
					showPopup(left, top);
				}
			};
			timer.schedule(schedule);
			break;
		}
		case Event.ONMOUSEOUT: {
			try {
				timer.cancel();
			} catch (final Exception e) {
			}
			if (isPanelShowing && popupPanel != null) {
				popupPanel.hide();
				isPanelShowing = false;
			}
			break;
		}
		}
	}

	public void setSchedule(final int schedule) {
		this.schedule = schedule;
	}

	private void showPopup(int left, int top) {
		if (popupPanel != null && !isPanelShowing) {
			popupPanel.setWidget(new StyledHTML(toolTip, "fontSize80"));
			popupPanel.addStyleName("CIPD_PopupPanel");

			popupPanel.setWidth("300px");
			
			//This is the implementation of setPopupPostitionAndShow(), but 
			//I reimplement it so I can use the non-final variables left & top
			
			popupPanel.setVisible(false);
			popupPanel.show();
			
			final int bottom = Window.getClientHeight();
			final int right = Window.getClientWidth();

			if (left + popupPanel.getOffsetWidth() > right) {
				left = right - popupPanel.getOffsetWidth();
			}
			if (top + popupPanel.getOffsetHeight() > bottom) {
				top = bottom - popupPanel.getOffsetHeight();
			}
					
			popupPanel.setPopupPosition(left, top);		
			
			popupPanel.setVisible(isPanelShowing = true);
		}
	}
}