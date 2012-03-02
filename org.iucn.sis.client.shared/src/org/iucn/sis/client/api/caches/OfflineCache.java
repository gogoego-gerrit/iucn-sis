package org.iucn.sis.client.api.caches;

import org.iucn.sis.client.api.utils.HasCache;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.OfflineMetadata;

import com.google.gwt.user.client.Window;
import com.solertium.lwxml.gwt.NativeDocumentImpl;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class OfflineCache implements HasCache {
	
	public static final OfflineCache impl = new OfflineCache();
	
	private OfflineMetadata metadata;
	private int updateCount;
	
	private OfflineCache() {
		metadata = new OfflineMetadata();
		updateCount = 0;
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
	
	public int getUpdateCount() {
		return updateCount;
	}
	
	public void initialize(final GenericCallback<Object> callback) {
		final String uri = UriBase.getInstance().getOfflineBase() + 
			"/services/init";
		final NativeDocument document = new NativeDocumentImpl();
		document.get(uri, new GenericCallback<String>() {
			public void onSuccess(String result) {
				NativeNodeList nodes = document.getDocumentElement().getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					NativeNode node = nodes.item(i);
					if ("offline".equals(node.getNodeName())) {
						OfflineMetadata md = OfflineMetadata.fromXML(node);
						if ("none".equals(md.getName()))
							callback.onFailure(new Exception());
						else
							metadata = md;
					}
					else if ("updates".equals(node.getNodeName()))
						updateCount = Integer.valueOf(((NativeElement)node).getAttribute("count"));
				}
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
