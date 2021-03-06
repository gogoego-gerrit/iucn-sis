package org.iucn.sis.shared.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.client.userui.BrowseUsersWindow;
import org.iucn.sis.client.userui.BrowseUsersWindow.SearchResults;
import org.iucn.sis.shared.acl.User;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class SISCompleteListTextArea extends VerticalPanel {

	public final boolean ENABLED = true;
	public final boolean DISABLED = false;

	protected TextArea textArea = null;
	protected Button addButton = null;

	protected BrowseUsersWindow browseUserWindow;
	/**
	 * Text of everything that was typed
	 */
	protected List<User> selectedUsers = null;

	protected String highlightStyle = null;

	/**
	 * either enabled or disabled
	 */
	private boolean style = ENABLED;

	public SISCompleteListTextArea() {

		textArea = new TextArea();
		addButton = new Button("Add User");
		selectedUsers = new ArrayList<User>();
		browseUserWindow = new BrowseUsersWindow() {

			@Override
			public void onSelect(ArrayList<User> selectedUsers) {
				SISCompleteListTextArea.this.selectedUsers = selectedUsers;
				generateTextFromUsers();
			}

		};
		highlightStyle = "redFont";

		setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		build();
	}

	private void build() {

		addButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				showMasterList();
			}

		});
		addButton.addStyleName("pointerCursor");

		setSpacing(4);

		add(textArea);
		textArea.setSize("100%", "100%");
		add(addButton);
	}

	/**
	 * Returns in csv form the list of items currently in the listbox.
	 */

	public void clearItemsInList() {
		selectedUsers.clear();
		textArea.setText("");
	}

	public void disable() {
		textArea.setEnabled(false);
		style = DISABLED;
	}

	public void enable() {
		textArea.setEnabled(true);
		style = ENABLED;
	}

	protected String generateTextFromUsers() {
//		ArrayUtils.quicksort(selectedUsers, new Comparator<User>() {
//
//			public int compare(User user1, User user2) {
//				return user1.getCitationName().compareTo(
//						user2.getCitationName());
//			}
//
//		});

		StringBuilder text = new StringBuilder();
		for (int i = 0; i < selectedUsers.size(); i++) {
			text.append(selectedUsers.get(i).getCitationName());
			
			if (i + 1 < selectedUsers.size() - 1)
				text.append(", ");

			else if (i + 1 == selectedUsers.size() - 1)
				text.append(" & ");
		}
		textArea.setText(text.toString());
		return text.toString();
	}

	public List<User> getSelectedUsers() {
		return selectedUsers;
	}

	public String getText() {
		return textArea.getText();
	}

	public boolean hasOldText() {
		return textArea.isEnabled();
	}

	public void setTextAreaEnabled(boolean enabled) {
		textArea.setEnabled(enabled);
	}

	protected void setUsers(List<User> users) {

		selectedUsers = users;
		generateTextFromUsers();
	}

	public void setUsersId(List<String> users) {

		if (users.size() > 0) {
			HashMap<String, List<String>> map = new HashMap<String, List<String>>();
			map.put("userid", users);
			browseUserWindow.search(map,
					new GenericCallback<List<SearchResults>>() {
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Error", "An error occurred searching for users. " +
									"Please check your Internet connection, then try again.");
						};

						public void onSuccess(List<SearchResults> results) {
							List<User> users = new ArrayList<User>();
							for (SearchResults result : results)
								users.add(result.getUser());
							setUsers(users);
						};
					});
		} else {
			setUsers(new ArrayList<User>());
		}

	}

	public void setUserText(String text) {
		textArea.setText(text);
		selectedUsers.clear();
	}

	private void showMasterList() {
		browseUserWindow.refresh(selectedUsers);
		browseUserWindow.show();
	}

}
