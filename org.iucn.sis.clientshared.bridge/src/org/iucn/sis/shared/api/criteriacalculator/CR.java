package org.iucn.sis.shared.api.criteriacalculator;

import java.util.HashMap;

/**
 * Represents the critically endangered class
 * 
 * @author liz.schwartz
 * 
 */
class CR extends Classification {

	// NUMBER OF CRITERIA PER LETTER
	public final int criteriaA = 4;
	public final int criteriaB = 2;
	public final int criteriaC = 2;
	public final int criteriaD = 1;
	public final int criteriaE = 1;	

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
	public final String[] factorsC = new String[] { Factors.populationSize, Factors.populationDeclineGenerations1,
			Factors.populationDecline, Factors.subpopulationSize, Factors.populationFluctuation };
	public final String[] factorsD = new String[] { Factors.populationSize };
	public final String[] factorsE = new String[] { Factors.extinctionGenerations3 };

	public CR() {
		super("CR");
		// A1, A2, A3, or A4 must be true in order for A to be true
		aPopulationReductionPast1 = 90; // >=
		aPopulationReductionPast2 = 80; // >=
		aPopulationReductionFuture3 = 80; // >=
		aPopulationReductionEither = 80; // >=
		
		bExtent = 100; // extent < 100 km^2
		bArea = 10; // area < 10km^2
		bLocations = 1; // a locations==1
		
		cPopulationSize = 250; // <250
		cPopulationDeclineGenerations1 = 25; // >=25
		cMaxSubpopulationSize = 50; // <=50
		cAlotInSubpopulation = 0.9; // (maxSubpopulationSize)/
		// populationSize >= .9
		
		// D
		dPopulationSize = 50; // < 50	
		
		// E
		eExtinctionGenerations = 50; // >=50
	}

	@Override
	public CriteriaResult c(HashMap<String, Range> factors) {
		return c(factors, Factors.populationDeclineGenerations1);
	}

}
