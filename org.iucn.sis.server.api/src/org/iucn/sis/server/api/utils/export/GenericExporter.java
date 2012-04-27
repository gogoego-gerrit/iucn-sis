package org.iucn.sis.server.api.utils.export;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.NamingException;

import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Taxon;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.DynamicWriter;
import com.solertium.util.TrivialExceptionHandler;

public class GenericExporter extends DynamicWriter implements Runnable {
	
	protected ExecutionContext source;
	protected ExecutionContext target;
	
	public GenericExporter(ExecutionContext source) {
		super();
		this.source = source;
	}
	
	public void setTarget(String name) throws NamingException {
		SystemExecutionContext ec = new SystemExecutionContext(name);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		
		setTarget(ec);
	}
	
	public void setSource(ExecutionContext source) {
		this.source = source;
	}
	
	public void setTarget(ExecutionContext target) {
		this.target = target;
	}
	
	public final void run() {
		Date start = Calendar.getInstance().getTime();
		
		write("Export started at %s", start);
		try {
			
			
			execute();
		} catch (Throwable e) {
			Debug.println(e);
			write("Failed unexpectedly: %s", e.getMessage());
		} finally {
			Date end = Calendar.getInstance().getTime();
			
			long time = end.getTime() - start.getTime();
			int secs = (int) (time / 1000);
			int mins = (int) (secs / 60);
			
			write("Export completed at %s in %s minutes, %s seconds.", end, mins, secs);
			
			close();
		}
	}
	
	protected void execute() throws Throwable {
		if (target == null) {
			write("Please specify a target.");
			return;
		}
		
		try {
			copyTables();
			afterRun();
		} catch (DBException e) {
			write("Failed to copy assessment data: %s", e.getMessage());
			Debug.println(e);
			return;
		}
	}
	
	/**
	 * Called after the execute() is completed.  
	 */
	protected void afterRun() throws DBException {
		
	}
	
	/**
	 * This function copies tables from the source database 
	 * (view), but only copies table which start with "export_".
	 * The contractual agreement here is that this table contains 
	 * an assessmentid column.
	 * @throws DBException
	 */
	private void copyTables() throws DBException {
		final Collection<String> allTables = source.getDBSession().listTables(source);
		
		for (String table : allTables) {
			write("Copying table %s", table);
			createAndCopyTable(table, getQuery(table), getRowProcessor(table));
		}
	}
	
	protected void createTable(String table) throws DBException {
		createTable(table, table);
	}
	
	protected void createTable(String table, String sourceTable) throws DBException {
		try {
			target.dropTable(table);
		} catch (DBException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		
		try {
			write(" -- Creating table " + table);
			target.createTable(table, source.getRow(sourceTable));
		} catch (DBException e) {
			TrivialExceptionHandler.ignore(this, e);
			
			DeleteQuery deleteAll = new DeleteQuery();
			deleteAll.setTable(table);
			
			try {
				target.doUpdate(deleteAll);
			} catch (DBException f) {
				TrivialExceptionHandler.ignore(this, f);
			}
		}
	}
	
	/**
	 * Creates a table, then queries the source database via 
	 * a supplied query via getQuery, then uses a supplied 
	 * row processor via getRowProcessor to copy the table. 
	 * @param tableName
	 * @throws DBException
	 */
	protected void createAndCopyTable(final String tableName, final SelectQuery query, final RowProcessor processor) throws DBException {
		createTable(tableName);
		
		source.doQuery(query, processor);
	}
	
	/**
	 * Get the query used for copying for this table. 
	 * Override this to trim down the result set. By 
	 * default, it does select * on the entire table.
	 * @param tableName
	 * @return
	 */
	protected SelectQuery getQuery(String tableName) {
		final SelectQuery sq = new SelectQuery();
		sq.select(new CanonicalColumnName(tableName, "*"));
		
		return sq;
	}
	
	/**
	 * Creates a copy processor.  Override this to do 
	 * explicit things if necessary.  For trimming down 
	 * results, you shoulld probably override getQuery 
	 * instead.
	 */
	protected RowProcessor getRowProcessor(final String tableName) throws DBException {
		return new CopyProcessor(tableName, source.getRow(tableName));
	}
	
	/**
	 * A helper method that recursively inserts a taxon 
	 * and all of its parents into an existing taxon table.
	 * @param taxon
	 * @param seen use this to ensure there are no duplicates 
	 * across your implementation
	 * @throws DBException
	 */
	protected void insertTaxa(Taxon taxon, HashSet<Integer> seen) throws DBException {
		if (taxon != null && !seen.contains(taxon.getId())) {
			SelectQuery query = new SelectQuery();
			query.select("taxon", "*");
			query.constrain(new QComparisonConstraint(
				new CanonicalColumnName("taxon", "id"), 
				QConstraint.CT_EQUALS, taxon.getId()
			));
			Row.Loader rl = new Row.Loader();
			source.doQuery(query, rl);
			
			Row sourceRow = rl.getRow();
			if (sourceRow != null) {
				Row targetRow = target.getRow("taxon");
				for (Column c : targetRow.getColumns())
					c.setObject(sourceRow.get(c.getLocalName()).getObject());
				
				InsertQuery insert = new InsertQuery();
				insert.setTable("taxon");
				insert.setRow(targetRow);
				
				write("Adding taxon %s to taxon table", taxon.getFullName());
				
				target.doUpdate(insert);
			}
			
			seen.add(taxon.getId());
			
			insertTaxa(taxon.getParent(), seen);
		}
	}
	
	protected void write(String template, Object... args) {
		write(String.format(template, args));
	}
	
	public class CopyProcessor extends RowProcessor {
		
		private final String table;
		private final Row templateRow;
		private final AtomicInteger count;
		
		public CopyProcessor(String table, Row template) {
			this.table = table;
			this.templateRow = template;
			this.count = new AtomicInteger(0);
		}
		
		public void process(Row sourceRow) {
			final InsertQuery q = new InsertQuery();
			try {
				Row targetRow = new Row(templateRow);
				if (targetRow != null) {
					for(Column c : targetRow.getColumns()){
						Column t = sourceRow.get(c.getLocalName());
						if(t!=null) c.setObject(t.getObject());
					}
					q.setTable(table);
					q.setRow(targetRow);
					target.doUpdate(q);
				} else
					write("No table named %s found", table);
			} catch (final Exception recorded) {
				try {
					write(q.getSQL(target.getDBSession()));
				} catch (Exception f) { }
				write(
						"  Exception: " + recorded.getClass().getName() + ": "
								+ recorded.getMessage());
			}
			if (count.incrementAndGet() % 1000 == 0) {
				write("  %s...", count.get());
			}
		}
	}	

}
