package org.iucn.sis.server.extensions.export.access.exported;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

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
	
	private static final String ACCESS_DB_TEMPLATE = "WSAccessExportTemplate2002.mdb";
	
	public static InputStream getTemplate() {
		return AccessExporter.class.getResourceAsStream(ACCESS_DB_TEMPLATE);
	}
	
	protected final List<String> toExport;
	private final String location;
	private final String fileName;
	
	private boolean inAfterRun;

	public AccessExporter(ExecutionContext source, Integer workingSetID, String location, String fileName) {
		super(source, workingSetID);
		
		this.location = location;
		this.fileName = fileName;
		this.toExport = new ArrayList<String>();
		this.inAfterRun = false;
	}
	
	@Override
	protected void afterRun() throws DBException {
		inAfterRun = true;
		
		super.afterRun();
		
		if (location == null) {
			write("Your database has been exported successfully.");
			return;
		};
		
		write("Write complete, zipping results...");
		
		try {
			write("--- Complete ---");
			write("You can now download your working set.");
			write("<a target=\"blank\" href=\"/apps/org.iucn.sis.server.extensions.export/downloads/%s\">Click here to download</a>", zip());
		} catch (Exception e) {
			write("Failed to zip database");
		}
		
		inAfterRun = false;
	}
	
	private String zip() throws Exception {
		File folder = new File(location);
		File tmp = new File(folder, fileName + ".zip");
		
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)));
		for (File file : folder.listFiles()) {
			//Don't zip yourself...
			if (file.getName().equals(tmp.getName()))
				continue;
			try {
				InputStream in = new BufferedInputStream(new FileInputStream(file));
				zos.putNextEntry(new ZipEntry(file.getName()));
				
				// Transfer bytes from the file to the ZIP file
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					zos.write(buf, 0, len);
				}
				
				zos.closeEntry();
			} catch (ZipException e) {
				write("Failed to write %s to zip file: %s", file.getName(), e.getMessage());
			}
		}
		
		zos.close();
		
		return tmp.getName();
	}
	
	@Override
	protected void createAndCopyTable(String tableName, SelectQuery query, RowProcessor processor) throws DBException {
		if (!inAfterRun && tableName.toLowerCase().startsWith("export_")) {
			toExport.add(tableName);
			createTable(tableName);
		}
		else {
			String name = tableName;
			if (name.startsWith("export_"))
				name = tableName.substring("export_".length());
			super.createAndCopyTable(name, query, processor);
		}
	}
	
	@Override
	protected void createTable(String table) throws DBException {
		String targetTable = table;
		if (targetTable.startsWith("export_"))
			targetTable = table.substring("export_".length());
		
		createTable(targetTable, table);
	}
	
	@Override
	protected RowProcessor getRowProcessor(String tableName) throws DBException {
		String name = tableName;
		if (name.startsWith("export_"))
			name = tableName.substring("export_".length());
		
		return new CopyProcessor(name, source.getRow(tableName));
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
		for (String table : new ArrayList<String>(toExport)) {
			final SelectQuery query = super.getQuery(table);
			query.constrain(new QComparisonConstraint(
				new CanonicalColumnName(table, "assessmentid"), 
				QConstraint.CT_EQUALS, assessment.getId()
			));
			
			source.doQuery(query, getRowProcessor(table));
		}
	}
	
}
