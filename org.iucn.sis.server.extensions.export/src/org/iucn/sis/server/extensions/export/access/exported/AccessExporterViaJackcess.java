package org.iucn.sis.server.extensions.export.access.exported;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.Session;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;
import com.solertium.db.CBoolean;
import com.solertium.db.CDate;
import com.solertium.db.CDateTime;
import com.solertium.db.CDouble;
import com.solertium.db.CInteger;
import com.solertium.db.CLong;
import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.TrivialExceptionHandler;

public class AccessExporterViaJackcess extends AccessExporter {
	
	private final Database target;

	public AccessExporterViaJackcess(ExecutionContext source, Integer workingSetID,
			String location, String fileName) throws IOException {
		super(source, workingSetID, location, fileName);
		
		target = Database.open(new File(location, fileName), false, false);
	}

	protected void createTable(String table, String sourceTable) throws DBException {
		//TODO: drop existing table if necessary?
		try {
			write(" -- Creating table " + table);
			
			TableBuilder builder = new TableBuilder(table);
			for (com.solertium.db.Column col : source.getRow(sourceTable).getColumns()) {
				ColumnBuilder cb = new ColumnBuilder(col.getLocalName());
				cb.setType(getType(col));
				
				builder.addColumn(cb.toColumn());
			}
			
			builder.toTable(target);
		} catch (DBException e) {
			TrivialExceptionHandler.ignore(this, e);
		} catch (IOException e) {
			throw new DBException(e);
		}
	}
	
	@Override
	protected RowProcessor getRowProcessor(String tableName) throws DBException {
		String name = tableName;
		if (name.startsWith("export_"))
			name = tableName.substring("export_".length());
		
		return new JCopyProcessor(name);
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
				try {
					Table tbl = target.getTable("taxon");
					Object[] row = new Object[tbl.getColumnCount()];
					int index = 0;
					for (Column c : tbl.getColumns())
						row[index++] = sourceRow.get(c.getName()).getObject();
					
					write("Adding taxon %s to taxon table", taxon.getFullName());
					
					tbl.addRow(row);
				} catch (Exception e) {
					throw new DBException(e);
				}
			}
			
			seen.add(taxon.getId());
			
			insertTaxa(taxon.getParent(), seen);
		}
	}
	
	protected void insertAssessment(Session session, Assessment assessment) throws DBException {
		{
			SelectQuery query = new SelectQuery();
			query.select("assessment", "*");
			query.constrain(new QComparisonConstraint(
				new CanonicalColumnName("assessment", "id"),
				QConstraint.CT_EQUALS, assessment.getId()
			));
			
			Row.Loader rl = new Row.Loader();
			source.doQuery(query, rl);
			
			if (rl.getRow() != null) {
				try {
					Table tbl = target.getTable("assessment");
					Object[] row = new Object[tbl.getColumnCount()];
					int index = 0;
					for (Column c : tbl.getColumns()) 
						row[index++] = rl.getRow().get(c.getName()).getObject();
					
					tbl.addRow(row);
				} catch (Exception e) {
					throw new DBException(e);
				}
			}
		}
		
		/*
		 * Now copy over the tables to export that 
		 * contain assessment fields; we only want 
		 * those ones for this particular assessment...
		 * 
		 * TODO: eval if one monster query would be 
		 * faster or (potentially) a bunch of little 
		 * queries...
		 */
		for (String table : new ArrayList<String>(toExport)) {
			final SelectQuery query = super.getQuery(table);
			query.constrain(new QComparisonConstraint(
				new CanonicalColumnName(table, "assessmentid"), 
				QConstraint.CT_EQUALS, assessment.getId()
			));
			
			source.doQuery(query, getRowProcessor(table));
		}
	}
	
	private DataType getType(com.solertium.db.Column column) {
		if (column instanceof CBoolean)
			return DataType.BOOLEAN;
		else if (column instanceof CInteger)
			return DataType.LONG;
		else if (column instanceof CString)
			return DataType.MEMO;
		else if (column instanceof CDateTime)
			return DataType.SHORT_DATE_TIME;
		else if (column instanceof CDate)
			return DataType.SHORT_DATE_TIME;
		else if (column instanceof CLong)
			return DataType.FLOAT;
		else if (column instanceof CDouble)
			return DataType.DOUBLE;
		else {
			write("Undefined column %s of type %s, default to string", column.getLocalName(), column.getClass().getSimpleName());
			return DataType.TEXT;
		}
	}
	
	public class JCopyProcessor extends RowProcessor {
		
		private final String table;
		private final AtomicInteger count;
		
		public JCopyProcessor(String table) {
			this.table = table;
			this.count = new AtomicInteger(0);
		}
		
		public void process(Row sourceRow) {
			try {
				Table tbl = target.getTable(table);
				Object[] columns = new Object[tbl.getColumnCount()];
				StringBuilder insert = new StringBuilder();
				int index = 0;
				for(Column c : tbl.getColumns()){
					com.solertium.db.Column t = sourceRow.get(c.getName());
					if(t!=null) 
						columns[index] = (t.getObject());
					else
						columns[index] = null;
					index++;
					
					insert.append(c.getName() + "=" + t.getObject());
					insert.append("; ");
				}
				//write(insert.toString());
				tbl.addRow(columns);
			} catch (final Exception recorded) {
				recorded.printStackTrace();
				write("Error recording row: %s", recorded.getMessage());
			}
			if (count.incrementAndGet() % 1000 == 0) {
				write("  %s...", count.get());
			}
		}
		
	}
	
}
