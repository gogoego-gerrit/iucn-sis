package io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Region;
import org.junit.Assert;
import org.junit.Test;

import core.BasicHibernateTest;

public class AssessmentIOTest extends BasicHibernateTest {
	
	private static final int EUROPE = 2;
	private static final int ASIA = 3;
	private static final int AFRICA = 4;
	private static final int ANTARCTICA = 5;
	
	private static final AtomicInteger id = new AtomicInteger(1);
	
	@Test
	public void testTwoGlobal() {
		AssessmentIO io = new AssessmentIO(null);
		
		List<Assessment> existing = new ArrayList<Assessment>();
		existing.add(createDraftRegionalAssessment(createGlobalRegion()));
		
		Assessment global = createDraftRegionalAssessment(createGlobalRegion());
		
		Assert.assertTrue(io.conflicts(global, existing));
	}
	
	@Test
	public void testNoConflict() {
		AssessmentIO io = new AssessmentIO(null);
		
		List<Assessment> existing = new ArrayList<Assessment>();
		existing.add(createDraftRegionalAssessment(createRegion(AFRICA, "Africa")));
		
		Assessment global = createDraftRegionalAssessment(createGlobalRegion());
		
		Assert.assertFalse(io.conflicts(global, existing));
	}
	
	@Test
	public void testMultiple() {
		AssessmentIO io = new AssessmentIO(null);
		
		List<Assessment> existing = new ArrayList<Assessment>();
		existing.add(createDraftRegionalAssessment(createGlobalRegion()));
		existing.add(createDraftRegionalAssessment(createRegion(AFRICA, "Africa")));
		existing.add(createDraftRegionalAssessment(createRegion(AFRICA, "Asia")));
		
		Assessment global = createDraftRegionalAssessment(createGlobalRegion(), createRegion(AFRICA, "Africa"));
		
		Assert.assertTrue(io.conflicts(global, existing));
	}
	
	/**
	 * Currently, you can create a new region assessment 
	 * even when an assessment for that region exists if 
	 * the assessment being created contains multiple 
	 * regions.
	 * 
	 * ??
	 */
	@Test
	public void testMultipleNonGlobal() {
		AssessmentIO io = new AssessmentIO(null);
		
		List<Assessment> existing = new ArrayList<Assessment>();
		existing.add(createDraftRegionalAssessment(createRegion(AFRICA, "Africa")));
		existing.add(createDraftRegionalAssessment(createRegion(ASIA, "Asia")));
		
		Assessment global = createDraftRegionalAssessment(createGlobalRegion(), createRegion(AFRICA, "Africa"));
		
		Assert.assertFalse(io.conflicts(global, existing));
	}
	
	@Test
	public void testMultipleNonGlobalConflict() {
		AssessmentIO io = new AssessmentIO(null);
		
		List<Assessment> existing = new ArrayList<Assessment>();
		existing.add(createDraftRegionalAssessment(createRegion(AFRICA, "Africa"), createRegion(ASIA, "Asia")));
		
		Assessment global = createDraftRegionalAssessment(createRegion(ASIA, "Asia"), createRegion(AFRICA, "Africa"));
		
		Assert.assertTrue(io.conflicts(global, existing));
	}
	
	/**
	 * If set A contains two regions & set B contains 
	 * 3, even if set B contains the same regions that 
	 * set A contains, it will not conflict. ??
	 */
	@Test
	public void testMultipleNonGlobalNoConflictMoreRegions() {
		AssessmentIO io = new AssessmentIO(null);
		
		List<Assessment> existing = new ArrayList<Assessment>();
		existing.add(createDraftRegionalAssessment(createRegion(AFRICA, "Africa"), 
				createRegion(EUROPE, "Europe"), createRegion(ASIA, "Asia")));
		
		Assessment global = createDraftRegionalAssessment(
			createRegion(ASIA, "Asia"), createRegion(AFRICA, "Africa"), createRegion(EUROPE, "Europe"), createRegion(ANTARCTICA, "Antarctica"));
		
		Assert.assertFalse(io.conflicts(global, existing));
	}
	
	private Assessment createDraftRegionalAssessment(Region... region) {
		Assessment assessment = new Assessment();
		assessment.setId(id.getAndIncrement());
		assessment.setType(AssessmentType.DRAFT_ASSESSMENT_TYPE);
		assessment.setSchema(SIS.get().getDefaultSchema());
		assessment.setRegions(Arrays.asList(region));
		
		return assessment;
	}
	
	private Region createGlobalRegion() {
		return createRegion(Region.GLOBAL_ID, "Global");
	}
	
	private Region createRegion(int id, String name) {
		return new Region(id, name, "");
	}

}
