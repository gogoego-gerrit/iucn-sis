package org.iucn.sis.shared.api.models.comparators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Taxon;

public abstract class AssessmentNavigationComparator implements Comparator<Assessment> {
	
	private final AssessmentDateComparator dateComparator;
	private final TaxonNavigationComparator taxonComparator;
	
	private final List<String> typeOrder;
	
	public AssessmentNavigationComparator() {
		this(false);
	}
	
	public AssessmentNavigationComparator(boolean sortByTaxon) {
		dateComparator = new AssessmentDateComparator();
		taxonComparator = sortByTaxon ? new TaxonNavigationComparator() : null;
		
		typeOrder = new ArrayList<String>();
		typeOrder.add(AssessmentType.DRAFT_ASSESSMENT_TYPE);
		typeOrder.add(AssessmentType.SUBMITTED_ASSESSMENT_TYPE);
		typeOrder.add(AssessmentType.FOR_PUBLICATION_ASSESSMENT_TYPE);
		typeOrder.add(AssessmentType.PUBLISHED_ASSESSMENT_TYPE);
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
			Integer s1 = typeOrder.indexOf(o1.getType());
			Integer s2 = typeOrder.indexOf(o2.getType());
			
			result = s1.compareTo(s2); 
			
			if (result == 0)
				result = dateComparator.compare(o1, o2);
		}
		
		return result;
	}
	
	public abstract Taxon getTaxonForAssessment(Assessment assessment);

}
