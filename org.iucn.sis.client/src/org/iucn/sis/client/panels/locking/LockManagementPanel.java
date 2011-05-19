package org.iucn.sis.client.panels.locking;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.gwt.ui.DrawsLazily;

public class LockManagementPanel extends LayoutContainer implements DrawsLazily {
	
	private boolean isDrawn = false;
	
	public LockManagementPanel() {
		super();
		setLayout(new FillLayout());
	}
	
	public void draw(final DrawsLazily.DoneDrawingCallback callback) {
		isDrawn = true;
		LockLoader.impl.load(new GenericCallback<Object>() {
			public void onSuccess(Object result) {
				render(callback);
			}
			public void onFailure(Throwable caught) {
				render(callback);
			}
		});
	}
	
	public boolean isDrawn() {
		return isDrawn;
	}
	
	private void render(final DrawsLazily.DoneDrawingCallback callback) {
		removeAll();
		/*
		final TabItem byItem = new TabItem("Individual Locked Assessments");
		byItem.add(new LockedAssessmentsTable());
		byItem.setLayout(new FillLayout());
		
		final TabItem byGroup = new TabItem("By Group");
		byGroup.add(new LockedGroupsTable());
		byGroup.setLayout(new FillLayout());
		
		final TabPanel panel = new TabPanel();
		panel.add(byItem);
		panel.add(byGroup);
		panel.addListener(Events.Select, new Listener<TabPanelEvent>() {
			public void handleEvent(TabPanelEvent be) {
				if (be.getItem().getWidget(0) instanceof DrawsLazily) {
					((DrawsLazily)be.getItem().getWidget(0)).draw(new DrawsLazily.DoneDrawingCallback() {
						public void isDrawn() {
							layout();
						}
					});
				}
			}
		});
		panel.setSelection(byItem);
		*/
		//add(panel);
		final GroupedTablePanel p2 = new GroupedTablePanel();
		p2.draw(new DoneDrawingCallback() {
			public void isDrawn() {
				add(p2);
				callback.isDrawn();
				layout();
			}
		});
	}

}
