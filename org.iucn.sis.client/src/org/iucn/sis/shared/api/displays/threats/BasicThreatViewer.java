package org.iucn.sis.shared.api.displays.threats;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.shared.api.data.LookupData;
import org.iucn.sis.shared.api.data.LookupData.LookupDataValue;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.fields.StressField;
import org.iucn.sis.shared.api.models.fields.ThreatsSubfield;
import org.iucn.sis.shared.api.structures.ClassificationInfo;
import org.iucn.sis.shared.api.structures.Structure;

import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class BasicThreatViewer extends Structure<Field> {
	
	private static final String IMPACT_RATING_HIGH = "High Impact";
	private static final String IMPACT_RATING_MEDIUM = "Medium Impact";
	private static final String IMPACT_RATING_LOW = "Low Impact";
	private static final String IMPACT_RATING_NO_NEGLIGIBLE = "No/Negligible Impact";
	private static final String IMPACT_RATING_PAST = "Past Impact";
	private static final String IMPACT_RATING_UNKNOWN = "Unknown";
	
	private final int[] timingImpact, scopeImpact, severityImpact;
	
	private ListBox timing, scope, severity;
	private HTML impactScore;
	
	private DataList stresses;
	
	public BasicThreatViewer(ThreatsTreeData data) {
		super(null, data.getDescription(), "none", data);
		
		timingImpact = new int[] { 0, 30, 3, 1, -10, 20	};
		scopeImpact = new int[] { 0, 3, 2, 1, -10 };
		severityImpact = new int[] { 0, 3, 2, 1, 1, 0, 0, -10 };
	}
	
	private String determineImpact() {
		return determineImpact(getListValue(timing, 0), getListValue(scope, 0), getListValue(severity, 0));
	}
	
	private String determineImpact(Integer timingValue, Integer scopeValue, Integer severityValue) {
		int score = timingImpact[timingValue] + 
			scopeImpact[scopeValue] + severityImpact[severityValue];

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
	
	@Override
	public void createWidget() {
		ThreatsTreeData treeData = (ThreatsTreeData)data;
		
		initializeListBox(timing = new ListBox(), treeData.getLookups().get("Threats_timingLookup"));
		initializeListBox(scope = new ListBox(), treeData.getLookups().get("Threats_scopeLookup"));
		initializeListBox(severity = new ListBox(), treeData.getLookups().get("Threats_severityLookup"));
		
		impactScore = new HTML("(Not specified)");
		
		final ChangeHandler impactHandler = new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				impactScore.setText(determineImpact());
			}
		};
		timing.addChangeHandler(impactHandler);
		scope.addChangeHandler(impactHandler);
		severity.addChangeHandler(impactHandler);
		
		stresses = new DataList();
		stresses.setCheckable(true);
		for (TreeDataRow row : treeData.getTreeData("Stresses").flattenTree().values()) {
			DataListItem item = new DataListItem();
			item.setItemId(row.getDisplayId());
			item.setId(row.getDisplayId());
			item.setText(row.getLabel());
			
			stresses.add(item);
		}
	}
	
	private void initializeListBox(ListBox box, LookupData data) {
		box.addItem("--- Select ---", null);
		for (LookupDataValue value : data.getValues())
			box.addItem(value.getLabel(), value.getID());
	}
	
	@Override
	public ArrayList<String> extractDescriptions() {
		final ArrayList<String> list = new ArrayList<String>();
		list.add("Timing");
		list.add("Scope");
		list.add("Severity");
		list.add("Impact Score");
		list.add("No. of Stresses");
		return list;
	}
	
	@Override
	public boolean hasChanged(Field rawField) {
		ThreatsSubfield field = new ThreatsSubfield(rawField);
		final List<Integer> selStresses = new ArrayList<Integer>();
		for (DataListItem item : stresses.getChecked())
			selStresses.add(Integer.valueOf(item.getItemId()));
		
		final List<Integer> oldValueStresses = new ArrayList<Integer>();
		for (StressField stress : field.getStresses())
			oldValueStresses.add(stress.getStress());
		
		return selStresses.size() != oldValueStresses.size() || 
			!selStresses.containsAll(oldValueStresses) ||
			hasListSelectionChanged(field.getTiming(), timing) || 
			hasListSelectionChanged(field.getScope(), scope) || 
			hasListSelectionChanged(field.getSeverity(), severity);
	}
	
	private boolean hasListSelectionChanged(Integer oldValue, ListBox newValueContainer) {
		Integer selected = null;
		if (hasListValue(newValueContainer))
			selected = Integer.valueOf(newValueContainer.getValue(newValueContainer.getSelectedIndex()));
		
		if (selected == null)
			return oldValue != null;
		else
			return selected.equals(oldValue);
	}
	
	private boolean hasListValue(ListBox listBox) {
		return listBox.getSelectedIndex() > 0;
	}
	
	private Integer getListValue(ListBox listBox) {
		return getListValue(listBox, null);
	}
	
	private Integer getListValue(ListBox listBox, Integer defaultValue) {
		return hasListValue(listBox) ? 
			Integer.valueOf(listBox.getValue(listBox.getSelectedIndex())) : 
			defaultValue;
	}
	
	private String getListText(ListBox listBox) {
		return hasListValue(listBox) ? 
			listBox.getItemText(listBox.getSelectedIndex()) : 
			"(Not specified)";
	}
	
	private void setListValue(ListBox listBox, Integer value) {
		if (value != null)
			for (int i = 1; i < listBox.getItemCount(); i++) {
				if (value.equals(Integer.valueOf(listBox.getValue(i)))) {
					listBox.setSelectedIndex(i);
					break;
				}
			}
	}
	
	private String getScore() {
		return !hasListValue(timing) && 
			!hasListValue(scope) && 
			!hasListValue(severity) ? 
				"(Not specified)" :
				determineImpact();
	}
	
	public String getData() {
		return null;
	}
	
	@Override
	public void save(Field parent, Field rawField) {
		ThreatsSubfield field = new ThreatsSubfield(rawField);
		/*
		 * Field will never be null, because user must 
		 * have already selected a threat.
		 */
		if (hasListValue(timing))
			field.setTiming(getListValue(timing));
		
		if (hasListValue(scope))
			field.setScope(getListValue(scope));
		
		if (hasListValue(severity))
			field.setSeverity(getListValue(severity));
		
		field.setScore(getScore());
		
		List<Integer> list = new ArrayList<Integer>();
		for (DataListItem item : stresses.getChecked())
			list.add(Integer.valueOf(item.getItemId()));
		
		field.setStresses(list);
	}
	
	@Override
	public void setData(Field raw) {
		ThreatsSubfield field = new ThreatsSubfield(raw);
		
		setListValue(timing, field.getTiming());
		setListValue(scope, field.getScope());
		setListValue(severity, field.getSeverity());
		
		for (StressField stress : field.getStresses()) {
			DataListItem item = 
				stresses.getItemByItemId(stress.getStress().toString());
			if (item != null)
				item.setChecked(true);
		}
		
		impactScore.setText(getScore());
	}
	
	@Override
	public void clearData() {
		timing.setSelectedIndex(-1);
		scope.setSelectedIndex(-1);
		severity.setSelectedIndex(-1);
		impactScore.setText("");
		
		for (DataListItem item : stresses.getItems())
			item.setChecked(false);
	}
	
	@Override
	protected Widget createLabel() {
		displayPanel = new HorizontalPanel();
		
		final VerticalPanel left = new VerticalPanel();
		final Grid grid = new Grid(4, 2);
		grid.setHTML(0, 0, "Timing: ");
		grid.setWidget(0, 1, timing);
		grid.setHTML(1, 0, "Scope: ");
		grid.setWidget(1, 1, scope);
		grid.setHTML(2, 0, "Severity: ");
		grid.setWidget(2, 1, severity);
		grid.setHTML(3, 0, "Impact Score: ");
		grid.setWidget(3, 1, impactScore);
		
		left.add(grid);
		
		displayPanel.add(left);
		displayPanel.add(stresses);
		
		return displayPanel;
	}
	
	@Override
	protected Widget createViewOnlyLabel() {
		displayPanel = new HorizontalPanel();
		
		final VerticalPanel left = new VerticalPanel();
		final Grid grid = new Grid(4, 2);
		grid.setHTML(0, 0, "Timing: ");
		grid.setWidget(0, 1, timing);
		grid.setHTML(1, 0, "Scope: ");
		grid.setWidget(1, 1, scope);
		grid.setHTML(2, 0, "Severity: ");
		grid.setWidget(2, 1, severity);
		grid.setHTML(3, 0, "Impact Score: ");
		grid.setWidget(3, 1, impactScore);
		
		left.add(grid);
		
		displayPanel.add(left);
		displayPanel.add(stresses);
		
		return displayPanel;
	}
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		List<ClassificationInfo> list = new ArrayList<ClassificationInfo>();
		list.add(new ClassificationInfo("Timing", getListText(timing)));
		list.add(new ClassificationInfo("Scope", getListText(scope)));
		list.add(new ClassificationInfo("Severity", getListText(severity)));
		list.add(new ClassificationInfo("Impact Score", getScore()));
		list.add(new ClassificationInfo("No. of Stresses", stresses.getChecked().size()+""));
		
		return list;
	}
	
	@Override
	public int getDisplayableData(ArrayList<String> rawData,
			ArrayList<String> prettyData, int offset) {
		throw new UnsupportedOperationException();
		/*ThreatsTreeData treeData = (ThreatsTreeData)data;
		
		String timingValue, scopeValue, severityValue;
		
		prettyData.add(offset, getPrettyData(timingValue = prettyData.get(offset), treeData.getLookups().get("Threats_timingLookup")));
		offset++;
		prettyData.add(offset, getPrettyData(scopeValue = prettyData.get(offset), treeData.getLookups().get("Threats_scopeLookup")));
		offset++;
		prettyData.add(offset, getPrettyData(severityValue = prettyData.get(offset), treeData.getLookups().get("Threats_severityLookup")));
		offset++;
		
		prettyData.add(offset++, determineImpact(Integer.valueOf(timingValue), Integer.valueOf(scopeValue), Integer.valueOf(severityValue)));
		prettyData.add(offset, rawData.get(offset));
		
		return ++offset;*/
	}
	
	private String getPrettyData(String id, LookupData data) {
		if ("-1".equals(id))
			return "(Not specified)";
		else
			return data.getLabel(id);
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		timing.setEnabled(isEnabled);
		scope.setEnabled(isEnabled);
		severity.setEnabled(isEnabled);
		stresses.setEnabled(isEnabled);
	}

}