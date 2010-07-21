package org.iucn.sis.shared.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.iucn.sis.client.components.panels.PanelManager;
import org.iucn.sis.client.expert.RegionalExpertQuestions;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class SISRegionalExpert extends LayoutContainer {

	private PanelManager manager;
	private Window shell;
	private HTML displayResult;
	private Button button;
	private HorizontalPanel results;
	private int listboxColumn = 1;
	private FlexTable table;
	private RegionalExpertQuestions questions;
	private String result;
	private HashMap listToQuestion;
	private ArrayList questionIndex;

	public SISRegionalExpert(Window shell, PanelManager manager) {

		setLayout(new RowLayout(Orientation.HORIZONTAL));

		this.manager = manager;
		this.shell = shell;
		results = new HorizontalPanel();
		table = new FlexTable();
		questions = new RegionalExpertQuestions();
		listToQuestion = new HashMap();
		questionIndex = new ArrayList();
		buildTable();
		add(table, new RowData(1d, 1d));
		add(results, new RowData(1d, 25));
	}

	private void addRow(String question) {
		int row = table.getRowCount();
		table.setText(row, 0, row + ".  " + question);
		ListBox list = buildAnswer();
		listToQuestion.put(list, question);
		questionIndex.add(question);
		table.setWidget(row, listboxColumn, list);
	}

	// NO NEED TO BUILD ANOTHER LIST
	private void addRow(String question, ListBox list) {
		int row = table.getRowCount();
		table.setText(row, 0, row + ".  " + question);
		table.setWidget(row, listboxColumn, list);
		questionIndex.add(question);
	}

	private ListBox buildAnswer() {
		final ListBox list = new ListBox(false);
		list.addItem("", "");
		list.addItem("Yes", RegionalExpertQuestions.YES + "");
		list.addItem("Don't Know", RegionalExpertQuestions.DONTKNOW + "");
		list.addItem("No", RegionalExpertQuestions.NO + "");

		list.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				determineNextMove(list);
			}
		});

		return list;
	}

	private void buildResults() {
		button = new Button("Cancel", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				close();

			}

		});
		displayResult = new HTML();

		results.add(displayResult);
		results.add(button);
		results.setCellHorizontalAlignment(button, HasHorizontalAlignment.ALIGN_RIGHT);
	}

	private void buildTable() {
		table.setCellPadding(4);
		addRow(questions.getFirstQuestion());

	}

	private void clearAfterRow(int row) {
		for (int i = row + 1; i < table.getRowCount(); i++) {
			ListBox list = (ListBox) table.getWidget(row + 1, listboxColumn);
			// listToQuestion.remove(list);
			table.removeRow(row + 1);
			questionIndex.remove(row + 1);
		}
		result = "";
	}

	private void close() {
		if (!button.getText().equalsIgnoreCase("Cancel")) {
			// TODO: SAVE RESULT
			// manager.AssessmentStufs.setRegionalResult(result)
		}
		shell.hide();
	}

	private void determineNextMove(ListBox list) {
		String question = (String) listToQuestion.get(list);
		int row = questionIndex.indexOf(question);
		clearAfterRow(row);

		// UNSELECTED THINGS
		if (!list.getValue(list.getSelectedIndex()).equalsIgnoreCase("")) {
			String nextQuestion;
			if (list.getValue(list.getSelectedIndex()).equalsIgnoreCase(RegionalExpertQuestions.YES + ""))
				nextQuestion = questions.getNextQuestion(question, RegionalExpertQuestions.YES);

			else if (list.getValue(list.getSelectedIndex()).equalsIgnoreCase(RegionalExpertQuestions.DONTKNOW + ""))
				nextQuestion = questions.getNextQuestion(question, RegionalExpertQuestions.DONTKNOW);

			else
				nextQuestion = questions.getNextQuestion(question, RegionalExpertQuestions.NO);

			if (listToQuestion.containsValue(nextQuestion)) {
				Iterator iter = listToQuestion.keySet().iterator();
				boolean found = false;
				ListBox listTemp = null;
				while (iter.hasNext() && !found) {
					listTemp = (ListBox) iter.next();
					if (listToQuestion.get(listTemp).equals(nextQuestion)) {
						list = listTemp;
					}
				}
				addRow(nextQuestion, listTemp);
				determineNextMove(listTemp);
			}

			else if (questions.isResult(nextQuestion)) {
				gotResult(result);
			}

			else {
				addRow(nextQuestion);
			}
		}

	}

	private void gotResult(String result) {
		this.result = result;
		if (result.equalsIgnoreCase(RegionalExpertQuestions.DOWNGRADE)) {
			displayResult.setText("Downgrade category");
		} else if (result.equalsIgnoreCase(RegionalExpertQuestions.UPGRADE)) {
			displayResult.setText("Upgrade category");
		} else
			displayResult.setText("No change in category");
	}

}
