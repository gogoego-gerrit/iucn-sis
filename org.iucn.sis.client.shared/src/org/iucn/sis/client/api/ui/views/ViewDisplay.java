package org.iucn.sis.client.api.ui.views;

import java.util.Arrays;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.SchemaCache;
import org.iucn.sis.client.api.caches.ViewCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.shared.api.acl.UserPreferences;
import org.iucn.sis.shared.api.models.Assessment;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.gwt.ui.DrawsLazily;

public class ViewDisplay extends LayoutContainer implements DrawsLazily {
	
	private ComplexListener<PageChangeRequest> pageChangelistener;
	
	private HTML lastSelected = null;
		
	public void setPageChangelistener(
			ComplexListener<PageChangeRequest> pageChangelistener) {
		this.pageChangelistener = pageChangelistener;
	}
	
	public void selectPage(HTML selection) {
		if (lastSelected != null)
			lastSelected.removeStyleName("bold");
		
		if (selection != null)
			selection.addStyleName("bold");
		
		lastSelected = selection;
	}
	
	public HTML getLastSelected() {
		return lastSelected;
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		Assessment assessment = AssessmentCache.impl.getCurrentAssessment();
		if (assessment == null) {
			callback.isDrawn();
			return;
		}
		
		String schema = assessment.getSchema(SchemaCache.impl.getDefaultSchema());
		if (schema.equals(ViewCache.impl.getCurrentSchema())) {
			callback.isDrawn();
			return;
		}
		
		selectPage(null);
		
		ViewCache.impl.fetchViews(schema, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				callback.isDrawn();
			}
			public void onSuccess(String arg0) {
				removeAll();
				
				String prefs = SISClientBase.currentUser.getPreference(UserPreferences.VIEW_CHOICES, null);
				List<String> viewsToShow = null;
				if (prefs != null && !prefs.equals(""))
					viewsToShow = Arrays.asList(prefs.split(","));

				for (final SISView curView : ViewCache.impl.getAvailableViews()) {
					if (viewsToShow == null || viewsToShow.contains(curView.getId())) {
						final ContentPanel curItem = new ContentPanel();
						curItem.setScrollMode(Scroll.AUTOY);
						curItem.setHeading(curView.getDisplayableTitle());

						for (final SISPageHolder curPage : curView.getPages()) {
							final HTML curPageLabel = new HTML(curPage.getPageTitle());
							curPageLabel.addStyleName("SIS_HyperlinkBehavior");
							curPageLabel.addStyleName("padded-viewContainer");
							curPageLabel.addClickHandler(new ClickHandler() {
								public void onClick(ClickEvent event) {
									if (pageChangelistener != null)
										pageChangelistener.handleEvent(
											new PageChangeRequest(curView, curPage)
										);
								}
							});
							curItem.add(curPageLabel);
						}

						add(curItem);
					}
				}
				
				callback.isDrawn();
			}
		});
	}

	public static class PageChangeRequest {
		
		private SISView view;
		private SISPageHolder page;
		private boolean allowSamePage;
		private boolean autoSaveOnPageChange;
		
		public PageChangeRequest(SISView view, SISPageHolder page) {
			this.view = view;
			this.page = page;
			this.autoSaveOnPageChange = true;
		}
		
		public SISPageHolder getPage() {
			return page;
		}
		
		public SISView getView() {
			return view;
		}
		
		public boolean isAllowSamePage() {
			return allowSamePage;
		}
		
		public void setAllowSamePage(boolean allowSamePage) {
			this.allowSamePage = allowSamePage;
		}
		
		public boolean isAutoSaveOnPageChange() {
			return autoSaveOnPageChange;
		}
		
		public void setAutoSaveOnPageChange(boolean autoSaveOnPageChange) {
			this.autoSaveOnPageChange = autoSaveOnPageChange;
		}
		
	}
	
}
