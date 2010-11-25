package org.iucn.sis.shared.api.schemes;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.gwt.ui.DrawsLazily;

public class ClassificationSchemeRowEditor extends LayoutContainer implements DrawsLazily {
	
	protected final boolean isViewOnly;
	
	private ComplexListener<ClassificationSchemeModelData> saveListener;
	private SimpleListener cancelListener;
	
	private boolean hideAfterOperation;
	
	public ClassificationSchemeRowEditor(boolean isViewOnly) {
		super(new FillLayout());
		this.isViewOnly = isViewOnly;
		this.hideAfterOperation = true;
	}
	
	public void setHideAfterOperation(boolean hideAfterOperation) {
		this.hideAfterOperation = hideAfterOperation;
	}
	
	public boolean isHideAfterOperation() {
		return hideAfterOperation;
	}
	
	public void setModel(ClassificationSchemeModelData model) {
		super.setModel(model);
	}
	
	public void setSaveListener(ComplexListener<ClassificationSchemeModelData> saveListener) {
		this.saveListener = saveListener;
	}
	
	public void setCancelListener(SimpleListener cancelListener) {
		this.cancelListener = cancelListener;
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		final ClassificationSchemeModelData model = getModel();
		
		add(createContainer(model));
		
		callback.isDrawn();
	}
	
	protected LayoutContainer createContainer(ClassificationSchemeModelData model) {
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(createWidgetContainer(model, isViewOnly), new BorderLayoutData(LayoutRegion.CENTER));
		container.add(createWidgetContainerToolbar(model), new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		
		return container;
	}
	
	protected LayoutContainer createWidgetContainer(ClassificationSchemeModelData model, boolean isViewOnly) {
		final LayoutContainer widgetContainer = new LayoutContainer(new FillLayout());
		widgetContainer.setScrollMode(Scroll.AUTO);
		widgetContainer.add(model.getDetailsWidget(isViewOnly));
		
		return widgetContainer;
	}
	
	protected ToolBar createWidgetContainerToolbar(final ClassificationSchemeModelData model) {
		final ToolBar buttonPanel = new ToolBar();
		buttonPanel.add(new Button("Save Selection", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				//									if(!((Boolean)box.getSelection().get(0).get("enabled")).booleanValue() || (!init.equals(box.getSelection().get(0).get("key")) && selected.containsKey(box.getSelection().get(0).get("key")))){
				/*if( (box.getValue(box.getSelectedIndex()).equals("") || (!init.equals(box.getValue(box.getSelectedIndex())) && selected.containsKey(box.getValue(box.getSelectedIndex())) ))){
					WindowUtils.errorAlert("This is not a selectable option. Please try again.");
					return;
				}*/
				
				saveSelection(model);
			}
		}));
		buttonPanel.add(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				cancel(model);
			}
		}));
		
		return buttonPanel;
	}
	
	protected void saveSelection(ClassificationSchemeModelData model) {
		if (saveListener != null)
			saveListener.handleEvent(model);
	}
	
	protected void cancel(ClassificationSchemeModelData model) {
		if (cancelListener != null)
			cancelListener.handleEvent();
	}

}
