package org.iucn.sis.server.extensions.videos;

import java.util.ArrayList;
import java.util.Collection;

import org.iucn.sis.server.api.application.SimpleSISApplication;

/**
 * ServerApplication.java
 * 
 * Runs only online, since you probably can't get to videos 
 * any other way...
 * 
 * Supports a setting for a source key for YouTube video 
 * sources.
 * 
 * @author carl.scott@solertium.com
 *
 */
public class ServerApplication extends SimpleSISApplication {
	
	public ServerApplication() {
		super(RunMode.ONLINE);
	}

	@Override
	public void init() {
		addServiceToRouter(new YouTubeVideoSource(app.getContext()));
	}
	
	@Override
	protected Collection<String> getSettingsKeys() {
		final ArrayList<String> keys = new ArrayList<String>();
		keys.add(YouTubeVideoSource.SOURCE_KEY);
		
		return keys;
	}

}
