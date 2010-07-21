/*
 * Copyright (C) 2004-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
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

package com.solertium.db;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * Simple table dump of query results -- for when we don't yet have a special
 * processing engine ...
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class SQLDateHelper {

	public final static String euroDateFormat = "d MMMM, yyyy";
	public final static String euroNDateFormat = "d MMMM yyyy";
	public final static String httpDateFormat = "EEE, dd MMM yyyy HH:mm:ss";
	public final static String iso8601DateFormat = "yyyy-MM-dd'T'HH:mm:ss";
	public final static String longDateFormat = "MMMM d, yyyy";
	public final static String longNDateFormat = "MMMM d yyyy";
	public final static String quickDateFormat = "MMM d";
	public final static String reverseSlashDateFormat = "MM/dd/yyyy";
	public final static String reverseSqlDateFormat = "MM-dd-yyyy";
	public final static String slashDateFormat = "yyyy/MM/dd";
	public final static String sqlDateFormat = "yyyy-MM-dd";
	public final static String sqlDateTimeFormat = "yyyy-MM-dd HH:mm:ss";

	public DateFormat getHttpDateFormat() {
		final DateFormat df = new SimpleDateFormat(httpDateFormat);
		df.setTimeZone(new SimpleTimeZone(0, "GMT"));
		return df;
	}

	public DateFormat getIso8601DateFormat() {
		final DateFormat df = new SimpleDateFormat(iso8601DateFormat);
		df.setTimeZone(new SimpleTimeZone(0, "GMT"));
		return df;
	}
	
	public static Date parse(String pattern, String source) {
		final Date date;
		try {
			date = new SimpleDateFormat(pattern).parse(source);
		} catch (ParseException e) {
			return null;
		}
		
		return new SQLDate(date);
	}

	public static class SQLDate extends Date {
		
		private static final long serialVersionUID = 1L;
		
		private final SimpleDateFormat format;
		
		public SQLDate(Date date) {
			super(date.getTime());
			format = new SimpleDateFormat(sqlDateFormat);
		}
		
		public String toString() {
			return format.format(this);
		}
		
	}
	
}
