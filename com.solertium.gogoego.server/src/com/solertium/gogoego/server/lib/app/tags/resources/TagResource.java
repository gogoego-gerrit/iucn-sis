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
package com.solertium.gogoego.server.lib.app.tags.resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.db.CLong;
import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.StaticRowID;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.ExperimentalSelectQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;
import com.solertium.db.utils.QueryUtils;
import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.tags.container.TagApplication;
import com.solertium.gogoego.server.lib.app.tags.utils.TagData;
import com.solertium.gogoego.server.lib.app.tags.utils.TagDatabaseUtils;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * TagResource.java
 * 
 * Handles all means of reading and writing tag information to a given uri. Tag
 * information is stored in a .tags.xml file located in the parent folder of the
 * given resource.
 * 
 * @author carl.scott
 * 
 */
public class TagResource extends BaseTagResource {

	private final VFS vfs;
	private final VFSPath uri;

	private final TagApplication app;

	public TagResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);

		uri = new VFSPath(request.getResourceRef().getRemainingPart());

		vfs = ServerApplication.getFromContext(context).getVFS();

		app = ((TagApplication)ServerApplication.getFromContext(context, TagApplication.REGISTRATION));
	}

	public Representation represent(Variant variant) throws ResourceException {
		try {
			if (vfs.isCollection(uri))
				throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		} catch (NotFoundException e) {
			GoGoDebug.get("finest").println("Resource does not exist at given VFSPath: " + uri);
		}

		return new DomRepresentation(variant.getMediaType(), QueryUtils.writeDocumentFromRowSet(getAllTags()));
	}

	/**
	 * Fetches the row ID, possibly adding a new entry
	 * 
	 * @param filename
	 * @param ec
	 * @return
	 */
	private String getResourceID() {
		return TagDatabaseUtils.getURIID(app.getSiteID(), convertTable(TABLE_RESOURCEURIS), uri.toString(), ec);
	}

	public List<Row> getAllTags() throws ResourceException {
		try {
			return getTagsForURI(uri, siteID, ec);
		} catch (DBException e) {
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

	private void updateLastModified(String resourceID) {
		final String table = convertTable(TABLE_RESOURCEURIS);

		final Row row = new Row();
		row.add(new CLong("lasttagged", Long.valueOf(Calendar.getInstance().getTimeInMillis())));

		final UpdateQuery query = new UpdateQuery();
		query.setTable(table);
		query.setRow(row);
		query.constrain(new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, resourceID);

		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}

	public static List<Row> getTagsForURI(VFSPath uri, String siteID, ExecutionContext ec) throws DBException {
		return getTagsForURI(uri, siteID, null, ec);
	}

	public static List<Row> getTagsForURI(VFSPath uri, String siteID, Long lastTagged, ExecutionContext ec)
			throws DBException {
		List<Row> list;

		// Get normal tags...
		{
			final String tagTable = convertTableBySite(BaseTagResource.TABLE_TAGS, siteID);
			final String resourceTable = convertTableBySite(BaseTagResource.TABLE_RESOURCEURIS, siteID);

			ExperimentalSelectQuery query = new ExperimentalSelectQuery();
			query.select(tagTable, "id");
			query.select(tagTable, "name");
			query.select(tagTable, "attributes");
			query.select(resourceTable, "lasttagged");
			query.constrain(new CanonicalColumnName(resourceTable, "uri"), QConstraint.CT_EQUALS, uri.toString());
			if (lastTagged != null)
				query.constrain(QConstraint.CG_AND, new CanonicalColumnName(resourceTable, "lasttagged"),
						QConstraint.CT_GT, lastTagged.toString());

			Row.Set rs = new Row.Set();

			ec.doQuery(query, rs);

			list = rs.getSet();
		}

		// Get default tags...
		{
			final String table = convertTableBySite(TABLE_DEFAULTTAGS, siteID);
			final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
			query.select(table, "tagid as id");
			query.select(convertTableBySite(TABLE_TAGS, siteID), "name");
			query.constrain(new CanonicalColumnName(table, "uri"), QConstraint.CT_EQUALS, uri.getCollection()
					.toString());

			final Row.Set rs = new Row.Set();
			try {
				ec.doQuery(query, rs);
			} catch (DBException e) {
				TrivialExceptionHandler.ignore(query, e);
			}

			final Iterator<Row> results = rs.getSet().iterator();
			while (results.hasNext()) {
				Row current = results.next();
				current.add(new CString("default", "true"));
				list.add(current);
			}
		}

		return list;
	}

	/**
	 * <root> <tag> {id} <tag> </root>
	 */
	public void storeRepresentation(Representation entity) throws ResourceException {
		try {
			if (vfs.isCollection(uri))
				throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		} catch (Exception e) {
			GoGoDebug.get("finest").println("Resource does not exist at given VFSPath: " + uri);
		}

		// Existing tags from DB
		final Set<Integer> tagsInDatabase = new TreeSet<Integer>();
		final Set<Integer> defaultTags = new TreeSet<Integer>();
		// Tags submitted from the client
		final Set<Integer> tagsSubmitted = new TreeSet<Integer>();

		final List<Row> tagRowsInDatabase = getAllTags();
		for (Iterator<Row> iterator = tagRowsInDatabase.iterator(); iterator.hasNext();) {
			final Row row = iterator.next();
			final Integer id = row.get("id").getInteger();

			if (row.get("default") == null)
				tagsInDatabase.add(id);
			else
				defaultTags.add(id);
		}

		/*
		 * Now we check the remove all tags that are in the default tags table
		 * that are already stored as tags in the database from a previous
		 * insertion.
		 * 
		 * Once removed, the remaining default tags are added to the list of
		 * tags submitted so that they will be inserted upon save.
		 */
		defaultTags.removeAll(tagsInDatabase);
		tagsSubmitted.addAll(defaultTags);

		{
			final Document submitted = getDocument(entity);
			final NodeCollection nodes = new NodeCollection(submitted.getDocumentElement().getChildNodes());
			for (Node current : nodes)
				if (current.getNodeName().equals("tag"))
					tagsSubmitted.add(new Integer(current.getTextContent()));
		}

		/*
		 * I am assuming that the document submitted is the authorative
		 * document, so any tags that are not in the document should be removed
		 * from the DB, and any tags that are there should be added if they are
		 * not in the DB.
		 * 
		 * Iterate through submitted tags, adding ones that are not in the
		 * tagsInDatabase list...
		 */
		final Integer resourceID;
		try {
			resourceID = new Integer(getResourceID());
		} catch (NumberFormatException impossible) {
			TrivialExceptionHandler.impossible(this, impossible);
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}

		final String insertTable = convertTable(TABLE_RESOURCETAGS);

		final Iterator<Integer> iterator = tagsSubmitted.iterator();
		while (iterator.hasNext()) {
			final Integer currentTagID = iterator.next();
			if (!tagsInDatabase.remove(currentTagID)) {
				// This tag was submitted but is not in the DB
				// TODO: write insert query.
				final InsertQuery query = new InsertQuery();
				query.setTable(insertTable);

				final Row template;
				try {
					template = ec.getRow(insertTable);
					template.get("id").setObject(Integer.valueOf((int) getRowID().get(ec, insertTable, "id")));
					template.get("tagid").setObject(currentTagID);
					template.get("uriid").setObject(resourceID);

					query.setRow(template);
				} catch (DBException e) {
					e.printStackTrace();
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}

				try {
					ec.doUpdate(query);
				} catch (DBException e) {
					GoGoDebug.get("fine").println("Could not insert tag {0}", currentTagID);
					TrivialExceptionHandler.ignore(this, e);
				}
			}
		}

		/*
		 * Now, iterate through the remaining tags and delete 'em
		 */
		final String deleteTable = convertTable(TABLE_RESOURCETAGS);
		final Iterator<Integer> removeIterator = tagsInDatabase.iterator();
		while (removeIterator.hasNext()) {
			final Integer currentTagID = removeIterator.next();

			final DeleteQuery query = new DeleteQuery();
			query.setTable(deleteTable);
			query.constrain(new CanonicalColumnName(deleteTable, "tagid"), QConstraint.CT_EQUALS, currentTagID);
			query.constrain(QConstraint.CG_AND, new CanonicalColumnName(deleteTable, "uriid"), QConstraint.CT_EQUALS,
					resourceID);

			try {
				ec.doUpdate(query);
			} catch (DBException e) {
				GoGoDebug.get("fine").println("Could not delete tag {0}", currentTagID);
				TrivialExceptionHandler.ignore(this, e);
			}
		}

		updateLastModified(resourceID.toString());
	}

	/**
	 * <root> <file name="..."> <tag>...</tag> </file>
	 * 
	 * 
	 * </root>
	 * 
	 * @return
	 */
	private Document getTagDocument() {
		return null;
		/*
		 * Document doc; try { doc = vfs.getDocument(uri.child(new
		 * VFSPathToken(".tags.xml"))); } catch (IOException e) { doc = null; }
		 * 
		 * return doc == null ?
		 * DocumentUtils.impl.createDocumentFromString("<root></root>") : doc;
		 */
	}

	private static class PowerTagData extends TagData {
		public PowerTagData(String filename) {
			super(filename);
		}

		public ArrayList<String> getAllTags() {
			return tags;
		}
	}

	public void updateDB() {
		final Document tagDoc = getTagDocument();

		/*
		 * Find all the default tags for the given folder. Note that we are
		 * guaranteed that the uri is a collection.
		 */
		final ArrayList<String> defaultTags = new ArrayList<String>();
		final NodeList defaultNodes = tagDoc.getElementsByTagName("default");
		for (int i = 0; i < defaultNodes.getLength(); i++) {
			Node current = defaultNodes.item(i);
			if (current.getNodeName().equals("default"))
				defaultTags.add(current.getTextContent());
		}

		/*
		 * Now, look through the tag document for all files that have tags.
		 * Create TagData for these and add the default tags to them.
		 */
		final HashMap<String, PowerTagData> tagMap = new HashMap<String, PowerTagData>();
		final NodeList fileNodes = tagDoc.getElementsByTagName("file");
		for (int i = 0; i < fileNodes.getLength(); i++) {
			Node current = fileNodes.item(i);
			if (current.getNodeName().equals("file")) {
				final String name = DocumentUtils.impl.getAttribute(current, "name");
				final PowerTagData td = new PowerTagData(name);
				final NodeList children = current.getChildNodes();
				for (int k = 0; k < children.getLength(); k++)
					if (children.item(k).getNodeName().equals("tag"))
						td.addTag(children.item(k).getTextContent());
				for (String defaultTag : defaultTags)
					td.addTag(defaultTag);
			}
		}

		final VFSPathToken[] files;
		try {
			files = vfs.list(uri);
		} catch (NotFoundException e) {
			TrivialExceptionHandler.impossible(this, e);
			return;
		}

		/*
		 * Look through each file that exists in this folder. If there are
		 * default tags, add a row to the DB that contains only the default
		 * tags. If tag data does exists, then add the tags.
		 */
		for (VFSPathToken fileName : files) {
			PowerTagData td = tagMap.get(fileName.toString());
			if (td == null && !defaultTags.isEmpty()) {
				td = new PowerTagData(fileName.toString());
				for (String defaultTag : defaultTags)
					td.addTag(defaultTag);
			}
			if (td != null) {
				final String rowID = getURIID(td.getUri(), app.getExecutionContext());
				final ArrayList<String> existingtags = getExistingTags(rowID, app.getExecutionContext());

				final Iterator<String> iterator = td.getAllTags().listIterator();
				while (iterator.hasNext()) {
					final String tag = iterator.next();
					if (existingtags.contains(tag))
						GoGoDebug.get("debug").println("Don't need to add " + tag + " cuz it exists");
					else {
						try {
							GoGoDebug.get("debug").println("- Trying to add tag " + tag);
							addTag(rowID, app.getExecutionContext(), tag);
							GoGoDebug.get("debug").println("- Added tag " + tag);
						} catch (DBException e) {
							GoGoDebug.get("debug").println("- Failed to add " + tag);
							e.printStackTrace();
							TrivialExceptionHandler.ignore(this, e);
						} catch (Exception e) {
							GoGoDebug.get("debug").println("- Failed to add " + tag);
							e.printStackTrace();
						}
					}
				}
				// Need to delete any tags that existed but are not in the doc
				for (int k = 0; k < existingtags.size(); k++)
					if (!td.containsTag(existingtags.get(k)))
						deleteTag(rowID, existingtags.get(k), app.getExecutionContext());
			}
		}
	}

	/**
	 * Fetches the row ID, possibly adding a new entry
	 * 
	 * @param filename
	 * @param ec
	 * @return
	 */
	private String getURIID(String filename, ExecutionContext ec) {
		return TagDatabaseUtils.getURIID(app.getSiteID(), app.getSiteID() + "_itemuris", uri + "/" + filename, ec);
	}

	private void addTag(String uriID, ExecutionContext ec, String tag) throws DBException {
		String tbl = app.getSiteID() + "_itemtags";
		TagDatabaseUtils.addTag(app.getSiteID(), tbl, uriID, ec, tag);
	}

	private ArrayList<String> getExistingTags(String uriID, ExecutionContext ec) {
		String tbl = app.getSiteID() + "_itemtags";

		SelectQuery q = new SelectQuery();
		q.select(tbl, "tag");
		q.constrain(new CanonicalColumnName(tbl, "uriid"), QConstraint.CT_EQUALS, uriID);

		Row.Set set = new Row.Set();
		ArrayList<String> list = new ArrayList<String>();
		try {
			ec.doQuery(q, set);

			Iterator<Row> iterator = set.getSet().listIterator();
			while (iterator.hasNext())
				list.add(iterator.next().get(0).toString());
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		return list;
	}

	private void deleteTag(String uriID, String tag, ExecutionContext ec) {
		GoGoDebug.get("debug").println("- Deleting tag " + tag + " from " + uriID);
		String tbl = app.getSiteID() + "_itemtags";

		DeleteQuery q = new DeleteQuery();
		q.setTable(tbl);
		q.constrain(new CanonicalColumnName(tbl, "uriid"), QConstraint.CT_EQUALS, uriID);
		q.constrain(new CanonicalColumnName(tbl, "tag"), QConstraint.CT_EQUALS, tag);

		try {
			ec.doUpdate(q);
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}

	public StaticRowID getRowID() {
		return TagDatabaseUtils.getRowID(app.getSiteID());
	}

}
