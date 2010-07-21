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

package com.solertium.gogoego.server.security;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gogoego.api.collections.Constants;
import org.gogoego.api.utils.MagicDisablingFilter;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Filter;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.templates.TemplateRegistry;
import com.solertium.util.MD5Hash;
import com.solertium.util.restlet.CookieUtility;
import com.solertium.util.restlet.RestletUtils;
import com.solertium.util.restlet.ScratchResource;
import com.solertium.util.restlet.ScratchResourceBin;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.restlet.SecurityFilter;

/**
 * GoGoSecurityFilter.java
 * 
 * Handles security checks for files.
 * 
 * 
 * @author carl.scott
 * 
 */
public class GoGoSecurityFilter extends SecurityFilter {

	protected final TemplateRegistry templateRegistry;
	protected final ScratchResourceBin scratchResourceBin;

	protected String key;

	public GoGoSecurityFilter(Context context, String key) {
		super(context);

		this.key = key;
		ServerApplication sa = ServerApplication.getFromContext(context);
		this.vfs = sa.getVFS();
		this.templateRegistry = sa.getTemplateRegistry();
		this.scratchResourceBin = sa.getScratchResourceBin();
	}

	/**
	 * A warning was found, let's check it out
	 */
	public int isInsecure(SecurityFilter.SecurityConditions conditions, Request request, Response response) {
		/*
		 * Lets not get in our own way. The first three checks ensure that we
		 * can differentiate between public calls that need to be monitored
		 * versus system calls that we should allow through.
		 */

		String bypass = RestletUtils.getHeader(request, "applicationKey");
		if (bypass != null && bypass.equals(key))
			return Filter.CONTINUE;

		System.out.println(request.getAttributes().keySet());
		if (!request.getAttributes().containsKey(Constants.SHORTCUT) && request.getProtocol().equals(Protocol.RIAP))
			return Filter.CONTINUE;

		if (request.getResourceRef().getPath().startsWith("/admin"))
			return Filter.CONTINUE;

		/*
		 * Unholy integration with Identity extension
		 */
		//FIXME Replace this with a smarter service-based mechanism ASAP
		if (request.getCookies().getFirst("Authorized-Identity") != null) {
			return Filter.CONTINUE;
		}

		if (conditions.getErrors().contains(VFSMetadata.PASSWORD_PROTECTED)) {
			HashMap<String, String> map = new HashMap<String, String>();
			String myURI = "/cookies/" + CookieUtility.associateHTTPBrowserID(request, response);
			if (scratchResourceBin.contains(myURI)) {
				HashMap<?, ?> input = (HashMap<?, ?>) scratchResourceBin.getResourceObject(myURI);
				Object iterator = input.entrySet().iterator();
				while (((Iterator<?>) iterator).hasNext()) {
					Map.Entry<?, ?> cur = (Map.Entry<?, ?>) ((Iterator<?>) iterator).next();
					map.put((String) cur.getKey(), (String) cur.getValue());
				}
			}

			if (request.getMethod().equals(Method.POST)) {
				Form form;
				try {
					form = new Form(request.getEntity());
				} catch (Exception e) {
					response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
					return Filter.STOP;
				}

				String input = form.getFirstValue("password");
				if (input == null) {
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return Filter.STOP;
				}

				map.put(conditions.getFailurePath(), new MD5Hash(input).toString());
			}

			String pw = conditions.getMetadata().getSecurityProperties().get(VFSMetadata.PASSWORD_PROTECTED);

			String input = map.get(conditions.getFailurePath());
			if (input == null || !input.equals(pw)) {
				String templateTag = templateRegistry.isRegistered("/templates/passwordprompt.html") ? "<meta name=\"template\" content=\"/templates/passwordprompt.html\" />"
						: "";
				String html = "<html><head><title>Login</title>" + templateTag + "</head><body>"
						+ "<div>Please login to see this page:</div>" + "<form method=\"POST\" action=\"\">"
						+ "<p>Enter Password: <input type=\"password\" name=\"password\" /></p>"
						+ "<input type=\"submit\" name=\"submitbutton\" value=\"Submit\" />" + "</form></body></html>";
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(new StringRepresentation(html, MediaType.TEXT_HTML));
				GoGoDebug.get("debug").println("Disabling caching because this is a login page");
				RestletUtils.addHeaders(response, "Cache-control", "no-cache");
				response.getAttributes().put(Constants.DISABLE_EXPIRATION, Boolean.TRUE);
				response.getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);
				return Filter.STOP;
			} else {
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE, 1);
				scratchResourceBin.add(new ScratchResource(new Reference(myURI), CookieUtility.associateHTTPBrowserID(
						request, response), c.getTime(), map));

				if (request.getMethod().equals(Method.GET))
					return Filter.CONTINUE;
				else if (request.getMethod().equals(Method.POST)) {
					response.setStatus(Status.REDIRECTION_FOUND);
					response.setLocationRef(request.getResourceRef());
					response.setEntity(null);

					return Filter.STOP;
				}
			}
		}

		response.setEntity(new StringRepresentation("This resource is inaccessible to the public"));
		response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		return Filter.STOP;
	}

}
