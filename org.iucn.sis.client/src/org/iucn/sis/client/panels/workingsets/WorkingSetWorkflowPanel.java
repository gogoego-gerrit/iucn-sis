package org.iucn.sis.client.panels.workingsets;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.models.ClientUser;
import org.iucn.sis.client.api.ui.users.panels.BrowseUsersWindow;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
import org.iucn.sis.client.panels.workflow.WorkflowNotesWindow;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.workflow.WorkflowStatus;
import org.iucn.sis.shared.api.workflow.WorkflowUserInfo;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.utils.ClientDocumentUtils;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.api.XMLWritingUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.gwt.ui.StyledHTML;

public class WorkingSetWorkflowPanel extends RefreshLayoutContainer implements DrawsLazily {
	
	private final PanelManager manager;
	private final TableData data1, data2;
	
	private WorkflowStatus currentStatus;
	
	public WorkingSetWorkflowPanel(PanelManager manager) {
		super();
		this.manager = manager;
		addStyleName("gwt-background");
		setScrollMode(Scroll.AUTO);
		setSize("100%", "100%");
		setLayout(new FillLayout());
		
		data1 = new TableData(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
		data1.setWidth("180px");
		
		data2 = new TableData(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
	}
	
	public void draw(final DrawsLazily.DoneDrawingCallback callback) {
		removeAll();
		
		final TableLayout layout = new TableLayout(2);
		layout.setCellSpacing(30);
		
		final LayoutContainer container = new LayoutContainer(layout);
		
		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getWorkflowBase() + "/workflow/set/" + WorkingSetCache.impl.getCurrentWorkingSet().getId() + "/status", new GenericCallback<String>() {
			public void onSuccess(String result) {
				final RowParser parser = new RowParser(document);
				if (parser.getRowCount() == 1) {
					final RowData row = parser.getFirstRow();
					addRow(new StyledHTML("Working Set Status", "bold"), 
							row.getField("status"), container);
					addRow(new StyledHTML("", "bold"), "", container);
					
					currentStatus = WorkflowStatus.getStatus(row.getField("status"));
					
					build(container);
					callback.isDrawn();
				}
				else
					onFailure(null);
				
			}
			public void onFailure(Throwable caught) {
				addRow(new StyledHTML("Working Set Status", "bold"), "draft", container);
				addRow(new StyledHTML("", "bold"), "", container);
				
				currentStatus = WorkflowStatus.DRAFT;
				
				build(container);
				callback.isDrawn();
			}
		});
	}
	
	@Override
	public void refresh() {
		draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				layout();
			}
		});
	}

	private void build(final LayoutContainer container) {
		addRow(createButton("View Notes", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final WorkflowNotesWindow window = new WorkflowNotesWindow();
				window.show();
			}
		}), "View all notes and comments made " +
				"about this working set during the " +
				"submission process.", container);
		
		addRow(new StyledHTML("Process Step", "bold"), new StyledHTML("Description", "bold"), container);
		
		addRow(createButton("Submit For Review", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				updateStatus(WorkflowStatus.REVIEW);
			}
		}), "Submit this working set for review.", container);
		
		addRow(createButton("Return to Draft", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				updateStatus(WorkflowStatus.DRAFT);
			}
		}), "Return this working set to draft status.", container);
		
		addRow(createButton("Submit for Consistency Check", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				updateStatus(WorkflowStatus.CONSISTENCY_CHECK);
			}
		}), "Submit this working set for consistency " +
				"check (requires working set to have been reviewed).", container);
		
		addRow(createButton("Return to Review Stage", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				updateStatus(WorkflowStatus.REVIEW);
			}
		}), "Return this working to review status.", container);
		
		addRow(createButton("Submit Final to Red List", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				updateStatus(WorkflowStatus.FINAL);
			}
		}), "Submit this working set to Red List for " +
				"final submission (requires working set " +
				"to have been checked for consistency.", container);
		
		addRow(createButton("Return to Consistency Check", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				updateStatus(WorkflowStatus.CONSISTENCY_CHECK);
			}
		}), "Return this working set to consistency check stage.", container);
		
		addRow(createButton("Publish", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				updateStatus(WorkflowStatus.PUBLISH);
			}
		}), "Publish this working set (RL Unit only)", container);
		
		removeAll();
		add(container);
	}
	
	private void addRow(Widget left, String right, LayoutContainer container) {
		addRow(left, new HTML(right), container);
	}
	
	private void addRow(Widget left, Widget right, LayoutContainer container) {
		container.add(left, data1);
		container.add(right, data2);
	}
	
	private Button createButton(String text, SelectionListener<ButtonEvent> listener) {
		Button b = new Button(text, listener);
		b.setMinWidth(175);

		return b;
	}
	
	private String getFailureMessage(final WorkflowStatus status) {
		if (WorkflowStatus.PUBLISH.equals(currentStatus))
			return "Working Set is already published.";
		
		if (WorkflowStatus.FINAL.equals(currentStatus)) {
			//It can move to any status
			return null;
		}
		else {
			//It can only move up or down.
			Debug.println("Current status is {0}; can only move up to {1} or back to {2}",
				currentStatus, currentStatus.getNextStatus(), currentStatus.getPreviousStatus());
			if (status.equals(currentStatus.getNextStatus())) {
				//Moved up in the chain
				return null;
			}
			else if (status.equals(currentStatus.getPreviousStatus())) {
				//Moved down in the chain
				return null;
			}
			else {
				return "The status of this working set can only be changed to " +
					writePossibleStatusValues(currentStatus) + ".";
			}
		}
	}
	
	private String writePossibleStatusValues(WorkflowStatus status) {
		StringBuilder possible = new StringBuilder();
		if (status.getPreviousStatus() != null)
			possible.append(status.getPreviousStatus());
		if (status.getNextStatus() != null) {
			if (!possible.toString().equals(""))
				possible.append(" or ");
			possible.append(status.getNextStatus());
		}
		return possible.toString();
	}
	
	private void updateStatus(final WorkflowStatus status) {
		final String error = getFailureMessage(status);
		if (error == null) {
			final CommentWindow window = new CommentWindow(status) {
				public void onSave(final String comment) {
					/*
					 * This is fine b/c you can only go forward to 
					 * final, there is no going backward to final.
					 */
					if (WorkflowStatus.FINAL.equals(status)) {
						sendToServer(status, comment, new GetUsersEvent(this));
					}
					else {
						getUsers(new Listener<GetUsersEvent>() {
							public void handleEvent(GetUsersEvent event) {
								sendToServer(status, comment, event);
							}
						});
					}
				}
			};
			window.show();
		}
		else {
			WindowUtils.errorAlert(error);
		}
	}
	
	private void sendToServer(final WorkflowStatus status, final String comment, final GetUsersEvent event)  {
		final StringBuilder bodyBuilder = new StringBuilder();
		bodyBuilder.append("<root>");
		bodyBuilder.append("<user>");
		bodyBuilder.append(XMLWritingUtils.writeCDATATag("id", SimpleSISClient.currentUser.getId()+""));
		bodyBuilder.append(XMLWritingUtils.writeCDATATag("username", SimpleSISClient.currentUser.getUsername()));
		bodyBuilder.append(XMLWritingUtils.writeCDATATag("displayname", SimpleSISClient.currentUser.getDisplayableName()));
		bodyBuilder.append(XMLWritingUtils.writeCDATATag("email", SimpleSISClient.currentUser.getEmail()));
		bodyBuilder.append("</user>");
		bodyBuilder.append(XMLWritingUtils.writeCDATATag("status", status.toString()));
		bodyBuilder.append(XMLWritingUtils.writeCDATATag("comment", comment));
		bodyBuilder.append(XMLWritingUtils.writeCDATATag("scope", "global"));
		for (WorkflowUserInfo userInfo : event.getUsers()) {
			bodyBuilder.append("<notify>");
			bodyBuilder.append(XMLWritingUtils.writeCDATATag("id", userInfo.getID().toString()));
			bodyBuilder.append(XMLWritingUtils.writeCDATATag("username", userInfo.getUsername()));
			bodyBuilder.append(XMLWritingUtils.writeCDATATag("displayname", userInfo.getDisplayName()));
			bodyBuilder.append(XMLWritingUtils.writeCDATATag("email", userInfo.getEmail()));
			bodyBuilder.append("</notify>");
		}
		bodyBuilder.append("</root>");
		
		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
		document.post(UriBase.getInstance().getWorkflowBase() +"/workflow/set/" + WorkingSetCache.impl.getCurrentWorkingSet().getId() + "/status", bodyBuilder.toString(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert(ClientDocumentUtils.parseStatus(document));
			}
			public void onSuccess(String result) {
				WindowUtils.infoAlert("Status updated to " + status + " successfully.");
				manager.workingSetHierarchy.refresh(new GenericCallback<Object>() {
					public void onSuccess(Object result) {
						refresh();	
					}
					public void onFailure(Throwable caught) {
						refresh();
					}
				});
			}
		});
	}
	
	private void getUsers(final Listener<GetUsersEvent> listener) {
		final UserNotificationWindow window = new UserNotificationWindow(listener);
		window.show();
	}
	
	private static class UserNotificationWindow extends BrowseUsersWindow implements ComplexListener<List<ClientUser>> {
		
		private final Listener<GetUsersEvent> listener;
		
		private UserNotificationWindow(Listener<GetUsersEvent> listener) {
			super();
			this.listener = listener;
			
			setInstructions("<span style=\"fontSize12\"><b>If you don’t know who to notify</b>, " +
					"please select \"Send to RL Unit\" and we will notify " +
					"the Red List unit and they will determine who should " +
					"get notified</span>", true);
			setHeight(600);
			
			
		}
		
		@Override
		protected void addButtons() {
			addButton(new Button("Send to Selected Users", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					onSave();
					hide();
				}
			}));
			//addButton(createOrButton());
			addButton(new Button("Send to RL Unit", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					hide();
					final GetUsersEvent event = new GetUsersEvent(this);
					event.addUser(0, "RLU", "Red List Unit", "redlistunit@iucnsis.org");
					//TODO: who is RLU?!?!?! add 'em 
					listener.handleEvent(event);
				}
			}));
			//addButton(createOrButton());
			addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					hide();
				}
			}));
			
		}
		
		@SuppressWarnings("unused")
		private Button createOrButton() {
			Button button = new Button(" OR ") {
				protected void afterRender() {
					for (String name : getStyleName().split(" "))
						removeStyleName(name);
				}
			};
			for (String name : button.getStyleName().split(" "))
				button.removeStyleName(name);
			button.setStyleName("no-style");
			return button;
		}
		
		@Override
		public void handleEvent(List<ClientUser> selectedUsers) {
			final GetUsersEvent event = new GetUsersEvent(this);
			for (ClientUser user : selectedUsers) {
				String mail = user.getEmail();
				if (mail == null || mail.equals(""))
					mail = user.getUsername();
				event.addUser(user.getId(), user.getUsername(), user.getDisplayableName(), mail);
			}
			listener.handleEvent(event);
		}
		
	}
	
	private static class GetUsersEvent extends BaseEvent {
		
		private final List<WorkflowUserInfo> list;
		
		public GetUsersEvent(Object source) {
			super(source);
			list = new ArrayList<WorkflowUserInfo>();
		}
		
		public void addUser(Integer id, String username, String displayName, String email) {
			list.add(new WorkflowUserInfo(id, username, displayName, email));
		}
		
		public List<WorkflowUserInfo> getUsers() {
			return list;
		}
	}
	
	private static abstract class CommentWindow extends Window {
		
		public CommentWindow(WorkflowStatus status) {
			super();
			setModal(true);
			setLayout(new FillLayout());
			setHeading("Add Comment");
// setAlignment(HorizontalAlignment.CENTER);
			setSize(350, 200);
			
			final TextArea comment = 
				FormBuilder.createTextArea("comment", null, "Comment", true);
			comment.setEmptyText("Enter comment here");
			
			final FormPanel form = new FormPanel();
			form.setBodyBorder(false);
			form.setHeaderVisible(false);
			form.add(FormBuilder.createLabelField("label", status.toString(), "Status"));
			form.add(comment);
			
			add(form);
			
			addButton(new Button("Continue", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					if (form.isValid()) {
						hide();
						onSave(comment.getValue());
					}
					else
						WindowUtils.errorAlert("Please enter a comment.");
				}
			}));
			addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					hide();
				}
			}));
		}
		
		public abstract void onSave(String comment);
		
	}
	
}
