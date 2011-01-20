package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.RecentlyAccessed;
import org.iucn.sis.shared.api.models.Region;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;

public class RecentlyAcccessedCache {
	
	public static final RecentlyAcccessedCache impl = new RecentlyAcccessedCache();
	
	private static final int CACHE_SIZE = 15;
	
	private final Map<String, List<? extends RecentInfo>> cache;
	
	private RecentlyAcccessedCache() {
		cache = new HashMap<String, List<? extends RecentInfo>>();
	
	}
	public void load(String type) {
		load(type, null);
	}
	
	@SuppressWarnings("serial")
	public void load(final String type, final GenericCallback<Object> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getRecentAssessmentsBase() + "/recent/" + type, new GenericCallback<String>() {
			public void onSuccess(String result) {
				final RowParser parser = new RowParser(document);
				final List<RecentInfo> list = new ArrayList<RecentInfo>() {
					public boolean add(RecentInfo e) {
						return e != null && super.add(e);
					}
				};
				for (RowData row : parser.getRows())
					list.add(parse(type, row));
				
				cache.put(type, list);
				if (callback != null)
					callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				if (callback != null)
					callback.onFailure(caught);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public <X extends RecentInfo> List<X> list(String type) {
		return new ArrayList<X>((List<X>)cache.get(type));
	}
	
	public void delete(final RecentInfo recent, final GenericCallback<Object> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.delete(UriBase.getInstance().getRecentAssessmentsBase() + "/recent/" + 
				recent.getAccessType() + "/" + recent.getAccessID(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				cache.get(recent.getAccessType()).remove(recent);
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public <X extends RecentInfo> void add(final String type, final X recent) {
		final RecentlyAccessed accessed = new RecentlyAccessed();
		accessed.setDate(new Date());
		accessed.setId(0);
		accessed.setObjectid(recent.getObjectID());
		accessed.setType(type);
		accessed.setUser(SISClientBase.currentUser);
		
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.post(UriBase.getInstance().getRecentAssessmentsBase() + "/recent/" + type, accessed.toXML(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				RowParser parser = new RowParser();
				parser.parseInsertRows(document);
				
				String id = parser.getFirstRow().getField("id_0");
				
				List<X> existing = (List<X>)cache.get(type);
				if (existing == null)
					existing = new ArrayList<X>();
				
				boolean found = false;
				X infoToAdd = recent;
				for (X current : existing) {
					if (current.getObjectID().equals(recent.getObjectID())) {
						infoToAdd = current;
						found = true;
						break;
					}
				}
				
				if (id != null)
					infoToAdd.setAccessID(Integer.valueOf(id));
				
				if (found)
					existing.remove(infoToAdd);
				
				existing.add(0, infoToAdd);
				
				//This is already managed on the server, no need to re-save.
				if (existing.size() > CACHE_SIZE)
					existing.remove(CACHE_SIZE);
			}
			public void onFailure(Throwable caught) {
				Debug.println("Failed to save a new current reference...");
			}
		});
	} 
	
	private RecentInfo parse(String type, RowData row) {
		RecentInfo info;
		if (RecentlyAccessed.ASSESSMENT.equals(type)) {
			info = new RecentAssessment(Integer.valueOf(row.getField("id")), 
					row.getField("status"), row.getField("species"), 
					row.getField("region"));  
		}
		else
			return null;
			
		info.setAccessID(Integer.valueOf(row.getField("accessid")));
		info.setAccessType(type);
		
		return info;
	}
	
	public static abstract class RecentInfo {
		
		private Integer accessID;
		private String accessType;
		
		public void setAccessID(Integer accessID) {
			this.accessID = accessID;
		}
		
		public Integer getAccessID() {
			return accessID;
		}
		
		public String getAccessType() {
			return accessType;
		}
		
		public void setAccessType(String accessType) {
			this.accessType = accessType;
		}
		
		public abstract Integer getObjectID();
	}
	
	public static class RecentAssessment extends RecentInfo {
		
		public String type, name, region;
		public Integer id;
		
		public RecentAssessment(Assessment assessment) {
			this.id = assessment.getId();
			this.type = assessment.getType();
			this.name = assessment.getSpeciesName();
			
			String region;
			if (assessment.isRegional()) {
				List<Integer> regions = assessment.getRegionIDs();
				if (regions.isEmpty())
					region = "(Unspecified Region)";
				else {
					Region r = RegionCache.impl.getRegionByID(regions.get(0));
					if (r == null)
						region = "(Invalid Region ID)";
					else if (regions.size() == 1)
						region = r.getName();
					else
						region = r.getName() + " + " + (regions.size() - 1) + " more...";
				}
				if (assessment.isEndemic())
					region += " -- Endemic";
			}
			else
				region = "Global";
			
			this.region = region;
		}

		public RecentAssessment(Integer id, String type, String name, String region) {
			this.id = id;
			this.type = type;
			this.name = name;
			this.region = region;
		}
		
		@Override
		public Integer getObjectID() {
			return id;
		}
		
	}
	

}
