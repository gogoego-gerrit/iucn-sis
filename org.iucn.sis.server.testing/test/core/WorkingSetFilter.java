package core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Region;
import org.iucn.sis.shared.api.models.Relationship;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.junit.Before;
import org.junit.Test;

public class WorkingSetFilter extends BasicHibernateTest {
	
	private static final int DRAFT_GLOBAL = 1;
	private static final int DRAFT_MEXICO = 2;
	private static final int DRAFT_FRANCE = 3;
	private static final int PUB_GLOBAL = 4;
	private static final int DRAFT_FRANCE_ITALY = 5;
	private static final int PUB_GLOBAL_MEXICO = 6;
	private static final int PUB_FRANCE = 7;
	
	private static final Region GLOBAL = Region.getGlobalRegion();
	private static final Region MEXICO = new Region(3, "Mexico", "Mexico");
	private static final Region FRANCE = new Region(4, "France", "France");
	private static final Region ITALY = new Region(5, "Italy", "Italy");

	
	@Before
	public void beforeTest(){
		System.out.println(" --- Starting --- ");
	}
	
	@Test
	public void testGlobalFilter() {
		WorkingSet ws = createDraftGlobalWorkingSet();
		
		Set<Integer> correctResult = new HashSet<Integer>();
		correctResult.add(DRAFT_GLOBAL);
		
		filter(ws, correctResult);
	}
	
	@Test
	public void testMexicoFilter() {
		WorkingSet ws = createWorkingSetWithFilter(AssessmentFilter.DRAFT_TAG, MEXICO);
		
		Set<Integer> correctResult = new HashSet<Integer>();
		correctResult.add(DRAFT_MEXICO);
		
		filter(ws, correctResult);
	}
	
	@Test
	public void testPublishedFilter() {
		WorkingSet ws = createPublishedGlobalWorkingSet();
		
		Set<Integer> correctResult = new HashSet<Integer>();
		correctResult.add(PUB_GLOBAL);
		
		filter(ws, correctResult);
	}
	
	/*
	 * Ensure that an assessment is found as most recent 
	 * even if it is not the most recent assessment.
	 */
	@Test
	public void testHistoricalPublishedFilter() {
		WorkingSet ws = createWorkingSetWithFilter(AssessmentFilter.RECENT_PUBLISHED, FRANCE);
		
		Set<Integer> correctResult = new HashSet<Integer>();
		correctResult.add(PUB_FRANCE);
		
		filter(ws, correctResult, getDraftAssessments(), getManyPublishedAssessments());
	}
	
	@Test
	public void testHistoricalRegionalPublishedFilter() {
		WorkingSet ws = createWorkingSetWithFilter(AssessmentFilter.RECENT_PUBLISHED, GLOBAL, MEXICO);
		
		Set<Integer> correctResult = new HashSet<Integer>();
		correctResult.add(PUB_GLOBAL_MEXICO);
		
		filter(ws, correctResult, getDraftAssessments(), getManyPublishedAssessments());
	}
	
	/*
	 * This should only allow the most recent Global assessment, not both of them.
	 */
	@Test
	public void testMostRecentPublishedFilter() {
		WorkingSet ws = createWorkingSetWithFilter(AssessmentFilter.RECENT_PUBLISHED, GLOBAL);
		ws.setRelationship(Relationship.fromName(Relationship.OR));
		
		Set<Integer> correctResult = new HashSet<Integer>();
		correctResult.add(PUB_GLOBAL);
		
		filter(ws, correctResult, getDraftAssessments(), getManyPublishedAssessments());
	}
	
	/*
	 * This should allow all published Global assessments, not just one. 
	 */
	@Test
	public void testAllGlobalPublishedFilter() {
		WorkingSet ws = createWorkingSetWithFilter(AssessmentFilter.ALL_PUBLISHED, GLOBAL);
		ws.setRelationship(Relationship.fromName(Relationship.OR));
		
		Set<Integer> correctResult = new HashSet<Integer>();
		correctResult.add(PUB_GLOBAL);
		correctResult.add(PUB_GLOBAL_MEXICO);
		
		filter(ws, correctResult, getDraftAssessments(), getManyPublishedAssessments());
	}
	
