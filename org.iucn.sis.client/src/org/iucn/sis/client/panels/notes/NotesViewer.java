package org.iucn.sis.client.panels.notes;

import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.NotesCache;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;

public class NotesViewer {
	
	public static void open(final Field field, final SimpleListener closeListener) {
		final Window window = WindowUtils.getWindow(true, true, "Notes for " + field.getName());
		if (closeListener != null)
			window.addListener(Events.Hide, new Listener<WindowEvent>() {
				public void handleEvent(WindowEvent be) {
					closeListener.handleEvent();
				}
			});

		final VerticalPanel panelAdd = new VerticalPanel();
		panelAdd.setSpacing(3);
		panelAdd.add(new HTML("Add Note: "));

		final TextArea area = new TextArea();
		area.setSize("400", "75");
		panelAdd.add(area);

		window.addButton(new Button("Add Note", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if ("".equals(area.getText())) {
					WindowUtils.errorAlert("Data Error", "Must enter note body.");
				} else {
					Notes currentNote = new Notes();
					currentNote.setValue(area.getText());

					NotesCache.impl.addNote(field, currentNote, AssessmentCache.impl.getCurrentAssessment(),
							new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Could not save note, please try again later.");
						}
						public void onSuccess(String result) {
							window.hide();
						}
					});
				}

			}
		}));
		window.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				window.hide();
			}
		}));

		final List<Notes> notes = NotesCache.impl.getNotesForCurrentAssessment(field);
		if (notes == null || notes.size() == 0) {
			window.add(new HTML("<div style='padding-top:10px';background-color:grey>"
					+ "<b>There are no notes for this field.</b></div>"));
			window.add(panelAdd);
		} else {
			ContentPanel eBar = new ContentPanel(new FillLayout(Orientation.VERTICAL));
			eBar.setHeight(200);
			eBar.setLayoutOnChange(true);
			eBar.setScrollMode(Scroll.AUTO);

			for (final Notes current : notes) {
				Image deleteNote = new Image("images/icon-note-delete.png");
				deleteNote.setTitle("Delete Note");
				deleteNote.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						NotesCache.impl.deleteNote(field, current, AssessmentCache.impl.getCurrentAssessment(),
								new GenericCallback<String>() {
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Could not remove note, please try again later.");
							}
							public void onSuccess(String result) {
								window.hide();
							};
						});
					}
				});

				LayoutContainer a = new LayoutContainer(new RowLayout(Orientation.HORIZONTAL));
				a.setLayoutOnChange(true);
				// a.setWidth(400);
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
			window.add(eBar);
			window.add(panelAdd);
		}

		window.setSize(500, 400);
		window.show();
		window.center();
	}

}
