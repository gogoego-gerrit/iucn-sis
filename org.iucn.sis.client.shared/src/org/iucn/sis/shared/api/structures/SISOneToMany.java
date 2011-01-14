package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.data.DisplayData;
import org.iucn.sis.shared.api.data.DisplayDataProcessor;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * This structure allows the user to create many instances of a single structure
 * inside of itself, allowing for a one-to-many "collection".
 * 
 * @author adam.schwartz
 */
@SuppressWarnings("unchecked")
public class SISOneToMany extends Structure<Field> {

	/**
	 * ArrayList<Structure>
	 */
	private ArrayList<StructureHolder> selected;

	private DisplayData defaultStructureData;

	private Button addNew;

	private VerticalPanel selectedPanel;

	public SISOneToMany(String struct, String descript, String structID, DisplayData defaultStructure) {
		super(struct, descript, structID);
		buildContentPanel(Orientation.VERTICAL);

		selected = new ArrayList<StructureHolder>();
		defaultStructureData = defaultStructure;
	}
	
	@Override
	public boolean hasChanged(Field field) {
		final Map<Integer, Field> savedFields = new HashMap<Integer, Field>();
		if (field != null)
			for (Field subfield : field.getFields())
				savedFields.put(subfield.getId(), subfield);
		
		for (StructureHolder holder : selected) {
			//Field has never been saved, if there are changes, let's save.
			if (holder.field == null || holder.field.getId() == 0) {
				if (holder.structure.hasChanged(holder.field)) {
					Debug.println("NEW: {0} says it has changed", holder.structure.getClass().getName());
					return true;
				}
				else
					return false;
			}
			else {
				//We are working with previously saved data that could have changed.
				Field dataField = savedFields.remove(holder.field.getId());
				if (dataField == null) { //How??
					Debug.println("OneToMany hasChanged badness, save to clean up.");
					return true;
				}
				
				if (holder.structure.hasChanged(dataField)) {
					Debug.println("OLD: {0} says it has changed", holder.structure.getClass().getName());
					return true;
				}
			}
		}
		
		return !savedFields.isEmpty();
	}
	
	@Override
	public void save(Field parent, Field field) {
		if (field == null) {
			field = new Field();
			field.setName(getId());
			field.setParent(parent);
			parent.getFields().add(field);
		}
		
		final List<StructureHolder> unsaved = new ArrayList<StructureHolder>();
		
		for (StructureHolder cur : selected) {
			if (cur.field == null)
				unsaved.add(cur);
			else
				cur.structure.save(field, cur.field);
		}
		
		for (StructureHolder cur : unsaved) {
			Field subfield = new Field(field.getName() + "Subfield", field.getAssessment());
			subfield.setParent(field);
			
			cur.structure.save(field, subfield);
			
			field.addField(subfield);
			
			cur.field = subfield;
		}
	}

	@Override
	public void clearData() {
		selected.clear();
	}

	@Override
	protected Widget createLabel() {
		clearDisplayPanel();
		selectedPanel.clear();

		for (final StructureHolder curStruct : selected) {
			HorizontalPanel structWrapper = new HorizontalPanel();

			Button remove = new Button();
			remove.setIconStyle("icon-remove");
			remove.setSize("18px", "18px");
			remove.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					WindowUtils.confirmAlert("Confirm", "Are you sure you want to delete this data?",
							new Listener<MessageBoxEvent>() {
								public void handleEvent(MessageBoxEvent be) {
									if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
										selected.remove(curStruct);
										createLabel();
									}
								}
							});
				}
			});

			structWrapper.add(remove);
			structWrapper.add(curStruct.structure.createLabel());

			selectedPanel.add(structWrapper);
		}

		selectedPanel.add(addNew);

		displayPanel.add(descriptionLabel);
		displayPanel.add(selectedPanel);
		return displayPanel;
	}

	@Override
	protected Widget createViewOnlyLabel() {
		clearDisplayPanel();
		selectedPanel.clear();
		((CellPanel) displayPanel).setSpacing(2);

		if (selected.size() == 0)
			selectedPanel.add(new HTML("No information available."));
		else
			for (Iterator<StructureHolder> iter = selected.listIterator(); iter.hasNext();)
				selectedPanel.add(iter.next().structure.createViewOnlyLabel());

		displayPanel.add(descriptionLabel);
		displayPanel.add(selectedPanel);
		return displayPanel;
	}

	@Override
	public void createWidget() {
		selectedPanel = new VerticalPanel();

		descriptionLabel = new HTML(description);
		descriptionLabel.addStyleName("bold");

		addNew = new Button("Add");
		addNew.setIconStyle("icon-add");
		addNew.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				Structure newOne = DisplayDataProcessor.processDisplayStructure(defaultStructureData);
				selected.add(new StructureHolder(newOne));

				createLabel();
			}
		});
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList<String> extractDescriptions() {
		Structure structureData = 
			DisplayDataProcessor.processDisplayStructure(defaultStructureData);
		
		ArrayList<String> ret = new ArrayList<String>();
		ret.addAll(structureData.extractDescriptions());
		return ret;
	}
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		return new ArrayList<ClassificationInfo>();
	}

	/**
	 * This structure returns null
	 */
	@Override
	public String getData() {
		return null;
	}

	public DisplayData getDefaultStructureData() {
		return defaultStructureData;
	}
	
	/**
	 * Pass in the raw data from an Assessment object, and this will return
	 * it in happy, displayable String form
	 * 
	 * @return ArrayList of Strings, having converted the rawData to nicely
	 *         displayable String data. Happy days!
	 */
	@Override
	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset) {
		int num = ((String) rawData.get(offset)).matches("\\d") ? Integer.parseInt((String) rawData.get(offset)) : 0;
		prettyData.add(offset, num + " selected.");

		offset++;

		Structure def = 
			DisplayDataProcessor.processDisplayStructure(defaultStructureData);

		for (int i = 0; i < num; i++)
			offset = def.getDisplayableData(rawData, prettyData, offset);

		return offset;
	}
	
	@Override
	public void setData(Field field) {
		selected.clear();
		
		if (field != null)
			for (Field subField : field.getFields())
				selected.add(new StructureHolder(DisplayDataProcessor.processDisplayStructure(defaultStructureData), subField));
	}
	
	public void setEnabled(boolean isEnabled) {
		for (Iterator<StructureHolder> iter = selected.listIterator(); iter.hasNext();)
			iter.next().structure.setEnabled(isEnabled);
	}
	
	public static class StructureHolder {
		
		private Structure<Object> structure;
		private Field field;
		
		public StructureHolder(Structure<Object> structure) {
			this(structure, null);
		}
		
		public StructureHolder(Structure<Object> structure, Field field) {
			this.structure = structure;
			this.field = field;
			
			this.structure.setData(field);
		}
		
	}
	
}