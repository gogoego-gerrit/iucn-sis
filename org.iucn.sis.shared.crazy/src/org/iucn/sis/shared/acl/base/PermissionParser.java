package org.iucn.sis.shared.acl.base;

import java.util.HashMap;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNamedNodeMap;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class PermissionParser {

	/**
	 * A HashMap that maintains mappings of Groups (e.g. Guest, RLU, SysAdmin, etc.) to
	 * their associated permission sink.
	 */
	private HashMap<String, PermissionGroup> groups;

	public PermissionParser(NativeDocument permissionDoc) {
		groups = new HashMap<String, PermissionGroup>();

		NativeNodeList groupEls = permissionDoc.getDocumentElement().getChildNodes();

		for( int i = 0; i < groupEls.getLength(); i++ ) {
			NativeNode curNode = groupEls.item(i);
			if( curNode.getNodeType() != NativeNode.ELEMENT_NODE )
				continue;

			final String groupName = curNode.getNodeName();
			PermissionGroup group;

			if( !groups.containsKey(groupName) )
				group = new PermissionGroup(groupName);
			else
				group = groups.get(groupName);

			NativeNodeList subList = curNode.getChildNodes();
			for( int j = 0; j < subList.getLength(); j++ ) {
				NativeNode subNode = subList.item(j);
				if( subNode.getNodeType() == NativeNode.ELEMENT_NODE ) {
					String subName = subNode.getNodeName();

					if( subName.equalsIgnoreCase("inherits") )
						processInheritsTag((NativeElement)subNode, group);
					else if( subName.equalsIgnoreCase("default") )
						processDefaultTag((NativeElement)subNode, group);
					else if( subName.equalsIgnoreCase("scope") )
						processScopeTag((NativeElement)subNode, group);
					else 
						processResourceTag((NativeElement)subNode, group);
				}
			}

			groups.put(group.getName(), group);
		}
	}

	public PermissionGroup getGroup(String groupName) {
		return groups.get(groupName);
	}

	public HashMap<String, PermissionGroup> getGroups() {
		return groups;
	}

	private void processResourceTag(NativeElement el, PermissionGroup group) {
		String name = el.getNodeName();
		PermissionResource resource = new PermissionResource(name + "/" + el.getAttribute("uri"), parsePermissionSet(el.getTextContent()));
		NativeNamedNodeMap attrs = el.getAttributes();
		for( int i = 0; i < attrs.getLength(); i++ ) {
			NativeNode attrEl = attrs.item(i);
			if( !attrEl.getNodeName().equals("uri") )
				resource.addAttribute(attrEl.getNodeName(), attrEl.getNodeValue());
		}

		group.addPermissionResource(resource);
	}

	private void processScopeTag(NativeElement el, PermissionGroup group) {
		group.setScopeURI(el.getAttribute("uri"));
	}

	protected void processInheritsTag(NativeElement el, PermissionGroup group) {
		String name = el.getTextContent();

		if( !groups.containsKey(name) )
			groups.put(name, new PermissionGroup(name));

		group.addInheritence(groups.get(name));
	}

	private void processDefaultTag(NativeElement el, PermissionGroup group) {
		PermissionSet set = parsePermissionSet(el.getTextContent());
		group.addPermissionResource(PermissionGroup.DEFAULT_PERMISSION_URI, set);
	}

	private PermissionSet parsePermissionSet(String permissionString) {
		PermissionSet set = new PermissionSet();

		if( permissionString == null || permissionString.equals("") )
			return set;

		String [] split;
		if( permissionString.indexOf(',') > -1 ) 
			split = permissionString.split(",");
		else
			split = new String[] { permissionString };

		for( int i = 0; i < split.length; i++ )
			switch( split[i].charAt(0) ) {
			case 'r':
				set.set(AuthorizableObject.READ, split[i].charAt(1) == ' ' ? null : split[i].charAt(1) == '+' );
				break;
			case 'w':
				set.set(AuthorizableObject.WRITE, split[i].charAt(1) == ' ' ? null : split[i].charAt(1) == '+' );
				break;
			case 'c':
				set.set(AuthorizableObject.CREATE, split[i].charAt(1) == ' ' ? null : split[i].charAt(1) == '+' );
				break;
			case 'd':
				set.set(AuthorizableObject.DELETE, split[i].charAt(1) == ' ' ? null : split[i].charAt(1) == '+' );
				break;
			case 'g':
				set.set(AuthorizableObject.GRANT, split[i].charAt(1) == ' ' ? null : split[i].charAt(1) == '+' );
				break;
			case 'u':
				set.set(AuthorizableObject.USE_FEATURE, split[i].charAt(1) == ' ' ? null : split[i].charAt(1) == '+' );
				break;
			default:
				break;
			}
		return set;
	}

}
