package org.gogoego.util.db.fluent;

import org.gogoego.util.db.DBSessionFactory;

public class Statics {
	
	public static Connection configure(String name, String uri, String username, String password){
		//FIXME: guessing needed.  Only postgresql implemented right now anyway.
		String guessedDriver = "org.postgresql.Driver";
		DBSessionFactory.registerDataSource(
				name,
				uri,
				guessedDriver,
				username,
				password);
		return new Connection(name);
	}

}
