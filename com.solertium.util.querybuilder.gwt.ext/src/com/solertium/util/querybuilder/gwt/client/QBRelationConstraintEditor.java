package com.solertium.util.querybuilder.gwt.client;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.gwt.ui.StyledHTML;
import com.solertium.util.querybuilder.gwt.client.chooser.TableChooser;
import com.solertium.util.querybuilder.gwt.client.chooser.TableChooserSaveListener;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.gwt.client.utils.QBButtonIcon;
import com.solertium.util.querybuilder.query.QBRelationConstraint;
import com.solertium.util.querybuilder.query.QueryConstants;
import com.solertium.util.querybuilder.struct.DBStructure;

public class QBRelationConstraintEditor extends Window {

	private FlexTable table;
	private QBRelationConstraint comparison;
	private DBStructure db;
	private GWTQBQuery query;
	
	private ListBox box;
	
	
	public QBRelationConstraintEditor(final GWTQBQuery query, final QBRelationConstraint comparison) {
		super();
		setModal(true);
		setClosable(true);
		setResizable(false);
		setSize(425, 200);
		setHeading("Edit Comparison");
		setButtonAlign(HorizontalAlignment.CENTER);
		this.db = DBStructure.getInstance();
		this.query = query;
		
		addStyleName("CIPD_PopupPanel");
		this.comparison = comparison;
	}
	
	public void draw() {
		VerticalPanel panel = new VerticalPanel();
		
		table = new FlexTable();
		table.setWidget(0, 0, new StyledHTML("Set Field 1: ", "fontSize80"));
		table.setWidget(1, 0, new StyledHTML("Set Comparison Type: ", "fontSize80"));
		table.setWidget(2, 0, new StyledHTML("Set Field 2: ", "fontSize80"));
		
		setLeftField(comparison.getLeftField());
		
		table.setWidget(1, 1, getComparisonTypes(Integer.toString(comparison.getComparisonType())));
		
		setRightField(comparison.getRightField());
		
		panel.add(table);
		
		renderButtonBar();
		
		add(panel);
		
		show();
	}
	
	private void setLeftField(String canonicalField) {
		comparison.setLeftField(canonicalField);

		VerticalPanel inner = new VerticalPanel();
		String field = comparison.getLeftField();
		if (field != null)
			inner.add(new StyledHTML(field, "fontSize80"));
		Button button = new Button("Set...", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				TableChooser chooser = TableChooser.getInstance(query, false);
				chooser.setAvailableTables(
					query.getTables()
				);
				chooser.addSaveListener(new TableChooserSaveListener() {
					public void onSave(String selectedTable, ArrayList<String> selectedColumns) {
						setLeftField(selectedTable + "." + selectedColumns.get(0));
					}
				});
				chooser.draw();
			}
		});
		button.addStyleName("button");
		inner.add(button);

		table.setWidget(0, 1, inner);
	}
	
	private ListBox getComparisonTypes(String selected) {
		box = new ListBox();
		box.addItem("equals (=)", QueryConstants.CT_EQUALS);
		box.addItem("is greater than (>)", QueryConstants.CT_GT);
		box.addItem("is less than (<)", QueryConstants.CT_LT);
		box.addItem("not", QueryConstants.CT_NOT);
		box.setSelectedIndex(0);

		if (selected != null) {
			for (int i = 0; i < box.getItemCount(); i++) {
				if (box.getValue(i).equals(selected)) {
					box.setSelectedIndex(i);
					break;
				}
			}
		}

		return box;
	}
	
	private void setRightField(String canonicalField) {
		comparison.setRightField(canonicalField);

		VerticalPanel inner = new VerticalPanel();
		String field = comparison.getRightField();
		if (field != null)
			inner.add(new StyledHTML(field, "fontSize80"));
		Button button = new Button("Set...", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				TableChooser chooser = TableChooser.getInstance(query, false);
				chooser.setAvailableTables(
					query.getTables()
				);
				chooser.addSaveListener(new TableChooserSaveListener() {
					public void onSave(String selectedTable, ArrayList<String> selectedColumns) {
						setRightField(selectedTable + "." + selectedColumns.get(0));
					}
				});
				chooser.draw();
			}
		});
		button.addStyleName("button");
		inner.add(button);

		table.setWidget(2, 1, inner);
	}
	
	private void renderButtonBar() {
		addButton(new QBButtonIcon("Save", "images/document-save.png") {
			public void onClick(Widget sender) {
				onSave();
			}
		});
		addButton(new QBButtonIcon("Close", "images/process-stop.png") {
			public void onClick(Widget sender) {
				onClose();
			}
		});
	}
	
	public void onSave() {
		comparison.setComparisonType(Integer.parseInt(box.getValue(box.getSelectedIndex())));
		close();
	}
	
	public void onClose() {
		close();
	}
}
