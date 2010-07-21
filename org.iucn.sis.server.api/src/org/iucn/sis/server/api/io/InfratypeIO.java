package org.iucn.sis.server.api.io;

import org.iucn.sis.server.api.persistance.InfratypeCriteria;
import org.iucn.sis.server.api.persistance.InfratypeDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Infratype;

public class InfratypeIO {
	
	public Infratype getInfratype(String infraname) {
		InfratypeCriteria criteria;
		try {
			criteria = new InfratypeCriteria();
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		criteria.name.eq(infraname);
		return InfratypeDAO.loadInfratypeByCriteria(criteria);
	}

}
