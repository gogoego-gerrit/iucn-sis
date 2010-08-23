package com.solertium.util.querybuilder.gwt.client.utils;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.google.gwt.user.client.ui.ScrollPanel;
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
		final CenteredPopupPanel pop = new CenteredPopupPanel(false, true);
		pop.addStyleName("CIPD_PopupPanel");

		VerticalPanel panel = new VerticalPanel();
		panel.add(new StyledHTML("Query Builder - User Entry", "CIPD_Header"));

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
		final ScrollPanel wrap = new ScrollPanel();
		wrap.setHeight("120px");
		wrap.setWidget(table);

		panel.add(wrap);

		FlexTable buttonBar = new FlexTable();
		buttonBar.setCellPadding(3);
		buttonBar.setCellSpacing(3);

		buttonBar.setWidget(0, 0, new StyledButton("Save", new ClickListener() {
			public void onClick(Widget sender) {
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
		buttonBar.setWidget(0, 1, new StyledButton("Close", new ClickListener() {
			public void onClick(Widget sender) {
				pop.hide();
			}
		}));
		buttonBar.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_BOTTOM);
		buttonBar.getCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_BOTTOM);
		buttonBar.setWidth("100%");

		panel.add(buttonBar);
		panel.setCellHorizontalAlignment(buttonBar, HasHorizontalAlignment.ALIGN_CENTER);

		pop.add(panel);
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

		public abstract void updateInput(int inputType);

		private void openContextMenu(final int currentMouseXPos,
				final int currentMouseYPos) {
			final ContextMenu contextMenu = new ContextMenu() {
				public void getAppropriateValueSettingTool(int inputType) {
					updateInput(inputType);
				}
			};
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

	abstract class ContextMenu extends MenuBar {

		public abstract class SmartCommand implements Command {
			public abstract void doAction();
			public void execute() {
				doAction();
				closeOnClick();
			}
		}

		public abstract void getAppropriateValueSettingTool(int inputType);

		public ContextMenu() {
			super(true);
			setAutoOpen(true);

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

			addItem(text);
			addItem(lut);
		}

		public void closeOnClick() {
			getParent().removeFromParent();
		}
	}

	static class StyledButton extends Button {

		public StyledButton(String text, ClickListener listener) {
			super(text, listener);
			addStyleName("button");
		}

	}

}
