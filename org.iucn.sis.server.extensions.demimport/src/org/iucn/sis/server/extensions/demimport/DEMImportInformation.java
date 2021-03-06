package org.iucn.sis.server.extensions.demimport;

import java.io.Writer;
import java.util.Date;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.FormattedDate;
import org.iucn.sis.server.api.utils.XMLUtils;
import org.iucn.sis.shared.api.debug.Debug;

import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

public class DEMImportInformation {
	static String logURL = "/logs/DEMImport.log";
	static String htmlLogURL = "/logs/DEMImportLog.html";
	static String verboseLogBaseURL = "/logs/verbose/";

	public static void addToQueue(DEMImportInformation info, VFS vfs) {
		Writer writer = null;
		vfs = SIS.get().getVFS();
		try {
			Debug.println(info.toString());
			
			String out = DocumentUtils.getVFSFileAsString(logURL, vfs);
			writer = vfs.getWriter(new VFSPath(logURL));
			writer.append(out);
			writer.close();
		} catch (Exception e) {
			Debug.println("DEM Import Logging Error: {0}", e);
			Debug.println("Failed to write to DEMImport log: ");
			Debug.println(info.toUnformattedHTML());

			try {
				writer.close();
			} catch (Exception ignored) {
			}
		}

		try {
			writer = vfs.getWriter(new VFSPath(info.getVerboseLogURL()));
			writer.append("<html><head><meta http-equiv=\"Content-Type\" " + "content=\"text/html; charset=UTF-8\">"
					+ "<style type=\"text/css\"> td {font-size: 10px} th {font-size: 12px}" + "</style></head><body>"
					+ info.getVerboseLog() + "</body></html>");

			writer.close();
		} catch (Exception e) {
			Debug.println("DEM Import Logging Error: {0}", e);
			Debug.println("Failed to write to verbose DEMImport log: ");
			Debug.println(info.getVerboseLog());

			try {
				writer.close();
			} catch (Exception ignored) {
			}
		}
		try {
			String out = info.toUnformattedHTML() + "<br><hr>" + DocumentUtils.getVFSFileAsString(htmlLogURL, vfs);
			writer = vfs.getWriter(new VFSPath(htmlLogURL));
			writer.write(out);
			writer.close();
		} catch (Exception e) {
			Debug.println("DEM Import Logging Error: {0}", e);
			Debug.println("Failed to write to verbose DEMImport log: ");
			Debug.println(info.getVerboseLog());

			try {
				writer.close();
			} catch (Exception ignored) {
			}
		}
	}

	private Date timestamp;
	private boolean failure;
	private String message;
	private String fileName;
	private String importer;
	private String verboseLog;

	public DEMImportInformation(Date timestamp, boolean failure, String message, String fileName, String importer,
			String verboseLog) {
		super();
		this.timestamp = timestamp;
		this.failure = failure;
		this.message = message;
		this.fileName = fileName;
		this.importer = importer;
		this.verboseLog = verboseLog;
	}

	public String getFileName() {
		return fileName;
	}

	public String getImporter() {
		return importer;
	}

	public String getMessage() {
		return message;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getVerboseLog() {
		return verboseLog;
	}

	public String getVerboseLogURL() {
		return verboseLogBaseURL + getTimestamp().getTime() + ".html";
	}

	public boolean isFailure() {
		return failure;
	}

	public void setFailure(boolean failure) {
		this.failure = failure;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setImporter(String importer) {
		this.importer = importer;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void setVerboseLog(String verboseLog) {
		this.verboseLog = verboseLog;
	}

	@Override
	public String toString() {
		String ret = "";
		ret += "\nDate of import: " + FormattedDate.impl.getDateWithTime(getTimestamp());
		ret += "\nFile imported: " + getFileName();
		ret += "\nUser: " + getImporter();
		ret += "\nImport success: " + isFailure();
		ret += "\nStatus log: " + getMessage().replaceAll("<br>", "\n");
		ret += "\nVerbose status log URL: /apps/org.iucn.sis.server/raw" + getVerboseLogURL();
		ret += "\n-------------------------------------";

		return ret;
	}

	public String toUnformattedHTML() {
		String ret = "";
		ret += "<br>Date of import: " + FormattedDate.impl.getDateWithTime(getTimestamp());
		ret += "<br>File imported: " + getFileName();
		ret += "<br>User: " + getImporter();
		ret += "<br>Import success: " + isFailure();
		ret += "<br>Status log: " + getMessage();
		ret += "<br>View <a target=\"_blank\" href=\"/raw" + getVerboseLogURL() + "\">verbose status log</a>";

		return ret;
	}

	public String toXML() {
		String ret = "<importInfo>";
		ret += "<date>" + FormattedDate.impl.getDateWithTime(getTimestamp()) + "</date>";
		ret += "<fileName>" + getFileName() + "</fileName>";
		ret += "<user>" + getImporter() + "</user>";
		ret += "<success>" + isFailure() + "</success>";
		ret += "<message>" + XMLUtils.clean(getMessage()) + "</message>";
		ret += "</importInfo>";

		return ret;
	}
}
