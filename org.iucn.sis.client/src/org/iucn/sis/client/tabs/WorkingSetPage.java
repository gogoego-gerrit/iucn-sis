package org.iucn.sis.client.tabs;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.filters.AssessmentFilterPanel;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
import org.iucn.sis.client.panels.workingsets.DeleteWorkingSetPanel;
import org.iucn.sis.client.panels.workingsets.WorkingSetAddAssessmentsPanel;
import org.iucn.sis.client.panels.workingsets.WorkingSetEditBasicPanel;
import org.iucn.sis.client.panels.workingsets.WorkingSetExporter;
import org.iucn.sis.client.panels.workingsets.WorkingSetOptionsPanel;
import org.iucn.sis.client.panels.workingsets.WorkingSetPermissionPanel;
import org.iucn.sis.client.panels.workingsets.WorkingSetReportPanel;
import org.iucn.sis.client.panels.workingsets.WorkingSetSummaryPanel;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.user.client.ui.Grid;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.gwt.ui.StyledHTML;

public class WorkingSetPage extends FeaturedItemContainer<WorkingSet> {
	
	private final WorkingSetSummaryPanel homePage = new WorkingSetSummaryPanel();
	private final WorkingSetEditBasicPanel editor = new WorkingSetEditBasicPanel(this);
	private final WorkingSetAddAssessmentsPanel assessments = new WorkingSetAddAssessmentsPanel(this);
	private final WorkingSetOptionsPanel taxa = new WorkingSetOptionsPanel();
	
	@Override
	protected void drawBody(DoneDrawingCallback callback) {
		setBodyContainer(homePage);
	}
	
	@Override
	protected LayoutContainer updateFeature() {
		final WorkingSet item = getSelectedItem();
		
		final LayoutContainer container = new LayoutContainer();
		container.add(new StyledHTML(item.getName(), "page_workingSet_featured_header"));
		
		final Grid stats = new Grid(3, 2);
		stats.setWidget(0, 0, new StyledHTML("Created:", "page_workingSet_featured_prompt"));
		stats.setWidget(0, 1, new StyledHTML(FormattedDate.impl.getDate(item.getCreatedDate()), "page_workingSet_featured_content"));
		stats.setWidget(1, 0, new StyledHTML("Mode:", "page_workingSet_featured_prompt"));
		stats.setWidget(1, 1, new StyledHTML("Public", "page_workingSet_featured_content"));
		stats.setWidget(2, 0, new StyledHTML("Scope:", "page_workingSet_featured_prompt"));
		stats.setWidget(2, 1, new StyledHTML(AssessmentFilterPanel.getString(item.getFilter()), "page_workingSet_featured_content"));
		
		container.add(stats);
		
		return container;
	}
	
	@Override
	protected void updateSelection(WorkingSet selection) {
		WorkingSetCache.impl.setCurrentWorkingSet(selection, true);
	}
	
	protected void setBodyContainer(LayoutContainer container) {
		bodyContainer.removeAll();
		
		if (container instanceof RefreshLayoutContainer)
			((RefreshLayoutContainer)container).refresh();
		
		bodyContainer.add(container);
	}
	
	@Override
	protected void drawOptions(DrawsLazily.DoneDrawingCallback callback) {
		if (optionsContainer.getItemCount() == 0) {
			final VerticalPanel buttonArea = new VerticalPanel();
			buttonArea.setSpacing(10);
			buttonArea.setWidth(150);
			
			buttonArea.add(createButton("Edit Basic Information", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					setBodyContainer(editor);
				}
			}));
			buttonArea.add(createButton("Taxa Manager", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					setBodyContainer(taxa);
				}
			}));
			buttonArea.add(createButton("Create Draft Assessment", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					setBodyContainer(assessments);
				}
			}));
			buttonArea.add(createButton("Permisison Manager", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.GRANT, 
							WorkingSetCache.impl.getCurrentWorkingSet())) {
						final WorkingSetPermissionPanel panel = new WorkingSetPermissionPanel();
						panel.draw(new DrawsLazily.DoneDrawingCallback() {
							public void isDrawn() {
								setBodyContainer(panel);	
							}
						});
					} else
						WindowUtils.errorAlert("Insufficient Permissions", "You do not have permission to manage " +
								"the permissions for this Working Set.");
				}
			}));
			buttonArea.add(createButton("Report Generator", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					setBodyContainer(new WorkingSetReportPanel());
				}
			}));
			buttonArea.add(createButton("Export to Offline", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					setBodyContainer(new WorkingSetExporter(WorkingSetPage.this));
				}
			}));
			buttonArea.add(createButton("Export to Access", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					final ContentPanel panel = new ContentPanel();
					panel.setHeading("Export to Access");
					panel.getHeader().addTool(new Button("Cancel", new SelectionListener<ButtonEvent>() {
						public void componentSelected(ButtonEvent ce) {
							setManagerTab();
						}
					}));
					panel.setUrl("/export/access/" + WorkingSetCache.impl.getCurrentWorkingSet().getId());
					
					setBodyContainer(panel);
				}
			}));
			buttonArea.add(createButton("Delete/Unsubscribe", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					DeleteWorkingSetPanel panel = new DeleteWorkingSetPanel(WorkingSetPage.this);
					setBodyContainer(panel);
				}
			}));
			
			optionsContainer.removeAll();
			optionsContainer.setLayout(new CenterLayout());
			optionsContainer.add(buttonArea);
		}
		callback.isDrawn();
	}
	
	private Button createButton(String text, SelectionListener<ButtonEvent> listener) {
		return createButton(text, null, listener);
	}
	
	private Button createButton(String text, String icon, SelectionListener<ButtonEvent> listener) {
		Button button = new Button(text);
		button.setIconStyle(icon);
		button.addSelectionListener(listener);
		
		return button;
	}
	
	public void setEditWorkingSetTab() {
		setBodyContainer(editor);
	}
	
	public void setManagerTab() {
		setBodyContainer(homePage);
	}
	
	public void setAssessmentTab() {
		setBodyContainer(assessments);
	}
	
	public void setEditTaxaTab() {
		setBodyContainer(taxa);
	}

}
