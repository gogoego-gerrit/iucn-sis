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
package com.solertium.db;

import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.jcip.annotations.ThreadSafe;

import org.apache.commons.dbcp.BasicDataSource;

import com.solertium.db.vendor.H2DBSession;
import com.solertium.db.vendor.HSQLDBSession;
import com.solertium.db.vendor.HXTTAccessSession;
import com.solertium.db.vendor.MSAccessSession;
import com.solertium.db.vendor.MySQLSession;
import com.solertium.db.vendor.PostgreSQLDBSession;
import com.solertium.db.vendor.TransactSQLSession;

@ThreadSafe
public class DBSessionFactory {

	private static ConcurrentHashMap<String, DBSession> dbSessions = new ConcurrentHashMap<String, DBSession>();
	private static ConcurrentHashMap<String, DataSource> knownDataSources = new ConcurrentHashMap<String, DataSource>();

	public static DBSession getDBSession(final String name)
			throws NamingException {
		final DBSession dbs = dbSessions.get(name);
		if (dbs != null)
			return dbs;
		return DBSessionFactory.newDBSession(name);
	}

	public static String getDSType(final DataSource ds) {
		if (ds == null)
			return null;
		String name = ds.getClass().getName();
		if (name.equals("org.apache.commons.dbcp.BasicDataSource"))
			name = ((org.apache.commons.dbcp.BasicDataSource) ds)
					.getDriverClassName();
		return name;
	}

	private static DBSession newDBSession(String name) throws NamingException {
		DataSource ds = knownDataSources.get(name);
		if (ds == null) { // try a JNDI lookup
			final Context ctx = new InitialContext();
			if (name == null)
				name = "webappdefault";
			if (name.startsWith("java:comp/env/jdbc"))
				try {
					ds = (DataSource) ctx.lookup(name);
				} catch (final NamingException ignored) {
				}
			else
				try {
					ds = (DataSource) ctx.lookup("java:comp/env/jdbc/" + name);
				} catch (final NamingException ignored) {
				}
			if (ds == null)
				throw new NamingException(
						"Could not resolve JNDI database name " + name);
			DBSessionFactory.registerDataSource(name, ds);
		}
		final String dbtype = DBSessionFactory.getDSType(ds);
		DBSession dbs;
		if ((dbtype.indexOf("jtds") > -1) || (dbtype.indexOf("microsoft") > -1))
			dbs = new TransactSQLSession(name, ds);
		else if (dbtype.indexOf("h2") > -1)
			dbs = new H2DBSession(name, ds);
		else if (dbtype.indexOf("hsqldb") > -1)
			dbs = new HSQLDBSession(name, ds);
		else if (dbtype.indexOf("hxtt") > -1)
			dbs = new HXTTAccessSession(name, ds);
		else if (dbtype.indexOf("JdbcOdbc") > -1)
			dbs = new MSAccessSession(name, ds);
		else if (dbtype.indexOf("postgresql") > -1)
			dbs = new PostgreSQLDBSession(name, ds);
		else
			dbs = new MySQLSession(name, ds);
		dbSessions.put(name, dbs);
		return dbs;
	}
	
	public static boolean isRegistered(final String name) {
		return knownDataSources.containsKey(name);
	}

	public static void registerDataSource(final String name, final DataSource ds) {
		if (knownDataSources.containsKey(name))
			return;
		knownDataSources.put(name, ds);
	}

	/**
	 * This method registers all the data sources declared in a properties file, e.g:<p>
	 * 
	 * dbsession.default.uri=jdbc:h2:foo<br>
	 * dbsession.default.driver=org.h2.Driver<br>
	 * dbsession.default.user=sa<br>
	 * dbsession.default.password=<br>
	 * 
	 * @param properties
	 * @throws NamingException
	 */
	public static void registerDataSources(final Properties properties) throws NamingException {
		for(Object o : properties.keySet()){
			if(o instanceof String){
				String s = (String) o;
				if(s.startsWith("dbsession.") && s.endsWith(".uri")){
					String[] parts = s.split("\\.");
					if(parts.length>1){
						System.out.println("Registering database "+parts[1]);
						registerDataSource(parts[1],properties);
					}
				}
			}
		}
	}

	public static void registerDataSource(final String name, final Properties properties)
		throws NamingException {
		boolean fail = false;
		StringWriter errmsg = new StringWriter();
		for(String p : new String[]{"uri","driver","user","password"}){
			String found = properties.getProperty("dbsession."+name+"."+p);
			if(found==null){
				if(fail) errmsg.write(", ");
				errmsg.write(name+"."+p);
				fail = true;
			}
		}
		if(fail) throw new NamingException("Required database connection property or properties missing: "+errmsg);
		registerDataSource(name,
				properties.getProperty("dbsession."+name+".uri"),
				properties.getProperty("dbsession."+name+".driver"),
				properties.getProperty("dbsession."+name+".user"),
				properties.getProperty("dbsession."+name+".password")
		);
	}

	public static void registerDataSource(final String name, final String url,
			final String driverClass, final String username,
			final String password) throws NamingException {
		if (knownDataSources.containsKey(name))
			return;
		final BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(driverClass);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setUrl(url);
		DBSessionFactory.registerDataSource(name, ds);
	}

	public static void unregisterDataSource(final String name) {
		BasicDataSource ds = (BasicDataSource) knownDataSources.get(name);
		if(ds==null) return;
		try{
			ds.close();
		} catch (SQLException exception) {
			System.err.println("Unable to close data source "+name);
		}
		knownDataSources.remove(name);
		dbSessions.remove(name);
	}

}
