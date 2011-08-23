package org.iucn.sis.server.api.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.PermissionGroupCriteria;
import org.iucn.sis.server.api.persistance.PermissionGroupDAO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Permission;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.query.ExperimentalSelectQuery;
import com.solertium.db.query.QRelationConstraint;

/**
 * Performs file system IO operations for Access Control (permission) related
 * classes.
 * 
 * @author rasanka.jayawardana
 * 
 */
public class PermissionGroupIO {
	
	private final Session session;
	
	public PermissionGroupIO(Session session) {
		this.session = session;
	}

	public List<PermissionGroup> getPermissionGroups() throws PersistentException {
		return SISPersistentManager.instance().listObjects(PermissionGroup.class, session);
	}

	public String getPermissionGroupsXML() throws DBException {
		ExperimentalSelectQuery query = new ExperimentalSelectQuery();
		query.select("permission_group", "*");
		query.select("permission", "permission_group_id");
		query.select("permission", "reads");
		query.select("permission", "writes");
		query.select("permission", "creates");
		query.select("permission", "deletes");
		query.select("permission", "grants");
		query.select("permission", "uses");
		query.select("permission", "url");
		query.select("permission", "id");
		query.join("permission", new QRelationConstraint(new CanonicalColumnName("permission_group", "id"),
				new CanonicalColumnName("permission", "permission_group_id")));
		String queryString = query.getSQL(SIS.get().getExecutionContext().getDBSession());
		queryString = queryString.replaceAll("JOIN", "LEFT JOIN");

		final StringBuilder ret = new StringBuilder("<permissions>");
		final Map<Integer, PermissionGroup> idToPermGroup = new HashMap<Integer, PermissionGroup>();

		SIS.get().getExecutionContext().doQuery(queryString, new RowProcessor() {

			@Override
			public void process(Row row) {
				Integer id = row.get(0).getInteger();
				PermissionGroup group = null;
				if (!idToPermGroup.containsKey(id)) {
					group = new PermissionGroup();
					group.setID(id);
					group.setName(row.get("name").getString());
					Integer parentID = row.get("parentid").getInteger();
					if (parentID != null && parentID != 0) {
						PermissionGroup parent = new PermissionGroup();
						parent.setID(parentID);
						group.setParent(parent);
					}
					group.setScopeURI(row.get("scopeuri").getString());
					idToPermGroup.put(id, group);
				} else {
					group = idToPermGroup.get(id);
				}
				Integer permissionID = row.get(row.getColumns().size() - 1).getInteger();
				if (permissionID != null && permissionID != 0) {
					Permission permission = new Permission();
					permission.setId(permissionID);
					permission.setRead(row.get("reads").getInteger().equals(1));
					permission.setWrite(row.get("writes").getInteger().equals(1));
					permission.setCreate(row.get("creates").getInteger().equals(1));
					permission.setDelete(row.get("deletes").getInteger().equals(1));
					permission.setGrant(row.get("grants").getInteger().equals(1));
					permission.setUse(row.get("uses").getInteger().equals(1));
					permission.setUrl(row.get("url").toString());
					group.getPermissions().add(permission);
				}
				
			}

		});

		for (Entry<Integer, PermissionGroup> entry : idToPermGroup.entrySet()) {
			if (entry.getValue().getParent() != null) {
				entry.getValue().setParent(idToPermGroup.get(entry.getValue().getParent().getId()));
			}

			ret.append(entry.getValue().toXML());
		}

		ret.append("</permissions>");
		return ret.toString();
	}

	public PermissionGroup getPermissionGroup(String name) throws PersistentException {
		PermissionGroupCriteria criteria = new PermissionGroupCriteria(session);
		criteria.name.eq(name);
		return PermissionGroupDAO.loadPermissionByCriteria(criteria);
	}
	
	public void deletePermissionGroup(PermissionGroup group) throws PersistentException{
		PermissionGroupDAO.deleteAndDissociate(group, session);
	}
	
	public void savePermissionGroup(PermissionGroup group) throws PersistentException, ResourceException{
		if(getPermissionGroup(group.getName()) != null)
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
		
		PermissionGroupDAO.savePermissionGroup(group, session);
	}
	
	public void updatePermissionGroup(PermissionGroup group) throws PersistentException{
		PermissionGroupDAO.updatePermissionGroup(group, session);
	}	

}
