package org.iucn.sis.server.api.persistance;


import javassist.util.proxy.MethodFilter;

import org.dom4j.DocumentException;
import org.gogoego.api.plugins.GoGoEgo;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.tuple.entity.EntityTuplizerFactory;
import org.hibernate.tuple.entity.PojoEntityTuplizer;
import org.iucn.sis.server.api.application.MultiClassLoader;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.persistance.listeners.SISHibernateListener;
import org.iucn.sis.server.api.persistance.ormmapping.ClassLoaderTester;
import org.postgresql.Driver;
import org.slf4j.impl.StaticLoggerBinder;

public class SISPersistentManager {

	private static SISPersistentManager instance;
	private SessionFactory sessionFactory;
	private static final String PROJECT_NAME = "liz";
	

	public static void setCurrentThread() {
		MultiClassLoader loader = new MultiClassLoader(SISPersistentManager.class.getClassLoader().getParent());
		loader.addClassLoader(SISPersistentManager.class.getClassLoader());
		loader.addClassLoader(Driver.class.getClassLoader());
		loader.addClassLoader(PostgreSQLDialect.class.getClassLoader());
		loader.addClassLoader(MethodFilter.class.getClassLoader());
		loader.addClassLoader(PojoEntityTuplizer.class.getClassLoader());
		loader.addClassLoader(EntityTuplizerFactory.class.getClassLoader());
		loader.addClassLoader(StaticLoggerBinder.class.getClassLoader());
		loader.addClassLoader(DocumentException.class.getClassLoader());
		loader.addClassLoader(DocumentException.class.getClassLoader().getParent());
		loader.addClassLoader(SISHibernateListener.class.getClassLoader());
		Thread.currentThread().setContextClassLoader(loader);
	}

	public static synchronized final SISPersistentManager instance() {
		if (instance == null) {
			System.out.println("getting new instance");
			setCurrentThread();
			instance = new SISPersistentManager();
			instance.sessionFactory = instance.buildSessionFactory();
		}
		return instance;
	}

	private SessionFactory buildSessionFactory() {
		return buildConfiguration().buildSessionFactory();
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public Session getSession() {
		setCurrentThread();
		return sessionFactory.getCurrentSession();
	}

	public String getProjectName() {
		return PROJECT_NAME;
	}

	private Configuration buildConfiguration() {
		Configuration configuation;
		setCurrentThread();
		try {
			configuation = new Configuration();
		} catch (Throwable e) {
			e.getCause().printStackTrace();
			return null;
		}
		configuation.configure(ClassLoaderTester.class.getResource(getProjectName() + ".cfg.xml"));
		
		String driverClass = GoGoEgo.getInitProperties().getProperty("dbsession.sis.driver");
		String dialect = GoGoEgo.getInitProperties().getProperty("database_dialect");
		String connectionURL = GoGoEgo.getInitProperties().getProperty("dbsession.sis.uri");
		String username = GoGoEgo.getInitProperties().getProperty("dbsession.sis.user");
		String password = GoGoEgo.getInitProperties().getProperty("dbsession.sis.password");
		SISHibernateListener listener = new SISHibernateListener();
		
		configuation.setProperty("hibernate.dialect", dialect);
		configuation.setProperty("hibernate.connection.driver_class", driverClass);
		configuation.setProperty("hibernate.connection.url", connectionURL);
		configuation.setProperty("hibernate.connection.username", username);
		configuation.setProperty("hibernate.connection.password", password);
		configuation.setListener("post-update", listener);
		configuation.setListener("post-delete", listener);
		configuation.setListener("post-insert", listener);

		
		return configuation;

	}
	

	public void saveObject(Object obj) throws PersistentException {		
		Session session = getSession();
		session.saveOrUpdate(obj);
	}

	public void deleteObject(Object obj) throws PersistentException {
		Session session = getSession();
		session.delete(obj);
	}

	public void lockObject(Object obj) throws PersistentException {
		Session session = getSession();
		session.lock(obj, LockMode.NONE);
	}
	
	

}
