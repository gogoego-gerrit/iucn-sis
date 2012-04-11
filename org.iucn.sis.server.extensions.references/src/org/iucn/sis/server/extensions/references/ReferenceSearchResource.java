package org.iucn.sis.server.extensions.references;

import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.restlets.SimpleRestlet;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Reference;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.vendor.H2DBSession;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.portable.XMLWritingUtils;


public class ReferenceSearchResource extends SimpleRestlet {
	
	public ReferenceSearchResource(final Context context) {
		super(context);
	}
	
	@Override
	public void definePaths() {
		paths.add(ServerApplication.PREFIX + "/search/reference");
	}
	
	@Override
	public Representation handleGet(Request request, Response response) throws ResourceException {
		final Form form = request.getResourceRef().getQueryAsForm();
		final Map<String, String> constraints = new HashMap<String, String>();
		for (String key : form.getNames())
			constraints.put(key, form.getFirstValue(key));
		
		return doQuery(constraints);
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response) throws ResourceException {
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		final Map<String, String> constraints = new HashMap<String, String>();
		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());
		for (Node node : nodes)
			constraints.put(node.getNodeName(), node.getTextContent());
		
		response.setEntity(doQuery(constraints));
		response.setStatus(Status.SUCCESS_OK);
	}
	
	private String clean(String value) {
		return SIS.get().getQueries().cleanSearchTerm(value).replace("'", "''");
	}
	
	private Representation doQuery(Map<String, String> constraints) throws ResourceException {
		String joinTable = constraints.remove("groupTable");
		String joinColumn = constraints.remove("groupColumn");
		
		if (joinTable == null || joinColumn == null) {
			joinTable = "field_reference";
			joinColumn = "fieldid";
		}
		
		String where = null;
		for (Map.Entry<String, String> entry : constraints.entrySet()) {
			if (!"start".equals(entry.getKey()) && !"limit".equals(entry.getKey())) {
				String table = "reference";
				String column = entry.getKey();
				try {
					if (DBSessionFactory.getDBSession("sis") instanceof H2DBSession) {
						table = "r";
						if ("year".equals(column))
							column = "\"year\"";
						else
							column = column.toUpperCase();
					}	
				} catch (NamingException e) {
					TrivialExceptionHandler.ignore(this, e);
				}
					
				if (where == null)
					where = "WHERE ";
				else
					where += " AND ";
				where += "UPPER(" + table + "." + column + ") like '%" + clean(entry.getValue().toUpperCase()) + "%'";
			}
		}
		
		if (where == null)
			return new StringRepresentation("<root/>", MediaType.TEXT_XML);

		String query = SIS.get().getQueries().getReferenceSearchQuery(where);
		
		final Map<Integer, Row> map = new HashMap<Integer, Row>();
		synchronized (this) {
			try {
				SIS.get().getExecutionContext().doQuery(query, new RowProcessor() {
					public void process(Row row) {
						map.put(row.get("id").getInteger(), row);
					}
				});
			} catch (DBException e) {
				Debug.println(e);
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
			}
		}
		
		if (map.isEmpty())
			return new StringRepresentation("<root/>", MediaType.TEXT_XML);
		
		final Session session = SISPersistentManager.instance().openSession();
		
		Criteria crit = session.createCriteria(Reference.class)
			.add(Restrictions.in("id", map.keySet()));
		
		final StringBuilder builder = new StringBuilder();
		builder.append("<root>");
		for (Object possiblReference : crit.list()) {
			Reference reference;
			try {
				reference = (Reference)possiblReference;
			} catch (ClassCastException e) {
				//??
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			if (map.containsKey(reference.getId())) {
				/*
				 * TODO: we can use the join type to set the count to something 
				 * context-sensitive.  For now, to stop the pain, going back to 
				 * the SIS 1 way and making the count be the system-wide total.
				 */
				try {
				Row row = map.get(reference.getId());
				
				StringBuilder attrs = new StringBuilder();
				for (Column col : row.getColumns())
					if (!"id".equals(col.getLocalName().toLowerCase()))
						attrs.append(String.format(" %s=\"%s\"", col.getLocalName().toLowerCase(), col.toString()));
				builder.append(String.format("<reference count=\"%s\"%s>", row.get("total_count"), attrs));
				} catch (Throwable e) {
					builder.append("<reference count=\"0\">");
				}
			}
			else //??!?
				builder.append("<reference count=\"0\">");
			
			for (Map.Entry<String, String> entry : reference.toMap().entrySet())
				builder.append(XMLWritingUtils.writeCDATATag(entry.getKey(), entry.getValue(), true));
			
			builder.append("</reference>");
		}
		builder.append("</root>");
		
		session.close();
		
		return new StringRepresentation(builder.toString(), MediaType.TEXT_XML);
	}
	
}
