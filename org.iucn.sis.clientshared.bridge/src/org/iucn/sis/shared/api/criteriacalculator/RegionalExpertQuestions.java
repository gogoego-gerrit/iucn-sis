package org.iucn.sis.shared.api.criteriacalculator;

import java.util.HashMap;

public class RegionalExpertQuestions {

	public static final String DOWNGRADE = "Downgrade";
	public static final String UPGRADE = "Upgrade";
	public static final String NOCHANGE = "No change";
	
	public static final int NO = 0;
	public static final int DONTKNOW = 1;
	public static final int YES = 2;
	
	private final String A = "Is the taxon a non-breeding visitor?";
	private final String B = "Does the regional population experience "
			+ "any significant immigration of propagules capable of reporducing in the regions?";
	private final String C = "Is the immigration expected to decrease?";
	private final String D = "Is the regional population a sink?";
	private final String E = "Are the conditions outside the region deteriorating?";
	private final String F = "Are the conditions within the region deteriorating?";
	private final String G = "Can the breeding population rescue the regional " + "population should it decline?";
	
	private HashMap<String, Answer> questionToAnswer;

	public RegionalExpertQuestions() {
		questionToAnswer = new HashMap<String, RegionalExpertQuestions.Answer>();
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
		Answer tempAnswer = questionToAnswer.get(question);
		if (answer == NO) {
			return tempAnswer.getNoAnswer();
		} else if (answer == DONTKNOW) {
			return tempAnswer.getMaybeAnswer();
		} else if (answer == YES)
			return tempAnswer.getYesAnswer();
		else
			return null;
	}

	/**
	 * Given the next question, determine if it is a 
	 * question or the result.
	 * 
	 * @param question
	 * @return true if so, false otherwise
	 */
	public boolean isResult(String question) {
		return DOWNGRADE.equals(question) || 
			UPGRADE.equals(question) || 
			NOCHANGE.equals(question);
	}
	
	private static class Answer {

		private final String noAnswer;
		private final String yesAnswer;
		private final String maybeAnswer;

		public Answer(String no, String dontknow, String yes) {
			noAnswer = no;
			maybeAnswer = dontknow;
			yesAnswer = yes;
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

}
