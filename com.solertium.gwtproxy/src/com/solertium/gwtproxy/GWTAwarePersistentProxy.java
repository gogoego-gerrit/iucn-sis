package com.solertium.gwtproxy;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Message;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.util.Template;

public class GWTAwarePersistentProxy extends Application {
	
	final private String target;
	final private String httpBasicUsername;
	final private String httpBasicPassword;
	private ChallengeResponse savedChallengeResponse;
	
	final private static String DEFAULT_TARGET = "http://localhost:11001";
	final private static String PROXY_SERVICE_MOUNT = "/proxy-service";
	
	public GWTAwarePersistentProxy() {
		String paramTarget = System.getProperty("PROXY_TARGET");
		target = paramTarget == null ? DEFAULT_TARGET : paramTarget;
		httpBasicUsername = System.getProperty("PROXY_USERNAME");
		httpBasicPassword = System.getProperty("PROXY_PASSWORD");
	}
	
	public GWTAwarePersistentProxy(Context context) {
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
					target+PROXY_SERVICE_MOUNT+"{rr}",
					Redirector.MODE_DISPATCHER);
		} else {
			savedChallengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC,
					httpBasicUsername,httpBasicPassword);
			redirector = new PersistantRedirector(getContext(),
					target+PROXY_SERVICE_MOUNT+"{rr}",
					Redirector.MODE_DISPATCHER){
					public void handle(Request request, Response response){
						request.setChallengeResponse(savedChallengeResponse);
						super.handle(request, response);
					}
				};
		}
		root.attachDefault(redirector);
		
		Logger restlet = Logger.getLogger("org.restlet");
		restlet.setLevel(Level.SEVERE);
		restlet.setUseParentHandlers(false);

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
	        String rr = targetRef.getPath();
	        if (rr != null && (rr.startsWith(PROXY_SERVICE_MOUNT) || System.getProperty("PROXY_TARGET") != null)) {
	        	rr = rr.replaceFirst(PROXY_SERVICE_MOUNT, "");
	        	targetRef.setPath(rr);
	        }
	        // Update the request to cleanly go to the target URI
	        request.setResourceRef(targetRef);
	        // PersistantRedirector will keep request headers!
	        // request.getAttributes().remove("org.restlet.http.headers");
	        getContext().getClientDispatcher().handle(request, response);

	        String cacheControl = getHeader(response, "Cache-Control");
	        
	        // Allow for response rewriting and clean the headers
	        response.setEntity(rewrite(response.getEntity()));
	        response.getAttributes().remove("org.restlet.http.headers");
	        
	        // Add cache-control headers if found...
	        if (cacheControl != null)
		        addHeaders(response, "Cache-Control", cacheControl);

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
	
	static void addHeaders(final Message message, 
		final String headerName, final String headerValue) {
		Form headers = (Form) message.getAttributes().get("org.restlet.http.headers");
		if (headers == null) {
			headers = new Form();
			message.getAttributes().put("org.restlet.http.headers", headers);
		}
		headers.add(headerName, headerValue);
	}
	
	static String getHeader(final Message message, final String headerName) {
		String ret = null;
		try {
			final Form headers = (Form)message.getAttributes().get("org.restlet.http.headers");
			ret = headers.getFirstValue(headerName);
			if (ret == null)
				ret = headers.getFirstValue(headerName.toLowerCase());
		} catch (final Exception poorly_handled) {
			System.out.println("Restlet Header Miss: " + poorly_handled.getMessage());
		}
		return ret;
	}

}
