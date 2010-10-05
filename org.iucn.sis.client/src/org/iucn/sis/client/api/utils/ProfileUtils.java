package org.iucn.sis.client.api.utils;

import java.util.Map;
import java.util.Map.Entry;

import org.iucn.sis.client.api.models.ClientUser;
import org.iucn.sis.shared.api.debug.Debug;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.login.client.NewAccountPanel;

public class ProfileUtils {
	public static ClientUser buildUserFromProfile(NativeDocument profile, String username) {
		NativeElement root = profile.getDocumentElement();
		ClientUser currentUser = new ClientUser();
		currentUser.setUsername(username);

		// backwards compatibility for the moment...
		if (root.getElementsByTagName("creds").getLength() != 0) {
			Debug.println("Profile detected Old version...");
			currentUser.setFirstName(root.getElementByTagName("first").getTextContent());
			currentUser.setLastName(root.getElementByTagName("last").getTextContent());
			currentUser.setAffiliation(root.getElementByTagName("affiliation").getTextContent());
		} else {
			Debug.println("Profile detected New shiny version!");
			NativeNodeList nodes = root.getChildNodes();
			for( int i = 0; i < nodes.getLength(); i++ ) {
				NativeNode curNode = nodes.item(i);
				if( curNode.getNodeType() != NativeNode.ELEMENT_NODE )
					continue;
				String name = curNode.getNodeName();
				if( name.equals(NewAccountPanel.EMAIL_KEY))
					currentUser.setEmail(curNode.getTextContent());
				else if( name.equals(NewAccountPanel.FIRSTNAME_KEY)) 
					currentUser.setFirstName(curNode.getTextContent());
				else if( name.equals(NewAccountPanel.LASTNAME_KEY))
					currentUser.setLastName(curNode.getTextContent());
				else if( name.equals(NewAccountPanel.AFFILIATION_KEY))
					currentUser.setAffiliation(curNode.getTextContent());
				else
					currentUser.setProperty(name, curNode.getTextContent());
			}
			if (root.getElementByTagName("quickGroup") == null)
				currentUser.setProperty("quickGroup", "rlu");
		}
		
		return currentUser;
	}

	public static String profileParameterSerializer(Map<String, String> parameters) {
		StringBuffer ret = new StringBuffer();

		ret.append("<profile>\n");

		for (Entry<String, String> curEntry : parameters.entrySet()) {
			String tag = curEntry.getKey();

			if (tag != NewAccountPanel.PASSWORD_KEY)
				ret.append("<" + tag + ">" + curEntry.getValue() + "</" + tag + ">\n");
		}

//		ret.append("<quickGroup>'guest'</quickGroup>\n");

		ret.append("</profile>");

		return ret.toString();
	}

	private static String safeGetText(NativeElement root, String key) {
		NativeElement cur = root.getElementByTagName(key);

		if (cur != null)
			return cur.getTextContent();
		else
			return "N/A";
	}
}
