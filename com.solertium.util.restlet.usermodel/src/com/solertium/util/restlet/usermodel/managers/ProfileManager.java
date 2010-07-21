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
package com.solertium.util.restlet.usermodel.managers;

import java.util.ArrayList;
import java.util.Map;

import org.restlet.resource.ResourceException;

import com.solertium.util.restlet.usermodel.core.Profile;

/**
 * ProfileManager.java
 * 
 * Interface for the support/implementation of a profile manager.  Needs 
 * to be able to perform CRUD operations on the implemented collection 
 * of profiles, and shall receive requests from a resource to do so.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface ProfileManager {
	
	public static final String PROFILE_MANAGER_INIT_KEY = "com.solertium.util.restlet.usermodel.managers.ProfileManager";
	
	/**
	 * Get the identifier for a user given the user name
	 * @param userName
	 * @return
	 * @throws ResourceException
	 */
	public String getUserID(String userName) throws ResourceException;

	/**
	 * Returns the name and profile for a given user
	 * @param username
	 * @return
	 */
	public Profile getProfile(String userID) throws ResourceException;
	
	/**
	 * Returns a lsit of all users
	 * @param 
	 * @return
	 */
	public ArrayList<String> getAllUsers() throws ResourceException;
	
	
	/**
	 * @deprecated add piece-meal
	 */
	//public void addProfile(Profile profile);
	
	/**
	 * Creates a new user entry with no profile information.
	 * @param userName the user
	 * @return the identifier for this entry
	 * @throws ResourceException
	 */
	public String addUser(String userName) throws ResourceException;
	
	/**
	 * Add a new field with the given key and value for 
	 * a particular user.
	 * @param userID
	 * @param fieldKey
	 * @param fieldValue
	 * @return the identifier for this field entry
	 * @throws ResourceException
	 */
	public String addField(String userID, String fieldKey, String fieldValue) throws ResourceException;
	
	/**
	 * Delete a user entry and their profile data. 
	 * @param username
	 * @throws ResourceException
	 */
	public void removeProfile(String userID) throws ResourceException;
	
	/**
	 * Remove a particular profile field for a user
	 * @param fieldID
	 * @throws ResourceException
	 */
	public void removeField(String userID, String fieldID) throws ResourceException;
	
	/**
	 * Change a username
	 * @param userID
	 * @param username
	 * @throws ResourceException
	 */
	public void updateUser(String userID, String username) throws ResourceException;
	
	/**
	 * Change a field value
	 * @param userID
	 * @param fieldID
	 * @param value
	 * @throws ResourceException
	 */
	public void updateField(String userID, String fieldID, String value) throws ResourceException;
	
	/**
	 * Retrive a mapping of all users.  The key should be the unique identifier, 
	 * the value should be the username.  Both will be returned to the client by 
	 * the resource.
	 * @return
	 * @throws ResourceException
	 */
	public Map<String, String> getUserMapping() throws ResourceException;
	
	/**
	 * @deprecated update piece-meal instead
	 */
	//public void updateProfile(String username, Profile profile);
	
	
	
	/**
	 * Determine if a user exists with a given username.
	 * @param username 
	 * @return true if so, false otherwise
	 * @throws ResourceException
	 */
	public boolean userExists(String username) throws ResourceException;

	
}
