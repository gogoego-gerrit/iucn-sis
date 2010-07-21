package org.iucn.sis.client.taxomatic;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.client.utilities.FormattedDate;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.data.WorkingSetCache;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class CreateNewTaxonPanel extends Window {
	public static CreateNewTaxonPanel impl = new CreateNewTaxonPanel();

	private TextBox name;
	private TextBox taxonomicAuthority;
	private ListBox rank;
	private ListBox status;
	private CheckBox hybrid;
	private boolean refresh;

	private Button save;
	private Button cancel;

	private TaxonNode parent;

	private final String DATA_KEY = "dataKeyString";
	
	private CreateNewTaxonPanel() {
		super();

		setModal(true);

		name = new TextBox();
		taxonomicAuthority = new TextBox();
		rank = new ListBox();
		status = new ListBox();
		hybrid = new CheckBox();
		hybrid.setText(" Hybrid");

		save = new Button("Save and Close");
		save.setIconStyle("icon-save");
		save.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				performSave();
			}
		});

		cancel = new Button("Cancel");
		cancel.setIconStyle("icon-cancel");
		cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		});

		FlowLayout layout = new FlowLayout(0);
		setLayout(layout);
		add(wrap("Name: ", name));
		add(wrap("Authority: ", taxonomicAuthority));
		add(wrap("Rank: ", rank));
		add(wrap("Status: ", status));
		add(wrap("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", hybrid));
		add(wrap(save, cancel));

		refresh = true;
	}

	private void addToWorkingSet(final TaxonNode newNode) {
		if (newNode.getLevel() >= TaxonNode.SPECIES) {
			WindowUtils.confirmAlert("Add To Working Set", "Would you like to add this taxon to a working set?",
					new Listener<MessageBoxEvent>() {
						public void handleEvent(MessageBoxEvent be) {
							if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
								createWorkingSetSelector(newNode);
							}
						}
					});
		}
	}

	private void createWorkingSetSelector(final TaxonNode newNode) {
		final Dialog s = new Dialog();
		s.setModal(true);
		s.setHeading("Choose Working Set");

		HTML message = new HTML("Choose the working set you would like to add " + newNode.getFullName() + " to.");

		final DataList list = new DataList();
		list.setCheckable(true);
		for (Iterator iter = WorkingSetCache.impl.getWorkingSets().values().iterator(); iter.hasNext();) {
			WorkingSetData curWS = (WorkingSetData) iter.next();
			DataListItem curItem = new DataListItem(curWS.getWorkingSetName());
			curItem.setData(DATA_KEY, curWS);

			list.add(curItem);
		}

		Button add = new Button("Add to Selected", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				String sets = "";

				List<DataListItem> checked = list.getChecked();
				for (DataListItem item : checked) {
					final WorkingSetData cur = (WorkingSetData) item.getData(DATA_KEY);

					sets += cur.getWorkingSetName() + ", ";
					cur.addSpeciesIDsAsCSV("" + newNode.getId());
					cur.setSorted(false);
					cur.sortSpeciesList();
					WorkingSetCache.impl.editWorkingSet(cur, new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
						}

						public void onSuccess(String result) {
							Info.display("Success!", newNode.getFullName() + " was added to the set {0}.", cur
									.getWorkingSetName());
						}
					});
				}

				s.hide();
			}
		});
		add.setIconStyle("icon-add");

		Button close = new Button("Close", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				s.hide();
			}
		});
		close.setIconStyle("icon-cancel");

		LayoutContainer content = s;
		content.add(message);
		content.add(list);

		s.addButton(add);
		s.addButton(close);

		s.show();
		s.center();
	}

	public void open(TaxonNode parentNode) {
		parent = parentNode;

		name.setText("");
		taxonomicAuthority.setText("");
		rank.clear();

		status.clear();
		for (Entry<String, String> entry : TaxonNode.displayableStatus.entrySet()) {
			status.addItem(entry.getValue(), entry.getKey());
		}

		if (parentNode.getLevel() >= TaxonNode.GENUS)
			status.addItem("Undescribed", "U");
		status.setSelectedIndex(0);

		if (parentNode.getLevel() == TaxonNode.SPECIES) {
			rank.addItem("Subspecies", TaxonNode.INFRARANK + "" + TaxonNode.INFRARANK_TYPE_SUBSPECIES);
			if (!parentNode.getFootprintAsString().contains("ANIMALIA"))
				rank.addItem("Variety", TaxonNode.INFRARANK + "" + TaxonNode.INFRARANK_TYPE_VARIETY);
			rank.addItem("Subpopulation", "" + TaxonNode.SUBPOPULATION);
			rank.setEnabled(true);
			hybrid.setVisible(true);
		} else {
			if (parent.getLevel() == TaxonNode.INFRARANK)
				rank.addItem(TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK_SUBPOPULATION), ""
						+ TaxonNode.INFRARANK_SUBPOPULATION);
			else
				rank
						.addItem(TaxonNode.getDisplayableLevel(parentNode.getLevel() + 1), ""
								+ (parentNode.getLevel() + 1));

			rank.setEnabled(false);
			hybrid.setVisible(false);
		}

		show();
	}

	private void performSave() {
		String message = "";
		int level = Integer.parseInt(rank.getValue(rank.getSelectedIndex()));

		int infraType = TaxonNode.INFRARANK_TYPE_NA;
		if (level > TaxonNode.INFRARANK_SUBPOPULATION) {
			infraType = level % (TaxonNode.INFRARANK * 10);
			level = Integer.parseInt(rank.getValue(rank.getSelectedIndex()).substring(0, 1));

		}

		if (name.getText().equals(""))
			message += "Name must be specified.<br />";
		if (level >= TaxonNode.SPECIES && taxonomicAuthority.getText().equals(""))
			message += "A taxonomic authority must be specified.<br />";

		if (!message.equals("")) {
			WindowUtils.errorAlert(message);
		} else {
			WindowUtils.showLoadingAlert("Saving taxon...");

			String fullName = "";

			final TaxonNode newNode = TaxonNodeFactory.createNode(-1, name.getText(), level, parent.getId() + "",
					parent.getFullName(), hybrid.isChecked(), status.getValue(status.getSelectedIndex()),
					SimpleSISClient.currentUser.getUsername(), FormattedDate.impl.getDate());
			newNode.setTaxonomicAuthority(taxonomicAuthority.getText());
			newNode.setInfraType(infraType);

			String[] newFootprint = new String[parent.getFootprint().length + 1];

			for (int i = 0; i < newFootprint.length - 1; i++)
				newFootprint[i] = parent.getFootprint()[i];

			newFootprint[newFootprint.length - 1] = parent.getName();
			newNode.setFootprint(newFootprint);

			fullName = newNode.generateFullName();

			newNode.setFullName(fullName);

			TaxomaticUtils.newTaxonNode(newNode, parent, new GenericCallback<TaxonNode>() {
				public void onFailure(Throwable caught) {
					WindowUtils.hideLoadingAlert();
					WindowUtils.errorAlert("Failure!", "New node " + newNode.getFullName() + " failed to save."
							+ " Either you have a bad connection to your server, or there "
							+ "already exists a node with the same name.");
				}

				public void onSuccess(TaxonNode arg0) {
					WindowUtils.hideLoadingAlert();
					Info.display(new InfoConfig("Taxon Created", "New taxon {0} was created successfully!", new Params(
							newNode.getFullName())));
					if (refresh)
						ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(newNode
								.getParentId()
								+ "");
					TaxonomyCache.impl.evictPaths();
					ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(newNode
							.getParentId()
							+ "");

					BaseEvent be = new BaseEvent(newNode);
					// be.source = impl;
					be.setCancelled(false);
					fireEvent(Events.StateChange, be);

					hide();

					addToWorkingSet(newNode);
				}
			});
		}
	}

	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}

	private HorizontalPanel wrap(String label, Widget widget) {
		return wrap(new HTML(label), widget);
	}

	private HorizontalPanel wrap(Widget one, Widget two) {
		HorizontalPanel panel = new HorizontalPanel();
		panel.setSpacing(3);
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		panel.add(one);
		panel.add(two);

		return panel;
	}
}
