package org.iucn.sis.shared.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;



import com.solertium.lwxml.shared.GenericCallback;

/**
 * A node in the Taxonomy hierarchy. Every node contains references to
 * assessments, synonyms, an id, and a
 * 
 * @author adam.schwartz
 * 
 */
public abstract class TaxonNode implements Referenceable, AuthorizableObject {

	public static String getDisplayableLevel(int level) {
		return getDisplayableLevel(level, -1);
	}

	public static String getDisplayableLevel(int level, int infraType) {

		if (level == INFRARANK) {
			if (infraType == INFRARANK_TYPE_SUBSPECIES) {
				return "Subspecies";
			} else if (infraType == INFRARANK_TYPE_VARIETY) {
				return "Variety";
			}
		}
		return displayableLevel[level];
	}

	public static int getDisplayableLevelCount() {
		return displayableLevel.length;
	}

	/**
	 * Used by the server to uniquely identify a TaxonNode, even if there has
	 * been massive changes to its data.
	 */
	private long id = -1;

	/**
	 * Use to order displayable taxons by arbitraty ordering. Only used if sort
	 * by is not set to alpha numeric
	 */
	protected float sequenceCode = 0;

	/**
	 * ArrayList<String> assessmentIDs
	 */
	protected ArrayList<String> assessmentSet;

	/**
	 * Displayable name of the node. May or may not be unique.
	 */
	protected String name;

	/**
	 * Full hierarchical name of the node. Is unique.
	 */
	protected String fullName;

	/**
	 * ArrayList<SynonymData> SynonymData
	 */
	protected ArrayList<SynonymData> synonyms;

	/**
	 * ArrayList<CommonNameData>
	 */
	protected ArrayList<CommonNameData> commonNames;

	/**
	 * ArrayList<Reference>
	 */
	protected ArrayList<ReferenceUI> taxonomicSource;

	/**
	 * ArrayList<Notes>
	 */
	protected ArrayList<Note> taxonNotes;
	protected String status;
	protected int level;
	protected int infraType;
	protected TaxonNode parent;

	protected String parentName;
	protected String parentId;
	protected String taxonomicAuthority;
	protected String recordAdded;

	protected String addedBy;

	protected String lastUpdatedBy;

	protected String lastUpdated;

	/**
	 * String [] containing the names of this node's "parents"
	 */
	protected String[] footprint;
	protected boolean hybrid;
	public static final String STATUS_NEW = "N";
	public static final String STATUS_ACCEPTED = "A";

	public static final String STATUS_DISCARDED = "D";
	public static final String STATUS_SYNONYM = "S";
	public static final Map<String, String> displayableStatus = new LinkedHashMap<String, String>() {
		{
			put(STATUS_NEW, "New");
			put(STATUS_ACCEPTED, "Accepted");
			put(STATUS_DISCARDED, "Discard");
			put(STATUS_SYNONYM, "Synonym");
		}
	};
	public static final int KINGDOM = 0;
	public static final int PHYLUM = 1;
	public static final int CLASS = 2;
	public static final int ORDER = 3;
	public static final int FAMILY = 4;
	public static final int GENUS = 5;
	public static final int SPECIES = 6;

	public static final int INFRARANK = 7;
	public static final int SUBPOPULATION = 8;
	public static final int INFRARANK_SUBPOPULATION = 9;

	public static final int INFRARANK_TYPE_NA = -1;
	public static final int INFRARANK_TYPE_SUBSPECIES = 0;
	public static final int INFRARANK_TYPE_VARIETY = 1;

	private static String[] displayableLevel = new String[] { "Kingdom", "Phylum", "Class", "Order", "Family", "Genus",
		"Species", "Infrarank", "Subpopulation", "Subpopulation" };

	public TaxonNode(long id, String name, int level, String parentId, String parentName, String addedBy, String created) {
		assessmentSet = new ArrayList<String>();
		synonyms = new ArrayList<SynonymData>();
		commonNames = new ArrayList<CommonNameData>();
		taxonNotes = new ArrayList<Note>();
		taxonomicSource = new ArrayList<ReferenceUI>();

		this.id = id;
		this.level = level;
		this.infraType = INFRARANK_TYPE_NA;
		this.parentName = parentName;
		this.parentId = parentId;
		setParentId(parentId);
		this.name = name;

		this.addedBy = addedBy;
		this.recordAdded = created;

		footprint = new String[0];
		status = "";
	}

	public String getFullURI() {
		return "resource/taxon/" + getLevel() + "/" + getName();
	}

	public String getProperty(String key) {
		return "";
	}

