package org.iucn.sis.shared.api.models.comparators;

import java.util.Comparator;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;

import com.solertium.util.portable.PortableAlphanumericComparator;

public abstract class AssessmentNavigationComparator implements Comparator<Assessment> {
	
	private final PortableAlphanumericComparator comparator;
	private final AssessmentDateComparator dateComparator;
	private final TaxonNavigationComparator taxonComparator;
	
	public AssessmentNavigationComparator() {
		this(false);
	}
	
	public AssessmentNavigationComparator(boolean sortByTaxon) {
		comparator = new PortableAlphanumericComparator();
		dateComparator = new AssessmentDateComparator();
		taxonComparator = sortByTaxon ? new TaxonNavigationComparator() : null;
	}
	
	@Override
	public int compare(Assessment o1, Assessment o2) {
		int result = 0;
		if (taxonComparator != null) {
			Taxon t1 = getTaxonForAssessment(o1);
			Taxon t2 = getTaxonForAssessment(o2);
			if (t1 != null && t2 != null)
				result = taxonComparator.compare(t1, t2);
		}
		
		if (result == 0) {
			String s1 = o1.getAssessmentType().getName();
			String s2 = o2.getAssessmentType().getName();
			
			result = comparator.compare(s1, s2); 
			
			if (result == 0)
				result = dateComparator.compare(o1, o2);
		}
		
		return result;
	}
	
	public abstract Taxon getTaxonForAssessment(Assessment assessment);

}
