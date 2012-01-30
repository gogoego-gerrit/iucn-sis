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
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.solertium.util.restlet.DesktopIntegration.IconProvider;

import edu.stanford.ejalbert.BrowserLauncher;

public class TrayUI {

	@SuppressWarnings("unchecked")
	public TrayUI(final String appname, final String uri, IconProvider provider) throws AWTException {

		final String furi = uri;

		try {
			final Class systemTray = Class.forName("java.awt.SystemTray");
			boolean isSupported = ((Boolean) systemTray
					.getMethod("isSupported").invoke(null, (Object[]) null))
					.booleanValue();
			if (!isSupported)
				throw new AWTException("SystemTray not supported");

			Image image = null;
			if ("/".equals(java.io.File.separator)) {
				image = Toolkit
						.getDefaultToolkit()
						.getImage(provider.getIcon22());
			}
			else
				image = Toolkit
						.getDefaultToolkit()
						.getImage(provider.getIcon16());
			final MouseListener mouseListener = new MouseListener() {
				public void mouseClicked(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				public void mousePressed(MouseEvent e) {
				}

				public void mouseReleased(MouseEvent e) {
				}
			};
			final ActionListener exitListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			};
			final ActionListener openListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						BrowserLauncher launcher = new BrowserLauncher();
						launcher.openURLinBrowser(furi);
					} catch (Exception poorlyHandled) {
						poorlyHandled.printStackTrace();
					}
				}
			};
			final PopupMenu popup = new PopupMenu();
			final MenuItem openItem = new MenuItem("Open " + appname);
			openItem.addActionListener(openListener);
			final MenuItem exitItem = new MenuItem("Exit");
			exitItem.addActionListener(exitListener);
			popup.add(openItem);
			popup.add(exitItem);

			final Class trayIconClass = Class.forName("java.awt.TrayIcon");
			final Object trayIcon = trayIconClass.getConstructor(Image.class,
					String.class, PopupMenu.class).newInstance(image,
					appname + " Server", popup);
			trayIconClass.getMethod("addMouseListener", MouseListener.class)
					.invoke(trayIcon, mouseListener);
			Object trayInstance = systemTray.getMethod("getSystemTray").invoke(null,(Object[]) null);
			systemTray.getMethod("add", trayIconClass).invoke(trayInstance, trayIcon);
		} catch (final Exception invocation) {
			invocation.printStackTrace();
			throw new AWTException("System Tray invocation failed");
		}

		try {
			for (int i = 0; i < 6; i++)
				try { // check to see if already running
					final Request request = new Request(Method.GET, uri);
					final Client client = new Client(Protocol.HTTP);
					final Response response = client.handle(request);
					response.getEntity().getText();
					try {
						final BrowserLauncher launcher = new BrowserLauncher();
						launcher.openURLinBrowser(uri);
						i = 9999; // done, exit loop
					} catch (final Exception poorlyHandled) {
						poorlyHandled.printStackTrace();
					}
				} catch (final Exception notResponsive) {
					System.err
							.println("Warning: server has not started yet, waiting ...");
					Thread.sleep(5000);
				}
		} catch (final Exception poorlyHandled) {
			poorlyHandled.printStackTrace();
		}

	}

}
