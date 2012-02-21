package org.iucn.sis.client.api.caches;

import org.iucn.sis.client.api.utils.HasCache;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.OfflineMetadata;

import com.solertium.lwxml.gwt.NativeDocumentImpl;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;

public class OfflineCache implements HasCache{
	
	public static final OfflineCache impl = new OfflineCache();
	
	private OfflineMetadata metadata;
	
	private OfflineCache() {
		metadata = new OfflineMetadata();
	}
	
	public void initialize(final GenericCallback<Object> callback) {
		final String uri = UriBase.getInstance().getOfflineBase() + 
			"/services/init";
		final NativeDocument document = new NativeDocumentImpl();
		document.get(uri, new GenericCallback<String>() {
			public void onSuccess(String result) {
				OfflineMetadata md = 
					OfflineMetadata.fromXML(document.getDocumentElement());
				if ("none".equals(md.getName()))
					callback.onFailure(new Exception());
				else
					callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	@Override
	public GenericCallback<NativeDocument> getCacheInitializer() {
		return new GenericCallback<NativeDocument>() {
			public void onSuccess(NativeDocument ndoc) {				
				metadata = OfflineMetadata.fromXML(ndoc.getDocumentElement());				
			}
			@Override
			public void onFailure(Throwable caught) {
				Debug.println("Failed to load Offline Metadata.");	
			}
		};
	}

	@Override
	public String getCacheUrl() {
		return UriBase.getInstance().getOfflineBase() +"/offline/metadata";
	}
	
	public OfflineMetadata get() {
		return metadata;
	}

}
