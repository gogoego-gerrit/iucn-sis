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
package com.solertium.db.restlet;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Filter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.RestletUtils;

/**
 * QueryBuilderFilter.java
 * 
 * The afterHandle checks to see if the results of a query 
 * that got run need to be handled in some special fashion 
 * (set for download, Word/Excel/HTML version, etc).
 *
 * @author carl.scott
 *
 */
public class QueryExportFilter extends Filter {
	
	public QueryExportFilter(Context context) {
		super(context);
	}
	
	public QueryExportFilter(Context context, Restlet next) {
		super(context, next);
	}
	
	/**
	 * Tries to fetch a query from the VFS.  If this query is supposed to 
	 * be downloaded as Word or Excel, the proper conversions are made and  
	 * appropriate headers are attached.
	 */
	public void afterHandle(Request request, Response response) {
		if (!response.getStatus().isSuccess())
			return;
		
		final String version = request.getResourceRef().getQueryAsForm().getFirstValue("version");
		final String download = request.getResourceRef().getQueryAsForm().getFirstValue("download");
		if (version == null || version.equals("")) 
			return;
		
		final String dateTimeMatch = "\\d.*-\\d*-\\d* \\d\\d:\\d\\d:\\d\\d";
		String extension = "xml";
		
		Document doc;
		try {
			doc = new DomRepresentation(response.getEntity()).getDocument();
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(this, e);
			return;
		}
		
		final Representation entity;
		
		if (version.equals("excel")) {
			extension = "csv";
				
			StringBuffer csv = new StringBuffer();
			String header = "";
				
			NodeList nodes = doc.getElementsByTagName("row");
				
			Node firstRow = nodes.item(0);
			for (int i = 0; i < firstRow.getChildNodes().getLength(); i++) {
				Node cur = firstRow.getChildNodes().item(i);
				if (cur.getNodeName().equals("field")) {
					header += (!header.equals("") ? "," : "") + 
					(BaseDocumentUtils.impl.getAttribute(cur, "name"));
				}
			}
				
			csv.append(header);
			csv.append("\r\n");
				
			for (int i = 0; i < nodes.getLength(); i++) {
				Node cur = nodes.item(i);
				if (cur.getNodeName().equals("row")) {
					String row = "";
					for (int k = 0; k < cur.getChildNodes().getLength(); k++) {
						Node field = cur.getChildNodes().item(k);
						if (field.getNodeName().equals("field")) {
							String text = field.getTextContent().trim();
							if (text.matches(dateTimeMatch))
								text = text.split(" ")[0];
							else if (text.startsWith("+") || text.startsWith("-"))
								text = " " + text;
							if (text.indexOf(',') != -1)
								text = "\"" + text + "\"";
							row += (!row.equals("") ? "," : "") + text;
						}
					}
					csv.append(row);
					csv.append("\r\n");
				}
			}
			
			/*
			 * Hack Fix for files that start with "ID"...
			 * http://support.microsoft.com/kb/323626
			 * 
			 * To open your file in Excel, open the file in a text editor, and 
			 * then insert an apostrophe at the beginning of the first line of text.
			 *
			 * How to Insert an Apostrophe
			 * To add an apostrophe to the beginning of the first line of text 
			 * in your file, follow these steps:
			 *   1. Open the text file in a text editor, such as Notepad. 
			 *   Click before the first character in the first line of text. 
			 *   Press the APOSTROPHE key on your keyboard (').
			 *   2. On the File menu, click Save. Quit the text editor. 
			 *   You can now open the file in Excel.
			 */
			String content = csv.toString();
			if (content.startsWith("ID"))
				content = "'" + content;
			
			entity = newStringRepresentation(content, MediaType.TEXT_ALL);
		}
		else if (version.equals("word") || version.equals("html")) {
			extension = version.equals("html") ? "html" : "doc";
						
			String head  = "<html><head><style>table {width: 100%;  border-collapse: collapse;  border: 1px solid #a0a0a0;	}" + 
				"td, th { border-top: 1px solid #a0a0a0;  border-left: 1px solid #a0a0a0;}" + 
				"th {  font-size: x-small;	}" +
				"td.elided {  border-top: none;	}" +
				"h1 {  font-size: 16pt;  font-weight: bold;  padding-top: 5px;  margin-top: 35px;  margin-bottom: 5px;  padding-bottom: 0px;  border-top: 1px dashed #a0a0a0;}"+
				"h2 {  font-size: 14pt;  font-weight: normal;  padding-top: 5px;	  margin-top: 5px;  margin-bottom: 5px;  padding-bottom: 0px;}"+
				"h3 {  font-size: 12pt;  font-weight: normal;  padding-top: 0px;  margin-top: 0px;  margin-bottom: 0px;  padding-bottom: 0px;}"+
				"h4 {  font-size: 10pt;  font-weight: normal;  padding-top: 0px;  margin-top: 0px;  margin-bottom: 0px;  padding-bottom: 0px;}"+
				"</style></head>";
			
			StringBuffer table = new StringBuffer();
			String header = "<body><table><tr>";
			
			NodeList nodes = doc.getElementsByTagName("row");
			
			Node firstRow = nodes.item(0);
			for (int i = 0; i < firstRow.getChildNodes().getLength(); i++) {
				Node cur = firstRow.getChildNodes().item(i);
				if (cur.getNodeName().equals("field")) {
					header += "<th>" + (BaseDocumentUtils.impl.getAttribute(cur, "name")) + "</th>";
				}
			}
				
			table.append(head);
			table.append(header);
			table.append("</tr>");
			
			for (int i = 0; i < nodes.getLength(); i++) {
				Node cur = nodes.item(i);
				if (cur.getNodeName().equals("row")) {
					String row = "<tr>";
					for (int k = 0; k < cur.getChildNodes().getLength(); k++) {
						Node field = cur.getChildNodes().item(k);
						if (field.getNodeName().equals("field")) {
							String data = field.getTextContent().trim();
							if (data.matches(dateTimeMatch))
								data = data.split(" ")[0];
							row += "<td>" + data + "</td>";								
						}
					}
					row += "</tr>";
					table.append(row);
				}
			}
			table.append("</table></body></html>");
		
			entity = newStringRepresentation(table.toString(), 
				version.equals("word") ? MediaType.APPLICATION_WORD : MediaType.TEXT_HTML
			);
		}
		else {
			extension = "xml";			
			entity = newDOMRepresentation(doc, MediaType.TEXT_XML);			
		}
		
		if ("true".equals(download)) {
			RestletUtils.addHeaders(response, "content-disposition",
					"attachment; filename=query." + extension);
			entity.setDownloadable(true);
			entity.setDownloadName("query." + extension);
		}
		
		response.setEntity(entity);
	}
	
	protected Representation newStringRepresentation(String content, MediaType mediaType) {
		return new StringRepresentation(content, mediaType);
	}
	
	protected Representation newDOMRepresentation(Document document, MediaType mediaType) {
		return new DomRepresentation(mediaType, document);
	}

}

	