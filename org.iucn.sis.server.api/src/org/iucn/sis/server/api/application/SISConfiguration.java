package org.iucn.sis.server.api.application;

import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Settings;
import org.iucn.sis.server.api.persistance.SISPersistentManager;

public class SISConfiguration extends Configuration {
	
	public SISConfiguration() {
		super(new SISSettingsFactory());
	}

	@Override
	public Configuration configure(String configFile) throws HibernateException {
		// TODO Auto-generated method stub
		return configure(SISPersistentManager.class.getResource("ormmapping/liz.cfg.xml"));
//		return  doConfigure(SISPersistentManager.class.getResourceAsStream("ormmapping/liz.cfg.xml"), "");
	}
	
	@Override
	public Settings buildSettings(Properties props) throws HibernateException {
		// TODO Auto-generated method stub
		return super.buildSettings(props);
	}
	
	
	@Override
	public SessionFactory buildSessionFactory() throws HibernateException {
		System.out.println("in building session factory");
		SISPersistentManager.setCurrentThread();
		return super.buildSessionFactory();
//		SessionFactory factory =  super.buildSessionFactory();
//		secondPassCompile();
//		
//		Mapping mapping = buildMapping();
//		
//		Iterator iter = classes.values().iterator();
//		while ( iter.hasNext() ) {
//			( (PersistentClass) iter.next() ).validate( mapping );
//		}
//		iter = collections.values().iterator();
//		while ( iter.hasNext() ) {
//			( (Collection) iter.next() ).validate( mapping );
//		}
//		
//		Environment.verifyProperties(getProperties());
//		
//		Properties copy = new Properties();
//		copy.putAll( getProperties() );
//		PropertiesHelper.resolvePlaceHolders(copy);
//		Settings settings = buildSettings(copy);
//		System.out.println("after building session factory");
//		return null;
	}
	
	
//	/**
//	 * Instantiate a new <tt>SessionFactory</tt>, using the properties and
//	 * mappings in this configuration. The <tt>SessionFactory</tt> will be
//	 * immutable, so changes made to the <tt>Configuration</tt> after
//	 * building the <tt>SessionFactory</tt> will not affect it.
//	 *
//	 * @return a new factory for <tt>Session</tt>s
//	 * @see org.hibernate.SessionFactory
//	 */
//	public SessionFactory buildSessionFactory() throws HibernateException {
//		log.debug( "Preparing to build session factory with filters : " + filterDefinitions );
//		secondPassCompile();
//		validate();
//		Environment.verifyProperties( properties );
//		Properties copy = new Properties();
//		copy.putAll( properties );
//		PropertiesHelper.resolvePlaceHolders( copy );
//		Settings settings = buildSettings( copy );
//
//		return new SessionFactoryImpl(
//				this,
//				mapping,
//				settings,
//				getInitializedEventListeners()
//			);
//	}
	
}
