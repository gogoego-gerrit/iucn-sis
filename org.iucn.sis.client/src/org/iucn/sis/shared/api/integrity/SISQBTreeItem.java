package org.iucn.sis.shared.api.integrity;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.solertium.util.querybuilder.gwt.client.chooser.TableChooser;
import com.solertium.util.querybuilder.gwt.client.chooser.TableChooserSaveListener;
import com.solertium.util.querybuilder.gwt.client.tree.QBTreeItem;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.query.SelectedField;

/**
 * SISQBTreeItem.java
 * 
 * Had to override QBTreeItem in order to use some specific behavior of the
 * SISTableChooser, namely the ability to specify which tables have already been
 * selected (which is not useful with the normal TableChooser).
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public abstract class SISQBTreeItem extends QBTreeItem {

	public SISQBTreeItem(int leftMargin) {
		super(leftMargin);
	}

	public SISQBTreeItem(int leftMargin, boolean isRoot) {
		super(leftMargin, isRoot);
	}

	public void load() {
		GWTQBQuery query = getQuery();
		for (int i = 0; i < query.getFields().size(); i++) {
			addChild(new SISQBFieldItem(query.getFields().get(i)) {
				public GWTQBQuery getQuery() {
					return SISQBTreeItem.this.getQuery();
				}
			});
		}
		draw();
	}

	protected Menu getMyContextMenu() {
		if (menu == null)
			menu = new SISFieldRootLevelContextMenu();
		return menu;
	}

	class SISFieldRootLevelContextMenu extends Menu {

		public SISFieldRootLevelContextMenu() {
			super();

			final MenuItem item = new MenuItem("Add Table",
					new SelectionListener<MenuEvent>() {
						public void componentSelected(MenuEvent ce) {
							final ArrayList<String> selected = new ArrayList<String>();
							for (QBTreeItem item : getChildren()) {
								if (item instanceof SISQBFieldItem)
									selected.add(((SISQBFieldItem) item)
											.getField().getTableName());
							}
							final TableChooser tc = TableChooser.getInstance(
									getQuery(), true);
							if (tc instanceof SISTableChooser)
								((SISTableChooser) tc)
										.setPreviouslySelectedTables(selected);
							tc.addSaveListener(new TableChooserSaveListener() {
								public void onSave(String selectedTable,
										ArrayList<String> selectedColumns) {
									for (int i = 0; i < selectedColumns.size(); i++) {
										SelectedField field = new SelectedField(
												selectedTable, selectedColumns
														.get(i));
										if (!getQuery().getTables().contains(
												field.getTableName()))
											getQuery().addField(field);
									}
									refresh();
								}
							});
							tc.draw();
						}
					});

			add(item);
		}

	}

}
