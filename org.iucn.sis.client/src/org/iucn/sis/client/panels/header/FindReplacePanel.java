package org.iucn.sis.client.panels.header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.FindResultCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.panels.filters.AssessmentFilterPanel;
import org.iucn.sis.shared.api.findreplace.FindReplaceData;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.iucn.sis.shared.api.utils.XMLUtils;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class FindReplacePanel extends LayoutContainer {

	public final static int NUMBEROFRESULTSSHOWED = 20;
	private final static String ALL = "all";

	private TextBox findBox;
	private TextBox replaceBox;
//	private ListBox fileBox;
	private ListBox selectorBox;
	private ListBox whereToSearchWithFileBox;
	private HTML selectorLabel;
	private VerticalPanel resultsTable;
	private Button searchButton;
	private HTML resultTextHTML;
	private CheckBox caseInsensitive;
	private CheckBox entireWord;
	private CheckBox regex;
	private AssessmentFilterPanel assFilterPanel;

	private HTML name;
	private HTML resultCount;
	private HTML type;
	private HTML description;
	private HTML currentResult;
	private Button replaceAllButton;
	private Button replaceButton;
	private Button skipButton;
	private Button skipAllInFileButton;

	private final int NAMECOLUMNINDEX = 0;
	private final int TYPECOLUMNINDEX = 1;
	private final int TEXTCOLUMNINDEX = 2;

	private LayoutContainer centerPanel;
	private VerticalPanel westPanel;
	private LayoutContainer southPanel;
	private BorderLayoutData centerData;
	private BorderLayoutData westData;
	private BorderLayoutData southData;

	private VerticalPanel textHP;
	private VerticalPanel filesHP;
	private VerticalPanel selectorHP;
	private VerticalPanel searchResults;

	private ArrayList<FindReplaceData> results;
	private Map<Integer, WorkingSet> workingSets;

	private String stringToFind;
//	private int searchType;
//	private String ids;
	private boolean isReplacePanelEmpty;
	private String options;
	private boolean build;
	private String criteria;
	
	private String searchedWorkingSetID;
	private AssessmentFilter filter;

	public FindReplacePanel() {
		super();
		results = new ArrayList();
		workingSets = new HashMap();
		isReplacePanelEmpty = true;
		setLayout(new BorderLayout());
		build = false;
	}

	private void afterSkipReplace() {

		if (FindResultCache.impl.doneWithCurrent()) {
			final FindReplaceData currentData = FindResultCache.impl.getCurrentFindResultData();
			if (currentData != null) {
				currentData.setFinished(true);
				results.remove(currentData);
				FindResultCache.impl.removeData(currentData);
				removeRow(0);
				if (results.size() > 0) {
					updateResults();
				} else {
					clearReplacePanel();
					updateResults();
				}
				if (currentData.needsReplacing()) {
					FindResultCache.impl.replace(currentData, new GenericCallback<String>() {

						public void onFailure(Throwable arg0) {
							WindowUtils.errorAlert("Error", "Error making replacements in " + currentData.getAssessmentName()
									+ " please try your find and replace again.");
						}

						public void onSuccess(String arg0) {
						}

					}, options, criteria);
				}
			}
		} else {
			
//			String description, 
//			String text, 
//			String originalWord, 
//			int currentNumberInFile,
//			int currentIndex
			
//			updateReplacePanelInfo("description", "text", "originalWord", 1, 1);
			
			updateReplacePanelInfo(FindResultCache.impl.getCurrentFindResultData().getCurrentDescription(),
					FindResultCache.impl.getCurrentFindResultData().getCurrentSentence().value,
					FindResultCache.impl.getCurrentFindResultData().getCurrentSentence().getCurrentText(),
					FindResultCache.impl.getCurrentFindResultData().getCurrentSentenceIndex(), 
					FindResultCache.impl.getCurrentFindResultData().getCurrentSentence().getOldWordIndex());
		}

	}

	private void build() {
		centerPanel = new LayoutContainer();
		westPanel = new VerticalPanel();
		southPanel = new LayoutContainer();
		centerData = new BorderLayoutData(LayoutRegion.CENTER, .5f);
		westData = new BorderLayoutData(LayoutRegion.WEST, .5f);
		southData = new BorderLayoutData(LayoutRegion.SOUTH, .35f);

		buildSearchPanel();
		buildResultsTable();
		buildResult();

		add(centerPanel, centerData);
		add(westPanel, westData);
		add(southPanel, southData);
		layout();
	}

	private void buildHeader() {
		HTML name = new HTML("File Name");
		HTML type = new HTML("Type");
		HTML text = new HTML("Results Count");
		name.addStyleName("SIS_taxonDescriptionHeader");
		type.addStyleName("SIS_taxonDescriptionHeader");
		text.addStyleName("SIS_taxonDescriptionHeader");
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(name);
		hp.add(type);
		hp.add(text);
		hp.setCellWidth(name, "60%");
		hp.setCellWidth(type, "20%");
		hp.setCellWidth(text, "20%");
		hp.setHeight("20px");
		resultsTable.add(hp);
		resultsTable.setCellVerticalAlignment(hp, HasVerticalAlignment.ALIGN_TOP);
		resultsTable.setCellHeight(hp, "20px");
		hp.setWidth("100%");
	}

	private void buildReplacePanel() {
		if (results.size() > 0) {
			FindReplaceData data = results.get(0);
			FindResultCache.impl.setCurrentFindResult(data);
			setReplacePanelInfo(data.getAssessmentName(), 
					data.getAssessmentType(), 
					data.getCurrentSentence().value, 
					data.getCurrentSentence().getCurrentText(), 
					data.getNumberOfResults(), data.getCurrentDescription(),
					data.getCurrentSentenceIndex(), 
					data.getCurrentSentence().getOldWordIndex());
		} else {
			clearReplacePanel();
		}
	}

	private void buildResult() {

		westPanel.addStyleName("lower-spacing");

		HTML html = new HTML("Name:&nbsp&nbsp");
		html.addStyleName("bold");
		html.addStyleName("color-dark-blue");
		name = new HTML();
		HorizontalPanel hpWrapper = new HorizontalPanel();
		hpWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		HorizontalPanel hp = new HorizontalPanel();
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp.add(html);
		hp.add(name);
		hpWrapper.add(hp);
		westPanel.add(hpWrapper);

		html = new HTML("File Type:&nbsp&nbsp");
		html.addStyleName("bold");
		html.addStyleName("color-dark-blue");
		type = new HTML();
		hpWrapper = new HorizontalPanel();
		hpWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp = new HorizontalPanel();
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp.add(html);
		hp.add(type);
		hpWrapper.add(hp);
		westPanel.add(hpWrapper);

		html = new HTML("Result Count In File:&nbsp&nbsp");
		html.addStyleName("bold");
		html.addStyleName("color-dark-blue");
		resultCount = new HTML();
		hp = new HorizontalPanel();
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp.add(html);
		hp.add(resultCount);

		html = new HTML("&nbsp&nbsp&nbsp&nbsp Current Result :&nbsp&nbsp");
		html.addStyleName("bold");
		html.addStyleName("color-dark-blue");
		currentResult = new HTML();
		hpWrapper = new HorizontalPanel();
		hpWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp.add(html);
		hp.add(currentResult);
		hpWrapper.add(hp);
		westPanel.add(hpWrapper);

		html = new HTML("Field:&nbsp&nbsp");
		html.addStyleName("bold");
		html.addStyleName("color-dark-blue");
		description = new HTML();
		hpWrapper = new HorizontalPanel();
		hpWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp = new HorizontalPanel();
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp.add(html);
		hp.add(description);
		hpWrapper.add(hp);
		westPanel.add(hpWrapper);

		html = new HTML("Text:");
		resultTextHTML = new HTML();
		resultTextHTML.addStyleName("whiteBackground");
		resultTextHTML.setHeight("120px");
		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setWidget(resultTextHTML);
		scrollPanel.addStyleName("whiteBackground");
		VerticalPanel vp = new VerticalPanel();
		vp.add(html);
		vp.add(scrollPanel);
		scrollPanel.setSize("100%", "100%");
		vp.setCellHeight(html, "16px");
		resultTextHTML.setWidth("100%");

		westPanel.add(vp);
		vp.setWidth("100%");

		html = new HTML("Replace with:");
		replaceBox = new TextBox();
		vp = new VerticalPanel();
		vp.add(html);
		vp.add(replaceBox);
		replaceBox.setWidth("100%");
		vp.setCellHorizontalAlignment(html, HasHorizontalAlignment.ALIGN_LEFT);
		westPanel.add(vp);
		vp.setWidth("100%");

		ButtonBar bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.LEFT);
		skipButton = new Button("Skip", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				skip();
			}
		});

		skipAllInFileButton = new Button("Skip all in file", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				skipAll();
			}
		});

		replaceButton = new Button("Replace", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				replace();
			}
		});

		replaceAllButton = new Button("Replace all in file", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				replaceAll();
			}

		});

		bar.add(skipButton);
		bar.add(skipAllInFileButton);
		bar.add(replaceButton);
		bar.add(replaceAllButton);
		westPanel.add(bar);
	}

	private void buildResultsTable() {

		resultsTable = new VerticalPanel();
		resultsTable.setWidth("100%");
		resultsTable.addStyleName("SIS_tableBackground");
		resultsTable.addStyleName("SIS_taxonDescriptionTable");
		buildHeader();
		ScrollPanel scroll = new ScrollPanel(resultsTable);
		southPanel.setLayout(new FillLayout());
		southPanel.add(scroll);

	}

	private void buildRow(FindReplaceData data) {

		HTML name = new HTML(data.getAssessmentName());
		HTML type = new HTML(data.getAssessmentType());
		HTML text = new HTML(data.getNumberOfResults() + "");
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(name);
		hp.add(type);
		hp.add(text);
		hp.setCellWidth(name, "60%");
		hp.setCellWidth(type, "20%");
		hp.setCellWidth(text, "20%");
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		hp.setWidth("100%");
		resultsTable.add(hp);
		resultsTable.setCellVerticalAlignment(hp, HasVerticalAlignment.ALIGN_TOP);

	}

	private void buildSearchPanel() {
		findBox = new TextBox();
		textHP = new VerticalPanel();
		HTML html = new HTML("Find text:");
		textHP.add(html);
		textHP.add(findBox);
		findBox.setWidth("100%");
		textHP.setCellHorizontalAlignment(html, HasHorizontalAlignment.ALIGN_LEFT);
		filesHP = new VerticalPanel();

		assFilterPanel = new AssessmentFilterPanel(new AssessmentFilter(), false, true, true, true);
		filesHP.add(assFilterPanel);
		selectorLabel = new HTML("Search in Working Set:");
		selectorBox = new ListBox(false);
		selectorHP = new VerticalPanel();
		selectorHP.add(selectorLabel);
		selectorHP.add(selectorBox);
		selectorBox.setWidth("100%");
		setSelectorBox();
		selectorHP.setCellHorizontalAlignment(selectorLabel, HasHorizontalAlignment.ALIGN_LEFT);
		selectorBox.addChangeListener(new ChangeListener() {
		
			public void onChange(Widget sender) {
				setAssessmentFilter();
			}
		});

		searchButton = new Button("Find", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				search();
			}
		});

		
		
		
		html = new HTML("Field to search: ");
		VerticalPanel whereToSearchWithHP = new VerticalPanel();
		whereToSearchWithFileBox = new ListBox(false);
		whereToSearchWithFileBox.setWidth("100%");
		whereToSearchWithFileBox.addItem("All criteria", ALL);
		whereToSearchWithFileBox.addItem(CanonicalNames.RedListRationale, CanonicalNames.RedListRationale);
		whereToSearchWithFileBox.addItem(CanonicalNames.ConservationActionsDocumentation,
				CanonicalNames.ConservationActionsDocumentation);
		whereToSearchWithFileBox.addItem(CanonicalNames.HabitatDocumentation, CanonicalNames.HabitatDocumentation);
		whereToSearchWithFileBox
				.addItem(CanonicalNames.PopulationDocumentation, CanonicalNames.PopulationDocumentation);
		whereToSearchWithFileBox.addItem(CanonicalNames.RangeDocumentation, CanonicalNames.RangeDocumentation);
		whereToSearchWithFileBox.addItem(CanonicalNames.ThreatsDocumentation, CanonicalNames.ThreatsDocumentation);
		whereToSearchWithFileBox.addItem(CanonicalNames.UseTradeDocumentation, CanonicalNames.UseTradeDocumentation);
		whereToSearchWithFileBox.addItem(CanonicalNames.TaxonomicNotes, CanonicalNames.TaxonomicNotes);

		whereToSearchWithHP.add(html);
		whereToSearchWithHP.add(whereToSearchWithFileBox);
		whereToSearchWithHP.setWidth("100%");
		whereToSearchWithHP.setCellHorizontalAlignment(html, HasHorizontalAlignment.ALIGN_LEFT);

		ButtonBar bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.RIGHT);
		bar.addStyleName("vertical-align-bottom");
		bar.add(searchButton);

		searchResults = new VerticalPanel();
		searchResults.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		Grid optionsGrid = new Grid(2, 2);
		caseInsensitive = new CheckBox(" Case insensitive");
		entireWord = new CheckBox(" Entire word only");
		entireWord.setChecked(true);
		regex = new CheckBox(" Regular expression");
		optionsGrid.setCellSpacing(6);
		optionsGrid.setWidget(0, 0, caseInsensitive);
		optionsGrid.setWidget(0, 1, entireWord);
		optionsGrid.setWidget(1, 0, regex);

		LayoutContainer widget = new LayoutContainer();
		RowLayout layout = new RowLayout();
		widget.setLayout(layout);
		widget.addStyleName("widgetBorder");

		widget.add(bar, new RowData(1, 25));
		widget.add(searchResults, new RowData(1, 25));
		widget.add(optionsGrid, new RowData(1, 25));
		widget.add(textHP, new RowData(1, 25));		
		widget.add(whereToSearchWithHP, new RowData(1, 25));
		widget.add(selectorHP, new RowData(1, 25));
		widget.add(filesHP, new RowData(1, 1));
		
		widget.setScrollMode(Scroll.AUTOY);

		centerPanel.add(widget);

	}
	
	private void setAssessmentFilter() {
		String value = selectorBox.getValue(selectorBox.getSelectedIndex());
		WorkingSet ws = WorkingSetCache.impl.getWorkingSet(Integer.valueOf(value));
		if (ws != null) {
			assFilterPanel.setFilter(ws.getFilter());
			assFilterPanel.setEnabled(true);
		} else {
			assFilterPanel.setEnabled(false);
		}
		
	}

	private void buildSearchResults() {
		searchResults.clear();
		String text;
		if (!FindResultCache.impl.reachedMax()) {
			text = "Files containing text: " + FindResultCache.impl.getFindResults().size();
			if (FindResultCache.impl.getFindResults().size() > NUMBEROFRESULTSSHOWED) {
				text += ", displaying top " + NUMBEROFRESULTSSHOWED + " results.";
			}
		} else {
			text = "More than " + FindResultCache.MAXNUMBEROFRESULTS + " results.  Please "
					+ "further restrict your search.";
		}
		searchResults.add(new HTML(text));
	}

	private void cancelFind() {
		stopSearch();
		updateResults();
	}

	private void clearPreviousResults() {
		results.clear();
		resultsTable.clear();
		buildHeader();
		clearReplacePanel();
		layout();
	}

	private void clearReplacePanel() {
		if (!isReplacePanelEmpty) {
			setReplacePanelInfo("", "", "", "", 0, "", 0, 0);
		}
		isReplacePanelEmpty = true;
	}

	private void continueSearching() {
		GenericCallback<String> callback = new GenericCallback<String>() {

			public void onFailure(Throwable arg0) {
				finishedSearching();
			}

			public void onSuccess(String arg0) {
				try {
					updateResults();
					if (!FindResultCache.impl.doneFinding() && !FindResultCache.impl.reachedMax()) {
						continueSearching();
					} else if (FindResultCache.impl.reachedMax()) {
						stopSearch();
					} else {
						finishedSearching();
					}
				} catch (Exception e) {
					e.printStackTrace();
					onFailure(new Throwable((String) arg0));
				}
			}
		};
		FindResultCache.impl.find(stringToFind, options, criteria, searchedWorkingSetID, filter, callback);
	}

	private void determineOptions() {
		StringBuffer buffer = new StringBuffer();
		if (caseInsensitive.isChecked())
			buffer.append("1,");
		else
			buffer.append("0,");
		if (entireWord.isChecked())
			buffer.append("1,");
		else
			buffer.append("0,");
		if (regex.isChecked())
			buffer.append("1");
		else
			buffer.append("0");
		options = buffer.toString();
		criteria = whereToSearchWithFileBox.getValue(whereToSearchWithFileBox.getSelectedIndex());
	}

	private void determineSearchType() {
//		String fileBoxValue = fileBox.getValue(fileBox.getSelectedIndex());
		
		

//		if (fileBoxValue.equalsIgnoreCase(FindResultCache.TAXON + "")) {
//			if (workingSetID.equalsIgnoreCase(FindResultCache.CURRENTTAXON + "")) {
//				searchType = FindResultCache.TAXON;
//				ids = TaxonomyCache.impl.getCurrentNode().getId() + "";
//			} else if (workingSetID.equalsIgnoreCase(FindResultCache.ALLTAXON + "")) {
//				searchType = FindResultCache.ALLTAXON;
//				ids = null;
//			} else {
//				searchType = FindResultCache.TAXON;
//				ids = WorkingSetCache.impl.getWorkingSet(workingSetID).getSpeciesIDsAsString();
//			}
//
//		} else if (fileBoxValue.equalsIgnoreCase(FindResultCache.DRAFTASSESSMENT + "")) {
//
//			if (workingSetID.equalsIgnoreCase(FindResultCache.ALLDRAFTASSESSMENTS + "")) {
//				searchType = FindResultCache.ALLDRAFTASSESSMENTS;
//				ids = null;
//			} else {
//				String[] typeID = workingSetID.split(",");
//				searchType = FindResultCache.DRAFTASSESSMENT;
//				if (workingSetID.startsWith(FindResultCache.DRAFTASSESSMENT + "")) {
//					ids = typeID[1];
//				} else {
//					ids = WorkingSetCache.impl.getWorkingSet(typeID[1]).getSpeciesIDsAsString();
//				}
//
//			}
//		}
//
//		else if (fileBoxValue.equalsIgnoreCase(FindResultCache.PUBLISHEDASSESSMENT + "")) {
//
//			if (workingSetID.equalsIgnoreCase(FindResultCache.ALLPUBLISHEDASSESSMENTS + "")) {
//				searchType = FindResultCache.ALLPUBLISHEDASSESSMENTS;
//				ids = null;
//			} else {
//				String[] typeID = workingSetID.split(",");
//				if (workingSetID.startsWith(FindResultCache.PUBLISHEDASSESSMENT + "")) {
//					ids = typeID[1];
//					searchType = FindResultCache.PUBLISHEDASSESSMENT;
//				} else {
//					ids = WorkingSetCache.impl.getWorkingSet(typeID[1]).getSpeciesIDsAsString();
//					searchType = FindResultCache.PUBLISHEDASSESSMENTS_FOR_TAXA;
//				}
//
//			}
//
//		}
//
//		else if (fileBoxValue.equalsIgnoreCase(FindResultCache.ALLASSESSMENTS + "")) {
//
//			if (workingSetID.equalsIgnoreCase(FindResultCache.ALLASSESSMENTS + "")) {
//				searchType = FindResultCache.ALLASSESSMENTS;
//				ids = null;
//			} else {
//				String[] typeID = workingSetID.split(",");
//				searchType = FindResultCache.ALLASSESSMENTS;
//				ids = WorkingSetCache.impl.getWorkingSet(typeID[1]).getSpeciesIDsAsString();
//
//			}
//
//		}

	}

	private void disableSearch() {
		searchButton.setText("Cancel Find");
		enabled(false);
	}

	private void enabled(boolean enabled) {
		findBox.setEnabled(enabled);
		selectorBox.setEnabled(enabled);
		whereToSearchWithFileBox.setEnabled(enabled);
		// searchButton.setEnabled(enabled);
	}

	private void enableReplacementButtons(boolean enable) {
		if (enable) {
			replaceAllButton.enable();
			skipButton.enable();
			replaceButton.enable();
			skipAllInFileButton.enable();
		} else {
			replaceAllButton.disable();
			skipButton.disable();
			replaceButton.disable();
			skipAllInFileButton.disable();
		}
	}

	private void enableSearch() {
		searchButton.setText("Find");
		enabled(true);
	}

