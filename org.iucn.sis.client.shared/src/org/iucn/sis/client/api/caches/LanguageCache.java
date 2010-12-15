package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.models.IsoLanguage;

import com.solertium.lwxml.gwt.utils.ClientDocumentUtils;
import com.solertium.lwxml.shared.GWTConflictException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;

public class LanguageCache {
	
	public static final LanguageCache impl = new LanguageCache();
	
	private Map<Integer, IsoLanguage> cacheByID;
	private Map<String, IsoLanguage> cacheByCode;
	
	private LanguageCache() {
		cacheByCode = null;
		cacheByID = null;
	}
	
	private void init(final SimpleListener callback) {
		if (cacheByID != null) {
			callback.handleEvent();
			return;
		}
		
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getSISBase() + "/languages", new GenericCallback<String>() {
			public void onSuccess(String result) {
				cacheByID = new LinkedHashMap<Integer, IsoLanguage>();
				cacheByCode = new LinkedHashMap<String, IsoLanguage>();
				
				final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("language");
				for (int i = 0; i < nodes.getLength(); i++) {
					final NativeElement node = nodes.elementAt(i);
					if ("language".equals(node.getNodeName())) {
						IsoLanguage language = IsoLanguage.fromXML(node);
						cacheByID.put(language.getId(), language);
						cacheByCode.put(language.getCode(), language);
					}
				}
				
				callback.handleEvent();
			}
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Error Loading Languages", "Could not load "
						+ "languages for the drop down. Please check your Internet "
						+ "connectivity if you are running online, or check your local "
						+ "server if you are running offline, then try again.");
				//callback.handleEvent();
			}
		});
	}
	
	public void get(Integer id, final ComplexListener<IsoLanguage> callback) {
		List<Integer> list = new ArrayList<Integer>();
		list.add(id);
	
		get(list, new ComplexListener<List<IsoLanguage>>() {
			public void handleEvent(List<IsoLanguage> eventData) {
				if (eventData.isEmpty())
					callback.handleEvent(null);
				else
					callback.handleEvent(eventData.get(0));
			}
		});
	}
	
	public void getByCode(String code, final ComplexListener<IsoLanguage> callback) {
		List<String> list = new ArrayList<String>();
		list.add(code);
	
		getByCode(list, new ComplexListener<List<IsoLanguage>>() {
			public void handleEvent(List<IsoLanguage> eventData) {
				if (eventData.isEmpty())
					callback.handleEvent(null);
				else
					callback.handleEvent(eventData.get(0));
			}
		});
	}
	
	public void get(final List<Integer> ids, final ComplexListener<List<IsoLanguage>> callback) {
		init(new SimpleListener() {
			public void handleEvent() {
				List<IsoLanguage> list = new ArrayList<IsoLanguage>();
				for (Integer id : ids)
					if (cacheByID.containsKey(id))
						list.add(cacheByID.get(id));
				callback.handleEvent(list);
			}
		});
	}
	
	public void getByCode(final List<String> codes, final ComplexListener<List<IsoLanguage>> callback) {
		init(new SimpleListener() {
			public void handleEvent() {
				List<IsoLanguage> list = new ArrayList<IsoLanguage>();
				for (String code : codes)
					if (cacheByCode.containsKey(code))
						list.add(cacheByCode.get(code));
				callback.handleEvent(list);
			}
		});
	}
	
	public void list(final ComplexListener<List<IsoLanguage>> callback) {
		init(new SimpleListener() {
			public void handleEvent() {
				callback.handleEvent(new ArrayList<IsoLanguage>(cacheByID.values()));
			}
		});
	}
	
	public void add(final IsoLanguage language, final GenericCallback<IsoLanguage> callback) {
		if (cacheByID.containsKey(language.getId())) {
			callback.onFailure(new GWTConflictException("Language already in list."));
			return;
		}
		
		init(new SimpleListener() {
			public void handleEvent() {
				final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
				document.put(UriBase.getInstance().getSISBase() + "/languages", language.toXML(), new GenericCallback<String>() {
					public void onSuccess(String result) {
						IsoLanguage newLanguage = IsoLanguage.fromXML(document.getDocumentElement());
						
						cacheByID.put(newLanguage.getId(), newLanguage);
						cacheByCode.put(newLanguage.getCode(), newLanguage);
						
						callback.onSuccess(newLanguage);
					}
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
			}
		});
	}
	
	public void remove(final IsoLanguage language, final GenericCallback<IsoLanguage> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.delete(UriBase.getInstance().getSISBase() + "/languages/" + language.getId(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				cacheByCode.remove(language.getCode());
				cacheByID.remove(language.getId());
				
				callback.onSuccess(language);
			}
			public void onFailure(Throwable caught) {
				if (caught instanceof GWTConflictException)
					WindowUtils.errorAlert(ClientDocumentUtils.parseStatus(document));
				callback.onFailure(caught);
			}
		});
	}
	
	public void update(final IsoLanguage language, final GenericCallback<IsoLanguage> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.post(UriBase.getInstance().getSISBase() + "/languages/" + language.getId(), language.toXML(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				callback.onSuccess(language);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

}
