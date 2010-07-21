package com.solertium.util.querybuilder.gwt.client.tree;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenu;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenuItem;
import com.solertium.util.querybuilder.gwt.client.utils.SmartCommand;
import com.solertium.util.querybuilder.query.QBComparisonConstraint;
import com.solertium.util.querybuilder.query.QBConstraint;
import com.solertium.util.querybuilder.query.QBConstraintGroup;
import com.solertium.util.querybuilder.query.QueryConstants;

public abstract class QBConditionGroupItem extends QBConditionGroupRootItem {
	
	private QBConstraint constraint;
	private int index;
	
	public QBConditionGroupItem(int index, QBConstraint constraint) {
		super(10, false);
		this.index = index;
		this.constraint = constraint;
		display = "Condition Group";
	}
	
	public QBConstraintGroup getConstraintGroup() {
		return (QBConstraintGroup)constraint;
	}
	
	protected ContextMenu getMyContextMenu() {
		return new ConditionGroupContextMenu();		
	}
	
	protected class ConditionGroupContextMenu extends ContextMenu {
		
		public ConditionGroupContextMenu() {
			super(true);
			
			if (children.isEmpty()) {
				final MenuItem item = new MenuItem("Add Condition");
				item.setSubMenu(getGeneralSubMenu(null));
				
				add(item);
				//addItem(new MenuItem("Add Condition", getGeneralSubMenu(null)));
			}
			else {			
				final MenuItem and = new MenuItem("AND Condition");
				and.setSubMenu(getGeneralSubMenu(QueryConstants.CG_AND));
				
				add(and);
				
				final MenuItem or = new MenuItem("OR Condition");
				or.setSubMenu(getGeneralSubMenu(QueryConstants.CG_OR));
				
				add(or);
//				addItem("AND Condition", getGeneralSubMenu(QueryConstants.CG_AND));
//				addItem("OR Condition", getGeneralSubMenu(QueryConstants.CG_OR));
			}
			
			addItem(new ContextMenuItem("Delete", new SmartCommand() {
				public void doAction() {
					((QBConditionGroupRootItem)getMyParent()).removeChild(index);
					getMyParent().refresh();
				}
			}));
		}
		
		private Menu getGeneralSubMenu(final String addMethod) {
			Menu addConditionMenu = new Menu();
			
			ContextMenuItem field = new ContextMenuItem("Field Comparison", new SmartCommand() {
				public void doAction() {
					QBComparisonConstraint nc = new QBComparisonConstraint();
					nc.comparisonType = new Integer(QueryConstants.CT_EQUALS);
					nc.compareValue = "";
					
					if (addMethod == null)
						getConstraintGroup().addConstraint(nc);
					else if (addMethod.equals(QueryConstants.CG_AND) || 
							addMethod.equals(QueryConstants.CG_OR)) {
						getConstraintGroup().addConstraint(
							Integer.parseInt(addMethod), nc
						);
					}
					
					refresh();
				}
			});
			
			ContextMenuItem group = new ContextMenuItem("Condition Group", new SmartCommand() {
				public void doAction() {
					QBConstraint nc = new QBConstraintGroup();
					
					if (addMethod == null)
						getConstraintGroup().addConstraint(nc);
					else if (addMethod.equals(QueryConstants.CG_AND) || 
							addMethod.equals(QueryConstants.CG_OR)) {
						getConstraintGroup().addConstraint(
							Integer.parseInt(addMethod), nc
						);
					}
					refresh();
				}
			});
			
			addConditionMenu.add(field);
			addConditionMenu.add(group);
			
			return addConditionMenu;
		}
		
	}

}
