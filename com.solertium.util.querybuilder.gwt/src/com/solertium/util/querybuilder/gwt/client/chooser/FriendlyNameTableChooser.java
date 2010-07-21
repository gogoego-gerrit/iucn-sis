/**
 *
 */
package com.solertium.util.querybuilder.gwt.client.chooser;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.solertium.util.gwt.ui.HTMLListBox;
import com.solertium.util.gwt.ui.HTMLMultipleListBox;
import com.solertium.util.portable.PortableAlphanumericComparator;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.struct.DBStructure;
import com.solertium.util.querybuilder.struct.QBColumn;
import com.solertium.util.querybuilder.struct.QBTable;

/**
 * FriendlyNameTableChooser.java
 *
 * @author carl.scott
 *
 */
public class FriendlyNameTableChooser extends TableChooser {

	public FriendlyNameTableChooser(final GWTQBQuery query, final boolean isMultipleSelect) {
		super(query, isMultipleSelect);
	}

	protected void populateTableListing() {
		Collections.sort(tables, new CaseInsensitiveAlphanumericComparator());
		for (int i = 0; i < tables.size(); i++) {
			QBTable cur = db.getTable(tables.get(i));
			if (cur.hasDescription())
				tableChooser.addItem(cur.getFriendlyName(), cur.getTableName(), cur.getDescription());
			else
				tableChooser.addItem(cur.getFriendlyName(), cur.getTableName());
		}
	}

	protected void loadColumns(QBTable table) {
		initColumns(table.getColumns().size());

		//this sorts the columns
		Iterator<String> it = table.getColumns().getColumnNames().listIterator();
		while (it.hasNext()) {
			String field = it.next();
			if (!query.isSelected(table.getTableName(), field)) {
				final QBColumn curCol = table.getColumns().getColumn(field);
				String desc = "";

				if (curCol.getRelatedColumn() != null) {
					QBTable t = db.getTable(curCol.getRelatedTable());
					if (t != null) {
						QBColumn col = t.getColumns().
							getColumn(curCol.getRelatedColumn());
						if (col != null) {
							if (t.hasFriendlyName())
								desc += "See table \"" + t.getFriendlyName() + "\" (" +
									t.getTableName() + "), ";
							else
								desc += "See table \"" + t.getTableName() + "\", ";

							if (col.hasFriendlyName())
								desc += "column \"" + col.getFriendlyName() + "\" (" +
									col.getName() + ") ";
							else
								desc += "column \"" + col.getName() + "\" ";

							desc += " for more information, as this column stores " +
								"only a lookup value.";
						}
					}
				}

				if (curCol.hasFriendlyName())
					if (desc.equals(""))
						desc += "Column Name: " + field;
					else
						desc = "Column Name: " + field + "<br/>" + desc;

				if (desc.equals(""))
					desc = null;

				if (isMultipleSelect)
					((HTMLMultipleListBox)columnChooser).addItem(curCol.getFriendlyName(), field, desc);
				else
					((HTMLListBox)columnChooser).addItem(curCol.getFriendlyName(), field, desc);
			}
		}
	}

	public static class CaseInsensitiveAlphanumericComparator implements Comparator<String>, Serializable {
		private static final long serialVersionUID = 1L;
		public int compare(String ol, String or) {
			QBTable table1 = DBStructure.getInstance().getTable(ol);
			QBTable table2 = DBStructure.getInstance().getTable(or);

			return new PortableAlphanumericComparator().compare(
				table1.getFriendlyName().toLowerCase(),
				table2.getFriendlyName().toLowerCase()
			);
		}
	}

}
