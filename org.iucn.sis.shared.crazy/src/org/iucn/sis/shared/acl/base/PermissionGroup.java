package org.iucn.sis.shared.acl.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.shared.acl.feature.AuthorizableAssessmentShim;
import org.iucn.sis.shared.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.data.WorkingSetCache;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentFilter;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

public class PermissionGroup {

	/**
	 * A HashMap that maintains a mapping of URIs to an OperationSet
	 */
	private HashMap<String, PermissionResource> resources;
	
	/**
	 * A HashMap that maintains a mapping of URIs to an OperationSet as originally specified.
	 * The resources map will contain mappings that also include inherited resources. 
	 */
	private HashMap<String, PermissionResource> preprocessedResources;
	
	private String scopeURI;
	private String originalScopeURI;
	
	private List<PermissionGroup> inherits;
	private boolean processedInherits = false;
	private String name;
	
	public static final String DEFAULT_PERMISSION_URI = "default";

	public PermissionGroup(String name) {
		this.inherits = new ArrayList<PermissionGroup>();
		this.resources = new HashMap<String, PermissionResource>();
		this.preprocessedResources = new HashMap<String, PermissionResource>();
		this.name = name;
		this.scopeURI = null;
		this.originalScopeURI = null;
	}
	
	/**
	 * Adds the URI-to-PermissionSet mappings from the inheritFrom to this PermissionGroup.
	 * <br>NOTE: This will OVERWRITE all existing URI mappings that exist in this group if the
	 * inheritsFrom group contains a mapping.
	 * 
	 * @param inheritsFrom
	 */
	public void addInheritence(PermissionGroup inheritsFrom) {
		inherits.add(inheritsFrom);
	}
	
	public void clearInheritences() {
		inherits.clear();
	}

	@Override
	public boolean equals(Object obj) {
		if( obj instanceof PermissionGroup )
			return getName().equals(((PermissionGroup)obj).getName());
		else
			return super.equals(obj);
	}
	
	public void setScopeURI(String scopeURI) {
		this.scopeURI = scopeURI;
	}
	
	public String getScopeURI() {
		return scopeURI;
	}
	
	public HashMap<String, PermissionResource> getResources() {
		return resources;
	}
	
	protected void processInheritences() {
		preprocessedResources = new HashMap<String, PermissionResource>(resources);
		originalScopeURI = scopeURI;
		
		String scope = null;
		for( PermissionGroup curGroup : inherits ) {
			curGroup.processInheritences();
			for( Entry<String, PermissionResource> curEntry : curGroup.getSink().entrySet() ) {
				addPermissionResource(curEntry.getValue().deepCopy());
				if( curGroup.getScopeURI() != null )
					scope = curGroup.getScopeURI();
			}
		}
		
		if( getScopeURI() == null )
			setScopeURI(scope);
		
		processedInherits = true;
	}
	
	public List<PermissionGroup> getInherits() {
		return inherits;
	}
	
	public boolean check(AuthorizableObject object, String operation) {
		boolean ret = false;
		
		if( !processedInherits )
			processInheritences();
		
		if( resources.containsKey(DEFAULT_PERMISSION_URI) )
			ret = resources.get(DEFAULT_PERMISSION_URI).check(operation);
		
		if( isInTaxonomicScope(object) ) { //If it's in scope, check the resource URIs
			String uri = "";

			if( object != null ) {
				uri = object.getFullURI();
				boolean found = false;

				while( uri.indexOf("/") > -1 && !found ) {
					if (resources.containsKey(uri) && resources.get(uri) != null ) {
						PermissionResource resource = resources.get(uri);
						ret = resource.check(operation);
						
						if( ret ) //Check attributes if it's a match
							for( Entry<String, String> cur : resource.getAttributes().entrySet() ) {
								if( cur != null && cur.getKey() != null && cur.getValue() != null )
									ret = object.getProperty(cur.getKey()).matches(cur.getValue());
							}
						
						found = true;
					} else
						uri = uri.substring(0, uri.lastIndexOf("/"));
				}
			}
		}
		
		return ret;
	}

