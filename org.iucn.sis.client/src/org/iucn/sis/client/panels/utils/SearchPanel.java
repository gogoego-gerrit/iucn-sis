package org.iucn.sis.client.panels.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.panels.search.SearchQuery;
import org.iucn.sis.client.panels.search.SearchResultPage;
import org.iucn.sis.client.panels.search.SearchResultPage.TaxonSearchResult;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.TaxonLevel;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

@SuppressWarnings("unchecked")
public class SearchPanel extends LayoutContainer {
	
	public enum SearchEvents {
		BeforeSearch, Select
	}
	
	private final Map<SearchEvents, List<Listener<SearchEvent>>> events;
	
	private final CheckBox common;
	private final CheckBox synonym;
	private final CheckBox sciName;
	private final RadioButton all, species;
	private final CheckBox countryOfOcc;
	private final TextBox countryOfOccText;
	private final CheckBox assessor;
	private final TextBox assessorText;
	private final TextBox searchBox;
	
	private ContentPanel advancedOptions;
	private boolean advancedSearch = false;
	
	protected SearchResultPage resultsPage;
	
	protected HorizontalPanel expandableSearch;
	protected ContentPanel expandableResults;
	protected NativeNodeList currentResults;
	protected Button searchButton;

	public SearchPanel() {
		super(new RowLayout(Orientation.VERTICAL));
		
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
		all = new RadioButton("level", "Search All Taxonomy Levels");
		species = new RadioButton("level", "Search Only Species and Below");
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
		
		events = new HashMap<SearchEvents, List<Listener<SearchEvent>>>();

		build();
	}
	
	protected SearchResultPage createSearchResultsPage(SearchQuery query) {
		return new SearchResultPage(query);
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
		
		expandableResults.setHeading("Search Results");
		expandableResults.removeAll();
	}

	protected void build() {
		final RowData fillBoth = new RowData(1, 1);
		final RowData fillHorizontal = new RowData(1, 25);

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
		
		all.setValue(false);
		species.setValue(true);
		
		HorizontalPanel level = new HorizontalPanel();
		level.add(species);
		level.add(all);

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
		vp.add(level);
		
		vp.add(new HTML("<hr><b>Assessment Filters:</b>"));
		vp.add(hp1);
		vp.add(hp2);
		advancedOptions.add(vp);

		// if(advancedSearch)/

	}

	private void buildResultsPanel() {
		expandableResults.setHeading("Search Results");
		expandableResults.setLayout(new FillLayout());
		expandableResults.setLayoutOnChange(true);
	}

	private void buildSearchPanel() {
		expandableSearch.add(searchBox);
		searchBox.setWidth("100%");
		searchButton = new Button("Search", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				
				if (searchBox.getText().trim().equalsIgnoreCase("")) {
					WindowUtils.errorAlert("Please enter search terms.");
					return;
				}else if(searchBox.getText().trim().length() < 3){
					WindowUtils.errorAlert("Please enter at least 3 Characters to search.");
					return;			
				}else	
					search(searchBox.getText());
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

	@Override
	protected void onAttach() {
		super.onAttach();
		advancedOptions.setExpanded(false);
	}
	
	private boolean onBeforeSearch(String value) {
		return execute(SearchEvents.BeforeSearch, new SearchEvent<String>(this, value));
	}

	private void search(String searchQuery) {
		search(searchToXML(searchQuery));
	}
	
	public void search(SearchQuery searchQuery) {
		if (onBeforeSearch(searchQuery.getQuery())) {
			searchButton.setEnabled(true);
			return;
		}
		
		searchButton.setEnabled(false);
		
		expandableResults.removeAll();
		
		resultsPage = createSearchResultsPage(searchQuery);
		resultsPage.addListener(Events.Select, new Listener<SearchPanel.SearchEvent<TaxonSearchResult>>() {
			public void handleEvent(SearchPanel.SearchEvent<TaxonSearchResult> be) {
				onSearchSelect(be.getValue().getTaxonID());
			}
		});
		resultsPage.draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				searchButton.setEnabled(true);
				advancedOptions.collapse();
				expandableResults.setHeading("Search Results [" + resultsPage.getLength() + " results]");
				expandableResults.removeAll();
				expandableResults.add(resultsPage);
			}
		});
	}

	private SearchQuery searchToXML(String searchQuery) {
		final SearchQuery query = new SearchQuery(searchQuery);
		query.setCommonName(common.getValue());
		query.setSynonym(synonym.getValue());
		query.setScientificName(sciName.getValue());
		if (all.getValue())
			query.setLevel(TaxonLevel.KINGDOM);
		else
			query.setLevel(TaxonLevel.SPECIES);
		if (countryOfOcc.getValue())
			query.setCountryOfOccurrence(countryOfOccText.getValue());
		if (assessor.getValue())
			query.setAssessor(assessor.getText());
		
		return query;
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
