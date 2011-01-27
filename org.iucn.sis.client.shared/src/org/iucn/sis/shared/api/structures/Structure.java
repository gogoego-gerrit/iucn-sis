/**
 * Structure.java
 * 
 * Abstract representation of a form field, complete with widgets & data
 * 
 * @author carl.scott
 */

package org.iucn.sis.shared.api.structures;

import org.iucn.sis.shared.api.models.Field;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class Structure<T> implements DisplayStructure<T, Field> {

	//protected ModelData model;
	
	protected String structure;
	protected String description;
	protected String title;
	protected Object data;
	protected Panel displayPanel;
	protected Widget descriptionLabel;

	protected final String id;

	protected String name;
	protected boolean isVisible = true;
	protected String style;

	protected boolean hiddenWidgets = false;
	protected boolean canRemoveDescription = true;

	/**
	 * Empty constructor...whatever
	 */
	public Structure() {
		this("", "", null, null);
	}

	/**
	 * Creates a new structure object, without any data
	 * 
	 * @param struct
	 *            the structure
	 * @param descript
	 *            the description
	 * @param structID TODO
	 */
	public Structure(String struct, String descript, String structID) {
		this(struct, descript, structID, null);
	}

	/**
	 * Creates a new structure object, given all of the data in
	 * 
	 * @param struct
	 *            the structure (see XMLConstants)
	 * @param descript
	 *            the description
	 * @param structID TODO
	 * @param data
	 *            an object with structure-specific data
	 */
	public Structure(String struct, String descript, String structID, Object data) {
		this.structure = struct;
		this.description = descript;
		this.data = data;
		this.id = structID;
		
		this.createWidget();
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
		if (style == Orientation.HORIZONTAL) {
			displayPanel = new HorizontalPanel();
			((HorizontalPanel) displayPanel).setSpacing(2);
			((HorizontalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		} else {
			displayPanel = new VerticalPanel();
			((VerticalPanel) displayPanel).setSpacing(2);
			((VerticalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		}

		// displayPanel = new ContentPanel( style | Style.HEADER |
		// Style.COLLAPSE );
		// displayPanel.setCollapse( true );
		// displayPanel.setText(description);
	}

	public boolean canRemoveDescription() {
		return canRemoveDescription;
	}

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
		
		descriptionLabel = new HTML(description);

		SimplePanel panel = new SimplePanel();
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
		
		descriptionLabel = new HTML(description);

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
	/*public Object getConstructionData() {
		return data;
	}*/

	/**
	 * Gets the description of the Structure
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	public String getId() {
		return id;
	}

	public String getStructureType() {
		return structure;
	}

	public String getTitle() {
		return title;
	}
	
	public boolean hasId() {
		return !(id == null || "".equals(id));
	}

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
	
	@Override
	public boolean isPrimitive() {
		return false;
	}

	public void setDescription(String description) {
		this.description = description;
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

}// class Structure
