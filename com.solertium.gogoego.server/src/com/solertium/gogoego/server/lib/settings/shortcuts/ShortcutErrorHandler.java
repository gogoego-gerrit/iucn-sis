package com.solertium.gogoego.server.lib.settings.shortcuts;

import org.gogoego.api.errors.ErrorHandler;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.GoGoMagicFilter;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.ViewFilter;

public class ShortcutErrorHandler implements ErrorHandler {
	
	public static final String BUNDLE_KEY = "com.solertium.gogoego.server.shortcuts";

	public Response handle404(Context context, Request request) throws ResourceException {
		String s = request.getResourceRef().getPath();
		String origS = s;
		String rr = "";
		while(s.indexOf("/")>=0){
			GoGoDebug.get("fine").println("404, shortcuts for ? {0}", s);
			String t = null;
			try {
				t = ServerApplication.getFromContext(context).getShortcut(s);
			} catch (ClassCastException e) {
				/*
				 * FIXME: there's a context issue if this occurs...
				 */
				GoGoDebug.get("error").println("Failure, but continuing: {0}", e.getMessage());
				t = null;
			}
			if (t != null) {
				t=t+rr;
				GoGoDebug.get("fine").println(" --- yes, shortcut is {0}", t);
				String query = request.getResourceRef().getQuery();
				if (query == null || query.equals(""))
					query = "";
				t = t + query;
				Request newReq = GoGoMagicFilter.newRequest(request.getMethod(), t, new StringRepresentation("",MediaType.TEXT_PLAIN), request);
				
				// special case here requires the page tree to be copied...
				request.getAttributes().put(ViewFilter.PAGE_TREE, newReq.getAttributes().get(ViewFilter.PAGE_TREE));
				
				return context.getClientDispatcher().handle(newReq);
			}
			int ptr = s.lastIndexOf("/");
			if(ptr>-1){
				rr = origS.substring(ptr);
				s = s.substring(0,ptr); // trim a chunk
			}
		}
		return null;
	}

}
