package org.iucn.sis.client.panels.taxa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.ui.models.image.ManagedImage;
import org.iucn.sis.client.api.ui.models.taxa.TaxonListElement;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.api.utils.TaxonPagingLoader;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.images.ImageManagerPanel;
import org.iucn.sis.client.panels.taxomatic.CommonNameToolPanel;
import org.iucn.sis.client.panels.taxomatic.EditCommonNamePanel;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.utils.AssessmentFormatter;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.iucn.sis.shared.api.utils.XMLUtils;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binder.DataListBinder;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class TaxonHomePage extends LayoutContainer {

	private final PanelManager panelManager;
	
	private Taxon taxon;
	
	public TaxonHomePage(PanelManager manager) {
		super();
		this.panelManager = manager;
		
		setLayoutOnChange(true);
		setScrollMode(Scroll.AUTO);
		
		addStyleName("gwt-background");
	}

	public void buildNotePopup() {
		final Window s = WindowUtils.getWindow(false, false, "Notes for " + taxon.getFullName());
		final LayoutContainer container = s;
		container.setLayoutOnChange(true);
		final VerticalPanel panelAdd = new VerticalPanel();
		panelAdd.setSpacing(3);
		panelAdd.add(new HTML("Add Note: "));

		final TextArea area = new TextArea();
		area.setSize("400", "75");
		panelAdd.add(area);

		Button save = new Button("Add Note");
		save.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (area.getText().trim().equalsIgnoreCase("")) {
					WindowUtils.errorAlert("Must enter note body.");

				} else {
					final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
					String url = UriBase.getInstance().getNotesBase() + "/notes/taxon/"+ taxon.getId();
					
					doc.post(url, area.getText().trim(), new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Unable to create note.");							
						};

						public void onSuccess(String result) {
							Notes note = Notes.fromXML(doc.getDocumentElement());
							taxon.getNotes().add(note);
						};
					});

					s.hide();
				}
			}
		});
		Button close = new Button("Close");
		close.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				s.hide();
			}
		});

		panelAdd.add(save);
		panelAdd.add(close);
		
		if (taxon == null || taxon.getNotes().isEmpty()) {
			container.add(new HTML("<div style='padding-top:10px';background-color:grey><b>There are no notes for this taxon.</b></div>"));
			container.add(panelAdd);
			s.setSize(500, 400);
			s.show();
			s.center();
		} else {
			
			final VerticalPanel eBar = new VerticalPanel();
			eBar.setSize("400", "200");

			
			
			for (final Notes note : TaxonomyCache.impl.getCurrentTaxon().getNotes()) {
				final HorizontalPanel a = new HorizontalPanel();
				Image deleteNote = new Image("images/icon-note-delete.png");
				deleteNote.setTitle("Delete Note");
				deleteNote.addClickHandler(new ClickHandler() {
					
					public void onClick(ClickEvent event) {
						NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
						String url = UriBase.getInstance().getNotesBase() + "/notes/note/" + note.getId();

						doc.post(url + "?option=remove", note.toXML(), new GenericCallback<String>() {
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Unable to delete note.");
							};

							public void onSuccess(String result) {
								taxon.getNotes().remove(note);
								eBar.remove(a);
							};
						});

					}
				});
				
				a.setWidth("100%");
				a.add(deleteNote);
				a.add(new HTML("<b>" + note.getEdit().getUser().getDisplayableName() + " ["
						+ FormattedDate.impl.getDate(note.getEdit().getCreatedDate()) + "]</b>  --"
						+ note.getValue()));// );
				eBar.add(a);		
			}
			
			container.add(eBar);
			container.add(panelAdd);
			s.setSize(500, 400);
			s.show();
			s.center();
		}
	}

	public void buildReferencePopup() {
		final Window s = WindowUtils.getWindow(false, false, "Add a references to " + taxon.getFullName());
		s.setIconStyle("icon-book");
		LayoutContainer container = s;
		container.setLayout(new FillLayout());

		ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel.setReferences(taxon);

		container.add(ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel);

		s.setSize(850, 550);
		s.show();
		s.center();
	}

	/*public void onResize(int width, int height) {
		super.onResize(width, height);
		inner.resize(width, height);
	}*/

	public void update(final Integer nodeID) {
		WindowUtils.showLoadingAlert("Loading...");

		Timer timer = new Timer() {
			public void run() {
				updateDelayed(nodeID);

			}
		};
		timer.schedule(150);

	}

	private void updateDelayed(final Integer nodeID) {
		removeAll();

		if (nodeID == null) {
			final TaxonDescriptionPanel inner = new TaxonDescriptionPanel(panelManager, null);
			inner.setUpdateListener(new ComplexListener<Integer>() {
				public void handleEvent(Integer eventData) {
					update(eventData);
				}
			});
			inner.addListener(Events.Resize, new Listener<BoxComponentEvent>() {
				public void handleEvent(BoxComponentEvent be) {
					inner.resize(be.getWidth(), be.getHeight());
				}
			});
			inner.updatePanel(new DrawsLazily.DoneDrawingCallback() {
				public void isDrawn() {
					add(inner);
					layout();
					WindowUtils.hideLoadingAlert();
				}
			});
		}
		else {
			boolean resetAsCurrent = TaxonomyCache.impl.getCurrentTaxon() == null ? true : !nodeID.equals(Integer
					.valueOf(TaxonomyCache.impl.getCurrentTaxon().getId()));
			TaxonomyCache.impl.fetchTaxon(nodeID, resetAsCurrent, new GenericCallback<Taxon>() {
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Taxon ID " + nodeID + " does not exist.");
					
					/*final TaxonDescriptionPanel inner = new TaxonDescriptionPanel(panelManager, null);
					inner.updatePanel(new DrawsLazily.DoneDrawingCallback() {
						public void isDrawn() {
							add(inner);
							layout();
							WindowUtils.hideLoadingAlert();
						}
					});*/
				}

				public void onSuccess(Taxon taxon) {
					TaxonHomePage.this.taxon = taxon;
					if (ClientUIContainer.bodyContainer.getSelectedItem().equals(
							ClientUIContainer.bodyContainer.tabManager.taxonHomePage)) {
						ClientUIContainer.headerContainer.update();
						final TaxonDescriptionPanel inner = new TaxonDescriptionPanel(panelManager, taxon);
						inner.addListener(Events.Resize, new Listener<BoxComponentEvent>() {
							public void handleEvent(BoxComponentEvent be) {
								inner.resize(be.getWidth(), be.getHeight());
							}
						});
						inner.setUpdateListener(new ComplexListener<Integer>() {
							public void handleEvent(Integer eventData) {
								update(eventData);
							}
						});
						inner.updatePanel(new DrawsLazily.DoneDrawingCallback() {
							public void isDrawn() {
								add(inner);
								layout();
								WindowUtils.hideLoadingAlert();
							}
						});
					}
				}
			});
		}
	}
}
