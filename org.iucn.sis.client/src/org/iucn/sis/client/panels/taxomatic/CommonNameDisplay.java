package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.LanguageCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.ui.notes.NotesWindow;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.IsoLanguage;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;

public class CommonNameDisplay implements Referenceable {

	static class AddCommonNameClickListener extends SelectionListener<ButtonEvent> {
		TextBox nameBox;
		ListBox isoBox;
		CheckBox isPrimary;
		Taxon  node;
		CommonName commonName;
		GenericCallback<String> callback;

		public AddCommonNameClickListener(TextBox name, ListBox iso, CheckBox isPrimary, Taxon  node,
				CommonName cName, GenericCallback<String> callback) {
			this.nameBox = name;
			this.isoBox = iso;
			this.isPrimary = isPrimary;
			this.node = node;
			this.commonName = cName;
			this.callback = callback;
		}

		@Override
		public void componentSelected(ButtonEvent ce) {
			if (nameBox.getText() != null && !nameBox.getText().equalsIgnoreCase("")) {
				String name = nameBox.getText();
				String language = getLanguage();
				String iso = getIsoCode();
				boolean primary = isPrimary.getValue();

				if (commonName != null) {
					if (!commonName.getName().equals(name))
						commonName.setName(name);
					if (!commonName.getLanguage().equals(language))
						commonName.setLanguage(language);
					if (!commonName.isPrimary() == primary) {
						commonName.setPrincipal(primary);
					}
					if (!commonName.getIsoCode().equals(iso))
						commonName.setIsoCode(iso);

				} else {
					commonName = CommonName.createCommonName(name, language, iso, primary);
					commonName.setChangeReason(CommonName.ADDED);
					node.getCommonNames().add(commonName);
				}

				TaxonomyCache.impl.saveTaxonAndMakeCurrent(node, new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						ClientUIContainer.headerContainer.update();
						callback.onFailure(null);
						destroy();
					};

					public void onSuccess(String result) {
						ClientUIContainer.headerContainer.update();
						callback.onSuccess(null);
						destroy();
					};
				});

			} else {
				WindowUtils.errorAlert("The name field cannot be blank!");
			}
		}

		private String getIsoCode() {
			String itemText = isoBox.getItemText(isoBox.getSelectedIndex());
			return itemText.substring(itemText.indexOf("| ") + 2, itemText.length());

			// return isoBox.getValue(isoBox.getSelectedIndex());
		}

		private String getLanguage() {
			String itemText = isoBox.getItemText(isoBox.getSelectedIndex());
			return itemText.substring(0, itemText.indexOf(" |"));

			// return isoBox.getItemText(isoBox.getSelectedIndex());

		}
	}

	private static void destroy() {
		panel.hide();
		panel.removeFromParent();
		panel = null;
	}

	public static Window getNewCommonNameDisplay(Taxon  node, final CommonName cName,
			GenericCallback<String> callback) {
		if (panel != null)
			return panel;

		panel = WindowUtils.getWindow(false, false, "");
		VerticalPanel contentPanel = new VerticalPanel();
		contentPanel.setSpacing(5);

		VerticalPanel leftPanel = new VerticalPanel();
		leftPanel.setSpacing(5);
		leftPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		VerticalPanel rightPanel = new VerticalPanel();
		rightPanel.setSpacing(5);
		rightPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		HorizontalPanel buttonPanel = new HorizontalPanel();
		if (cName == null)
			panel.setHeading("Add New Common Name");
		else
			panel.setHeading("Edit Common Name");

		TextBox nameBox = new TextBox();
		final ListBox isoBox = new ListBox();
		final CheckBox isPrimary = new CheckBox();
		if (node.getCommonNames().size() == 0) {
			isPrimary.setValue(true);
			isPrimary.setEnabled(false);
		}
		isPrimary.setText("Primary Name");

		final Button add = new Button();
		if (cName == null)
			add.setText("Add Name");
		else
			add.setText("Save");
		add.addSelectionListener(new AddCommonNameClickListener(nameBox, isoBox, isPrimary, node, cName, callback));
		add.setEnabled(false);

		Button cancel = new Button();
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				destroy();
			}
		});

		LanguageCache.impl.list(new ComplexListener<List<IsoLanguage>>() {
			public void handleEvent(List<IsoLanguage> eventData) {
				int index = 0;
				for (IsoLanguage language : eventData) {
					String lang = language.getName();
					String iso = language.getCode();

					isoBox.addItem(lang, iso);

					if (cName != null && iso.equals(cName.getIsoCode()))
						isoBox.setSelectedIndex(index);

					if (cName == null && lang.equals("English"))
						isoBox.setSelectedIndex(index);
					
					index++;
				}

				add.setEnabled(true);
			}
		});
		
		if (cName != null) {
			nameBox.setText(cName.getName());
			isPrimary.setValue(cName.isPrimary());
		}

		HTML nameLabel = new HTML("Name: ");
		HTML langLabel = new HTML("Language: ");

		leftPanel.add(nameLabel);
		rightPanel.add(nameBox);

		leftPanel.add(langLabel);
		rightPanel.add(isoBox);
		rightPanel.add(isPrimary);

		buttonPanel.add(add);
		buttonPanel.add(cancel);

		HorizontalPanel wrap = new HorizontalPanel();
		wrap.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		wrap.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		wrap.add(leftPanel);
		wrap.add(rightPanel);

		contentPanel.add(wrap);
		contentPanel.add(buttonPanel);

		panel.add(contentPanel);
		panel.setSize(700, 300);
		return panel;
	}

	Taxon  node;

	CommonName name;

	Image notesImage;

	private HorizontalPanel labelPanel = new HorizontalPanel();

	private static Window panel;

	public CommonNameDisplay(Taxon  theNode) {
		node = theNode;
		name = null;
	}

	public CommonNameDisplay(Taxon  theNode, CommonName theName) {
		node = theNode;
		name = theName;
	}

	public void addReferences(ArrayList<Reference> references, final GenericCallback<Object> callback) {
		if (name == null) {
			callback.onFailure(new Exception("No common name selected."));
			return;
		}

		int added = 0;
		for (int i = 0; i < references.size(); i++) {
			Reference current = references.get(i);
			if (name.getReference().add(current) )
				added++;
		}

		if (added > 0) {
			TaxonomyCache.impl.saveTaxonAndMakeCurrent(node, new GenericCallback<String>() {
			
				public void onSuccess(String result) {
					ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId());
					callback.onSuccess(result);
				}
			
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
	}

	public Set<Reference> getReferencesAsList() {
		return name.getReference();
	}

	public void onReferenceChanged(final GenericCallback<Object> callback) {
		TaxonomyCache.impl.saveTaxonAndMakeCurrent(node, new GenericCallback<String>() {
		
			@Override
			public void onSuccess(String result) {
				callback.onSuccess(result);		
			}
		
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	public void removeReferences(ArrayList<Reference> references, final GenericCallback<Object> callback) {
		if (name == null) {
			callback.onFailure(new Exception("No common name selected."));
			return;
		}

		int removed = 0;
		for (int i = 0; i < references.size(); i++)
			if (name.getReference().remove(references.get(i)))
				removed++;

		// TODO: Find out why the removed count isn't moving correctly...?
		// if (removed > 0)
		// {
		TaxonomyCache.impl.saveTaxonAndMakeCurrent(node, new GenericCallback<String>() {
			
			public void onSuccess(String result) {
				ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId());
				callback.onSuccess(result);
			}
		
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
		
		// }

	}

	public Widget show(final GenericCallback<String> callback) {
		Image removeImage = new Image("images/icon-note-delete.png");
		removeImage.setPixelSize(14, 14);
		removeImage.setTitle("Remove this common name");
		removeImage.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				WindowUtils.confirmAlert("Confirm", "Are you sure you want to remove this common name?", 
						new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						// node.getCommonNames().remove( name );
						name.setChangeReason(CommonName.DELETED);
						TaxonomyCache.impl.saveTaxonAndMakeCurrent(node, new GenericCallback<String>() {
							public void onFailure(Throwable caught) {
								callback.onFailure(caught);
							};
	
							public void onSuccess(String result) {
								callback.onSuccess(null);
							};
						});
					}
				});
			}
		});
		Image editImage = new Image("images/icon-note-edit.png");
		editImage.setPixelSize(14, 14);
		editImage.setTitle("Edit this common name");
		editImage.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				WindowManager.get().hideAll();
				Window temp = getNewCommonNameDisplay(node, name, callback);
				// temp.show();
				temp.show();
				temp.center();
			}
		});

		Image referenceImage = new Image("images/icon-book.png");
		if (name.getReference().size() == 0)
			referenceImage.setUrl("images/icon-book-grey.png");
		referenceImage.setPixelSize(14, 14);
		referenceImage.setTitle("Add/Remove References");
		referenceImage.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final Window s = WindowUtils.getWindow(true, true, "Add a references to Common Name" + name.getName());
				s.setIconStyle("icon-book");
				s.setLayout(new FillLayout());

				ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel
						.setReferences(CommonNameDisplay.this);

				s.add(ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel);

				s.setSize(850, 550);
				s.show();
				s.center();
			}
		});

		notesImage = new Image("images/icon-note.png");
		if (name.getNotes().size() == 0)
			notesImage.setUrl("images/icon-note-grey.png");
		notesImage.setTitle("Add/Remove Notes");
		notesImage.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final NotesWindow window = new NotesWindow(new CommonNameToolPanel.CommonNameNoteAPI(node, name));
				window.show();
			}
		});
		
		String display = "&nbsp;&nbsp;" + name.getName() + " --- "
				+ (name.getLanguage().equals("") ? 
						(name.getIso() != null ? name.getIsoCode() : "No iso") 
						: name.getLanguage());

		if (name.getChangeReason() != 0) {
			display += " -- " + CommonName.reasons[name.getChangeReason()];
		}
		HTML disp = new HTML(display);

		if (!CommonName.reasons[name.getChangeReason()].equals("DELETED")) {
			if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
				labelPanel.add(removeImage);
				labelPanel.add(editImage);
				labelPanel.add(referenceImage);
				labelPanel.add(notesImage);
			}
		} else {
			disp.setStyleName("deleted");
		}

		labelPanel.add(disp);

		return labelPanel;
	}

}
