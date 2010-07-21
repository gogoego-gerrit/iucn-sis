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

package com.solertium.vfs.restlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.jcip.annotations.NotThreadSafe;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.DateHelper;
import com.solertium.util.Replacer;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

@NotThreadSafe
public class Dav1VFSResource extends WritableVFSResource {

	private static class DavProperty implements Comparable<DavProperty> {
		private String namespace = "";
		private String property = "";

		public DavProperty(final String namespace1, final String property1) {
			setNamespace(namespace1);
			setProperty(property1);
		}

		public int compareTo(final DavProperty dpother) {
			final String here = toString();
			final String there = dpother.toString();
			return here.compareTo(there);
		}

		public boolean equals(final Object other) {
			try {
				final DavProperty dpother = (DavProperty) other;
				if (toString().equals(dpother.toString()))
					return true;
			} catch (final Exception castAssumptionsFailed) {
				return false;
			}
			return false;
		}

		protected String getNamespace() {
			return namespace;
		}

		protected String getProperty() {
			return property;
		}

		public int hashCode() {
			return toString().hashCode();
		}

		protected void setNamespace(final String namespace) {
			this.namespace = namespace;
		}

		protected void setProperty(final String property) {
			this.property = property;
		}

		public String toString() {
			return getNamespace() + ":" + getProperty();
		}
	}

	public static String escape(String fragment) {
		try {
			fragment = java.net.URLEncoder.encode(fragment, "UTF-8");
			// disallow "+" encoding of spaces -- it does not work for
			// many WebDAV clients, whereas %20 encoding does work.
			fragment = Replacer.replace(fragment,"+", "%20");
		} catch (final UnsupportedEncodingException unlikely) {
			throw new RuntimeException("Expected encoding UTF-8 not available");
		}
		return fragment;
	}

