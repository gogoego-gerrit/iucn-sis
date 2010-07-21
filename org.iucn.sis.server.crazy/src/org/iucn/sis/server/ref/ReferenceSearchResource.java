package org.iucn.sis.server.ref;

import java.io.IOException;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.iucn.sis.server.utils.SelectCountDBProcessor;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;

public class ReferenceSearchResource extends Resource {

	public ReferenceSearchResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_XML));
		setModifiable(true);
	}
	
	@Override
	public void acceptRepresentation(Representation entity) throws ResourceException {
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
		}
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
