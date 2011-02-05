package org.iucn.sis.server.utils.logging;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class WorkingsetLogBuilder extends BaseServiceRestlet {

	private static EventLogger logger = EventLogger.impl;
	private static DBWorkingSetBuffer buffer;

	private final VFS vfs;
	
	public WorkingsetLogBuilder(Context context) {
		super(context);
		this.vfs = SIS.get().getVFS();
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
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		handleGet(request, response, session);
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		try {
			ExecutionContext ec = new SystemExecutionContext("default");
			ec.setExecutionLevel(ExecutionContext.ADMIN);
			ec.dropTable("WORKINGSETLOG");

			buffer = new DBWorkingSetBuffer();
			logger.addBuffer(buffer);
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		crawlWorkingsets();
		
		return new StringRepresentation("Done");
	}

}
