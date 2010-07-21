package org.iucn.sis.client.taxomatic;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.components.panels.PanelManager;
import org.iucn.sis.client.components.panels.TaxonomyBrowserPanel;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.QuickButton;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * LateralMove.java
 * 
 * Allows user to finds nodes to perform a lateral move to the current node
 * 
 * @author carl.scott
 * 
 */
public class LateralMove extends TaxonChooser {

	private Button move;
	private Button cancel;
	private final int level;
	private TaxonNode parentNode;
	private PanelManager manager;
	private List<String> childrenNodes;

	public LateralMove(PanelManager manager) {
		super();
		parentNode = TaxonomyCache.impl.getCurrentNode();
		level = parentNode.getLevel();
		childrenNodes = new ArrayList<String>();
		load();
	}

	public ArrayList getApplicableTaxonLevels() {
		ArrayList list = new ArrayList();
		int level = parentNode.getLevel();
		list.add((level + 1) + "");
		return list;
	}

	@Override
	public ButtonBar getButtonBar() {
		ButtonBar bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.CENTER);
		move = new Button("Move Taxa", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				onSubmit();
			}
		});
		bar.add(move);
		cancel = new Button("Cancel Move", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				onClose();
			}
		});
		bar.add(cancel);

		return bar;
	}

	@Override
	public String getDescription() {
		return "Choose one or more " + TaxonNode.getDisplayableLevel(parentNode.getLevel() + 1)
				+ " which you would like " + "to move into " + parentNode.getFullName()
				+ ".  Therefore all chosen taxa will become children of " + parentNode.getFullName() + ".";
	}

	@Override
	public TaxonomyBrowserPanel getTaxonomyBrowserPanel() {
		return new TaxonomyBrowserPanel() {
			@Override
			protected void addViewButtonToFootprint() {
				footprintPanel.add(new QuickButton("Add", new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						String display = footprints[footprints.length - 1];
						addItem(footprints, TaxonomyCache.impl.getNode(display));
					}
				}));
			}
		};
	}

	@Override
	public void onSubmit() {

		move.setEnabled(false);
		cancel.setEnabled(false);

		if (childrenNodes.size() > 0) {
			TaxomaticUtils.impl.lateralMove(parentNode.getId() + "", childrenNodes, new GenericCallback<String>() {
				public void onFailure(Throwable arg0) {
					WindowUtils.errorAlert("Error", "Unable to complete the move of the selected taxa.  "
							+ "Please confirm that no one else is currently on the system and try again.");
					move.setEnabled(true);
					cancel.setEnabled(true);

				}

				public void onSuccess(String arg0) {
					WindowUtils.infoAlert("Success",
							"All selected taxa have been successfully moved to be children of "
									+ parentNode.getFullName());
					move.setEnabled(true);
					cancel.setEnabled(true);
					ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(parentNode
							.getId()
							+ "");
					TaxonomyCache.impl.evictPaths();
					onClose();

				}

			});
		} else {
			WindowUtils.infoAlert("You must choose at least one taxon to move.");
		}

	}

	@Override
	public void removeItem(String id) {
		childrenNodes.remove(id);
	}

	@Override
	public boolean validate(String[] footPrints, TaxonNode node) {

		boolean correctLevel = (node.getLevel() == level + 1);
		boolean notAlreadyInList = !childrenNodes.contains(node.getId() + "");
		boolean notParent = !node.getParentId().equalsIgnoreCase(parentNode.getId() + "");
		if (!correctLevel) {
			WindowUtils.errorAlert("Error", "You may only choose taxon at the "
					+ TaxonNode.getDisplayableLevel(level + 1) + " level.  ");
		} else if (!notParent) {
			WindowUtils.errorAlert("Error", "You may not choose current children of " + parentNode.getFullName() + ".");
		} else if (notAlreadyInList) {
			childrenNodes.add(node.getId() + "");
		}

		return correctLevel && notAlreadyInList && notParent;
	}
}
