package org.iucn.sis.shared.api.data;

public abstract class CanParseCriteriaString {

	/**
	 * This function will be called when a particular criterion is parsed out.
	 * The "prefix" is the first letter (A, B, C, D, or E) and the "suffix" is
	 * the rest of the criterion (e.g. 1, 2b(iii), etc).
	 * 
	 * @param prefix
	 * @param suffix
	 */
	public abstract void foundCriterion(String prefix, String suffix);

	protected final void parse2_3CriteriaString(String criteria) {
		if (criteria.equals(""))
			return;

		String[] semiColonSplit;

		if (criteria.indexOf(";") > -1)
			semiColonSplit = criteria.split(";");
		else
			semiColonSplit = new String[] { criteria };

		for (int i = 0; i < semiColonSplit.length; i++) {
			semiColonSplit[i] = semiColonSplit[i].trim();
			char firstLetter = semiColonSplit[i].charAt(0);
			String internalCriteria = semiColonSplit[i].substring(1);
			if (firstLetter == 'A') {
				String[] plusSplit = internalCriteria.split("\\+");
				for (int j = 0; j < plusSplit.length; j++) {
					String numLetters = plusSplit[j];

					// GET NUMBER
					char number = numLetters.charAt(0);

					// GET LETTERS
					for (int k = 1; k < numLetters.length(); k++) {

						char letter = numLetters.charAt(k);

						StringBuffer finalString = new StringBuffer();
						finalString.append(firstLetter);
						finalString.append(number);
						finalString.append(letter);

						// Mark Grid
						foundCriterion("A", finalString.toString());
					}
				}
			}

			else if (firstLetter == 'B') {
				String[] plusSplit = internalCriteria.split("\\+");
				for (int j = 0; j < plusSplit.length; j++) {
					String numLetters = plusSplit[j];

					// GET NUMBER
					char number = numLetters.charAt(0);

					if (number == '1') {
						foundCriterion("B", "B1");
					}

					// GET LETTERS
					for (int k = 1; k < numLetters.length(); k++) {

						char letter = numLetters.charAt(k);

						StringBuffer finalString = new StringBuffer();
						finalString.append(firstLetter);
						finalString.append(number);
						finalString.append(letter);

						// Mark Grid
						foundCriterion("B", finalString.toString());
					}
				}

			}

			else if (firstLetter == 'C') {

				String[] plusSplit = internalCriteria.split("\\+");

				for (int j = 0; j < plusSplit.length; j++) {

					String almostThere = plusSplit[j];

					// EITHER A 1 OR A 2 FIRST
					if (almostThere.equalsIgnoreCase("1")) {

						foundCriterion("C", "C1");

					}

					else {
						if (almostThere.matches(".*[b].*")) {
							foundCriterion("C", "C2b");
						}

						if (almostThere.matches(".*a.*")) {
							foundCriterion("C", "C2a");
						}

					}

				}
			} else if (firstLetter == 'D') {
				if (internalCriteria.length() == 0 || internalCriteria.trim().equalsIgnoreCase("")) {
					foundCriterion("D", "D");
				} else if (internalCriteria.trim().equalsIgnoreCase("1")) {
					foundCriterion("D", "D1");
				} else if (internalCriteria.trim().equalsIgnoreCase("2")) {
					foundCriterion("D", "D2");
				} else {
					foundCriterion("D", "D1");
					foundCriterion("D", "D2");
				}

			}

			else if (firstLetter == 'E') {
				foundCriterion("E", "E");
			}
		}
	}

