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
package com.solertium.util.restlet.authentication;

import java.util.HashMap;

import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowID;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;
import com.solertium.util.SysDebugger;

/**
 * Helper class for performing authentication via an H2 db.
 *
 * In order to use, this, your database must conform to the Authenicator
 * rules of password encryption, and your usernames and passwords must be
 * stored in the same table.
 *
 * @author carl.scott
 *
 */
public class DBAuthenticator extends Authenticator {

	private final HashMap<String, String> loginCache;

	protected final ExecutionContext ec;
	protected final String userTable, userColumn, passwordColumn, idColumn;

	/**
	 * Constructor.
	 *
	 * Supply the table in which to find the supplied username and password columns,
	 * and the authenticator will take care of checking against these tables,
	 * updating passwords, etc.
	 *
	 * @param ec the execution context
	 * @param userTable the user table
	 * @param userColumn the column for the user name in the user table
	 * @param passwordColumn the column for the password in the user table
	 */
	public DBAuthenticator(ExecutionContext ec, String userTable, String userColumn, String passwordColumn) {
		this(ec, userTable, userColumn, passwordColumn, null);
	}

	/**
	 * Constructor.
	 *
	 * Supply the table in which to find the supplied username and password columns,
	 * and the authenticator will take care of checking against these tables,
	 * updating passwords, etc.
	 *
	 * By supplying an idColumn, you are saying that this table uses IDs, which is
	 * important for adding new accounts.
	 *
	 * @param ec
	 * @param userTable
	 * @param userColumn
	 * @param passwordColumn
	 * @param idColumn the id column, or null if it does not exist.
	 */
	public DBAuthenticator(ExecutionContext ec, String userTable, String userColumn, String passwordColumn, String idColumn) {
		super();
		this.ec = ec;
		this.userTable = userTable;
		this.userColumn = userColumn;
		this.passwordColumn = passwordColumn;
		this.idColumn = idColumn;

		loginCache = new HashMap<String, String>();
	}

	/**
	 * Allow caching
	 * @param login
	 * @param password
	 * @return
	 */
	public boolean hasCachedSuccess(String login, String password) {
		return translatePassword(login, password).
			equalsIgnoreCase(loginCache.get(getSHA1Hash(login)));
	}

	protected String translatePassword(final String login, final String password) {
		String md5Pass = getMD5Hash(password + login);
		String sha1Pass = getSHA1Hash(password + login);

		return md5Pass + "," + sha1Pass;
	}

	public boolean changePassword(String login, String newPassword) {
		final String translatedPW = translatePassword(login, newPassword);

		final Row row = new Row();
		row.add(new CString(userColumn, login));
		row.add(new CString(passwordColumn, translatedPW));

		final UpdateQuery query = new UpdateQuery();
		query.setTable(userTable);
		query.setRow(row);
		query.constrain(
			new CanonicalColumnName(userTable, userColumn),
			QConstraint.CT_EQUALS, login
		);

		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			System.out.println("query was " + query.getSQL(ec.getDBSession()));
			e.printStackTrace();
			SysDebugger.getNamedInstance("error").println("Authenticator failed to update password: {0}", e.getMessage());
			return false;
		}

		loginCache.put(getSHA1Hash(login), translatedPW);

		return true;
	}

	public boolean validateAccount(String login, String password) {
		if (hasCachedSuccess(login, password))
			return true;

		final SelectQuery query = new SelectQuery();
		query.select(userTable, passwordColumn);
		query.constrain(new CanonicalColumnName(userTable, userColumn), QConstraint.CT_EQUALS, login);
		
		final Row.Loader rl = new Row.Loader();

		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			e.printStackTrace();
			SysDebugger.getNamedInstance("error").println("Failed to query for account: {0}", e.getMessage());
		}

		if (rl.getRow() == null)
			return false;

		final String dbPW;
		try {
			dbPW = rl.getRow().get(passwordColumn).toString();
		} catch (NullPointerException e) {
			SysDebugger.getNamedInstance("error").println("Unexpected NPE in DBAuthenticator: {0}", e.getMessage());
			return false;
		}
		
		final String translated = translatePassword(login, password);
		boolean success = translated.equals(dbPW);
		
		if (success)
			loginCache.put(getSHA1Hash(login), translated);

		return success;
	}

	public String putNewAccount(String login, String password) throws AccountExistsException {
		if (doesAccountExist(login))
			throw new AccountExistsException();

		final Row row = new Row();
		if (idColumn != null) {
			try {
				row.add(new CInteger(idColumn, Long.valueOf(RowID.get(ec, userTable, idColumn))));
			} catch (DBException e) {
				e.printStackTrace();
				SysDebugger.getNamedInstance("error").println("Failed to get new ID {0}", e.getMessage());
				return null;
			}
		}
		row.add(new CString(userColumn, login));
		row.add(new CString(passwordColumn, translatePassword(login, password)));

		final InsertQuery query = new InsertQuery();
		query.setTable(userTable);
		query.setRow(row);

		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			e.printStackTrace();
			SysDebugger.getNamedInstance("error").println("Add new account failed: {0}", e.getMessage());
			return null;
		}

		return "true";
	}

	public boolean doesAccountExist(String login) {
		final SelectQuery query = new SelectQuery();
		query.select(userTable, userColumn);
		query.constrain(new CanonicalColumnName(userTable, userColumn), QConstraint.CT_EQUALS, login);

		final Row.Loader rl = new Row.Loader();

		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			SysDebugger.getNamedInstance("error").println("Failed to do query {0} ", e.getMessage());
			return false;
		}

		return rl.getRow() != null;
	}
}
