package org.iucn.sis.server.extensions.recentasms;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
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
		if ("all".equals(request.getAttributes().get("type"))) {
			//TODO: hibernate-ize if possible
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -1);
			
			String date = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
			
			String query = 
			"SELECT u.first_name, u.last_name, u.email, e.created_date, e.reason, " +
			"t.friendly_name, a.id as asm_id, t.id as taxon_id " +
			"FROM assessment_edit ae " +
			"JOIN edit e ON e.id = ae.editid " +
			"JOIN \"user\" u ON u.id = e.userid " +
			"JOIN assessment a ON a.id = ae.assessmentid " +
			"JOIN taxon t ON t.id = a.taxonid " +
			"WHERE reason is not null AND created_date > '" + date + "' " + 
			"ORDER BY created_date DESC " +
			"LIMIT 250";
			
			List<Object[]> list;
			try {
				list = session.createSQLQuery(query).list();
			} catch (HibernateException e) {
				e.printStackTrace();
				if (e.getCause() instanceof SQLException)
					((SQLException)e.getCause()).getNextException().printStackTrace();
				
				throw e;
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
		else
			throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
	
	

}
