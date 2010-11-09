package org.iucn.sis.client.panels.taxomatic;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class TaxomaticPromotePanel extends LayoutContainer {

	private HTML message;
	private ButtonBar bar;
	private Taxon  currentNode;
	private boolean error;

	public TaxomaticPromotePanel(PanelManager manager) {
		currentNode = TaxonomyCache.impl.getCurrentTaxon();
		build();
	}

	private void build() {
		RowLayout layout = new RowLayout(Orientation.VERTICAL);
		// layout.setMargin(5);
		// layout.setSpacing(5);
		setLayout(layout);
		setSize(500, 300);

		buildMessage();
		buildButtons();

		add(message, new RowData(1d, 1d));
		add(bar, new RowData(1d, 25));
	}

	private void buildButtons() {

		bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.RIGHT);
		final Button cancelButton = new Button("OK", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				close();
			}

		});
		bar.add(cancelButton);

		if (!error) {
			cancelButton.setText("Cancel");
			bar.add(new Button("Promote taxon", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					final Button source = ce.getButton();
					cancelButton.setEnabled(false);
					source.setEnabled(false);

					TaxomaticUtils.impl.performPromotion(currentNode, new GenericCallback<String>() {
						public void onFailure(Throwable arg0) {
							cancelButton.setEnabled(true);
							source.setEnabled(true);
						}

						public void onSuccess(String arg0) {
							close();
							WindowUtils
									.infoAlert("Success", currentNode.getName() + " has successfully been promoted.");
						}
					});
				}
			}));
		}

	}

	private void buildMessage() {
		if (currentNode == null) {
			message = new HTML("<b>ERROR:</b> Please first select a taxa.");
			error = true;
		} else if (currentNode.getLevel() != TaxonLevel.INFRARANK) {
			message = new HTML("<b>ERROR:</b> You may only promote infraranks.");
			error = true;
		} else {
			message = new HTML("<b>Instructions:</b> By promoting " + currentNode.getFullName() + ", "
					+ currentNode.getFullName() + " will become a species " + " and will have the same parent that "
					+ currentNode.getParentName() + " has.");
			error = false;
		}

	}

	private void close() {
		// manager.taxonomicSummaryPanel.update(currentNode.getId()+"");
		BaseEvent be = new BaseEvent(this);
		be.setCancelled(false);
		fireEvent(Events.Close, be);
	}
}
