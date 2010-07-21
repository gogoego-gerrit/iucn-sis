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
import java.util.List;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

import com.solertium.gogoego.server.lib.app.exporter.workers.base.BaseExportParser;
import com.solertium.gogoego.server.lib.app.exporter.workers.base.BaseExportParserFactory;
import com.solertium.gogoego.server.lib.app.exporter.workers.base.ExportData;
import com.solertium.gogoego.server.lib.app.tags.utils.TagApplicationDataUtility;
import com.solertium.gogoego.server.lib.templates.TemplateRegistry;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.MediaTypeManager;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

/**
 * AppEngineBaseExportFactory.java
 * 
 * 
 * 
 * @author liz.schwartz
 */
public class AppEngineBaseExportFactory extends BaseExportParserFactory {

	private static class AppengineBaseParser extends BaseExportParser {

		private final VFS vfs;
		private final Context context;

		public AppengineBaseParser(VFS vfs, Context context) {
			this.vfs = vfs;
			this.context = context;
		}

		public AppEngineExportData getExportData(VFSPath path) {
			AppEngineExportData exportData = null;

			if (vfs.exists(path)) {
				try {
					exportData = new AppEngineExportData(path, context, vfs);
				} catch (InstantiationError e) {
					TrivialExceptionHandler.ignore(this, e);
				}

			}
			return exportData;
		}

	}

	private static class BlankBaseParser extends BaseExportParser {

		public ExportData getExportData(VFSPath path) {
			return null;
		}
	}

	public static class AppEngineExportData implements ExportData {

		public final static String TEMPLATE = "template";
		public final static String COLLECTION = "collection";
		public final static String FILE = "regular";
		public final String[] NOT_ALLOWED_FILE_ENDINGS = { ".part", "~", ".tar", ".tgz", ".zip" };

		protected String translatedURL;
		protected long lastModified;
		protected long size;
		protected String category;
		protected VFSPath originalURLPath;
		protected MediaType mediaType;
		protected Representation representation;
		protected final Context context;
		protected final VFS vfs;

		public AppEngineExportData(VFSPath path, Context context, VFS vfs) {
			this.originalURLPath = path;

			boolean allowed = true;
			for (String end : NOT_ALLOWED_FILE_ENDINGS) {
				if (originalURLPath.toString().endsWith(end)) {
					allowed = false;
				}
			}

			if (!allowed)
				throw new InstantiationError();

			try {
				size = vfs.getLength(originalURLPath);
				lastModified = vfs.getLastModified(originalURLPath);
			} catch (NotFoundException e) {
				TrivialExceptionHandler.ignore(this, e);
			}

			mediaType = MediaTypeManager.getMediaType(originalURLPath.toString());

			// DETERMINE CATEGORY AND TRANSLATED URL
			if (originalURLPath.toString().startsWith("/(SYSTEM)/collections/")) {
				translatedURL = originalURLPath.toString().substring("/(SYSTEM)".length());
				category = COLLECTION;

				if (originalURLPath.toString().endsWith(".xml"))
					translatedURL = translatedURL.substring(0, translatedURL.length() - ".xml".length());
			} else {
				translatedURL = originalURLPath.toString();
				if (TemplateRegistry.getFromContext(context).isRegistered(originalURLPath.toString())) {
					category = TEMPLATE;
				} else {
					category = FILE;
				}
			}

			this.context = context;
			this.vfs = vfs;

		}

		public static String translatePath(String path) {
			if (path.startsWith("/(SYSTEM)/collections/")) {
				path = path.substring("/(SYSTEM)".length());
				if (path.endsWith(".xml"))
					path = path.substring(0, path.length() - ".xml".length());
			}

			return path;
		}

		public static String determineCategory(String path, Context context) {
			if (TemplateRegistry.getFromContext(context).isRegistered(path))
				return TEMPLATE;
			else if (translatePath(path).equals(path))
				return FILE;
			else
				return COLLECTION;
		}

		public String getOriginalPath() {
			return originalURLPath.toString();
		}

		public Long getLastModified() {
			return Long.valueOf(lastModified);
		}

		public long getSize() {
			return size;
		}

		public String getCategory() {
			return category;
		}

		public MediaType getMediaType() {
			return mediaType;
		}

		public Representation getRepresentation() {
			try {
				InputStream stream = vfs.getInputStream(originalURLPath);
				representation = new InputRepresentation(stream, mediaType);
				representation.setSize(size);
			} catch (IOException e) {
				e.getCause().printStackTrace();
			}

			return representation;

		}

		public String getPath() {
			return translatedURL;
		}

		public List<String> getTags() {

			if (!(category.equalsIgnoreCase(COLLECTION) && originalURLPath.toString().endsWith("/index.xml"))) {
				List<String> tags;
				try {
					TagApplicationDataUtility utility = new TagApplicationDataUtility(context);

					try {
						tags = utility.getTagListForFile(VFSUtils.parseVFSPath(this.translatedURL), vfs);
					} catch (VFSPathParseException e) {
						e.printStackTrace();
						return null;
					}
					if (tags.isEmpty())
						return null;
					else
						return tags;

				} catch (InstantiationException e) {
					return null;
				}
			} else
				return null;

		}

	}

	public AppEngineBaseExportFactory(VFS vfs, Context context) {
		super(vfs);
		BlankBaseParser blankParser = new BlankBaseParser();
		AppengineBaseParser parser = new AppengineBaseParser(vfs, context);

		// ADD EXPORTDATA PARSER
		setDefaultParser(parser);
		for (String url : AppEngineExportUtils.filesToSpecificallyFetch) {
			addParser(url, parser);
		}

		// ADD IGNORE PARSER
		addParser(AppEngineExportUtils.directoryToIgnore, blankParser);
		addParser(AppEngineExportUtils.tempDirectory, blankParser);
		addParser(AppEngineExportUtils.trashDirectory, blankParser);

	}

	public static class AppEngineDataParserException extends Exception {

		private static final long serialVersionUID = 1L;

		public AppEngineDataParserException() {
			super();
		}

		public AppEngineDataParserException(String message) {
			super(message);
		}

	}

}
