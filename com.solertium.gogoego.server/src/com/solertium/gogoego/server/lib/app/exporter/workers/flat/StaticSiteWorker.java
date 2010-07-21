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
package com.solertium.gogoego.server.lib.app.exporter.workers.flat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.representations.GoGoEgoInputRepresentation;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.exporter.utils.ExportException;
import com.solertium.gogoego.server.lib.app.exporter.utils.SimpleExporterSettings;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorker;
import com.solertium.gogoego.server.lib.app.exporter.workers.base.BaseExportParser;
import com.solertium.gogoego.server.lib.app.exporter.workers.base.BaseExportParserFactory;
import com.solertium.gogoego.server.lib.app.exporter.workers.base.ExportData;
import com.solertium.gogoego.server.lib.app.exporter.workers.gge.StaticExportDataParser;
import com.solertium.util.BaseTagListener;
import com.solertium.util.NodeCollection;
import com.solertium.util.TagFilter;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.TagFilter.Tag;
import com.solertium.util.restlet.CookieUtility;
import com.solertium.util.restlet.MediaTypeManager;
import com.solertium.util.restlet.ScratchResource;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;

/**
 * StaticSiteWorker.java
 * 
 * @author carl.scott
 * 
 */
public class StaticSiteWorker extends ExporterWorker {

	public StaticSiteWorker(VFS vfs) {
		super(vfs);
	}

	public void init(final VFSPath homeFolder, final SimpleExporterSettings configuration) throws GoGoEgoApplicationException {
		
	}

