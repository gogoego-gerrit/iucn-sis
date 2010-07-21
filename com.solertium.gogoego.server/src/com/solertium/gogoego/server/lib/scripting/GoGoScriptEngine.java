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
package com.solertium.gogoego.server.lib.scripting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.gogoego.api.collections.Constants;
import org.gogoego.api.representations.GoGoEgoRepresentation;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import com.solertium.gogoego.server.cm.PluginAgent;

/**
 * GoGoScriptEngine.java
 * 
 * Provide dynamic scripting for full-blown script files.
 * 
 * @author rob.heittman
 *
 */
public class GoGoScriptEngine {

	private static GoGoScriptEngine instance;
	public List<String> scriptExtensions;

	private Context context;

	public static GoGoScriptEngine getInstance(Context context) {
		if (instance == null)
			instance = new GoGoScriptEngine(context);
		return instance;
	}	

	private GoGoScriptEngine(Context context) {
		this.context = context;

		/*
		 * FIXME: this should use OSGi fun now instead.
		 */
		final ArrayList<String> iScriptExtensions = new ArrayList<String>(3);
		iScriptExtensions.add("js");
		iScriptExtensions.add("py");
		iScriptExtensions.add("rb");

		scriptExtensions = new CopyOnWriteArrayList<String>(iScriptExtensions);
	}

	public List<String> getScriptExtensions() {
		return scriptExtensions;
	}

	public void doScripting(final Request request, final Response response) {
		final String uri = request.getResourceRef().getPath();
		if (uri.lastIndexOf(".") > -1) {
			final String extension = uri.substring(uri.lastIndexOf(".") + 1);
			if (!scriptExtensions.contains(extension))
				return;
			String content = null;
			Representation entity = response.getEntity();
			if (entity == null)
				return;

			try {
				content = entity.getText();
			} catch (final IOException unlikely) {
				throw new RuntimeException("Could not read script entity into text");
			}

			boolean serverParsed = true;
			if ("js".equals(extension))
				try {
					final BufferedReader br = new BufferedReader(new StringReader(content));
					final String first = br.readLine();
					br.close();
					if ((first == null) || (first.indexOf("server-parsed") == -1))
						serverParsed = false;
				} catch (final IOException okay) {
					serverParsed = false;
				}

			final ScriptEngine engine = PluginAgent.getScriptEngineBroker().getEngineByExtension(extension);
			if (serverParsed && (engine != null)) {
				final StringWriter sw = new StringWriter(4096);
				final ScriptContext ctx = engine.getContext();
				ctx.setWriter(sw);
				ctx.setErrorWriter(sw);
				engine.setContext(ctx);
				engine.put("document", sw);
				engine.put("request", request);
				engine.put("uri", request.getResourceRef().getPath());

				final GoGoEgoRepresentation baseRepresentation = (GoGoEgoRepresentation) request.getAttributes().get(
						Constants.BASE_REPRESENTATION);
				if (baseRepresentation != null) {
					/*final Reference baseReference = (Reference) request.getAttributes().get(
							GoGoMagicFilter.BASE_REFERENCE);*/
					try {
						engine.put("base", baseRepresentation);
					} catch (final Exception e) {
						engine.put("base", null);
					}
				}

				Object res = null;
				try {
					res = engine.eval(content);
				} catch (final Exception e) {
					sw.write("\n\nError:\n" + e.getClass().getCanonicalName() + ":\n" + e.getMessage());
					if (e.getCause() != null)
						sw.write("\nCaused by:\n" + e.getClass().getCanonicalName() + ":\n" + e.getMessage());
				}
				String all = sw.toString();
				if ("".equals(all))
					all = all + res;
				MediaType typ = MediaType.TEXT_PLAIN;
				if (all.startsWith("<?"))
					typ = MediaType.TEXT_XML;
				else if (all.startsWith("<"))
					typ = MediaType.TEXT_HTML;

				all += engine.getBindings(0).keySet().toString();
				entity = new StringRepresentation(all, typ);
				response.setEntity(entity);
			} else
				response.setEntity(new StringRepresentation(content, MediaType.TEXT_PLAIN));
		}
	}

}
