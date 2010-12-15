package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.displays.ClassificationScheme;
import org.iucn.sis.shared.api.models.Field;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.Widget;

public class SISClassificationSchemeStructure extends Structure<Field> {
	private ClassificationScheme scheme;

	public SISClassificationSchemeStructure(String struct, String descript, String structID, Object data) {
		super(struct, descript, structID, data);
		buildContentPanel(Orientation.VERTICAL);
	}
	
	@Override
	public void save(Field parent, Field field) {
		/*if (field == null) {
			field = new Field();
			field.setName(getId());
			field.setParent(parent);
		}
			
		for (Entry<String, DisplayStructure> cur : scheme.getSelected().entrySet() ) {
			Field subfield = new Field(field.getName() + "Subfield", field.getAssessment());
			subfield.setParent(field);
			
			cur.getValue().save(field, subfield);
			
			field.getFields().add(subfield);
		}*/
	}
	
	@Override
	public void clearData() {
		//scheme.setSelectedAsStructures(new HashMap<String, DisplayStructure>());
	}

	@Override
	protected Widget createLabel() {
		/*clearDisplayPanel();
		displayPanel.setSize("60%", "100%");
		Widget p = scheme.getProtectedWidgetContent(false);
		int h = scheme.recalculateHeightAfterSingleAdd();
		if( h > -1 )
			p.setHeight(h+"px");
		else
			p.setHeight( ((scheme.getSelected().size() * 25) + 20)+"px");
		scheme.getGrid().setWidth(500);
		p.setWidth("500px");
		displayPanel.add(p);*/
		return displayPanel;
	}
	
	@Override
	protected Widget createViewOnlyLabel() {
		clearDisplayPanel();
		displayPanel.add(scheme.showViewOnly());
		return displayPanel;
	}

	@Override
	public void createWidget() {
		scheme = new ClassificationScheme((TreeData) data);
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();
		ret.add("No. of " + scheme.getDescription());
		return ret;
	}
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		return new ArrayList<ClassificationInfo>();
	}

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
		/*int num = ((String) rawData.get(offset)).matches("\\d") ? Integer.parseInt((String) rawData.get(offset)) : 0;
		prettyData.add(offset, num+"");

		offset++;

		for (int i = 0; i < num; i++) {
			String cur = (String) rawData.get(offset);
			if (scheme.getCodeToDesc().containsKey(cur))
				prettyData.add(offset, scheme.getCodeToDesc().get(cur)+"");
			else
				prettyData.add(offset, cur);

			offset++;
		}*/

		return offset;
	}

	public ClassificationScheme getScheme() {
		return scheme;
	}

	@Override
	public void setData(Field field) {
		//final Map<String, PrimitiveField> data = field.getKeyToPrimitiveFields();
		/*
		Map<String, DisplayStructure> selected = new HashMap<String, DisplayStructure>();
		//for (Entry<String, PrimitiveField> entry : data.entrySet()) {
			//String id = entry.getKey();
			// DUPLICATE WORK BEING DONE IN SET SELECTED...
			//Modify this to generate structure FOR THE GIVEN KEY so class schemes can has multiple structs
			Structure struct = scheme.generateDefaultStructure(); 
			//struct.setData(data);
			struct.setData(field);

			selected.put(id, struct);
		//}

		scheme.setSelectedAsStructures(selected);*/
	}

	@Override
	public void setEnabled(boolean isEnabled) {
	}
	
	@Override
	public boolean hasChanged(Field field) {
		// TODO Auto-generated method stub
		return true;
	}

	/*@Override
	public String toXML() {
		String ret = "<!-- This tag is for the classification scheme, noting how many selections it has -->\r\n";
		ret += "<structure>" + scheme.getSelected().size() + "</structure>\r\n";

		for (Iterator iter = scheme.getSelected().keySet().iterator(); iter.hasNext();) {
			String curKey = (String) iter.next();

			ret += "<structure>" + curKey + "</structure>\r\n";
			ret += ((Structure) scheme.getSelected().get(curKey)).toXML();
		}

		return ret;
	}*/
	


}
