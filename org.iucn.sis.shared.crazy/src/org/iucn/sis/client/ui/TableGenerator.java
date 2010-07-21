package org.iucn.sis.client.ui;

import java.util.Date;
import java.util.Iterator;

import org.iucn.sis.client.displays.SISRow;
import org.iucn.sis.client.utilities.SISTreeItem;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;

/**
 * Generates tables.
 * 
 * @author adam.schwartz
 * 
 */
public class TableGenerator {
	/******* GWT-ONLY VERSION ********/

	public static final int LEFT_MARGIN_WIDTH = 20;

	/**
	 * Private helper function to recursively add children to the tree items
	 * 
	 * @param curRoot
	 *            the current SISRow to work on
	 * @param parentItem
	 *            the parent tree Item of this row
	 * @param leftMargin
	 *            how far over to the left this tree item is indented
	 */
	private static void addToGWTTreeRecursive(SISRow curRoot, SISTreeItem parentItem, int leftMargin) {
		// For each child...
		for (int i = 0; i < curRoot.getChildren().size(); i++) {
			SISRow curChild = (SISRow) curRoot.getChildren().get(i);

			// ...create a new tree Item for it, and add it to the parent...
			SISTreeItem item = new SISTreeItem(curChild, leftMargin);
			parentItem.addChild(item);

			// ...if the child has children, recurse to get those added
			if (curChild.hasChild())
				addToGWTTreeRecursive(curChild, item, leftMargin + LEFT_MARGIN_WIDTH);
		}
	}

	private static void addToTableRecursive(SISRow curRoot, FlexTable table) {
		/*
		 * int i = 0; //Attach myself if I'm not a root! if( !curRoot.isRoot() )
		 * { table.insertRow( table.getRowCount() ); for( Iterator widgetIter =
		 * curRoot.getMyWidgets().iterator(); widgetIter.hasNext(); ) { Widget
		 * curWidget = (Widget)widgetIter.next();
		 * 
		 * table.addCell(i); table.setWidget(table.getRowCount()-1, i, curWidget
		 * ); i++; } }
		 * 
		 * //Check for children for( i = 0; i < curRoot.getChildren().size();
		 * i++ ) { SISRow curChild = ((SISRow)curRoot.getChildren().get( i ));
		 * 
		 * addToTableRecursive( curChild, table ); }
		 */
	}

	// Check for children and add them!
	private static void addToTreeRecursive(SISRow curRoot, TreeItem parentItem) {
		// For each of the children
		for (int i = 0; i < curRoot.getChildren().size(); i++) {
			SISRow curChild = (SISRow) curRoot.getChildren().get(i);

			if (curChild.hasChild()) {
				// addToTreeRecursive(curChild,
				// parentItem.addItem(curChild.getMyWidgets().toArray()));
				addToTreeRecursive(curChild, parentItem.addItem(curChild.getMyWidgetsAsFlexTable()));
			} else {
				// parentItem.addItem(curChild.getMyWidgets().toArray());
				parentItem.addItem(curChild.getMyWidgetsAsFlexTable());
			}

			parentItem.setState(curChild.isExpanded());
		}
	}

	public static HTMLTable generateEmptyTable() {
		return new FlexTable();
	}

	/**
	 * Generates a tree out of SISTreeItem objects. These trees can generate
	 * fully generally under half a second for substantially sized trees
	 * (Threats, Habitats).
	 * 
	 * @param tree
	 *            the SISRow to show
	 * @return the UI representation
	 */
	public static Widget generateSISTreeTableRecursive(SISRow tree) {
		Date startTime = new Date();
		Date overheadtime = new Date();

		SISTreeItem current = new SISTreeItem(tree, 0);
		addToGWTTreeRecursive(tree, current, LEFT_MARGIN_WIDTH);

		// Show the top-level tree item
		current.show();

		Date endTime = new Date();

		SysDebugger.getInstance().println(
				"Tree-generation time: "
						+ ((endTime.getTime() - startTime.getTime()) - (overheadtime.getTime() - startTime.getTime()))
						+ "ms.");

		return current;
	}

	/**
	 * Boring old table, no indentations.
	 * 
	 * @param rows
	 *            ArrayList of SisTableRows
	 * @returns FlexTable, in particular
	 */
	public static HTMLTable generateTable(SISRow roots) {
		FlexTable table = new FlexTable();
		table.setCellPadding(2);
		// Date startTime = new Date();
		// Date overheadTime = new Date();

		for (Iterator rootIter = roots.getChildren().iterator(); rootIter.hasNext();) {
			int i = 0;
			SISRow curRoot = (SISRow) rootIter.next();

			table.insertRow(table.getRowCount());

			for (Iterator widgetIter = curRoot.getMyWidgets().iterator(); widgetIter.hasNext();) {
				Widget curWidget = (Widget) widgetIter.next();

				table.addCell(i);
				table.setWidget(table.getRowCount() - 1, i, curWidget);
				i++;
			}

			addToTableRecursive(curRoot, table);
		}

		for (int i = 0; i < table.getRowCount(); i++)
			table.getRowFormatter().setStyleName(i, "tdx");

		return table;
	}

	/**
	 * Tree table, but fields are not in order because of TreeTable
	 * implementation.
	 * 
	 * @param rows
	 *            ArrayList of SisTableRows
	 * @returns a TreeTable
	 */
	public static TreeTable generateTreeTableRecursive(SISRow tree) {
		Date startTime = new Date();
		Date overheadTime = new Date();

		TreeTable treeTable = new TreeTable();

		// Add yourself to the tree, then add its children
		// TreeItem current = treeTable.addItem(tree.getMyWidgets().toArray());
		TreeItem current = treeTable.addItem(tree.getMyWidgetsAsFlexTable());
		addToTreeRecursive(tree, current);
		current.setState(tree.isExpanded());

		// Format each row
		for (int i = 0; i < treeTable.getRowCount(); i++)
			treeTable.getRowFormatter().setStyleName(i, "tdx");

		Date endTime = new Date();

		SysDebugger.getInstance().println(
				"Tree-generation time: "
						+ ((endTime.getTime() - startTime.getTime()) - (overheadTime.getTime() - startTime.getTime()))
						+ "ms.");

		treeTable.updateVisibility();
		return treeTable;
	}

}
