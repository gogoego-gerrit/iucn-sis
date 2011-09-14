package org.iucn.sis.client.panels.criteracalculator;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.shared.api.criteriacalculator.ExpertResult;
import org.iucn.sis.shared.api.criteriacalculator.ExpertUtils;
import org.iucn.sis.shared.api.criteriacalculator.FuzzyExpImpl;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.fields.RedListFuzzyResultField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

/**
 * This panel visually shows the results of the Expert System.
 * 
 * @author liz.schwartz
 * @author carl.scott
 */
public class ExpertPanel extends BasicWindow implements DrawsLazily {
	
	private final Html criteria, CR, EN, VU, range;
	
	private HorizontalPanel expertBar;
	private VerticalPanel centerWidget;

	public ExpertPanel() {
		super("Criteria Generator", "icon-expert");
		setLayout(new FillLayout());
		setSize(440, 350);

		expertBar = new HorizontalPanel();
		expertBar.addStyleName("expert-background");
		expertBar.addStyleName("expert-border");

		range = new Html();
		range.addStyleName("expert-criteria");
//		range.setSize("200px", "60px");

		criteria = new Html();
		criteria.setStyleName("expert-criteria");
//		criteria.setSize("275px", "30px");

		CR = new Html("CR Criteria: N/A");
		CR.addStyleName("expert-criteria");
		
		EN = new Html("EN Criteria: N/A");
		EN.addStyleName("expert-criteria");
		
		VU = new Html("VU Criteria: N/A");
		VU.addStyleName("expert-criteria");
		
		centerWidget = new VerticalPanel();
		centerWidget.setVerticalAlign(VerticalAlignment.TOP);
		centerWidget.setSpacing(10);
		centerWidget.add(range);
		centerWidget.add(criteria);
		
		centerWidget.setHorizontalAlign(HorizontalAlignment.CENTER);
		centerWidget.add(CR);
		centerWidget.add(EN);
		centerWidget.add(VU);
		centerWidget.add(new HTML("<br><br>"));

		HTML messageHTML1 = new HTML("** The result reflects most recently saved data **");
		HTML messageHTML2 = new HTML("** Please save your data before viewing the criteria calculator's result **");
		messageHTML1.addStyleName("expert-criteria");
		messageHTML1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		messageHTML1.setWordWrap(true);
		messageHTML2.addStyleName("expert-criteria");
		messageHTML2.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		messageHTML2.setWordWrap(true);
		
		VerticalPanel messageWrap = new VerticalPanel();
		messageWrap.add(messageHTML1);
		messageWrap.add(messageHTML2);
		
		centerWidget.setVerticalAlign(VerticalAlignment.BOTTOM);
		centerWidget.add(messageWrap);
		
		add(centerWidget);
	}

	public HorizontalPanel createDisplay(int leftInt, int bestInt, int rightInt) {
		HorizontalPanel display = new HorizontalPanel();

		HorizontalPanel bar0 = new HorizontalPanel();
		bar0.add(new HTML("&nbsp"));
		int barsize = leftInt - 10;
		bar0.setSize(barsize + "px", "20px");
		HorizontalPanel bar1 = new HorizontalPanel();
		bar1.add(new HTML("&nbsp"));
		barsize = bestInt - leftInt;
		if (barsize < 0)
			barsize = 0;
		bar1.setSize(barsize + "px", "20px");
		bar1.addStyleName("expert-line");
		HorizontalPanel bar2 = new HorizontalPanel();
		bar2.add(new HTML("&nbsp"));
		bar2.setSize("20px", "20px");
		bar2.addStyleName("expert-diamond");
		HorizontalPanel bar3 = new HorizontalPanel();
		bar3.add(new HTML("&nbsp"));
		barsize = rightInt - bestInt;
		bar3.setSize(barsize + "px", "20px");
		bar3.addStyleName("expert-line");
		HorizontalPanel bar4 = new HorizontalPanel();
		bar4.add(new HTML("&nbsp"));
		barsize = 400 - rightInt;
		bar4.setSize(barsize + "px", "20px");

		display.add(bar0);
		display.add(bar1);
		display.add(bar2);
		display.add(bar3);
		display.add(bar4);
		display.setSize("400px", "60px");
		return display;
	}

	private String getCriteria(int criteria) {
		if (criteria <= FuzzyExpImpl.xCR)
			return "Critically Endangered";
		else if (criteria <= FuzzyExpImpl.xEN)
			return "Endangered";
		else if (criteria <= FuzzyExpImpl.xVU)
			return "Vulnerable";
		else
			return "Least Concern";
	}

