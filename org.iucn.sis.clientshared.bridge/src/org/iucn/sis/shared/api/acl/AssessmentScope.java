package org.iucn.sis.shared.api.acl;

import java.util.ArrayList;

public class AssessmentScope extends Scope {

	private ArrayList<String> assessments;

	public AssessmentScope() {
		this(new ArrayList<String>());
	}

	public AssessmentScope(ArrayList<String> list) {
		assessments = new ArrayList<String>();
		assessments.addAll(list);
	}

	public void addAssessmentNumber(String number) {
		assessments.add(number);
	}

	@Override
	public boolean matches(Object requirement) {
		try {
			String number = (String) requirement;
			for (int i = 0; i < assessments.size(); i++) {
				if ((assessments.get(i)).equalsIgnoreCase(number)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Assessment Scope";
	}

}
