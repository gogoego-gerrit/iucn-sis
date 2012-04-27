package org.iucn.sis.server.extensions.export.access.exported;

import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.shared.api.utils.CanonicalNames;

public class FriendlyNameFactory {
	
	private static final FriendlyNameFactory impl = new FriendlyNameFactory();
	
	public static String get(String name) {
		return impl.toFriendlyName(name);
	}
	
	private final Map<String, String> pretty;
	
	private FriendlyNameFactory() {
		pretty = new HashMap<String, String>();
		pretty.put("STRESSES", "Stresses");
		for (String[] array : new String[][] { CanonicalNames.allCanonicalNames, 
				org.iucn.sis.server.schemas.birdlife.CanonicalNames.values, 
				org.iucn.sis.server.schemas.usetrade.CanonicalNames.values})
			for (String name : array)
				pretty.put(name.toUpperCase(), name);
	}
	
	public String toFriendlyName(String name) {
		String p = pretty.get(name.toUpperCase());
		if (p == null)
			System.err.println("Friendly name cache miss for " + name);
		return p == null ? name : p;
	}

}
