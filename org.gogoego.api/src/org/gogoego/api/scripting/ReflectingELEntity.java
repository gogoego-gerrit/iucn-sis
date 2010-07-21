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
package org.gogoego.api.scripting;

import java.util.Iterator;
import java.util.List;

import com.solertium.util.TrivialExceptionHandler;

/**
 * ReflectingELEntity - does method calls as EL dot syntax for convenience.
 * 
 * @author carl.scott
 * 
 */
public abstract class ReflectingELEntity implements ELEntity {

	/**
	 * Based on input such as base.executeMethod(param1, "param2"), 
	 * this function will attempt to find the the specified method 
	 * that matches the parameters given and execute the function. 
	 * 
	 * Currently, only string parameters or no parameters are 
	 * readily acceptable.
	 */
	public String resolveEL(String key) {
		if (key == null)
			return "";

		int firstIndex = key.indexOf("(");
		int secondIndex = key.indexOf(")");
		
		if (firstIndex == -1 || secondIndex == -1)
			return "";

		String methodName = key.substring(0, firstIndex);
		String params = key.substring(firstIndex + 1, secondIndex);
		String[] allParams = null;
		if (firstIndex + 1 != secondIndex) {
			allParams = params.split(",");
			for (int i = 0; i < allParams.length; i++)
				allParams[i] = allParams[i].replace("\"", "").trim();
		}
		else
			allParams = new String[0];

		Object retValue = null;

		java.lang.reflect.Method[] methods = this.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			java.lang.reflect.Method current = methods[i];
			if (current.getName().equalsIgnoreCase(methodName)
					&& (current.getParameterTypes().length == allParams.length)) {
				try {
					retValue = current.invoke(this, (Object[]) allParams);
					break;
				} catch (Exception e) {
					e.printStackTrace();
					TrivialExceptionHandler.ignore(this, e);
				}
			}
		}

		return retValue == null ? "" : retValue.toString();

	}

	protected String listToCSV(final List<String> list) {
		String csv = "";
		final Iterator<String> iterator = list.listIterator();
		while (iterator.hasNext())
			csv += iterator.next() + (iterator.hasNext() ? "," : "");
		return csv;
	}

}
