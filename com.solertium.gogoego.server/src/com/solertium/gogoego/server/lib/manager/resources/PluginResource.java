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
package com.solertium.gogoego.server.lib.manager.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.gogoego.server.lib.clienttools.UploadWorker;
import com.solertium.gogoego.server.lib.manager.container.ManagerApplication;
import com.solertium.gogoego.server.lib.settings.base.SimpleSettingsWorker.SimpleSettingsWorkerException;

/**
 * PluginResource.java
 * 
 * @author dave.fritz
 *
 */
public class PluginResource extends Resource {

	public PluginResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		getVariants().add(new Variant(MediaType.TEXT_XML));
		getVariants().add(new Variant(MediaType.MULTIPART_ALL));
	}

	public Representation represent(Variant variant) throws ResourceException {
		try {
			return getInit();
		} catch (SimpleSettingsWorkerException e) {
			e.printStackTrace();
		}
		return null;
	}

	private ArrayList<HashMap<String, String>> getPlugins() {

		ArrayList<HashMap<String, String>> plugins = new ArrayList<HashMap<String, String>>();
		String vmroot = ((ManagerApplication) ManagerApplication.getCurrent()).getVMRoot();
		try {
			File dir = new File(vmroot + "/plugins");
			File[] extensions = dir.listFiles();

			for (File file : extensions) {
				JarFile jarFile = new JarFile(file);
				HashMap<String, String> plugin = new HashMap<String, String>();
				plugin.put("name", (String) jarFile.getManifest().getMainAttributes().getValue("Bundle-Name"));
				plugin.put("version", (String) jarFile.getManifest().getMainAttributes().getValue("Bundle-Version"));
				plugin.put("vendor", (String) jarFile.getManifest().getMainAttributes().getValue("Bundle-Vendor"));
				plugin.put("filename", file.getName());
				// JarResources entry = new
				// JarResources((String)jarFile.getManifest().getMainAttributes().getValue("Bundle-Activator"));

				plugins.add(plugin);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return plugins;
	}

	@SuppressWarnings("unchecked")
	public Representation getInit() throws SimpleSettingsWorkerException {
		Document initDoc = DocumentUtils.impl.newDocument();

		Element root = initDoc.createElement("root");
		for (Map<String, Object> entry : PluginAgent.getBundleManagementBroker().getAvailableBundles()) {

			Element element = initDoc.createElement("plugin");
			element.setAttribute("name", (String) entry.get("name"));
			element.setAttribute("version", (String) entry.get("version"));
			element.setAttribute("vendor", (String) entry.get("provider"));
			element.setAttribute("description", (String) entry.get("description"));
			for (String obj : (ArrayList<String>) entry.get("services")) {
				Element filterOn = initDoc.createElement("services");
				filterOn.setTextContent(obj);
				element.appendChild(filterOn);
			}

			root.appendChild(element);

		}
		initDoc.appendChild(root);

		return new DomRepresentation(MediaType.TEXT_XML, initDoc);
	}

	public void acceptRepresentation(Representation entity) throws ResourceException {
		Document doc = DocumentUtils.impl.newDocument();

		final Element root = doc.createElement("root");
		doc.appendChild(root);

		final FileItemFactory factory = new DiskFileItemFactory();
		final RestletFileUpload uploadServlet = new RestletFileUpload(factory);
		String vmroot = ((ManagerApplication) ManagerApplication.getCurrent()).getVMRoot();

		String uri = vmroot + "/plugins/";

		try {
			final List<FileItem> items = uploadServlet.parseRequest(getRequest());
			for (final FileItem item : items)
				if (!item.isFormField()) {
					final String filename = UploadWorker.convertWindowsPath(item.getName()).toString();

					final String fileUri = uri + "/" + filename;

					if (item.getFieldName().equals("")) {
						Element failure = doc.createElement("file");
						failure.appendChild(DocumentUtils.impl.createCDATAElementWithText(doc, "name", filename
								.toString()));
						failure.appendChild(DocumentUtils.impl.createElementWithText(doc, "status", "FAILURE"));
						failure.appendChild(DocumentUtils.impl.createCDATAElementWithText(doc, "notes",
								"No field name supplied"));

						root.appendChild(failure);
						continue;
					}

					final OutputStream out = new FileOutputStream(fileUri);
					out.write(item.get());
					out.close();

					Element success = doc.createElement("file");
					success
							.appendChild(DocumentUtils.impl
									.createCDATAElementWithText(doc, "name", filename.toString()));
					success.appendChild(DocumentUtils.impl.createElementWithText(doc, "status", "SUCCESS"));
					root.appendChild(success);
					
					PluginLogWriter.log(vmroot, filename.toString(), "System: Uploaded new version of plugin.");
				}

			getResponse().setStatus(Status.SUCCESS_MULTI_STATUS);
			getResponse().setEntity(
					new StringRepresentation(DocumentUtils.impl.serializeDocumentToString(doc, true),
							MediaType.TEXT_HTML));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeRepresentations() throws ResourceException {
		String vmroot = ((ManagerApplication) ManagerApplication.getCurrent()).getVMRoot();
		ArrayList<HashMap<String, String>> plugins = getPlugins();
		String name = (String) getRequest().getAttributes().get("plugin");
		String version = (String) getRequest().getAttributes().get("version");

		for (HashMap<String, String> pluginData : plugins) {
			if (pluginData.get("name").equals(name) && pluginData.get("version").equals(version)) {
				File file = new File(vmroot + "/plugins/" + pluginData.get("filename"));
				if (file.delete()) {
					//Yes, this actually does work...
					if (file.exists()) file.delete();
					PluginLogWriter.log(vmroot, pluginData.get("filename"), "System: Deleted Plugin");
					getResponse().setStatus(Status.SUCCESS_OK);
				}
				else
					getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		}

	}

}
