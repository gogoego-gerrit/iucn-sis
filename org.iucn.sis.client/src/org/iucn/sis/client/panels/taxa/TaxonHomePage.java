package org.iucn.sis.client.panels.taxa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.ui.notes.NoteAPI;
import org.iucn.sis.client.api.ui.notes.NotesWindow;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.Timer;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class TaxonHomePage extends LayoutContainer {
	
	private Taxon taxon;
	
	public TaxonHomePage() {
		super();
		
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
		SimpleSISClient.getInstance().onShowReferenceEditor(
			"Manage References for " + taxon.getFullName(), 
			new ReferenceableTaxon(taxon, new SimpleListener() {
				public void handleEvent() {
					update(taxon.getId());
				}
			}), 
			null, null
		);
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
		
		final TaxonDescriptionPanel inner = new TaxonDescriptionPanel(taxon);
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
	
	public static class ReferenceableTaxon implements Referenceable {
		
		private final Taxon taxon;
		private final SimpleListener afterChangeListener;
		
		public ReferenceableTaxon(Taxon taxon, SimpleListener afterChangeListener) {
			this.taxon = taxon;
			this.afterChangeListener = afterChangeListener;
		}
		
		public void addReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
			taxon.getReference().addAll(references);
			persist(callback);
		}
		
		public Set<Reference> getReferencesAsList() {
			return new HashSet<Reference>(taxon.getReference());
		}

		public void onReferenceChanged(GenericCallback<Object> callback) {

		}

		public void removeReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
			taxon.getReference().removeAll(references);
			persist(callback);
		}
		
		private void persist(final GenericCallback<Object> callback) {
			TaxonomyCache.impl.saveReferences(taxon, new GenericCallback<String>() {
				public void onSuccess(String result) {
					if (afterChangeListener != null)
						afterChangeListener.handleEvent();
					
					callback.onSuccess(result);
				}
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
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
