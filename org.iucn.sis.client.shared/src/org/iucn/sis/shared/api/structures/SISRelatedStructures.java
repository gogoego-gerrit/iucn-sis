package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.utils.XMLUtils;
import org.iucn.sis.shared.api.views.components.Rule;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

@SuppressWarnings("unchecked")
public class SISRelatedStructures extends Structure<Field> implements DominantStructure<Field> {

	public static final int DEFAULT_LAYOUT = 0;
	public static final int VERTICAL_PANEL = 1;
	public static final int HORIZONTAL_PANEL = 2;
	public static final int FLEXTABLE = 3;
	public static final int FLEXTABLE_NODESCRIPTION = 4;

	private DominantStructure dominantStructure;
	private ArrayList<DisplayStructure> dependantStructures;
	private ArrayList<Rule> activityRules;
	// private ArrayList myWidgets;
	// private Panel dependantPanel;

	private int dependentsDisplayType = DEFAULT_LAYOUT;
	private int displayType = DEFAULT_LAYOUT;
	
	private boolean showLabels;

	private CellPanel dependentsPanel;

	public SISRelatedStructures(String structure, String description, String structID, DominantStructure dominantStructure,
			ArrayList dependantStructures, ArrayList<Rule> activityRules) {
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
	
	public void setShowLabel(boolean showLabels) {
		this.showLabels = showLabels;
	}
	
	@Override
	public void save(Field parent, Field field) {
		if (field == null) {
			if (getId() != null) {
				field = new Field();
				field.setName(getId());
				field.setParent(parent);
				
				parent.addField(field);
			}
			else
				field = parent;
		}
		
		if (dominantStructure.isPrimitive())
			dominantStructure.save(field, field.getPrimitiveField(dominantStructure.getId()));
		else
			if (dominantStructure.hasId())
				dominantStructure.save(field, field.getField(dominantStructure.getId()));
			else
				dominantStructure.save(field, null);
		
		for (DisplayStructure cur : dependantStructures) {
			if (cur.isPrimitive())
				cur.save(field, field.getPrimitiveField(cur.getId()));
			else {
				if (cur.hasId())
					cur.save(field, field.getField(cur.getId()));
				else
					cur.save(null, field);
			}
		}
	}
	
	@Override
	public boolean hasChanged(Field field) {
		boolean hasChanged = false;
		
		if (dominantStructure.isPrimitive())
			hasChanged = dominantStructure.hasChanged(field == null ? null : field.getPrimitiveField(dominantStructure.getId()));
		else if (dominantStructure.hasId())
			hasChanged = dominantStructure.hasChanged(field == null ? null : field.getField(dominantStructure.getId()));
		else
			hasChanged = dominantStructure.hasChanged(field);
		
		Debug.println("For {0}, dominant struct {1} changed? {2}", getDescription(), dominantStructure.getId(), hasChanged);
		
		if (!hasChanged) {
			for (DisplayStructure cur : dependantStructures) {
				if (cur.isPrimitive())
					hasChanged = cur.hasChanged(field == null ? null : field.getPrimitiveField(cur.getId()));
				else if (cur.hasId())
					hasChanged = cur.hasChanged(field == null ? null : field.getField(cur.getId()));
				else
					hasChanged = cur.hasChanged(field);
		
				Debug.println("For {0}, dep struct {1} changed? {2}", getDescription(), cur.getId(), hasChanged);
				
				if (hasChanged)
					break;
			}
		}
		
		return hasChanged;
	}
	
	@Override
	public void addListenerToActiveStructure(ChangeHandler changeListener, ClickHandler clickListener,
			KeyUpHandler keyboardListener) {
		dominantStructure.addListenerToActiveStructure(changeListener, clickListener, keyboardListener);
	}

	@Override
	public void clearData() {
		dominantStructure.clearData();

		for (int i = 0; i < dependantStructures.size(); i++)
			(dependantStructures.get(i)).clearData();
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
				dependentsPanel.add((dependantStructures.get(i)).generateViewOnly());
			else
				dependentsPanel.add((dependantStructures.get(i)).generate());
		}

		displayPanel.add(dependentsPanel);

		try {
			updateDependantPanel();
		} catch (Throwable e) {
			//Debug.println(e);
		}

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
	public ArrayList<String> extractDescriptions() {
		ArrayList<String> ret = new ArrayList<String>();

		ret.addAll(dominantStructure.extractDescriptions());
		for (Iterator<DisplayStructure> iter = dependantStructures.iterator(); iter.hasNext();)
			ret.addAll((iter.next()).extractDescriptions());

		return ret;
	}
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		ArrayList<ClassificationInfo> list = new ArrayList<ClassificationInfo>();
		
		list.addAll(dominantStructure.getClassificationInfo());
		for (DisplayStructure structure : dependantStructures)
			list.addAll(structure.getClassificationInfo());
		
		return list;
	}

