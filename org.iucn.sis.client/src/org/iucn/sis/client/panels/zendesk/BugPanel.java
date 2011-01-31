package org.iucn.sis.client.panels.zendesk;

import java.util.ArrayList;

import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.utils.RefreshPortlet;
import org.iucn.sis.shared.api.debug.Debug;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

public class BugPanel extends RefreshPortlet {
	
	private static final int NUMBEROFBUGTODISPLAY = 10;
	
	private final String rule;
	
	private NativeDocument buglist = null;
	
	static class Bug {
		public final String subject;
		public final String date;
		public final String from;
		public final String niceId;

		public Bug(String subject, String date, String from, String niceid) {
			this.subject = subject;
			this.date = date;
			this.from = from;
			this.niceId=niceid;
		}
	}
	
	public BugPanel(String rule, String header) {
		this.rule=rule;
		
		setLayout(new FitLayout());
		setLayoutOnChange(true);
		setHeading(header);
		
		refresh();
	}
	
	@Override
	public void refresh() {
		removeAll();
		
		if (buglist == null || buglist.getPeer() == null) {
			buglist = SimpleSISClient.getHttpBasicNativeDocument();
			buglist.get(UriBase.getInstance().getZendeskBase() + "/zendesk/rules/"+ rule, new GenericCallback<String>() {
				public void onSuccess(String result) {
					loadBugs(buglist);
				}
				public void onFailure(Throwable caught) {
					caught.printStackTrace();
				}
			});
		}
		else
			loadBugs(buglist);
	}
	
	private void loadBugs(NativeDocument buglist) {
		if (buglist == null || buglist.getDocumentElement() == null) {
			add(new HTML("No tickets available to load."));
			return;
		}
		
		final NativeNodeList tickets = buglist.getDocumentElement().getElementsByTagName("ticket");

		final ArrayList<Bug> bugArrayList = new ArrayList<Bug>();
		for (int i = 0; i < tickets.getLength() && i < NUMBEROFBUGTODISPLAY; i++) {
			NativeElement message = tickets.elementAt(i);
			String subject = message.getElementByTagName("subject").getText();
			String date = message.getElementByTagName("created-at").getText();
			String from = message.getElementByTagName("req-name").getText();
			String nid = message.getElementByTagName("nice-id").getText();
			bugArrayList.add(new Bug(subject, date, from, nid));
		}
		
		if (bugArrayList.isEmpty()) {
			add(new HTML("There are no tickets to view."));
			return;
		}
		
		int row = 0;
			
		final Grid bugTable = new Grid(bugArrayList.size() + 1, 3);
		bugTable.getColumnFormatter().setWidth(0, "15%");
		bugTable.getColumnFormatter().setWidth(1, "70%");
		bugTable.getColumnFormatter().setWidth(2, "20%");
		bugTable.setWidth("100%");
		
		bugTable.setWidget(row, 0, new HTML("Id"));
		bugTable.setWidget(row, 1, new HTML("Subject"));
		bugTable.setWidget(row, 2, new HTML("Reporter"));
		bugTable.getRowFormatter().addStyleName(row, "hasBoldHTML");
		bugTable.getRowFormatter().addStyleName(row, "hasUnderlinedHTML");
			
		row++;
			
		for (Bug current : bugArrayList) {
			bugTable.setWidget(row, 0, new HTML(current.niceId));
			bugTable.setWidget(row, 1, new HTML(current.subject));
			bugTable.setWidget(row, 2, new HTML(current.from));
			bugTable.getRowFormatter().addStyleName(row, "pointerCursor");
				
			row++;
		}
			
		bugTable.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final Cell cell = bugTable.getCellForEvent(event);
				if (cell == null)
					return;
				
				getCredentials(new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						//TODO: pull site from settings.
						WindowUtils.errorAlert("Could not load or authenticate you, please visit http://iucnsis.zendesk.com directly.");
					}
					public void onSuccess(String result) {
						final Window w = new Window(){
							public void hide() {
								logout();
								//Cookies.setCookie("_zendesk_session", "", new Date(0), "iucnsis.zendesk.org", "/", false);
								Cookies.removeCookie("_zendesk_session");
								super.hide();
							}
						};
						w.setUrl("http://iucnsis.zendesk.com/tickets/"+bugTable.getText(cell.getRowIndex(), 0)+"/"+result);
						w.setSize(800, 600);
						w.show();
					}
				});
			}
		});
		
		add(bugTable);
	}
	
	private void getCredentials(final GenericCallback<String> callback){
		final String email = SimpleSISClient.currentUser.getEmail();
		final String name = SimpleSISClient.currentUser.getFirstName() + " " +  SimpleSISClient.currentUser.getLastName();
		String organization = SimpleSISClient.currentUser.getAffiliation();
		//String external_id = String.valueOf(SimpleSISClient.currentUser.getId());
		
		String xml = "<root><user name=\""+name+"\" email=\""+email+"\" organization=\""+organization+"\"/></root>";
		
		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		doc.postAsText(UriBase.getInstance().getZendeskBase() + "/zendesk/login", xml, new GenericCallback<String>() {
			public void onSuccess(String result) {
				callback.onSuccess(doc.getText());				
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	private void logout() {
		final String email = SimpleSISClient.currentUser.getEmail();
		final String name = SimpleSISClient.currentUser.getFirstName() + " " +  SimpleSISClient.currentUser.getLastName();
		String organization = SimpleSISClient.currentUser.getAffiliation();
		//String external_id = String.valueOf(SimpleSISClient.currentUser.getId());
		
		String xml = "<root><user name=\""+name+"\" email=\""+email+"\" organization=\""+organization+"\"/></root>";
		
		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		doc.postAsText(UriBase.getInstance().getZendeskBase() +"/zendesk/logout", xml, new GenericCallback<String>() {
			public void onSuccess(String result) {
				//callback.onSuccess(doc.getText());
				Debug.println("Zendesk Logout successful");
			}
			public void onFailure(Throwable caught) {
				Debug.println(caught);				
			}
		});
	}
}
