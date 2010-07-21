/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.vfs.restlet;

import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;

/*
 * Copyright (C) 2009 Solertium Corporation
 * Copyright (C) 2003-2005 Frederico Caldiera Knabben
 *
 * This file is part of the open source GoGoEgo project, forked
 * from the FCKEditor project under the derivative works terms of GPL.
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
 * 
 * 
 */

public class FCKConnectorRestlet extends Restlet {
	protected final VFS vfs;

	public FCKConnectorRestlet(final Context context) {
		super(context);
		VFSProvidingApplication va = (VFSProvidingApplication) context.getAttributes().get(VFSProvidingApplication.INITIALIZING_KEY);
		if(va==null) va = (VFSProvidingApplication) Application.getCurrent();
		vfs = va.getVFS();
	}
	
	public FCKConnectorRestlet(final Context context, final VFS vfs) {
		super(context);
		this.vfs = vfs;
	}

	protected Node createCommonXML(final Document doc, final String commandStr,
			final String typeStr, final String currentPath,
			final String currentUrl) {
		System.out.println("Command: " + commandStr);
		System.out.println("Type: " + typeStr);
		System.out.println("Path: " + currentPath);
		System.out.println("Url: " + currentUrl);
		
		final Element root = doc.createElement("Connector");
		root.setAttribute("command", commandStr);
		root.setAttribute("resourceType", typeStr);

		final Element myEl = doc.createElement("CurrentFolder");
		myEl.setAttribute("path", currentPath);
		myEl.setAttribute("url", currentUrl);

		root.appendChild(myEl);
		doc.appendChild(root);

		return root;
	}

	private String getExtension(final String fileName) {
		return fileName.substring(fileName.lastIndexOf(".") + 1);
	}
	
