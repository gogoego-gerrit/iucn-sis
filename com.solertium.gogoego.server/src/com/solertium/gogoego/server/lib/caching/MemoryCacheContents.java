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
package com.solertium.gogoego.server.lib.caching;

import java.util.Calendar;
import java.util.Date;

import com.solertium.gogoego.server.lib.resources.PageTreeNode;

/**
 * MemoryCacheContents.java
 * 
 * Cached content information.  Will eventually be used for smart 
 * eviction policies based on size instead of simple item count.
 * 
 * @author carl.scott
 *
 */
public class MemoryCacheContents {
	
	private final String contents;
	private final long size;
	private final Date timeCached;
	private final Date expiration;
	
	private final PageTreeNode pageTree;

	/**
	 * 
	 * Units:
	 * m - minute
	 * h - hour
	 * d - day
	 * y - year 
	 * 
	 * @param contents the contents to cache
	 * @param expires the expiration date, in format #unit (10h, 5d, etc)
	 */
	public MemoryCacheContents(String contents, String expires, PageTreeNode pageTree) {
		this.contents = contents;
		this.expiration = parseExpirationDate(expires);
		this.size = contents.length();
		this.timeCached = Calendar.getInstance().getTime();
		this.pageTree = pageTree;
	}
	
	public String getContents() {
		return contents;
	}
	
	public Date getExpirationDate() {
		return new Date(expiration.getTime());
	}
	
	public PageTreeNode getPageTree() {
		return pageTree;
	}
	
	public long getSize() {
		return size;
	}
	
	public Date getTimeCached() {
		return new Date(timeCached.getTime());
	}
	
	public boolean isExpired() {
		return Calendar.getInstance().getTime().after(expiration);
	}
	
	public static Date parseExpirationDate(String expires) {
		final Calendar dCal = Calendar.getInstance();
		dCal.add(Calendar.HOUR, 1);
		
		final Date defaultDate = dCal.getTime();
		
		if (expires == null)
			return defaultDate;
		
		expires = expires.toLowerCase();
		
		final StringBuilder number = new StringBuilder();
		int i;
		for (i = 0; i < expires.length(); i++) {
			char c = expires.charAt(i); 
			if (Character.isDigit(c))
				number.append(c);
			else
				break;
		}
		char unit = Character.UNASSIGNED;
		while (i < expires.length()) {
			if (Character.isLetter(expires.charAt(i))) {
				unit = expires.charAt(i);
				break;
			}
			i++;
		}
		
		if (unit == Character.UNASSIGNED)
			return defaultDate;
		
		int time;
		try {
			time = Integer.parseInt(number.toString());
		} catch (NumberFormatException e) {
			return defaultDate;
		}
		
		int calendarField;
		switch (unit) {
			case 'm': { 
				calendarField = Calendar.MINUTE;
				break;
			}
			case 'h': {
				calendarField = Calendar.HOUR;
				break;
			}
			case 'd': {
				calendarField = Calendar.DATE;
				break;
			}
			case 'y': {
				calendarField = Calendar.YEAR;
				break;
			}
			default:
				return defaultDate;
		}
		
		final Calendar cal = Calendar.getInstance();
		cal.add(calendarField, time);
		
		return cal.getTime();
	}
	
	

}
