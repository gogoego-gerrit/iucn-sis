package com.solertium.util.querybuilder.gwt.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;
import com.solertium.util.gwt.ui.StyledHTML;
import com.solertium.util.gwt.ui.WindowAlertMessage;
import com.solertium.util.querybuilder.gwt.client.chooser.TableChooser;
import com.solertium.util.querybuilder.gwt.client.chooser.TableChooserSaveListener;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.gwt.client.utils.QBButtonIcon;
import com.solertium.util.querybuilder.query.QBComparisonConstraint;
import com.solertium.util.querybuilder.query.QueryConstants;
import com.solertium.util.querybuilder.struct.DBStructure;
import com.solertium.util.querybuilder.struct.QBLookupTable;
import com.solertium.util.querybuilder.utils.SQLDateTimeFormat;

public class QBComparisonConstraintEditor extends Window {

	protected FlexTable table;
	protected final QBComparisonConstraint comparison;
	protected final DBStructure db;
	protected final GWTQBQuery query;

	protected ListBox box;

	protected TextBox input;
	protected ListBox inputBox;
	protected DateField inputDate;

	protected static final int INPUT_TYPE_FREE_TEXT = 1;
	protected static final int INPUT_TYPE_LOOKUP_VALUE = 2;
	protected static final int INPUT_TYPE_ASK_USER = 3;
	protected static final int INPUT_TYPE_DATE = 4;
	protected static final int INPUT_TYPE_NULL = 5;

	protected int inputType;

	public QBComparisonConstraintEditor(final GWTQBQuery query, final QBComparisonConstraint comparison) {
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

		input = new TextBox();
		inputBox = new ListBox();
		inputDate = new DateField();
	}
	
	protected void setInitInputType() {
		if (comparison.ask != null && comparison.ask.booleanValue())
			inputType = INPUT_TYPE_ASK_USER;
		else if (comparison.compareValue == null && comparison.getField() != null)
			inputType = INPUT_TYPE_NULL;
		else
			inputType = INPUT_TYPE_FREE_TEXT;
	}

	public void draw() {
		VerticalPanel panel = new VerticalPanel();

		table = new FlexTable();
		table.setWidget(0, 0, new StyledHTML("Set Field: ", "fontSize80"));
		table.setWidget(1, 0, new StyledHTML("Set Comparison Type: ", "fontSize80"));
		table.setWidget(2, 0, new StyledHTML("Set Value: ", "fontSize80"));

		setField(comparison.getField());

		table.setWidget(1, 1, getComparisonTypes(comparison.getComparisonType()));

		setInitInputType();

		getAppropriateValueSettingTool();

		table.setWidget(2, 2, new ChooserIcon());

		panel.add(table);
		
		renderButtonBar();
		
		add(panel);
		show();
	}

	public void getAppropriateValueSettingTool() {
		if (inputType == INPUT_TYPE_ASK_USER)
			table.setWidget(2, 1, new StyledHTML("The user will be prompted for a value.", "fontSize60"));
		else if (inputType == INPUT_TYPE_LOOKUP_VALUE) {
			QBLookupTable lut = db.getLookupTable(comparison.getField());
			lut.getLookupValues(new GenericCallback<NativeDocument>() {
				public void onSuccess(NativeDocument result) {
					inputBox.setSelectedIndex(0);
					RowParser parser = new RowParser();
					parser.parseAmbiguousSearchRows(result);
					Iterator<RowData> it = parser.iterator();
					while (it.hasNext()) {
						RowData cur = it.next();
						inputBox.addItem(cur.getField(RowData.AMBIG_NAME), cur.getField(RowData.AMBIG_VALUE));
						if (comparison.getComparisonValue() != null && comparison.getComparisonValue().equals(cur.getField(RowData.AMBIG_VALUE)))
							inputBox.setSelectedIndex(inputBox.getItemCount()-1);
					}
					table.setWidget(2, 1, inputBox);
				}
				public void onFailure(Throwable caught) {
					new WindowAlertMessage(WindowAlertMessage.IMAGE_DIALOG_ERROR,
						"Could not find look up table, please enter " +
						"the appropriate value by hand.", "OK").show();
					inputType = INPUT_TYPE_FREE_TEXT;
					getAppropriateValueSettingTool();
				}
			});
		}
		else if (inputType == INPUT_TYPE_DATE) {
			Date value;
			
			Object init = comparison.getComparisonValue();
			if (init == null)
				value = new Date();
			else if (init instanceof Date)
				value = (Date)init;
			else {
				try {
					value = SQLDateTimeFormat.getInstance().parse(comparison.getComparisonValue().toString());
				} catch (IllegalArgumentException e) {
					value = new Date();
				} catch (NullPointerException e) {
					value = new Date();
				} catch (Exception e) {
					value = new Date();
				}
			}
			
			inputDate.setValue(value);
			table.setWidget(2, 1, inputDate);
		}
		else if (inputType == INPUT_TYPE_NULL)
			table.setWidget(2, 1, new StyledHTML("null", "fontSize60"));
		else {
			if (comparison.compareValue != null)
				input.setText(comparison.getComparisonValue().toString());
			table.setWidget(2, 1, input);
		}
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
		if (inputType == INPUT_TYPE_FREE_TEXT) {
			comparison.setComparisonValue(input.getText());
			comparison.ask = null;
		}
		else if (inputType == INPUT_TYPE_ASK_USER) {
			comparison.setComparisonValue("");
			comparison.ask = Boolean.TRUE;
		}
		else if (inputType == INPUT_TYPE_LOOKUP_VALUE) {
			comparison.setComparisonValue(inputBox.getValue(inputBox.getSelectedIndex()));
			comparison.ask = null;
		}
		else if (inputType == INPUT_TYPE_DATE) {
			comparison.setComparisonValue(inputDate.getValue());
			comparison.ask = null;
		}
		else if (inputType == INPUT_TYPE_NULL) {
			comparison.setComparisonValue(null);
			comparison.ask = null;
		}
		hide();
	}

