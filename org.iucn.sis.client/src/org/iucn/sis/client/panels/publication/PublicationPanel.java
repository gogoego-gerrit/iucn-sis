package org.iucn.sis.client.panels.publication;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.PublicationCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.publication.PublicationBatchChange.BatchUpdateEvent;
import org.iucn.sis.client.panels.utils.TaxonomyBrowserPanel;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.BorderLayoutEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class PublicationPanel extends LayoutContainer implements DrawsLazily {
	
	private final PublicationGrid grid;
	private final PublicationBatchChange form;
	private final FilteringTaxonomyBrowserPanel browser;
	
	private final BorderLayout layout;
	
	private boolean drawn = false;
	
	public PublicationPanel() {
		super();
		setLayout(new FillLayout());
		
		grid = new PublicationGrid();
		form = new PublicationBatchChange();
		browser = new FilteringTaxonomyBrowserPanel(new ComplexListener<Taxon>() {
			public void handleEvent(Taxon eventData) {
				grid.filterByTaxon(eventData);
			}
		});
		
		layout = new BorderLayout();
		layout.addListener(Events.Expand, new Listener<BorderLayoutEvent>() {
			public void handleEvent(BorderLayoutEvent be) {
				form.update();
				grid.showCheckbox();
			}
		});
		layout.addListener(Events.Collapse, new Listener<BorderLayoutEvent>() {
			public void handleEvent(BorderLayoutEvent be) {
				grid.hideCheckbox();
			}
		});
		
		form.addListener(Events.CancelEdit, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				layout.collapse(LayoutRegion.NORTH);
			}
		});
		form.addListener(Events.BeforeEdit, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				be.setCancelled(grid.getChecked().isEmpty());
			}
		});
		form.addListener(Events.StartEdit, new Listener<PublicationBatchChange.BatchUpdateEvent>() {
			public void handleEvent(BatchUpdateEvent be) {
				batchUpdate(be, new GenericCallback<Object>() {
					public void onFailure(Throwable caught) {
						//Already handled...
					}
					public void onSuccess(Object result) {
						Info.display("Success", "Changes made successfully, refreshing...");
						grid.refresh();
					}
				});
			}
		});
	}
	
	public void batchUpdate(PublicationBatchChange.BatchUpdateEvent event, GenericCallback<Object> callback) {
		List<PublicationModelData> checked = grid.getChecked();
		if (checked.isEmpty()) {
			WindowUtils.errorAlert("Please select at least one row.");
			return;
		}
		
		List<Integer> ids = new ArrayList<Integer>();
		for (PublicationModelData model : checked)
			ids.add(model.getModel().getId());
		
		PublicationCache.impl.updateData(event.getStatus(), 
				event.getTargetGoal(), event.getTargetApproved(), 
				event.getNotes(), event.getPriority(), ids, callback);
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		browser.update();
		grid.draw(new DoneDrawingCallback() {
			public void isDrawn() {
				if (!drawn) {
					drawn = true;
				
					final LayoutContainer formGrid;
					if (canBatchChange()) {
						int size = 250;
						
						final BorderLayoutData top = new BorderLayoutData(LayoutRegion.NORTH, size, size, size);
						top.setFloatable(false);
						top.setCollapsible(true);
						top.setSplit(false);
						
						formGrid = new LayoutContainer(layout);
						formGrid.add(form, top);
						formGrid.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
						
						layout.collapse(LayoutRegion.NORTH);
					}
					else {
						formGrid = new LayoutContainer(new FillLayout());
						formGrid.add(grid);
						
						grid.hideCheckbox();
					}
						
					
					final BorderLayoutData left = new BorderLayoutData(LayoutRegion.WEST, 250, 250, 250);
					left.setFloatable(true);
					left.setCollapsible(true);
					left.setSplit(false);
					
					final ContentPanel browserPanel = new ContentPanel();
					browserPanel.setLayout(new FillLayout());
					browserPanel.setHeading("Filter by Taxonomy");
					browserPanel.add(browser);
					
					final LayoutContainer container = new LayoutContainer(new BorderLayout());
					container.add(browserPanel, left);
					container.add(formGrid, new BorderLayoutData(LayoutRegion.CENTER));
					
					((BorderLayout)container.getLayout()).collapse(LayoutRegion.WEST);
					
					add(container);
				}
				callback.isDrawn();
			}
		});
	}
	
	private boolean canBatchChange() {
		return AuthorizationCache.impl.canUse(AuthorizableFeature.PUBLICATION_MANAGER_EDITING_FEATURE);
	}
	
	private static class FilteringTaxonomyBrowserPanel extends TaxonomyBrowserPanel {
		
		private final ComplexListener<Taxon> listener;
		
		public FilteringTaxonomyBrowserPanel(ComplexListener<Taxon> listener) {
			super();
			this.listener = listener;
		}
		
		protected void addViewButtonToFootprint() {
			footprintPanel.add(new Button("Filter", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					TaxonomyCache.impl.fetchTaxon(Integer.valueOf(footprints[footprints.length - 1]), true, new GenericCallback<Taxon>() {
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Failed to fetch taxon, please try again later.");
						}
						public void onSuccess(Taxon result) {
							listener.handleEvent(result);
						}
					});
				}
			}));
		}
		
	}

}
