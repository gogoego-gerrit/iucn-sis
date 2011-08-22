package org.iucn.sis.client.api.utils;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;

public interface HasCache {
	
	public String getCacheUrl();
	
	public GenericCallback<NativeDocument> getCacheInitializer();

}
