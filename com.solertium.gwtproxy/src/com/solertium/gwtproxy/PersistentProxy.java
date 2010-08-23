package com.solertium.gwtproxy;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.util.Template;

/**
 * PersistentProxy.java
 * 
 * This version works exactly like the Proxy except it 
 * subclasses the Redirector class in order to 
 * allow the HTTP headers to persist across the Proxy 
 * request.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 *
 */
public class PersistentProxy extends Application {
	
	final private String target;
	final private String httpBasicUsername;
	final private String httpBasicPassword;
	private ChallengeResponse savedChallengeResponse;
	
	final private static String DEFAULT_TARGET = "http://localhost:11001";
	
	public PersistentProxy() {
		String paramTarget = System.getProperty("PROXY_TARGET");
		target = paramTarget == null ? DEFAULT_TARGET : paramTarget;
		httpBasicUsername = System.getProperty("PROXY_USERNAME");
		httpBasicPassword = System.getProperty("PROXY_PASSWORD");
	}
	
	public PersistentProxy(Context context) {
		super(context);
		String paramTarget = context.getParameters().getFirst("target").getValue();
		if(paramTarget == null) paramTarget = System.getProperty("PROXY_TARGET");
		target = paramTarget == null ? DEFAULT_TARGET : paramTarget;
		httpBasicUsername = System.getProperty("PROXY_USERNAME");
		httpBasicPassword = System.getProperty("PROXY_PASSWORD");
	}

	public Restlet createRoot() {
		final Router root = new Router(getContext());
		
		Redirector redirector;
		if(httpBasicUsername==null || httpBasicPassword==null){
			redirector = new PersistantRedirector(getContext(),
					target+"{rr}",
					Redirector.MODE_DISPATCHER);
		} else {
			savedChallengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC,
					httpBasicUsername,httpBasicPassword);
			redirector = new PersistantRedirector(getContext(),
					target+"{rr}",
					Redirector.MODE_DISPATCHER){
					public void handle(Request request, Response response){
						request.setChallengeResponse(savedChallengeResponse);
						super.handle(request, response);
					}
				};
		}
		root.attachDefault(redirector);

		return root;
	}
	
	static class PersistantRedirector extends Redirector {
	
		public PersistantRedirector(Context context, String targetPattern, int mode) {
			super(context, targetPattern, mode);
		}

		protected void redirectDispatcher(Reference targetRef, Request request,
	            Response response) {
	        // Save the base URI if it exists as we might need it for redirections
	        final Reference baseRef = request.getResourceRef().getBaseRef();

	        // Update the request to cleanly go to the target URI
	        request.setResourceRef(targetRef);
	        // PersistantRedirector will keep request headers!
	        // request.getAttributes().remove("org.restlet.http.headers");
	        getContext().getClientDispatcher().handle(request, response);

	        // Allow for response rewriting and clean the headers
	        response.setEntity(rewrite(response.getEntity()));
	        response.getAttributes().remove("org.restlet.http.headers");

	        // In case of redirection, we may have to rewrite the redirect URI
	        if (response.getLocationRef() != null) {
	            final Template rt = new Template(this.targetTemplate);
	            rt.setLogger(getLogger());
	            final int matched = rt.parse(response.getLocationRef().toString(),
	                    request);

	            if (matched > 0) {
	                final String remainingPart = (String) request.getAttributes()
	                        .get("rr");

	                if (remainingPart != null) {
	                    response.setLocationRef(baseRef.toString() + remainingPart);
	                }
	            }
	        }
	    }
	}

}
