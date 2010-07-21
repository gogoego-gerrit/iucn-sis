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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * SiteBuilder.java
 * 
 * Utility class that allows for creating of website in a given space, providing
 * a siteID and its resource domain regex, and also a default start page.
 * 
 * @author carl.scott
 * 
 */
public class SiteBuilder {

	private String hostRoot;
	private String siteID;
	private String matches;
	private String httpsHost;
	private String repositoryURI;
	private VFS vfs;
	private String errorMessage;

	public boolean killSite() {
		Document doc;
		final File spec;
		try {
			spec = new File(hostRoot + "/hosts.xml");
			final BufferedReader reader = new BufferedReader(new FileReader(spec));
			String line = "";
			String xml = "";
			while ((line = reader.readLine()) != null)
				xml += line;
			reader.close();
			doc = DocumentUtils.impl.createDocumentFromString(xml);
		} catch (IOException e) {
			return false;
		}

		if (doc == null)
			return false;

		NodeList nodes = doc.getElementsByTagName("host");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node current = nodes.item(i);
			if (current.getNodeName().equals("host") && DocumentUtils.impl.getAttribute(current, "id").equals(siteID)) {
				current.getParentNode().removeChild(current);
			}
		}

		return writeHostFile(doc, spec);
	}

	public static Document getHostDocument(final File spec) {
		Document doc = null;
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(spec));
			String line = "";
			String xml = "";
			while ((line = reader.readLine()) != null)
				xml += line;
			reader.close();
			doc = DocumentUtils.impl.createDocumentFromString(xml);
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(spec, e);
		}
		return doc;
	}

	public boolean updateSite() {
		final File spec = new File(hostRoot + "/hosts.xml");
		Document doc = getHostDocument(spec);
		if (doc == null)
			return false;

		Element host = doc.createElement("host");
		if (httpsHost != null)
			host.setAttribute("httpsHost", httpsHost);
		host.setAttribute("id", siteID);
		host.setAttribute("name", siteID);
		host.setAttribute("match", matches);

		return updateSiteIfPossible(doc, host) && writeHostFile(doc, spec);
	}

	public boolean createNewSite() {
		final File spec = new File(hostRoot + "/hosts.xml");
		Document doc = getHostDocument(spec);
		if (doc == null)
			return false;

		Element host = doc.createElement("host");
		if (httpsHost != null)
			host.setAttribute("httpsHost", httpsHost);
		host.setAttribute("id", siteID);
		host.setAttribute("name", siteID);
		host.setAttribute("match", matches);

		return addSiteIfPossible(doc, host) && buildSite() && writeHostFile(doc, spec);
	}

	private boolean addSiteIfPossible(Document doc, Element site) {
		NodeList nodes = doc.getElementsByTagName("host");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node current = nodes.item(i);
			if (current.getNodeName().equals("host") && DocumentUtils.impl.getAttribute(current, "id").equals(siteID)) {
				setErrorMessage("Site " + siteID + " already exists");
				return false;
			}
		}
		doc.getDocumentElement().appendChild(site);
		return true;
	}

	private boolean updateSiteIfPossible(Document doc, Element site) {
		boolean found = false;
		NodeList nodes = doc.getElementsByTagName("host");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node current = nodes.item(i);
			if (current.getNodeName().equals("host") && DocumentUtils.impl.getAttribute(current, "id").equals(siteID)) {
				NamedNodeMap attrs = site.getAttributes();
				for (int k = 0; k < attrs.getLength(); k++) {
					Node curAttr = attrs.item(k);
					((Element) current).setAttribute(curAttr.getNodeName(), curAttr.getNodeValue());
				}
				found = true;
				break;
			}
		}
		return found;
	}

	private boolean writeHostFile(Document doc, File spec) {
		try {
			final TransformerFactory tfac = TransformerFactory.newInstance();
			tfac.setAttribute("indent-number", "3");

			final Transformer t = tfac.newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			t.transform(new DOMSource(doc), new StreamResult(new BufferedWriter(new FileWriter(spec))));
			return true;
		} catch (final Exception e) {
			setErrorMessage("Could not write file " + e.getMessage());
			return false;
		}
	}

	public static boolean writeStaticHostFile(Document doc, File spec) {
		try {
			final TransformerFactory tfac = TransformerFactory.newInstance();
			tfac.setAttribute("indent-number", "3");

			final Transformer t = tfac.newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			t.transform(new DOMSource(doc), new StreamResult(new BufferedWriter(new FileWriter(spec))));
			return true;
		} catch (final TransformerException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	private boolean buildSite() {
		String url = hostRoot + "/" + siteID + "/vfs";
		final File spec = new File(url);
		boolean dirsMade;
		try {
			dirsMade = spec.mkdirs();
		} catch (SecurityException e) {
			return false;
		}
		if (!dirsMade) // Either site exists or didnt make dirs
			return false;

		String[] reqDirs = new String[] { "HEAD", "META", "UNDO" };
		for (int i = 0; i < reqDirs.length && dirsMade; i++) {
			try {
				dirsMade = new File(url + "/" + reqDirs[i]).mkdir();
			} catch (SecurityException e) {
				return false;
			}
		}

		if (!dirsMade)
			return false;

		vfs = null;
		try {
			vfs = VFSFactory.getVersionedVFS(spec);
		} catch (final NotFoundException nf) {
			setErrorMessage("VFS " + spec.getPath() + " could not be opened.");
		}
		
		final VFSPathToken[] defaultDirectories = new VFSPathToken[] {
			new VFSPathToken("templates"), new VFSPathToken("includes"), 
			new VFSPathToken("images"), new VFSPathToken("css") 
		};
		
		for (VFSPathToken directory : defaultDirectories) {
			try {
				vfs.makeCollections(VFSPath.ROOT.child(directory));
			} catch (IOException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}

		return DocumentUtils.writeVFSFile("/index.html", vfs, DocumentUtils.impl
				.createDocumentFromString("<html><head><title>" + siteID + " Index Page</title></head>"
						+ "<body><h1>Hello, World!</h1>" + "<p>Welcome to your new GoGoEgo Site!</p>"
						+ "<p><b>Site ID:</b> " + siteID + "</p>" + "<p><b>Regex Match:</b> " + matches + "</p>"
						+ "<p><b>HTTPS Host:</b> " + (httpsHost == null ? "None" : httpsHost) + "</p>"
						+ (repositoryURI == null ? "" : "<p><b>Repository URI:</b> " + repositoryURI + "</p>")
						+ "<h2>Admin Tool</h2>" + "<p>To begin editing your site, please login to the "
						+ "<a href=\"/admin/index.html\"> admin tool.</a></p>" + "</body></html>"));
	}

	public VFS getVFS() {
		return vfs;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	private void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setHttpsHost(String httpsHost) {
		this.httpsHost = httpsHost;
	}

	public void setMatches(String matches) {
		this.matches = matches;
	}

	public void setRepositoryURI(String repositoryURI) {
		this.repositoryURI = repositoryURI;
	}

	public void setSiteID(String siteID) {
		this.siteID = siteID;
	}

	public void setHostRoot(String hostRoot) {
		this.hostRoot = hostRoot;
	}

}
