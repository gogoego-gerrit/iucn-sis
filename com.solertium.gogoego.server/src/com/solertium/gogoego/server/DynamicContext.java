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

package com.solertium.gogoego.server;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.gogoego.api.applications.TemplateRegistryAPI;
import org.gogoego.api.collections.Constants;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.representations.GoGoEgoRepresentation;
import org.gogoego.api.representations.TouchListener;
import org.gogoego.api.representations.Trap;
import org.gogoego.api.scripting.ELEntity;
import org.gogoego.api.scripting.ReflectingELEntity;
import org.gogoego.api.scripting.ScriptableObjectFactory;
import org.gogoego.api.scripting.ScriptedPages;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.gogoego.server.lib.caching.MemoryCache;
import com.solertium.gogoego.server.lib.caching.MemoryCacheContents;
import com.solertium.gogoego.server.lib.representations.ImageUtils;
import com.solertium.gogoego.server.lib.resources.PageTreeNode;
import com.solertium.util.Replacer;
import com.solertium.util.TagFilter;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.InternalRequest;

public class DynamicContext implements ELEntity {

	public static class ScriptableObjectResolver extends ReflectingELEntity {
		Request request;
		Context context;
		Reference baseReference;
		ImageUtils resizer;
		Form formEntity;
		public ScriptableObjectResolver(Request request, Context context, final Reference baseReference){
			this.request = request;
			this.context = context;
			this.baseReference = baseReference;
			this.formEntity = null;
		}
		public String getParameter(String parameterName){
			String val = request.getResourceRef().getQueryAsForm().getFirstValue(parameterName);
			if (val==null) {
				if (formEntity == null) {
					//You most likely want the original request.
					Request originalRequest = request;
					if (originalRequest instanceof InternalRequest)
						originalRequest = ((InternalRequest)originalRequest).getFirstRequest();
					try {
						//Read the entity as a form
						formEntity = new Form(originalRequest.getEntity());
						
						//Rewrite the entity fresh.
						originalRequest.setEntity(formEntity.getWebRepresentation());
					} catch (Exception e) {
						//Didn't work now, wont work later; set empty form
						formEntity = new Form();
					}
				}
				val = formEntity.getFirstValue(parameterName);
			}
			return val;
		}
		public Object getPlugin(String key) {
			return getPlugin(key, null);
		}

		public Object getPlugin(String key, String minimumVersion) {
			ScriptableObjectFactory sof = PluginAgent.getScriptableObjectBroker().getScriptableObjectFactory(
					key, minimumVersion);
			if (sof != null) {
				Object obj = sof.getScriptableObject(request);
				if (obj instanceof Trap) {
					try {
						Trap<ScriptedPages> trap = (Trap<ScriptedPages>)obj;
						trap.addTouchListener(new TouchListener<ScriptedPages>() {
							public void touched(ScriptedPages representation) {
								handle(representation);
							}
						});
					} catch (Throwable e) {
						TrivialExceptionHandler.ignore(this, e);
					}
				}
				return obj;
			}
			else
				return null;
		}
		public ImageUtils getResizer() {
			return resizer != null ? resizer : (resizer = new ImageUtils(context));
		}
		public Request getRequest() {
			return request;
		}
		public String getUri() {
			return baseReference == null ? request.getResourceRef().getPath() : baseReference.getPath();
		}
		public Reference getReference() {
			return baseReference == null ? request.getResourceRef() : baseReference;
		}
		public String getCookie(String name){
			Request r;
			if(request instanceof InternalRequest)
				r = ((InternalRequest) request).getFirstRequest();
			else
				r = request;
			String ret = r.getCookies().getFirstValue(name);
			return ret;
		}
		public void removeCookie(String name, boolean secure, boolean accessRestricted){
			GoGoMagicFilter.addCookie(new CookieSetting(0, name, "", null, null, null, 0, secure, accessRestricted));
		}
		public void addCookie(String name, String value, boolean secure, boolean accessRestricted){
			GoGoMagicFilter.addCookie(new CookieSetting(0, name, value, "/", null, null, -1, secure, accessRestricted));
		}
		public void addCookie(String name, String value, String path, String domain, String comment, int maxAge, boolean secure, boolean accessRestricted){
			GoGoMagicFilter.addCookie(new CookieSetting(0, name, value, path, domain, comment, maxAge, secure, accessRestricted));
		}
		public Reference getBaseReference() {
			return baseReference;
		}
		public Reference getBrowserReference() {
			Request top = getRequest();
			Reference ref;
			if (top instanceof InternalRequest)
				top = ((InternalRequest) top).getFirstRequest();
			if (top.getReferrerRef() != null)
				ref = top.getReferrerRef();
			else
				ref = top.getResourceRef();
			return ref;
		}
		public void handle(ScriptedPages page) {
			
		}
	}
	
