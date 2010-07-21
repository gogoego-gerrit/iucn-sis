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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.solertium.db.satools.SABoolean;
import com.solertium.db.satools.SADouble;
import com.solertium.db.satools.SAInteger;
import com.solertium.db.satools.SAMap;
import com.solertium.db.satools.SAMultiMap;
import com.solertium.db.satools.SAString;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class DBAnswer {

	private static HashMap<String, Object> resultCache = new HashMap<String, Object>();

	private static ExecutionContext getExecutionContext(){
		ExecutionContext ec = new SilentExecutionContext();
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
		return ec;
	}
	
	public static Boolean booleanResult(final DBSession dbs, final String query) {
		try {
			final SABoolean b = new SABoolean();
			dbs.doQuery(query, b, getExecutionContext());
			return b.b;
		} catch (final Exception brokenSql) {
		}
		return false;
	}

	public static double doubleOrZero(final BigDecimal d) {
		if (d == null)
			return 0.0D;
		return d.doubleValue();
	}

	public static double doubleOrZero(final Double d) {
		if (d == null)
			return 0.0D;
		return d.doubleValue();
	}

	public static double doubleResult(final DBSession dbs, final String query) {
		try {
			final SADouble p = new SADouble();
			dbs.doQuery(query, p, getExecutionContext());
			return DBAnswer.doubleOrZero(p.d);
		} catch (final Exception brokenSql) {
		}
		return 0.0D;
	}

	public static void flushCache() {
		synchronized (resultCache) {
			resultCache = new HashMap<String, Object>();
		}
	}

	public static int integerResult(final DBSession dbs, final String query) {
		try {
			final SAInteger p = new SAInteger();
			dbs.doQuery(query, p, getExecutionContext());
			return p.i;
		} catch (final Exception brokenSql) {
			brokenSql.printStackTrace();
		}
		return 0;
	}

	public static Map<Object, Object> mapResult(final DBSession dbs,
			final String query) {
		try {
			final SAMap s = new SAMap();
			dbs.doQuery(query, s, getExecutionContext());
			return s.m;
		} catch (final Exception brokenSql) {
		}
		return null;
	}

	public static Map<Object, List<Object>> multiMapResult(final DBSession dbs,
			final String query) {
		try {
			final SAMultiMap s = new SAMultiMap();
			dbs.doQuery(query, s, getExecutionContext());
			return s.m;
		} catch (final Exception brokenSql) {
		}
		return null;
	}

	public static String stringResult(final DBSession dbs, final String query) {
		try {
			final SAString p = new SAString();
			dbs.doQuery(query, p, getExecutionContext());
			return p.s;
		} catch (final Exception brokenSql) {
		}
		return "";
	}

}
