package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.Set;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
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

	@SuppressWarnings("deprecation")
	protected Widget getDeleteWidget() {
		Image removeImage = new Image("images/icon-note-delete.png");
		removeImage.setPixelSize(14, 14);
		removeImage.setTitle("Remove this common name");
		removeImage.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				if (com.google.gwt.user.client.Window.confirm("Really remove this common name?")) {
					cn.setChangeReason(CommonName.DELETED);
					cn.setValidated(false);
					TaxonomyCache.impl.addOrEditCommonName(taxon, cn, new GenericCallback<String>() {

						@Override
						public void onSuccess(String result) {
							WindowUtils.infoAlert("Successful delete of common name " + cn.getName());
							ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(cn
									.getTaxon().getId());
						}

						@Override
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Common name was unable to be deleted");
							
						}
					});
				}
			}
		});
		return removeImage;
	}

	@SuppressWarnings("deprecation")
	protected Widget getEditWidget() {
		Image editImage = new Image("images/icon-note-edit.png");
		editImage.setPixelSize(14, 14);
		editImage.setTitle("Edit this common name");
		editImage.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				WindowManager.get().hideAll();
				Window temp = new EditCommonNamePanel(cn, taxon, new GenericCallback<CommonName>() {

					@Override
					public void onSuccess(CommonName result) {
						ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(taxon.getId());
					}

					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub

					}
				});
				temp.setSize(550, 250);
				temp.show();
				temp.center();
			}
		});
		return editImage;
	}

	@SuppressWarnings("deprecation")
	protected Widget getReferenceWidget() {
		Image referenceImage = new Image("images/icon-book.png");
		if (cn.getReference().size() == 0)
			referenceImage.setUrl("images/icon-book-grey.png");
		referenceImage.setPixelSize(14, 14);
		referenceImage.setTitle("Add/Remove References");
		referenceImage.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				final Window s = WindowUtils.getWindow(true, true, "Add a references to Common Name" + cn.getName());
				s.setIconStyle("icon-book");
				s.setLayout(new FillLayout());
				
				ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel.setReferences(CommonNameToolPanel.this);
				s.add(ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel);

				s.setSize(850, 550);
				s.show();
				s.center();
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

	@SuppressWarnings("deprecation")
	public void buildNotePopup(final Image notesImage) {
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
					Notes newNote = new Notes();
					newNote.setValue(area.getText());
					s.hide();
					TaxonomyCache.impl.addNoteToCommonName(taxon, cn, newNote, new GenericCallback<String>() {

						@Override
						public void onSuccess(String result) {
							WindowUtils.infoAlert("Saved", "Common name " + cn.getName() + " was saved.");
							notesImage.setUrl("images/icon-note.png");
							ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(taxon.getId());
						}

						@Override
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Error",
									"An error occurred when trying to save the common name data related to "
											+ cn.getTaxon().getFullName() + ".");
						}
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

		if (cn.getNotes().isEmpty()) {
			container
					.add(new HTML(
							"<div style='padding-top:10px';background-color:grey><b>There are no notes for this field.</b></div>"));
			container.add(panelAdd);
		} else {

			final ContentPanel eBar = new ContentPanel();
			eBar.setHeight(200);
			RowLayout layout = new RowLayout(Orientation.VERTICAL);
			eBar.setLayout(layout);
			eBar.setLayoutOnChange(true);

			for (final Notes current : cn.getNotes()) {
				Image deleteNote = new Image("images/icon-note-delete.png");
				final HorizontalPanel alignCorrectly = new HorizontalPanel();
				deleteNote.setTitle("Delete Note");
				deleteNote.addClickListener(new ClickListener() {
					public void onClick(Widget sender) {
						TaxonomyCache.impl.deleteNoteOnCommonNames(taxon, cn, current, new GenericCallback<String>() {

							@Override
							public void onSuccess(String result) {
								eBar.remove(alignCorrectly);
								
							}

							@Override
							public void onFailure(Throwable caught) {
								// TODO Auto-generated method stub
								
							}
						});
					}
				});
				
				HorizontalPanel a = new HorizontalPanel();
				alignCorrectly.add(a);
				
				// innerLayout.setSpacing(10);
				a.setHorizontalAlignment(ALIGN_LEFT);
				alignCorrectly.setWidth("400px");
				a.add(deleteNote);
				a.setCellHorizontalAlignment(deleteNote, ALIGN_LEFT);
				HTML html = new HTML(current.getValue());
				a.add(html);
				a.setCellHorizontalAlignment(html, ALIGN_LEFT);
				// a.add(new HTML("<b>" + current. + " [" + current.getDate() +
				// "]</b>  --"
				// + current.getBody()), new RowData(1d, 1d));// );

				eBar.add(alignCorrectly, new RowData(1d, -1d));
			}
			container.add(eBar);
			container.add(panelAdd);
		}

		s.setSize(500, 400);
		s.show();
		s.center();

	}

	@Override
	public void addReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
		TaxonomyCache.impl.addReferencesToCommonNam(taxon, cn, references, callback);
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
		TaxonomyCache.impl.removeReferencesToCommonNam(taxon, cn, references, listener);
	}
	

}
