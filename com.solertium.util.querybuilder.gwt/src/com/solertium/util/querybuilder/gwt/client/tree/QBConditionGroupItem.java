package com.solertium.util.querybuilder.gwt.client.tree;

import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
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
	
	protected ContextMenu getContextMenu() {
		return new ConditionGroupContextMenu();		
	}
	
	class ConditionGroupContextMenu extends ContextMenu {
		
		public ConditionGroupContextMenu() {
			super(true);
			setAutoOpen(true);
			
			if (children.isEmpty()) {
				addItem(new MenuItem("Add Condition", getGeneralSubMenu(null)));
			}
			else {			
				addItem("AND Condition", getGeneralSubMenu(QueryConstants.CG_AND));
				addItem("OR Condition", getGeneralSubMenu(QueryConstants.CG_OR));
			}
			
			addItem(new ContextMenuItem("Delete", new SmartCommand() {
				public void doAction() {
					((QBConditionGroupRootItem)getMyParent()).removeChild(index);
					getMyParent().refresh();
				}
			}));
		}
		
		private MenuBar getGeneralSubMenu(final String addMethod) {
			MenuBar addConditionMenu = new MenuBar(true);
			
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
			
			addConditionMenu.addItem(field);
			addConditionMenu.addItem(group);
			
			return addConditionMenu;
		}
		
	}

}
