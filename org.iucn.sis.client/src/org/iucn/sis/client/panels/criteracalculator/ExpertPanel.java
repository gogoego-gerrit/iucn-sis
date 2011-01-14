package org.iucn.sis.client.panels.criteracalculator;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.shared.api.criteriacalculator.ExpertResult;
import org.iucn.sis.shared.api.criteriacalculator.FuzzyExpImpl;
import org.iucn.sis.shared.api.models.Assessment;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * This panel visually shows the results of the Expert System.
 * 
 * @author liz.schwartz
 * 
 */
public class ExpertPanel extends LayoutContainer {
	
	public final static String titleText = "Criteria Generator";
	
	private HTML title;
	private HorizontalPanel expertBar;
	private HTML criteria;
	
	private HTML CR;
	private HTML EN;
	private HTML VU;
	
	private String criteriaConstant;
	private HTML range;
	private VerticalPanel centerWidget;
	
	public final int xDD = 0;
	public final int xCR = 100;
	public final int xEN = 200;
	public final int xVU = 300;
	public final int xLR = 400;

	public ExpertPanel(PanelManager manager) {
		super();
		setLayout(new BorderLayout());
		setSize(520, 300);
		criteriaConstant = "Criteria String: ";

		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 100);
		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER, 25, 25, 100);
		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 10, 10, 10);
		BorderLayoutData eastData = new BorderLayoutData(LayoutRegion.EAST, 10, 10, 10);

		HorizontalPanel filler = new HorizontalPanel();
		filler.add(new HTML("&nbsp"));
		HorizontalPanel filler2 = new HorizontalPanel();
		filler2.add(new HTML("&nbsp"));
		add(filler, westData);
		add(filler2, eastData);

		title = new HTML();
		add(title, northData);

		expertBar = new HorizontalPanel();
		expertBar.addStyleName("expert-background");
		expertBar.addStyleName("expert-border");

		range = new HTML();
		range.addStyleName("expert-criteria");
//		range.setSize("200px", "60px");

		criteria = new HTML();
		criteria.setStyleName("expert-criteria");
//		criteria.setSize("275px", "30px");

		CR = new HTML("CR Criteria: N/A");
		CR.addStyleName("expert-criteria");
		EN = new HTML("EN Criteria: N/A");
		EN.addStyleName("expert-criteria");
		VU = new HTML("VU Criteria: N/A");
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
		
		add(centerWidget, centerData);

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
//		add(messageWrap, southData);
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
		if (criteria <= xCR)
			return "Critically Endangered";
		else if (criteria <= xEN)
			return "Endangered";
		else if (criteria <= xVU)
			return "Vulnerable";
		else
			return "Least Concern";
	}

	public HorizontalPanel getPanel() {
		if (expertBar == null)
			update();
		return expertBar;
	}

	private void getRange(int left, int best, int right) {
		String leftCriteria = getCriteria(left);
		String bestCriteria = getCriteria(best);
		String rightCriteria = getCriteria(right);

		if (leftCriteria.equals(rightCriteria)) {
			range.setText("The species is classified as: " + leftCriteria.toUpperCase() + "");
		} else {
			range.setHTML("The species is best classified as: " + bestCriteria.toUpperCase()
					+ "<br /> Can also be classified " + "as: " + leftCriteria.toUpperCase() + " through "
					+ rightCriteria.toUpperCase());

		}
	}

	public void update() {
		Assessment assessment = AssessmentCache.impl.getCurrentAssessment();
		expertBar.removeAll();
		range.setText("");
		
		if (assessment == null) {
			criteria.setHTML("Please select an assessment before starting criteria calculator. <br />");
			return;
		}

		Integer assessmentName = assessment.getId();
		String speciesName = assessment.getSpeciesName();

		title.setText(speciesName.toUpperCase() + " - " + assessmentName + "\r\n \r\n");
		title.addStyleName("expert-title");
		title.setWordWrap(false);
		
		String[] expertResults = assessment.getCategoryFuzzyResult().split(",");
		int leftInt;
		int bestInt;
		int rightInt;

		String CRString, ENString, VUString, criteriaStr;
		
		// THEY ARE OPENING IT WITHOUT CHANGING DATA, AND DIDN'T NEED TO
		// SAVE
		if (expertResults.length != 3) {
			FuzzyExpImpl expert = new FuzzyExpImpl();
			ExpertResult result = expert.doAnalysis(AssessmentCache.impl.getCurrentAssessment());
			
			leftInt = result.getLeft();
			bestInt = result.getBest();
			rightInt = result.getRight();
			
			CRString = result.getCriteriaStringCR();
			ENString = result.getCriteriaStringEN();
			VUString = result.getCriteriaStringVU();
			
			criteriaStr = result.getCriteriaString();
		} else {
			leftInt = Integer.valueOf(expertResults[0]).intValue();
			bestInt = Integer.valueOf(expertResults[1]).intValue();
			rightInt = Integer.valueOf(expertResults[2]).intValue();
			
			CRString = ENString = VUString = "N/A";
			
			criteriaStr = assessment.getCategoryCriteria();
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
				criteria.setHTML(criteriaConstant + criteriaStr);
				
			CR.setVisible(true);
			EN.setVisible(true);
			VU.setVisible(true);
		} else {
			if (expertBar.getParent() != null)
				centerWidget.remove(expertBar);
			criteria.setHTML("<b>Not enough saved information to run the criteria calculator. <br /></b>");
				
			CR.setVisible(false);
			EN.setVisible(false);
			VU.setVisible(false);
		}
			
		CR.setHTML("CR Criteria: " + CRString);
		EN.setHTML("EN Criteria: " + ENString);
		VU.setHTML("VU Criteria: " + VUString);

		try {
			layout();
		} catch (Exception e) {
		}
	}

}
