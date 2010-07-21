package org.iucn.sis.client.expert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class RegionalExpertWidget extends LayoutContainer {

	private HTML displayResult;
	private VerticalPanel results;
	private LayoutContainer tableEmulator;
//	private TableLayout tableLayout;

	private RegionalExpertQuestions questions;
	private String result;
	private HashMap<ListBox, String> listToQuestion;
	private LinkedHashMap<String, String> questionToAnswer;
	private ArrayList<String> questionIndex;
	private ListBox amount;

	public RegionalExpertWidget() {
//		setWidth("100%");

		results = new VerticalPanel();
		results.setSpacing(4);

		tableEmulator = new LayoutContainer();
		tableEmulator.setLayout(new RowLayout(Orientation.VERTICAL));
		
//		tableLayout = new TableLayout(2);
//		tableLayout.setCellPadding(8);
//		tableLayout.setWidth("100%");
//		tableEmulator.setLayout(tableLayout);

		questions = new RegionalExpertQuestions();
		listToQuestion = new HashMap<ListBox, String>();
		questionToAnswer = new LinkedHashMap<String, String>();
		questionIndex = new ArrayList<String>();
		displayResult = new HTML();
		displayResult.addStyleName("redFont");
		amount = new ListBox();
		amount.addItem(" --- Select --- ", "0");
		amount.addItem("1", "1");
		amount.addItem("2", "2");
		
		setLayout(new RowLayout(Orientation.VERTICAL));
		add(tableEmulator, new RowData(1, -1));
		add(new Html("<hr>"), new RowData(1, -1));
		add(results, new RowData(1, -1));
		
		buildTable();
	}

	private ListBox addRow(String question) {
		int row = getNumRows();
		ListBox list = buildAnswer();
		listToQuestion.put(list, question);
		questionIndex.add(question);
		tableEmulator.add(buildRow(new Html(row + ".  " + question), list), new RowData(1, -1));
//		tableEmulator.add(new Html(row + ".  " + question));
//		tableEmulator.add(list);

		return list;
	}
	
	private HorizontalPanel buildRow(Html label, ListBox list) {
		HorizontalPanel panel = new HorizontalPanel();
		panel.setSpacing(3);
		panel.add(label);
		panel.add(list);
		return panel;
	}

	// NO NEED TO BUILD ANOTHER LIST
	private void addRow(String question, ListBox list) {
		int row = getNumRows();
		tableEmulator.add(buildRow(new Html(row + ".  " + question), list), new RowData(1, -1));
//		tableEmulator.add(new Html(row + ".  " + question));
//		tableEmulator.add(list);
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
		results.removeAll();
		results.add(displayResult);

		if (result.equalsIgnoreCase(RegionalExpertQuestions.DOWNGRADE)
				|| result.equalsIgnoreCase(RegionalExpertQuestions.UPGRADE)) {
			HorizontalPanel p = new HorizontalPanel();
			p.setVerticalAlign(VerticalAlignment.BOTTOM);
			p.add(new HTML("How many categories?&nbsp;"));
			p.add(amount);
			results.add(p);
		} else {
			amount.setSelectedIndex(0);
		}
		layout();
	}

	private void buildTable() {
		addRow(questions.getFirstQuestion());
	}

	private void clearAfterRow(int row) {
		while (getNumRows() - 1 > row) {
			tableEmulator.remove(tableEmulator.getWidget(tableEmulator.getItemCount() - 1));
//			tableEmulator.remove(tableEmulator.getWidget(tableEmulator.getItemCount() - 1));

			questionToAnswer.remove(questionIndex.get(questionIndex.size() - 1));
			questionIndex.remove(questionIndex.size() - 1);
		}

		gotResult("");
	}

	public void clearWidgetData() {
		questionToAnswer.clear();
		listToQuestion.clear();
		questionIndex.clear();
		tableEmulator.removeAll();

		buildTable();
		gotResult("");
		layout();
	}

	private ListBox determineNextMove(ListBox list) {
		ListBox ret = null;

		String question = listToQuestion.get(list);
		int row = questionIndex.indexOf(question);
		clearAfterRow(row);

		// UNSELECTED THINGS
		String answer = list.getValue(list.getSelectedIndex());
		questionToAnswer.put(question, String.valueOf(list.getSelectedIndex()));

		if (!answer.equalsIgnoreCase("")) {
			String nextQuestion;
			if (answer.equalsIgnoreCase(RegionalExpertQuestions.YES + "")) {
				nextQuestion = questions.getNextQuestion(question, RegionalExpertQuestions.YES);
			} else if (answer.equalsIgnoreCase(RegionalExpertQuestions.DONTKNOW + "")) {
				nextQuestion = questions.getNextQuestion(question, RegionalExpertQuestions.DONTKNOW);
			} else {
				nextQuestion = questions.getNextQuestion(question, RegionalExpertQuestions.NO);
			}

			if (listToQuestion.containsValue(nextQuestion)) {
				boolean found = false;
				ListBox listTemp = null;

				for (Entry<ListBox, String> curEntry : listToQuestion.entrySet()) {
					if (found)
						break;

					listTemp = curEntry.getKey();
					if (listToQuestion.get(listTemp).equals(nextQuestion)) {
						list = listTemp;
						found = true;
					}
				}
				addRow(nextQuestion, listTemp);
				determineNextMove(listTemp);

				ret = listTemp;
			}

			else if (questions.isResult(nextQuestion)) {
				gotResult(nextQuestion);
			}

			else {
				ret = addRow(nextQuestion);
			}
		}

		layout();

		return ret;
	}

	private int getNumRows() {
		return tableEmulator.getItemCount();
	}

	public String getResultString() {
		return result;
	}

	public String getWidgetData() {
		String ret = getResultString();
		if (ret == null)
			ret = "";
		ret += "," + amount.getValue(amount.getSelectedIndex());

		for (Entry<String, String> curEntry : questionToAnswer.entrySet())
			ret += "," + curEntry.getValue();

		return ret;
	}

	private void gotResult(String result) {
		this.result = result;

		if (result.equalsIgnoreCase(RegionalExpertQuestions.DOWNGRADE)) {
			displayResult.setText("Downgrade category");
		} else if (result.equalsIgnoreCase(RegionalExpertQuestions.UPGRADE)) {
			displayResult.setText("Upgrade category");
		} else if (result.equalsIgnoreCase(RegionalExpertQuestions.NOCHANGE)) {
			displayResult.setText("No change in category");
		} else {
			displayResult.setText("(No result)");
		}

		buildResults();
	}

	public void setWidgetData(String data) {
		if (data == null || data.equals(""))
			clearWidgetData();
		else {
			questionToAnswer.clear();
			listToQuestion.clear();
			questionIndex.clear();
			tableEmulator.removeAll();

			try {
				String[] split = data.split(",");

				result = split[0];
				amount.setSelectedIndex(Integer.valueOf(split[1]));

				ListBox curBox = addRow(questions.getFirstQuestion());
				for (int i = 2; i < split.length; i++) {
					if (curBox != null) {
						curBox.setSelectedIndex(Integer.valueOf(split[i]));
						curBox = determineNextMove(curBox);
					}
				}

				result = split[0];
				amount.setSelectedIndex(Integer.valueOf(split[1]));
				gotResult(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
