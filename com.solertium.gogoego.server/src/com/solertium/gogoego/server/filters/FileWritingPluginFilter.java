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
package com.solertium.gogoego.server.filters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.gogoego.api.filters.FileWritingFilter;
import org.gogoego.api.filters.FileWritingFilterFactory;
import org.gogoego.api.filters.FileWritingFilterMetadata;
import org.gogoego.api.filters.FilterResults;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.gogoego.api.representations.GoGoEgoStringRepresentation;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;
import org.restlet.util.WrapperRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.gogoego.server.lib.app.writefilter.container.FileWritingFilterApplication;
import com.solertium.gogoego.server.lib.app.writefilter.container.FileWritingFilterSettings;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;

public class FileWritingPluginFilter extends Filter {
	
	private static final String RESET_CONTENT = "com.solertium.gogoego.server.filewritingfilter.reset";
	
	public FileWritingPluginFilter(Context context, Restlet next) {
		super(context, next);
	}
	
	protected void afterHandle(Request request, Response response) {
		// If app not installed, don't bother
		if (!ServerApplication.getFromContext(getContext()).isApplicationInstalled(FileWritingFilterApplication.REGISTRATION))
			return;
		
		// If not a write operation, don't bother
		if (!(Method.POST.equals(request.getMethod()) || Method.PUT.equals(request.getMethod())))
			return;
		
		if (response.getStatus().isSuccess() && request.getAttributes().containsKey(RESET_CONTENT))
			response.setStatus(Status.SUCCESS_RESET_CONTENT);
	}	
	
	protected int beforeHandle(Request request, Response response) {
		// If app not installed, don't bother
		if (!ServerApplication.getFromContext(getContext()).isApplicationInstalled(FileWritingFilterApplication.REGISTRATION))
			return Filter.CONTINUE;
		
		// This is the list of installed filters
		final FileWritingFilterSettings settings = 
			FileWritingFilterApplication.getInstance(getContext()).getSettings();
		
		// If nothing installed, don't bother
		if (settings.getFilterKeys().isEmpty())
			return Filter.CONTINUE;
		
		// If not a write operation, don't bother
		if (!(Method.POST.equals(request.getMethod()) || Method.PUT.equals(request.getMethod())))
			return Filter.CONTINUE;
		
		final Reference reference = request.getResourceRef();
		final Representation entity = request.getEntity();
		
		final Representation wrap;
		if (MediaType.TEXT_ALL.includes(entity.getMediaType())) {
			//Need to get text, re-write text, and make a rep of it...
			String text;
			try {
				text = entity.getText();
			} catch (Exception e) {
				//Let it fail later and be handled in a more appropriate manner
				return Filter.CONTINUE;
			}
			
			wrap = new GoGoEgoStringRepresentation(text, entity.getMediaType());
			
			//Re-write the entity
			request.setEntity(text, entity.getMediaType());
		}
		else
			wrap = new PrivateRepresentation(entity);
		
		validateEntity(settings, request, response, reference, wrap, 1);
		
		return Filter.CONTINUE;
	}
	
	private int validateEntity(final FileWritingFilterSettings settings, final Request request, final Response response, Reference reference, Representation entity, int attemptCount) {
		if (attemptCount >= 10) {
			//There's a problem with something auto-validating, and another filter failing it and providing a 
			//new representation that fails a different filter... for now, to keep expected behavior, 
			//let it save...
			return Filter.CONTINUE;
		}
		
		for (String bundleID : settings.getFilterKeys()) {
			final FileWritingFilterFactory factory;
			try {
				factory = PluginAgent.getFileWritingFilterBroker().getPlugin(bundleID);
			} catch (Throwable e) {
				continue;
			}
			
			final FileWritingFilter filter;
			try {
				filter = factory.newInstance();
			} catch (Throwable e) {
				//OSGi error, conintue;
				continue;
			}
			
			final FilterResults results;
			try {
				results = filter.filter(getContext(), reference, entity);
			} catch (Exception e) {
				//Silly integrator throwing uncaught exceptions ... I don't respect your plugin :)
				continue;
			}
			
			if (!results.isSuccess()) {
				Representation update = results.getUpdatedRepresentation();
				if (update != null) {
					if (MediaType.TEXT_ALL.includes(entity.getMediaType()) && filter.isAutomatic()) {
						GoGoEgoStringRepresentation newRep;
						try {
							newRep = new GoGoEgoStringRepresentation(update.getText(), update.getMediaType());
						} catch (Exception e) {
							continue;
						}
						request.setEntity(newRep);
						request.getAttributes().put(RESET_CONTENT, Boolean.TRUE);
						validateEntity(settings, request, response, reference, newRep, attemptCount+1);
					}
					else {
						request.setEntity(update);
						request.getAttributes().put(RESET_CONTENT, Boolean.TRUE);
						validateEntity(settings, request, response, reference, update, attemptCount+1);
					}
				}
				else {
					final Document document = BaseDocumentUtils.impl.newDocument();
					final Element el = document.createElement("root");
					
					final Element requestInfo = document.createElement("request");
					requestInfo.appendChild(BaseDocumentUtils.impl.createElementWithText(document, "uri", reference.toString()));
					el.appendChild(requestInfo);
					
					FileWritingFilterMetadata md;
					try {
						md = factory.getMetadata();
					} catch (Throwable e) {
						md = null;
					}
					
					final Element filterInfo = document.createElement("filter");
					filterInfo.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(document, "name", 
						(md == null || md.getName() == null) ? filter.getClass().getSimpleName() : md.getName()));
					filterInfo.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(document, "description", 
						(md == null || md.getDescription() == null) ? "None Provided" : md.getDescription()));
					el.appendChild(filterInfo);
					
					el.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(document, "error", results.getErrorMessage()));
					
					if (MediaType.TEXT_ALL.includes(entity.getMediaType())) {
						try {
							el.appendChild(BaseDocumentUtils.impl.createCDATAElementWithText(document, "entity", entity.getText()));
						} catch (Exception e) {
							TrivialExceptionHandler.ignore(this, e);
						}
					}
					
					document.appendChild(el);
					
					/**
					 * FIXME: I want this to be a 412 but it clashes with the 
					 * LastModifiedProtectionFilter (which is 412 but should be 
					 * 409 Conflict.
					 */
					response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
					response.setEntity(new GoGoEgoDomRepresentation(document));
					
					return Filter.STOP;
				}
			}
			else
				GoGoEgo.debug().println("Passed validation for {0}", bundleID);
		}
		
		return Filter.CONTINUE;
	}
	
	public static class PrivateRepresentation extends WrapperRepresentation {
		
		public PrivateRepresentation(Representation wrappedRepresentation) {
			super(wrappedRepresentation);
		}
		
		public ReadableByteChannel getChannel() throws IOException {
			throw new UnsupportedOperationException("This is a private representation; no reads or writes allowed");
		}
		
		public Reader getReader() throws IOException {
			throw new UnsupportedOperationException("This is a private representation; no reads or writes allowed");
		}
		
		public InputStream getStream() throws IOException {
			throw new UnsupportedOperationException("This is a private representation; no reads or writes allowed");
		}
		
		public void write(OutputStream outputStream) throws IOException {
			throw new UnsupportedOperationException("This is a private representation; no reads or writes allowed");
		}
		
		public void write(WritableByteChannel writableChannel) throws IOException {
			throw new UnsupportedOperationException("This is a private representation; no reads or writes allowed");
		}
		
		public void write(Writer writer) throws IOException {
			throw new UnsupportedOperationException("This is a private representation; no reads or writes allowed");
		}
		
	}

}
