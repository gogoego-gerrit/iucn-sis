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
package com.solertium.util.restlet;

import org.restlet.Context;

/**
 * ContextHelper avoids name collisions and the annoyance of naming context
 * variables, and provides some extra type safety when accessing objects from
 * the context.  ContextHelper works if you have a single instance of a given
 * class that you wish to store or extract from the Restlet context.  The
 * canonical name of the class is used as the key.  This is useful in Resources
 * which are short-lived and must access something from the Context.
 *
 * First, set up something like this:
 *
 * private final static ContextHelper<NeededClass> neededHelper =
 *   new ContextHelper<NeededClass>(NeededClass.class);
 *   
 * The above can be done in a utility class accessible via import static,
 * if many resources must access the same context objects.
 * 
 * To store in the context:
 * neededHelper.store(context, instance);
 *
 * To fetch from the context:
 * NeededClass instance = neededHelper.fetch(context);
 * 
 * @author robheittman
 *
 * @param <T>
 */
public class ContextHelper<T> {
	
	private final Class<T> type;
	private final String typeKey;
	
	public ContextHelper(Class<T> type){
		this.type = type;
		this.typeKey = type.getCanonicalName();
	}
	
	public T fetch(Context context){
		Object o = context.getAttributes().get(typeKey);
		if(o==null) return null;
		if(type.isAssignableFrom(o.getClass()))
			return type.cast(o);
		else throw new RuntimeException("Expected "+typeKey+" in context but found "+o.getClass().getCanonicalName());
	}

	public void store(Context context, T object){
		context.getAttributes().put(typeKey,object);
	}

}
