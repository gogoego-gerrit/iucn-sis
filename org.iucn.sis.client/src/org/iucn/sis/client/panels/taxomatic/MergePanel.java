package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.HashMap;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.event.ButtonEvent;
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
	protected final HashMap<String, Taxon> selectedNodes;
	protected final Taxon  currentNode;

	public MergePanel() {
		super();
		setHeading("Perform Merge");
		setIconStyle("icon-merge");
		currentNode = TaxonomyCache.impl.getCurrentTaxon();
		level = currentNode.getLevel();
		selectedNodes = new HashMap<String, Taxon>();
		load();
	}

	@Override
	public ButtonBar createButtonBar() {
		ButtonBar bar = new ButtonBar();

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

		String taxaLevel = Taxon .getDisplayableLevel(level);
		String fullName = currentNode.getFullName();

		return "Choose 1 or more " + taxaLevel + " taxa to merge into the " + taxaLevel + " " + fullName + ".  "
				+ "All of the chosen taxa will be synonimized as " + fullName;

	}

	@Override
	public void onSubmit() {
		if (currentNode != null) {
			final ArrayList<Taxon> nodes = new ArrayList<Taxon>(selectedNodes.values());

			if (nodes.size() > 0) {
				TaxomaticUtils.impl.performMerge(nodes, currentNode, new GenericCallback<String>() {
					public void onFailure(Throwable arg0) {
						//Error already reported by default callback.
					}
					public void onSuccess(String arg0) {
						String nodeString = "";
						for (int i = 0; i < nodes.size(); i++)
							nodeString += ((Taxon ) nodes.get(i)).getFullName() + ",";

						WindowUtils.infoAlert("Successful Merge", "The taxon (taxa) "
								+ nodeString.substring(0, nodeString.length() - 1)
								+ " has (have) been merged into taxon " + currentNode.getFullName() + ".");
						onClose();
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
	public boolean validate(String[] footPrint, Taxon  node) {
		boolean sameTaxa = (node.getId() != currentNode.getId());
		boolean validLevel = level == node.getLevel();
		String id = footPrint[footPrint.length - 1];
		boolean newID = !selectedNodes.containsKey(id);

		if (!validLevel)
			WindowUtils.errorAlert("Error", "Please " + "choose a taxon at the " + Taxon .getDisplayableLevel(level)
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
