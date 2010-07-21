package org.iucn.sis.client.displays;

import java.util.ArrayList;

import org.iucn.sis.shared.structures.DominantStructure;
import org.iucn.sis.shared.structures.Rule;
import org.iucn.sis.shared.structures.SISStructureCollection;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;

public class RelatedDisplays extends Display {

	// Display storage
	private Display dominantDisplay;
	private ArrayList dependantDisplays;

	// Rules
	private ArrayList rules;

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
		rules = new ArrayList();

		if (dominantDisplay != null)
			setToListenForActive();
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
				if (displayType.equalsIgnoreCase(SISPageHolder.VERTICAL))
					return showVertical(viewOnly);
				else if (displayType.equalsIgnoreCase(SISPageHolder.HORIZONTAL))
					return showHorizontal(viewOnly);
				else if (displayType.equalsIgnoreCase(SISPageHolder.TABLE))
					return showTable(viewOnly);
				else
					return showDefault(viewOnly);
			}
		} catch (Exception e) {
			return new SimplePanel();
		}
	}

	public ArrayList getDependantDisplays() {
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
			retWidgets.addAll(((Display) dependantDisplays.get(i)).getMyWidgets());
		SysDebugger.getInstance().println(
				"Returned " + myStructures.size() + " widgets to show for " + this.description);
		return retWidgets;
	}

	private void processRule(String reaction) {
		SysDebugger.getInstance().println("The reaction: " + reaction);
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
		ClickListener listener = new ClickListener() {
			public void onClick(Widget sender) {
				runRuleCheck();
			}
		};
		ChangeListener clistener = new ChangeListener() {
			public void onChange(Widget sender) {
				runRuleCheck();
			}
		};
		KeyboardListenerAdapter kListener = new KeyboardListenerAdapter() {
			@Override
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
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
			dependantDisplayType = SISPageHolder.HORIZONTAL;
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

			if (dependantDisplayType.equalsIgnoreCase(SISPageHolder.HORIZONTAL)) {
				contentTable.getCellFormatter().setWidth(row, column, horizontal_width + "%");
				column++;
			} else if (dependantDisplayType.equalsIgnoreCase(SISPageHolder.VERTICAL)) {
				contentTable.getCellFormatter().setWidth(row, column, vertical_width + "%");
				row++;
			} else if (dependantDisplayType.equalsIgnoreCase(SISPageHolder.TABLE)) {
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

	@Override
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
	}

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
		SysDebugger.getInstance().println("Updated related displays");
		processRule(reaction);
	}
}
