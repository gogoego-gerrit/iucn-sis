package org.iucn.sis.server.api.application;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationActivator;
import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.applications.GoGoEgoApplicationFactory;
import org.gogoego.api.applications.GoGoEgoApplicationManagement;
import org.gogoego.api.applications.GoGoEgoApplicationMetaData;

import com.solertium.vfs.VFS;

public abstract class SISActivator extends GoGoEgoApplicationActivator {
	
	/**
	 * Returns a new instance of the SIS APPLICATION
	 * @return
	 */
	protected abstract SISApplication getInstance();
	
	
	/**
	 * Returns the description of the application
	 * @return
	 */
	protected abstract String getAppDescription();
	
	
	/**
	 * Returns the name of the application
	 * @return
	 */
	protected abstract String getAppName();
	
	
	/**
	 * Called on uninstall.. 
	 * 
	 * override if you want something to happen
	 * 
	 * @param vfs
	 */
	protected void onUninstall(VFS vfs) {
		
	}
	
	/**
	 * Called on install
	 * 
	 * override if you want something to happen
	 * 
	 * @param vfs
	 */
	protected void onInstall(VFS vfs) throws GoGoEgoApplicationException {
		
	}
	
	@Override
	public final GoGoEgoApplicationFactory getApplicationFactory() {
		return new GoGoEgoApplicationFactory() {
		
			@Override
			public GoGoEgoApplication newInstance() {
				return getInstance();
			}
		
			@Override
			public GoGoEgoApplicationMetaData getMetaData() {
				return new GoGoEgoApplicationMetaData() {
				
					@Override
					public String getName() {
						return getAppName();
					}
				
					@Override
					public String getDescription() {
						return getAppDescription();
					}
				};
			}
		
			@Override
			public GoGoEgoApplicationManagement getManagement() {
				return new GoGoEgoApplicationManagement() {
				
					@Override
					public void uninstall(VFS vfs) throws GoGoEgoApplicationException {
						onUninstall(vfs);
				
					}
				
					@Override
					public void install(VFS vfs) throws GoGoEgoApplicationException {
						onInstall(vfs);
				
					}
				};
			}
		};
	}
	

}
