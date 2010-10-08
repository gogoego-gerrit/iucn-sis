package org.iucn.sis.client.panels.zendesk;

import java.util.ArrayList;

import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.utils.RefreshPortlet;
import org.iucn.sis.shared.api.debug.Debug;

import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

public class BugPanel extends RefreshPortlet {
	
	private static final int NUMBEROFBUGTODISPLAY = 10;
	
	private final String rule;
	
	@SuppressWarnings("unused")
	private PanelManager panelManager;
	private VerticalPanel content;
	private NativeDocument buglist = null;
	
	static class Bug {
		public String subject = "";
		public String date = "";
		public String from = "";
		public String niceId="";
		

		public Bug(String subject, String date, String from, String niceid) {
			this.subject = subject;
			this.date = date;
			this.from = from;
			this.niceId=niceid;
		}
	}
	
	public BugPanel(String rule, PanelManager panelManager, String header) {
		this.panelManager = panelManager;
		this.rule=rule;
		setHeading(header);
		content = new VerticalPanel();
		add(content);
		
		refresh();
	}
	
	@Override
	public void refresh() {
		content.clear();
		
		if (buglist != null)
			loadBugs(buglist);
		else {
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
	}
	
	private void loadBugs(NativeDocument buglist) {
		final NativeNodeList tickets = buglist.getDocumentElement().getElementsByTagName("ticket");
		
		final ArrayList<Bug> bugArrayList = new ArrayList<Bug>();
		
		final Grid bugTable = new Grid(1, 3);
		bugTable.setWidget(0, 0, new HTML("<b>Id</b>"));
		bugTable.setWidget(0, 1, new HTML("<b>Subject</b>"));
		bugTable.setWidget(0, 2, new HTML("<b>Reporter</b>"));

		if ((tickets != null)) {
			for (int i = 0; i < tickets.getLength(); i++) {
				NativeElement message = tickets.elementAt(i);
				String subject = message.getElementByTagName("subject").getText();
				String date = message.getElementByTagName("created-at").getText();
				String from = message.getElementByTagName("req-name").getText();
				String nid = message.getElementByTagName("nice-id").getText();
				bugArrayList.add(new Bug(subject, date, from, nid));
			}

			int nm = tickets.getLength();
			if (nm > NUMBEROFBUGTODISPLAY)
				nm = NUMBEROFBUGTODISPLAY;

			bugTable.resize(nm + 1, 3);
			for (int i = 0; i < nm; i++) {
				bugTable.setWidget(i + 1, 0, new HTML(bugArrayList.get(i).niceId));
				bugTable.setWidget(i + 1, 1, new HTML(bugArrayList.get(i).subject));
				bugTable.setWidget(i + 1, 2, new HTML(bugArrayList.get(i).from));
				bugTable.getRowFormatter().addStyleName(i + 1, "pointerCursor");
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
//									Cookies.setCookie("_zendesk_session", "", new Date(0), "iucnsis.zendesk.org", "/", false);
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

			bugTable.getColumnFormatter().setWidth(0, "15%");
			bugTable.getColumnFormatter().setWidth(1, "70%");
			bugTable.getColumnFormatter().setWidth(2, "20%");

			bugTable.setBorderWidth(1);

			content.add(bugTable);
		}
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
