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
package com.solertium.gogoego.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.gogoego.api.debugging.GoGoDebugger;
import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Application;

import com.solertium.util.SysDebugger;
import com.solertium.util.TrivialExceptionHandler;

/**
 * GoGoDebug.java
 * 
 * Set up debugging based on GoGoEgo's specific use cases.  Handles 
 * site-specific debugging with a hook for system-level debugging.
 * 
 * This should be used in favor of GoGoEgo.debug iff you want to use 
 * the system() level debugger.   This is purposely not available to 
 * the GoGoEgo object, explicitly available from within this project.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GoGoDebug {
	
	private static boolean isInit = false;
	
	public static void init() {
		SysDebugger.getInstance().setLogLevel(SysDebugger.FINEST);
		SysDebugger.getInstance().setSystemDebugLevel(SysDebugger.ALL);
		
		GoGoDebuggingImpl systemWide = new GoGoDebuggingImpl();
		systemWide.setForcePrint(true);
		systemWide.setPrepend("System: ");
		
		SysDebugger.getInstance().addNamedInstance("%system%", systemWide);
		
		if ("true".equals(System.getProperty("HOSTED_MODE", "false")))
			SysDebugger.setDefaultListener(new SysDebugger.WriterListener() {
				public void onWrite(String out) {
					System.out.print(out);
				}
			});
		
		isInit = true;
	}
	
	public static SizeAndVersion getSize(String vmroot, String siteID, String property, long maxSize) {
		int maxVersion = getVersion(vmroot, siteID, property, false);
		long size = 0;
		int i;
		for (i = 0; i <= maxVersion && size < maxSize; i++) {
			final File file = new File(vmroot + "/_logs/" + siteID + "[" + i + "]." + property);
			if (file.exists())
				size += file.length();
		}
		return new SizeAndVersion(size, i-1);
	}
	
	public static int getVersion(String vmroot, String siteID, String property, boolean increment) {
		final File file = new File(vmroot + "/_logs/" + siteID + ".version");
		final Properties properties = new Properties();
		
		int version = -1;
		if (file.exists()) {
			final BufferedInputStream in;
			try {
				properties.load(in = new BufferedInputStream(new FileInputStream(file)));
			} catch (IOException e) {
				return version;
			}
			
			try {
				version = Integer.parseInt(properties.getProperty(property));
			} catch (NullPointerException e) {
				TrivialExceptionHandler.ignore(properties, e);
			} catch (NumberFormatException e) {
				TrivialExceptionHandler.ignore(properties, e);
			}
			
			try {
				in.close();
			} catch (IOException e) {
				TrivialExceptionHandler.ignore(in, e);
			}
		}
		else
			file.getParentFile().mkdirs();
		
		if (increment) {
			//Size is certainly 0, we can increment
			if (version == -1)
				properties.setProperty(property, Integer.toString(++version));
			else {
				/*
				 * Sensible default for logs is 10 gig
				 * 
				 * Feel free to change any of the numbers to 
				 * meet a better sensible default (most likely 
				 * the 10 on the end ;) ... )
				 */
				long maxSize = 1024 * 1000 * 1000 * 10;
				try {
					maxSize = Long.parseLong(GoGoEgo.getInitProperties().getProperty("com.solertium.gogoego.server.logging.maxsize"));
				} catch (Exception e) {
					TrivialExceptionHandler.ignore(e, e);
				}
				
				final SizeAndVersion sizeInfo = getSize(vmroot, siteID, property, maxSize);
				if (sizeInfo.size < maxSize)
					properties.setProperty(property, Integer.toString(++version));
				else
					properties.setProperty(property, Integer.toString(sizeInfo.version));
			}
			
			BufferedOutputStream out = null;
			try {
				properties.store(out = new BufferedOutputStream(new FileOutputStream(file)), null);
			} catch (IOException e) {
				TrivialExceptionHandler.ignore(file, e);
			} finally {
				try {
					if (out != null) 
						out.close();
				} catch (IOException f) {
					TrivialExceptionHandler.ignore(file, f);
				}
			}
		}
		
		return version;
	}
	
	public static void init(String siteID) {
		if (!isInit)
			init();
		
		final String vmroot = GoGoEgo.getInitProperties().getProperty("GOGOEGO_VMROOT");
		final String debugPath = vmroot + "/_logs/" + siteID + 
			"[" + getVersion(vmroot, siteID, "debug", true) + "].debug";
		final String logPath = vmroot + "/_logs/" + siteID + 
			"[" + getVersion(vmroot, siteID, "log", true) + "].log";

		OutputStream debugOS;
		OutputStream logOS;
		try {
			debugOS = new FileOutputStream(new File(debugPath));
			logOS = new FileOutputStream(new File(logPath));
			system().println("Made debug files at {0}", debugPath);
			system().println("Made log files at {0}", logPath);
		} catch (IOException e) {
			e.printStackTrace();
			debugOS = System.out;
			logOS = System.out;
		}
		
		GoGoDebuggingImpl prepend = new GoGoDebuggingImpl(SysDebugger.INFO, debugOS);
		prepend.setPrepend("## ");

		GoGoDebuggingImpl force = new GoGoDebuggingImpl(SysDebugger.INFO, debugOS);
		force.setForcePrint(true);
				
		/*
		 * These should be used throughout the application as the output of
		 * these debuggers can be centrally managed in this class. If you need
		 * to get your own, great, but you'll have to manage it yourself.
		 */
		SysDebugger.getInstance().addNamedInstance(siteID + "##", prepend);
		SysDebugger.getInstance().addNamedInstance(siteID + "force", force);
		SysDebugger.getInstance().addNamedInstance(siteID + "fine", new GoGoDebuggingImpl(SysDebugger.FINE, debugOS));
		SysDebugger.getInstance().addNamedInstance(siteID + "config", new GoGoDebuggingImpl(SysDebugger.CONFIG, debugOS));
		SysDebugger.getInstance().addNamedInstance(siteID + "debug", new GoGoDebuggingImpl(SysDebugger.FINEST, debugOS));
		SysDebugger.getInstance().addNamedInstance(siteID + "warning", new GoGoDebuggingImpl(SysDebugger.WARNING, debugOS));
		SysDebugger.getInstance().addNamedInstance(siteID + "error", new GoGoDebuggingImpl(SysDebugger.SEVERE, debugOS));
		SysDebugger.getInstance().addNamedInstance(siteID + "file", new GoGoDebuggingImpl(SysDebugger.INFO, debugOS));
		SysDebugger.getInstance().addNamedInstance(siteID, new GoGoDebuggingImpl(SysDebugger.INFO, debugOS));
		
		/*
		 * Logger OS configuration
		 */
		SysDebugger.getInstance().addNamedInstance(siteID + "log", new GoGoDebuggingImpl(SysDebugger.INFO, logOS));
		
	}
	
	public static GoGoDebugger get(String debuggerName) {
		try {
			return get(debuggerName, ServerApplication.getFromContext(Application.getCurrent().getContext()).getInstanceId());
		} catch (Throwable e) {
			return system();
		}
	}
	
	public static GoGoDebugger get(String debuggerName, String siteID) {
		return (GoGoDebugger)SysDebugger.getNamedInstance(siteID+debuggerName);
	}

	public static GoGoDebugger system() {
		return (GoGoDebugger)SysDebugger.getNamedInstance("%system%");
	}
	
	public static class SizeAndVersion {
		public long size;
		public int version;
		public SizeAndVersion(long size, int version) {
			this.size = size;
			this.version = version;
		}
	}
}
