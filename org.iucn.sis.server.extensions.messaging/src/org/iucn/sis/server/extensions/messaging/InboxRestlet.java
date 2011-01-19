package org.iucn.sis.server.extensions.messaging;

import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

public class InboxRestlet extends BaseServiceRestlet {
	public InboxRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/inbox/{userName}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response) throws ResourceException {
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

		return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
	}

}
