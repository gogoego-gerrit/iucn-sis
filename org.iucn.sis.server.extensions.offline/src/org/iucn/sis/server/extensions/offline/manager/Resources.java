package org.iucn.sis.server.extensions.offline.manager;

import java.io.InputStream;

public class Resources {
	
	public static InputStream get(String name) {
		return Resources.class.getResourceAsStream(name);
	}
	

}