	public static class Canvas {
		private final StringWriter writer = new StringWriter();

		@Override
		public String toString() {
			return writer.toString();
		}

		public void write(final String string) {
			writer.write(Replacer.replace(string,"${","\\$\\{"));
		}

		public void eval(final String string) {
			writer.write(string);
		}

		public void writeEL(final String string) {
			writer.write("${" + string + "}");
		}
	}

	private final static class JSR223Entry {
		final ScriptContext context;
		final ScriptEngine engine;

		public JSR223Entry(final ScriptEngine engine, final ScriptContext context) {
			this.engine = engine;
			this.context = context;
		}
	}

	private static long seq = 1L;

	private final Map<String, Object> bindings = new HashMap<String, Object>();

	Canvas canvas = new Canvas();
	private final Context context;
	private final Map<String, JSR223Entry> engines = new HashMap<String, JSR223Entry>();

	private final Map<String, Object> map = new HashMap<String, Object>();

	private final Request request;
	private final Reference baseReference;
	private final boolean isBaseEditable;

	private TemplateRegistryAPI registry;;
	private final Collection<ScriptedPages> scriptedPages;

	private static ThreadLocal<DynamicContext> myDC = new ThreadLocal<DynamicContext>() {
		@Override
		protected synchronized DynamicContext initialValue() {
			return null;
		}
	};

	public static DynamicContext getCurrent() {
		return myDC.get();
	}

	/**
	 * Creates a new DynamicContext
	 * @param context the restlet context
	 * @param request the current request (may be different than the original request)
	 * @param baseReference the base reference, may be null if there's no base object
	 * @param isBaseEditable true if base is editable, false otherwise
	 */
	public DynamicContext(final Context context, final Request request, final Reference baseReference, boolean isBaseEditable) {
		//sa = (ServerApplication) Application.getCurrent();
		try {
			registry = GoGoEgo.get().getFromContext(context).getTemplateRegistry();
		} catch (Throwable e) {
			registry = null;
		}
		this.request = request;
		this.baseReference = baseReference;
		this.context = context;
		this.isBaseEditable = isBaseEditable;
		this.scriptedPages = new ArrayList<ScriptedPages>();
		
		if (baseReference != null)
			request.getAttributes().put(Constants.BASE_REFERENCE, baseReference);
		
		ELEntity resolver = new ScriptableObjectResolver(request, context, baseReference) {
			public void handle(ScriptedPages page) {
				scriptedPages.add(page);
			}
		};
		
		put("GoGoEgo", resolver);
		put("gogoego", resolver);
	}
	
	public Collection<ScriptedPages> getScriptedPages() {
		return scriptedPages;
	}

	/**
	 * Adds dynamic variables to a scripting context and 
	 * evaluates the script.
	 * @param scriptType the script type as can be looked up 
	 * in our script context
	 * @param script the script
	 */
	public void exec(final String scriptType, final String script) {
		myDC.set(this);
		canvas = new Canvas();
		
		final ScriptContext sc = getScriptContext(scriptType);
		if (sc != null) {
			final Bindings engineBindings = sc.getBindings(ScriptContext.ENGINE_SCOPE);
			// It might be harmful to NOT clear bindings, but we want to try
//			engineBindings.clear();
			engineBindings.put("document", canvas);

			for (final Map.Entry<String, Object> binding : bindings.entrySet())
				engineBindings.put(binding.getKey(), binding.getValue());

			try {
				engines.get(scriptType).engine.eval(script, sc);
			} catch (final Exception exception) {
				exception.printStackTrace();
				GoGoDebug.get("error").println("Script Failure on page {0} with base {1}", request.getResourceRef(), baseReference);
				GoGoDebug.get("error").println(exception.getMessage());
				canvas.write("<!-- Exception running script: " + exception.getMessage() + " -->");
			}
		}
		myDC.set(null);
	}