	private void getRange(int left, int best, int right) {
		String leftCriteria = getCriteria(left);
		String bestCriteria = getCriteria(best);
		String rightCriteria = getCriteria(right);

		if (leftCriteria.equals(rightCriteria)) {
			range.setHtml("The species is classified as: " + leftCriteria.toUpperCase() + "");
		} else {
			range.setHtml("The species is best classified as: " + bestCriteria.toUpperCase()
					+ "<br /> Can also be classified " + "as: " + leftCriteria.toUpperCase() + " through "
					+ rightCriteria.toUpperCase());

		}
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
	public void draw(DoneDrawingCallback callback) {
		Assessment assessment = AssessmentCache.impl.getCurrentAssessment();
		if (assessment == null) {
			WindowUtils.errorAlert("Please select an assessment before starting criteria calculator.");
			return;
		}
		
		expertBar.removeAll();
		range.setHtml("");

		Integer assessmentName = assessment.getId();
		String speciesName = assessment.getSpeciesName();

		setHeading("Criteria Generator - " + speciesName + " (" + assessmentName + ")");
		
		AssessmentCache.impl.getCurrentAssessment().generateFields();
		Field fuzzyField = AssessmentCache.impl.getCurrentAssessment().getField(CanonicalNames.RedListFuzzyResult);
		boolean isFreshRun;
		if (isFreshRun = fuzzyField == null) {
			ExpertUtils.processAssessment(AssessmentCache.impl.getCurrentAssessment());
			fuzzyField = AssessmentCache.impl.getCurrentAssessment().getField(CanonicalNames.RedListFuzzyResult);
		}
		else {
			RedListFuzzyResultField proxy = new RedListFuzzyResultField(fuzzyField);
			if ("".equals(proxy.getCategory()) || "DD".equals(proxy.getCategory())) {
				ExpertUtils.processAssessment(AssessmentCache.impl.getCurrentAssessment());
				fuzzyField = AssessmentCache.impl.getCurrentAssessment().getField(CanonicalNames.RedListFuzzyResult);
			}
		}
		
		int leftInt;
		int bestInt;
		int rightInt;
		
		String CRString, ENString, VUString, criteriaStr;
		
		if (fuzzyField == null) {
			leftInt = bestInt = rightInt = 0;
			CRString = ENString = VUString = criteriaStr = "N/A";
		}
		else {
			RedListFuzzyResultField proxy = new RedListFuzzyResultField(fuzzyField);
			ExpertResult result = proxy.getExpertResult();
			
			leftInt = result.getLeft();
			bestInt = result.getBest();
			rightInt = result.getRight();
			
			CRString = result.getCriteriaCR().toString();
			ENString = result.getCriteriaEN().toString();
			VUString = result.getCriteriaVU().toString();
			
			criteriaStr = result.getCriteriaString();
		}

		// CHECK TO MAKE SURE ENOUGH INFORMATION FOR RESULT
		if (leftInt >= 0) {
			if (expertBar.getParent() != null)
				centerWidget.remove(expertBar);
			expertBar = createDisplay(leftInt, bestInt, rightInt);
			expertBar.addStyleName("expert-background");
			expertBar.addStyleName("expert-border");
			
			centerWidget.insert(expertBar, 2);

			getRange(leftInt, bestInt, rightInt);
			
			if (criteriaStr != null)
				criteria.setHtml("Criteria String: " + criteriaStr);
				
			CR.setVisible(true);
			EN.setVisible(true);
			VU.setVisible(true);
		} else {
			if (expertBar.getParent() != null)
				centerWidget.remove(expertBar);
			criteria.setHtml("<b>Not enough saved information to run the criteria calculator. <br /></b>");
				
			CR.setVisible(false);
			EN.setVisible(false);
			VU.setVisible(false);
		}
			
		CR.setHtml("CR Criteria: " + CRString);
		EN.setHtml("EN Criteria: " + ENString);
		VU.setHtml("VU Criteria: " + VUString);
		
		getButtonBar().removeAll();
		if (!isFreshRun) {
			addButton(new Button("Re-run Expert System", new SelectionListener<ButtonEvent>() {
				public void componentSelected(final ButtonEvent ce) {
					final Assessment assessment = AssessmentCache.impl.getCurrentAssessment();
					Field fuzzyField = assessment.getField(CanonicalNames.RedListFuzzyResult);
					assessment.getField().remove(fuzzyField);
					draw(new DrawsLazily.DoneDrawingCallback() {
						public void isDrawn() {
							layout();
						}
					});
				}
			}));
		}
		addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();	
			}
		}));

		callback.isDrawn();
	}

}
