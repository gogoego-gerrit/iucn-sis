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
package com.solertium.gogoego.server.lib.app.tags.container;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.naming.NamingException;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.applications.GoGoEgoApplicationManagement;
import org.gogoego.api.applications.HasSettingsUI;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.DocumentUtils;
import org.gogoego.api.utils.ProductProperties;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.routing.Router;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.db.DBException;
import com.solertium.db.DBSession;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.vendor.H2DBSession;
import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.applications.ServerApplicationImpl;
import com.solertium.gogoego.server.lib.app.tags.resources.BaseTagResource;
import com.solertium.gogoego.server.lib.app.tags.resources.DumpResource;
import com.solertium.gogoego.server.lib.app.tags.resources.TagBrowseResource;
import com.solertium.gogoego.server.lib.app.tags.resources.TagResource;
import com.solertium.gogoego.server.lib.app.tags.resources.TagSearchEngineResource;
import com.solertium.gogoego.server.lib.app.tags.resources.TagSettingsResource;
import com.solertium.gogoego.server.lib.app.tags.resources.TagUtilityResource;
import com.solertium.gogoego.server.lib.app.tags.resources.admin.AvailableRegistryDataResource;
import com.solertium.gogoego.server.lib.app.tags.resources.admin.AvailableRegistryResource;
import com.solertium.gogoego.server.lib.app.tags.resources.admin.WritableTagGroupResource;
import com.solertium.gogoego.server.lib.app.tags.resources.admin.WritableTagListResource;
import com.solertium.gogoego.server.lib.app.tags.resources.global.TagGroupResource;
import com.solertium.gogoego.server.lib.app.tags.resources.global.TagListResource;
import com.solertium.gogoego.server.lib.app.tags.utils.CollectionTagger;
import com.solertium.gogoego.server.lib.app.tags.utils.TagDatabaseUtils;
import com.solertium.util.NodeCollection;
import com.solertium.util.Replacer;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPathToken;

/**
 * TagApplication.java
 * 
 * Provides the GoGoEgoApplication implementation and integration points, and
 * hooks in all the restlets and resources to their appropriate resource space.
 * 
 * Properties;
 * 
 * GOGOEGO_TAGS_DBLOCATION - The folder where tags are stored. It is assumed
 * that this folder is directly below the vmroot.
 * 
 * GOGOEGO_TAGS_DBNAME - The name of the database.
 * 
 * @author carl.scott
 * 
 */
public class TagApplication extends GoGoEgoApplication implements GoGoEgoApplicationManagement, HasSettingsUI {

	public static final String REGISTRATION = "com.solertium.gogoego.server.lib.app.tags.container.TagApplication";

	private final VFSPathToken dbLocation;
	private final VFSPathToken dbName;

	private ExecutionContext ec;

	/**
	 * @param application
	 */
	public TagApplication() {
		VFSPathToken dbLocation;
		try {
			dbLocation = new VFSPathToken(ProductProperties.getProperties().getProperty(
					"GOGOEGO_TAGS_DBLOCATION"));
		} catch (NullPointerException e) {
			dbLocation = new VFSPathToken("(db)");
		} catch (IllegalArgumentException e) {
			dbLocation = new VFSPathToken("(db)");
		}

		this.dbLocation = dbLocation;

		VFSPathToken dbName;
		try {
			dbName = new VFSPathToken(ProductProperties.getProperties().getProperty("GOGOEGO_TAGS_DBNAME"));
		} catch (NullPointerException e) {
			dbName = new VFSPathToken("tagdb");
		} catch (IllegalArgumentException e) {
			dbName = new VFSPathToken("tagdb");
		}

		this.dbName = dbName;
	}

	public void init(ServerApplicationImpl application) {
		this.app = application;
	}

	public ExecutionContext getExecutionContext() {
		return ec;
	}

