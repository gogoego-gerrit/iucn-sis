package org.iucn.sis.shared.api.displays.threats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeModelData;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeRowEditor;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.DataListEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;

public abstract class GroupedThreatRowEditor extends ClassificationSchemeRowEditor {
	
	protected final Collection<ClassificationSchemeModelData> models;
	protected final LayoutContainer displayContainer;
	protected final DataList list;
	
	protected final TreeData treeData;
	protected final TreeDataRow groupBy;
	
	protected ComplexListener<ClassificationSchemeModelData> removeListener;
	protected ComplexListener<ClassificationSchemeModelData> addListener;

	public GroupedThreatRowEditor(final Collection<ClassificationSchemeModelData> models, final TreeData treeData, final TreeDataRow groupBy, boolean isViewOnly) {
		super(isViewOnly);
		this.models = models;
		this.displayContainer = new LayoutContainer(new FillLayout());
		this.list = new DataList();
		this.treeData = treeData;
		this.groupBy = groupBy;
		
		displayContainer.setLayoutOnChange(true);
		
		list.addListener(Events.SelectionChange, new Listener<DataListEvent>() {
			public void handleEvent(DataListEvent be) {
				if (be.getSelected().isEmpty())
					return;
				
				DataListItem item = be.getSelected().get(0);
				ClassificationSchemeModelData model = item.getData("value");
				
				displayEditor(model);
				
				fireSelectionChangedEvent(new SelectionChangedEvent<ClassificationSchemeModelData>(null, model));
			}
		});
	}
	
	public List<ClassificationSchemeModelData> getModelsFromList() {
		List<ClassificationSchemeModelData> models = new ArrayList<ClassificationSchemeModelData>();
		for (DataListItem item : list.getItems()) {
			ClassificationSchemeModelData model = item.getData("value");
			if (model != null)
				models.add(model);
		}
		return models;
			
	}
	
	public void setRemoveListener(ComplexListener<ClassificationSchemeModelData> removeListener) {
		this.removeListener = removeListener;
	}
	
	public void setAddListener(ComplexListener<ClassificationSchemeModelData> addListener) {
		this.addListener = addListener;
	}
	
	protected abstract void init(SimpleListener listener);
	
	protected abstract DataListItem createDataListItem(ClassificationSchemeModelData model);
	
	protected abstract ButtonBar createButtonBar();
	
	@Override
	public final void draw(final DoneDrawingCallback callback) {
		init(new SimpleListener() {
			public void handleEvent() {
				renderContainer();
				callback.isDrawn();
			}
		});
	}
	
	private void displayEditor(ClassificationSchemeModelData model) {
		displayContainer.removeAll();
		displayContainer.add(createContainer(model));
	}
	
	private void renderContainer() {
		for (ClassificationSchemeModelData model : models) {
			DataListItem item = createDataListItem(model);
			if (item != null) {
				item.setData("value", model);
				list.add(item);
				
				if (model.equals(getModel()))
					list.setSelectedItem(item);
			}
		}
		
		final LayoutContainer left = new LayoutContainer(new BorderLayout());
		left.add(list, new BorderLayoutData(LayoutRegion.CENTER));
		left.add(createButtonBar(), new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(left, new BorderLayoutData(LayoutRegion.WEST, 200));
		container.add(displayContainer, new BorderLayoutData(LayoutRegion.CENTER));
		
		add(container);
	}

	protected void removeSelectedModel() {
		DataListItem item = list.getSelectedItem();
		if (item == null)
			return;
		
		list.remove(item);
		
		displayContainer.removeAll();
		
		if (removeListener != null) {
			ClassificationSchemeModelData model = item.getData("value");
			removeListener.handleEvent(model);
		}
		
		fireSelectionChangedEvent();
	}
	
	protected void fireSelectionChangedEvent() {
		SelectionChangedEvent<ClassificationSchemeModelData> fauxEvent = 
			new SelectionChangedEvent<ClassificationSchemeModelData>(null, (ClassificationSchemeModelData)null);
		fauxEvent.getSelection().clear();
		
		fireSelectionChangedEvent(fauxEvent);
	}
	
	protected void fireSelectionChangedEvent(SelectionChangedEvent<ClassificationSchemeModelData> event) {
		fireEvent(Events.SelectionChange, event);
	}
}