	private String getGoGoMarkerTag(final GoGoEgoRepresentation representation, 
			final Reference reference, final String content, final PageTreeNode contentNode) {
		/*String markerclass = representation.getContentType();
		try {
			final PageMetadata pm = new PageMetadata(new StringReader(content));

			TemplateDataAPI templateData;
			if (pm.getTemplate() == null)
				templateData = null;
			else {
				templateData = sa.getTemplateRegistry().getRegisteredTemplate(pm.getTemplate());
				if (templateData.getUri().equalsIgnoreCase(""))
					templateData = null;
			}

			markerclass = templateData == null ? (pm.getMeta().containsKey("contentType") ? pm.getMeta().get(
					"contentType") : "text/html") : templateData.getContentType();
		} catch (final IOException unlikely) {
			TrivialExceptionHandler.handle(this, unlikely);
			markerclass = "text/html";
		} catch (final NullPointerException npe) {
			TrivialExceptionHandler.handle(this, npe);
			markerclass = "text/html";
		}
		
		if (sa.getTemplateRegistry().isRegistered(baseReference.getPath()))
			return content;
		
		 */
		seq++;
		/*String uri;
		final int substr = reference.getRemainingPart().indexOf("?");
		if (substr != -1)
			uri = reference.getRemainingPart().substring(0, substr);
		else
			uri = reference.getRemainingPart();*/
		
				
		return "<gogo:marker gclass=\"" + contentNode.getContentType() + 
			"\" src=\"" + contentNode.getUri() + "\" />" + content;
	}
	
	private String getWYSIWYGMarker(final GoGoEgoRepresentation representation, final String key, final Reference reference, final String content) {
		if (registry != null && registry.isRegistered(request.getResourceRef().getPath()) || !"text/html".equals(representation.getContentType())) {
			GoGoDebug.get("fine").println("Returning non-editable content");
			return content;
		}
		else {
			GoGoDebug.get("fine").println("Returning editable content");
			return "<gogo:marker gclass=\"text/html\" src=\"" + reference.getPath() + "\" id=\"" + key + "\">" + content + "</gogo:marker>";
		}
	}

	private ScriptContext getScriptContext(final String scriptType) {
		final JSR223Entry result = engines.get(scriptType);
		if (result != null)
			return result.context;
		//final ScriptEngine engine = GoGoScriptEngine.manager.getEngineByName(scriptType);
		final ScriptEngine engine = PluginAgent.getScriptEngineBroker().getEngineByName(scriptType);
		if (engine == null) {
			System.err.println("No scripting engine found for \"" + scriptType + "\"");
			return null;
		}
		final ScriptContext sc = engine.getContext();
		if (sc == null)
			return null;
		engines.put(scriptType, new JSR223Entry(engine, sc));
		return sc;
	}

	private boolean isEditing() {
		return request.getAttributes().get(Constants.EDITMODE) != null;
	}

	/**
	 * Create a copy of this dynamic context
	 * @return the new instance
	 */
	public DynamicContext newInstance() {
		final DynamicContext dc = new DynamicContext(context, request, baseReference, isBaseEditable);
		return dc;
	}

	/**
	 * Add a binding to the bindings
	 * @param key
	 * @param value
	 */
	public void put(final String key, final ELEntity value) {
		map.put(key, value);
		putBinding(key, value);
	}

	/**
	 * Add a binding to the bindings
	 * @param key
	 * @param value
	 */
	public void put(final String key, final String value) {
		map.put(key, value);
		putBinding(key, value);
	}

