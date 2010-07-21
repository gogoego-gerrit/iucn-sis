package org.iucn.sis.shared.data.assessments;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.shared.BaseAssessment;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class RegionCache {

	public static class RegionModel extends BaseModel {
		private static final long serialVersionUID = 4770673249783507045L;

		Region region;

		public RegionModel(NativeElement el) {
			String id = el.getAttribute("id");
			String name = el.getElementByTagName("name").getTextContent();
			String description = el.getElementByTagName("description").getTextContent();
			region = new Region(id, name, description);
			set("name", region.getRegionName());
			set("description", region.getDescription());
			set("id", region.getId());
		}

		public RegionModel(Region r) {
			region = r;
			set("name", region.getRegionName());
			set("description", region.getDescription());
			set("id", region.getId());
		}

		public Region getRegion() {
			return region;
		}

		public void sinkModelDataIntoRegion() {
			region.setRegionName((String) get("name"));
			region.setId((String) get("id"));
			region.setDescription((String) get("description"));
		}
	}

	public static RegionCache impl = new RegionCache();
	private HashMap<String, Region> idToRegion;

	private HashMap<String, Region> nameToRegion;

	private RegionCache() {
		idToRegion = new HashMap<String, Region>();
		nameToRegion = new HashMap<String, Region>();
		
		Region global = new Region(BaseAssessment.GLOBAL_ID, "Global", "Global");
		add(global);
	}

	public void add(Region region) {
		idToRegion.put(region.getId(), region);
		nameToRegion.put(region.getRegionName(), region);
	}

	public void fetchRegions(NativeDocument doc) {
		fetchRegions(doc, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(String result) {
			}
		});
	}

	public void fetchRegions(final NativeDocument doc, final GenericCallback<String> wayback) {
		idToRegion.clear();
		nameToRegion.clear();

		doc.get("/regions", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				wayback.onFailure(caught);
			}

			public void onSuccess(String result) {
				NativeNodeList nodeList = doc.getDocumentElement().getElementsByTagName("region");
				for (int i = 0; i < nodeList.getLength(); i++) {
					final NativeElement el = nodeList.elementAt(i);

					final String id = el.getAttribute("id");
					final String name = el.getElementByTagName("name").getTextContent();
					final String description = el.getElementByTagName("description").getTextContent();
					add(new Region(id, name, description));
				}
				
				Region global = new Region(BaseAssessment.GLOBAL_ID, "Global", "Global");
				add(global);
				wayback.onSuccess("OK");
			}
		});
	}

	public Region getRegionByID(String id) {
		return idToRegion.get(id);
	}

	public Region getRegionByName(String name) {
		return nameToRegion.get(name);
	}

	/**
	 * A safe way to access a region name. If the Region cannot be found based
	 * on the regionID parameter, a default failure String will be returned,
	 * something along the lines of "(Invalid Region ID)". If the regionID
	 * parameter is a list of regions, the return value will include the first,
	 * trailed by a "..." to indicate there are more.
	 * 
	 * @param regionID
	 * @return String - either region name, or an Invalid string
	 */
	public String getRegionName(List<String> regionID) {
		if (regionID.size() > 1) {
			Region reg = getRegionByID(regionID.get(0));
			if (reg != null)
				return reg.getRegionName() + "...";
			else
				return "(Invalid Region ID)";
		} else if (regionID.size() == 1) {
			Region reg = getRegionByID(regionID.get(0));
			if (reg != null)
				return reg.getRegionName();
			else
				return "(Invalid Region ID)";
		} else
			return "(Invalid Region ID)";
	}

	/**
	 * A safe way to access a region name. If the Region cannot be found based
	 * on the regionID parameter, a default failure String will be returned,
	 * something along the lines of "(Invalid Region ID)".
	 * 
	 * @param regionID
	 * @return String - either region name, or an Invalid string
	 */
	public String getRegionName(String regionID) {
		Region reg = getRegionByID(regionID);
		if (reg != null)
			return reg.getRegionName();
		else
			return "(Invalid Region ID)";
	}
	
	/**
	 * Gets regions from the filter and builds a readable string out of it.
	 * 
	 * @param filter an assessmentFilter
	 * @return readable string of filter's region names
	 */
	public String getRegionNamesAsReadable(AssessmentFilter filter) {
		List<String> regions = filter.getRegions();
		StringBuilder csv = new StringBuilder();
		for (int i = 0; i < regions.size() - 1; i++) {
			String region = regions.get(i);
			csv.append(RegionCache.impl.getRegionName(region) + ", ");
		}
			
		if (csv.length() > 0)
			return csv.toString() + " and " + RegionCache.impl.getRegionName(regions.get(regions.size()-1));
		else if (regions.size() > 0)
			return RegionCache.impl.getRegionName(regions.get(0));
		else
			return "";
	}
	
	public Collection<Region> getRegions() {
		return idToRegion.values();
	}

	public void remove(Region region) {
		idToRegion.remove(region.getId());
		nameToRegion.remove(region.getRegionName());
	}

	public void saveRegions(final NativeDocument doc, final List<Region> toBeSaved,
			final GenericCallback<String> wayback) {
		StringBuffer buf = new StringBuffer("<regions>");
		for (Region curRegion : toBeSaved) {
			buf.append(curRegion.toXML());
			buf.append("\r\n");
		}
		buf.append("</regions>");
		
		idToRegion.clear();
		nameToRegion.clear();

		doc.post("/regions", buf.toString(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				wayback.onFailure(caught);
			}

			public void onSuccess(String result) {
				NativeNodeList nodeList = doc.getDocumentElement().getElementsByTagName("region");
				for (int i = 0; i < nodeList.getLength(); i++) {
					final NativeElement el = nodeList.elementAt(i);

					final String id = el.getAttribute("id");
					final String name = el.getElementByTagName("name").getTextContent();
					final String description = el.getElementByTagName("description").getTextContent();
					add(new Region(id, name, description));
				}
				wayback.onSuccess("OK");
			}
		});
	}

	public void update(Region oldRegionInfo, Region newRegionInfo) {
		idToRegion.remove(oldRegionInfo.getId());
		nameToRegion.remove(oldRegionInfo.getRegionName());
		add(newRegionInfo);
	}
}
