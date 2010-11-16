package org.iucn.sis.client.panels.taxa;

import java.util.Collection;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.notes.NoteAPI;
import org.iucn.sis.client.panels.notes.NotesWindow;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.Timer;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

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
		final NotesWindow window = new NotesWindow(new TaxonNoteAPI(taxon));
		window.setHeading("Notes for " + taxon.getFullName());
		window.show();	
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
	
	private void redraw(final Taxon taxon) {
		this.taxon = taxon;
		
		final TaxonDescriptionPanel inner = new TaxonDescriptionPanel(panelManager, taxon);
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
				removeAll();
				add(inner);
				layout();
				WindowUtils.hideLoadingAlert();
			}
		});
	}

	private void updateDelayed(final Integer nodeID) {
		if (nodeID == null) {
			redraw(null);
		}
		else {
			final boolean resetAsCurrent = TaxonomyCache.impl.getCurrentTaxon() == null ? true : !nodeID.equals(Integer
					.valueOf(TaxonomyCache.impl.getCurrentTaxon().getId()));
			TaxonomyCache.impl.fetchTaxon(nodeID, resetAsCurrent, new GenericCallback<Taxon>() {
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Taxon ID " + nodeID + " does not exist.");
				}
				public void onSuccess(Taxon taxon) {
					if (!resetAsCurrent)
						redraw(taxon);
				}
			});
		}
	}
	
	private static class TaxonNoteAPI implements NoteAPI {
		
		private final Taxon taxon;
		
		public TaxonNoteAPI(Taxon taxon) {
			this.taxon = taxon;
		}
		
		@Override
		public void addNote(final Notes note, final GenericCallback<Object> callback) {
			final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
			String url = UriBase.getInstance().getNotesBase() + "/notes/taxon/"+ taxon.getId();
			
			doc.post(url, note.toXML(), new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);							
				};

				public void onSuccess(String result) {
					Notes note = Notes.fromXML(doc.getDocumentElement());
					taxon.getNotes().add(note);
					callback.onSuccess(result);
				};
			});
		}
		
		@Override
		public void deleteNote(final Notes note, final GenericCallback<Object> callback) {
			NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
			String url = UriBase.getInstance().getNotesBase() + "/notes/note/" + note.getId();

			doc.delete(url, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				};

				public void onSuccess(String result) {
					taxon.getNotes().remove(note);
					callback.onSuccess(result);
				};
			});
		}
		
		@Override
		public void loadNotes(ComplexListener<Collection<Notes>> listener) {
			listener.handleEvent(taxon.getNotes());
		}
		
		@Override
		public void onClose() {
			//Nothing to do
		}
		
	}
}
