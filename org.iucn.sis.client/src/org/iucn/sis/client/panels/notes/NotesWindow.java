package org.iucn.sis.client.panels.notes;

import java.util.Collection;

import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.shared.api.models.Notes;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
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
		setSize(500, 400);
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
					LayoutContainer eBar = new LayoutContainer(new FillLayout(Orientation.VERTICAL));
					eBar.setHeight(200);
					eBar.setLayoutOnChange(true);
					eBar.setScrollMode(Scroll.AUTO);

					for (final Notes current : notes) {
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

						LayoutContainer a = new LayoutContainer(new RowLayout(Orientation.HORIZONTAL));
						a.add(deleteNote, new RowData());
						
						try {
							a.add(new HTML("<b>" + current.getEdit().getUser().getDisplayableName() + 
									" [" + FormattedDate.impl.getDate(current.getEdit().getCreatedDate()) + 
									"]</b>  --" + current.getValue()), new RowData(1d, 1d));// );
						} catch (NullPointerException e) {
							a.add(new HTML("<i>Edit information unavailable</i>  --" + current.getValue()), new RowData(1d, 1d));// );
						}

						eBar.add(a, new RowData(1d, 1d));
					}
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
							Notes currentNote = new Notes();
							currentNote.setValue(area.getValue());

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

}
