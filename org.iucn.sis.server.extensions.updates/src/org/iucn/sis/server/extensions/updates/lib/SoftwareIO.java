package org.iucn.sis.server.extensions.updates.lib;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.solertium.util.portable.XMLWritingUtils;

public class SoftwareIO {
	
	/**
	 * Need to find the following information:
	 * 
	 *  - Primary Plug-in Updates (plugins)
	 *  - Secondary Plug-in Updates ({workspace}/plugins)
	 *  - SQL Updates ({updates})
	 * 
	 * FIXME: In the future, it would be nice if this could 
	 * work based on version numbers instead of modification 
	 * dates, but going with what we've got for now...
	 *  
	 * @param date get updates of modules modified after this 
	 * date (so, not including the given date).
	 */
	public SoftwareUpdates listUpdates(Date date) throws IOException {
		final File workingDirectory;
		try {
			File existing = new File("instance.ini").getAbsoluteFile();
			workingDirectory = existing.getParentFile();
		} catch (Exception e) {
			throw new IOException(e);
		}
		if (workingDirectory == null)
			throw new IOException();
		
		final SoftwareUpdates updates = new SoftwareUpdates(workingDirectory);
		
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.add(Calendar.DATE, 1);
		
		final Date baseline = calendar.getTime();
		
		final FileFilter filter = new FileFilter() {
			public boolean accept(File pathname) {
				Date date = new Date(pathname.lastModified());
				return !pathname.isDirectory() && !date.before(baseline);
			}
		};
		
		//Get primary updates...
		File primaryFolder = new File(workingDirectory, "plugins");
		if (primaryFolder.exists() && primaryFolder.isDirectory()) {
			for (File file : primaryFolder.listFiles(filter))
				updates.addPrimaryUpdate(file);
		}
		
		//Get secondary updates...
		File workspaceFolder = new File(GoGoEgo.getInitProperties().getProperty("GOGOEGO_VMROOT", "workspace"));
		File secondaryFolder = new File(workspaceFolder, "plugins");
		if (secondaryFolder.exists() && secondaryFolder.isDirectory())
			for (File file : secondaryFolder.listFiles(filter))
				updates.addSecondaryUpdate(file);
		
		//Get SQL updates
		File sqlFolder = new File(workspaceFolder, "updates");
		if (sqlFolder.exists() && sqlFolder.isDirectory())
			for (File file : sqlFolder.listFiles(filter))
				updates.addSQLUpdate(file);
		
		return updates;
	}
	
	public File zip(Date date) throws IOException, ResourceException {
		SoftwareUpdates updates = listUpdates(date);
		if (updates.getCount() == 0)
			throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, "No updates available.");
		
		File tmp = File.createTempFile("updates", "zip");
		
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmp));
		
		if (updates.hasPrimaryUpdates()) {
			zos.putNextEntry(new ZipEntry("primary/"));
			for (File file : updates.getPrimaryUpdates())
				addToZip(zos, "primary", file);
		}
		
		if (updates.hasSecondaryUpdates()) {
			zos.putNextEntry(new ZipEntry("secondary/"));
			List<File> list = updates.getSecondaryUpdates();
			Collections.sort(list, new FileComparator());
			String currentPlugin = null;
			for (File file : updates.getSecondaryUpdates()) {
				String prefix = file.getName().split("_")[0];
				if (prefix.equals(currentPlugin))
					continue;
				
				currentPlugin = prefix;
				addToZip(zos, "secondary", file);
			}
		}
		
		if (updates.hasSQLUpdates()) {
			zos.putNextEntry(new ZipEntry("sql/"));
			for (File file : updates.getSqlUpdates())
				addToZip(zos, "sql", file);
		}
		
		zos.close();
		
		return tmp;
	}
	
	private void addToZip(ZipOutputStream zos, String folder, File file) throws IOException {
		zos.putNextEntry(new ZipEntry(folder + "/" + file.getName()));
		
		InputStream is = new FileInputStream(file);
		int len;
		byte[] buf = new byte[65536];
		while ((len = is.read(buf)) > 0) {
			zos.write(buf, 0, len);
		}
		zos.closeEntry();
	}
	
	public static class SoftwareUpdates {
		
		private final List<File> primaryUpdates;
		private final List<File> secondaryUpdates;
		private final List<File> sqlUpdates;
		
		private final File base;
		
		private boolean restartRequired;
		
		public SoftwareUpdates(File base) {
			this.base = base;
			primaryUpdates = new ArrayList<File>();
			secondaryUpdates = new ArrayList<File>();
			sqlUpdates = new ArrayList<File>();
		}
		
		public boolean hasPrimaryUpdates() {
			return !primaryUpdates.isEmpty();
		}
		
		public boolean hasSecondaryUpdates() {
			return !secondaryUpdates.isEmpty();
		}
		
		public boolean hasSQLUpdates() {
			return !sqlUpdates.isEmpty();
		}
		
		public void addPrimaryUpdate(File path) {
			restartRequired = true;
			primaryUpdates.add(path);
		}
		
		public void addSecondaryUpdate(File path) {
			secondaryUpdates.add(path);
		}
		
		public void addSQLUpdate(File path) {
			sqlUpdates.add(path);
		}
		
		public List<File> getPrimaryUpdates() {
			return primaryUpdates;
		}
		
		public List<File> getSecondaryUpdates() {
			return secondaryUpdates;
		}
		
		public List<File> getSqlUpdates() {
			return sqlUpdates;
		}
		
		public boolean isRestartRequired() {
			return restartRequired;
		}
		
		public int getCount() {
			return primaryUpdates.size() + secondaryUpdates.size() + sqlUpdates.size();
		}
		
		public String toXML() {
			StringBuilder out = new StringBuilder();
			out.append("<updates count=\"" + getCount() + "\" restart=\"" + isRestartRequired() + "\">");
			out.append(XMLWritingUtils.writeCDATATag("base", base.getAbsolutePath()));
			for (File file : primaryUpdates)
				out.append(XMLWritingUtils.writeCDATATag("primary", file.getPath()));
			for (File file : secondaryUpdates)
				out.append(XMLWritingUtils.writeCDATATag("secondary", file.getPath()));
			for (File file : sqlUpdates)
				out.append(XMLWritingUtils.writeCDATATag("sql", file.getPath()));
			out.append("</updates>");
			
			return out.toString();
		}
		
	}

	private static class FileComparator implements Comparator<File> {
		
		@Override
		public int compare(File o1, File o2) {
			return Long.valueOf(o2.lastModified()).compareTo(Long.valueOf(o1.lastModified()));
		}
		
	}
	
}
