package org.iucn.sis.client.panels.assessments;

import com.extjs.gxt.ui.client.widget.TabPanel;

public class AssessmentSummaryPanel extends TabPanel {

	// private PanelManager panelManager = null;
	// private VerticalPanel vp = null;
	// private VerticalPanel vp2 = null;
	// private HorizontalPanel hp = null;
	// private boolean built;
	// private Assessment currentAssessment = null;
	// private BorderLayoutData center;
	// private BorderLayoutData west;
	// private LayoutContainer westContainer;
	// private LayoutContainer centerContainer;
	// private LayoutContainer rationaleAndNotesContainer;
	//	
	// private LayoutContainer basicInfo;
	// private LayoutContainer notesPanel;
	// private ExpertPanel exp;
	// private LayoutContainer expert;
	// private LayoutContainer expertInfo;
	// private CheckBox checkBoxPossiblyExtinct;
	// private CheckBox checkBoxPossiblyExtinctCandidate;
	// private HorizontalPanel expertHorizontal;
	//	
	// private boolean overrideExpert = false;
	// private ListBox classificationListBox;
	// private CheckBox isRegional;
	// private ListBox currentPopulationTrendList;
	// private Tree genuineTree;
	// private Tree nongenuineTree;
	// private Tree otherTree;
	// private TextArea rationaleTextArea;
	// private TextArea noteTextArea;
	// private TextBox sourceDateTextBox;
	// private TextBox sourceTextBox;
	// private TextBox regionTextBox;
	// private TextBox dateModifiedTextBox;
	// private TextBox dateFinalizedTextBox;
	// private TextBox dateAddedTextBox;
	//	
	// //TODO: NEEDS DATE ASSESSED
	//	
	// private TextBox evaluatorTextBox;
	// private TextArea assessorsTextBox;
	// private HTML assessmentTypeHTML;
	// private HTML speciesNameHTML;
	// private TextBox dateLastSeenTextBox;
	// private Button overRideButton;
	// private Button cancelButton;
	// private Button clearButton;
	// private Button createStringButton;
	// private HTML expertCategoryHtml;
	// private HTML expertAbbreviationHtml;
	// private HTML criteriaStringHtml;
	// private HTML categoryAbbreviationHtml;
	// private HTML categoryHtml;
	// private TextBox criteriaVersionTextBox;
	// private HTML expertTitleHtml;
	// private HTML regionhtml;
	//	
	//	
	// /**
	// * The basicInfo panel is made of
	// * horizontal panels. Index of arrayList
	// * represents index of widget, and if the
	// * second widget in the horizontal panel
	// * is editable.
	// */
	// private ArrayList basicInfoEditable;
	//	
	//	
	// public AssessmentSummaryPanel( PanelManager manager )
	// {
	// // super(Style.HEADER, "x-panel" );
	// // BorderLayout layout = new BorderLayout();
	// // layout.setSpacing(0);
	// // layout.setMargin(0);
	// // setLayout(layout);
	// panelManager = manager;
	// vp = new VerticalPanel();
	// vp2 = new VerticalPanel();
	// hp = new HorizontalPanel();
	// built = false;
	// classificationToGrid = new HashMap();
	//
	// basicInfoEditable = new ArrayList();
	// // setSize("100%", "100%");
	// }
	//	
	//	
	// /**
	// * adds all widgets used in the panel
	// */
	// private void build(){
	// if (!built){
	//		
	// // center = new BorderLayoutData(LayoutRegion.CENTER, .55f, 500, 10000);
	// // west = new BorderLayoutData(LayoutRegion.WEST, .45f, 300, 10000);
	//			
	// westContainer = new LayoutContainer();
	// centerContainer = new LayoutContainer();
	// centerContainer.setLayout(new FillLayout());
	// westContainer.setLayout(new FillLayout());
	//
	// rationaleAndNotesContainer = new LayoutContainer();
	// rationaleAndNotesContainer.setLayout( new FlowLayout(0));
	//			
	// buildBasicInfo(westContainer);
	// buildNotes(rationaleAndNotesContainer);
	// buildExpertInfo(centerContainer);
	// buildGrid();
	//
	// TabItem item1 = new TabItem();
	// item1.add( westContainer );
	// item1.setText("Assessment Information");
	//			
	// TabItem item2 = new TabItem();
	// item2.add( centerContainer );
	// item2.setText("Red Listing Information");
	//
	// TabItem item3 = new TabItem();
	// item3.add( rationaleAndNotesContainer );
	// item3.setText("Justification and Notes");
	//			
	// addListener(Events.SelectionChange, new Listener() {
	// public void handleEvent(BaseEvent be)
	// {
	// onResize(getWidth(), getHeight());
	// // getSelection().layout();
	// }
	// });
	//			
	// // add(westContainer, west);
	// // add(centerContainer, center);
	//			
	// add( item1 );
	// add( item2 );
	// add( item3 );
	//			
	// built = true;
	// }
	// }
	//	
	// protected void onResize(int width, int height)
	// {
	// super.onResize(width, height);
	//		
	// centerContainer.setSize( width, height+100 );
	// westContainer.setSize( width, height+100 );
	// rationaleAndNotesContainer.setSize( width, height+100 );
	// }
	//	
	//	
	//	
	// private void buildNotes(LayoutContainer container){
	// notesPanel = new LayoutContainer();
	// // notesPanel.setText("Rationale and Notes");
	// // notesPanel.setCollapse(true);
	// notesPanel.setScrollMode(Scroll.AUTO);
	//		
	// VerticalPanel panel = new VerticalPanel();
	// panel.setSpacing(4);
	// HTML html = new HTML("Justification: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// rationaleTextArea = new TextArea();
	// panel.add(rationaleTextArea);
	// rationaleTextArea.setWidth("100%");
	// rationaleTextArea.setHeight("250px");
	// notesPanel.add(panel);
	// panel.setWidth("100%");
	//	
	//		
	// panel = new VerticalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Notes: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// noteTextArea = new TextArea();
	// panel.add(noteTextArea);
	// noteTextArea.setWidth("100%");
	// noteTextArea.setHeight("75px");
	// notesPanel.add(panel);
	// panel.setWidth("100%");
	//		
	// container.add(notesPanel);
	// }
	//	
	// private void buildExpertInfo(LayoutContainer container){
	//		
	// expertInfo = new LayoutContainer();
	// // expertInfo.setText("Assessment Status Information");
	// // expertInfo.setCollapse(true);
	// FillLayout layout = new FillLayout();
	// layout.setOrientation(Orientation.HORIZONTAL);
	// expertInfo.setLayout( layout );
	//		
	// exp = new ExpertPanel(panelManager);
	// // VerticalPanel expertVerticalPanel = new VerticalPanel();
	// // expertHorizontal = new HorizontalPanel();
	// // expertVerticalPanel.addStyleName("expert-border");
	// // expertHorizontal.addStyleName("expert-background");
	// // expertHorizontal.addStyleName("expert-border");
	// // expertHorizontal.setWidth("400px");
	// // expertHorizontal.setHeight("55px");
	// // expertVerticalPanel.setHorizontalAlignment(HasHorizontalAlignment.
	// ALIGN_CENTER);
	// // expertVerticalPanel.setSpacing(4);
	// // expertVerticalPanel.add(expertHorizontal);
	// // expertInfo.add(expertVerticalPanel);
	// // expertVerticalPanel.setWidth("100%");
	// // container.add(exp);
	//		
	//		
	//		
	// VerticalPanel overall = new VerticalPanel();
	//		
	// HorizontalPanel panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// HTML html = new HTML("Category: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// categoryHtml = new HTML();
	// categoryHtml.addStyleName("my-cpanel-hdr-text");
	// panel.add(categoryHtml);
	// overall.add(panel);
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Category Abbreviation: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// categoryAbbreviationHtml = new HTML();
	// categoryAbbreviationHtml.addStyleName("my-cpanel-hdr-text");
	// panel.add(categoryAbbreviationHtml);
	// overall.add(panel);
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Criteria String: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// criteriaStringHtml = new HTML();
	// criteriaStringHtml.addStyleName("my-cpanel-hdr-text");
	// panel.add(criteriaStringHtml);
	// overall.add(panel);
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Criteria Version: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// criteriaVersionTextBox = new TextBox();
	// panel.add(criteriaVersionTextBox);
	// overall.add(panel);
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Date Last Seen: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// dateLastSeenTextBox = new TextBox();
	// panel.add(dateLastSeenTextBox);
	// overall.add(panel);
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(20);
	// checkBoxPossiblyExtinct = new CheckBox("  Possibly extinct");
	// panel.add(checkBoxPossiblyExtinct);
	// checkBoxPossiblyExtinctCandidate = new
	// CheckBox("  Possibly extinct candidate");
	// panel.add(checkBoxPossiblyExtinctCandidate);
	// overall.add(panel);
	//		
	// // overallInfo.add(overall);
	//		
	// expert = new LayoutContainer();
	// expert.setLayout(new FlowLayout(4));
	// expert.setBorders(true);
	// expertTitleHtml = new HTML();
	// expertTitleHtml.addStyleName("my-cpanel-hdr-text");
	// expert.add(expertTitleHtml);
	//		
	// // overallInfo.add(expert);
	//		
	//		
	// // expertInfo.add(overallInfo);
	//		
	// LayoutContainer left = new LayoutContainer();
	// LayoutContainer right = new LayoutContainer();
	// left.setWidth("50%");
	// left.add(overall);
	// right.add(expert);
	// right.setWidth("50%");
	//		
	// expertInfo.add(left);
	// expertInfo.add(right);
	//		
	// container.add(expertInfo);
	//		
	//		
	// expertAbbreviationHtml = new HTML();
	// expertCategoryHtml = new HTML();
	//		
	// classificationListBox = new ListBox(false);
	// classificationListBox.addChangeListener(new ChangeListener(){
	// public void onChange(Widget sender) {
	// int index = ((ListBox)sender).getSelectedIndex();
	// // setPictureResult(((ListBox)sender).getValue(index));
	// setResult(((ListBox)sender).getItemText(index));
	// }
	// });
	// classificationListBox.addItem("");
	// classificationListBox.addItem("Extinct (EX)", "EX");
	// classificationListBox.addItem("Extinct in the Wild (EW)", "EW");
	// classificationListBox.addItem("Critically Endangered (CR)", "CR");
	// classificationListBox.addItem("Endangered (EN)", "EN");
	// classificationListBox.addItem("Vulnerable (VU)", "VU");
	// classificationListBox.addItem("Near Threatened (NT)", "NT");
	// classificationListBox.addItem("Least Concern (LC)", "LC");
	// classificationListBox.addItem("Data Deficient (DD)", "DD");
	// classificationListBox.addItem("Not Evaluated (NE)", "NE");
	//		
	// clearButton = new Button("Clear", new SelectionListener(){
	// public void widgetSelected(BaseEvent be) {
	// clearGrid();
	// classificationListBox.setSelectedIndex(0);
	// }
	// });
	//		
	// cancelButton = new Button("Delete Manual Classification", new
	// SelectionListener(){
	// public void widgetSelected(BaseEvent be) {
	// deleteManual();
	// }
	// });
	//		
	// createStringButton = new Button("Create Criteria String", new
	// SelectionListener(){
	// public void widgetSelected(BaseEvent be) {
	// String criteria = createCriteriaString();
	// criteriaStringHtml.setText(criteria);
	// }
	// });
	//		
	// overRideButton = new Button("Override Expert Results", new
	// SelectionListener(){
	// public void widgetSelected(BaseEvent be) {
	// refreshManualExpert();
	// overrideExpert = true;
	// }
	// });
	//		
	//		
	//		
	// }
	//	
	// private void buildBasicInfo(LayoutContainer container){
	//
	// basicInfo = new LayoutContainer();
	// // basicInfo.setScrollMode(Scroll.AUTO);
	// // basicInfo.setText("Basic Information");
	// FillLayout layout = new FillLayout();
	// layout.setOrientation(Orientation.HORIZONTAL);
	// basicInfo.setLayout( layout );
	//		
	// LayoutContainer left = new LayoutContainer();
	// left.setLayout( new FlowLayout(0) );
	// LayoutContainer right = new LayoutContainer();
	// right.setLayout( new FlowLayout(0) );
	//		
	// HorizontalPanel panel = new HorizontalPanel();
	// HTML html = new HTML("Species name: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.setSpacing(4);
	// panel.add(html);
	// speciesNameHTML = new HTML();
	// panel.add(speciesNameHTML);
	// // basicInfo.add(panel);
	// left.add( panel );
	// basicInfoEditable.add(new Boolean(false));
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Assessment Type: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// assessmentTypeHTML = new HTML();
	// panel.add(assessmentTypeHTML);
	// // basicInfo.add(panel);
	// left.add( panel );
	// basicInfoEditable.add(new Boolean(false));
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Assessors: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// assessorsTextBox = new TextArea();
	// assessorsTextBox.setSize("150px", "200px");
	// panel.add(assessorsTextBox);
	// // basicInfo.add(panel);
	// left.add( panel );
	// basicInfoEditable.add(new Boolean(true));
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Evaluators: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// evaluatorTextBox = new TextBox();
	// panel.add(evaluatorTextBox);
	// // basicInfo.add(panel);
	// left.add( panel );
	// // evaluatorTextBox.setSize("100%", "100%");
	// basicInfoEditable.add(new Boolean(true));
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Date Added: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// dateAddedTextBox = new TextBox();
	// panel.add(dateAddedTextBox);
	// dateAddedTextBox.setSize("100%", "100%");
	// // basicInfo.add(panel);
	// left.add( panel );
	// basicInfoEditable.add(new Boolean(true));
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Date Finalized: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// dateFinalizedTextBox = new TextBox();
	// panel.add(dateFinalizedTextBox);
	// dateFinalizedTextBox.setSize("100%", "100%");
	// // basicInfo.add(panel);
	// left.add( panel );
	// basicInfoEditable.add(new Boolean(true));
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Date Modified: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// dateModifiedTextBox = new TextBox();
	// panel.add(dateModifiedTextBox);
	// dateModifiedTextBox.setSize("100%", "100%");
	// // basicInfo.add(panel);
	// left.add( panel );
	// basicInfoEditable.add(new Boolean(true));
	//		
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Source: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// sourceTextBox = new TextBox();
	// panel.add(sourceTextBox);
	// sourceTextBox.setSize("100%", "100%");
	// // basicInfo.add(panel);
	// right.add( panel );
	// basicInfoEditable.add(new Boolean(true));
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Source Date: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// sourceDateTextBox = new TextBox();
	// panel.add(sourceDateTextBox);
	// sourceDateTextBox.setSize("100%", "100%");
	// // basicInfo.add(panel);
	// right.add( panel );
	// basicInfoEditable.add(new Boolean(true));
	//		
	//		
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Is Regional: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// regionhtml = new HTML("Region: ");
	// regionhtml.addStyleName("my-cpanel-hdr-text");
	// regionTextBox = new TextBox();
	// isRegional = new CheckBox("");
	// isRegional.addClickListener(new ClickListener(){
	// public void onClick(Widget sender) {
	// regionhtml.setVisible(((CheckBox)sender).isChecked());
	// regionTextBox.setVisible(((CheckBox)sender).isChecked());
	// if (!regionTextBox.isVisible()){
	// regionTextBox.setText("");
	// }
	// }
	// });
	// panel.add(isRegional);
	// panel.add(regionhtml);
	// panel.add(regionTextBox);
	// // basicInfo.add(panel);
	// right.add( panel );
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Current Population Trend: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// currentPopulationTrendList = new ListBox(false);
	// currentPopulationTrendList.addItem("");
	// currentPopulationTrendList.addItem("increasing");
	// currentPopulationTrendList.addItem("decreasing");
	// currentPopulationTrendList.addItem("stable");
	// currentPopulationTrendList.addItem("unknown");
	// currentPopulationTrendList.setSelectedIndex(0);
	// panel.add(currentPopulationTrendList);
	// // basicInfo.add(panel);
	// right.add( panel );
	//		
	// vp = new VerticalPanel();
	// vp.setSpacing(4);
	// html = new HTML("Change Reason: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// vp.add(html);
	//		
	// genuineTree = new Tree();
	// TreeItem item = new TreeItem(new CheckBox("Genuine Change"));
	// item.addItem(getCheckBox("Genuine Recent", genuineTree));
	// item.addItem(getCheckBox("Genuine since first assessment", genuineTree));
	// item.setState(true);
	// genuineTree.addItem(item);
	// genuineTree.ensureSelectedItemVisible();
	//		
	// nongenuineTree = new Tree();
	// item = new TreeItem(new CheckBox("Nongenuine Change"));
	// item.addItem(getCheckBox("New Information", nongenuineTree));
	// item.addItem(getCheckBox("Knowledge of criteria", nongenuineTree));
	// item.addItem(getCheckBox("Incorrect data previously used",
	// nongenuineTree));
	// item.addItem(getCheckBox("Criteria Revision", nongenuineTree));
	// item.addItem(getCheckBox("Taxonomy", nongenuineTree));
	// item.addItem(getCheckBox("Other", nongenuineTree));
	// item.setState(true);
	// nongenuineTree.addItem(item);
	// nongenuineTree.ensureSelectedItemVisible();
	//				
	// otherTree = new Tree();
	// item = new TreeItem(new CheckBox("No Change"));
	// item.addItem(getCheckBox("Same Category and Criteria", otherTree));
	// item.addItem(getCheckBox("Same Category but change in Criteria",
	// otherTree));
	// otherTree.addItem(item);
	// item.setState(true);
	// otherTree.ensureSelectedItemVisible();
	//		
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// panel.add(genuineTree);
	// panel.add(nongenuineTree);
	// panel.add(otherTree);
	//		
	// vp.add(panel);
	// // basicInfo.add(vp);
	// right.add( vp );
	//		
	// left.setWidth("50%");
	// right.setWidth("50%");
	// basicInfo.add( left );
	// basicInfo.add( right );
	// westContainer.add(basicInfo);
	// }
	//	
	// private CheckBox getCheckBox(String text,final Tree tree){
	// CheckBox check = new CheckBox(text);
	// check.addClickListener(new ClickListener(){
	// public void onClick(Widget sender) {
	// ((CheckBox)tree.getItem(0).getWidget()).setChecked(true);
	// }
	// });
	// return check;
	// }
	//	
	// /**
	// * Clears all grid info
	// */
	// private void clearGrid(){
	// for ( int i = 0; i < gridA.getColumnCount(); i++){
	// for (int j = 0; j < gridA.getRowCount(); j++){
	// ((CheckBox)gridA.getWidget(j, i)).setChecked(false);
	// }
	// }
	//
	// for ( int i = 0; i < gridB.getColumnCount(); i++){
	// for (int j = 0; j < gridB.getRowCount(); j++){
	// if(((CheckBox)gridB.getWidget(j, i)) != null)
	// ((CheckBox)gridB.getWidget(j, i)).setChecked(false);
	// }
	// }
	//
	// for ( int i = 0; i < gridC.getColumnCount(); i++){
	// for (int j = 0; j < gridC.getRowCount(); j++){
	// if (((CheckBox)gridC.getWidget(j, i)) != null)
	// ((CheckBox)gridC.getWidget(j, i)).setChecked(false);
	// }
	// }
	//
	// for ( int i = 0; i < gridD.getColumnCount(); i++){
	// for (int j = 0; j < gridD.getRowCount(); j++){
	// if (((CheckBox)gridD.getWidget(j, i)) != null)
	// ((CheckBox)gridD.getWidget(j, i)).setChecked(false);
	// }
	// }
	//
	// for ( int i = 0; i < gridE.getColumnCount(); i++){
	// for (int j = 0; j < gridE.getRowCount(); j++){
	// ((CheckBox)gridE.getWidget(j, i)).setChecked(false);
	// }
	// }
	// }
	//	
	//	
	// private void enableGrid(boolean readOnly){
	// for ( int i = 0; i < gridA.getColumnCount(); i++){
	// for (int j = 0; j < gridA.getRowCount(); j++){
	// ((CheckBox)gridA.getWidget(j, i)).setEnabled(readOnly);
	// }
	// }
	//		
	// for ( int i = 0; i < gridB.getColumnCount(); i++){
	// for (int j = 0; j < gridB.getRowCount(); j++){
	// if(((CheckBox)gridB.getWidget(j, i)) != null)
	// ((CheckBox)gridB.getWidget(j, i)).setEnabled(readOnly);
	// }
	// }
	//		
	// for ( int i = 0; i < gridC.getColumnCount(); i++){
	// for (int j = 0; j < gridC.getRowCount(); j++){
	// if (((CheckBox)gridC.getWidget(j, i)) != null)
	// ((CheckBox)gridC.getWidget(j, i)).setEnabled(readOnly);
	// }
	// }
	//		
	// for ( int i = 0; i < gridD.getColumnCount(); i++){
	// for (int j = 0; j < gridD.getRowCount(); j++){
	// if (((CheckBox)gridD.getWidget(j, i)) != null)
	// ((CheckBox)gridD.getWidget(j, i)).setEnabled(readOnly);
	// }
	// }
	//		
	// for ( int i = 0; i < gridE.getColumnCount(); i++){
	// for (int j = 0; j < gridE.getRowCount(); j++){
	// ((CheckBox)gridE.getWidget(j, i)).setEnabled(readOnly);
	// }
	// }
	// }
	//	
	//	
	//	
	// /**
	// * If the assessment has changed, then need to change
	// * everything in the widgets to reflect the new assessment
	// */
	// private void refresh(boolean viewOnly){
	//		
	// // setText("Assessment Summary --- " +
	// currentAssessment.getSpeciesName());
	//		
	// speciesNameHTML.setText(currentAssessment.getSpeciesName().trim());
	// String text =
	// getValidationStatus(currentAssessment.getValidationStatus());
	// assessmentTypeHTML.setText(text);
	// text = getText(currentAssessment.getEvaluators());
	// evaluatorTextBox.setText(text);
	// text = getText(currentAssessment.getAssessors());
	// assessorsTextBox.setText(text);
	// text = getText(currentAssessment.getDateAdded());
	// dateAddedTextBox.setText(text);
	// text = getText(currentAssessment.getDateFinalized());
	// dateFinalizedTextBox.setText(text);
	// text = getText(currentAssessment.getDateModified());
	// dateModifiedTextBox.setText(text);
	// text = getText(currentAssessment.getRegion());
	// regionTextBox.setText(text);
	// text = getText(currentAssessment.getSource());
	// sourceTextBox.setText(text);
	// text = getText(currentAssessment.getSourceDate());
	// sourceDateTextBox.setText(text);
	// boolean check = currentAssessment.isRegional();
	// isRegional.setChecked(check);
	// regionTextBox.setVisible(check);
	// regionhtml.setVisible(check);
	// checkBoxPossiblyExtinct.setChecked(currentAssessment.isPossiblyExtinct());
	// checkBoxPossiblyExtinctCandidate.setChecked(currentAssessment.
	// isPossiblyExtinctCandidate());
	//		
	// text = currentAssessment.getCurrentPopulationTrend();
	// for (int i = 0; i < currentPopulationTrendList.getItemCount(); i++){
	// if (text.trim().equals(currentPopulationTrendList.getItemText(i))){
	// currentPopulationTrendList.setSelectedIndex(i);
	// i = currentPopulationTrendList.getItemCount();
	// }
	// }
	//		
	//		
	//		
	// //TODO: TREES
	// text = currentAssessment.getRationale();
	// rationaleTextArea.setText(text);
	// text = currentAssessment.getNote();
	// noteTextArea.setText(text);
	//
	// refreshExpert();
	// refreshTree();
	//		
	// makeEdit(viewOnly);
	//		
	//		
	//		
	// }
	//	
	//	
	// private void refreshExpert(){
	// String text = getText(currentAssessment.getCritVersion());
	// setCriteriaVersion(text);
	// text = getText(currentAssessment.getLastSeen());
	// setDateLastSeen(text);
	//		
	// if (!currentAssessment.getManualCategoryCriteria().trim().equals("")){
	// overrideExpert = true;
	// // refreshFuzzyExpert();
	// refreshManualExpert();
	// }
	// else {
	// // refreshManualExpert();
	// refreshFuzzyExpert();
	// overrideExpert = false;
	// }
	//	
	// }
	//	
	// private void refreshFuzzyExpert(){
	//		
	// setCriteriaString(currentAssessment.getCategoryCriteria());
	//		
	//		
	// expert.removeAll();
	// expertTitleHtml.setText("Expert System Classification");
	// expert.add(expertTitleHtml);
	// HorizontalPanel panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// HTML html = new HTML("Category: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// SysDebugger.getInstance().println(
	// "this is currentAssessment.getCategoryFuzzyResult "
	// +currentAssessment.getCategoryFuzzyResult() );
	// SysDebugger.getInstance().println("This is text classification " +
	// currentAssessment.getCategoryText());
	//		
	// //TODO: CHANGE THIS!!!
	// String text = "";
	// if (currentAssessment.getCategoryFuzzyResult().trim().equals("")){
	// text = ExpertResultParser.impl.getTextClassification(currentAssessment.
	// getCategoryText());
	// SysDebugger.getInstance().println("This is text " + text);
	// categoryHtml.setText(ExpertResultParser.impl.getBestTextClassification(
	// currentAssessment.getCategoryText()));
	// }
	// else {
	// text = ExpertResultParser.impl.getTextClassification(currentAssessment.
	// getCategoryFuzzyResult());
	// SysDebugger.getInstance().println("This is text " + text);
	// categoryHtml.setText(ExpertResultParser.impl.getBestTextClassification(
	// currentAssessment.getCategoryFuzzyResult()));
	// }
	// expertCategoryHtml.setText(text);
	//		
	// panel.add(expertCategoryHtml);
	// expert.add(panel);
	//		
	// panel = new HorizontalPanel();
	// panel.setSpacing(4);
	// html = new HTML("Abbreviation: ");
	// html.addStyleName("my-cpanel-hdr-text");
	// panel.add(html);
	// text = getText(currentAssessment.getCategoryAbbreviation());
	// html.addStyleName("my-cpanel-hdr-text");
	// expertAbbreviationHtml.setText(text);
	// panel.add(expertAbbreviationHtml);
	// expert.add(panel);
	//		
	//		
	//		
	//		
	// expert.add(overRideButton);
	//		
	// // setPictureResult(currentAssessment.getCategoryFuzzyResult());
	//
	// categoryAbbreviationHtml.setText(expertAbbreviationHtml.getText());
	// expert.layout();
	// try{expertInfo.layout();}catch(Exception e){};
	//		
	// }
	//	
	//	
	// private void refreshManualExpert(){
	// expert.removeAll();
	// expertTitleHtml.setText("Manual Classification");
	// expert.add(expertTitleHtml);
	//		
	// setCriteriaString(currentAssessment.getManualCategoryCriteria());
	// categoryAbbreviationHtml.setText(currentAssessment.
	// getManualCategoryAbbreviation());
	//		
	//		
	// HorizontalPanel hp = new HorizontalPanel();
	// hp.setSpacing(4);
	// hp.add (new HTML("Classification: "));
	// hp.add(classificationListBox);
	// expert.add(hp);
	//		
	// if (!currentAssessment.getManualCategoryAbbreviation().equals("")){
	// for (int i = 1; i < classificationListBox.getItemCount(); i++){
	// if (classificationListBox.getValue(i).equalsIgnoreCase(currentAssessment.
	// getManualCategoryAbbreviation())){
	// classificationListBox.setSelectedIndex(i);
	// categoryHtml.setText(classificationListBox.getItemText(i).replaceFirst(
	// "\\(.*\\).*", ""));
	// }
	// }
	// }
	// else
	// classificationListBox.setSelectedIndex(0);
	//		
	// if (currentAssessment.getManualCategoryCriteria()!=null &&
	// !currentAssessment.getCategoryAbbreviation().trim().equals("")){
	// // parseCriteriaString();
	// }
	//		
	// //SET GRIDS
	// VerticalPanel vp = new VerticalPanel();
	// vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
	// HorizontalPanel hp1 = new HorizontalPanel();
	// vp.setSpacing(4);
	// hp1.setSpacing(4);
	// hp1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
	// hp1.add(new HTML("Criteria A:"));
	// hp1.add(gridA);
	// hp1.setStyleName("summary-border");
	// vp.add(hp1);
	//		
	// hp1 = new HorizontalPanel();
	// hp1.setSpacing(4);
	// hp1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
	// hp1.add(new HTML("Criteria B: "));
	// hp1.add(gridB);
	// hp1.addStyleName("summary-border");
	// vp.add(hp1);
	// hp1 = new HorizontalPanel();
	// hp1.setSpacing(4);
	// hp1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
	// hp1.add(new HTML("Criteria C: "));
	// hp1.add(gridC);
	// hp1.addStyleName("summary-border");
	// hp1.setSpacing(4);
	// hp1.add(new HTML("Criteria D: "));
	// hp1.add(gridD);
	// hp1.addStyleName("summary-border");
	// hp1.add(new HTML("Criteria E: "));
	// hp1.add(gridE);
	// vp.add(hp1);
	//				
	// expert.add(vp);
	//		
	// HorizontalPanel buttons = new HorizontalPanel();
	//
	// buttons.add(createStringButton);
	// buttons.add(clearButton);
	// buttons.add(cancelButton);
	// expert.add(buttons);
	// expert.layout();
	// try{expertInfo.layout();}catch(Exception e){};
	// }
	//	
	// private void deleteManual(){
	// overrideExpert = false;
	// currentAssessment.setManualCategoryAbbreviation("");
	// currentAssessment.setManualCategoryCriteria("");
	// refreshExpert();
	// }
	//	
	// private void check(String grid, String key){
	//		
	// SysDebugger.getInstance().println("calling check with grid " + grid +
	// " and key " + key);
	// String index = (String)classificationToGrid.get(key);
	// try{
	// String [] keys = index.split(",");
	// int row = Integer.valueOf(keys[0]).intValue();
	// int col = Integer.valueOf(keys[1]).intValue();
	//			
	// if (grid.equalsIgnoreCase("A")){
	// ((CheckBox)gridA.getWidget(row, col)).setChecked(true);
	// }
	// else if (grid.equalsIgnoreCase("B")){
	// ((CheckBox)gridB.getWidget(row, col)).setChecked(true);
	// }
	// else if (grid.equalsIgnoreCase("C")){
	// ((CheckBox)gridC.getWidget(row, col)).setChecked(true);
	// }
	// else if (grid.equalsIgnoreCase("D")){
	// ((CheckBox)gridD.getWidget(row, col)).setChecked(true);
	// }
	// else if (grid.equalsIgnoreCase("E")){
	// ((CheckBox)gridE.getWidget(row, col)).setChecked(true);
	// }
	//			
	// }catch(Exception e){}
	//		
	// }
	//	
	// // private void parseCriteriaString(){
	// // String criteriaString = currentAssessment.getManualCategoryCriteria();
	// //
	// // String [] cat = criteriaString.split(";");
	// // for (int i = 0; i < cat.length; i++){
	// //
	// // if (cat[i].startsWith("A")){
	// // String [] A = cat[i].split("+");
	// // A[0] = A[0].substring(1);
	// // for (int j = 0; j < A.length; j++){
	// // String finalString = "A" + A[i].substring(0, 1);
	// // for (int k = 0; k < A[i].length(); k++){
	// // check("A", finalString + A[i].substring(k+1, k+2));
	// // }
	// //
	// //
	// // }
	// // }
	// // else if (cat[i].startsWith("B")){
	// // String [] B = cat[i].split("+");
	// // B[0] = B[0].substring(1);
	// // for (int j = 0; j < B.length; j++){
	// // String finalString = "B" + B[i].substring(0,1);
	// // int index = B[i].indexOf("(");
	// // if (index > 2){
	// // check("B", finalString + "a");
	// // }
	// // else
	// //
	// // }
	// // }
	// // else if (cat[i].startsWith("C")){
	// //
	// // }
	// // else if (cat[i].startsWith("D")){
	// //
	// // }
	// // else if (cat[i].startsWith("E")){
	// //
	// // }
	// //
	// //
	// // }
	// //
	// // }
	//	
	//	
	//	
	//	
	// /**
	// * gets result out of the list box and sets category
	// * abbreviation and non abbreviation
	// * @param result
	// */
	// private void setResult(String result){
	// int index = result.indexOf("(");
	// setAbbreviation(result.substring(index+1, index+3));
	// }
	//	
	// private void setAbbreviation(String cat)
	// {
	// expertAbbreviationHtml.setText(cat);
	// categoryAbbreviationHtml.setText(cat);
	//		
	// if (cat.equals("EX"))
	// setCategory("Extinct");
	// else if (cat.equals("EW"))
	// setCategory("Extinct in the Wild");
	// else if (cat.equals("CR"))
	// setCategory("Critically Endangered");
	// else if (cat.equals("EN"))
	// setCategory("Endangered");
	// else if (cat.equals("VU"))
	// setCategory("Vulnerable");
	// else if (cat.equals("NT"))
	// setCategory("Near Threatened");
	// else if (cat.equals("LC"))
	// setCategory("Least Concern");
	// else if (cat.equals("DD"))
	// setCategory("Data Deficient");
	// else if (cat.equals("NE"))
	// setCategory("Not Evaluated");
	// else {
	// setCategory("N/A");
	//			
	// expertAbbreviationHtml.setText("N/A");
	// categoryAbbreviationHtml.setText("N/A");
	// }
	// }
	//	
	// private void setCategory(String result){
	// expertCategoryHtml.setText(result);
	// categoryHtml.setText(result);
	// }
	//	
	//	
	// /**
	// * Takes in either a csv which places in a range, or takes in
	// * a category which places it on the picture
	// * @param result
	// */
	// private void setPictureResult(String result){
	//		
	// SysDebugger.getInstance().println("I am in setPicture with result " +
	// result);
	//		
	// String [] results = result.split(",");
	// if (results.length == 3){
	// try{
	// int left = Integer.valueOf(results[0]).intValue();
	// int best = Integer.valueOf(results[1]).intValue();
	// int right = Integer.valueOf(results[2]).intValue();
	// if (left >= 0){
	// expertHorizontal = exp.createDisplay(left, best, right);
	// }
	// }
	// catch (Exception e){
	// HTML error = new HTML("Not enough data to run expert system");
	// expertHorizontal.add(error);
	// }
	// }
	// else if ( result.matches(".*CR.*")){
	// expertHorizontal = exp.createDisplay(50,50,50);
	// }
	// else if (result.matches(".*EN.*")){
	// expertHorizontal = exp.createDisplay(150, 150, 150);
	// }
	// else if (result.matches(".*VU.*")){
	// expertHorizontal = exp.createDisplay(250, 250, 250);
	// }
	//		
	// else if (result.matches(".*LC.*")){
	// expertHorizontal = exp.createDisplay(350, 350, 350);
	// }
	//		
	// }
	//	
	// private void setCriteriaVersion(String crit){
	// criteriaVersionTextBox.setText(crit);
	// }
	//	
	// private void setDateLastSeen(String date){
	// dateLastSeenTextBox.setText(date);
	// }
	// private void setCriteriaString(String criteria){
	// criteriaStringHtml.setText(getText(criteria));
	// }
	//	
	//	
	//	
	//	
	// void makeEdit(boolean viewOnly){
	// rationaleTextArea.setReadOnly(viewOnly);
	// noteTextArea.setReadOnly(viewOnly);
	// sourceDateTextBox.setReadOnly(viewOnly);
	// sourceTextBox.setReadOnly(viewOnly);
	// regionTextBox.setReadOnly(viewOnly);
	// dateModifiedTextBox.setReadOnly(viewOnly);
	// dateFinalizedTextBox.setReadOnly(viewOnly);
	// dateAddedTextBox.setReadOnly(viewOnly);
	// evaluatorTextBox.setReadOnly(viewOnly);
	// assessorsTextBox.setReadOnly(viewOnly);
	// classificationListBox.setEnabled(!viewOnly);
	// isRegional.setEnabled(!viewOnly);
	// currentPopulationTrendList.setEnabled(!viewOnly);
	// rationaleTextArea.setReadOnly(viewOnly);
	// noteTextArea.setReadOnly(viewOnly);
	// checkBoxPossiblyExtinct.setEnabled(!viewOnly);
	// checkBoxPossiblyExtinctCandidate.setEnabled(!viewOnly);
	// overRideButton.setVisible(!viewOnly);
	// cancelButton.setVisible(!viewOnly);
	// clearButton.setVisible(!viewOnly);
	// createStringButton.setVisible(!viewOnly);
	// dateLastSeenTextBox.setReadOnly(viewOnly);
	// criteriaVersionTextBox.setReadOnly(viewOnly);
	//		
	//		
	// setEditTree(genuineTree, !viewOnly);
	// setEditTree(nongenuineTree, !viewOnly);
	// setEditTree(otherTree, !viewOnly);
	// enableGrid(!viewOnly);
	// }
	//	
	//	
	// void save(){
	// currentAssessment.setAssessors(saveText(assessorsTextBox.getText()));
	// currentAssessment.setCritVersion(saveText(criteriaVersionTextBox.getText()
	// ));
	//		
	// int index = currentPopulationTrendList.getSelectedIndex();
	// if (index > 0){
	// currentAssessment.setCurrentPopulationTrend(currentPopulationTrendList.
	// getItemText(index));
	// }
	// else {
	// currentAssessment.setCurrentPopulationTrend("");
	// }
	// currentAssessment.setDateAdded(saveText(dateAddedTextBox.getText()));
	// currentAssessment.setDateAssessed(saveText(dateAddedTextBox.getText()));
	// currentAssessment.setDateFinalized(saveText(dateFinalizedTextBox.getText()
	// ));
	// currentAssessment.setDateModified(saveText(dateModifiedTextBox.getText()))
	// ;
	// currentAssessment.setEvaluators(saveText(evaluatorTextBox.getText()));
	// currentAssessment.setLastSeen(saveText(dateLastSeenTextBox.getText()));
	// currentAssessment.setNote(XMLUtils.clean(noteTextArea.getText()));
	// currentAssessment.setPossiblyExtinct(checkBoxPossiblyExtinct.isChecked());
	// currentAssessment.setPossiblyExtinctCandidate(
	// checkBoxPossiblyExtinctCandidate.isChecked());
	// currentAssessment.setRationale(XMLUtils.clean(rationaleTextArea.getText())
	// );
	// currentAssessment.setRegional(isRegional.isChecked());
	// if (isRegional.isChecked())
	// currentAssessment.setRegion(saveText(regionTextBox.getText()));
	// else
	// currentAssessment.setRegion("");
	// currentAssessment.setSource(XMLUtils.clean(sourceTextBox.getText()));
	// currentAssessment.setSourceDate(saveText(sourceDateTextBox.getText()));
	//		
	// saveExpert();
	// boolean save = saveChangeReason();
	// if (save){
	// AssessmentCache.impl.saveCurrentAssessment( new
	// GenericCallback<String>(){
	// public void onSuccess(String arg0)
	// {
	// WindowUtils.hideLoadingAlert();
	// Info.display("Save Complete", "Successfully saved assessment {0}.",
	// AssessmentCache.impl.getCurrentAssessment().getSpeciesName() );
	// }
	// public void onFailure(Throwable arg0) {
	// WindowUtils.hideLoadingAlert();
	// // layout();
	// Info.display( new InfoConfig("Save Failed", "Failed to save assessment.",
	// new Params("")) );
	// }
	// });
	// }
	// }
	//	
	// private String saveText(String text){
	// if (text.trim().equals("N/A")){
	// return "";
	// }
	// else
	// return text;
	// }
	//	
	//	
	// private void saveExpert(){
	// currentAssessment.setCategoryCriteria(saveText(criteriaStringHtml.getText(
	// )));
	// currentAssessment.setCategoryAbbreviation(saveText(categoryAbbreviationHtml
	// .getText()));
	//
	//		
	// if (overrideExpert){
	// if (classificationListBox.getSelectedIndex() != 0){
	//				
	// currentAssessment.setManualCategoryAbbreviation(saveText(currentAssessment
	// .getCategoryAbbreviation()));
	// SysDebugger.getInstance().println("I am saving abb " +
	// currentAssessment.getManualCategoryAbbreviation());
	// currentAssessment.setManualCategoryCriteria(saveText(criteriaStringHtml.
	// getText()));
	// }
	// else
	// currentAssessment.setManualCategoryAbbreviation("");
	// }
	//
	//
	//		
	// }
	//	
	// private void refreshTree(){
	// TreeItem rootGenuine = genuineTree.getItem(0);
	// ((CheckBox)rootGenuine.getWidget()).setChecked(currentAssessment.
	// isGenuineChange());
	// ((CheckBox)rootGenuine.getChild(0).getWidget()).setChecked(
	// currentAssessment.isGenuineRecent());
	// ((CheckBox)rootGenuine.getChild(1).getWidget()).setChecked(
	// currentAssessment.isGenuineSinceFirst());
	//		
	// TreeItem rootNonGenuine = nongenuineTree.getItem(0);
	// ((CheckBox)rootNonGenuine.getWidget()).setChecked(currentAssessment.
	// isNonGenuineChange());
	// ((CheckBox)rootNonGenuine.getChild(0).getWidget()).setChecked(
	// currentAssessment.isKnowledgeNew());
	// ((CheckBox)rootNonGenuine.getChild(1).getWidget()).setChecked(
	// currentAssessment.isKnowledgeCriteria());
	// ((CheckBox)rootNonGenuine.getChild(2).getWidget()).setChecked(
	// currentAssessment.isKnowledgeCorrection());
	// ((CheckBox)rootNonGenuine.getChild(3).getWidget()).setChecked(
	// currentAssessment.isCriteriaRevision());
	// ((CheckBox)rootNonGenuine.getChild(4).getWidget()).setChecked(
	// currentAssessment.isTaxonomy());
	// ((CheckBox)rootNonGenuine.getChild(5).getWidget()).setChecked(
	// currentAssessment.isOther());
	//		
	// TreeItem rootNo = otherTree.getItem(0);
	// ((CheckBox)rootNo.getWidget()).setChecked(currentAssessment.isNoChange());
	// ((CheckBox)rootNo.getChild(0).getWidget()).setChecked(currentAssessment.
	// isSame());
	// ((CheckBox)rootNo.getChild(1).getWidget()).setChecked(currentAssessment.
	// isCriteriaChange());
	// }
	//	
	// private boolean saveChangeReason(){
	//		
	// boolean save = true;
	//		
	// TreeItem rootGenuine = genuineTree.getItem(0);
	// boolean genuine = ((CheckBox)rootGenuine.getWidget()).isChecked();
	// boolean genrecent =
	// ((CheckBox)rootGenuine.getChild(0).getWidget()).isChecked();
	// boolean genlong =
	// ((CheckBox)rootGenuine.getChild(1).getWidget()).isChecked();
	//		
	// TreeItem rootNonGenuine = nongenuineTree.getItem(0);
	// boolean nongenuine = ((CheckBox)rootNonGenuine.getWidget()).isChecked();
	// boolean newInfo =
	// ((CheckBox)rootNonGenuine.getChild(0).getWidget()).isChecked();
	// boolean knowCriteria =
	// ((CheckBox)rootNonGenuine.getChild(1).getWidget()).isChecked();
	// boolean incorrectData =
	// ((CheckBox)rootNonGenuine.getChild(2).getWidget()).isChecked();
	// boolean criteriaVer =
	// ((CheckBox)rootNonGenuine.getChild(3).getWidget()).isChecked();
	// boolean taxonomy =
	// ((CheckBox)rootNonGenuine.getChild(4).getWidget()).isChecked();
	// boolean other =
	// ((CheckBox)rootNonGenuine.getChild(5).getWidget()).isChecked();
	//		
	// TreeItem rootNo = otherTree.getItem(0);
	// boolean no = ((CheckBox)rootNo.getWidget()).isChecked();
	// boolean same = ((CheckBox)rootNo.getChild(0).getWidget()).isChecked();
	// boolean sameDiff =
	// ((CheckBox)rootNo.getChild(1).getWidget()).isChecked();
	//		
	//		
	// boolean first = genuine || genrecent || genlong;
	// boolean second = nongenuine || newInfo || knowCriteria || incorrectData
	// || criteriaVer || other;
	// boolean third = no || same || sameDiff;
	//		
	// if ((first && second) || (first && third) || (second && third)){
	// MessageBox error = new MessageBox(Style.ICON_ERROR, Style.OK);
	// error.setMessage(
	// "You may only choose one type of change for the change reason");
	// error.setText("ERROR");
	// error.show();
	// save = false;
	// }
	//		
	// else {
	//			
	// currentAssessment.setGenuineChange(genuine);
	// currentAssessment.setGenuineRecent(genrecent);
	// currentAssessment.setGenuineSinceFirst(genlong);
	//				
	// currentAssessment.setNonGenuineChange(nongenuine);
	// currentAssessment.setOther(other);
	// currentAssessment.setCriteriaRevision(criteriaVer);
	// currentAssessment.setKnowledgeCorrection(incorrectData);
	// currentAssessment.setKnowledgeCriteria(knowCriteria);
	// currentAssessment.setKnowledgeNew(newInfo);
	// currentAssessment.setTaxonomy(taxonomy);
	//				
	// currentAssessment.setNoChange(no);
	// currentAssessment.setSame(same);
	// currentAssessment.setCriteriaChange(sameDiff);
	//				
	//			
	// }
	//				
	// return save;
	// }
	//	
	//	
	// private void setEditTree(Tree tree, boolean viewOnly){
	// TreeItem root = tree.getItem(0);
	// ((CheckBox)root.getWidget()).setEnabled(viewOnly);
	// for (int i = 0; i < root.getChildCount(); i++){
	// ((CheckBox)root.getChild(i).getWidget()).setEnabled(viewOnly);
	// }
	// }
	//	
	//	
	// private String getText(String text){
	// if (text == null || text.trim().equals(""))
	// return "N/A";
	// else
	// return text;
	// }
	//	
	// private String getValidationStatus(String status){
	// if (status == null || status == ""){
	// return "N/A";
	// }
	// else if (status.equals(AssessmentType.DRAFT_ASSESSMENT_TYPE)){
	// return "Draft Assessment";
	// }
	// else if (status.equals(AssessmentType.PUBLISHED_ASSESSMENT_TYPE)){
	// return "Published Assessment";
	// }
	// else if (status.equals(AssessmentType.USER_ASSESSMENT_TYPE)){
	// return "User Assessment";
	// }
	// else
	// return "N/A";
	// }
	//	
	// /**
	// * decides what to send the user to, build, or refresh
	// */
	// public void update(boolean viewOnly)
	// {
	// if (!built){
	// build();
	// }
	// currentAssessment = AssessmentCache.impl.getCurrentAssessment();
	// refresh(viewOnly);
	//		
	//		
	// // String speciesName = currentAssessment.getSpeciesName();
	// // String evaluators = currentAssessment.getEvaluators();
	// // String dateAdded = currentAssessment.getDateAdded();
	// // String dateFinalized = currentAssessment.getDateFinalized();
	// // String validationStatus = currentAssessment.getValidationStatus();
	// //
	// // String cat = currentAssessment.getCategoryAbbreviation();
	// // String range = currentAssessment.getCategoryFuzzyResult();
	// // String catString = currentAssessment.getCategoryCriteria();
	// // String [] results = range.split(",");
	// //
	// // setText("Assessment Summary --- " + speciesName);
	//		
	//		
	//		
	//		
	// // //CREATE CENTER PANEL
	// // ExpertPanel exp = new ExpertPanel(panelManager);
	// // VerticalPanel expertVerticalPanel = new VerticalPanel();
	// // HorizontalPanel expertHorizontal = new HorizontalPanel();
	// // expertVerticalPanel.addStyleName("expert-border");
	// // for (int i = 0; i < 2; i++)
	// // expertVerticalPanel.add(new HTML("&nbsp"));
	// // if (results.length == 3){
	// // try{
	// // int left = Integer.valueOf(results[0]).intValue();
	// // int best = Integer.valueOf(results[1]).intValue();
	// // int right = Integer.valueOf(results[2]).intValue();
	// // if (left >= 0){
	// // expertHorizontal = exp.createDisplay(left, best, right);
	// // }
	// // }
	// // catch (Exception e){
	// // HTML error = new HTML("Not enough data to run expert system");
	// // expertHorizontal.add(error);
	// // }
	// // }
	// // else if ( cat.matches(".*CR.*")){
	// // expertHorizontal = exp.createDisplay(50,50,50);
	// // }
	// // else if (cat.matches(".*EN.*")){
	// // expertHorizontal = exp.createDisplay(150, 150, 150);
	// // }
	// // else if (cat.matches(".*VU.*")){
	// // expertHorizontal = exp.createDisplay(250, 250, 250);
	// // }
	// // else if (cat.matches(".*EX.*")){
	// // expertVerticalPanel.add(new HTML("Extinct"));
	// // }
	// // else if (cat.matches(".*LC.*")){
	// // expertVerticalPanel.add(new HTML("Least Concern"));
	// // }
	// // else if (cat.matches(".*LR.*")){
	// // expertVerticalPanel.add(new HTML("Least Concern"));
	// // }
	// // else if (cat.matches(".*NE.*")){
	// // expertVerticalPanel.add(new HTML("Not Evaluated"));
	// // }
	// // expertHorizontal.addStyleName("expert-background");
	// // expertHorizontal.addStyleName("expert-border");
	// // expertHorizontal.setWidth("400px");
	// // expertHorizontal.setHeight("100px");
	// //
	// //
	// //
	// //
	// // expertVerticalPanel.add(expertHorizontal);
	// //
	// //
	// //
	// //
	// // vp2.add(expertVerticalPanel);
	// // vp2.addStyleName("summary-border");
	// // hp.add(vp2);
	// //
	// // add(hp);
	//
	//		
	// }
	//	
	//	

}
