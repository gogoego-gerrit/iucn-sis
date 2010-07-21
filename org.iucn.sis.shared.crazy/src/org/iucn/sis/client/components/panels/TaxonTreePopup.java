package org.iucn.sis.client.components.panels;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.components.panels.TaxonomyBrowserPanel.TaxonListElement;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.google.gwt.user.client.Event;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;

public class TaxonTreePopup extends Window {

	public static void fetchChildren(final TaxonNode node, final GenericCallback<List<TaxonListElement>> wayback) {
		TaxonomyCache.impl.fetchPath(String.valueOf(node.getId()), new GenericCallback<NativeDocument>() {
			public void onFailure(Throwable caught) {
				wayback.onFailure(new Throwable());
			}

			public void onSuccess(NativeDocument result) {
				final NativeNodeList options = (result).getDocumentElement().getElementsByTagName("option");
				final ArrayList<TaxonListElement> childModel = new ArrayList<TaxonListElement>();
				String name = "";
				for (int i = 0; i < options.getLength(); i++) {
					if (i == options.getLength() - 1)
						name += options.elementAt(i).getText();
					else
						name += options.elementAt(i).getText() + ",";
				}
				if (name != "") {
					TaxonomyCache.impl.fetchList(name, new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							wayback.onFailure(caught);
						}

						public void onSuccess(String result) {
							for (int i = 0; i < options.getLength(); i++) {
								childModel.add(new TaxonListElement(TaxonomyCache.impl.getNode(options.elementAt(i)
										.getText()), ""));
							}

							wayback.onSuccess(childModel);
						}
					});
				} else {
					wayback.onFailure(new Throwable());
				}

			}
		});
	}

	public TaxonTreePopup(TaxonNode node) {
		super();
		setClosable(true);
		setHeading("Full Taxonomic View");
		setHeight(300);
		setWidth(350);
		build(node);
	}

	private void build(final TaxonNode node) {
		final String[] footprint = node.getFootprint();

		LayoutContainer content = getContent();
		content.removeAll();
		final Tree tree = new Tree();
		tree.setNodeIconStyle("");
		tree.setOpenNodeIconStyle("");

		// tree.setNodeImageStyle("");
		// tree.setOpenNodeImageStyle("");
		final TreeStore<TaxonListElement> store = new TreeStore<TaxonListElement>();

		TreeBinder<TaxonListElement> binder = new TreeBinder<TaxonListElement>(tree, store);
		binder.setDisplayProperty("name");

		// viewer.setContentProvider(new ModelTreeContentProvider());
		// viewer.setLabelProvider(new ModelLabelProvider());

		// final Model treeModel = new Model();

		for (int i = 0; i < footprint.length; i++) {
			String fullName = "";

			for (int j = 5; j < (i >= TaxonNode.SUBPOPULATION ? i - 1 : i); j++)
				fullName += footprint[j] + " ";
			fullName += footprint[i];

			store.add(new TaxonListElement(TaxonNode.getDisplayableLevel(i) + ": " + fullName), true);
		}

		tree.addListener(Events.OnMouseUp, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				TreeItem selected = tree.getSelectionModel().getSelectedItem();

				if (selected != null) {
					TaxonomyCache.impl.fetchNodeWithKingdom(footprint[0], selected.getText().substring(
							selected.getText().indexOf(" ") + 1), false, new GenericCallback<TaxonNode>() {
						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub

						}

						public void onSuccess(TaxonNode result) {
							ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(String
									.valueOf((result).getId()));

						}
					});

					setVisible(false);
				}

			}
		});

		// viewer.setInput(treeModel);
		content.add(tree);
		tree.expandAll();
	}

	public LayoutContainer getContent() {
		return this;
	}
}
