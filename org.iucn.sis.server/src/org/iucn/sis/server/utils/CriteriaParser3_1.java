package org.iucn.sis.server.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class CriteriaParser3_1 {

	protected LinkedHashMap gridA;
	protected LinkedHashMap gridB;
	protected LinkedHashMap gridC;
	protected LinkedHashMap gridD;
	protected LinkedHashMap gridE;

	public CriteriaParser3_1() {
		gridA = new LinkedHashMap();
		gridB = new LinkedHashMap();
		gridC = new LinkedHashMap();
		gridD = new LinkedHashMap();
		gridE = new LinkedHashMap();

		gridA.put("A1a", new Boolean(false));
		gridA.put("A1b", new Boolean(false));
		gridA.put("A1c", new Boolean(false));
		gridA.put("A1d", new Boolean(false));
		gridA.put("A1e", new Boolean(false));
		gridA.put("A2a", new Boolean(false));
		gridA.put("A2b", new Boolean(false));
		gridA.put("A2c", new Boolean(false));
		gridA.put("A2d", new Boolean(false));
		gridA.put("A2e", new Boolean(false));
		gridA.put("A3b", new Boolean(false));
		gridA.put("A3c", new Boolean(false));
		gridA.put("A3d", new Boolean(false));
		gridA.put("A3e", new Boolean(false));
		gridA.put("A4a", new Boolean(false));
		gridA.put("A4b", new Boolean(false));
		gridA.put("A4c", new Boolean(false));
		gridA.put("A4d", new Boolean(false));
		gridA.put("A4e", new Boolean(false));

		gridB.put("B1a", new Boolean(false));
		gridB.put("B1b(i)", new Boolean(false));
		gridB.put("B1b(ii)", new Boolean(false));
		gridB.put("B1b(iii)", new Boolean(false));
		gridB.put("B1b(iv)", new Boolean(false));
		gridB.put("B1b(v)", new Boolean(false));
		gridB.put("B1c(i)", new Boolean(false));
		gridB.put("B1c(ii)", new Boolean(false));
		gridB.put("B1c(iii)", new Boolean(false));
		gridB.put("B1c(iv)", new Boolean(false));
		gridB.put("B2a", new Boolean(false));
		gridB.put("B2b(i)", new Boolean(false));
		gridB.put("B2b(ii)", new Boolean(false));
		gridB.put("B2b(iii)", new Boolean(false));
		gridB.put("B2b(iv)", new Boolean(false));
		gridB.put("B2b(v)", new Boolean(false));
		gridB.put("B2c(i)", new Boolean(false));
		gridB.put("B2c(ii)", new Boolean(false));
		gridB.put("B2c(iii)", new Boolean(false));
		gridB.put("B2c(iv)", new Boolean(false));

		gridC.put("C1", new Boolean(false));
		gridC.put("C2a(i)", new Boolean(false));
		gridC.put("C2a(ii)", new Boolean(false));
		gridC.put("C2b", new Boolean(false));

		gridD.put("D", new Boolean(false));
		gridD.put("D1", new Boolean(false));
		gridD.put("D2", new Boolean(false));

		gridE.put("E", new Boolean(false));
	}

	protected void check(String grid, String key) {
		if (grid.equalsIgnoreCase("A"))
			gridA.put(key, new Boolean(true));

		else if (grid.equalsIgnoreCase("B"))
			gridB.put(key, new Boolean(true));

		else if (grid.equalsIgnoreCase("C"))
			gridC.put(key, new Boolean(true));

		else if (grid.equalsIgnoreCase("D"))
			gridD.put(key, new Boolean(true));

		else if (grid.equalsIgnoreCase("E"))
			gridE.put(key, new Boolean(true));

	}

	public String createCriteriaString() {
		String A = getAchecked();
		String B = getBchecked();
		String C = getCchecked();
		String D = getDchecked();
		String E = getEchecked();

		StringBuffer string = new StringBuffer();
		if (A.length() > 0) {
			A = "A" + A;
			string.append(A + ";");
		}
		if (B.length() > 0) {
			B = "B" + B;
			string.append(B + ";");
		}
		if (C.length() > 0) {
			C = "C" + C;
			string.append(C + ";");
		}
		if (D.length() > 0) {
			string.append(D + ";");
		}
		if (E.length() > 0) {
			string.append(E + ";");
		}

		if (string.length() > 0)
			return string.substring(0, string.length() - 1);
		else
			return string.toString();

	}

	protected String getAchecked() {

		int t1 = 5;
		int t2 = 10;
		int t3 = 14;
		int t4 = 19;

		String temp1 = "";
		String temp2 = "";
		String temp3 = "";
		String temp4 = "";

		StringBuffer getChecked = new StringBuffer();
		int i = 0;
		for (Iterator iter = gridA.keySet().iterator(); iter.hasNext();) {
			Object temp = iter.next();

			if (((Boolean) gridA.get(temp)).booleanValue()) {
				String tmp = (String) temp;
				if (i < t1) {
					if (temp1.equals("")) {
						temp1 = tmp.substring(1, 2);
					}
					temp1 += tmp.substring(2, 3);
				} else if (i < t2) {
					if (temp2.equals("")) {
						temp2 = tmp.substring(1, 2);
					}
					temp2 += tmp.substring(2, 3);
				} else if (i < t3) {
					if (temp3.equals("")) {
						temp3 = tmp.substring(1, 2);
					}
					temp3 += tmp.substring(2, 3);
				} else {
					if (temp4.equals("")) {
						temp4 = tmp.substring(1, 2);
					}
					temp4 += tmp.substring(2, 3);
				}
			}

			i++;

		}

		getChecked.append(temp1);
		if (getChecked.length() > 0)
			getChecked.append("+");
		getChecked.append(temp2);
		if (getChecked.length() > 0)
			getChecked.append("+");
		getChecked.append(temp3);
		if (getChecked.length() > 0)
			getChecked.append("+");
		getChecked.append(temp4);
		if (getChecked.length() > 0)
			getChecked.append("+");

		if (getChecked.length() > 0) {
			return getChecked.substring(0, getChecked.length() - 1);
		} else
			return getChecked.toString();
	}

	protected String getBchecked() {
		StringBuffer getChecked = new StringBuffer();
		String temp1 = "1";
		String temp2 = "2";
		String lastLetter = "";
		String innerIs = "";
		int i = 0;

		for (Iterator iter = gridB.keySet().iterator(); iter.hasNext();) {
			Object temp = iter.next();
			if (((Boolean) gridB.get(temp)).booleanValue()) {
				String letter = ((String) temp).substring(2, 3);
				String is = ((String) temp).substring(3).replaceFirst(".*\\(", "");
				is = is.replaceFirst("\\).*", "");
				if (i < 10) {
					if (!letter.equals(lastLetter)) {
						lastLetter = letter;
						temp1 += letter;
						if (!innerIs.equals("")) {
							temp1 += "(" + innerIs.substring(0, innerIs.length() - 1);
						}
						innerIs = "";
					}

					innerIs += is + ",";

				} else if (i == 10) {
					if (!innerIs.equals("")) {
						temp1 += "(" + innerIs.substring(0, innerIs.length() - 1);
					}
					innerIs = "";
					letter = "";
				}
				if (i >= 10) {
					if (!letter.equals(lastLetter)) {
						lastLetter = letter;
						temp2 += letter;
						if (!innerIs.equals("")) {
							temp2 += "(" + innerIs.substring(0, innerIs.length() - 1);
						}
						innerIs = "";
					}

					innerIs += is + ",";
				}
			}

			i++;
		}

		if (!innerIs.equals("")) {
			temp2 += "(" + innerIs.substring(0, innerIs.length() - 1);
		}

		if (!temp1.equals("") && !temp2.equals("")) {
			getChecked.append(temp1 + "+" + temp2);
		} else if (!temp1.equals("")) {
			getChecked.append(temp1);
		} else if (!temp2.equals("")) {
			getChecked.append(temp2);
		}
		return getChecked.toString();

	}

	protected String getCchecked() {
		StringBuffer getChecked = new StringBuffer();

		boolean ai = ((Boolean) gridC.get("C2a(i)")).booleanValue();
		boolean aii = ((Boolean) gridC.get("C2a(ii)")).booleanValue();

		String tempa = "";
		if (ai && aii) {
			tempa = "2a(i,ii)";
		} else if (ai) {
			tempa = "2a(i)";
		} else if (aii) {
			tempa = "2a(ii)";
		}

		boolean b = ((Boolean) gridC.get("C2b")).booleanValue();
		String tempb = "";
		if (b) {
			if (aii || ai) {
				tempb = "b";
			} else {
				tempb = "2b";
			}
		}

		if (((Boolean) gridC.get("C1")).booleanValue()) {
			getChecked.append("1");
			if (aii | ai | b) {
				getChecked.append("+");
			}
		}
		getChecked.append(tempa + tempb);
		return getChecked.toString();
	}

	protected String getDchecked() {
		String temp = "";
		if (((Boolean) gridD.get("D")).booleanValue()) {
			temp = "D";
		} else {
			if (((Boolean) gridD.get("D1")).booleanValue() && ((Boolean) gridD.get("D2")).booleanValue()) {
				temp = "D1+2";
			} else if (((Boolean) gridD.get("D1")).booleanValue())
				temp = "D1";
			else if (((Boolean) gridD.get("D2")).booleanValue())
				temp = "D2";
		}
		return temp;
	}

	protected String getEchecked() {
		String temp = "";
		if (((Boolean) gridE.get("E")).booleanValue()) {
			temp = "E";
		}
		return temp;
	}

	public LinkedHashMap getGridA() {
		return gridA;
	}

	public LinkedHashMap getGridB() {
		return gridB;
	}

	public LinkedHashMap getGridC() {
		return gridC;
	}

	public LinkedHashMap getGridD() {
		return gridD;
	}

	public LinkedHashMap getGridE() {
		return gridE;
	}

	/**
	 * criteria is a valid criteria string, and checks the applicable check
	 * marks in the grid
	 * 
	 * @param criteria
	 */
	public void parseCriteriaString(String criteria) {
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
						check("A", finalString.toString());
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
						check("B", aString);

					if (bString.length() > 0) {
						String[] allToCheck = bString.split(",");
						int numeralIndex = allToCheck[0].indexOf('i');
						if (numeralIndex < 0)
							numeralIndex = allToCheck[0].indexOf('v');
						String realOne = allToCheck[0].substring(0, numeralIndex);
						allToCheck[0] = allToCheck[0].substring(numeralIndex);
						for (int k = 0; k < allToCheck.length; k++) {
							check("B", realOne + "(" + allToCheck[k] + ")");
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
							check("B", realOneC + "(" + allToCheckC[k] + ")");
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

						check("C", "C1");

					}

					else {
						if (almostThere.matches(".*[b].*")) {
							check("C", "C2b");
						}

						if (almostThere.indexOf("a(i)") > -1) {
							check("C", "C2a(i)");
						}

						else if (almostThere.indexOf("a(ii)") > -1) {
							check("C", "C2a(ii)");
						}

						else if (almostThere.indexOf("a(i,ii)") > -1) {
							check("C", "C2a(i)");
							check("C", "C2a(ii)");
						}
					}

				}

			} else if (firstLetter == 'D') {
				if (internalCriteria.length() == 0 || internalCriteria.trim().equalsIgnoreCase("")) {
					check("D", "D");
				} else if (internalCriteria.trim().equalsIgnoreCase("1")) {
					check("D", "D1");
				} else if (internalCriteria.trim().equalsIgnoreCase("2")) {
					check("D", "D2");
				} else {
					check("D", "D1");
					check("D", "D2");
				}

			}

			else if (firstLetter == 'E') {
				check("E", "E");
			}
		}
	}
}
