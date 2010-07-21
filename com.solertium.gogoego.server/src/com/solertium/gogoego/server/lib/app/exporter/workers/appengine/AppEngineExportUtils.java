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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gogoego.api.debugging.GoGoDebugger;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.lib.app.exporter.utils.ExportException;
import com.solertium.gogoego.server.lib.app.exporter.workers.appengine.AppEngineBaseExportFactory.AppEngineExportData;
import com.solertium.gogoego.server.lib.app.tags.utils.TagApplicationDataUtility;
import com.solertium.util.MD5Hash;
import com.solertium.util.SHA1Hash;
import com.solertium.util.restlet.RestletUtils;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSException;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

/**
 * AppEngineExportUtils.java
 * 
 * A utility class that performs helper functions for the basic export functions
 * of the AppEngineExporter ExporterWorker.
 * 
 * @author liz.schwartz
 * 
 */
public class AppEngineExportUtils {

	public static final String ONE_TAG_FILE = ".GGESelective";
	public static final String DIRECTORY_PATH = "/(SYSTEM)/exporter/config/appengine/";
	public static final String LAST_UPLOADED = DIRECTORY_PATH + "uploadedFiles.xml";
	public static final String directoryToIgnore = "/(SYSTEM)";
	public final static String tempDirectory = "tmp";
	public final static String trashDirectory = "Trash";
	public final static String[] filesToSpecificallyFetch = { "/(SYSTEM)/collections", "/(SYSTEM)/scriptobjects.xml" };

	private static final int MAX_TRIES = 5;
	private static final String KEY_FILE = DIRECTORY_PATH + "config.xml";
	private final VFS vfs;
	private final AppEngineSettings settings;

	/**
	 * Constructor
	 * 
	 * @param vfs
	 * @param settings
	 */
	public AppEngineExportUtils(final VFS vfs, final AppEngineSettings settings) {
		this.vfs = vfs;
		this.settings = settings;
	}

	/**
	 * Takes the last uploaded file, and enters in the last synchronization
	 * information
	 * 
	 * @return
	 */
	public HashMap<String, Long> parseLastUpdate() {
		try {
			VFSPath lastUploadedPath = VFSUtils.parseVFSPath(LAST_UPLOADED);

			HashMap<String, Long> filenameToTimestamp = new HashMap<String, Long>();
			boolean uploadedBefore = vfs.exists(lastUploadedPath);

			if (uploadedBefore) {
				Document doc = DocumentUtils.getReadOnlyDocument(lastUploadedPath, vfs);

				// GET LAST TIME THE SYNCHRONIZATION OCCURED
				NodeList lastUpdatedList = doc.getDocumentElement().getElementsByTagName("lastUpdated");
				if (lastUpdatedList.getLength() > 0) {
					filenameToTimestamp.put(LAST_UPLOADED, new Long(lastUpdatedList.item(0).getTextContent()));
				}

				// GET LAST MODIFIED FOR ALL FILES
				NodeList list = doc.getDocumentElement().getElementsByTagName("file");
				for (int i = 0; i < list.getLength(); i++) {
					Element fileElement = (Element) list.item(i);
					filenameToTimestamp.put(DocumentUtils.cleanFromXML(fileElement.getAttribute("filename")), new Long(
							fileElement.getAttribute("lastModified")));
				}
			}
			return filenameToTimestamp;
		} catch (VFSPathParseException impossible) {
			impossible.printStackTrace();
			return null;
		} catch (NotFoundException impossible) {
			impossible.printStackTrace();
			return null;
		} catch (IOException unlikely) {
			unlikely.printStackTrace();
			return null;
		}
	}
	
	private GoGoDebugger debug() {
		return GoGoDebug.get("debug");
	}

	/**
	 * Deletes all temporary files on appengine, not currently used, but should
	 * maybe be used in the future.
	 */
	/*
	 * public void deleteTmpFiles() { Request newRequest = new
	 * Request(Method.DELETE, settings.getWebAddress()); Client client = new
	 * Client(Protocol.HTTP); Response newResponse = client.handle(newRequest);
	 * // Response newResponse =
	 * getContext().getClientDispatcher().handle(newRequest); if
	 * (newResponse.getStatus().isError()) { client = new Client(Protocol.HTTP);
	 * newResponse = client.handle(newRequest); // newResponse =
	 * getContext().getClientDispatcher().handle(newRequest); } }
	 */

