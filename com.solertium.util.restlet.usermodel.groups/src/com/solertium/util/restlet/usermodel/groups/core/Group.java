/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package com.solertium.util.restlet.usermodel.groups.core;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.util.BaseDocumentUtils;

/**
 * Group.java
 * 
 * Object representation of a group of users
 * 
 * @author david.fritz
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class Group {
	private String name;
	private String id;
	private ArrayList<String> users;
	
	public Group(String name, String id) {
		this.name = name;
		this.id = id;
		
		users = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}
	
	public String getID() {
		return id;
	}
	
	public void addUser(String profile){
		users.add(profile);
	}
	
	public void removeUser(String profile){
		users.remove(profile);
	}
	
	public ArrayList<String> getUsers(){
		return users;
	}
	
	public static Group fromXML(String xml){
		Document doc = BaseDocumentUtils.impl.createDocumentFromString(xml);
		String groupname = doc.getElementsByTagName("name").item(0).getTextContent();
		String groupID = doc.getDocumentElement().getAttribute("id");
		Group group = new Group(groupname, groupID);
		NodeList users = doc.getElementsByTagName("user");
		for(int i=0;i<users.getLength();i++){
			String name = ((Element)users.item(i)).getTextContent();
			group.users.add(name);
		}
		return group;
	}
	
	public String toXML(){
		String xml ="<group id=\"" + getID() + "\">";
		xml+="<name>"+name+"</name>";
		
		for(String user: users){
			xml+="<user>"+user+"</user>";
		}
		xml += "</group>";
		return xml;
	}
}
