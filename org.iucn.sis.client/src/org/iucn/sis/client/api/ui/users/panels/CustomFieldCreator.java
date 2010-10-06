/**
 * 
 */
package org.iucn.sis.client.api.ui.users.panels;

import java.util.List;

import org.iucn.sis.client.api.utils.UriBase;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.api.XMLWritingUtils;

/**
 * CustomFieldCreator.java
 * 
 * Window that allows a user to create and edit custom fields.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public abstract class CustomFieldCreator extends Window {

	/**
	 * OptionEditor
	 * 
	 * Window that pops up to allow editing of a select menu option or creation
	 * of a new one.
	 * 
	 * @author carl.scott <carl.scott@solertium.com>
	 * 
	 */
	private static abstract class OptionEditor extends Window {

		public OptionEditor() {
			this(null);
		}

		public OptionEditor(String text) {
			final TextBox option = new TextBox();
			if (text != null)
				option.setText(text);

			setHeading("Set Option");
			setClosable(true);

			add(new HTML("Enter Option Name: "));
			add(option);

			addButton(new Button("Done", new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					String data = option.getText();
					if (data.equals(""))
						WindowUtils.errorAlert("Error", "Please enter text or cancel.");
					else {
						close();
						onSave(data);
					}
				}
			}));

			show();
		}

		public abstract void onSave(String text);
	}

	private boolean editMode = false;

	private String id = null;
	private final TextBox name;
	private final ListBox required;
	private final ListBox type;

	private final DataList options;

	public CustomFieldCreator() {
		super();
		setModal(true);
		setClosable(true);

		name = new TextBox();
		name.setName("name");

		required = new ListBox();
		required.setName("required");
		required.addItem("Yes", "true");
		required.addItem("No", "false");

		type = new ListBox();
		type.setName("type");
		type.addItem("Text Input", "text");
		type.addItem("Drop-Down Menu", "select");

		options = new DataList();
		options.setSize(150, 200);
		options.setSelectionMode(SelectionMode.SINGLE);
	}

	private void addNew() {
		final NativeDocument document = NativeDocumentFactory.newNativeDocument();
		document.put(UriBase.getInstance().getUserBase() + "/manager/custom", getXML(),
				new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Error", "Could not save, please try again later.");
					}

					public void onSuccess(String result) {
						Info.display("Success", "Field saved.");
						close();
						onChange();
					}
				});
	}

	private void createBody() {
		final LayoutContainer container = new LayoutContainer();
		container.setLayout(new BorderLayout());

		final LayoutContainer listContainer = new LayoutContainer();
		listContainer.setLayout(new BorderLayout());
		listContainer.setSize(300, 300);

		final LayoutContainer optionWrapper = new LayoutContainer();
		optionWrapper.setLayout(new CenterLayout());
		optionWrapper.add(options);

		listContainer.add(optionWrapper, new BorderLayoutData(LayoutRegion.CENTER, 100));

		final ButtonBar bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.CENTER);
		bar.add(new Button("Add", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				new OptionEditor() {
					@Override
					public void onSave(String text) {
						options.add(text);
					}
				};
			}
		}));
		bar.add(new Button("Edit", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				final DataListItem sel = options.getSelectedItem();
				if (sel != null)
					new OptionEditor(sel.getText()) {
						@Override
						public void onSave(String text) {
							sel.setText(text);
						}
					};
			}
		}));
		bar.add(new Button("Remove", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				DataListItem sel = options.getSelectedItem();
				if (sel != null)
					options.remove(sel);
			}
		}));
		listContainer.add(bar, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		listContainer.setVisible(options.getItemCount() != 0);

		final FlexTable table = new FlexTable();
		table.setHeight("75px");
		table.setHTML(0, 0, "Field Name: ");
		table.setWidget(0, 1, name);
		table.setHTML(1, 0, "Required? ");
		table.setWidget(1, 1, required);
		table.setHTML(2, 0, "Field Type: ");
		table.setWidget(2, 1, type);

		type.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				String sel = type.getValue(type.getSelectedIndex());
				if ("select".equals(sel)) {
					listContainer.setVisible(true);
					listContainer.layout();
				} else {
					listContainer.setVisible(false);
					options.removeAll();
				}
			}
		});

		container.add(table, new BorderLayoutData(LayoutRegion.CENTER, .5f));
		container.add(listContainer, new BorderLayoutData(LayoutRegion.EAST, .5f));

		setLayout(new FillLayout());
		add(container);
	}

	/**
	 * Draw the UI
	 * 
	 */
	public void draw() {
		setSize(550, 300);
		createBody();

		addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				save();
			}
		}));
		addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				close();
			}
		}));
	}

	private String getXML() {
		final String fieldType;
		String xml = "<root>";
		xml += XMLWritingUtils.writeCDATATag("name", name.getText());
		xml += XMLWritingUtils.writeCDATATag("required", required.getValue(required.getSelectedIndex()));
		xml += XMLWritingUtils.writeCDATATag("type", fieldType = type.getValue(type.getSelectedIndex()));
		if ("select".equals(fieldType)) {
			List<DataListItem> items = options.getItems();
			for (DataListItem item : items)
				xml += XMLWritingUtils.writeTag("option", item.getText());
		}
		xml += "</root>";
		return xml;
	}

	/**
	 * Once this data has been updated via POST or PUT, the onchange function is
	 * called to signal that this custom field's data has indeed changed.
	 * 
	 */
	public abstract void onChange();

	private void save() {
		if (editMode)
			updateExisting();
		else
			addNew();
	}

	/**
	 * Sets this to editmode, supplied with data to edit. This data will be
	 * POSTed to the server upon save instead of PUT.
	 * 
	 * @param data
	 */
	public void setEditing(CustomFieldViewPanel.CustomFieldModelData data) {
		editMode = true;
		id = (String) data.get("id");

		final String fieldType;

		name.setText((String) data.get("name"));
		required.setSelectedIndex(("true".equals(data.get("required"))) ? 0 : 1);
		type.setSelectedIndex("text".equals(fieldType = (String) data.get("type")) ? 0 : 1);

		if ("select".equals(fieldType)) {
			String optionsText = data.get("options");
			if (optionsText != null && optionsText != "") {
				String[] split = optionsText.split("::");
				for (int i = 0; i < split.length; i++)
					options.add(new DataListItem(split[i]));
			}
		}
	}

	private void updateExisting() {
		final NativeDocument document = NativeDocumentFactory.newNativeDocument();
		document.post(UriBase.getInstance().getUserBase() + "/manager/custom/" + id, getXML(),
				new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Error", "Could not save, please try again later.");
					}

					public void onSuccess(String result) {
						Info.display("Success", "Field saved.");
						close();
						onChange();
					}
				});
	}
}