	/**
	 * Returns a hashMap<String, FileInformation> of all files on the filesystem
	 * under the head directory that we need to
	 * 
	 */
	// public Map<VFSPath, ExportData > getFiles() throws VFSPathParseException
	// {
	// ArrayList<String> directoriesToIgnore = new ArrayList<String>();
	// directoriesToIgnore.add(directoryToIgnore);
	// directoriesToIgnore.add(tempDirectory);
	// directoriesToIgnore.add(trashDirectory);
	//
	// return ExportUtils.getFiles(vfs, directoriesToIgnore, null, new
	// ArrayList<String>(Arrays
	// .asList(filesToSpecificallyFetch)));
	//		
	// return exportHelper.getExportData();
	// }
	/**
	 * Sends the application properties to appengine
	 * 
	 * @param onlyOnChange
	 * @param filenamesToTimestamp
	 * @param uploads
	 * @return
	 * @throws VFSPathParseException
	 * @throws NotFoundException
	 */
	public Status updateApplicationInitialization(boolean onlyOnChange, HashMap<String, String> filenamesToTimestamp,
			ArrayList<String> uploads) throws VFSPathParseException, NotFoundException {
		Status error = null;
		VFSPath vfspath = VFSUtils.parseVFSPath(KEY_FILE);

		if (onlyOnChange) {
			String oldTimestamp = filenamesToTimestamp.get(vfspath.toString());
			String newTimestamp = "" + vfs.getLastModified(vfspath);
			if (!newTimestamp.equals(oldTimestamp)) {
				onlyOnChange = false;
				filenamesToTimestamp.put(vfspath.toString(), newTimestamp);
			}

		}

		if (!onlyOnChange) {
			error = sendApplicationPropertiesToAppEngine("", "applicationID", settings.getApplicationID());
			if (error == null) {
				error = sendApplicationPropertiesToAppEngine("", "baseLink", settings.getLinkAddress());
			}

		}

		if (error == null) {
			uploads.add(KEY_FILE);
		}

		return error;

	}

	// /**
	// * Updates the application properties for the appengine application.
	// *
	// * @param onlyOnChange
	// * -- boolean specifies if only update when property files have
	// * changed
	// * @param filenameToLastModified
	// * -- hashmap of all files looked at with their timestamp of last
	// * modified
	// * @param uploads
	// * -- arraylist of successfully uploaded files, or files that
	// * didn't need uploading
	// * @return
	// * @throws VFSPathParseException
	// */
	// public Status updateApplicationProperties(HashMap<String, Long>
	// filenameToLastModified, boolean onlyOnChange,
	// ArrayList<VFSPath> uploads) throws VFSPathParseException {
	// Status error = null;
	// VFSPath vfspath = VFSUtils.parseVFSPath(LocalProperties.LOCATION +
	// "/appengine");
	//
	// if (vfs.exists(vfspath)) {
	//
	// try {
	// VFSPathToken[] tokens = vfs.list(vfspath);
	// Properties property = new Properties();
	//
	// for (int i = 0; i < tokens.length && error == null; i++) {
	// VFSPathToken token = tokens[i];
	// VFSPath fullpath = vfspath.child(token);
	//
	// String newTimestamp = "" + vfs.getLastModified(fullpath);
	// String oldTimestamp =
	// filenameToLastModified.get(fullpath.toString()).toString();
	//
	// boolean doUpdate = false;
	// if (!newTimestamp.equals(oldTimestamp)) {
	// doUpdate = true;
	// filenameToLastModified.put(fullpath.toString(), new Long(newTimestamp));
	// }
	//
	// if (!onlyOnChange || doUpdate) {
	// HashMap<String, String> propertyToValues = new HashMap<String, String>();
	// property.clear();
	// property.load(vfs.getInputStream(fullpath));
	//
	// for (Object oprop : property.keySet()) {
	// // property.stringPropertyNames not in Java 1.5 --
	// // so cast instead
	// String prop = (String) oprop;
	// Status updateStatus =
	// sendApplicationPropertiesToAppEngine(token.toString(), prop,
	// propertyToValues.get(prop));
	//
	// if (updateStatus.isError()) {
	// error = updateStatus;
	// }
	// }
	//
	// }
	//
	// if (error == null) {
	// uploads.add(fullpath);
	// }
	//
	// }
	//
	// } catch (NotFoundException impossible) {
	// error = Status.SERVER_ERROR_INTERNAL;
	// } catch (IOException e) {
	// error = Status.SERVER_ERROR_SERVICE_UNAVAILABLE;
	// }
	//
	// }
	// return error;
	//
	// }

