package org.iucn.sis.client.panels.workingsets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
import org.iucn.sis.client.panels.utils.SearchPanel;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.table.TableItem;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * Panel that allows users to add taxon to their working set
 */
public class WorkingSetAddTaxaSearchPanel extends RefreshLayoutContainer {

	class MySearchPanel extends SearchPanel {

		public MySearchPanel(PanelManager manager) {
			super(manager);
		}

		@Override
		protected void buildTable() {

			columns = new TableColumn[6];

			columns[0] = new TableColumn("", .01f);
			columns[0].setMinWidth(25);
			columns[0].setMaxWidth(25);
			columns[0].setAlignment(HorizontalAlignment.CENTER);

			columns[1] = new TableColumn("Scientific Name", .35f);
			columns[1].setMinWidth(75);
			columns[1].setMaxWidth(300);

			columns[2] = new TableColumn("Common Name", .35f);
			columns[2].setMinWidth(75);
			columns[2].setMaxWidth(300);
			columns[2].setAlignment(HorizontalAlignment.LEFT);

			columns[3] = new TableColumn("Level", .15f);
			columns[3].setMinWidth(30);
			columns[3].setMaxWidth(100);

			columns[4] = new TableColumn("Category", .14f);
			columns[4].setMinWidth(30);
			columns[4].setMaxWidth(100);
			columns[4].setAlignment(HorizontalAlignment.LEFT);

			columns[5] = new TableColumn("id", 0);
			columns[5].setHidden(true);

			table.setColumnModel(new TableColumnModel(columns));

			table.setWidth("100%");

			TableColumnModel cm = new TableColumnModel(columns);
			table.setColumnModel(cm);
			table.setSelectionMode(SelectionMode.SINGLE);

			expandableResults.setLayout(new BorderLayout());
			expandableResults.add(table, new BorderLayoutData(LayoutRegion.CENTER));
			expandableResults.add(toolbar, new BorderLayoutData(LayoutRegion.SOUTH, 30));

		}

		public void deselectAll() {
			for (int i = 0; i < table.getItemCount(); i++) {
				TableItem item = table.getItem(i);
				if (((CheckBox) item.getValue(0)).isEnabled()) {
					((CheckBox) item.getValue(0)).setChecked(false);
				}
			}
		}

