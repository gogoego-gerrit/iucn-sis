/*
 * Copyright (C) 2009 Solertium Corporation
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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Reduce noise associated with getting certain kinds of resources from
 * the current classloader.
 * 
 * @author rob.heittman
 */
public class ClasspathResources {
	
	public static class ClasspathResourceException extends IOException {
		private static final long serialVersionUID = 1L;

		public ClasspathResourceException(String message, Throwable cause){
			super(message);
			initCause(cause);
		}
	}
	
	public static Document getDocument(Class<?> referenceClass, String relativePath)
		throws ClasspathResourceException {
		try{
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					referenceClass.getResourceAsStream(relativePath)
				);
		} catch (ParserConfigurationException pcx) {
			throw new RuntimeException("XML Parser configuration error",pcx);
		} catch (SAXException sax) {
			throw new ClasspathResourceException("Parsing failed for "+relativePath,sax);
		} catch (IOException iox) {
			throw new ClasspathResourceException("Failed to retrieve "+relativePath,iox);
		}
	}

	public static Document getDocument(Object referenceObject, String relativePath)
		throws ClasspathResourceException {
		return getDocument(referenceObject.getClass(),relativePath);
	}

}
