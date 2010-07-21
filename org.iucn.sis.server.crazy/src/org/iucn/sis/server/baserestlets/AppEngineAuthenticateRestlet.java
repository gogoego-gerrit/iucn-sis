package org.iucn.sis.server.baserestlets;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.util.Base64;
import org.restlet.representation.Representation;

import com.solertium.util.MD5Hash;

/**
 * Accepts incoming requests from the GoogleAccountsAuthButton and dispatches
 * required calls to the AppEngine app that performs GoogleAccounts user
 * validation.
 * 
 * @see com.solertium.gwt.utils.GoogleAccountsAuthButton
 * @author adam.schwartz
 */
public class AppEngineAuthenticateRestlet extends Restlet {
	private class ValidationTuple {
		private Date timestamp;
		private String userIDHash;

		public ValidationTuple(Date timestamp, String userIDHash) {
			this.timestamp = timestamp;
			this.userIDHash = userIDHash;
		}

		public boolean isTupleValid(String newIDHash) {
			Calendar oneMinuteLater = Calendar.getInstance();
			oneMinuteLater.setTime(timestamp);
			oneMinuteLater.add(Calendar.SECOND, TICKET_VALID_PERIOD_SECS);

			if (newIDHash.equals(userIDHash) && oneMinuteLater.getTime().after(new Date()))
				return true;
			else
				return false;
		}
	}
	private String targetAuthenticationURL;
	private MD5Hash hasher;
	private ConcurrentHashMap<String, ValidationTuple> ticketsIssued;
	private ConcurrentHashMap<String, String> signatures;

	private ConcurrentHashMap<Date, ClientInfo> badTicketAttempts;
	private KeyPair keyPair;
	private Signature signer;

	private Signature verifier;

	private final int TICKET_VALID_PERIOD_SECS = 60;