	public Representation doCommand(Document document, Context context, String command) throws ExportException {
		if ("download".equals(command)) {
			final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

			Representation representation = null;
			for (Node node : nodes) {
				if (node.getNodeName().equals("download")) {
					try {
						ScratchResource resource = ServerApplication.getFromContext(context).getScratchResourceBin()
								.get("/apps/exporter/downloads/" + node.getTextContent());
						if (resource == null)
							throw new ExportException(Status.CLIENT_ERROR_GONE);

						File file = new File((String) resource.getResource());
						String dlName = file.getName();
						representation = new GoGoEgoInputRepresentation(
							new FileInputStream(file), MediaType.APPLICATION_ZIP
						);
						representation.setDownloadable(true);
						representation.setDownloadName(dlName);
					} catch (IOException e) {
						throw new ExportException(e);
					}
					break;
				}
			}
			return representation;
		} else
			throw new ExportException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "Command not supported: " + command);
	}

	/*
	 * First, all static VFS files must be converted. We don't need anything in
	 * the (SYSTEM) folder but everything else is eligible. Then, each of those
	 * exported files needs to be examined and all local links need to be
	 * resolved if they have not been already, collections in particular.
	 */
	public Document exportAll(Document document, Context context) throws ExportException {
		final File zipFile;
		final ZipOutputStream out;
		try {
			final Calendar date = Calendar.getInstance();
			out = new ZipOutputStream(new FileOutputStream(zipFile = File.createTempFile("site-"
					+ new SimpleDateFormat("yyyy-MM-dd").format(date.getTime()), ".zip")));
		} catch (IOException e) {
			throw new ExportException(e);
		}

		final BaseExportParser ignoreParser = new BaseExportParser() {
			public ExportData getExportData(VFSPath path) {
				return null;
			}
		};

		final BaseExportParserFactory factory = new BaseExportParserFactory(context, vfs);
		factory.setDefaultParser(new StaticExportDataParser(vfs, context));
		factory.addParser("/(SYSTEM)", ignoreParser);
		factory.addParser("/tmp", ignoreParser);

		final HashMap<VFSPath, Representation> extraFiles = new HashMap<VFSPath, Representation>();

		final Map<VFSPath, ExportData> results = factory.getExportData();
		final Set<VFSPath> localSet = results.keySet();
		final Iterator<Map.Entry<VFSPath, ExportData>> iterator = results.entrySet().iterator();
		while (iterator.hasNext()) {
			final Map.Entry<VFSPath, ExportData> current = iterator.next();
			final Representation rep;

			if (current.getValue().getMediaType().equals(MediaType.TEXT_HTML))
				rep = inspect(current.getKey(), current.getValue().getRepresentation(), context, extraFiles, localSet);
			else
				rep = current.getValue().getRepresentation();

			try {
				addToZip(out, current.getKey().toString(), rep);
			} catch (IOException e) {
				continue;
			}
		}

		/*
		 * Now we have to write representations for the generated entities
		 * located via inspection.
		 */
		final Iterator<Map.Entry<VFSPath, Representation>> genIterator = extraFiles.entrySet().iterator();
		while (genIterator.hasNext()) {
			final Map.Entry<VFSPath, Representation> current = genIterator.next();
			final Representation rep = current.getValue();

			try {
				addToZip(out, current.getKey().toString(), rep);
			} catch (IOException e) {
				continue;
			}
		}

		try {
			out.close();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		final Calendar expiration = Calendar.getInstance();
		expiration.add(Calendar.DATE, 1);

		final String downloadKey = CookieUtility.newUniqueID();

		ServerApplication.getFromContext(context).getScratchResourceBin().add(
				new ScratchResource(new Reference("/apps/exporter/downloads/" + downloadKey), "public", expiration
						.getTime(), zipFile.getAbsolutePath()));

		Document responseDoc = DocumentUtils.impl.newDocument();
		Element root = responseDoc.createElement("root");
		root.appendChild(DocumentUtils.impl.createElementWithText(responseDoc, "download", downloadKey));
		responseDoc.appendChild(root);

		return responseDoc;
	}

	/**
	 * Exports a single file (or folder). It does not attempt to resolve the
	 * uris via inspect function.
	 */
	public Document exportFile(Document document, Context context) throws ExportException {
		final File zipFile;
		final ZipOutputStream out;
		try {
			final Calendar date = Calendar.getInstance();
			out = new ZipOutputStream(new FileOutputStream(zipFile = File.createTempFile("files-"
					+ new SimpleDateFormat("yyyy-MM-dd").format(date.getTime()), ".zip")));
		} catch (IOException e) {
			throw new ExportException(e);
		}

		final BaseExportParser ignoreParser = new BaseExportParser() {
			public ExportData getExportData(VFSPath path) {
				return null;
			}
		};

		final BaseExportParserFactory factory = new BaseExportParserFactory(context, vfs);
		factory.setDefaultParser(new StaticExportDataParser(vfs, context));
		factory.addParser("/(SYSTEM)", ignoreParser);
		factory.addParser("/tmp", ignoreParser);

		final ArrayList<VFSPath> paths = new ArrayList<VFSPath>() {
			private static final long serialVersionUID = 1L;

			public boolean add(VFSPath e) {
				return !contains(e) && super.add(e);
			}
		};
		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

		for (Node node : nodes) {
			if (node.getNodeName().equals("file") || node.getNodeName().equals("folder")) {
				final VFSPath path;
				try {
					path = VFSUtils.parseVFSPath(node.getTextContent());
				} catch (VFSUtils.VFSPathParseException e) {
					continue;
				}

				boolean isCollection = false;
				try {
					isCollection = vfs.isCollection(path);
				} catch (NotFoundException e) {
					throw new ExportException(Status.CLIENT_ERROR_BAD_REQUEST, "Specified path " + path
							+ " is not available.");
				}

				if (!isCollection)
					paths.add(path);
				else {
					final VFSPathToken[] list;
					try {
						list = vfs.list(path);
					} catch (IOException impossible) {
						throw new ExportException(impossible);
					}

					for (VFSPathToken token : list)
						paths.add(path.child(token));
				}
			}
		}

		for (VFSPath path : paths) {
			final Map<VFSPath, ExportData> results = factory.getExportData(path, 0);
			final Iterator<Map.Entry<VFSPath, ExportData>> iterator = results.entrySet().iterator();
			while (iterator.hasNext()) {
				final Map.Entry<VFSPath, ExportData> current = iterator.next();
				final Representation rep = current.getValue().getRepresentation();

				try {
					addToZip(out, current.getKey().toString(), rep);
				} catch (IOException e) {
					continue;
				}
			}
		}

		try {
			out.close();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		final Calendar expiration = Calendar.getInstance();
		expiration.add(Calendar.DATE, 1);

		final String downloadKey = CookieUtility.newUniqueID();

		ServerApplication.getFromContext(context).getScratchResourceBin().add(
				new ScratchResource(new Reference("/apps/exporter/downloads/" + downloadKey), "public", expiration
						.getTime(), zipFile.getAbsolutePath()));

		Document responseDoc = DocumentUtils.impl.newDocument();
		Element root = responseDoc.createElement("root");
		root.appendChild(DocumentUtils.impl.createElementWithText(responseDoc, "download", downloadKey));
		responseDoc.appendChild(root);

		return responseDoc;
	}

	private void addToZip(ZipOutputStream out, String fileName, Representation rep) throws IOException {
		byte[] buff = new byte[18024];

		final InputStream in = rep.getStream();
		final ZipEntry entry = new ZipEntry(fileName);
		try {
			entry.setTime(rep.getModificationDate().getTime());
			entry.setSize(rep.getSize());
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		out.putNextEntry(entry);
		int len;
		while ((len = in.read(buff)) > 0)
			out.write(buff, 0, len);
		out.closeEntry();
		in.close();
	}

	private Representation inspect(final VFSPath currentPath, final Representation representation,
			final Context context, final HashMap<VFSPath, Representation> extraFiles, final Set<VFSPath> localSet) {
		final Reader reader;
		try {
			reader = new StringReader(representation.getText());
		} catch (IOException e) {
			return representation;
		}
		final StringWriter out = new StringWriter();
		final TagFilter tf = new TagFilter(reader, out);
		tf.shortCircuitClosingTags = false;
		tf.registerListener(new BaseTagListener() {
			public List<String> interestingTagNames() {
				final List<String> list = new ArrayList<String>();
				list.add("a");
				list.add("img");
				list.add("link");
				list.add("script");
				return list;
			}

			public void process(Tag t) throws IOException {
				if (t.name.equals("a")) {
					final String href = t.getAttribute("href");
					if (href != null && href.startsWith("/")) {
						// Local link, we need to resolve it.
						try {
							int index = href.indexOf("?");
							final Reference ref = new Reference(href);
							final VFSPath vfsPath = VFSUtils
									.parseVFSPath(index == -1 ? href : href.substring(0, index));
							if (!extraFiles.containsKey(vfsPath) && !localSet.contains(vfsPath)) {
								final Request request = new Request(Method.GET, "riap://host" + ref, null);
								final Response response = context.getClientDispatcher().handle(request);
								if (response.getStatus().isSuccess()) {
									if (MediaType.TEXT_HTML.equals(MediaTypeManager.getMediaType(vfsPath.getName()))) {
										extraFiles.put(vfsPath, null);
										Representation rep = inspect(vfsPath, response.getEntity(), context,
												extraFiles, localSet);
										extraFiles.put(vfsPath, rep);
									}
									else
										extraFiles.put(vfsPath, response.getEntity());
								}
							}
						} catch (VFSUtils.VFSPathParseException e) {
							TrivialExceptionHandler.ignore(this, e);
						}
					}
				}
				String attr;
				if (t.name.equals("a") || t.name.equals("link"))
					attr = "href";
				else
					attr = "src";

				if (t.attr.containsKey(attr)) {
					String url = t.getAttribute(attr);
					if (url != null && !url.startsWith("mailto")) {
						if (url.startsWith("/"))
							url = url.substring(1);

						String prepend = "";
						if (!currentPath.getCollection().equals(VFSPath.ROOT)) {
							String[] split = currentPath.getCollection().toString().substring(1).split("/");
							for (int i = 0; i < split.length; i++)
								prepend += "../";
						}

						t.setAttribute(attr, prepend + url);
						t.rewrite();
					}
				}
			}
		});
		try {
			tf.parse();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
			return representation;
		}

		return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorker
	 * #refresh(org.w3c.dom.Document, com.solertium.vfs.VFS,
	 * org.restlet.Context)
	 */
	@Override
	public Document refresh(Document document, Context context) throws ExportException {
		return exportAll(document, context);
	}

	public static class StaticExportConfig {
		private static final long serialVersionUID = 1L;
		
		private final SimpleExporterSettings settings;
		
		public StaticExportConfig(SimpleExporterSettings settings) {
			this.settings = settings;
		}

		public String getLocation() {
			return settings.get("location");
		}
	}

}
