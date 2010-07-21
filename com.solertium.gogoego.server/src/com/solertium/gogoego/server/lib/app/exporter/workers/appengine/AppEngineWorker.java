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
package com.solertium.gogoego.server.lib.app.exporter.workers.appengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.debugging.GoGoDebugger;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.lib.app.exporter.utils.ExportException;
import com.solertium.gogoego.server.lib.app.exporter.utils.SimpleExporterSettings;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorker;
import com.solertium.gogoego.server.lib.app.exporter.workers.appengine.AppEngineBaseExportFactory.AppEngineExportData;
import com.solertium.gogoego.server.lib.app.exporter.workers.base.ExportData;
import com.solertium.gogoego.server.lib.app.exporter.workers.base.SimpleExportInstructions;
import com.solertium.util.MD5Hash;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

/**
 * AppEngineWorker.java
 * 
 * Exports to AppEngine :)
 * 
 * @author liz.schwartz
 * 
 */
public class AppEngineWorker extends ExporterWorker {

	private AppEngineSettings settings;
	private AppEngineExportUtils utils;
	
	public AppEngineWorker(VFS vfs) {
		super(vfs);
	}
	
	private GoGoDebugger debug() {
		return GoGoDebug.get("debug");
	}
	
	public void init(VFSPath homeFolder, SimpleExporterSettings configuration) throws GoGoEgoApplicationException {
		settings = new AppEngineSettings(configuration);
		utils = new AppEngineExportUtils(vfs, settings);
		
		/*
		 * Load private settings
		 */
		final Document installDocument;
		try {
			installDocument = vfs.getDocument(AppEngineExporterManager.getVFSInstallPath());
		} catch (IOException e) {
			throw new GoGoEgoApplicationException("Private settings not found", e);
		}
		final NodeCollection fields = 
			new NodeCollection(installDocument.getDocumentElement().getChildNodes());
		for (Node field : fields)
			if (field.getNodeType() == Node.ELEMENT_NODE) {
				// Use DocUtils to avoid NPE with ConcurrentHashMap
				final String name = DocumentUtils.impl.getAttribute(field, "name");
				final String value = field.getTextContent();

				debug().println("Setting {0} to {1}", name, value);

				// This will break if bogus settings are supplied or
				// a setting can not be set.
				if (!settings.setProperty(name, value))
					throw new GoGoEgoApplicationException("Invalid property values");
			}
	}

	public boolean beforeSave(Document document) {
		debug().println("in beforeSave");

		final NodeCollection fields = new NodeCollection(document.getDocumentElement().getChildNodes());

		for (Node field : fields)

			if (field.getNodeType() == Node.ELEMENT_NODE) {
				// Use DocUtils to avoid NPE with ConcurrentHashMap
				final String name = DocumentUtils.impl.getAttribute(field, "name");
				final String value = field.getTextContent();

				Status error = utils.sendApplicationPropertiesToAppEngine("", name.toLowerCase(), value);
				if (error != null)
					return false;

			}

		return true;

	}

	/**
	 * Does a synchronize of all data between the sites
	 * 
	 * @throws ExportException
	 */
	public Document exportAll(final Document document, final Context context) throws ExportException {

		// DECLERATION and COLLECTION OF NECESSARY INFORMATION

		debug().println("in export all");
		final long currentTimestamp = new Date().getTime();
		final HashMap<String, Long> filenameToTimestamp = utils.parseLastUpdate();
		/*long lastUpdated = 0;
		if (filenameToTimestamp.containsKey(AppEngineExportUtils.LAST_UPLOADED)) {
			lastUpdated = filenameToTimestamp.remove(AppEngineExportUtils.LAST_UPLOADED);
		}*/
		final AppEngineBaseExportFactory exportHelper = (AppEngineBaseExportFactory) utils.getAppengineParser(context);
		final Map<VFSPath, ExportData> fileInformation = exportHelper.getExportData();

		try {
			exportFilesGivenInformation(context, utils, exportHelper, fileInformation, currentTimestamp,
					filenameToTimestamp, false);
		} catch (VFSPathParseException e) {
			return null;
		}

		return getSuccessStory();
	}

	/**
	 * Only exports the single file and the tags related to that file
	 */
	public Document exportFile(Document doc, final Context context) throws ExportException {
		// DECLERATION and COLLECTION OF NECESSARY INFORMATION
		final long currentTimestamp = new Date().getTime();
		final HashMap<String, Long> filenameToTimestamp = utils.parseLastUpdate();
		final Map<VFSPath, ExportData> fileInformation = new HashMap<VFSPath, ExportData>();
		final AppEngineBaseExportFactory exportHelper = (AppEngineBaseExportFactory) utils.getAppengineParser(context);

		NodeList files = doc.getDocumentElement().getElementsByTagName("file");
		ArrayList<String> filesToUpload = new ArrayList<String>();
		for (int i = 0; i < files.getLength(); i++) {
			String filename = files.item(i).getTextContent();
			if (!filename.startsWith("/")) {
				filename = "/" + filename;
			}
			filesToUpload.add(filename);

			try {
				fileInformation.putAll(exportHelper.getExportData(VFSUtils.parseVFSPath(filename), 0));
			} catch (VFSPathParseException e) {
				throw new ExportException(e.getCause());
			}

		}

		try {
			exportFilesGivenInformation(context, utils, exportHelper, fileInformation, currentTimestamp,
					filenameToTimestamp, true);
		} catch (VFSPathParseException e) {
			throw new ExportException(e.getCause());
		}

		return getSuccessStory();

	}

