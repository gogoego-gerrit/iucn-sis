package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.data.DisplayDataProcessor;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.structures.SISOneToManyWindow.StructureHolder;
import org.iucn.sis.shared.api.views.components.DisplayData;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This structure allows the user to create many instances of a single structure
 * inside of itself, allowing for a one-to-many "collection".
 * 
 * @author adam.schwartz
 */
@SuppressWarnings("unchecked")
public class SISOneToMany extends Structure<Field> {
	
	private final List<Integer> displayFields;

	private SISOneToManyWindow oneToMany;
	private SimplePanel records;
	private HTML recordCount;

	public SISOneToMany(String struct, String descript, String structID, Map<String, Object> data) {
		super(struct, descript, structID);
		
		buildContentPanel(Orientation.VERTICAL);
		
		DisplayData defaultStructure = (DisplayData) data.get("data");
		String columnCSV = (String) data.get("columns");
		
		oneToMany = new SISOneToManyWindow(descript, defaultStructure);
		oneToMany.addWindowListener(new WindowListener() {
			public void windowHide(WindowEvent we) {
				updateRecordCount();
				
				updateRecords();
			}
		});
		
		displayFields = new ArrayList<Integer>();
		if (columnCSV != null) {
			for (String value : columnCSV.split(",")) {
				try {
					displayFields.add(Integer.valueOf(value.trim()));
				} catch (Exception e) { }
			}
		}
	}
	
	@Override
	protected void buildContentPanel(Orientation style) {
		super.buildContentPanel(style);
		
		displayPanel.addStyleName("SIS_oneToMany_sideBar");
		((VerticalPanel)displayPanel).setSpacing(10);
	}
	
	@Override
	public boolean hasChanged(Field field) {
		final Map<Integer, Field> savedFields = new HashMap<Integer, Field>();
		if (field != null)
			for (Field subfield : field.getFields())
				savedFields.put(subfield.getId(), subfield);
		
		for (StructureHolder holder : oneToMany.getSelected()) {
			//Field has never been saved, if there are changes, let's save.
			if (holder.getField() == null || holder.getField().getId() == 0) {
				if (holder.hasChanged()) {
					Debug.println("NEW: {0} says it has changed", holder.getStructure().getClass().getName());
					return true;
				}
				else
					return false;
			}
			else {
				//We are working with previously saved data that could have changed.
				Field dataField = savedFields.remove(holder.getField().getId());
				if (dataField == null) { //How??
					Debug.println("OneToMany hasChanged badness, save to clean up.");
					return true;
				}
				
				if (holder.getStructure().hasChanged(dataField)) {
					Debug.println("OLD: {0} says it has changed", holder.getStructure().getClass().getName());
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
			if (parent != null) {
				field.setParent(parent);
				parent.getFields().add(field);
			}
		}
		
		final List<StructureHolder> unsaved = new ArrayList<StructureHolder>();
		final List<Integer> saved = new ArrayList<Integer>();
		saved.add(0);
		for (StructureHolder cur : oneToMany.getSelected()) {
			if (cur.getField() == null)
				unsaved.add(cur);
			else {
				cur.getStructure().save(field, cur.getField());
				saved.add(cur.getField().getId());
			}
		}
		
		for (StructureHolder cur : unsaved) {
			Field subfield = new Field(field.getName() + "Subfield", null);
			subfield.setParent(field);
			
			cur.getStructure().save(field, subfield);
			
			field.getFields().add(subfield);
			
			cur.setField(subfield);
		}
				
		final HashSet<Field> savedSet = new HashSet<Field>();
		for (Field subfield : field.getFields())
			if (saved.contains(subfield.getId()))
				savedSet.add(subfield);
		
		field.setFields(savedSet);
	}

	@Override
	public void clearData() {
		oneToMany.clear();
	}

	@Override
	protected Widget createLabel() {
		clearDisplayPanel();
		
		updateRecordCount();
		
		updateRecords();

		displayPanel.add(descriptionLabel);
		displayPanel.add(recordCount);
		displayPanel.add(records);
		displayPanel.add(new Button("View/Edit", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				oneToMany.createLabel(false);
			}
		}));
		return displayPanel;
	}

