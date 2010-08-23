package org.iucn.sis.shared.api.integrity;

import com.google.gwt.user.client.Window;
import com.solertium.util.querybuilder.gwt.client.tree.QBFieldItem;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenu;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenuItem;
import com.solertium.util.querybuilder.gwt.client.utils.SmartCommand;
import com.solertium.util.querybuilder.query.SelectedField;
import com.solertium.util.querybuilder.struct.QBTable;

/**
 * SISQBFieldItem.java
 * 
 * Had to override QBFieldItem in order to have the custom display functionality
 * such that only the table name shows under the "Tables" section (formerly
 * "Fields"). Also, the user can not delete the assessment table from the list,
 * as they'll just screw themselves, and most of the QueryBuilder functionality
 * (moving columns, join patterns, etc) don't apply here.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public abstract class SISQBFieldItem extends QBFieldItem {

	public SISQBFieldItem(SelectedField field) {
		super(field);

		display = field.getTableName();
	}

	public SISQBFieldItem(QBTable table, String columnName) {
		super(table, columnName);

		display = table.getFriendlyName();
	}

	public SelectedField getField() {
		return field;
	}

	public ContextMenu getMyContextMenu() {
		if ("assessment".equalsIgnoreCase(field.getTableName()))
			return null;
		else
			return new SISFieldItemContextMenu();
	}

	class SISFieldItemContextMenu extends ContextMenu {

		public SISFieldItemContextMenu() {
			super(true);

			addItem(new ContextMenuItem("Delete", new SmartCommand() {
				public void doAction() {
					if (Window
							.confirm("Are you sure you want to remove this field?")) {
						getQuery().removeField(field);
						getMyParent().refresh();
					}
				}
			}));
		}

	}

}
