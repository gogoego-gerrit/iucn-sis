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

package com.solertium.util.restlet;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

import edu.stanford.ejalbert.BrowserLauncher;

public class DesktopIntegration {
	
	public static void launch(String appname, String startUriFragment, StandardServerComponent component){
		launch(appname, startUriFragment, new SimpleIconProvider("appicon.png"), component);
	}

	public static void launch(String appname, String startUriFragment, IconProvider provider, StandardServerComponent component){
		String uri = "http://localhost:11001/index.html";
		
		//For testing purposes...
		if (component != null) {
			int httpport = component.getHttpPort();
			
			uri = "http://127.0.0.1:"+httpport+startUriFragment;

			try{
				System.out.println("Starting on port "+httpport);
				
				// try the port
				ServerSocket serverSocket = new ServerSocket(httpport, 1, InetAddress.getByName("127.0.0.1"));
				serverSocket.close();
				
				try{
					component.start();
				} catch (Exception exception) {
					System.err.println("Component failed to start.");
					exception.printStackTrace();
					System.exit(0);
				}
			} catch (IOException exception) {
				// could not open port ...
				try { // check to see if already running
					Request request = new Request(Method.GET, "http://127.0.0.1:"+httpport+"/version");
					Client client = new Client(Protocol.HTTP);
					Response response = client.handle(request);
					response.getEntity().getText();
					System.out.println("Opening in existing instance");
					try{
						BrowserLauncher launcher = new BrowserLauncher();
						launcher.openURLinBrowser(uri);
					} catch (Exception poorlyHandled){
						poorlyHandled.printStackTrace();
					}
					System.exit(0);
				} catch (Exception notResponsive) {
					System.err.println("The usual server port is not responding");
					System.exit(0);
				}
			}
		}
		System.out.println("Started");
		
		try {
			new TrayUI(appname, uri, provider);
		} catch (AWTException notray) {
			System.out.println("System tray not supported, using fallback UI");
			notray.printStackTrace();
			new WindowUI(appname, uri, provider);
		} catch (NoClassDefFoundError noclass) {
			System.out.println("Java 1.5, using fallback UI");
			noclass.printStackTrace();
			new WindowUI(appname, uri, provider);
		}
	}
	
	public static class SimpleIconProvider implements IconProvider {
		
		private final String icon;
		
		public SimpleIconProvider(String icon) {
			this.icon = icon;
		}
		
		public String getIcon16() {
			return getIcon();
		}
		
		public String getIcon22() {
			return getIcon();
		}
		
		public String getIcon48() {
			return getIcon();
		}
		
		public String getIcon() {
			File file = new File(icon);
			try {
				return file.exists() ? file.getAbsolutePath() : null;//file.exists() ? file.toURI().toURL() : null;
			} catch (Exception e) {
				return null;
			}
		}
		
	}
	
	public static class BaseIconProvider implements IconProvider {
		
		private final String folder;
		private final String iconNameBase;
		
		public BaseIconProvider(String folder, String iconNameBase) {
			this.folder = folder;
			this.iconNameBase = iconNameBase;
		}
		
		public String getIcon16() {
			return inject(16);
		}
		
		public String getIcon22() {
			return inject(22);
		}
		
		public String getIcon48() {
			return inject(48);
		}
		
		private String inject(int size) {
			try {
				int index = iconNameBase.lastIndexOf('.');
				String path = iconNameBase.substring(0, index) + "_" + size + 
					iconNameBase.substring(index);
				File file;
				if (folder != null && !"".equals(folder))
					file = new File(folder, path);
				else
					file = new File(path);
			
				return file.exists() ? file.getAbsolutePath() : null;//file.exists() ? file.toURI().toURL() : null;
			} catch (Exception e) {
				return null;
			}
		}
		
	}
	
	public static interface IconProvider {
		
		public String getIcon16();
		
		public String getIcon22();
		
		public String getIcon48();
		
	}
	
}
