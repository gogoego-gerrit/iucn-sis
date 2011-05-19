package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.utils.TaxonomyBrowserPanel;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.utils.AssessmentFormatter;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.QuickButton;
import com.solertium.util.extjs.client.WindowUtils;

@SuppressWarnings("deprecation")
public class TaxomaticAssessmentMover extends TaxomaticWindow {

	private final Taxon source;
	private final Html htmlOfNodeToMoveAssessmentsINTO;
	private final DataList assessmentList;
	private final Button submitButton;
	
	private Taxon target; 

	public TaxomaticAssessmentMover(Taxon currentNode) {
		super("Move Assessments", "icon-document-move");
		
		this.source = currentNode;
		this.target = null;
		
		htmlOfNodeToMoveAssessmentsINTO = new Html("No taxon selected");
		assessmentList = new DataList();
		submitButton = new Button("Complete Move", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				onSubmit();
			};
		});
		load();
	}

	protected void addItem(Taxon target) {
		if (target == null)
			return;
		
		this.target = target;

		htmlOfNodeToMoveAssessmentsINTO.setHtml(target.getFullName());
	}

	protected VerticalPanel getRightSide() {
		VerticalPanel vp = new VerticalPanel();
		VerticalPanel top = new VerticalPanel();
		VerticalPanel middle = new VerticalPanel();
		VerticalPanel bottom = new VerticalPanel();

		top.setSpacing(5);
		top.add(new HTML("Taxon Selected: "));
		top.add(htmlOfNodeToMoveAssessmentsINTO);

		middle.setSpacing(5);
		middle.add(new HTML("Assessments To Move: "));
		assessmentList.setCheckable(true);
		assessmentList.setWidth("100%");
		assessmentList.setScrollMode(Scroll.AUTO);
		for (Assessment data : AssessmentCache.impl.getPublishedAssessmentsForTaxon(source.getId())) {
			DataListItem item = new DataListItem();
			String displayable = "Published -- ";

			displayable += data.getDateAssessed();

			if (data.isRegional())
				displayable += " --- " + RegionCache.impl.getRegionName(data.getRegionIDs());
			else
				displayable += " --- " + "Global";

			displayable += " --- " + AssessmentFormatter.getProperCategoryAbbreviation(data);
			item.setText(displayable);
			item.setData("id", data.getId());
			assessmentList.add(item);
		}
		middle.add(assessmentList);

		bottom.add(new Button("Complete Move", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				onSubmit();
			};
		}));

		vp.setSpacing(15);
		vp.add(top);
		vp.add(middle);
		vp.add(bottom);

		return vp;
	}

	protected void load() {
		LayoutContainer full = new LayoutContainer(new BorderLayout());
		
		TaxonomyBrowserPanel tp = new TaxonomyBrowserPanel() {
			@Override
			protected void addViewButtonToFootprint() {
				footprintPanel.add(new QuickButton("Add", new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						String display = footprints[footprints.length - 1];
						addItem(TaxonomyCache.impl.getTaxon(display));
					}
				}));
			}
		};
		if (source != null) {
			tp.update(source.getId() + "");
		} else {
			tp.update();
		}

		int size = TaxonChooser.PANEL_WIDTH / 2;

		LayoutContainer left = new LayoutContainer();
		left.setLayout(new FillLayout());
		left.setSize(size, TaxonChooser.PANEL_HEIGHT);
		left.add(tp);

		full.add(new HTML("<b> Instructions:</b> Select a taxon (species level or lower) and 1 or more "
				+ source.getFullName()
				+ "'s published assessments.  All chosen assessments will move " + "from "
				+ source.getFullName() + " to the chosen taxon."), new BorderLayoutData(
				LayoutRegion.NORTH, TaxonChooser.HEADER_HEIGHT));

		full.add(left, new BorderLayoutData(LayoutRegion.WEST, size));
		full.add(getRightSide(), new BorderLayoutData(LayoutRegion.CENTER, size));

		full.setSize(TaxonChooser.PANEL_WIDTH, TaxonChooser.PANEL_HEIGHT);

		add(full);
		layout();
	}

	/**
	 * submits the information from the ui to the server
	 */
	protected void onSubmit() {
		final List<DataListItem> checked = assessmentList.getChecked();
		if (target == null) {
			WindowUtils.infoAlert("Please select a taxon to move the assessments into.");
		} else if (checked.size() == 0) {
			WindowUtils.infoAlert("Please select at least one assessment to move from "
					+ source.getFullName() + ".");
		} else {
			submitButton.setEnabled(false);
			ArrayList<String> ids = new ArrayList<String>();
			for (DataListItem item : checked) {
				Integer id = item.getData("id");
				ids.add(id.toString());
			}
			TaxomaticUtils.impl.performMoveAssessments(ids, Integer.toString(source.getId()), Integer.toString(target.getId()),
					new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					WindowUtils
						.errorAlert("Error",
						"An error occurred while moving assessments, and the assessments were not successfully moved.");
				}
				public void onSuccess(String result) {
					if (assessmentList.getChecked().size() == assessmentList.getItemCount()) {
						WindowUtils.infoAlert("Success", "The selected assessments were moved from "
								+ source.getFullName() + " into "
								+ target.getFullName() + ".");
					} else {
						WindowUtils.confirmAlert("Success", "The selected assessments were moved from "
								+ source.getFullName() + " into "
								+ target.getFullName() + ".  Would you like to move more "
								+ "assessments from " + source.getFullName() + "?",
								new WindowUtils.MessageBoxListener() {
							@Override
							public void onNo() {
								TaxomaticAssessmentMover.this.hide();
							}
							public void onYes() {
								for (DataListItem item : checked) {
									assessmentList.remove(item);
								}
							}
						}, "Move More Assessments", "Finished");
					}
				}
			});
		}
	}

}
