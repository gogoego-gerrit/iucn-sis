package org.iucn.sis.client.panels;

import org.iucn.sis.client.api.caches.MarkedCache;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.Widget;

public class MonkeyNavigatorPanel extends LayoutContainer {
	
	private final ToolBar header;
	private final Html heading;
	
	private final LayoutContainer content;
	
	public MonkeyNavigatorPanel() {
		this(new FlowLayout());
	}
	
	public MonkeyNavigatorPanel(Layout layout) {
		super();
		super.setLayout(new FillLayout());
		setBorders(true);
		
		heading = new Html();
		heading.addStyleName("moneky_nav_heading");
		
		header = new ToolBar();
		header.setSpacing(4);
		header.add(heading);
		header.add(new FillToolItem());
		
		final LayoutContainer headerWrapper = new LayoutContainer(new FillLayout());
		headerWrapper.addStyleName("monkey_nav_header");
		headerWrapper.add(header);
		
		content = new LayoutContainer();
		content.setLayout(layout);
		content.setLayoutOnChange(true);
		content.setBorders(false);
		content.setScrollMode(Scroll.AUTOY);
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(header, new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
		container.add(content, new BorderLayoutData(LayoutRegion.CENTER));
		
		super.add(container);
	}
	
	public void setHeading(String text) {
		heading.setHtml(text);
	}
	
	@Override
	public void setLayout(Layout layout) {
		content.setLayout(layout);
	}
	
	@Override
	public void setLayoutOnChange(boolean layoutOnChange) {
		content.setLayoutOnChange(layoutOnChange);
	}
	
	@Override
	protected boolean add(Component item) {
		return content.add(item);
	}
	
	@Override
	public boolean add(Widget widget) {
		return content.add(widget);
	}
	
	@Override
	public boolean add(Widget widget, LayoutData layoutData) {
		return content.add(widget, layoutData);
	}
	
	/**
	 * @deprecated use addTool(Component item) instead. 
	 */
	public ToolBar getToolBar() {
		return header;
	}
	
	public void addTool(Component item) {
		header.add(item);
	}
	
	@Override
	public boolean removeAll() {
		return content.removeAll();
	}
	
	protected IconButton createIconButton(String icon, String title, SelectionListener<IconButtonEvent> listener) {
		IconButton button = new IconButton(icon);
		button.addSelectionListener(listener);
		button.setTitle(title);
		
		return button;
	}
	
	protected Menu createMarkingContextMenu(SelectionListener<MenuEvent> listener) {
		Menu menu = new Menu();
		menu.add(createMenuItem("green-menu", MarkedCache.GREEN, "Mark Green", listener));
		menu.add(createMenuItem("blue-menu", MarkedCache.BLUE, "Mark Blue", listener));
		menu.add(createMenuItem("red-menu", MarkedCache.RED, "Mark Red", listener));
		menu.add(createMenuItem("regular-menu", MarkedCache.NONE, "Unmark",  listener));
		
		return menu;
	}
	
	protected MenuItem createMenuItem(String style, String itemID, String text, SelectionListener<MenuEvent> listener) {
		MenuItem item = new MenuItem();
		item.addStyleName(style);
		item.setItemId(itemID);
		item.setText(text);
		item.addSelectionListener(listener);
		
		return item;
	}
	
	protected void navigate(WorkingSet ws, Taxon taxon, Assessment assessment) {
		StateManager.impl.setState(ws, taxon, assessment);
	}

}
