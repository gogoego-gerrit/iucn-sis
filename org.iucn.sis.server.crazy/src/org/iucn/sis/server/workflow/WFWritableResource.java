package org.iucn.sis.server.workflow;

import java.util.Calendar;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.db.CDateTime;
import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Row;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;
import com.solertium.db.restlet.DBResource;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.ElementCollection;
import com.solertium.util.NodeCollection;

public class WFWritableResource extends DBResource {
	
	private final String workingSet;

	public WFWritableResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		
		workingSet = (String)request.getAttributes().get("working-set");
	}

	/**
	 * <root>
	 * 	<table name="...">
	 * 		<row>
	 * 			<field name="...">...</field>
	 * 			...
	 * 		</row>
	 * 		...
	 * 	</table>
	 * 	...
	 * </root>
	 */
	public void acceptRepresentation(Representation entity) throws ResourceException {
		final Document document = getDocument(entity);
		final ElementCollection tables = new ElementCollection(
			document.getDocumentElement().getElementsByTagName("table")
		);
		for (Element el : tables) {
			String tbl = el.getAttribute("name");
			
			final NodeCollection rows = new NodeCollection(el.getChildNodes());
			for (Node node : rows) {
				if ("row".equals(node.getNodeName())) {
					final Row row = new Row();
					final ElementCollection fields = new ElementCollection(
						node.getChildNodes()	
					);
					for (Node field : fields) {
						if ("field".equals(field.getNodeName())) {
							row.add(new CString(
								BaseDocumentUtils.impl.getAttribute(field, "name"), 
								field.getTextContent()
							));
						}
					}
					
					if (WorkflowConstants.WORKFLOW_NOTES_TABLE.equals(tbl)) {
						row.add(new CInteger("workflowstatusid", getID()));
						row.add(new CDateTime("date", Calendar.getInstance().getTime()));
					}
					
					if (WorkflowConstants.WORKFLOW_TABLE.equals(tbl) && exists())
						update(row);
					else
						insert(tbl, row);
				}
			}
		}
		
	}
	
	private void insert(String table, Row row) throws ResourceException {
		row.add(new CInteger("id", newID(table, "id")));
		
		final InsertQuery query = new InsertQuery();
		query.setTable(table);
		query.setRow(row);
		
		doUpdate(query);
	}
	
	private void update(Row row) throws ResourceException {
		final UpdateQuery query = new UpdateQuery();
		query.setRow(row);
		query.setTable(WorkflowConstants.WORKFLOW_TABLE);
		query.constrain(getConstraint());
		
		doUpdate(query);
	}
	
	private Document getDocument(Representation entity) throws ResourceException {
		try  {
			return new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
	}
	
	private boolean exists() throws ResourceException {
		final SelectQuery query = new SelectQuery();
		query.select(WorkflowConstants.WORKFLOW_TABLE, "id");
		query.constrain(getConstraint());
		
		return getRow(query) != null;
	}
	
	private Number getID() throws ResourceException {
		final SelectQuery query = new SelectQuery();
		query.select(WorkflowConstants.WORKFLOW_TABLE, "id");
		query.constrain(getConstraint());
		
		final Row row = getRow(query);
		
		if (row == null)
			return null;
		else
			return row.get("id").getInteger();
	}
	
	private QConstraint getConstraint() {
		return new QComparisonConstraint(
			new CanonicalColumnName(WorkflowConstants.WORKFLOW_TABLE, "workingsetid"), 
			QConstraint.CT_EQUALS, workingSet
		);
	}
	
}
