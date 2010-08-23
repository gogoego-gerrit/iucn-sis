package org.iucn.sis.shared.api.integrity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.gwt.ui.HTMLMultipleListBox;
import com.solertium.util.gwt.ui.StyledHTML;
import com.solertium.util.querybuilder.gwt.client.chooser.FriendlyNameTableChooser;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.gwt.client.utils.QBButtonIcon;
import com.solertium.util.querybuilder.struct.QBTable;

/**
 * SISTableChooser.java
 * 
 * This table chooser extension has the ability to hide previously selected
 * tables, and it also disallows column selection. It uses only a subset of the
 * tables available DBStructure, and always uses "asm_id" as the column in
 * question, just for compatibility with the QBQuery API.
 * 
 * Additionally, the table chooser allows for selection of multiple tables.
 * 
 * TODO: verify that all the tables made available should be there. May need
 * more (or less).
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public class SISTableChooser extends FriendlyNameTableChooser {

	private HTMLMultipleListBox tableChooser;
	private Collection<String> previouslySelectedTables;

	public SISTableChooser(GWTQBQuery query, boolean isMultipleSelect) {
		super(query, isMultipleSelect);
		setWidth(270);
		previouslySelectedTables = new ArrayList<String>();
	}

	public void setPreviouslySelectedTables(
			Collection<String> previouslySelectedTables) {
		this.previouslySelectedTables = previouslySelectedTables;
	}

	public void draw() {
		tableChooser = new HTMLMultipleListBox("250px") {
			public void onChange(String selectedValue) {
				// save.setEnabled(true);
				// loadColumns(db.getTable(selectedValue));
			}

			public void onChecked(String checkedValue, boolean isChecked) {
				save.setEnabled(!tableChooser.getCheckedValues().isEmpty());
			}
		};
		tableChooser.setHeight("200px");
		tableChooser.setTooltipSchedule(500);

		populateTableListing();

		table.setWidget(0, 0, new StyledHTML("Choose Table", "CIPD_Header"));
		table.setWidget(1, 0, tableChooser);
		table.getCellFormatter().setVerticalAlignment(0, 0,
				HasVerticalAlignment.ALIGN_TOP);

		addButton(save = new QBButtonIcon("Save", "images/document-save.png") {
			public void onClick(Widget sender) {
				onSave();
			}
		});
		addButton(new QBButtonIcon("Close", "images/process-stop.png") {
			public void onClick(Widget sender) {
				onClose();
			}
		});
// setAlignment(HorizontalAlignment.CENTER);

		save.setEnabled(false);

		add(table);

		show();
	}

	protected void populateTableListing() {
		final ArrayList<String> fields = new ArrayList<String>(tables);
		/*fields.add("assessment");
		fields.add("assessment_reference");
		fields.add("ConservationActions");
		fields.add("CountryOccurrence");
		fields.add("FAOOccurrence");
		fields.add("GeneralHabitats");
		fields.add("EcosystemServices");
		fields.add("InPlaceEducation");
		fields.add("InPlaceLandWaterProtection");
		fields.add("InPlaceSpeciesManagement");
		fields.add("LargeMarineEcosystems");
		fields.add("LandCover");
		fields.add("RedListCriteria");
		fields.add("RedListCriteria_3_1");
		fields.add("RedListCriteria_2_3");
		fields.add("RedListReasonsForChange");
		fields.add("RegionInformation");
		fields.add("Research");
		fields.add("Stresses");
		fields.add("Threats");*/

		for (String table : previouslySelectedTables)
			fields.remove(table);

		tableChooser.init(fields.size());

		Collections.sort(fields, new CaseInsensitiveAlphanumericComparator());
		for (int i = 0; i < fields.size(); i++) {
			QBTable cur = db.getTable(fields.get(i));
			if (!previouslySelectedTables.contains(cur.getTableName())) {
				if (cur.hasDescription())
					tableChooser.addItem(cur.getFriendlyName(), cur
							.getTableName(), cur.getDescription());
				else
					tableChooser.addItem(cur.getFriendlyName(), cur
							.getTableName());
			}
		}
	}

	public void onSave() {
		for (int i = 0; i < saveListeners.size(); i++) {
			for (String table : tableChooser.getCheckedValues()) {
				final ArrayList<String> list = new ArrayList<String>();
				if ("assessment".equals(table))
					list.add("uid");
				else
					list.add("asm_id");
				(saveListeners.get(i)).onSave(table, list);
			}
		}
		hide();
	}

}
