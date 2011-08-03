package org.iucn.sis.shared.api.criteriacalculator;

import java.util.HashMap;

import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;

/**
 * Represents the endangered class
 * 
 * @author liz.schwartz
 * 
 */
class EN extends Classification {

	// NUMBER OF CRITERIA FOR EACH LETTER
	public final int criteriaA = 4;
	public final int criteriaB = 2;
	public final int criteriaC = 2;
	public final int criteriaD = 1;
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
	public Range d;
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
	public final String[] factorsC = new String[] { Factors.populationSize, Factors.populationDeclineGenerations2,
			Factors.populationDecline, Factors.subpopulationSize, Factors.populationFluctuation };
	public final String[] factorsD = new String[] { Factors.populationSize };
	public final String[] factorsE = new String[] { Factors.extinctionGenerations5 };

	public EN() {
		super(ResultCategory.EN);
		
		// A1, A2, or A3 must be true in order for A to be true
		aPopulationReductionPast1 = 70; // >=70
		aPopulationReductionPast2 = 50; // >=
		aPopulationReductionFuture3 = 50; // >=
		aPopulationReductionEither = 50; // >=
		
		// B1 or B2 has to be true for B to be true
		bExtent = 5000; // extent < 5000 km^2
		bArea = 500; // area < 500 km^2
		bLocations = 5; // a locations<=5
		
		// C -- populationSize and (C1 or C2)
		cPopulationSize = 2500; // <250
		cPopulationDeclineGenerations1 = 25; // >=25
		cMaxSubpopulationSize = 250; // <=250
		cAlotInSubpopulation = 0.95; // (maxSubpopulationSize)/populationSize >= .95
		
		// D
		dPopulationSize = 250; // < 250
		
		// E
		eExtinctionGenerations = 20; // >=20
	}

	@Override
	public CriteriaResult c(HashMap<String, Range> factors) {
		return c(factors, Factors.populationDeclineGenerations2);
	}

}
