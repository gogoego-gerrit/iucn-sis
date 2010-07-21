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
package com.solertium.util.restlet.authentication;

import org.restlet.data.Request;

import com.solertium.util.MD5Hash;
import com.solertium.util.SHA1Hash;

/**
 * Helper class for performing authentication.
 * 
 * @author adam.schwartz
 *
 */
public abstract class Authenticator implements Comparable<Authenticator> {
	public abstract boolean doesAccountExist(String login);
	
	public abstract boolean validateAccount(String login, String password);
	
	public abstract boolean changePassword(String login, String newPassword);
	
	public abstract String putNewAccount(String login, String password)
		throws AccountExistsException;
	
	protected int priority = 0;
	
	public String emailConfirmationCode(String email) {
		return "confirmationCode";
	}
	
	/**
	 * Function that can be overridden to do something after putting a new account
	 * @param username
	 * @param password
	 * @param confirmationCode
	 * @param request
	 */
	public void afterPutNewAccount(String username, String password, String confirmationCode, Request request) {
		//DEFAULT value is to do nothing
	}
	
	public String getMD5Hash(String in)
	{
		MD5Hash hash = new MD5Hash(in);
		return hash.toString();
	}
	
	public String getSHA1Hash(String in)
	{
		SHA1Hash hash = new SHA1Hash(in);
		return hash.toString();
	}
	
	public static class AccountExistsException extends Exception
	{
		private static final long serialVersionUID = 1L;
		public AccountExistsException()
		{
			super("Account already exists");
		}
		public AccountExistsException(String message)
		{
			super(message);
		}
	}
	
	public static class AccountNotFoundException extends Exception
	{
		private static final long serialVersionUID = 1L;
		public AccountNotFoundException()
		{
			super("Account does not exist");
		}
		public AccountNotFoundException(String message)
		{
			super(message);
		}
	}
	
	public boolean hasCachedSuccess(String login, String secret) {
		return false;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	/** 
	 * @param o
	 * @return
	 */
	public int compareTo(Authenticator o) {
		return (this.equals(o)) ? 0 : 
			this.priority < o.priority ? -1 : 1;
	}
	
	/**
	 * Override this to allow for the resetting of passwords
	 * 
	 * @param username
	 * @return
	 */
	public boolean resetPassword(String username) {
		return false;
	}
	
	/**
	 * Override this method to allow for the deleting of user accounts
	 * 
	 * @param username
	 * @return
	 * @throws AccountNotFoundException 
	 */
	public boolean deleteUser(String username) throws AccountNotFoundException {
		return false;
	}
	
	
	
	
}
