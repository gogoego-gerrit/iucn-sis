package org.iucn.sis.server.simple;

import java.io.IOException;
import java.util.ArrayList;

import org.iucn.sis.server.H2DBIndexType;
import org.iucn.sis.server.VFSSearchCrawler;
import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.FilenameStriper;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;

import com.solertium.util.SysDebugger;

public class SearchRestlet extends ServiceRestlet {

	VFSSearchCrawler crawler;

	public SearchRestlet(VFSSearchCrawler crawler, String vfsroot, Context context) {
		super(vfsroot, context);
		this.crawler = crawler;
	}

	@Override
	public void definePaths() {
		paths.add("/search");

	}

	@Override
	public void performService(Request request, Response response) {
		try {
			Document terms = new DomRepresentation(request.getEntity()).getDocument();;

			String results = "<results>\r\n";
			H2DBIndexType index = (H2DBIndexType) crawler.getIndex();
			ArrayList<String> resultList = index.get(terms);

			for (int i = 0; i < resultList.size(); i++) {
				String temp = resultList.get(i);
				String dir = FilenameStriper.getIDAsStripedPath(temp);

				Document node = DocumentUtils.getVFSFileAsDocument("/browse/nodes/" + dir + ".xml", vfs);

				results += "<result id=\"" + resultList.get(i) + "\">" + node.getDocumentElement().getAttribute("name")
						+ "</result>\r\n";
			}

			results += "</results>\r\n";

			SysDebugger.getInstance().println(results);
			response.setEntity(results, MediaType.TEXT_XML);
			response.setStatus(Status.SUCCESS_OK);
		} catch (IOException e) {
			e.printStackTrace();

			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

}
