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
package com.solertium.gogoego.server.filters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.resources.PageTreeNode;
import com.solertium.gogoego.server.lib.resources.StaticPageTreeNode;

/**
 * ConditionalGetFilter.java
 * 
 * Since GoGoEgo templating can influence the last modified 
 * date of an entity in many ways, this filter will store 
 * the last modified date of    
 * 
 * @author user
 *
 */
public class ConditionalGetProcess implements Runnable {
	
	private ServerApplication sa = null;
	private String uri;
	
	public ConditionalGetProcess(String uri, ServerApplication sa) {
		this.uri = uri;
		this.sa = sa;
	}
	
	public void run() {
		final Collection<String> urisToFlush = new HashSet<String>();
		for (Map.Entry<String, StaticPageTreeNode> entry : sa.getLastModifiedMap().entrySet())
			if (entry.getValue().contains(uri))
				urisToFlush.add(entry.getKey());
		
		for (String path : urisToFlush)
			sa.getLastModifiedMap().remove(path);
	}

}
