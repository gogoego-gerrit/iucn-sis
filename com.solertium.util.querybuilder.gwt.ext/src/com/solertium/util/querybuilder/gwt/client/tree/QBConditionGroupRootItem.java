package com.solertium.util.querybuilder.gwt.client.tree;

import java.util.Iterator;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenu;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenuItem;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.gwt.client.utils.SmartCommand;
import com.solertium.util.querybuilder.query.QBComparisonConstraint;
import com.solertium.util.querybuilder.query.QBConstraint;
import com.solertium.util.querybuilder.query.QBConstraintGroup;
import com.solertium.util.querybuilder.query.QBRelationConstraint;
import com.solertium.util.querybuilder.query.QueryConstants;

public abstract class QBConditionGroupRootItem extends QBTreeItem {

	public QBConditionGroupRootItem() {
		this(0, true);
	}

	public QBConditionGroupRootItem(int leftMargin, boolean isRoot) {
		super(leftMargin, isRoot);
	}

	protected ContextMenu getMyContextMenu() {
		return new ConditionRootLevelContextMenu();
	}

	public QBConstraintGroup getConstraintGroup() {
		return getQuery().getConditions();
	}

	public void removeChild(int index) {
		getConstraintGroup().remove(index);
	}

	protected Image buildButton() {
		if (button == null) {
			if (isRoot || hasChildren()) {
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

	public void load() {
		Iterator<Object> it = getConstraintGroup().iterator();

		int index = 0;
		while (it.hasNext()) {
			Object cur = it.next();
			if (cur instanceof QBComparisonConstraint)
				addChild(new QBConditionItem(index, (QBConstraint)cur) {
					public GWTQBQuery getQuery() {
						return QBConditionGroupRootItem.this.getQuery();
					}
				});
			else if (cur instanceof QBRelationConstraint)
				addChild(new QBConditionItem(index, (QBConstraint)cur) {
					public GWTQBQuery getQuery() {
						return QBConditionGroupRootItem.this.getQuery();						
					}
				});
			else if (cur instanceof QBConstraintGroup)
				addChild(new QBConditionGroupItem(index, (QBConstraint)cur) {
					public GWTQBQuery getQuery() {
						return QBConditionGroupRootItem.this.getQuery();
					}
				});
			else
				addChild(new TextItem(cur));
			index++;
		}

		draw();
	}

	public class TextItem extends QBTreeItem {

		public TextItem(Object text) {
			super(QBConditionGroupRootItem.this.leftMargin);
			display = text.toString();
			if (display.equals("1"))
				display = "AND";
			else
				display = "OR";
		}

		public String getIcon() {
			return null;
		}

		public void load() {
			draw();
		}
		
		protected Menu getMyContextMenu() {
			return null;
		}

		public ContextMenu getContextMenu() {
			return null;
		}

		public GWTQBQuery getQuery() {
			return null;
		}

	}

	public class ConditionRootLevelContextMenu extends ContextMenu {

		public ConditionRootLevelContextMenu() {
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
				//addItem("AND Condition", getGeneralSubMenu(QueryConstants.CG_AND));
				//addItem("OR Condition", getGeneralSubMenu(QueryConstants.CG_OR));
			}
		}
		
		protected QBComparisonConstraint newComparisonConstraint() {
			final QBComparisonConstraint nc = new QBComparisonConstraint();
			nc.comparisonType = new Integer(QueryConstants.CT_EQUALS);
			nc.compareValue = "";
			
			return nc;
		}
		
		protected QBRelationConstraint newRelationConstraint() {
			final QBRelationConstraint nc = new QBRelationConstraint();
			
			return nc;
		}

		private Menu getGeneralSubMenu(final String addMethod) {
			Menu addConditionMenu = new Menu();

			ContextMenuItem field = new ContextMenuItem("Field Comparison", new SmartCommand() {
				public void doAction() {
					QBComparisonConstraint nc = newComparisonConstraint();

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
			
			ContextMenuItem relation = new ContextMenuItem("Field Relation", new SmartCommand() {
				public void doAction() {
					QBRelationConstraint nc = newRelationConstraint();
					
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
			addConditionMenu.add(relation);
			addConditionMenu.add(group);

			return addConditionMenu;
		}

	}

}
