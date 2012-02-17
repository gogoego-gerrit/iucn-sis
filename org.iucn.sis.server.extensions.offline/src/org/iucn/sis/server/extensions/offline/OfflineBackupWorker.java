package org.iucn.sis.server.extensions.offline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload.FileItem;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.shared.api.models.OfflineMetadata;

import com.solertium.util.TrivialExceptionHandler;

public class OfflineBackupWorker {
	
	public static void restore(String path) throws Exception {
		File folder = new File(path);
		if (!folder.exists() || !folder.isDirectory())
			throw new Exception("The path given is not a directory or does not exist.");
		
		replace(folder);
	}
	
	private static void replace(File fromFolder) throws IOException {
		backup();
		
		delete();
		
		File toFolder = getSourceFolder();
		
		String dbName = null;
		for (File file : fromFolder.listFiles()) {
			if (file.getName().endsWith(".data.db"))
				dbName = file.getName();
			copyFile(file, toFolder, file.getName());
		}
		
		if (dbName != null) {
			File dbUriName = new File(toFolder, dbName.substring(0, dbName.indexOf(".data.db")));
		
			setDatabaseUri(dbUriName.getAbsolutePath(), true);
		}
	}
	
	
	public static void upload(FileItem item) throws Exception {
		File sourceFile = getSourceFolder();
		
		File uploadFolder = new File(sourceFile, "upload");
		uploadFolder.mkdir();
		
		ZipInputStream zis = new ZipInputStream(item.getInputStream());
		
		ZipEntry current = null;
		while ((current = zis.getNextEntry()) != null) {
			if (current.getName().contains("lock.db"))
				continue;
			
			File file = new File(uploadFolder, current.getName());
			
			FileOutputStream os = new FileOutputStream(file);
			final byte[] buf = new byte[65536];
			int i = 0;
			while ((i = zis.read(buf)) != -1)
				os.write(buf, 0, i);
			os.close();
			zis.closeEntry();
		}
		zis.close();

		replace(uploadFolder);
		
		for (File file : uploadFolder.listFiles())
			if (!file.delete()) file.delete();
		
		if (!uploadFolder.delete()) uploadFolder.delete();
	}
	
	public static OfflineMetadata get() {
		Properties init = SIS.get().getSettings(null);
		
		String dbUri = init.getProperty("dbsession.sis.uri");
		String dbLocation = removeJDBCPrefix(dbUri);
		String dbName = getDbNameFromUri(dbLocation);
		
		File file = new File(dbLocation+".data.db");
		
		if (!file.exists())
			return null;
		
		OfflineMetadata metadata = new OfflineMetadata();
		metadata.setName(dbName);
		metadata.setLocation(dbLocation);
		metadata.setLastModified(new Date(file.lastModified()));
		
		return metadata;
	}
	
	public static List<OfflineMetadata> listBackups() {
		File sourceFile = getSourceFolder();
		File[] fileList = sourceFile.listFiles();
		
		List<OfflineMetadata> list = new ArrayList<OfflineMetadata>();
		for (File file : fileList) {
			if (file.isDirectory()) {
				String[] split = file.getName().split("_");
				String time = split[split.length-1];
				String date = split[split.length-2];
				String full = date + "_" + time;
				String name = file.getName().substring(0, file.getName().length()-21);
				
				OfflineMetadata md = new OfflineMetadata();
				md.setName(name);
				md.setLocation(file.getAbsolutePath());
				try {
					md.setLastModified(new SimpleDateFormat("yyyyMMdd_hhmm").parse(full));
				} catch (ParseException e) {
					TrivialExceptionHandler.impossible(md, e);
				}
				
				list.add(md);
			}
		}
		
		return list;
	}
	
	private static boolean setDatabaseUri(String path, boolean refresh) {
		Properties settings = SIS.get().getSettings(null);
		settings.setProperty("dbsession.sis.uri", "jdbc:h2:file:" + path);
		
		try {
			SIS.get().saveSettings(null);
		} catch (Exception e) {
			return false;
		}
		
		if (refresh)
			SISPersistentManager.refresh();
		
		return true;
	}
	
	public static String backup() {
		SISPersistentManager.instance().shutdown();	
		
		try {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd_hhmm");
			
			String location = getSourceLocation();
			File destDir = new File(location + "_backup_" + 
				fmt.format(Calendar.getInstance().getTime()));
			destDir.mkdir();
			
			File sourceFile = getSourceFolder();
			File[] fileList = sourceFile.listFiles();
			
			for (int i = 0; i < fileList.length; i++) {				
				if (fileList[i].getName().contains(".lock") || fileList[i].getName().startsWith("none"))
					continue;
					
				if (fileList[i].isFile() && fileList[i].getName().endsWith(".db")) 
					copyFile(fileList[i].getAbsolutePath(), destDir.getAbsolutePath(), fileList[i].getName());			    
			} 
			
			return destDir.getAbsolutePath();
		} catch (Exception e) {
			return null;
		} finally {
			SISPersistentManager.refresh();
		}
	}
	
	public static void delete() {
		SISPersistentManager.instance().shutdown();
		
		File sourceFile = getSourceFolder();
		File[] fileList = sourceFile.listFiles();
		
		for (int i = 0; i < fileList.length; i++) {
			System.out.println("Looking to delete file " + fileList[i].getName());
			if (fileList[i].isFile() && fileList[i].getName().endsWith(".db") && 
					fileList[i].delete() && fileList[i].delete());
			//Wacky, I know, but it's on purpose...
		}
		
		File none = new File(sourceFile, "none");
		
		setDatabaseUri(none.getAbsolutePath(), false);
	}
	
	private static void copyFile(String sourcePath, String destFoldername, String destFilename) throws IOException {
		File sourceFile = new File(sourcePath);
		if (!sourceFile.exists())
		    return;
		
		File destFolder = new File(destFoldername);
		if (!destFolder.exists() || !destFolder.isDirectory())
			return;
		
		copyFile(sourceFile, destFolder, destFilename);
	}
	
	private static void copyFile(File sourceFile, File destFolder, String destFilename) throws IOException {
		if (destFilename.startsWith("none"))
			return;
		
		File destFile = new File(destFolder, destFilename);
		if (!destFile.exists())
		    destFile.createNewFile();
		
		FileChannel source = new FileInputStream(sourceFile).getChannel();
		FileChannel destination = new FileOutputStream(destFile).getChannel();
		
		if (destination != null && source != null)
		    destination.transferFrom(source, 0, source.size());
		
		if (source != null)
		    source.close();
		
		if (destination != null)
		    destination.close();
	}
	
	private static String getDbNameFromUri(String uri){		
		return new File(uri).getName();
	}
	
	private static String removeJDBCPrefix(String uri){		
		return uri.substring(uri.indexOf("file:")+5,uri.length());
	}
	
	private static String getSourceLocation() {
		Properties init = SIS.get().getSettings(null);
		
		return removeJDBCPrefix(init.getProperty("dbsession.sis.uri"));
	}
	
	private static File getSourceFolder() {
		String location = getSourceLocation();
		
		File tempfile = new File(location);
		
		//Can't just call get folder as property may just be a placeholder...
		String sourceDir = location.substring(0, location.indexOf(tempfile.getName())-1);
		
		return new File(sourceDir); 
	}

}
