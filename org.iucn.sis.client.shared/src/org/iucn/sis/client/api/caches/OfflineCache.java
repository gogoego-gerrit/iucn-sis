package org.iucn.sis.client.api.caches;

import org.iucn.sis.client.api.utils.HasCache;
import org.iucn.sis.client.api.utils.SIS;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.OfflineMetadata;

import com.google.gwt.user.client.Window;
import com.solertium.lwxml.gwt.NativeDocumentImpl;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.events.ComplexListener;

public class OfflineCache implements HasCache{
	
	public static final OfflineCache impl = new OfflineCache();
	
	private OfflineMetadata metadata;
	
	private OfflineCache() {
		metadata = new OfflineMetadata();
	}
	
	public void openManager() {
		openManager(null);
	}
	
	public void openManager(String queryParams) {
		String query = "";
		if (queryParams != null && !"".equals(queryParams))
			query = "?" + queryParams;
		
		Window.Location.assign(UriBase.getInstance().getOfflineBase() + "/manager" + query);
	}
	
	public void checkForUpdates(final ComplexListener<Integer> listener) {
		if (SIS.isSoftwareCurrent())
			listener.handleEvent(0);
		else {
			final String uri = UriBase.getInstance().getUpdatesBase() + 
				"/updates/" + SIS.getSoftwareVersion() + "/list";
			final NativeDocument document = new NativeDocumentImpl();
			document.get(uri, new GenericCallback<String>() {
				public void onSuccess(String result) {
					try {
						listener.handleEvent(Integer.valueOf(
							document.getDocumentElement().getAttribute("count")
						));
					} catch (Exception e) {
						listener.handleEvent(0);
					}
				}
				public void onFailure(Throwable caught) {
					listener.handleEvent(0);
				}
			});
		}
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
