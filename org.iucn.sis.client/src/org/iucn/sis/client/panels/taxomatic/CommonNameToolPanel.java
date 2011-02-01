package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.ui.notes.NoteAPI;
import org.iucn.sis.client.api.ui.notes.NotesWindow;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;

public class CommonNameToolPanel extends HorizontalPanel implements Referenceable {

	protected final CommonName cn;
	protected final Taxon taxon;

	public CommonNameToolPanel(CommonName commonName, Taxon taxon) {
		cn = commonName;
		this.taxon = taxon;
		draw();
	}

	protected void draw() {
		add(getDeleteWidget());
		add(getEditWidget());
		add(getReferenceWidget());
		add(getNotesWiget());
	}

	protected Widget getDeleteWidget() {
		Image removeImage = new Image("images/icon-note-delete.png");
		removeImage.setPixelSize(14, 14);
		removeImage.setTitle("Remove this common name");
		removeImage.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				WindowUtils.confirmAlert("Confirm", "Are you sure you want to remove this common name?", new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						cn.setChangeReason(CommonName.DELETED);
						cn.setValidated(false);
						TaxonomyCache.impl.editCommonName(taxon, cn, new GenericCallback<String>() {
							public void onSuccess(String result) {
								WindowUtils.infoAlert("Successful delete of common name " + cn.getName());
								//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(taxon.getId());
							}

							@Override
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Common name was unable to be deleted");
								
							}
						});
					}
				});
			}
		});
		return removeImage;
	}

	protected Widget getEditWidget() {
		Image editImage = new Image("images/icon-note-edit.png");
		editImage.setPixelSize(14, 14);
		editImage.setTitle("Edit this common name");
		editImage.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				WindowManager.get().hideAll();
				Window temp = new EditCommonNamePanel(cn, taxon, new ComplexListener<CommonName>() {
					public void handleEvent(CommonName eventData) {
						//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(taxon.getId());
					}
				});
				temp.show();
			}
		});
		return editImage;
	}

	protected Widget getReferenceWidget() {
		Image referenceImage = new Image("images/icon-book.png");
		if (cn.getReference().size() == 0)
			referenceImage.setUrl("images/icon-book-grey.png");
		referenceImage.setPixelSize(14, 14);
		referenceImage.setTitle("Add/Remove References");
		referenceImage.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ClientUIContainer.bodyContainer.openReferenceManager(
					CommonNameToolPanel.this, "Add a references to Common Name" + cn.getName());
			}
		});
		return referenceImage;
	}

	protected Widget getNotesWiget() {
		final Image notesImage = new Image("images/icon-note.png");
		if (cn.getNotes().isEmpty())
			notesImage.setUrl("images/icon-note-grey.png");
		notesImage.setTitle("Add/Remove Notes");
		notesImage.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				buildNotePopup(notesImage);
				
			}
		});

		return notesImage;
	}

	public void buildNotePopup(final Image notesImage) {
		NotesWindow window = new NotesWindow(new CommonNameNoteAPI(taxon, cn));
		window.show();
	}

	@Override
	public void addReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
		TaxonomyCache.impl.addReferencesToCommonName(taxon, cn, references, callback);
	}

	@Override
	public Set<Reference> getReferencesAsList() {
		return cn.getReference();
	}

	@Override
	public void onReferenceChanged(GenericCallback<Object> callback) {
		// NOT NECESSARY I DON"T THINK
	}

	@Override
	public void removeReferences(ArrayList<Reference> references, GenericCallback<Object> listener) {
		TaxonomyCache.impl.removeReferencesToCommonName(taxon, cn, references, listener);
	}
	
	public static class CommonNameNoteAPI implements NoteAPI {
		
		private final CommonName commonName;
		private final Taxon taxon;
		
		private boolean hasChanged;
		
		public CommonNameNoteAPI(Taxon taxon, CommonName commonName) {
			this.taxon = taxon;
			this.commonName = commonName;
			
			hasChanged = false;
		}
		
		@Override
		public void addNote(Notes note, final GenericCallback<Object> callback) {
			TaxonomyCache.impl.addNoteToCommonName(taxon, commonName, note, new GenericCallback<String>() {
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
			TaxonomyCache.impl.deleteNoteOnCommonNames(taxon, commonName, note, new GenericCallback<String>() {
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
			listener.handleEvent(commonName.getNotes());
		}
		
	}
	

}
