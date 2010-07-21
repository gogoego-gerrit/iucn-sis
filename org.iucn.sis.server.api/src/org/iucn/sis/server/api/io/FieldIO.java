package org.iucn.sis.server.api.io;

import org.iucn.sis.server.api.persistance.FieldDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Field;

public class FieldIO {

	public Field get(Integer fieldID) {
		try {
			return FieldDAO.getFieldByORMID(fieldID);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
}
