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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.util.Base64;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Filter;
import org.restlet.security.Guard;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.RestletUtils;
import com.solertium.util.restlet.authentication.Authenticator.AccountExistsException;
import com.solertium.util.restlet.authentication.Authenticator.AccountNotFoundException;

/**
 * Provides an authentication medium that verifies a user's credentials, then
 * returns a ticket signed by this restlet that can be used by other Restlets to
 * verify the user's authentication credentials.
 * 
 * @author adam.schwartz
 * 
 *         A user is allowed to attempt validation on a single identifier up to
 *         MAX_TRIES times, after which they will be forbidden.
 * 
 * @author carl.scott
 * 
 */
public abstract class AuthnGuard extends Guard {
	protected Map<String, Authenticator> authenticators;
	protected HashMap<String, PendingUser> confirmationCodes;

	protected final Map<String, LockStatus> checking;

	public static final String SCOPE_COMPONENT = "component";
	public static final String SCOPE_HOST = "host";

	protected String scope = SCOPE_HOST;

	protected final static Integer DENY = Integer.valueOf(0);

	protected boolean mustConfirmNewUsers = false;
	protected int MAX_TRIES = 3;

	public AuthnGuard(final Context context, final ChallengeScheme scheme, final String realm) {
		super(context, scheme, realm);

		checking = new HashMap<String, LockStatus>();
		confirmationCodes = new HashMap<String, PendingUser>();
		authenticators = new HashMap<String, Authenticator>();
		setDefaultAuthenticator();
	}

