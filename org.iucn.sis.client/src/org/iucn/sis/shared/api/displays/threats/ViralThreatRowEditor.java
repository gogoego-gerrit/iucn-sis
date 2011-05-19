package org.iucn.sis.shared.api.displays.threats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.caches.VirusCache;
import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Virus;
import org.iucn.sis.shared.api.models.fields.ViralThreatsSubfield;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeModelData;
import org.iucn.sis.shared.api.structures.Structure;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;

@SuppressWarnings("deprecation")
public class ViralThreatRowEditor extends GroupedThreatRowEditor {
	
	public ViralThreatRowEditor(final Collection<ClassificationSchemeModelData> models, final TreeData treeData, final TreeDataRow groupBy, boolean isViewOnly) {
		super(models, treeData, groupBy, isViewOnly);
	}
	
	@Override
	protected void init(SimpleListener listener) {
		final ArrayList<Integer> initViruses = new ArrayList<Integer>();
		for (ClassificationSchemeModelData model : models) {
			ViralThreatsSubfield field = new ViralThreatsSubfield(model.getField());
			Integer value = field.getVirus();
			if (value != null)
				initViruses.add(value);
		}
		
		fetchList(initViruses, listener);
	}
	
	@Override
	protected DataListItem createDataListItem(ClassificationSchemeModelData model) {
		ViralThreatsSubfield field = new ViralThreatsSubfield(model.getField());
		Integer virusID = field.getVirus();
		
		DataListItem item = new DataListItem();
		if (virusID == null)
			item.setText("(No virus selected)");
		else {
			Virus virus = VirusCache.impl.getFromCache(virusID);
			if (virus != null)
				item.setText(virus.getName());
			else
				return null;
		}
		item.setData("virus", virusID);
		
		return item;
	}
	
	@Override
	protected ButtonBar createButtonBar() {
		final ButtonBar bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.CENTER);
		bar.add(new Button("Add Virus", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final VirusChooser chooser = new VirusChooser();
				
				final Window window = WindowUtils.newWindow("Virus Chooser");
				window.setSize(600, 600);
				window.setLayout(new FitLayout());
				window.addButton(new Button("Add Selected", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						window.hide();
						
						final Map<Integer, Virus> map = new HashMap<Integer, Virus>();
						for (Virus virus : chooser.getSelection())
							map.put(virus.getId(), virus);
						
						for (DataListItem item : list.getItems()) {
							Integer value = item.getData("virus");
							map.remove(value);
						}
						
						for (Map.Entry<Integer, Virus> entry : map.entrySet()) {
							Field field = new Field("ThreatsSubfield", null);
							ViralThreatsSubfield proxy = new ViralThreatsSubfield(field);
							proxy.setVirus(entry.getKey());
							
							ClassificationSchemeModelData model = 
								new ThreatClassificationSchemeModelData(generateDefaultStructure(), field);
							model.setSelectedRow(groupBy);
							
							DataListItem item = new DataListItem();
							item.setText(entry.getValue().getName());
							item.setData("virus", entry.getKey());
							item.setData("value", model);
							
							list.add(item);
							
							if (addListener != null)
								addListener.handleEvent(model);
						}
						
					}
				}));
				window.addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						window.hide();
					}
				}));
				
				chooser.draw(new DoneDrawingCallback() {
					public void isDrawn() {
						window.add(chooser);
						window.show();
					}
				});
			}
		}));
		bar.add(new Button("Remove Virus", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (list.getSelectedItem() == null)
					return;
				
				WindowUtils.confirmAlert("Confirm", "Are you sure you want to remove this row?", new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						removeSelectedModel();
					}
				});
			}
		}));
		
		return bar;
	}
	
	@SuppressWarnings("unchecked")
	private Structure generateDefaultStructure() {
		ThreatsTreeData treeData = (ThreatsTreeData) this.treeData;
		
		BasicThreatViewer structure = new BasicThreatViewer(treeData);
		structure.setIsVisible(treeData.getIsVisible());
		structure.setName(treeData.getName());
		
		return structure;
	}
	
	private void fetchList(ArrayList<Integer> list, final SimpleListener callback) {
		if (list.isEmpty())
			callback.handleEvent();
		else {
			VirusCache.impl.get(list, new ComplexListener<List<Virus>>() {
				public void handleEvent(List<Virus> eventData) {
					callback.handleEvent();
				}
			});
		}
	}

}
