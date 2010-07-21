/**
 * 
 */
package com.solertium.util.restlet.authorization.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

import com.solertium.util.restlet.authorization.base.Authorizer;

/**
 * AuthorizationFilter.java
 *
 * @author carl.scott <carl.scott@solertium.com>
 *
 */
public class AuthorizationFilter extends Filter {
	
	protected final HashMap<String, Collection<Method>> translator;
	protected final Authorizer baseAuthorizer;
	
	protected final Collection<Authorizer> additionalAuthorizers;
	
	public AuthorizationFilter(final Context context, final Authorizer baseAuthorizer) {
		super(context);
		this.baseAuthorizer = baseAuthorizer;
		this.translator = new HashMap<String, Collection<Method>>();
		
		this.additionalAuthorizers = new ArrayList<Authorizer>();
		
		final Collection<Method> create = new ArrayList<Method>(); {
			create.add(Method.PUT);
			create.add(Method.MKCOL);
			create.add(Method.COPY);
			create.add(Method.MOVE);
		} translator.put("create", create);
		
		final Collection<Method> read = new ArrayList<Method>(); {
			read.add(Method.GET);
			read.add(Method.HEAD);
			read.add(Method.OPTIONS);
			read.add(Method.PROPFIND);
		 read.add(Method.PROPPATCH);
		} translator.put("read", read);
		
		final Collection<Method> update = new ArrayList<Method>(); {
			update.add(Method.POST);
		} translator.put("update", update);
		
		final Collection<Method> delete = new ArrayList<Method>(); {
			delete.add(Method.DELETE);
		} translator.put("delete", delete);
	}
	
	public void addAdditionalAuthorizer(final Authorizer authorizer) {
		additionalAuthorizers.add(authorizer);
	}
	
	protected int beforeHandle(Request request, Response response) {
		final String uri = getUri(request, response);
		final String action = getAction(request, response);
		final String actor = getActor(request, response);
		
		if (uri == null || action == null) {
			/*
			 * Assume that this uri or method is not under 
			 * authorization control at this level.
			 */
			return Filter.CONTINUE;
		}
		
		if (baseAuthorizer.isAuthorized(uri, actor, action)) {
			int status = Filter.CONTINUE;
			for (final Iterator<Authorizer> iterator = getAdditionalAuthorizers().iterator(); 
				iterator.hasNext() && (status = iterator.next().isAuthorized(uri, actor, action) ? 
						Filter.CONTINUE : Filter.STOP) == Filter.CONTINUE;);
			return status;
		} else {
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			return Filter.STOP;
		}
	}
	
	protected Collection<Authorizer> getAdditionalAuthorizers() {
		return additionalAuthorizers; 
	}
	
	/**
	 * If you want to re-map the true URI to something else, 
	 * override this method.  Otherwise, it will simply 
	 * return the remaining path of the URI
	 * @param request
	 * @return the uri
	 */
	protected String getUri(final Request request, final Response response) {
		return request.getResourceRef().getRemainingPart();
	}
	
	protected String getAction(final Request request, final Response response) {
		String action = null;
		for (Map.Entry<String, Collection<Method>> entry : translator.entrySet()) {
			if (entry.getValue().contains(request.getMethod())) {
				action = entry.getKey();
				break;
			}
		}
		return action;
	}
	
	/**
	 * You'll probably want to override this with the appropriate entity 
	 * ID, probably located from the cookies of the request object.
	 * @param request
	 * @param response
	 * @return
	 */
	protected String getActor(final Request request, final Response response) {
		return null;
	}

}
