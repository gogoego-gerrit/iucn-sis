package org.iucn.sis.client.panels.taxomatic;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.taxomatic.TaxomaticUtils.TaxonomyException;
import org.iucn.sis.shared.api.models.Infratype;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.TaxonStatus;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
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
import com.solertium.lwxml.shared.GWTResponseException;
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

	private Taxon parent;

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

	private void addToWorkingSet(final Taxon newNode) {
		if (newNode.getLevel() >= TaxonLevel.SPECIES) {
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

	private void createWorkingSetSelector(final Taxon newNode) {
		final Dialog s = new Dialog();
		s.setModal(true);
		s.setHeading("Choose Working Set");

		HTML message = new HTML("Choose the working set you would like to add " + newNode.getFullName() + " to.");

		final DataList list = new DataList();
		list.setCheckable(true);
		for (Iterator iter = WorkingSetCache.impl.getWorkingSets().values().iterator(); iter.hasNext();) {
			WorkingSet curWS = (WorkingSet) iter.next();
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
					final WorkingSet cur = (WorkingSet) item.getData(DATA_KEY);

					sets += cur.getWorkingSetName() + ", ";
					cur.getSpeciesIDs().add(newNode.getId());
//					cur.setSorted(false);
//					cur.sortSpeciesList();
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

	public void open(Taxon parentNode) {
		parent = parentNode;

		name.setText("");
		taxonomicAuthority.setText("");
		rank.clear();

		status.clear();
		for (Entry<String, String> entry : TaxonStatus.displayableStatus.entrySet()) {
			status.addItem(entry.getValue(), entry.getKey());
		}

		if (parentNode.getLevel() >= TaxonLevel.GENUS)
			status.addItem("Undescribed", "U");
		status.setSelectedIndex(0);

		if (parentNode.getLevel() == TaxonLevel.SPECIES) {
			rank.addItem("Subspecies", TaxonLevel.INFRARANK + "" + Infratype.INFRARANK_TYPE_SUBSPECIES);
			if (!parentNode.getFootprintAsString().contains("ANIMALIA"))
				rank.addItem("Variety", TaxonLevel.INFRARANK + "" + Infratype.INFRARANK_TYPE_VARIETY);
			rank.addItem("Subpopulation", "" + TaxonLevel.SUBPOPULATION);
			rank.setEnabled(true);
			hybrid.setVisible(true);
		} else {
			if (parent.getLevel() == TaxonLevel.INFRARANK)
				rank.addItem(Taxon.getDisplayableLevel(TaxonLevel.INFRARANK_SUBPOPULATION), ""
						+ TaxonLevel.INFRARANK_SUBPOPULATION);
			else
				rank
						.addItem(Taxon.getDisplayableLevel(parentNode.getLevel() + 1), ""
								+ (parentNode.getLevel() + 1));

			rank.setEnabled(false);
			hybrid.setVisible(false);
		}

		show();
	}

	private void performSave() {
		String message = "";
		int level = Integer.parseInt(rank.getValue(rank.getSelectedIndex()));

		Integer infraType = null;
		if (level > TaxonLevel.INFRARANK_SUBPOPULATION) {
			infraType = level % (TaxonLevel.INFRARANK * 10);
			level = Integer.parseInt(rank.getValue(rank.getSelectedIndex()).substring(0, 1));

		}

		if (name.getText().equals(""))
			message += "Name must be specified.<br />";
		if (level >= TaxonLevel.SPECIES && taxonomicAuthority.getText().equals(""))
			message += "A taxonomic authority must be specified.<br />";

		if (!message.equals("")) {
			WindowUtils.errorAlert(message);
		} else {
			WindowUtils.showLoadingAlert("Saving taxon...");

			String fullName = "";

			final Taxon newNode = Taxon.createNode(0, name.getText(), level, hybrid.getValue()); 
			newNode.setParent(parent);
			newNode.setStatus(status.getValue(status.getSelectedIndex()));
			newNode.setTaxonomicAuthority(taxonomicAuthority.getText());
			if( infraType != null )
				newNode.setInfratype(Infratype.getInfratype(infraType, newNode));

			String[] newFootprint = new String[parent.getFootprint().length + 1];

			for (int i = 0; i < newFootprint.length - 1; i++)
				newFootprint[i] = parent.getFootprint()[i];

			newFootprint[newFootprint.length - 1] = parent.getName();
			newNode.setFootprint(newFootprint);
			fullName = newNode.generateFullName();
			newNode.setFriendlyName(fullName);

			TaxomaticUtils.impl.createNewTaxon(newNode, parent, new GenericCallback<Taxon>() {
				public void onFailure(Throwable caught) {
					WindowUtils.hideLoadingAlert();
					if (caught instanceof TaxonomyException) {
						WindowUtils.errorAlert(caught.getMessage());
					}
					else if (caught instanceof GWTResponseException && ((GWTResponseException)caught).getCode() == 423)
						WindowUtils.errorAlert("Taxomatic In Use", "Sorry, but another " +
								"taxomatic operation is currently running. Please try " +
								"again later!");
					else
						WindowUtils.errorAlert("Failure!", "New node " + newNode.getFullName() + " failed to save."
								+ " Either you have a bad connection to your server, or there "
								+ "already exists a node with the same name.");
				}

				public void onSuccess(Taxon arg0) {
					WindowUtils.hideLoadingAlert();
					Info.display(new InfoConfig("Taxon Created", "New taxon {0} was created successfully!", new Params(
							newNode.getFullName())));
					TaxonomyCache.impl.evictPaths();
					ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(arg0.getId());
					BaseEvent be = new BaseEvent(newNode);
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
