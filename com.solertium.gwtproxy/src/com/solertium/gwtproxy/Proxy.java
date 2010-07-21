package com.solertium.gwtproxy;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;

public class Proxy extends Application {
	
	final private String target;
	final private String httpBasicUsername;
	final private String httpBasicPassword;
	private ChallengeResponse savedChallengeResponse;
	
	final private static String DEFAULT_TARGET = "http://localhost:11001";
	
	public Proxy(){
		String paramTarget = System.getProperty("PROXY_TARGET");
		target = paramTarget == null ? DEFAULT_TARGET : paramTarget;
		httpBasicUsername = System.getProperty("PROXY_USERNAME");
		httpBasicPassword = System.getProperty("PROXY_PASSWORD");
	}
	
	public Proxy(Context context){
		super(context);
		String paramTarget = context.getParameters().getFirst("target").getValue();
		if(paramTarget == null) paramTarget = System.getProperty("PROXY_TARGET");
		target = paramTarget == null ? DEFAULT_TARGET : paramTarget;
		httpBasicUsername = System.getProperty("PROXY_USERNAME");
		httpBasicPassword = System.getProperty("PROXY_PASSWORD");
	}

	@Override
	public Restlet createRoot() {
		final Router root = new Router(getContext());

		Redirector redirector;
		if(httpBasicUsername==null || httpBasicPassword==null){
			redirector = new Redirector(getContext(),
					target+"{rr}",
					Redirector.MODE_DISPATCHER);
		} else {
			savedChallengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC,
					httpBasicUsername,httpBasicPassword);
			redirector = new Redirector(getContext(),
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

}
