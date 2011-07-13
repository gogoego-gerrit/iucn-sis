package org.iucn.sis.server.extensions.export.access.exported;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.NamingException;

import org.hibernate.Session;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

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
	
	private final Session session;
	private final ExecutionContext source;
	private final WorkingSet workingSet;
	
	private ExecutionContext target, template;
	
	public GenericExporter(Session session, ExecutionContext source, WorkingSet workingSet) {
		super();
		this.session = session;
		this.source = source;
		this.workingSet = workingSet;
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
		if (target == null) {
			write("Please specify a target.");
			return;
		}
		
		if (template != null) {
			try {
				initFromTemplate();
			} catch (DBException e) {
				write("Failed to initialize database: %s", e.getMessage());
				return;
			}
		}
		
		try {
			copyAssessmentData();
		} catch (DBException e) {
			write("Failed to copy assessment data: %s", e.getMessage());
			return;
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
		final Collection<String> tables = source.getDBSession().listTables(source);
		
		final AssessmentFilterHelper helper = new AssessmentFilterHelper(session, workingSet.getFilter());
		final int size = workingSet.getTaxon().size();
		
		int count = 0;
		for (Taxon taxon : workingSet.getTaxon()) {
			write("Copying %s, (%s/%s)", taxon.getFullName(), ++count, size);
			for (Assessment assessment : helper.getAssessments(taxon.getId())) {
				for (String table : tables) {
					if (table.startsWith("export_")) {
						createAndCopyTable(source, table, new QComparisonConstraint(
							new CanonicalColumnName(table, "assessmentid"), 
							QConstraint.CT_EQUALS, assessment.getId()
						));
					}
				}
			}
		}	
	}
	
	private void createAndCopyTable(final ExecutionContext source, final String table) throws DBException {
		createAndCopyTable(source, table, null);
	}
	
	private void createAndCopyTable(final ExecutionContext source, final String table, final QConstraint constraint) throws DBException {
		try {
			target.dropTable(table);
		} catch (DBException ignored) {
			TrivialExceptionHandler.ignore(this, ignored);
		}
		target.createTable(table, source.getRow(table));
		
		final SelectQuery sq = new SelectQuery();
		sq.select(new CanonicalColumnName(table, "*"));
		if (constraint != null)
			sq.constrain(constraint);
		
		source.doQuery(sq, new CopyProcessor(table));
	}
	
	private void initFromTemplate() throws DBException {
		for (String table : template.getDBSession().listTables(template))
			createAndCopyTable(template, table);
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
				for(Column c : targetRow.getColumns()){
					Column t = sourceRow.get(c.getLocalName());
					if(t!=null) c.setObject(t.getObject());
				}
				q.setTable(table);
				q.setRow(targetRow);
				target.doUpdate(q);
			} catch (final Exception recorded) {
				write(q.getSQL(target.getDBSession()));
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
