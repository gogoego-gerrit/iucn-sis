package org.iucn.sis.shared.api.utils;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.FetchMode;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.shared.api.acl.BasePermissionUtils;
import org.iucn.sis.shared.api.acl.PermissionDataSource;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.AssessmentFilter;
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
	
	private static class AuthorizableWorkingSet extends WorkingSet {
		
		private static final long serialVersionUID = 1L;
		
		private final String taxonIDs;
		private final AssessmentFilter filter;
		
		public AuthorizableWorkingSet(String taxonIDs, AssessmentFilter filter) {
			this.taxonIDs = "," + taxonIDs + ",";
			this.filter = filter;
		}
		
		@Override
		public boolean containsTaxon(Integer id) {
			return taxonIDs.contains(","+id+",");
		}
		
		@Override
		public AssessmentFilter getFilter() {
			return filter;
		}
		
	}
	
	private static class ClientPermissionDataSource implements PermissionDataSource {
		
		@Override
		public List<WorkingSet> getAllWorkingSets() {
			List<WorkingSet> list = new ArrayList<WorkingSet>();
			for (WorkingSet set : WorkingSetCache.impl.getWorkingSets().values()) {
				if (WorkingSetCache.impl.isCached(set.getId(), FetchMode.FULL))
					list.add(set);
				else {
					list.add(new AuthorizableWorkingSet(
						WorkingSetCache.impl.getTaxaForWorkingSetCSV(set.getId()), 
						set.getFilter()
					));
				}
			}
			
			return list;
		}
		
		@Override
		public Taxon getTaxon(int id) {
			return TaxonomyCache.impl.getTaxon(id);
		}
		
		public WorkingSet getWorkingSet(int id) {
			final WorkingSet ws;
			if (WorkingSetCache.impl.isCached(id, FetchMode.FULL))
				ws = WorkingSetCache.impl.getWorkingSet(id);
			else if (WorkingSetCache.impl.isCached(id, FetchMode.PARTIAL))
				ws = new AuthorizableWorkingSet(
					WorkingSetCache.impl.getTaxaForWorkingSetCSV(id), 
					WorkingSetCache.impl.getWorkingSet(id).getFilter()
				);
			else
				ws = null;
			
			return ws;
		}
		
	}

}
