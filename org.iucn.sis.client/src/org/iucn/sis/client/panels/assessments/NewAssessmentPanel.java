package org.iucn.sis.client.panels.assessments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.FetchMode;
import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.caches.SchemaCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.SchemaCache.AssessmentSchema;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableDraftAssessment;
import org.iucn.sis.shared.api.acl.feature.AuthorizablePublishedAssessment;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Region;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.comparators.AssessmentDateComparator;
import org.iucn.sis.shared.api.utils.AssessmentFormatter;
import org.iucn.sis.shared.api.utils.AssessmentUtils;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.solertium.lwxml.shared.GWTConflictException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class NewAssessmentPanel extends BasicWindow implements DrawsLazily {
	
	private static final int WIDTH = 300;

	private final boolean canCreateDraft;
	private final boolean canCreateDraftRegional;
	private final boolean canCreateDraftGlobal;
	private final boolean canCreatePublished;
	private final Taxon node;
	
	private ComboBox<NameValueModelData> type = null;
	private ComboBox<NameValueModelData> template = null;
	private ComboBox<NameValueModelData> schema = null;
	private CheckBoxListView<NameValueModelData> regions = null;
	private ComboBox<NameValueModelData> endemic = null;

	public NewAssessmentPanel() {
		this(TaxonomyCache.impl.getCurrentTaxon());
	}
	
	public NewAssessmentPanel(Taxon taxon) {
		super("New " + taxon.getFullName() + " Assessment");
		setSize(500, 400);
		setLayout(new FitLayout());
		
		this.node = taxon;
		
		String defaultSchema = SchemaCache.impl.getDefaultSchema();
		this.canCreateDraft = AuthorizationCache.impl.hasRight(AuthorizableObject.CREATE, new AuthorizableDraftAssessment(node, defaultSchema));
		this.canCreateDraftRegional = AuthorizationCache.impl.hasRight(AuthorizableObject.CREATE, new AuthorizableDraftAssessment(node, defaultSchema, "0"));
		this.canCreateDraftGlobal = AuthorizationCache.impl.hasRight(AuthorizableObject.CREATE, new AuthorizableDraftAssessment(node, defaultSchema, "global"));
		this.canCreatePublished = AuthorizationCache.impl.hasRight(AuthorizableObject.CREATE, new AuthorizablePublishedAssessment(node, defaultSchema));
	}

	private void build() {
		buildType();
		buildSchema();
		buildRegionWidgets();
		buildTemplate(); 
		
		TableLayout layout = new TableLayout(2);
		layout.setCellSpacing(10);
		
		TableData leftData = new TableData("25%", "100%");
		leftData.setVerticalAlign(VerticalAlignment.TOP);
		
		TableData rightData = new TableData("65%", "100%");
		
		type.setWidth(WIDTH);

		final LayoutContainer container = new LayoutContainer(layout);
		
		container.add(new Html("Assessment Type:"), leftData);
		container.add(type, rightData);
		
		if (schema != null) {
			schema.setWidth(WIDTH);
			
			container.add(new Html("Assessment Schema:"), leftData);
			container.add(schema, rightData);
		}
		
		template.setWidth(WIDTH);
		container.add(new Html("From:"), leftData);
		container.add(template, rightData);
		
		regions.setWidth(WIDTH);
		container.add(new Html("Region:"), leftData);
		container.add(regions, rightData);
		
		endemic.setWidth(WIDTH);
		container.add(new Html("Is Endemic?"), leftData);
		container.add(endemic, rightData);
		
		add(container);
		addButton(new Button("Create Assessment", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				String message = null;
				message = checkValidity();
				if (message != null) {
					WindowUtils.errorAlert(message);
				} else
					createNewAssessment();			
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}

	private void buildRegionWidgets() {
		NameValueModelData endemicDefault = null;
		
		List<NameValueModelData> list = new ArrayList<NameValueModelData>();
		list.add(new NameValueModelData("No", "no"));
		list.add(endemicDefault = new NameValueModelData("Yes", "yes"));
		
		endemic = FormBuilder.createModelComboBox("endemic", null, "", false, list.toArray(new NameValueModelData[list.size()]));
		endemic.setValue(endemicDefault);
		endemic.addSelectionChangedListener(new SelectionChangedListener<NameValueModelData>() {
			public void selectionChanged(SelectionChangedEvent<NameValueModelData> se) {
				NameValueModelData selection = se.getSelectedItem();
				if (selection == null)
					return;
				
				if (!selection.getValue().equals("yes")) {
					//Must be global
					List<NameValueModelData> checked = regions.getChecked();
					for (NameValueModelData current : checked) {
						if (current.getValue().equals(""+Region.GLOBAL_ID)) {
							WindowUtils.infoAlert("Must be Endemic", "An assessment flagged as global " +
									"must also be flagged as endemic. Please remove the global tag if " +
									"you wish to make this to non-endemic.");
							endemic.setValue(endemic.getStore().getAt(1));
							break;
						}
					}
				}
			}
		});
		
		ListStore<NameValueModelData> models = new ListStore<NameValueModelData>();
		models.setStoreSorter(new RegionSorter());
		
		NameValueModelData selection = null;
		for (Region cur : RegionCache.impl.getRegions()) {
			NameValueModelData model = null;
			if (canCreateDraft || canCreatePublished || 
					cur.getId() == Region.GLOBAL_ID && canCreateDraftGlobal || 
					cur.getId() != Region.GLOBAL_ID && canCreateDraftRegional) {
				model = new NameValueModelData(cur.getName(), cur.getId() + "");
				models.add(model);
				if (cur.getId() == Region.GLOBAL_ID)
					selection = model;
			}
		}
		
		models.sort("text", SortDir.ASC);
		
		regions = new CheckBoxListView<NameValueModelData>();
		regions.setStore(models);
		regions.setChecked(selection, true);
		regions.setHeight(160);
		
		updateRegions(SchemaCache.impl.getDefaultSchema());
	}
	
	private void updateTemplates(String schema) {
		NameValueModelData blank;
		List<NameValueModelData> models = new ArrayList<NameValueModelData>();
		models.add(blank = new NameValueModelData("blank", "0"));
		
		final List<Assessment> published = new ArrayList<Assessment>(
			AssessmentCache.impl.getPublishedAssessmentsForTaxon(node.getId())
		);
		Collections.sort(published, new AssessmentDateComparator());
		for (Assessment data : published) {
			if (!schema.equals(data.getSchema(SchemaCache.impl.getDefaultSchema())))
				continue;
			
			String displayable = "Published -- ";
			
			displayable += data.getDateAssessed() == null ? "No date assessed" : 
				FormattedDate.FULL.getDate(data.getDateAssessed());

			if (data.isRegional())
				displayable += " --- " + RegionCache.impl.getRegionName(data.getRegionIDs());
			else
				displayable += " --- " + "Global";

			displayable += " --- " + AssessmentFormatter.getProperCategoryAbbreviation(data);
			
			models.add(new NameValueModelData(displayable, data.getId()+""));
		}
		
		final List<Assessment> drafts = new ArrayList<Assessment>(
			AssessmentCache.impl.getDraftAssessmentsForTaxon(node.getId())
		);
		Collections.sort(drafts, new AssessmentDateComparator());
		for (Assessment data : drafts) {
			if (!schema.equals(data.getSchema(SchemaCache.impl.getDefaultSchema())))
				continue;
			
			String displayable = "Draft -- ";

			displayable += data.getDateAssessed() == null ? "No date assessed" : 
				FormattedDate.FULL.getDate(data.getDateAssessed());

			if (data.isRegional())
				displayable += " --- " + RegionCache.impl.getRegionName(data.getRegionIDs());
			else
				displayable += " --- " + "Global";

			displayable += " --- " + AssessmentFormatter.getProperCategoryAbbreviation(data);
			
			models.add(new NameValueModelData(displayable, data.getId()+""));
			//template.addItem(displayable, data.getId()+"");
		}
		
		template = FormBuilder.createModelComboBox("template", null, "", false, models.toArray(new NameValueModelData[models.size()]));
		template.setValue(blank);
	}

	private void buildTemplate() {
		updateTemplates(SchemaCache.impl.getDefaultSchema());
	}

	private void buildType() {
		List<NameValueModelData> models = new ArrayList<NameValueModelData>();
		
		if (canCreateDraft || canCreateDraftRegional || canCreateDraftGlobal ) {
			String type = AssessmentType.DRAFT_ASSESSMENT_TYPE;
			models.add(new NameValueModelData(
				AssessmentType.getAssessmentType(type).getDisplayName(true), type
			));
		}
		if (canCreatePublished) {
			String type = AssessmentType.PUBLISHED_ASSESSMENT_TYPE;
			models.add(new NameValueModelData(
				AssessmentType.getAssessmentType(type).getDisplayName(true), type
			));
		}

		type = FormBuilder.createModelComboBox("type", null, "", false, models.toArray(new NameValueModelData[models.size()]));
	}
	
	private void buildSchema() {
		List<AssessmentSchema> list = SchemaCache.impl.listFromCache();
		if (!list.isEmpty()) {
			List<NameValueModelData> models = new ArrayList<NameValueModelData>();
			NameValueModelData selection = null;
			for (AssessmentSchema current : list) {
				NameValueModelData model = new NameValueModelData(current.getName(), current.getId()); 
				models.add(model);
				if (SchemaCache.impl.getDefaultSchema().equals(current.getId()))
					selection = model;
			}
			
			schema = FormBuilder.createModelComboBox("schema", null, "", false, models.toArray(new NameValueModelData[models.size()]));
			schema.setValue(selection);
			schema.addSelectionChangedListener(new SelectionChangedListener<NameValueModelData>() {
				public void selectionChanged(SelectionChangedEvent<NameValueModelData> se) {
					NameValueModelData selection = se.getSelectedItem();
					if (selection != null) {
						updateTemplates(selection.getValue());
						updateRegions(selection.getValue());
					}
				}
			});
		}
	}
	
	private void updateRegions(String schema) {
		boolean visible = !schema.endsWith("usetrade");
		
		regions.setEnabled(visible);
		endemic.setEnabled(visible);
	}

	private String checkValidity() {
		String error = null;

		if (type.getValue() == null)
			error = "Please select an assessment type.";
		else if (type.getValue().getValue().equals(AssessmentType.DRAFT_ASSESSMENT_TYPE)) {
			List<Integer> locality = new ArrayList<Integer>();
			boolean isEndemic = false;

			if (regions.isEnabled()) {
				List<NameValueModelData> regionMap = regions.getChecked();
				
				if (regionMap.isEmpty()) {
					error = "Please select a region.";
				} else if (endemic.getValue() == null) {
					error = "Please select whether the new assessment should be endemic.";
				} else {
					isEndemic = endemic.getValue().getValue().equalsIgnoreCase("yes");
	
					for (NameValueModelData model : regionMap)
						locality.add(Integer.valueOf(model.getValue()));
					
					if (locality.contains(Region.GLOBAL_ID) && !isEndemic) {
						WindowUtils.infoAlert("Global is Endemic", "A Global assessment must be " +
							"also flagged endemic. This has been fixed for you.");
						endemic.setValue(endemic.getStore().getAt(1));
					}
				}
			
				if (error == null) {
					String selectedSchema = schema.getValue().getValue();
					
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
		}
		return error;
	}

	private void createNewAssessment() {
		final Boolean isEndemic;
		final List<Integer> locality;
		
		if (regions.isEnabled()) {
			//Check locality things
			isEndemic = this.endemic.getValue().getValue().equalsIgnoreCase("yes");
	
			locality = new ArrayList<Integer>();
			for (NameValueModelData model : regions.getChecked())
				locality.add(Integer.valueOf(model.getValue()));
		}
		else {
			locality = null;
			isEndemic = null;
		}
		
		final String selSchema;
		if (schema.getValue() != null)
			selSchema = schema.getValue().getValue();
		else
			selSchema = null;
		
		// CHECK TEMPLATE
		if (template.getValue() != null && !template.getValue().getValue().equals("0")) {
			final Integer assessmentID = Integer.valueOf(template.getValue().getValue());
	
			AssessmentCache.impl.fetchAssessment(assessmentID, FetchMode.FULL, new GenericCallback<Assessment>() {
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("An error occurred fetching the template Assessment"
							+ " you chose. Please report this error to an SIS Administrator.");
				}
				public void onSuccess(Assessment data) {
					doCreate(isEndemic, selSchema, data, locality);
				}
			});
		} else
			doCreate(isEndemic, selSchema, null, locality);
	}

	private void doCreate(Boolean isEndemic, String schema, Assessment theTemplate, List<Integer> locality) {
		AssessmentUtils.createNewAssessment(node, type.getValue().getValue(), 
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
		if (!(canCreateDraft || canCreateDraftGlobal || canCreateDraftRegional || canCreatePublished))
			WindowUtils.errorAlert("Sorry, you do not have permission to create neither draft nor published assessments.");
		else {
			draw(new DrawsLazily.DoneDrawingCallback() {
				public void isDrawn() {
					open();
				}
			});
		}
	}
	
	private void open() {
		super.show();
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		if (node == null) {
			WindowUtils.errorAlert("You must have a taxon selected first.");
		} else {
			AssessmentCache.impl.fetchPartialAssessmentsForTaxon(node.getId(), new GenericCallback<String>() {
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
	
	private static class NameValueModelData extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		public NameValueModelData(String name, String value) {
			super();
			set("text", name);
			set("value", value);
		}
		
		public String getName() {
			return get("text");
		}
		
		public String getValue() {
			return get("value");
		}
		
	}
	
	private static class RegionSorter extends StoreSorter<NameValueModelData> {
		
		private final PortableAlphanumericComparator comparator;
		
		public RegionSorter() {
			comparator = new PortableAlphanumericComparator();
		}
		
		@Override
		public int compare(Store<NameValueModelData> store, NameValueModelData m1, NameValueModelData m2, String property) {
			String r1 = m1.getValue();
			String r2 = m2.getValue();
			
			if (r1.equals(r2))
				return 0;
			else if (r1.equals(""+Region.GLOBAL_ID))
				return -1;
			else if (r2.equals(""+Region.GLOBAL_ID))
				return 1;
		
			return comparator.compare(m1.getName(), m2.getName());
		}
		
	}
	
}
