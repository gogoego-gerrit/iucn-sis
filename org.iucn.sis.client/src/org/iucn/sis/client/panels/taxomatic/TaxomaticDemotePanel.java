package org.iucn.sis.client.panels.taxomatic;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.utils.TaxonomyBrowserPanel;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.QuickButton;
import com.solertium.util.extjs.client.WindowUtils;

public class TaxomaticDemotePanel extends TaxonChooser {

	private ButtonBar bar;
	private Taxon  currentNode;
	private PanelManager manager;
	private String parentid;

	public TaxomaticDemotePanel(PanelManager manager) {
		super();
		this.manager = manager;
		currentNode = TaxonomyCache.impl.getCurrentTaxon();
		load();
		parentid = null;
	}

	private void buildButtons() {
		bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.RIGHT);
		final Button cancelButton = new Button("Cancel", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				onClose();
			}

		});
		bar.add(cancelButton);

		final Button promoteButton = new Button("Demote taxon");
		SelectionListener listener = new SelectionListener<ComponentEvent>() {
			@Override
			public void componentSelected(ComponentEvent ce) {
				onSubmit();
			}

		};
		promoteButton.addSelectionListener(listener);
		bar.add(promoteButton);
	}

	private void close() {
		manager.taxonomicSummaryPanel.update(currentNode.getId());
		BaseEvent be = new BaseEvent(this);
		be.setCancelled(false);
		fireEvent(Events.Close, be);
	}

	@Override
	public ButtonBar getButtonBar() {
		buildButtons();
		return bar;
	}

	@Override
	public String getDescription() {
		return "By demoting " + currentNode.getFullName() + ", " + currentNode.getFullName()
				+ " will become an infrarank " + " and will have the parent that you chose from the list.";
	}

	@Override
	public void onSubmit() {
		for (Component button : bar.getItems())
			button.setEnabled(false);

		if (parentid != null) {
			TaxomaticUtils.impl.performDemotion(currentNode, parentid, new GenericCallback<String>() {

				public void onFailure(Throwable arg0) {
					for (Component button : bar.getItems())
						button.setEnabled(true);

					WindowUtils.errorAlert("Error", "There was an error while trying to " + " demote "
							+ currentNode.getFullName() + ". Please make sure "
							+ "that no one else is currently using SIS and try again later.");
				}

				public void onSuccess(String arg0) {
					for (Component button : bar.getItems())
						button.setEnabled(true);

					close();
					WindowUtils.infoAlert("Success", currentNode.getName() + " has successfully been demoted.");
					manager.taxonomicSummaryPanel.update(currentNode.getId());
				}

			});
		}

		else {
			WindowUtils.errorAlert("Error", "Please select a species where you would like to place "
					+ currentNode.getFullName());
		}

	}

	@Override
	public void removeItem(String id) {
		parentid = null;
	}

	@Override
	public boolean validate(String[] footPrint, Taxon  node) {
		if (node.getLevel() == TaxonLevel.SPECIES) {
			if (parentid == null) {
				long tempParentID = node.getId();

				if (currentNode.getId() != tempParentID) {
					parentid = node.getId() + "";
					return true;
				} else {
					WindowUtils.errorAlert("Error", "You can not demote " + currentNode.getFullName()
							+ " to be a child of itself.  Please select a different species to be the new parent of "
							+ currentNode.getFullName());
					return false;
				}

			} else {
				WindowUtils.errorAlert("Error", "You can only chose, one species to "
						+ "place the current node under.  In order to remove a species "
						+ "from the list, right click and select remove");
				return false;
			}

		}

		else
			return false;
	}

}
