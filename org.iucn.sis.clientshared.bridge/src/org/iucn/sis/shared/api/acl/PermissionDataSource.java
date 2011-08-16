package org.iucn.sis.shared.api.acl;

import java.util.List;

import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

public interface PermissionDataSource {
	
	public Taxon getTaxon(int id);
	
	public WorkingSet getWorkingSet(int id);
	
	public List<WorkingSet> getAllWorkingSets();

}
