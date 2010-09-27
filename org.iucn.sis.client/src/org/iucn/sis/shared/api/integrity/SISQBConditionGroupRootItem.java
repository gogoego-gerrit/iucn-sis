package org.iucn.sis.shared.api.integrity;

import java.util.Iterator;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.util.querybuilder.gwt.client.tree.QBConditionGroupItem;
import com.solertium.util.querybuilder.gwt.client.tree.QBConditionGroupRootItem;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenu;
import com.solertium.util.querybuilder.gwt.client.utils.ContextMenuItem;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.gwt.client.utils.SmartCommand;
import com.solertium.util.querybuilder.query.QBComparisonConstraint;
import com.solertium.util.querybuilder.query.QBConstraint;
import com.solertium.util.querybuilder.query.QBConstraintGroup;
import com.solertium.util.querybuilder.query.QBRelationConstraint;

public abstract class SISQBConditionGroupRootItem extends QBConditionGroupRootItem {

	public SISQBConditionGroupRootItem() {
		super();
	}

	public SISQBConditionGroupRootItem(int leftMargin, boolean isRoot) {
		super(leftMargin, isRoot);
	}

	public void load() {
		Iterator<Object> it = getConstraintGroup().iterator();

		int index = 0;
		while (it.hasNext()) {
			Object cur = it.next();
			if (cur instanceof QBComparisonConstraint)
				addChild(new SISQBConditionItem(index, (QBConstraint)cur) {
					public GWTQBQuery getQuery() {
						return SISQBConditionGroupRootItem.this.getQuery();
					}
				});
			else if (cur instanceof QBRelationConstraint)
				addChild(new SISQBConditionItem(index, (QBConstraint)cur) {
					public GWTQBQuery getQuery() {
						return SISQBConditionGroupRootItem.this.getQuery();						
					}
				});
			else if (cur instanceof QBConstraintGroup) {
				addChild(new QBConditionGroupItem(index, (QBConstraint)cur) {
					public GWTQBQuery getQuery() {
						return SISQBConditionGroupRootItem.this.getQuery();
					}
					protected ContextMenu getMyContextMenu() {
						final ContextMenu menu = super.getMyContextMenu();
						
						ContextMenuItem desc = new ContextMenuItem("Set Failure Description", new SmartCommand() {
							public void doAction() {
								final SISQBQuery query = (SISQBQuery)getQuery();
								final String id = getConstraintGroup().getID();
								
								final TextArea area = new TextArea();
								area.setEmptyText("Click to Enter Description");
								area.setValue(query.getErrorMessage(id));
								area.setSize(350, 100);
								
								final Window window = new Window();
								window.setHeading("Set Failure Description");
								window.setModal(true);
//								window.setAlignment(HorizontalAlignment.CENTER);
								window.setClosable(true);
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
						
						final Component deleteOption = menu.getItem(1);
						menu.remove(deleteOption);
						
						menu.addItem(desc);
						menu.add(deleteOption);
						
						return menu;
					}
				});
			}
			else
				addChild(new TextItem(cur));
			index++;
		}

		draw();
	}

}
