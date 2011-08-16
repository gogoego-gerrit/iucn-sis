package core;

import java.io.File;

import javax.naming.NamingException;

import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.ProductProperties;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.shared.api.debug.Debug;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.solertium.db.DBSessionFactory;
import com.solertium.util.CurrentBinary;

/**
 * Base class that you can use to create a test 
 * case that depends on having your database and 
 * a Hibernate Session available.
 * 
 * This calls in to your Hibernate database when 
 * the class is loaded based on your local config 
 * properties, and shuts it down afterwards.  Also 
 * provided are utility methods for opening & 
 * closing sessions and transactions.
 * 
 * @author carl.scott
 *
 */
public class BasicHibernateTest extends BasicTest {
	
	/**
	 * Instantiates a database & SIS based on your 
	 * local configuration properties, and sets 
	 * the debugger to the test mode version.
	 */
	@BeforeClass
	public static void startup() throws NamingException {
		BasicTest core = new BasicTest();
		
		//Grab the properties file from this project
		File file = CurrentBinary.getDirectory(core);
		if (file.getAbsolutePath().endsWith(File.separatorChar + "bin"))
			file = file.getParentFile();
		ProductProperties.impl.setWorkingDirectory(file);
		
		//Register the database specified in the properties file
		DBSessionFactory.registerDataSources(GoGoEgo.getInitProperties());
		
		//Set the debugger to this.
		Debug.setInstance(core);
		
		//Calls SISPersistentManager.instance()
		SIS.get();
	}
	
	public BasicHibernateTest() {
		super();
	}
	
	/**
	 * Opens a new session.  This is useful for fetching 
	 * data, but if you plan on doing inserts, updates, 
	 * or deletions, be sure you begin a transaction first, 
	 * or call the openTransaction method instead. 
	 * @return new session
	 */
	protected Session openSession() {
		return SISPersistentManager.instance().openSession();
	}
	
	/**
	 * Opens a new session and begins a new transaction. 
	 * This can be used for any sort of operations, but 
	 * is necessary when attempting inserts, updates, or 
	 * deletions.  If you are only doing selects, you can 
	 * safely call openSession instead.
	 * @return new session with an open transaction 
	 */
	protected Session openTransaction() {
		Session session = SISPersistentManager.instance().openSession();
		session.beginTransaction();
		return session;
	}
	
	/**
	 * Commits the current transaction and closes the 
	 * session.  Not useful if you are attempting 
	 * multiple transactions, but typically, for test 
	 * cases, this is good.
	 * @param session
	 */
	protected void closeTransaction(Session session) {
		session.getTransaction().commit();
		session.close();
	}
	
	/**
	 * Closes an open session.
	 * @param session
	 */
	protected void closeSession(Session session) {
		session.close();
	}

	/**
	 * Shuts down the database, necessary for H2 
	 * to remove locks.
	 */
	@AfterClass
	public static void shutdown() {
		SISPersistentManager.instance().shutdown();
	}

}
