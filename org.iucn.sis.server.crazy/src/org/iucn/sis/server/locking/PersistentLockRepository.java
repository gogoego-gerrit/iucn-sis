package org.iucn.sis.server.locking;

import java.util.Date;

import javax.naming.NamingException;

import org.w3c.dom.Document;

import com.solertium.db.CDateTime;
import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowID;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.SysDebugger;
import com.solertium.util.TrivialExceptionHandler;

public class PersistentLockRepository extends LockRepository {
	
	public static final String LOCK_TABLE = "persistentlock";
	public static final String LOCK_GROUPS_TABLE = "persistentlockgroup";
	
	public static ExecutionContext getExecutionContext() {
		final ExecutionContext ec;
		try {
			ec = new SystemExecutionContext(DBSessionFactory.getDBSession("assess"));
			ec.setAPILevel(ExecutionContext.API_ONLY);
			ec.setExecutionLevel(ExecutionContext.ADMIN);
			ec.appendStructure(getStructureDocument(), true);
		} catch (NamingException e) {
			throw new RuntimeException("The database was not found", e);
		} catch (DBException e) {
			throw new RuntimeException(
					"The database structure could not be set", e);
		}
		return ec;
	}
	
	private static Document getStructureDocument() {
		return BaseDocumentUtils.impl.getInputStreamFile(
			PersistentLockRepository.class.getResourceAsStream("lockstruct.xml")
		);
	}
	
	private final ExecutionContext ec;
	
	public PersistentLockRepository() {
		try {
			ec = new SystemExecutionContext(DBSessionFactory.getDBSession("assess"));
			ec.createStructure(getStructureDocument());
		} catch (NamingException e) {
			throw new RuntimeException("The database was not found", e);
		} catch (DBException e) {
			throw new RuntimeException(
					"The database structure could not be set", e);
		}
		ec.setAPILevel(ExecutionContext.API_ONLY);
		ec.setExecutionLevel(ExecutionContext.READ_WRITE);
	}

	public Lock getLockedAssessment(String id, String type) {
		final String identifier = id + type;
		final SelectQuery query = new SelectQuery();
		query.select(LOCK_TABLE, "owner");
		query.select(LOCK_TABLE, "type");
		query.constrain(new CanonicalColumnName(LOCK_TABLE, "lockid"), 
			QConstraint.CT_EQUALS, identifier	
		);
		
		final Row.Loader rl = new Row.Loader();
		
		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			return null;
		}
		
		if (rl.getRow() == null)
			return null;
		else {
			final Row row = rl.getRow();
			return new LockRepository.Lock(
				identifier, row.get("owner").toString(), 
				LockType.fromString(row.get("type").toString()), this
			);
		}
	}

	public boolean isAssessmentPersistentLocked(String id, String type) {
		final String identifier = id + type;
		final SelectQuery query = new SelectQuery();
		query.select(LOCK_TABLE, "id");
		query.constrain(new CanonicalColumnName(LOCK_TABLE, "lockid"), 
			QConstraint.CT_EQUALS, identifier	
		);
		
		final Row.Loader rl = new Row.Loader();
		
		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			return false;
		}
		
		return rl.getRow() != null;
	}

	public Lock lockAssessment(String id, String type, String owner, LockType lockType) {
		return lockAssessment(id, type, owner, lockType, null);
	}
	
	public Lock lockAssessment(String id, String type, String owner, LockType lockType, String groupID) {
		if (isAssessmentPersistentLocked(id, type)) {
			System.out.println("This assessment is already locked");
			return getLockedAssessment(id, type);
		}
		
		final Integer rowID;
		try {
			rowID = Integer.valueOf((int)RowID.get(ec, LOCK_TABLE, "id"));
		} catch (DBException e) {
			System.out.println("Could not create lock! " + e.getMessage());
			return null;
		}
		
		final Row row = new Row();
		row.add(new CInteger("id", rowID));
		row.add(new CString("lockid", id + type));
		row.add(new CString("owner", owner));
		row.add(new CDateTime("date", new Date()));
		row.add(new CString("type", lockType.toString()));

		final InsertQuery query = new InsertQuery();
		query.setRow(row);
		query.setTable(LOCK_TABLE);
		
		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			System.out.println("Could not add lock: " + e.getMessage());
			return null;
		}
		
		if (groupID != null) {
			Integer groupRowID = null;
			try {
				groupRowID = Integer.valueOf((int)RowID.get(ec, LOCK_GROUPS_TABLE, "id"));
			} catch (DBException e) {
				System.out.println("Could not create lock group! " + e.getMessage());
				//return null;
			}
			
			if (groupRowID != null) {
				final Row groupRow = new Row();
				groupRow.add(new CInteger("id", groupRowID));
				groupRow.add(new CInteger("persistentlockid", rowID));
				groupRow.add(new CString("groupid", groupID));
				
				final InsertQuery groupQuery = new InsertQuery();
				groupQuery.setTable(LOCK_GROUPS_TABLE);
				groupQuery.setRow(groupRow);
				
				try {
					ec.doUpdate(groupQuery); 
				} catch (DBException e) {
					TrivialExceptionHandler.ignore(this, e);
				}
			}
		}
		
		return new LockRepository.Lock(id+type, owner, lockType, this);
	}

	public void removeLockByID(String id) {
		final Row.Loader rl = new Row.Loader(); {
			final SelectQuery query = new SelectQuery();
			query.select(LOCK_TABLE, "id");
			query.constrain(new CanonicalColumnName(LOCK_TABLE, "lockid"), 
				QConstraint.CT_EQUALS, id
			);
			
			try {
				ec.doQuery(query, rl);
			} catch (DBException e) {
				e.printStackTrace();
				return;
			}
		}
		
		final Integer rowID;
		if (rl.getRow() == null) {
			System.out.println("Lock " + id + " not found.");
			return;
		}
		else
			rowID = rl.getRow().get("id").getInteger();
		
		{
			final DeleteQuery query = new DeleteQuery();
			query.setTable(LOCK_TABLE);
			query.constrain(new CanonicalColumnName(LOCK_TABLE, "id"), 
				QConstraint.CT_EQUALS, rowID
			);
			
			try {
				ec.doUpdate(query); 
				SysDebugger.out.println("Removed lock {0} ({1})", rowID, id);
			} catch (DBException e) {
				System.out.println("Delete query failed: " + e.getMessage());
				TrivialExceptionHandler.ignore(this, e);
			}
		}
		{
			final DeleteQuery query = new DeleteQuery();
			query.setTable(LOCK_GROUPS_TABLE);
			query.constrain(new CanonicalColumnName(LOCK_GROUPS_TABLE, "persistentlockid"), 
				QConstraint.CT_EQUALS, rowID
			);
			
			try {
				ec.doUpdate(query); 
			} catch (DBException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
	}
	
	public void clearGroup(String id) {
		final DeleteQuery query = new DeleteQuery();
		query.setTable(LOCK_GROUPS_TABLE);
		query.constrain(new CanonicalColumnName(LOCK_GROUPS_TABLE, "groupid"), QConstraint.CT_EQUALS, id);
		
		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			System.out.println("Could not clear lock group table of locks for " + id + ": " + e.getMessage());
			TrivialExceptionHandler.ignore(this, e);
		}
	}

}
