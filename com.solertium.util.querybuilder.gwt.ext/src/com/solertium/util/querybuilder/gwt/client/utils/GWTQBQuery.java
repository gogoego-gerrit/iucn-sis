package com.solertium.util.querybuilder.gwt.client.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;
import com.solertium.util.gwt.ui.StyledHTML;
import com.solertium.util.gwt.ui.WindowAlertMessage;
import com.solertium.util.querybuilder.query.QBComparisonConstraint;
import com.solertium.util.querybuilder.query.QBQuery;
import com.solertium.util.querybuilder.struct.DBStructure;
import com.solertium.util.querybuilder.struct.QBLookupTable;

/**
 * GWTQBQuery.java
 *
 * @author user
 *
 */
public class GWTQBQuery extends QBQuery {

	public void getXML(final XMLCallback callback) {
		ArrayList<QBComparisonConstraint> list = new ArrayList<QBComparisonConstraint>();
		conditions.getFieldsWithAskValues(list);

		if (list.isEmpty())
			callback.onSuccess(toXML());
		else {
			openFieldPrompt(list, callback);
		}
	}

	private void openFieldPrompt(final ArrayList<QBComparisonConstraint> fields, final XMLCallback callback) {
		final Window pop = new Window();
		pop.setModal(true);
		pop.setResizable(false);
		pop.setClosable(true);
		pop.addStyleName("CIPD_PopupPanel");
		pop.setHeading("Query Builder - User Entry");
		pop.setSize(450, 350);

		final FlexTable table = new FlexTable();
		table.setCellPadding(3);
		table.setCellSpacing(3);

		for (int i = 0; i < fields.size(); i++) {
			final QBComparisonConstraint cur = fields.get(i);
			table.setWidget(i, 0, new StyledHTML(cur.getField(), "fontSize80;bold"));
			table.setWidget(i, 1, new TextBox());
			if (DBStructure.getInstance().hasLookupTable(cur.getField()))
				table.setWidget(i, 2, new ChooserIcon(i) {
					public void updateInput(int inputType) {
						if (inputType == INPUT_TYPE_LOOKUP_VALUE) {
							QBLookupTable lut = DBStructure.getInstance().getLookupTable(cur.getField());
							lut.getLookupValues(new GenericCallback<NativeDocument>() {
								public void onSuccess(NativeDocument result) {
									ListBox inputBox = new ListBox();
									inputBox.setSelectedIndex(0);
									RowParser parser = new RowParser();
									parser.parseAmbiguousSearchRows(result);
									Iterator<RowData> it = parser.iterator();
									while (it.hasNext()) {
										RowData curRow = it.next();
										inputBox.addItem(curRow.getField(RowData.AMBIG_NAME), curRow.getField(RowData.AMBIG_VALUE));
									}
									table.setWidget(index, 1, inputBox);
								}
								public void onFailure(Throwable caught) {
									new WindowAlertMessage(WindowAlertMessage.IMAGE_DIALOG_ERROR,
										"Could not find look up table, please enter " +
										"the appropriate value by hand.", "OK").show();
									updateInput(INPUT_TYPE_FREE_TEXT);
								}
							});
						}
						else {
							table.setWidget(index, 1, new TextBox());
						}
					}
				});
		}

		//Cuz who knows how many there will be.
		pop.setScrollMode(Scroll.AUTO);

		pop.addButton(new StyledButton("Save", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				for (int i = 0; i < table.getRowCount(); i++) {
					QBComparisonConstraint cur = fields.get(i);
					Widget widget = table.getWidget(i, 1);
					if (widget instanceof TextBox)
						cur.setComparisonValue(((TextBox)widget).getText());
					else {
						ListBox box = (ListBox)widget;
						cur.setComparisonValue(box.getValue(box.getSelectedIndex()));
					}
					callback.addValue(cur.getField(), cur.writeCompareValue());
				}
				pop.hide();
				callback.onSuccess(toXML());
			}
		}));
		pop.addButton(new StyledButton("Close", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				pop.hide();
			}
		}));
		pop.setButtonAlign(HorizontalAlignment.CENTER);

		pop.add(table);
		pop.show();
	}

	public static abstract class XMLCallback {
		protected final HashMap<String, String> selectedValues = new HashMap<String, String>();

		public abstract void onSuccess(String xml);

		protected void addValue(String key, String value) {
			selectedValues.put(key, value);
		}

		protected HashMap<String, String> getSelectedValues() {
			return selectedValues;
		}

	}

	public abstract class ChooserIcon extends Image {

		protected int index;

		public ChooserIcon(int index) {
			super("images/small/accessories-text-editor.png");
			addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					openContextMenu();
				}
			});
		}

		public abstract void updateInput(int inputType);

		private void openContextMenu() {
			final ContextMenu contextMenu = new ContextMenu() {
				public void getAppropriateValueSettingTool(int inputType) {
					updateInput(inputType);
				}
			};
			contextMenu.show(this);
		}

	}

	abstract class ContextMenu extends Menu {

		public abstract class SmartCommand extends SelectionListener<MenuEvent> {
			public void componentSelected(MenuEvent ce) {
				execute();
			}
			public abstract void doAction();
			public void execute() {
				doAction();
				closeOnClick();
			}
		}

		public abstract void getAppropriateValueSettingTool(int inputType);

		public ContextMenu() {
			super();

			final MenuItem text = new MenuItem("Text Entry", new SmartCommand() {
				public void doAction() {
					getAppropriateValueSettingTool(INPUT_TYPE_FREE_TEXT);
				}
			});

			final MenuItem lut = new MenuItem("Lookup Table", new SmartCommand() {
				public void doAction() {
					getAppropriateValueSettingTool(INPUT_TYPE_LOOKUP_VALUE);
				}
			});

			add(text);
			add(lut);
		}

		public void closeOnClick() {
			getParent().removeFromParent();
		}
	}

	static class StyledButton extends Button {

		public StyledButton(String text, SelectionListener<ButtonEvent> listener) {
			super(text, listener);
		}

	}

}
