/*
 * Copyright (C) 2000-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2008 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.util.restlet.authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.solertium.util.Mutex;
import com.solertium.util.TrivialExceptionHandler;

/**
 * FileAuthenticator.java
 *
 * @author carl.scott
 *
 */
public abstract class FileAuthenticator extends Authenticator {
	
	protected String oneOrMore = "(\\|\\w+:\\w+,\\w+\\|\\s*)*?";
	
	protected Mutex mutex;

	protected HashMap<String, String> loginCache;
	
	public FileAuthenticator() {
		super();
		this.loginCache = new HashMap<String, String>();
		this.mutex = new Mutex();
	}
	
	public boolean hasCachedSuccess(String login, String password) {
		String sha1Login = getSHA1Hash(login);
		String md5Pass = getMD5Hash(password + login);
		String sha1Pass = getSHA1Hash(password + login);
		
		return (md5Pass + "," + sha1Pass).equalsIgnoreCase(loginCache.get(sha1Login));
	}

	/** 
	 * @param login
	 * @param newPassword
	 * @return
	 */
	@Override
	public boolean changePassword(String login, String newPassword) {
		String sha1Login = getSHA1Hash(login);
		String md5Pass = getMD5Hash(newPassword + login);
		String sha1Pass = getSHA1Hash(newPassword + login);
		boolean success = false;
		
		if( acquireMutex() ) {
			String contents = getFileContents();
			
			Pattern p = Pattern.compile("(\\|" + sha1Login + ":\\w+,\\w+\\|\\s*)");
			Matcher m = p.matcher(contents);
						
			if( m.find() ) {
				contents = contents.replace(m.group(0), "|" + sha1Login + ":" + md5Pass + "," + sha1Pass + "|\n");
				writeFile(contents);
				success = true;
				loginCache.remove(sha1Login);
			}
				
			mutex.release();
		}
		
		return success;
	}

	/** 
	 * @param login
	 * @return
	 */
	@Override
	public boolean doesAccountExist(String login) {
		String sha1Login = getSHA1Hash(login);
		
		if( loginCache.containsKey(sha1Login) )
			return true;
		
		String contents = getFileContents();
		return contents.contains("|" + sha1Login + ":");
	}

	/** 
	 * @param login
	 * @param password
	 * @return
	 * @throws AccountExistsException
	 */
	@Override
	public String putNewAccount(String login, String password)
			throws AccountExistsException {
		if( doesAccountExist(login) )
			throw new AccountExistsException();
		
		String sha1Login = getSHA1Hash(login);
		String md5Pass = getMD5Hash(password + login);
		String sha1Pass = getSHA1Hash(password + login);
		
		if( acquireMutex() )
		{
			String contents = getFileContents();
			contents += "|" + sha1Login + ":" + md5Pass + "," + sha1Pass + "|\n";
			
			writeFile(contents);
			
			loginCache.put(sha1Login, md5Pass + "," + sha1Pass);
			
			mutex.release();
		}
		else
			return null;
		
		return "true";//.emailConfirmationCode(login);
	}

	/** 
	 * @param login
	 * @param password
	 * @return
	 */
	@Override
	public boolean validateAccount(String login, String password) {
		String sha1Login = getSHA1Hash(login);
		String md5Pass = getMD5Hash(password + login);
		String sha1Pass = getSHA1Hash(password + login);

		if( loginCache.containsKey( sha1Login ) )
			return loginCache.get(sha1Login).equalsIgnoreCase(md5Pass + "," + sha1Pass);
		else if( acquireMutex() )
		{
			String contents = getFileContents();
			System.out.println("Looking for " + sha1Login + ":");
					
			if( contents.contains("|" + sha1Login + ":" + md5Pass + "," + sha1Pass + "|") ) {
				loginCache.put(sha1Login, md5Pass + "," + sha1Pass);
					
				mutex.release();
				return true;
			}
			
			mutex.release();
		}
		
		return false;
	}
	
	protected void doFormat() {
		String contents = getFileContents();
		if (!contents.startsWith("|") && !contents.equals("")) {
			final ArrayList<String> lines = new ArrayList<String>();
			BufferedReader reader;
			try {
				reader = getReader(); 
			} catch (IOException e) {
				return;
			}
			
			try {
				String cur = "";
				while ((cur = reader.readLine()) != null)
					lines.add(cur);
			} catch (IOException e) {
				lines.clear();
			}
			
			try {
				reader.close(); 
			} catch (IOException unlikely) {
				TrivialExceptionHandler.ignore(this, unlikely);
			}
			
			if (!lines.isEmpty())
				writeFile("");
			else
				return;
			
			for (String line : lines)
				try {
					putNewAccount(line.split(":")[0], line.split(":")[1]);
				} catch (Authenticator.AccountExistsException e) {
					TrivialExceptionHandler.ignore(this, e);
				} catch (IndexOutOfBoundsException e) {
					TrivialExceptionHandler.ignore(this, e);
				}
		}
	}
	
	protected final boolean acquireMutex() {
		try	{
			return mutex.attempt(1000);
		}
		catch (InterruptedException e) {
			return false;
		}
	}

	protected boolean writeFile(String string) {
		Writer writer = null;
		
		try {
			writer = getWriter();
			writer.write(string);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				writer.close();
			}
			catch (Exception ignored) {}
		}
	}
	
	public abstract BufferedReader getReader() throws IOException;
	
	public abstract Writer getWriter() throws IOException;
	
	public abstract String getFileContents();
}