	/**
	 * Sends a request to the server when all is needed is the method, the
	 * webaddress to send the request to, and the content of the request object.
	 * 
	 * @param method
	 * @param webaddress
	 * @param xml
	 * @return
	 */
	public Status sendRequestToServerWithHashes(AppEngineExportData data, HashMap<String, Long> filenameToTimestamp) {
		Status error = null;
		String filename = data.getPath();
		String originalFilename = data.getOriginalPath();

		String newTimestamp = new Date().getTime() + "";
		String[] hashes = getHashes(originalFilename);
		String address = settings.getWebAddress();

		debug().println("trying to send originalFilename {0} with filename {1} with newTimestamp: {2}", 
				originalFilename, filename, newTimestamp);

		int i = 0;
		Request newRequest = null;
		Response newResponse = null;

		do {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// DO NOTHING
			}
			Representation representation = data.getRepresentation();
			if (representation == null)
				break;

			try {
				newRequest = new Request(Method.PUT, address + "?filename=" + URLEncoder.encode(filename, "UTF-8"),
						representation);
			} catch (UnsupportedEncodingException e) {
				return Status.SERVER_ERROR_INTERNAL;
			}
			setHeaders(newRequest, hashes[1], hashes[0], newTimestamp, data.getCategory());
			Client client = new Client(Protocol.HTTP);
			newResponse = new Response(newRequest);
			client.handle(newRequest, newResponse);
			i++;

		} while (newResponse.getStatus().isServerError() && i < MAX_TRIES);

		if (newResponse == null)
			error = Status.SERVER_ERROR_INTERNAL;

		else if (newResponse.getStatus().isError())
			error = newResponse.getStatus();

