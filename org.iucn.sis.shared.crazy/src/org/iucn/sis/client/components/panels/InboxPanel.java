package org.iucn.sis.client.components.panels;

import java.util.ArrayList;

import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.client.ui.RefreshPortlet;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * This class represents the inbox ... duh!
 * 
 * @author liz.schwartz
 * 
 */
public class InboxPanel extends RefreshPortlet {
	static class Message {
		public String subject = "";
		public String date = "";
		public String from = "";
		public String body = "";

		public Message(String subject, String date, String from, String body) {
			this.subject = subject;
			this.date = date;
			this.from = from;
			this.body = body;
		}
	}

	private int NUMBEROFMESSAGESTODISPLAY = 2;

	private int NUMBEROFCOLUMNS = 3;
	private PanelManager panelManager;
	private NativeDocument messageList;

	private VerticalPanel content;

	public InboxPanel(PanelManager panelManager) {
		super("x-panel");
		this.panelManager = panelManager;
		content = new VerticalPanel();
		setHeading("My Inbox");

		messageList = SimpleSISClient.getHttpBasicNativeDocument();
		messageList.get("/inbox/" + SimpleSISClient.currentUser.getUsername(), new GenericCallback<String>() {

			public void onFailure(Throwable caught) {
				loadInboxFailure();

			}

			public void onSuccess(String arg0) {
				loadInbox();
			}

		});

		add(content);

	}

	private void loadInbox() {
		NativeNodeList messages = messageList.getDocumentElement().getElementsByTagName("message");
		final ArrayList messageArrayList = new ArrayList();

		Grid inboxTable = new Grid(1, NUMBEROFCOLUMNS);
		inboxTable.setWidget(0, 0, new HTML("<b>Date</b>"));
		inboxTable.setWidget(0, 1, new HTML("<b>Subject</b>"));
		inboxTable.setWidget(0, 2, new HTML("<b>From</b>"));

		if ((messages != null)) {
			for (int i = 0; i < messages.getLength(); i++) {
				NativeElement message = messages.elementAt(i);
				String subject = message.getElementByTagName("subject").getText();
				String date = message.getElementByTagName("date").getText();
				String from = message.getElementByTagName("from").getText();
				String body = message.getElementByTagName("body").getText();
				messageArrayList.add(new Message(subject, date, from, body));

			}

			int nm = messages.getLength();
			if (nm > NUMBEROFMESSAGESTODISPLAY)
				nm = NUMBEROFMESSAGESTODISPLAY;

			inboxTable.resize(nm + 1, NUMBEROFCOLUMNS);
			for (int i = 0; i < nm; i++) {
				inboxTable.setWidget(i + 1, 0, new HTML(((Message) messageArrayList.get(i)).date));
				inboxTable.setWidget(i + 1, 1, new HTML(((Message) messageArrayList.get(i)).subject));
				inboxTable.setWidget(i + 1, 2, new HTML(((Message) messageArrayList.get(i)).from));
				inboxTable.getRowFormatter().addStyleName(i + 1, "pointerCursor");

			}

			inboxTable.addTableListener(new TableListener() {
				public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
					if (row > 0) {
						WindowUtils.infoAlert(((Message) messageArrayList.get(row - 1)).subject,
								((Message) messageArrayList.get(row - 1)).body);
					}
				}

			});

			inboxTable.getColumnFormatter().setWidth(0, "15%");
			inboxTable.getColumnFormatter().setWidth(1, "70%");
			inboxTable.getColumnFormatter().setWidth(2, "20%");

			inboxTable.setBorderWidth(1);

			layout();

			content.add(inboxTable);
		}

		else {

		}

	}

	private void loadInboxFailure() {

	}

	@Override
	public void refresh() {
		content.clear();
		loadInbox();
	}
}
