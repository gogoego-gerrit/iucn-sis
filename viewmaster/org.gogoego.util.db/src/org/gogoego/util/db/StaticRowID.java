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

package org.gogoego.util.db;

import java.util.concurrent.ConcurrentHashMap;

import org.gogoego.util.db.query.InsertQuery;
import org.gogoego.util.db.query.QComparisonConstraint;
import org.gogoego.util.db.query.QConstraint;
import org.gogoego.util.db.query.SelectQuery;
import org.gogoego.util.db.query.UpdateQuery;
import org.gogoego.util.db.satools.SAInteger;
import org.gogoego.util.db.shared.CInteger;
import org.gogoego.util.db.shared.CLong;
import org.gogoego.util.db.shared.CString;
import org.gogoego.util.db.shared.CanonicalColumnName;
import org.gogoego.util.db.shared.Column;
import org.gogoego.util.db.shared.Row;
import org.gogoego.util.getout.GetOut;

/**
 * StaticRowID.java
 * 
 * Utility for generating database row IDs.  It is not tied to the 
 * IDCOUNT table specifically, you can use any table you want to use.
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class StaticRowID {

	public CanonicalColumnName IDCOUNT_ID = new CanonicalColumnName(
			"IDCOUNT", "id");
	public CanonicalColumnName IDCOUNT_TBL = new CanonicalColumnName(
			"IDCOUNT", "tbl");

	public ConcurrentHashMap<String, SelectQuery> selects = new ConcurrentHashMap<String, SelectQuery>();
	
	public static StaticRowID instance = new StaticRowID();
	
	public static StaticRowID getInstance() {
		instance.IDCOUNT_ID = new CanonicalColumnName("IDCOUNT", "id");
		instance.IDCOUNT_TBL = new CanonicalColumnName("IDCOUNT", "tbl");
		return instance;
	}
	
	public static StaticRowID getInstance(CanonicalColumnName idCountID, CanonicalColumnName idCountTBL) {
		instance.IDCOUNT_ID = idCountID;
		instance.IDCOUNT_TBL = idCountTBL;
		return instance;
	}

	public long get(final DBSession dbsess, final String table)
			throws DBException {
		return get(dbsess, table, null);
	}

	public long get(final DBSession dbsess, final String table,
			final String checkColumn) throws DBException {
		long highid = 0;
		final String key = dbsess.toString() + "\0" + table;
		SelectQuery sq = selects.get(key);
		if (sq == null) {
			final QConstraint constraint = new QComparisonConstraint(
					IDCOUNT_TBL, QConstraint.CT_EQUALS, table);
			sq = new SelectQuery();
			sq.select(IDCOUNT_ID);
			sq.constrain(constraint);
			selects.put(key, sq);
		}
		synchronized (sq) {
			final ExecutionContext ec = new SystemExecutionContext();
			ec.setExecutionLevel(ExecutionContext.READ_WRITE);
			ec.setDBSession(dbsess);
			final SAInteger sai = new SAInteger();
			try {
				ec.doQuery(sq, sai);
			} catch (final DBException idcountBrokenDbx) {
				ec
						.debug("Exception checking IDCOUNT table, attempting to create it");
				final Row r = new Row();
				final Column c1 = new CString("tbl", "");
				c1.setKey(true);
				r.add(c1);
				final Column c2 = new CInteger("id", 0);
				r.add(c2);
				try {
					ec.setExecutionLevel(ExecutionContext.ADMIN);
					ec.createTable(IDCOUNT_TBL.getTable(), r);
				} catch (final DBException idcountCreateDbx) {
					ec.debug("Failure creating IDCOUNT table");
					GetOut.log(idcountCreateDbx);
				}
			}
			highid = sai.i;
			boolean mustinsert = false;
			if (highid == 0) {
				mustinsert = true;
				final QConstraint iconstraint = new QComparisonConstraint(
						IDCOUNT_TBL, QConstraint.CT_EQUALS, "IDBASE");
				sq = new SelectQuery();
				sq.select(IDCOUNT_ID);
				sq.constrain(iconstraint);
				ec.debug(sq.getSQL(dbsess));
				final SAInteger saibase = new SAInteger();
				try {
					dbsess.doQuery(sq, saibase, ec);
				} catch (final DBException getEx) {
					ec.debug("Exception getting IDBASE");
				}
				highid = saibase.i;
				if (saibase.i == 0) {
					ec.debug("Setting IDBASE to 1 for the first time");
					// need to set IDBASE -- default base is 10000
					final Row row = new Row();
					row.add(new CString("tbl", "IDBASE"));
					row.add(new CLong("id", 1));
					final InsertQuery ibq = new InsertQuery(IDCOUNT_TBL.getTable(), row);
					try {
						dbsess.doUpdate(ibq, ec);
					} catch (final DBException setEx) {
						ec.debug("Exception setting IDBASE");
					}
					highid = 1;
				}
			}
			if (highid == 0)
				throw new DBException("Cannot generate ID for new record.");
			if (checkColumn != null) {
				ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
				final SAInteger saicheck = new SAInteger();
				try {
					ec
							.doQuery(
									"select max("
											+ dbsess
													.formatCanonicalColumnName(new CanonicalColumnName(
															table, checkColumn))
											+ ") from "
											+ dbsess.formatIdentifier(table),
									saicheck);
				} catch (final DBException exception) {
					ec.debug("Could not check max of " + checkColumn);
				}
				if (saicheck.i >= highid)
					highid = saicheck.i + 1; // pad by one for extra buffer
			}
			highid++;
			final Row row = new Row();
			row.add(new CString("tbl", table));
			row.add(new CInteger("id", highid));
			if (mustinsert) {
				final InsertQuery iq = new InsertQuery(IDCOUNT_TBL.getTable(), row);
				try {
					dbsess.doUpdate(iq, ec);
				} catch (final Exception ex) {
					ec.debug("Exception inserting new id count");
					throw new DBException(
							"Cannot save ID pointer for new record.");
				}
			} else {
				final QConstraint constraint = new QComparisonConstraint(
						IDCOUNT_TBL, QConstraint.CT_EQUALS, table);
				final UpdateQuery uq = new UpdateQuery(IDCOUNT_TBL.getTable(), row,
						constraint);
				try {
					dbsess.doUpdate(uq, ec);
				} catch (final Exception ex) {
					ec.debug("Exception updating id count");
					throw new DBException(
							"Cannot save ID pointer for new record.");
				}
			}
		}
		return highid;
	}

	public long get(final ExecutionContext ec, final String table)
			throws DBException {
		return get(ec.getDBSession(), table, null);
	}

	public long get(final ExecutionContext ec, final String table,
			final String checkColumn) throws DBException {
		return get(ec.getDBSession(), table, checkColumn);
	}

	public String getString(final DBSession dbsess, final String table)
			throws Exception {
		return Long.toString(get(dbsess, table));
	}

}
