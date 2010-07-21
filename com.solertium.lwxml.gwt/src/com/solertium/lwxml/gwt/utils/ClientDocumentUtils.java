/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.lwxml.gwt.utils;

import java.util.Date;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GWTResponseException;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

/**
 * ClientDocumentUtils.java
 * 
 * Parses the standard status notices that get passed back from 
 * a DocumentUtils.createErrorDocument/createConfirmDocument.
 *
 * @author carl.scott
 *
 */
public class ClientDocumentUtils {
	
	/**
	 * Parse the status from the document
	 * @param document the document
	 * @return the status, never null
	 */
	public static String parseStatus(NativeDocument document) {
		String status = "";
		if (document == null)
			return status;
		if (document.getPeer() == null || document.getDocumentElement() == null) {
			if ("".equals(document.getText()))
				return status;
			else if ("true".equals(document.getHeaders().get("com.solertium.lwxml.gwt.isFailure")))
				return status;
			else {
				final NativeDocument failureDoc = NativeDocumentFactory.newNativeDocument();
				failureDoc.setHeader("com.solertium.lwxml.gwt.isFailure", "true");
				failureDoc.parse(document.getText());
				return parseStatus(failureDoc);
			}
		}
		
		try {
			NativeNodeList statusNodes = document.getDocumentElement().getChildNodes();
			for (int i = 0; i < statusNodes.getLength(); i++) {
				NativeNode current = statusNodes.item(i);
				if (current.getNodeName().equalsIgnoreCase("status")) {
					status = current.getTextContent();
					break;
				}
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return status;
	}
	
	public static String handleFilterRejectionError(Throwable caught, String responseText) {
		String filterName = "No Name Given", errorMsg = "No Message Provided";
		final NativeDocument responseDoc = NativeDocumentFactory.newNativeDocument();
		responseDoc.parse(responseText);
		final NativeNodeList nodes = responseDoc.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeNode node = nodes.item(i);
			if ("filter".equals(node.getNodeName())) {
				final NativeNodeList children = node.getChildNodes();
				for (int k = 0; k < children.getLength(); k++) {
					final NativeNode curChild = children.item(k);
					if ("name".equals(curChild.getNodeName()))
						filterName = curChild.getTextContent();
				}
					
			}
			else if ("error".equals(node.getNodeName()))
				errorMsg = node.getTextContent();
		}
		
		return "You can not save this file because a file writing filter has detected and error. Please correct and try again.<br/><br/>" + 
			"Filter Name: " + filterName + "<br/>Error: " + errorMsg;
	}
	
	/**
	 * Will attempt to see if the error is due to the file being out of date.  If so, 
	 * it will send back the error message.  If not, it will return null.
	 * @param caught the exception
	 * @param responseText the document's response text (it can be null
	 * @return the response message, if available.
	 */
	public static String handleOutOfDateError(Throwable caught, String responseText) {
		if (responseText != null && caught instanceof GWTResponseException && ((GWTResponseException)caught).getCode() == 412) {
			Date lastModified = null;
			final NativeDocument response = NativeDocumentFactory.newNativeDocument();
			response.parse(responseText);
			final NativeNodeList nodes = response.getDocumentElement().getElementsByTagName("span");
			for (int i = 1; i < nodes.getLength(); i++) {
				NativeNode current = nodes.item(i);
				if (current.getNodeName().equals("span")) {
					try {
						lastModified = new Date(Long.parseLong(current.getTextContent()));
						break;
					} catch (Exception e) { 
						//Continue on;
					}
				}
			}
			
			String text = "This file has been modified since you opened it -- " + 
				responseText + " -- would you like to save anyway?";
			if (lastModified != null)
				text = "This file has been modified since you opened it -- " + 
					lastModified.toString() + " -- would you like to save anyway?";
			return text;
		}
		return null;
	}

}
