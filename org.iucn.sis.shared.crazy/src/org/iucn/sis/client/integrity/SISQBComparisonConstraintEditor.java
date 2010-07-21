package org.iucn.sis.client.integrity;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.solertium.util.gwt.ui.StyledHTML;
import com.solertium.util.querybuilder.gwt.client.QBComparisonConstraintEditor;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.query.QBComparisonConstraint;

public class SISQBComparisonConstraintEditor extends QBComparisonConstraintEditor {
	
	protected static final int INPUT_TYPE_DATE_NOW = 6;

	public SISQBComparisonConstraintEditor(GWTQBQuery query, QBComparisonConstraint comparison) {
		super(query, comparison);
	}
	
	protected void setInitInputType() {
		if ("${date.now}".equals(comparison.compareValue))
			inputType = INPUT_TYPE_DATE_NOW;
		else
			super.setInitInputType();
	}
	
	public void getAppropriateValueSettingTool() {
		if (inputType != INPUT_TYPE_DATE_NOW)
			super.getAppropriateValueSettingTool();
		else {
			table.setWidget(2, 1, new StyledHTML("date.now()", "fontSize60"));
		}
	}
	
	public void onSave() {
		if (inputType != INPUT_TYPE_DATE_NOW)
			super.onSave();
		else {
			comparison.setComparisonType(Integer.parseInt(box.getValue(box.getSelectedIndex())));
			comparison.setComparisonValue("${date.now}");
			comparison.ask = null;
			close();
		}
		super.onSave();
	}
	
	public Menu getMyContextMenu() {
		return new SISContextMenu();
	}

	public class SISContextMenu extends InternalContextMenu {

		public SISContextMenu() {
			super();
		}

		protected void buildMenu() {
			final MenuItem text = new MenuItem("Text Entry", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					swap(INPUT_TYPE_FREE_TEXT);
				}
			});
			
			final Menu dateMenu = new Menu();
			dateMenu.add(new MenuItem("Date Entry", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					swap(INPUT_TYPE_DATE);
				}
			}));
			dateMenu.add(new MenuItem("Date.Now()", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					swap(INPUT_TYPE_DATE_NOW);
				}
			}));
			
			final MenuItem dateMenuItem = new MenuItem("Date...");
			dateMenuItem.setSubMenu(dateMenu);
			
			final MenuItem nil = new MenuItem("Null Value", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					swap(INPUT_TYPE_NULL);
				}
			});

			final MenuItem lut = new MenuItem("Lookup Table", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					swap(INPUT_TYPE_LOOKUP_VALUE);
				}
			});

			final MenuItem ask = new MenuItem("Ask User", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					swap(INPUT_TYPE_ASK_USER);
				}
			});

			add(text);
			//TODO: check field type?
			add(dateMenuItem);
			add(nil);
			if (comparison.getField() != null && db.hasLookupTable(comparison.getField()))
				add(lut);
			add(ask);
		}
	}
	
}