	public void addAssessment(String assessment) {
		if (!assessmentSet.contains(assessment))
			assessmentSet.add(assessment);
	}

	public void addAssessments(ArrayList<String> assessments) {
		for (Iterator<String> iter = assessments.iterator(); iter.hasNext();)
			addAssessment(iter.next());
	}

	public void addCommonName(CommonNameData commonName) {
		this.commonNames.add(commonName);
		sortCommonNames();
	}

	public void addCommonNameAsPrimary(CommonNameData commonName) {
		this.commonNames.add(0, commonName);
	}

	public void addReference(ReferenceUI ref) {
		taxonomicSource.add(ref);
	}

	public void addReferences(ArrayList<ReferenceUI> references, GenericCallback<Object> callback) {
		int added = 0;
		for (int i = 0; i < references.size(); i++) {
			ReferenceUI current = references.get(i);
			if (!getSources().contains(current)) {
				addReference(current);
				added++;
			}
		}

		if (added > 0)
			onReferenceChanged(callback);
	}

	public void addSynonym(SynonymData synonym) {
		this.synonyms.add(synonym);
	}

	public void attachNote(Note note) {
		taxonNotes.add(note);
	}

	public void correctFullName() {
		String fullName = generateFullName();
		this.fullName = fullName;
	}

	public String generateFullName() {
		String fullName = "";
		for (int i = 5; i < (level >= TaxonNode.SUBPOPULATION ? level - 1 : level); i++)
			fullName += getFootprint()[i] + " ";

		if (level <= TaxonNode.SPECIES) {
			infraType = INFRARANK_TYPE_NA;
		} else {
			if (infraType == INFRARANK_TYPE_SUBSPECIES)
				fullName += "ssp. ";
			if (infraType == INFRARANK_TYPE_VARIETY)
				fullName += "var. ";
		}

		name = name.replace("ssp.", "").trim();
		name = name.replace("var.", "").trim();

		fullName += name;
		return fullName.trim();
	}

	public ArrayList<String> getAssessments() {
		return assessmentSet;
	}

	public String getAssessmentsCSV() {
		String csv = "";
		for (int i = 0; i < assessmentSet.size(); i++)
			csv += assessmentSet.get(i) + ",";

		return csv.length() > 0 ? csv.substring(0, csv.length() - 1) : csv;
	}

	public ArrayList<CommonNameData> getCommonNames() {
		sortCommonNames();
		return commonNames;
	}

	public String getDisplayableLevel() {
		return getDisplayableLevel(level, infraType);
	}

	public String[] getFootprint() {
		return footprint;
	}

	public String getFootprintAsString() {
		return getFootprintAsString(0);
	}

	public String getFootprintAsString(int startIndex) {
		return getFootprintAsString(startIndex, " ");
	}
	
	public String getFootprintAsString(int startIndex, String separator) {
		String ret = "";

		if (footprint.length <= startIndex)
			return ret;

		for (int i = startIndex; i < footprint.length - 1; i++)
			ret += footprint[i] + separator;
		ret += footprint[footprint.length - 1];

		return ret;
	}

	public String getFootprintCSV() {
		String csv = "";
		for (int i = 0; i < footprint.length; i++) {
			csv += footprint[i] + ",";
		}
		if (csv.length() > 0) {
			csv = csv.substring(0, csv.length() - 1);
		}
		return csv;
	}

	public String getFullName() {
		return fullName;
	}

	public long getId() {
		return id;
	}

	public int getInfrarankType() {
		return infraType;
	}

	public String getKingdomName() {
		return getFootprint().length > 0 ? getFootprint()[0] : getName();
	}
	
	public int getLevel() {
		return level;
	}

	public String getLevelString() {
		return getDisplayableLevel(getLevel(), getInfrarankType());
	}

	public String getName() {
		return name;
	}

	/*
	 * public void addAssessments( ArrayList assessments ) { for( Iterator iter
	 * = assessments.iterator(); iter.hasNext(); ) addAssessment(
	 * (BaseAssessment)iter.next() ); }
	 */

	public ArrayList<Note> getNotes() {
		return taxonNotes;
	}

	public TaxonNode getParent() {
		return parent;
	}

	public String getParentId() {
		// return String.valueOf(parent.getId());
		return parentId;
	}

	public String getParentName() {
		return parentName;
	}

	public ArrayList<ReferenceUI> getReferencesAsList() {
		return getSources();
	}

	public float getSequenceCode() {
		return sequenceCode;
	}

	public ArrayList<ReferenceUI> getSources() {
		return taxonomicSource;
	}

