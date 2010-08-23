package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.utils.XMLUtils;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SISRelatedStructures extends DominantStructure {

	public static final int DEFAULT_LAYOUT = 0;
	public static final int VERTICAL_PANEL = 1;
	public static final int HORIZONTAL_PANEL = 2;
	public static final int FLEXTABLE = 3;
	public static final int FLEXTABLE_NODESCRIPTION = 4;

	private DominantStructure dominantStructure;
	private ArrayList<Structure> dependantStructures;
	private ArrayList activityRules;
	// private ArrayList myWidgets;
	// private Panel dependantPanel;

	private int dependentsDisplayType = DEFAULT_LAYOUT;
	private int displayType = DEFAULT_LAYOUT;

	private CellPanel dependentsPanel;

	public SISRelatedStructures(String structure, String description, String structID, DominantStructure dominantStructure,
			ArrayList dependantStructures, ArrayList activityRules) {
		super(structure, description, structID);

		this.dominantStructure = dominantStructure;
		this.dependantStructures = dependantStructures;
		this.activityRules = activityRules;

		try {
			init(description);
		} catch (Error e) {
			// You'd better be trying to create a Structure on the
			// server-side...
		}
	}

	@Override
	protected PrimitiveField getNewPrimitiveField() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void save(Field field) {
		dominantStructure.save(field);
		for( Structure cur : dependantStructures )
			cur.save(field);
	}
	
	@Override
	public boolean hasChanged() {
		if( dominantStructure.hasChanged() ) {
			return true;
		} else {
			for( Structure cur : dependantStructures )
				if( cur.hasChanged() )
					return true;
		}
		
		return false;
	}
	
	@Override
	public void addListenerToActiveStructure(ChangeListener changeListener, ClickHandler clickListener,
			KeyboardListener keyboardListener) {
		dominantStructure.addListenerToActiveStructure(changeListener, clickListener, keyboardListener);

		// dominantStructure.addListenerToActiveStructure(new ChangeListener() {
		// public void onChange(Widget sender) { updateDependantPanel(); } },new
		// ClickHandler() { public void onClick(ClickEvent event) {
		// updateDependantPanel(); } });

		/*
		 * if(dominantStructure.structure.equalsIgnoreCase(XMLConstants.
		 * BOOLEAN_STRUCTURE)) { ((CheckBox)dominantStructure).addListener(null,
		 * new ClickHandler() { public void onClick(ClickEvent event) {
		 * updateDependantPanel(); } }); } else if
		 * (dominantStructure.structure.equalsIgnoreCase
		 * (XMLConstants.BOOLEAN_UNKNOWN_STRUCTURE) ||
		 * dominantStructure.structure
		 * .equalsIgnoreCase(XMLConstants.SINGLE_SELECT_STRUCTURE) ||
		 * dominantStructure
		 * .structure.equalsIgnoreCase(XMLConstants.MULTIPLE_SELECT_STRUCTURE)
		 * ||dominantStructure.structure.equalsIgnoreCase(XMLConstants.
		 * QUALIFIER_STRUCTURE)) {
		 * ((ListBox)dominantStructure.getActiveStructure
		 * ()).addChangeListener(new ChangeListener() { public void
		 * onChange(Widget sender) { updateDependantPanel(); } }); } else if
		 * (dominantStructure
		 * .structure.equalsIgnoreCase(XMLConstants.BUTTON_STRUCTURE)) {
		 * ((Button)dominantStructure.getActiveStructure()).addClickHandler(new
		 * ClickHandler() { public void onClick(ClickEvent event) {
		 * updateDependantPanel(); } }); } else if
		 * (dominantStructure.structure.equalsIgnoreCase
		 * (XMLConstants.THREAT_STRUCTURE)) {
		 * ((Button)dominantStructure.getActiveStructure()).addClickHandler(new
		 * ClickHandler() { public void onClick(ClickEvent event) {
		 * updateDependantPanel(); } }); }
		 */
	}

	@Override
	public void clearData() {
		dominantStructure.clearData();

		for (int i = 0; i < dependantStructures.size(); i++)
			((Structure) dependantStructures.get(i)).clearData();
	}

	@Override
	public Widget createLabel() {
		return createLabel(false);
	}

	private Widget createLabel(boolean viewOnly) {
		if (displayType == HORIZONTAL_PANEL)
			buildContentPanel(Orientation.HORIZONTAL);
		else
			buildContentPanel(Orientation.VERTICAL);

		// displayPanel.add(descriptionLabel);

		if (dependentsDisplayType == HORIZONTAL_PANEL) {
			dependentsPanel = new HorizontalPanel();
			((HorizontalPanel) dependentsPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		} else {
			dependentsPanel = new VerticalPanel();
			((VerticalPanel) dependentsPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		}
		dependentsPanel.setSpacing(2);

		if (viewOnly)
			displayPanel.add(dominantStructure.generateViewOnly());
		else
			displayPanel.add(dominantStructure.generate());

		for (int i = 0; i < dependantStructures.size(); i++) {
			if (viewOnly)
				dependentsPanel.add(((Structure) dependantStructures.get(i)).generateViewOnly());
			else
				dependentsPanel.add(((Structure) dependantStructures.get(i)).generate());
		}

		displayPanel.add(dependentsPanel);

		updateDependantPanel();

		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		return createLabel(true);
	}

	@Override
	public void createWidget() {
		// setTolistenForActive();
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();

		ret.addAll(dominantStructure.extractDescriptions());
		for (Iterator iter = dependantStructures.iterator(); iter.hasNext();)
			ret.addAll(((Structure) iter.next()).extractDescriptions());

		return ret;
	}

	@Override
	public String getData() {
		return null;
	}

	public ArrayList getDependantStructures() {
		return dependantStructures;
	}

	public String getDependentXML() {
		String ret = "";

		for (int i = 0; i < activityRules.size(); i++) {
			if (dominantStructure.isActive((Rule) activityRules.get(i))) {
				Rule curRule = (Rule) activityRules.get(i);
				if (dominantStructure.isActive(curRule)) {
					String rule = (curRule.isIndexAffected(i) ? curRule.getOnTrue() : curRule.getOnFalse());

					for (int j = 0; j < dependantStructures.size(); j++) {
						Structure curDep = (Structure) dependantStructures.get(j);

						if (rule.equalsIgnoreCase(Rule.HIDE) || rule.equalsIgnoreCase(Rule.DISABLE))
							curDep.clearData();
					}
				}
			} else {
				for (int j = 0; j < dependantStructures.size(); j++) {
					if (((Rule) activityRules.get(i)).isIndexAffected(j)) {
						String rule = ((Rule) activityRules.get(i)).getOnFalse();

						if (rule.equalsIgnoreCase(Rule.HIDE) || rule.equalsIgnoreCase(Rule.DISABLE))
							((Structure) dependantStructures.get(j)).clearData();
					}
				}
			}
		}

		for (int i = 0; i < dependantStructures.size(); i++)
			ret += ((Structure) dependantStructures.get(i)).toXML();

		return ret;
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
		offset = dominantStructure.getDisplayableData(rawData, prettyData, offset);

		for (int i = 0; i < dependantStructures.size(); i++)
			offset = ((Structure) dependantStructures.get(i)).getDisplayableData(rawData, prettyData, offset);

		return offset;
	}

	public DominantStructure getDominantStructure() {
		return dominantStructure;
	}

	/*
	 * private void updateDependantPanel() { Rule rule; //for (int k = 0; k <
	 * activityRules.size(); k++) { if ((rule = this.isDominantActive()) !=
	 * null) { ///A rule returned true
	 * SysDebugger.getInstance().println("Got a rule!"); for (int i = 0; i <
	 * dependantStructures.size(); i++) { processRule(true,
	 * (rule.isIndexAffected(i)?rule.getOnTrue():rule.getOnFalse()),
	 * (Structure)dependantStructures.get(i)); } } //} }
	 */

	private void init(String description) {
		descriptionLabel = new HTML(description);
		addListenerToActiveStructure(new ChangeListener() {
			public void onChange(Widget sender) {
				updateDependantPanel();
			}
		}, new ClickHandler() {
			public void onClick(ClickEvent event) {
				updateDependantPanel();
			}
		}, new KeyboardListenerAdapter() {
			@Override
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateDependantPanel();
			}
		});
	}

	@Override
	public boolean isActive(Rule rule) {
		for (int i = 0; i < activityRules.size(); i++)
			if (dominantStructure.isActive(rule))
				return true;
		return false;
	}

	public ArrayList isDominantActive() {
		ArrayList activeRules = new ArrayList();
		for (int i = 0; i < activityRules.size(); i++) {
			if (dominantStructure.isActive((Rule) activityRules.get(i))) {
				// return (Rule)activityRules.get(i);
				activeRules.add(activityRules.get(i));
			} else {
				for (int j = 0; j < dependantStructures.size(); j++) {
					if (((Rule) activityRules.get(i)).isIndexAffected(j)) {
						processRule(true, ((Rule) activityRules.get(i)).getOnFalse(), (Structure) dependantStructures
								.get(j));
					}
				}
			}
		}
		return activeRules; // null;
	}

	private void processRule(boolean isIndexAffected, String rule, Structure structure) {
		// SysDebugger.getInstance().println("Processing rule " + rule);
		if (rule.equalsIgnoreCase(Rule.SHOW) || rule.equalsIgnoreCase(Rule.HIDE)) {
			for (int i = 0; i < dependantStructures.size(); i++)
				if (isIndexAffected)
					structure.displayPanel.setVisible(rule.equalsIgnoreCase(Rule.SHOW));
		} else if (rule.equalsIgnoreCase(Rule.ENABLE)) {
			for (int i = 0; i < dependantStructures.size(); i++)
				if (isIndexAffected)
					structure.enable();
		} else if (rule.equalsIgnoreCase(Rule.DISABLE)) {
			for (int i = 0; i < dependantStructures.size(); i++)
				if (isIndexAffected)
					structure.disable();
		}
	}

	@Override
	public void setData(Map<String, PrimitiveField> data) {
		for (int i = 0; i < dependantStructures.size() + 1; i++) {
			if (i == 0){
				dominantStructure.setData(data);
				model = new BaseModel(dominantStructure.extractModelData().getProperties());
			}
			else{
				((Structure) dependantStructures.get(i - 1)).setData(data);
				for(String key: ((Structure) dependantStructures.get(i - 1)).extractModelData().getPropertyNames())
					model.set(key, ((Structure) dependantStructures.get(i - 1)).extractModelData().get(key));
			}
		}
	}

	/*
	 * public Rule isDominantActive() { for (int i = 0; i <
	 * activityRules.size(); i++) { if
	 * (dominantStructure.isActive((Rule)activityRules.get(i))) { return
	 * (Rule)activityRules.get(i); } else { for (int j = 0; j <
	 * dependantStructures.size(); j++) { if
	 * (((Rule)activityRules.get(i)).isIndexAffected(j)) { processRule(true,
	 * ((Rule)activityRules.get(i)).getOnFalse(),
	 * (Structure)dependantStructures.get(j)); } } } } return null; }
	 */

	public void setDependentsLayout(int dependentsLayout) {
		dependentsDisplayType = dependentsLayout;
	}

	public void setDisplayType(int displayType) {
		this.displayType = displayType;
		for (int i = 0; i < dependantStructures.size(); i++) {
			if (((Structure) dependantStructures.get(i)).getStructureType()
					.equalsIgnoreCase(XMLUtils.RELATED_STRUCTURE)) {
				((SISRelatedStructures) dependantStructures.get(i)).setDisplayType(displayType);
			}
		}
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		dominantStructure.setEnabled(isEnabled);
		for (int i = 0; i < dependantStructures.size(); i++) {
			((Structure) dependantStructures.get(i)).setEnabled(isEnabled);
		}
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}

	private void updateDependantPanel() {
		ArrayList activeRules = isDominantActive();
		// SysDebugger.getInstance().println("There are " + activeRules.size() +
		// " rules active");
		for (int j = 0; j < activeRules.size(); j++) {
			Rule rule = (Rule) activeRules.get(j);
			for (int i = 0; i < dependantStructures.size(); i++) {
				processRule(true, (rule.isIndexAffected(i) ? rule.getOnTrue() : rule.getOnFalse()),
						(Structure) dependantStructures.get(i));
			}
		}
	}
	


}
