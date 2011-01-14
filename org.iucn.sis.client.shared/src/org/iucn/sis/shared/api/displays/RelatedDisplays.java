package org.iucn.sis.shared.api.displays;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.structures.DisplayStructure;
import org.iucn.sis.shared.api.structures.DominantStructure;
import org.iucn.sis.shared.api.structures.Rule;
import org.iucn.sis.shared.api.structures.SISStructureCollection;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

@Deprecated
/**
 * This class is not used and should not be used, as it does not function. However, the concept
 * of related displays in the UI is a good one, so it has been left for reference and perhaps
 * an eventual port.
 */
@SuppressWarnings("unchecked")
public class RelatedDisplays extends Display {

	// Display storage
	private Display dominantDisplay;
	private List<Display> dependantDisplays;

	// Rules
	private ArrayList<Rule> rules;

	// Display Row
	private Integer dominantDisplayRow;
	private ArrayList dependantDisplaysRows;
	private String displayType = null;
	private String dependantDisplayType = null;

	/**
	 * Creates infastructure for dominant display, awaiting a dominant display
	 */
	public RelatedDisplays() {
		this(null);
	}

	/**
	 * Creates a dominantDisplay given a Display the param display MUST set its
	 * dominantStructureIndex to be a structure that extends DominantStructure
	 * 
	 * @param display
	 *            the display you want to be dominant
	 */
	public RelatedDisplays(Display display) {
		super();
		dominantDisplay = display;
		dependantDisplays = new ArrayList();
		dependantDisplaysRows = new ArrayList();
		rules = new ArrayList<Rule>();

		if (dominantDisplay != null)
			setToListenForActive();
	}

	@Override
	public void save() {
		//TODO: Save.
	}

	@Override
	public boolean hasChanged() {
		for (DisplayStructure struct : dominantDisplay.getStructures())
			if (struct.hasChanged(field))
				return true;
		
		for (Display curDep : dependantDisplays)
			for (DisplayStructure struct : curDep.getStructures())
				if (struct.hasChanged(field))
					return true;
		
		return false;
	}

	public void addDependantDisplay(Display displayToAdd) {
		addDependantDisplay(displayToAdd, -1);
	}

	public void addDependantDisplay(Display displayToAdd, int row) {
		dependantDisplays.add(displayToAdd);
		if (row >= 0)
			dependantDisplaysRows.add(new Integer(row));
		else
			dependantDisplaysRows.add(null);
	}

	public void addRule(Rule rule) {
		rules.add(rule);
	}

	@Override
	public Widget generateContent(boolean viewOnly) {
		try {
			if (displayType == null)
				return showDefault(viewOnly);
			else {
				if (displayType.equalsIgnoreCase(VERTICAL))
					return showVertical(viewOnly);
				else if (displayType.equalsIgnoreCase(HORIZONTAL))
					return showHorizontal(viewOnly);
				else if (displayType.equalsIgnoreCase(TABLE))
					return showTable(viewOnly);
				else
					return showDefault(viewOnly);
			}
		} catch (Exception e) {
			return new SimplePanel();
		}
	}
	
	@Override
	public void removeStructures() {
		/*
		 * Since we re-draw each time, no need for this implementation
		 */
	}

	public List<Display> getDependantDisplays() {
		return dependantDisplays;
	}

	// DISPLAY TYPE

	public Display getDominantDisplay() {
		return dominantDisplay;
	}

	@Override
	public ArrayList getMyWidgets() {
		ArrayList retWidgets = new ArrayList();
		retWidgets.addAll(dominantDisplay.getMyWidgets());
		for (int i = 0; i < dependantDisplays.size(); i++)
			retWidgets.addAll((dependantDisplays.get(i)).getMyWidgets());
		return retWidgets;
	}

