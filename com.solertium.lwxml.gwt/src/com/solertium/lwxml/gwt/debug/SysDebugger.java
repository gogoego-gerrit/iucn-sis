/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */

package com.solertium.lwxml.gwt.debug;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * SysDebugger.java
 *
 * Uses standard System.out.print statements to debug code. Useful to keep GWT
 * code clean and quickly turn debugging print statements on and off, without
 * being tied to an explicit logger implementation. Also useful for later
 * refactoring to use the logger of your choice.
 *
 * You have one logger -- the default instance. From there, you can create named
 * instances of the SysDebugger and store them with any string name, and
 * reference it later. This can be for your own abstraction of logging levels,
 * or to utilize log features.
 *
 * The levels in descending order are:
 * <ul>
 * <li>SEVERE (highest value)
 * <li>WARNING
 * <li>INFO
 * <li>CONFIG
 * <li>FINE
 * <li>FINER
 * <li>FINEST (lowest value)
 * </ul>
 *
 * In addition there is a level OFF that can be used to turn off logging, and a
 * level ALL that can be used to enable logging of all messages.
 * <p>
 *
 * @author carl.scott@solertium.com
 *
 */
public class SysDebugger {

	public static final int OFF = Integer.MAX_VALUE;
	public static final int SEVERE = 700;
	public static final int WARNING = 600;
	public static final int INFO = 500;
	public static final int CONFIG = 400;
	public static final int FINE = 300;
	public static final int FINER = 200;
	public static final int FINEST = 100;
	public static final int ALL = Integer.MIN_VALUE;

	private static SysDebugger defaultInstance;

	private HashMap<String, SysDebugger> instances = new HashMap<String, SysDebugger>();
	private String prepend;

	private boolean forcePrint = false;
	private int applicationLogLevel;

	private int logLevel;

	private ArrayList<WriterListener> listeners;

	/**
	 * Retrieves the static instance of the SysDebugger
	 *
	 * @return the debugger
	 */
	public static SysDebugger getInstance() {
		if (defaultInstance == null)
			defaultInstance = new SysDebugger();
		return defaultInstance;
	}

	/**
	 * Checks the default instance for a named instance of a SysDebugger. If
	 * found, the instance is returned. Otherwise, the default instance is
	 * returned.
	 *
	 * @param instanceName
	 *            the debugger's name
	 * @return the debugger in question, or the default debugger
	 */
	public static SysDebugger getNamedInstance(String instanceName) {
		SysDebugger instance = getInstance();
		if (instance.instances.containsKey(instanceName))
			return instance.instances.get(instanceName);
		else
			return instance;
	}

	public static void addListenerToAll(final WriterListener listener) {
		SysDebugger instance = getInstance();
		instance.addListener(listener);
		for (SysDebugger sys : instance.instances.values())
			sys.addListener(listener);
	}

	/**
	 * Helper method that applies an automatic setup routine to create the
	 * default debugger and adds two named instances, one which prepends '##' to
	 * any string to make it stand out, and a second which forces its output to
	 * print to screen, even if the application has debugging turned off.
	 *
	 * This can also be used as a template to write your own custom debugging
	 * mechanism.
	 *
	 * @param application
	 *            the debugging application to apply debugging to
	 */
	public static void autoSetup(DebuggingApplication application) {
		SysDebugger.getInstance().setApplication(application);

		SysDebugger prepend = new SysDebugger();
		prepend.setApplication(application);
		prepend.setPrepend("(Client) ## ");

		SysDebugger force = new SysDebugger();
		force.setApplication(application);
		force.setPrepend("(Client) ");
		force.setForcePrint(true);

		SysDebugger error = new SysDebugger();
		error.setApplication(application);
		error.setLogLevel(SEVERE);
		error.setPrepend("(Client) # Error Occured: ");

		SysDebugger.getInstance().addNamedInstance("##", prepend);
		SysDebugger.getInstance().addNamedInstance("force", force);
		SysDebugger.getInstance().addNamedInstance("error", error);
	}

	/**
	 * Creates a new SysDebugger with the INFO log level. You should use this
	 * only when instantiating an instance to add to the default instance's
	 * named instances mapping, though you aren't forced to do so.
	 *
	 */
	public SysDebugger() {
		this(INFO);
	}

	/**
	 * Creates a new SysDebugger with a particular log level.
	 *
	 * @param logLevel
	 *            the log level
	 */
	public SysDebugger(int logLevel) {
		this.logLevel = logLevel;
		this.prepend = "";

		instances = new HashMap<String, SysDebugger>();
		listeners = new ArrayList<WriterListener>();
	}

	public void addListener(final WriterListener listener) {
		listeners.add(listener);
	}

	/**
	 * Adds a named instance to the debugger, which can be referenced later by
	 * the instanceName defined by this method.
	 *
	 * @param instanceName
	 *            the name of the instance
	 * @param instance
	 *            the instance
	 */
	public void addNamedInstance(String instanceName, SysDebugger instance) {
		instances.put(instanceName, instance);
	}

	/**
	 * Prints a debugging message and new line to the screen, if debugging is
	 * turned on or forced.
	 *
	 * @param text
	 *            the text to print
	 */
	public void println(Object toPrint) {
		println(toPrint.toString());
	}

	/**
	 * Prints a debugging message and new line to the screen, if debugging is
	 * turned on or forced.
	 *
	 * @param text
	 *            the text to print
	 */
	public void println(String toPrint) {
		if (showDebug() || forcePrint)
			doWrite(toPrint, null);
	}

	public void println(String toPrint, Object... params) {
		if (showDebug() || forcePrint)
			doWrite(toPrint, params);
	}

	private void doWrite(String toPrint, Object[] params) {
		final String out = prepend + substitute(toPrint, params);
		System.out.println(out + "\r\n");
		for (WriterListener listener : listeners)
			listener.onWrite(out);
	}

	private String substitute(String text, Object[] params) {
		if (params == null || params.length == 0)
			return text;
		for (int i = 0; i < params.length; i++) {
			final Object p = params[i];
			text = text.replaceAll("\\{" + i + "}", safeRegexReplacement(p == null ? "" : p));
		}
		return text;
	}

	private String safeRegexReplacement(final Object replacement) {
		return replacement == null ? "null" : replacement.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$",
				"\\\\\\$");
	}

	/**
	 * Sets the application to the debugger. You must set this application. The
	 * debugging boolean is set based on what the debugging application's
	 * showDebug setting currently is. This should be the last called made.
	 *
	 * @param application
	 *            the debugging application
	 */
	public void setApplication(DebuggingApplication application) {
		applicationLogLevel = application.getLogLevel();

	}

	/**
	 * Sets force printing for the debugger. If true, it will print a debugging
	 * message regardless of if the application has debugging on or off.
	 * Otherwise, it will not.
	 *
	 * @param forcePrint
	 *            true to force printing, false otherwise
	 */
	public void setForcePrint(boolean forcePrint) {
		this.forcePrint = forcePrint;
	}

	/**
	 * Sets the level for this particular debugger.
	 *
	 * @param logLevel
	 */
	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

	/**
	 * Sets a string to prepend to every printed statement
	 *
	 * @param prepend
	 *            the string to prepend
	 */
	public void setPrepend(String prepend) {
		this.prepend = prepend;
	}

	public boolean showDebug() {
		return applicationLogLevel != OFF && logLevel >= applicationLogLevel;
	}

	public static interface WriterListener {
		public void onWrite(final String out);
	}
}
