package org.iucn.sis.server.simple;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.io.WorkingSetIO;
import org.iucn.sis.server.locking.PersistentLockRepository;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;

import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.utils.QueryUtils;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;

public class LockManagementRestlet extends ServiceRestlet {
	
	private final ExecutionContext ec;
	
	public LockManagementRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
		
		if(SISContainerApp.amIOnline)
			this.ec = PersistentLockRepository.getExecutionContext();
		else
			this.ec = null;
	}

	public void definePaths() {
		paths.add("/management/locks/{protocol}");
		paths.add("/management/locks/{protocol}/{identifier}");
	}

	public void performService(Request request, Response response) {
		final String table = (String)request.getAttributes().get("protocol");
		if (!isValid(table)) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid protocol");
			return;
		}
		
		try {
			if (Method.GET.equals(request.getMethod()))
				doGet(response, table);
			else if (Method.DELETE.equals(request.getMethod()))
				doDelete(request, response, table);
			else
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		} catch (Throwable e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private void doGet(Response response, final String table) {
		final SelectQuery query = new SelectQuery();
		query.select(table, "*");
		
		final Row.Set rs = new Row.Set();
		
		try {
			ec.doQuery(query, rs);
		} catch (DBException e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
			return;
		}
		
		if (PersistentLockRepository.LOCK_TABLE.equals(table)) {
			for (Row row : rs.getSet()) {
				final char[] lockid = row.get("lockid").toString().toCharArray();
				final StringBuilder idBuilder = new StringBuilder();
				final StringBuilder typeBuilder = new StringBuilder();
				
				boolean haveID = false;
				for (int i = 0; i < lockid.length; i++) {
					if (!haveID && Character.isDigit(lockid[i]))
						idBuilder.append(lockid[i]);
					else {
						haveID = true;
						typeBuilder.append(lockid[i]);
					}
				}
				AssessmentData data = AssessmentIO.readAssessment(vfs, idBuilder.toString(), typeBuilder.toString(), null);
				if (data == null)
					row.add(new CString("species", row.get("lockid").toString()));
				else
					row.add(new CString("species", data.getSpeciesName()));
				row.add(new CString("status", typeBuilder.toString()));
			}
		}
		else {
			for (Row row : rs.getSet()) {
				WorkingSetData data = WorkingSetIO.readPublicWorkingSetAsWorkingSetData(vfs, row.get("groupid").toString());
				if (data == null)
					row.add(new CString("groupname", row.get("groupid").toString()));
				else
					row.add(new CString("groupname", data.getWorkingSetName()));
			}
		}

		response.setEntity(new DomRepresentation(
			MediaType.TEXT_XML, QueryUtils.writeDocumentFromRowSet(rs.getSet())
		));
	}

	private void doDelete(Request request, Response response, final String table) {
		final String identifier = (String)request.getAttributes().get("identifier");
		if (identifier == null) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply an identifier");
			return;
		}
		
		if (PersistentLockRepository.LOCK_TABLE.equals(table)) {
			final Integer id;
			try {
				id = Integer.valueOf(identifier);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid identifier");
				return;
			}
			
			final DeleteQuery query = new DeleteQuery();
			query.setTable(table);
			query.constrain(new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, id);
			
			try {
				ec.doUpdate(query);
			} catch (DBException e) {
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
				return;
			}

			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, BaseDocumentUtils.impl.createConfirmDocument("Assessment unlocked.")));
		}
		else {
			final QConstraint groupConstraint = new QComparisonConstraint(
				new CanonicalColumnName(PersistentLockRepository.LOCK_GROUPS_TABLE, "groupid"), 
				QConstraint.CT_EQUALS, identifier
			);
		
			final Row.Set rs = new Row.Set(); {
				final SelectQuery query = new SelectQuery();
				query.select(PersistentLockRepository.LOCK_GROUPS_TABLE, "persistentlockid");
				query.constrain(groupConstraint);
			
				try {
					ec.doQuery(query, rs);
				} catch (DBException e) {
					e.printStackTrace();
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					return;
				}
			}
			
			for (Row row : rs.getSet()) {
				final DeleteQuery query = new DeleteQuery();
				query.setTable(PersistentLockRepository.LOCK_TABLE);
				query.constrain(new CanonicalColumnName(PersistentLockRepository.LOCK_TABLE, "id"), QConstraint.CT_EQUALS, row.get("persistentlockid").getInteger());
				
				try {
					ec.doUpdate(query);
				} catch (DBException e) {
					System.out.println("Failed to unlock row with id: " + row.get("persistentlockid"));
					e.printStackTrace();
					TrivialExceptionHandler.ignore(this, e);
				}
			}
			
			
			/*
			final DeleteQuery query = new DeleteQuery();
			query.setTable(PersistentLockRepository.LOCK_TABLE);
			query.join(PersistentLockRepository.LOCK_GROUPS_TABLE, new QRelationConstraint(
				new CanonicalColumnName(PersistentLockRepository.LOCK_TABLE, "id"), 
				new CanonicalColumnName(PersistentLockRepository.LOCK_GROUPS_TABLE, "persistentlockid")
			));
			query.constrain(groupConstraint);
			
			try {
				ec.doUpdate(query); 
			} catch (DBException e) {
				System.out.println(query.getSQL(ec.getDBSession()));
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
				return;
			}*/
			
			final DeleteQuery deleteGroup = new DeleteQuery();
			deleteGroup.setTable(PersistentLockRepository.LOCK_GROUPS_TABLE);
			deleteGroup.constrain(groupConstraint);
			
			try {
				ec.doUpdate(deleteGroup); 
			} catch (DBException e) {
				//Extra data will lay around, but doesn't affect the locking system.
				e.printStackTrace();
				TrivialExceptionHandler.ignore(this, e);
			}
			
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, BaseDocumentUtils.impl.createConfirmDocument("Assessments unlocked.")));
		}
	}
	
	private boolean isValid(String table) {
		return PersistentLockRepository.LOCK_TABLE.equals(table) || 
			PersistentLockRepository.LOCK_GROUPS_TABLE.equals(table);
	}

}
