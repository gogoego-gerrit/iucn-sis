package org.iucn.sis.shared.data;

import java.util.ArrayList;

import org.iucn.sis.shared.data.assessments.AssessmentFilter;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class WorkingSetParser {

	private ArrayList<WorkingSetData> workingSets;
	private ArrayList<String> allSpeciesIDs;

	public WorkingSetParser() {
		workingSets = new ArrayList<WorkingSetData>();
		allSpeciesIDs = new ArrayList<String>();
	}

	public ArrayList<String> getAllSpeciesIDs() {
		return allSpeciesIDs;
	}

	public String getAllSpeciesIDsAsCSV() {
		StringBuilder temp = new StringBuilder();
		for (String cur : allSpeciesIDs) {
			temp.append(cur);
			temp.append(",");
		}
		return temp.substring(0, temp.length() - 1);
	}

	public ArrayList<WorkingSetData> getWorkingSets() {
		return workingSets;
	}

	public WorkingSetData parseSingleWorkingSet(NativeElement workingSetNativeDoc) {
		final String id = workingSetNativeDoc.getAttribute("id");
		
		String workflow_status = workingSetNativeDoc.getAttribute("workflow_status");
		if ("".equals(workflow_status) || workflow_status == null)
			workflow_status = "draft";
		String creator = workingSetNativeDoc.getAttribute("creator");
		NativeElement workingSetInfo = workingSetNativeDoc.getElementByTagName("info");

		//If server sends back working set tag with no data...
		if( workingSetInfo.getElementByTagName("name") != null ) {
			String name = workingSetInfo.getElementByTagName("name").getText();
			String description = workingSetInfo.getElementByTagName("description").getText();
			String date = workingSetInfo.getElementByTagName("date").getText();
			String mode = workingSetInfo.getElementByTagName("mode").getText();
			String notes = workingSetInfo.getElementByTagName("notes").getText();
			AssessmentFilter filter = new AssessmentFilter();
			NativeElement taxa = workingSetNativeDoc.getElementByTagName("taxa");
			NativeNodeList species = taxa.getElementsByTagName("species");
			NativeElement persons = workingSetNativeDoc.getElementByTagName("persons");
			NativeNodeList person = persons.getElementsByTagName("person");

			NativeNodeList filters = workingSetNativeDoc.getElementsByTagName(AssessmentFilter.HEAD_TAG);
			if (filters.getLength() > 0) {
				filter = AssessmentFilter.parseXML(filters.elementAt(0));
			}

			ArrayList<String> speciesIDs = new ArrayList<String>();
			if (species.getLength() > 0) {
				for (int j = 0; j < species.getLength(); j++) {
					String specid = species.elementAt(j).getText();
					speciesIDs.add(specid);

				}
			}

			ArrayList<String> personsNames = new ArrayList<String>();
			if (person.getLength() > 0) {
				for (int j = 0; j < person.getLength(); j++) {
					String personName = person.elementAt(j).getText();
					personsNames.add(personName);
				}
			}

			final WorkingSetData data = 
				new WorkingSetData(speciesIDs, name, description, date, creator, id, notes, mode, personsNames, filter);
			if (workflow_status != null)
				data.setWorkflowStatus(workflow_status);

			return data;
		} else
			return null;
	}

	/**
	 * Given a nativeNodeList of workingsets, it parses the working set, puts
	 * the working sets in the working set cache, and returns the taxa ids that
	 * needs to be fetched.
	 * 
	 * @param workingSets
	 * @return
	 */
	private void parseWorkingSetList(NativeNodeList workingSetList) {
		for (int i = 0; i < workingSetList.getLength(); i++) {
			NativeElement workingSet = workingSetList.elementAt(i);
			WorkingSetData cur = parseSingleWorkingSet(workingSet);
			if( cur != null )
				workingSets.add(cur);
		}
	}

	/**
	 * 
	 * @param ndoc
	 * @param backInTheDay
	 */
	public void parseWorkingSetXML(NativeDocument ndoc) {
		NativeNodeList privateWorkingSetList = ndoc.getDocumentElement().getElementsByTagName("private").elementAt(0)
				.getElementsByTagName("workingSet");

		NativeNodeList publicWorkingSetList = ndoc.getDocumentElement().getElementsByTagName("public").elementAt(0)
				.getElementsByTagName("workingSet");

		workingSets.clear();

		if (publicWorkingSetList != null)
			parseWorkingSetList(publicWorkingSetList);

		if (privateWorkingSetList != null)
			parseWorkingSetList(privateWorkingSetList);
	}
}
