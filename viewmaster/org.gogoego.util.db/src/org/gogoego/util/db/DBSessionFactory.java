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
package org.gogoego.util.db;

import java.io.StringWriter;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.gogoego.util.getout.GetOut;

public abstract class DBSessionFactory {

	private static ConcurrentHashMap<String, DBSession> dbSessions = new ConcurrentHashMap<String, DBSession>();
	private static ConcurrentHashMap<String, DataSource> knownDataSources = new ConcurrentHashMap<String, DataSource>();

	public static DBSession getDBSession(final String name) {
		final DBSession dbs = dbSessions.get(name);
		if (dbs != null)
			return dbs;
		return DBSessionFactory.newDBSession(name);
	}

	public static String getDSType(final DataSource ds) {
		if (ds == null)
			return null;
		String name = ds.getClass().getName();
		if (ds instanceof org.apache.commons.dbcp.BasicDataSource)
			name = ((org.apache.commons.dbcp.BasicDataSource) ds)
					.getDriverClassName();
		return name;
	}

	private static DBSession newDBSession(String name) {
		DataSource ds = knownDataSources.get(name);
		if (ds == null) { // try a JNDI lookup
			GetOut.log("Nuts, JNDI lookup requested");
			throw new RuntimeException("no data source registered as "+name);
		}
		final String dbtype = DBSessionFactory.getDSType(ds);
		DBSession dbs = null;

		for(DBSessionFactory factory: factories)
			if(factory.handles(dbtype)) dbs = factory.connectSession(name, ds);
		
		if(dbs == null)
			throw new RuntimeException("no DBSession adapter can handle "+dbtype);
		dbSessions.put(name, dbs);
		return dbs;
	}
	
	protected static List<DBSessionFactory> factories = new CopyOnWriteArrayList<DBSessionFactory>();
	
	public static void registerFactory(DBSessionFactory factory){
		synchronized(factories){
			if(!factories.contains(factory))
				factories.add(factory);
		}
	}
	
	public static void unregisterFactory(DBSessionFactory factory){
		synchronized(factories){
			if(factories.contains(factory))
				factories.remove(factory);
		}
	}
	
	/**
	 * Concrete classes implement this to provide explicit registration, useful
	 * when an OSGi framework is not in effect (e.g. for testing/mocking)
	 */
	public abstract void register();
	
	protected abstract boolean handles(String dbtype);
	
	protected abstract DBSession connectSession(String name, DataSource ds);
	
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
	public static void registerDataSources(final Properties properties) {
		for(Object o : properties.keySet()){
			if(o instanceof String){
				String s = (String) o;
				if(s.startsWith("dbsession.") && s.endsWith(".uri")){
					String[] parts = s.split("\\.");
					if(parts.length>1){
						GetOut.log("Registering database "+parts[1]);
						registerDataSource(parts[1],properties);
					}
				}
			}
		}
	}

	public static void registerDataSource(final String name, final Properties properties) {
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
		if(fail) throw new RuntimeException("Required database connection property or properties missing: "+errmsg);
		registerDataSource(name,
				properties.getProperty("dbsession."+name+".uri"),
				properties.getProperty("dbsession."+name+".driver"),
				properties.getProperty("dbsession."+name+".user"),
				properties.getProperty("dbsession."+name+".password")
		);
	}
	
	
	public void connectPool(String name, String url, String driverClass,
			String username, String password) {
		
		final BasicDataSource ds = new BasicDataSource(){
			/**
			 * DBCP 1.4's implementation is ... unexpected.  You can set a custom
			 * classloader, but in fact it won't be used to load the driver.
			 * Bug reported on dev@commons list.
			 */
			@Override
		    protected ConnectionFactory createConnectionFactory() throws SQLException {
				try{
					Driver driver = loadDriver();

			        String user = username;
			        if (user != null)
			            connectionProperties.put("user", user);

			        String pwd = password;
			        if (pwd != null)
			            connectionProperties.put("password", pwd);

			        ConnectionFactory driverConnectionFactory = new DriverConnectionFactory(driver, url, connectionProperties);
			        return driverConnectionFactory;
				} catch (Exception ex) {
					throw new SQLException(ex);
				}
		    }
		};
		
		ds.setDriverClassName(driverClass);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setUrl(url);
		DBSessionFactory.registerDataSource(name, ds);
	}
	
	protected abstract Driver loadDriver();
	
	public static void registerDataSource(final String name, final String url,
			final String driverClass, final String username,
			final String password) {
		if (knownDataSources.containsKey(name))
			return;
		
		for(DBSessionFactory factory: factories)
			if(factory.handles(driverClass)) factory.connectPool(name, url, driverClass, username, password);
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
