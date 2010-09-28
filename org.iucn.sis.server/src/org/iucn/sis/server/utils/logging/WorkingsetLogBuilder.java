package org.iucn.sis.server.utils.logging;

import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class WorkingsetLogBuilder extends ServiceRestlet {

	private static EventLogger logger = EventLogger.impl;
	private static DBWorkingSetBuffer buffer;

	public WorkingsetLogBuilder(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	private void crawlWorkingsets() {
		Document current;
		VFSPathToken[] filelist;
		try {
			filelist = vfs.list(new VFSPath("/workingsets"));
			for (int i = 0; i < filelist.length; i++) {
				if (filelist[i].toString().endsWith(".xml")) {
					current = DocumentUtils.getVFSFileAsDocument("/workingsets/" + filelist[i], vfs);
					NodeList mode = current.getDocumentElement().getElementsByTagName("mode");
					if (mode.item(0).getTextContent().equals("public")) {
						String creator = current.getDocumentElement().getAttribute("creator");
						String id = current.getDocumentElement().getAttribute("id");
						String name = current.getDocumentElement().getElementsByTagName("name").item(0)
								.getTextContent();
						String date = current.getDocumentElement().getElementsByTagName("date").item(0)
								.getTextContent();

						String xml = "<log>\r\n</log>";
						String log = "<workingSet creator=\"" + creator + "\" id=\"" + id + "\" date=\"" + date
								+ "\" name=\"" + name + "\"/>";
						xml = xml.replace("</log>", log + "\r\n</log>");

						buffer.addEvent(DocumentUtils.createDocumentFromString(xml));

					}
				}
			}
			logger.flushBuffers();
		} catch (NotFoundException e) {
		}

	}

	@Override
	public void definePaths() {
		paths.add("/workingSet/log/rebuild");

	}

	@Override
	public void performService(Request request, Response response) {
		try {

			ExecutionContext ec = new SystemExecutionContext("default");
			ec.setExecutionLevel(ExecutionContext.ADMIN);
			ec.dropTable("WORKINGSETLOG");

			buffer = new DBWorkingSetBuffer();
			logger.addBuffer(buffer);
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}

		crawlWorkingsets();
	}

}
