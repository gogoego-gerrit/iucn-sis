//package org.iucn.sis.server.api.persistance;
///**
// * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
// * 
// * This is an automatic generated file. It will be regenerated every time 
// * you generate persistence class.
// * 
// * Modifying its content may cause the program not work, or your work may lost.
// */
//
///**
// * Licensee: 
// * License Type: Evaluation
// */
//import java.util.Properties;
//
//import javassist.util.proxy.MethodFilter;
//
//import org.gogoego.api.plugins.GoGoEgo;
//import org.hibernate.FlushMode;
//import org.hibernate.cfg.Configuration;
//import org.hibernate.dialect.PostgreSQLDialect;
//import org.hibernate.tuple.entity.EntityTuplizerFactory;
//import org.hibernate.tuple.entity.PojoEntityTuplizer;
//import org.iucn.sis.server.api.application.MultiClassLoader;
//import org.iucn.sis.server.api.application.PersistentManager;
//import org.iucn.sis.server.api.application.SISConfiguration;
//import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
//import org.postgresql.Driver;
//import org.slf4j.impl.StaticLoggerBinder;
//
//
//
//public class CopyOfSISPersistentManager extends PersistentManager {
//	
//	
//	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
//	public static void initalize() {
//		
//		String driverClass = GoGoEgo.getInitProperties().getProperty("dbsession.sis.driver");
//		String dialect = GoGoEgo.getInitProperties().getProperty("database_dialect");
//		String connectionURL = GoGoEgo.getInitProperties().getProperty("dbsession.sis.uri");
//		String username = GoGoEgo.getInitProperties().getProperty("dbsession.sis.user");
//		String password = GoGoEgo.getInitProperties().getProperty("dbsession.sis.password");
//				
//		setCurrentThread();
//		_connectionSetting = new JDBCConnectionSetting(driverClass, dialect, connectionURL, username, password);
//		
//		
//	}
//	
//	public static void setCurrentThread() {
//		MultiClassLoader loader = new MultiClassLoader(CopyOfSISPersistentManager.class.getClassLoader().getParent());
//		loader.addClassLoader(CopyOfSISPersistentManager.class.getClassLoader());
//		loader.addClassLoader(Driver.class.getClassLoader());
//		loader.addClassLoader(PostgreSQLDialect.class.getClassLoader());
//		loader.addClassLoader(MethodFilter.class.getClassLoader());
//		loader.addClassLoader(PojoEntityTuplizer.class.getClassLoader());
//		loader.addClassLoader(EntityTuplizerFactory.class.getClassLoader());
//		loader.addClassLoader(StaticLoggerBinder.class.getClassLoader());
//		
//		Thread.currentThread().setContextClassLoader(loader);
//	}
//	
//	
//	@Override
//	protected Configuration createConfiguration() {
//		return new SISConfiguration();
//	}
//	
//	@Override
//	public Session getSession() throws PersistentException {
////		// TODO Auto-generated method stub
//		return super.getSession();
//		
//		
//	}
//    
//	private static MultiClassLoader loader;
//	
//	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
//	
//	private static final String PROJECT_NAME = "liz";
//	private static PersistentManager _instance = null;
//	private static SessionType _sessionType = SessionType.THREAD_BASE;
//	private static int _timeToAlive = 60000;
//	private static JDBCConnectionSetting _connectionSetting = null;
//	private static Properties _extraProperties = null;
//	
//	private CopyOfSISPersistentManager() throws PersistentException {
//		super(_connectionSetting, _sessionType, _timeToAlive, new String[] {}, _extraProperties);
//		setFlushMode(FlushMode.AUTO);
//	}
//	
//	public String getProjectName() {
//		return PROJECT_NAME;
//	}
//	
//	public static synchronized final PersistentManager instance() throws PersistentException {
//		if (_instance == null) {
//			initalize();
//			_instance = new CopyOfSISPersistentManager();
//		}
//		
//		return _instance;
//	}
//	
//	public void disposePersistentManager() throws PersistentException {
//		_instance = null;
//		super.disposePersistentManager();
//	}
//	
//	public static void setSessionType(SessionType sessionType) throws PersistentException {
//		if (_instance != null) {
//			throw new PersistentException("Cannot set session type after create PersistentManager instance");
//		}
//		else {
//			_sessionType = sessionType;
//		}
//		
//	}
//	
//	public static void setAppBaseSessionTimeToAlive(int timeInMs) throws PersistentException {
//		if (_instance != null) {
//			throw new PersistentException("Cannot set session time to alive after create PersistentManager instance");
//		}
//		else {
//			_timeToAlive = timeInMs;
//		}
//		
//	}
//	
//	public static void setJDBCConnectionSetting(JDBCConnectionSetting aConnectionSetting) throws PersistentException {
//		if (_instance != null) {
//			throw new PersistentException("Cannot set connection setting after create PersistentManager instance");
//		}
//		else {
//			_connectionSetting = aConnectionSetting;
//		}
//		
//	}
//	
//	public static void setHibernateProperties(Properties aProperties) throws PersistentException {
//		if (_instance != null) {
//			throw new PersistentException("Cannot set hibernate properties after create PersistentManager instance");
//		}
//		else {
//			_extraProperties = aProperties;
//		}
//		
//	}
//	
//	public static void saveJDBCConnectionSetting() {
//		PersistentManager.saveJDBCConnectionSetting(PROJECT_NAME, _connectionSetting);
//	}
//	
//	
//	
//	
//}
