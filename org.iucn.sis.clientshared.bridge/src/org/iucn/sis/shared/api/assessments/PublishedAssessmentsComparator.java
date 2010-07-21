package org.iucn.sis.shared.api.assessments;

import java.util.Comparator;
import java.util.Date;

import org.iucn.sis.shared.api.models.Assessment;

public class PublishedAssessmentsComparator implements Comparator<Assessment> {

	public int compare(Assessment o1, Assessment o2) {

		if (o1.isGlobal() && o2.isRegional())
			return 1;

		if (o2.isGlobal() && o1.isRegional())
			return -1;

		Date date2 = o2.getDateAssessed();
		Date date1 = o1.getDateAssessed();

		if (date2 == null)
			return -1;
		else if (date1 == null)
			return 1;

		return date2.compareTo(date1);
	}

}
