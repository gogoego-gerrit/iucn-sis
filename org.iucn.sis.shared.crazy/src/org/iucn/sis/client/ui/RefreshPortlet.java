package org.iucn.sis.client.ui;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.custom.Portlet;

/**
 * A content panel that has a refresh icon built in. You must overload the
 * refresh function in order to use this class.
 * 
 * @author liz.schwartz
 * 
 */
public abstract class RefreshPortlet extends Portlet {

	public RefreshPortlet() {
		super();
	}

	public RefreshPortlet(String baseStyle) {
		super();
		// setStyleName(baseStyle);

		// setIconStyle("x-tool-refresh");
	}

	private void buildRefreshButton() {
		ToolButton refreshBtn = new ToolButton("x-tool-refresh");
		refreshBtn.addStyleName("x-tool-refresh");
		refreshBtn.addSelectionListener(new SelectionListener<IconButtonEvent>() {
			@Override
			public void componentSelected(IconButtonEvent ce) {
				refresh();
			}
		});

		head.addTool(refreshBtn);
	}

	/**
	 * Applies default behavior configurations to this Portlet. Doesn't need to
	 * be run if this RefreshContentPanel isn't going to be attached to a
	 * Portal.
	 */
	public void configureThisAsPortlet() {
		setCollapsible(true);
		setAnimCollapse(false);
		// getHeader().addTool(new ToolButton("x-tool-gear"));
		// getHeader().addTool(new ToolButton("x-tool-close", new
		// SelectionListener<ComponentEvent>() {
		//
		// @Override
		// public void componentSelected(ComponentEvent ce) {
		// removeFromParent();
		// }
		//
		// }));
	}

	@Override
	protected void initTools() {
		// super.initTools();
		buildRefreshButton();
	}

	public abstract void refresh();
}