	@Override
	protected Widget createViewOnlyLabel() {
		clearDisplayPanel();
		
		updateRecordCount();
		
		updateRecords();
		
		displayPanel.add(descriptionLabel);
		displayPanel.add(recordCount);
		displayPanel.add(records);
		displayPanel.add(new Button("View", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				oneToMany.createLabel(false);
			}
		}));
		return displayPanel;
	}
	
	private void updateRecords() {
		records.clear();
		
		if (displayFields.isEmpty()) {
			records.setWidget(new HTML(""));
			return;
		}
		
		List<StructureHolder> holders = oneToMany.getSelected();
		if (holders.isEmpty()) {
			records.setWidget(new HTML(""));
			return;
		}
		
		List<String> descs = holders.get(0).
			getStructure().extractDescriptions();
		
		Grid grid = new Grid(holders.size() + 1, displayFields.size());
		grid.setCellSpacing(0);
		grid.setCellPadding(8);
		grid.addStyleName("page_assessment_classScheme_grid");
		
		int row = 0;
		int column = 0;
		for (Integer value : displayFields) {
			try {
				grid.setHTML(row, column, "<span class=\"page_assessment_classScheme_grid_th\">" + descs.get(value) + "</span>");
			} catch (IndexOutOfBoundsException e) {
				grid.setHTML(row, column, "<span class=\"page_assessment_classScheme_grid_th\">-</span>");
			} finally {
				column++;
			}
		}
		
		row++;
		
		for (StructureHolder holder : holders) {
			final ArrayList<String> raw = new ArrayList<String>(), 
				pretty = new ArrayList<String>();
			
			final Structure<Object> structure = holder.getStructure();
			
			final Map<String, String> map = new LinkedHashMap<String, String>();
		
			for (Object obj : structure.getClassificationInfo()) {
				ClassificationInfo info = (ClassificationInfo)obj;
				map.put(info.getDescription(), info.getData());
				raw.add(info.getData());
			}
			
			try {
				structure.getDisplayableData(raw, pretty, 0);
			} catch (Exception e) {
				continue;
			}
			
			column = 0;
			for (Integer value : displayFields) {
				try {
					grid.setHTML(row, column, "<span class=\"page_assessment_classScheme_content\">" + pretty.get(value) + "</span>");
				} catch (IndexOutOfBoundsException e) {
					grid.setHTML(row, column, "<span class=\"page_assessment_classScheme_content\">-</span>");
				} finally {
					column++;
				}
			}
			
			row++;
		}
		
		for (int i = 0; i < grid.getColumnCount(); i++)
			grid.getColumnFormatter().setWidth(i, "150px");
		
		records.setWidget(grid);
	}
	
	private void updateRecordCount() {
		int size = oneToMany.getSelected().size();
		
		String description = this.description;
		if (!(description == null || "".equals(description)))
			description = " " + description;
		
		recordCount.setHTML(size == 0 ? "No" + description + " records." : 
			size == 1 ? "One" + description + " record." : 
			size + description + " records");
		
	}

	@Override
	public void createWidget() {
		descriptionLabel = new HTML(description);
		descriptionLabel.addStyleName("SIS_oneToManyDescription");
		
		recordCount = new HTML();
		
		records = new SimplePanel();
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList<String> extractDescriptions() {
		Structure structureData = 
			DisplayDataProcessor.processDisplayStructure(oneToMany.getDisplayData());
		
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
			DisplayDataProcessor.processDisplayStructure(oneToMany.getDisplayData());

		for (int i = 0; i < num; i++)
			offset = def.getDisplayableData(rawData, prettyData, offset);

		return offset;
	}
	
	@Override
	public void setData(Field field) {
		oneToMany.setData(field);
	}
	
	public void setEnabled(boolean isEnabled) {
		oneToMany.setEnabled(isEnabled);
	}
	
	
	
}