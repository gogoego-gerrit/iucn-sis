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
package org.gogoego.api.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;

import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.data.MediaType;
import org.w3c.dom.Document;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.MediaTypeManager;
import com.solertium.vfs.BoundsException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.utils.VFSUtils;

/**
 * DocumentUtils.java
 * 
 * A utility class to do some of the more mundane operations.
 * 
 * @author carl.scott
 * 
 */
public class DocumentUtils extends BaseDocumentUtils {

	public static final DocumentUtils impl = new DocumentUtils();

	private DocumentUtils() {
		super();
	}

	private static void logError(String msg) {
		try {
			GoGoEgo.debug("debug").println("DocumentUtils ERROR: " + msg);
		} catch (Throwable e) {
			//Let's not fail on a log error, but can fail in testing contexts
			TrivialExceptionHandler.ignore(msg, e);
		}
	}

	public static Document getReadOnlyDocument(final VFSPath uri, final VFS vfs) throws NotFoundException, IOException {
		return vfs.getDocument(uri);
	}

	public static Document getReadWriteDocument(final VFSPath uri, final VFS vfs) throws IOException {
		return vfs.getMutableDocument(uri);
	}

	/**
	 * @deprecated Use vfs.getDocument(VFSPath path)
	 */
	public static Document getVFSFile(final String uri, final VFS vfs) {
		try {
			return vfs.getDocument(VFSUtils.parseVFSPath(uri));
		} catch (VFSUtils.VFSPathParseException ex) {
			logError("getVFSFile (" + uri + ")" + ex.getMessage());
			return null;
		} catch (final Exception e) {
			logError("getVFSFile (" + uri + "): " + e.getMessage());
			return null;
		}
	}

	/**
	 * @deprecated Use vfs.getString(VFSPath path)
	 */
	public static String getVFSFileAsString(final String uri, final VFS vfs) {
		String content = null;
		try {
			VFSPath path = VFSUtils.parseVFSPath(uri);
			return vfs.getString(path);
		} catch (final VFSUtils.VFSPathParseException ex) {
			TrivialExceptionHandler.ignore(uri, ex);
		} catch (final IOException e) {
			TrivialExceptionHandler.ignore(uri, e);
		}
		return content;
	}

	/**
	 * Writes a document to the store's VFS
	 * 
	 * @param uri
	 *            where the file goes
	 * @param document
	 *            the document
	 * @return true if successful, false otherwise
	 */
	public static boolean writeVFSFile(final String uri, final VFS vfs, final Document document) {
		Writer writer;
		try {
			writer = vfs.getWriter(VFSUtils.parseVFSPath(uri));
		} catch (IOException e) {
			return false;
		} catch (Exception e) {
			return false;
		}

		String method = "xml";
		MediaType mt = MediaTypeManager.getMediaType(uri);
		if (MediaType.TEXT_HTML.equals(mt))
			method = "html";
		else if (MediaType.TEXT_PLAIN.equals(mt))
			method = "text";

		final HashMap<String, String> outputProps = new HashMap<String, String>();
		outputProps.put(OutputKeys.METHOD, method);
		outputProps.put(OutputKeys.INDENT, "yes");
		outputProps.put(OutputKeys.OMIT_XML_DECLARATION, "no");

		final HashMap<String, String> factoryAttrs = new HashMap<String, String>();
		factoryAttrs.put("indent-number", "3");

		boolean result;
		try {
			impl._serializeDocument(document, writer, outputProps, factoryAttrs);
			result = true;
		} catch (TransformerException e) {
			GoGoEgo.debug("error").println(
					"DocumentUtils ERROR: writeVFSFile (" + uri + "): " + e.getMessage());
			result = false;
		}

		try {
			writer.close();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(writer, e);
		}

		return result;
	}

	/**
	 * Writes a document to the store's VFS. Will, by default, overwrite a file
	 * that already exists at this URI.
	 * 
	 * @param uri
	 *            where the file goes
	 * @param document
	 *            the document
	 * @return true if successful, false otherwise
	 * 
	 * @author adam.schwartz
	 */
	public static boolean writeVFSFile(VFSPath uri, VFS vfs, String string) {
		return writeVFSFile(uri, vfs, true, string);
	}

