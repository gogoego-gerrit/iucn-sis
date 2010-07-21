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
package com.solertium.util.restlet.usermodel.core;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.portable.XMLWritingUtils;

/**
 * Profile.java
 * 
 * Represents a user profile and data
 * 
 * @author david.fritz
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class Profile {
	
	private long id;
	private String username;
	private HashMap<String, String> userData;
	
	public Profile() {
		userData= new HashMap<String, String>();
	}
	
	public Profile(String username) {
		this.username= username;
		userData= new HashMap<String, String>();
	}

	public Profile(String username, HashMap<String, String> userData) {
		this.userData = userData;
		this.username=username;
	}
	
	public void setId(long id){
		this.id=id;
	}
	
	public long getId() {
		return id;
	}
	
	public String getName(){
		return username;
	}
	
	public HashMap<String, String> getData(){
		return userData;
	}
	
	public static Profile fromXML(String xml){
		Document doc = BaseDocumentUtils.impl.createDocumentFromString(xml);
		String username = doc.getElementsByTagName("name").item(0).getTextContent();
		Profile profile = new Profile(username);
		NodeList data = doc.getElementsByTagName("data");
		for(int i=0;i<data.getLength();i++){
			String key = ((Element)data.item(i)).getAttribute("id");
			String value = ((Element)data.item(i)).getTextContent();
			profile.userData.put(key, value);
		}
		return profile;
	}
	
	public String toXML() {
		String xml = "<profile>";
		xml += XMLWritingUtils.writeTag("name", username);
		
		for (Map.Entry<String, String> entry : userData.entrySet())
			xml += "<data id=\"" + entry.getKey() + "\">" + 
				entry.getValue() + "</data>";
		
		xml += "</profile>";
		return xml;
	}
	
}
