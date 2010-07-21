package com.solertium.util.querybuilder.gwt.client.utils;

import com.google.gwt.user.client.ui.Image;
import com.solertium.util.gwt.ui.ButtonIcon;

public abstract class QBButtonIcon extends ButtonIcon {

	public QBButtonIcon(final String text, final Image image) {
		super(text, image, "CIPD_ButtonIcon", true);
	}

	public QBButtonIcon(final String text, final String url) {
		this(text, url, true);
	}

	public QBButtonIcon(final String text, final String url,
			final boolean isIconOnLeft) {
		super(text, new Image(url), "CIPD_ButtonIcon", isIconOnLeft);
	}

}
