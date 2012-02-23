package org.iucn.sis.server.extensions.offline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.application.SIS;

import com.solertium.db.ExecutionContext;
import com.solertium.util.DynamicWriter;
import com.solertium.util.TrivialExceptionHandler;

public class OfflineUpdater extends DynamicWriter implements Runnable {
			
	private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
	
	private enum UpdateMode {
		PRIMARY, SECONDARY, SQL, UNSET
	}
	
	private final ExecutionContext ec;
	private final File file;
	
	private boolean live;
	
	private File primaryFolder;
	private File secondaryFolder;
	
	public OfflineUpdater(ExecutionContext ec, File file) {
		super();
		this.ec = ec;
		this.file = file;
		this.live = false;
	}
	
	public void setLive(boolean live) {
		this.live = live;
	}
	
	public void run() {
		Date start = Calendar.getInstance().getTime();
		if (!isLive())
			write("=== Running in Test Mode ===");
		write("Update started at %s", start);
		
		final File workingDirectory;
		try {
			File existing = new File("instance.ini").getAbsoluteFile();
			workingDirectory = existing.getParentFile();
		} catch (Exception e) {
			write("Error trying to start update: %s", e.getMessage());
			return;
		}
		if (workingDirectory == null) {
			write("Unknown error trying to start update, could not find working directory. Stopping.");
			return;
		}
		
		this.primaryFolder = new File(workingDirectory, "plugins");
		if (!primaryFolder.exists() || !primaryFolder.isDirectory()) {
			write("No plugins folder found, invalid/incompatible application setup, stopping.");
			return;
		}
		
		File workspaceFolder = new File(GoGoEgo.getInitProperties().getProperty("GOGOEGO_VMROOT", "workspace"));
		this.secondaryFolder = new File(workspaceFolder, "plugins");
		if (!secondaryFolder.exists() || !secondaryFolder.isDirectory()) {
			write("No workspace plugins folder found, invalid/incompatible application setup, stopping.");
			return;
		}
		
		ZipInputStream zis;
		try {
			zis = new ZipInputStream(new FileInputStream(file));
		} catch (IOException e) {
			write("Failed to open update ZIP file, stopping.");
			return;
		}
		
		try {
			if (execute(zis))
				write("<b>IMPORTANT: A restart is required to " +
					"complete the update process.  Please shut down and re-start SIS.</b>");
		} catch (IOException e) {
			write("Error occurring during update: %s", e.getMessage());
		} finally {
			Date end = Calendar.getInstance().getTime();

			long time = end.getTime() - start.getTime();
			int secs = (int) (time / 1000);
			int mins = (int) (secs / 60);

			write("Updates completed at %s in %s minutes, %s seconds.", end, mins, secs);
			write("<br/><a href=\"../manager\">Click here to return to the Offline Manager.</a>");

			try {
				zis.close();
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
			
			close();
		}
	}
	
	private boolean execute(ZipInputStream zis) throws IOException {
		ZipEntry entry = null;
		int count = 0;
		boolean restart = false;
		UpdateMode mode = UpdateMode.UNSET;
		while ((entry = zis.getNextEntry()) != null) {
			if (entry.isDirectory()) {
				String name = entry.getName();
				if (name.startsWith("primary"))
					mode = UpdateMode.PRIMARY;
				else if (name.startsWith("secondary"))
					mode = UpdateMode.SECONDARY;
				else if (name.startsWith("sql"))
					mode = UpdateMode.SQL;
				else
					mode = UpdateMode.UNSET;
			}
			else {
				switch (mode) {
					case PRIMARY:
						applyPrimaryUpdates(zis, entry);
						count++;
						restart = true;
						break;
					case SECONDARY:
						applySecondaryUpdate(zis, entry);
						count++;
						break;
					case SQL:
						applySQLUpdate(zis, entry); 
						count++;
						break;
					default:
						write("No update mode specified for file: %s", entry.getName());
				}
			}
		}
		
		if (count > 0) {
			write("%s updates have been applied.", count);
			updateVersion();
		}
		else
			write("No updates were found.");
		
		return restart;
	}
	
	private void copy(InputStream is, OutputStream os) throws IOException {
		if (isLive()) {
			int len;
			byte[] buf = new byte[65536];
			while ((len = is.read(buf)) > 0) {
				os.write(buf, 0, len);
			}
			
			write(" + File copied successfully");
		}
		else {
			write(" # Test mode, file not copied");
		}
	}
	
	private void applyPrimaryUpdates(InputStream is, ZipEntry entry) {
		write("Updating primary plugin %s", entry.getName());
		applyPluginUpdate(is, entry, primaryFolder);
	}
	
	private void applySecondaryUpdate(InputStream is, ZipEntry entry) {
		write("Updating secondary plugin %s", entry.getName());
		applyPluginUpdate(is, entry, secondaryFolder);
	}
	
	private void applyPluginUpdate(InputStream source, ZipEntry entry, File parentFolder) {
		FileOutputStream target;
		try {
			target = new FileOutputStream(new File(parentFolder, getName(entry)));
		} catch (IOException e) {
			write("Could not open file for updating: %s", entry.getName());
			return;
		}
		
		try {
			copy(source, target);
		} catch (IOException e) {
			write("Could not copy file %s: %s", entry.getName(), e.getMessage());
		} finally {
			try {
				target.close();
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
	}
	
	private void applySQLUpdate(InputStream is, ZipEntry entry) {
		if (!entry.getName().endsWith("sql"))
			return;
		
		final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		
		try {
			StringBuilder in = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().equals(""))
					continue;
				
				in.append(line);
				if (line.endsWith(";")) {
					String query = in.toString();
					write("Running update: %s", query);
					try {
						if (isLive()) {
							ec.doUpdate(query);
							write(" + Update successful.");
						}
						else {
							write(" # Test mode, not running query.");
						}
					} catch (Exception e) {
						write(" - Failed to run update script: %s", e.getMessage());
					}
					in = new StringBuilder();
				}
			}
		} catch (IOException e) {
			write("I/O error reading %s: %s", entry.getName(), e.getMessage());
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
	}
	
	private String getName(ZipEntry entry) {
		return entry.getName().substring(entry.getName().indexOf('/'));
	}
	
	private void updateVersion() {
		if (isLive()) {
			String newVersion = fmt.format(Calendar.getInstance().getTime()); 
			SIS.get().getSettings(null).
				setProperty(OfflineSettings.VERSION, newVersion);
			
			try {
				SIS.get().saveSettings(null);
			} catch (IOException e) { 
				TrivialExceptionHandler.ignore(e, e);
			}
			write("Software version updated to %s", newVersion);
		}
		else
			write("# Software version not updated, test mode.");
		
	}
	
	public boolean isLive() {
		return live;
	}
	
	private void write(String template, Object... args) {
		write(String.format(template, args));
	}

}
