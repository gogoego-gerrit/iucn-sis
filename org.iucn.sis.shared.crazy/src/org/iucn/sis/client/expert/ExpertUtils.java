package org.iucn.sis.client.expert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;

import com.solertium.util.extjs.client.WindowUtils;

public class ExpertUtils {

	public static void processAssessment(AssessmentData currentAssessment) {
		FuzzyExpImpl expert = new FuzzyExpImpl();
		ExpertResult expertResult = expert.doAnalysis(currentAssessment);
		
		currentAssessment.setCategoryCriteria(expertResult.getCriteriaString());
		currentAssessment.setCrCriteria(expertResult.getCriteriaStringCR());
		currentAssessment.setEnCriteria(expertResult.getCriteriaStringEN());
		currentAssessment.setVuCriteria(expertResult.getCriteriaStringVU());

		if (expertResult.getResult() != null) {
			currentAssessment.setCategoryFuzzyResult(expertResult.getLeft() + "," + expertResult.getBest() + ","
					+ expertResult.getRight());
			currentAssessment.setCategoryAbbreviation(expertResult.getAbbreviatedCategory());


			if (currentAssessment.isRegional()
					&& currentAssessment.getDataMap().containsKey(CanonicalNames.RegionExpertQuestions)) {
				ArrayList<String> regionalExpString = (ArrayList<String>) currentAssessment.getDataMap().get(
						CanonicalNames.RegionExpertQuestions);
				String[] regionalExpData = regionalExpString.get(0).split(",");

				if (regionalExpData.length > 2) {
					String upDown = regionalExpData[0];
					String amount = regionalExpData[1];
					String category = currentAssessment.getCategoryAbbreviation();

					if (upDown.equals(RegionalExpertQuestions.UPGRADE)) {
						int amountUp = Integer.valueOf(amount);
						category = slideCategory(amountUp, category);
					} else if (upDown.equals(RegionalExpertQuestions.DOWNGRADE)) {
						int amountDown = Integer.valueOf(amount);
						category = slideCategory(-1 * amountDown, category);
					}

					currentAssessment.setCategoryAbbreviation(category);
				}
			} else {
				System.out.println("Couldn't find regional expert question data.");
			}

		} else {
			currentAssessment.setCategoryFuzzyResult("-1,-1,-1");
			currentAssessment.setCategoryAbbreviation("DD");
		}
	}
	
	private static String slideCategory(int amount, String startValue) {
		List<String> cats = Arrays.asList(new String[] { "LC", "VU", "EN", "CR" });
		int index = cats.indexOf(startValue);

		if (index < 0) {
			return startValue;
		} else {
			int newIndex = index + amount;

			if (newIndex < 0) {
				WindowUtils.errorAlert("The number of categories to downgrade is too many. " + "You cannot shift " + -1
						* amount + " categories from " + startValue + ". Please correct this error.");
				return startValue;
			} else if (newIndex >= cats.size()) {
				WindowUtils.errorAlert("The number of categories to upgrade is too many. " + "You cannot shift "
						+ amount + " categories from " + startValue + ". Please correct this error.");
				return startValue;
			} else {
				return cats.get(newIndex);
			}
		}
	}
}
