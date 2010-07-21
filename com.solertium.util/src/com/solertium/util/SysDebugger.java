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

package com.solertium.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.w3c.dom.Document;

/**
 * SysDebugger.java
 * 
 * Uses standard System.out.print statements to debug code. Useful to keep
 * server-side code clean and quickly turn debugging print statements on and
 * off, without being tied to an explicit logger implementation. Also useful for
 * later refactoring to use the logger of your choice.
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
 * Set the property SYSDEBUG_SHOW to "true" if you want debugging, and set it to
 * false (or leave it unset) to not show debugging.
 * 
 * @author carl.scott@solertium.com
 * 
 */
public class SysDebugger extends Handler {

	public static final String SYSTEM_PROPERTY = "SYSDEBUG_LEVEL";

	public static final int OFF = Integer.MAX_VALUE;
	public static final int SEVERE = 1000;
	public static final int WARNING = 900;
	public static final int INFO = 800;
	public static final int CONFIG = 700;
	public static final int FINE = 500;
	public static final int FINER = 400;
	public static final int FINEST = 300;
	public static final int ALL = Integer.MIN_VALUE;

	private static SysDebugger defaultInstance;
	private static WriterListener defaultListener;
	
	/**
	 * @deprecated use SysDebugger.getInstance() instead and 
	 * create your own debugger!
	 */
	public static SysDebugger out = getDeprecatedInstance();

	private HashMap<String, SysDebugger> instances = new HashMap<String, SysDebugger>();
	private String prepend = "";

	private int debugLevel = OFF;
	private boolean forcePrint = false;
	private int logLevel = INFO;

	private OutputStream output;
	private Logger logger = null;
	
	private Map<Class<?>, PrintHandler<?>> printHandlers;
	
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
	
	private static SysDebugger getDeprecatedInstance() {
		SysDebugger deprecated = new SysDebugger();
		deprecated.setForcePrint(true);
		
		return deprecated;		
	}

	public static boolean isInit() {
		return defaultInstance != null;
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
			return (SysDebugger) instance.instances.get(instanceName);
		else
			return instance;
	}
	
