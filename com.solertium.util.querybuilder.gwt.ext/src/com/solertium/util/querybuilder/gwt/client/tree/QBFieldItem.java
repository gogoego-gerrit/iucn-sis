package com.solertium.util.querybuilder.gwt.client.tree;

import com.google.gwt.user.client.Window;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenu;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenuItem;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.gwt.client.utils.SmartCommand;
import com.solertium.util.querybuilder.query.QueryConstants;
import com.solertium.util.querybuilder.query.SelectedField;
import com.solertium.util.querybuilder.struct.DBStructure;
import com.solertium.util.querybuilder.struct.QBTable;

public abstract class QBFieldItem extends QBTreeItem {

	protected SelectedField field;
	protected final boolean isFriendly = DBStructure.
		getInstance().getChooserType().equals(DBStructure.TABLE_CHOOSER_FRIENDLY);

	public QBFieldItem(QBTable table, String columnName) {
		super(10, false);
		field = new SelectedField(table.getTableName(), columnName);

		display = field.getDisplay(isFriendly);
	}

	public QBFieldItem(SelectedField field) {
		super(10, false);
		this.field = field;

		display = field.getDisplay(isFriendly);
	}

	public abstract GWTQBQuery getQuery();

	public String getIcon() {
		return "images/small/applications-system.png";
	}

	public ContextMenu getMyContextMenu() {
		return new FieldItemContextMenu();
	}

	class FieldItemContextMenu extends ContextMenu {

		public FieldItemContextMenu() {
			super(true);

			final int index = getMyParent().children.indexOf(QBFieldItem.this);

			ContextMenuItem ascOrder = new ContextMenuItem("Ascending Order", new SmartCommand() {
				public void doAction() {
					field.setAttribute("sort", QueryConstants.SORT_ASC);
					display = field.getDisplay(isFriendly);
					refresh();
				}
			});

			ContextMenuItem descOrder = new ContextMenuItem("Descending Order", new SmartCommand() {
				public void doAction() {
					field.setAttribute("sort", QueryConstants.SORT_DESC);
					display = field.getDisplay(isFriendly);
					refresh();
				}
			});

			ContextMenuItem noOrder = new ContextMenuItem("No Order", new SmartCommand() {
				public void doAction() {
					field.setAttribute("sort", null);
					display = field.getDisplay(isFriendly);
					refresh();
				}
			});

			ContextMenuItem hRepeat = new ContextMenuItem("Hide Repeats", new SmartCommand() {
				public void doAction() {
					field.setAttribute("range", "true");
					display = field.getDisplay(isFriendly);
					refresh();
				}
			});

			ContextMenuItem sRepeat = new ContextMenuItem("Show Repeats", new SmartCommand() {
				public void doAction() {
					field.setAttribute("range", "false");
					display = field.getDisplay(isFriendly);
					refresh();
				}
			});

			ContextMenuItem inJoin = new ContextMenuItem("Default (Inner) Join", new SmartCommand() {
				public void doAction() {
					field.setAttribute("outer", "false");
					display = field.getDisplay(isFriendly);
					refresh();
				}
			});

			ContextMenuItem outJoin = new ContextMenuItem("Outer Join", new SmartCommand() {
				public void doAction() {
					field.setAttribute("out", "true");
					display = field.getDisplay(isFriendly);
					refresh();
				}
			});

			ContextMenuItem moveUp = new ContextMenuItem("Move Up", new SmartCommand() {
				public void doAction() {
					getQuery().swapOrder(index, index-1);
					getMyParent().refresh();
				}
			});

			ContextMenuItem moveDown = new ContextMenuItem("Move Down", new SmartCommand() {
				public void doAction() {
					getQuery().swapOrder(index, index+1);
					getMyParent().refresh();
				}
			});

			ContextMenuItem delete = new ContextMenuItem("Delete", new SmartCommand() {
				public void doAction() {
					if (Window.confirm("Are you sure you want to remove this field?")) {
						getQuery().removeField(field);
						getMyParent().refresh();
					}
				}
			});

			addItem(ascOrder);
			addItem(descOrder);
			addItem(noOrder);
			addItem(hRepeat);
			addItem(sRepeat);
			addItem(inJoin);
			addItem(outJoin);

			if (index != 0)
				addItem(moveUp);

			if (getMyParent().children.size() > 1 && index != getMyParent().children.size()-1)
				addItem(moveDown);

			addItem(delete);
		}

	}

}