	public void onClose() {
		hide();
	}

	private ListBox getComparisonTypes(String selected) {
		box = new ListBox();
		box.addItem("equals (=)", QueryConstants.CT_EQUALS);
		box.addItem("is greater than (>)", QueryConstants.CT_GT);
		box.addItem("is less than (<)", QueryConstants.CT_LT);
		box.addItem("contains", QueryConstants.CT_CONTAINS);
		box.addItem("starts with", QueryConstants.CT_STARTS_WITH);
		box.addItem("ends with", QueryConstants.CT_ENDS_WITH);
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

	private void setField(String canonicalField) {
		comparison.setField(canonicalField);

		VerticalPanel inner = new VerticalPanel();
		String field = comparison.getField();
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
						setField(selectedTable + "." + selectedColumns.get(0));
					}
				});
				chooser.draw();
			}
		});
		button.addStyleName("button");
		inner.add(button);

		table.setWidget(0, 1, inner);
	}
	
	public Menu getMyContextMenu() {
		return new InternalContextMenu();
	}

	public class ChooserIcon extends Image {

		public ChooserIcon() {
			super("images/small/accessories-text-editor.png");
			addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					openContextMenu();
				}
			});
		}

		/*public void onBrowserEvent(Event evt) {
			switch(DOM.eventGetType(evt)) {
				case Event.ONCLICK: {
					openContextMenu(DOM.eventGetClientX(evt), DOM.eventGetClientY(evt));
					break;
				}
				default:
					super.onBrowserEvent(evt);
			}
		}*/

		private void openContextMenu() {
			final InternalContextMenu contextMenu = new InternalContextMenu();
			contextMenu.show(this);
		}

	}

	public class InternalContextMenu extends Menu {

		public InternalContextMenu() {
			super();
			buildMenu();
		}
		
		protected void buildMenu() {
			final MenuItem text = new MenuItem("Text Entry", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					swap(INPUT_TYPE_FREE_TEXT);
				}
			});
			
			final MenuItem date = new MenuItem("Date Entry", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					swap(INPUT_TYPE_DATE);
				}
			});
			
			final MenuItem nil = new MenuItem("Null Value", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					swap(INPUT_TYPE_NULL);
				}
			});

			final MenuItem lut = new MenuItem("Lookup Table", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					swap(INPUT_TYPE_LOOKUP_VALUE);
				}
			});

			final MenuItem ask = new MenuItem("Ask User", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					swap(INPUT_TYPE_ASK_USER);
				}
			});

			add(text);
			//TODO: check field type?
			add(date);
			add(nil);
			if (comparison.getField() != null && db.hasLookupTable(comparison.getField()))
				add(lut);
			add(ask);
		}
		
		protected void swap(int type) {
			inputType = type;
			getAppropriateValueSettingTool();
		}

		public void closeOnClick() {
			getParent().removeFromParent();
		}
	}

}