	public void setMustConfirmNewUsers(boolean mustConfirmNewUsers) {
		this.mustConfirmNewUsers = mustConfirmNewUsers;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public void addAuthenticator(String domain, Authenticator authenticator) {
		authenticators.put(domain, authenticator);
	}

	@Override
	public void accept(final Request request, final Response response) {
		if (!request.getResourceRef().getPath().startsWith("/authn")) {
			getNext().handle(request, response);
		} else {
			response.setStatus(Status.SUCCESS_OK, "Authorization Accepted.");
		}
	}

	protected abstract void setDefaultAuthenticator();

	protected abstract boolean bypassAuth(Request request);

	protected void doChallenge(final Response response) {
		response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
		response.setChallengeRequest(new ChallengeRequest(getScheme(), getRealm()));
	}

	protected Authenticator getAuthenticator(String domain) {
		if (domain == null)
			return authenticators.get(getRealm());
		else
			return authenticators.get(domain);
	}

	@Override
	public final int doHandle(final Request request, final Response response) {
		final ChallengeResponse challenge = request.getChallengeResponse();
		final String domain = RestletUtils.getHeader(request, "authenticatorDomain");

		if (request.getResourceRef().getRemainingPart().startsWith("/origin")) {
			try {
				Document doc;
				try {
					String text = request.getEntity().getText();
					doc = BaseDocumentUtils.impl.createDocumentFromString(text);
				} catch (Exception e) {
					try {
						System.out.println("Entity is " + request.getEntity().getText());
					} catch (Exception f) {
						System.out.println("Could not display contents...");
						TrivialExceptionHandler.ignore(this, e);
					}
					throw e;
				}

				String u = doc.getElementsByTagName("u").item(0).getTextContent();
				String p = doc.getElementsByTagName("p").item(0).getTextContent();

				String key = u + "_" + (domain == null ? getRealm() : domain);
				LockStatus lock = checking.get(key);

				if (u == null || p == null) {
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply "
							+ "the necessary auth values. It is possible your browser "
							+ "does not support our authentication implementation.");
				} else if (lock != null && lock.isLocked()) {
					getContext().getLogger().info("User locked out: " + challenge.getIdentifier());
					if (lock.isLockExpired()) {
						lock.unlock();
						response.setEntity("Invalid login credentials.", MediaType.TEXT_PLAIN);
						response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
						return Guard.AUTHENTICATION_STALE;
					} else {
						response.setEntity("Maximum invalid attempts has been "
								+ "reached for this username. Please try again later.", MediaType.TEXT_PLAIN);
						response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
						return Guard.AUTHENTICATION_INVALID;
					}
				} else if (validate(u, p.toCharArray(), domain)) {
					String base64Encoded = u + ":" + p;
					response.setEntity(Base64.encode(base64Encoded.getBytes(), false), MediaType.TEXT_PLAIN);
					response.setStatus(Status.SUCCESS_ACCEPTED);
					checking.remove(u);
				} else {
					incrementLock(u, (domain == null ? getRealm() : domain));
					response.setEntity("Invalid login credentials.", MediaType.TEXT_PLAIN);
					response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
				}
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply "
						+ "the necessary auth values. It is possible your browser "
						+ "does not support our authentication implementation.");
			}
		} else if (request.getResourceRef().getRemainingPart().startsWith("/confirm")) {
			/*
			 * Want to find the confirmation code from the URL and see if it's
			 * mapped to anything. If so, then we want to create a new account
			 * based on the supplied information.
			 */
			String confirmCode = request.getResourceRef().getLastSegment();
			PendingUser user = confirmationCodes.remove(confirmCode);
			if (user == null) {
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				response.setEntity(new DomRepresentation(MediaType.TEXT_XML, BaseDocumentUtils.impl
						.createErrorDocument("Confirmation code invalid.")));
			} else {
				String error = null;
				try {
					getAuthenticator(domain).putNewAccount(user.username, user.password);
				} catch (Authenticator.AccountExistsException unlikely) {
					error = unlikely.getLocalizedMessage();
				}
				if (error == null) {
					response.setStatus(Status.SUCCESS_CREATED);
					response.setEntity(new DomRepresentation(MediaType.TEXT_XML, BaseDocumentUtils.impl
							.createConfirmDocument("New Account \"" + user.username + "\" created.")));
				} else {
					response.setStatus(Status.CLIENT_ERROR_CONFLICT);
					response.setEntity(new DomRepresentation(MediaType.TEXT_XML, BaseDocumentUtils.impl
							.createErrorDocument(error)));
				}
			}
		} else if (request.getResourceRef().getRemainingPart().startsWith("/reset")) {

			try {
				Document doc = new DomRepresentation(request.getEntity()).getDocument();
				String u = doc.getElementsByTagName("u").item(0).getTextContent();
				if (getAuthenticator(domain).resetPassword(u)) {
					response.setStatus(Status.SUCCESS_OK);
				} else {
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}

			} catch (IOException e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
				e.printStackTrace();
			}
		} else if (request.getResourceRef().getRemainingPart().startsWith("/authn/remove")) {
			doRemoveUser(request, response, domain);
		} else if (request.getResourceRef().getPath().endsWith("/authn")) {
			if (Method.PUT.equals(request.getMethod())) {
				handleAuthnPut(request, response, domain);
			} else if (request.getMethod() == Method.POST) {
				try {
					Document doc = new DomRepresentation(request.getEntity()).getDocument();

					String u = doc.getElementsByTagName("u").item(0).getTextContent();
					String oldP = doc.getElementsByTagName("oldP").item(0).getTextContent();
					String newP = doc.getElementsByTagName("newP").item(0).getTextContent();

					if (validate(u, oldP.toCharArray(), domain)) {
						if (getAuthenticator(domain).changePassword(u, newP)) {
							String base64Encoded = u + ":" + newP;
							response.setEntity(Base64.encode(base64Encoded.getBytes(), false), MediaType.TEXT_PLAIN);
							response.setStatus(Status.SUCCESS_OK);
						} else
							response.setStatus(Status.CLIENT_ERROR_LOCKED);
					} else
						response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
				} catch (IOException e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
					e.printStackTrace();
				}
			} else
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		} else if (challenge != null) {
			final String key = challenge.getIdentifier() + "_" + (domain == null ? getRealm() : domain);
			final LockStatus lock = checking.get(key);
			if (bypassAuth(request)) {
				accept(request, response);
				return Filter.CONTINUE;
			} else if (lock != null && lock.isLocked()) {
				getContext().getLogger().info("User locked out: " + challenge.getIdentifier());
				if (lock.isLockExpired()) {
					lock.unlock();
					response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
					return Guard.AUTHENTICATION_STALE;
				} else {
					response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
					return Guard.AUTHENTICATION_INVALID;
				}
			} else if (validate(challenge.getIdentifier(), challenge.getSecret(), domain)) {
				checking.remove(key);
				accept(request, response);
			} else {
				respondToFailure(challenge.getIdentifier(), domain, response);
			}
		} else {
			if (bypassAuth(request)) {
				accept(request, response);
				return Filter.CONTINUE;
			} else
				doChallenge(response);
		}