	public static void setDefaultListener(WriterListener listener) {
		defaultListener = listener;
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
	 * Debugging is turned on here based on the system property SYSDEBUG_SHOW.
	 * If true, debugging is on. Otherwise, it's off.
	 */
	public static void autoSetup() {
		Integer debugInt = getSystemVariableLevel();
		int debugLvl;
		if (debugInt == null)
			debugLvl = OFF;
		else
			debugLvl = debugInt.intValue();

		SysDebugger.getInstance().setLogLevel(INFO);
		SysDebugger.getInstance().setSystemDebugLevel(debugLvl);

		SysDebugger prepend = new SysDebugger();
		prepend.setPrepend("## ");

		SysDebugger force = new SysDebugger();
		force.setForcePrint(true);

		SysDebugger.getInstance().addNamedInstance("##", prepend);
		SysDebugger.getInstance().addNamedInstance("force", force);
	}

	/**
	 * Fetches the system variable and returns it as in integer, or null if one
	 * was not specified.
	 * 
	 * Valid variable values are the level names as string or any integer.
	 * 
	 * @return the level as set in system properties
	 */
	public static Integer getSystemVariableLevel() {
		Integer level;
		String debugStr = System.getProperties().getProperty(SYSTEM_PROPERTY);
		if (debugStr == null)
			level = null;
		else {
			try {
				level = Integer.parseInt(debugStr);
			} catch (NumberFormatException e) {
				debugStr = debugStr.toUpperCase();
				if (debugStr.equals("OFF"))
					level = OFF;
				else if (debugStr.equals("ALL"))
					level = ALL;
				else if (debugStr.equals("SEVERE"))
					level = SEVERE;
				else if (debugStr.equals("WARNING"))
					level = WARNING;
				else if (debugStr.equals("INFO"))
					level = INFO;
				else if (debugStr.equals("CONFIG"))
					level = CONFIG;
				else if (debugStr.equals("FINE"))
					level = FINE;
				else if (debugStr.equals("FINER"))
					level = FINER;
				else if (debugStr.equals("FINEST"))
					level = FINEST;
				else
					level = OFF;
			}
		}
		return level;
	}

	/**
	 * Creates a new SysDebugger. You should use this only when instantiating an
	 * instance to add to the default instance's named instances mapping, though
	 * you aren't forced to do so.
	 */
	public SysDebugger() {
		this(INFO);
	}

	public SysDebugger(final int logLevel) {
		this(logLevel, System.out);
	}

	public SysDebugger(final int logLevel, final OutputStream output) {
		instances = new HashMap<String, SysDebugger>();
		printHandlers = new HashMap<Class<?>, PrintHandler<?>>();
		listeners = new ArrayList<WriterListener>();
		
		setLogLevel(logLevel);	
		setOutputStream(output);		
		addPrinterHandler(new PrintHandler<Document>() {
			public java.lang.Class<Document> getHandlerClass() {
				return Document.class;
			}
			public String getString(Document arg0) {
				return BaseDocumentUtils.impl.serializeDocumentToString(arg0, true, true);
			}
		});
		addPrinterHandler(new PrintHandler<Throwable>() {
			public String getString(Throwable e) {
				final StringBuilder out = new StringBuilder();
				out.append(e.toString() + "\r\n");
				for (StackTraceElement el : e.getStackTrace())
					out.append(el.toString() + "\r\n");
				int count = 0;
				Throwable t = e;
				while (count++ < 10 && (t = t.getCause()) != null) {
					out.append(t.toString() + "\r\n");
					for (StackTraceElement el : t.getStackTrace())
						out.append(el.toString() + "\r\n");
				}
				return out.toString();
			}
			public Class<Throwable> getHandlerClass() {
				return Throwable.class;
			}
		});
	}
	
	public void addListener(WriterListener listener) {
		listeners.add(listener);
	}

	/**
	 * Sets where log message get printed to
	 * 
	 * @param output
	 */
	public void setOutputStream(final OutputStream output) {
		if (output != null)
			this.output = output;
	}

	/**
	 * If you set a logger, this will act as a logger instead!
	 * 
	 * @param logger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
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
		if (instance == null)
			return;
		instance.setSystemDebugLevel(debugLevel);
		if (defaultListener != null)
			instance.addListener(defaultListener);
		instances.put(instanceName, instance);
	}
	
	public boolean isNamedInstance(String instanceName) {
		return instances.containsKey(instanceName);
	}
	
	@SuppressWarnings("unchecked")
	public void addPrinterHandler(PrintHandler handler) {
		printHandlers.put(handler.getHandlerClass(), handler);
	}

	/**
	 * Prints a debugging message and new line to the screen, if debugging is
	 * turned on or forced.
	 * 
	 * @param text
	 *            the text to print
	 */
	public void println(Object toPrint) {
		if (showDebug() || forcePrint)
			doWrite(toPrint == null ? "null" : toPrint.toString(), null);
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

	public void println(String toPrint, Class<?> clazz) {
		if (showDebug() || forcePrint)
			doWrite(toPrint, clazz);
	}

	public void println(String toPrint, Object... params) {
		println(toPrint, (Class<?>) null, params);
	}

	public void println(String toPrint, Class<?> clazz, Object... params) {
		if (showDebug() || forcePrint)
			doWrite(toPrint, clazz, params);
	}

	private void doWrite(String toPrint, Class<?> clazz) {
		doWrite(toPrint, clazz, null);
	}

	private void doWrite(String toPrint, Class<?> clazz, Object[] params) {
		try {
			String msg = (clazz == null ? "" : clazz.getName() + ": ") + prepend + substitute(toPrint, params) + "\r\n";
			if (logger == null) {
				output.write(msg.getBytes());
				output.flush();
			}
			else
				logger.log(Level.parse(logLevel + ""), msg);
			for (WriterListener listener : listeners)
				listener.onWrite(msg);
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}

	private String substitute(String text, Object[] params) {
		if (params == null || params.length == 0)
			return text;
		for (int i = 0; i < params.length; i++) {
			final Object p = params[i];
			text = text.replaceAll("\\{" + i + "}", safeRegexReplacement(p == null ? "" : getString(p)));
		}
		return text;
	}

	private String safeRegexReplacement(final Object replacement) {
		return replacement == null ? "null" : replacement.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$",
				"\\\\\\$");
	}

	protected String getString(final Object replacement) {
		if (replacement instanceof String)
			return (String) replacement;
		else 
			for (Map.Entry<Class<?>, PrintHandler<?>> entry : printHandlers.entrySet())
				if (entry.getKey().isAssignableFrom(replacement.getClass()))
					return entry.getValue().toString(replacement);
		return replacement.toString();
	}

	public void flush() {
		try {
			output.flush();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}

	public void close() throws SecurityException {
		try {
			output.close();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}

	public void publish(LogRecord record) {
		if (record != null && debugLevel != OFF && record.getLevel().intValue() > debugLevel)
			if (record.getMessage() == null)
				println("");
			else
				println(record.getMessage());
	}

	public void setSystemDebugLevel(int level) {
		this.debugLevel = level;
	}

	/**
	 * Sets force printing for the debugger. If true, it will print a debugging
	 * message regardless of if debugging is on or off. Otherwise, it will not.
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
		if (logLevel == OFF || logLevel == ALL)
			throw new IllegalArgumentException("ALL and OFF are not " +
				"valid debugger levels, try using a different level " +
				"via a SysDebugger constant, such as SysDebugger.INFO");
		
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

	/**
	 * Determines whether to show debugging messages. It should show if
	 * debugging is not off and the SysDebugger level is greater than 
	 * or equal to the application's setting.
	 * 
	 * @return true if it should show debug, false otherwise
	 */
	public boolean showDebug() {
		return (debugLevel != OFF && logLevel >= debugLevel);
	}
	
	public static abstract class PrintHandler<T> {
		public abstract Class<T> getHandlerClass();
		
		@SuppressWarnings("unchecked")
		private final String toString(Object object) {
			return getString((T)object);
		}
		
		public abstract String getString(T object);
	}
	
	public static interface WriterListener {
		public void onWrite(final String out);
	}

}
