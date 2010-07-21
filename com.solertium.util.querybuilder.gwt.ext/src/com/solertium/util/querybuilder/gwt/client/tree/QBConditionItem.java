package com.solertium.util.querybuilder.gwt.client.tree;

import com.solertium.util.gwt.ui.WindowAlertMessage;
import com.solertium.util.querybuilder.gwt.client.QBComparisonConstraintEditor;
import com.solertium.util.querybuilder.gwt.client.QBRelationConstraintEditor;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenu;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenuItem;
import com.solertium.util.querybuilder.gwt.client.utils.SmartCommand;
import com.solertium.util.querybuilder.query.QBComparisonConstraint;
import com.solertium.util.querybuilder.query.QBConstraint;
import com.solertium.util.querybuilder.query.QBConstraintGroup;
import com.solertium.util.querybuilder.query.QBRelationConstraint;
import com.solertium.util.querybuilder.query.QueryConstants;

public abstract class QBConditionItem extends QBTreeItem {
	
	private static final String UNDEFINED = "Undefined Comparison";
	
	protected final QBConstraint constraint;
	protected final int index;

	public QBConditionItem(int index, QBConstraint constraint) {
		super(10, false);
		this.index = index;
		this.constraint = constraint;
		setDisplay();
	}
	
	public String getIcon() {
		if (constraint instanceof QBConstraintGroup)
			return "images/small/folder.png";
		else
			return "images/small/applications-system.png";
	}
	
	public ContextMenu getMyContextMenu() {
		return new ConditionItemFieldContextMenu();
	}
	
	public void setDisplay() {
		if (constraint == null)
			display = UNDEFINED;
		if (constraint instanceof QBConstraintGroup)
			display = "Condition Group";
		else if (constraint instanceof QBComparisonConstraint) {
			QBComparisonConstraint c = (QBComparisonConstraint)constraint;
			if (c.getField() != null) {
				String cName = c.getField();
				display = cName.substring(cName.indexOf(".")+1) + 
					" <span class=\"fontSize60\">(" + cName + ")</span>" + 
					" " +  getComparisonString(c.getComparisonType()) + 
					" " + getCompValueString(c);
			} else {
				display = UNDEFINED;
			}
		}
		else if (constraint instanceof QBRelationConstraint) {
			QBRelationConstraint c = (QBRelationConstraint)constraint;
			if (c.getLeftField() != null && c.getRightField() != null) {
				String left = c.getLeftField();
				String right = c.getRightField();
				display = left.substring(left.indexOf('.')+1) + 
					" <span class=\"fontSize60\">(" + left + ")</span>" + 
					" " + getComparisonString(Integer.toString(c.getComparisonType())) + 
					" " + right.substring(right.indexOf('.')+1) + 
					" <span class=\"fontSize60\">(" + right + ")</span>";
			}
			else
				display = UNDEFINED;
		}
		else
			display = UNDEFINED;
	}
	
	public String getCompValueString(QBComparisonConstraint c) {
		if (c.compareValue == null)
			return "null";
		else if (Boolean.TRUE.equals(c.ask))
			return "${userInputRequired";
		else
			return "\"" + c.writeCompareValue() + "\"";
	}
	
	public String getComparisonString(String value) {
		if (value.equals(QueryConstants.CT_EQUALS))
			return "=";
		else if (value.equals(QueryConstants.CT_LT))
			return "<";
		else if (value.equals(QueryConstants.CT_GT))
			return ">";
		else if (value.equals(QueryConstants.CT_STARTS_WITH))
			return "starts with";
		else if (value.equals(QueryConstants.CT_CONTAINS))
			return "contains";
		else if (value.equals(QueryConstants.CT_ENDS_WITH))
			return "ends with";
		else if (value.equals(QueryConstants.CT_NOT))
			return "not";
		return "=";
	}

	public class ConditionItemFieldContextMenu extends ContextMenu {
		
		public ConditionItemFieldContextMenu() {
			super(true);
			draw();
		}
		
		public void draw() {
			ContextMenuItem setComparison = new ContextMenuItem("Set Comparison", new SmartCommand() {
				public void doAction() {
					if (constraint instanceof QBComparisonConstraint) {
						QBComparisonConstraintEditor editor = new QBComparisonConstraintEditor(getQuery(), (QBComparisonConstraint)constraint) {
							public void onSave() {
								super.onSave();
								getMyParent().refresh();
							}
						};
						editor.draw();
					}
					else if (constraint instanceof QBRelationConstraint) {
						QBRelationConstraintEditor editor = new QBRelationConstraintEditor(getQuery(), (QBRelationConstraint)constraint) {
							public void onSave() {
								super.onSave();
								getMyParent().refresh();
							}
						};
						editor.draw();
					}
					else {
						WindowAlertMessage m = new WindowAlertMessage(WindowAlertMessage.IMAGE_DIALOG_ERROR, "Build error: Could not find appropriate editor tool for this constraint.", "OK");
						m.show();
					}
				}
			});
			
			ContextMenuItem delete = new ContextMenuItem("Delete", new SmartCommand() {
				public void doAction() {
					((QBConditionGroupRootItem)getMyParent()).removeChild(index);
					getMyParent().refresh();
				}
			});
			
			addItem(setComparison);
			addItem(delete);
			
		}	
	}
}