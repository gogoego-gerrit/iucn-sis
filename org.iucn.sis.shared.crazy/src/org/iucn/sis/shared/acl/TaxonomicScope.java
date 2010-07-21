package org.iucn.sis.shared.acl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.iucn.sis.shared.data.assessments.AssessmentData;

public class TaxonomicScope extends Scope {

	private HashMap<String, ArrayList<String>> permissions; // String,

	// ArrayList<String>

	public TaxonomicScope() {
		permissions = new HashMap<String, ArrayList<String>>();
	}

	public TaxonomicScope(String key, String value) {
		this.permissions = new HashMap<String, ArrayList<String>>();
		ArrayList<String> list = new ArrayList<String>();
		list.add(value);
		permissions.put(key, list);
	}

	public void addPermission(String key, String value) {
		if (permissions.containsKey(key)) {
			permissions.get(key).add(value);
		} else {
			ArrayList list = new ArrayList();
			list.add(value);
			permissions.put(key, list);
		}

	}

	private boolean isEmptyString(String key, HashMap map) {
		if (map.containsKey(key))
			return (map.get(key) == null || ((String) map.get(key)).equalsIgnoreCase(""));
		else
			return true;

	}

	@Override
	public boolean matches(Object requirement) {
		try {
			AssessmentData assessment = (AssessmentData) requirement;
			// Iterator iterator = permissions.keySet().iterator();
			// while (iterator.hasNext()) {
			// String key = (String)iterator.next();
			// if (!assessment.getTaxonomy().containsKey(key)) {
			// return false;
			// }
			// ArrayList list = (ArrayList)permissions.get(key);
			// for (int i = 0; i < list.size(); i++) {
			// if
			// (assessment.getTaxonomy(key).equalsIgnoreCase((String)list.get(
			// i))) {
			// return true;
			// }
			// }
			// }
			return false;
		} catch (Exception e) {
			try {
				HashMap taxonomy = (HashMap) requirement;

				// Find the lowest level requirement
				ArrayList searchTerms = new ArrayList();

				// Brute force
				/*
				 * if (isEmptyString(XMLUtils.KINGDOM, taxonomy))
				 * searchTerms.add(XMLUtils.KINGDOM); else if
				 * (isEmptyString(XMLUtils.PHYLUM, taxonomy))
				 * searchTerms.add(XMLUtils.PHYLUM); else if
				 * (isEmptyString(XMLUtils.CLASS, taxonomy))
				 * searchTerms.add(XMLUtils.CLASS); else if
				 * (isEmptyString(XMLUtils.ORDER, taxonomy))
				 * searchTerms.add(XMLUtils.ORDER); else if
				 * (isEmptyString(XMLUtils.FAMILY, taxonomy))
				 * searchTerms.add(XMLUtils.FAMILY); else if
				 * (isEmptyString(XMLUtils.GENUS, taxonomy))
				 * searchTerms.add(XMLUtils.GENUS); else if
				 * (isEmptyString(XMLUtils.SPECIES, taxonomy))
				 * searchTerms.add(XMLUtils.SPECIES); else return true;
				 */

				// Wisely
				Iterator iterator = permissions.keySet().iterator();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					if (isEmptyString(key, taxonomy))
						return false; // user doesnt care to check this key
					else
						// check on this key
						searchTerms.add(key);
				}

				// Search each requirement requested
				for (int i = 0; i < searchTerms.size(); i++) {
					String key = (String) searchTerms.get(i);
					ArrayList list = permissions.get(key);
					boolean found = false;
					for (int j = 0; j < list.size(); j++) {
						if (((String) taxonomy.get(key)).equalsIgnoreCase(((String) list.get(j)))) {
							found = true;
							break;
						}
					}
					if (!found)
						return false;
				}

				// If it wasn't stopped already then it passes!
				return true;
			} catch (Exception f) {
				f.printStackTrace();
				return false;
			}
		}
	}

	public boolean matches(String key, String value) {
		return permissions.containsKey(key) && permissions.get(key).get(0).equalsIgnoreCase(value);
	}

	@Override
	public String toString() {
		return "Taxonomic Scope";
	}
}
