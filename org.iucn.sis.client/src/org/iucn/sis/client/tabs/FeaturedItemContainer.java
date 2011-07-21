package org.iucn.sis.client.tabs;

import java.util.List;

import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.util.gwt.ui.DrawsLazily;

public abstract class FeaturedItemContainer<T> extends PageContainer implements DrawsLazily {
	
	protected final LayoutContainer featuredItemContainer;
	protected final LayoutContainer optionsContainer;
	protected final LayoutContainer bodyContainer;
	
	private List<T> items;
	private T selected;
	
	private String url;
	
	public FeaturedItemContainer() {
		super(new FillLayout());
		addStyleName("gwt-background");
		addStyleName("featured_item_container");
		
		selected = null;
		
		featuredItemContainer = new LayoutContainer(new FillLayout());
		featuredItemContainer.setLayoutOnChange(true);
		optionsContainer = new LayoutContainer(new FillLayout());
		optionsContainer.setLayoutOnChange(true);
		bodyContainer = new LayoutContainer(new FillLayout());
		bodyContainer.setLayoutOnChange(true);
		bodyContainer.addStyleName("gwt-background");
		
		final LayoutContainer left = new LayoutContainer(new BorderLayout()); {
			left.add(featuredItemContainer, new BorderLayoutData(LayoutRegion.NORTH, 200, 200, 200));
			left.add(optionsContainer, new BorderLayoutData(LayoutRegion.CENTER));
		}
		left.addStyleName("gwt-background");
		left.addStyleName("featured_left");
		
		final BorderLayoutData leftData = new BorderLayoutData(LayoutRegion.WEST);
		leftData.setSplit(true);
		leftData.setMaxSize(300);
		leftData.setMinSize(200);
		leftData.setMargins(new Margins(0, 5, 0, 5));
		
		final BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0, 0, 0, 5));
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.addStyleName("gwt-background");
		container.add(left, leftData);
		container.add(bodyContainer, centerData);
		
		add(container);
	}
	
	public void setItems(List<T> items) {
		this.items = items;
	}
	
	public void setSelectedItem(T item) {
		this.selected = item;
	}
	
	public T getSelectedItem() {
		return selected;
	}
	
	public List<T> getAllItems() {
		return items;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	} 
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		drawOptions(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				drawBody(new DrawsLazily.DoneDrawingCallback() {
					public void isDrawn() {
						drawFeatureArea();
						
						callback.isDrawn();
					}
				});
			}
		});
	}
	
	protected abstract void drawOptions(DoneDrawingCallback callback);
	
	protected void drawFeatureArea() {
		final LayoutContainer top = updateFeature();
		final ToolBar bar = new ToolBar();
		
		//TODO: make these icons
		final Button prev = new Button("Previous"), next = new Button("Next");
		
		prev.setIconStyle("icon-previous");
		prev.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				int index = items.indexOf(selected);
				updateSelection(items.get(index - 1));
			}
		});
		next.setIconStyle("icon-next");
		next.setIconAlign(IconAlign.RIGHT);
		next.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				int index = items.indexOf(selected);
				updateSelection(items.get(index + 1));
			}
		});
		
		bar.add(prev);
		bar.add(new FillToolItem());
		bar.add(next);
		
		setButtonState(prev, next, items.indexOf(selected));
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(top, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(bar, new BorderLayoutData(LayoutRegion.SOUTH, 30, 30, 30));
		
		featuredItemContainer.removeAll();
		featuredItemContainer.add(container); 
	}
	
	private void setButtonState(Button prev, Button next, int index) {
		int size = items.size();
		if (size > 0) {
			prev.setEnabled(index > 0);
			next.setEnabled(index + 1 < size);
		}
		else {
			prev.setEnabled(false);
			next.setEnabled(false);
		}
	}
	
	protected HTML createSpacer(int size) {
		HTML spacer = new HTML("&nbsp;");
		spacer.setHeight(size + "px");
		
		return spacer;
	}
	
	protected abstract void drawBody(DrawsLazily.DoneDrawingCallback callback);

	protected abstract LayoutContainer updateFeature();
	
	protected abstract void updateSelection(T selection);
	
}
