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

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class CommonNameToolPanel extends Menu implements Referenceable {

	private final CommonName cn;
	private final Taxon taxon;

	public CommonNameToolPanel(CommonName commonName, Taxon taxon) {
		this.cn = commonName;
		this.taxon = taxon;
		
		add(getDeleteWidget());
		add(getEditWidget());
		add(getReferenceWidget());
		add(getNotesWiget());
	}

	private MenuItem getDeleteWidget() {
		MenuItem removeImage = new MenuItem();
		removeImage.setText("Remove this common name");
		removeImage.setIconStyle("icon-note-delete");
		removeImage.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				WindowUtils.confirmAlert("Confirm", "Are you sure you want to remove this common name?", new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						WindowUtils.showLoadingAlert("Deleting common name...");
						cn.setChangeReason(CommonName.DELETED);
						cn.setValidated(false);
						TaxonomyCache.impl.editCommonName(taxon, cn, new GenericCallback<String>() {
							public void onSuccess(String result) {
								WindowUtils.hideLoadingAlert();
								WindowUtils.infoAlert("Successful delete of common name " + cn.getName());
								ClientUIContainer.bodyContainer.refreshBody();
							}

							@Override
							public void onFailure(Throwable caught) {
								WindowUtils.hideLoadingAlert();
								WindowUtils.errorAlert("Common name was unable to be deleted");
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
		editImage.setText("Edit this common name");
		editImage.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				WindowManager.get().hideAll();
				final NewCommonNameEditor editor = new NewCommonNameEditor();
				editor.draw(new DrawsLazily.DoneDrawingCallback() {
					public void isDrawn() {
						editor.setCommonName(cn);
						editor.show();
					}
				});
				/*Window temp = new EditCommonNamePanel(cn, taxon, new ComplexListener<CommonName>() {
					public void handleEvent(CommonName eventData) {
						ClientUIContainer.bodyContainer.refreshTaxonPage();
					}
				});
				temp.show();*/
			}
		});
		return editImage;
	}

	private MenuItem getReferenceWidget() {
		String icon = cn.getReference().isEmpty() ? "icon-book-grey" : "icon-book";
		
		MenuItem referenceImage = new MenuItem();
		referenceImage.setIconStyle(icon);
		referenceImage.setText("Add/Remove References");
		referenceImage.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				ClientUIContainer.bodyContainer.openReferenceManager(
					CommonNameToolPanel.this, "Add a references to Common Name " + cn.getName());
			}
		});
		return referenceImage;
	}

	private MenuItem getNotesWiget() {
		String icon = cn.getNotes().isEmpty() ? "icon-note-grey" : "icon-note"; 
		MenuItem notesImage = new MenuItem();
		notesImage.setIconStyle(icon);
		notesImage.setText("Add/Remove Notes");
		notesImage.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				NotesWindow window = new NotesWindow(new CommonNameNoteAPI(taxon, cn));
				window.show();
			}
		});

		return notesImage;
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
		TaxonomyCache.impl.removeReferencesFromCommonName(taxon, cn, references, listener);
	}
	
	@Override
	public ReferenceGroup groupBy() {
		return ReferenceGroup.CommonName;
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
