/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


/**
 * list resources available from the classpath
 * 
 * @author stoughto
 * 
 *         Modified to be cross-browser and have the ability to load contents,
 *         since there is no access to the actuai input streams once search
 *         operations have completed.
 * @author carl.scott
 * 
 */
public class ResourceList {

	private static final SysDebugger debug = SysDebugger.getNamedInstance("debug");
	private static final ResourceList instance = new ResourceList();

	public static ResourceList getInstance() {
		return instance;
	}

	/**
	 * for all elements of java.class.path get a Collection of resources Pattern
	 * pattern = Pattern.compile(".*"); gets all resources
	 * 
	 * @param pattern
	 *            the pattern to match
	 * @return the resources in the order they are found
	 */
	public Collection<String> getResources(Pattern pattern) {
		ArrayList<String> retval = new ArrayList<String>();
		String classPath = System.getProperty("java.class.path", ".");
		String[] classPathElements = classPath.split(getDelimeter());
		for (String element : classPathElements) {
			debug.println(element);
			retval.addAll(getResources(element, pattern, false));
		}
		return retval;
	}

	/**
	 * for all elements of java.class.path get a Collection of resource contents
	 * Pattern pattern = Pattern.compile(".*"); gets all resources
	 * 
	 * @param pattern
	 *            the pattern to match
	 * @return the resource contents in the order they are found
	 */
	public Collection<String> getResourceContents(Pattern pattern) {
		ArrayList<String> retval = new ArrayList<String>();
		String classPath = System.getProperty("java.class.path", ".");
		debug.println("Classpath is " + classPath);
		String[] classPathElements = classPath.split(getDelimeter());
		for (String element : classPathElements) {
			debug.println(element);
			retval.addAll(getResources(element, pattern, true));
		}
		return retval;
	}

	private String getDelimeter() {
		return File.separatorChar == '\\' ? ";" : ":";
	}

	private Collection<String> getResources(String element, Pattern pattern, boolean contents) {
		ArrayList<String> retval = new ArrayList<String>();
		File file = new File(element);
		if (file.isDirectory())
			retval.addAll(getResourcesFromDirectory(file, pattern, contents));
		else
			retval.addAll(getResourcesFromJarFile(file, pattern, contents));
		return retval;
	}

	public Collection<String> getResourcesFromJarFile(File file, Pattern pattern, boolean contents) {
		ArrayList<String> retval = new ArrayList<String>();
		ZipFile zf;
		try {
			zf = new ZipFile(file);
		} catch (ZipException e) {
			throw new Error(e);
		} catch (IOException e) {
			throw new Error(e);
		}
		Enumeration<? extends ZipEntry> e = zf.entries();
		while (e.hasMoreElements()) {
			ZipEntry ze = e.nextElement();
			String fileName = ze.getName();
			boolean accept = pattern.matcher(fileName).matches();
			if (accept)
				if (contents) {
					try {
						retval.add(parseContents(new InputStreamReader(zf.getInputStream(ze))));
					} catch (Exception r) {
						debug.println("Could not read JAR contents for " + fileName);
					}
				} else
					retval.add(fileName);
		}
		try {
			zf.close();
		} catch (IOException e1) {
		}
		return retval;
	}

	private String parseContents(Reader is) throws IOException {
		final BufferedReader reader = new BufferedReader(is);
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null)
			builder.append(line);
		is.close();
		return builder.toString();
	}

	private Collection<String> getResourcesFromDirectory(File directory, Pattern pattern, boolean contents) {
		ArrayList<String> retval = new ArrayList<String>();
		File[] fileList = directory.listFiles();
		for (File file : fileList) {
			if (file.isDirectory())
				retval.addAll(getResourcesFromDirectory(file, pattern, contents));
			else {
				try {
					String fileName = file.getCanonicalPath();
					boolean accept = pattern.matcher(fileName).matches();
					if (accept)
						if (contents)
							try {
								retval.add(parseContents(new FileReader(file)));
							} catch (Exception r) {
								debug.println("Could not read contents for " + fileName);
							}
						else
							retval.add(fileName);
				} catch (IOException e) {
					throw new Error(e);
				}
			}
		}
		return retval;
	}

}
