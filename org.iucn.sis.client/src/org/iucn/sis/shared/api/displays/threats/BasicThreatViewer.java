package org.iucn.sis.shared.api.displays.threats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.data.LookupData;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.data.LookupData.LookupDataValue;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.fields.StressField;
import org.iucn.sis.shared.api.models.fields.ThreatsSubfield;
import org.iucn.sis.shared.api.schemes.BasicClassificationSchemeViewer;
import org.iucn.sis.shared.api.schemes.CodingOptionTreePanel;
import org.iucn.sis.shared.api.structures.ClassificationInfo;
import org.iucn.sis.shared.api.structures.Structure;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.extjs.client.WindowUtils;

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
	
	private CodingOptionTreePanel stresses;
	//private DataList stresses;
	
	public BasicThreatViewer(ThreatsTreeData data) {
		super(null, data.getDescription(), null, data);
		
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
		
		stresses = new CodingOptionTreePanel(treeData.getTreeData("Stresses"), new ArrayList<TreeDataRow>(), new ArrayList<String>());
		/*stresses = new DataList();
		stresses.setCheckable(true);
		
		ArrayList<TreeDataRow> rows = new ArrayList<TreeDataRow>(
			treeData.getTreeData("Stresses").flattenTree().values()	
		);
		Collections.sort(rows, new BasicClassificationSchemeViewer.TreeDataRowComparator());
		
		for (TreeDataRow row : rows) {
			DataListItem item = new DataListItem();
			item.setItemId(row.getDisplayId());
			item.setId(row.getDisplayId());
			item.setText(row.getFullLineage());
			
			stresses.add(item);
		}*/
	}
	
	protected void initializeListBox(ListBox box, LookupData data) {
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
		for (TreeDataRow item : stresses.getSelection())
			selStresses.add(Integer.valueOf(item.getDisplayId()));
		
		final List<Integer> oldValueStresses = new ArrayList<Integer>();
		for (StressField stress : field.getStresses())
			oldValueStresses.add(stress.getStress());
		
		return selStresses.size() != oldValueStresses.size() || 
			!selStresses.containsAll(oldValueStresses) ||
			hasListSelectionChanged(field.getTiming(), timing) || 
			hasListSelectionChanged(field.getScope(), scope) || 
			hasListSelectionChanged(field.getSeverity(), severity);
	}
	
	protected boolean hasListSelectionChanged(Integer oldValue, ListBox newValueContainer) {
		Integer selected = null;
		if (hasListValue(newValueContainer))
			selected = Integer.valueOf(newValueContainer.getValue(newValueContainer.getSelectedIndex()));
		
		if (selected == null)
			return oldValue != null;
		else
			return selected.equals(oldValue);
	}
	
	protected boolean hasListValue(ListBox listBox) {
		return listBox.getSelectedIndex() > 0;
	}
	
	protected Integer getListValue(ListBox listBox) {
		return getListValue(listBox, null);
	}
	
	protected Integer getListValue(ListBox listBox, Integer defaultValue) {
		return hasListValue(listBox) ? 
			Integer.valueOf(listBox.getValue(listBox.getSelectedIndex())) : 
			defaultValue;
	}
	
	protected String getListText(ListBox listBox) {
		return hasListValue(listBox) ? 
			listBox.getItemText(listBox.getSelectedIndex()) : 
			"(Not specified)";
	}
	
	protected void setListValue(ListBox listBox, Integer value) {
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
		for (TreeDataRow item : stresses.getSelection())
			list.add(Integer.valueOf(item.getDisplayId()));
		
		field.setStresses(list);
	}
	
	@Override
	public void setData(Field raw) {
		ThreatsSubfield field = new ThreatsSubfield(raw);
		
		setListValue(timing, field.getTiming());
		setListValue(scope, field.getScope());
		setListValue(severity, field.getSeverity());
		
		ThreatsTreeData treeData = (ThreatsTreeData)data;
		Map<String, TreeDataRow> flatTree = 
			treeData.getTreeData("Stresses").flattenTree();
		ArrayList<TreeDataRow> selected = new ArrayList<TreeDataRow>();
		for (StressField stress : field.getStresses()) {
			String key = stress.getStress().toString();
			if (flatTree.containsKey(key))
				selected.add(flatTree.get(key));
		}
		stresses = new CodingOptionTreePanel(treeData.getTreeData("Stresses"), selected, new ArrayList<String>());
		
		impactScore.setText(getScore());
	}
	
	@Override
	public void clearData() {
		timing.setSelectedIndex(-1);
		scope.setSelectedIndex(-1);
		severity.setSelectedIndex(-1);
		impactScore.setText("");
		
		ThreatsTreeData treeData = (ThreatsTreeData)data;
		
		stresses = new CodingOptionTreePanel(treeData.getTreeData("Stresses"), new ArrayList<TreeDataRow>(), new ArrayList<String>());
	}
	
	@Override
	protected Widget createLabel() {
		displayPanel = new VerticalPanel();
		
		displayPanel.add(createBasicEditor(false));
		
		return displayPanel;
	}
	
	@Override
	protected Widget createViewOnlyLabel() {
		displayPanel = new VerticalPanel();
		
		displayPanel.add(createBasicEditor(true));
		
		return displayPanel;
	}
	
	private String getViewOnlyText(ListBox box) {
		return box.getSelectedIndex() <= 0 ? "None Selected" : 
			box.getItemText(box.getSelectedIndex());
	}
	
	protected Widget createBasicEditor(boolean viewOnly) {
		final Grid grid = new Grid(5, 2);
		
		grid.setHTML(0, 0, "Timing: ");
		if (viewOnly)
			grid.setHTML(0, 1, getViewOnlyText(timing));
		else
			grid.setWidget(0, 1, timing);
		
		grid.setHTML(1, 0, "Scope: ");
		if (viewOnly)
			grid.setHTML(1, 1, getViewOnlyText(scope));
		else
			grid.setWidget(1, 1, scope);
		
		grid.setHTML(2, 0, "Severity: ");
		if (viewOnly)
			grid.setHTML(2, 1, getViewOnlyText(severity));
		else
			grid.setWidget(2, 1, severity);
		
		grid.setHTML(3, 0, "Impact Score: ");
		grid.setWidget(3, 1, impactScore);
		
		final VerticalPanel stressSelctionContainer = new VerticalPanel();
		final VerticalPanel stressSelectionListing = new VerticalPanel();
		listSelectedStresses(stresses.getSelection(), stressSelectionListing);
		stressSelctionContainer.add(stressSelectionListing);
		if (!viewOnly)
			stressSelctionContainer.add(new Button("View/Edit Stresses", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					final Window window = WindowUtils.newWindow("Add Stresses", null, true, true);
					window.setClosable(false);
					window.setLayout(new FillLayout());
					window.setSize(600, 600);
					window.setScrollMode(Scroll.AUTO);
					window.add(stresses);
					window.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
						public void componentSelected(ButtonEvent ce) {
							window.hide();
							
							listSelectedStresses(stresses.getSelection(), stressSelectionListing);
						}
					}));
					window.show();
				}
			}));
		grid.setHTML(4, 0, "Stresses: ");
		grid.setWidget(4, 1, stressSelctionContainer);
		
		for (int i = 0; i < grid.getRowCount(); i++)
			grid.getCellFormatter().setVerticalAlignment(i, 0, HasVerticalAlignment.ALIGN_TOP);
		
		return grid;
	}
	
	private void listSelectedStresses(Collection<TreeDataRow> selection, VerticalPanel container) {
		container.clear();
		final List<TreeDataRow> list = new ArrayList<TreeDataRow>(selection);
		Collections.sort(list, new BasicClassificationSchemeViewer.TreeDataRowComparator());
		for (TreeDataRow row : list)
			container.add(new HTML(row.getFullLineage()));
	}
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		List<ClassificationInfo> list = new ArrayList<ClassificationInfo>();
		list.add(new ClassificationInfo("Timing", getListText(timing)));
		list.add(new ClassificationInfo("Scope", getListText(scope)));
		list.add(new ClassificationInfo("Severity", getListText(severity)));
		list.add(new ClassificationInfo("Impact Score", getScore()));
		list.add(new ClassificationInfo("No. of Stresses", stresses.getSelection().size()+""));
		
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
	
	@SuppressWarnings("unused")
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
