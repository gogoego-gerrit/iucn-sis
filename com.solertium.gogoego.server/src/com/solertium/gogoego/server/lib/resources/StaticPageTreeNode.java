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
package com.solertium.gogoego.server.lib.resources;

import java.util.Date;

/**
 * StaticPageTreeNode.java
 * 
 * Wraps a page tree node, storing the last modified date so 
 * it does not have to be fetched each time.  Can store other 
 * data here as necessary.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, 
 * <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class StaticPageTreeNode {
	
	private final PageTreeNode node;
	private final long lastModified;
	private final Date expirationDate;
	
	public StaticPageTreeNode(PageTreeNode node) {
		this.node = node;
		this.lastModified = node.getLastModified();
		this.expirationDate = node.getExpirationDate();
	}
	
	public long getLastModified() {
		return lastModified;
	}
	
	public boolean contains(String uri) {
		return node.contains(uri);
	}
	
	public Date getExpirationDate() {
		return (expirationDate == null) ? null : new Date(expirationDate.getTime());
	}

}
