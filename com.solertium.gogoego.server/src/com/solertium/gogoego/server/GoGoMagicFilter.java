/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.gogoego.server;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gogoego.api.applications.TemplateDataAPI;
import org.gogoego.api.collections.Constants;
import org.gogoego.api.collections.GenericCollection;
import org.gogoego.api.collections.GoGoEgoItem;
import org.gogoego.api.errors.ErrorHandler;
import org.gogoego.api.errors.ErrorHandlerFactory;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.gogoego.api.representations.GoGoEgoRepresentation;
import org.gogoego.api.representations.GoGoEgoStringRepresentation;
import org.gogoego.api.utils.MagicDisablingFilter;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.gogoego.server.lib.representations.RepresentationUtils;
import com.solertium.gogoego.server.lib.resources.PageTreeNode;
import com.solertium.gogoego.server.lib.resources.StaticPageTreeNode;
import com.solertium.gogoego.server.lib.settings.shortcuts.ShortcutErrorHandler;
import com.solertium.gogoego.server.lib.templates.CustomTemplateData;
import com.solertium.gogoego.server.lib.templates.TemplateData;
import com.solertium.gogoego.server.lib.templates.TemplateRegistry;
import com.solertium.util.BaseTagListener;
import com.solertium.util.PageMetadata;
import com.solertium.util.TagFilter;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.TagFilter.Tag;
import com.solertium.util.restlet.InternalRequest;
import com.solertium.util.restlet.RestletUtils;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.restlet.VFSResource;

