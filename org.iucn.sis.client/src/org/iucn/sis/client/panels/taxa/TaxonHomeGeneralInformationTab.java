package org.iucn.sis.client.panels.taxa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.ui.notes.NoteAPI;
import org.iucn.sis.client.api.ui.notes.NotesWindow;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.assessments.SingleFieldEditorPanel;
import org.iucn.sis.client.panels.taxomatic.CommonNameToolPanel;
import org.iucn.sis.client.panels.taxomatic.EditCommonNamePanel;
import org.iucn.sis.client.tabs.TaxonHomePageTab.ReferenceableTaxon;
import org.iucn.sis.client.tabs.TaxonHomePageTab.TaxonNoteAPI;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.fields.ProxyField;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.iucn.sis.shared.api.utils.CommonNameComparator;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.portable.PortableAlphanumericComparator;
import com.solertium.util.portable.XMLWritingUtils;

public class TaxonHomeGeneralInformationTab extends LayoutContainer implements DrawsLazily {
	
	private static final int SECTION_LIST_LIMIT = 5;
	
	private static final String NO_DATA_STYLE = "";
	
	public TaxonHomeGeneralInformationTab() {
		super(new FillLayout());
		setLayoutOnChange(true);
	}
	
	@Override
	public void draw(DoneDrawingCallback callback) {
		removeAll();
		
		final Taxon node = TaxonomyCache.impl.getCurrentTaxon();
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(drawGeneralInformation(node), new BorderLayoutData(LayoutRegion.CENTER));
		container.add(drawCommonNamesAndSynonyms(node), new BorderLayoutData(LayoutRegion.EAST, .5f));
		
		add(container);
		
		callback.isDrawn();
	}
	
	private LayoutContainer drawGeneralInformation(final Taxon node) {
		final AccordionLayout layout = new AccordionLayout();
		
		final LayoutContainer container = new LayoutContainer();
		container.setLayout(layout);
		
		final ContentPanel overview = new ContentPanel(new FillLayout());
		overview.setHeading("Overview");
		overview.add(getOverviewInformation(node));
		
		container.add(overview);
		
		final ContentPanel taxonomicNotes = new ContentPanel(new FillLayout());
		taxonomicNotes.setHeading("Taxonomic Notes");
		taxonomicNotes.add(getTaxonomicNotesInformation(node));
		
		container.add(taxonomicNotes);
		
		layout.setActiveItem(overview);
		
		return container;
	}
	
