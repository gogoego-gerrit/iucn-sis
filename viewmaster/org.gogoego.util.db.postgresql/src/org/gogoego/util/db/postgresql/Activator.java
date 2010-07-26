package org.gogoego.util.db.postgresql;

import org.gogoego.util.db.DBSessionFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	DBSessionFactory factory = null;
	
	boolean started = false;
	
	public void start(final BundleContext context) throws Exception {
		if(started) return;
		
		PostgreSQLDBSessionFactory.getInstance().register();
		started = true;
	}

	public void stop(BundleContext context) throws Exception {
		if(!started) return;
		if(factory==null) DBSessionFactory.unregisterFactory(factory);
	}

}