/**
 * GoGoMagicFilter.java
 * 
 * All of the magical processing behavior of GoGoEgo is triggered via this
 * filter.
 * 
 * @author rob.heittman
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GoGoMagicFilter extends Filter {

	public final List<String> scriptExtensions;
	public final List<MediaType> eligibleMediaTypes;
	
	private static int MAX_TEMPLATE_RECURSION;
	
	private static ThreadLocal<List<CookieSetting>> secureCookieSettings = new ThreadLocal<List<CookieSetting>>();
	
	public static final void addCookie(CookieSetting cookieSetting){
		if(secureCookieSettings.get() == null)
			secureCookieSettings.set(new ArrayList<CookieSetting>());
		secureCookieSettings.get().add(cookieSetting);
	}
	
	public static final List<CookieSetting> getCookies() {
		return secureCookieSettings.get();
	}
	
	public static final void removeCookies() {
		secureCookieSettings.remove();
	}

	public GoGoMagicFilter(final Context context) {
		super(context);
		int iMAX_TEMPLATE_RECURSION;
		try {
			iMAX_TEMPLATE_RECURSION = Integer.parseInt(GoGoEgo.getInitProperties().getProperty("GOGOEGO_MAX_TEMPLATE_RECURSION", "10"));
		} catch (NullPointerException e) {
			iMAX_TEMPLATE_RECURSION = 10;
		} catch (NumberFormatException e) {
			iMAX_TEMPLATE_RECURSION = 10;
		}
		
		MAX_TEMPLATE_RECURSION = iMAX_TEMPLATE_RECURSION;
		
		final ArrayList<String> iScriptExtensions = new ArrayList<String>(3);
		iScriptExtensions.add("js");
		iScriptExtensions.add("py");
		iScriptExtensions.add("rb");
		scriptExtensions = new CopyOnWriteArrayList<String>(iScriptExtensions);

		final ArrayList<MediaType> iEligibleMediaTypes = new ArrayList<MediaType>(2);
		iEligibleMediaTypes.add(MediaType.TEXT_HTML);
		iEligibleMediaTypes.add(MediaType.TEXT_PLAIN);
		
		eligibleMediaTypes = new CopyOnWriteArrayList<MediaType>(iEligibleMediaTypes);
	}
	
	protected int beforeHandle(Request request, Response response) {
		//GoGoEgo.debug().println("Received request for {0}", request.getResourceRef());
		if (Protocol.HTTP.equals(request.getProtocol()) || Protocol.HTTPS.equals(request.getProtocol())) {
			//GoGoEgo.debug().println("Setting top level request to be {0}", request.getResourceRef());
			request.getAttributes().put(Constants.TOP_LEVEL_REQUEST, Boolean.TRUE);
		}
		//else
			//request.getAttributes().remove(Constants.TOP_LEVEL_REQUEST);
		return Filter.CONTINUE;
	}

	@Override
	public void afterHandle(final Request request, final Response response) {
		if (response.getStatus().isRedirection()) {
			if (VFSResource.ENTITY_HACKS) {
				response.setEntity(new StringRepresentation(
						"<p>If you are not redirected, please follow the link below:</p>" + "<a href=\""
								+ response.getLocationRef() + "\">" + response.getLocationRef() + "</a>",
						MediaType.TEXT_HTML));
			}
			return;
		}
		
		/*
		 * Page Tree
		 */
		{
			String contentType = "unknown";
			long modificationDate = 0;
			Representation entity = response.getEntity();
			if (entity != null && entity instanceof GoGoEgoRepresentation) {
				contentType = ((GoGoEgoRepresentation)entity).getContentType();
				try {
					modificationDate = entity.getModificationDate().getTime();
				} catch (NullPointerException e) {
					GoGoEgo.debug("error").println("{0} failed to return a modification date", entity.getClass().getName());
					TrivialExceptionHandler.ignore(this, e);
				}
			}
			
			final PageTreeNode pageTreeNode = new PageTreeNode(
				request.getResourceRef().getPath(), contentType, 
				"content", modificationDate
			);
			if (request.getReferrerRef() != null)
				pageTreeNode.setAccessURI(request.getReferrerRef().getPath());
			
			if (request.getAttributes().get(ViewFilter.PAGE_TREE) == null)
				request.getAttributes().put(ViewFilter.PAGE_TREE, pageTreeNode);
			
			
			response.setEntity(entity);
		}
		
		boolean hasMagic = (request.getAttributes().get(MagicDisablingFilter.MAGIC_DISABLING_KEY) == null);
		if (hasMagic)
			doMagic(request, response);
		
		if (response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND))
			handleError(request, response);

		try {
			ServerApplication.getFromContext(getContext()).getApplicationEvents()
					.fireEvent(getContext(), request, response);
		} catch (ClassCastException e) {
			/*
			 * FIXME: there's a context issue if this occurs...
			 */
			GoGoDebug.get("error").println("Failure, but continuing: {0}", e.getMessage());
		} catch (Error e) {
			// SHould not fail is an application has bad handling of an event.
			TrivialExceptionHandler.ignore(this, e);
		}
		
		//final Protocol p = request.getResourceRef().getSchemeProtocol();
		//if (Protocol.HTTP.equals(p) || Protocol.HTTPS.equals(p)) { // top level
		if (request.getAttributes().containsKey(Constants.TOP_LEVEL_REQUEST)) {
			PageTreeNode node = 
				(PageTreeNode)request.getAttributes().get(ViewFilter.PAGE_TREE);
			if (node != null && "text/html".equals(node.getContentType())) {
				String path;
				if (request.getReferrerRef() != null)
					path = request.getReferrerRef().getPath();
				else
					path = request.getResourceRef().getPath();
				ServerApplication.getFromContext(getContext()).
					getLastModifiedMap().put(path, new StaticPageTreeNode(node));
			}
			RepresentationUtils.processFinalEntity(request, response);
		}
		//}

		RepresentationUtils.processCharSetAndExpiration(request, response);
	}
	
	private void handleError(final Request request, final Response response) {
		final VFS vfs = GoGoEgo.get().getFromContext(getContext()).getVFS();
		final VFSPath path = new VFSPath("/(SYSTEM)/404/sort.xml");
		
		Document document = null;
		if (vfs.exists(path)) {
			try {
				document = vfs.getDocument(path);
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
		
		final Map<String, ErrorHandlerFactory> map = 
			PluginAgent.getErrorHandlerBroker().getPlugins();
		
		final Collection<ErrorHandlerFactory> plugins;
		if (document != null) {
			plugins = new ArrayList<ErrorHandlerFactory>();
			final GenericCollection collection = new GenericCollection(document);
			for (GoGoEgoItem item : collection.getItems().values()) {
				ErrorHandlerFactory plugin = map.remove(item.getItemName());
				if (plugin != null)
					plugins.add(plugin);
			}
			plugins.addAll(map.values());
		}
		else {
			plugins = new ArrayList<ErrorHandlerFactory>();
			//Always try shortcuts first unless otherwise specified
			plugins.add(map.remove(ShortcutErrorHandler.BUNDLE_KEY));
			plugins.addAll(map.values());
		}
		
		Response correctedResponse = null;
		for (ErrorHandlerFactory factory : plugins) {
			final ErrorHandler handler = factory.newInstance();
			final Response tentative;
			try {
				tentative = handler.handle404(getContext(), request);
			} catch (ResourceException e) {
				continue;
			} catch (Throwable e) {
				continue;
			}
			
			if (tentative == null || tentative.getStatus().isServerError() || tentative.getStatus().isServerError())
				continue;
			else {
				correctedResponse = tentative;
				break;
			}
		}
		
		if (correctedResponse != null)
			copyResponse(correctedResponse, response);
	}

	private void doMagic(final Request request, final Response response) {
		//Taking the training wheels off...
		/*GoGoDebug.get("fine").println("\n--- START POST-RETRIEVAL MAGIC FOR {0} ---", request.getResourceRef());
		GoGoDebug.get("fine").println("Reference: {0}", request.getResourceRef());
		GoGoDebug.get("fine").println("Status: {0}", response.getStatus());
		GoGoDebug.get("fine").println("EditMode: {0}", request.getAttributes().containsKey(Constants.EDITMODE));
		GoGoDebug.get("fine").println("Force Magic: {0}", response.getAttributes().containsKey(Constants.FORCE_MAGIC));*/

		if (response.getAttributes().get(VFSResource.IS_COLLECTION) != null) {
			// Do internal redirect, but as a brand-new request
			if (Boolean.TRUE.equals(response.getAttributes().get(VFSResource.IS_ROOT))) {
				final StringBuilder path = new StringBuilder();
				path.append('/');
				//TODO: user-defined? not right now...
				path.append("index.html");
				if (request.getResourceRef().getQuery() != null) {
					path.append('?');
					path.append(request.getResourceRef().getQuery());
				}
				
				final Request rootRequest = newRequest(request.getMethod(), new Reference("riap://host" + path.toString()), request.getEntity(), request);
				
				final Response rootResponse = getContext().getClientDispatcher().handle(rootRequest);
				copyResponse(rootResponse, response);
			}
			else { //Do external redirect
				final String root = request.getResourceRef().getPath();
				final StringBuilder path = new StringBuilder();
				path.append(root);
				if (!root.endsWith("/"))
					path.append('/');
				//TODO: user-defined? not right now...
				path.append("index.html");
				if (request.getResourceRef().getQuery() != null) {
					path.append('?');
					path.append(request.getResourceRef().getQuery());
				}
				response.setEntity(null);
				response.redirectPermanent(new Reference(request.getResourceRef().getBaseRef(), path.toString()));
			}
		} else if (response.getStatus().isSuccess()) {
			// GoGoScriptEngine.getInstance(getContext()).doScripting(request,
			// response);

			final Representation entity = response.getEntity();
			
			if ((entity != null)
					&& (eligibleMediaTypes.contains(entity.getMediaType()) || response.getAttributes().containsKey(
							Constants.FORCE_MAGIC)))
				try {
					final GoGoEgoBaseRepresentation representation;
					if (entity instanceof GoGoEgoBaseRepresentation)
						representation = (GoGoEgoBaseRepresentation) entity;
					else
						representation = new GoGoEgoStringRepresentation(entity.getText()); //TEMP HACK!!

					//final GoGoEgoBaseRepresentation finalRep = 
					resolveRepresentation(
						getContext(), request, response, request.getResourceRef(), 
						null, request.getResourceRef(), representation
					);					
					
					//recurseToTemplate(request, response, finalRep);					
				} catch (final IOException x) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}
		}

		else if (response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
			// attempt template-compound URI
			final TemplateRegistry templateRegistry = 
				ServerApplication.getFromContext(getContext()).getTemplateRegistry();

			final String uri = request.getResourceRef().getPath();

			final String parent_uri = uri.substring(0, uri.lastIndexOf("/"));
			final String key = uri.substring(uri.lastIndexOf("/") + 1);

			if (templateRegistry.isRegistered(key)) {
				final Request baseRequest = newRequest(
					request.getMethod(), "riap://host" + parent_uri, 
					request.getEntity(), request
				);
				baseRequest.getAttributes().put(Constants.USES_DYNAMIC_TEMPLATING, Boolean.TRUE);

				final Response baseResponse = getContext().getClientDispatcher().handle(baseRequest);
				if (baseResponse.getStatus().isSuccess()) {
					final Representation entity = baseResponse.getEntity();
					final GoGoEgoBaseRepresentation baseRepresentation;
					if (entity instanceof GoGoEgoBaseRepresentation) 
						baseRepresentation = (GoGoEgoBaseRepresentation)baseResponse.getEntity();
					else {
						GoGoDebug.get("severe").println(
							"GoGoMagicFilter: Request for {0} broke the model, returned a {1}", 
							baseRequest.getResourceRef(), entity.getClass().getName()
						);
						//Pretty much impossible but..
						try {
							baseRepresentation = new GoGoEgoStringRepresentation(entity.getText(), MediaType.TEXT_HTML);
						} catch (IOException e) {
							response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
							return;
						}
					}
					
					/*
					 * Because this request doesn't really exist, we need 
					 * to do a couple special things...
					 */
					if (baseResponse.getAttributes().containsKey(MagicDisablingFilter.MAGIC_DISABLING_KEY))
						response.setEntity(baseRepresentation);
					else {
						request.setReferrerRef(baseRequest.getResourceRef());
						final TemplateData templateData = 
							(TemplateData)templateRegistry.getRegisteredTemplate(key);
						
						// now process template with base
						response.setRequest(baseRequest);
						
						final Reference baseRef;
						PageTreeNode node = (PageTreeNode)baseRequest.getAttributes().get(ViewFilter.PAGE_TREE);
						if (node == null)
							baseRef = baseRequest.getResourceRef(); //whatever
						else
							baseRef = new Reference("riap://host" + node.getUri());
						
						doTemplating(templateData, baseRef, baseRequest, response, baseRepresentation);
					}
					request.getAttributes().put(ViewFilter.PAGE_TREE, (PageTreeNode)baseRequest.getAttributes().get(ViewFilter.PAGE_TREE));
					
				}					
				else
					response.setStatus(baseResponse.getStatus());
			}
		}

		//Taking the training wheels off...
		//GoGoDebug.get("fine").println("\n--- END POST-RETRIEVAL MAGIC FOR " + request.getResourceRef() + " ---");
	}
	
	private static TemplateDataAPI getTemplateData(Request request, Response response, GoGoEgoBaseRepresentation representation) {
		final TemplateRegistry templateRegistry;
		try {
			templateRegistry = ((ServerApplication) Application.getCurrent()).getTemplateRegistry();
		} catch (Throwable e) {
			return null;
		}
		
		final PageMetadata pm;
		try {
			pm = new PageMetadata(new StringReader(representation.getContent()));
		} catch (IOException fairlyImpossible) {
			TrivialExceptionHandler.impossible(representation, fairlyImpossible);
			return null;
		}
		String cacheSetting = pm.getMeta().get("cache");
		if (cacheSetting != null && "never".equals(cacheSetting)) {
			GoGoDebug.get("fine").println("Disabling caching due to meta setting");
			RestletUtils.setHeader(response, "Cache-control", "private, no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
			response.getAttributes().put(Constants.DISABLE_EXPIRATION, Boolean.TRUE);
		}
		
		final String template = pm.getTemplate();

		if ((template != null) && templateRegistry.isRegistered(template))
			return templateRegistry.getRegisteredTemplate(template);
		
		return null;
	}
	
	private static void recurseToTemplate(Request request, Response response, GoGoEgoBaseRepresentation representation) {
		// Recurse to a template if needed
		final TemplateDataAPI tData = 
			getTemplateData(request, response, representation);
		
		if (tData != null) {
			if (tData.isAllowed(request.getResourceRef().getPath())) {
				doTemplating(tData, request.getResourceRef(), request, response, representation);
			} else {
				response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
				response.setEntity(new GoGoEgoStringRepresentation(
					"The " + "template " + tData.getUri() + 
					" is not allowed " + "to template " + 
					request.getResourceRef().getPath()
				));
			}
		}
		else
			response.setEntity(representation);
	}
		
	private static void resolveRepresentation(
			final Context context, final Request baseRequest, final Response baseResponse, 
			final Reference baseReference, 
			final GoGoEgoBaseRepresentation baseRepresentation, 
			final Reference templateReference, 
			final GoGoEgoBaseRepresentation templateRepresentation) {
		
		final DynamicContext dc = new DynamicContext(context, baseRequest, baseReference, true);	
		
		/*
		 * Do scripting and possibly apply the template...
		 * Note that the template representation now contains the 
		 * most complete version of the content.
		 */
		
		//FIXME: instead of hard-coding to HTML, check for eligible media types
		final GoGoEgoBaseRepresentation representation;
		if (baseRepresentation != null || MediaType.TEXT_HTML.equals(templateRepresentation.getMediaType())) {
			final String templatedContent = RepresentationUtils.processDynamic(templateRepresentation, baseRepresentation, dc);
			
			if (baseRepresentation == null) { 
				representation = new GoGoEgoStringRepresentation(templatedContent, templateRepresentation.getPreferredMediaType());
				representation.setContentType(templateRepresentation.getContentType());
			}
			else
				representation = new GoGoEgoStringRepresentation(
					resolveMetaTags(baseRepresentation, templatedContent), 
					templateRepresentation.getPreferredMediaType()
				);
		}
		else
			representation = templateRepresentation;
		
		PageTreeNode node = (PageTreeNode)baseRequest.getAttributes().get(ViewFilter.PAGE_TREE);
		if (node != null) {
			node.setContentType(representation.getContentType());
			node.setTag(baseRepresentation == null ? "content" : "template");
			baseRequest.getAttributes().put(ViewFilter.PAGE_TREE, node);
		}
		
		recurseToTemplate(baseRequest, baseResponse, representation);		
	}
	
	/**
	 * Given a key of a registered template and a base, find the template and 
	 * use it to template the given base representation. 
	 * @param templateKey the template key 
	 * @param baseRepresentation the base representation
	 * @return the new templated representation
	 * @throws NotFoundException thrown if the template is not found
	 */
	public static GoGoEgoBaseRepresentation applyTemplating(final String templateKey, final GoGoEgoBaseRepresentation baseRepresentation) throws NotFoundException {
		TemplateDataAPI template = ServerApplication.
			getFromContext(Application.getCurrent().getContext()).
			getTemplateRegistry().getRegisteredTemplate(templateKey);
		
		if (template == null)
			throw new NotFoundException(templateKey);
		else
			return applyTemplating(template, baseRepresentation);			
	}
	
	/**
	 * Given the template data and the base representation, template the content
	 * @param template the template data
	 * @param baseRepresentation the base representation
	 * @return the new templated content
	 */
	public static GoGoEgoBaseRepresentation applyTemplating(final TemplateDataAPI template, final GoGoEgoBaseRepresentation baseRepresentation) {
		return applyTemplating(template.getRepresentation(), baseRepresentation);
	}
	
	/**
	 * Given two representations, template the first with the second.  A registered 
	 * template need not be used here.
	 * @param templateRepresentation the template
	 * @param baseRepresentation the base
	 * @return the new templated content
	 */
	public static GoGoEgoBaseRepresentation applyTemplating(final GoGoEgoBaseRepresentation templateRepresentation, final GoGoEgoBaseRepresentation baseRepresentation) {
		String extension = ".html";
		if (MediaType.TEXT_XML.equals(baseRepresentation.getMediaType()))
			extension = ".xml";
		
		final Request fauxRequest = new Request(Method.GET, "riap://host/internal/" + baseRepresentation.getClass().getName() + extension);
		final Response fauxResponse = new Response(fauxRequest);
		
		doTemplating(
			new CustomTemplateData(templateRepresentation),
			fauxRequest.getResourceRef(), 
			fauxRequest, fauxResponse, baseRepresentation
		);
		
		return (GoGoEgoBaseRepresentation)fauxResponse.getEntity();
	}

	private static void doTemplating(final TemplateDataAPI templateData, 
			final Reference baseReference, 
			final Request baseRequest, final Response baseResponse,
			final GoGoEgoBaseRepresentation baseRepresentation) {
		
		final Request templateRequest = newRequest(baseRequest.getMethod(), "riap://host"
				+ templateData.getUri(), null, baseRequest);
		
		/*
		 * Template no longer need to go through the view filter or the 
		 * after handle, so the PageTree attribute is never set to null, 
		 * but I know the template exists so I can go ahead and add the 
		 * page tree node.
		 */
		
			/*templateRequest.getAttributes().put(ViewFilter.PAGE_TREE, 
				new PageTreeNode(templateData.getUri(), templateData.getContentType(), "template"));*/
		
		PageTreeNode current = (PageTreeNode)baseRequest.getAttributes().get(ViewFilter.PAGE_TREE);
		PageTreeNode template = 
			new PageTreeNode(templateData.getUri(), 
					templateData.getContentType(), 
					"template", 
					templateData.getRepresentation().getModificationDate().getTime()
			);
		
		if (current != null) {
			//template.setBase(current);
			template.addChild(current);
			templateRequest.getAttributes().put(ViewFilter.PAGE_TREE, template);
			
			//Check for infinite recursion...		
			if (template.getLevel() > MAX_TEMPLATE_RECURSION) {
				baseResponse.setEntity(new GoGoEgoDomRepresentation(current.toXML()));
				baseResponse.setStatus(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE);
				return;
			}
		}

		resolveRepresentation(
			Application.getCurrent().getContext(), templateRequest, baseResponse,
			baseReference, 
			baseRepresentation,	templateRequest.getResourceRef(), 
			templateData.getRepresentation()
		);
		
		if (!baseResponse.isEntityAvailable()) {
			//Epic failure...
			baseResponse.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
		else {
			PageTreeNode updatedTemplate = (PageTreeNode)templateRequest.getAttributes().get(ViewFilter.PAGE_TREE);
			if (updatedTemplate != null) {
				baseRequest.getAttributes().put(ViewFilter.PAGE_TREE, updatedTemplate);
			}
			
			baseResponse.setStatus(Status.SUCCESS_OK);
		}
	}

	private static String resolveMetaTags(GoGoEgoRepresentation baseResource, String templateContent) {
		if (templateContent.indexOf("<html") == -1 || templateContent.indexOf("<head") == -1)
			return templateContent;

		String baseContent = baseResource.getContent();
		if (templateContent.indexOf("<html") == -1 || templateContent.indexOf("<head") == -1)
			return templateContent;

		final PageMetadata md;
		try {
			md = new PageMetadata(baseContent);
			md.getMeta().remove("template");
		} catch (IOException e) {
			return templateContent;
		}

		final ArrayList<String> found = new ArrayList<String>();

		final StringWriter writer = new StringWriter();
		final TagFilter tf = new TagFilter(new StringReader(templateContent), writer);
		tf.shortCircuitClosingTags = false;
		tf.registerListener(new BaseTagListener() {
			public List<String> interestingTagNames() {
				final ArrayList<String> list = new ArrayList<String>();
				list.add("meta");
				return list;
			}

			public void process(final Tag tag) throws IOException {
				Iterator<String> iterator = md.getMeta().keySet().iterator();
				while (iterator.hasNext()) {
					String current = iterator.next();
					if (current.equals(tag.getAttribute("name"))) {
						String content = md.getMeta().get(current);
						if (content != null) {
							tag.setAttribute("content", content);
							tag.rewrite();
							found.add(current);
						}
					}
				}
			}
		});
		try {
			tf.parse();
		} catch (Exception e) {
			e.printStackTrace();
			return templateContent;
		}

		final String out;
		if (found.containsAll(md.getMeta().keySet()))
			out = writer.toString();
		else {
			for (int i = 0; i < found.size(); i++)
				md.getMeta().remove(found.get(i));

			int index = templateContent.indexOf("<head");
			index += "<head>".length();

			String prefix = templateContent.substring(0, index);
			String suffix = templateContent.substring(index + 1);

			StringBuilder newMetaTags = new StringBuilder();

			Iterator<Map.Entry<String, String>> iterator = md.getMeta().entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, String> current = iterator.next();
				newMetaTags.append("<meta name=\"" + current.getKey() + "\" content=\"" + current.getValue()
						+ "\" />\r\n");
			}

			out = prefix + "\r\n" + newMetaTags.toString() + suffix;
		}

		return out;
	}

	public static Request newRequest(final Method method, final Reference reference, final Representation entity,
			final Request originalRequest, boolean overwriteQuery) {
		if (overwriteQuery)
			reference.setQuery(originalRequest.getResourceRef().getQuery());
		final Request newRequest = new InternalRequest(originalRequest, method, reference, entity);
		//FIXME This is dangerous; copy rather than reference the header form
		newRequest.getAttributes().put("org.restlet.http.headers",originalRequest.getAttributes().get("org.restlet.http.headers"));
		if (originalRequest.getAttributes().containsKey(Constants.EDITMODE))
			newRequest.getAttributes().put(Constants.EDITMODE,
					originalRequest.getAttributes().get(Constants.EDITMODE));
		if (originalRequest.getAttributes().containsKey(ViewFilter.SHOW_TREE))
			newRequest.getAttributes().put(ViewFilter.SHOW_TREE, Boolean.TRUE);
		if (originalRequest.getAttributes().containsKey(Constants.SHORTCUT))
			newRequest.getAttributes().put(Constants.SHORTCUT, Boolean.TRUE);
		newRequest.getCookies().addAll(originalRequest.getCookies());

		return newRequest;
	}
	
	public static Request newRequest(final Method method, final Reference reference, final Representation entity,
			final Request originalRequest) {
		return newRequest(method, reference, entity, originalRequest, true);
	}

	public static Request newRequest(final Method method, final String reference, final Representation entity,
			final Request originalRequest) {
		return newRequest(method, new Reference(reference), entity, originalRequest);
	}
	
	private static void copyResponse(final Response from, final Response to) {
		to.setEntity(from.getEntity());
		to.setStatus(from.getStatus());
		to.setLocationRef(from.getLocationRef());
		to.getAttributes().clear();
		to.getAttributes().putAll(from.getAttributes());
	}

}
