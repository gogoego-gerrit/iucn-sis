package org.iucn.sis.server.extensions.user.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.TransactionResource;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.extensions.user.application.UserManagementApplication;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.solertium.db.CInteger;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowID;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.ExperimentalSelectQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.Query;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;
import com.solertium.db.utils.QueryUtils;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.ElementCollection;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.restlet.VFSResource;
import com.solertium.vfs.utils.VFSUtils;

/**
 * UserResource.java
 * 
 * Allows for listing, adding, and updating users and their profile or custom
 * fields.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
@SuppressWarnings("deprecation")
public class UserResource extends TransactionResource {

	private static final String MODE_LIGHT = "light";
	private static final String MODE_FULL = "full";

	private final ExecutionContext ec;

	private final String mode;
	private final VFSPath uri;

	public UserResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);

		this.ec = UserManagementApplication.getFromContext(context).getExecutionContext();

		VFSPath uri;
		try {
			uri = VFSResource.decodeVFSPath(request.getResourceRef().getRemainingPart());
		} catch (VFSUtils.VFSPathParseException e) {
			uri = null;
		}

		this.uri = uri;
		this.mode = MODE_LIGHT.equalsIgnoreCase(request.getResourceRef().getQueryAsForm().getFirstValue("mode")) ? MODE_LIGHT
				: MODE_FULL;

		getVariants().add(new Variant(MediaType.TEXT_XML));
		getVariants().add(new Variant(MediaType.TEXT_CSV));
	}

	/*
	 * Send updates as:
	 * 
	 * <root> <field name="..."></field> <customfield name="..."></customfield>
	 * </root>
	 */
	public void acceptRepresentation(Representation entity, Session session) throws ResourceException {
		if (uri == null || VFSPath.ROOT.equals(uri))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		Document doc = getDocument(entity);
		
		if( uri.getName().equalsIgnoreCase("batch") ) {
			final ElementCollection users = new ElementCollection(doc.getDocumentElement().getElementsByTagName("user"));
			
			for( Element userEl : users ) {
				final Integer userID = Integer.valueOf(userEl.getAttribute("id"));
				updateUserData(userID, new NodeCollection(userEl.getChildNodes()));
			}
		} else {
			final Integer userID;
			try {
				userID = Integer.valueOf(uri.getName());
			} catch (NumberFormatException e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			}

			final NodeCollection nodes = new NodeCollection(doc.getDocumentElement().getChildNodes());
			updateUserData(userID, nodes);
		}
		
		getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
	}
	
	

	private void updateUserData(final Integer userID, final NodeCollection nodes) throws ResourceException {
		final Row profileFieldsTemplate, customFieldsTemplate;
		final Row profileFields = new Row();
		try {
			profileFieldsTemplate = ec.getRow("profile");
			customFieldsTemplate = ec.getRow("customfielddata");
		} catch (DBException e) {
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		final SelectQuery initCustom = new SelectQuery();
		initCustom.select("customfielddata", "*");
		initCustom.constrain(new CanonicalColumnName("customfielddata", "userid"), QConstraint.CT_EQUALS, userID);

		final Row.Set rs = new Row.Set();

		try {
			ec.doQuery(initCustom, rs);
		} catch (DBException e) {
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		boolean updateProfileData = false;

		for (Node node : nodes) {
			/*
			 * this will handle multiple columns in one row.
			 */
			if (node.getNodeName().equals("field")) {
				String name = BaseDocumentUtils.impl.getAttribute(node, "name");
				Column col = profileFieldsTemplate.get(name);
				if (col != null) {
					col.setObject(node.getTextContent());
					profileFields.add(col);
					updateProfileData = true;
				}
			}
			/*
			 * this will handle multiple rows in one table, and can either be
			 * add or update
			 */
			else if (node.getNodeName().equals("customfield")) {
				final Integer id;
				try {
					id = Integer.parseInt(BaseDocumentUtils.impl.getAttribute(node, "name"));
				} catch (NumberFormatException e) {
					e.printStackTrace();
					continue;
				}
				final Query entryQuery;
				Row custom = null;
				for (Row row : rs.getSet()) {
					if (id.equals(row.get("fieldid").getInteger())) {
						custom = row;
						break;
					}
				}
				if (custom == null) {
					custom = new Row(customFieldsTemplate);
					try {
						custom.get("id").setObject(Integer.valueOf((int) RowID.get(ec, "customfielddata", "id")));
					} catch (DBException e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
					}
					custom.get("userid").setObject(userID);
					custom.get("fieldid").setObject(id);
					custom.get("value").setObject(node.getTextContent());

					entryQuery = new InsertQuery();
					((InsertQuery) entryQuery).setRow(custom);
					((InsertQuery) entryQuery).setTable("customfielddata");
				} else {
					custom.get("value").setObject(node.getTextContent());

					entryQuery = new UpdateQuery();
					((UpdateQuery) entryQuery).setRow(custom);
					((UpdateQuery) entryQuery).setTable("customfielddata");
					((UpdateQuery) entryQuery).constrain(new CanonicalColumnName("customfielddata", "id"),
							QConstraint.CT_EQUALS, custom.get("id").getInteger());
				}

				try {
					ec.doUpdate(entryQuery);
				} catch (DBException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
		}

		if (updateProfileData) {
			final UpdateQuery updateQuery = new UpdateQuery();
			updateQuery.setRow(profileFields);
			updateQuery.setTable("profile");
			updateQuery.constrain(new CanonicalColumnName("profile", "userid"), QConstraint.CT_EQUALS, userID);

			try {
				ec.doUpdate(updateQuery);
			} catch (DBException e) {
				e.printStackTrace();
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}

		updateXMLProfile(userID);
	}

	private Document getDocument(Representation entity) throws ResourceException {
		try {
			return new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
	}

	private Integer getUserID(String username) throws ResourceException {
		final SelectQuery query = new SelectQuery();
		query.select("user", "id");
		query.constrain(new CanonicalColumnName("user", "username"), QConstraint.CT_EQUALS, username);

		final Row.Loader rl = new Row.Loader();

		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		return rl.getRow() != null ? rl.getRow().get("id").getInteger() : null;
	}
	
	public void removeRepresentations(Session session) throws ResourceException {
		if (uri == null || VFSPath.ROOT.equals(uri))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		final Integer userID;
		try {
			userID = Integer.valueOf(uri.getName());
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}

		final DeleteQuery profile = new DeleteQuery();
		profile.setTable("profile");
		profile.constrain(new CanonicalColumnName("profile", "userid"), QConstraint.CT_EQUALS, userID);

		try {
			ec.doUpdate(profile);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		final DeleteQuery custom = new DeleteQuery();
		custom.setTable("customfielddata");
		custom.constrain(new CanonicalColumnName("customfielddata", "userid"), QConstraint.CT_EQUALS, userID);

		try {
			ec.doUpdate(custom);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		final DeleteQuery user = new DeleteQuery();
		user.setTable("user");
		user.constrain(new CanonicalColumnName("user", "id"), QConstraint.CT_EQUALS, userID);

		try {
			ec.doUpdate(user);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		getResponse().setStatus(Status.SUCCESS_OK);
	}

	protected Representation getAllUserProfileInfo(UserIO userIO) {
		User[] users;
		try {
			users = userIO.getAllUsers();
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		StringBuilder xml = new StringBuilder("<result>");
		
		for (User user : users) {
			
			xml.append("<row>");
			xml.append("<field name=\"ID\">" + user.getId() + "</field>");
			xml.append("<field name=\"USERNAME\"><![CDATA[" + DocumentUtils.getDisplayString(user.getUsername()) + "]]></field>");
			xml.append("<field name=\"FIRSTNAME\"><![CDATA[" + DocumentUtils.getDisplayString(user.getFirstName()) + "]]></field>");
			xml.append("<field name=\"LASTNAME\"><![CDATA[" + DocumentUtils.getDisplayString(user.getLastName()) + "]]></field>");
			xml.append("<field name=\"INITIALS\"><![CDATA[" + DocumentUtils.getDisplayString(user.getInitials()) + "]]></field>");
			xml.append("<field name=\"EMAIL\"><![CDATA[" + DocumentUtils.getDisplayString(user.getEmail()) + "]]></field>");
			xml.append("<field name=\"AFFILIATION\"><![CDATA[" + DocumentUtils.getDisplayString(user.getAffiliation()) + "]]></field>");
			xml.append("<field name=\"QUICKGROUP\"><![CDATA[" + DocumentUtils.getDisplayString(user.getQuickGroupString()) + "]]></field>");
			xml.append("<field name=\"SIS\">" + user.getSisUser() + "</field>");
			xml.append("<field name=\"RAPIDLIST\">" + user.getRapidlistUser() + "</field>");
			xml.append("</row>");
		}
		
		xml.append("</result>");
		return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
		
	}
	
	@Override
	public Representation represent(Variant variant, Session session) throws ResourceException {
		if (uri == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		final Form queryStr = getQuery();
		final String activeOnly = queryStr.getFirstValue("active");
		final String format = queryStr.getFirstValue("format");
		
		//GET ALL USER PROFILE INFO
		if (format == null && uri.equals(VFSPath.ROOT)) {
			Representation rep = getAllUserProfileInfo(new UserIO(session));
			if (rep == null)
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			return rep;
			
		} else {
			final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
			query.select("user", "*");

			if (MODE_FULL.equals(mode)) {
				query.select("profile", "firstname");
				query.select("profile", "lastname");
				query.select("profile", "initials");
				query.select("profile", "email");
				query.select("profile", "affiliation");
				query.select("profile", "quickGroup");
				query.select("profile", "sis");
				query.select("profile", "rapidlist");
			}

			if (!uri.equals(VFSPath.ROOT)) {
				try {
					query.constrain(new CanonicalColumnName("user", "id"), QConstraint.CT_EQUALS, Integer.valueOf(uri
							.getName()));
				} catch (NumberFormatException e) {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
				}
			}

			final Row.Set rs = new Row.Set();
			
			if( activeOnly != null && activeOnly.equalsIgnoreCase("true") )
				query.constrain(new CanonicalColumnName("profile", "sis"), QConstraint.CT_EQUALS, Boolean.valueOf(true));
				
			if( format != null && format.equalsIgnoreCase("csv")) {
				try {
					ec.doQuery(query, rs);
				} catch (DBException e) {
					e.printStackTrace();
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Query Failed", e);
				}
				
				StringBuilder accounts = new StringBuilder();
				for (Row row : rs.getSet()) {
					Column username = row.get("username");
					String email = username.getString();
					if( email.contains("@") ) {
						accounts.append(email);
						accounts.append(", ");
					}
				}
				
				return new StringRepresentation(accounts.substring(0, accounts.length()-2).toString(), MediaType.TEXT_CSV);

			} else {
				try {
//					System.out.println("doing this aewsome thing");
//					Class.forName("org.postgresql.Driver");
//					System.out.println("doign this awesome query");
					ec.doQuery(query, rs);
				} catch (DBException e) {
					e.printStackTrace();
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Query Failed", e);
				} 
//				catch (ClassNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

				// Now to inject custom fields

				if (MODE_FULL.equals(mode)) {
					final ExperimentalSelectQuery customFieldQuery = new ExperimentalSelectQuery();
					customFieldQuery.select("customfielddata", "userid");
					customFieldQuery.select("customfielddata", "fieldid");
					customFieldQuery.select("customfielddata", "value");

					if (!uri.equals(VFSPath.ROOT)) {
						customFieldQuery.constrain(new CanonicalColumnName("customfielddata", "userid"), QConstraint.CT_EQUALS,
								Integer.valueOf(uri.getName()));
					}

					final Row.Set custRows = new Row.Set();
					try {
						ec.doQuery(customFieldQuery, custRows);
					} catch (DBException e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
					}

					final HashMap<Integer, ArrayList<Row>> dataMap = new HashMap<Integer, ArrayList<Row>>();
					for (Row row : custRows.getSet()) {
						Integer key = row.get("userid").getInteger();
						ArrayList<Row> list = dataMap.get(key);
						if (list == null)
							list = new ArrayList<Row>();
						list.add(row);
						dataMap.put(key, list);
					}

					final Document document = BaseDocumentUtils.impl.newDocument();
					final Element root = document.createElement("result");
					for (Row row : rs.getSet()) {
						Element el = document.createElement("row");
						ArrayList<Column> columns = row.getColumns();
						Iterator<Column> it = columns.iterator();
						Integer id = null;
						while (it.hasNext()) {
							Column c = it.next();
							Element cEl = document.createElement("field");
							cEl.setAttribute("name", c.getLocalName());
							if ("id".equalsIgnoreCase(c.getLocalName()))
								id = c.getInteger();
							Text cText = document.createTextNode(c.toString());
							cEl.appendChild(cText);
							el.appendChild(cEl);
						}
						ArrayList<Row> customFieldRows = dataMap.get(id);
						if (customFieldRows != null) {
							for (Row current : customFieldRows) {
								Element custEl = document.createElement("field");
								custEl.setAttribute("name", "custom:" + current.get("fieldid"));
								custEl.setTextContent(current.get("value").toString());
								el.appendChild(custEl);
							}
						}
						root.appendChild(el);
					}

					document.appendChild(root);

					return new DomRepresentation(variant.getMediaType(), document);
				} else
					return new DomRepresentation(variant.getMediaType(), QueryUtils.writeDocumentFromRowSet(rs.getSet()));
			}
		}
	}

	/*
	 * Send user data as:
	 * 
	 * <root> <username> .. </username> </root>
	 */
	@Override
	public void storeRepresentation(Representation entity, Session session) throws ResourceException {
		if (uri == null || !VFSPath.ROOT.equals(uri))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		final NodeCollection nodes = new NodeCollection(getDocument(entity).getDocumentElement().getChildNodes());

		final Document document = BaseDocumentUtils.impl.newDocument();
		Element root = document.createElement("root");

		for (Node node : nodes) {
			if (node.getNodeName().equals("username")) {
				final String proposedUserName = node.getTextContent();
				if (proposedUserName == null || proposedUserName.equals(""))
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No username given.");

				if (getUserID(proposedUserName) == null) {
					final Integer id;
					final Row row;
					try {
						row = ec.getRow("user");
						row.get("id").setObject(id = Integer.valueOf((int) RowID.get(ec, "user", "id")));
						row.get("username").setObject(proposedUserName);
					} catch (DBException unlikely) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, unlikely);
					}

					final InsertQuery insert = new InsertQuery();
					insert.setRow(row);
					insert.setTable("user");

					final Element element = BaseDocumentUtils.impl.createCDATAElementWithText(document, "user",
							proposedUserName);
					element.setAttribute("id", id.toString());

					try {
						ec.doUpdate(insert);
						root.appendChild(element);
					} catch (DBException e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not do insert", e);
					}

					// Add profile row
					final Row profileEntry = new Row();
					try {
						profileEntry.add(new CInteger("id", Integer.valueOf((int) RowID.get(ec, "profile", "id"))));
						profileEntry.add(new CInteger("userid", id));
					} catch (DBException unlikely) {
						break;
					}

					final InsertQuery profileQuery = new InsertQuery();
					profileQuery.setRow(profileEntry);
					profileQuery.setTable("profile");

					try {
						ec.doUpdate(profileQuery);
					} catch (DBException unlikely) {
						TrivialExceptionHandler.ignore(this, unlikely);
					}
					
					updateXMLProfile(id);
				} else
					throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "Username " + proposedUserName
							+ " exists.");

				break;
			}
		}

		document.appendChild(root);

		getResponse().setStatus(Status.SUCCESS_CREATED);
		getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, document));
	}

	private boolean updateXMLProfile(Integer id) {
		//FIXME
		return false;
//		String sql = "select * from (SELECT * FROM " +
//				"(SELECT USERID, FIRSTNAME, LASTNAME, EMAIL, AFFILIATION, QUICKGROUP, INITIALS, SIS, RAPIDLIST FROM PROFILE where ID=" + id + ") T1 " +
//				"left join (select FIELDID, VALUE from CUSTOMFIELDDATA where CUSTOMFIELDDATA.USERID=" + id + ") T2 " +
//				"left join USER where T1.USERID=USER.id) T3 " +
//				"left join CUSTOMFIELD on T3.FIELDID=CUSTOMFIELD.ID";
//		
//		Set set = new Set();
//		
//		try {
//			ec.doQuery(sql, set);
//			
//			StringBuilder xml = new StringBuilder("<profile>\r\n");
//			
//			List<Row> rows = set.getSet();
//			String [] vals = new String [] { "firstname", "lastname", "email", "affiliation", "quickGroup", "initials", "sis", "rapidlist" };
//			for( String curVal : vals ) {
//				xml.append("<" + curVal + ">");
//				xml.append(XMLUtils.clean(rows.get(0).get(curVal).getString(Column.NEVER_NULL)));
//				xml.append("</" + curVal + ">\r\n");
//			}
//			
//			for( Row row : rows ) {
//				if( row.get("name").getString() != null ) {
//					xml.append("<" + row.get("name").getString() + ">");
//					xml.append(XMLUtils.clean(row.get("value").getString()));
//					xml.append("</" + row.get("name").getString() + ">\r\n");
//				}
//			}
//			xml.append("</profile>");
//			
//			String username = rows.get(0).get("username").getString();
//			ProfileIO.updateUserProfile(SISContainerApp.getStaticVFS(), username, xml.toString());
//			return true;
////			Request request = new Request(Method.POST, "riap://host/profile/" + username, 
////					new StringRepresentation(xml.toString(), MediaType.TEXT_XML));
////			Response proResponse = getContext().getClientDispatcher().handle(request);
////			
////			if( proResponse.getStatus().isSuccess() )
////				return true;
////			else
////				return false;
//		} catch (DBException e) {
//			e.printStackTrace();
//			return false;
//		} catch (Throwable e) {
//			e.printStackTrace();
//			return false;
//		}
	}
}