	@Test
	public void testUpdateFilter() {
		WorkingSet ws = createDraftGlobalWorkingSet();
		
		Set<Integer> correctResult = new HashSet<Integer>();
		correctResult.add(DRAFT_GLOBAL);
		
		filter(ws, correctResult);
		
		/*
		 * Change it, now requires both regions specified, will 
		 * match only one assessment.
		 */
		AssessmentFilter filter = ws.getFilter();
		filter.setRegionType(Relationship.AND);
		filter.getRegions().clear();
		filter.getRegions().add(FRANCE);
		filter.getRegions().add(ITALY);
		
		ws.setFilter(filter);
		
		correctResult.clear();
		correctResult.add(DRAFT_FRANCE_ITALY);
		
		filter(ws, correctResult);
		
		/*
		 * Change from AND to OR, so that either of the specified 
		 * regions match.  Now should match 2 assessments.
		 */
		filter = ws.getFilter();
		filter.setRegionType(Relationship.OR);
		
		ws.setFilter(filter);
		
		correctResult.clear();
		correctResult.add(DRAFT_FRANCE_ITALY);
		correctResult.add(DRAFT_FRANCE);
		
		filter(ws, correctResult);
		
		/*
		 * Change it.  Now add Mexico to the mix, and it 
		 * should be able to find a third assessment.
		 */
		filter = ws.getFilter();
		filter.getRegions().add(MEXICO);
		
		ws.setFilter(filter);
		
		correctResult.clear();
		correctResult.add(DRAFT_FRANCE_ITALY);
		correctResult.add(DRAFT_FRANCE);
		correctResult.add(DRAFT_MEXICO);
		
		filter(ws, correctResult);
		
		/*
		 * Change it.  Require the assessment to have all 
		 * regions by using AND.  This should not match any 
		 * assessments.
		 */
		filter = ws.getFilter();
		filter.setRegionType(Relationship.AND);
		
		ws.setFilter(filter);
		
		correctResult.clear();
		
		filter(ws, correctResult);
		
		/*
		 * Change it.  Use published instead of draft, and 
		 * set regions to global.  Should find one assessment. 
		 */
		filter = ws.getFilter();
		filter.setAllRegions();
		filter.getRegions().add(GLOBAL);
		filter.setDraft(false);
		filter.setRecentPublished(false);
		filter.setAllPublished(true);
		
		ws.setFilter(filter);
		
		correctResult.clear();
		correctResult.add(PUB_GLOBAL);
		
		filter(ws, correctResult);
	}
	
	private void filter(WorkingSet ws, Set<Integer> correctResult) {
		filter(ws, correctResult, getDraftAssessments(), getPublishedAssessments());
	}
	
	private void filter(WorkingSet ws, Set<Integer> correctResult, List<Assessment> draftAssessments, 
			List<Assessment> publishedAssessments) {
		AssessmentFilter filter = ws.getFilter();
		AssessmentFilterHelperForTesting helper = 
			new AssessmentFilterHelperForTesting(filter, draftAssessments, publishedAssessments);
		
		Debug.println("Filter is draft? {0} with regions {1}", filter.isDraft(), filter.listRegionIDs());
		
		Set<Integer> allowed = new HashSet<Integer>();
		for (Assessment assessment : helper.getAssessments(0))
			allowed.add(assessment.getId());
		
		Debug.println("There are {0} assessments allowed. {1}", allowed.size(), allowed);
		
		Assert.assertNotNull(allowed);
		Assert.assertNotNull(correctResult);
		Assert.assertEquals(correctResult.size(), allowed.size());
		Assert.assertTrue(correctResult.containsAll(allowed));
	}
	
	private List<Assessment> getPublishedAssessments() {
		final List<Assessment> list = new ArrayList<Assessment>();
		
		Assessment pubGlobal = new Assessment(); {
			Collection<Region> regions = new ArrayList<Region>();
			regions.add(GLOBAL);
		
			pubGlobal.setId(PUB_GLOBAL);
			pubGlobal.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
			pubGlobal.setRegions(regions);
		}
		list.add(pubGlobal);
		
		return list;
	}
	
