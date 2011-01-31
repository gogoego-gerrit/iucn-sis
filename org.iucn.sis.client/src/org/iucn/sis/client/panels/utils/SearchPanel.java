package org.iucn.sis.client.panels.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.utils.AssessmentFormatter;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.ContainerEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

@SuppressWarnings({"deprecation", "unchecked"})
public class SearchPanel extends LayoutContainer {
	
	public enum SearchEvents {
		BeforeSearch, Select
	}
	
	protected final int NUMBER_OF_RESULTS = 20;
	
	protected final Table table;
	
	private final CheckBox common;
	private final CheckBox synonym;
	private final CheckBox sciName;
	private final CheckBox countryOfOcc;
	private final TextBox countryOfOccText;
	private final CheckBox assessor;
	private final TextBox assessorText;
	private final TextBox searchBox;
	private final Map<SearchEvents, List<Listener<SearchEvent>>> events;
	
	protected HorizontalPanel expandableSearch;
	protected ContentPanel expandableResults;
	protected NativeNodeList currentResults;
	protected int start = 0;
	protected Button searchButton;
	
	private ContentPanel advancedOptions;
	private boolean advancedSearch = false;
	private Button next;
	private Button prev;	

	public SearchPanel() {
		// BUILDING ALL FINAL STUFFS
		expandableResults = new ContentPanel();
		expandableResults.setStyleName("x-panel");
		expandableResults.setCollapsible(true);

		expandableSearch = new HorizontalPanel();

		advancedOptions = new ContentPanel();
		advancedOptions.setCollapsible(true);
		advancedOptions.setStyleName("x-panel");

		common = new CheckBox();
		synonym = new CheckBox();
		sciName = new CheckBox();
		countryOfOcc = new CheckBox();
		countryOfOccText = new TextBox();
		assessor = new CheckBox();
		assessorText = new TextBox();
		
		searchBox = new TextBox();
		searchBox.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				char keyCode = event.getCharCode();
				if (keyCode == KeyCodes.KEY_ENTER)
					searchButton.fireEvent(Events.Select);
			}
		});
		
		table = new Table();
		table.setBulkRender(false);
		
		events = new HashMap<SearchEvents, List<Listener<SearchEvent>>>();

		build();
	}
	
	public void addBeforeSearchListener(Listener<SearchEvent<String>> listener) {
		addListener(SearchEvents.BeforeSearch, listener);
	}
	
	public void addSearchSelectionListener(Listener<SearchEvent<Integer>> listener) {
		addListener(SearchEvents.Select, listener);
	}
	
	protected void addListener(SearchEvents eventType, Listener listener) {
		List<Listener<SearchEvent>> list = events.get(eventType);
		if (list == null)
			list = new ArrayList<Listener<SearchEvent>>();
		list.add(listener);
		
		events.put(eventType, list);
	}
	
	public void resetSearchBox() {
		searchBox.setText("");
	}
	
	public void setSearchText(String text, boolean openAdvanced) {
		searchBox.setText(text);
		if (openAdvanced)
			advancedOptions.expand();
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

		common.setValue(true);
		common.setText("Search Common Names");
		
		synonym.setValue(true);
		synonym.setText("Search Synonyms");

		sciName.setText("Search Scientific Names");
		sciName.setValue(true);

		HorizontalPanel hp1 = new HorizontalPanel();
		countryOfOcc.setText("Country of Occurrence");
		hp1.add(countryOfOcc);
		hp1.add(countryOfOccText);

		HorizontalPanel hp2 = new HorizontalPanel();
		assessor.setText("Assessor");
		hp2.add(assessor);
		hp2.add(assessorText);

		common.setWidth("100px");
		synonym.setWidth("100px");
		sciName.setWidth("100px");

		// panel.add(showAdvanced);
		vp.add(new HTML("<b>Taxon Filters:</b>"));
		vp.add(common);
		vp.add(synonym);
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
					search(searchBox.getText());
				}
			}
		});

		expandableSearch.add(searchButton);
		
		final HTML showAdvanced = new HTML("Show Advanced Search");
		showAdvanced.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
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
	
	protected TableColumnModel getColumnModel() {
		TableColumn[] columns = new TableColumn[7];

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

		return new TableColumnModel(columns);
	}
	
	protected TableItem buildTableItem(Taxon taxon, Object[] row) {
		Set<Assessment> assessmentList = AssessmentCache.impl.getPublishedAssessmentsForTaxon(taxon.getId());
		
		row[0] = taxon.getFullName();
		if (taxon.getCommonNames().size() > 0)
			row[1] = (new ArrayList<CommonName>(taxon.getCommonNames()).get(0)).getName().toLowerCase();
		else
			row[1] = "";
		row[2] = Taxon.getDisplayableLevel(taxon.getLevel());
		row[4] = String.valueOf(taxon.getId());
		
		if (!assessmentList.isEmpty()) {
			Assessment aData = assessmentList.iterator().next();
			row[3] = AssessmentFormatter.getProperCategoryAbbreviation(aData);
		} else
			row[3] = "N/A";
										
		if (taxon.getFootprint().length >= 5) {
			row[5] = taxon.getFootprint()[4];
		}
		else
			row[5] = "N/A";
		if (taxon.getFootprint().length >= 6) {
			row[6] = taxon.getFootprint()[5];
		}
		else
			row[5] = "N/A";
		
		return new TableItem(row); 
	}

	protected void buildTable() {
		table.setColumnModel(getColumnModel());

		expandableResults.setLayout(new BorderLayout());
		expandableResults.add(table, new BorderLayoutData(LayoutRegion.CENTER));
		expandableResults.add(buildToolbar(), new BorderLayoutData(LayoutRegion.SOUTH, 30));
	}

	protected ToolBar buildToolbar() {
		ToolBar toolbar = new ToolBar();

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
				fillTable(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
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
				fillTable(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
			}
		});

		toolbar.add(prev);
		toolbar.add(next);

		return toolbar;
	}

	public void fillTable(final DrawsLazily.DoneDrawingCallback callback) {
		table.removeAll();
		final List<Integer> fetchList = new ArrayList<Integer>();
		for (int i = start; i < start + NUMBER_OF_RESULTS && i < currentResults.getLength(); i++)
			fetchList.add(Integer.valueOf(((NativeElement) currentResults.item(i)).getAttribute("id")));

		if (fetchList.size() > 0)
			TaxonomyCache.impl.fetchList(fetchList, new GenericCallback<String>() {
				public void onFailure(Throwable arg0) {
					WindowUtils.hideLoadingAlert();
					WindowUtils.errorAlert("Error loading results. Inconsistency in index table.");
					
					callback.isDrawn();
				}
				public void onSuccess(String arg0) {	
					AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, fetchList),
							new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							onSuccess(null);
						}
						
						public void onSuccess(String result) {
							final Object[][] x = new Object[20][table.getColumnCount()];
							for (int i = start; i < start + NUMBER_OF_RESULTS && i < currentResults.getLength(); i++) {
								Taxon currentNode = TaxonomyCache.impl.getTaxon(((NativeElement) currentResults.item(i))
										.getAttribute("id"));

								try {
									table.add(buildTableItem(currentNode, x[i - start]));
								} catch (Throwable e) {
									e.printStackTrace();
								}
							}
							callback.isDrawn();
						}
					});
				}
			});
		else
			callback.isDrawn();
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		advancedOptions.setExpanded(false);
	}
	
	private boolean onBeforeSearch(String value) {
		return execute(SearchEvents.BeforeSearch, new SearchEvent<String>(this, value));
	}

	public void search(String searchQuery) {
		if (onBeforeSearch(searchQuery)) {
			searchButton.setEnabled(true);
			return;
		}
		
		start = 0;
		searchButton.setEnabled(false);

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

				fillTable(new DrawsLazily.DoneDrawingCallback() {
					public void isDrawn() {
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
		});
	}

	protected String searchToXML(String searchQuery) {
		String xml = "<search>\r\n";
		if (common.isChecked())
			xml += "<commonName><![CDATA[" + searchQuery + "]]></commonName>\r\n";
		if (synonym.isChecked())
			xml += "<synonym><![CDATA[" + searchQuery + "]]></synonym>\r\n";
		if (sciName.isChecked())
			xml += "<sciName><![CDATA[" + searchQuery + "]]></sciName>\r\n";
		if (countryOfOcc.isChecked())
			xml += "<country><![CDATA[" + countryOfOccText.getText() + "]]></country>\r\n";
		if (assessor.isChecked())
			xml += "<assessor><![CDATA[" + assessorText.getText() + "]]></assessor>\r\n";
		xml += "</search>";
		return xml;
	}
	
	private boolean onSearchSelect(Integer taxonID) {
		return execute(SearchEvents.Select, new SearchEvent<Integer>(this, taxonID));
	}
	
	private boolean execute(SearchEvents eventType, SearchEvent event) {
		boolean cancelled = false;
		if (events.containsKey(eventType)) {
			for (Listener<SearchEvent> listener : events.get(eventType)) {
				try {
					listener.handleEvent(event);
				} catch (Throwable e) {
					Debug.println("Failed to run {0} search listener: {1}", SearchEvents.BeforeSearch, e);
				}
				cancelled |= event.isCancelled();
			}
		}
		return cancelled;
	}

	private void setSelectionModelForTable() {
		table.setSelectionModel(new TableSelectionModel(SelectionMode.SINGLE) {
			protected void onMouseDown(ContainerEvent ce) {
				super.onMouseDown(ce);
				if (table.getSelectedItem() != null) {
					onSearchSelect(getTaxonID(table.getSelectedItem()));
				}
			}
		});
	}
	
	protected Integer getTaxonID(TableItem item) {
		return Integer.valueOf((String)item.getValue(4));
	}
	
	public static class SearchEvent<T> extends BaseEvent {
		
		private final T value;
		
		public SearchEvent(Object source, T data) {
			super(source);
			this.value = data;
		}
		
		public T getValue() {
			return value;
		}
		
	}

}
