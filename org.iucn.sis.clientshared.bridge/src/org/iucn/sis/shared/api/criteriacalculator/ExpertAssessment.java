package org.iucn.sis.shared.api.criteriacalculator;

import java.util.HashMap;

import org.iucn.sis.shared.api.debug.Debug;

/**
 * An in-memory representation of an Assessment containing the information the
 * expert system actually cares about.
 * 
 * @author adam.schwartz
 * 
 */
public class ExpertAssessment {
	private HashMap factors;
	private String assessmentID;

	public ExpertAssessment() {
		factors = new HashMap();
	}

	public ExpertAssessment(HashMap displays) {
		factors = new HashMap();
	}

	public void addFactor(String key, Object value) {
		factors.put(key, value);
	}

	public long getArea() {
		return ((Long) factors.get(Factors.area)).longValue();
	}

	public String getAssessmentID() {
		return assessmentID;
	}

	public long getExtent() {
		return ((Long) factors.get(Factors.extent)).longValue();
	}

	public Range getExtinctionGenerations3() {
		return (Range) factors.get(Factors.extinctionGenerations3);
	}

	public Range getExtinctionGenerations5() {
		return (Range) factors.get(Factors.extinctionGenerations5);
	}

	public Range getExtinctionYears100() {
		return (Range) factors.get(Factors.extinctionYears100);
	}

	public Object getFactor(String key) {
		return factors.get(key);
	}

	public HashMap getFactors() {
		return factors;
	}

	public Range getGenerationLength() {
		return (Range) factors.get(Factors.generationLength);
	}

	public Range getLocations() {
		return ((Range) factors.get(Factors.locations));
	}

	public Range getPopulationDeclineGenerations1() {
		return ((Range) factors.get(Factors.populationDeclineGenerations1));
	}

	public Range getPopulationDeclineGenerations2() {
		return ((Range) factors.get(Factors.populationDeclineGenerations2));
	}

	public Range getPopulationDeclineGenerations3() {
		return ((Range) factors.get(Factors.populationDeclineGenerations3));
	}

	public long getPopulationReductionEither() {
		return ((Long) factors.get(Factors.populationReductionEither)).longValue();
	}

	public String getPopulationReductionEitherBasis() {
		return (String) factors.get(Factors.populationReductionEitherBasis);
	}

	public Boolean getPopulationReductionEitherCeased() {
		return (Boolean) factors.get(Factors.populationReductionEitherCeased);
	}

	public Boolean getPopulationReductionEitherReversible() {
		return (Boolean) factors.get(Factors.populationReductionEitherReversible);
	}

	public Boolean getPopulationReductionEitherUnderstood() {
		return (Boolean) factors.get(Factors.populationReductionEitherUnderstood);
	}

	public long getPopulationReductionFuture() {
		return ((Long) factors.get(Factors.populationReductionFuture)).longValue();
	}

	public String getPopulationReductionFutureBasis() {
		return (String) factors.get(Factors.populationReductionFutureBasis);
	}

	public Boolean getPopulationReductionFutureCeased() {
		return (Boolean) factors.get(Factors.populationReductionFutureCeased);
	}

	public Boolean getPopulationReductionFutureReversible() {
		return (Boolean) factors.get(Factors.populationReductionFutureReversible);
	}

	public Boolean getPopulationReductionFutureUnderstood() {
		return (Boolean) factors.get(Factors.populationReductionFutureUnderstood);
	}

	public long getPopulationReductionPast() {
		return ((Long) factors.get(Factors.populationReductionPast)).longValue();
	}

	public String getPopulationReductionPastBasis() {
		return (String) factors.get(Factors.populationReductionPastBasis);
	}

	public Boolean getPopulationReductionPastCeased() {
		return (Boolean) factors.get(Factors.populationReductionPastCeased);
	}

	public Boolean getPopulationReductionPastReversible() {
		return (Boolean) factors.get(Factors.populationReductionPastReversible);
	}

	public Boolean getPopulationReductionPastUnderstood() {
		return (Boolean) factors.get(Factors.populationReductionPastUnderstood);
	}

	public Range getPopulationSize() {
		return (Range) factors.get(Factors.populationSize);
	}

	public long getSubpopulationSize() {
		if (factors.get(Factors.subpopulationSize) == null)
			Debug.println("Whoopz");
		return ((Long) factors.get(Factors.subpopulationSize)).longValue();
	}

	public boolean isAreaDecline() {
		return ((Boolean) factors.get(Factors.areaDecline)).booleanValue();
	}

	public boolean isAreaFluctuation() {
		return ((Boolean) factors.get(Factors.areaFluctuation)).booleanValue();
	}

	public boolean isAreaRestricted() {
		return ((Boolean) factors.get(Factors.areaRestricted)).booleanValue();
	}

	public boolean isExtentDecline() {
		return ((Boolean) factors.get(Factors.extentDecline)).booleanValue();
	}

	public boolean isExtentFluctuation() {
		return ((Boolean) factors.get(Factors.extentFluctuation)).booleanValue();
	}

	public boolean isHabitatDecline() {
		return ((Boolean) factors.get(Factors.habitatDecline)).booleanValue();
	}

	public boolean isLocationDecline() {
		return ((Boolean) factors.get(Factors.locationDecline)).booleanValue();
	}

	public boolean isLocationFluctuation() {
		return ((Boolean) factors.get(Factors.locationFluctuation)).booleanValue();
	}

	public boolean isPopulationDecline() {
		return ((Boolean) factors.get(Factors.populationDecline)).booleanValue();
	}

	public boolean isPopulationFluctuation() {
		return ((Boolean) factors.get(Factors.populationFluctuation)).booleanValue();
	}

	public boolean isSevereFragmentation() {
		return ((Boolean) factors.get(Factors.severeFragmentation)).booleanValue();
	}

	public boolean isSubpopulationDecline() {
		return ((Boolean) factors.get(Factors.subpopulationDecline)).booleanValue();
	}

	public boolean isSubpopulationFluctuation() {
		return ((Boolean) factors.get(Factors.subpopulationFluctuation)).booleanValue();
	}

	public boolean isSubpopulationSingle() {
		return ((Boolean) factors.get(Factors.subpopulationSingle)).booleanValue();
	}

	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}

}
