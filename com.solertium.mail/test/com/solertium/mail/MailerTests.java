/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.mail;

import org.junit.Test;

public class MailerTests {

	public void populate(Mailer m, String testName){
		m.setSubject("Test ["+testName+"]");
		m.setBody("This message is sent by an automated test: "+testName);
	}
	
	/**
	 * To successfully run this test you will need to supply a valid open SMTP server
	 * in the system environment com.solertium.mail.server
	 */
	@Test
	public void sendUnauthenticatedLocal(){
		String testName = "sendUnauthenticatedLocal";
		String s = System.getenv("com.solertium.mail.server");
		if(s==null){
			assert true;
			return;
		}
		try{
			Mailer m = new Mailer(s,25,false,"root",null);
			m.setFrom("root@localhost");
			m.setTo("gogoego.tests@gmail.com");
			populate(m,testName);
			m.send();
		} catch (Exception x) {
			x.printStackTrace();
			throw new AssertionError("Mailer test "+testName+"failed due to exception "+x.getClass().getName()+": "+x.getMessage());
		}
	}
	
	/**
	 * To successfully run this test you will need to supply a valid GMail account and
	 * password in the system environment: com.solertium.mail.gmail.account and
	 * com.solertium.mail.gmail.password
	 */
	@Test
	public void sendGmail(){
		String testName = "sendGmail";
		String u = System.getenv("com.solertium.mail.gmail.account");
		String p = System.getenv("com.solertium.mail.gmail.password");
		if(u==null || p==null){
			assert true;
			return;
		}
		try{
			Mailer m = new GMailer(u,p);
			m.setTo("gogoego.tests@gmail.com");
			populate(m,testName);
			m.send();
		} catch (Exception x) {
			throw new AssertionError("Mailer test "+testName+"failed due to exception "+x.getClass().getName()+": "+x.getMessage());
		}
	}

}