	public Restlet getPublicRouter() {
		if (ec == null)
			return null;

		Router root = new Router(app.getContext());

		// Information about tags
		root.attach("/list", TagListResource.class);
		root.attach("/list/{tagID}", TagListResource.class);

		// Information about tag groups
		root.attach("/groups", TagGroupResource.class);
		root.attach("/groups/{groupID}", TagGroupResource.class);

		root.attach("/dump", DumpResource.class);
		root.attach("/dump/{tableName}", DumpResource.class);

		root.attach("/browse/{mode}", TagBrowseResource.class);

		root.attach("/create", TagResource.class);

		root.attach("/fetch", TagResource.class);

		root.attach("/search", TagSearchEngineResource.class);
		/*
		 * The functions below are dangerous and should not be used by the
		 * general public quite yet.
		 */
		/*
		 * root.attach("/evolve", TagUtilityResource.class);
		 * root.attach("/clean", TagUtilityResource.class);
		 */

		root.attach("/upgrade", TagUtilityResource.class);

		return root;
	}

	public Restlet getPrivateRouter() {
		if (ec == null)
			return null;

		Router admin = new Router(app.getContext());

		admin.attach("/settings", TagSettingsResource.class);

		admin.attach("/list", WritableTagListResource.class);

		admin.attach("/groups", WritableTagGroupResource.class);
		admin.attach("/groups/{groupID}", WritableTagGroupResource.class);

		admin.attach("/available", AvailableRegistryResource.class);
		admin.attach("/available/{protocol}", AvailableRegistryResource.class);
		admin.attach("/available/{protocol}/{id}", AvailableRegistryDataResource.class);

		return admin;
	}
	
	public String getSettingsURL() {
		return "local";
	}

	public String getSiteID() {
		return app.getSiteID();
	}

	public VFS getVFS() {
		return app.getVFS();
	}

	public boolean isInstalled() {
		final String tagDBName = "tags";//app.getSiteID() + "_tags";
		try {
			final String vmroot = GoGoEgo.getInitProperties().getProperty("GOGOEGO_VMROOT");
			final String location = vmroot + File.separatorChar + dbLocation + File.separatorChar + dbName;
			
			if (!DBSessionFactory.isRegistered(tagDBName)) {
				GoGoDebug.get("config").println("Registering db at " + location);
				DBSessionFactory.registerDataSource(tagDBName, "jdbc:h2:file:" + location, "org.h2.Driver", "sa", "");
			}
			else
				GoGoDebug.get("config").println("Tag db already registered for " + location);

			ec = new SystemExecutionContext(tagDBName);

			DBSession dbs = ec.getDBSession();
			if (dbs instanceof H2DBSession)
				dbs.setIdentifierCase(DBSession.CASE_UNCHECKED);

			ec.setExecutionLevel(ExecutionContext.ADMIN);
			ec.setAPILevel(ExecutionContext.SQL_ALLOWED);

			try {
				// A table already existing is OK
				ec.appendStructure(getStructureDocument(app.getSiteID()), false);
			} catch (DBException f) {
				ec = null;
				f.printStackTrace();
				return false;
			}

			app.getApplicationEvents().register(Method.PROPFIND, "",
					new CollectionTagger(app.getSiteID(), ec, app.getVFS()));

			return true;
		} catch (NamingException e) {
			ec = null;
			e.printStackTrace();
			return false;
		}
	}

