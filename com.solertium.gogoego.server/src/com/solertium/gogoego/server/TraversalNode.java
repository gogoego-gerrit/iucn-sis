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
package com.solertium.gogoego.server;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.solertium.vfs.VFSPath;

/**
 * TraversalNode.java
 * 
 * @author rob.heittman
 *
 */
public class TraversalNode implements TouchListener {

	public static class HTMLCacheEntry {
		public final String html;
		public final Date modified;

		public HTMLCacheEntry(String html, Date modified) {
			this.html = html;
			this.modified = modified;
		}
	}

	public static Map<VFSPath, Map<String, Boolean>> dependencyMap = new ConcurrentHashMap<VFSPath, Map<String, Boolean>>();
	public static Map<String, HTMLCacheEntry> cachedResults = new ConcurrentHashMap<String, HTMLCacheEntry>();

	public TraversalNode replacedBy = null;
	public boolean usedBase = false;
	public boolean compound = false;
	public String simpleUri;
	private String uri;

	private WeakReference<TraversalNode> parent = null;

	public TraversalNode(String uri) {
		this.uri = uri;
	}

	private List<TraversalNode> children = new ArrayList<TraversalNode>();

	public List<TraversalNode> getChildren() {
		return children;
	}

	private Map<VFSPath, Boolean> paths = new HashMap<VFSPath, Boolean>();

	public Set<VFSPath> getPaths() {
		return paths.keySet();
	}

	private void setParent(TraversalNode parent) {
		this.parent = new WeakReference<TraversalNode>(parent);
	}

	public TraversalNode getParent() {
		if (parent == null)
			return null;
		return parent.get();
	}

	public void addChild(TraversalNode t) {
		children.add(t);
		t.setParent(this);
	}

	public void addPath(VFSPath p) {
		addPath(p, true);
	}

	public void addPath(VFSPath p, boolean direct) {
		paths.put(p, direct);
		if (parent != null) {
			TraversalNode ptn = parent.get();
			if (ptn != null)
				parent.get().addPath(p, false);
		}
	}

	public String getUri() {
		return uri;
	}

	public void dump() {
		TraversalLog.log("\n");
		TraversalLog.log("=== Coalesced Dump ===");
		localdump(0);
		TraversalLog.log("==========================");
		TraversalLog.log("=== Raw Dump ===");
		rawdump(0);
		TraversalLog.log("==========================");
		TraversalLog.log("\n");
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	private void rawdump(int depth) {
		StringBuilder spaces = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			spaces.append("  ");
		}
		if (getParent() == null || !getParent().getUri().endsWith(" " + getUri())) {
			TraversalLog.log(spaces + uri);
		}
		/*
		 * for(Map.Entry<VFSPath, Boolean> entry : paths.entrySet()){
		 * TraversalLog.log(spaces +
		 * "(VFS: "+entry.getKey()+" / "+entry.getValue()+")"); }
		 */
		for (TraversalNode child : children) {
			child.rawdump(depth + 1);
		}
	}

	private void localdump(int depth) {
		if (replacedBy != null) {
			for (TraversalNode child : children) {
				if (!child.equals(replacedBy)) {
					replacedBy.addChild(child);
				}
			}
			replacedBy.localdump(depth);
			return;
		}
		StringBuilder spaces = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			spaces.append("  ");
		}
		if (getParent() == null || !getParent().getUri().endsWith(" " + getUri())) {
			TraversalLog.log(spaces + uri);
		}
		/*
		 * for(Map.Entry<VFSPath, Boolean> entry : paths.entrySet()){
		 * TraversalLog.log(spaces +
		 * "(VFS: "+entry.getKey()+" / "+entry.getValue()+")"); }
		 */
		for (TraversalNode child : children) {
			child.localdump(depth + 1);
		}
	}

	public void mapDependencies() {
		if (!uri.contains(".htm"))
			return;
		for (Map.Entry<VFSPath, Boolean> entry : paths.entrySet()) {
			VFSPath p = entry.getKey();
			Map<String, Boolean> m = dependencyMap.get(p);
			if (m == null) {
				m = new HashMap<String, Boolean>();
				dependencyMap.put(p, m);
			}
			m.put(uri, true);
		}
	}

	public void touched() {
		usedBase = true;
	}

}
