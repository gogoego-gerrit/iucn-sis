package com.solertium.util.querybuilder.gwt.client.utils;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public abstract class QBButtonIcon extends Button {

	public QBButtonIcon(final String text, final Image image) {
		super(text);
		addStyleName("CIPD_ButtonIcon");
		addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				onClick(QBButtonIcon.this);
			}
		});
		//super(text, image, "CIPD_ButtonIcon", true);
	}

	public QBButtonIcon(final String text, final String url) {
		super(text);
		addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				onClick(QBButtonIcon.this);
			}
		});
	}

	public QBButtonIcon(final String text, final String url,
			final boolean isIconOnLeft) {
		super(text);
		addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				onClick(QBButtonIcon.this);
			}
		});
		//super(text, new Image(url), "CIPD_ButtonIcon", isIconOnLeft);
	}
	
	public abstract void onClick(Widget sender);

}
