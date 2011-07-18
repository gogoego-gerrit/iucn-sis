package org.iucn.sis.server.extensions.export.access.exported;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.NamingException;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.DynamicWriter;
import com.solertium.util.TrivialExceptionHandler;

public class GenericExporter extends DynamicWriter implements Runnable {
	
	private final ExecutionContext source;
	private final Integer workingSetID;
	
	private ExecutionContext target, template;
	
	private Session session;
	private WorkingSet workingSet;
	
	public GenericExporter(Session session, ExecutionContext source, Integer workingSetID) {
		super();
		this.source = source;
		this.workingSetID = workingSetID;
	}
	
	public void setTarget(String name) throws NamingException {
		SystemExecutionContext ec = new SystemExecutionContext(name);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		
		setTarget(ec);
	}
	
	public void setTarget(ExecutionContext target) {
		this.target = target;
	}
	
	public void setTemplate(String name) throws NamingException {
		SystemExecutionContext ec = new SystemExecutionContext(name);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		
		setTemplate(ec);
	}
	
	public void setTemplate(ExecutionContext template) {
		this.template = template;
	}
	
	public void run() {
		Date start = Calendar.getInstance().getTime();
		
		write("Export started at %s", start);
		try {
			session = SIS.get().getManager().openSession();
			workingSet = new WorkingSetIO(session).readWorkingSet(workingSetID);
			
			if (workingSet == null)
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Working set " + workingSet + " was not found");
			
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
	
	private void execute() throws Throwable {
		if (target == null) {
			write("Please specify a target.");
			return;
		}
		
		if (template != null) {
			try {
				initFromTemplate();
			} catch (DBException e) {
				write("Failed to initialize database: %s", e.getMessage());
				Debug.println(e);
				return;
			}
		}
		
		try {
			copyAssessmentData();
		} catch (DBException e) {
			write("Failed to copy assessment data: %s", e.getMessage());
			Debug.println(e);
			return;
		}
	}
	
	private void insertTaxa(Taxon taxon, HashSet<Integer> seen) throws DBException {
		if (taxon != null && !seen.contains(taxon.getId())) {
			SelectQuery query = new SelectQuery();
			query.select("taxon", "*");
			query.constrain(new QComparisonConstraint(
				new CanonicalColumnName("taxon", "id"), 
				QConstraint.CT_EQUALS, taxon.getId()
			));
			Row.Loader rl = new Row.Loader();
			source.doQuery(query, rl);
			
			Row row = rl.getRow();
			if (row != null) {
				InsertQuery insert = new InsertQuery();
				insert.setTable("taxon");
				insert.setRow(row);
				
				write("Adding taxon %s to taxon table", taxon.getFullName());
				
				target.doUpdate(insert);
			}
			
			seen.add(taxon.getId());
			
			insertTaxa(taxon.getParent(), seen);
		}
	}
	
	/**
	 * This function copies tables from the source database 
	 * (view), but only copies table which start with "export_".
	 * The contractual agreement here is that this table contains 
	 * an assessmentid column.
	 * @throws DBException
	 */
	private void copyAssessmentData() throws DBException {
		final Collection<String> allTables = source.getDBSession().listTables(source);
		final Collection<String> tables = new ArrayList<String>();
		
		//Create taxon table, will be populated later
		try {
			target.dropTable("taxon");
		} catch (DBException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		target.createTable("taxon", source.getRow("taxon"));
		
		//Create and populate other tables that don't have assessment data
		for (String table : allTables) {
			if (table.startsWith("export_"))
				tables.add(table);
			else if (!"taxon".equals(table)) {
				write("Copying table %s", table);
				createAndCopyTable(table, source, table);
			}
		}
		
		final AssessmentFilterHelper helper = new AssessmentFilterHelper(session, workingSet.getFilter());
		final int size = workingSet.getTaxon().size();
		final HashSet<Integer> seen = new HashSet<Integer>();
		
		int count = 0;
		for (Taxon taxon : workingSet.getTaxon()) {
			insertTaxa(taxon, seen);
			Collection<Assessment> assessments = helper.getAssessments(taxon.getId());
			write("Copying %s eligible assessments for %s, (%s/%s)", 
				assessments.size(), taxon.getFullName(), ++count, size);
			for (Assessment assessment : assessments) {
				for (String table : tables) {
					String friendly = table.substring("export_".length());
					write("Writing to %s", friendly);
					createAndCopyTable(friendly, source, table, new QComparisonConstraint(
						new CanonicalColumnName(table, "assessmentid"), 
						QConstraint.CT_EQUALS, assessment.getId()
					));
				}
			}
		}	
	}
	
	private void createAndCopyTable(final String targetTable, final ExecutionContext source, final String table) throws DBException {
		createAndCopyTable(targetTable, source, table, null);
	}
	
	private void createAndCopyTable(final String targetTable, final ExecutionContext source, final String sourceTable, final QConstraint constraint) throws DBException {
		try {
			target.dropTable(targetTable);
		} catch (DBException ignored) {
			TrivialExceptionHandler.ignore(this, ignored);
		}
		target.createTable(targetTable, source.getRow(sourceTable));
		
		final SelectQuery sq = new SelectQuery();
		sq.select(new CanonicalColumnName(sourceTable, "*"));
		if (constraint != null)
			sq.constrain(constraint);
		
		source.doQuery(sq, new CopyProcessor(targetTable));
	}
	
	private void initFromTemplate() throws DBException {
		for (String table : template.getDBSession().listTables(template))
			createAndCopyTable(table, template, table);
	}
	
	private void write(String template, Object... args) {
		write(String.format(template, args));
	}
	
	private class CopyProcessor extends RowProcessor {
		
		private final String table;
		private final AtomicInteger count;
		
		public CopyProcessor(String table) {
			this.table = table;
			this.count = new AtomicInteger(0);
		}
		
		public void process(Row sourceRow) {
			final InsertQuery q = new InsertQuery();
			try {
				Row targetRow = target.getRow(table);
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
