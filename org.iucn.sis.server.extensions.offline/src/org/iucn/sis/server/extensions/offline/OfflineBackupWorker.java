package org.iucn.sis.server.extensions.offline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.SISPersistentManager;

public class OfflineBackupWorker {
	
	public static String backup() {
		SISPersistentManager.instance().shutdown();
		
		Properties init = SIS.get().getSettings(null);
		
		String dbUri = init.getProperty("dbsession.sis.uri");
		String location =  dbUri.substring(dbUri.indexOf("file:")+5,dbUri.length());	
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd_hhmm");
		try {
			File tempfile = new File(location);
			String sourceDir = location.substring(0, location.indexOf(tempfile.getName())-1);
			
			File destDir = new File(location + "_backup_" + 
				fmt.format(Calendar.getInstance().getTime()));
			destDir.mkdir();
			
			File sourceFile = new File(sourceDir);
			File[] fileList = sourceFile.listFiles();
			
			for (int i = 0; i < fileList.length; i++) {				
				if (fileList[i].getName().contains(".lock"))
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
		Properties init = SIS.get().getSettings(null);
		
		String dbUri = init.getProperty("dbsession.sis.uri");
		String location =  dbUri.substring(dbUri.indexOf("file:")+5,dbUri.length());
		
		File tempfile = new File(location);
		String sourceDir = location.substring(0, location.indexOf(tempfile.getName())-1);
		
		File sourceFile = new File(sourceDir);
		File[] fileList = sourceFile.listFiles();
		
		for (int i = 0; i < fileList.length; i++) {				
			if (fileList[i].getName().contains(".lock"))
				continue;
				
			if (fileList[i].isFile() && fileList[i].getName().endsWith(".db") && 
					fileList[i].delete() && fileList[i].delete());
			//Wacky, I know, but it's on purpose...
		}
	}
	
	private static void copyFile(String sourcePath, String destFolder, String destFilename) throws IOException {
		
		File sourceFile = new File(sourcePath);
		File destFile = new File(destFolder + File.separator + destFilename);
		
		if (!sourceFile.exists()) {
		    return;
		}
		if (!destFile.exists()) {
		    destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;
		source = new FileInputStream(sourceFile).getChannel();
		destination = new FileOutputStream(destFile).getChannel();
		if (destination != null && source != null) {
		    destination.transferFrom(source, 0, source.size());
		}
		if (source != null) {
		    source.close();
		}
		if (destination != null) {
		    destination.close();
		}
	
	}

}
