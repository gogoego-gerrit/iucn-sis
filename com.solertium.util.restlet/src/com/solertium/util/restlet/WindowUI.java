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

import java.awt.Button;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.solertium.util.restlet.DesktopIntegration.IconProvider;

import edu.stanford.ejalbert.BrowserLauncher;

public class WindowUI extends Frame {
	
	private static final long serialVersionUID = 3L;

	String uri = null;
	
	public WindowUI(String appname, String uri, IconProvider provider){
		this.uri = uri;
		final String furi = uri;

		// use full size (48px) icon image
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(provider.getIcon48()));
		
		setSize(400,200);
		setTitle(appname+" Server");
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent event){
				System.exit(0);
			}
		});
		add("North",new Label(uri));
		Button b = new Button("Open "+appname+" Web Page");
		b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					BrowserLauncher launcher = new BrowserLauncher();
					launcher.openURLinBrowser(furi);
				} catch (Exception poorlyHandled){
					poorlyHandled.printStackTrace();
				}
			}
		});
		add("Center",b);
		add("South",new Label("Close this window to shut down "+appname+" Server."));
		pack();
		setVisible(true);
		try{
			for(int i=0;i<6;i++){
				try { // check to see if already running
					Request request = new Request(Method.GET, uri);
					Client client = new Client(Protocol.HTTP);
					Response response = client.handle(request);
					response.getEntity().getText();
					try{
						BrowserLauncher launcher = new BrowserLauncher();
						launcher.openURLinBrowser(uri);
						i=9999; // done, exit loop
					} catch (Exception poorlyHandled){
						poorlyHandled.printStackTrace();
					}
				} catch (Exception notResponsive) {
					System.err.println("Warning: server has not started yet, waiting ...");
					Thread.sleep(5000);
				}
			}
		} catch (Exception poorlyHandled){
			poorlyHandled.printStackTrace();
		}
	}

}
