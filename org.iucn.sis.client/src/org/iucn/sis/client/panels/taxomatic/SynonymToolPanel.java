package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.ui.notes.NoteAPI;
import org.iucn.sis.client.api.ui.notes.NotesWindow;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;

public class SynonymToolPanel extends Menu implements Referenceable {
	
	private final Synonym synonym;
	private final Taxon taxon;
	
	public SynonymToolPanel(Synonym synonym, Taxon taxon) {
		this.synonym = synonym;
		this.taxon = taxon;
		
		add(getDeleteWidget());
		add(getEditWidget());
		add(getReferenceWidget());
		add(getNotesWiget());
	}
	
	private MenuItem getDeleteWidget() {
		MenuItem removeImage = new MenuItem();
		removeImage.setText("Remove this synonym");
		removeImage.setIconStyle("icon-note-delete");
		removeImage.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				WindowUtils.confirmAlert("Confirm", "Are you sure you want to remove this synonym?", new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						TaxonomyCache.impl.deleteSynonymn(taxon, synonym, new GenericCallback<String>() {
							public void onSuccess(String result) {
								WindowUtils.infoAlert("Successful delete of synonym " + synonym.getName());
								ClientUIContainer.bodyContainer.refreshBody();
							}
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Synonym was unable to be deleted");
							}
						});
					}
				});
			}
		});
		return removeImage;
	}
	
	private MenuItem getEditWidget() {
		MenuItem editImage = new MenuItem();
		editImage.setIconStyle("icon-note-edit");
		editImage.setText("Edit this synonym");
		editImage.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				NewTaxonSynonymEditor editor = new NewTaxonSynonymEditor();
				editor.setSynonym(synonym);
				editor.show();
			}
		});
		return editImage;
	}
	
	private MenuItem getReferenceWidget() {
		String icon = synonym.getReference().isEmpty() ? "icon-book-grey" : "icon-book";
		
		MenuItem referenceImage = new MenuItem();
		referenceImage.setIconStyle(icon);
		referenceImage.setText("Add/Remove References");
		referenceImage.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				ClientUIContainer.bodyContainer.openReferenceManager(
					SynonymToolPanel.this, "Add a references to Synonym " + synonym.getFriendlyName());
			}
		});
		return referenceImage;
	}
	
	private MenuItem getNotesWiget() {
		String icon = synonym.getNotes().isEmpty() ? "icon-note-grey" : "icon-note"; 
		MenuItem notesImage = new MenuItem();
		notesImage.setIconStyle(icon);
		notesImage.setText("Add/Remove Notes");
		notesImage.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				NotesWindow window = new NotesWindow(new SynonymNoteAPI(taxon, synonym));
				window.show();
			}
		});

		return notesImage;
	}
	
	@Override
	public void addReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
		TaxonomyCache.impl.addReferencesToSynonym(taxon, synonym, references, callback);
	}
	
	public Set<Reference> getReferencesAsList() {
		return synonym.getReference();
	}
	
	@Override
	public void onReferenceChanged(GenericCallback<Object> callback) {
	}
	
	@Override
	public void removeReferences(ArrayList<Reference> references, GenericCallback<Object> listener) {
		TaxonomyCache.impl.removeReferencesFromSynonym(taxon, synonym, references, listener);
	}
	
	@Override
	public ReferenceGroup groupBy() {
		return ReferenceGroup.Synonym;
	}
	
	public static class SynonymNoteAPI implements NoteAPI {
		
		private final Synonym synonym;
		private final Taxon taxon;
		
		private boolean hasChanged;
		
		public SynonymNoteAPI(Taxon taxon, Synonym synonym) {
			this.taxon = taxon;
			this.synonym = synonym;
			
			hasChanged = false;
		}
		
		@Override
		public void addNote(Notes note, final GenericCallback<Object> callback) {
			TaxonomyCache.impl.addNoteToSynonym(taxon, synonym, note, new GenericCallback<String>() {
				public void onSuccess(String result) {
					hasChanged = true;
					callback.onSuccess(result);
				}
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
		
		@Override
		public void deleteNote(Notes note, final GenericCallback<Object> callback) {
			TaxonomyCache.impl.deleteNoteOnSynonym(taxon, synonym, note, new GenericCallback<String>() {
				public void onSuccess(String result) {
					hasChanged = true;
					callback.onSuccess(result);
				}
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
		
		@Override
		public void onClose() {
			if (hasChanged)
				//TaxonomyCache.impl.setCurrentTaxon(taxon);
				ClientUIContainer.bodyContainer.refreshTaxonPage();
				//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(taxon.getId());
		}
		
		@Override
		public void loadNotes(ComplexListener<Collection<Notes>> listener) {
			listener.handleEvent(synonym.getNotes());
		}
		
	}

}
