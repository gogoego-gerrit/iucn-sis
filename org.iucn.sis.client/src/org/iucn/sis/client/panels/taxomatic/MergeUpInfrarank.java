package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;

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
			final ArrayList infrarankIDS = new ArrayList(selectedNodes.keySet());

			if (infrarankIDS.size() > 0) {
				TaxomaticUtils.impl.performMergeUpInfrarank(infrarankIDS, currentNode.getId(), currentNode
						.getFullName(), new GenericCallback<String>() {
					public void onFailure(Throwable arg0) {
						WindowUtils.errorAlert("Error", "Internal Server Error.  Other people may also "
								+ "be modifying taxa right now.  Please try again later.");

					}

					public void onSuccess(String arg0) {
						StringBuilder nodeString = new StringBuilder();
						ArrayList nodes = new ArrayList(selectedNodes.values());
						for (int i = 0; i < nodes.size(); i++)
							nodeString.append(((Taxon ) nodes.get(i)).getFullName() + ",");

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
