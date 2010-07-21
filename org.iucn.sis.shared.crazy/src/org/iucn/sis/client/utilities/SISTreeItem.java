package org.iucn.sis.client.utilities;

import java.util.ArrayList;

import org.iucn.sis.client.displays.SISRow;
import org.iucn.sis.shared.structures.Structure;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;

/**
 * SISTreeItem.java
 * 
 * Generates a tree item for a given SISRow quickly. A single row takes
 * generally under 100 ms for the largest rows, with the average case being
 * 15-25ms. Takes advantage of GWT-inherent widgets to increase speed (and lazy
 * generation), and SIS knowledge to improve look and feel.
 * 
 * @author carl.scott
 */
public class SISTreeItem extends VerticalPanel {

	private ArrayList children; // Group of SISTreeItems
	private VerticalPanel childPanel; // Displays children

	private SISRow sisRow; // The SISRow this represents

	private Image button; // The expand/collapse buttons
	private boolean treeIsExpanded = false; // Is the tree currently expanded

	private SimplePanel leftMarginPanel; // Indentations
	private int leftMargin = 0; // Size of the indentation

	private boolean isShown = false; // Has the tree item been rendered?

	/**
	 * Constructor. Just instantiates and takes in data, and nothing else! This
	 * is a lazy function to avoid wasting time when unnecessary
	 * 
	 * @param sisRow
	 *            the row this item represents
	 * @param leftMargin
	 */
	public SISTreeItem(SISRow sisRow, int leftMargin) {
		super();
		this.leftMargin = leftMargin;
		this.sisRow = sisRow;

		children = new ArrayList();
		childPanel = new VerticalPanel();

		addStyleName("SIS_gwtTreeRow");
	}

	/******* GETTERS, SETTERS, AND THE LIKE **********/

	/**
	 * Adds a child to this tree Item
	 * 
	 * @param child
	 *            the SISTreeItem to add
	 */
	public void addChild(SISTreeItem child) {
		children.add(child);
	}

	/**
	 * Builds a expand/collapse button for this item, or a blank one if
	 * necessary. Only builds it once.
	 * 
	 * @return the button as an image
	 */
	private Image buildButton() {
		if (button == null) {
			if (hasChildren()) {
				button = new Image("images/tree_closed.gif");
				button.addClickListener(new ClickListener() {
					public void onClick(Widget sender) {
						if (treeIsExpanded)
							collapseTree();
						else
							expandTree();
					}
				});
			} else {
				button = new Image("images/tree_white.gif");
			}
		}
		return button;
	}

	/********** SHOW THE TREE *************/

	/**
	 * Builds the indentations for the tree item. Only does this once.
	 * 
	 * @return the panel to indent the tree item
	 */
	private Widget buildLeftMargin() {
		if (leftMarginPanel == null) {
			leftMarginPanel = new SimplePanel();
			leftMarginPanel.setWidth(leftMargin + "px");
		}
		return leftMarginPanel;
	}

	/**
	 * Collapses the tree. Obviously, trees are collapsed by default (to be
	 * consistent with our lazy setup)
	 */
	public void collapseTree() {
		treeIsExpanded = false;
		button.setUrl("images/tree_closed.gif");
		setChildrenVisible(false);
	}

	/********** BUILD THE TREE *************/

	/**
	 * Expands the tree
	 */
	public void expandTree() {
		treeIsExpanded = true;
		button.setUrl("images/tree_open.gif");
		setChildrenVisible(true);
	}

	/**
	 * Determines if this tree item has children
	 * 
	 * @return true if so, false otherwise
	 */
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	/**
	 * When the tree is expanded or collapse, children's visibility needs to be
	 * updated!
	 * 
	 * @param isVisible
	 *            true if expanded, false otherwise
	 */
	private void setChildrenVisible(boolean isVisible) {
		if (isVisible) { // Expand the tree
			if (childPanel.getWidgetCount() == 0) {
				for (int i = 0; i < children.size(); i++) {
					SISTreeItem current = (SISTreeItem) children.get(i);

					// Now that this row is being requested, build it
					current.show();

					// Add it to the parent's childPanel
					childPanel.add(current);
				}
			}
			childPanel.setVisible(true);
		} else { // Collapse the tree
			childPanel.setVisible(false);
		}
	}

	/**
	 * Builds the tree item. Only does this one.
	 */
	public void show() {
		if (!isShown) {
			FlexTable row = new FlexTable();
			row.setWidget(0, 0, buildLeftMargin());
			row.setWidget(0, 1, buildButton());

			int column = 2;
			// If the row is codeable or I can remove the description, add the
			// description
			// and set it to the requested width
			SysDebugger.getInstance().println(
					"The first structure is a " + sisRow.getMyStructures().getStructure(0).getStructureType()
							+ " where canRemoveDesction = "
							+ sisRow.getMyStructures().getStructure(0).canRemoveDescription());
			if (!sisRow.isCodeable() || (sisRow.getMyStructures().getStructure(0)).canRemoveDescription()) {
				row.setWidget(0, column, new HTML(sisRow.getLabel()));
				// TODO: make width a parameter
				if (sisRow.isCodeable())
					row.getCellFormatter().setWidth(0, column, "200px");
				column++;
			}

			// If the row is codeable, add it's widgets to the table as to make
			// it nice and neat. Alternately (and more quickly, just add the
			// whole thing
			if (sisRow.isCodeable()) {
				for (int j = 0; j < sisRow.getMyStructures().size(); j++) {
					// Prettier:
					Structure curStructure = sisRow.getMyStructures().getStructure(j);
					row.setWidget(0, column++, curStructure.generate());
					curStructure.hideDescriptionLabel(true);

					// Quicker:
					// row.setWidget(0, column++,
					// sisRow.getMyWidgetsAsFlexTable());
				}
			}

			if (sisRow.isExpanded()) {
				expandTree();
			}

			// Add the row and the child panel to this tree item
			add(row);
			add(childPanel);

			// Set this so this function never does work again!
			isShown = true;
		}
	}
}