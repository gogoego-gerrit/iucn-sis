package org.iucn.sis.server.api.io;

import java.util.Arrays;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.RegionCriteria;
import org.iucn.sis.server.api.persistance.RegionDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Region;

public class RegionIO {

	public Region getRegion(Integer id) {
		try {
			return RegionDAO.getRegionByORMID(id);
		} catch (PersistentException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Region> getRegions() throws PersistentException {
		return Arrays.asList(RegionDAO.listRegionByCriteria(new RegionCriteria()));
	}
	
	public void saveRegion(Region region) throws PersistentException {
		SIS.get().getManager().saveObject(region);
	}
	
}