	public static Document getStructureDocument(String siteID) {
		String xml;
		final BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(TagApplication.class.getResourceAsStream("struct.xml")));
		} catch (NullPointerException e) {
			return null;
		}

		try {
			StringBuilder builder = new StringBuilder();
			String in;
			while ((in = reader.readLine()) != null)
				builder.append(in);

			xml = builder.toString();
		} catch (IOException e) {
			xml = null;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				TrivialExceptionHandler.ignore(reader, e);
			}
		}

		if (xml == null)
			return null;

		// TODO: make this smarter...???
		return DocumentUtils.impl.createDocumentFromString(Replacer.replace(xml, "${siteID}", siteID));
	}

	/**
	 * Not too much to do here that's not handled in isInstalled().
	 */
	public void install(VFS vfs) throws GoGoEgoApplicationException {
		final String siteID = ((ServerApplication)Application.getCurrent()).getInstanceId();
		final String vmroot = GoGoEgo.getInitProperties().getProperty("GOGOEGO_VMROOT");

		if (vmroot == null)
			throw new GoGoEgoApplicationException("Error finding database location");

		GoGoDebug.get("config").println("Registering db at " + vmroot);

		final ExecutionContext ec;
		final String tagDBName = "tags";//siteID + "_tags";

		if (!DBSessionFactory.isRegistered(tagDBName)) {
			try {
				DBSessionFactory.registerDataSource(tagDBName, "jdbc:h2:file:" + vmroot + File.separatorChar + (dbLocation)
						+ File.separatorChar + dbName, "org.h2.Driver", "sa", "");
			} catch (NamingException e) {
				throw new GoGoEgoApplicationException("Unlikely error in naming (1)", e);
			}
		}

		try {
			ec = new SystemExecutionContext(tagDBName);
		} catch (NamingException e) {
			DBSessionFactory.unregisterDataSource(tagDBName);
			throw new GoGoEgoApplicationException("Unlikely error in naming (2)", e);
		}

		DBSession dbs = ec.getDBSession();
		if (dbs instanceof H2DBSession)
			dbs.setIdentifierCase(DBSession.CASE_UNCHECKED);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);

		try {
			// A table already existing is OK
			ec.appendStructure(getStructureDocument(siteID), true);
		} catch (DBException f) {
			DBSessionFactory.unregisterDataSource(tagDBName);
			throw new GoGoEgoApplicationException("Could not build database tables.");
		}

		/*
		 * Add a default tag group that is at the all tags level. It will
		 * cascade.
		 */
		{
			try {
				final String table = BaseTagResource.convertTableBySite(BaseTagResource.TABLE_GROUPS, siteID);
				final Row row = ec.getRow(table);
				row.get("id").setObject(
						Integer.valueOf((int) TagDatabaseUtils.getRowID(siteID).get(ec, table, "id")));
				row.get("name").setObject("default");

				final InsertQuery query = new InsertQuery();
				query.setTable(table);
				query.setRow(row);

				ec.doUpdate(query);
			} catch (DBException e) {
				TrivialExceptionHandler.ignore(this, e);
			}

			// TODO: filter stuff
		}
	}

	/**
	 * Remove appropriate database tables.
	 */
	public void uninstall(VFS vfs) throws GoGoEgoApplicationException {
		final String siteID = ((ServerApplication)Application.getCurrent()).getInstanceId();
		final String vmroot = GoGoEgo.getInitProperties().getProperty("GOGOEGO_VMROOT");

		if (vmroot == null)
			throw new GoGoEgoApplicationException("Error finding database location");

		GoGoDebug.get("config").println("Registering db at " + vmroot);

		final ExecutionContext ec;
		final String tagDBName = "tags";//siteID + "_tags";

		if (!DBSessionFactory.isRegistered(tagDBName)) {
			try {
				DBSessionFactory.registerDataSource(tagDBName, "jdbc:h2:file:" + vmroot + File.separatorChar + (dbLocation)
						+ File.separatorChar + dbName, "org.h2.Driver", "sa", "");
			} catch (NamingException e) {
				throw new GoGoEgoApplicationException("Unlikely error in naming (1)", e);
			}
		}

		try {
			ec = new SystemExecutionContext(tagDBName);
		} catch (NamingException e) {
			DBSessionFactory.unregisterDataSource(tagDBName);
			throw new GoGoEgoApplicationException("Unlikely error in naming (2)", e);
		}

		DBSession dbs = ec.getDBSession();
		if (dbs instanceof H2DBSession)
			dbs.setIdentifierCase(DBSession.CASE_UNCHECKED);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);

		final Document struct = getStructureDocument(siteID);

		final NodeCollection nodes = new NodeCollection(struct.getDocumentElement().getChildNodes());

		for (Node node : nodes) {
			if (node.getNodeName().equals("table")) {
				String name = DocumentUtils.impl.getAttribute(node, "name");
				if (!"".equals(name))
					try {
						GoGoDebug.get("fine").println("Deleting table {0}", name);
						ec.doUpdate("DROP TABLE " + name);
					} catch (DBException e) {
						GoGoDebug.get("debug").println("Error on {0}: {1}", name, e.getMessage());
						TrivialExceptionHandler.ignore(this, e);
					}
			}
		}
	}

}
