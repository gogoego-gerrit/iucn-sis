package com.solertium.bootupdater;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Updates files prior to application start, then starts
 * the application with a designated command line.
 * 
 * java -jar bootupdater.jar
 *
 * Supply bootupdater.properties with:
 * COMMAND=-Xmx128m -jar application.jar
 * 
 * If there is an Application directory, all operations are
 * done relative to that directory.
 * 
 * "updates" is the default update directory.  Any files in
 * "updates" will overwrite current ones.
 * 
 * @param args
 */
public class BootUpdater {

	public static void main(String[] args) throws Exception {
		File myBinary = CurrentBinary.getDirectory(new BootUpdater());
		File applicationDir = new File(myBinary,"Application");
		if(applicationDir.exists()){
			System.out.println("Running application in "+applicationDir.getPath());
			myBinary = applicationDir;
		}
		File propfile = new File(myBinary,"bootupdater.properties");
		Properties props = new Properties();
		if(propfile.exists())
			props.load(new FileInputStream(propfile));
		String command = props.getProperty("COMMAND");
		if(command==null) command = "-Xmx128m -jar application.jar";
		File updatef = new File(myBinary,"updates");
		String jhome = System.getProperties().getProperty("java.home");
		String cmd = "java";
		if(jhome!=null){
			File jbindir = new File(jhome,"bin");
			File javaw = new File(jbindir,"javaw");
			File java = new File(jbindir,"java");
			if(javaw.exists()) cmd = javaw.getCanonicalPath();
			else if(java.exists()) cmd = java.getCanonicalPath();
		}
		String execme = cmd+" "+command;
		
		int exitcode = 42;
		
		while(exitcode==42){ // exit code 42 means to restart; normal or
				// abnormal exit (0, 1, -1) means to stop
			if(!updatef.exists()) updatef.mkdirs();
			copyUpdates(updatef,myBinary);
			System.out.println("Starting: "+execme);
			ProcessBuilder pb = new ProcessBuilder(execme.split("\\s"));
			pb.directory(myBinary);
			pb.redirectErrorStream(true);
			final Process p = pb.start();
			// Spawn thread to read output of spawned program
	        new Thread() {
				public void run() {
					// hook into output from spawned program
					final InputStream is = p.getInputStream();
					final InputStreamReader isr = new InputStreamReader(is);
					final BufferedReader br = new BufferedReader(isr, 100);
					String line;
					try {
						try {
							while ((line = br.readLine()) != null) {
								System.out.println(line);
							}
						} catch (EOFException e) {
						}
						br.close();
					} catch (IOException e) {
						System.err.println("problem reading child output"
								+ e.getMessage());
					}
					// returning from run kills the thread.
				}
			}.start();
			
	        try {
				p.waitFor();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
	
			p.getInputStream().close();
	        p.getOutputStream().close();
	        p.getErrorStream().close();
	        
	        exitcode = p.exitValue();
		}
    }
	
	private static void copyUpdates(File src, File target) {
		try{
			File[] files = src.listFiles();
			for(File file : files){
				if("updates".equals(file.getName())) continue;
				if(file.isDirectory()){
					copyUpdates(file,new File(target,file.getName()));
				} else {
					target.mkdirs();
					File tfile = new File(target,file.getName());
					FileInputStream srcs = new FileInputStream(file);
					FileOutputStream tgts = new FileOutputStream(tfile);
					System.out.println("UPDATE: "+file.getPath()+" -> "+tfile.getPath());
					copyStream(srcs,tgts);
					srcs.close();
					tgts.close();
					file.delete();
				}
			}
			src.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static final int BUFFER_SIZE = 65536;

	private static void copyStream(final InputStream is, final OutputStream os)
			throws IOException {
		final byte[] buf = new byte[BUFFER_SIZE];
		int i = 0;
		while ((i = is.read(buf)) != -1)
			os.write(buf, 0, i);
	}
	
}
