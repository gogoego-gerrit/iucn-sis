package org.iucn.sis.server.api.io;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.IsoLanguageCriteria;
import org.iucn.sis.server.api.persistance.IsoLanguageDAO;
import org.iucn.sis.shared.api.models.IsoLanguage;

public class IsoLanguageIO {
	
	private final Session session;
	
	public IsoLanguageIO(Session session) {
		this.session = session;
	}
	
	public IsoLanguage getIsoLanguageByCode(String isoCode) {
		IsoLanguageCriteria criteria = new IsoLanguageCriteria(session);
		
		criteria.code.eq(isoCode);
		return IsoLanguageDAO.loadIsoLanguageByCriteria(criteria);
	}

}
