package org.iucn.sis.server.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * Class that takes a filename and outputs a zipped version of the file,
 * shamelessly ripped from the Java Developers Almanac 1.4 online.
 */
public class FileZipper {

	public static ZipOutputStream zipper(File inFile, File outFile) throws IOException {
		File[] filenames = new File[] { inFile };
		return zipper(filenames, outFile);
	}

	/**
	 * Outputs a zipped file.
	 * 
	 * @param filenames
	 *            - a String array of filenames that you would like zipped in a
	 *            file
	 * @param outFilename
	 *            - the zipped filename
	 * @throws IOException
	 */
	public static ZipOutputStream zipper(File[] filenames, File outFile) throws IOException {
		byte[] buf = new byte[1024];

		// Create the ZIP file
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));

		// Compress the files
		for (int i = 0; i < filenames.length; i++) {
			InputStream in = new FileInputStream(filenames[i]);

			try {
				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(filenames[i].getName()));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				// Complete the entry
				out.closeEntry();
			} catch (ZipException e) {
				e.printStackTrace();
				System.out.println("*** ERROR occurred with the entry " + filenames[i] + " while building zip file. Bypassing to allow operation to finish...");
			}

			in.close();
		}

		// Complete the ZIP file
		out.close();

		return out;
	}

	public static ZipOutputStream zipper(VFS vfs, String filename, String outFilename) throws IOException {
		String[] filenames = { filename };
		return zipper(vfs, filenames, outFilename);
	}

	/**
	 * Outputs a zipped file.
	 * 
	 * @param filenames
	 *            - a String array of filenames that you would like zipped in a
	 *            file
	 * @param outFilename
	 *            - the zipped filename
	 * @throws IOException
	 */
	public static ZipOutputStream zipper(VFS vfs, String[] filenames, String outFilename) throws IOException {
		// Create the ZIP file
		ZipOutputStream out = new ZipOutputStream(vfs.getOutputStream(outFilename));

		// Compress the files
		for (int i = 0; i < filenames.length; i++) {
			InputStream in = vfs.getInputStream(new VFSPath(filenames[i]));

			try {
				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(filenames[i]));

				// Transfer bytes from the file to the ZIP file
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				// Complete the entry
				out.closeEntry();
			} catch (ZipException e) {
				e.printStackTrace();
				System.out.println("*** ERROR occurred with the entry " + filenames[i] + " while building zip file. Bypassing to allow operation to finish...");
			}
			
			in.close();
		}

		// Complete the ZIP file
		out.close();

		return out;
	}

}
