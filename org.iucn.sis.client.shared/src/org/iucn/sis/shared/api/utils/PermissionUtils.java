package org.iucn.sis.shared.api.utils;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.shared.api.acl.BasePermissionUtils;
import org.iucn.sis.shared.api.acl.PermissionDataSource;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

public class PermissionUtils extends BasePermissionUtils {
	
	private static final PermissionUtils impl = new PermissionUtils(new ClientPermissionDataSource());
	
	private PermissionUtils(PermissionDataSource source) {
		super(source);
	}
	
	/**
	 * Returns whether the object fits within the criteria set forth by the scopeURI.  
	 * 
	 * @param object
	 * @return
	 */
	public static boolean isInScope(PermissionGroup group, AuthorizableObject object) {
		return impl.inScope(group, object);
	}
	
	/**
	 * 		
	 * @param object
	 * @param operation
	 * @return
	 */
	public static boolean checkMe(PermissionGroup group, AuthorizableObject object, String operation) {
		return impl.hasPermission(group, object, operation);
	}
	
	private static class ClientPermissionDataSource implements PermissionDataSource {
		
		@Override
		public List<WorkingSet> getAllWorkingSets() {
			return new ArrayList<WorkingSet>(WorkingSetCache.impl.getWorkingSets().values());
		}
		
		@Override
		public Taxon getTaxon(int id) {
			return TaxonomyCache.impl.getTaxon(id);
		}
		
		public WorkingSet getWorkingSet(int id) {
			return WorkingSetCache.impl.getWorkingSet(id);
		}
		
	}

}
