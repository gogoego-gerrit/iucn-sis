package com.solertium.bootupdater;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;

public class CurrentBinary {

	public static void main(final String[] args) {
		String path = ".";
		try{
			final CurrentBinary cb = new CurrentBinary();
			path = CurrentBinary.getDirectory(cb).toString();
		} catch (RuntimeException rx) {
			path = ".";
		} finally {
			System.out.println(path);
		}
	}
	
	public static File getDirectory(Object cb) {
		final Class<?> qc = cb.getClass();
		final CodeSource source = qc.getProtectionDomain().getCodeSource();
		final URL location = source.getLocation();
		try{
			File f = new File(location.toURI());
			if(f.isFile()) return f.getParentFile();
			return f;
		} catch (URISyntaxException us) {
			throw new RuntimeException("The base URI for the application could not be resolved");
		}
	}

}
