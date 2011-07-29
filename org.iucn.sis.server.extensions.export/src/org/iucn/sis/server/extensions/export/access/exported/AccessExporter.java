package org.iucn.sis.server.extensions.export.access.exported;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.utils.DatabaseExporter;
import org.iucn.sis.shared.api.models.Assessment;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.RowProcessor;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;

public class AccessExporter extends DatabaseExporter {
	
	private final List<String> toExport;

	public AccessExporter(ExecutionContext source, Integer workingSetID) {
		super(source, workingSetID);
		
		this.toExport = new ArrayList<String>();
	}
	
	@Override
	protected void createAndCopyTable(String tableName, SelectQuery query, RowProcessor processor) throws DBException {
		if (tableName.toLowerCase().startsWith("export_"))
			toExport.add(tableName);
		else
			super.createAndCopyTable(tableName, query, processor);
	}
	
	@Override
	protected RowProcessor getRowProcessor(String tableName) throws DBException {
		String name = tableName;
		if (name.startsWith("export_"))
			name = tableName.substring("export_".length());
		
		return super.getRowProcessor(name);
	}
	
	protected void insertAssessment(Session session, Assessment assessment) throws DBException {
		super.insertAssessment(session, assessment);
		
		/*
		 * Now copy over the tables to export that 
		 * contain assessment fields; we only want 
		 * those ones for this particular assessment...
		 * 
		 * TODO: eval if one monster query would be 
		 * faster or (potentially) a bunch of little 
		 * queries...
		 */
		for (String table : toExport) {
			final SelectQuery query = super.getQuery(table);
			query.constrain(new QComparisonConstraint(
				new CanonicalColumnName(table, "assessmentid"), 
				QConstraint.CT_EQUALS, assessment.getId()
			));
			
			createAndCopyTable(table, query, getRowProcessor(table));
		}
	}
	
}
