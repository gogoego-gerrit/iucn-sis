package org.iucn.sis.shared.api.data;

import java.util.ArrayList;

import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class WorkingSetParser {

	private ArrayList<WorkingSet> workingSets;
	private ArrayList<String> allSpeciesIDs;

	public WorkingSetParser() {
		workingSets = new ArrayList<WorkingSet>();
		allSpeciesIDs = new ArrayList<String>();
	}

	public ArrayList<String> getAllSpeciesIDs() {
		return allSpeciesIDs;
	}

	public ArrayList<WorkingSet> getWorkingSets() {
		return workingSets;
	}

	public WorkingSet parseSingleWorkingSet(NativeElement workingSetNativeDoc) {
		return WorkingSet.fromXML(workingSetNativeDoc);
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
			WorkingSet cur = WorkingSet.fromXML(workingSet);
			if (cur != null)
				workingSets.add(cur);
		}
	}

	/**
	 * 
	 * @param ndoc
	 * @param backInTheDay
	 */
	public void parseWorkingSetXML(NativeDocument ndoc) {
		NativeNodeList publicWorkingSetList = ndoc.getDocumentElement().getElementsByTagName("workingSet");

		workingSets.clear();

		if (publicWorkingSetList != null)
			parseWorkingSetList(publicWorkingSetList);
	}
}
