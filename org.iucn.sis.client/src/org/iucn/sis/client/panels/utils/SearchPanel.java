package org.iucn.sis.client.panels.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.ContainerEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.table.TableItem;
import com.extjs.gxt.ui.client.widget.table.TableSelectionModel;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

public class SearchPanel extends LayoutContainer {

	protected HorizontalPanel expandableSearch;
	protected ContentPanel expandableResults;
	private ContentPanel advancedOptions;
	private PanelManager panelManager = null;
	private boolean advancedSearch = false;
	private final CheckBox common;
	private final CheckBox sciName;
	private final CheckBox countryOfOcc;
	private final TextBox countryOfOccText;
	private final CheckBox assessor;
	private final TextBox assessorText;
	protected final int NUMBER_OF_RESULTS = 20;
	protected int start = 0;
	protected NativeNodeList currentResults;
	protected final Table table;
	protected TableColumn[] columns;
	protected ToolBar toolbar;
	private Button next;
	private Button prev;
	protected final TextBox searchBox;
	protected Button searchButton;

	public SearchPanel(PanelManager manager) {
		panelManager = manager;

		// BUILDING ALL FINAL STUFFS
		expandableResults = new ContentPanel();
		expandableResults.setStyleName("x-panel");
		expandableResults.setCollapsible(true);

		expandableSearch = new HorizontalPanel();

		advancedOptions = new ContentPanel();
		advancedOptions.setCollapsible(true);
		advancedOptions.setStyleName("x-panel");

		common = new CheckBox();
		sciName = new CheckBox();
		countryOfOcc = new CheckBox();
		countryOfOccText = new TextBox();
		assessor = new CheckBox();
		assessorText = new TextBox();
		table = new Table();
		table.setBulkRender(false);
		searchBox = new TextBox();
		searchBox.addKeyboardListener(new KeyboardListenerAdapter() {
			@Override
			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if (keyCode == KEY_ENTER)
					searchButton.fireEvent(Events.Select);
			}
		});

