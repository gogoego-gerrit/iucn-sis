package org.iucn.sis.client.panels.criteracalculator;

import java.util.HashMap;

import org.iucn.sis.client.panels.PanelManager;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CriteriaStringParser extends VerticalPanel {

	private static final int NONE = 0;
	private static final int CRITICALLYENDANGERED = 1;

	private PanelManager manager;

	private HTML result;
	private Button createStringButton;
	private Grid gridA;
	private Grid gridB;
	private Grid gridC;
	private Grid gridD;
	private Grid gridE;

	/*
	 * Maps string of single criteria to grid row, column Criteria String ->
	 * row,column
	 */
	private HashMap classificationToGrid;

	public CriteriaStringParser(PanelManager manager) {
		this.manager = manager;
		classificationToGrid = new HashMap();
		result = new HTML("Criteria String: ");
		build();
	}

	private void build() {

		add(result);

		buildGrids();
		add(gridA);
		add(gridB);
		add(gridC);
		add(gridD);
		add(gridE);

		buildButton();
		add(createStringButton);

	}

	private void buildButton() {
		createStringButton = new Button("Update Criteria String", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				updateResult(createCriteriaString());
			}
		});
	}

	private void buildGrids() {
		gridA = new Grid(4, 5);
		gridB = new Grid(6, 5);
		gridC = new Grid(3, 2);
		gridD = new Grid(3, 1);
		gridE = new Grid(1, 1);
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
		gridA.setWidget(2, 0, createWidget("A3a", 2, 0));
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
		gridB.setWidget(2, 4, createWidget("B1c(v)", 2, 4));
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
		gridB.setWidget(5, 4, createWidget("B2c(v)", 5, 4));

		gridC.setWidget(0, 0, createWidget("C1", 0, 0));
		gridC.setWidget(1, 0, createWidget("C2a(i)", 1, 0));
		gridC.setWidget(1, 1, createWidget("C2a(ii)", 1, 1));
		gridC.setWidget(2, 0, createWidget("C2b", 2, 0));

		gridD.setWidget(0, 0, createWidget("D", 0, 0));
		gridD.setWidget(1, 0, createWidget("D1", 1, 0));
		gridD.setWidget(2, 0, createWidget("D2", 2, 0));

		gridE.setWidget(0, 0, createWidget("E", 0, 0));
	}

	// private int possibleClassifications(){
	//		
	// }

	/**
	 * Looks at the grids, and creates a criteria string from that.
	 */
	private String createCriteriaString() {
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

	private CheckBox createWidget(String text, int i, int j) {
		classificationToGrid.put(text, i + "," + j);
		return new CheckBox("  " + text);
	}

	private String getAchecked() {
		StringBuffer getChecked = new StringBuffer();
		for (int i = 0; i < gridA.getRowCount(); i++) {
			String tempA = "";
			for (int j = 0; j < gridA.getColumnCount(); j++) {
				if (((CheckBox) gridA.getWidget(i, j)).isChecked()) {
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

	private String getBchecked() {
		StringBuffer getChecked = new StringBuffer();
		String temp1 = "";
		String temp2 = "";
		for (int i = 0; i < gridB.getRowCount(); i++) {
			String innerIndex = "";
			String letter = "";
			for (int j = 0; j < gridB.getColumnCount(); j++) {
				if ((((CheckBox) gridB.getWidget(i, j)) != null) && (((CheckBox) gridB.getWidget(i, j)).isChecked())) {
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

	private String getCchecked() {
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

	private String getDchecked() {
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

	private String getEchecked() {
		String temp = "";
		if (((CheckBox) gridE.getWidget(0, 0)).isChecked()) {
			temp = "E";
		}
		return temp;
	}

	private void updateResult(String criteriaString) {
		result.setText("Criteria String: " + criteriaString);
	}

}
