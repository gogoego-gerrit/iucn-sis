package org.iucn.sis.client.locking;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.iucn.sis.client.simple.SimpleSISClient;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;

public class LockLoader {
	
	public static final LockLoader impl = new LockLoader();
	
	private final Map<String, RowData> persistentLocks;
	private final Map<String, RowData> persistentLockGroups;
	
	private LockLoader() {
		persistentLocks = new LinkedHashMap<String, RowData>();
		persistentLockGroups = new LinkedHashMap<String, RowData>();
	}
	
	public void load(final GenericCallback<Object> callback) {
		persistentLocks.clear();
		persistentLockGroups.clear();
		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
		document.get("/management/locks/persistentlock", new GenericCallback<String>() {
			public void onSuccess(String result) {
				final RowParser parser = new RowParser(document);
				for (RowData row : parser.getRows())
					persistentLocks.put(row.getField("id"), row);
				
				final NativeDocument groupDocument = SimpleSISClient.getHttpBasicNativeDocument();
				groupDocument.get("/management/locks/persistentlockgroup", new GenericCallback<String>() {
					public void onSuccess(String result) {
						final RowParser gParser = new RowParser(groupDocument);
						for (RowData row : gParser.getRows())
							persistentLockGroups.put(row.getField("id"), row);
						callback.onSuccess(null);
					}
					public void onFailure(Throwable caught) {
						callback.onSuccess(null);
					}
				});
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public Collection<RowData> getPersistentLockGroups() {
		return persistentLockGroups.values();
	}
	
	public Collection<RowData> getPersistentLocks() {
		return persistentLocks.values();
	}
	
	public RowData getPersistentLockGroup(String id) {
		return persistentLockGroups.get(id);
	}
	
	public void removePersistentLock(String id) {
		persistentLocks.remove(id);
	}
	
	public void removePersistentLockGroup(String id, Collection<String> persistentLockIDs) {
		for (String lockID : persistentLockIDs)
			persistentLocks.remove(lockID);
		persistentLockGroups.remove(id);
	}

}
