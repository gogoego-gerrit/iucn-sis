package org.iucn.sis.client.components.panels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.components.panels.region.AddRegionPanel;
import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.data.assessments.AssessmentFetchRequest;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.acl.feature.AuthorizableDraftAssessment;
import org.iucn.sis.shared.acl.feature.AuthorizablePublishedAssessment;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.Region;
import org.iucn.sis.shared.data.assessments.RegionCache;
import org.iucn.sis.shared.data.assessments.RegionCache.RegionModel;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GWTConflictException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class NewAssessmentPanel extends LayoutContainer {

	private ListBox type = null;
	private ListBox template = null;
//	private RadioButton isGlobal = null;
//	private RadioButton isRegional = null;
//	private ComboBox<RegionModel> region = null;
//	private HorizontalPanel regionPanel = null;
	
	private AddRegionPanel regionsPanel = null;
	
//	private IconButton addRegionButton = null;
	private ListStore<RegionModel> regions = null;

	private ListBox endemic = null;
	private Button createAssessment = null;
	private Label typeLabel = null;
	private Label regionLabel = null;
	private Label endemicLabel = null;

	private Label templateLable = null;

	private TextBox newDescription = new TextBox();

	private TaxonNode node = null;
	private boolean canCreateDraft;
	private boolean canCreateDraftRegional;
	private boolean canCreateDraftGlobal;

	public NewAssessmentPanel(PanelManager manager) {
		refresh();
	}

	private void build() {
		removeAll();
		buildType();
		buildRegionWidgets();
		buildSaveButton();
		buildTemplate();

		TableLayout layout = new TableLayout(2);
		layout.setCellSpacing(10);
		TableData leftData = new TableData("25%", "100%");
		TableData rightData = new TableData("65%", "100%");
		TableData span = new TableData();
		span.setColspan(2);

		setLayout(layout);
		add(typeLabel, leftData);
		add(type, rightData);
		add(templateLable, leftData);
		add(template, rightData);
		add(regionLabel, leftData);
		add(regionsPanel, rightData);
		add(endemicLabel, leftData);
		add(endemic, rightData);
		add(createAssessment, span);
		regionsPanel.draw();
		layout();
	}

	private void buildRegionWidgets() {
		regions = new ListStore<RegionModel>();
		
		newDescription.setSize("100%", "45px");

		regionLabel = new Label("Region: ");
		regionsPanel = new AddRegionPanel();
		
		endemic = new ListBox(false);
		endemic.insertItem("", 0);
		endemic.insertItem("no", 1);
		endemic.insertItem("yes", 2);
		endemic.setSelectedIndex(2);
		endemic.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				if( endemic.getSelectedIndex() != 2 )
					for( Entry<ComboBox<RegionModel>, RegionModel> cur : regionsPanel.getBoxesToSelected().entrySet() ) {
						if( cur.getValue().getRegion().getId().equals(AssessmentData.GLOBAL_ID) ) {
							WindowUtils.infoAlert("Must be Endemic", "An assessment flagged as global " +
									"must also be flagged as endemic. Please remove the global tag if " +
									"you wish to make this to non-endemic.");
							endemic.setSelectedIndex(2);
							break;
						}
					}						
			}
		});
		
		endemicLabel = new Label("Is Endemic? ");
		refreshRegionStore();
	}

	private void buildSaveButton() {
		createAssessment = new Button("Create Assessment");

		SelectionListener<ButtonEvent> listener = new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				String message = null;
				message = checkValidity();
				if (message != null) {
					WindowUtils.errorAlert(message);
				} else
					createNewAssessment();
			}

		};
		createAssessment.addSelectionListener(listener);

	}

	private void buildTemplate() {
		template = new ListBox(false);
		templateLable = new Label("From: ");

		template.insertItem("blank", 0);
		for (int i = 0; i < node.getAssessments().size(); i++) {
			AssessmentData data = AssessmentCache.impl.getPublishedAssessment(node.getAssessments().get(i), false);
			String displayable = "Published -- ";

			displayable += data.getDateAssessed();

			if (data.isRegional())
				displayable += " --- " + RegionCache.impl.getRegionName(data.getRegionIDs());
			else
				displayable += " --- " + "Global";

			displayable += " --- " + data.getProperCategoryAbbreviation();
			template.addItem(displayable, data.getAssessmentID());
		}
		List<AssessmentData> drafts = AssessmentCache.impl.getDraftAssessmentsForTaxon(node.getId() + "");
		for (AssessmentData data : drafts) {
			String displayable = "Draft -- ";

			displayable += data.getDateAssessed();

			if (data.isRegional())
				displayable += " --- " + RegionCache.impl.getRegionName(data.getRegionIDs());
			else
				displayable += " --- " + "Global";

			displayable += " --- " + data.getProperCategoryAbbreviation();
			template.addItem(displayable, data.getAssessmentID());
		}

		// TODO: ADD USER ONES

	}

	private void buildType() {
		type = new ListBox(false);
		type.insertItem("", 0);
		type.insertItem(BaseAssessment.USER_ASSESSMENT_STATUS, type.getItemCount());

		canCreateDraft = AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.CREATE, new AuthorizableDraftAssessment(node));
		canCreateDraftRegional = AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.CREATE, new AuthorizableDraftAssessment(node, "0"));
		canCreateDraftGlobal = AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.CREATE, new AuthorizableDraftAssessment(node, AssessmentData.GLOBAL_ID));
		
		if (canCreateDraft || canCreateDraftRegional || canCreateDraftGlobal )
			type.insertItem(BaseAssessment.DRAFT_ASSESSMENT_STATUS, type.getItemCount());
		if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.CREATE, new AuthorizablePublishedAssessment(node)))
			type.insertItem(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, type.getItemCount());

		typeLabel = new Label("Assessment Type: ");
	}

	private String checkValidity() {
		String error = null;

		if( type.getItemText(type.getSelectedIndex()).equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS) ) {
			List<String> locality = new ArrayList<String>();
			boolean isEndemic = false;

			if (type.getSelectedIndex() == 0) {
				error = "Please select an assessment type.";
			} else {
				HashMap<ComboBox<RegionModel>, RegionModel> regionMap = regionsPanel.getBoxesToSelected();
					
				if (regionMap.size() == 0) {
					error = "Please select a region.";
				} else if (endemic.getSelectedIndex() == 0) {
					error = "Please select whether the new assessment should be endemic.";
				} else {
					isEndemic = endemic.getItemText(endemic.getSelectedIndex()).equalsIgnoreCase("yes");

					for( Entry<ComboBox<RegionModel>, RegionModel> cur : regionMap.entrySet() )
						locality.add(cur.getValue().getRegion().getId());
					
					if( locality.contains(AssessmentData.GLOBAL_ID ) && !isEndemic ) {
						WindowUtils.infoAlert("Global is Endemic", "A Global assessment must be " +
							"also flagged endemic. This has been fixed for you.");
						endemic.setSelectedIndex(2);
					}
				}
			}

			if( error == null ) {
				List<AssessmentData> checkAgainst = AssessmentCache.impl.getDraftAssessmentsForTaxon(node);
				for( AssessmentData cur : checkAgainst ) {
					if( (cur.isEndemic() || cur.isGlobal()) && isEndemic )
						error = "Only one draft assessment for each taxon may exist that is either endemic " +
						" or global.";
					else {
						for( String curLocality : locality )
							if( cur.getRegionIDs().contains(curLocality) ) {
								error = "An assessment exists that contains the locality " + 
									RegionCache.impl.getRegionName(curLocality) + ". " +
										"Only one assessment may use each locality.";
								break;
							}
					}
 				}
			}
		}
		return error;
	}

	private void createNewAssessment() {
		boolean isEndemic = false;
		List<String> locality = new ArrayList<String>();
		
		//Check locality things
		isEndemic = this.endemic.getItemText(this.endemic.getSelectedIndex()).equalsIgnoreCase("yes");

		for( Entry<ComboBox<RegionModel>, RegionModel> cur : regionsPanel.getBoxesToSelected().entrySet() )
			locality.add(cur.getValue().getRegion().getId());
		
		// CHECK TEMPLATE
		if (template.getSelectedIndex() != 0) {
			final String assessmentID = template.getValue(template.getSelectedIndex());
			final String assessmentType = template.getItemText(template.getSelectedIndex()).substring(0,
				template.getItemText(template.getSelectedIndex()).indexOf('-')).trim().toLowerCase() + "_status";

			final boolean endemicArg = isEndemic;
			final List<String> localityArg = locality;
			
			AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(assessmentID + "_" + assessmentType), 
					new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("An error occurred fetching the template Assessment"
							+ " you chose. Please report this error to an SIS Administrator.");
				}

				public void onSuccess(String result) {
					AssessmentData data = AssessmentCache.impl.getAssessment(assessmentType, assessmentID, false);
					doCreate(endemicArg, data, localityArg);
				}
			});
		} else
			doCreate(isEndemic, null, locality);
	}

	private void doCreate(boolean isEndemic, AssessmentData theTemplate, List<String> locality) {
		AssessmentCache.impl.createNewAssessment(node, type.getItemText(type.getSelectedIndex()), 
				theTemplate, locality, isEndemic, new GenericCallback<String>() {

			public void onFailure(Throwable caught) {
				String message = "An error occurred while creating an assessment for " + node.getFullName() + ".";
				if( caught instanceof GWTConflictException )
					message += " An assessment with the specified Region set already exists.";
				WindowUtils.errorAlert(message);
			}

			public void onSuccess(String newID) {
				WindowManager.get().hideAll();
				ClientUIContainer.bodyContainer.setSelection(ClientUIContainer.bodyContainer.tabManager.assessmentEditor);
				WindowUtils.infoAlert("An assessment for " + node.getFullName() + " has been created.");
			}

		});
	}

	private void refresh() {
		node = TaxonomyCache.impl.getCurrentNode();

		if (node == null) {
			WindowUtils.errorAlert("You must have a taxon selected first.");
		} else {
			AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, ""+node.getId()), new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					WindowUtils
							.errorAlert("Error fetching template assessments! Please "
									+ "check your Internet connectivity if you are using SIS online, "
									+ "or ensure your local server is still running if you are using "
									+ "the offline version.");
				}

				public void onSuccess(String result) {
					build();
				}
			});
		}

	}

	private void refreshRegionStore() {
		regions.removeAll();
		
		for (Region cur : RegionCache.impl.getRegions()) {
			if( canCreateDraft )
				regions.add(new RegionModel(cur));
			else if( cur.getId().equals(AssessmentData.GLOBAL_ID) && canCreateDraftGlobal )
				regions.add(new RegionModel(cur));
			else if( canCreateDraftRegional )
				regions.add(new RegionModel(cur));
		}
		
		regionsPanel.setRegionsSelected(AssessmentData.GLOBAL_ID);
	}
}
