package org.iucn.sis.shared.api.models.comparators;

import java.util.Comparator;
import java.util.Date;

import org.iucn.sis.shared.api.models.Assessment;

public class AssessmentDateComparator implements Comparator<Assessment> {
	
	public int compare(Assessment o1, Assessment o2) {
		Date date1 = o1.getDateAssessed();
		Date date2 = o2.getDateAssessed();
		
		if (date1 == null && date2 == null)
			return 0;
		else if (date1 == null)
			return 1;
		else if (date2 == null)
			return -1;
		else
			return date1.compareTo(date2) * -1;
	}

}
