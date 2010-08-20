package org.iucn.sis.server.restlets.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.locking.LockRepository;
import org.iucn.sis.server.api.locking.PersistentLockRepository;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;

import com.solertium.db.CDateTime;
import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.Row;
import com.solertium.db.utils.QueryUtils;
import com.solertium.util.BaseDocumentUtils;

public class LockManagementRestlet extends ServiceRestlet {
	
	private final LockRepository repository;
	
	public LockManagementRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
		
		repository = new PersistentLockRepository();
	}

	public void definePaths() {
		if (SIS.amIOnline()) {
			paths.add("/management/locks/{protocol}");
			paths.add("/management/locks/{protocol}/{identifier}");
		}
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
		final List<Row> rows = new ArrayList<Row>();
		
		if (PersistentLockRepository.LOCK_TABLE.equals(table)) {
			for (LockRepository.Lock lock : repository.listLocks()) {
				final Row row = new Row(); 
				/*final char[] lockid = lock..toCharArray();
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
				}*/
				Assessment data = SIS.get().getAssessmentIO().
					getAssessment(Integer.valueOf(lock.getLockID()));
				if (data == null)
					row.add(new CString("species", lock.getLockID().toString()));
				else
					row.add(new CString("species", data.getSpeciesName()));
				row.add(new CString("status", lock.getLockType().toString()));
				row.add(new CInteger("lockid", lock.getLockID()));
				row.add(new CString("owner", lock.getUsername()));
				row.add(new CDateTime("date", new Date(lock.getWhenLockAcquired())));
			}
		}
		else {
			for (Map.Entry<String, List<Integer>> entry : repository.listGroups().entrySet()) {
				WorkingSet data = SIS.get().getWorkingSetIO().readWorkingSet(Integer.valueOf(entry.getKey()));
				
				String groupName;
				if (data == null)
					groupName = entry.getKey();
				else 
					groupName = data.getWorkingSetName();
				
				for (Integer lockID : entry.getValue()) {
					final Row row = new Row();
					row.add(new CString("groupid", entry.getKey()));
					row.add(new CString("groupname", groupName));
					row.add(new CInteger("persistentlockid", lockID));
					
					rows.add(row);
				}
			}
		}

		response.setEntity(new DomRepresentation(
			MediaType.TEXT_XML, QueryUtils.writeDocumentFromRowSet(rows)
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
			
			repository.removeLockByID(id);

			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, BaseDocumentUtils.impl.createConfirmDocument("Assessment unlocked.")));
		}
		else {
			repository.clearGroup(identifier);
			
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, BaseDocumentUtils.impl.createConfirmDocument("Assessments unlocked.")));
		}
	}
	
	private boolean isValid(String table) {
		return PersistentLockRepository.LOCK_TABLE.equals(table) || 
			PersistentLockRepository.LOCK_GROUPS_TABLE.equals(table);
	}

}
