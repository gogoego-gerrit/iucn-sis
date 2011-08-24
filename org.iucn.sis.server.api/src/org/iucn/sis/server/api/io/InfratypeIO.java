package org.iucn.sis.server.api.io;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.InfratypeCriteria;
import org.iucn.sis.server.api.persistance.InfratypeDAO;
import org.iucn.sis.shared.api.models.Infratype;

public class InfratypeIO {
	
	private final Session session;
	
	public InfratypeIO(Session session) {
		this.session = session;
	}
	
	public Infratype getInfratype(int id) {
		InfratypeCriteria criteria = new InfratypeCriteria(session);
		criteria.id.eq(id);
		
		return criteria.uniqueInfratype();
	}
	
	public Infratype getInfratype(String infraname) {
		InfratypeCriteria criteria = new InfratypeCriteria(session);
		
		criteria.name.eq(infraname);
		return InfratypeDAO.loadInfratypeByCriteria(criteria);
	}

}
