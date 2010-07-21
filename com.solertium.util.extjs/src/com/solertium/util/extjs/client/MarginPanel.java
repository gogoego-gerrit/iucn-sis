/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.util.extjs.client;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * MarginPanel.java
 * 
 * @author user
 * 
 */
public class MarginPanel extends LayoutContainer implements WindowResizeListener {

	protected final int spacerHeight;
	protected final double marginWidth;

	protected final LayoutContainer center;

	public MarginPanel() {
		this(.05);
	}

	public MarginPanel(double marginWidth) {
		this(marginWidth, 10);
	}

	/**
	 * Creates a new MarginPanel with a specified margin width (percentage) and
	 * spacer size (pixels).
	 * 
	 * @param marginWidth
	 * @param spacerHeight
	 */
	public MarginPanel(double marginWidth, int spacerHeight) {
		super();
		setLayoutOnChange(true);
		
		Window.addWindowResizeListener(this);

		this.marginWidth = marginWidth;
		this.spacerHeight = spacerHeight;

		center = new LayoutContainer();

		draw();
	}

	@Override
	protected boolean add(Component item) {
		return add((Widget) item);
	}

	@Override
	public boolean add(Widget widget) {
		if (center.getItemCount() > 0)
			center.add(getSpacer());

		return center.add(widget);
	}

	public void clear() {
		center.removeAll();
	}

	protected void draw() {
		final BorderLayout bl = new BorderLayout();

		setLayout(bl);
		center.setScrollMode(Scroll.AUTO);		

		add(center, new BorderLayoutData(LayoutRegion.CENTER, 600));
		add(getMargin(), new BorderLayoutData(LayoutRegion.WEST, 
			(float) marginWidth, (int)marginWidth, (int)marginWidth
		));
		add(getMargin(), new BorderLayoutData(LayoutRegion.EAST, 
			(float) marginWidth, (int)marginWidth, (int)marginWidth
		));
	}
	
	public void setCenterHeight(int height) {
		center.setHeight(height);
	}

	private Widget getMargin() {
		HTML margin = new HTML("&nbsp;");
		margin.setWidth("100%");
		return margin;
	}

	private Widget getSpacer() {
		HTML spacer = new HTML("&nbsp;");
		spacer.setHeight(spacerHeight + "px");
		return spacer;
	}

	@Override
	public void onDetach() {
		Window.removeWindowResizeListener(this);
		super.onDetach();
	}

	@Override
	protected void onResize(int width, int height) {
		super.onResize(width, height);
		for (int i = 0; i < center.getItemCount(); i++) {
			Widget current = center.getWidget(i);
			if (current instanceof LayoutContainer)
				((LayoutContainer) current).layout();
		}
	}

	public void onWindowResized(int width, int height) {
		onResize(width, height);
	}

}
