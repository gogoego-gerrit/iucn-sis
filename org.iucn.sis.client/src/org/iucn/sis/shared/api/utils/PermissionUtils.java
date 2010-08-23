package org.iucn.sis.shared.api.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableAssessmentShim;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Permission;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.PermissionResourceAttribute;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Region;
import org.iucn.sis.shared.api.models.Relationship;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

public class PermissionUtils {
	
	/**
	 * Returns whether the object fits within the criteria set forth by the scopeURI.  
	 * 
	 * @param object
	 * @return
	 */
	public static boolean isInScope(PermissionGroup group, AuthorizableObject object) {
		if (group.getScope() == null || group.getScope().trim().equals("")) {
			return true;
		}
		else if (object instanceof AuthorizableFeature || object instanceof WorkingSet || object instanceof Reference) {
			// Features, working sets and references aren't scoped
			return true;
		} else if (object instanceof Taxon) {
			Taxon taxon = (Taxon) object;

			if (group.getScope().startsWith("taxon")) {
				String[] split = group.getScope().split("/");
				int level = Integer.valueOf(split[1]);
				boolean kingdomMatch = true;

				if (split.length > 3 && taxon.getFootprint().length > 0)
					kingdomMatch = split[3].equalsIgnoreCase(taxon.getFootprint()[0]);

				if (taxon.getFootprint().length > level)
					return kingdomMatch && taxon.getFootprint()[level].equalsIgnoreCase(split[2]);
				else if (taxon.getLevel() == level)
					return kingdomMatch && taxon.getFullName().equalsIgnoreCase(split[2]);
				else
					return false;
			} else if (group.getScope().startsWith("workingSets")) {
				Map<Integer, WorkingSet> sets = WorkingSetCache.impl.getWorkingSets();
				for (Entry<Integer, WorkingSet> curEntry : sets.entrySet()) {
					if (curEntry.getValue().getSpeciesIDs().contains(taxon.getId() + ""))
						return true;
				}
				return false;
			} else if (group.getScope().startsWith("workingSet")) {
				String ids = group.getScope().substring(group.getScope().indexOf("/") + 1, group.getScope().length());
				Map<Integer, WorkingSet> sets = WorkingSetCache.impl.getWorkingSets();

				String[] split = ids.split(",");
				boolean ret = false;

				for (String cur : split) {
					if (sets.containsKey(Integer.valueOf(cur))) {
						ret = sets.get(Integer.valueOf(cur)).getSpeciesIDs().contains(taxon.getId());

						if (ret)
							return true;
					}
				}

				return false;
			} else
				return false;
		} else if (object instanceof Assessment) {
			if (group.getScope().startsWith("workingSet/")) {
				Assessment assessment = (Assessment) object;
				String ids = group.getScope().substring(group.getScope().indexOf("/") + 1, group.getScope().length());
				Map<Integer, WorkingSet> sets = WorkingSetCache.impl.getWorkingSets();

				String[] split = ids.contains(",") ? ids.split(",") : new String[] { ids };

				for (String curStr : split) {
					Integer cur = Integer.valueOf(curStr);

					if (sets.containsKey(cur)) {
						if (sets.get(cur).getSpeciesIDs().contains(assessment.getSpeciesID())) {
							Collection<Region> filterRegionList = sets.get(cur).getFilter().getRegions();
							List<Integer> filterRegionIDs = new ArrayList<Integer>();
							for (Region curReg : filterRegionList)
								filterRegionIDs.add(curReg.getId());

							if (sets.get(cur).getFilter().isAllRegions()) {
								return true;
							} else if (sets.get(cur).getFilter().getRegionType().equals(Relationship.AND)) {
								if (filterRegionIDs.containsAll(assessment.getRegionIDs())
										&& assessment.getRegionIDs().containsAll(filterRegionIDs)) {
									return true;
								}
							} else if (sets.get(cur).getFilter().getRegionType().equals(Relationship.OR)) {
								for (Integer curRegion : assessment.getRegionIDs()) {
									if (filterRegionIDs.contains(curRegion)) {
										return true;
									}
								}
							}
						}
					}
				}

				return false;
			} else {
				Taxon node = TaxonomyCache.impl.getTaxon(((Assessment) object).getSpeciesID());
				return isInScope(group, node);
			}
		} else if (object instanceof AuthorizableAssessmentShim) {
			AuthorizableAssessmentShim shim = (AuthorizableAssessmentShim) object;
			return isInScope(group, shim.getTaxon());
		} else
			return false; // Invalid scope URI
	}
	
	/**
	 * 		
	 * @param object
	 * @param operation
	 * @return
	 */
	public static boolean checkMe(PermissionGroup group, AuthorizableObject object, String operation) {
		boolean hasPermission = group.getDefaultPermission(operation);
		
		if (object != null && object != null && isInScope(group, object)) {			
			String uri = object.getFullURI();
			boolean found = false;			
			while (uri.indexOf("/") > -1 && !found) {
				if (group.getResourceToPermission().containsKey(uri)) {
					Permission perm = group.getResourceToPermission().get(uri);
					hasPermission = perm.check(operation);
					if (hasPermission) {
						for (PermissionResourceAttribute cur : perm.getAttributes()) {
							if (cur != null && cur.getName() != null && cur.getRegex() != null)
								hasPermission = object.getProperty(cur.getName()).matches(cur.getRegex());
						}
					}
					found = true;
				} else {
					uri = uri.substring(0, uri.lastIndexOf("/"));
				}
			}
		}
		return hasPermission;
	}

}
