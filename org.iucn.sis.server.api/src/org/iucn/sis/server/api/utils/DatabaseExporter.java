package org.iucn.sis.server.api.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.utils.export.GenericExporter;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;

public class DatabaseExporter extends GenericExporter {
	
	protected final Integer workingSetID;
	protected final List<String> copyLater;
	
	public DatabaseExporter(ExecutionContext source) {
		this(source, null);
	}

	public DatabaseExporter(ExecutionContext source, Integer workingSetID) {
		super(source);
		this.workingSetID = workingSetID;
		this.copyLater = new ArrayList<String>();
		copyLater.add("assessment");
		copyLater.add("taxon");
		copyLater.add("reference");
	}
	
	@Override
	protected void createAndCopyTable(String tableName, SelectQuery query, RowProcessor processor) throws DBException {
		if (!copyLater.contains(tableName.toLowerCase()))
			super.createAndCopyTable(tableName, query, processor);
	}
	
	protected void afterRun() throws DBException {
		final Session session;
		try {
			session = SIS.get().getManager().openSession();
		} catch (Exception e) {
			throw new DBException("Could not open SIS session.");
		}
		
		final WorkingSet workingSet = new WorkingSetIO(session).readWorkingSet(workingSetID);
		
		createTable("taxon");
		createTable("assessment");
		
		try {
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
					write("Writing assessment #%s", assessment.getId());
					insertAssessment(session, assessment);
				}
			}
		} finally {
			session.close();
		}
	}
	
	@Override
	protected SelectQuery getQuery(String tableName) {
		SelectQuery query = super.getQuery(tableName);
		if (workingSetID != null) {
			/*
			 * We only need our working set...
			 */
			if ("workingSet".equalsIgnoreCase(tableName)) {
				query.constrain(new CanonicalColumnName(tableName, "id"), 
					QConstraint.CT_EQUALS, workingSetID);
			}
			else if (tableName.toLowerCase().startsWith("working_set_"))
				query.constrain(new CanonicalColumnName(tableName, "working_setid"), 
					QConstraint.CT_EQUALS, workingSetID);
		}			
		return query;
	}
	
	protected void insertAssessment(Session session, Assessment assessment) throws DBException {
		SelectQuery query = new SelectQuery();
		query.select("assessment", "*");
		query.constrain(new QComparisonConstraint(
			new CanonicalColumnName("assessment", "id"),
			QConstraint.CT_EQUALS, assessment.getId()
		));
		
		Row.Loader rl = new Row.Loader();
		source.doQuery(query, rl);
		
		if (rl.getRow() != null)
			target.doUpdate(new InsertQuery("assessment", rl.getRow()));
	}

}
