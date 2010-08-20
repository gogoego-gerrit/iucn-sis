package org.iucn.sis.server.api.locking;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NamingException;

import org.iucn.sis.shared.api.models.User;
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
import com.solertium.db.RowProcessor;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QComparisonConstraint;
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

	@Override
	public Lock getLockedAssessment(Integer id) {
		final Integer identifier = id;
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
				LockType.fromString(row.get("type").toString()), 
				row.get("date").getDate(), this
			);
		}
	}

	public boolean isAssessmentPersistentLocked(Integer id) {
		final String identifier = id.toString();
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
	
	@Override
	public Map<String, List<Integer>> listGroups() {
		final SelectQuery query = new SelectQuery();
		query.select(LOCK_GROUPS_TABLE, "*");
		
		final Map<String, List<Integer>> map = 
			new ConcurrentHashMap<String, List<Integer>>();
		
		synchronized (this) {
			try {
				ec.doQuery(query, new RowProcessor() {
					public void process(Row row) {
						final String groupID = row.get("groupid").toString();
						
						List<Integer> list = map.get(groupID); 
						if (list == null)
							list = new ArrayList<Integer>();
						
						list.add(row.get("persistentlockid").getInteger());
						
						map.put(groupID, list);
					}
				});
			} catch (DBException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		};
		
		return map;
	}
	
	public List<Lock> listLocks() {
		final SelectQuery query = new SelectQuery();
		query.select(LOCK_TABLE, "*");
		
		final List<Lock> list = new ArrayList<Lock>();
		
		synchronized (this) {
			try {
				ec.doQuery(query, new RowProcessor() {
					public void process(Row row) {
						list.add(new LockRepository.Lock(
							row.get("lockid").getInteger(), row.get("owner").toString(), 
							LockType.fromString(row.get("type").toString()), 
							row.get("date").getDate(), 
							PersistentLockRepository.this
						));
					}
				});
			} catch (DBException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		};
		
		return list;
	}

	@Override
	public Lock lockAssessment(Integer id, User owner, LockType lockType) {
		return lockAssessment(id, owner, lockType, null);
	}
	
	@Override
	public Lock lockAssessment(Integer id, User owner, LockType lockType, String groupID) {
		if (isAssessmentPersistentLocked(id)) {
			System.out.println("This assessment is already locked");
			return getLockedAssessment(id);
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
		row.add(new CString("lockid", id.toString()));
		row.add(new CString("owner", owner.getUsername()));
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
		
		return new LockRepository.Lock(id, owner.getUsername(), lockType, this);
	}

	public void removeLockByID(Integer id) {
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
		final QConstraint groupConstraint = new QComparisonConstraint(
			new CanonicalColumnName(PersistentLockRepository.LOCK_GROUPS_TABLE, "groupid"), 
			QConstraint.CT_EQUALS, id
		);
		
		final Row.Set rs = new Row.Set(); {
			final SelectQuery query = new SelectQuery();
			query.select(PersistentLockRepository.LOCK_GROUPS_TABLE, "persistentlockid");
			query.constrain(groupConstraint);
		
			try {
				ec.doQuery(query, rs);
			} catch (DBException e) {
				e.printStackTrace();
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
