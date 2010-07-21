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

package com.solertium.util.restlet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class ScratchResourceBin {

	private final static ScratchResourceBin instance = new ScratchResourceBin();
	
	private ConcurrentHashMap<String,List<String>> dependencyMap = new ConcurrentHashMap<String,List<String>>();
	
	private ConcurrentHashMap<String,ScratchResource> scratchResources = 
		new ConcurrentHashMap<String,ScratchResource>();
	
	public static ScratchResourceBin getInstance(){
		return instance;
	}
	
	public void add(ScratchResource resource){
		scratchResources.put(resource.getReference().toString(), resource);
	}
	
	public boolean contains(String uri){
		return scratchResources.containsKey(uri);
	}
	
	public ScratchResource get(String uri){
		return scratchResources.get(uri);
	}

	public ScratchResource remove(String uri){
		return scratchResources.remove(uri);
	}
	
	public void showContents() {
		System.out.println(scratchResources.keySet());
	}

	public void addDependency(String dependsOnURI, String dependencyURI){
		List<String> dlist = dependencyMap.get(dependsOnURI);
		if(dlist==null){
			dlist = new ArrayList<String>();
			dependencyMap.put(dependsOnURI, dlist);
		}
		if(!dlist.contains(dependencyURI)) dlist.add(dependencyURI);
	}
	
	public Object getResourceObject(String uri){
		ScratchResource sr = scratchResources.get(uri);
		if(sr==null) return null;
		return sr.getResource();
	}
	
}
