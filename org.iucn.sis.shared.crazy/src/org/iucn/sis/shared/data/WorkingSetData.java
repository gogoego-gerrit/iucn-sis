package org.iucn.sis.shared.data;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.assessments.AssessmentFilter;
import org.iucn.sis.shared.workflow.WorkflowStatus;

/**
 * Class that represents a working set, contains an arraylist that holds
 * assessments and its own name;
 * 
 * @author liz.schwartz
 * 
 */
public class WorkingSetData implements AuthorizableObject {

	public static final String PUBLIC = "public";
	public static final String PRIVATE = "private";

	/**
	 * An arrayList that holds species string ids.
	 */
	private List<String> speciesIDs;
	private String workingSetName;
	private String description;
	private String date;
	private String creator;
	private String id;
	private String notes;
	private WorkflowStatus workflowStatus;
	private AssessmentFilter filter;
	/**
	 * An arraylist that holds the list of people associated with the working
	 * set (if there is any)
	 */
	private List<String> people;
	/**
	 * will either be public or private
	 */
	private String mode;

	private boolean sorted = false;

	public WorkingSetData() {
		speciesIDs = new ArrayList<String>();
		people = new ArrayList<String>();
		filter = new AssessmentFilter();
		workflowStatus = WorkflowStatus.DRAFT;
		workingSetName = "";
		description = "";
		date = "";
		creator = "";
		notes = "";
	}

	public WorkingSetData(List<String> taxaIDs, String workingSetName, String description, String date, String creator,
			String id, String notes, String mode, ArrayList<String> people, AssessmentFilter filter) {
		this.speciesIDs = taxaIDs;
		this.workingSetName = workingSetName;
		this.description = description;
		this.date = date;
		this.creator = creator;
		this.id = id;
		this.notes = notes;
		this.mode = mode;
		this.people = people;
		this.filter = filter;
	}

	public String getFullURI() {
		return "resource/workingSet/" + getId();
	}
	
	public String getProperty(String key) {
		if( "name".equalsIgnoreCase(key) ) {
			return getWorkingSetName();
		} else
			return "";
	}
	
	public void addSpeciesIDsAsCSV(String species) {
		String[] ids = species.split(",");
		for (int i = 0; i < ids.length; i++) {
			if (!speciesIDs.contains(ids[i])) {
				speciesIDs.add(ids[i]);
			}
		}
		sortSpeciesList();
	}

	public String getCreator() {
		return creator;
	}

	public String getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}
	
	public AssessmentFilter getFilter() {
		return filter;
	}

	public String getId() {
		return id;
	}

	public String getMode() {
		return mode;
	}

	public String getNotes() {
		return notes;
	}

	public List<String> getPeople() {
		return people;
	}

	public String getPeopleAsCSV() {
		StringBuffer csv = new StringBuffer();
		for (int i = 0; i < people.size(); i++) {
			csv.append(people.get(i) + ",");
		}
		if (csv.length() > 0) {
			return csv.substring(0, csv.length() - 1);
		} else
			return csv.toString();
	}

	public List<String> getSpeciesIDs() {
		return speciesIDs;
	}

	public String getSpeciesIDsAsString() {
		sortSpeciesList();

		StringBuffer id = new StringBuffer();
		for (int i = 0; i < speciesIDs.size(); i++)
			id.append(speciesIDs.get(i) + ",");
		if (id.indexOf(",") >= 0)
			return id.substring(0, id.length() - 1);
		else
			return "";
	}
	
	public WorkflowStatus getWorkflowStatus() {
		return workflowStatus;
	}

	public String getWorkingSetName() {
		return workingSetName;
	}
	
	public boolean hasWorkflowStatus() {
		return workflowStatus != null;
	}

	public boolean isPublic() {
		if (mode.equalsIgnoreCase(PUBLIC)) {
			return true;
		} else
			return false;
	}

	public boolean isSorted() {
		return sorted;
	}

	public void remove(String speciesID) {
		speciesIDs.remove(speciesID);
		sortSpeciesList();
	}
	
	public void removeSpeciesIDs(List<String> species) {
		for (String cur : species)
			speciesIDs.remove(cur);

		sortSpeciesList();
	}
	
	public void removeSpeciesIDsAsCSV(String species) {
		String[] ids = species.split(",");
		for (int i = 0; i < ids.length; i++)
			speciesIDs.remove(ids[i]);

		sortSpeciesList();
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setFilter(AssessmentFilter filter) {
		System.out.println("the filter is being set within the working set data obj with filter " + filter + " and filter " + filter.getRegionType());
		this.filter = filter;
	}

	public void setID(String id) {
		this.id = id;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setPeople(ArrayList people) {
		this.people = people;
	}

	public void setPeople(String peopleAsCSV) {
		String[] peeps = peopleAsCSV.split(",");
		people.clear();
		for (int i = 0; i < peeps.length; i++) {
			if (!people.contains(peeps[i]))
				people.add(peeps[i]);
		}

	}

	/**
	 * Flag to indicate if the species list needs to be resorted.
	 * 
	 * @param sorted
	 *            - true if needs resorting, false if not
	 */
	public void setSorted(boolean sorted) {
		this.sorted = sorted;
	}

	public void setSpeciesIDs(List<String> idsToSave) {
		this.speciesIDs = idsToSave;
	}

	public void setSpeciesIDsAsCSV(String species) {
		speciesIDs.clear();
		String[] ids = species.split(",");
		for (int i = 0; i < species.length(); i++) {
			speciesIDs.add(ids[i]);
		}

		sortSpeciesList();
	}
	
	/*
	 * @deprecated pass a WorkflowStatus object instead.
	 */
	public void setWorkflowStatus(String workflowStatus) {
		setWorkflowStatus(WorkflowStatus.getStatus(workflowStatus));
	}
	
	public void setWorkflowStatus(WorkflowStatus workflowStatus) {
		this.workflowStatus = workflowStatus;
	}

	public void setWorkingSetName(String workingSetName) {
		this.workingSetName = workingSetName;
	}

	public void sortSpeciesList() {
		if (sorted)
			return;

		Object[] ids = speciesIDs.toArray();

		try {
			setSpeciesIDs(TaxonomyUtils.sortTaxaIDsProperlyAsArrayList(ids));
			setSorted(true);
		} catch (Exception e) {
			setSorted(false);
		}
	}

	@Override
	public String toString() {
		return toXML();
	}

	public String toXML() {
		StringBuffer xml = new StringBuffer();
		xml.append("<workingSet id=\"" + getId() + "\" creator=\"" + getCreator() + "\">\r\n");
		xml.append("<info>\r\n");
		xml.append("<name>" + getWorkingSetName() + "</name>\r\n");
		xml.append("<date>" + getDate() + "</date>\r\n");
		xml.append("<mode>" + getMode() + "</mode>\r\n");
		xml.append("<description>" + getDescription() + "</description>\r\n");
		xml.append("<notes>" + getNotes() + "</notes>\r\n");
		xml.append("</info>\r\n");
		xml.append("<taxa>\r\n");
		for (int i = 0; i < getSpeciesIDs().size(); i++) {
			xml.append("<species>" + getSpeciesIDs().get(i) + "</species>\r\n");
		}
		xml.append("</taxa>\r\n");
		xml.append("<persons>\r\n");
		for (int i = 0; i < people.size(); i++) {
			xml.append("<person>" + people.get(i) + "</person>\r\n");
		}
		xml.append("</persons>\r\n");
		xml.append(filter.toXML()+"\r\n");
		xml.append("</workingSet>\r\n");
		return xml.toString();
	}
}
