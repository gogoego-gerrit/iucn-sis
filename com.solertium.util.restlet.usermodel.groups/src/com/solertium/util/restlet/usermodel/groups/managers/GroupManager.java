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
package com.solertium.util.restlet.usermodel.groups.managers;

import java.util.Map;

import org.restlet.resource.ResourceException;

import com.solertium.util.restlet.usermodel.groups.core.Group;

/**
 * GroupManager.java
 * 
 * Provides the implementation behind storage of a user group.  Should 
 * be able to handle CRUD operations in regard to user groups.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface GroupManager {
	
	public static final String GROUP_MANAGER_INIT_KEY = "com.solertium.util.restlet.usermodel.groups.core.GroupManager";
	
	/**
	 * Fetch a particular group (GET)
	 * @param groupname
	 * @return
	 */
	public Group getGroup(String groupID) throws ResourceException;
	
	/**
	 * Fetch a group list(PROPFIND)
	 * @param groupname
	 * @return
	 */
	public Map<String, String> getGroupMapping() throws ResourceException;
	
	/**
	 * Create a new group (PUT)
	 * @param groupname
	 * @return the identifier for the new group
	 */
	public String addGroup(String groupName) throws ResourceException;
	
	/**
	 * Add a new user to a group.  The userID should come from 
	 * the profile table.
	 * @param groupID
	 * @param userID
	 * @return
	 * @throws ResourceException
	 */
	public String addUser(String groupID, String userID) throws ResourceException;
	
	/**
	 * Remove an existing group (DELETE)
	 * @param groupname
	 */
	public void removeGroup(String groupID) throws ResourceException;
	
	/**
	 * Remove a user from a group
	 * @param groupID
	 * @param userID
	 * @throws ResourceException
	 */
	public void removeUser(String groupID, String userID) throws ResourceException;
	
	/**
	 * Update an existing group
	 * @param groupname
	 * @param group
	 */
	public void updateGroup(String groupID, String groupName) throws ResourceException;
	
	/**
	 * Get the group given the user identifier.
	 * @param user
	 */
	public Group getGroupForUser(String userIdentifier) throws ResourceException;
		
	
}
