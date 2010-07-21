package org.iucn.sis.shared.structures;

import java.util.HashMap;

import org.iucn.sis.shared.data.CanParseCriteriaString;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class CriteriaGrid extends CanParseCriteriaString {

	protected Grid gridA;
	protected Grid gridB;
	protected Grid gridC;
	protected Grid gridD;
	protected Grid gridE;

	protected VerticalPanel gridPanel;

	protected HashMap classificationToGrid;

	public CriteriaGrid() {
		classificationToGrid = new HashMap();
		buildGrid();

		gridPanel = new VerticalPanel();
		gridPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		gridPanel.setSpacing(4);

		HorizontalPanel hp1 = new HorizontalPanel();
		hp1.setSpacing(4);
		hp1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp1.add(new HTML("Criterion A:"));
		hp1.add(gridA);
		hp1.setStyleName("summary-border");
		gridPanel.add(hp1);

		hp1 = new HorizontalPanel();
		hp1.setSpacing(4);
		hp1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp1.add(new HTML("Criterion B: "));
		hp1.add(gridB);
		hp1.addStyleName("summary-border");
		gridPanel.add(hp1);
		hp1 = new HorizontalPanel();
		hp1.setSpacing(4);
		hp1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp1.add(new HTML("Criterion C: "));
		hp1.add(gridC);
		hp1.addStyleName("summary-border");
		hp1.setSpacing(4);
		hp1.add(new HTML("Criterion D: "));
		hp1.add(gridD);
		hp1.addStyleName("summary-border");
		hp1.add(new HTML("Criterion E: "));
		hp1.add(gridE);
		gridPanel.add(hp1);
	}

	/**
	 * This function should instantiate the grid
	 */
	public abstract void buildGrid();

	protected void check(String grid, String key) {

		String index = (String) classificationToGrid.get(key);
		try {
			String[] keys = index.split(",");
			int row = Integer.valueOf(keys[0]).intValue();
			int col = Integer.valueOf(keys[1]).intValue();

			if (grid.equalsIgnoreCase("A")) {
				((CheckBox) gridA.getWidget(row, col)).setChecked(true);
			} else if (grid.equalsIgnoreCase("B")) {
				((CheckBox) gridB.getWidget(row, col)).setChecked(true);
			} else if (grid.equalsIgnoreCase("C")) {
				((CheckBox) gridC.getWidget(row, col)).setChecked(true);
			} else if (grid.equalsIgnoreCase("D")) {
				((CheckBox) gridD.getWidget(row, col)).setChecked(true);
			} else if (grid.equalsIgnoreCase("E")) {
				((CheckBox) gridE.getWidget(row, col)).setChecked(true);
			}

		} catch (Exception e) {
		}

	}

	/**
	 * Clears all grid info
	 */
	public void clearWidgets() {
		for (int i = 0; i < gridA.getColumnCount(); i++) {
			for (int j = 0; j < gridA.getRowCount(); j++) {
				if (gridA.getWidget(j, i) != null)
					((CheckBox) gridA.getWidget(j, i)).setChecked(false);
			}
		}

		for (int i = 0; i < gridB.getColumnCount(); i++) {
			for (int j = 0; j < gridB.getRowCount(); j++) {
				if (gridB.getWidget(j, i) != null)
					((CheckBox) gridB.getWidget(j, i)).setChecked(false);
			}
		}

		for (int i = 0; i < gridC.getColumnCount(); i++) {
			for (int j = 0; j < gridC.getRowCount(); j++) {
				if (gridC.getWidget(j, i) != null)
					((CheckBox) gridC.getWidget(j, i)).setChecked(false);
			}
		}

		for (int i = 0; i < gridD.getColumnCount(); i++) {
			for (int j = 0; j < gridD.getRowCount(); j++) {
				if (gridD.getWidget(j, i) != null)
					((CheckBox) gridD.getWidget(j, i)).setChecked(false);
			}
		}

		for (int i = 0; i < gridE.getColumnCount(); i++) {
			for (int j = 0; j < gridE.getRowCount(); j++) {
				if (gridE.getWidget(j, i) != null)
					((CheckBox) gridE.getWidget(j, i)).setChecked(false);
			}
		}
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

	protected CheckBox createWidget(String text, int i, int j) {
		classificationToGrid.put(text, i + "," + j);

		return new CheckBox("  " + text);
	}

	@Override
	public void foundCriterion(String prefix, String suffix) {
		check(prefix, suffix);
	}

	protected abstract String getAchecked();

	protected abstract String getBchecked();

	protected abstract String getCchecked();

	protected abstract String getDchecked();

	protected abstract String getEchecked();

	public VerticalPanel getWidget() {
		return gridPanel;
	}

	public abstract boolean isChecked(String key);

	public abstract boolean isCriteriaValid(String criteriaString, String category);

	/**
	 * Publicly accessible way to have this criteria grid parse a criteria
	 * String. Based on the grid's version, this should invoke the appropriate
	 * parse function from the CanParseCriteriaString abstract class.
	 * 
	 * @param criteriaString
	 */
	public abstract void parseCriteriaString(String criteriaString);

	protected void setEnabled(boolean isManual) {
		for (int i = 0; i < gridA.getColumnCount(); i++) {
			for (int j = 0; j < gridA.getRowCount(); j++) {
				if (gridA.getWidget(j, i) != null)
					((CheckBox) gridA.getWidget(j, i)).setEnabled(isManual);
			}
		}

		for (int i = 0; i < gridB.getColumnCount(); i++) {
			for (int j = 0; j < gridB.getRowCount(); j++) {
				if (gridB.getWidget(j, i) != null)
					((CheckBox) gridB.getWidget(j, i)).setEnabled(isManual);
			}
		}

		for (int i = 0; i < gridC.getColumnCount(); i++) {
			for (int j = 0; j < gridC.getRowCount(); j++) {
				if (gridC.getWidget(j, i) != null)
					((CheckBox) gridC.getWidget(j, i)).setEnabled(isManual);
			}
		}

		for (int i = 0; i < gridD.getColumnCount(); i++) {
			for (int j = 0; j < gridD.getRowCount(); j++) {
				if (gridD.getWidget(j, i) != null)
					((CheckBox) gridD.getWidget(j, i)).setEnabled(isManual);
			}
		}

		for (int i = 0; i < gridE.getColumnCount(); i++) {
			for (int j = 0; j < gridE.getRowCount(); j++) {
				if (gridE.getWidget(j, i) != null)
					((CheckBox) gridE.getWidget(j, i)).setEnabled(isManual);
			}
		}
	}

	public void setVisible(boolean visible) {
		gridPanel.setVisible(visible);
	}

	/**
	 * This is called whenever the resulting criteria String is changed.
	 * 
	 * @param result
	 */
	protected abstract void updateCriteriaString(String result);
}
