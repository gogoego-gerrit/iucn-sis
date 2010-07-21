package org.iucn.sis.client.panels.assessments;

import com.extjs.gxt.ui.client.widget.Dialog;

public class AddDraftAssessmentsPanel extends Dialog {

	// private PanelManager manager;
	// private List list;
	// private LayoutContainer content;
	// private String id;
	//
	// public AddDraftAssessmentsPanel(PanelManager manager, String id) {
	// super(Style.OK_CANCEL | Style.CLOSE );
	// content = getContent();
	// content.setLayout(new FlowLayout(0));
	// content.setSize(380, 500);
	// setCloseOnButtonClick(true);
	// this.id = id;
	// list = new List(Style.MULTI | Style.CHECK);
	// build();
	// setSize(400, 500);
	// this.manager = manager;
	//		
	// }
	//	
	// protected void onButtonPressed(BaseEvent be) {
	// if (getCloseOnButtonClick() &&
	// ((ButtonBar)be.getSource()).getButtonPressed().getText
	// ().equalsIgnoreCase("cancel")) {
	// hide();
	// }
	// }
	//	
	// private void build()
	// {
	// list.setBorders(false);
	// DataListItem item = new DataListItem("Loading...");
	// item.disable();
	// list.add(item);
	// setText("Create Draft Assessments for Selected Taxa");
	// final WorkingSet ws = WorkingSetCache.impl.getCurrentWorkingSet();
	// AssessmentCache.impl.fetchDraftList(ws.getSpeciesIDsAsString(), true, new
	// GenericCallback<String>(){
	// public void onFailure(Throwable caught) {
	// list.removeAll();
	// DataListItem item = new DataListItem("Error fetching taxa.");
	// item.disable();
	// list.add(item);
	// getButtonById(Dialog.OK).disable();
	// }
	// public void onSuccess(String arg0) {
	// list.removeAll();
	// for (int i = 0; i < ws.getSpeciesIDs().size(); i++){
	// String tempID = (String)ws.getSpeciesIDs().get(i);
	// Assessment temp = AssessmentCache.impl.getAssessment(tempID,
	// AssessmentType.DRAFT_ASSESSMENT_TYPE, false);
	// if (temp == null){
	// Taxon  node = TaxonomyCache.impl.getNode(tempID);
	// DataListItem item = new DataListItem(node.getFullName());
	// item.setId(tempID);
	// if (id != null && tempID.equals(id)){
	// item.setChecked(true);
	// }
	// else
	// item.setChecked(false);
	// list.add(item);
	// }
	// }
	//				
	// if (list.getItemCount() == 0){
	// list.removeAll();
	// DataListItem item = new
	// DataListItem("No taxa available to create drafts.");
	// item.disable();
	// list.add(item);
	// getButtonById(Dialog.OK).disable();
	//					
	// }
	// }
	// });
	//		
	//		
	// list.setSize(350, 300);
	// list.addStyleName("gwt-background");
	// content.add(list);
	//		
	// addButton("Select All", new SelectionListener(){
	// public void widgetSelected(BaseEvent be) {
	// for (int i = 0; i < list.getItemCount(); i++){
	// list.getItem(i).setChecked(true);
	// }
	// }
	// });
	// addButton("Deselect All", new SelectionListener(){
	// public void widgetSelected(BaseEvent be) {
	// for (int i = 0; i < list.getItemCount(); i++){
	// list.getItem(i).setChecked(false);
	// }
	// }
	// });
	// getButtonById(Dialog.OK).setText("Create Draft Assessment");
	// getButtonById(Dialog.OK).addSelectionListener(new SelectionListener(){
	// public void widgetSelected(BaseEvent be) {
	// createDraftAssessments();
	// getButtonBar().disable();
	// }
	// });
	// getButtonById(Dialog.CANCEL_ID).setText("Cancel");
	// }
	//	
	// private void createDraftAssessments()
	// {
	// DataListItem [] items = list.getChecked();
	//		
	// if (items.length > 0)
	// {
	// StringBuffer csvIDs = new StringBuffer();
	// StringBuffer names = new StringBuffer();
	// for (int i = 0; i < items.length; i++){
	// csvIDs.append(items[i].getId() + ",");
	// names.append(items[i].getText() + ", ");
	// }
	// AssessmentCache.impl.createNewDraftAssessments(csvIDs.substring(0,
	// csvIDs.length()-1));
	// Info.display("Assessments Added", "Draft Assessments added to " +
	// names.substring(0, names.length()-2), "");
	// manager.workingSetFullPanel.refreshAssessmentTable(csvIDs.substring(0,
	// csvIDs.length()-1));
	// hide();
	// }
	// else
	// {
	// hide();
	// Info.display( new InfoConfig("No Draft Assessments Created",
	// "No taxa were selected to add draft assessments") );
	// }
	// }
}