	/**
	 * Create an AppEngineAuthenticateRestlet, to commune with the
	 * uservalidation.py AppEngine application and the GoogleAccountsAuthButton
	 * class.
	 * 
	 * @param context
	 * @param googleAppAuthenticationURL
	 *            - Should be of the format (protocol)://(host)[:(port)
	 *            -optional] WITH NO TRAILING /
	 */
	public AppEngineAuthenticateRestlet(Context context, String googleAppAuthenticationURL) {
		super(context);

		ticketsIssued = new ConcurrentHashMap<String, ValidationTuple>();
		signatures = new ConcurrentHashMap<String, String>();
		badTicketAttempts = new ConcurrentHashMap<Date, ClientInfo>();
		targetAuthenticationURL = googleAppAuthenticationURL;
		hasher = new MD5Hash();

		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			keyPair = generator.generateKeyPair();

			signer = Signature.getInstance("SHA1withRSA");
			signer.initSign(keyPair.getPrivate());

			verifier = Signature.getInstance("SHA1withRSA");
			verifier.initSign(keyPair.getPrivate());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR initializing AppEngineAuthRestlet: No key " + "pairs generated.");
		}

	}

	private Response dispatchGetRequest(String resourceUri) {
		return getContext().getClientDispatcher().get(resourceUri);
	}

	private void doLogin(Request request, Response response) {
		String reentry = request.getResourceRef().getQueryAsForm().getFirstValue("reentry");
		String sigmaTicket = generateTicket(request);

		Response authResponse = dispatchGetRequest(targetAuthenticationURL + "/validation/login?reentry=" + reentry
				+ "&t=" + sigmaTicket);

		try {
			String redirect = authResponse.getEntity().getText();

			if (!redirect.startsWith("http"))
				redirect = targetAuthenticationURL + redirect;

			response.setEntity(redirect, MediaType.TEXT_PLAIN);
			response.setStatus(Status.SUCCESS_OK);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
					"Could not get login location. AppEngine might be down.");
		}
	}

	private void doValidate(Request request, Response response) {
		// Do this to avoid hangs.
		request.getEntity().toString();

		Form queryParams = request.getResourceRef().getQueryAsForm();

		String signature = queryParams.getFirstValue("t");
		String user = queryParams.getFirstValue("usr");
		String userHash = generateUserInfoHash(request);

		String ticket = signatures.get(signature);
		validateSignedTicket(signature, ticket);

		if (!ticketsIssued.containsKey(ticket)) {
			badTicketAttempts.put(new Date(), request.getClientInfo());

			String message = "The ticket provided was not issued by this authentiation "
					+ "agent. This login attempt and your user information have been logged, "
					+ "and will be reviewed.";
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, message);
		} else {
			if (!ticketsIssued.get(ticket).isTupleValid(userHash)) {
				badTicketAttempts.put(new Date(), request.getClientInfo());

				String message = "The user information provided is not the same as to "
						+ "whom the ticket was originally assigned. This login attempt "
						+ "and your user information have been logged, " + "and will be reviewed.";
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, message);
			} else {
				Response authResponse = dispatchGetRequest(targetAuthenticationURL + "/validation?t=" + signature
						+ "&usr=" + user);

				Representation responseEntity = authResponse.getEntity();
				if (responseEntity != null) {
					try {
						// Work-around annoying entity-needs-to-be-consumed
						// issues
						responseEntity.getText();
					} catch (Exception ignored) {
					}
				}

				if (authResponse.getStatus().isSuccess())
					response.setStatus(Status.SUCCESS_OK);
				else
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		}
	}

	private synchronized String generateTicket(Request request) {
		Date ts = new Date();
		hasher.update(ts.getTime() + "");
		String ticket = hasher.toString();
		String userHash = generateUserInfoHash(request);
		String sigmaTicket = getTicketSigned(ticket);

		ticketsIssued.put(ticket, new ValidationTuple(new Date(), userHash));
		signatures.put(sigmaTicket, ticket);

		return sigmaTicket;
	}

	private synchronized String generateUserInfoHash(Request request) {
		String userInfo = request.getClientInfo().getAddress();
		userInfo += request.getClientInfo().getAgent();
		// userInfo +=
		// request.getClientInfo().getAcceptedLanguages().hashCode();

		hasher.update(userInfo);
		return hasher.toString();
	}

	private void getLogoutURL(Request request, Response response) {
		String reentry = request.getResourceRef().getQueryAsForm().getFirstValue("reentry");

		// Response authResponse = dispatchGetRequest(new Reference(
		// targetAuthenticationURL+"/validation/logout?reentry=" + reentry));

		Response authResponse = dispatchGetRequest(targetAuthenticationURL + "/validation/logout?reentry=" + reentry);

		try {
			String redirect = authResponse.getEntity().getText();

			if (!redirect.startsWith("http"))
				redirect = targetAuthenticationURL + redirect;

			response.setEntity(redirect, MediaType.TEXT_PLAIN);
			response.setStatus(Status.SUCCESS_OK);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
					"Could not get logout location. AppEngine might be down.");
		}
	}

	/**
	 * Running the ticket through this function keeps someone else from
	 * generating tickets for this process. Returns the signature of the ticket.
	 * 
	 * @param unpermutedNonce
	 * @return keyed nonce
	 */
	private synchronized String getTicketSigned(String ticket) {
		try {
			signer.update(ticket.getBytes("UTF-8"));
			byte[] result = signer.sign();

			return Base64.encode(result, false).replaceAll("/", "x").replaceAll("\\+", "y");
		} catch (Exception e) {
			e.printStackTrace();
			return ticket;
		}
	}

	@Override
	public void handle(Request request, Response response) {
		try {
			if (request.getMethod() == Method.GET) {
				if (request.getResourceRef().getPath().endsWith("logout")) {
					getLogoutURL(request, response);
				} else {
					doLogin(request, response);
				}
			} else if (request.getMethod() == Method.POST) {
				doValidate(request, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	/**
	 * Running the ticket through this function keeps someone else from
	 * generating tickets for this process.
	 * 
	 * @param unpermutedNonce
	 * @return keyed nonce
	 */
	private synchronized boolean validateSignedTicket(String signature, String ticket) {
		try {
			verifier.update(ticket.getBytes("UTF-8"));
			byte[] result = verifier.sign();

			String shouldBe = Base64.encode(result, false).replaceAll("/", "x").replaceAll("\\+", "y");
			return shouldBe.equals(signature);
		} catch (Exception e) {
			e.printStackTrace();
			return signature.equals(ticket);
		}
	}

}
