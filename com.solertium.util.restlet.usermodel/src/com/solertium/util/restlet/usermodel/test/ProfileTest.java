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
package com.solertium.util.restlet.usermodel.test;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;

/**
 * ProfileTest.java
 * 
 * JUnit Test Case
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class ProfileTest {
	
	private String userID;

	@Before
	public void setUp() throws Exception {
		Client client = new Client(Protocol.HTTP);
		Response resp = client.put("http://localhost:11001/profile", new StringRepresentation("<root><row id=\"1\"><field name=\"NAME\">bob</field></row></root>"));
		if (!resp.getStatus().isSuccess())
			throw new RuntimeException("Could not add test user");
		
		final Document document = new DomRepresentation(resp.getEntity()).getDocument();
		userID = document.getElementsByTagName("row").item(0).getTextContent();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddProfile(){
		
	}
	
	@Test
	public void testGetProfile(){
		Client client = new Client(Protocol.HTTP);
		Response resp = client.get("http://localhost:11001/profile/" + userID);
		try{
			System.out.println(resp.getEntity().getText());
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println(resp.getStatus());
		
	}
	
	@Test
	public void testRemoveProfile(){
		Client client = new Client(Protocol.HTTP);
		Response resp = client.delete("http://localhost:11001/profile/" + userID);
		try{
			System.out.println(resp.getEntity().getText());
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println(resp.getStatus());
		
		Assert.assertTrue(resp.getStatus().isSuccess());
	}
	
	@Test
	public void testUpdateProfile(){
		Client client = new Client(Protocol.HTTP);
		Response resp = client.post("http://localhost:11001/profile/" + userID, 
			new StringRepresentation("<root><field name=\"NAME\">robert</field></root>"));
		System.out.println(resp.getStatus());
		Response resp2 = client.get("http://localhost:11001/profile/" + userID);
		try{
			System.out.println(resp2.getEntity().getText());
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
}
