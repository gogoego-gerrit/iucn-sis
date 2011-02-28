package core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Region;
import org.iucn.sis.shared.api.models.Relationship;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.junit.Test;

public class WorkingSetFilter extends BasicTest {
	
	@Test
	public void testGlobalFilter() {
		WorkingSet ws = createDraftGlobalWorkingSet();
		
		Set<Integer> correctResult = new HashSet<Integer>();
		correctResult.add(1);
		
		filter(ws, correctResult);
	}
	
	@Test
	public void testMexicoFilter() {
		WorkingSet ws = createDraftMexicoWorkingSet();
		
		Set<Integer> correctResult = new HashSet<Integer>();
		correctResult.add(2);
		
		filter(ws, correctResult);
	}
	
	@Test
	public void testPublishedFilter() {
		WorkingSet ws = createPublishedGlobalWorkingSet();
		
		Set<Integer> correctResult = new HashSet<Integer>();
		correctResult.add(4);
		
		filter(ws, correctResult);
	}
	
	private void filter(WorkingSet ws, Set<Integer> correctResult) {
		AssessmentFilter filter = ws.getFilter();
		
		Set<Integer> allowed = new HashSet<Integer>();
		
		for (Assessment assessment : getAssessments())
			if (allowAssessment(filter, assessment, "org.iucn.sis.server.schemas.redlist", filter.listRegionIDs())) {
				Debug.println("Filter is published? {0}", filter.isAllPublished() || filter.isRecentPublished());
				Debug.println("Assessment published?? {0}", assessment.isPublished());
				if (filter.isDraft() && assessment.isDraft())
					allowed.add(assessment.getId());
				else if ((filter.isAllPublished() || filter.isRecentPublished()) && assessment.isPublished())
					allowed.add(assessment.getId());
			}
		
		Debug.println("There are {0} assessments allowed.", allowed.size());
		
		Assert.assertTrue(correctResult.size() == allowed.size() && correctResult.containsAll(allowed));
	}
	
	private Collection<Assessment> getAssessments() {
		final List<Assessment> list = new ArrayList<Assessment>();
		
		Assessment global = new Assessment(); {
			Collection<Region> regions = new ArrayList<Region>();
			regions.add(Region.getGlobalRegion());
		
			global.setId(1);
			global.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
			global.setRegions(regions);
		}
		list.add(global);
		
		Assessment mexico = new Assessment(); {
			Collection<Region> regions = new ArrayList<Region>();
			regions.add(new Region(3, "Mexico", "Mexico"));
		
			mexico.setId(2);
			mexico.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
			mexico.setRegions(regions);
		}
		list.add(mexico);
		
		Assessment france = new Assessment(); {
			Collection<Region> regions = new ArrayList<Region>();
			regions.add(new Region(4, "France", "France"));
		
			france.setId(3);
			france.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
			france.setRegions(regions);
		}
		list.add(france);
		
		Assessment pubGlobal = new Assessment(); {
			Collection<Region> regions = new ArrayList<Region>();
			regions.add(Region.getGlobalRegion());
		
			pubGlobal.setId(4);
			pubGlobal.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
			pubGlobal.setRegions(regions);
		}
		list.add(pubGlobal);
		
		return list;
	}
	
	private WorkingSet createDraftGlobalWorkingSet() {
		AssessmentFilter filter = new AssessmentFilter();
		
		WorkingSet ws = new WorkingSet();
		ws.setFilter(filter);
		
		return ws;
	}
	
	private WorkingSet createPublishedGlobalWorkingSet() {
		AssessmentFilter filter = new AssessmentFilter(AssessmentFilter.ALL_PUBLISHED);
		
		WorkingSet ws = new WorkingSet();
		ws.setFilter(filter);
		
		return ws;
	}
	
	private WorkingSet createDraftMexicoWorkingSet() {
		AssessmentFilter filter = new AssessmentFilter();
		
		WorkingSet ws = new WorkingSet();
		ws.setFilter(filter);
		ws.getRegion().clear();
		ws.getRegion().add(new Region(3, "Mexico", "Mexico"));
		
		return ws;
	}

	private boolean allowAssessment(AssessmentFilter filter, Assessment assessment, String schema, List<Integer> filterRegions) {
		//reportAssessmentInformation(assessment);
		
		boolean result = false;
		if (filter.isRecentPublished() && assessment.isPublished() && assessment.getIsHistorical() )
			result = false;
		else if (filter.isDraft() && !((filter.isRecentPublished() || filter.isAllPublished()) && !assessment.isDraft()))
			result = false;
		else {
			if (filter.getRelationshipName().equalsIgnoreCase(Relationship.ALL)) {
				result = true;
			}
			else if (filter.getRelationshipName().equalsIgnoreCase(Relationship.OR)) {
				List<Integer> regionIds = assessment.getRegionIDs();
				for (Region region : filter.getRegions())
					if (regionIds.contains(region.getId()))
						result |= true;
				
				result |= false; //If it hasn't returned yet, no region matched
			}
			else {
				List<Integer> assessmentRegions = assessment.getRegionIDs();
				Debug.println("ANDing filter regions {0} against assessment regions {1}", filterRegions, assessmentRegions);
				result = assessmentRegions.size() == filterRegions.size() && assessmentRegions.containsAll(filterRegions);
			}
		}
		
		if (schema != null)
			result &= schema.equals(assessment.getSchema("org.iucn.sis.server.schemas.redlist"));
		
		Debug.println("Assessment {0} is allowed: {1}", assessment.getId(), result);
		
		return result;
	}
	
}
