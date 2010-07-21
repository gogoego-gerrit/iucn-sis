package org.iucn.sis.client.taxomatic;

import java.util.ArrayList;

import org.iucn.sis.client.components.panels.PanelManager;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.taxonomyTree.CommonNameData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class TaxonCommonNameEditor extends LayoutContainer {

	private final PanelManager manager;
	private final TaxonNode node;
	private final VerticalPanel commonNameInfo;
	private final ArrayList allCommonNames;
	private CommonNameData currentCommonName;
	private ButtonBar bar;
	private Button delete;
	private ListBox commonNameList;

	private ListBox validated;
	private ListBox status;
	private TextBox name;
	private HTML language;

	private int numberAdded;

	public TaxonCommonNameEditor(PanelManager manager) {
		this.manager = manager;
		this.node = TaxonomyCache.impl.getCurrentNode();
		commonNameInfo = new VerticalPanel();
		allCommonNames = new ArrayList();
		numberAdded = 0;

		name = new TextBox();
		name.addFocusListener(new FocusListener() {

			public void onFocus(Widget arg0) {
				// TODO Auto-generated method stub

			}

			public void onLostFocus(Widget arg0) {
				String oldName = name.getName();
				boolean found = false;
				for (int i = 0; i < commonNameList.getItemCount() && !found; i++) {
					if (commonNameList.getItemText(i).equalsIgnoreCase(oldName)) {
						commonNameList.setItemText(i, name.getText());
						name.setName(name.getText());
						found = true;
					}
				}
			}

		});
		validated = new ListBox();
		validated.addItem("true", "true");
		validated.addItem("false", "false");

		language = new HTML();

		status = new ListBox();
		for (int i = 0; i < CommonNameData.reasons.length; i++)
			status.addItem(CommonNameData.reasons[i], i + "");

		draw();
	}

	private void clearData() {
		name.setText("");
		language.setText("");
		status.setSelectedIndex(0);
		validated.setSelectedIndex(0);

	}

	private void close() {
		BaseEvent event = new BaseEvent(this);
		event.setCancelled(false);
		fireEvent(Events.Close, event);
	}

	private void draw() {
		addStyleName("gwt-background");

		if (node == null) {
			add(new HTML("Please first select a taxon to add edit the commonName data for."));
			return;
		}

		setLayout(new BorderLayout());
		BorderLayoutData north = new BorderLayoutData(LayoutRegion.NORTH, .1f);
		BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER, .8f);
		BorderLayoutData south = new BorderLayoutData(LayoutRegion.SOUTH, .1f);

		drawInstructions(north);
		drawCommonNameChooser(center);
		drawButtons(south);

		getCommonNameData();
		refreshListBox();
	}

	private void drawButtons(BorderLayoutData layoutData) {
		bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.RIGHT);
		Button save = new Button("Save");
		save.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				save();
			}

		});
		Button saveAndClose = new Button("Save and Close");
		saveAndClose.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				saveAndClose();
			}

		});
		Button close = new Button("Cancel");
		close.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				close();
			}

		});
		delete = new Button("Delete CommonName");
		delete.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				// String oldName = name.getName();
				// boolean found = false;
				// for (int i = 0; i < commonNameList.getItemCount() && !found;
				// i++)
				// {
				// if (commonNameList.getItemText(i).equalsIgnoreCase(oldName))
				// {
				// commonNameList.removeItem(i);
				// commonNameList.setSelectedIndex(0);
				// found = true;
				// }
				// }
				// if (found)
				allCommonNames.remove(currentCommonName);

				currentCommonName = null;
				refreshListBox();
				refreshCommonNameData(currentCommonName);
			}

		});
		bar.add(close);
		bar.add(save);
		bar.add(saveAndClose);
		bar.add(delete);
		add(bar, layoutData);
	}

	private void drawCommonNameChooser(BorderLayoutData layoutData) {
		HorizontalPanel outterWraper = new HorizontalPanel();
		outterWraper.setSpacing(0);
		outterWraper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		HorizontalPanel panel = new HorizontalPanel();
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		HTML html = new HTML("Common Names: ");
		panel.add(html);
		panel.setSpacing(5);

		commonNameList = new ListBox(false);
		commonNameList.addItem("", "");
		commonNameList.addChangeListener(new ChangeListener() {
			public void onChange(Widget arg0) {
				String value = commonNameList.getValue(commonNameList.getSelectedIndex());
				if (value.equalsIgnoreCase("")) {
					refreshCommonNameData(null);
				} else {
					refreshCommonNameData((CommonNameData) allCommonNames.get(Integer.parseInt(value)));
				}
			}
		});
		commonNameList.setWidth("200px");
		panel.add(commonNameList);
		panel.setSpacing(10);
		outterWraper.add(panel);

		LayoutContainer container = new LayoutContainer();
		ScrollPanel scroller = new ScrollPanel(commonNameInfo);
		commonNameInfo.setSize("100%", "100%");
		container.setLayout(new BorderLayout());
		container.add(outterWraper, new BorderLayoutData(LayoutRegion.NORTH, 35));
		container.add(scroller, new BorderLayoutData(LayoutRegion.CENTER));
		commonNameInfo.addStyleName("gwt-background");
		add(container, layoutData);

		layout();
	}

	private void drawInstructions(BorderLayoutData layoutData) {
		HTML html = new HTML("<b>Instructions:</b> Select the commonName from the list which"
				+ " you would like to edit, or chose to create a new commonName.", true);
		add(html, layoutData);
	}

	private void getCommonNameData() {
		allCommonNames.clear();
		ArrayList temp = node.getCommonNames();
		for (int i = 0; i < temp.size(); i++) {
			allCommonNames.add(((CommonNameData) temp.get(i)).deepCopy());
		}
	}

	private String getShowableData(String data) {
		if (data == null) {
			data = "";
		}
		return data;
	}

	public void refreshCommonNameData(CommonNameData newCommonName) {
		storePreviousData();
		clearData();

		currentCommonName = newCommonName;

		commonNameInfo.clear();
		if (currentCommonName == null) {
			commonNameInfo.setBorderWidth(0);
			delete.setEnabled(false);
		} else {
			delete.setEnabled(true);
			commonNameInfo.setBorderWidth(1);
			commonNameInfo.setSpacing(10);

			HorizontalPanel hp = new HorizontalPanel();
			HTML html = new HTML("Name: ");
			name.setText(getShowableData(currentCommonName.getName()));
			name.setName(currentCommonName.getName());
			hp.setSpacing(5);
			hp.add(html);
			hp.add(name);
			commonNameInfo.add(hp);

			hp = new HorizontalPanel();
			html = new HTML("Language / ISO: ");
			language.setText(getShowableData(currentCommonName.getLanguage()) + " / "
					+ getShowableData(currentCommonName.getIsoCode()));
			hp.setSpacing(5);
			hp.add(html);
			hp.add(language);
			commonNameInfo.add(hp);

			hp = new HorizontalPanel();
			html = new HTML("Validated: ");
			validated.setSelectedIndex(currentCommonName.isValidated() ? 0 : 1);
			hp.setSpacing(5);
			hp.add(html);
			hp.add(validated);
			commonNameInfo.add(hp);

			hp = new HorizontalPanel();
			html = new HTML("Status: ");
			status.setSelectedIndex(currentCommonName.getChangeReason());
			hp.setSpacing(5);
			hp.add(html);
			hp.add(status);
			commonNameInfo.add(hp);
		}

	}

	public void refreshListBox() {
		commonNameList.clear();
		commonNameList.addItem("", "");
		for (int i = 0; i < allCommonNames.size(); i++) {
			commonNameList.addItem(((CommonNameData) allCommonNames.get(i)).getName(), i + "");
		}
	}

	private void save() {

		bar.disable();
		storePreviousData();
		int index = 0;
		for (int i = 0; i < allCommonNames.size(); i++) {
			if (allCommonNames.get(index) == null) {
				SysDebugger.getInstance().println("I am removing index " + index);
				allCommonNames.remove(index);
			} else {
				SysDebugger.getInstance().println(
						"In the loop with " + ((CommonNameData) allCommonNames.get(index)).getName());
				index++;
			}

		}
		SysDebugger.getInstance().println(
				"Before I just set the node to have all commonNames " + TaxonNodeFactory.nodeToDetailedXML(node));
		node.setCommonNames(allCommonNames);
		SysDebugger.getInstance().println(
				"I just set the node to have all commonNames " + TaxonNodeFactory.nodeToDetailedXML(node));
		TaxomaticUtils.impl.writeNodeToFS(node, new GenericCallback<Object>() {

			public void onFailure(Throwable arg0) {
				bar.enable();
				WindowUtils.errorAlert("Error", "An error occurred when trying to save the commonName data related to "
						+ node.getFullName() + ".");
			}

			public void onSuccess(Object arg0) {
				bar.enable();
				WindowUtils.infoAlert("Saved", "All the commonName data related to " + node.getFullName()
						+ " has been saved.");
				manager.taxonomicSummaryPanel.update(node.getId() + "");
			}

		});
	}

	private void saveAndClose() {
		bar.disable();
		storePreviousData();
		int index = 0;
		for (int i = 0; i < allCommonNames.size(); i++) {
			if (allCommonNames.get(index) == null)
				allCommonNames.remove(index);
			else
				index++;
		}

		node.setCommonNames(allCommonNames);
		TaxomaticUtils.impl.writeNodeToFS(node, new GenericCallback<Object>() {

			public void onFailure(Throwable arg0) {
				bar.enable();
				WindowUtils.errorAlert("Error", "An error occurred when trying to save the commonName data related to "
						+ node.getFullName() + ".");
			}

			public void onSuccess(Object arg0) {
				bar.enable();
				WindowUtils.infoAlert("Saved", "All the commonName data related to " + node.getFullName()
						+ " has been saved.");
				manager.taxonomicSummaryPanel.update(node.getId() + "");
				close();
			}

		});
	}

	private void storePreviousData() {
		if (currentCommonName != null) {
			currentCommonName.setName(name.getText());
			currentCommonName.setValidated(validated.getValue(validated.getSelectedIndex()).equalsIgnoreCase("true"),
					status.getSelectedIndex());
		}

	}

}
