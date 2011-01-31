package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.solertium.lwxml.shared.GenericCallback;
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
	private Taxon  parentNode;
	private List<String> childrenNodes;

	public LateralMove() {
		super();
		setHeading("Perform Lateral Move");
		setIconStyle("icon-lateral-move");
		parentNode = TaxonomyCache.impl.getCurrentTaxon();
		level = parentNode.getLevel();
		childrenNodes = new ArrayList<String>();
		load();
	}

	@Override
	public ButtonBar createButtonBar() {
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
		return "Choose one or more " + Taxon .getDisplayableLevel(parentNode.getLevel() + 1)
				+ " which you would like " + "to move into " + parentNode.getFullName()
				+ ".  Therefore all chosen taxa will become children of " + parentNode.getFullName() + ".";
	}

	@Override
	public void onSubmit() {

		move.setEnabled(false);
		cancel.setEnabled(false);

		if (childrenNodes.size() > 0) {
			TaxomaticUtils.impl.lateralMove(parentNode.getId() + "", childrenNodes, new GenericCallback<String>() {
				public void onFailure(Throwable arg0) {
					move.setEnabled(true);
					cancel.setEnabled(true);
				}
				public void onSuccess(String arg0) {
					WindowUtils.infoAlert("Success",
							"All selected taxa have been successfully moved to be children of "
									+ parentNode.getFullName());
					move.setEnabled(true);
					cancel.setEnabled(true);
					/*ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(parentNode
							.getId());*/
					TaxonomyCache.impl.evictPaths();
					TaxonomyCache.impl.setCurrentTaxon(parentNode);
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
	public boolean validate(String[] footPrints, Taxon  node) {

		boolean correctLevel = (node.getLevel() == level + 1);
		boolean notAlreadyInList = !childrenNodes.contains(node.getId() + "");
		boolean notParent = node.getParentId() != parentNode.getId();
		if (!correctLevel) {
			WindowUtils.errorAlert("Error", "You may only choose taxon at the "
					+ Taxon .getDisplayableLevel(level + 1) + " level.  ");
		} else if (!notParent) {
			WindowUtils.errorAlert("Error", "You may not choose current children of " + parentNode.getFullName() + ".");
		} else if (notAlreadyInList) {
			childrenNodes.add(node.getId() + "");
		}

		return correctLevel && notAlreadyInList && notParent;
	}
}
