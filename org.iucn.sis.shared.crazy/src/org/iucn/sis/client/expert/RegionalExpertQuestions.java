package org.iucn.sis.client.expert;

import java.util.HashMap;

public class RegionalExpertQuestions {

	class Answer {

		String noAnswer;
		String yesAnswer;
		String maybeAnswer;

		public Answer(String no, String dontknow, String yes) {
			noAnswer = no;
			yesAnswer = yes;
			maybeAnswer = dontknow;
		}

		public String getMaybeAnswer() {
			return maybeAnswer;
		}

		public String getNoAnswer() {
			return noAnswer;
		}

		public String getYesAnswer() {
			return yesAnswer;
		}

	}

	public static final String DOWNGRADE = "Downgrade";
	public static final String UPGRADE = "Upgrade";

	public static final String NOCHANGE = "No change";
	private final String A = "Is the taxon a non-breeding visitor?";
	private final String B = "Does the regional population experience "
			+ "any significant immigration of propagules capable of reporducing in the regions?";
	private final String C = "Is the immigration expected to decrease?";
	private final String D = "Is the regional population a sink?";
	private final String E = "Are the conditions outside the region deteriorating?";
	private final String F = "Are the conditions within the region deteriorating?";

	private final String G = "Can the breeding population rescue the regional " + "population should it decline?";
	public static final int NO = 0;
	public static final int DONTKNOW = 1;

	public static final int YES = 2;

	private HashMap questionToAnswer;

	public RegionalExpertQuestions() {

		questionToAnswer = new HashMap();
		questionToAnswer.put(A, new Answer(B, B, E));
		questionToAnswer.put(B, new Answer(NOCHANGE, NOCHANGE, C));
		questionToAnswer.put(C, new Answer(DOWNGRADE, D, D));
		questionToAnswer.put(D, new Answer(NOCHANGE, NOCHANGE, UPGRADE));
		questionToAnswer.put(E, new Answer(F, NOCHANGE, NOCHANGE));
		questionToAnswer.put(F, new Answer(G, NOCHANGE, NOCHANGE));
		questionToAnswer.put(G, new Answer(NOCHANGE, NOCHANGE, DOWNGRADE));

	}

	public String getFirstQuestion() {
		return A;
	}

	/**
	 * Returns the next question (or result if end of the cycle)
	 * 
	 * @param question
	 * @param answer
	 * @return
	 */
	public String getNextQuestion(String question, int answer) {
		Answer tempAnswer = (Answer) questionToAnswer.get(question);
		if (answer == NO) {
			return tempAnswer.getNoAnswer();
		} else if (answer == DONTKNOW) {
			return tempAnswer.getMaybeAnswer();
		} else
			return tempAnswer.getYesAnswer();
	}

	/**
	 * given the answer, determines if it is a final result
	 * 
	 * @param answer
	 * @return
	 */
	public boolean isResult(String answer) {
		if (answer.equalsIgnoreCase(DOWNGRADE) || answer.equalsIgnoreCase(UPGRADE) || answer.equalsIgnoreCase(NOCHANGE))
			return true;
		else
			return false;
	}

}
