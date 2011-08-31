package org.iucn.sis.client.api.caches;

public class CacheEntry<T> {
	
	private T entry;
	private FetchMode mode;
	
	public CacheEntry(T entry, FetchMode mode) {
		this.entry = entry;
		this.mode = mode;
	}
	
	public T getEntry() {
		return entry;
	}
	
	public FetchMode getMode() {
		return mode;
	}

}