	/**
	 * Writes a document to the store's VFS, forcing an overwrite
	 * 
	 * @param uri
	 *            where the file goes
	 * @param String
	 *            the string
	 * @return true if successful, false otherwise, including if the
	 *         forceOverwrite is false
	 * 
	 * @author adam.schwartz
	 */
	public static boolean writeVFSFile(VFSPath uri, VFS vfs, boolean forceOverwrite, String string) {

		if (!forceOverwrite && vfs.exists(uri))
			return false;

		Writer writer = null;

		try {
			writer = vfs.getWriter(uri);
			writer.write(string);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			GoGoEgo.debug("fine").println("DocumentUtils ERROR: writeVFSFile (" + uri + "): " + e.getMessage());
			return false;
		} finally {
			try {
				writer.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
		}
	}

	public static String getVFSFileAsStringSafe(String uri, VFS vfs) {
		String content = null;
		try {
			if (!uri.startsWith("/")) {
				uri = "/" + uri;
			}
			VFSPath path = new VFSPath(uri);
			content = getVFSFileAsStringSafe(path, vfs);
		} catch (IllegalArgumentException e) {
		}

		return content;
	}

	public static String getVFSFileAsStringSafe(VFSPath vfsPath, VFS vfs) {
		String content = null;
		try {
			content = vfs.getString(vfsPath);
		} catch (NotFoundException nf) {
			System.err.println("  --" + vfsPath.toString() + " not found (probably this is ok)");
		} catch (BoundsException retryAsStream) {
			try {
				Reader in = vfs.getReader(vfsPath);
				BufferedReader buf = new BufferedReader(in);
				String line = "";
				String xml = "";
				while ((line = buf.readLine()) != null)
					xml += line;
				buf.close();
				return xml;
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException serious) {
			serious.printStackTrace();
		}

		return content;
	}

	/**
	 * @deprecated use MediaTypeManager.getMediaType() instead
	 */
	public static MediaType getMediaTypeFromFileExtension(String fileName) {
		MediaType media = MediaType.ALL;

		if (fileName.toLowerCase().endsWith("pdf"))
			media = MediaType.APPLICATION_PDF;
		else if (fileName.toLowerCase().endsWith("css"))
			media = MediaType.TEXT_CSS;
		else if (fileName.toLowerCase().endsWith("js"))
			media = MediaType.TEXT_JAVASCRIPT;
		else if (fileName.toLowerCase().endsWith("gif"))
			media = MediaType.IMAGE_GIF;
		else if (fileName.toLowerCase().endsWith("jpg") || fileName.toLowerCase().endsWith("jpeg"))
			media = MediaType.IMAGE_JPEG;
		else if (fileName.toLowerCase().endsWith("png"))
			media = MediaType.IMAGE_PNG;
		else if (fileName.toLowerCase().endsWith("tif") || fileName.toLowerCase().endsWith("tiff"))
			media = MediaType.IMAGE_TIFF;
		else if (fileName.toLowerCase().endsWith("doc"))
			media = MediaType.APPLICATION_WORD;
		else if (fileName.toLowerCase().endsWith("rtf"))
			media = MediaType.APPLICATION_RTF;
		else if (fileName.toLowerCase().endsWith("txt"))
			media = MediaType.APPLICATION_RTF;
		else if (fileName.toLowerCase().endsWith("xml"))
			media = MediaType.TEXT_XML;
		else if (fileName.toLowerCase().endsWith("html") || fileName.toLowerCase().endsWith("htm"))
			media = MediaType.TEXT_HTML;

		return media;
	}

	public static String clean(String cleanMe) {
		if (cleanMe != null) {
			cleanMe = cleanMe.replaceAll("(?!&amp;)(?!&lt;)(?!&gt;)(?!&quot;)&", "&amp;");
			cleanMe = cleanMe.replaceAll("<", "&lt;");
			cleanMe = cleanMe.replaceAll(">", "&gt;");
			cleanMe = cleanMe.replaceAll("\"", "&quot;");

			return cleanMe;
		}

		return "";
	}

	public static String cleanFromXML(String cleanMe) {
		if (cleanMe != null) {
			cleanMe = cleanMe.replaceAll("&amp;", "&");
			cleanMe = cleanMe.replaceAll("&lt;", "<");
			cleanMe = cleanMe.replaceAll("&gt;", ">");
			cleanMe = cleanMe.replaceAll("&quot;", "\"");

			return cleanMe;
		}

		return "";
	}

}
