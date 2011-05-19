package org.iucn.sis.shared.api.integrity;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.WindowAlertMessage;
import com.solertium.util.querybuilder.gwt.client.QBComparisonConstraintEditor;
import com.solertium.util.querybuilder.gwt.client.QBRelationConstraintEditor;
import com.solertium.util.querybuilder.gwt.client.tree.QBConditionItem;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenu;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenuItem;
import com.solertium.util.querybuilder.gwt.client.utils.SmartCommand;
import com.solertium.util.querybuilder.query.QBComparisonConstraint;
import com.solertium.util.querybuilder.query.QBConstraint;
import com.solertium.util.querybuilder.query.QBRelationConstraint;

public abstract class SISQBConditionItem extends QBConditionItem {

	public SISQBConditionItem(int index, QBConstraint constraint) {
		super(index, constraint);
	}
	
	public ContextMenu getMyContextMenu() {
		return new SISConditionItemFieldContextMenu();
	}
	
	public class SISConditionItemFieldContextMenu extends ConditionItemFieldContextMenu {
		
		public SISConditionItemFieldContextMenu() {
			super();
		}
		
		public void draw() {
			ContextMenuItem setComparison = new ContextMenuItem("Set Comparison", new SmartCommand() {
				public void doAction() {
					if (constraint instanceof QBComparisonConstraint) {
						QBComparisonConstraintEditor editor = new SISQBComparisonConstraintEditor(getQuery(), (QBComparisonConstraint)constraint) {
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
			
			ContextMenuItem desc = new ContextMenuItem("Set Failure Description", new SmartCommand() {
				public void doAction() {
					final SISQBQuery query = (SISQBQuery)getQuery();
					final String id = constraint.getID();
					
					final TextArea area = new TextArea();
					area.setEmptyText("Click to Enter Description");
					area.setValue(query.getErrorMessage(id));
					area.setSize(350, 100);
					
					final Window window = WindowUtils.newWindow("Set Failure Description");
					window.setSize(400, 200);
					window.setLayout(new FillLayout());
					window.add(area);
					window.addButton(new Button("Set", new SelectionListener<ButtonEvent>() {
						public void componentSelected(ButtonEvent ce) {
							query.setErrorMessage(id, area.getValue());
							window.hide();
						}
					}));
					window.addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
						public void componentSelected(ButtonEvent ce) {
							window.hide();
						}
					}));
					window.show();
				}
			});
			
			ContextMenuItem delete = new ContextMenuItem("Delete", new SmartCommand() {
				public void doAction() {
					((SISQBConditionGroupRootItem)getMyParent()).removeChild(index);
					getMyParent().refresh();
				}
			});
			
			addItem(setComparison);
			addItem(desc);
			addItem(delete);
		}
	}

}
