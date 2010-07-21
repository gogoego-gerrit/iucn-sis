package org.iucn.sis.client.expert;

public abstract class Factors {
	public final static String locations = "LocationsNumber";
	public final static String locationDecline = "LocationContinuingDecline";
	public final static String locationFluctuation = "LocationExtremeFluctuation";

	public final static String subpopulationDecline = "SubpopulationContinuingDecline";
	public final static String subpopulationFluctuation = "SubpopulationExtremeFluctuation";
	public final static String subpopulationSingle = "SubpopulationSingle";
	public final static String subpopulationSize = "MaxSubpopulationSize";

	public final static String area = "AOO";
	public final static String areaDecline = "AOOContinuingDecline";
	public final static String areaFluctuation = "AOOExtremeFluctuation";
	public final static String areaRestricted = "AreaRestricted";
	public final static String extent = "EOO";
	public final static String extentDecline = "EOOContinuingDecline";
	public final static String extentFluctuation = "EOOExtremeFluctuation";

	public final static String habitatDecline = "HabitatContinuingDecline";
	public final static String severeFragmentation = "SevereFragmentation";
	public final static String generationLength = "GenerationLength";

	public final static String extinctionGenerations5 = "ExtinctionProbabilityGenerations5";
	public final static String extinctionGenerations3 = "ExtinctionProbabilityGenerations3";
	public final static String extinctionYears100 = "ExtinctionProbabilityYears100";

	public final static String populationSize = "PopulationSize";
	public final static String populationFluctuation = "PopulationExtremeFluctuation";
	public final static String populationDecline = "PopulationContinuingDecline";
	public final static String populationDeclineGenerations1 = "PopulationDeclineGenerations1";
	public final static String populationDeclineGenerations2 = "PopulationDeclineGenerations2";
	public final static String populationDeclineGenerations3 = "PopulationDeclineGenerations3";

	public final static String populationReductionPast = "PopulationReductionPast";
	public final static String populationReductionPastBasis = "PopulationReductionPastBasis";
	public final static String populationReductionPastUnderstood = "PopulationReductionPastUnderstood";
	public final static String populationReductionPastReversible = "PopulationReductionPastReversible";
	public final static String populationReductionPastCeased = "PopulationReductionPastCeased";

	public final static String populationReductionFuture = "PopulationReductionFuture";
	public final static String populationReductionFutureBasis = "PopulationReductionFutureBasis";
	public final static String populationReductionFutureUnderstood = "PopulationReductionFutureUnderstood";
	public final static String populationReductionFutureReversible = "PopulationReductionFutureReversible";
	public final static String populationReductionFutureCeased = "PopulationReductionFutureCeased";

	public final static String populationReductionEither = "PopulationReductionPastandFuture";
	public final static String populationReductionEitherCeased = "PopulationReductionPastandFutureCeased";
	public final static String populationReductionEitherUnderstood = "PopulationReductionPastandFutureUnderstood";
	public final static String populationReductionEitherBasis = "PopulationReductionPastandFutureBasis";
	public final static String populationReductionEitherReversible = "PopulationReductionPastandFutureReversible";

	public static String[] factors = new String[] { locations, locationDecline, locationFluctuation,
			subpopulationDecline, subpopulationFluctuation, subpopulationSingle, subpopulationSize, area, areaDecline,
			areaFluctuation, areaRestricted, extent, extentDecline, extentFluctuation, habitatDecline,
			severeFragmentation, generationLength, extinctionGenerations3, extinctionGenerations5, extinctionYears100,
			populationSize, populationFluctuation, populationDecline, populationDeclineGenerations1,
			populationDeclineGenerations2, populationDeclineGenerations3, populationReductionPast,
			populationReductionPastBasis, populationReductionPastCeased, populationReductionPastReversible,
			populationReductionPastUnderstood, populationReductionFuture, populationReductionFutureBasis,
			populationReductionFutureCeased, populationReductionFutureReversible, populationReductionFutureUnderstood,
			populationReductionEither, populationReductionEitherBasis, populationReductionEitherCeased,
			populationReductionEitherReversible, populationReductionEitherUnderstood };

	public final static int NUMBER_OF_FACTORS = factors.length;

}
