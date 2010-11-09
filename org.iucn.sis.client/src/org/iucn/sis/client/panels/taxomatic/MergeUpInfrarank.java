package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.Iterator;

import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class MergeUpInfrarank extends MergePanel {

	public MergeUpInfrarank() {
		super();
	}

	@Override
	public String getDescription() {

		String fullName = currentNode.getFullName();

		return "Choose 1 or more of " + fullName + "'s " + TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK)
				+ " to merge into " + fullName + ".  " + "All of the chosen "
				+ TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK) + " taxa will be synonimized as " + fullName;

	}

	@Override
	public void onSubmit() {

		if (currentNode != null) {
			final ArrayList<String> infrarankIDS = new ArrayList<String>(selectedNodes.keySet());

			if (infrarankIDS.size() > 0) {
				TaxomaticUtils.impl.performMergeUpInfrarank(infrarankIDS, currentNode.getId(), currentNode
						.getFullName(), new GenericCallback<String>() {
					public void onFailure(Throwable arg0) {
						//Error already displayed by default callback.
					}

					public void onSuccess(String arg0) {
						StringBuilder nodeString = new StringBuilder();
						for (Iterator<Taxon> iter = selectedNodes.values().iterator(); iter.hasNext(); )
							nodeString.append(iter.next().getFullName() + (iter.hasNext() ? ", " : ""));

						String noun = selectedNodes.size() == 1 ? "taxon" : "taxa";
						String verb = selectedNodes.size() == 1 ? "has" : "have";
						
						WindowUtils.infoAlert("Successful Merge", "The " + noun + " " 
								+ nodeString + " " + verb + " " 
								+ "been merged into taxon " + currentNode.getFullName() + ".");
						close();
					}

				});
			}
		} else {
			WindowUtils.errorAlert("Error", "Please navigate to a node so that nodes may be merged into it.");
		}
	}

	@Override
	public boolean validate(String[] footPrint, Taxon  node) {

		boolean correctLevel = (node.getLevel() == TaxonLevel.INFRARANK);
		boolean correctParent = node.getParentId() == currentNode.getId();
		boolean notAlreadyInList = !selectedNodes.containsKey(node.getId() + "");

		if (!correctLevel)
			WindowUtils.errorAlert("Error", "Please choose a taxon at the "
					+ TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK) + " level.");
		else if (!correctParent)
			WindowUtils.errorAlert("Error", "You must choose an " + TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK)
					+ " of " + currentNode.getFullName() + ".");

		else if (notAlreadyInList) {
			selectedNodes.put(node.getId() + "", node);
		}

		return correctParent && correctLevel && notAlreadyInList;

	}

}