	private void processRule(String reaction) {
		if (reaction.equalsIgnoreCase(Rule.ENABLE)) {
			for (int i = 0; i < dependantDisplays.size(); i++)
				((Display) dependantDisplays.get(i)).enableStructures();
		} else if (reaction.equalsIgnoreCase(Rule.SHOW))
			for (int i = 0; i < dependantDisplays.size(); i++)
				((Display) dependantDisplays.get(i)).showStructures();
		else if (reaction.equalsIgnoreCase(Rule.DISABLE))
			for (int i = 0; i < dependantDisplays.size(); i++)
				((Display) dependantDisplays.get(i)).disableStructures();
		else if (reaction.equalsIgnoreCase(Rule.HIDE))
			for (int i = 0; i < dependantDisplays.size(); i++)
				((Display) dependantDisplays.get(i)).hideStructures();
	}

	private void runRuleCheck() {
		for (int i = 0; i < rules.size(); i++) {
			try {
				if (((DominantStructure) dominantDisplay.getStructures().get(
						dominantDisplay.getDominantStructureIndex())).isActive((Rule) rules.get(i))) {
					updateDependantDisplays(((Rule) rules.get(i)).getOnTrue(), true);
				} else {
					updateDependantDisplays(((Rule) rules.get(i)).getOnFalse(), false);
				}
			} catch (ClassCastException e) {
				try {
					if (((DominantStructure) ((SISStructureCollection) dominantDisplay.getStructures().get(0))
							.getStructureAt(dominantDisplay.getDominantStructureIndex())).isActive((Rule) rules.get(i))) {
						updateDependantDisplays(((Rule) rules.get(i)).getOnTrue(), true);
					} else {
						updateDependantDisplays(((Rule) rules.get(i)).getOnFalse(), false);
					}
				} catch (Exception f) {
				}
			}
		}
	}
	
	@Override
	public void setField(Field field) {
		//TODO: Implement
	}

	public void setDependantDisplay(ArrayList dependantDisplays) {
		this.dependantDisplays = dependantDisplays;
	}

	// DEPENDANT DISPLAY FUNCTIONS

