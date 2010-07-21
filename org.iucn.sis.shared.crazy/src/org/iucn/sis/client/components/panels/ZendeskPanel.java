package org.iucn.sis.client.components.panels;
import org.iucn.sis.client.simple.SimpleSISClient;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.TextBox;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;

public class ZendeskPanel extends Window {
	final TextBox subject;
	final TextArea ticket;
	
	public ZendeskPanel() {
		subject = new TextBox();
		ticket = new TextArea();
		setSize(800, 600);
		build();
	}
	
	private void build(){
				
				final Window w = new Window();
				getCredentials(new GenericCallback<String>() {
					
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
						
					}
					
					public void onSuccess(String result) {
						System.out.println("https://iucnsis.zendesk.com/access/remote/"+result);
						setUrl("https://iucnsis.zendesk.com/access/remote/"+result);
						show();
						
					}
				});
		
	}
	
	private void sendTicket(){
		
		NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		
		doc.post("/zendesk/tickets", buildTicketRequest(ticket.getValue()), new GenericCallback<String>() {
			public void onSuccess(String result) {
				// TODO Auto-generated method stub
				
			}
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
	private String buildTicketRequest(String description){
		final String email = SimpleSISClient.currentUser.getUsername();
		String name = SimpleSISClient.currentUser.getFirstName() + " " +  SimpleSISClient.currentUser.getLastName();
		
		return "<ticket>"+
		  "<description>"+description+"</description>"+
		  "<priority-id>0</priority-id>"+  
		  "<requester-name>"+name+"</requester-name>"+
		  "<requester-email>"+email+"</requester-email>"+   
		"</ticket>";
//		
	}
	
	private void getCredentials(final GenericCallback<String> callback){
		final String email = SimpleSISClient.currentUser.getUsername();
		final String name = SimpleSISClient.currentUser.getFirstName() + " " +  SimpleSISClient.currentUser.getLastName();
		String organization = SimpleSISClient.currentUser.getOrganizationName();
		//String external_id = String.valueOf(SimpleSISClient.currentUser.getId());
		
		String xml = "<root><user name=\""+name+"\" email=\""+email+"\" organization=\""+organization+"\"/></root>";
		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		
		doc.postAsText("/zendesk/authn", xml, new GenericCallback<String>() {
			public void onSuccess(String result) {
				callback.onSuccess(doc.getText());
				
			}
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				
			}
		});
	}
	

	private void logout(){
		final String email = SimpleSISClient.currentUser.getUsername();
		final String name = SimpleSISClient.currentUser.getFirstName() + " " +  SimpleSISClient.currentUser.getLastName();
		String organization = SimpleSISClient.currentUser.getOrganizationName();
		//String external_id = String.valueOf(SimpleSISClient.currentUser.getId());
		
		String xml = "<root><user name=\""+name+"\" email=\""+email+"\" organization=\""+organization+"\"/></root>";
		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		
		doc.postAsText("/zendesk/logout", xml, new GenericCallback<String>() {
			public void onSuccess(String result) {
				//callback.onSuccess(doc.getText());
				
			}
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				
			}
		});
	}
		
	@Override
	public void close() {
		Cookies.removeCookie("_zendesk_session");
		logout();
	
		super.hide();
	}
	
}