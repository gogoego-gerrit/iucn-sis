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
package org.gogoego.api.mail;

import java.util.Properties;

import org.gogoego.api.plugins.GoGoEgo;

import com.solertium.mail.Mailer;

/**
 * Provides a default mailer for the current GoGoEgo instance
 * (based on the InitProperties, notionally the contents of
 * config.ini)  This provides local backwards compatibility
 * for GoGoEgo 1.0 and GoGoEgo 1.1 configurations, and adds the
 * new ability to use authentication and/or TLS.
 * 
 * GOGOEGO_MAILSERVER - old property for mail server name (deprecated)
 * com.solertium.mail.server - mail server name
 * com.solertium.mail.port - mail server port
 * com.solertium.mail.ssl - use SSL/TLS
 * com.solertium.mail.account - user account for mail server authentication
 * com.solertium.mail.password - password for mail server authentication
 * 
 * @author robheittman
 *
 */
public class InstanceMailer {
	
	private static InstanceMailer instanceMailer;
	
	final String server;
	final int iport;
	final boolean bssl;
	final String account;
	final String password;
	
	public static synchronized InstanceMailer getInstance(){
		if(instanceMailer==null) instanceMailer = new InstanceMailer();
		return instanceMailer;
	}
	
	private InstanceMailer(){
		Properties p = GoGoEgo.getInitProperties();
		
		// old 1.0/1.1 property
		String pserver = p.getProperty("GOGOEGO_MAILSERVER");
		
		// new standard property
		if(pserver==null) pserver = p.getProperty("com.solertium.mail.server");
		
		// fallback
		if(pserver==null) pserver = "localhost";
		
		server = pserver;
		
		int pport = 25;
		String port = p.getProperty("com.solertium.mail.port");		
		if(port!=null){
			pport = Integer.valueOf(port);
		}
		iport = pport;

		boolean pssl = false;
		String ssl = p.getProperty("com.solertium.mail.ssl");
		if("true".equals(ssl)){
			pssl = true;
		}
		bssl = pssl;
		
		String paccount = p.getProperty("com.solertium.mail.account");
		if(paccount==null) paccount="root";
		account = paccount;
		
		password = p.getProperty("com.solertium.mail.password");
	}
	
	public Mailer getMailer(){
		return new Mailer(server,iport,bssl,account,password);
	}
	
}