	public void setDependantDisplayType(String depDisplayType) {
		this.dependantDisplayType = depDisplayType;
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

	public void setDominantDisplay(Display display) {
		this.dominantDisplay = display;
		setToListenForActive();
	}

	public void setDominantRow(int row) {
		this.dominantDisplayRow = new Integer(row);
	}

	// DOMINANT DISPLAY FUNCTIONALITY

	public void setRules(ArrayList rules) {
		this.rules = rules;
	}

	/**
	 * Adds a click listener to the dominant display's DominantStructure
	 */
	private void setToListenForActive() {
		ClickHandler listener = new ClickHandler() {
			public void onClick(ClickEvent event) {
				runRuleCheck();
			}
		};
		ChangeHandler clistener = new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				runRuleCheck();
			}
		};
		KeyUpHandler kListener = new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				runRuleCheck();
			}
		};

		try {
			((DominantStructure) dominantDisplay.getStructures().get(dominantDisplay.getDominantStructureIndex()))
					.addListenerToActiveStructure(clistener, listener, kListener);
		} catch (ClassCastException e) {
			try {
				((DominantStructure) ((SISStructureCollection) dominantDisplay.getStructures().get(0))
						.getStructureAt(dominantDisplay.getDominantStructureIndex())).addListenerToActiveStructure(
						clistener, listener, kListener);
			} catch (Exception f) {
			}
		}
	}

	// RULES

	private Widget showDefault(boolean viewOnly) {
		VerticalPanel fullPanel = new VerticalPanel();

		HorizontalPanel domPanel = new HorizontalPanel();
		if (viewOnly)
			domPanel.add(dominantDisplay.showViewOnly());
		else
			domPanel.add(dominantDisplay.showDisplay());
		domPanel.setWidth("100%");

		FlexTable contentTable = new FlexTable();
		if (dependantDisplayType == null)
			dependantDisplayType = HORIZONTAL;
		double horizontal_width = 100 / dependantDisplays.size();
		double vertical_width = 100;
		int row = 0;
		int column = 0;
		for (int i = 0; i < dependantDisplays.size(); i++) {
			if (viewOnly)
				contentTable.setWidget(row, column, ((Display) dependantDisplays.get(i)).showViewOnly());
			else
				contentTable.setWidget(row, column, ((Display) dependantDisplays.get(i)).showDisplay());

			contentTable.getCellFormatter().setVerticalAlignment(row, column, HasVerticalAlignment.ALIGN_TOP);

			if (dependantDisplayType.equalsIgnoreCase(HORIZONTAL)) {
				contentTable.getCellFormatter().setWidth(row, column, horizontal_width + "%");
				column++;
			} else if (dependantDisplayType.equalsIgnoreCase(VERTICAL)) {
				contentTable.getCellFormatter().setWidth(row, column, vertical_width + "%");
				row++;
			} else if (dependantDisplayType.equalsIgnoreCase(TABLE)) {
				if (dependantDisplaysRows.get(i) == null) {
					column++;
				} else {
					row = ((Integer) dependantDisplaysRows.get(i)).intValue();
					column = 0;
				}
			}
		}

		fullPanel.add(domPanel);
		fullPanel.add(contentTable);

		runRuleCheck();

		return fullPanel;
	}

	private Widget showHorizontal(boolean viewOnly) {
		FlexTable fullPanel = new FlexTable();
		int numDisplays = dependantDisplays.size() + 1;
		double width = 100 / numDisplays;
		int insert = 0;

		if (viewOnly)
			fullPanel.setWidget(0, insert, dominantDisplay.showViewOnly());
		else
			fullPanel.setWidget(0, insert, dominantDisplay.showDisplay());

		fullPanel.getCellFormatter().setVerticalAlignment(0, insert, HasVerticalAlignment.ALIGN_TOP);
		fullPanel.getCellFormatter().setWidth(0, insert, width + "%");

		for (int i = 0; i < dependantDisplays.size(); i++) {
			insert++;
			if (viewOnly)
				fullPanel.setWidget(0, insert, ((Display) dependantDisplays.get(i)).showViewOnly());
			else
				fullPanel.setWidget(0, insert, ((Display) dependantDisplays.get(i)).showDisplay());

			fullPanel.getCellFormatter().setVerticalAlignment(0, insert, HasVerticalAlignment.ALIGN_TOP);
			fullPanel.getCellFormatter().setWidth(0, insert, width + "%");
		}

		runRuleCheck();

		return fullPanel;
	}

	private Widget showTable(boolean viewOnly) {
		VerticalPanel fullPanel = new VerticalPanel();
		fullPanel.setWidth("100%");

		FlexTable rowPanel = new FlexTable();
		rowPanel.setCellPadding(2);
		rowPanel.setCellSpacing(2);
		rowPanel.setWidth("100%");

		int row = 0;
		int column = 0;

		if (dominantDisplayRow != null)
			row = dominantDisplayRow.intValue();

		SimplePanel domInnerContent = new SimplePanel();
		domInnerContent.setWidth("100%");

		if (viewOnly)
			domInnerContent.setWidget(dominantDisplay.showViewOnly());
		else
			domInnerContent.setWidget(dominantDisplay.showDisplay());

		rowPanel.setWidget(row, column, domInnerContent);
		// rowPanel.getCellFormatter().addStyleName(row, column,
		// "SISPage_Field");
		rowPanel.getCellFormatter().setVerticalAlignment(row, column, HasVerticalAlignment.ALIGN_TOP);

		for (int i = 0; i < dependantDisplays.size(); i++) {
			if (dependantDisplaysRows.get(i) == null) {
				column++;
			}
			// NO ROW SPECIFIED
			else {
				if (((Integer) dependantDisplaysRows.get(i)).intValue() == row) {
					column++;
				}
				// new row
				else {
					double width = 100 / ++column;
					for (int j = 0; j <= column; j++)
						rowPanel.getCellFormatter().setWidth(row, j, width + "%");

					FlexTable tableToAdd = rowPanel;
					fullPanel.add(tableToAdd);

					rowPanel = new FlexTable();
					rowPanel.setCellPadding(2);
					rowPanel.setCellSpacing(2);
					rowPanel.setWidth("100%");

					column = 0;
					row = 0;
				}
				// row = ((Integer)dependantDisplaysRows.get(i)).intValue();
			}

			/*
			 * CS You can give this a style, but if it has padding it will more
			 * than likely break the UI
			 */
			SimplePanel depInnerContent = new SimplePanel();
			depInnerContent.setWidth("100%");

			if (viewOnly)
				depInnerContent.setWidget(((Display) dependantDisplays.get(i)).showViewOnly());
			else
				depInnerContent.setWidget(((Display) dependantDisplays.get(i)).showDisplay());

			rowPanel.setWidget(row, column, depInnerContent);
			rowPanel.getCellFormatter().setVerticalAlignment(row, column, HasVerticalAlignment.ALIGN_TOP);
			// rowPanel.getCellFormatter().addStyleName(row, column,
			// "SISPage_Field");
		}

		// All done, now set the width for the last row...
		double width = 100 / ++column;

		for (int k = 0; k < column; k++)
			rowPanel.getCellFormatter().setWidth(row, k, width + "%");

		fullPanel.add(rowPanel);

		runRuleCheck();

		return fullPanel;
	}

	private Widget showVertical(boolean viewOnly) {
		VerticalPanel fullPanel = new VerticalPanel();

		SimplePanel domPanel = new SimplePanel();
		domPanel.setWidth("100%");
		if (viewOnly)
			domPanel.setWidget(dominantDisplay.showViewOnly());
		else
			domPanel.setWidget(dominantDisplay.showDisplay());

		VerticalPanel contentPanel = new VerticalPanel();
		contentPanel.setWidth("100%");
		contentPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		for (int i = 0; i < dependantDisplays.size(); i++) {
			if (viewOnly)
				contentPanel.add(((Display) dependantDisplays.get(i)).showViewOnly());
			else
				contentPanel.add(((Display) dependantDisplays.get(i)).showDisplay());
		}

		fullPanel.add(domPanel);
		fullPanel.add(contentPanel);

		runRuleCheck();

		return fullPanel;
	}

	/**
	 * Returns the string representation of a field.
	 * 
	 * @return the field, string form
	 */
	@Override
	public String toString() {
		return "< " + dominantDisplay.displayID + ", " + "relateddisplay" + ", " + description + " >\r\n";
	}

	/*@Override
	public String toThinXML() {
		String xmlRetString = "";
		xmlRetString += dominantDisplay.toThinXML() + "\n";
		for (int i = 0; i < dependantDisplays.size(); i++)
			xmlRetString += ((Display) dependantDisplays.get(i)).toThinXML() + "\n";
		return xmlRetString;
	}

	@Override
	public String toXML() {
		String xmlRetString = "";
		xmlRetString += dominantDisplay.toXML() + "\n";
		for (int i = 0; i < dependantDisplays.size(); i++)
			xmlRetString += ((Display) dependantDisplays.get(i)).toXML() + "\n";
		return xmlRetString;
	}*/

	/**
	 * Change the dependant displays based on the dominant one's result and the
	 * stored reactions
	 * 
	 * @param reaction
	 *            the reaction
	 * @param result
	 *            the result
	 */
	private void updateDependantDisplays(String reaction, boolean result) {
		/*
		 * if (result) { if (reaction.equalsIgnoreCase(Rule.ENABLE)) for (int i
		 * = 0; i < dependantDisplays.size(); i++)
		 * ((Display)dependantDisplays.get(i)).enableStructures(); else if
		 * (reaction.equalsIgnoreCase(Rule.SHOW)) for (int i = 0; i <
		 * dependantDisplays.size(); i++)
		 * ((Display)dependantDisplays.get(i)).showStructures(); } else { if
		 * (reaction.equalsIgnoreCase(Rule.DISABLE)) for (int i = 0; i <
		 * dependantDisplays.size(); i++)
		 * ((Display)dependantDisplays.get(i)).disableStructures(); else if
		 * (reaction.equalsIgnoreCase(Rule.HIDE)) for (int i = 0; i <
		 * dependantDisplays.size(); i++)
		 * ((Display)dependantDisplays.get(i)).hideStructures(); }
		 */
		processRule(reaction);
	}
}