	private void getElements(final String protocol, final VFSPath dir, final Node root, final Document doc) {
		final Element files = doc.createElement(protocol + "s");
		root.appendChild(files);
		VFSPathToken[] fileList;
		try {
			fileList = vfs.list(dir);
			ArrayList<String> sortedFileList = new ArrayList<String>();
			for (int i = 0; i < fileList.length; i++)
				sortedFileList.add(fileList[i].toString());
			
			Collections.sort(sortedFileList, new AlphanumericComparator());
			
			for (final String element : sortedFileList) {
				final VFSPath current = dir.child(new VFSPathToken(element));
				if ("Folder".equals(protocol) == vfs.isCollection(current)) {
					final Element myEl = doc.createElement(protocol);
					myEl.setAttribute("name", element);
					myEl.setAttribute("url", current.toString());
					
					if (protocol.equals("File"))
						myEl.setAttribute("size", "" + vfs.getLength(current));
					
					files.appendChild(myEl);
				}
			}
		} catch (final Exception e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}

	protected void getFiles(final String dir, final Node root, final Document doc) {
		try {
			getElements("File", VFSUtils.parseVFSPath(dir), root, doc);
		} catch (VFSUtils.VFSPathParseException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}
	
	protected void getFolders(final String dir, final Node root, final Document doc) {
		try {
			getElements("Folder", VFSUtils.parseVFSPath(dir), root, doc);
		} catch (VFSUtils.VFSPathParseException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}

	private String getNameWithoutExtension(final String fileName) {
		return fileName.substring(0, fileName.lastIndexOf("."));
	}

	@Override
	public void handle(final Request request, final Response response) {
		if (request.getMethod().equals(Method.GET))
			handleGet(request, response);
		else if (request.getMethod().equals(Method.POST))
			handlePost(request, response);
		else
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}

	protected void handleGet(final Request request, final Response response) {
		final Form parameters = request.getResourceRef().getQueryAsForm();
		final String commandStr = parameters.getValues("Command");
		final String typeStr = parameters.getValues("Type");
		final VFSPath currentFolderStr;
		try {
			System.out.println("Parsing " + parameters.getValues("CurrentFolder"));
			currentFolderStr = VFSUtils.parseVFSPath(parameters.getValues("CurrentFolder"));
		} catch (VFSUtils.VFSPathParseException e) {
			return;
		}

		final Document document = newDocument();

		final Node root = createCommonXML(document, commandStr, typeStr,
			parameters.getValues("CurrentFolder"), parameters.getValues("CurrentFolder"));

		if (commandStr.equalsIgnoreCase("GetFolders")) {
			getFolders(currentFolderStr.toString(), root, document);
		} else if (commandStr.equalsIgnoreCase("GetFoldersAndFiles")) {
			getFolders(currentFolderStr.toString(), root, document);
			getFiles(currentFolderStr.toString(), root, document);
		} else if (commandStr.equalsIgnoreCase("CreateFolder")) {
			final String newFolderStr = parameters.getValues("NewFolderName");
			String retValue = "110";
			if (vfs.exists(currentFolderStr.child(new VFSPathToken(newFolderStr)))) {
				retValue = "101";
			} else {
				try {
					vfs.makeCollection(currentFolderStr.child(new VFSPathToken(newFolderStr)));
					retValue = "0";
				} catch (final Exception e) {
					retValue = "102";
				}
			}
			final Element myEl = document.createElement("Error");
			myEl.setAttribute("number", retValue);
			root.appendChild(myEl);
		}

		response.setStatus(Status.SUCCESS_OK);
		Form headers = (Form) response.getAttributes().get(
				"org.restlet.http.headers");
		if (headers == null) {
			headers = new Form();
			response.getAttributes().put("org.restlet.http.headers", headers);
		}
		headers.add("Content-type", "text/xml; charset=UTF-8");
		headers.add("Cache-control", "no-cache");

		response.setEntity(new DomRepresentation(MediaType.TEXT_XML, document));
	}

	protected void handlePost(final Request request, final Response response) {
		final Form parameters = request.getResourceRef().getQueryAsForm();
		final String currentFolderStr = parameters.getValues("CurrentFolder");
		handlePost(request, response, currentFolderStr);
	}
	
	protected void handlePost(final Request request, final Response response, final String currentFolderStr) {
		final Form parameters = request.getResourceRef().getQueryAsForm();
		final String commandStr = parameters.getValues("Command");		

		String retVal = "0";
		String newName = "";

		if (!commandStr.equalsIgnoreCase("FileUpload"))
			retVal = "203";
		else {
			final RestletFileUpload upload = new RestletFileUpload(new DiskFileItemFactory());
			try {
				final List<?> items = upload.parseRequest(request);

				final Map<String, Object> fields = new HashMap<String, Object>();

				final Iterator<?> iterator = items.iterator();
				while (iterator.hasNext()) {
					final FileItem item = (FileItem) iterator.next();
					if (item.isFormField())
						fields.put(item.getFieldName(), item.getString());
					else
						fields.put(item.getFieldName(), item);
				}

				final FileItem uploadFile = (FileItem) fields.get("NewFile");
				String fileNameLong = uploadFile.getName();
				fileNameLong = fileNameLong.replace('\\', '/');
				final String[] pathParts = fileNameLong.split("/");
				final String fileName = pathParts[pathParts.length - 1];

				final String nameWithoutExt = getNameWithoutExtension(fileName);
				final String ext = getExtension(fileName);
				File pathToSave = new File(currentFolderStr, fileName);
				
				int counter = 1;
				while (pathToSave.exists()) {
					newName = nameWithoutExt + "(" + (counter++) + ")" + "." + ext;
					retVal = "201";
					pathToSave = new File(currentFolderStr, newName);
				}
				
				System.out.println("Path to save: " + pathToSave);
				try {
					OutputStream fout = vfs.getOutputStream(VFSUtils.parseVFSPath(pathToSave.getPath()));
					fout.write(uploadFile.get());
					fout.close();
				} catch (Exception e) { }
				
				
				//uploadFile.write(pathToSave);
			} catch (final Exception e) {
				e.printStackTrace();
				retVal = "203";
			}
		}

		response.setStatus(Status.SUCCESS_OK);
		Form headers = (Form) response.getAttributes().get(
				"org.restlet.http.headers");
		if (headers == null) {
			headers = new Form();
			response.getAttributes().put("org.restlet.http.headers", headers);
		}
		headers.add("Content-type", "text/xml; charset=UTF-8");
		headers.add("Cache-control", "no-cache");

		String script = "<html><head><title></title><script type=\"text/javascript\">";
		script += "function doWork() {";
		script += "window.parent.frames['frmUpload'].OnUploadCompleted(" + retVal + ",'" + newName + "');";
		//script += "window.parent.OnUploadComplete(" + retVal + ",'" + newName + "', '" + newName + "');";
		//script += "window.location.reload(true);";
		script += "}</script></head><body onload=\"doWork()\">Upload done!</body></html>";
		response.setEntity(new StringRepresentation(script, MediaType.TEXT_HTML));

		//System.out.println(script);
	}

	protected Document newDocument() {
		final DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.newDocument();
		} catch (final Exception e) {
			return null;
		}
	}
	
	class AlphanumericComparator implements Comparator<CharSequence>,	Serializable {
		private static final long serialVersionUID = 1L;

		public int compare(final CharSequence l, final CharSequence r) {
			int ptr = 0;
			int msd = 0;
			int diff = 0;
			char a, b;
	
			final int llength = l.length();
			final int rlength = r.length();
			final int min;
	
			if (rlength < llength)
				min = rlength;
			else
				min = llength;
	
			boolean rAtEnd, rHasNoMoreDigits;
	
			while (ptr < min) {
				a = l.charAt(ptr);
				b = r.charAt(ptr);
				diff = a - b;
				if ((a > '9') || (b > '9') || (a < '0') || (b < '0')) {
					if (diff != 0)
						return diff;
					msd = 0;
				} else {
					if (msd == 0)
						msd = diff;
					rAtEnd = rlength - ptr < 2;
					if (llength - ptr < 2) {
						if (rAtEnd)
							return msd;
						return -1;
					}
					if (rAtEnd)
						return 1;
					rHasNoMoreDigits = isNotDigit(r.charAt(ptr + 1));
					if (isNotDigit(l.charAt(ptr + 1))) {
						if (rHasNoMoreDigits && (msd != 0))
							return msd;
						if (!rHasNoMoreDigits)
							return -1;
					} else if (rHasNoMoreDigits)
						return 1;
				}
				ptr++;
			}
			return llength - rlength;
		}

		boolean isNotDigit(final char x) {
			return (x > '9') || (x < '0');
		}
	}
}


