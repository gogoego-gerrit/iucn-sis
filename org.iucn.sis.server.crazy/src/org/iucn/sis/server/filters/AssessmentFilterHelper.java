package org.iucn.sis.server.filters;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.utils.MostRecentFlagger;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentFilter;

import com.solertium.vfs.VFS;

public class AssessmentFilterHelper {

	protected final AssessmentFilter filter;

	public AssessmentFilterHelper(AssessmentFilter filter) {
		this.filter = filter;
	}

	public List<AssessmentData> getAssessments( String taxaID, VFS vfs) {
		ArrayList<AssessmentData> ret = new ArrayList<AssessmentData>();

		if (filter.isDraft()) {
			List<AssessmentData> draftAssessments = AssessmentIO.readAllDraftAssessments(vfs, taxaID);
			for (AssessmentData draft : draftAssessments)
				if (allowAssessment(draft))
					ret.add(draft);			
		}

		if (filter.isRecentPublished() || filter.isAllPublished()) {
			List<AssessmentData> publishedAssessments  = AssessmentIO.readPublishedAssessmentsForTaxon(vfs, taxaID);
			MostRecentFlagger.flagMostRecentInList(publishedAssessments);
			for (AssessmentData published : publishedAssessments) {
				if (published != null) {
					if (allowAssessment(published)) {
						ret.add(published);
						
					}
				}
				
			}
		}

		return ret;
	}

	public boolean allowAssessment(AssessmentData assessment) {
		if (filter.isRecentPublished() && assessment.isPublished() && assessment.isHistorical() )
			return false;
		else if( filter.isDraft() && !assessment.isDraft() )
			return false;
		else {
			if (filter.getRegionType().equalsIgnoreCase(AssessmentFilter.REGION_TYPE_ALL))
				return true;
			else if (filter.getRegionType().equalsIgnoreCase(AssessmentFilter.REGION_TYPE_OR)) {
				List<String> regionIds = assessment.getRegionIDs();
				for (String region : filter.getRegions())
					if (regionIds.contains(region))
						return true;
				
				return false; //If it hasn't returned yet, no region matched
			}
			else //AssessmentFilter.REGION_TYPE_AND
				return assessment.getRegionIDs().containsAll(filter.getRegions());
		}
	}
		

}
