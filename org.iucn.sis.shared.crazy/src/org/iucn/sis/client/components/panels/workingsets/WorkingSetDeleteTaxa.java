package org.iucn.sis.client.components.panels.workingsets;

import java.util.List;

import org.iucn.sis.client.components.panels.PanelManager;
import org.iucn.sis.client.components.panels.workingsets.WorkingSetTaxaList.TaxaData;
import org.iucn.sis.client.ui.RefreshLayoutContainer;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.data.WorkingSetCache;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;

public class WorkingSetDeleteTaxa extends RefreshLayoutContainer {

	PanelManager manager = null;
	HTML instructions = null;
	DataList list = null;
	Button delete = null;

	public WorkingSetDeleteTaxa(PanelManager manager) {
		super();
		this.manager = manager;
		build();
		layout();
	}

	private void build() {
		RowLayout layout = new RowLayout();
		RowData north = new RowData(1d, 40);
		RowData center = new RowData(1d, -1);
		RowData south = new RowData(1d, -1);
		setLayout(layout);

		buildInstructions(north);
		buildContent(center);
		buildButtons(south);
	}

	private void buildButtons(RowData data) {
		ButtonBar buttons = new ButtonBar();
		buttons.setAlignment(HorizontalAlignment.LEFT);
		delete = new Button("Delete Taxa", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				deleteTaxa();
			}
		});
		buttons.add(delete);
		add(buttons, data);
	}

	private void buildContent(RowData data) {
		list = new DataList();
		list.setFlatStyle(true);
		list.addStyleName("gwt-background");
		list.setBorders(true);
		list.setScrollMode(Scroll.AUTO);
		add(list, data);
	}

	private void buildInstructions(RowData data) {
		instructions = new HTML();
		add(instructions, data);
	}

	private void deleteTaxa() {

		if (list.getItemCount() > 0) {
			delete.setEnabled(false);

			final WorkingSetData ws = WorkingSetCache.impl.getCurrentWorkingSet();
			List<String> idsToSave = ws.getSpeciesIDs();

			for (int i = 0; i < list.getItemCount(); i++) {
				idsToSave.remove(list.getItem(i).getId());
			}

			ws.setSpeciesIDs(idsToSave);
			ws.setSorted(false);
			ws.sortSpeciesList();
			WorkingSetCache.impl.editWorkingSet(ws, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					Info.display(new InfoConfig("Error", "Error removing taxa from working set "
							+ ws.getWorkingSetName() + "."));
				}

				public void onSuccess(String arg0) {
					Info.display("Taxa Removed", "Taxa was successfully removed from working set "
							+ ws.getWorkingSetName() + ".", "");
					manager.workingSetOptionsPanel.listChanged();
					list.removeAll();
				}
			});
		}

	}

	/*
	 * clears list
	 */
	@Override
	public void refresh() {

		if (list.getItemCount() != 0)
			list.removeAll();

		if (WorkingSetCache.impl.getCurrentWorkingSet() != null) {
			instructions.setHTML("<b>Instructions:</b> Select the taxa that you would like to delete from the "
					+ WorkingSetCache.impl.getCurrentWorkingSet().getWorkingSetName() + " working set.  The taxa that "
					+ "will be removed are added to the list below.");
			delete.setEnabled(true);
		} else {
			instructions.setHTML("<b>Instructions:</b> Please first select a working set.");
			delete.setEnabled(false);

		}
	}

	public void refreshTaxa(List<TaxaData> checked) {
		list.removeAll();
			
		for (TaxaData data : checked)
			refreshTaxa(data.getName(), data.getType(), data.getID(), data.getChildIDS());
	}

	public void refreshTaxa(String name, String type, String id, String childIDS) {
		// ADDING TO LIST
		if (type.equals(TaxaData.FULLNAME)) {
			DataListItem item = new DataListItem();
			item.setIconStyle("icon-trash");
			item.setText(name);
			item.setId(id);
			list.add(item);
		} else {
			String[] ids = childIDS.split(",");
			for (int i = 0; i < ids.length; i++) {
				DataListItem item = new DataListItem();
				item.setIconStyle("icon-trash");
				TaxonNode node = TaxonomyCache.impl.getNode(ids[i]);
				item.setText(node.getFullName());
				item.setId(node.getId() + "");
				list.add(item);
			}
		}

		layout();
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);

		list.setHeight(height - 40 - 30);
	}

}