		return error;

	}

	public Status sendRequestToAddLink(AppEngineExportData data) {
		final String webaddress = settings.getWebAddress() + "/addLink";
		Status error = sendRequestToServer(Method.PUT, webaddress, "<xml><lastModified>" + data.getLastModified()
				+ "</lastModified>\r\n<filename>" + DocumentUtils.clean(data.getPath()) + "</filename>" + "\r\n<type>"
				+ data.getCategory() + "</type>\r\n</xml>");
		return error;
	}

	// /**
	// * Returns an xml string representing tagging information, when there was
	// an
	// * error, returns null.
	// */
	// public String getTaggingInformation(String filename) {
	//
	// try {
	// String folderName = filename.substring(0,
	// filename.lastIndexOf("/.tags.xml"));
	// debug.println("the foldername is " + folderName);
	// VFSPath folderPath = VFSUtils.parseVFSPath(folderName);
	// Document doc =
	// DocumentUtils.getReadOnlyDocument(VFSUtils.parseVFSPath(filename), vfs);
	// Element documentElement = doc.getDocumentElement();
	// ArrayList<String> defaultTags = null;
	//
	// if (documentElement.getElementsByTagName("default").getLength() > 0) {
	// NodeList defaultNodeList = ((Element)
	// documentElement.getElementsByTagName("default").item(0))
	// .getElementsByTagName("tag");
	//
	// defaultTags = new ArrayList<String>();
	// for (int i = 0; i < defaultNodeList.getLength(); i++) {
	// defaultTags.add(defaultNodeList.item(i).getTextContent());
	// }
	//
	// }
	// NodeList fileTags = documentElement.getElementsByTagName("file");
	// HashMap<String, ArrayList<String>> filenameToTags = new HashMap<String,
	// ArrayList<String>>();
	//
	// for (int i = 0; i < fileTags.getLength(); i++) {
	// Element fileTag = (Element) fileTags.item(i);
	// String localFilename = folderName + "/" + fileTag.getAttribute("name");
	// ArrayList<String> tags = new ArrayList<String>();
	//
	// NodeList localFileTags = fileTag.getElementsByTagName("tag");
	// for (int j = 0; j < localFileTags.getLength(); j++) {
	// tags.add(localFileTags.item(j).getTextContent());
	// }
	//
	// if (filenameToTags.containsKey(localFilename)) {
	// filenameToTags.get(localFilename).addAll(tags);
	// } else {
	// filenameToTags.put(localFilename, tags);
	// }
	// }
	//
	// if (defaultTags != null) {
	// VFSPathToken[] fileList = vfs.list(folderPath);
	// for (VFSPathToken path : fileList) {
	// VFSPath filePath = folderPath.child(path);
	// if (!vfs.isCollection(filePath)) {
	// String fullFilename = filePath.toString();
	// if (filenameToTags.containsKey(fullFilename)) {
	// filenameToTags.get(fullFilename).addAll(defaultTags);
	// } else {
	// filenameToTags.put(fullFilename, defaultTags);
	// }
	//
	// }
	//
	// }
	// }
	//
	// StringBuilder xml = new StringBuilder("<xml>\n");
	//
	// Iterator<Entry<String, ArrayList<String>>> iter =
	// filenameToTags.entrySet().iterator();
	// while (iter.hasNext()) {
	// Entry<String, ArrayList<String>> entry = iter.next();
	// xml.append("<file name=\"" + entry.getKey() + "\" >\n");
	// for (String tagname : entry.getValue()) {
	// xml.append("<tag>" + tagname + "</tag>\n");
	// }
	// xml.append("</file>\n");
	// }
	//
	// xml.append("</xml>");
	//
	// return xml.toString();
	//
	// } catch (VFSPathParseException impossible) {
	// return null;
	// } catch (IOException unlikely) {
	// return null;
	// }
	//
	// }

	/**
	 * Sets the headers of a request object
	 * 
	 * @param request
	 * @param sha1
	 * @param md5
	 * @param timestampModified
	 */
	private void setHeaders(Request request, String sha1, String md5, String timestampModified, String type) {
		RestletUtils.addHeaders(request, "sha1", sha1);
		RestletUtils.addHeaders(request, "md5", md5);
		RestletUtils.addHeaders(request, "lastModified", timestampModified);
		RestletUtils.addHeaders(request, "type", type);
	}

	private void setHeaders(Request request, String sha1, String md5, String timestampModified) {
		RestletUtils.addHeaders(request, "sha1", sha1);
		RestletUtils.addHeaders(request, "md5", md5);
		RestletUtils.addHeaders(request, "lastModified", timestampModified);
	}

	/**
	 * Get hashes given the content
	 * 
	 * @param content
	 * @param timestamp
	 * @return
	 */
	public String[] getHashesGivenContent(String content, String timestamp) {
		debug().println("creating hashes given content");
		debug().println(content);
		debug().println(timestamp);
		debug().println(settings.getGoGoKey());

		String sha1 = new SHA1Hash(settings.getGoGoKey() + timestamp + content).toString();
		String md5 = new MD5Hash(settings.getGoGoKey() + timestamp + content).toString();

		String[] returnValues = { md5, sha1 };
		return returnValues;
	}

	/**
	 * Gets the hash given the filename
	 * 
	 * @param filename
	 * @return
	 */
	public String[] getHashes(String filename) {
		try {
			VFSPath filePath = VFSUtils.parseVFSPath(filename);
			InputStream is = vfs.getInputStream(filePath);
			long length = vfs.getLength(filePath);
			debug().println("this is gogoKey {0}", settings.getGoGoKey());
			byte[] gogokeyByte = settings.getGoGoKey().getBytes("UTF-8");
			byte[] bytes = new byte[(int) length + gogokeyByte.length];
			for (int i = 0; i < gogokeyByte.length; i++)
				bytes[i] = gogokeyByte[i];

			if (is.read(bytes, gogokeyByte.length, (int) length) != -1) {
				String sha1 = new SHA1Hash(bytes).toString();
				String md5 = new MD5Hash(bytes).toString();

				debug().println("For filename {0} this is sha1 hash {1} and this is md5 {2}", filename, sha1, md5);
						
				String[] returnValues = { md5, sha1 };
				return returnValues;
			} else
				return new String[] { "", "" };
		} catch (VFSUtils.VFSPathParseException e) {
			String[] returnValues = { "", "" };
			return returnValues;
		} catch (IOException e) {
			String[] returnValues = { "", "" };
			return returnValues;
		}

	}

	/**
	 * Sends the application properties to the appengine site, needed on a
	 * complete refresh or if the settings have been changed.
	 * 
	 * @param context
	 * @param property
	 * @param value
	 * @return
	 */
	public Status sendApplicationPropertiesToAppEngine(String context, String property, String value) {
		return sendRequestToServer(Method.PUT, settings.getWebAddress() + "/updateApplicationProperties",
				"<xml><context>" + DocumentUtils.clean(context) + "</context>\r<property>"
						+ DocumentUtils.clean(property) + "</property>\r<value>" + DocumentUtils.clean(value)
						+ "</value>\r</xml>");
	}

	/**
	 * Sends a request to the server when all is needed is the method, the
	 * webaddress to send the request to, and the content of the request object.
	 * 
	 * @param method
	 * @param webaddress
	 * @param xml
	 * @return
	 */
	public Status sendRequestToServer(Method method, String webaddress, String xml) {

		// GET HASHES TO PLACE IN HEADER
		String timestamp = "" + new Date().getTime();
		String[] md5Andsha1 = getHashesGivenContent(xml, timestamp);

		// SEND REQUEST
		Status error = null;
		int maxTries = 5;
		int count = 0;

		while (count < maxTries) {

			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// DO NOTHING
			}

			debug().println(xml);
			Request newRequest = new Request(method, webaddress, new StringRepresentation(xml, MediaType.TEXT_XML));
			setHeaders(newRequest, md5Andsha1[1], md5Andsha1[0], timestamp);

			Client client = new Client(Protocol.HTTP);
			Response newResponse = client.handle(newRequest);

			if (newResponse.getStatus().isSuccess()) {
				error = null;
				break;
			} else {
				error = newResponse.getStatus();
				count++;
			}

		}

		return error;
	}

	/**
	 * For files that are too large to be stored on appengine, this will add a
	 * link to the large file where the base of the link is specified in the
	 * linkaddress variable.
	 * 
	 * @param filenamesToLarge
	 * @param filenameToTimestamp
	 * @return
	 */
	public ArrayList<String> addLinks(ArrayList<String> filenamesToLarge, HashMap<String, String> filenameToTimestamp) {
		Status error = null;
		ArrayList<String> filesSuccessfullyLinked = new ArrayList<String>();

		for (int i = 0; i < filenamesToLarge.size() && error == null; i++) {
			String filename = filenamesToLarge.get(i);

			try {

				VFSPath filePath = VFSUtils.parseVFSPath(filename);
				String oldTimestamp = filenameToTimestamp.get(filename);
				String newTimestamp = "" + vfs.getLastModified(filePath);

				if (!newTimestamp.equals(oldTimestamp)) {

					filenameToTimestamp.put(filename, newTimestamp);
					debug().println("Thie is filename {0} and this is newTimestamp {1}", filename, 
							filenameToTimestamp.get(filename));

					if (!filename.startsWith("/")) {
						filename = "/" + filename;
					}

					String link = settings.getLinkAddress() + filename;

					long timestamp = new Date().getTime();

					String xml = "<xml lastModified=\"" + newTimestamp + "\" " + "timestamp=\"" + timestamp
							+ "\" hash=\""
							+ new MD5Hash(settings.getGoGoKey() + filename + link + timestamp).toString()
							+ "\" ><![CDATA[" + link + "]]></xml>\r\n";

					debug().println("Hashed {0}", settings.getGoGoKey() + filename + link + timestamp);

					error = sendRequestToServer(Method.PUT, settings.getWebAddress() + "/addLink?filename="
							+ URLEncoder.encode(filename, "UTF-8"), xml);

				}
			} catch (UnsupportedEncodingException unlikely) {
				error = Status.CLIENT_ERROR_BAD_REQUEST;
			} catch (VFSPathParseException e) {
				error = Status.SERVER_ERROR_INTERNAL;
			} catch (VFSException e) {
				error = Status.SERVER_ERROR_INTERNAL;
			}

			if (error == null)
				filesSuccessfullyLinked.add(filename);

		}

		debug().println("this is filesSuccessfullyLinked {0}", filesSuccessfullyLinked.toString());
		return filesSuccessfullyLinked;
	}

	/**
	 * Deletes the files that are in fileNames, but are no longer on the
	 * filesystem
	 * 
	 * @param fileNames
	 * @return
	 * @throws VFSPathParseException
	 */
	public Status deleteFiles(List<VFSPath> filesToDelete, Map<VFSPath, List<String>> uriToParsedTags, Context context)
			throws VFSPathParseException {

		Status error = null;
		debug().println("This is filesToDelete {0}" + filesToDelete);

		int maxSend = 10; // SENDS IN AT MOST 10 FILES TO DELETE AT ONCE
		int count = 0;

		while (count < filesToDelete.size() && error == null) {
			long timestamp = new Date().getTime();
			StringBuilder builder = new StringBuilder("<xml timestamp=\"" + timestamp + "\">\r\n");

			for (int i = 0; i < maxSend && count < filesToDelete.size(); i++) {
				String filename = filesToDelete.get(count).toString();

				builder.append("<file filename=\"" + AppEngineExportData.translatePath(filename) + "\" >\r\n");
				List<String> oldTags = uriToParsedTags.get(filesToDelete.get(count));
				if (oldTags != null) {
					for (String tag : oldTags) {
						builder.append("<tag>" + tag + "</tag>\r\n");
					}
				}
				builder.append("<type>" + AppEngineExportData.determineCategory(filename, context) + "</type>\r\n");
				builder.append("</file>\r\n");

				count++;
			}
			builder.append("</xml>");
			error = sendRequestToServer(Method.POST, settings.getWebAddress() + "/delete", builder.toString());
		}

		return error;
	}

	/**
	 * Adds tags to all files in taggedFiles list.
	 * 
	 * @param taggedFiles
	 * @param filenameToTimestamp
	 * @param uploaded
	 * @return
	 */
	public Status addTags(VFSPath path, String category, List<String> tags, List<String> previousTags) {

		Status error = null;

		// GET XML FOR DOCUMENT TO SEND TO APPENGINE
		String xml = getTagDocument(path.toString(), category, tags, previousTags);

		try {
			error = sendRequestToServer(Method.PUT, settings.getTagAddress() + "?filename="
					+ URLEncoder.encode(ONE_TAG_FILE, "utf8"), xml);
		} catch (UnsupportedEncodingException e) {
			error = Status.SERVER_ERROR_INTERNAL;
		}

		return error;

	}

	private String getTagDocument(String filename, String category, List<String> tags, List<String> previousTags) {
		StringBuffer xml = new StringBuffer();

		xml.append("<xml>");
		xml.append("<file name=\"" + DocumentUtils.clean(filename) + "\" >\r\n");
		xml.append("<gogoType>" + category + "</gogoType>\r\n");
		if (tags != null) {
			for (String tag : tags) {
				xml.append("<tag><![CDATA[" + tag + "]]></tag>\r\n");
				if (previousTags != null && previousTags.contains(tag))
					previousTags.remove(tag);
			}
		}

		if (previousTags != null) {
			for (String previousTag : previousTags) {
				xml.append("<previousTag><![CDATA[" + previousTag + "]]></previousTag>\r\n");
			}
		}

		xml.append("</file>");
		xml.append("</xml>");
		return xml.toString();

	}

	/**
	 * Sends a request to appengine to do the method specified by the parameter
	 * method
	 */
	private Status doMethod(String method, String parameter) {
		Status error = null;

		long timestamp = new Date().getTime();
		StringBuilder xml = new StringBuilder("<xml timestamp=\"" + timestamp + "\" hash=\""
				+ new MD5Hash(settings.getGoGoKey() + method + timestamp).toString() + "\" method=\"" + method + "\"");

		if (parameter != null)
			xml.append(" parameter=\"" + parameter + "\"");

		xml.append(">\r\n </xml>");

		error = sendRequestToServer(Method.POST, settings.getWebAddress() + "/doMethod", xml.toString());

		return error;
	}

	/**
	 * Sends a request to appengine to do the method specified by the paramter
	 * method and must be a method that does not need a parameter
	 * 
	 * @param method
	 * @return
	 */
	private Status doMethod(String method) {

		return doMethod(method, null);

	}

	/**
	 * Adds tags to the files that are included in both the
	 * successfullyUploadedFiles and filenames arraylist. The tagged file must
	 * be in the taggedFiles arrayList.
	 * 
	 * @param taggedFiles
	 * @param successfullyUploadedFiles
	 * @param filenames
	 * @return
	 * @throws NotFoundException
	 * @throws VFSPathParseException
	 * @throws IOException
	 */
	public Status addTagsOnlyToSelectedFiles(ArrayList<String> taggedFiles,
			ArrayList<String> successfullyUploadedFiles, ArrayList<String> filenames) throws NotFoundException,
			VFSPathParseException, IOException {
		Status error = null;

		while (!filenames.isEmpty() && error == null) {
			int maxSend = 10;
			StringBuilder xml = new StringBuilder("<xml>");
			// ONLY DO SO MANY AT ONCE ... DON'T WANT TO OVERLOAD SYSTEM
			for (int num = 0; num < maxSend && !filenames.isEmpty(); num++) {
				String fullFilename = filenames.remove(0);

				if (successfullyUploadedFiles.contains(fullFilename)) {
					int lastIndex = fullFilename.lastIndexOf("/");
					String path = "";
					String filename = fullFilename;
					if (lastIndex != -1) {
						path = fullFilename.substring(0, lastIndex);
						filename = fullFilename.substring(lastIndex + 1);
					}

					String taggedFilename = path + "/.tags.xml";

					// THERE ARE POSSIBLE TAGS FOR THE FILE
					if (taggedFiles.contains(taggedFilename)) {
						Document taggedDoc = DocumentUtils.getReadOnlyDocument(VFSUtils.parseVFSPath(taggedFilename),
								vfs);
						NodeList taggedFileList = taggedDoc.getDocumentElement().getElementsByTagName("file");
						NodeList defaultTags = taggedDoc.getDocumentElement().getElementsByTagName("default");
						xml.append("<file name=\"" + DocumentUtils.clean(fullFilename) + "\" >\n");
						if (defaultTags.getLength() > 0) {
							xml.append("<tag><![CDATA[" + defaultTags.item(0).getTextContent() + "]]></tag>\n");
						}
						boolean cont = true;
						for (int i = 0; i < taggedFileList.getLength() && cont; i++) {
							Element fileElement = (Element) taggedFileList.item(i);
							if (filename.equals(fileElement.getAttribute("name"))) {
								cont = false;
								NodeList tags = fileElement.getElementsByTagName("tag");
								for (int j = 0; j < tags.getLength(); j++) {
									xml.append("<tag><![CDATA[" + tags.item(j).getTextContent() + "]]></tag>\n");
								}

							}
						}
						xml.append("</file>\n");

					}
					// THERE ARE NO TAGS FOR THE GIVEN FILE
					else {
						xml.append("<file name=\"" + DocumentUtils.clean(fullFilename) + "\" />\n");
					}
				}
			}

			String newTimestamp = "" + new Date().getTime();
			String[] hashes = getHashesGivenContent(xml.toString(), newTimestamp);

			int maxTries = 5;
			int count = 0;

			while (count < maxTries) {

				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// DO NOTHING
				}

				debug().println(xml);
				Request newRequest = new Request(Method.PUT, settings.getTagAddress() + "?filename="
						+ URLEncoder.encode(".GGESelective", "UTF-8"),
						new StringRepresentation(xml, MediaType.TEXT_XML));
				setHeaders(newRequest, hashes[1], hashes[0], newTimestamp);

				// Response newResponse =
				// getContext().getClientDispatcher().handle(newRequest);
				Client client = new Client(Protocol.HTTP);
				Response newResponse = client.handle(newRequest);

				if (newResponse.getStatus().isSuccess()) {
					error = null;
					break;
				} else {
					error = newResponse.getStatus();
					count++;
				}
			}

		}
		return error;
	}

	/**
	 * Saves the files in uploaded, but does not overwrite existing files,
	 * therefore it is not a complete upload, and the currentTimestamp should
	 * not matter
	 * 
	 * @param filenameToTimestamp
	 * @param uploaded
	 * @throws NotFoundException
	 * @throws IOException
	 */
	public void saveErrorOrAppendLastUpdated(HashMap<String, Long> filenameToTimestamp) throws IOException {
		saveLastUpdateFile(filenameToTimestamp, 0, false);
	}

	/**
	 * Allows the user to save on a successful last update
	 * 
	 * @param filenameToTimestamp
	 * @param timestamp
	 * @throws IOException
	 */
	public void saveSuccessfullLastUpdate(HashMap<String, Long> filenameToTimestamp, long timestamp) throws IOException {
		saveLastUpdateFile(filenameToTimestamp, timestamp, true);
	}

	/**
	 * Saves the filenameToTimestamp information in the LAST_UPLOADED file.
	 * 
	 * @param filenameToTimestamp
	 * @param currentTimestamp
	 * @param overwriteTime
	 * @throws IOException
	 */
	protected void saveLastUpdateFile(HashMap<String, Long> filenameToTimestamp, long currentTimestamp,
			boolean overwriteTime) throws IOException {

		VFSPath lastUploadedPath = VFSUtils.parseVFSPath(LAST_UPLOADED);
		StringBuilder xml = new StringBuilder();
		boolean exists = vfs.exists(lastUploadedPath);

		if (!overwriteTime && exists) {
			String oldFile = DocumentUtils.getVFSFileAsStringSafe(lastUploadedPath, vfs);
			xml.append(oldFile.substring(0, oldFile.lastIndexOf("</updatedFiles>")));
		} else {
			xml.append("<updatedFiles>\r\n");
			xml.append("<lastUpdated>" + currentTimestamp + "</lastUpdated>\r\n");
		}

		for (Entry<String, Long> entry : filenameToTimestamp.entrySet()) {
			String filename = DocumentUtils.clean(entry.getKey());
			xml.append("<file filename=\"" + filename + "\" lastModified=\"" + entry.getValue().toString()
					+ "\" /> \r\n");
		}

		xml.append("</updatedFiles>\r\n");
		DocumentUtils.writeVFSFile(LAST_UPLOADED, vfs, DocumentUtils.impl.createDocumentFromString(xml.toString()));

	}

	/**
	 * Gets the tags on a specific file, does not take into account time, just
	 * returns the tags for that specific file.
	 * 
	 * @param filename
	 * @param context
	 * @param vfs
	 * @return
	 * @throws ExportException
	 */
	public HashMap<String, ArrayList<String>> getTagsOnFile(String filename, Context context, VFS vfs)
			throws ExportException {
		HashMap<String, ArrayList<String>> uriToTags = new HashMap<String, ArrayList<String>>();
		TagApplicationDataUtility utility;
		try {
			utility = new TagApplicationDataUtility(context);
		} catch (InstantiationException e) {
			// TAGS AREN'T INSTALLED, RETURN EMPTY HASHMAP
			return uriToTags;
		}

		VFSPath path;
		try {
			path = VFSUtils.parseVFSPath(filename);
		} catch (VFSPathParseException e) {
			throw new ExportException();
		}

		Document tagDocument = utility.getTagsForFile(path, vfs);
		parseTagDocument(tagDocument, uriToTags);
		return uriToTags;
	}

	/**
	 * Returns the tags on those files since the last time there was an update,
	 * if there is no lastUpdated file, then it returns all tags.
	 * 
	 * @param context
	 * @param vfs
	 * @return
	 */
	public HashMap<String, ArrayList<String>> getTagsSinceTimestamp(long timestamp, Context context, VFS vfs) {

		HashMap<String, ArrayList<String>> uriToTags = new HashMap<String, ArrayList<String>>();
		TagApplicationDataUtility utility;
		try {
			utility = new TagApplicationDataUtility(context);
		} catch (InstantiationException e) {
			// TAGS NOT INSTALLED, RETURN EMPTY HASHMAP
			debug().println("Threw new instantation error");
			return uriToTags;
		}

		VFSPath root;
		try {
			root = VFSUtils.parseVFSPath("/");
		} catch (VFSPathParseException impossible) {
			return uriToTags;
		}

		Document tagsDoc = utility.getTagsForDirectory(root, vfs, Long.valueOf(timestamp), true);
		parseTagDocument(tagsDoc, uriToTags);

		return uriToTags;
	}

	/**
	 * Parses the document returned by the TagApplicationDataUtility which holds
	 * the file names and the tags associated with each tag.
	 * 
	 * @param tagsDoc
	 * @param uriToTags
	 */
	protected void parseTagDocument(Document tagsDoc, HashMap<String, ArrayList<String>> uriToTags) {
		Element docElement = tagsDoc.getDocumentElement();
		NodeList files = docElement.getElementsByTagName("file");
		for (int i = 0; i < files.getLength(); i++) {
			Element file = (Element) files.item(i);
			String uri = file.getAttribute("uri");
			NodeList tags = file.getElementsByTagName("tag");
			ArrayList<String> tagList = new ArrayList<String>();
			for (int j = 0; j < tags.getLength(); j++) {
				tagList.add(tags.item(i).getTextContent());
			}
			uriToTags.put(uri, tagList);
		}
	}

	/**
	 * Starts a new thread which flushes the entire cache
	 */
	public void flushCache() {
		new Thread(new CacheFlusher(this.settings.getSiteAddress())).start();
	}

	public AppEngineBaseExportFactory getAppengineParser(Context context) {
		return new AppEngineBaseExportFactory(vfs, context);

	}

	/**
	 * Class that will flush all search results and the cache
	 * 
	 * @author liz.schwartz
	 * 
	 */
	private class CacheFlusher implements Runnable {

		private String siteAddress;

		public CacheFlusher(String siteAddress) {
			this.siteAddress = siteAddress;
		}

		public void run() {
			doMethod("flushSearchResults");
			doMethod("flushCache");

			SiteWalker siteWalker = new SiteWalker(siteAddress, 3600);
			siteWalker.walk(new Reference(siteAddress));
			while (siteWalker.processRetries()) {
				// NOTHING TO DO
			}
		}
	}

}
