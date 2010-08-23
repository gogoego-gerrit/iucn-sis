/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.gogoego.server.lib.app.tags.resources;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * TagGroupRegistryResource.java
 * 
 * @author carl.scott
 * 
 */
public class TagGroupRegistryResource extends BaseTagResource {

	protected final String protocol;
	protected final String remaining;

	/**
	 * @param context
	 * @param request
	 * @param response
	 */
	public TagGroupRegistryResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(false);

		// app = (TagApplication) TagApplication.getFromContext(context,
		// TagApplication.APP_NAME);
		protocol = (String) request.getAttributes().get("protocol");
		remaining = (String) request.getAttributes().get("remaining");
	}
	/*
	 * public Representation represent(Variant variant) throws ResourceException
	 * { if ("uri".equals(protocol)) { if (remaining != null) { try { return new
	 * DomRepresentation(variant.getMediaType(), getGroupsByURI(VFSResource
	 * .decodeVFSPath(remaining))); } catch (VFSUtils.VFSPathParseException e) {
	 * throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e); } } else
	 * return new DomRepresentation(variant.getMediaType(),
	 * getAllGroupDirectories()); } else if ("view".equals(protocol)) { if
	 * (remaining != null) { String viewID; try { viewID =
	 * URLDecoder.decode(remaining, "UTF-8"); } catch
	 * (UnsupportedEncodingException e) { viewID = remaining; }
	 * 
	 * return new DomRepresentation(variant.getMediaType(),
	 * getGroupsByView(viewID)); } else return new
	 * DomRepresentation(variant.getMediaType(), getAllGroupViews());
	 * 
	 * } else throw new
	 * ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED,
	 * "Invalid protocol " + protocol); }
	 * 
	 * private Document getAllGroupViews() throws ResourceException { final
	 * ExperimentalSelectQuery query = new ExperimentalSelectQuery();
	 * query.select(convertTable(TABLE_GROUPVIEWS), "*");
	 * query.select(convertTable(TABLE_GROUPS), "name");
	 * 
	 * final Row.Set rs = new Row.Set();
	 * 
	 * try { ec.doQuery(query, rs); } catch (DBException e) { throw new
	 * ResourceException(Status.SERVER_ERROR_INTERNAL, e); }
	 * 
	 * return QueryUtils.writeDocumentFromRowSet(rs.getSet()); }
	 * 
	 * private Document getAllGroupDirectories() throws ResourceException {
	 * final String dirTable = convertTable(TABLE_GROUPDIRECTORIES); // final
	 * String dirRules = convertTable(TABLE_DIRECTORYRULES);
	 * 
	 * final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
	 * query.select(dirTable, "*"); query.select(convertTable(TABLE_GROUPS),
	 * "name");
	 * 
	 * final Row.Set rs = new Row.Set();
	 * 
	 * try { System.out.println(query.getSQL(ec.getDBSession()));
	 * ec.doQuery(query, rs); } catch (DBException e) { e.printStackTrace();
	 * throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e); }
	 * 
	 * return QueryUtils.writeDocumentFromRowSet(rs.getSet()); }
	 * 
	 * private Document getGroupsByView(String viewID) throws ResourceException
	 * { final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
	 * query.select(convertTable(TABLE_GROUPVIEWS), "groupid");
	 * query.select(convertTable(TABLE_GROUPS), "name"); query.constrain(new
	 * CanonicalColumnName(convertTable(TABLE_GROUPVIEWS), "viewid"),
	 * QConstraint.CT_EQUALS, viewID);
	 * 
	 * final Row.Set rs = new Row.Set();
	 * 
	 * try { app.getExecutionContext().doQuery(query, rs); } catch (DBException
	 * e) { throw new ResourceException(e); }
	 * 
	 * return QueryUtils.writeDocumentFromRowSet(rs.getSet()); }
	 * 
	 * private Document getGroupsByURI(VFSPath uri) throws ResourceException {
	 * final Collection<VFSPath> searchPaths = new ArrayList<VFSPath>();
	 * 
	 * // Find the directory rules boolean cascade = false; { final String table
	 * = convertTable(TABLE_DIRECTORYRULES);
	 * 
	 * SelectQuery query = new SelectQuery(); query.select(table, "cascade");
	 * query.constrain(new CanonicalColumnName(table, "uri"),
	 * QConstraint.CT_EQUALS, uri.toString());
	 * 
	 * Row.Loader rl = new Row.Loader(); try {
	 * app.getExecutionContext().doQuery(query, rl); Row row = rl.getRow();
	 * cascade = row != null && row.get("cascade") != null &&
	 * "true".equals(row.get("cascade").toString()); } catch (DBException e) {
	 * cascade = false; }
	 * 
	 * if (cascade) { final VFSPath start = VFSPath.ROOT;
	 * searchPaths.add(start); for (VFSPathToken token : uri.getTokens())
	 * searchPaths.add(start.child(token)); } else searchPaths.add(uri); }
	 * 
	 * final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
	 * query.select(convertTable(TABLE_GROUPDIRECTORIES), "groupid");
	 * query.select(convertTable(TABLE_GROUPS), "name");
	 * 
	 * for (VFSPath path : searchPaths) { query.constrain(QConstraint.CG_OR, new
	 * QComparisonConstraint(new CanonicalColumnName(
	 * convertTable(TABLE_GROUPDIRECTORIES), "uri"), QConstraint.CT_EQUALS,
	 * path.toString())); }
	 * 
	 * final Row.Set rs = new Row.Set();
	 * 
	 * try { app.getExecutionContext().doQuery(query, rs); } catch (DBException
	 * e) { throw new ResourceException(e); }
	 * 
	 * return QueryUtils.writeDocumentFromRowSet(rs.getSet()); }
	 */

}
