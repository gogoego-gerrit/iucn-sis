package org.iucn.sis.shared.api.structures;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;

@SuppressWarnings("deprecation")
public abstract class CriteriaGrid3_1 extends CriteriaGrid {

	protected String[] categoriesForValidation = { "CR", "EN", "VU" };

	public void buildGrid() {
		gridA = new Grid(4, 5);
		gridB = new Grid(6, 5);
		gridC = new Grid(3, 2);
		gridD = new Grid(3, 1);
		gridE = new Grid(1, 1);

		gridA.addTableListener(new TableListener() {
			public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
				updateCriteriaString(createCriteriaString());
			}
		});
		gridB.addTableListener(new TableListener() {
			public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
				updateCriteriaString(createCriteriaString());
			}
		});
		gridC.addTableListener(new TableListener() {
			public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
				updateCriteriaString(createCriteriaString());
			}
		});
		gridD.addTableListener(new TableListener() {
			public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
				updateCriteriaString(createCriteriaString());
			}
		});
		gridE.addTableListener(new TableListener() {
			public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
				updateCriteriaString(createCriteriaString());
			}
		});

		gridA.setCellSpacing(4);
		gridB.setCellSpacing(4);
		gridC.setCellSpacing(4);
		gridD.setCellSpacing(4);
		gridE.setCellSpacing(4);

		gridA.setWidget(0, 0, createWidget("A1a", 0, 0));
		gridA.setWidget(0, 1, createWidget("A1b", 0, 1));
		gridA.setWidget(0, 2, createWidget("A1c", 0, 2));
		gridA.setWidget(0, 3, createWidget("A1d", 0, 3));
		gridA.setWidget(0, 4, createWidget("A1e", 0, 4));
		gridA.setWidget(1, 0, createWidget("A2a", 1, 0));
		gridA.setWidget(1, 1, createWidget("A2b", 1, 1));
		gridA.setWidget(1, 2, createWidget("A2c", 1, 2));
		gridA.setWidget(1, 3, createWidget("A2d", 1, 3));
		gridA.setWidget(1, 4, createWidget("A2e", 1, 4));
		// gridA.setWidget(2, 0, createWidget("A3a", 2, 0));
		gridA.setWidget(2, 1, createWidget("A3b", 2, 1));
		gridA.setWidget(2, 2, createWidget("A3c", 2, 2));
		gridA.setWidget(2, 3, createWidget("A3d", 2, 3));
		gridA.setWidget(2, 4, createWidget("A3e", 2, 4));
		gridA.setWidget(3, 0, createWidget("A4a", 3, 0));
		gridA.setWidget(3, 1, createWidget("A4b", 3, 1));
		gridA.setWidget(3, 2, createWidget("A4c", 3, 2));
		gridA.setWidget(3, 3, createWidget("A4d", 3, 3));
		gridA.setWidget(3, 4, createWidget("A4e", 3, 4));

		gridB.setWidget(0, 0, createWidget("B1a", 0, 0));
		gridB.setWidget(1, 0, createWidget("B1b(i)", 1, 0));
		gridB.setWidget(1, 1, createWidget("B1b(ii)", 1, 1));
		gridB.setWidget(1, 2, createWidget("B1b(iii)", 1, 2));
		gridB.setWidget(1, 3, createWidget("B1b(iv)", 1, 3));
		gridB.setWidget(1, 4, createWidget("B1b(v)", 1, 4));
		gridB.setWidget(2, 0, createWidget("B1c(i)", 2, 0));
		gridB.setWidget(2, 1, createWidget("B1c(ii)", 2, 1));
		gridB.setWidget(2, 2, createWidget("B1c(iii)", 2, 2));
		gridB.setWidget(2, 3, createWidget("B1c(iv)", 2, 3));
		// gridB.setWidget(2, 4, createWidget("B1c(v)", 2, 4));
		gridB.setWidget(3, 0, createWidget("B2a", 3, 0));
		gridB.setWidget(4, 0, createWidget("B2b(i)", 4, 0));
		gridB.setWidget(4, 1, createWidget("B2b(ii)", 4, 1));
		gridB.setWidget(4, 2, createWidget("B2b(iii)", 4, 2));
		gridB.setWidget(4, 3, createWidget("B2b(iv)", 4, 3));
		gridB.setWidget(4, 4, createWidget("B2b(v)", 4, 4));
		gridB.setWidget(5, 0, createWidget("B2c(i)", 5, 0));
		gridB.setWidget(5, 1, createWidget("B2c(ii)", 5, 1));
		gridB.setWidget(5, 2, createWidget("B2c(iii)", 5, 2));
		gridB.setWidget(5, 3, createWidget("B2c(iv)", 5, 3));
		// gridB.setWidget(5, 4, createWidget("B2c(v)", 5, 4));

		gridC.setWidget(0, 0, createWidget("C1", 0, 0));
		gridC.setWidget(1, 0, createWidget("C2a(i)", 1, 0));
		gridC.setWidget(1, 1, createWidget("C2a(ii)", 1, 1));
		gridC.setWidget(2, 0, createWidget("C2b", 2, 0));

		gridD.setWidget(0, 0, createWidget("D", 0, 0));
		gridD.setWidget(1, 0, createWidget("D1", 1, 0));
		gridD.setWidget(2, 0, createWidget("D2", 2, 0));

		gridE.setWidget(0, 0, createWidget("E", 0, 0));
	}

	protected String getAchecked() {
		StringBuffer getChecked = new StringBuffer();
		for (int i = 0; i < gridA.getRowCount(); i++) {
			String tempA = "";
			for (int j = 0; j < gridA.getColumnCount(); j++) {
				if (gridA.getWidget(i, j) != null && ((CheckBox) gridA.getWidget(i, j)).isChecked()) {
					String temp = ((CheckBox) gridA.getWidget(i, j)).getText().trim();
					if (tempA.equals("")) {
						tempA = temp.substring(1, 2);
					}
					tempA += temp.substring(2, 3);

				}
			}
			if (!tempA.equals(""))
				getChecked.append(tempA + "+");
		}
		if (getChecked.length() > 0) {
			return getChecked.substring(0, getChecked.length() - 1);
		} else
			return getChecked.toString();
	}

	protected String getBchecked() {
		StringBuffer getChecked = new StringBuffer();
		String temp1 = "";
		String temp2 = "";
		for (int i = 0; i < gridB.getRowCount(); i++) {
			String innerIndex = "";
			String letter = "";
			for (int j = 0; j < gridB.getColumnCount(); j++) {
				if (((gridB.getWidget(i, j)) != null) && (((CheckBox) gridB.getWidget(i, j)).isChecked())) {
					String temp = ((CheckBox) gridB.getWidget(i, j)).getText().trim();
					// DO INDEX 1
					if (i < 3 && temp1.equals("")) {
						temp1 = "1";
					}
					// DO INDEX 2
					else if (i >= 3 && temp2.equals("")) {
						temp2 = "2";
					}
					if (letter.trim().equals("")) {
						letter = temp.substring(2, 3);
					}
					// get inner index
					if (temp.length() > 3) {
						temp = temp.replaceFirst(".*\\(", "");
						temp = temp.replaceFirst("\\).*", "");
						innerIndex += temp + ",";
					}
				}
			}

			if (i < 3 && temp1.length() > 0) {
				temp1 = temp1 + letter;
				if (innerIndex.length() > 0) {
					innerIndex = "(" + innerIndex.substring(0, innerIndex.length() - 1) + ")";
					temp1 = temp1 + innerIndex;
				}
			} else if (i >= 3 && temp2.length() > 0) {
				temp2 = temp2 + letter;
				if (innerIndex.length() > 0) {
					innerIndex = "(" + innerIndex.substring(0, innerIndex.length() - 1) + ")";
					temp2 = temp2 + innerIndex;
				}
			}
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

		boolean ai = ((CheckBox) gridC.getWidget(1, 0)).isChecked();
		boolean aii = ((CheckBox) gridC.getWidget(1, 1)).isChecked();

		String tempa = "";
		if (ai && aii) {
			tempa = "2a(i,ii)";
		} else if (ai) {
			tempa = "2a(i)";
		} else if (aii) {
			tempa = "2a(ii)";
		}

		boolean b = ((CheckBox) gridC.getWidget(2, 0)).isChecked();
		String tempb = "";
		if (b) {
			if (aii || ai) {
				tempb = "b";
			} else {
				tempb = "2b";
			}
		}

		if (((CheckBox) gridC.getWidget(0, 0)).isChecked()) {
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
		if (((CheckBox) gridD.getWidget(0, 0)).isChecked()) {
			temp = "D";
		} else {
			if ((((CheckBox) gridD.getWidget(1, 0)).isChecked()) && (((CheckBox) gridD.getWidget(2, 0)).isChecked())) {
				temp = "D1+2";
			} else if (((CheckBox) gridD.getWidget(1, 0)).isChecked())
				temp = "D1";
			else if (((CheckBox) gridD.getWidget(2, 0)).isChecked())
				temp = "D2";
		}
		return temp;
	}

	protected String getEchecked() {
		String temp = "";
		if (((CheckBox) gridE.getWidget(0, 0)).isChecked()) {
			temp = "E";
		}
		return temp;
	}

	public boolean isChecked(String key) {
		String grid = key.substring(0, 1);
		String index = (String) classificationToGrid.get(key);

		String[] keys = index.split(",");
		int row = Integer.valueOf(keys[0]).intValue();
		int col = Integer.valueOf(keys[1]).intValue();

		if (grid.equalsIgnoreCase("A")) {
			return ((CheckBox) gridA.getWidget(row, col)).isChecked();
		} else if (grid.equalsIgnoreCase("B")) {
			return ((CheckBox) gridB.getWidget(row, col)).isChecked();
		} else if (grid.equalsIgnoreCase("C")) {
			return ((CheckBox) gridC.getWidget(row, col)).isChecked();
		} else if (grid.equalsIgnoreCase("D")) {
			return ((CheckBox) gridD.getWidget(row, col)).isChecked();
		} else if (grid.equalsIgnoreCase("E")) {
			return ((CheckBox) gridE.getWidget(row, col)).isChecked();
		} else {
			return false;
		}
	}

	@Override
	public boolean isCriteriaValid(String criteria, String category) {
		boolean doValidation = false;
		boolean valid = true;
		boolean fastFail = false;
		for (String cat : categoriesForValidation) {
			if (cat.equalsIgnoreCase(category)) {
				doValidation = true;
				break;
			}
		}
		if (doValidation) {
			valid = false;
			if (criteria.contains("A"))
				valid = true;
			else {

				// CHECK INVALIDITY OF E CRITERIA
				if (criteria.contains("E")) {
					valid = true;
				}
				// CHECK INVALIDITY OF D CRITERIA
				if (criteria.contains("D") && !valid) {
					if (category.equalsIgnoreCase("VU")) {
						if (!isChecked("D"))
							return true;
						else
							fastFail = true;
					} else {
						if (!isChecked("D1") && !isChecked("D2")) {
							valid = true;
						} else {
							fastFail = true;
						}

					}
				}
				// CHECK INVALIDITY OF C CRITERIA
				if (!fastFail && criteria.contains("C") && !valid) {
					String cString = getCchecked();
					boolean contains1 = cString.contains("1");
					boolean contains2 = cString.contains("2");
					if (!(contains1 && contains2)) {
						valid = true;
					} else {
						fastFail = true;
					}
				}
				// CHECK INVALIDITY OF B CRITERIA
				if (!fastFail && criteria.contains("B") && !valid) {

					int numberB1Checked = 0;
					int numberB2Checked = 0;

					String[] b1bWidgets = { "B1b(i)", "B1b(ii)", "B1b(iii)", "B1b(iv)", "B1b(v)" };
					String[] b1cWidgets = { "B1c(i)", "B1c(ii)", "B1c(iii)", "B1c(iv)" };
					String[] b2bWidgets = { "B2b(i)", "B2b(ii)", "B2b(iii)", "B2b(iv)", "B2b(v)" };
					String[] b2cWidgets = { "B2c(i)", "B2c(ii)", "B2c(iii)", "B2c(iv)" };

					if (isChecked("B1a")) {
						numberB1Checked++;
					}
					for (String possible : b1bWidgets) {
						if (isChecked(possible)) {
							numberB1Checked++;
							break;

						}

					}
					for (String possible : b1cWidgets) {
						if (isChecked(possible)) {
							numberB1Checked++;
							break;
						}
					}
					if (numberB1Checked >= 2)
						valid = true;
					if (!valid) {

						if (isChecked("B2a")) {
							numberB2Checked++;
						}
						for (String possible : b2bWidgets) {
							if (isChecked(possible)) {
								numberB2Checked++;
								break;
							}
						}
						for (String possible : b2cWidgets) {
							if (isChecked(possible)) {
								numberB2Checked++;
								break;
							}
						}
						if (numberB2Checked >= 2)
							valid = true;
					}

				}

			}
		}
		return valid && !fastFail;
	}

	@Override
	public final void parseCriteriaString(String criteriaString) {
		parse3_1CriteriaString(criteriaString);
	}

}
