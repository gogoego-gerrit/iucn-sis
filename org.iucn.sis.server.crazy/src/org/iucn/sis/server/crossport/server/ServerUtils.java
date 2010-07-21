package org.iucn.sis.server.crossport.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.solertium.util.SysDebugger;

/**
 * This utils class does a few helpful things with XML for SiS servers.
 * 
 * @author adam.schwartz
 */
public class ServerUtils {
	/**
	 * Tries to open the file given as the fileName argument and returns its
	 * contents as a String.
	 * 
	 * @param fileName
	 * @return String - file contents
	 */
	public static String getFileContentsAsString(String fileName) {
		try {
			String file = "";
			BufferedReader reader = new BufferedReader(
					new FileReader(ServerUtils.class.getResource(fileName).getPath()));
			String curLine = "";

			while (curLine != null) {
				file += curLine + "\r\n";
				curLine = reader.readLine();
			}

			return file;
		} catch (Exception e) {
			SysDebugger.getInstance().println("WARNING: Error reading file " + fileName + ". Returning empty string.");
			e.printStackTrace();
			return "";
		}
	}

	public static BufferedReader getFileReaderForFile(String fileName) {
		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(ServerUtils.class.getResource(fileName).getPath()));
			return reader;
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean writeStringToFile(String fileToWriteTo, String str) {
		try {
			BufferedWriter writer2 = new BufferedWriter(new PrintWriter(new FileWriter(new File(
					"src/org/iucn/sis/assess/server/hibernate/csv/" + fileToWriteTo))));
			writer2.write(str);
			writer2.close();
			return true;
		} catch (Exception f) {
			SysDebugger.getInstance().println("BAD: " + f.getMessage());
			f.printStackTrace();
			return false;
		}
	}

	public static boolean writeStringToFile(String root, String fileToWriteTo, String str) {
		try {
			BufferedWriter writer2 = new BufferedWriter(new PrintWriter(new FileWriter(new File(root + fileToWriteTo))));
			writer2.write(str);
			writer2.close();
			return true;
		} catch (Exception f) {
			SysDebugger.getInstance().println("BAD: " + f.getMessage());
			f.printStackTrace();
			return false;
		}
	}
}