	public static void insertPropfindResponse(final VFSPath uri,
			final int remaining_depth,
			final List<DavProperty> requested_properties, final Document doc,
			final Element parent, final VFS vfs) {

		final Map<String, Element> properties = new HashMap<String, Element>();

		String fragment = Dav1VFSResource.escape(vfs.getName(uri));
		if ("".equals(fragment))
			fragment = "/";

		boolean col = false;
		try {
			col = vfs.isCollection(uri);
		} catch (final NotFoundException nf) {
			throw new RuntimeException(uri + " not found");
		}
		
		VFSMetadata md = vfs.getMetadata(uri);
		if (!md.isHidden() && (col && (remaining_depth == 0)) || (!col)) {
			// set up basic response element
			final Element response = doc.createElementNS("DAV:", "response");
			final Element href = doc.createElementNS("DAV:", "href");
			href.appendChild(doc.createTextNode(fragment));
			response.appendChild(href);

			// set up all properties we actually know about
			long lastmod = 0;
			try {
				lastmod = vfs.getLastModified(uri);
			} catch (final NotFoundException nf) {
				throw new RuntimeException("Expected URI " + uri
						+ " was not found");
			}
			final Date lmd = new Date(lastmod);
			String lm = new SimpleDateFormat(DateHelper.iso8601DateFormat)
					.format(lmd);
			final Element creationdate = doc.createElementNS("DAV:",
					"creationdate");
			creationdate.setAttributeNS("urn:schemas-microsoft-com:datatypes",
					"dt", "dateTime.tz");
			creationdate.appendChild(doc.createTextNode(lm));
			properties.put(new DavProperty("DAV:", "creationdate").toString(),
					creationdate);

			final Element resourcetype = doc.createElementNS("DAV:",
					"resourcetype");
			properties.put(new DavProperty("DAV:", "resourcetype").toString(),
					resourcetype);

			final String n = vfs.getName(uri);

			final Element name = doc.createElementNS("DAV:", "name");
			name.appendChild(doc.createTextNode(n));
			properties.put(new DavProperty("DAV:", "name").toString(), name);

			final Element etag = doc.createElementNS("DAV:", "getetag");
			try {
				etag.appendChild(doc.createTextNode(vfs.getETag(uri)));
				properties.put(new DavProperty("DAV:", "getetag").toString(),
						etag);
			} catch (final NotFoundException unlikely) {
			} // would have happened before now

			
			String displayName = null;
			try {
				displayName = md.getArbitraryData().get("displayname");
			} catch (NullPointerException e) {
				TrivialExceptionHandler.ignore(md, e);
			}
			
			final Element displayname = doc.createElementNS("DAV:",
					"displayname");
			displayname.appendChild(displayName == null ? doc.createTextNode(n) : displayname);
			properties.put(new DavProperty("DAV:", "displayname").toString(),
					displayname);

			// These are placeholders; locking is non-functional
			final Element lockdiscovery = doc.createElementNS("DAV:",
					"lockdiscovery");
			properties.put(new DavProperty("DAV:", "lockdiscovery").toString(),
					lockdiscovery);
			final Element supportedlock = doc.createElementNS("DAV:",
					"supportedlock");
			properties.put(new DavProperty("DAV:", "supportedlock").toString(),
					supportedlock);

			if (uri.equals("/")) {
				final Element isroot = doc.createElementNS("DAV:", "isroot");
				isroot.appendChild(doc.createTextNode("1"));
				isroot.setAttributeNS("urn:schemas-microsoft-com:datatypes",
						"dt", "boolean");
				properties.put(new DavProperty("DAV:", "isroot").toString(),
						isroot);
			} else {
				final Element isroot = doc.createElementNS("DAV:", "isroot");
				isroot.appendChild(doc.createTextNode("0"));
				isroot.setAttributeNS("urn:schemas-microsoft-com:datatypes",
						"dt", "boolean");
				properties.put(new DavProperty("DAV:", "isroot").toString(),
						isroot);
				/*
				 * Element parentname =
				 * doc.createElementNS("DAV:","parentname");
				 * parentname.appendChild(doc.createTextNode(f.getParentFile().getName()));
				 * properties.put(new
				 * DavProperty("DAV:","parentname").toString(), parentname);
				 */
			}

			final Element ishidden = doc.createElementNS("DAV:", "ishidden");
			ishidden.appendChild(doc.createTextNode("0"));
			ishidden.setAttributeNS("urn:schemas-microsoft-com:datatypes",
					"dt", "boolean");
			properties.put(new DavProperty("DAV:", "ishidden").toString(),
					ishidden);

			if (col) {
				resourcetype.appendChild(doc.createElementNS("DAV:",
						"collection"));
				final Element iscollection = doc.createElementNS("DAV:",
						"iscollection");
				iscollection.appendChild(doc.createTextNode("1"));
				iscollection.setAttributeNS(
						"urn:schemas-microsoft-com:datatypes", "dt", "boolean");
				properties.put(new DavProperty("DAV:", "iscollection")
						.toString(), iscollection);
			} else {
				final Element iscollection = doc.createElementNS("DAV:",
						"iscollection");
				iscollection.appendChild(doc.createTextNode("0"));
				iscollection.setAttributeNS(
						"urn:schemas-microsoft-com:datatypes", "dt", "boolean");
				properties.put(new DavProperty("DAV:", "iscollection")
						.toString(), iscollection);
				final Element contentlength = doc.createElementNS("DAV:",
						"getcontentlength");
				long length = 0;
				try {
					length = vfs.getLength(uri);
				} catch (final NotFoundException nf) {
				}
				contentlength.appendChild(doc.createTextNode("" + length));
				contentlength.setAttributeNS(
						"urn:schemas-microsoft-com:datatypes", "dt", "int");
				properties.put(new DavProperty("DAV:", "getcontentlength")
						.toString(), contentlength);

				final Element getcontenttype = doc.createElementNS("DAV:",
						"getcontenttype");
				getcontenttype.appendChild(doc
						.createTextNode("application/binary"));
				properties.put(new DavProperty("DAV:", "getcontenttype")
						.toString(), getcontenttype);
			}
			lm = new SimpleDateFormat(DateHelper.httpDateFormat).format(lmd);
			final Element getlastmodified = doc.createElementNS("DAV:",
					"getlastmodified");
			getlastmodified.setAttributeNS(
					"urn:schemas-microsoft-com:datatypes", "dt",
					"dateTime.rfc1123");
			getlastmodified.appendChild(doc.createTextNode(lm + " GMT"));
			properties.put(new DavProperty("DAV:", "getlastmodified")
					.toString(), getlastmodified);

			// propstat for valid responses
			final Element propstat = doc.createElementNS("DAV:", "propstat");
			response.appendChild(propstat);
			final Element prop = doc.createElementNS("DAV:", "prop");
			propstat.appendChild(prop);
			final Element status = doc.createElementNS("DAV:", "status");
			status.appendChild(doc.createTextNode("HTTP/1.1 200 OK"));
			propstat.appendChild(status);
			final List<DavProperty> missing = new ArrayList<DavProperty>();
			if (requested_properties.size() > 0) {
				final Iterator<DavProperty> it = requested_properties
						.iterator();
				while (it.hasNext()) {
					final DavProperty dp = it.next();
					if (properties.containsKey(dp.toString()))
						// if(d) log.debug("Including requested property
						// "+dp.getNamespace()+" / "+dp.getProperty());
						prop.appendChild(properties.get(dp.toString()));
					else
						missing.add(dp);
				}
			} else {
				final Iterator<Element> it = properties.values().iterator();
				while (it.hasNext())
					prop.appendChild(it.next());
			}

			if (missing.size() > 0) {
				// propstat for missing responses
				final Element propstat2 = doc.createElementNS("DAV:",
						"propstat");
				response.appendChild(propstat2);
				final Element prop2 = doc.createElementNS("DAV:", "prop");
				propstat2.appendChild(prop2);
				final Element status2 = doc.createElementNS("DAV:", "status");
				status2.appendChild(doc
						.createTextNode("HTTP/1.1 404 Not Found"));
				propstat2.appendChild(status2);
				final Iterator<DavProperty> it = missing.iterator();
				while (it.hasNext()) {
					final DavProperty dp = it.next();
					prop2.appendChild(doc.createElementNS(dp.getNamespace(), dp
							.getProperty()));
					// if(d) log.debug("Missing requested property
					// "+dp.getNamespace()+" / "+dp.getProperty());
				}
			}
			parent.appendChild(response);
		}
		if (col && (remaining_depth > 0))
			try {
				final VFSPathToken[] tokens = vfs.list(uri);
				for (final VFSPathToken token : tokens)
					Dav1VFSResource.insertPropfindResponse(uri.child(token),
							remaining_depth - 1, requested_properties, doc,
							parent, vfs);
			} catch (final NotFoundException nfx) {
				throw new RuntimeException(uri + " not found");
			}
	}
	
