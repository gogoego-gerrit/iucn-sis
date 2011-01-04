package org.iucn.sis.client.panels.taxa.tagging;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.ui.models.taxa.TaxonListElement;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
import org.iucn.sis.client.panels.utils.TaxonomyBrowserPanel;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;

public class TaxaTaggingBrowser extends RefreshLayoutContainer {

	static class MyTaxonomyBrowser extends TaxonomyBrowserPanel {
		// private ListStore<TaxonListElement> store = null;

		public MyTaxonomyBrowser() {
			super();
			setAsCheckable(true);
		}

		@Override
		protected void addViewButtonToFootprint() {
			// Don't do it for this one...
		}

		public void deselectAll() {
			for (int i = 0; i < getList().getItemCount(); i++)
				getList().getItem(i).setChecked(false);
		}

		public List<TaxonListElement> getViewerChecked() {
			return getBinder().getCheckedSelection();
		}

		public void selectAll() {
			for (int i = 0; i < getList().getItemCount(); i++)
				getList().getItem(i).setChecked(true);
		}
	}

	private final MyTaxonomyBrowser browser;
	private final SimpleListener tagListener;
	
	private String currentTag;

	public TaxaTaggingBrowser(SimpleListener tagListener) {
		super();
		setLayout(new BorderLayout());

		this.tagListener = tagListener;
		
		browser = new MyTaxonomyBrowser();
		browser.setCheckableLevel(TaxonLevel.FAMILY);
		browser.update();

		build();
	}
	
	public void setCurrentTag(String currentTag) {
		this.currentTag = currentTag;
	}

	private void build() {
		add(new HTML("<b>Instructions:</b> Please select taxa to tag."), new BorderLayoutData(LayoutRegion.NORTH, 40f));
		add(browser, new BorderLayoutData(LayoutRegion.CENTER));
		add(buildButtons(), new BorderLayoutData(LayoutRegion.SOUTH, 25f));
	}

	private ButtonBar buildButtons() {
		ButtonBar buttons = new ButtonBar();

		buttons.add(new Button("Add", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (currentTag == null) {
					WindowUtils.errorAlert("Please select a tag first.");
					return;
				}
					
				WindowUtils.showLoadingAlert("Tagging taxa as " + currentTag + "; this "
						+ "may take some time. If you encounter any \"Unresponsive Script\" "
						+ "warnings, please continue clicking to let the script continue running "
						+ "until it finishes on its own. A group of 2000 species, for "
						+ "instance, may take up to 3 minutes. We apologize for this temporary inconvenience...");
				List<TaxonListElement> checked = browser.getViewerChecked();
				if (checked.isEmpty()) {
					WindowUtils.hideLoadingAlert();
					WindowUtils.errorAlert("Please check one or more taxa.");
				} else {
					/*
					 * Jim says let's tag the actual taxon selected, 
					 * not the child taxa.
					 */
					List<Taxon> taxaToAdd = new ArrayList<Taxon>();
					for (TaxonListElement element : checked)
						taxaToAdd.add(element.getNode());
					
					TaxonomyCache.impl.tagTaxa(currentTag, taxaToAdd, new GenericCallback<Object>() {
						public void onSuccess(Object result) {
							WindowUtils.hideLoadingAlert();
							WindowUtils.infoAlert("Taxa successfully tagged as " + currentTag);
								
							if (tagListener != null)
								tagListener.handleEvent();
						}
						public void onFailure(Throwable caught) {
							WindowUtils.hideLoadingAlert();
							WindowUtils.errorAlert("Error tagging taxa.");
						}
					});
					/*final StringBuilder ids = new StringBuilder("<ids>");
					for (final TaxonListElement element : checked) {
						ids.append("<id>");
						ids.append(element.getNode().getId());
						ids.append("</id>");
					}
					ids.append("</ids>");

					TaxonomyCache.impl.fetchLowestLevelTaxa(ids.toString(), new GenericCallback<List<Taxon>>() {
						public void onFailure(Throwable caught) {
							WindowUtils.hideLoadingAlert();
							WindowUtils.errorAlert("Error loading taxa into this "
								+ "working set. Please check your Internet connection " + "and try again.");
						}
						public void onSuccess(List<Taxon> taxaToAdd) {
							if (!taxaToAdd.isEmpty()) {
								TaxonomyCache.impl.tagTaxa(currentTag, taxaToAdd, new GenericCallback<Object>() {
									public void onSuccess(Object result) {
										WindowUtils.hideLoadingAlert();
										WindowUtils.infoAlert("Taxa successfully tagged as " + currentTag);
											
										if (tagListener != null)
											tagListener.handleEvent();
									}
									public void onFailure(Throwable caught) {
										WindowUtils.hideLoadingAlert();
										WindowUtils.errorAlert("Error tagging taxa.");
									}
								});
							} else {
								WindowUtils.hideLoadingAlert();
								WindowUtils.errorAlert("No taxa tagged.");
							}
						}
					});*/
				}
			}
		}));
		buttons.add(new Button(" Select All ", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				browser.selectAll();
			}
		}));
		buttons.add(new Button(" Deselect All ", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				browser.deselectAll();
			}
		}));

		return buttons;
	}

	@Override
	public void refresh() {
		browser.update();
		if (tagListener != null)
			tagListener.handleEvent();
	}
}
