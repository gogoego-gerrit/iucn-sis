package org.iucn.sis.client.panels.taxa;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.ui.models.taxa.TaxonListElement;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;

import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

public class TaxonTreePopup extends Window {

	public static void fetchChildren(final Taxon node, final GenericCallback<List<TaxonListElement>> wayback) {
		TaxonomyCache.impl.fetchPath(String.valueOf(node.getId()), new GenericCallback<NativeDocument>() {
			public void onFailure(Throwable caught) {
				wayback.onFailure(new Throwable());
			}

			public void onSuccess(NativeDocument result) {
				final NativeNodeList options = (result).getDocumentElement().getElementsByTagName("option");
				final ArrayList<TaxonListElement> childModel = new ArrayList<TaxonListElement>();
				List<Integer> ids = new ArrayList<Integer>();
				for (int i = 0; i < options.getLength(); i++) {
					ids.add(Integer.valueOf(options.elementAt(i).getText()));
				}
				if (ids.size() > 0) {
					TaxonomyCache.impl.fetchList(ids, new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							wayback.onFailure(caught);
						}

						public void onSuccess(String result) {
							for (int i = 0; i < options.getLength(); i++) {
								childModel.add(new TaxonListElement(TaxonomyCache.impl.getTaxon(options.elementAt(i)
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

	public TaxonTreePopup(Taxon node) {
		super();
		setClosable(true);
		setHeading("Full Taxonomic View");
		setHeight(300);
		setWidth(350);
		build(node);
	}

	private void build(final Taxon node) {
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

		

		for (int i = 0; i < footprint.length; i++) {
			String fullName = "";

			for (int j = 5; j < (i >= TaxonLevel.SUBPOPULATION ? i - 1 : i); j++)
				fullName += footprint[j] + " ";
			fullName += footprint[i];

			store.add(new TaxonListElement(Taxon.getDisplayableLevel(i) + ": " + fullName), true);
		}
		binder.addSelectionChangedListener(new SelectionChangedListener<TaxonListElement>() {
		
			@Override
			public void selectionChanged(SelectionChangedEvent<TaxonListElement> se) {
				TreeItem selected = tree.getSelectionModel().getSelectedItem();
				
				if (selected != null) {
					TaxonomyCache.impl.fetchTaxonWithKingdom(footprint[0], selected.getText().substring(
							selected.getText().indexOf(" ") + 1), true, new GenericCallback<Taxon>() {
						public void onFailure(Throwable caught) {
						}

						public void onSuccess(Taxon result) {
							SISClientBase.getInstance().onTaxonChanged();
						}
					});

					setVisible(false);
				}
		
			}
		});
		tree.addListener(Events.OnChange, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				TreeItem selected = tree.getSelectionModel().getSelectedItem();
				WindowUtils.errorAlert("selected is " + selected);
				if (selected != null) {
					TaxonomyCache.impl.fetchTaxonWithKingdom(footprint[0], selected.getText().substring(
							selected.getText().indexOf(" ") + 1), true, new GenericCallback<Taxon>() {
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("on an error");
							// TODO Auto-generated method stub

						}

						public void onSuccess(Taxon result) {
							WindowUtils.errorAlert("on success");
							SISClientBase.getInstance().onTaxonChanged();
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
