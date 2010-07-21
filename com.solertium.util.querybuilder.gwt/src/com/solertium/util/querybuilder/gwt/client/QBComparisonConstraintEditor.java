package com.solertium.util.querybuilder.gwt.client;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;
import com.solertium.util.gwt.ui.CenteredPopupPanel;
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

public class QBComparisonConstraintEditor extends CenteredPopupPanel {

	protected FlexTable table;
	protected QBComparisonConstraint comparison;
	protected DBStructure db;
	protected GWTQBQuery query;

	protected ListBox box;

	protected TextBox input;
	protected ListBox inputBox;

	protected static final int INPUT_TYPE_FREE_TEXT = 1;
	protected static final int INPUT_TYPE_LOOKUP_VALUE = 2;
	protected static final int INPUT_TYPE_ASK_USER = 3;

	protected int inputType;

	public QBComparisonConstraintEditor(final GWTQBQuery query, final QBComparisonConstraint comparison) {
		super(false, true);
		this.db = DBStructure.getInstance();
		this.query = query;

		addStyleName("CIPD_PopupPanel");
		this.comparison = comparison;

		input = new TextBox();
		inputBox = new ListBox();
	}

	public void draw() {
		VerticalPanel panel = new VerticalPanel();
		panel.add(new StyledHTML("Edit Comparison", "CIPD_Header"));

		table = new FlexTable();
		table.setWidget(0, 0, new StyledHTML("Set Field: ", "fontSize80"));
		table.setWidget(1, 0, new StyledHTML("Set Comparison Type: ", "fontSize80"));
		table.setWidget(2, 0, new StyledHTML("Set Value: ", "fontSize80"));

		setField(comparison.getField());

		table.setWidget(1, 1, getComparisonTypes(comparison.getComparisonType()));

		inputType = INPUT_TYPE_FREE_TEXT;
		if (comparison.ask != null && comparison.ask.booleanValue())
			inputType = INPUT_TYPE_ASK_USER;

		getAppropriateValueSettingTool();

		table.setWidget(2, 2, new ChooserIcon());

		panel.add(table);

		panel.add(getButtonBar());

		setWidget(panel);
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
		else {
			if (comparison.compareValue != null)
				input.setText(comparison.writeCompareValue());
			table.setWidget(2, 1, input);
		}
	}

	protected Widget getButtonBar() {
		FlexTable buttonBar = new FlexTable();
		buttonBar.setCellPadding(3);
		buttonBar.setCellSpacing(3);

		buttonBar.setWidget(0, 0, new QBButtonIcon("Save", "images/document-save.png") {
			public void onClick(Widget sender) {
				onSave();
			}
		});
		buttonBar.setWidget(0, 3, new QBButtonIcon("Close", "images/process-stop.png") {
			public void onClick(Widget sender) {
				onClose();
			}
		});

		buttonBar.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_BOTTOM);
		buttonBar.getCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_BOTTOM);
		buttonBar.setWidth("100%");

		return buttonBar;
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
		hide();
	}

	public void onClose() {
		hide();
	}

	protected ListBox getComparisonTypes(String selected) {
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

	protected void setField(String canonicalField) {
		comparison.setField(canonicalField);

		VerticalPanel inner = new VerticalPanel();
		String field = comparison.getField();
		if (field != null)
			inner.add(new StyledHTML(field, "fontSize80"));
		Button button = new Button("Set...", new ClickListener() {
			public void onClick(Widget sender) {
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

	public class ChooserIcon extends Image {

		public ChooserIcon() {
			super("images/small/accessories-text-editor.png");
		}

		public void onBrowserEvent(Event evt) {
			switch(DOM.eventGetType(evt)) {
				case Event.ONCLICK: {
					openContextMenu(DOM.eventGetClientX(evt), DOM.eventGetClientY(evt));
					break;
				}
				default:
					super.onBrowserEvent(evt);
			}
		}

		protected void openContextMenu(final int currentMouseXPos,
				final int currentMouseYPos) {
			final ContextMenu contextMenu = new ContextMenu();
			final PopupPanel panel = new PopupPanel(true, false);
			panel.setWidget(contextMenu);
			panel.addStyleName("GoGoEgo_Explorer_ContextMenu");
			panel.show();

			// Adjust position of right click menu.
			final int width = contextMenu.getOffsetWidth(), height = contextMenu
					.getOffsetHeight();
			int left = currentMouseXPos, top = currentMouseYPos;

			if (currentMouseXPos + width > Window.getClientWidth()) {
				left = Window.getClientWidth() - width - 20;
			}
			if (currentMouseYPos + height > Window.getClientHeight()) {
				top = Window.getClientHeight() - height - 20;
			}

			panel.setPopupPosition(left, top);
		}

	}

	class ContextMenu extends MenuBar {

		public abstract class SmartCommand implements Command {
			public abstract void doAction();
			public void execute() {
				doAction();
				closeOnClick();
			}
		}

		public ContextMenu() {
			super(true);
			setAutoOpen(true);

			final MenuItem text = new MenuItem("Text Entry", new SmartCommand() {
				public void doAction() {
					inputType = INPUT_TYPE_FREE_TEXT;
					getAppropriateValueSettingTool();
				}
			});

			final MenuItem lut = new MenuItem("Lookup Table", new SmartCommand() {
				public void doAction() {
					inputType = INPUT_TYPE_LOOKUP_VALUE;
					getAppropriateValueSettingTool();
				}
			});

			final MenuItem ask = new MenuItem("Ask User", new SmartCommand() {
				public void doAction() {
					inputType = INPUT_TYPE_ASK_USER;
					getAppropriateValueSettingTool();
				}
			});

			addItem(text);
			if (comparison.getField() != null && db.hasLookupTable(comparison.getField()))
				addItem(lut);
			addItem(ask);
		}

		public void closeOnClick() {
			getParent().removeFromParent();
		}
	}

}
