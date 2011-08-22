package org.iucn.sis.client.api.caches;

import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.HasCache;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.models.Marked;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;

public class MarkedCache implements HasCache {
	
	public static final String WORKING_SET = "working_set";
	public static final String ASSESSMENT = "assessment";
	public static final String TAXON = "taxon";

	public static final String GREEN = "green-style";
	public static final String RED = "red-style";
	public static final String BLUE = "blue-style";
	public static final String NONE = "regular-style";

	public static final MarkedCache impl = new MarkedCache();

	private final Map<String, Map<Integer, Marked>> cache;

	private MarkedCache() {
		cache = new HashMap<String, Map<Integer,Marked>>();
	}

	public String getAssessmentStyle(Integer id) {
		return getStyle(cache.get(ASSESSMENT), id);
	}

	public String getTaxaStyle(Integer id) {
		return getStyle(cache.get(TAXON), id);
	}

	public String getWorkingSetStyle(Integer id) {
		return getStyle(cache.get(WORKING_SET), id);
	}
	
	private String getStyle(Map<Integer, Marked> map, Integer id) {
		String style = NONE;
		if (map != null && map.containsKey(id)) {
			Marked marked = map.get(id);
			if (marked != null)
				style = marked.getMark();
		}
		return style;
	}

	public void markAssement(Integer id, String style) {
		mark(ASSESSMENT, id, style);
	}

	public void markTaxa(Integer id, String style) {
		mark(TAXON, id, style);
	}

	public void markWorkingSet(Integer id, String style) {
		mark(WORKING_SET, id, style);
	}
	
	private void mark(String type, Integer id, String style) {
		Map<Integer, Marked> map = cache.get(type);
		if (map == null) {
			map = new HashMap<Integer, Marked>();
			cache.put(type, map);
		}
		
		if (style.equalsIgnoreCase(GREEN) || style.equalsIgnoreCase(RED) || style.equalsIgnoreCase(BLUE)) {
			Marked marked = map.get(id);
			if (marked == null) {
				marked = new Marked();
				marked.setObjectid(id);
				marked.setType(type);
				marked.setUser(SISClientBase.currentUser);	
			}
			marked.setMark(style);
			
			map.put(id, marked);
			save();
		}
		else
			unmark(type, id);
	}

	public void onLogout() {
		cache.clear();
	}

	private void parseXML(NativeDocument doc) throws NullPointerException {
		NativeNodeList nodes = doc.getDocumentElement().getElementsByTagName("marked");
		for (int i = 0; i < nodes.getLength(); i++) {
			Marked marked = Marked.fromXML(nodes.elementAt(i));
			Map<Integer, Marked> map = cache.get(marked.getType());
			if (map == null)
				map = new HashMap<Integer, Marked>();
			
			map.put(marked.getObjectid(), marked);
			
			cache.put(marked.getType(), map);
		}
	}

	private void save() {
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.put(UriBase.getInstance().getTagBase() +"/mark/" + SISClientBase.currentUser.getUsername(), toXML(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				update();
			}
			public void onSuccess(String arg0) {
			}
		});
	}

	private String toXML() {
		StringBuilder out = new StringBuilder();
		out.append("<root>");
		
		for (Map<Integer, Marked> map : cache.values())
			for (Marked marked : map.values())
				out.append(marked.toXML());
		
		out.append("</root>");
		return out.toString();
	}

	public void unmarkAssessment(Integer id) {
		unmark(ASSESSMENT, id);
	}

	public void unmarkTaxon(Integer id) {
		unmark(TAXON, id);
	}

	public void unmarkWorkingSet(Integer id) {
		unmark(WORKING_SET, id);
	}
	
	private void unmark(String type, Integer id) {
		Map<Integer, Marked> map = cache.get(type);
		if (map != null && map.containsKey(id)) {
			map.remove(id);
			save();
		}
	}

	/**
	 * Gets the last save version of marked.xml
	 */
	public void update() {
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.get(getCacheUrl(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				cache.clear();
			}
			public void onSuccess(String arg0) {
				parseXML(ndoc);
			}
		});
	}
	
	@Override
	public GenericCallback<NativeDocument> getCacheInitializer() {
		return new GenericCallback<NativeDocument>() {
			public void onFailure(Throwable caught) {
				cache.clear();
			}
			public void onSuccess(NativeDocument document) {
				parseXML(document);
			}
		};
	}
	
	@Override
	public String getCacheUrl() {
		return UriBase.getInstance().getTagBase() + "/mark/" + SISClientBase.currentUser.getUsername();
	}

}