		@Override
		public void fillTable() {
			SysDebugger.getInstance().println("I am in fill table");
			table.removeAll();
			List<Integer> fetchList = new ArrayList<Integer>();
			for (int i = start; i < start + NUMBER_OF_RESULTS && i < currentResults.getLength(); i++)
				fetchList.add( Integer.valueOf(((NativeElement) currentResults.item(i)).getAttribute("id")) );

			if (fetchList.size() > 0)
				TaxonomyCache.impl.fetchList(fetchList, new GenericCallback<String>() {
					public void onFailure(Throwable arg0) {
						WindowUtils.errorAlert("Failure while fetching search results.");
					}

					public void onSuccess(String arg0) {
						List<Integer> taxaIDs = new ArrayList<Integer>();
						final String[][] x = new String[20][5];
						for (int i = start; i < start + NUMBER_OF_RESULTS && i < currentResults.getLength(); i++) {
							Taxon  currentNode = TaxonomyCache.impl.getTaxon(((NativeElement) currentResults.item(i))
									.getAttribute("id"));
							
							x[i - start][0] = currentNode.getFullName();
							if (currentNode.getCommonNames().size() > 0)
								x[i - start][1] = (new ArrayList<CommonName>(currentNode.getCommonNames()).get(0)).getName().toLowerCase();
							else
								x[i - start][1] = "";
							x[i - start][2] = Taxon .getDisplayableLevel(currentNode.getLevel());
							x[i - start][4] = String.valueOf(currentNode.getId());
							x[i - start][3] = currentNode.getId()+"";
							taxaIDs.add(currentNode.getId());
						}
						if (taxaIDs.size() > 0) {
							AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, taxaIDs), new GenericCallback<String>() {
								public void onFailure(Throwable caught) {
									SysDebugger.getInstance().println("Error while searching in addTaxonPanel");
									SysDebugger.getInstance().println(caught.getMessage());
									WindowUtils.errorAlert("Error while fetching search results - "
											+ "please try again.");
									setButtonsEnabled(false);
								}

								public void onSuccess(String result) {
									SysDebugger.getInstance()
											.println("I am in fetchlist succed in the add taxon panel");

									for (int i = start; i < start + NUMBER_OF_RESULTS && i < currentResults.getLength(); i++) {
										List<Assessment> aData = AssessmentCache.impl.getPublishedAssessmentsForTaxon(
												Integer.valueOf(x[i - start][3]));
										if (aData == null || aData.size() == 0)
											x[i - start][3] = "N/A";
										else
											x[i - start][3] = aData.get(0).getCategoryAbbreviation();

										Object[] y = new Object[6];

										y[1] = x[i - start][0];
										y[2] = x[i - start][1];
										y[3] = x[i - start][2];
										y[4] = x[i - start][3];
										y[5] = x[i - start][4];

										y[0] = new CheckBox();
										if (workingSet.getSpeciesIDs().contains(y[5])) {

											((CheckBox) y[0]).setChecked(true);
											((CheckBox) y[0]).setEnabled(false);

										}

										TableItem item = new TableItem(y);
										table.add(item);

									}

									if (NUMBER_OF_RESULTS > 0) {
										setButtonsEnabled(true);
									} else {
										setButtonsEnabled(false);
									}
								}
							});
						} else {

							int numChecked = 0;
							for (int i = start; i < start + NUMBER_OF_RESULTS && i < currentResults.getLength(); i++) {
								Assessment aData = AssessmentCache.impl.getPublishedAssessment(Integer.valueOf(x[i - start][3]),
										false);
								x[i - start][3] = "N/A";

								Object[] y = new Object[6];

								y[1] = x[i - start][0];
								y[2] = x[i - start][1];
								y[3] = x[i - start][2];
								y[4] = x[i - start][3];
								y[5] = x[i - start][4];

								y[0] = new CheckBox();
								if (workingSet.getSpeciesIDs().contains(y[5])) {

									((CheckBox) y[0]).setChecked(true);
									((CheckBox) y[0]).setEnabled(false);
									numChecked++;
								}

								TableItem item = new TableItem(y);
								table.add(item);

							}

							if (currentResults.getLength() > 0 && currentResults.getLength() != numChecked) {
								setButtonsEnabled(true);
							} else {
								setButtonsEnabled(false);
							}
						}

					}
				});
			else
				setButtonsEnabled(false);

		}

		public int getNumberToDisplay() {
			return NUMBER_OF_RESULTS;
		}

		public String getSelected() {
			StringBuffer taxonToAdd = new StringBuffer();
			List<TableItem> items = table.getItems();
			for (TableItem item : items) {
				if (((CheckBox) item.getValue(0)).isChecked()) {
					String id = (String) item.getValue(5);
					taxonToAdd.append(id + ",");
				}
			}
			if (taxonToAdd.length() > 0) {
				return taxonToAdd.substring(0, taxonToAdd.length() - 1);
			} else
				return "";
		}

		public void selectAll() {
			for (int i = 0; i < table.getItemCount(); i++) {
				TableItem item = table.getItem(i);
				((CheckBox) item.getValue(0)).setChecked(true);
			}
		}

		// OVERRIDE TO CHANGE SELECTION BEHAVIOR
		@Override
		protected void setSelectionModelForTable() {
		}

		public void updateTable() {
			if (workingSet != null) {
				for (int i = 0; i < table.getItemCount(); i++) {
					TableItem item = table.getItem(i);
					if (workingSet.getSpeciesIDs().contains(item.getValue(5))) {
						((CheckBox) item.getValue(0)).setChecked(true);
						((CheckBox) item.getValue(0)).setEnabled(false);
					} else {
						((CheckBox) item.getValue(0)).setChecked(false);
						((CheckBox) item.getValue(0)).setEnabled(true);
					}
				}
			} else {
				table.removeAll();
				searchBox.setText("");
			}
		}

	}

	private HorizontalPanel buttons;
	private HTML instruct;
	private WorkingSet workingSet;
	private PanelManager manager;
	private MySearchPanel searchPanel;
	private BorderLayoutData north;

	private BorderLayoutData center;

	public WorkingSetAddTaxaSearchPanel(PanelManager manager) {
		super();
		this.manager = manager;
		north = new BorderLayoutData(LayoutRegion.NORTH, 70);
		center = new BorderLayoutData(LayoutRegion.CENTER);
		setLayout(new BorderLayout());
		addInstructions();
		searchPanel = new MySearchPanel(manager);
		add(searchPanel, center);

	}

	private void addInstructions() {
		VerticalPanel instructions = new VerticalPanel();

		instruct = new HTML();
		instruct.setWordWrap(true);

		buttons = new HorizontalPanel();
		buttons.setSpacing(2);

		Button selectAll = new Button(" Select All", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				searchPanel.selectAll();
			}
		});

		Button deselectAll = new Button(" Deselect All", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				searchPanel.deselectAll();
			}
		});

		final Button add = new Button("Add");
		add.addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(final ButtonEvent ce) {
				add.setEnabled(false);
				String taxonToAdd = searchPanel.getSelected();

				// NEED TO ADD THINGS TO WORKINGSET
				if (taxonToAdd.length() > 0) {
					String [] ids = taxonToAdd.split(",");
					Collection<Taxon> taxaToAdd = new HashSet<Taxon>();
					for( String curID : ids )
						taxaToAdd.add(TaxonomyCache.impl.getTaxon(curID));
					
					WorkingSetCache.impl.editTaxaInWorkingSet(workingSet, taxaToAdd, null, new GenericCallback<String>() {
					
						@Override
						public void onSuccess(String result) {
							WindowUtils.infoAlert("Taxon successfully added " + "to working set "
									+ workingSet.getName());
							add.setEnabled(true);
							manager.workingSetOptionsPanel.listChanged();
					
						}
					
						@Override
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Error adding taxon to " + "working set " + workingSet.getName());
							add.setEnabled(true);
					
						}
					});
					
				}

				// NOTHING TO ADD
				else {
					add.setEnabled(true);
					WindowUtils.errorAlert("No taxon to add to working set " + workingSet.getWorkingSetName());
				}

			}
		});

		buttons.add(add);
		buttons.add(selectAll);
		buttons.add(deselectAll);

		instructions.add(instruct);
		instructions.add(buttons);
		add(instructions, north);

		setButtonsEnabled(false);

	}

	@Override
	public void refresh() {
		workingSet = WorkingSetCache.impl.getCurrentWorkingSet();
		searchPanel.updateTable();
		if (workingSet != null) {
			instruct.setHTML("<b>Instructions:</b> Search for taxa to add to the " + workingSet.getWorkingSetName()
					+ " working set.  Notice: pressing select all will only select taxa currently in the table.");
		} else {
			instruct.setHTML("<b>Instructions:</b> Please select a working set to add taxa to.");
		}
		setButtonsEnabled(true);
		layout();

	}

	public void setButtonsEnabled(boolean enabled) {
		for (int i = 0; i < buttons.getWidgetCount(); i++) {
			Button button = (Button) buttons.getWidget(i);
			button.setEnabled(enabled);
		}
	}

}
