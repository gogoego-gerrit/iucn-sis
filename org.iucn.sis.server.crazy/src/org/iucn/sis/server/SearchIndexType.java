package org.iucn.sis.server;

import org.w3c.dom.Document;

public interface SearchIndexType {

	public long getLastModified(String key);

	public void index(Document doc, long lastModified);

}
