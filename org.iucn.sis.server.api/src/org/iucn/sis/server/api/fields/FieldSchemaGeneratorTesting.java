package org.iucn.sis.server.api.fields;

import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;
import javax.xml.transform.sax.SAXSource;

import org.iucn.sis.shared.api.models.Field;
import org.xml.sax.InputSource;

import com.solertium.db.DBSessionFactory;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.SchemaValidator;

public class FieldSchemaGeneratorTesting {
	
	public static void main(String[] args) {
		Properties properties = new Properties();
		properties.setProperty("dbsession.sis_lookups.uri","jdbc:h2:file:/var/sis/newest_vfs/HEAD/databases/sis_lookups");
		properties.setProperty("dbsession.sis_lookups.driver","org.h2.Driver");
		properties.setProperty("dbsession.sis_lookups.user","sa");
		properties.setProperty("dbsession.sis_lookups.password","");
		
		final FieldSchemaGenerator generator;
		
		try {
			DBSessionFactory.registerDataSources(properties);
			generator = new FieldSchemaGenerator();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
		
		//validateEOO(generator);
		
		validate(generator, "FAOOccurrence");
		
		/*
		try {
			System.out.println(generator.getField("GeneralHabitats").toXML());
			System.out.println("\n\n" + (BaseDocumentUtils.impl.serializeDocumentToString(generator.getSchema("GeneralHabitats"), false, true)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		*/
		
	}
	
	private static void validateEOO(FieldSchemaGenerator generator) {
		Field field;
		String schema;
		try {
			System.out.println((field = generator.getField("EOO")).toXML());
			System.out.println("\n\n" + (schema = BaseDocumentUtils.impl.serializeDocumentToString(generator.getSchema("EOO"), false, true)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		System.out.println("This will likely fail...");
		validate(field, schema);
		
		System.out.println("-------------------\nThis will likely succeed...");
		field.getPrimitiveField("range").setRawValue("0");
		field.getPrimitiveField("qualifier").setRawValue("1");
		
		validate(field, schema);
	}
	
	private static void validate(FieldSchemaGenerator generator, String fieldName) {
		Field field;
		String schema;
		try {
			System.out.println((field = generator.getField(fieldName)).toXML());
			System.out.println("\n\n" + (schema = BaseDocumentUtils.impl.serializeDocumentToString(generator.getSchema(fieldName), false, true)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		validate(field, schema);
	}
	
	private static void validate(Field field, String schema) {
		List<String> warnings = SchemaValidator.validate(
			new SAXSource(new InputSource(new StringReader(schema))),
			new StringReader(field.toXML())
		);
		if (warnings.isEmpty())
			System.out.println("Field " + field.getName() + " passes validation");
		else {
			System.out.println("Validation Warnings (" + warnings.size() + ")");
			for (String warning : warnings) 
				System.out.println(warning);
		}
	}

}
