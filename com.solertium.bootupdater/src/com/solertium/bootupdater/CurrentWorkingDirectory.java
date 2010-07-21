package com.solertium.bootupdater;

import java.io.File;
import java.io.IOException;

public class CurrentWorkingDirectory {

	public static void main(String[] args) {
		File f = new File("here");
		String path = ".";
		try{
			path = f.getCanonicalPath();
			path = path.substring(0,path.lastIndexOf("here")-1);
		} catch (IOException iox) {
			path = ".";
		} catch (RuntimeException rx) {
			path = ".";
		} finally {
			System.out.println(path);
		}
	}
	
}
