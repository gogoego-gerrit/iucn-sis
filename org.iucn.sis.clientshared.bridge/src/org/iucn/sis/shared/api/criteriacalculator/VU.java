package org.iucn.sis.shared.api.criteriacalculator;

import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;


/**
 * Represents the vulnerable classification
 * 
 * @author liz.schwartz
 * 
 */
class VU extends Classification {

	// NUMBER OF CRITERIA PER LETTER
	public final int criteriaA = 4;
	public final int criteriaB = 2;
	public final int criteriaC = 2;
	public final int criteriaD = 2;
	public final int criteriaE = 1;

	// RANGES FOR EACH CRITERIA
	public Range a1;
	public Range a2;
	public Range a3;
	public Range a4;
	public Range b1;
	public Range b2;
	public Range c1;
	public Range c2;
	public Range d1;
	public Range d2;
	public Range e;

	// FACTORS IN EACH CRITERIA
	public final String[] factorsA1 = new String[] { Factors.populationReductionPast,
			Factors.populationReductionPastReversible, Factors.populationReductionPastUnderstood,
			Factors.populationReductionPastCeased };
	public final String[] factorsA2 = new String[] { Factors.populationReductionPast,
			Factors.populationReductionPastReversible, Factors.populationReductionPastUnderstood,
			Factors.populationReductionPastCeased };
	public final String[] factorsA3 = new String[] { Factors.populationReductionFuture };
	public final String[] factorsA4 = new String[] { Factors.populationReductionEither,
			Factors.populationReductionEitherCeased, Factors.populationReductionEitherUnderstood,
			Factors.populationReductionEitherReversible };
	public final String[] factorsB1 = new String[] { Factors.extent, Factors.severeFragmentation, Factors.locations,
			Factors.extentDecline, Factors.areaDecline, Factors.habitatDecline, Factors.locationDecline,
			Factors.subpopulationDecline, Factors.populationDecline, Factors.extentFluctuation,
			Factors.areaFluctuation, Factors.locationFluctuation, Factors.subpopulationFluctuation,
			Factors.populationFluctuation };
	public final String[] factorsB2 = new String[] { Factors.area, Factors.severeFragmentation, Factors.locations,
			Factors.extentDecline, Factors.areaDecline, Factors.habitatDecline, Factors.locationDecline,
			Factors.subpopulationDecline, Factors.populationDecline, Factors.extentFluctuation,
			Factors.areaFluctuation, Factors.locationFluctuation, Factors.subpopulationFluctuation,
			Factors.populationFluctuation };
	public final String[] factorsC = new String[] { Factors.populationSize, Factors.populationDeclineGenerations3,
			Factors.populationDecline, Factors.subpopulationSize, Factors.populationFluctuation };
	public final String[] factorsD1 = new String[] { Factors.populationSize };
	public final String[] factorsD2 = new String[] { Factors.areaRestricted, Factors.locations };
	public final String[] factorsE = new String[] { Factors.extinctionYears100 };

	public VU() {
		super(ResultCategory.VU);
		
		// A1, A2, or A3 must be true in order for A to be true
		aPopulationReductionPast1 = 50; // >=70
		aPopulationReductionPast2 = 30; // >=
		aPopulationReductionFuture3 = 30; // >=
		aPopulationReductionEither = 30; // >=

		// B1 or B2 has to be true for B to be true
		bExtent = 20000; // extent < 20000 km^2
		bArea = 2000; // area < 2000 km^2
		bLocations = 10; // a locations<=10

		// C -- populationSize and (C1 or C2)
		cPopulationSize = 10000; // <10000
		cPopulationDeclineGenerations1 = 10; // >=10
		cMaxSubpopulationSize = 1000; // <=1000
		cAlotInSubpopulation = 1;
		
		// D either D1 or D2 is all true
		dPopulationSize = 1000; // < 1000
		
		// E
		eExtinctionGenerations = 10; // >=10
	}

	public CriteriaResult c(HashMap<String, Range> factors) {
		return c(factors, Factors.populationDeclineGenerations3);
	}
	
	/**
	 * @deprecation use d1 instead. 
	 */
	@Deprecated
	public CriteriaResult d(Range ps) {
		return d1(ps);
	}

	public CriteriaResult d1(Range ps) {
		d1 = Range.lessthan(ps, dPopulationSize);
		
		CriteriaResult analysis = new CriteriaResult(name, "d1");
		analysis.range = d1;
		if (isNonZero(d1))
			analysis.setCriteriaSet(new CriteriaSet(name, "D1"));
		
		return analysis;
	}

	public CriteriaResult d2(Map<String, Range> map) {
		Range ar = map.get(Factors.areaRestricted);
		
		Range loc = map.get(Factors.locations);
		loc = Range.lessthanequal(loc, 5);
		
		Range fin = Range.independentOR(ar, loc);
		
		CriteriaResult analysis = new CriteriaResult(name, "d2");
		d2 = fin;
		analysis.range = fin;
		if (isNonZero(fin))
			analysis.setCriteriaSet(new CriteriaSet(name, "D2"));
		
		return analysis;
	}

}
