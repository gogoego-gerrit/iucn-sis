package org.iucn.sis.server.api.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.acl.BasePermissionUtils;
import org.iucn.sis.shared.api.acl.PermissionDataSource;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;

public class PermissionUtils extends BasePermissionUtils {
	
	private final User user;
	
	public PermissionUtils(final Session session, final User user) {
		super(new HibernatePermissionDataSource(session, user));
		this.user = user;
	}
	
	public boolean hasPermission(String operation, AuthorizableObject auth) {
		//If it's a working set, test ownership first to escape early
		if (auth instanceof WorkingSet) {
			WorkingSet ws = (WorkingSet)auth;
			if (ws.getCreator() != null && user.getUsername().equals(ws.getCreator().getUsername()))
				return true;
		}
		
		for (PermissionGroup curGroup : user.getPermissionGroups()) {
			if (hasPermission(curGroup, auth, operation))
				return true;
		}
		
		return false;
	}
	
	private static class HibernatePermissionDataSource implements PermissionDataSource {
		
		private final User user;
		private final WorkingSetIO wsIO;
		private final TaxonIO taxonIO;
		
		public HibernatePermissionDataSource(Session session, User user) {
			this.user = user;
			this.wsIO = new WorkingSetIO(session);
			this.taxonIO = new TaxonIO(session);
		}
		
		@Override
		public List<WorkingSet> getAllWorkingSets() {
			try {
				return Arrays.asList(wsIO.getSubscribedWorkingSets(user.getId()));
			} catch (PersistentException e) {
				return new ArrayList<WorkingSet>();
			}
		}
		
		@Override
		public Taxon getTaxon(int id) {
			return taxonIO.getTaxon(id);
		}
		
		@Override
		public WorkingSet getWorkingSet(int id) {
			return wsIO.readWorkingSet(id);
		}
		
	}
}
