package org.iucn.sis.server.utils.scripts;

import org.iucn.sis.client.userui.UserModelTabPanel;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class CreateRapidlistUsers {

	public static void main(String [] args) {
		createRapidlistUsers();
	}
	
	public static void createRapidlistUsers() {
		String csv = DocumentUtils.getVFSFileAsString("/utils/currentRLUsers.txt", SISContainerApp.getStaticVFS());
		String [] lines = csv.split("\n");
		System.out.println("Building " + lines.length + " users from as many lines.");
		
		for( String curLine : lines ) {
			String [] data = curLine.split(",");
			
			String username = data[1].toLowerCase();
			String password = data[2];
			String first = ""; 
			String last = ""; 
			if( data[4].contains(" ") ) {
				first = data[4].substring(0, data[4].indexOf(" ", 0));
				last = data[4].substring(data[4].indexOf(" ", 0)+1, data[4].length());
			} else {
				first = data[4];
			}
			String businessUnit = "N/A";

			if( data.length > 5 ) {
				businessUnit = data[5];

				if( businessUnit.startsWith("\"") ) {
					int index = 6;
					while( !businessUnit.endsWith("\"") )
						businessUnit += "," + data[index++];

					businessUnit = businessUnit.substring(1, businessUnit.length()-1);
				}
			}
			
//			if( username.toUpperCase().matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}") )
//				System.out.println(username + ", " + password + ", " + first + ":" + last + ", " + businessUnit);
//			else
//				System.out.println("****** BAD USERNAME " + username);
			
			if( username.toUpperCase().matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}") )
				putNewAccountAndProfile(username, password, first, last, businessUnit);
		}
	}
	
	private static void putNewAccountAndProfile(final String username, final String password,
			final String first, final String last, final String bu) {
		StringBuffer xml = new StringBuffer("<auth>");
		xml.append("<u>");
		xml.append(username);
		xml.append("</u>");
		xml.append("<p>");
		xml.append(password);
		xml.append("</p>");
		xml.append("</auth>");

		final NativeDocument doc = NativeDocumentFactory.newNativeDocument();
		doc.putAsText("/authn", xml.toString(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				System.out.println("Account " + username + " already exists!");
			}

			public void onSuccess(String result) {
				putNewProfile(username, password, first, last, bu);
			}
		});
	}

	private static void putNewProfile(final String username, final String password, final String first, 
			final String last, final String bu) {
		final NativeDocument createDoc = NativeDocumentFactory.newNativeDocument();
		createDoc.put(UserModelTabPanel.CONSTANTS_ATTACHMENT_POINT + "/list", 
				"<root><username><![CDATA[" + username + "]]></username></root>", new GenericCallback<String>() {
			public void onSuccess(String result) {
				final NativeNodeList nodes = createDoc.getDocumentElement().getElementsByTagName("user");
				String id = "";
				for (int i = 0; i < nodes.getLength(); i++) {
					NativeElement current = nodes.elementAt(i);
					id = current.getAttribute("id");
				}
					
				final NativeDocument document = NativeDocumentFactory.newNativeDocument();
				document.post(UserModelTabPanel.CONSTANTS_ATTACHMENT_POINT + "/list" + "/" + id,
					getXML(username, first, last, bu), new GenericCallback<String>() {
							public void onFailure(Throwable caught) {
								System.out.println("Seriously unfortunate failure trying to put profile info for " + username + ".");
							}

							public void onSuccess(String result) {
								System.out.println("Success for user " + username);
							}
						});
			}
			public void onFailure(Throwable caught) {
				System.out.println("Seriously unfortunate failure trying to create profile for " + username + ".");
			}
		});
	}
	
	private static String getXML(String username, String firstname, String lastname, String affiliation) {
		StringBuilder ret = new StringBuilder("<root>");
		ret.append("<field name=\"username\"><![CDATA[" + username + "]]></field>");
		ret.append("<field name=\"firstname\"><![CDATA[" + firstname + "]]></field>");
		ret.append("<field name=\"lastname\"><![CDATA[" + lastname + "]]></field>");
		ret.append("<field name=\"affiliation\"><![CDATA[" + affiliation + "]]></field>");
		ret.append("<field name=\"quickGroup\">guest</field>");
		ret.append("<field name=\"sis\">false</field>");
		ret.append("<field name=\"rapidlist\">true</field>");
		ret.append("</root>");
		return ret.toString();
	}
}
