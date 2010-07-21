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
package com.solertium.util.restlet.usermodel.groups.managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.solertium.db.query.QRelationConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.Replacer;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.usermodel.groups.core.Group;

/**
 * H2GroupManager.java
 * 
 * Database implementation of a group manager. 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class H2GroupManager implements GroupManager {
	
	private final ExecutionContext ec;
	private final String group_table;
	private final String user_table;
	
	public H2GroupManager(String location) {
		group_table = location + "_GROUP";
		user_table = location + "_USER";
		
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
	
	public H2GroupManager(final String location, final ExecutionContext ec) {
		this.group_table = location + "_GROUP";
		this.user_table = location + "_USER";
		
		this.ec = ec;
		
		init();
	}
	
	private void init() {
		try {
			InputStream stream = H2GroupManager.class.getResourceAsStream("structure.xml");
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String xml="";
			try{
				while(reader.ready()){
					xml += reader.readLine() +'\n';
				}
				xml = Replacer.replace(xml, "${group_table}", group_table);
				xml = Replacer.replace(xml, "${user_table}", user_table);
				
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
	
	/**
	 * @return the group_table
	 */
	public String getGroupTable() {
		return group_table;
	}
	
	/**
	 * @return the user_table
	 */
	public String getUserTable() {
		return user_table;
	}
	
	public boolean groupExists(String groupName) throws ResourceException {
		final SelectQuery query = new SelectQuery();
		query.select(group_table, "ID");
		query.constrain(
			new CanonicalColumnName(group_table, "NAME"), 
			QConstraint.CT_EQUALS, groupName
		);
		
		final Row.Loader rl = new Row.Loader();
		
		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		return rl.getRow() != null;
	}
	
	public String addGroup(String groupName) throws ResourceException {
		if (groupExists(groupName))
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
		
		System.out.println("inserting group");
		
		final Row row = new Row();
		int id = 0;
		try {
			id = (int) RowID.get(ec, group_table, "ID");
		} catch (DBException ex) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex);
		}
		
		row.add(new CInteger("ID", new Integer(id)));
		row.add(new CString("NAME", groupName));
		
		final InsertQuery isql = new InsertQuery();
		isql.setRow(row);
		isql.setTable(group_table);

		try {
			ec.doUpdate(isql);
		} catch (DBException dbException) {
			TrivialExceptionHandler.ignore(this, dbException);
		}

		return Integer.toString(id);
		/*
		for(String user: group.getUsers()){
			

		}
		*/
	}
	
	public String addUser(String groupID, String userID) throws ResourceException {
		int id;
		
		final Row dataRow = new Row();
		try {
			dataRow.add(new CInteger("ID", new Integer(id = (int) RowID.get(ec, user_table, "ID"))));
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		dataRow.add(new CInteger("GROUP_ID", Integer.valueOf(groupID)));
		dataRow.add(new CInteger("USER_ID", Integer.valueOf(userID)));
		
		final InsertQuery data_isql = new InsertQuery();
		data_isql.setTable(user_table);
		data_isql.setRow(dataRow);
		
		try {
			ec.doUpdate(data_isql);
		} catch (DBException dbException) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, dbException);
		}
		
		return Integer.toString(id);
	}
	
	public Group getGroup(String groupID) throws ResourceException {
		final SelectQuery query = new SelectQuery();
		query.select(group_table, "*");
		query.constrain(
			new CanonicalColumnName(group_table, "ID"), 
			QConstraint.CT_EQUALS, Integer.valueOf(groupID)
		);
		
		Row.Loader rl = new Row.Loader();
		
		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		if (rl.getRow() == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		final Group group = new Group(
			rl.getRow().get("NAME").toString(),
			rl.getRow().get("ID").toString()
		);
		
		final SelectQuery data_query = new SelectQuery();
		data_query.select(user_table, "*");
		data_query.constrain(
			new CanonicalColumnName(user_table, "GROUP_ID"), 
			QConstraint.CT_EQUALS, Integer.valueOf(groupID)
		);
			
		final Row.Set data_rs = new Row.Set();
		
		try {
			ec.doQuery(data_query, data_rs);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		for (Row dbRow : data_rs.getSet())
			group.addUser(dbRow.get("USER_ID").toString());
		
		return group;
	}
	
	public void removeUser(String groupID, String userRowID) throws ResourceException {
		final DeleteQuery query = new DeleteQuery();
		query.setTable(user_table);
		query.constrain(
			new CanonicalColumnName(user_table, "ID"), 
			QConstraint.CT_EQUALS, Integer.valueOf(userRowID)
		);
		
		try {
			ec.doUpdate(query); 
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	public void removeGroup(String groupID) throws ResourceException {
		{
			final DeleteQuery dq = new DeleteQuery();
			dq.setTable(group_table);
			dq.constrain(
				new CanonicalColumnName(group_table, "ID"), 
				QConstraint.CT_EQUALS, Integer.valueOf(groupID)
			);
			try {
				ec.doUpdate(dq);
			} catch (DBException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
		{
			final DeleteQuery data_dq = new DeleteQuery();
			data_dq.setTable(user_table);
			data_dq.constrain(
				new CanonicalColumnName(user_table, "GROUP_ID"), 
				QConstraint.CT_EQUALS, user_table
			);
			try{
				ec.doUpdate(data_dq);
			} catch (DBException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
	}
	
	public void updateGroup(String groupID, String groupName) throws ResourceException {
		if (groupExists(groupName))
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, 
				"Group " + groupName + " already exists."
			);
		
		final Row row = new Row();
		row.add(new CString("NAME", groupName));
		
		final UpdateQuery query = new UpdateQuery();
		query.setTable(group_table);
		query.setRow(row);
		query.constrain(
			new CanonicalColumnName(group_table, "ID"), 
			QConstraint.CT_EQUALS, Integer.valueOf(groupID)
		);
		
		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	public Group getGroupForUser(String userID) throws ResourceException {
		final SelectQuery query = new SelectQuery();
		query.select(group_table, "ID");
		query.join(user_table, new QRelationConstraint(
			new CanonicalColumnName(user_table, "GROUP_ID"), 
			new CanonicalColumnName(group_table, "ID")
		));
		query.constrain(
			new CanonicalColumnName(user_table, "USER_ID"), 
			QConstraint.CT_EQUALS, Integer.valueOf(userID)
		);
		
		final Row.Loader rl = new Row.Loader();
		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			return null;
		}
		
		if (rl.getRow() == null)
			return null;
		
		return getGroup(rl.getRow().get("ID").getString());
	}
	
	public Map<String, String> getGroupMapping() throws ResourceException {
		final SelectQuery query = new SelectQuery();
		query.select(group_table, "*");
		
		final Row.Set rs = new Row.Set();
		
		try {
			ec.doQuery(query, rs);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final HashMap<String, String> profiles = new HashMap<String, String>();
		for (Row row : rs.getSet())
			profiles.put(row.get("ID").toString(), row.get("NAME").getString());
			
		return profiles;
	}

}
