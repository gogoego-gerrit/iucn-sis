package org.iucn.sis.client.panels.bookmarks;

import java.util.Date;

import org.iucn.sis.client.api.caches.BookmarkCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.container.StateChangeEvent;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.shared.api.models.Bookmark;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * Creates a new bookmark based on the 
 * current state of the application.
 * 
 * @author carl.scott
 *
 */
public class NewBookmarkPanel extends Window {
	
	private final TextField<String> name;
	private final StateChangeEvent state;
	
	public NewBookmarkPanel() {
		super();
		setIconStyle("icon-bookmark");
		setButtonAlign(HorizontalAlignment.CENTER);
		setHeading("Create New Bookmark");
		setSize(400, 200);
		
		state = new StateChangeEvent(
			StateManager.impl.getWorkingSet(), 
			StateManager.impl.getTaxon(), 
			StateManager.impl.getAssessment(), 
			null
		);
		
		name = new TextField<String>();
		name.setAllowBlank(false);
		name.setMaxLength(1000);
		name.setValue(state.getDisplayName());
		name.setWidth(350);
		
		add(new Html("Enter a name for your bookmark below:"));
		add(spacer(20));
		add(name);
		
		addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (!name.isValid()) {
					WindowUtils.errorAlert("Please enter a valid bookmark name.");
					return;
				}
				
				final Bookmark bookmark = new Bookmark();
				bookmark.setDate(new Date());
				bookmark.setName(name.getValue());
				bookmark.setValue(state.getToken());
				bookmark.setUser(SISClientBase.currentUser);
				
				BookmarkCache.impl.add(bookmark, new GenericCallback<String>() {
					public void onSuccess(String result) {
						Info.display("Success", "Bookmark added successfully.");
						hide();
					}
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Bookmark could not be added, please try again later.");
					}
				});
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}
	
	private HTML spacer(int size) {
		HTML spacer = new HTML("&nbsp;");
		spacer.setHeight(size + "px");
		return spacer;
	}

}