	@Override
	public String getData() {
		return null;
	}

	public ArrayList<DisplayStructure> getDependantStructures() {
		return dependantStructures;
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

	private void init(String description) {
		descriptionLabel = new HTML(description);
		addListenerToActiveStructure(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				updateDependantPanel();
			}
		}, new ClickHandler() {
			public void onClick(ClickEvent event) {
				updateDependantPanel();
			}
		}, new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
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

	public ArrayList<Rule> isDominantActive() {
		ArrayList<Rule> activeRules = new ArrayList<Rule>();
		for (Rule rule : activityRules) {
			if (dominantStructure.isActive(rule)) {
				// return (Rule)activityRules.get(i);
				activeRules.add(rule);
			} else {
				for (int j = 0; j < dependantStructures.size(); j++) {
					if (rule.isIndexAffected(j)) {
						processRule(true, rule.getOnFalse(), (Structure) dependantStructures
								.get(j));
					}
				}
			}
		}
		return activeRules; // null;
	}

	private void processRule(boolean isIndexAffected, String rule, Structure structure) {
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
	public void setData(Field data) {
		Field field = data;
		if (getId() != null && field != null)
			field = field.getField(getId());
		
		if (dominantStructure.isPrimitive())
			dominantStructure.setData(field == null ? null : field.getPrimitiveField(dominantStructure.getId()));
		else
			dominantStructure.setData(field == null ? null : field.getField(dominantStructure.getId()));
		
		for (DisplayStructure structure : dependantStructures) {
			if (structure.isPrimitive())
				structure.setData(field == null ? null : field.getPrimitiveField(structure.getId()));
			else
				if (structure.hasId())
					structure.setData(field == null ? null : field.getField(structure.getId()));
				else
					structure.setData(field);
		}
		
		//updateDependantPanel();
		
		//Map<String, PrimitiveField> data = field.getKeyToPrimitiveFields();
		/*for (int i = 0; i < dependantStructures.size() + 1; i++) {
			if (i == 0){
				if (fie)
				dominantStructure.setData(field);
				//model = new BaseModel(dominantStructure.extractModelData().getProperties());
			}
			else{
				((Structure) dependantStructures.get(i - 1)).setData(field);
				//for(String key: ((Structure) dependantStructures.get(i - 1)).extractModelData().getPropertyNames())
				//	model.set(key, ((Structure) dependantStructures.get(i - 1)).extractModelData().get(key));
			}
		}*/
	}

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

	private void updateDependantPanel() {
		ArrayList<Rule> activeRules = isDominantActive();
		for (Rule rule : activeRules) {
			for (int i = 0; i < dependantStructures.size(); i++) {
				boolean isAffected = rule.isIndexAffected(i);
				processRule(isAffected, (isAffected ? rule.getOnTrue() : rule.getOnFalse()),
						(Structure) dependantStructures.get(i));
			}
		}
	}
	
	public boolean hideDescriptionLabel(boolean forever) {
		if (showLabels)
			return false;
		
		if (!(dominantStructure instanceof SISDominantStructureCollection)) {
			Structure dom = (Structure)dominantStructure;
			return dom.hideDescriptionLabel(forever);
		} else
			return false;
	}

}