		// setLayoutOnChange(true);
		build();

	}

	protected void build() {
		final RowData fillBoth = new RowData(1, 1);
		final RowData fillHorizontal = new RowData(1, 25);
		RowLayout layout = new RowLayout(Orientation.VERTICAL);
		setLayout(layout);

		Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
			public void handleEvent(ComponentEvent be) {
				if (be.getType() == Events.Collapse) {
					setLayoutData(be.getComponent(), fillHorizontal);
				} else {
					setLayoutData(be.getComponent(), fillBoth);
				}

				layout();
			}
		};

		buildToolbar();
		buildTable();
		buildSearchPanel();
		buildAdvancedOptionsPanel();
		buildResultsPanel();

		advancedOptions.addListener(Events.Expand, listener);
		advancedOptions.addListener(Events.Collapse, listener);
		expandableResults.addListener(Events.Expand, listener);
		expandableResults.addListener(Events.Collapse, listener);

		add(expandableSearch, fillHorizontal);
		add(advancedOptions, fillHorizontal);
		add(expandableResults, fillBoth);

		advancedOptions.collapse();
		expandableResults.expand();

		layout();
	}

	private void buildAdvancedOptionsPanel() {
		advancedOptions.setHeading("Advanced Search Options");
		VerticalPanel vp = new VerticalPanel();
		advancedOptions.setLayout(new FillLayout());

		common.setChecked(true);
		common.setText("Search Common Names");

		sciName.setText("Search Scientific Names");
		sciName.setChecked(true);

		HorizontalPanel hp1 = new HorizontalPanel();
		countryOfOcc.setText("Country of Occurrence");
		hp1.add(countryOfOcc);
		hp1.add(countryOfOccText);

		HorizontalPanel hp2 = new HorizontalPanel();
		assessor.setText("Assessor");
		hp2.add(assessor);
		hp2.add(assessorText);

		common.setWidth("100px");
		sciName.setWidth("100px");

		// panel.add(showAdvanced);
		vp.add(new HTML("<b>Taxon Filters:</b>"));
		vp.add(common);
		vp.add(sciName);
		vp.add(new HTML("<hr><b>Assessment Filters:</b>"));
		vp.add(hp1);
		vp.add(hp2);
		advancedOptions.add(vp);

		// if(advancedSearch)/

	}

	private void buildResultsPanel() {
		expandableResults.setHeading("Search Results");

	}

	private void buildSearchPanel() {

		expandableSearch.add(searchBox);
		searchBox.setWidth("100%");
		searchButton = new Button("Search", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (!searchBox.getText().trim().equalsIgnoreCase("")) {
					searchButton.setEnabled(false);
					start = 0;
					search(searchBox.getText());
				}
			}
		});

		expandableSearch.add(searchButton);
		final HTML showAdvanced = new HTML("Show Advanced Search");
		showAdvanced.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {

				if (!advancedSearch) {
					advancedSearch = true;
					showAdvanced.setText("Hide Advanced Search");
					advancedOptions.setVisible(true);
				} else {
					advancedSearch = false;
					showAdvanced.setText("Show Advanced Search");
				}

				layout();

			}
		});

	}

	protected void buildTable() {

		columns = new TableColumn[7];

		columns[0] = new TableColumn("Scientific Name", .27f);
		columns[0].setMinWidth(75);
		columns[0].setMaxWidth(300);

		columns[1] = new TableColumn("Common Name", .27f);
		columns[1].setMaxWidth(300);
		columns[1].setMinWidth(75);
		columns[1].setAlignment(HorizontalAlignment.LEFT);

		columns[2] = new TableColumn("Level", .1f);
		columns[2].setMaxWidth(50);
		columns[2].setMaxWidth(100);

		columns[3] = new TableColumn("Category", .1f);
		columns[3].setMaxWidth(50);
		columns[3].setMaxWidth(50);
		columns[3].setAlignment(HorizontalAlignment.RIGHT);

		columns[4] = new TableColumn("id", 0);
		columns[4].setHidden(true);

		columns[5] = new TableColumn("Family", .13f);
		columns[5].setMinWidth(75);
		columns[5].setMaxWidth(100);
		columns[5].setAlignment(HorizontalAlignment.RIGHT);

		columns[6] = new TableColumn("Genus", .13f);
		columns[6].setMinWidth(75);
		columns[6].setMaxWidth(100);
		columns[6].setAlignment(HorizontalAlignment.RIGHT);

		TableColumnModel cm = new TableColumnModel(columns);
		table.setColumnModel(cm);

		expandableResults.setLayout(new BorderLayout());
		expandableResults.add(table, new BorderLayoutData(LayoutRegion.CENTER));
		expandableResults.add(toolbar, new BorderLayoutData(LayoutRegion.SOUTH, 30));

	}

	private void buildToolbar() {
		toolbar = new ToolBar();

		next = new Button();
		next.setIconStyle("icon-next");
		next.setText("Next " + NUMBER_OF_RESULTS + " Results");
		next.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				start += NUMBER_OF_RESULTS;
				if (start + NUMBER_OF_RESULTS > currentResults.getLength())
					next.setEnabled(false);
				if (start > 0)
					prev.setEnabled(true);
				fillTable();
			}
		});

		prev = new Button();
		prev.setIconStyle("icon-previous");
		prev.setText("Previous " + NUMBER_OF_RESULTS + " Results");
		prev.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				start -= NUMBER_OF_RESULTS;
				if (start < 0)
					start = 0;
				if (start == 0)
					prev.setEnabled(false);
				if (start + NUMBER_OF_RESULTS < currentResults.getLength())
					next.setEnabled(true);
				fillTable();

			}
		});

		toolbar.add(prev);
		toolbar.add(next);

	}

	public void fillTable() {
		table.removeAll();
		final List<Integer> fetchList = new ArrayList<Integer>();
		for (int i = start; i < start + NUMBER_OF_RESULTS && i < currentResults.getLength(); i++)
			fetchList.add(Integer.valueOf(((NativeElement) currentResults.item(i)).getAttribute("id")));

		if (fetchList.size() > 0)
			TaxonomyCache.impl.fetchList(fetchList, new GenericCallback<String>() {
				public void onFailure(Throwable arg0) {
					WindowUtils.hideLoadingAlert();
					WindowUtils.errorAlert("Error loading results. Inconsistency in index table.");
				}

				public void onSuccess(String arg0) {
					
					AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, fetchList),
							new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							drawResults();
						}
						
						public void onSuccess(String result) {
							drawResults();
						}

						private void drawResults() {
							final String[][] x = new String[20][7];
							for (int i = start; i < start + NUMBER_OF_RESULTS && i < currentResults.getLength(); i++) {
								Taxon currentNode = TaxonomyCache.impl.getTaxon(((NativeElement) currentResults.item(i))
										.getAttribute("id"));
								List<Assessment> assessmentList = AssessmentCache.impl.getPublishedAssessmentsForTaxon(currentNode.getId());
								
								x[i - start][0] = currentNode.getFullName();
								if (currentNode.getCommonNames().size() > 0)
									x[i - start][1] = (new ArrayList<CommonName>(currentNode.getCommonNames()).get(0)).getName().toLowerCase();
								else
									x[i - start][1] = "";
								x[i - start][2] = Taxon.getDisplayableLevel(currentNode.getLevel());
								x[i - start][4] = String.valueOf(currentNode.getId());
								
								if (assessmentList.size() > 0) {
									Assessment aData = assessmentList.get(0);
									x[i - start][3] = aData.getProperCategoryAbbreviation();
								} else
									x[i - start][3] = "N/A";
																
								if (currentNode.getFootprint().length >= 5) {
									x[i - start][5] = currentNode.getFootprint()[4];
								}
								else
									x[i - start][5] = "N/A";
								if (currentNode.getFootprint().length >= 6) {
									x[i - start][6] = currentNode.getFootprint()[5];
								}
								else
									x[i - start][5] = "N/A";
								
								TableItem tItem = new TableItem(x[i - start]);
								table.add(tItem);

							}
						}
					});
				}
			});

	}

	@Override
	protected void onAttach() {
		// TODO Auto-generated method stub
		super.onAttach();
		advancedOptions.setExpanded(false);
	}

	private void search(String searchQuery) {
		String searchOptions = "";

		if (searchQuery.matches("^[0-9]+$")) {
			SysDebugger.getInstance().println("We gots a number to jump to.....");
			panelManager.taxonomicSummaryPanel.update(Integer.valueOf(searchQuery));
			searchButton.setEnabled(true);
			WindowManager.get().hideAll();
			return;
		}

		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();

		ndoc.post(UriBase.getInstance().getSISBase() +"/search", searchToXML(searchQuery), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.hideLoadingAlert();
				WindowUtils.errorAlert("Error loading results. Inconsistency in index table.");
			}

			public void onSuccess(String result) {

				currentResults = ndoc.getDocumentElement().getElementsByTagName("result");
				if (currentResults.getLength() > NUMBER_OF_RESULTS)
					next.setVisible(true);
				expandableResults.setHeading("Search Results [" + currentResults.getLength() + " results]");

				fillTable();

				if (currentResults.getLength() > 0) {
					next.setVisible(true);
					next.setEnabled(true);
					if (!(currentResults.getLength() > NUMBER_OF_RESULTS))
						next.setEnabled(false);
					prev.setVisible(true);
					prev.setEnabled(false);
				}

				else {
					// expandableResults.add(new HTML("No Results."));
					prev.setEnabled(false);
					next.setEnabled(false);
				}

				setSelectionModelForTable();
				searchButton.setEnabled(true);
			}
		});
	}

	protected String searchToXML(String searchQuery) {
		String xml = "<search>\r\n";
		if (common.isChecked())
			xml += "<commonName>" + searchQuery + "</commonName>\r\n";
		if (sciName.isChecked())
			xml += "<sciName>" + searchQuery + "</sciName>\r\n";
		if (countryOfOcc.isChecked())
			xml += "<country>" + countryOfOccText.getText() + "</country>\r\n";
		if (assessor.isChecked())
			xml += "<assessor>" + assessorText.getText() + "</assessor>\r\n";
		xml += "</search>";
		return xml;
	}

	protected void setSelectionModelForTable() {
		table.setSelectionModel(new TableSelectionModel(SelectionMode.SINGLE) {
						
			@Override
			protected void onMouseDown(ContainerEvent ce) {
				super.onMouseDown(ce);
				if (table.getSelectedItem() != null) {
					TaxonomyCache.impl.fetchTaxon(Integer.valueOf((String)table.getSelectedItem().getValues()[4]), true,
							new GenericCallback<Taxon >() {
								public void onFailure(Throwable caught) {
								}

								public void onSuccess(Taxon  result) {
									panelManager.taxonomicSummaryPanel
											.update(Integer.valueOf((String)table.getSelectedItem().getValues()[4]));
									WindowManager.get().hideAll();
								}
							});
				}
				
			}
		});
	}

}