	private void putBinding(final String key, final Object o) {
		bindings.put(key, o);
	}
	
	private Map<String, String> extractModifiers(String key) {
		final Map<String, String> map = new HashMap<String, String>();
		int index = key.indexOf(';');
		if (index > 0) {
			String[] split;
			try {
				split = key.substring(index+1).split(";");
			} catch (IndexOutOfBoundsException e) {
				return map;
			}
			for (String current : split) {
				if (!"".equals(current)) {
					String[] entry = current.split("=");
					map.put(entry[0], entry.length > 1 ? entry[1] : null);
				}
			}
		}
		return map;
	}
	
	private void updatePageTree(PageTreeNode contentPage) {
		PageTreeNode pageTree = (PageTreeNode)request.getAttributes().get(ViewFilter.PAGE_TREE);
		if (pageTree != null && contentPage != null) {
			//GoGoDebug.get("fine").println("Pagetree for included content\r\n{0}", contentPage);
			pageTree.addChild(contentPage);
			request.getAttributes().put(ViewFilter.PAGE_TREE, pageTree);
		}
	}
	
	public void updatePageTree(Collection<PageTreeNode> pageTreeNodes) {
		PageTreeNode pageTree = (PageTreeNode)request.getAttributes().get(ViewFilter.PAGE_TREE);
		if (pageTree != null) {
			//GoGoDebug.get("fine").println("Pagetree for included content\r\n{0}", contentPage);
			for (PageTreeNode contentPage : pageTreeNodes)
				pageTree.addChild(contentPage);
			
		}
		request.getAttributes().put(ViewFilter.PAGE_TREE, pageTree);
	}

