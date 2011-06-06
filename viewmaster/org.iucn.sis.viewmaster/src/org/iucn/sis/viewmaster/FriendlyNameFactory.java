package org.iucn.sis.viewmaster;

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
		for (String name : CanonicalNames.allCanonicalNames)
			pretty.put(name.toUpperCase(), name);
		
		//TODO: Birdlife fields?
	}
	
	public String toFriendlyName(String name) {
		String p = pretty.get(name.toUpperCase());
		return p == null ? name : p;
	}

}
