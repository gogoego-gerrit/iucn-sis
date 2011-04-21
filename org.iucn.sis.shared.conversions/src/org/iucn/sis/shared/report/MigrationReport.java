package org.iucn.sis.shared.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.models.Assessment;

import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

public class MigrationReport {
	
	private final List<String> warnings;
	private final List<String> errors;
	
	public MigrationReport() {
		this.warnings = new ArrayList<String>();
		this.errors = new ArrayList<String>();
	}
	
	public boolean isMigrationSuccessful() {
		return warnings.isEmpty() && errors.isEmpty();
	}
	
	public void addWarning(String warning) {
		warnings.add(warning);
	}
	
	public void addError(String error) {
		errors.add(error);
	}
	
	public void save(Assessment assessment, VFS vfs) throws IOException {
		String striped = FilenameStriper.getIDAsStripedPath(Integer.toString(assessment.getId()));
		VFSPath uri = new VFSPath("/migration/" + assessment.getTaxon().getId() + "/" + 
				striped + ".html");
		if (!vfs.exists(uri.getCollection()))
			vfs.makeCollections(uri.getCollection());
		
		final BufferedWriter writer = new BufferedWriter(vfs.getWriter(uri));
		try {
			writer.write(toXHTML(assessment));
		} catch (IOException e) {
			throw e;
		} finally {
			writer.close();
		}
	}
			 
	
	public String toXHTML(Assessment assessment) {
		final StringBuilder out = new StringBuilder();
		out.append("<html>");
		out.append("<head>");
		out.append(String.format("<title>Migration Report for %s</title>", assessment.getDisplayText()));
		out.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/apps/org.iucn.sis.server.extensions.migration/2.0/reports/styles.css\" />");
		out.append("</head>");
		out.append("<body>");
		out.append(String.format("<h1>SIS 2.0 Migration Report for %s</h1>", assessment.getDisplayText()));
		if (!warnings.isEmpty())
			appendXHTMLList(out, warnings, "Warnings");
		if (!errors.isEmpty())
			appendXHTMLList(out, errors, "Errors");
		out.append("</body>");
		out.append("</html>");
		
		return out.toString();
	}
	
	private void appendXHTMLList(StringBuilder out, List<String> list, String heading) {
		out.append("<h2>" + heading + "</h2>");
		out.append("<ul>");
		for (String value : list)
			out.append("<li>" + value + "</li>");
		out.append("</ul>");
	}

}