	private List<Assessment> getManyPublishedAssessments() {
		final List<Assessment> list = new ArrayList<Assessment>();
		
		//Most recent
		Assessment pubGlobal = new Assessment(); {
			Collection<Region> regions = new ArrayList<Region>();
			regions.add(GLOBAL);
		
			pubGlobal.setId(PUB_GLOBAL);
			pubGlobal.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
			pubGlobal.setRegions(regions);
			pubGlobal.setDateAssessed(Calendar.getInstance().getTime());
		}
		
		//Published 3 months ago.
		Assessment pubGlobalMexico = new Assessment(); {
			final Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, -3);
			
			Collection<Region> regions = new ArrayList<Region>();
			regions.add(GLOBAL);
			regions.add(MEXICO);
			
			pubGlobalMexico.setId(PUB_GLOBAL_MEXICO);
			pubGlobalMexico.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
			pubGlobalMexico.setRegions(regions);
			pubGlobalMexico.setDateAssessed(calendar.getTime());
		}
		
		Assessment pubFrance = new Assessment(); {
			final Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.YEAR, -1);
			
			Collection<Region> regions = new ArrayList<Region>();
			regions.add(FRANCE);
			
			pubFrance.setId(PUB_FRANCE);
			pubFrance.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
			pubFrance.setRegions(regions);
			pubFrance.setDateAssessed(calendar.getTime());
		}
		
		list.add(pubGlobal);
		list.add(pubGlobalMexico);
		list.add(pubFrance);
		
		return list;
	}
	
	private List<Assessment> getDraftAssessments() {
		final List<Assessment> list = new ArrayList<Assessment>();
		
		Assessment global = new Assessment(); {
			Collection<Region> regions = new ArrayList<Region>();
			regions.add(GLOBAL);
		
			global.setId(DRAFT_GLOBAL);
			global.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
			global.setRegions(regions);
		}
		list.add(global);
		
		Assessment mexico = new Assessment(); {
			Collection<Region> regions = new ArrayList<Region>();
			regions.add(MEXICO);
		
			mexico.setId(DRAFT_MEXICO);
			mexico.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
			mexico.setRegions(regions);
		}
		list.add(mexico);
		
		Assessment france = new Assessment(); {
			Collection<Region> regions = new ArrayList<Region>();
			regions.add(FRANCE);
		
			france.setId(DRAFT_FRANCE);
			france.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
			france.setRegions(regions);
		}
		list.add(france);
		
		Assessment franceItaly = new Assessment(); {
			Collection<Region> regions = new ArrayList<Region>();
			regions.add(FRANCE);
			regions.add(ITALY);
			
			franceItaly.setId(DRAFT_FRANCE_ITALY);
			franceItaly.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
			franceItaly.setRegions(regions);
		}
		list.add(franceItaly);
		
		return list;
	}
	
	private WorkingSet createDraftGlobalWorkingSet() {
		AssessmentFilter filter = new AssessmentFilter();
		
		WorkingSet ws = new WorkingSet();
		ws.setFilter(filter);
		
		return ws;
	}
	
	private WorkingSet createPublishedGlobalWorkingSet() {
		return createWorkingSetWithFilter(AssessmentFilter.ALL_PUBLISHED);
	}
	
	private WorkingSet createWorkingSetWithFilter(String filterType, Region... regions) {
		AssessmentFilter filter = new AssessmentFilter(filterType);
		
		WorkingSet ws = new WorkingSet();
		ws.setFilter(filter);
		if (regions.length > 0) {
			ws.getRegion().clear();
			for (Region region : regions)
				ws.getRegion().add(region);
		}
		
		return ws;
	}
	
	/**
	 * AssessmentFilterHelperForTesting.java
	 * 
	 * Simulate the AssessmentFilterHelper's read Assessments function by using 
	 * our testing supply of assessments instead of hitting the database.  The 
	 * rest of the functionality comes directly from the superclass, so this 
	 * should be a true test of the functionality in-place.
	 * 
	 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
	 *         href="http://www.solertium.com">Solertium Corporation</a>
	 *
	 */
	public static class AssessmentFilterHelperForTesting extends AssessmentFilterHelper {
		
		private final List<Assessment> drafts;
		private final List<Assessment> published;
		
		public AssessmentFilterHelperForTesting(AssessmentFilter filter, List<Assessment> drafts, List<Assessment> published) {
			super(null, filter, true);
			this.drafts = drafts;
			this.published = published;
		}
		
		protected List<Assessment> readUnpublishedAssessments(Integer taxonID) {
			return drafts;
		}
		
		protected List<Assessment> readPublishedAssessments(Integer taxonID) {
			return published;
		}
	}
	
}

