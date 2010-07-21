package org.iucn.sis.server.extensions.user.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.db.CInteger;
import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowID;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.NodeCollection;
import com.solertium.util.SysDebugger;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * ImportFromVFS.java
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public class ImportFromVFS {

	private static final SysDebugger debug = SysDebugger.getNamedInstance("debug");

	private static final VFSPath USERS_DIR = new VFSPath("/users");
	private static final VFSPathToken PROFILE = new VFSPathToken("profile.xml");

	private Integer addUser(String username, ExecutionContext ec) throws DBException, NullPointerException {
		final Integer id;

		final Row row = ec.getRow("user");
		row.get("id").setObject(id = Integer.valueOf((int) RowID.get(ec, "user", "id")));
		row.get("username").setObject(username);

		final InsertQuery query = new InsertQuery();
		query.setTable("user");
		query.setRow(row);

		debug.println("- " + query.getSQL(ec.getDBSession()));
		ec.doUpdate(query);

		return id;
	}

	private Integer createCustomField(String fieldname, ExecutionContext ec) throws DBException, NullPointerException {
		final Integer id;

		final Row row = ec.getRow("customfield");
		row.get("id").setObject(id = Integer.valueOf((int) RowID.get(ec, "customfield", "id")));
		row.get("name").setObject(fieldname);
		row.get("required").setObject("false");
		row.get("type").setObject("text");

		final InsertQuery query = new InsertQuery();
		query.setRow(row);
		query.setTable("customfield");

		debug.println("- " + query.getSQL(ec.getDBSession()));
		ec.doUpdate(query);

		return id;
	}

	private ArrayList<String> getExistingUsers(ExecutionContext ec) throws DBException {
		final ArrayList<String> list = new ArrayList<String>();

		final SelectQuery query = new SelectQuery();
		query.select("user", "username");

		final Row.Set rs = new Row.Set();

		ec.doQuery(query, rs);

		for (Row row : rs.getSet())
			list.add(row.get("username").toString());

		return list;
	}

	/**
	 * Going to assume that all usernames are unique!
	 */
	public void importUsers(VFS vfs, ExecutionContext ec) {
		final ArrayList<String> nodesToSkip = new ArrayList<String>();
		nodesToSkip.add("first");
		nodesToSkip.add("last");

		final ArrayList<String> usersToSkip = new ArrayList<String>();
		usersToSkip.add("_userTemplates");

		final VFSPathToken[] tokens;
		try {
			tokens = vfs.list(USERS_DIR);
		} catch (NotFoundException e) {
			debug.println("Error loading VFS data: " + e.getMessage());
			return;
		}

		final Row template;
		try {
			template = ec.getRow("profile");
		} catch (DBException e) {
			return;
		}

		final ArrayList<String> existingUsers;
		try {
			existingUsers = getExistingUsers(ec);
		} catch (DBException e) {
			return;
		}

		final HashMap<String, Integer> customFields = new HashMap<String, Integer>();

		for (final VFSPathToken token : tokens) {
			if (usersToSkip.contains(token.toString()) || existingUsers.contains(token.toString()))
				continue;

			boolean isCollection = false;
			try {
				isCollection = vfs.isCollection(USERS_DIR.child(token));
			} catch (IOException e) {
				continue;
			}
			if (!isCollection)
				continue;

			final Document document;
			try {
				document = vfs.getDocument(USERS_DIR.child(token).child(PROFILE));
			} catch (IOException e) {
				continue;
			}

			final Integer userID;
			try {
				userID = addUser(token.toString(), ec);
			} catch (DBException e) {
				return;
			} catch (NullPointerException e) {
				return;
			}

			final Row profileRow = new Row();
			try {
				profileRow.add(new CInteger("id", Integer.valueOf((int) RowID.get(ec, "profile", "id"))));
			} catch (DBException e) {
				return;
			}
			profileRow.add(new CInteger("userid", userID));

			final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());
			for (Node node : nodes) {
				if (node.getNodeType() == Node.TEXT_NODE)
					continue;

				Column col = template.get(node.getNodeName());
				if (col != null) {
					col.setObject(node.getTextContent());
					profileRow.add(col);
				} else {
					if (nodesToSkip.contains(node.getNodeName()))
						continue;

					Integer customFieldID = customFields.get(node.getNodeName());
					if (customFieldID == null) {
						try {
							customFieldID = createCustomField(node.getNodeName(), ec);
							customFields.put(node.getNodeName(), customFieldID);
						} catch (DBException e) {
							continue;
						} catch (NullPointerException e) {
							e.printStackTrace();
							continue;
						}
					}

					final Row customFieldDataRow;
					try {
						customFieldDataRow = ec.getRow("customfielddata");
						customFieldDataRow.get("id").setObject(
								Integer.valueOf((int) RowID.get(ec, "customfielddata", "id")));
						customFieldDataRow.get("userid").setObject(userID);
						customFieldDataRow.get("fieldid").setObject(customFieldID);
						customFieldDataRow.get("value").setObject(node.getTextContent());
					} catch (DBException e) {
						continue;
					}

					final InsertQuery query = new InsertQuery();
					query.setRow(customFieldDataRow);
					query.setTable("customfielddata");

					try {
						debug.println("- " + query.getSQL(ec.getDBSession()));
						ec.doUpdate(query);
					} catch (DBException e) {
						debug.println("Adding custom field failed: " + e.getMessage());
						TrivialExceptionHandler.ignore(this, e);
					}
				}
			}

			final InsertQuery query = new InsertQuery();
			query.setTable("profile");
			query.setRow(profileRow);

			try {
				debug.println("- " + query.getSQL(ec.getDBSession()));
				ec.doUpdate(query);
			} catch (DBException e) {
				debug.println("Adding user " + token + " failed: " + e.getMessage());
			}
		}
	}

}
