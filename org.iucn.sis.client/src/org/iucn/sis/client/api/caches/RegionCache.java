package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.Region;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class RegionCache {

	public static RegionCache impl = new RegionCache();
	private HashMap<Integer, Region> idToRegion;

	private HashMap<String, Region> nameToRegion;

	private RegionCache() {
		idToRegion = new HashMap<Integer, Region>();
		nameToRegion = new HashMap<String, Region>();
		
		
		add(Region.getGlobalRegion());
	}

	public void add(Region region) {
		idToRegion.put(region.getId(), region);
		nameToRegion.put(region.getName(), region);
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

		doc.get(UriBase.getInstance().getSISBase() +"/regions", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				wayback.onFailure(caught);
			}

			public void onSuccess(String result) {
				NativeNodeList nodeList = doc.getDocumentElement().getElementsByTagName("region");
				for (int i = 0; i < nodeList.getLength(); i++) {
					final NativeElement el = nodeList.elementAt(i);
					add(Region.fromXML(el));
				}				
				add(Region.getGlobalRegion());
				wayback.onSuccess("OK");
			}
		});
	}

	public Region getRegionByID(Integer id) {
		return idToRegion.get(id);
	}

	public List<Region> getRegionsByID(List<Integer> ids) {
		List<Region> regions = new ArrayList<Region>();
		for( Integer id : ids )
			regions.add(idToRegion.get(id));
		
		return regions;
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
	public String getRegionName(List<Integer> regionID) {
		if (regionID.size() > 1) {
			Region reg = getRegionByID(regionID.get(0));
			if (reg != null)
				return reg.getName() + "...";
			else
				return "(Invalid Region ID)";
		} else if (regionID.size() == 1) {
			Region reg = getRegionByID(regionID.get(0));
			if (reg != null)
				return reg.getName();
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
	public String getRegionName(Integer regionID) {
		Region reg = getRegionByID(regionID);
		if (reg != null)
			return reg.getName();
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
		List<Region> regions = new ArrayList<Region>(filter.getRegions());
		StringBuilder csv = new StringBuilder();
		for (int i = 0; i < regions.size() - 1; i++) {
			Region region = regions.get(i);
			csv.append(region.getName() + ", ");
		}
			
		if (csv.length() > 0)
			return csv.toString() + " and " + regions.get(regions.size()-1).getName();
		else if (regions.size() > 0)
			return regions.get(0).getName();
		else
			return "";
	}
	
	public Collection<Region> getRegions() {
		return idToRegion.values();
	}

	public void remove(Region region) {
		idToRegion.remove(region.getId());
		nameToRegion.remove(region.getName());
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

		doc.post(UriBase.getInstance().getSISBase() +"/regions", buf.toString(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				wayback.onFailure(caught);
			}

			public void onSuccess(String result) {
				NativeNodeList nodeList = doc.getDocumentElement().getElementsByTagName("region");
				for (int i = 0; i < nodeList.getLength(); i++) {
					final NativeElement el = nodeList.elementAt(i);
					add(Region.fromXML(el));
				}
				wayback.onSuccess("OK");
			}
		});
	}

	public void update(Region oldRegionInfo, Region newRegionInfo) {
		idToRegion.remove(oldRegionInfo.getId());
		nameToRegion.remove(oldRegionInfo.getName());
		add(newRegionInfo);
	}
}