	/**
	 * criteria is a valid criteria string, and checks the applicable check
	 * marks in the grid
	 * 
	 * @param criteria
	 */
	protected final void parse3_1CriteriaString(String criteria) {
		if (criteria.equals(""))
			return;

		String[] semiColonSplit;

		if (criteria.indexOf(";") > -1)
			semiColonSplit = criteria.split(";");
		else
			semiColonSplit = new String[] { criteria };

		for (String raw : semiColonSplit) {
			String value = raw.trim();
			if ("".equals(value))
				continue;
			char firstLetter = value.charAt(0);
			String internalCriteria = value.substring(1);
			if (firstLetter == 'A') {
				String[] plusSplit = internalCriteria.split("\\+");
				for (int j = 0; j < plusSplit.length; j++) {
					String numLetters = plusSplit[j];

					// GET NUMBER
					char number = numLetters.charAt(0);

					// GET LETTERS
					for (int k = 1; k < numLetters.length(); k++) {

						char letter = numLetters.charAt(k);

						StringBuffer finalString = new StringBuffer();
						finalString.append(firstLetter);
						finalString.append(number);
						finalString.append(letter);

						// Mark Grid
						foundCriterion("A", finalString.toString());
					}
				}
			}

			else if (firstLetter == 'B') {

				String[] plusSplit = internalCriteria.split("\\+");
				for (int j = 0; j < plusSplit.length; j++) {
					String numLettersNumerals = plusSplit[j];
					String aString = "";
					String bString = "";
					String cString = "";

					// GET NUMBER
					char number = numLettersNumerals.charAt(0);
					int index = 1;

					boolean addToBString = false;

					for (int k = index; k < numLettersNumerals.length(); k++) {

						char character = numLettersNumerals.charAt(k);

						if (character == 'a') {
							aString = "B" + Character.toString(number) + "a";
						}

						else if (character == 'b') {
							bString = "B" + Character.toString(number) + "b";
							addToBString = true;
						}

						else if (character == 'c') {
							cString = "B" + Character.toString(number) + "c";
							addToBString = false;
						}

						else if (character != '(' && character != ')' && character != ' ') {

							if (addToBString) {
								bString = bString + Character.toString(character);
							} else
								cString = cString + Character.toString(character);

						}

					}

					// Place Checks
					if (aString.length() > 0)
						foundCriterion("B", aString);

					if (bString.length() > 0) {
						String[] allToCheck = bString.split(",");
						int numeralIndex = allToCheck[0].indexOf('i');
						if (numeralIndex < 0)
							numeralIndex = allToCheck[0].indexOf('v');
						String realOne = allToCheck[0].substring(0, numeralIndex);
						allToCheck[0] = allToCheck[0].substring(numeralIndex);
						for (int k = 0; k < allToCheck.length; k++) {
							foundCriterion("B", realOne + "(" + allToCheck[k] + ")");
						}
					}

					if (cString.length() > 0) {
						String[] allToCheckC = cString.split(",");
						int numeralIndex = allToCheckC[0].indexOf('i');
						if (numeralIndex < 0)
							numeralIndex = allToCheckC[0].indexOf('v');
						String realOneC = allToCheckC[0].substring(0, numeralIndex);
						allToCheckC[0] = allToCheckC[0].substring(numeralIndex);
						for (int k = 0; k < allToCheckC.length; k++) {
							foundCriterion("B", realOneC + "(" + allToCheckC[k] + ")");
						}
					}

				}
			}

			else if (firstLetter == 'C') {

				String[] plusSplit = internalCriteria.split("\\+");

				for (int j = 0; j < plusSplit.length; j++) {

					String almostThere = plusSplit[j];

					// EITHER A 1 OR A 2 FIRST
					if (almostThere.equalsIgnoreCase("1")) {

						foundCriterion("C", "C1");

					}

					else {
						if (almostThere.matches(".*[b].*")) {
							foundCriterion("C", "C2b");
						}

						if (almostThere.indexOf("a(i)") > -1) {
							foundCriterion("C", "C2a(i)");
						}

						else if (almostThere.indexOf("a(ii)") > -1) {
							foundCriterion("C", "C2a(ii)");
						}

						else if (almostThere.indexOf("a(i,ii)") > -1) {
							foundCriterion("C", "C2a(i)");
							foundCriterion("C", "C2a(ii)");
						}
					}

				}

			} else if (firstLetter == 'D') {
				if (internalCriteria.length() == 0 || internalCriteria.trim().equalsIgnoreCase("")) {
					foundCriterion("D", "D");
				} else if (internalCriteria.trim().equalsIgnoreCase("1")) {
					foundCriterion("D", "D1");
				} else if (internalCriteria.trim().equalsIgnoreCase("2")) {
					foundCriterion("D", "D2");
				} else {
					foundCriterion("D", "D1");
					foundCriterion("D", "D2");
				}

			}

			else if (firstLetter == 'E') {
				foundCriterion("E", "E");
			}
		}
	}

}
