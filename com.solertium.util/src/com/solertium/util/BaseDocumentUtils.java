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

package com.solertium.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * BaseDocumentUtils.java
 * 
 * A utility class to do some of the more mundane operations.
 * 
 * @author carl.scott
 * 
 */
public class BaseDocumentUtils {
	
	public static final BaseDocumentUtils impl = new BaseDocumentUtils();
	
	public static BaseDocumentUtils getInstance() {
		return impl;
	}
	
	protected BaseDocumentUtils() {}

	/**
	 * Writes a document that sends a simple confirmation message
	 * 
	 * @param confirm
	 *            the message
	 * @return the Document <response> <status>@param confirm</status>
	 *         </response>
	 */
	public Document createConfirmDocument(String confirm) {
		if (confirm == null)
			confirm = "true";

		return createDocumentFromString("<response>\r\n<status><![CDATA["
			+ confirm + "]]></status>\r\n</response>");
	}

	public Document createDocumentFromString(final String xml) {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(xml)));
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates an element with text :)
	 * 
	 * @param document
	 *            the document for which the element is to be created
	 * @param elementName
	 *            the name of the new element
	 * @param text
	 *            the text to put in the element
	 * @return a new element <@param elementName>@param text</@param
	 *         elementName>
	 * 
	 */
	public Element createElementWithText(final Document document,
			final String elementName, final String text) {
		final Element element = document.createElement(elementName);
		element.appendChild(document.createTextNode(text == null ? "" : text));
		return element;
	}

	/**
	 * Creates a CDATA element with text :)
	 * 
	 * @param document
	 *            the document for which the element is to be created
	 * @param elementName
	 *            the name of the new element
	 * @param text
	 *            the text to put in the element
	 * @return a new element <@param elementName><![CDATA[@param text]]></@param
	 *         elementName>
	 * 
	 */
	public Element createCDATAElementWithText(final Document document, 
			final String elementName, final String text) {
		final Element element = document.createElement(elementName);
		element.appendChild(document.createCDATASection(text == null ? "" : text));
		return element;
	}
	
	public Document createErrorDocument(final String error) {
		return createConfirmDocument(error);
	}

	/**
	 * this fail-safe method will get an attribute from a node
	 * 
	 * @param node
	 *            the node
	 * @param attribute
	 *            the name of the attribute
	 * @return the attribute, or "" if nothing there
	 */
	public String getAttribute(final Node node, final String attribute) {
		try {
			final String ret = node.getAttributes().
				getNamedItem(attribute).getTextContent();
			return (ret != null) ? ret : "";
		} catch (final Exception e) {
			return "";
		}
	}
	
	/**
	 * Gets a document from input stream
	 * @param uri where the file is
	 * @return the Document
	 */
	public Document getInputStreamFile(InputStream stream) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line = "", xml = "";
			while ((line = reader.readLine()) != null)
				xml += line;
			return createDocumentFromString(xml);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Creates a new document and returns it, or null on error.
	 * @return
	 */
	public Document newDocument() {
		final DocumentBuilderFactory factory = 
			DocumentBuilderFactory.newInstance();
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.newDocument();
		} catch (final Exception e) {
			return null;
		}

	}

	/**
	 * Serialize a document to string, includes the headers and does not add tabs
	 * @param document the document to serialize
	 * @return the doc as a string
	 */
	public String serializeDocumentToString(final Document document) {
		return serializeDocumentToString(document, false, false);
	}
	
	/**
	 * Serialize a document to string, option to include headers, and does not add tabs
	 * @param document the document to serialize
	 * @param stripDocType true if you want to strip doc type declaration, false otherwise
	 * @return the doc as a string
	 */
	public String serializeDocumentToString(final Document document, boolean stripDocType) {
		return serializeDocumentToString(document, stripDocType, false);
	}
	
	/**
	 * Serializes a document to a string, allowing for passing of doc type inclusion 
	 * and indention parameters, setting them in accordance to the transformer API
	 * @param document the document
	 * @param stripDocType true if you want to strip the doc type from the output, false otherwise
	 * @param indent true if you want prettier xml output, false, otherwise
	 * @return
	 */
	public String serializeDocumentToString(final Document document, boolean stripDocType, boolean indent) {
		HashMap<String, String> outputProps = new HashMap<String, String>();
		outputProps.put(OutputKeys.INDENT, indent ? "yes" : "no");
		outputProps.put(OutputKeys.OMIT_XML_DECLARATION, stripDocType ? "yes" : "no");
		
		HashMap<String, String> factoryAttrs = new HashMap<String, String>();
		factoryAttrs.put("indent-number", "3");
		
		final StringWriter writer = new StringWriter();
		try {
			_serializeDocument(document, writer, outputProps, factoryAttrs);
		} catch (TransformerException e) {
			TrivialExceptionHandler.ignore(writer, e);
			return "";
		}
		
		return writer.toString();
	}
	
	protected void _serializeDocument(final Document document, final Writer writer, 
		final HashMap<String, String> outputProps, final HashMap<String, String> factoryAttrs) 
		throws TransformerException {
		
		final TransformerFactory tfac = TransformerFactory.newInstance();
		if (factoryAttrs != null) {
			final Iterator<Map.Entry<String, String>> it = factoryAttrs.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> cur = it.next();
				tfac.setAttribute(cur.getKey(), cur.getValue());
			}					
		}
		final Transformer t = tfac.newTransformer();
		if (outputProps != null) {
			final Iterator<Map.Entry<String, String>> it = outputProps.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> cur = it.next();
				t.setOutputProperty(cur.getKey(), cur.getValue());
			}
		}
		
		t.transform(new DOMSource(document), new StreamResult(writer));
	}
	
	/**
	 * Removes doc type headers from an XML string.
	 * 	 * 
	 * If you are using after a serialize document to string call, 
	 * use serializeDocumentToString(document, true) instead.
	 * 
	 * @deprecated use serializeDocumentToString(document, true) instead.
	 * 
	 * @param xml the xml as a string
	 * @return the new string
	 */
	public String stripDocType(String xml) {
		int index = xml.indexOf("?>");
		return (index != -1) ? xml.substring(index+2) : xml;
	}

	/**
	 * Serializes a node to a string
	 * @param node the node
	 * @return
	 */
	public String serializeNodeToString(Node node) {
		try {
			StringWriter writer = new StringWriter();
			
			Transformer dt = TransformerFactory.newInstance().newTransformer();
        	dt.setOutputProperty(OutputKeys.METHOD, "xml");
        	dt.setOutputProperty(OutputKeys.INDENT, "yes");
        	dt.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        	
			dt.transform(new DOMSource(node), new StreamResult(writer));
			writer.flush();
	           
			return writer.toString();
		} catch (TransformerConfigurationException f) {
			f.printStackTrace();
			return "";
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		} 
	}

}
