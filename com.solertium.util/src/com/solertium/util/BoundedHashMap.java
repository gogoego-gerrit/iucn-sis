/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.util;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a simple bounded HashMap with a FIFO eviction policy.  This
 * need comes up often enough, for simple performance enhancements that
 * call for a flyweight solution without external dependencies.
 * 
 * The provided unit tests check for the more overt leaks and collisions.
 * 
 * There is almost certainly unnecessary oversynchronization taking place
 * in here.  Any operation that could change the FIFO is synchronized on
 * the FIFO.  The underlying ConcurrentHashMap should ensure that gets
 * are deterministic with regard to puts; get operations are not
 * oversynchronized here.  The assumption is that gets are more
 * frequent than puts; they are penalized less.
 * 
 * For a real big-time cache, use EHCache, OSCache, etc.
 * 
 * @author Rob Heittman
 *
 */
public class BoundedHashMap<K,V> extends ConcurrentHashMap<K,V> {

	private static final long serialVersionUID = 1L;

	private final int maxSize;
	
	private final LinkedList<K> list;
	
	private int count;
	
	public BoundedHashMap(int maxSize){
		super();
		this.maxSize = maxSize;
		list = new LinkedList<K>();
	}
	
	@Override
	public V put(K key, V value){
		if (key == null || value == null) // doesn't fly with concurrent hash maps
			return null;
		synchronized(list){
			if(get(key)==null){ // replacements don't count
				list.addLast(key);
				count++;
				if(count>maxSize){
					K toRemove = list.removeFirst();
					super.remove(toRemove);
					count--;
				}
			}
			return super.put(key, value);
		}
	}
	
	@Override
	public synchronized V remove(Object o){
		final V retValue;
		synchronized(list){
			if ((retValue = super.remove(o)) != null) {
				list.remove(o);
				count--;
			}
			return retValue;
		}
	}
	
	@Override
	public synchronized void clear(){
		synchronized(list){
			super.clear();
			list.clear();
			count=0;
		}
	}
	
	/**
	 * Supports unit tests.
	 * 
	 * @return size of internal linked list
	 */
	int getListSize(){
		return list.size();
	}

	/**
	 * Supports unit tests.
	 * 
	 * @return size of internal map
	 */
	int getMapSize(){
		return list.size();
	}
	
	/**
	 * Supports unit tests.
	 * 
	 * @return internal count of entries
	 */
	int getCount(){
		return count;
	}

}