	private LayoutContainer getTaxonomicNotesInformation(final Taxon node) {
		final ProxyField field = new ProxyField(node.getTaxonomicNotes());
		final String value = field.getTextPrimitiveField("value");
		
		final HtmlContainer html = new HtmlContainer();
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		if ("".equals(value))
			html.setHtml("<i>No taxonomic notes.</i>");
		else
			html.setHtml(value);
		
		container.add(html, new BorderLayoutData(LayoutRegion.CENTER));
		
		if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
			final ButtonBar bar = new ButtonBar();
			bar.setAlignment(HorizontalAlignment.CENTER);
			bar.add(new Button("Edit", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					final Field field = node.getTaxonomicNotes() != null? 
						node.getTaxonomicNotes() : 
						new Field(CanonicalNames.TaxonomicNotes, null);
					
					SingleFieldEditorPanel editor = new SingleFieldEditorPanel(field);
					editor.setSaveListener(new ComplexListener<Field>() {
						public void handleEvent(final Field eventData) {
							if (!eventData.hasData() && (eventData.getReference() == null || eventData.getReference().isEmpty()))
								node.setTaxonomicNotes(null);
							else
								node.setTaxonomicNotes(field);
							
							// TODO save to server...
							TaxonomyCache.impl.saveTaxon(node, new GenericCallback<String>() {
								public void onSuccess(String result) {
									ProxyField proxy = new ProxyField(eventData);
									String value = proxy.getTextPrimitiveField("value");
									if ("".equals(value))
										html.setHtml("<i>No taxonomic notes.</i>");
									else
										html.setHtml(value);
									
									Info.display("Success", "Changes saved.");
								}
								public void onFailure(Throwable caught) {
									WindowUtils.errorAlert("Could not save changes, please try again later.");
								}
							});
						}
					});
					editor.show();
				}
			}));
			
			container.add(bar, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		}
		
		return container;
	}
	
	private LayoutContainer getOverviewInformation(final Taxon node) {
		final TableData layout = new TableData();
		layout.setPadding(5);
		
		final LayoutContainer data = new LayoutContainer(new TableLayout(1));
		data.addStyleName("page_taxon_general");
		
		String url = "http://www.iucnredlist.org/apps/redlist/details/" + node.getId();
		String prefix = node.getTaxonLevel().getLevel() >= TaxonLevel.SPECIES ? "Full Name" : "Name";
		String tag = node.isDeprecated() ? "s" : "i";
		
		data.add(new Span(prefix + ":  " + XMLWritingUtils.writeTag(tag, node.getFullName()) + 
				"&nbsp;(<a target=\"blank\" href=\"" + url + "\">" + 
				node.getId() + "</a>)"), layout);
		
		data.add(new Span("Level: " + node.getDisplayableLevel()), layout);
		
		if (node.getParentName() != null) {
			HTML parentHTML = new Span("Parent:  <i>" + node.getParentName() + "</i>");
			data.add(parentHTML, layout);
		}
		if (node.getTaxonomicAuthority() != null && !node.getTaxonomicAuthority().equals("")) {
			data.add(new Span("Taxonomic Authority: " + node.getTaxonomicAuthority()), layout);
		}

		data.add(new Span("Status: " + node.getTaxonStatus().getName()), layout);
		data.add(new Span("Hybrid: " + (node.getHybrid() ? "Yes" : "No")), layout);
		
		return data;	
	}
	
	private LayoutContainer createSectionHeader(final String name) {
		return createSectionHeader(name, null);
	}
	
	private LayoutContainer createSectionHeader(final String name, final ComplexListener<IconButton> listener) {
		return createSectionHeader(name, listener, null);
	}
	
	private LayoutContainer createSectionHeader(final String name, final ComplexListener<IconButton> listener, final String styleName) {
		return createSectionHeader(name, listener, styleName, "icon-gear");
	}
	
	private LayoutContainer createSectionHeader(final String name, final ComplexListener<IconButton> listener, final String styleName, final String iconStyle) {
		final TableLayout layout = new TableLayout(2);
		layout.setCellPadding(2);
		layout.setCellSpacing(2);
		layout.setWidth("100%");
		
		final LayoutContainer container = new LayoutContainer(layout);
		container.setHeight(30);
		container.setStyleName(styleName == null ? "page_taxon_section_header" : styleName);
		container.add(new Span(name), new TableData(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
		
		if (listener != null) {
			final IconButton icon = new IconButton(iconStyle);
			icon.addSelectionListener(new SelectionListener<IconButtonEvent>() {
				public void componentSelected(IconButtonEvent ce) {
					listener.handleEvent(ce.getIconButton());
				}
			});			
			container.add(icon, new TableData(HorizontalAlignment.RIGHT, VerticalAlignment.MIDDLE));
		}
		
		return container;
	}
	
	private LayoutContainer drawCommonNamesAndSynonyms(final Taxon node) {
		LayoutContainer data = new LayoutContainer();
		data.setScrollMode(Scroll.AUTO);
		
		data.add(createSectionHeader("Synonyms (" + node.getSynonyms().size() + ")"));
		if (node.getSynonyms().isEmpty())
			data.add(createSectionHeader("<i>No Synonyms.</i>", null, NO_DATA_STYLE));
		else
			addSynonyms(node, data);
		
		ComplexListener<IconButton> commonNameListener = null;
		if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
			commonNameListener = new ComplexListener<IconButton>() {
				public void handleEvent(IconButton icon) {
					Window addNameBox = new EditCommonNamePanel(null, node, 
							new ComplexListener<CommonName>() {
						public void handleEvent(CommonName eventData) {
							//TaxonomyCache.impl.setCurrentTaxon(node);
							ClientUIContainer.bodyContainer.refreshBody();
						}
					});
					addNameBox.show();
				}
			};
		}
		
		data.add(createSectionHeader("Common Names (" + node.getCommonNames().size() + ")", commonNameListener, null, "icon-add"));
		
		if (node.getCommonNames().isEmpty())
			data.add(createSectionHeader("<i>No Common Names.</i>", null, NO_DATA_STYLE));
		else {
			addCommonNames(node, data);
		}
		
		final String referenceStyle = node.getReference().isEmpty() ? 
			"icon-book-grey" : "icon-book";
		data.add(createSectionHeader("Taxonomic Sources (" + node.getReference().size() + ")", new ComplexListener<IconButton>() {
			public void handleEvent(IconButton eventData) {
				SimpleSISClient.getInstance().onShowReferenceEditor(
					"Manage Taxonomic Sources for " + node.getFullName(), 
					new ReferenceableTaxon(node, new SimpleListener() {
						public void handleEvent() {
							ClientUIContainer.bodyContainer.refreshBody();
						}
					}), 
					null, null
				);
			}
		}, null, referenceStyle));
		
		if (node.getReference().isEmpty())
			data.add(createSectionHeader("<i>No Taxonomic Sources.</i>", null, NO_DATA_STYLE));
		else {
			int count = 0;
			for (Iterator<Reference> iter = node.getReference().iterator(); iter.hasNext() && count < SECTION_LIST_LIMIT; count++)
				data.add(createSectionHeader(iter.next().getCitation(), null, ""));
			
			if (node.getReference().size() > SECTION_LIST_LIMIT) {
				HTML viewAll = new HTML("View all...");
				viewAll.addStyleName("page_taxon_section_view_all");
				viewAll.addStyleName("SIS_HyperlinkLookAlike");
				viewAll.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						SimpleSISClient.getInstance().onShowReferenceEditor(
							"Manage Taxonomic Sources for " + node.getFullName(), 
							new ReferenceableTaxon(node, new SimpleListener() {
								public void handleEvent() {
									ClientUIContainer.bodyContainer.refreshBody();
								}
							}), 
							null, null
						);
					}
				});
				data.add(viewAll);
			}
		}
		
		final String notesStyle = node.getNotes().isEmpty() ? 
			"icon-note-grey" : "icon-note";
		data.add(createSectionHeader("Notes (" + node.getNotes().size() + ")", new ComplexListener<IconButton>() {
			public void handleEvent(IconButton eventData) {
				final NotesWindow window = new NotesWindow(new TaxonNoteAPI(node));
				window.setHeading("Notes for " + node.getFullName());
				window.show();	
			}
		}, null, notesStyle));
		
		if (node.getNotes().isEmpty())
			data.add(createSectionHeader("<i>No Notes.</i>", null, NO_DATA_STYLE));
		else {
			int count = 0;
			for (Iterator<Notes> iter = node.getNotes().iterator(); iter.hasNext() && count < SECTION_LIST_LIMIT; count++)
				data.add(createSectionHeader(NotesWindow.formatNote(iter.next()), null, ""));
			
			if (node.getNotes().size() > SECTION_LIST_LIMIT) {
				HTML viewAll = new HTML("View all...");
				viewAll.addStyleName("page_taxon_section_view_all");
				viewAll.addStyleName("SIS_HyperlinkLookAlike");
				viewAll.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						final NotesWindow window = new NotesWindow(new TaxonNoteAPI(node));
						window.setHeading("Notes for " + node.getFullName());
						window.show();
					}
				});
				data.add(viewAll);
			}
		}
		
		return data;
	}
	
	private void addSynonyms(final Taxon node, LayoutContainer data) {
		final List<Synonym> ordered = new ArrayList<Synonym>(node.getSynonyms());
		Collections.sort(ordered, new Comparator<Synonym>() {
			private final PortableAlphanumericComparator comparator = new PortableAlphanumericComparator();
			public int compare(Synonym o1, Synonym o2) {
				return comparator.compare(o1.toDisplayableString(), o2.toDisplayableString());
			}
		});
		
		int count = 0;
		for (Iterator<Synonym> iter = ordered.listIterator(); iter.hasNext() && count < SECTION_LIST_LIMIT; count++)
			data.add(getSynonymDisplay(iter.next(), node));
		
		if (node.getSynonyms().size() > SECTION_LIST_LIMIT) {
			HTML viewAll = new HTML("View all...");
			viewAll.addStyleName("page_taxon_section_view_all");
			viewAll.addStyleName("SIS_HyperlinkLookAlike");
			viewAll.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					final Window window = WindowUtils.newWindow("Synonyms", null, false, false);
					window.setSize(350, 350);
					window.setScrollMode(Scroll.AUTO);
					
					for (final Synonym current : ordered)
						window.add(getSynonymDisplay(current, node));

					window.show();
				}
			});
			data.add(viewAll);
		}
	}
	
	private Widget getSynonymDisplay(final Synonym synonym, final Taxon node) {
		String value = synonym.toDisplayableString();
		if (Synonym.ADDED.equals(synonym.getStatus()) || Synonym.DELETED.equals(synonym.getStatus()))
			value += "-- " + synonym.getStatus();
		
		if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
			String iconStyle = synonym.getNotes().isEmpty() ? "icon-note-grey" : "icon-note";
			return (createSectionHeader(value, new ComplexListener<IconButton>() {
				public void handleEvent(IconButton eventData) {
					final NotesWindow window = new NotesWindow(new SynonymNoteAPI(node, synonym));
					window.show();
				}
			}, "", iconStyle));
		}
		else
			return (createSectionHeader(value, null, null));
	}
	
	private Widget getCommonNameDisplay(final CommonName curName, final Taxon node) {
		StringBuilder display = new StringBuilder();
		if (curName.isPrimary())
			display.append("*&nbsp;");
		else
			display.append("&nbsp;&nbsp;");
		
		display.append(curName.getName());
		if (curName.getIso() != null) {
			display.append(" (" + curName.getLanguage() + ")");
		}
	
		String styleName = curName.getChangeReason() == CommonName.DELETED ? "deleted" : "";
		
		return createSectionHeader(display.toString(), new ComplexListener<IconButton>() {
			public void handleEvent(IconButton icon) {
				if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
					CommonNameToolPanel cntp = new CommonNameToolPanel(curName, node);
					cntp.show(icon);
				}	
			}
		}, styleName);
	}
	
	private void addCommonNames(final Taxon node, LayoutContainer data) {
		final ArrayList<CommonName> commonNames = new ArrayList<CommonName>(node.getCommonNames());
		Collections.sort(commonNames, new CommonNameComparator());
		
		int count = 0;
		for (Iterator<CommonName> iter = commonNames.listIterator(); iter.hasNext() && count < SECTION_LIST_LIMIT; count++)
			data.add(getCommonNameDisplay(iter.next(), node));
		
		if (commonNames.size() > SECTION_LIST_LIMIT) {
			HTML viewAll = new HTML("View all...");
			viewAll.addStyleName("page_taxon_section_view_all");
			viewAll.addStyleName("SIS_HyperlinkLookAlike");
			viewAll.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					final Window container = WindowUtils.newWindow("Edit Common Names", null, false, false);
					container.setScrollMode(Scroll.AUTO);
	
					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
						Button item = new Button();
						item.setText("New Common Name");
						item.setIconStyle("icon-add");
						item.addSelectionListener(new SelectionListener<ButtonEvent>() {
							public void componentSelected(ButtonEvent ce) {
								container.hide();
								Window addNameBox = new EditCommonNamePanel(null, node, new ComplexListener<CommonName>() {
									public void handleEvent(CommonName eventData) {
										//TaxonomyCache.impl.setCurrentTaxon(node);
										ClientUIContainer.bodyContainer.refreshBody();
									}
								});
								addNameBox.show();
							}
						});
	
						ToolBar tBar = new ToolBar();
						tBar.add(item);
	
						container.add(tBar);
					}
	
					for (CommonName curName : commonNames) {
						container.add(getCommonNameDisplay(curName, node));
					}
					container.setSize(350, 550);
					container.show();
					container.center();
	
				}
			});
			
			data.add(viewAll);
		}
	}
	
	private static class SynonymNoteAPI implements NoteAPI {
		
		private final Synonym synonym;
		private final Taxon taxon;
		
		private boolean hasChanged;
		
		public SynonymNoteAPI(Taxon taxon, Synonym synonym) {
			this.synonym = synonym;
			this.taxon = taxon;
			
			hasChanged = false;
		}

		@Override
		public void addNote(final Notes note, final GenericCallback<Object> callback) {
			note.setSynonym(synonym);
			
			final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
			document.put(UriBase.getInstance().getNotesBase() + "/notes/synonym/" + synonym.getId(), note.toXML(), new GenericCallback<String>() {
				public void onSuccess(String result) {
					Notes newNote = Notes.fromXML(document.getDocumentElement());
					
					note.setEdits(newNote.getEdits());
					note.setId(newNote.getId());
					
					synonym.getNotes().add(note);
					
					hasChanged = true;
					
					callback.onSuccess(result);
				}public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
		
		@Override
		public void deleteNote(final Notes note, final GenericCallback<Object> callback) {
			final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
			document.delete(UriBase.getInstance().getNotesBase() + "/notes/note/" + note.getId(), new GenericCallback<String>() {
				public void onSuccess(String result) {
					synonym.getNotes().remove(note);
					hasChanged = true;
					callback.onSuccess(result);
				}
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
		
		@Override
		public void loadNotes(ComplexListener<Collection<Notes>> listener) {
			listener.handleEvent(synonym.getNotes());
		}
		
		@Override
		public void onClose() {
			if (hasChanged)
				//TaxonomyCache.impl.setCurrentTaxon(taxon);
				ClientUIContainer.bodyContainer.refreshBody();
				//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(taxon.getId());			
		}
		
	}
	
	private static class Span extends HTML {
		
		public Span(String html, String... style) {
			super("<span>" + html + "</span>");
			for (String styleName : style)
				addStyleName(styleName);
		}
		
	}

}
