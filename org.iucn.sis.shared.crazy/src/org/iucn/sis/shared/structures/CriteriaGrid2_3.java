package org.iucn.sis.shared.structures;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;

public abstract class CriteriaGrid2_3 extends CriteriaGrid {

	protected String[] categoriesForValidation = { "CR", "EN", "VU" };

	public void buildGrid() {
		gridA = new Grid(2, 5);
		gridB = new Grid(3, 5);
		gridC = new Grid(2, 2);
		gridD = new Grid(2, 2);
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
		gridA.setWidget(1, 0, createWidget("A2b", 1, 0));
		gridA.setWidget(1, 1, createWidget("A2c", 1, 1));
		gridA.setWidget(1, 2, createWidget("A2d", 1, 2));
		gridA.setWidget(1, 3, createWidget("A2e", 1, 3));

		gridB.setWidget(0, 0, createWidget("B1", 0, 0));
		gridB.setWidget(1, 0, createWidget("B2a", 1, 0));
		gridB.setWidget(1, 1, createWidget("B2b", 1, 1));
		gridB.setWidget(1, 2, createWidget("B2c", 1, 2));
		gridB.setWidget(1, 3, createWidget("B2d", 1, 3));
		gridB.setWidget(1, 4, createWidget("B2e", 1, 4));
		gridB.setWidget(2, 0, createWidget("B3a", 2, 0));
		gridB.setWidget(2, 1, createWidget("B3b", 2, 1));
		gridB.setWidget(2, 2, createWidget("B3c", 2, 2));
		gridB.setWidget(2, 3, createWidget("B3d", 2, 3));

		gridC.setWidget(0, 0, createWidget("C1", 0, 0));
		gridC.setWidget(1, 0, createWidget("C2a", 1, 0));
		gridC.setWidget(1, 1, createWidget("C2b", 1, 1));

		gridD.setWidget(0, 0, createWidget("D", 0, 0));
		gridD.setWidget(1, 0, createWidget("D1", 1, 0));
		gridD.setWidget(1, 1, createWidget("D2", 1, 1));

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
		for (int i = 1; i < gridB.getRowCount(); i++) {
			String tempB = "";
			for (int j = 0; j < gridB.getColumnCount(); j++) {
				if (gridB.getWidget(i, j) != null && ((CheckBox) gridB.getWidget(i, j)).isChecked()) {
					String temp = ((CheckBox) gridB.getWidget(i, j)).getText().trim();
					if (tempB.equals("")) {
						tempB = temp.substring(1, 2);
					}
					tempB += temp.substring(2, 3);

				}
			}
			if (!tempB.equals(""))
				getChecked.append(tempB + "+");
		}

		if (((CheckBox) gridB.getWidget(0, 0)).isChecked()) {
			getChecked.insert(0, "1+");
		}

		if (getChecked.length() > 0) {
			return getChecked.substring(0, getChecked.length() - 1);
		} else
			return getChecked.toString();
	}

	protected String getCchecked() {

		boolean checked1 = ((CheckBox) gridC.getWidget(0, 0)).isChecked();
		boolean checked2 = ((CheckBox) gridC.getWidget(1, 0)).isChecked();
		boolean checked3 = ((CheckBox) gridC.getWidget(1, 1)).isChecked();

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
		if (((CheckBox) gridD.getWidget(0, 0)).isChecked()) {
			temp = "D";
		} else {
			if ((((CheckBox) gridD.getWidget(1, 0)).isChecked()) && (((CheckBox) gridD.getWidget(1, 1)).isChecked())) {
				temp = "D1+2";
			} else if (((CheckBox) gridD.getWidget(1, 0)).isChecked())
				temp = "D1";
			else if (((CheckBox) gridD.getWidget(1, 1)).isChecked())
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

	@Override
	public final void parseCriteriaString(String criteriaString) {
//		parse3_1CriteriaString(criteriaString);
		parse2_3CriteriaString(criteriaString);
	}

	@Override
	public boolean isCriteriaValid(String criteria, String category) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
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
}