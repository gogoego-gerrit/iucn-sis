package org.iucn.sis.server.api.io;

import java.util.Arrays;
import java.util.List;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.RegionCriteria;
import org.iucn.sis.server.api.persistance.RegionDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Region;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

public class RegionIO {
	
	private final Session session;
	
	public RegionIO(Session session) {
		this.session = session;
	}

	public Region getRegion(Integer id) {
		try {
			return RegionDAO.getRegionByORMID(session, id);
		} catch (PersistentException e) {
			Debug.println(e);
			return null;
		}
	}
	
	public List<Region> getRegions() throws PersistentException {
		return Arrays.asList(RegionDAO.listRegionByCriteria(new RegionCriteria(session)));
	}
	
	public void saveRegion(Region region) throws PersistentException, ResourceException {
		if(getRegionsByName(region).length > 0)
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);

		SIS.get().getManager().saveObject(session, region);
	}
	
	private Region[] getRegionsByName(Region region) throws PersistentException {
		RegionCriteria criteria = new RegionCriteria(session);
		criteria.name.ilike(region.getName().trim());
		criteria.id.ne(region.getId());
		Region[] regions = criteria.listRegion();
		
		return regions;	
	}	
	
}