		return Filter.CONTINUE;
	}

	protected void doRemoveUser(final Request request, final Response response, final String domain) {
		String currentUser = null;
		if (!bypassAuth(request)) {
			currentUser = request.getChallengeResponse().getIdentifier();
		}

		String userToDelete = null;
		try {
			userToDelete = new DomRepresentation(request.getEntity()).getDocument().getDocumentElement().getElementsByTagName("u")
					.item(0).getTextContent();
		} catch (DOMException e1) {
			e1.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (IOException e1) {
			e1.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}

		if (userToDelete == null || (currentUser == null && !bypassAuth(request))) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else if (!bypassAuth(request) && currentUser.equals(getAuthenticator(domain).getSHA1Hash(userToDelete))) {
			response.setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED, "You can not delete yourself.");
		} else {
			try {
				getAuthenticator(domain).deleteUser(userToDelete);
				response.setStatus(Status.SUCCESS_OK);
			} catch (AccountNotFoundException e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		}
	}

	protected void handleAuthnPut(final Request request, final Response response, final String domain) {
		try {
			Document doc = new DomRepresentation(request.getEntity()).getDocument();

			String u = doc.getElementsByTagName("u").item(0).getTextContent();
			String p = doc.getElementsByTagName("p").item(0).getTextContent();

			if (mustConfirmNewUsers) {
				if (getAuthenticator(domain).doesAccountExist(u))
					throw new Authenticator.AccountExistsException();

				final String confirmCode = getAuthenticator(domain).emailConfirmationCode(u);
				if (confirmCode != null) {
					final PendingUser user = new PendingUser();
					user.username = u;
					user.password = p;
					confirmationCodes.put(confirmCode, user);
				} else
					response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
			} else {
				String confirmCode = getAuthenticator(domain).putNewAccount(u, p);
				if (confirmCode != null) {
					response.setStatus(Status.SUCCESS_OK);

					// this may override your status and entity...
					addNewProfile(u, p, response, doc);

					getAuthenticator(domain).afterPutNewAccount(u, p, confirmCode, request);

					if (response.getEntity() == null || !response.getEntity().isAvailable()) {
						String base64Encoded = u + ":" + p;
						response.setEntity(Base64.encode(base64Encoded.getBytes(), false), MediaType.TEXT_PLAIN);
					}
				} else
					response.setStatus(Status.CLIENT_ERROR_FAILED_DEPENDENCY);
			}
		} catch (AccountExistsException e) {
			response.setStatus(Status.CLIENT_ERROR_CONFLICT, e.getLocalizedMessage());
		} catch (IOException e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			e.printStackTrace();
		}
	}

	/**
	 * Immediately after a new user is added, you may opt to add a profile.
	 * 
	 * @param username
	 * @param password
	 * @param response
	 * @param doc
	 *            -- the request entity document
	 */
	protected void addNewProfile(String username, String password, Response response, Document doc) {
		Request req = new Request(Method.PUT, "riap://" + scope + "/profile/" + username, new StringRepresentation(
				"<empty/>", MediaType.TEXT_XML));
		req.setChallengeResponse(new ChallengeResponse(getScheme(), username, password));

		getContext().getServerDispatcher().handle(req);
	}

	private void respondToFailure(String identifier, String domain, Response response) {
		incrementLock(identifier, domain);

		if (isOutOfTries(identifier, domain))
			response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		else
			doChallenge(response);
	}

	private void incrementLock(String identifier, String domain) {
		final String key = identifier + "_" + (domain == null ? getRealm() : domain);

		LockStatus lock = checking.get(key);
		if (lock == null) {
			lock = new LockStatus();
			lock.tries = 0;
		}

		int tries = lock.tries.intValue();
		tries++;

		if (tries >= MAX_TRIES) {
			lock.tries = DENY;
			lock.lock();
		} else
			lock.tries = Integer.valueOf(tries);

		checking.put(identifier, lock);
	}

	public void setMaxTries(int tries) {
		this.MAX_TRIES = tries;
	}

	private boolean isOutOfTries(String identifier, String domain) {
		final String key = identifier + "_" + (domain == null ? getRealm() : domain);
		return checking.containsKey(key) && DENY == checking.get(key).tries;
	}

	/**
	 * Performs validation using an Authenticator specified by the domain.
	 * 
	 * @param id
	 * @param secret
	 * @param domain
	 * 
	 * @return whether they're valid user or not
	 */
	protected boolean validate(String id, char[] secret, String domain) {
		return getAuthenticator(domain).validateAccount(id, new String(secret));
	}

	protected boolean validate(String id, char[] secret) {
		return getAuthenticator(null).validateAccount(id, new String(secret));
	}

	static class LockStatus {
		private Integer tries;
		private Date lockTime;

		public void setTries(int tries) {
			this.tries = Integer.valueOf(tries);
		}

		public void lock() {
			lockTime = Calendar.getInstance().getTime();
		}

		public void unlock() {
			lockTime = null;
		}

		public boolean isLocked() {
			return lockTime != null;
		}

		public boolean isLockExpired() {
			Date now = Calendar.getInstance().getTime();

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(lockTime.getTime());
			cal.add(Calendar.MINUTE, 5);
			Date fiveMinsLater = cal.getTime();

			return fiveMinsLater.after(now);
		}
	}

	static class PendingUser {
		private String username;
		private String password;
	}
}
