package org.iucn.sis.client.expert;

public class ExpertResultParser {

	public static ExpertResultParser impl = new ExpertResultParser();
	public final static int xDD = 0;
	public final static int xCR = 100;
	public final static int xEN = 200;
	public final static int xVU = 300;
	public final static int xLR = 400;

	private ExpertResultParser() {
	}

	public String getBestTextClassification(String categoryText) {
		String[] results = categoryText.split(",");
		try {
			int best = Integer.parseInt(results[1]);

			String bestCriteria = getCriteria(best);

			String string = "";
			if (best < xDD) {
				string = "Data Deficient";
			} else
				string = bestCriteria;

			return string;
		} catch (Exception e) {
			return "There is not enough data determine the assessment's classfication.";
		}
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

	public String getTextClassification(String categoryText) {
		String[] results = categoryText.split(",");
		try {
			int left = Integer.parseInt(results[0]);
			int best = Integer.parseInt(results[1]);
			int right = Integer.parseInt(results[2]);

			String leftCriteria = getCriteria(left);
			String bestCriteria = getCriteria(best);
			String rightCriteria = getCriteria(right);

			String string = "";
			if (left < xDD) {
				string = "There is not enough data determine the assessment's classfication.";
			} else if (leftCriteria.equals(rightCriteria)) {
				string = leftCriteria.toUpperCase();
			} else {
				string = "The species is best classified as " + bestCriteria.toUpperCase()
						+ " but can also be classified " + "as " + leftCriteria.toUpperCase() + " through "
						+ rightCriteria.toUpperCase();
			}

			return string;
		} catch (Exception e) {
			return "There is not enough data determine the assessment's classfication.";
		}
	}

}
