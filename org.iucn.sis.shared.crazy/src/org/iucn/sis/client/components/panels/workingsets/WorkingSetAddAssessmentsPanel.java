package org.iucn.sis.client.components.panels.workingsets;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.components.panels.PanelManager;
import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.data.assessments.AssessmentFetchRequest;
import org.iucn.sis.client.ui.RefreshLayoutContainer;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.data.WorkingSetCache;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.data.assessments.AssessmentFilter;
import org.iucn.sis.shared.data.assessments.RegionCache;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class WorkingSetAddAssessmentsPanel extends RefreshLayoutContainer {

	private interface YesNoCallback {
		void onYes();
		void onNo();
	}
	
	private PanelManager manager;
	private HTML instructions;
	private DataList list;
	private Button add;
	private Button cancel;
	private ButtonBar buttons;
	private RadioButton addToSelected;
	private RadioButton addToEntireWorkingSet;
	private RadioButton published;
	private RadioButton empty;

	public WorkingSetAddAssessmentsPanel(PanelManager manager) {
		this.manager = manager;

		build();
	}

	private void build() {

		RowLayout layout = new RowLayout();
		// layout.setSpacing(10);
		// layout.setMargin(6);

		instructions = new HTML();
		list = new DataList();
		list.setCheckable(true);
		list.addStyleName("gwt-background");
		list.setScrollMode(Scroll.AUTO);

		add = new Button("Create Assessments", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				createNewAssessmentsIfNotExist();
			}
		});
		cancel = new Button("Cancel", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				cancel();
			}
		});

		
		addToEntireWorkingSet = new RadioButton("type", "Entire working set");
		addToEntireWorkingSet.addClickListener(new ClickListener() {
		
			public void onClick(Widget sender) {
				list.setVisible(addToSelected.isChecked());
		
			}

		});
		addToEntireWorkingSet.setChecked(true);
		addToSelected = new RadioButton("type", "Selected taxa (List of taxa may take a while to load)");
		addToSelected.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				list.setVisible(addToSelected.isChecked());
				if (addToSelected.isChecked())
					refreshList();
			}

		});
		VerticalPanel vp = new VerticalPanel();
		vp.add(new HTML("Would you like to add draft assessments to the entire working set, or selected species in the working set?"));
		VerticalPanel inner = new VerticalPanel();
		inner.setSpacing(10);
		inner.add(addToEntireWorkingSet);
		inner.add(addToSelected);
		vp.add(inner);
		buttons = new ButtonBar();
		buttons.add(add);
		buttons.add(cancel);

		addStyleName("gwt-background");


		published = new RadioButton("published", "Most Recently Published Assessment for Working " +
				"Set's defined region, or most recent global if no published exists for said region.");
		published.setChecked(true);
		empty = new RadioButton("published", "Empty Assessment");
		VerticalPanel vp2 = new VerticalPanel();
		vp2.add(new HTML("What template should the new draft assessments be based upon?"));
		VerticalPanel inner2 = new VerticalPanel();
		inner2.setSpacing(10);
		inner2.add(published);
		inner2.add(empty);
		vp2.add(inner2);

		setLayout(layout);
		add(instructions, new RowData(1d, -1));
		//		add(type, new RowData(1d, -1));
		
		add(vp2, new RowData(1d,-1));
		add(vp, new RowData(1d,-1));
		add(list, new RowData(1d, 1d));
		add(buttons, new RowData(1d, -1));

		layout();

		hideList();
	}

	private void cancel() {
		manager.workingSetBrowser.setManagerTab();
	}


	private void createNewAssessmentsIfNotExist() {
		add.disable();
		boolean useTemplate = published.isChecked();
		AssessmentFilter filter = WorkingSetCache.impl.getCurrentWorkingSet().getFilter().deepCopy();
		filter.setRecentPublished(true);
		filter.setDraft(false);
		filter.setAllPublished(false);
		if (filter.getRegionType().equalsIgnoreCase(AssessmentFilter.REGION_TYPE_OR)) {
			WindowUtils.errorAlert("Unable to create draft assessements for a working set with assessment scope \"ANY\".  Please temporarily change your assessment scope to \"ALL\".");
			return;
		}
		
		List<String> speciesID = null;
		if (addToSelected.isChecked()) {
			speciesID = new ArrayList<String>();
			for (DataListItem item : list.getItems()) {
				if (item.isChecked())
					speciesID.add(item.getId());
			}
			if (speciesID.size() == 0) {
				WindowUtils.errorAlert("Please specify the species that you would like to create assessments for.");
				return;
			}				
		}
		else {
			speciesID = WorkingSetCache.impl.getCurrentWorkingSet().getSpeciesIDs();
		}
		
		AssessmentCache.impl.createGlobalDraftAssessments(speciesID, useTemplate, filter, new GenericCallback<String>() {

			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("There was an error while creating the draft assessments <br/>: " + caught.getMessage());
				add.enable();
				hideList();
			}

			public void onSuccess(String arg0) {
				WindowUtils.infoAlert("Success", (String) arg0);
				cancel();
				add.enable();
				hideList();
			}

		});

	}


	private void hideList() {
		list.setVisible(false);
		buttons.setVisible(true);

	}

	@Override
	public void refresh() {

		final WorkingSetData ws = WorkingSetCache.impl.getCurrentWorkingSet();

		if (ws == null) {
			instructions.setHTML("<b>Instructions:</b> Please select a working set from the navigator which you would like"
					+ " to add draft assessments to.<br/><br/><br/>");
			add.setEnabled(false);
			
		} else if (ws.getFilter().getRegionType().equalsIgnoreCase(AssessmentFilter.REGION_TYPE_OR)) {
			instructions.setHTML("<b>Instructions:</b> Please change your working set assessment region scope to \"ALL\" before continuing.  " +
					"This operation does not support the \"ANY\" working set region scope.<br/><br/><br/>");
			add.setEnabled(false);
		}

		else {
			instructions
			.setHTML("<b>Instructions:</b> This operation will add draft assessments for the species in this working set.  The created " +
					"assessments will have a region of " + RegionCache.impl.getRegionNamesAsReadable(ws.getFilter()) + ".  " + 
					"Please either choose to create draft assessments for all " + " taxa in the working set "
					+ ws.getWorkingSetName() + " or select to add draft assessments individually to "
					+ "taxa.  If you choose to create draft assessments to the entire working set, a draft assessment "
					+ "will be created for each taxa in the working set.  However, if a draft assessment already exists, "
					+ "the current draft assessment will <i>not</i> be overwritten. <br/><br/><br/>");

			add.setEnabled(true);

		}

		layout();

	}

	private void refreshList() {
		showList();
		final WorkingSetData ws = WorkingSetCache.impl.getCurrentWorkingSet();
		list.removeAll();
		add.disable();

		if (ws != null) {
			AssessmentFetchRequest req = new AssessmentFetchRequest();
			req.addForTaxa(ws.getSpeciesIDs());
			AssessmentCache.impl.fetchAssessments(req, new GenericCallback<String>() {

				public void onFailure(Throwable caught) {
					list.add("Loading error ...");
					recalculate();
					layout();
				}

				public void onSuccess(String arg0) {
					for (int i = 0; i < ws.getSpeciesIDs().size(); i++) {
						String id = ws.getSpeciesIDs().get(i);
						DataListItem item = new DataListItem(TaxonomyCache.impl.getNode(id).getFullName());
						item.setId(id);
						if (AssessmentCache.impl.getDraftAssessment(id, false) == null) {
							list.add(item);
						}

					}

					add.enable();
					recalculate();
					layout();

				}

			});
		}

		else {
			instructions
					.setHTML("<b>Instructions:</b> Please select a working set from the navigator which you would like"
							+ " to add draft assessments to.");
			layout();
		}

	}

	private void showList() {
		list.setVisible(true);
		buttons.setVisible(true);
	}
}
