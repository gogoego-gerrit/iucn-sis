package org.iucn.sis.client.panels.taxomatic;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.utils.TaxonomyBrowserPanel;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.ui.HTML;

/**
 * TaxonChooser.java
 * 
 * UI to choose multiple Taxon s
 * 
 * @author carl.scott
 * 
 */
public abstract class TaxonChooser extends LayoutContainer {

	public static final int HEADER_HEIGHT = 65;
	public static final int PANEL_HEIGHT = 400;
	public static final int PANEL_WIDTH = 590;

	private DataList selected;
	private Taxon  currentNode;

	public TaxonChooser() {
		super();
	}

	public void addItem(String[] footPrint, Taxon  node) {
		if (validate(footPrint, node)) {
			DataListItem li = new DataListItem(node.getFullName());
			selected.add(li);
		}
	}

	public abstract ButtonBar getButtonBar();

	public abstract String getDescription();

	public Menu getListMenu() {
		Menu m = new Menu();
		MenuItem item = new MenuItem();
		item.setText("Remove");
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				selected.remove((DataListItem) ce.getSource());
			}
		});
		m.add(item);
		return m;
	}

	public int getNumberInList() {
		return selected.getItemCount();
	}

	public LayoutContainer getRightSide() {
		BorderLayout layout = new BorderLayout();

		LayoutContainer container = new LayoutContainer();
		container.setLayout(layout);

		container.add(getButtonBar(), new BorderLayoutData(LayoutRegion.SOUTH, .1F));

		selected = new DataList();
		selected.setContextMenu(getListMenu());

		LayoutContainer right = new LayoutContainer();
		right.setLayout(new FillLayout());
		right.add(selected);

		container.add(selected, new BorderLayoutData(LayoutRegion.CENTER, .8F));
		container.setHeight(PANEL_HEIGHT - 20);
		return container;
	}

	public final TaxonomyBrowserPanel getTaxonomyBrowserPanel() {
		return new TaxomaticTaxonomyBrowserPanel(this);
	}

	public void load() {

		currentNode = TaxonomyCache.impl.getCurrentTaxon();

		BorderLayout layout = new BorderLayout();
		// layout.setMargin(5);
		// layout.setSpacing(5);

		LayoutContainer full = new LayoutContainer();
		full.setLayout(layout);
		full.setLayoutOnChange(true);

		TaxonomyBrowserPanel tp = getTaxonomyBrowserPanel();

		if (currentNode != null) {
			tp.update(currentNode.getId() + "");
		} else {
			tp.update();
		}

		int size = PANEL_WIDTH / 2;

		LayoutContainer left = new LayoutContainer();
		left.setLayout(new FillLayout());
		left.setSize(size, PANEL_HEIGHT);
		left.add(tp);

		full.add(new HTML("<b> Instructions:</b> " + getDescription()), new BorderLayoutData(LayoutRegion.NORTH,
				HEADER_HEIGHT));
		full.add(left, new BorderLayoutData(LayoutRegion.WEST, size));
		full.add(getRightSide(), new BorderLayoutData(LayoutRegion.CENTER, size));

		full.setSize(PANEL_WIDTH, PANEL_HEIGHT);

		add(full);
	}

	public void onClose() {
		BaseEvent be = new BaseEvent(this);
		be.setCancelled(false);
		fireEvent(Events.Close, be);
	}

	public abstract void onSubmit();

	public abstract void removeItem(String id);

	public abstract boolean validate(String[] footPrint, Taxon  node);

}
