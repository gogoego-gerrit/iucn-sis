package org.iucn.sis.server.api.io;

import org.iucn.sis.server.api.persistance.IsoLanguageCriteria;
import org.iucn.sis.server.api.persistance.IsoLanguageDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.IsoLanguage;

public class IsoLanguageIO {
	
	public IsoLanguage getIsoLanguageByCode(String isoCode) {
		IsoLanguageCriteria criteria;
		try {
			criteria = new IsoLanguageCriteria();
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		criteria.code.eq(isoCode);
		return IsoLanguageDAO.loadIsoLanguageByCriteria(criteria);
	}

}
