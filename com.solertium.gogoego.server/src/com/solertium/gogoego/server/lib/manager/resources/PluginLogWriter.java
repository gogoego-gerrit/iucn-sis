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
package com.solertium.gogoego.server.lib.manager.resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.restlet.data.MediaType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.MediaTypeManager;

/**
 * PluginLogWriter.java
 * 
 * Write new comment entries to the log file.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class PluginLogWriter {
	
	public static void log(String vmroot, String plugin, String comment) {
		final File folder = new File(vmroot + File.separator + "log");
		if (!folder.exists())
			folder.mkdirs();
		
		final String uri = vmroot + File.separator + "log" + File.separator + plugin + (plugin.endsWith(".xml") ? "" : ".xml");
		
		final Document document = getExistingDocument(uri);
		
		final Element date = document.createElement("field");
		date.setAttribute("name", "date");
		date.setTextContent(new Date().getTime()+"");
		
		final Element log = BaseDocumentUtils.impl.
			createCDATAElementWithText(document, "field", comment);
		log.setAttribute("name", "comment");
		
		final Element row = document.createElement("row");
		row.appendChild(date);
		row.appendChild(log);
		
		final Node firstChild = document.getDocumentElement().getFirstChild();
		if (firstChild == null)
			document.getDocumentElement().appendChild(row);
		else
			document.getDocumentElement().insertBefore(row, firstChild);
		
		final Writer writer;
		try {
			writer = new BufferedWriter(new PrintWriter(new FileWriter(new File(uri))));
		} catch (IOException e) {
			return;
		} catch (Exception e) {
			return;
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

		try {
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
		} catch (TransformerException e) {
			e.printStackTrace();
			TrivialExceptionHandler.ignore(writer, e);
		}

		try {
			writer.close();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(writer, e);
		}
	}
	
	private static Document getExistingDocument(final String uri) {
		final InputStream stream;
		try {
			stream = new FileInputStream(new File(uri));
		} catch (FileNotFoundException e) {
			final Document document = BaseDocumentUtils.impl.newDocument();
			document.appendChild(document.createElement("root"));
			return document;
		}
		
		return BaseDocumentUtils.impl.getInputStreamFile(stream);	
	}

}
