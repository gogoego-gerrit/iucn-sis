package org.iucn.sis.shared.api.schemes;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.util.gwt.ui.DrawsLazily;

public class ClassificationSchemeRowEditor extends LayoutContainer implements DrawsLazily {
	
	protected final boolean isViewOnly;
	
	public ClassificationSchemeRowEditor(boolean isViewOnly) {
		super(new FillLayout());
		this.isViewOnly = isViewOnly;
	}
	
	public void setModel(ClassificationSchemeModelData model) {
		super.setModel(model);
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		final ClassificationSchemeModelData model = getModel();
		
		add(createWidgetContainer(model, isViewOnly));
		
		callback.isDrawn();
	}
	
	protected LayoutContainer createContainer(ClassificationSchemeModelData model) {
		return createWidgetContainer(model, isViewOnly);
	}
	
	protected LayoutContainer createWidgetContainer(ClassificationSchemeModelData model, boolean isViewOnly) {
		final LayoutContainer widgetContainer = new LayoutContainer(new FillLayout());
		widgetContainer.setScrollMode(Scroll.AUTO);
		widgetContainer.add(model.getDetailsWidget(isViewOnly));
		
		return widgetContainer;
	}
	

}
