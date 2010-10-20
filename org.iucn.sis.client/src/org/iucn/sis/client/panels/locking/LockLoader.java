package org.iucn.sis.client.panels.locking;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;

public class LockLoader {
	
	public static final LockLoader impl = new LockLoader();
	
	private final Map<String, RowData> persistentLocks;
	
	private LockLoader() {
		persistentLocks = new LinkedHashMap<String, RowData>();
	}
	
	public void load(final GenericCallback<Object> callback) {
		persistentLocks.clear();
		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getSISBase() +"/management/locks/persistentlock", new GenericCallback<String>() {
			public void onSuccess(String result) {
				final RowParser parser = new RowParser(document);
				for (RowData row : parser.getRows())
					persistentLocks.put(row.getField("lockid"), row);
				
				callback.onSuccess(null);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public Collection<RowData> getPersistentLocks() {
		return persistentLocks.values();
	}
	
	public void removePersistentLock(String id) {
		persistentLocks.remove(id);
	}
	
	public void removePersistentLockGroup(Collection<String> persistentLockIDs) {
		for (String lockID : persistentLockIDs)
			persistentLocks.remove(lockID);
	}

}
