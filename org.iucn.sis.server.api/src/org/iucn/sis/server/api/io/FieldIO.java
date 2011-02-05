package org.iucn.sis.server.api.io;

import java.util.List;

import javax.naming.NamingException;

import org.hibernate.Session;
import org.iucn.sis.server.api.fields.FieldSchemaGenerator;
import org.iucn.sis.server.api.fields.definitions.FieldDefinitionLoader;
import org.iucn.sis.server.api.persistance.FieldDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field;
import org.w3c.dom.Document;

public class FieldIO {
	
	private final FieldSchemaGenerator generator;
	
	private final Session session;
	
	public FieldIO(Session session) {
		FieldSchemaGenerator generator = null;
		try {
			generator = new FieldSchemaGenerator();
		} catch (NamingException e) {
			System.err.println("sis_lookups database is not defined.");
		}
		
		this.generator = generator;
		this.session = session;
	}

	public Field get(Integer fieldID) {
		try {
			return FieldDAO.getFieldByORMID(session, fieldID);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			Debug.println(e);
			return null;
		}
	}
	
	public static List<String> getAllFields() {
		return FieldDefinitionLoader.getAllFields();
	}
	
	public Field getDefinition(String fieldName) throws Exception {
		return generator.getField(fieldName);
	}
	
	public Document getSchema(String fieldName) throws Exception {
		return generator.getSchema(fieldName);
	}
	
}