//	private void fileSelectionChanged() {
//		selectorBox.setEnabled(!(fileBox.getSelectedIndex() == 0));
//		setSelectorBox();
//	}

	private void finishedSearching() {
		enabled(true);
		buildSearchResults();
		enableSearch();
	}

	public void refresh() {
		if (!build) {
			build();
			build = true;
		}

//		fileBox.setSelectedIndex(0);
		for (int i = 1; i < selectorBox.getItemCount(); i++) {
			selectorBox.removeItem(i);
		}
		setSelectorBox();
		selectorBox.setSelectedIndex(0);
		workingSets = WorkingSetCache.impl.getWorkingSets();
		
	}

	private void removeRow(int index) {
		resultsTable.remove(index + 1);
	}

	private void replace() {
		if (!isReplacePanelEmpty) {
			String replaceText = replaceBox.getText();
			if (replaceText.equalsIgnoreCase("")) {
				WindowUtils.errorAlert("Error", "You must enter something to replace the string with.");
			} else {
				// FindResultCache.impl.getCurrentFindResultData().
				// replaceCurrentSentence(replaceText);
				FindResultCache.impl.replaceCurrentSentence(replaceText);
				afterSkipReplace();
			}
		}
	}

	private void replaceAll() {
		if (!isReplacePanelEmpty) {
			String replaceText = replaceBox.getText();
			if (replaceText.equalsIgnoreCase("")) {
				WindowUtils.errorAlert("Error", "You must enter something to replace the string with.");
			} else {
				FindResultCache.impl.replaceAllSentences(replaceText);
				afterSkipReplace();

			}
		}
	}

	private void search() {

		if (searchButton.getText().equalsIgnoreCase("find")) {
			String errorMessage = null;

			if (findBox.getText().trim().equalsIgnoreCase("")) {
				errorMessage = "Please enter text which you would like to search for.";
			} else if (selectorBox.getSelectedIndex() == 0) {
				errorMessage = "Please select a working set.";
			} else {
				errorMessage = assFilterPanel.checkValidity();
				
			}

			if (errorMessage != null) {
				WindowUtils.errorAlert("Error", errorMessage);
				return;
			}

			enabled(false);
			clearPreviousResults();
			stringToFind = findBox.getText();
			searchedWorkingSetID = selectorBox.getValue(selectorBox.getSelectedIndex());
			filter = assFilterPanel.getFilter();
			determineOptions();
//			determineSearchType();
			continueSearching();
			disableSearch();
		} else {
			cancelFind();
		}
	}

	private void setReplacePanelInfo(String name, String type, String text, String original, int numberOfResults,
			String description, int currentNumberInFile, int currentIndexInSentence) {
		this.type.setHTML(type);
		this.name.setHTML(name);
		if (numberOfResults > 0) {
			this.resultCount.setHTML("" + numberOfResults);
		} else
			this.resultCount.setHTML("");
		updateReplacePanelInfo(description, text, original, currentNumberInFile, currentIndexInSentence);
		isReplacePanelEmpty = false;
	}

	private void setSelectorBox() {
		selectorBox.clear();
		selectorBox.addItem("");
		for (Entry<Integer, WorkingSet> ws : WorkingSetCache.impl.getWorkingSets().entrySet()) {
			String id = String.valueOf(ws.getValue().getId());
			String name = ws.getValue().getWorkingSetName();
			selectorBox.addItem(name, id);
		}
		
		
		

//		if (fileBox.getValue(fileBox.getSelectedIndex()).equalsIgnoreCase(FindResultCache.TAXON + "")) {
//			if (TaxonomyCache.impl.getCurrentNode() != null)
//				selectorBox.addItem("Current Taxon", FindResultCache.CURRENTTAXON + "");
//			selectorBox.addItem("All taxon", FindResultCache.ALLTAXON + "");
//			Iterator iter = workingSets.keySet().iterator();
//			while (iter.hasNext()) {
//				String key = (String) iter.next();
//				selectorBox.addItem("All taxa in " + ((WorkingSet) workingSets.get(key)).getWorkingSetName()
//						+ " working set", ((WorkingSet) workingSets.get(key)).getId());
//			}
//
//		} else if (fileBox.getValue(fileBox.getSelectedIndex()).equalsIgnoreCase(FindResultCache.DRAFTASSESSMENT + "")) {
//
//			if (TaxonomyCache.impl.getCurrentNode() != null) {
//				List<Assessment> list = AssessmentCache.impl.getDraftAssessmentsForTaxon(TaxonomyCache.impl.getCurrentNode());
//				for (Assessment data : list) {
//					if (data != null) {
//						if (data.isRegional())
//							selectorBox.addItem(TaxonomyCache.impl.getCurrentNode().getFullName()
//									+ " draft assessment for region "
//									+ RegionCache.impl.getRegionName(data.getRegionIDs()),
//									FindResultCache.DRAFTASSESSMENT + "," + data.getAssessmentID());
//						else
//							selectorBox.addItem(TaxonomyCache.impl.getCurrentNode().getFullName()
//									+ " global draft assessment", FindResultCache.DRAFTASSESSMENT + ","
//									+ data.getAssessmentID());
//					}
//				}
//			}
//			// selectorBox.addItem("All draft assessments",
//			// FindResultCache.ALLDRAFTASSESSMENTS + "");
//			Iterator iter = workingSets.keySet().iterator();
//			while (iter.hasNext()) {
//				String key = (String) iter.next();
//				selectorBox.addItem("All draft assessments in "
//						+ ((WorkingSet) workingSets.get(key)).getWorkingSetName() + " working set", "id,"
//						+ ((WorkingSet) workingSets.get(key)).getId());
//			}
//
//		} else if (fileBox.getValue(fileBox.getSelectedIndex()).equalsIgnoreCase(
//				FindResultCache.PUBLISHEDASSESSMENT + "")) {
//			if (TaxonomyCache.impl.getCurrentNode() != null) {
//				ArrayList list = TaxonomyCache.impl.getCurrentNode().getAssessments();
//				for (int i = 0; i < list.size(); i++) {
//					Assessment data = (Assessment) AssessmentCache.impl.getPublishedAssessment((String) list
//							.get(i), false);
//					if (data != null) {
//						selectorBox.addItem(TaxonomyCache.impl.getCurrentNode().getFullName() + " assessment dated "
//								+ data.getDateAssessed(), FindResultCache.PUBLISHEDASSESSMENT + ","
//								+ data.getAssessmentID());
//					}
//				}
//			}
//			// selectorBox.addItem("All published assessments",
//			// FindResultCache.ALLPUBLISHEDASSESSMENTS + "");
//			Iterator iter = workingSets.keySet().iterator();
//			while (iter.hasNext()) {
//				String key = (String) iter.next();
//				selectorBox.addItem("All published assessments in "
//						+ ((WorkingSet) workingSets.get(key)).getWorkingSetName() + " working set", "id,"
//						+ ((WorkingSet) workingSets.get(key)).getId());
//			}
//		} else if (fileBox.getValue(fileBox.getSelectedIndex()).equalsIgnoreCase(FindResultCache.ALLASSESSMENTS + "")) {
//			// selectorBox.addItem("All published and draft assessments",
//			// FindResultCache.ALLASSESSMENTS + "");
//			Iterator iter = workingSets.keySet().iterator();
//			while (iter.hasNext()) {
//				String key = (String) iter.next();
//				selectorBox.addItem("All assessments in " + ((WorkingSet) workingSets.get(key)).getWorkingSetName()
//						+ " working set", "id," + ((WorkingSet) workingSets.get(key)).getId());
//			}
//		}

		layout();

	}

	private void skip() {
		if (!isReplacePanelEmpty) {
			FindResultCache.impl.skipCurrentSentence();
			afterSkipReplace();
		}
	}

	private void skipAll() {
		if (!isReplacePanelEmpty) {
			FindResultCache.impl.skipAllSentences();
			afterSkipReplace();
		}
	}

	private void stopSearch() {
		FindResultCache.impl.deleteSearch(new GenericCallback<String>() {

			public void onFailure(Throwable caught) {
				enableSearch();
			}

			public void onSuccess(String arg0) {
				enableSearch();
			}

		});
	}

	// TODO:
	private void updateReplacePanelInfo(String description, String text, String originalWord, int currentNumberInFile,
			int currentIndex) {

		this.description.setText(description);
		// SysDebugger.getInstance().println("This is the info I got: text=" +
		// text + " originalWord=" + originalWord +
		// " currentNumberInFile=" + currentNumberInFile + " currentIndex=" +
		// currentIndex);
//		resultTextHTML.setHTML(text);
		if (currentNumberInFile > 0) {
			this.currentResult.setHTML("" + currentNumberInFile);
		} else
			this.currentResult.setHTML("");
		// text = XMLUtils.clean(text);
		// currentIndex = text.indexOf(originalWord, currentIndex);
		text = text.substring(0, currentIndex) + "<b><span color=\"blue\">" + originalWord + "</span></b>"
				+ text.substring(currentIndex + originalWord.length());
		resultTextHTML.setHTML(XMLUtils.cleanFromXML(text));
	}

	private void updateResults() {
		if (results.size() < NUMBEROFRESULTSSHOWED) {
			for (int i = results.size(); i < NUMBEROFRESULTSSHOWED; i++) {
				// SysDebugger.getInstance().println("I want to update results");
				FindReplaceData data = FindResultCache.impl.getFindResultData(i);

				// MUST ADD IT TO OUR TABLE
				if ((!results.contains(data)) && data != null) {
					results.add(data);
					buildRow(data);

				}
			}
			buildReplacePanel();
		}
		buildSearchResults();

	}

}
