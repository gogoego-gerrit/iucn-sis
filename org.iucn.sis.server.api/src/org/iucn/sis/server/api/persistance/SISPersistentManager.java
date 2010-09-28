package org.iucn.sis.server.api.persistance;


import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javassist.util.proxy.MethodFilter;

import org.dom4j.DocumentException;
import org.gogoego.api.plugins.GoGoEgo;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
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
import org.iucn.sis.shared.api.debug.Debug;
import org.postgresql.Driver;
import org.slf4j.impl.StaticLoggerBinder;

public class SISPersistentManager {

	private static SISPersistentManager instance;
	private SessionFactory sessionFactory;

	public static void setCurrentThread() {
		Thread.currentThread().setContextClassLoader(createClassLoader());
	}
	
	private static ClassLoader createClassLoader() {
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
		
		return loader;
	}

	public static synchronized final SISPersistentManager instance() {
		if (instance == null || instance.sessionFactory == null) {
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

	private Configuration buildConfiguration() {
		Configuration configuration = new SISPersistenceConfiguration();
		setCurrentThread();
		
		final String configUri = GoGoEgo.getInitProperties().getProperty("org.iucn.sis.server.configuration.uri", "local:SIS");
		if (configUri.startsWith("file")) {
			Debug.println("Creating new persistence manager from file " + configUri);
			try {
				configuration.configure(new URL(configUri));
			} catch (MalformedURLException e) {
				throw new RuntimeException(configUri + " is not a valid URL", e);
			} catch (HibernateException e) {
				throw new RuntimeException(e);
			}
		}
		else if (configUri.startsWith("local:") && !configUri.equals("local:")) {
			final String name = configUri.substring(configUri.indexOf(':')+1);
			Debug.println("Creating new persistence manager from local resource " + name);
			try {
				configuration.configure(ClassLoaderTester.class.getResource(name + ".cfg.xml"));
			} catch (HibernateException e) {
				throw new RuntimeException(e);
			}
		}
		
		String driverClass = GoGoEgo.getInitProperties().getProperty("dbsession.sis.driver");
		String dialect = GoGoEgo.getInitProperties().getProperty("database_dialect");
		String connectionURL = GoGoEgo.getInitProperties().getProperty("dbsession.sis.uri");
		String username = GoGoEgo.getInitProperties().getProperty("dbsession.sis.user");
		String password = GoGoEgo.getInitProperties().getProperty("dbsession.sis.password");
		
		SISHibernateListener listener = new SISHibernateListener();
		
		configuration.setProperty("hibernate.dialect", dialect);
		configuration.setProperty("hibernate.connection.driver_class", driverClass);
		configuration.setProperty("hibernate.connection.url", connectionURL);
		configuration.setProperty("hibernate.connection.username", username);
		configuration.setProperty("hibernate.connection.password", password);
		configuration.setListener("post-update", listener);
		configuration.setListener("post-delete", listener);
		configuration.setListener("post-insert", listener);

		return configuration;
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
		//FIXME: should we be using the code below?
		//session.buildLockRequest(LockOptions.NONE).lock(obj);
	}
	
	@SuppressWarnings("unchecked")
	public <X> X getObject(Class<X> clazz, Serializable id) throws PersistentException {
		Session session = getSession();
		try {
			return (X) session.get(clazz, id);
		} catch (ClassCastException e) {
			throw new PersistentException(e);
		} catch (HibernateException e) {
			throw new PersistentException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <X> List<X> listObjects(Class<X> criteria) throws PersistentException {
		Session session = getSession();
		try {
			return session.createCriteria(criteria).list();
		} catch (ClassCastException e) {
			throw new PersistentException(e);
		} catch (HibernateException e) {
			throw new PersistentException(e);
		}
	}
	
	private static class SISPersistenceConfiguration extends Configuration {
		
		private static final long serialVersionUID = 1L;
		
		public SISPersistenceConfiguration() {
			super();
		}
		
		@Override
		public Configuration addResource(String arg0) throws MappingException {
			return addResource(arg0, ClassLoaderTester.class.getClassLoader());
		}
		
		@Override
		public Configuration addResource(String arg0, ClassLoader arg1)
				throws MappingException {
			return addInputStream(arg1.getResourceAsStream(arg0));
		}
	}

}