	public boolean isInTaxonomicScope(AuthorizableObject object) {
		if( scopeURI == null || scopeURI.equals("") )
			return true;
		else if( object instanceof AuthorizableFeature || object instanceof WorkingSetData || object instanceof ReferenceUI ) { 
			//Features, working sets and references aren't scoped
			return true;
		} else if( object instanceof TaxonNode ) {
			TaxonNode taxon = (TaxonNode)object;
			
			if( scopeURI.startsWith("taxon") ) {
				String [] split = scopeURI.split("/");
				int level = Integer.valueOf(split[1]);
				boolean kingdomMatch = true;
				
				if( split.length > 3 && taxon.getFootprint().length > 0 )
					kingdomMatch = split[3].equalsIgnoreCase(taxon.getFootprint()[0]);

				if( taxon.getFootprint().length > level )
					return kingdomMatch && taxon.getFootprint()[level].equalsIgnoreCase(split[2]);
				else if( taxon.getLevel() == level )
					return kingdomMatch && taxon.getFullName().equalsIgnoreCase(split[2]);
				else
					return false;
			} else if( scopeURI.startsWith("workingSets") ) {
				HashMap<String, WorkingSetData> sets = WorkingSetCache.impl.getWorkingSets();
				for (Entry<String, WorkingSetData> curEntry : sets.entrySet()) {
					if (curEntry.getValue().getSpeciesIDs().contains(taxon.getId() + ""))
						return true;
				}
				return false;
			} else if( scopeURI.startsWith("workingSet") ) {
				String ids = scopeURI.substring(scopeURI.indexOf("/")+1, scopeURI.length());
				HashMap<String, WorkingSetData> sets = WorkingSetCache.impl.getWorkingSets();
				
				String [] split = ids.split(",");
				boolean ret = false;
				
				for( String cur : split ) {
					if( sets.containsKey(cur) ) {
						ret = sets.get(cur).getSpeciesIDs().contains(taxon.getId() + "");
					
						if( ret )
							return true;
					}
				}
				
				return false;
			} else
				return false;
		} else if( object instanceof AssessmentData ) {
			if( scopeURI.startsWith("workingSet/") ) {
				AssessmentData assessment = (AssessmentData)object;
				String ids = scopeURI.substring(scopeURI.indexOf("/")+1, scopeURI.length());
				HashMap<String, WorkingSetData> sets = WorkingSetCache.impl.getWorkingSets();
				
				String [] split = ids.contains(",") ? ids.split(",") : new String[] { ids };
				
				for( String cur : split ) {
					if( sets.containsKey(cur) ) {
						if( sets.get(cur).getSpeciesIDs().contains(assessment.getSpeciesID()) ) {
							List<String> filterRegions = sets.get(cur).getFilter().getRegions();
							if( sets.get(cur).getFilter().isAllRegions() ) {
								return true;
							} else if( sets.get(cur).getFilter().getRegionType().equals(AssessmentFilter.REGION_TYPE_AND) ) {
								if( filterRegions.containsAll(assessment.getRegionIDs()) &&
										assessment.getRegionIDs().containsAll(filterRegions)) {
									return true;
								}
							} else if( sets.get(cur).getFilter().getRegionType().equals(AssessmentFilter.REGION_TYPE_OR) ) {
								for( String curRegion : assessment.getRegionIDs() ) {
									if( filterRegions.contains(curRegion) ) {
										return true;
									}
								}
							}
						}
					}
				}
				
				return false;
			} else {
				TaxonNode node = TaxonomyCache.impl.getNode(((AssessmentData)object).getSpeciesID());
				return isInTaxonomicScope(node);
			}
		} else if( object instanceof AuthorizableAssessmentShim ) {
			AuthorizableAssessmentShim shim = (AuthorizableAssessmentShim)object; 
			return isInTaxonomicScope(shim.getTaxon());
		} else
			return false; //Invalid scope URI
	}
	
	public HashMap<String, PermissionResource> getPreprocessedResources() {
		if( !processedInherits )
			return resources;
		else
			return preprocessedResources;
	}
	
	public String getOriginalScopeURI() {
		if( !processedInherits )
			return scopeURI;
		else
			return originalScopeURI;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public PermissionSet getSetFromProcessedResources(String uri) {
		if( resources.containsKey(uri ))
			return resources.get(uri).getSet();
		else
			return null;
	}
	
	protected HashMap<String, PermissionResource> getSink() {
		return resources;
	}
	
	public void addPermissionResource(PermissionResource resource) {
		resources.put(resource.getURI(), resource);	
	}
	
	public void addPermissionResource(String uri, PermissionSet set) {
		resources.put(uri, new PermissionResource(uri, set));	
	}
	
	public PermissionResource removeResource(String uri) {
		return resources.remove(uri);
	}
	
	public String toXML() {
		StringBuilder ret = new StringBuilder("");
		ret.append("<" + getName() + ">\r\n");
		if( getPreprocessedResources().containsKey(DEFAULT_PERMISSION_URI) ) {
			String def = getPreprocessedResources().get(DEFAULT_PERMISSION_URI).getSet().toString();
			if( !def.equals("") ) {
				ret.append("<default>");
				ret.append(def);
				ret.append("</default>\r\n");
			}
		}
		
		for( PermissionGroup curInherit : inherits ) {
			ret.append("<inherits>");
			ret.append(curInherit.getName());
			ret.append("</inherits>");
		}
		
		if( getOriginalScopeURI() != null && !getOriginalScopeURI().equals("") ) {
			ret.append("<scope uri=\"");
			ret.append(getOriginalScopeURI());
			ret.append("\" />");
		}
		
		for( Entry<String, PermissionResource> curSet : getPreprocessedResources().entrySet() ) {
			if( !curSet.getKey().equals(DEFAULT_PERMISSION_URI) ) {
				ret.append(curSet.getValue().toXML());
				ret.append("\r\n");
			}
		}
		
		ret.append("</" + getName() + ">\r\n");
		
		return ret.toString();
	}
}
