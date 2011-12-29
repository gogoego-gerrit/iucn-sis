package org.iucn.sis.server.extensions.recentasms;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.solertium.util.portable.XMLWritingUtils;

public class RecentActivityRestlet extends BaseServiceRestlet {
	
	public RecentActivityRestlet(Context context) {
		super(context);
	}
	
	@Override
	public void definePaths() {
		paths.add("/activity/{type}");
		paths.add("/activity/{type}/{user}");
	}
	
	@SuppressWarnings("unchecked")
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		
		String mode = (String)request.getAttributes().get("type");
		
		String query = null;
		/*
		 * Queries require 
		 * 1. user.first_name
		 * 2. user.last_name
		 * 3. user.email
		 * 4. edit.created_date
		 * 5. edit.reason
		 * 6. taxon.friendly_name
		 * 7. assessment.id as asm_id
		 * 8. taxon.id as taxon_id
		 */
		
		/*
		 * If you have working sets, it will find recent activity for 
		 * those working sets.  If you don't then it won't bother.
		 */
		if ("ws".equals(mode)) {
			User user = getUser(request, session);
			Hibernate.initialize(user.getSubscribedWorkingSets());
			if (user.getSubscribedWorkingSets() != null && !user.getSubscribedWorkingSets().isEmpty())
				query = 
					SIS.get().getQueries().getRecentActivity(mode, cal.getTime(), Integer.toString(user.getId()));
			else
				return new StringRepresentation("<root/>", MediaType.TEXT_XML);
		}
		else if ("mine".equals(mode)) {
			User user = getUser(request, session);
			
			query = SIS.get().getQueries().getRecentActivity(mode, cal.getTime(), Integer.toString(user.getId()));
		}
		
		if (query == null) {
			query = SIS.get().getQueries().getRecentActivity(mode, cal.getTime());
		}
		
		final List<Object[]> list;
		try {
			list = session.createSQLQuery(query).list();
		} catch (Exception e) {
			Debug.println(e);
			throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, e);
		}
		
		final StringBuilder out = new StringBuilder();
		out.append("<root>");
		
		for (Object[] row : list) {
			User user = new User();
			user.setFirstName((String)row[0]);
			user.setLastName((String)row[1]);
			user.setEmail((String)row[2]);
			
			out.append("<activity taxonid=\"" + row[7] + "\" assessmentid=\"" + row[6] + "\">");
			out.append(XMLWritingUtils.writeCDATATag("user", user.getDisplayableName()));
			out.append(XMLWritingUtils.writeTag("date", ((Date)row[3]).getTime() + ""));
			out.append(XMLWritingUtils.writeCDATATag("reason", (String)row[4]));
			out.append(XMLWritingUtils.writeCDATATag("taxon", (String)row[5]));
			out.append("</activity>");
			
		}
		
		out.append("</root>");
		
		return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
	}
	
}
