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
package com.solertium.util.restlet.usermodel.managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowID;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.Replacer;
import com.solertium.util.SysDebugger;
import com.solertium.util.restlet.usermodel.core.Profile;

/**
 * H2ProfileManager.java
 * 
 * Database implementation of a profile manager.
 * 
 * @author david.fritz
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class H2ProfileManager implements ProfileManager {

	private final ExecutionContext ec;
	private final String profile_table;
	private final String data_table;
		
	public H2ProfileManager(String location) {
		profile_table = location + "_PROFILE";
		data_table = location + "_PROFILEDATA";
		
		final String DB = location + "_PROFILES";
		
		if (DBSessionFactory.isRegistered(DB)) {
			try {
				ec = new SystemExecutionContext(DB);
			} catch (NamingException unlikely) {
				throw new RuntimeException("Could not find database " + DB, unlikely);
			}
		} else {
			final Properties defaultProps = new Properties();
			defaultProps.setProperty("dbsession." + DB + ".uri", "jdbc:h2:file:h2_db/" + DB);
			defaultProps.setProperty("dbsession." + DB + ".driver", "org.h2.Driver");
			defaultProps.setProperty("dbsession." + DB + ".user", "sa");
			defaultProps.setProperty("dbsession." + DB + ".password", "");

			try {
				DBSessionFactory.registerDataSource(DB, defaultProps);
				ec = new SystemExecutionContext(DB);
			} catch (NamingException e) {
				throw new RuntimeException("Could not create database");
			}
		}

		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		
		init();
	}
	
	public H2ProfileManager(final String location, final ExecutionContext ec) {
		this.profile_table = location + "_PROFILE";
		this.data_table = location + "_PROFILEDATA";
		
		this.ec = ec;
		
		init();
	}
	
	private void init() {
		try {
			InputStream stream = H2ProfileManager.class.getResourceAsStream("structure.xml");
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String xml="";
			try{
				while(reader.ready()){
					xml += reader.readLine() +'\n';
				}
				xml = Replacer.replace(xml, "${profile_table}", profile_table);
				xml = Replacer.replace(xml, "${data_table}", data_table);
				
				ec.appendStructure(BaseDocumentUtils.impl.createDocumentFromString(xml), true);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		catch (DBException e) {
			throw new RuntimeException("Could not load data structure", e);
		}


	}
	
	public boolean userExists(String username) throws ResourceException {
		final SelectQuery query = new SelectQuery();
		query.select(profile_table, "ID");
		query.constrain(
			new CanonicalColumnName(profile_table, "NAME"), 
			QConstraint.CT_EQUALS, username
		);
		
		final Row.Loader rl = new Row.Loader();
		
		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		return rl.getRow() != null;
	}
	
	public String getUserID(String userName) throws ResourceException {
		final SelectQuery query = new SelectQuery();
		query.select(profile_table, "ID");
		query.constrain(
			new CanonicalColumnName(profile_table, "NAME"),
			QConstraint.CT_EQUALS, userName
		);
		
		final Row.Loader rl = new Row.Loader();
		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		if (rl.getRow() == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		return rl.getRow().get("ID").toString();
	}
	
	public Profile getProfile(String userID) throws ResourceException {
		final SelectQuery query = new SelectQuery();
		query.select(profile_table, "NAME");
		query.constrain(
			new CanonicalColumnName(profile_table, "ID"), 
			QConstraint.CT_EQUALS, Integer.valueOf(userID)
		);
		
		final Row.Loader rl = new Row.Loader();
		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		if (rl.getRow() == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		final Integer id = Integer.valueOf(userID);
			
		final SelectQuery data_query = new SelectQuery();
		data_query.select(data_table, "*");
		data_query.constrain(
			new CanonicalColumnName(data_table, "PROFILE_ID"), 
			QConstraint.CT_EQUALS, id
		);
			
		final Row.Set data_rs = new Row.Set();
		
		try {
			SysDebugger.getInstance().println("Profile Query: {0}", data_query.getSQL(ec.getDBSession()));
			ec.doQuery(data_query, data_rs);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final HashMap<String, String> userData = new HashMap<String, String>();
			
		for (Row dbRow : data_rs.getSet()) 
			userData.put(dbRow.get("KEY").getString(), dbRow.get("VALUE").getString());
			
		return new Profile(rl.getRow().get("NAME").toString(), userData);
	}
	
	
	public ArrayList<String> getAllUsers() throws ResourceException {
		ArrayList<String> users = new ArrayList<String>();
		
		final Map<String, String> mapping = getUserMapping();
		for (Map.Entry<String, String> entry : mapping.entrySet()) {
			users.add(entry.getValue());
		}

		return users;
	}
	 
	/*public void addProfile(Profile profile) {
		
		if(getProfile(profile.getName())!= null){
			System.out.println("Profile already exists");
			return;
		}
		System.out.println("inserting profile");
		InsertQuery isql = new InsertQuery();
		Row row = new Row();
		int id = 0;
		try {
			id = (int) RowID.get(ec, profile_table, "ID");
		} catch (DBException ex) {

		}
		
		row.add(new CInteger("ID", new Integer(id)));
		row.add(new CString("NAME", profile.getName()));
		

		isql.setRow(row);
		isql.setTable(profile_table);

		try {
			ec.doUpdate(isql);
		} catch (DBException dbException) {
			TrivialExceptionHandler.ignore(this, dbException);
		}

		for(String key: profile.getData().keySet()){
			InsertQuery data_isql = new InsertQuery();
			data_isql.setTable(data_table);
			Row dataRow = new Row();
			try {
				dataRow.add(new CInteger("ID", new Integer((int) RowID.get(ec, data_table, "ID"))));
				dataRow.add(new CInteger("PROFILE_ID", id));
				dataRow.add(new CString("KEY", key));
				dataRow.add(new CString("VALUE", profile.getData().get(key)));
				data_isql.setRow(dataRow);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			try {
				System.out.println(data_isql.getSQL(ec.getDBSession()));
				ec.doUpdate(data_isql);
			} catch (DBException dbException) {
				TrivialExceptionHandler.ignore(this, dbException);
			}

		}
	}*/
	
	public void removeProfile(String userID) throws ResourceException {
		{
			final DeleteQuery dq = new DeleteQuery();
			dq.setTable(profile_table);
			dq.constrain(
				new CanonicalColumnName(profile_table, "ID"), 
				QConstraint.CT_EQUALS, Integer.valueOf(userID)
			);
			
			try {
				ec.doUpdate(dq);
			} catch (DBException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
		
		{
			final DeleteQuery data_dq = new DeleteQuery();
			data_dq.setTable(profile_table);
			data_dq.constrain(
				new CanonicalColumnName(data_table, "PROFILE_ID"), 
				QConstraint.CT_EQUALS, Integer.valueOf(userID)
			);
			
			try {
				ec.doUpdate(data_dq);
			} catch(DBException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
	}
	
	public void removeField(String userID, String fieldID) throws ResourceException {
		final DeleteQuery query = new DeleteQuery();
		query.setTable(data_table);
		query.constrain(
			new CanonicalColumnName(data_table, "ID"), 
			QConstraint.CT_EQUALS, Integer.valueOf(fieldID)
		);
		
		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	public String addField(String userID, String fieldKey, String fieldValue) throws ResourceException {
		final SelectQuery check = new SelectQuery();
		check.select(data_table, "ID");
		check.constrain(
			new CanonicalColumnName(data_table, "PROFILE_ID"), 
			QConstraint.CT_EQUALS, Integer.valueOf(userID)
		);
		check.constrain(
			new CanonicalColumnName(data_table, "KEY"), 
			QConstraint.CT_EQUALS, fieldKey
		);
		
		final Row.Loader rl = new Row.Loader();
		
		try {
			ec.doQuery(check, rl);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		if (rl.getRow() != null) {
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, 
				"Field " + fieldKey + " already exists with id " + 
				rl.getRow().get("ID").toString()
			);
		}
		
		final int id;
		final Row row = new Row();
		try {
			row.add(new CInteger("ID", new Integer(id = (int) RowID.get(ec, data_table, "ID"))));
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		row.add(new CInteger("PROFILE_ID", Integer.valueOf(userID)));
		row.add(new CString("KEY", fieldKey));
		row.add(new CString("VALUE", fieldValue));
		
		final InsertQuery query = new InsertQuery();
		query.setTable(data_table);
		query.setRow(row);
		
		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		return Integer.toString(id);
	}
	
	public void updateUser(String userID, String username) throws ResourceException {
		if (userExists(username))
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
		
		final Row row = new Row();
		row.add(new CString("NAME", username));
		
		final UpdateQuery query = new UpdateQuery();
		query.setTable(profile_table);
		query.setRow(row);
		query.constrain(
			new CanonicalColumnName(profile_table, "ID"), 
			QConstraint.CT_EQUALS, Integer.valueOf(userID)
		);
		
		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	public void updateField(String userID, String fieldID, String value) throws ResourceException {
		final Row row = new Row();
		row.add(new CString("VALUE", value));

		final UpdateQuery query = new UpdateQuery();
		query.setTable(profile_table);
		query.setRow(row);
		query.constrain(
			new CanonicalColumnName(data_table, "ID"), 
			QConstraint.CT_EQUALS, Integer.valueOf(fieldID)
		);
		
		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	public String addUser(String userName) throws ResourceException {
		if (userExists(userName))
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, 
				"Username " + userName + " already exists."
			);
		
		final Row row = new Row();
		final int id; 
		try {
			id = (int) RowID.get(ec, profile_table, "ID");
		} catch (DBException ex) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex);
		}
		
		row.add(new CInteger("ID", Integer.valueOf(id)));
		row.add(new CString("NAME", userName));
		
		final InsertQuery query = new InsertQuery();
		query.setTable(profile_table);
		query.setRow(row);
		
		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		return Integer.toString(id);
	}
	
	public Map<String, String> getUserMapping() throws ResourceException {
		final Map<String, String> map = new HashMap<String, String>();
		
		final SelectQuery query = new SelectQuery();
		query.select(profile_table, "*");
		
		final Row.Set rs = new Row.Set();
		
		try {
			ec.doQuery(query, rs);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		for (Row row : rs.getSet())
			map.put(row.get("ID").toString(), row.get("NAME").toString());
		
		return map;	
	}
	
	/*
	public void updateProfile(String username, Profile profile) {
		removeProfile(username);
		addProfile(profile);
		System.out.println(profile.toXML());
		
	}
	*/
	
}
