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
package com.solertium.gogoego.server.lib.manager.html;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.w3c.dom.Document;

import com.solertium.util.restlet.MediaTypeManager;

/**
 * ManagerFileLoader.java
 * 
 * This file only exists to fetch files
 * 
 * @author carl.scott
 * 
 */
public class ManagerFileLoader {

	public static Document fetchDocument(String filename) {
		try {
			return DocumentUtils.impl.getInputStreamFile(ManagerFileLoader.class.getResourceAsStream(filename));
		} catch (Exception e) {
			return null;
		}
	}

	public static Representation fetchRepresentation(String filename) {
		try {
			return new InputRepresentation(ManagerFileLoader.class.getResourceAsStream(filename), MediaTypeManager
					.getMediaType(filename));
		} catch (Exception e) {
			return null;
		}
	}

}
