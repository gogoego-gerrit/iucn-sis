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
import com.solertium.util.querybuilder.struct.QBColumn;
import com.solertium.util.querybuilder.struct.QBTable;

/**
 * OfficialNameTableChooser.java
 *
 * @author carl.scott
 *
 */
public class OfficialNameTableChooser extends TableChooser {

	/**
	 * @param query
	 */
	public OfficialNameTableChooser(final GWTQBQuery query, final boolean isMultipleSelect) {
		super(query, isMultipleSelect);
	}

	protected void loadColumns(QBTable table) {
		initColumns(table.getColumns().size());

		//this sorts the columns
		Iterator<String> it = table.getColumns().getColumnNames().listIterator();
		while (it.hasNext()) {
			String field = it.next();
			if (!query.isSelected(table.getTableName(), field)) {
				QBColumn curCol = table.getColumns().getColumn(field);
				String desc = "";
				if (curCol.getRelatedColumn() != null) {
					QBTable t = db.getTable(curCol.getRelatedTable());
					if (t != null) {
						QBColumn col = t.getColumns().
							getColumn(curCol.getRelatedColumn());
						if (col != null) {
							desc += "See table \"" + t.getTableName() + "\", "
							 	+ "column \"" + col.getName() + "\" "
							 	+ " for more information, as this column stores " +
								"only a lookup value.";
						}
					}
				}

				if (desc.equals(""))
					desc = null;

				if (isMultipleSelect)
					((HTMLMultipleListBox)columnChooser).addItem(field, field, desc);
				else
					((HTMLListBox)columnChooser).addItem(field, field, desc);
			}
		}
	}

	protected void populateTableListing() {
		Collections.sort(tables, new CaseInsensitiveAlphanumericComparator());
		for (int i = 0; i < tables.size(); i++) {
			QBTable cur = db.getTable(tables.get(i));
			if (cur.hasDescription())
				tableChooser.addItem(cur.getTableName(), cur.getTableName(), cur.getDescription());
			else
				tableChooser.addItem(cur.getTableName());
		}
	}

	private static class CaseInsensitiveAlphanumericComparator implements Comparator<String>, Serializable {
		private static final long serialVersionUID = 1L;
		public int compare(String ol, String or) {
			String table1 = ol;
			String table2 = or;

			return new PortableAlphanumericComparator().
				compare(table1.toLowerCase(), table2.toLowerCase());
		}
	}

}
