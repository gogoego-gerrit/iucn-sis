package org.gogoego.util.db.postgresql;

import java.sql.Driver;

import javax.sql.DataSource;

import org.gogoego.util.db.DBSession;
import org.gogoego.util.db.DBSessionFactory;

public class PostgreSQLDBSessionFactory extends DBSessionFactory {

	private static PostgreSQLDBSessionFactory instance = new PostgreSQLDBSessionFactory();
	
	public static DBSessionFactory getInstance(){
		return instance;
	}
	
	@Override
	protected DBSession connectSession(String name, DataSource ds) {
		return new PostgreSQLDBSession(name, ds);
	}

	@Override
	protected boolean handles(String dbtype) {
		if(dbtype.indexOf("postgresql") > -1) return true;
		return false;
	}
	
	@Override
	protected Driver loadDriver() {
		return new org.postgresql.Driver();
	}
	
	public void register() {
		DBSessionFactory.registerFactory(this);
	}
	
}
