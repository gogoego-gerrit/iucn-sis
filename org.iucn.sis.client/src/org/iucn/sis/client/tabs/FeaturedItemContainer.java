package org.iucn.sis.client.tabs;

import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.util.gwt.ui.DrawsLazily;

public abstract class FeaturedItemContainer<T> extends PageContainer implements DrawsLazily {
	
	protected final LayoutContainer featuredItemContainer;
	protected final LayoutContainer optionsContainer;
	protected final LayoutContainer bodyContainer;
	
	private List<T> items;
	private T selected;
	
	public FeaturedItemContainer() {
		super(new FillLayout());
		
		selected = null;
		
		featuredItemContainer = new LayoutContainer(new FitLayout());
		optionsContainer = new LayoutContainer(new FlowLayout());
		bodyContainer = new LayoutContainer(new FillLayout());
		bodyContainer.setLayoutOnChange(true);
		
		final LayoutContainer left = new LayoutContainer(new BorderLayout()); {
			left.add(featuredItemContainer, new BorderLayoutData(LayoutRegion.NORTH, 200, 200, 200));
			left.add(optionsContainer, new BorderLayoutData(LayoutRegion.CENTER));
		}
		
		final BorderLayoutData leftData = new BorderLayoutData(LayoutRegion.WEST);
		leftData.setSplit(true);
		leftData.setMaxSize(300);
		leftData.setMinSize(200);
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(left, leftData);
		container.add(bodyContainer, new BorderLayoutData(LayoutRegion.CENTER));
		
		add(container);
	}
	
	public void setItems(List<T> items) {
		this.items = items;
	}
	
	public void setSelectedItem(T item) {
		this.selected = item;
		draw(new DoneDrawingCallback() {
			public void isDrawn() {
				layout();
			}
		});
	}
	
	public T getSelectedItem() {
		return selected;
	}
	
	public List<T> getAllItems() {
		return items;
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
		
		prev.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				int index = items.indexOf(selected);
				updateSelection(items.get(index - 1));
			}
		});
		next.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				int index = items.indexOf(selected);
				updateSelection(items.get(index + 1));
			}
		});
		
		bar.add(prev);
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
	
	protected abstract void drawBody(DrawsLazily.DoneDrawingCallback callback);

	protected abstract LayoutContainer updateFeature();
	
	protected abstract void updateSelection(T selection);
	
}
