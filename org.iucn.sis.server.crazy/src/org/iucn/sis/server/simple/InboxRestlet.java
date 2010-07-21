package org.iucn.sis.server.simple;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class InboxRestlet extends ServiceRestlet {
	public InboxRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/inbox/{userName}");
	}

	private void getMessages(Response response, String userName) {
		// TODO: GET ACTUAL MESSAGES

		StringBuffer xml = new StringBuffer("<messages>\r\n");

		/*
		 * xml.append("<message>\r\n");xml.append(
		 * "<subject>Assessment dendroica_cerulea for Review</subject>\r\n" );
		 * xml.append("<from>Adam Smith</from>\r\n");
		 * xml.append("<date>12/02/08</date>");xml.append(
		 * "<body>Adam Smith wants you to review the assessment for dendroica_cerulea.</body>\r\n"
		 * ); xml.append("</message>\r\n");
		 * 
		 * xml.append("<message>\r\n");
		 * xml.append("<subject>New Feature: My Working Set Panel</subject>\r\n"
		 * ); xml.append("<from>Admin</from>\r\n");
		 * xml.append("<date>12/02/08</date>");xml.append(
		 * "<body>A new feature has been added to SIS.  You can now add working sets and species names"
		 * + " in your My Working Set panel. </body>\r\n");
		 * xml.append("</message>\r\n");
		 */

		xml.append("</messages>");

		response.setEntity(xml.toString(), MediaType.TEXT_XML);
	}

	@Override
	public void performService(Request request, Response response) {

		if (request.getResourceRef().getPath().startsWith("/inbox/")) {
			getMessages(response, (String) request.getAttributes().get("userName"));
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}

	}

}
