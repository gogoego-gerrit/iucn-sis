package org.iucn.sis.server.restlets.assessments;

import javax.naming.NamingException;

import org.iucn.sis.server.ServerApplication;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.utils.Constants;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;

@SuppressWarnings("deprecation")
public class AsmChangesResource extends Resource{

	private  ExecutionContext ec;
	private String asm_id;

	public AsmChangesResource(Context context, Request request, Response response) throws NamingException{
		super(context, request, response);

		if( SIS.amIOnline() ) {
			ec = new SystemExecutionContext(Constants.MIRROR_MANAGER_DBSESSION_NAME);
			ec.setExecutionLevel(ExecutionContext.READ_WRITE);
			ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
		}
		asm_id = request.getResourceRef().getLastSegment();
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public Representation represent(Variant variant) throws ResourceException {
		if( SIS.amIOnline() ) {
			final StringBuilder results = new StringBuilder("<results>\n");

			SelectQuery query = new SelectQuery();
			query.select(AsmChanges.DELETED_ASSESSMENTS_TABLE_NAME, "*");
			query.constrain(new CanonicalColumnName(AsmChanges.DELETED_ASSESSMENTS_TABLE_NAME, AsmChanges.ASM_ID), 
					QConstraint.CT_EQUALS, asm_id);

			try {
				ec.doQuery(query, new RowProcessor() {

					@Override
					public void process(Row row) {
						String uid = row.get(AsmChanges.UID).getString();
						String tableName = row.get(AsmChanges.TABLE_NAME_FIELD).getString();
						String timestamp = row.get(AsmChanges.TIMESTAMP).getString();
						String user = row.get(AsmChanges.USER).getString();
						results.append("<change asm_id=\"" + asm_id + "\" status=\"DELETED\"><uid>" + uid + "</uid><field>" + 
								tableName + "</field><date>" + timestamp + "</date><user>" + user +"</user></change>\n" );			
					}
				});
			} catch (DBException e) {
				e.printStackTrace();
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				return null;
			}

			query = new SelectQuery();
			query.select(AsmChanges.EDIT_TABLE_NAME, "*");
			query.constrain(new CanonicalColumnName(AsmChanges.EDIT_TABLE_NAME, AsmChanges.ASM_ID), 
					QConstraint.CT_EQUALS, asm_id);

			try {
				ec.doQuery(query, new RowProcessor() {

					@Override
					public void process(Row row) {
						String uid = row.get(AsmChanges.UID).getString();
						String tableName = row.get(AsmChanges.TABLE_NAME_FIELD).getString();
						String structureName = row.get(AsmChanges.STRUCTURE_NAME).getString();
						String value = row.get(AsmChanges.VALUE).getString();
						String timestamp = row.get(AsmChanges.TIMESTAMP).getString();
						String user = row.get(AsmChanges.USER).getString();

						results.append("<change asm_id=\"" + asm_id + "\"><uid>" + uid + "</uid><field>" + 
								tableName + "</field>" + "<name>" + structureName + "</name><value><![CDATA[" + value 
								+ "]]></value><date>" + timestamp + "</date><user>" + user +"</user></change>\n" );

					}
				});
			} catch (DBException e) {
				e.printStackTrace();
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				return null;
			}

			results.append("</results>");

			getResponse().setStatus(Status.SUCCESS_OK);
			return new StringRepresentation(results.toString(), MediaType.TEXT_XML);
		} else {
			getResponse().setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
			return new StringRepresentation("This feature is not available offline.", MediaType.TEXT_PLAIN);
		}
	}
}
