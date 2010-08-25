package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.utils.XMLUtils;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SISStructureCollection extends Structure {

	public static final int TREE = 0;
	public static final int VERTICAL_PANEL = 1;
	public static final int HORIZONTAL_PANEL = 2;
	public static final int FLEXTABLE = 3;

	private ArrayList<Structure> structures;
	private int displayType = 1;

	public SISStructureCollection(String structure, String description, String structID, Object structures) {
		this(structure, description, structID, structures, 1);
	}

	public SISStructureCollection(String structure, String description, String structID, Object structures, int displayType) {
		super(structure, description, structID, structures);

		// displayPanel = new VerticalPanel();
		this.displayType = displayType;
		// HashMap initValues = new HashMap();
		// for (int i = 0; i < this.structures.size(); i++) {
		// initValues.putAll(((Structure)this.structures.get(i)).getValues());
		// }
		// tracker.setInitValues(initValues);
	}

	@Override
	public void save(Field field) {
		for( Structure cur : structures )
			cur.save(field);
	}

	@Override
	public boolean hasChanged() {
		for( Structure cur : structures )
			if( cur.hasChanged() )
				return true;

		return false;
	}

	@Override
	public void clearData() {
		for (int i = 0; i < structures.size(); i++)
			((Structure) structures.get(i)).clearData();
	}

	@Override
	public Widget createLabel() {
		return createLabel(false);
	}

	private Widget createLabel(boolean viewOnly) {
		// SysDebugger.getInstance().println("Display type = " + displayType);
		// if (this.displayType == 0) {
		// //TODO: display a tree
		// SysDebugger.getInstance().println("I want a tree");
		// return null;
		// }
		if (displayType == 1) {
			// SysDebugger.getInstance().println("Showing a vertical panel");
			// displayPanel = new VerticalPanel();
			buildContentPanel(Orientation.VERTICAL);
			((VerticalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
			// displayPanel.add(descriptionLabel);
			for (int i = 0; i < structures.size(); i++) {
				if (viewOnly)
					displayPanel.add(((Structure) structures.get(i)).generateViewOnly());
				else
					displayPanel.add(((Structure) structures.get(i)).generate());
			}
			return displayPanel;
		}
		// else if (displayType == 2) {
		else {
			// displayPanel = new HorizontalPanel();
			buildContentPanel(Orientation.HORIZONTAL);
			((HorizontalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
			// displayPanel.add(descriptionLabel);
			for (int i = 0; i < structures.size(); i++) {
				if (viewOnly)
					displayPanel.add(((Structure) structures.get(i)).generateViewOnly());
				else
					displayPanel.add(((Structure) structures.get(i)).generate());
			}
			return displayPanel;
		}
		// else if (displayType == 3) {
		// displayPanel = new FlexTable();
		// int insert = 0;
		// if (descriptionLabel != null)
		// ((FlexTable)displayPanel).setWidget(0, insert++, descriptionLabel);
		// for (int i = 0; i < structures.size(); i++) {
		// if (viewOnly)
		// ((FlexTable)displayPanel).setWidget(0, insert++,
		// ((Structure)structures.get(i)).generateViewOnly());
		// else
		// ((FlexTable)displayPanel).setWidget(0, insert++,
		// ((Structure)structures.get(i)).generate());
		// }
		// return displayPanel;
		// }
		// else
		// return null;
	}

	@Override
	public Widget createViewOnlyLabel() {
		return createLabel(true);
	}

	@Override
	public void createWidget() {
		structures = (ArrayList) data;
		descriptionLabel = new HTML(description);
		
		/*
		 * try { for (int i = 0; i < structures.size(); i++) {
		 * ((Structure)structures.get(i)).createWidget(); } } catch (Exception
		 * e) { SysDebugger.getInstance().println("Hmm.." + e.getMessage());
		 * e.printStackTrace(); }
		 */
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();

		for (Iterator iter = structures.iterator(); iter.hasNext();)
			ret.addAll(((Structure) iter.next()).extractDescriptions());

		return ret;
	}
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		ArrayList<ClassificationInfo> list = new ArrayList<ClassificationInfo>();
		
		for (Structure structure : structures)
			list.addAll(structure.getClassificationInfo());
		
		return list;
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
		for (int i = 0; i < structures.size(); i++)
			offset = ((Structure) structures.get(i)).getDisplayableData(rawData, prettyData, offset);

		return offset;
	}

	public Structure getStructureAt(int index) {
		return (Structure) structures.get(index);
	}

	public ArrayList getStructures() {
		return structures;
	}
	
	@Override
	public void setData(Field field) {
		for (int i = 0; i < structures.size(); i++) {
			((Structure) structures.get(i)).setData(field);
			/*for(String key: ((Structure) structures.get(i)).extractModelData().getPropertyNames())
				model.set(key, ((Structure) structures.get(i)).extractModelData().get(key));*/
		}
	}

	public void setDisplayType(int displayType) {
		this.displayType = displayType;
		for (int i = 0; i < structures.size(); i++) {
			if (getStructureAt(i).getStructureType().equalsIgnoreCase(XMLUtils.STRUCTURE_COLLECTION))
				((SISStructureCollection) getStructureAt(i)).setDisplayType(displayType);
		}
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		for (int i = 0; i < structures.size(); i++) {
			((Structure) structures.get(i)).setEnabled(isEnabled);
		}
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}

	
}
