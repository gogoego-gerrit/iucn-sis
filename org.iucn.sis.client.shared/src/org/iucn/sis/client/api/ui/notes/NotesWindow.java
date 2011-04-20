package org.iucn.sis.client.api.ui.notes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Notes;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class NotesWindow extends Window implements DrawsLazily {
	
	private final NoteAPI api;
	
	public NotesWindow(final NoteAPI api) {
		super();
		this.api = api;
		
		setLayout(new FillLayout());
		setSize(500, 550);
		setHeading("View Notes");
	}
	
	public void show() {
		draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				open();
			}
		});
	}
	
	private void open() {
		super.show();
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		api.loadNotes(new ComplexListener<Collection<Notes>>() {
			public void handleEvent(final Collection<Notes> notes) {
				final LayoutContainer container = new LayoutContainer(new BorderLayout());
				
				final BorderLayoutData top = new BorderLayoutData(LayoutRegion.NORTH, .75f);
				
				if (notes == null || notes.size() == 0) {
					container.add(new HtmlContainer("<div style='padding-top:10px';background-color:grey>"
							+ "<b>There are no notes for this field.</b></div>"), top);
				} else {
					TableLayout layout = new TableLayout(1);
					layout.setCellPadding(0);
					layout.setCellSpacing(0);
					
					TableData layoutData = new TableData();
					layoutData.setMargin(0);
					layoutData.setPadding(0);
					
					LayoutContainer eBar = new LayoutContainer(layout);
					eBar.setScrollMode(Scroll.AUTO);

					for (final Notes current : sorted(notes)) {
						Image deleteNote = new Image("images/icon-note-delete.png");
						deleteNote.setTitle("Delete Note");
						deleteNote.addClickHandler(new ClickHandler() {
							public void onClick(ClickEvent event) {
								api.deleteNote(current, new GenericCallback<Object>() {
									public void onSuccess(Object result) {
										hide();
										Info.display("Success", "Note removed successfully");
									}
									public void onFailure(Throwable caught) {
										WindowUtils.errorAlert("Could not remove note, please try again later.");
									}
								});
							}
						});

						HorizontalPanel a = new HorizontalPanel();
						a.addStyleName("notes_entry");
						a.setVerticalAlign(VerticalAlignment.TOP);
						a.setSpacing(4);
						a.add(deleteNote);
						a.add(new HtmlContainer("<div class=\"notes_entry_html\">" + 
							createEditLabelText(current) + " -- " + toHTML(current.getValue()) + "</div>"));

						eBar.add(a, layoutData);
					}
					
					LayoutContainer wrapper = new LayoutContainer(new FillLayout());
					wrapper.setScrollMode(Scroll.AUTOX);
					wrapper.add(eBar);
					
					container.add(eBar, top);
				}

				final TextArea area = new TextArea();
				area.setEmptyText("Type here to enter a new note.");
				
				container.add(area, new BorderLayoutData(LayoutRegion.CENTER, .25f));
				
				add(container);
				
				addButton(new Button("Add Note", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						if (area.getValue() == null || "".equals(area.getValue())) {
							WindowUtils.errorAlert("Data Error", "Must enter note body.");
						} else {
							Edit edit = new Edit();
							edit.setCreatedDate(new Date());
							edit.setUser(SISClientBase.currentUser);
							
							Notes currentNote = new Notes();
							currentNote.setValue(area.getValue());
							currentNote.setEdit(edit);

							api.addNote(currentNote, new GenericCallback<Object>() {
								public void onSuccess(Object result) {
									hide();
								}
								
								@Override
								public void onFailure(Throwable caught) {
									WindowUtils.errorAlert("Could not save note, please try again later.");	
								}
							});
						}
					}
				}));
				addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						hide();
					}
				}));
				
				addListener(Events.Hide, new Listener<WindowEvent>() {
					public void handleEvent(WindowEvent be) {
						api.onClose();
					}
				});
				
				callback.isDrawn();
			}
		});
	}
	
	private List<Notes> sorted(Collection<Notes> notes) {
		final List<Notes> sorted = new ArrayList<Notes>(notes);
		Collections.sort(sorted, new NotesComparator());
		
		return sorted;
	}
	
	protected String createEditLabelText(Notes note) {
		final String defaultValue = "<i>Edit information unavailable</i>";
		if (note.getEdits() == null || note.getEdits().isEmpty())
			return defaultValue;
		
		final Edit edit = note.getEdit();
		try {
			return "<b>" + edit.getUser().getDisplayableName() + 
			" [" + FormattedDate.impl.getDate(edit.getCreatedDate()) + 
			"]</b>";
		} catch (Throwable e) {
			Debug.println(e);
			return defaultValue;
		}
	}
	
	protected String toHTML(String value) {
		return value.replace("\n", "<br/>");
	}
	
	private static class NotesComparator implements Comparator<Notes> {
		
		@Override
		public int compare(Notes o1, Notes o2) {
			Edit e1 = o1.getEdit();
			Edit e2 = o2.getEdit();
			
			if (e1 == null && e2 == null)
				return 0;
			else if (e1 == null)
				return 1;
			else if (e2 == null)
				return -1;
			
			int result = e1.getCreatedDate().compareTo(e2.getCreatedDate());
			
			if (result == 0)
				result = Integer.valueOf(o1.getId()).compareTo(Integer.valueOf(o2.getId()));
			
			return result * -1;
		}
		
	}

}
