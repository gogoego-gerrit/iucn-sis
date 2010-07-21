package org.iucn.sis.client.panels.workingsets;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.ui.models.taxa.TaxonListElement;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
import org.iucn.sis.client.panels.utils.TaxonomyBrowserPanel;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class WorkingSetAddTaxaBrowserPanel extends RefreshLayoutContainer {

	static class MyTaxonomyBrowser extends TaxonomyBrowserPanel {
		// private ListStore<TaxonListElement> store = null;

		public MyTaxonomyBrowser(PanelManager manager) {
			super();
			setAsCheckable(true);
		}

		@Override
		protected void addViewButtonToFootprint() {
			// Don't do it for this one...
		}

		public void deselectAll() {
			for (int i = 0; i < list.getItemCount(); i++)
				list.getItem(i).setChecked(false);
		}

		public List<TaxonListElement> getViewerChecked() {
			return binder.getCheckedSelection();
		}

		public void selectAll() {
			for (int i = 0; i < list.getItemCount(); i++)
				list.getItem(i).setChecked(true);
		}
	}

	private ButtonBar buttons = null;
	private WorkingSet workingSet = null;
	private MyTaxonomyBrowser browser = null;
	private PanelManager manager;

	private HTML instructions = null;

	public WorkingSetAddTaxaBrowserPanel(PanelManager manager) {
		super();

		this.manager = manager;
		browser = new MyTaxonomyBrowser(manager);
		instructions = new HTML();

		build();
	}

	private void build() {
		BorderLayout layout = new BorderLayout();
		// layout.setSpacing(10);
		setLayout(layout);

		buildButtons();
		add(instructions, new BorderLayoutData(LayoutRegion.NORTH, 60f));
		add(browser, new BorderLayoutData(LayoutRegion.CENTER));
		add(buttons, new BorderLayoutData(LayoutRegion.SOUTH, 25f));

		layout();
	}

	private void buildButtons() {
		buttons = new ButtonBar();

		Button selectAll = new Button(" Select All ", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				browser.selectAll();
			}
		});

		Button deselectAll = new Button(" Deselect All ", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				browser.deselectAll();
			}
		});

		final Button add = new Button("Add");
		add.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				add.setEnabled(false);
				if (workingSet != null) {
					WindowUtils.showLoadingAlert("Adding taxa to your working set; this "
							+ "may take some time. If you encounter any \"Unresponsive Script\" "
							+ "warnings, please continue clicking to let the script continue running "
							+ "until it finishes on its own. A working set of 2000 species, for "
							+ "instance, may take up to 3 minutes. We apologize for this temporary inconvenience...");
					List<TaxonListElement> checked = browser.getViewerChecked();

					if (checked.size() == 0) {
						add.setEnabled(true);
						WindowUtils.hideLoadingAlert();
						WindowUtils.errorAlert("Please check one or more taxa.");
					} else {
						final StringBuilder ids = new StringBuilder("<ids>");
						for (final TaxonListElement element : checked) {
							ids.append("<id>");
							ids.append(element.getNode().getId());
							ids.append("</id>");
						}
						ids.append("</ids>");

						TaxonomyCache.impl.fetchLowestLevelTaxa(ids.toString(), new GenericCallback<String>() {
							public void onFailure(Throwable caught) {
								WindowUtils.hideLoadingAlert();
								WindowUtils.errorAlert("Error loading taxa into this "
										+ "working set. Please check your Internet connection " + "and try again.");
								add.setEnabled(true);
							}

							public void onSuccess(String arg0) {
								String taxonToAdd = arg0;
								if (taxonToAdd.length() > 0) {
									String [] ids = taxonToAdd.split(",");
									Collection<Taxon> taxaToAdd = new HashSet<Taxon>();
									for (String id : ids) {
										taxaToAdd.add(TaxonomyCache.impl.getTaxon(id));
									}
									WorkingSetCache.impl.editTaxaInWorkingSet(workingSet, taxaToAdd, null, new GenericCallback<String>() {
									
										@Override
										public void onSuccess(String result) {
											WindowUtils.hideLoadingAlert();
											WindowUtils.infoAlert("Taxon successfully added " + "to working set "
													+ workingSet.getWorkingSetName());
											add.setEnabled(true);
											manager.workingSetOptionsPanel
													.refreshTaxaList(manager.workingSetOptionsPanel.checkPermissions());
											manager.workingSetOptionsPanel.listChanged();
									
										}
									
										@Override
										public void onFailure(Throwable caught) {
											WindowUtils.hideLoadingAlert();
											WindowUtils.errorAlert("Error adding taxon to " + "working set "
													+ workingSet.getWorkingSetName());
											add.setEnabled(true);
									
										}
									});
								} else {
									WindowUtils.hideLoadingAlert();
									add.setEnabled(true);
									WindowUtils.errorAlert("No taxon to add to working set " + workingSet.getWorkingSetName());
								}
							}
						});
					}
				} else {
					add.setEnabled(true);
				}
			}
		});

		buttons.add(add);
		buttons.add(selectAll);
		buttons.add(deselectAll);

	}

	@Override
	public void refresh() {
		workingSet = WorkingSetCache.impl.getCurrentWorkingSet();
		browser.update();
		if (workingSet != null)
			instructions.setHTML("<b>Instructions:</b> Browse down to a family, genus, species, sub-species, or"
					+ " infrarank.  Selecting a taxa will bring all sub taxa into the "
					+ workingSet.getWorkingSetName() + " working set.");
		else
			instructions.setHTML("<b>Instructions:</b> Please select a working set.");
	}
}