	protected void exportFilesGivenInformation(Context context, AppEngineExportUtils utils,
			AppEngineBaseExportFactory exportHelper, Map<VFSPath, ExportData> fileInformation, long currentTimestamp,
			HashMap<String, Long> filenameToTimestamp, boolean appendToUpdateDocument) throws VFSPathParseException,
			ExportException {

		debug().println("in exportFilesGivenInformation");
		Status error = null;
		final Document document;
		final Map<VFSPath, List<String>> parsedURIToTags = new HashMap<VFSPath, List<String>>();
		try {
			document = vfs.getDocument(AppEngineExporterManager.getVFSTagXMLPath());
			parsedURIToTags.putAll(SimpleExportInstructions.parseTagDocument(document));
		} catch (NotFoundException e1) {
			// IGNORE BECAUSE MEANS TAGS HAVE NOT BEEN LOOKED AT BEFORE
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new ExportException();
		}

		// RETURN IF THERE IS NOTHING TO UPLOAD
		if (fileInformation.isEmpty()) {
			debug().println("fileInformation is empty");
			return;
		}

		// UPDATE REMAINING USER FILES
		Iterator<Entry<VFSPath, ExportData>> iter = fileInformation.entrySet().iterator();
		while (iter.hasNext() && error == null) {
			Entry<VFSPath, ExportData> entry = iter.next();
			AppEngineExportData data = (AppEngineExportData) entry.getValue();

			if (!filenameToTimestamp.containsKey(data.getOriginalPath())
					|| (filenameToTimestamp.containsKey(data.getOriginalPath()) && filenameToTimestamp.get(data
							.getOriginalPath()) < data.getLastModified())) {
				if (data.getPath().equalsIgnoreCase("/index.html"))
					debug().println("the timestamp says {0}" + data.getLastModified());

				if (data.getSize() < 999951) {
					debug()
							.println("trying to send a request to server with hashes with filename " + data.getPath());
					error = utils.sendRequestToServerWithHashes((AppEngineExportData) data, filenameToTimestamp);

				} else {
					debug().println("trying to send link to server with filename " + data.getPath());
					error = utils.sendRequestToAddLink((AppEngineExportData) data);

					if (error == null) {
						debug().println("the sending was successfull");
						filenameToTimestamp.put(data.getOriginalPath(), data.getLastModified());
					}
				}

			}
		}

		// DELETE FILES
		if (error == null) {

			Set<String> fileNames = filenameToTimestamp.keySet();
			ArrayList<VFSPath> vfsPaths = new ArrayList<VFSPath>();

			for (String filename : fileNames) {
				vfsPaths.add(VFSUtils.parseVFSPath(filename));
			}

			List<VFSPath> filesToDelete = exportHelper.findDeletedResorces(vfsPaths);
			debug().println("These are the filenames to be deleted {0}", filesToDelete.toString());
			error = utils.deleteFiles(filesToDelete, parsedURIToTags, context);

			if (error == null) {
				debug().println("the error was null");
				for (VFSPath path : filesToDelete) {
					filenameToTimestamp.remove(path.toString());
					parsedURIToTags.remove(path);
				}
			}

		}

		// ADD TAGS TO FILES
		if (error == null) {
			error = updateTags(fileInformation, parsedURIToTags);

			if (error != null)
				throw new ExportException(error);
		}

		// FLUSH THE CACHE HOLDING PREVIOUS RESULTS
		debug().println("flushing the cache");
		utils.flushCache();

		// SAVE THE FILE UPLOAD INFORMATION
		if (!appendToUpdateDocument && error == null) {
			try {
				debug().println("saving the successfull upload");
				utils.saveSuccessfullLastUpdate(filenameToTimestamp, currentTimestamp);

			} catch (IOException e) {
				throw new ExportException(e);
			}

		}

		// THERE WAS AN ERROR, SO APPEND THE FILES THAT WE SUCCESSFULLY
		// UPLOADED, NOT CHANGING
		// ANY OF THE FILES THAT WEREN'T SUCCESSFULLY UPLOADED
		else {
			try {
				debug().println("saving the unsuccessfull upload");
				utils.saveErrorOrAppendLastUpdated(filenameToTimestamp);
			} catch (IOException e) {
				throw new ExportException(error);
			}
			if (error != null)
				throw new ExportException(error);
		}

		if (error == null) {
			DocumentUtils.writeVFSFile(AppEngineExporterManager.getVFSTagXMLPath().toString(), vfs,
					SimpleExportInstructions.createTagDocument(parsedURIToTags));
		}

	}

