package org.iucn.sis.client.panels.assessments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.caches.SchemaCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.SchemaCache.AssessmentSchema;
import org.iucn.sis.client.api.ui.models.region.RegionModel;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.region.AddRegionPanel;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableDraftAssessment;
import org.iucn.sis.shared.api.acl.feature.AuthorizablePublishedAssessment;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Region;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.utils.AssessmentFormatter;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.solertium.lwxml.shared.GWTConflictException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class NewAssessmentPanel extends Window implements DrawsLazily {

	private ListBox type = null;
	private ListBox template = null;
	private ListBox schema = null;
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
	private Label schemaLabel = null;
	private Label regionLabel = null;
	private Label endemicLabel = null;

	private Label templateLable = null;

	private TextBox newDescription = new TextBox();

	private final Taxon node;
	private boolean canCreateDraft;
	private boolean canCreateDraftRegional;
	private boolean canCreateDraftGlobal;

	public NewAssessmentPanel() {
		this(TaxonomyCache.impl.getCurrentTaxon());
	}
	
	public NewAssessmentPanel(Taxon taxon) {
		this.node = taxon;
	}

	private void build() {
		buildType();
		buildSchema();
		buildRegionWidgets();
		buildSaveButton();
		buildTemplate(); 
		
		TableLayout layout = new TableLayout(2);
		layout.setCellSpacing(10);
		TableData leftData = new TableData("25%", "100%");
		TableData rightData = new TableData("65%", "100%");
		TableData span = new TableData();
		span.setColspan(2);

		final LayoutContainer container = new LayoutContainer(layout);
		
		container.add(typeLabel, leftData);
		container.add(type, rightData);
		
		if (schema != null) {
			container.add(schemaLabel, leftData);
			container.add(schema, rightData);
		}
		
		container.add(templateLable, leftData);
		container.add(template, rightData);
		
		container.add(regionLabel, leftData);
		container.add(regionsPanel, rightData);
		
		container.add(endemicLabel, leftData);
		container.add(endemic, rightData);
		
		regionsPanel.draw();
		
		add(container);
		addButton(createAssessment);
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
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
		endemic.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				if( endemic.getSelectedIndex() != 2 )
					for( Entry<ComboBox<RegionModel>, RegionModel> cur : regionsPanel.getBoxesToSelected().entrySet() ) {
						if( cur.getValue().getRegion().getId() == (Region.GLOBAL_ID) ) {
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
	
	private void updateTemplates(String schema) {
		template.clear();
		template.insertItem("blank", 0);
		for (Assessment data : AssessmentCache.impl.getPublishedAssessmentsForTaxon(node.getId())) {
			if (!schema.equals(data.getSchema(SchemaCache.impl.getDefaultSchema())))
				continue;
			
			String displayable = "Published -- ";

			displayable += data.getDateAssessed();

			if (data.isRegional())
				displayable += " --- " + RegionCache.impl.getRegionName(data.getRegionIDs());
			else
				displayable += " --- " + "Global";

			displayable += " --- " + AssessmentFormatter.getProperCategoryAbbreviation(data);
			template.addItem(displayable, data.getId()+"");
		}
		for (Assessment data : AssessmentCache.impl.getDraftAssessmentsForTaxon(node.getId())) {
			if (!schema.equals(data.getSchema(SchemaCache.impl.getDefaultSchema())))
				continue;
			
			String displayable = "Draft -- ";

			displayable += data.getDateAssessed();

			if (data.isRegional())
				displayable += " --- " + RegionCache.impl.getRegionName(data.getRegionIDs());
			else
				displayable += " --- " + "Global";

			displayable += " --- " + AssessmentFormatter.getProperCategoryAbbreviation(data);
			template.addItem(displayable, data.getId()+"");
		}

		// TODO: ADD USER ONES		
	}

	private void buildTemplate() {
		template = new ListBox(false);
		templateLable = new Label("From: ");

		updateTemplates(SchemaCache.impl.getDefaultSchema());
	}

	private void buildType() {
		type = new ListBox(false);
		type.insertItem("", 0);
		

		canCreateDraft = AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.CREATE, new AuthorizableDraftAssessment(node));
		canCreateDraftRegional = AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.CREATE, new AuthorizableDraftAssessment(node, "0"));
		canCreateDraftGlobal = AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.CREATE, new AuthorizableDraftAssessment(node, Region.GLOBAL_ID+""));
		
		if (canCreateDraft || canCreateDraftRegional || canCreateDraftGlobal )
			type.insertItem(AssessmentType.DRAFT_ASSESSMENT_TYPE, type.getItemCount());
		if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.CREATE, new AuthorizablePublishedAssessment(node)))
			type.insertItem(AssessmentType.PUBLISHED_ASSESSMENT_TYPE, type.getItemCount());

		typeLabel = new Label("Assessment Type: ");
	}
	
	private void buildSchema() {
		List<AssessmentSchema> list = SchemaCache.impl.listFromCache();
		if (!list.isEmpty()) {
			schema = new ListBox();
			int selection = -1, index = 0;
			for (AssessmentSchema current : list) {
				schema.addItem(current.getName(), current.getId());
				if (SchemaCache.impl.getDefaultSchema().equals(current.getId()))
					selection = index;
				
				index++;
			}
			schema.setSelectedIndex(selection);
			schema.addChangeHandler(new ChangeHandler() {
				public void onChange(ChangeEvent event) {
					updateTemplates(schema.getValue(schema.getSelectedIndex()));
				}
			});
		
			schemaLabel = new Label("Assessment Schema: ");
		}
	}

	private String checkValidity() {
		String error = null;

		if (type.getSelectedIndex() == 0)
			error = "Please select an assessment type.";
		else if (type.getItemText(type.getSelectedIndex()).equals(AssessmentType.DRAFT_ASSESSMENT_TYPE)) {
			List<Integer> locality = new ArrayList<Integer>();
			boolean isEndemic = false;

			HashMap<ComboBox<RegionModel>, RegionModel> regionMap = regionsPanel.getBoxesToSelected();
				
			if (regionMap.isEmpty()) {
				error = "Please select a region.";
			} else if (endemic.getSelectedIndex() == 0) {
				error = "Please select whether the new assessment should be endemic.";
			} else {
				isEndemic = endemic.getItemText(endemic.getSelectedIndex()).equalsIgnoreCase("yes");

				for (Entry<ComboBox<RegionModel>, RegionModel> cur : regionMap.entrySet())
					locality.add(cur.getValue().getRegion().getId());
				
				if (locality.contains(Region.GLOBAL_ID) && !isEndemic) {
					WindowUtils.infoAlert("Global is Endemic", "A Global assessment must be " +
						"also flagged endemic. This has been fixed for you.");
					endemic.setSelectedIndex(2);
				}
			}

			if (error == null) {
				String selectedSchema = schema.getValue(schema.getSelectedIndex());
				
				Set<Assessment> checkAgainst = AssessmentCache.impl.
					getAssessmentsForTaxon(node.getId(), AssessmentType.DRAFT_ASSESSMENT_STATUS_ID, selectedSchema);
				
				for (Assessment cur : checkAgainst) {
					if ((cur.isEndemic() || cur.isGlobal()) && isEndemic)
						error = "Only one draft assessment for each taxon may exist that is either endemic " +
						" or global.";
					else {
						for (Integer curLocality : locality)
							if (cur.getRegionIDs().contains(curLocality)) {
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
		List<Integer> locality = new ArrayList<Integer>();
		
		//Check locality things
		isEndemic = this.endemic.getItemText(this.endemic.getSelectedIndex()).equalsIgnoreCase("yes");

		for( Entry<ComboBox<RegionModel>, RegionModel> cur : regionsPanel.getBoxesToSelected().entrySet() )
			locality.add(cur.getValue().getRegion().getId());
		
		final String selSchema;
		if (schema.getSelectedIndex() != -1)
			selSchema = schema.getValue(schema.getSelectedIndex());
		else
			selSchema = null;
		
		// CHECK TEMPLATE
		if (template.getSelectedIndex() != 0) {
			final Integer assessmentID = Integer.valueOf(template.getValue(template.getSelectedIndex()));
			/*final String assessmentType = template.getItemText(template.getSelectedIndex()).substring(0,
				template.getItemText(template.getSelectedIndex()).indexOf('-')).trim().toLowerCase() + "_status";*/

			final boolean endemicArg = isEndemic;
			final List<Integer> localityArg = locality;
			
			AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(assessmentID), 
					new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("An error occurred fetching the template Assessment"
							+ " you chose. Please report this error to an SIS Administrator.");
				}

				public void onSuccess(String result) {
					Assessment data = AssessmentCache.impl.getAssessment(assessmentID, false);
					doCreate(endemicArg, selSchema, data, localityArg);
				}
			});
		} else
			doCreate(isEndemic, selSchema, null, locality);
	}

	private void doCreate(boolean isEndemic, String schema, Assessment theTemplate, List<Integer> locality) {
		AssessmentCache.impl.createNewAssessment(node, type.getItemText(type.getSelectedIndex()), 
				schema, theTemplate, locality, isEndemic, new GenericCallback<String>() {

			public void onFailure(Throwable caught) {
				String message = "An error occurred while creating an assessment for " + node.getFullName() + ".";
				if( caught instanceof GWTConflictException )
					message += " An assessment with the specified Region set already exists.";
				WindowUtils.errorAlert(message);
			}

			public void onSuccess(String newID) {
				WindowManager.get().hideAll();
				//ClientUIContainer.bodyContainer.setSelection(ClientUIContainer.bodyContainer.tabManager.assessmentEditor);
				//WindowUtils.infoAlert("An assessment for " + node.getFullName() + " has been created.");
			}

		});
	}
	
	@Override
	public void show() {
		draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				open();
			}
		});
	}
	
	private void open() {
		super.show();
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		if (node == null) {
			WindowUtils.errorAlert("You must have a taxon selected first.");
		} else {
			setHeading("New " + node.getFullName() + " Assessment");
			setSize(550, 300);
			setLayout(new FitLayout());
			setButtonAlign(HorizontalAlignment.CENTER);
			AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, node.getId()), new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					WindowUtils
							.errorAlert("Error fetching template assessments! Please "
									+ "check your Internet connectivity if you are using SIS online, "
									+ "or ensure your local server is still running if you are using "
									+ "the offline version.");
				}

				public void onSuccess(String result) {
					SchemaCache.impl.list(new ComplexListener<List<AssessmentSchema>>() {
						public void handleEvent(List<AssessmentSchema> eventData) {
							build();
							
							callback.isDrawn();
						}
					});
				}
			});
		}
	}

	private void refreshRegionStore() {
		regions.removeAll();
		
		for (Region cur : RegionCache.impl.getRegions()) {
			if( canCreateDraft )
				regions.add(new RegionModel(cur));
			else if( cur.getId() == (Region.GLOBAL_ID) && canCreateDraftGlobal )
				regions.add(new RegionModel(cur));
			else if( canCreateDraftRegional )
				regions.add(new RegionModel(cur));
		}
		
		regionsPanel.setSelectedRegions(Arrays.asList(Region.getGlobalRegion()));
	}
}
