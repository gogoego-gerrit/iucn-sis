package org.iucn.sis.server.api.fields;

import java.util.Properties;

import javax.naming.NamingException;

import com.solertium.db.DBSessionFactory;
import com.solertium.util.BaseDocumentUtils;

public class TreeBuilderTesting {
	
	public static void main(String[] args) {
		Properties properties = new Properties();
		properties.setProperty("dbsession.sis_lookups.uri","jdbc:h2:file:/var/sis/databases/sis_lookups");
		properties.setProperty("dbsession.sis_lookups.driver","org.h2.Driver");
		properties.setProperty("dbsession.sis_lookups.user","sa");
		properties.setProperty("dbsession.sis_lookups.password","");
		
		final TreeBuilder builder;
		
		try {
			DBSessionFactory.registerDataSources(properties);
			builder = new TreeBuilder();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
		
		System.out.println(BaseDocumentUtils.impl.serializeDocumentToString(builder.buildTree("Stresses"), true, true));
	}

}