	private Status updateTags(Map<VFSPath, ExportData> fileInformation, Map<VFSPath, List<String>> previousTagMap) {
		Status error = null;

		debug().println("in updateTags");

		for (Entry<VFSPath, ExportData> currentInfo : fileInformation.entrySet()) {

			VFSPath path = currentInfo.getKey();

			AppEngineExportData data = (AppEngineExportData) currentInfo.getValue();
			List<String> currentTags = data.getTags();
			List<String> previousTags = previousTagMap.get(path);

			boolean sendTags = false;
			// AT LEAST THEY ARE BOTH NOT NULL

			if (currentTags == null && previousTags == null)
				sendTags = false;

			// DELETE CURRENT TAGS
			else if (currentTags == null && previousTags != null) {
				sendTags = true;
				debug().println("currentTags is null");
			}

			// ADD ALL TAGS
			else if (previousTags == null && currentTags != null) {
				sendTags = true;
				debug().println("previousTags is null");
			}

			// CHECK TO SEE IF YOU NEED TO ADD TAGS
			else if (previousTags != null && previousTags.size() == currentTags.size()) {
				ArrayList<String> tags = new ArrayList<String>(previousTags);
				tags.removeAll(currentTags);

				// NEED TO RESEND TAGS
				if (!tags.isEmpty()) {
					sendTags = true;
					debug().println("the tags is not empty");
				}

			}

			// SIZE IS NOT EQUAL, DEFINITELY ADD
			else {
				debug().println("the size is not equal");
				sendTags = true;
			}

			if (sendTags) {
				debug().println("sending tags for " + data.getPath());
				try {
					error = utils.addTags(VFSUtils.parseVFSPath(data.getPath()), data.getCategory(), currentTags,
							previousTags);
				} catch (VFSPathParseException e) {
					return Status.SERVER_ERROR_INTERNAL;
				}
				if (error == null) {
					previousTagMap.put(path, currentTags);
				}
			}

		}

		return error;

	}

	/**
	 * Does a complete refresh of all data (except the secret key) between
	 * appengine and GGE
	 */
	public Document refresh(Document document, final Context context) throws ExportException {

		boolean needsMoreDeleting = true;
		int count = 0;
		int max = 11;

		do {
			long timestamp = new Date().getTime();

			String xml = "<info timestamp=\"" + timestamp + "\" hash=\""
					+ new MD5Hash(settings.getGoGoKey() + timestamp).toString() + "\" />\r\n";

			debug().println("this is the xml I am sending " + xml);
			debug().println("with key " + settings.getGoGoKey());

			Request newRequest = new Request(Method.POST, settings.getWebAddress() + "/deleteAll",
					new StringRepresentation(xml, MediaType.TEXT_XML));

			Client client = new Client(Protocol.HTTP);
			Response newResponse = client.handle(newRequest);
			// Response newResponse =
			// getContext().getClientDispatcher().handle(newRequest);

			needsMoreDeleting = !newResponse.getStatus().equals(Status.SUCCESS_ACCEPTED);
			count++;

		} while (needsMoreDeleting && count < max);

		// SUCESSFUL DELETION OF ALL DATA
		if (!needsMoreDeleting) {
			try {
				if (vfs.exists(VFSUtils.parseVFSPath(AppEngineExportUtils.LAST_UPLOADED)))
					vfs.delete(VFSUtils.parseVFSPath(AppEngineExportUtils.LAST_UPLOADED));
			} catch (NotFoundException impossible) {
				TrivialExceptionHandler.handle(this, impossible);
			} catch (ConflictException unlikely) {
				TrivialExceptionHandler.handle(this, unlikely);
			} catch (VFSPathParseException impossible) {
				TrivialExceptionHandler.handle(this, impossible);
			}

			return exportAll(document, context);
			// synchronizeAllFiles(request, response);

		}

		// UNSUCCESSFUL ... TRY TO DELETE THINGS OUT OF THE UPLOADED FILES FILE
		else {
			try {
				if (vfs.exists(VFSUtils.parseVFSPath(AppEngineExportUtils.LAST_UPLOADED))) {

					Document doc = DocumentUtils.getReadWriteDocument(VFSUtils
							.parseVFSPath(AppEngineExportUtils.LAST_UPLOADED), vfs);
					Element docElement = doc.getDocumentElement();
					docElement.setAttribute("error", "true");
					DocumentUtils.writeVFSFile(AppEngineExportUtils.LAST_UPLOADED, vfs, doc);
				}

			} catch (VFSPathParseException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (ConflictException e) {
				e.printStackTrace();
			} catch (IOException unlikely) {
				TrivialExceptionHandler.handle(this, unlikely);
			}

			throw new ExportException(Status.SERVER_ERROR_INTERNAL, "Unable to some files "
					+ "in appengine.  Please try again to refresh your data.");
		}
	}

	public Representation doCommand(Document document, final Context context, String command) throws ExportException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns a document which holds the success information
	 * 
	 * @return
	 */
	public Document getSuccessStory() {
		return DocumentUtils.impl.createConfirmDocument("Successful Export.");
	}

}
