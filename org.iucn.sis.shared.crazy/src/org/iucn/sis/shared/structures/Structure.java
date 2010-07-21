/**
 * Structure.java
 * 
 * Abstract representation of a form field, complete with widgets & data
 * 
 * @author carl.scott
 */

package org.iucn.sis.shared.structures;

import java.util.ArrayList;
import java.util.HashMap;

import org.iucn.sis.client.utilities.ChangeTracker;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class Structure {

	protected HashMap values;
	protected ModelData model;
	
	protected String structure;
	protected String description;
	protected String title;
	protected Object data;
	protected ChangeTracker tracker;
	protected Panel displayPanel;
	protected Widget descriptionLabel;

	protected String id;

	protected String name;
	protected boolean isVisible = true;
	protected String style;

	protected boolean hiddenWidgets = false;
	protected boolean canRemoveDescription = true;

	/**
	 * Empty constructor...whatever
	 */
	public Structure() {
		this("", "", null);
	}

	/**
	 * Creates a new structure object, without any data
	 * 
	 * @param struct
	 *            the structure
	 * @param descript
	 *            the description
	 */
	public Structure(String struct, String descript) {
		this(struct, descript, null);
	}

	/**
	 * Creates a new structure object, given all of the data in
	 * 
	 * @param struct
	 *            the structure (see XMLConstants)
	 * @param descript
	 *            the description
	 * @param data
	 *            an object with structure-specific data
	 */
	public Structure(String struct, String descript, Object data) {
		this.model = new BaseModel();
		
		this.structure = struct;
		this.description = descript;
		this.data = data;
		tracker = new ChangeTracker();

		try {
			this.createWidget();
		} catch (Error e) {
//			System.out.println("You'd better be trying to create a Structure on the " + "server-side...");
		}

		// values = getValues();
	}

	public void addTitleToLabel() {
		if (title != null) {
			if (descriptionLabel == null) {
				descriptionLabel = new HTML(description);
			}
			// descriptionLabel.setTitle(title);
		}
	}

	protected void buildContentPanel(Orientation style) {
		try {
			if (style == Orientation.HORIZONTAL) {
				displayPanel = new HorizontalPanel();
				((HorizontalPanel) displayPanel).setSpacing(2);
				((HorizontalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
			} else {
				displayPanel = new VerticalPanel();
				((VerticalPanel) displayPanel).setSpacing(2);
				((VerticalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
			}
		} catch (Error e) {
			// Still better be trying to create a Structure on the
			// server-side...
		}

		// displayPanel = new ContentPanel( style | Style.HEADER |
		// Style.COLLAPSE );
		// displayPanel.setCollapse( true );
		// displayPanel.setText(description);
	}

	public boolean canRemoveDescription() {
		return canRemoveDescription;
	}

	public abstract void clearData();

	protected void clearDisplayPanel() {
		displayPanel.clear();

		// displayPanel.removeAll();
	}

	/**
	 * Returns a Panel representation of this Structure
	 * 
	 * @return the Panel, or any Widget that accurately represents this
	 *         Structure
	 */
	protected abstract Widget createLabel();

	/**
	 * Returns a Panel representation of this Structure with report-like data.
	 * User can not edit.
	 * 
	 * @return the panel, or any Widget that accurately represents this
	 *         Structure
	 */
	protected abstract Widget createViewOnlyLabel();

	/**
	 * Called when a Structure is created and implemented by each extending
	 * class. This creates all the widgets nec'y for a Structure
	 */
	public abstract void createWidget();

	/**
	 * Disables all the Widgets in a particular structure
	 */
	public void disable() {
		setEnabled(false);
	}

	/**
	 * Enables all the Widgets in a particular structure
	 */
	public void enable() {
		setEnabled(true);
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	public abstract ArrayList extractDescriptions();
	
	
	public ModelData extractModelData(){
		return model;
	}
	
	/**
	 * Returns this field in the form of a Widget (Panel to be plopped on a UI).
	 * 
	 * @return the Panel (Widget)
	 */
	public Widget generate() {
		// return WidgetGenerator.createPanel(this);
		addTitleToLabel();

		if (!isVisible) {
			hideWidgets();
			return displayPanel;
		}
		if (hiddenWidgets)
			return displayPanel;

		SimplePanel panel = new SimplePanel();
		descriptionLabel = new HTML(description);
		panel.setWidget(createLabel());
		if (style != null)
			panel.addStyleName(style);

		if (descriptionLabel != null)
			descriptionLabel.removeStyleName("bold");

		return panel;
		// return this.createLabel();
	}

	public Widget generateViewOnly() {
		addTitleToLabel();

		if (!isVisible) {
			hideWidgets();
			return displayPanel;
		}
		if (hiddenWidgets)
			return displayPanel;

		SimplePanel panel = new SimplePanel();
		panel.setWidget(createViewOnlyLabel());
		if (style != null)
			panel.addStyleName(style);

		if (descriptionLabel != null)
			descriptionLabel.addStyleName("bold");

		return panel;
	}

	/**
	 * Returns the Object data that is sometimes passed into a Structure for
	 * construction purposes, e.g. an SISMultiSelect data == ArrayList with the
	 * list options. Might be null if structure did not require any construction
	 * data.
	 * 
	 * @return Object data - construction information, sometimes
	 */
	public Object getConstructionData() {
		return data;
	}

	public abstract Object getData();

	/**
	 * Gets the description of the Structure
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Pass in the raw data from an AssessmentData object, and this will return
	 * it in happy, displayable String form
	 * 
	 * @return ArrayList of Strings, having converted the rawData to nicely
	 *         displayable String data. Happy days!
	 */
	public abstract int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset);

	public String getId() {
		return id;
	}

	/**
	 * Gets a description of the type of structure this is
	 * 
	 * @return the structure
	 */
	public String getStructureType() {
		return structure;
	}

	public String getTitle() {
		return title;
	}

	/**
	 * Hides a structure
	 */
	public void hide() {
		displayPanel.setVisible(false);
	}

	public boolean hideDescriptionLabel(boolean forever) {
		if (canRemoveDescription) {
			if (descriptionLabel != null) {
				descriptionLabel.setVisible(false);
				if (forever)
					descriptionLabel.removeFromParent();
			}
			return true;
		} else
			return false;
	}

	/**
	 * Creates a structure with just its description, no widgets
	 */
	public void hideWidgets() {
		hiddenWidgets = true;
		displayPanel = new HorizontalPanel();
		// displayPanel = new ContentPanel( Style.HEADER | Style.COLLAPSE );
		// displayPanel.setCollapse( true );
		// displayPanel.setText(description);
		displayPanel.add(new HTML(description));
	}

	public void setCanRemoveDescription(boolean canRemoveDescription) {
		this.canRemoveDescription = canRemoveDescription;
	}

	public int setData(ArrayList dataList, int dataOffset){
		ArrayList<String> keys = (ArrayList<String>)extractDescriptions();
		
//		if( dataList.size()-dataOffset-keys.size() >= 0 )
		try {
			for(String key: keys)
				model.set(key, dataList.get(dataOffset+keys.indexOf(key)));
		} catch (Exception ignored) {}
				
		return dataOffset;
	}

	/**
	 * Sets the description
	 * 
	 * @param description
	 *            the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Private Helper function to enable or disable a structure
	 * 
	 * @param isEnabled
	 */
	protected abstract void setEnabled(boolean isEnabled);

	public void setId(String id) {
		this.id = id;
	}

	public void setIsVisible(String isVisible) {
		if (isVisible != null)
			this.isVisible = isVisible.equalsIgnoreCase("true");
		else
			this.isVisible = true;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Unhides a structure
	 */
	public void show() {
		displayPanel.setVisible(true);
	}

	/**
	 * Allows for a structure to show with widgets afer a hideWidgets call
	 * Should not have to use
	 */
	public void showWidgets() {
		hiddenWidgets = false;
		generate();
	}

	public abstract String toXML();

}// class Structure
