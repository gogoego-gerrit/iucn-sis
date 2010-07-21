package org.iucn.sis.client.taxomatic;

import java.util.ArrayList;
import java.util.HashMap;

import org.iucn.sis.client.components.panels.TaxonomyBrowserPanel;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.QuickButton;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * MergePanel.java
 * 
 * Merges two or more nodes of the same taxonomic level together.
 * 
 * @author carl.scott
 * 
 */
public class MergePanel extends TaxonChooser {

	protected final int level;
	protected final HashMap selectedNodes;
	protected final TaxonNode currentNode;

	public MergePanel() {
		super();
		currentNode = TaxonomyCache.impl.getCurrentNode();
		level = currentNode.getLevel();
		selectedNodes = new HashMap();
		load();
	}

	protected void close() {
		BaseEvent be = new BaseEvent(this);
		be.setCancelled(false);
		fireEvent(Events.Close, be);
	}

	@Override
	public ButtonBar getButtonBar() {
		ButtonBar bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.CENTER);

		bar.add(new QuickButton("Merge Taxa", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				onSubmit();
			}
		}));
		bar.add(new QuickButton("Cancel Merge", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				onClose();
			}
		}));

		return bar;
	}

	@Override
	public String getDescription() {

		String taxaLevel = TaxonNode.getDisplayableLevel(level);
		String fullName = currentNode.getFullName();

		return "Choose 1 or more " + taxaLevel + " taxa to merge into the " + taxaLevel + " " + fullName + ".  "
				+ "All of the chosen taxa will be synonimized as " + fullName;

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
		if (currentNode != null) {
			final ArrayList nodes = new ArrayList(selectedNodes.values());

			if (nodes.size() > 0) {
				TaxomaticUtils.impl.performMerge(nodes, currentNode, new GenericCallback<String>() {
					public void onFailure(Throwable arg0) {
						WindowUtils.errorAlert("Error", "Internal Server Error.  Other people may also "
								+ "be modifying taxa right now.  Please try again later.");

					}

					public void onSuccess(String arg0) {
						String nodeString = "";
						for (int i = 0; i < nodes.size(); i++)
							nodeString += ((TaxonNode) nodes.get(i)).getFullName() + ",";

						WindowUtils.infoAlert("Successful Merge", "The taxon (taxa) "
								+ nodeString.substring(0, nodeString.length() - 1)
								+ " has (have) been merged into taxon " + currentNode.getFullName() + ".");
						close();
					}

				});
			}
		} else {
			WindowUtils.errorAlert("Error", "Please navigate to a node so that nodes may be merged into it.");
		}
	}

	@Override
	public void removeItem(String id) {
		selectedNodes.remove(id);
	}

	@Override
	public boolean validate(String[] footPrint, TaxonNode node) {
		boolean sameTaxa = (node.getId() != currentNode.getId());
		boolean validLevel = level == node.getLevel();
		String id = footPrint[footPrint.length - 1];
		boolean newID = !selectedNodes.containsKey(id);

		if (!validLevel)
			WindowUtils.errorAlert("Error", "Please " + "choose a taxon at the " + TaxonNode.getDisplayableLevel(level)
					+ " level.");
		else if (!sameTaxa)
			WindowUtils.errorAlert("Error", "You may not merge the taxa with itself.  Please choose another "
					+ "taxa to merge into " + currentNode.getFullName() + ".");
		else if (newID) {
			selectedNodes.put(id, node);
		}
		return validLevel && sameTaxa && newID;
	}
}
