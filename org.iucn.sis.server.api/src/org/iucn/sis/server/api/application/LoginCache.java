package org.iucn.sis.server.api.application;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.util.Couple;

public class LoginCache {

	private final Map<String, String> loginCache;
	private final SISDBAuthenticator authenticator;

	public LoginCache() {
		this.loginCache = new ConcurrentHashMap<String, String>();
		this.authenticator = new SISDBAuthenticator(null);
	}
	
	/**
	 * Add a new entry to the login cache.
	 * @param login the username
	 * @param password the password
	 * @param translate true to translate/encode the username/pw, false if this is already done. 
	 */
	public void add(String login, String password, boolean translate) {
		final Couple<String, String> credentials = translate(login, password, translate);
		
		loginCache.put(credentials.getFirst(), credentials.getSecond());
	}
	
	/**
	 * Remove an entry from the login cache
	 * @param login the username
	 * @param translate true to translate/encode the username, false if this is already done.
	 */
	public void remove(String login, boolean translate) {
		final Couple<String, String> credentials = translate(login, null, translate);
		
		loginCache.remove(credentials.getFirst());
	}
	
	/**
	 * Determine if credentials for the given user are cached with the 
	 * given password
	 * @param login the username
	 * @param password the potential password
	 * @param translate true to translate/encode the username/pw, false if this is already done.
	 * @return
	 */
	public boolean contains(String login, String password, boolean translate) {
		final Couple<String, String> credentials = translate(login, password, translate);
		
		return credentials.getSecond() != null && 
			credentials.getSecond().equals(loginCache.get(credentials.getFirst()));
	}
	
	/**
	 * Translates the username and password using SISDBAuthenticator's 
	 * methodology, unless the translated parameter is false
	 * @param login the username
	 * @param password the password
	 * @param translate true to translate/encode the username/pw, false if this is already done.
	 * @return a Couple containing the username (first) and the password (second)
	 */
	private Couple<String, String> translate(String login, String password, boolean translate) {
		final String translatedPW;
		final String translatedUN;
		if (translate) {
			translatedPW = password == null ? null : authenticator.translatePassword(login, password);
			translatedUN = authenticator.getSHA1Hash(login.toLowerCase());
		}
		else {
			translatedPW = password;
			translatedUN = login;
		}
		return new Couple<String, String>(translatedUN, translatedPW);
	}
	
}