	public String getStatus() {
		return status;
	}

	public ArrayList<SynonymData> getSynonyms() {
		return synonyms;
	}

	public String getTaxonomicAuthority() {
		return taxonomicAuthority;
	}

	public boolean isDeprecated() {
		return !status.matches("(^[NnAaUu]$)|(^New$)");
	}

	@Deprecated
	/**
	 * Invokes isDeprecated() - use that method instead. This will be removed in a future
	 * release.
	 */
	public boolean isDeprecatedStatus() {
		return isDeprecated();
	}

	public boolean isHybrid() {
		return hybrid;
	}

	/**
	 * To be used by a search function. Meanders through the node's common
	 * names, synonyms and its ID to see if the key matches any of those
	 * strings.
	 * 
	 * @param key
	 * @return
	 */
	public boolean matches(String key) {
		if (key.equalsIgnoreCase(name))
			return true;

		/*
		 * for( Iterator iter = commonNames.listIterator(); iter.hasNext(); )
		 * if( key.equalsIgnoreCase( (String)iter.next() ) ) return true;
		 * 
		 * for( Iterator iter = synonyms.listIterator(); iter.hasNext(); ) if(
		 * key.equalsIgnoreCase( (String)iter.next() ) ) return true;
		 */
		return false;
	}

	public void onReferenceChanged(GenericCallback<Object> callback) {
//		TaxomaticUtils.impl.writeNodeToFS(this, callback);
	}

	public void removeAssessment(String assessment) {
		if (assessmentSet.contains(assessment))
			assessmentSet.remove(assessment);

	}

	public void removeNote(Note note) {
		taxonNotes.remove(note);
	}

	public boolean removeReference(ReferenceUI ref) {
		return taxonomicSource.remove(ref);
	}

	public void removeReferences(ArrayList<ReferenceUI> references, GenericCallback<Object> callback) {
		int removed = 0;
		for (int i = 0; i < references.size(); i++)
			if (removeReference(references.get(i)))
				removed++;

		if (removed > 0)
			onReferenceChanged(callback);
	}
	
	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}
	
	public String getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * Sets the child's parent information to point to this taxon.
	 * 
	 * @param child
	 */
	public void setAsParent(TaxonNode child) {
		child.setParent(this);
		child.setParentId(String.valueOf(id));
	}

	public void setCommonNameAsPrimary(CommonNameData commonName) {
		for (int i = 0; i < commonNames.size(); i++) {
			(commonNames.get(i)).setPrimary(false);
		}
		commonName.setPrimary(true);
		sortCommonNames();
	}

	public void setCommonNames(ArrayList<CommonNameData> commonNames) {
		if (commonNames != null)
			this.commonNames = commonNames;
	}

	public void setFootprint(String[] footprint) {
		this.footprint = footprint;
	}

	public void setFootprintAtLevel(int level, String speciesName) {
		footprint[level] = speciesName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setHybrid(boolean isHybrid) {
		hybrid = isHybrid;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setInfraType(int infraType) {
		this.infraType = infraType;
	}

	public void setLastUpdated(String date) {
		lastUpdated = date;
	}

	public void setLevel(int newLevel) {
		level = newLevel;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(TaxonNode parent) {
		this.parent = parent;
	}

	public void setParentId(final String parentId) {

		if (parentId == null || parentId.equals(""))
			return;

		this.parentId = parentId;
	}

	public void setParentName(String parentFullName) {
		parentName = parentFullName;
	}

	public void setSequenceCode(float sequence) {
		sequenceCode = sequence;
	}

	public void setSources(ArrayList<ReferenceUI> sources) {
		taxonomicSource = sources;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setSynonyms(ArrayList<SynonymData> synonyms) {
		this.synonyms = synonyms;
	}

	public void setTaxonomicAuthority(String taxonomicAuthority) {
		this.taxonomicAuthority = taxonomicAuthority;
	}

	public void setUpdatedBy(String user) {
		lastUpdatedBy = user;
	}
	
	

	private void sortCommonNames() {
		ArrayList<CommonNameData> newList = new ArrayList<CommonNameData>();
		// commonNames.clear();

		for (int i = 0; i < commonNames.size(); i++) {
			if ((commonNames.get(i)).isPrimary()) {
				newList.add(0, (commonNames.get(i)));
				// addCommonNameAsPrimary(((CommonNameData)commonNames.get(i)));
			} else {
				newList.add((commonNames.get(i)));
				// addCommonName(((CommonNameData)commonNames.get(i)));
			}
		}

		commonNames = newList;

	}

}
