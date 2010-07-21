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
package com.solertium.gogoego.server.lib.app.exporter.workers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.restlet.Context;
import org.restlet.representation.Representation;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.lib.app.exporter.utils.ExportException;
import com.solertium.gogoego.server.lib.app.exporter.utils.SimpleExporterSettings;
import com.solertium.gogoego.server.lib.app.tags.utils.TagApplicationDataUtility;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * ExporterWorker.java
 * 
 * Does the actual exporting of files and folders.
 * 
 * @author carl.scott
 * 
 */
public abstract class ExporterWorker {
	
	protected final VFS vfs;
	
	public ExporterWorker(VFS vfs) {
		this.vfs = vfs;
	}

	public abstract void init(final VFSPath homeFolder, 
			final SimpleExporterSettings configuration) throws GoGoEgoApplicationException;

	/**
	 * Export the entire file system (synchronize)
	 * 
	 * @param document
	 *            the instruction document
	 * @param vfs
	 * @return document with export results
	 * @throws ExportException
	 */
	public abstract Document exportAll(Document document, Context context) throws ExportException;

	/**
	 * Export a single file on the file system
	 * 
	 * @param document
	 *            the instruction document
	 * @param vfs
	 * @return document with export result
	 * @throws ExportException
	 */
	public abstract Document exportFile(Document document, Context context) throws ExportException;

	/**
	 * Do a full refresh (full export as opposed to a synchronized export)
	 * 
	 * @param document
	 *            the instruction document
	 * @param vfs
	 * @return document with export results
	 * @throws ExportException
	 */
	public abstract Document refresh(Document document, Context context) throws ExportException;

	/**
	 * Perform an ExporterWorker-specific arbitrary command
	 * 
	 * @param document
	 *            the instruction document
	 * @param vfs
	 * @param command
	 *            the command
	 * @return document with results
	 * @throws ExportException
	 */
	public abstract Representation doCommand(Document document, Context context, String command) throws ExportException;

	public Document getTagsForFile(VFSPath uri, Context context) {
		TagApplicationDataUtility utility = getTagUtility(context);
		return (utility == null) ? null : utility.getTagsForFile(uri, vfs);
	}

	public Document getTagsForDirectory(VFSPath directory, Context context, boolean recursive) {
		return getTagsForDirectory(directory, 0, context, recursive);
	}

	public Document getTagsForDirectory(VFSPath directory, long date, Context context, boolean recursive) {
		TagApplicationDataUtility utility = getTagUtility(context);
		return (utility == null) ? null : utility.getTagsForDirectory(directory, vfs, date, recursive);
	}

	public TagApplicationDataUtility getTagUtility(Context context) {
		try {
			return new TagApplicationDataUtility(context);
		} catch (InstantiationException e) {
			return null;
		}
	}

	/**
	 * Should we ever need this...
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unused")
	private TagApplicationDataUtility getTagUtilityViaReflection(Context context) {
		final Class<?> c;
		try {
			c = Class.forName("com.solertium.gogoego.server.lib.app.tags.utils.TagApplicationDataUtility");
		} catch (ClassNotFoundException e) {
			return null;
		}

		final Constructor<?> constructor;
		try {
			constructor = c.getConstructor(new Class[] { Context.class });
		} catch (NoSuchMethodException e) {
			return null;
		}

		final TagApplicationDataUtility utility;
		try {
			utility = (TagApplicationDataUtility) constructor.newInstance(new Object[] { context });
		} catch (IllegalAccessException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		} catch (InstantiationException e) {
			return null;
		}

		return utility;
	}

}
