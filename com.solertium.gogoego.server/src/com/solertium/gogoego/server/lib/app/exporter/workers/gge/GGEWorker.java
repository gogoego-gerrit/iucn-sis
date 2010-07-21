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
package com.solertium.gogoego.server.lib.app.exporter.workers.gge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.gogoego.api.applications.GoGoEgoApplicationException;
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
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.lib.app.exporter.utils.ExportException;
import com.solertium.gogoego.server.lib.app.exporter.utils.SimpleExporterSettings;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorker;
import com.solertium.gogoego.server.lib.app.exporter.workers.base.BaseExportParserFactory;
import com.solertium.gogoego.server.lib.app.exporter.workers.base.ExportData;
import com.solertium.util.MD5Hash;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.utils.VFSUtils;

/**
 * GGEWorker.java
 * 
 * Exports to the Importer Application
 * 
 * @author carl.scott
 * 
 */
public class GGEWorker extends ExporterWorker {

	private GGEExportConfig settings;

	public GGEWorker(VFS vfs) {
		super(vfs);
	}

	public void init(VFSPath homeFolder, SimpleExporterSettings configuration) throws GoGoEgoApplicationException {
		settings = new GGEExportConfig(configuration);
	}

	public Representation doCommand(Document document, Context context, String command) throws ExportException {
		throw new ExportException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "Unsupported operation " + command);
	}

	public Document exportAll(Document document, Context context) throws ExportException {
		final GGEPastExportData pastData = new GGEPastExportData(vfs);

		final BaseExportParserFactory factory = new BaseExportParserFactory(context, vfs);
		performDelete(factory.findDeletedResorces(pastData.getResourcePaths()), pastData);

		return performExport(factory.getExportData(Long.valueOf(pastData.getLastExportDate().getTime())).values(),
				pastData);
	}

	public Document exportFile(Document document, Context context) throws ExportException {
		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

		final GGEPastExportData pastData = new GGEPastExportData(vfs);

		final BaseExportParserFactory factory = new BaseExportParserFactory(context, vfs);
		final ArrayList<ExportData> exportQueue = new ArrayList<ExportData>();
		for (Node node : nodes) {
			if (node.getNodeName().equals("file")) {
				VFSPath path;
				try {
					path = VFSUtils.parseVFSPath(node.getTextContent());
				} catch (VFSUtils.VFSPathParseException e) {
					throw new ExportException(Status.CLIENT_ERROR_BAD_REQUEST, e);
				}

				final ExportData data = factory.getExportData(path, pastData.getLastExportDate(path).getTime()).
					get(path);

				if (data != null)
					exportQueue.add(data);
			}
		}

		return performExport(exportQueue, pastData);
	}

	private Document performExport(final Collection<ExportData> exportQueue, final GGEPastExportData pastData)
			throws ExportException {
		final File zipFile;
		final ZipOutputStream out;
		try {
			final Calendar date = Calendar.getInstance();
			out = new ZipOutputStream(new FileOutputStream(zipFile = File.createTempFile("ggeexport-"
					+ new SimpleDateFormat("yyyy-MM-dd").format(date.getTime()), ".zip")));
		} catch (IOException e) {
			throw new ExportException(e);
		}

		final Document document = DocumentUtils.impl.newDocument();

		final Element root = document.createElement("root");

		final Date today = Calendar.getInstance().getTime();

		for (ExportData current : exportQueue) {
			byte[] buff = new byte[18024];

			Element sent = document.createElement("file");
			sent.setAttribute("uri", current.getPath());
			sent.setAttribute("timestamp", current.getLastModified().toString());

			try {
				final InputStream in = current.getRepresentation().getStream();
				out.putNextEntry(new ZipEntry(current.getPath()));
				int len;
				while ((len = in.read(buff)) > 0)
					out.write(buff, 0, len);
				out.closeEntry();
				in.close();
				sent.setAttribute("success", "true");
				root.appendChild(sent);
			} catch (IOException e) {
				sent.setAttribute("success", "false");
				root.appendChild(sent);
				continue;
			}

			/*
			 * final Reference url = new Reference(settings.getLocation() +
			 * current.getPath());
			 * 
			 * final Request request = new Request(Method.PUT, url,
			 * current.getRepresentation()); final Response response =
			 * client.handle(request); if (!response.getStatus().isSuccess())
			 * throw new ExportException(Status.SERVER_ERROR_INTERNAL,
			 * "Failed to export " + current.getPath());
			 */

			root.appendChild(sent);

			pastData.setLastExportDate(new VFSPath(current.getPath()), today);
		}

		try {
			out.close();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		pastData.setLastExportDate(today);
		pastData.save();

		document.appendChild(root);

		final Reference url = new Reference(settings.getLocation() + 
			"/apps/com.solertium.gogoego.server.lib.app.importer.container.ImporterApplication/import" + 
			"?key=" + new MD5Hash(settings.getKey()));
		final Request request;
		try {
			request = new Request(Method.PUT, url, new InputRepresentation(new FileInputStream(zipFile),
					MediaType.APPLICATION_ZIP));
		} catch (IOException e) {
			e.printStackTrace();
			throw new ExportException(e);
		}

		final Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		if (response.getStatus().isSuccess())
			return document;
		else
			throw new ExportException(response.getStatus(), "Failed to import.");
	}

	private void performDelete(final Collection<VFSPath> exportQueue, final GGEPastExportData pastData)
			throws ExportException {
		final Client client = new Client(Protocol.HTTPS);
		for (VFSPath current : exportQueue) {
			final Reference url = new Reference(settings.getLocation() + current);

			final Request request = new Request(Method.DELETE, url, null);
			final Response response = client.handle(request);
			if (!response.getStatus().isSuccess())
				throw new ExportException(Status.SERVER_ERROR_INTERNAL, "Failed to export " + current);

			pastData.setLastExportDate(current, null);
		}

		pastData.save();
	}

	/**
	 * Does a complete overwrite...
	 */
	public Document refresh(Document document, Context context) throws ExportException {
		final BaseExportParserFactory factory = new BaseExportParserFactory(context, vfs);

		return performExport(factory.getExportData().values(), new GGEPastExportData(vfs));
	}

	public static class GGEExportConfig {
		private SimpleExporterSettings settings;

		public GGEExportConfig(SimpleExporterSettings settings) {
			this.settings = settings;
		}

		public String getLocation() {
			return settings.get("location");
		}
		
		public String getKey() {
			return settings.get("key");
		}
	}
}
