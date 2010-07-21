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

import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SchemaValidator.java
 * 
 * Validates a given schema against a given document.  If you call 
 * validate, it will return a listing of all errors.  If the document 
 * is valid, then there will be no errors.  Calling isValid will simply 
 * call validate and use this knowledge to determine validation status 
 * and return a simple boolean.  More information can be found using 
 * validate.
 *
 * @author carl.scott
 *
 */
public class SchemaValidator {
	
	public static boolean isValid(InputStream schemaDoc, Reader document) {
		return isValid(new StreamSource(schemaDoc), document);
	}
	
	public static boolean isValid(Reader schemaDoc, Reader document) {
		return isValid(new SAXSource(new InputSource(schemaDoc)), document);
	}
	
	public static boolean isValid(Source schemaDoc, Reader document) {
		List<String> validation = validate(schemaDoc, document);;
		return validation.isEmpty();
	}
	
	public static boolean isValid(URL schemaURL, Reader document) {
		try {
			return validate(schemaURL, document).isEmpty();
		} catch (Exception e) {
			return false;
		}
	}
	
	public static List<String> validate(URL schemaURL, Reader document) {
		final SchemaFactory sFactory = SchemaFactory.newInstance(
			"http://www.w3.org/2001/XMLSchema"
		);
		try {
			return validate(sFactory.newSchema(schemaURL), document);
		} catch (Exception e) {
			return Arrays.asList(e.getMessage());
		}
	}
	
	public static List<String> validate(Source schemaDoc, Reader document) {
		final SchemaFactory sFactory = SchemaFactory.newInstance(
			"http://www.w3.org/2001/XMLSchema"
		);
		try {
			return validate(sFactory.newSchema(schemaDoc), document);
		} catch (Exception e) {
			return Arrays.asList(e.getMessage());
		}
	}
	
	private static ArrayList<String> validate(Schema schema, Reader document) {
		final DocumentBuilderFactory factory = 
			DocumentBuilderFactory.newInstance();	
		
		factory.setSchema(schema);		
		factory.setNamespaceAware(true);
		factory.setValidating(false);	//This is used for DTDs
		
		final MyDefaultHandler dh = new MyDefaultHandler();
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();			
			builder.setErrorHandler(dh);		
			builder.parse(new InputSource(document));
		} catch (Exception e) {
			e.printStackTrace();
			dh.errors.add(e.getMessage());
		}
		
		return dh.errors;
	}
	
	static class MyDefaultHandler extends DefaultHandler {
		private boolean isValid = true;
		private ArrayList<String> errors = new ArrayList<String>();
		public void error(SAXParseException se) {
			errors.add(se.getMessage());
			isValid = false;	
		}
		public void fatalError(SAXParseException se) {
			errors.add(se.getMessage());
			isValid = false;	
		}
		public void warning(SAXParseException se) {
			errors.add(se.getMessage());
			isValid = false;		
		}
		public boolean isValid() {
			return isValid;
		}
	}

}
