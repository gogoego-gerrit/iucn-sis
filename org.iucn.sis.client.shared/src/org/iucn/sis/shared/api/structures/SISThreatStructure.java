package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SISThreatStructure extends Structure<Field> implements DominantStructure<Field> {

	public static final int TIMING_PAST_UNLIKELY_RETURN_INDEX = 1;
	public static final int TIMING_ONGOING_INDEX = 2;
	public static final int TIMING_FUTURE_INDEX = 3;
	public static final int TIMING_UNKNOWN_INDEX = 4;
	public static final int TIMING_PAST_LIKELY_RETURN_INDEX = 5;

	public static final String THREAT_ID_KEY = "ThreatsLookup";
	public static final String THREATS_TIMING_KEY = "timing";
	public static final String SCOPE_TIMING_KEY = "scope";
	public static final String SEVERITY_TIMING_KEY = "severity";
	public static final String SCORE_TIMING_KEY = "score";
	
	private static final String IMPACT_RATING_HIGH = "High Impact";
	private static final String IMPACT_RATING_MEDIUM = "Medium Impact";
	private static final String IMPACT_RATING_LOW = "Low Impact";
	private static final String IMPACT_RATING_NO_NEGLIGIBLE = "No/Negligible Impact";
	private static final String IMPACT_RATING_PAST = "Past Impact";
	private static final String IMPACT_RATING_UNKNOWN = "Unknown";

	private ListBox timing;
	private ListBox scope;
	private ListBox severity;
	private Label impactScore;

	private boolean activeToggle = true;

	public SISThreatStructure(String struct, String descript, String structID) {
		super(struct, descript, structID);
		// displayPanel = new VerticalPanel();
		buildContentPanel(Orientation.VERTICAL);
		canRemoveDescription = false;
	}
	
	@Override
	public void save(Field parent, Field field) {
		if (field == null) {
			field = new Field();
			field.setName(getId());
			field.setParent(parent);
		}
		
		field.addPrimitiveField(new ForeignKeyPrimitiveField(THREATS_TIMING_KEY, field, 
				Integer.valueOf(timing.getSelectedIndex()), null));
		field.addPrimitiveField(new ForeignKeyPrimitiveField(SCOPE_TIMING_KEY, field, 
				Integer.valueOf(scope.getSelectedIndex()), null));
		field.addPrimitiveField(new ForeignKeyPrimitiveField(SEVERITY_TIMING_KEY, field, 
				Integer.valueOf(severity.getSelectedIndex()), null));
		
		
		int myTiming = Integer.parseInt(timing.getValue(timing.getSelectedIndex()));
		int myScope = Integer.parseInt(scope.getValue(scope.getSelectedIndex()));
		int mySeverity = Integer.parseInt(severity.getValue(severity.getSelectedIndex()));
		
		field.addPrimitiveField(new StringPrimitiveField(SCORE_TIMING_KEY, field, 
				impactScore.getText()));
	}
	
	@Override
	public void addListenerToActiveStructure(ChangeListener changeListener, ClickHandler clickListener,
			KeyboardListener keyboardListener) {
	}

	@Override
	public void clearData() {
		timing.setSelectedIndex(0);
		scope.setSelectedIndex(0);
		severity.setSelectedIndex(0);
		impactScore = new Label("");
	}

	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		SimplePanel tier1 = new SimplePanel();
		tier1.setWidget(descriptionLabel);

		int column = 0;
		FlexTable tier2 = new FlexTable();
		// tier2.setWidget(0, column++, showNotes);
		tier2.setWidget(0, column++, new HTML("Timing:&nbsp;"));
		tier2.setWidget(0, column++, timing);
		tier2.setWidget(0, column++, new HTML("Scope:&nbsp;"));
		tier2.setWidget(0, column++, scope);
		tier2.setWidget(0, column++, new HTML("Severity:&nbsp;"));
		tier2.setWidget(0, column++, severity);
		tier2.setWidget(0, column++, new HTML("Impact Score:&nbsp;"));
		tier2.setWidget(0, column++, impactScore);

		HorizontalPanel tNotes = new HorizontalPanel();
		tNotes.setSpacing(3);
		tNotes.setWidth("100%");

		displayPanel.add(tier1);
		displayPanel.add(tier2);
		displayPanel.add(tNotes);

		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();
		if (descriptionLabel != null)
			displayPanel.add(descriptionLabel);

		displayPanel.add(new HTML("Timing: " + timing.getItemText(timing.getSelectedIndex())));
		displayPanel.add(new HTML("; Scope: " + scope.getItemText(scope.getSelectedIndex())));
		displayPanel.add(new HTML("; Severity: " + severity.getItemText(severity.getSelectedIndex())));
		displayPanel.add(new HTML("; Impact: " + impactScore.getText()));
		// displayPanel.add(new HTML("; Notes: " + threatNotes.getText()));
		return displayPanel;
	}

	@Override
	public void createWidget() {

		ChangeListener impactChange = new ChangeListener() {
			public void onChange(Widget sender) {
				updateImpactScore();
			}
		};

		timing = new ListBox();
		timing.addItem("--- Select Timing ---", "0");
		timing.addItem("Past, Unlikely to Return", "30");
		timing.addItem("Ongoing", "3");
		timing.addItem("Future", "1");
		timing.addItem("Unknown", "-10");
		timing.addItem("Past, Likely to Return", "20");
		timing.setSelectedIndex(0);
		timing.addChangeListener(impactChange);
		
		
		DOM.setEventListener(timing.getElement(), timing);

		scope = new ListBox();
		scope.addItem("--- Select Scope ---", "0");
		scope.addItem("Whole (>90%)", "3");
		scope.addItem("Majority (50-90%)", "2");
		scope.addItem("Minority (<50%)", "1");
		scope.addItem("Unknown", "-10");
		scope.setSelectedIndex(0);
		scope.addChangeListener(impactChange);
		DOM.setEventListener(scope.getElement(), scope);

		severity = new ListBox();
		severity.addItem("--- Select Severity ---", "0");
		severity.addItem("Very Rapid Declines", "3");
		severity.addItem("Rapid Declines", "2");
		severity.addItem("Slow, Significant Declines", "1");
		severity.addItem("Causing/Could cause fluctuations", "1");
		severity.addItem("Negligible declines", "0");
		severity.addItem("No decline", "0");
		severity.addItem("Unknown", "-10");
		severity.setSelectedIndex(0);
		severity.addChangeListener(impactChange);
		DOM.setEventListener(severity.getElement(), severity);

		impactScore = new Label();
		updateImpactScore();
	}

	private String determineImpact(int timing, int scope, int severity) {
		int score = timing + scope + severity;

		if (score < 0) // SPECIAL CASE 2: UNKNOWN FOR ANY FIELD
			return IMPACT_RATING_UNKNOWN;
		else if (score >= 0 && score <= 2) // NO/NEGLIGIBLE IMPACT
			return IMPACT_RATING_NO_NEGLIGIBLE + ": " + score;
		else if (score >= 3 && score <= 5) // LOW IMPACT
			return IMPACT_RATING_LOW + ": " + score;
		else if (score >= 6 && score <= 7) // MEDIUM IMPACT
			return IMPACT_RATING_MEDIUM + ": " + score;
		else if (score >= 8 && score <= 9) // HIGH IMPACT
			return IMPACT_RATING_HIGH + ": " + score;
		else
			// SPECIAL CASE 1: PAST IMPACT
			return IMPACT_RATING_PAST;
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();
		ret.add("Timing");
		ret.add("Scope");
		ret.add("Severity");
		ret.add("Impact Score");
		ret.add("Notes");
		return ret;
	}
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		return new ArrayList<ClassificationInfo>();
	}

	@Override
	public String getData() {
		return null;
	}

	/**
	 * Pass in the raw data from an Assessment object, and this will return
	 * it in happy, displayable String form
	 * 
	 * @return ArrayList of Strings, having converted the rawData to nicely
	 *         displayable String data. Happy days!
	 */
	@Override
	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset) {
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				new Object[] { "Past", "Ongoing", "Future", "Unknown" }));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				new Object[] { "Whole (>90%)", "Majority (50-90%)", "Minority (<50%)", "Unknown" }));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				new Object[] { "Very Rapid Declines", "Rapid Declines", "Slow, Significant Declines",
						"Causing/Could cause fluctuations", "Negligible declines", "No decline", "Unknown" }));
		offset++;
		prettyData.add(offset, rawData.get(offset).toString());
		offset++;
		prettyData.add(offset, rawData.get(offset).toString());
		offset++;

		return offset;
	}

	public Label getImpactScore() {
		return impactScore;
	}

	public ListBox getScope() {
		return scope;
	}

	public ListBox getSeverity() {
		return severity;
	}

	public ListBox getTiming() {
		return timing;
	}
	
	@Override
	public boolean hasChanged(Field field) {
		// TODO Auto-generated method stub
		return true;
	}

	// This means non-codeable for me
	@Override
	public void hideWidgets() {
		hiddenWidgets = true;
		// displayPanel = new HorizontalPanel();
		buildContentPanel(Orientation.HORIZONTAL);

	}

	@Override
	public boolean isActive(Rule activityRule) {
		return (activeToggle = !activeToggle);
	}
	
	@Override
	public void setData(Field field) {
		Map<String, PrimitiveField> data = field.getKeyToPrimitiveFields();
		//super.setData(field);
		timing.setSelectedIndex(((ForeignKeyPrimitiveField)data.get(THREATS_TIMING_KEY)).getValue());
		scope.setSelectedIndex(((ForeignKeyPrimitiveField)data.get(SCOPE_TIMING_KEY)).getValue());
		severity.setSelectedIndex(((ForeignKeyPrimitiveField)data.get(SEVERITY_TIMING_KEY)).getValue());
		impactScore = new Label(((StringPrimitiveField)data.get(SCORE_TIMING_KEY)).getValue());

		updateImpactScore();

//		threatNotes.setText(XMLUtils.cleanFromXML((String) data.get(dataOffset)));
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		timing.setEnabled(isEnabled);
		scope.setEnabled(isEnabled);
		severity.setEnabled(isEnabled);
	}

	public String toXML() {
		return StructureSerializer.toXML(this);
	}

	private void updateImpactScore() {
		int myTiming = Integer.parseInt(timing.getValue(timing.getSelectedIndex()));
		int myScope = Integer.parseInt(scope.getValue(scope.getSelectedIndex()));
		int mySeverity = Integer.parseInt(severity.getValue(severity.getSelectedIndex()));
		String score = determineImpact(myTiming, myScope, mySeverity);

		impactScore.setVisible(false);
		impactScore.setText(score);
		impactScore.setVisible(true);
	}

	
	
}// class SISThreatStructure