	protected String getDavHeaderValue(){
		return "1";
	}

	public Dav1VFSResource(final Context context, final Request request,
			final Response response) {
		super(context, request, response);
		setCustomHeader("DAV",getDavHeaderValue());
	}
	
	public boolean allowCopy() {
		return true;
	}

	public boolean allowMkcol() {
		return true;
	}
	
	public boolean allowMkcols() {
		return true;
	}

	public boolean allowMove() {
		return true;
	}

	@Override
	public boolean allowOptions() {
		return true;
	}

	public boolean allowPropfind() {
		return true;
	}

	public boolean allowProppatch() {
		return true;
	}

	private String getHeader(final String header) {
		String ret = null;
		try {
			final org.restlet.data.Form headers = (org.restlet.data.Form) getRequest()
					.getAttributes().get("org.restlet.http.headers");
			ret = headers.getFirstValue(header);
			if (ret == null)
				ret = headers.getFirstValue(header.toLowerCase());
		} catch (final Exception poorly_handled) {
			poorly_handled.printStackTrace();
		}
		return ret;
	}

	public void handleCopy() {
		final Response response = getResponse();
		VFSPath from = uri;
		final String sto = getHeader("Destination");
		try {
			VFSPath to = VFSResource.decodeVFSPath(sto);
			vfs.copy(from, to);
			response.setStatus(Status.SUCCESS_CREATED); // 201 CREATED
		} catch (final NotFoundException c) {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND); // 404 NOT FOUND
		} catch (final ConflictException x) {
			response.setStatus(Status.CLIENT_ERROR_CONFLICT); // 409 CONFLICT
																// couldn't
																// delete
		} catch (final VFSPathParseException vp) {
			vp.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL); // 500 SERVER ERROR
		}
		return;
	}

	public void handleMkcol() {
		final Response response = getResponse();
		try {
			vfs.makeCollection(uri);
			response.setStatus(Status.SUCCESS_CREATED); // 201 CREATED
		} catch (final NotFoundException c) {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND); // 404 NOT FOUND
		} catch (final ConflictException x) {
			response.setStatus(Status.CLIENT_ERROR_CONFLICT); // 409 CONFLICT
																// couldn't
																// delete
		}
		return;
	}
	
	public void handleMkcols() {
		final Response response = getResponse();
		try {
			vfs.makeCollections(uri);
			response.setStatus(Status.SUCCESS_CREATED); // 201 CREATED
		} catch (final NotFoundException c) {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND); // 404 NOT FOUND
		} catch (final ConflictException x) {
			response.setStatus(Status.CLIENT_ERROR_CONFLICT); // 409 CONFLICT
																// couldn't
																// delete
		}
		return;
	}

	public void handleMove() {
		final Response response = getResponse();
		VFSPath from = uri;
		final String sto = getHeader("Destination");
		if(sto==null){
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
		try {
			VFSPath to = null;
			if(sto.startsWith("http:")||sto.startsWith("https:")){
				String internalUri = getRequest().getResourceRef().getRemainingPart();
				String fullUri = getRequest().getResourceRef().toString();
				String stub = fullUri.substring(0,fullUri.length()-internalUri.length());
				if(sto.startsWith(stub)){
					to = VFSResource.decodeVFSPath(sto.substring(stub.length()));
				} else {
					System.out.println("Bad MOVE request: destination "+sto+" does not start with "+stub);
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return;
				}
			} else {
				to = VFSResource.decodeVFSPath(sto);
			}
			System.out.println("MOVE from "+from);
			System.out.println("MOVE to "+to);
			vfs.move(from, to);
			response.setStatus(Status.SUCCESS_CREATED); // 201 CREATED
		} catch (final NotFoundException c) {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND); // 404 NOT FOUND
		} catch (final ConflictException x) {
			response.setStatus(Status.CLIENT_ERROR_CONFLICT); // 409 CONFLICT
																// couldn't
																// delete
		} catch (final VFSPathParseException vp) {
			vp.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL); // 500 SERVER ERROR
		}
		return;
	}

	@Override
	public void handleOptions() {
		super.handleOptions();
		getResponse().getAllowedMethods().add(Method.HEAD);
		getResponse().getAllowedMethods().add(Method.GET);
		getResponse().getAllowedMethods().add(Method.PUT);
		getResponse().getAllowedMethods().add(Method.POST);
		getResponse().getAllowedMethods().add(Method.PROPFIND);
		getResponse().getAllowedMethods().add(Method.PROPPATCH);
		getResponse().getAllowedMethods().add(Method.OPTIONS);
		getResponse().getAllowedMethods().add(Method.MKCOL);
		getResponse().getAllowedMethods().add(Method.DELETE);
		getResponse().getAllowedMethods().add(Method.COPY);
		getResponse().getAllowedMethods().add(Method.MOVE);
		setCustomHeader("MS-Author-Via", "DAV");
	}

	@Override
	public void handlePropfind() {
		final Request request = getRequest();
		final Response response = getResponse();
		if (!vfs.exists(uri)) {
			request.release();
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}
		final String depth = getHeader("Depth");
		final List<DavProperty> requested_properties = new ArrayList<DavProperty>();
		try {
			final Document reqdoc = new DomRepresentation(request.getEntity())
					.getDocument();
			final Element propel = (Element) reqdoc.getDocumentElement()
					.getElementsByTagNameNS("DAV:", "prop").item(0);
			final NodeList props = propel.getChildNodes();
			for (int i = 0; i < props.getLength(); i++) {
				final Node n = props.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					final Element e = (Element) n;
					final DavProperty dp = new DavProperty(e.getNamespaceURI(),
							e.getLocalName());
					/*
					 * if(d) log.debug("PROPFIND client request for
					 * "+e.getNamespaceURI()+ " / "+e.getLocalName());
					 */
					requested_properties.add(dp);
				}
			}
		} catch (final Exception badxml) {
			System.err.println("Broken/no XML in request body");
		}
		/*
		 * Going against RFC2518 because what RFC2518 says is wrong. The RFC
		 * says to treat no Depth header as Depth:Infinity; this means "when the
		 * client does the laziest possible thing; the server should make the
		 * most expensive possible assumption." This server uses Depth: 1 as the
		 * default if the header is missing.
		 */
		int idepth = 1;
		if ("0".equals(depth))
			idepth = 0;
		if ("1".equals(depth))
			idepth = 1;
		/*
		 * Also, in violation of RFC2518 (for the same reason) we limit Infinity
		 * to 32 levels ... higher than this probably indicates infinite
		 * recursion anyway.
		 */
		if ("Infinity".equals(depth))
			idepth = 32;
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument();
		} catch (final ParserConfigurationException unlikely) {
			throw new RuntimeException(unlikely);
		}
		if (doc != null) {
			doc.createElementNS("DAV:", "d:preload1");
			doc.createElementNS("urn:schemas-microsoft-com:datatypes",
					"M:preload2");
			final Element multistatus = doc.createElementNS("DAV:",
					"multistatus");
			doc.appendChild(multistatus);
			try {
				Dav1VFSResource.insertPropfindResponse(uri, idepth,
						requested_properties, doc, multistatus, vfs);
			} catch (final Exception bad) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL,
						"WebDAV Server Error");
				return;
			}
			response.setStatus(new Status(207));
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, doc));
		} else {
			response.setStatus(new Status(500));
			response.setEntity(new StringRepresentation(
					"Unable to construct an XML document"));
		}
	}

	@Override
	public void handleProppatch() {
		final Request request = getRequest();
		final Response response = getResponse();
		if (!vfs.exists(uri)) {
			request.release();
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}
		try {
			final Document reqdoc = new DomRepresentation(request.getEntity()).getDocument();
			final Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
			final Element multistatus = doc.createElementNS("DAV:",
					"multistatus");
			doc.appendChild(multistatus);
			final Element respel = doc.createElementNS("DAV:", "response");
			final Element href = doc.createElementNS("DAV:", "href");
			href.appendChild(doc.createTextNode(Dav1VFSResource.escape(vfs
					.getName(uri))));
			respel.appendChild(href);
			multistatus.appendChild(respel);
			response.setStatus(Status.SUCCESS_MULTI_STATUS);

			final Element propel = (Element) reqdoc.getDocumentElement()
					.getElementsByTagNameNS("DAV:", "prop").item(0);
			final NodeList props = propel.getChildNodes();
			for (int i = 0; i < props.getLength(); i++) {
				final Node n = props.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					final Element e = (Element) n;
					final String propname = e.getNamespaceURI()
							+ e.getLocalName();
					final String value = e.getTextContent();
					final Element propstat = doc.createElementNS("DAV:",
							"propstat");
					respel.appendChild(propstat);
					final Element prop = doc.createElementNS("DAV:", "prop");
					propstat.appendChild(prop);
					final Element property = doc.createElementNS(e
							.getNamespaceURI(), e.getLocalName());
					prop.appendChild(property);
					final Element status = doc
							.createElementNS("DAV:", "status");
					respel.appendChild(status);
					if ("DAV:getlastmodified".equals(propname)) {
						Date dt = null;
						try {
							dt = new SimpleDateFormat(
									DateHelper.iso8601DateFormat).parse(value);
						} catch (final Exception ignored) {
							TrivialExceptionHandler.ignore(this, ignored);
						}
						if (dt == null)
							try {
								dt = new SimpleDateFormat(
										DateHelper.httpDateFormat).parse(value);
							} catch (final Exception ignored) {
								TrivialExceptionHandler.ignore(this, ignored);
							}
						if (dt != null)
							try {
								vfs.setLastModified(uri, dt);
							} catch (final NotFoundException nf) {
								throw new RuntimeException(uri
										+ " not found when setting mtime");
							}
						status.appendChild(doc
								.createTextNode("HTTP/1.1 200 OK"));
					} else
						status
								.appendChild(doc
										.createTextNode("HTTP/1.1 403 Unsupported property"));
				}
			}
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, doc));
		} catch (final RuntimeException badxml) {
			badxml.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity(new StringRepresentation(
					"Error processing PROPPATCH"));
		} catch (final ParserConfigurationException pc) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity(new StringRepresentation(
					"Parser configuration exception"));
		} catch (final IOException io) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity(new StringRepresentation("I/O exception"));
		}
	}

	protected void setCustomHeader(final String header, final String value) {
		try {
			Form headers = (Form) getResponse().getAttributes().get(
					"org.restlet.http.headers");
			if (headers == null)
				headers = new Form();
			headers.add(header, value);
			getResponse().getAttributes().put("org.restlet.http.headers",
					headers);
		} catch (final Exception ignored) {
			ignored.printStackTrace();
		}
	}
}
