package org.iucn.sis.server.extensions.references;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.TransactionResource;
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
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.util.NodeCollection;
import com.solertium.util.portable.XMLWritingUtils;

public class ReferenceSearchResource extends TransactionResource {

	public ReferenceSearchResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		setModifiable(true);
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	@Override
	public Representation represent(Variant variant, Session session) throws ResourceException {
		final Form form = getRequest().getResourceRef().getQueryAsForm();
		final Map<String, String> constraints = new HashMap<String, String>();
		for (String key : form.getNames())
			constraints.put(key, form.getFirstValue(key));
		
		return doQuery(constraints, session);
	}
	
	@Override
	public void acceptRepresentation(Representation entity, Session session) throws ResourceException {
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
		
		getResponse().setEntity(doQuery(constraints, session));
		getResponse().setStatus(Status.SUCCESS_OK);
	}
	
	private Representation doQuery(Map<String, String> constraints, final Session session) throws ResourceException {
		String joinTable = constraints.remove("groupTable");
		String joinColumn = constraints.remove("groupColumn");
		
		if (joinTable == null || joinColumn == null) {
			joinTable = "field_reference";
			joinColumn = "fieldid";
		}
		
		String where = null;
		for (Map.Entry<String, String> entry : constraints.entrySet()) {
			if (!"start".equals(entry.getKey()) && !"limit".equals(entry.getKey())) {
				if (where == null)
					where = "WHERE ";
				else
					where += " AND ";
				where += "UPPER(reference." + entry.getKey() + ") like '%" + entry.getValue().toUpperCase() + "%'";
			}
		}

		String query = "SELECT reference.id, COUNT("+joinTable+"."+joinColumn+") as usage " +
			"FROM reference " + 
			"LEFT JOIN " + joinTable + " ON reference.id = "+joinTable+".referenceid";
		if (where != null)
			query += " " + where;
		query += " GROUP BY (reference.id) LIMIT 500";
		
		final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		synchronized (this) {
			try {
				SIS.get().getExecutionContext().doQuery(query, new RowProcessor() {
					public void process(Row row) {
						map.put(row.get("id").getInteger(), row.get("usage").getInteger());
					}
				});
			} catch (DBException e) {
				Debug.println(e);
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
		
		if (map.isEmpty())
			return new StringRepresentation("<root/>", MediaType.TEXT_XML);
		
		Criteria crit = 
			session.createCriteria(Reference.class);
		crit = crit.add(Restrictions.in("id", map.keySet()));
		
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
			
			String count = map.containsKey(reference.getId()) ? map.get(reference.getId()) + "" : "0";
			builder.append("<reference count=\"" + count + "\">");
			for (Map.Entry<String, String> entry : reference.toMap().entrySet()) {
				builder.append(XMLWritingUtils.writeCDATATag(entry.getKey(), entry.getValue(), true));
			}
			builder.append("</reference>");
		}
		builder.append("</root>");
		
		return new StringRepresentation(builder.toString(), MediaType.TEXT_XML);
		
		/*
		try {
			final ExecutionContext ec = new SystemExecutionContext(ReferenceApplication.DBNAME);
			ec.setAPILevel(ExecutionContext.SQL_ALLOWED);

			int start = 0;
			int limit = 0;
			String where = "WHERE ";
			
			DomRepresentation queryRep = new DomRepresentation(entity);
			Document queryDoc = queryRep.getDocument();
			
			NodeList nodes = queryDoc.getDocumentElement().getChildNodes();

			for( int i = 0; i < nodes.getLength(); i++ ) {
				if( nodes.item(i).getNodeType() != Node.ELEMENT_NODE )
					continue;
				
				Element el = (Element)nodes.item(i);
				String fld = el.getNodeName();
				String p = el.getTextContent();
				String ct;
				
				if ("start".equalsIgnoreCase(fld))
					start = Integer.valueOf(p).intValue();
				else if ("limit".equalsIgnoreCase(fld))
					limit = Integer.valueOf(p).intValue();
				else {
					if (!"year".equalsIgnoreCase(fld))
						ct = "UPPER(" + fld + ") LIKE UPPER('%" + p + "%')";
					else
						ct = fld + "='" + p + "'";
					where += ct + " AND ";
				}
			}
			
			where = where.substring(0, where.length() - 5);

			String query = "SELECT * FROM (SELECT BIB_HASH AS \"MATCHED_HASH\", COUNT(ASM_ID) "
					+ "AS \"COUNT\" FROM (SELECT ASM_ID, BIB_HASH FROM "
					+ "BIBLIOGRAPHY LEFT JOIN ASSESSMENT_REFERENCE ON ASSESSMENT_REFERENCE.REF_ID="
					+ "BIBLIOGRAPHY.BIB_HASH " + where + ") GROUP BY BIB_HASH) T1 JOIN BIBLIOGRAPHY "
					+ "WHERE BIBLIOGRAPHY.BIB_HASH=T1.MATCHED_HASH LIMIT " + start + "," + limit;

			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			@SuppressWarnings("unchecked")
			final Element rootEl = doc.createElement("references");
			doc.appendChild(rootEl);
			ec.doQuery(query, new ReferenceRowProcessor(doc, rootEl, ReferenceLabels.getInstance()));

			String countQuery = "SELECT COUNT(BIBLIOGRAPHY.BIB_HASH) FROM BIBLIOGRAPHY " + where;
			SelectCountDBProcessor countProc = new SelectCountDBProcessor();
			ec.doQuery(countQuery, countProc);

			final Element totalCountEl = doc.createElement("totalCount");
			rootEl.appendChild(totalCountEl);
			totalCountEl.setAttribute("total", "" + countProc.getCount());
			
			DomRepresentation rep = new DomRepresentation(MediaType.TEXT_XML, doc);
			rep.setCharacterSet(CharacterSet.UTF_8);
			getResponse().setEntity(rep);
		} catch (final DBException dbx) {
			dbx.printStackTrace();
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			getResponse().setEntity(new StringRepresentation("No matching references found", MediaType.TEXT_PLAIN));
		} catch (final NamingException nx) {
			nx.printStackTrace();
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			getResponse().setEntity(new StringRepresentation("Reference database not available", MediaType.TEXT_PLAIN));
		} catch (final IOException iox) {
			iox.printStackTrace();
			getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
			getResponse().setEntity(new StringRepresentation("Unparsable entity", MediaType.TEXT_PLAIN));
		} catch (final ParserConfigurationException px) {
			px.printStackTrace();
			throw new RuntimeException("XML Parser not properly configured", px);
		}*/
	}
//
//	@Override
//	public Representation represent(final Variant variant) {
//		final Form f = getRequest().getResourceRef().getQueryAsForm();
//		try {
//			final ExecutionContext ec = new SystemExecutionContext(ReferenceApplication.DBNAME);
//			ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
//
//			int start = 0;
//			int limit = 0;
//			String where = "WHERE ";
//			for (final Parameter p : f) {
//				String fld = p.getName();
//				String ct;
//
//				if ("start".equalsIgnoreCase(fld))
//					start = Integer.valueOf(p.getValue()).intValue();
//				else if ("limit".equalsIgnoreCase(fld))
//					limit = Integer.valueOf(p.getValue()).intValue();
//				else {
//					if (!"year".equalsIgnoreCase(fld))
//						ct = "UPPER(" + fld + ") LIKE UPPER('%" + p.getValue() + "%')";
//					else
//						ct = fld + "='" + p.getValue() + "'";
//
//					where += ct + " AND ";
//				}
//			}
//
//			where = where.substring(0, where.length() - 5);
//
//			String query = "SELECT * FROM (SELECT BIB_HASH AS \"MATCHED_HASH\", COUNT(ASM_ID) "
//					+ "AS \"COUNT\" FROM (SELECT ASM_ID, BIB_HASH FROM "
//					+ "BIBLIOGRAPHY LEFT JOIN ASSESSMENT_REFERENCE ON ASSESSMENT_REFERENCE.REF_ID="
//					+ "BIBLIOGRAPHY.BIB_HASH " + where + ") GROUP BY BIB_HASH) T1 JOIN BIBLIOGRAPHY "
//					+ "WHERE BIBLIOGRAPHY.BIB_HASH=T1.MATCHED_HASH LIMIT " + start + "," + limit;
//
//			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//			@SuppressWarnings("unchecked")
//			final Element rootEl = doc.createElement("references");
//			doc.appendChild(rootEl);
//			ec.doQuery(query, new ReferenceRowProcessor(doc, rootEl, ReferenceLabels.getInstance()));
//
//			String countQuery = "SELECT COUNT(BIBLIOGRAPHY.BIB_HASH) FROM BIBLIOGRAPHY " + where;
//			SelectCountDBProcessor countProc = new SelectCountDBProcessor();
//			ec.doQuery(countQuery, countProc);
//
//			final Element totalCountEl = doc.createElement("totalCount");
//			rootEl.appendChild(totalCountEl);
//			totalCountEl.setAttribute("total", "" + countProc.getCount());
//
//			System.out.println("Query: " + query);
//			System.out.println("Total is " + countProc.getCount());
//			
//			DomRepresentation rep = new DomRepresentation(MediaType.TEXT_XML, doc);
//			rep.setCharacterSet(CharacterSet.UTF_8);
//			return rep;
//		} catch (final DBException dbx) {
//			dbx.printStackTrace();
//			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
//			return new StringRepresentation("No matching references found", MediaType.TEXT_PLAIN);
//		} catch (final NamingException nx) {
//			nx.printStackTrace();
//			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
//			return new StringRepresentation("Reference database not available", MediaType.TEXT_PLAIN);
//		} catch (final ParserConfigurationException px) {
//			px.printStackTrace();
//			throw new RuntimeException("XML Parser not properly configured", px);
//		}
//	}
}
