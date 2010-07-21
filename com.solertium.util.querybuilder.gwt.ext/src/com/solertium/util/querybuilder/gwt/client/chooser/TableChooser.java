package com.solertium.util.querybuilder.gwt.client.chooser;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.gwt.ui.HTMLListBox;
import com.solertium.util.gwt.ui.HTMLMultipleListBox;
import com.solertium.util.gwt.ui.StyledHTML;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.gwt.client.utils.QBButtonIcon;
import com.solertium.util.querybuilder.struct.DBStructure;
import com.solertium.util.querybuilder.struct.QBTable;

public abstract class TableChooser extends Window {

	protected DBStructure db;
	protected GWTQBQuery query;

	protected ArrayList<TableChooserSaveListener> saveListeners;

	protected HTMLListBox tableChooser;
	protected Widget columnChooser;

	protected boolean isMultipleSelect;

	protected FlexTable table;
	protected QBButtonIcon save;
	protected ArrayList<String> tables;

	public static TableChooser getInstance(final GWTQBQuery query, final boolean isMultipleSelect) {
		TableChooser chooser = TableChooserCreator.getInstance().getTableChooser(
			DBStructure.getInstance().getChooserType(), query, isMultipleSelect);
		if (chooser == null)
			chooser = new OfficialNameTableChooser(query, isMultipleSelect);
		return chooser;
	}

	protected TableChooser(final GWTQBQuery query, final boolean isMultipleSelect) {
		super();
		setHeading("Table Chooser");
		setModal(true);
		setSize(520, 285);
		addStyleName("CIPD_PopupPanel");

		this.db = DBStructure.getInstance();
		this.query = query;
		this.isMultipleSelect = isMultipleSelect;

		table = new FlexTable();
		tables = db.getTableNames();
		saveListeners = new ArrayList<TableChooserSaveListener>();
	}

	public void setAvailableTables(ArrayList<String> tables) {
		this.tables = tables;
	}

	public void draw() {
		tableChooser = new HTMLListBox("250px", tables.size()) {
			public void onChange(String selectedValue) {
				save.setEnabled(false);
				loadColumns(db.getTable(selectedValue));
			}
		};
		tableChooser.setHeight("200px");
		tableChooser.setTooltipSchedule(500);

		populateTableListing();

		if (isMultipleSelect) {
			columnChooser = new HTMLMultipleListBox("250px") {
				public void onChange(String selectedValue) {

				}
				public void onChecked(String selectedValue, boolean isChecked) {
					save.setEnabled(!((HTMLMultipleListBox)columnChooser).getCheckedValues().isEmpty());
				}
			};
			columnChooser.setHeight("200px");
			((HTMLMultipleListBox)columnChooser).setTooltipSchedule(500);
		}
		else {
			columnChooser = new HTMLListBox("250px", 0) {
				public void onChange(String selectedValue) {
					save.setEnabled(true);
				}
			};
			columnChooser.setHeight("200px");
			((HTMLListBox)columnChooser).setTooltipSchedule(500);
		}

		table.setWidget(0, 0, new StyledHTML("Choose Table", "CIPD_Header"));
		table.setWidget(0, 1, new StyledHTML("Choose Column", "CIPD_Header"));
		table.setWidget(1, 0, tableChooser);
		table.setWidget(1, 1, columnChooser);
		table.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
		table.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);

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
		
		setButtonAlign(HorizontalAlignment.CENTER);

		save.setEnabled(false);


		add(table);
		
		show();
	}

	public void initColumns(int size) {
		if (isMultipleSelect) {
			((HTMLMultipleListBox)columnChooser).clear();
			((HTMLMultipleListBox)columnChooser).init(size);
		}
		else {
			((HTMLListBox)columnChooser).clear();
			((HTMLListBox)columnChooser).init(size);
		}
	}

	protected abstract void populateTableListing();

	protected abstract void loadColumns(QBTable table);

	public ArrayList<String> getSelectedColumns() {
		if (isMultipleSelect)
			return ((HTMLMultipleListBox)columnChooser).getCheckedValues();
		else {
			ArrayList<String> list = new ArrayList<String>();
			list.add((((HTMLListBox)columnChooser).getSelectedIndex() == -1) ?
				null : ((HTMLListBox)columnChooser).getSelectedValue());
			return list;
		}
	}

	public String getSelectedTable() {
		return (tableChooser.getSelectedIndex() == -1) ? null : tableChooser.getSelectedValue();
	}

	public void onSave() {
		for (int i = 0; i < saveListeners.size(); i++)
			(saveListeners.get(i)).
				onSave(getSelectedTable(), getSelectedColumns());
		hide();
	}

	public void onClose() {
		hide();
	}

	public void addSaveListener(TableChooserSaveListener listener) {
		saveListeners.add(listener);
	}
}