	/**
	 * Evaluates context based on the bindings put and 
	 * the base, if available.
	 */
	public String resolveEL(final String key) {
		String retString = null;
		final Map<String, String> modifiers = extractModifiers(key);	
		
		int index = key.indexOf(';');
		final String path = index == -1 ? key : key.substring(0, index);
		
		// Content is being referenced from some external source
		if (key.startsWith("/")) {
			if (!modifiers.containsKey("no-cache") && MemoryCache.getInstance().contains(context, path)) {
				MemoryCacheContents contents = MemoryCache.getInstance().get(context, path);
				updatePageTree(contents.getPageTree());
				retString = contents.getContents();
			}
			else {
				final Reference reference = new Reference("riap://host" + path);
				
				final Request contentRequest = GoGoMagicFilter.newRequest(Method.GET, reference, null, request, false);
	
				final GoGoEgoBaseRepresentation representation;
				final Response response = context.getClientDispatcher().handle(contentRequest);
				if (response.getStatus().isSuccess() && response.isEntityAvailable()) {
					representation = (GoGoEgoBaseRepresentation)response.getEntity();
					
					PageTreeNode contentPage = (PageTreeNode)contentRequest.getAttributes().get(ViewFilter.PAGE_TREE);
					if (contentPage != null) {
						contentPage.setAccessURI(path);
						contentPage.setModifiers(modifiers);
						//GoGoDebug.get("fine").println("Pagetree for included content\r\n{0}", contentPage);
						updatePageTree(contentPage);
					}
					else
						GoGoDebug.get("fine").println("No pagetree for {0}", contentRequest.getResourceRef());
					
					retString = extractBody(representation);
					
					/*
					 * If it's straight HTML content, then add a marker.
					 * If it's a template with content that's NOT HTML (i.e. collection/item), 
					 * then add a marker.
					 */
					if (isEditing() && !modifiers.containsKey("no-edit")) {
						if (contentPage.isContent() && "text/html".equals(contentPage.getContentType())) {
							retString = getGoGoMarkerTag(representation, reference, retString, contentPage);
						}
						else if (contentPage.isTemplateWithContent()) {
							PageTreeNode contentNode = contentPage.getContentForURI(reference.getPath());
							if (contentNode != null && !"text/html".equals(contentNode.getContentType()) && !contentNode.hasChildren()) {
								retString = getGoGoMarkerTag(representation, reference, retString, contentNode);
							}
						}
					}
					
					if (modifiers.containsKey("cache")) {
						if (contentPage != null)
							MemoryCache.getInstance().cache(context, path, retString, modifiers.get("cache-expires"), contentPage);
						else
							MemoryCache.getInstance().cache(context, path, retString, modifiers.get("cache-expires"));
					}
				}
				else
					retString = "<!-- representation null for " + reference + " -->";
			}
		}

		// Content is referenced from within, maybe "base"
		else {
			final String cacheCheck = request.getResourceRef().getPath()+":"+path;
			if (!modifiers.containsKey("no-cache") && MemoryCache.getInstance().contains(context, cacheCheck)) {
				MemoryCacheContents contents = MemoryCache.getInstance().get(context, cacheCheck);
				updatePageTree(contents.getPageTree());
				retString = contents.getContents();
			}
			else {
				String left = path;
				String right = null;
				final int i = left.indexOf(".");
				if (i > -1) {
					left = path.substring(0, i);
					right = path.substring(i + 1);
				}
				Object res = map.get(left);
				
				if (res == null) { // try 1.1 OSGi-style plugin lookup of object
					ScriptableObjectFactory sof = PluginAgent.getScriptableObjectBroker().getScriptableObjectFactory(left,
							null);
					if (sof != null)
						res = sof.getScriptableObject(request);
				}
	
				if (res instanceof String)
					retString = (String) res;
				else if (res instanceof ELEntity) {
					final ELEntity myContent = (ELEntity) res;
					retString = myContent.resolveEL(right);
	
					if (isEditing() && !right.equalsIgnoreCase("title") && !modifiers.containsKey("no-edit")) {
						if (res instanceof GoGoEgoRepresentation) {
							/*retString = getGoGoMarkerTag((GoGoEgoRepresentation) myContent, 
									baseReference, content);*/
							GoGoDebug.get("fine").println(
								"Base ref is {0} for request with resource ref {1}", 
								baseReference, request.getResourceRef()
							);
							
							PageTreeNode pages = (PageTreeNode)request.getAttributes().get(ViewFilter.PAGE_TREE);
							PageTreeNode contentNode = pages.getContentForURI(baseReference.getPath());
							if ("text/html".equals(contentNode.getContentType()) && !contentNode.hasChildren())
								retString = getWYSIWYGMarker((GoGoEgoRepresentation)myContent, right, baseReference, retString);
						}
					} 
				}
				
				PageTreeNode pages = (PageTreeNode)request.getAttributes().get(ViewFilter.PAGE_TREE);
				PageTreeNode contentPage = null;
				if (pages != null) {
					String contentType = "text/html";
					if (res instanceof GoGoEgoRepresentation)
						contentType = ((GoGoEgoRepresentation)res).getContentType();
					
					contentPage = new PageTreeNode(path, contentType, "include", pages.getRawLastModified(), baseReference.getPath());
					contentPage.setModifiers(modifiers);
					
					updatePageTree(contentPage);
				}
				
				if (modifiers.containsKey("cache")) {
					if (contentPage != null)
						MemoryCache.getInstance().cache(context, cacheCheck, retString, modifiers.get("cache-expires"), contentPage);
					else
						MemoryCache.getInstance().cache(context, cacheCheck, retString, modifiers.get("cache-expires"));
				}
			}
		}
		
		return retString;
		//return Replacer.replace(retString,"${","\\$\\{");
	}
	
	private String extractBody(GoGoEgoBaseRepresentation representation) {
		final String content = representation.getContent();
		if (content.indexOf("<html") != -1 && content.indexOf("<body") != -1) {
			final StringWriter out = new StringWriter();
			final TagFilter tf = new TagFilter(new StringReader(content), out);
			try {
				tf.extractInteriorOf("body");
			} catch (IOException e) {
				return content;
			}
			return out.toString();
		}
		/*
		 * This is just a fragment, so if it's HTML, we can 
		 * edit it here...
		 */
		else {
			return content;
		}
	}

	@Override
	public String toString() {
		return canvas.toString();
	}

}
