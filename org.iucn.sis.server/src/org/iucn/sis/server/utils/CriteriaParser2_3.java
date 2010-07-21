package org.iucn.sis.server.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class CriteriaParser2_3 {

	protected LinkedHashMap gridA;
	protected LinkedHashMap gridB;
	protected LinkedHashMap gridC;
	protected LinkedHashMap gridD;
	protected LinkedHashMap gridE;

	public CriteriaParser2_3() {
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
		gridA.put("A2b", new Boolean(false));
		gridA.put("A2c", new Boolean(false));
		gridA.put("A2d", new Boolean(false));
		gridA.put("A2e", new Boolean(false));

		gridB.put("B1", new Boolean(false));
		gridB.put("B2a", new Boolean(false));
		gridB.put("B2b", new Boolean(false));
		gridB.put("B2c", new Boolean(false));
		gridB.put("B2d", new Boolean(false));
		gridB.put("B2e", new Boolean(false));
		gridB.put("B3a", new Boolean(false));
		gridB.put("B3b", new Boolean(false));
		gridB.put("B3d", new Boolean(false));

		gridC.put("C1", new Boolean(false));
		gridC.put("C2a", new Boolean(false));
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

		String temp1 = "";
		String temp2 = "";

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
				}

				else {
					if (temp2.equals("")) {
						temp2 = tmp.substring(1, 2);
					}
					temp2 += tmp.substring(2, 3);
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

		if (getChecked.length() > 0) {
			return getChecked.substring(0, getChecked.length() - 1);
		} else
			return getChecked.toString();
	}

	protected String getBchecked() {
		int t1 = 1;
		int t2 = 6;

		String temp1 = "";
		String temp2 = "";
		String temp3 = "";
		String temp4 = "";

		StringBuffer getChecked = new StringBuffer();
		int i = 0;
		for (Iterator iter = gridB.keySet().iterator(); iter.hasNext();) {
			Object temp = iter.next();
			String tmp = (String) temp;
			if (((Boolean) gridB.get(temp)).booleanValue()) {
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
				} else {
					if (temp3.equals("")) {
						temp3 = tmp.substring(1, 2);
					}
					temp3 += tmp.substring(2, 3);
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

		if (getChecked.length() > 0) {
			return getChecked.substring(0, getChecked.length() - 1);
		} else
			return getChecked.toString();
	}

	protected String getCchecked() {

		boolean checked1 = ((Boolean) gridC.get("C1")).booleanValue();
		boolean checked2 = ((Boolean) gridC.get("C2a")).booleanValue();
		boolean checked3 = ((Boolean) gridC.get("C2b")).booleanValue();

		String checked = "";
		if (checked2 && checked3) {
			checked = "2ab";
		} else if (checked2) {
			checked = "2a";
		} else if (checked3) {
			checked = "2b";
		}

		if (checked1) {
			if (checked.length() > 0) {
				checked = "1+" + checked;
			} else {
				checked = "1";
			}
		}

		return checked;
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
	 * TODO: NOT YET SUPPORTED...
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
					String numLetters = plusSplit[j];

					// GET NUMBER
					char number = numLetters.charAt(0);

					if (number == '1') {
						check("B", "B1");
					}

					// GET LETTERS
					for (int k = 1; k < numLetters.length(); k++) {

						char letter = numLetters.charAt(k);

						StringBuffer finalString = new StringBuffer();
						finalString.append(firstLetter);
						finalString.append(number);
						finalString.append(letter);

						// Mark Grid
						check("B", finalString.toString());
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

						if (almostThere.matches(".*a.*")) {
							check("C", "C2a");
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
