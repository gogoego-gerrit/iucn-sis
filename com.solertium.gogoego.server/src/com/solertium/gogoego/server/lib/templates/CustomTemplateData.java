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
package com.solertium.gogoego.server.lib.templates;

import org.gogoego.api.applications.TemplateDataAPI;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;


/**
 * CustomTemplateData.java
 * 
 * Implementation of TemplateData created on the fly from some 
 * Representation. 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class CustomTemplateData implements TemplateDataAPI {
	
	private final GoGoEgoBaseRepresentation representation;
	
	public CustomTemplateData(final GoGoEgoBaseRepresentation representation) {
		this.representation = representation;
	}

	public String getContentType() {
		return "text/html";
	}

	public String getDisplayName() {
		return "Auto-Generated Template";
	}

	public GoGoEgoBaseRepresentation getRepresentation() {
		return representation;
	}

	public String getUri() {
		return "/internal/templates/" + representation.getClass().getName() + ".html";
	}

	public boolean isAllowed(String uri) {
		return true;
	}

}
