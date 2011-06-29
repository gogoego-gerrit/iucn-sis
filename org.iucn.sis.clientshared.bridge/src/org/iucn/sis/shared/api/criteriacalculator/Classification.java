package org.iucn.sis.shared.api.criteriacalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;


public abstract class Classification {
	
	protected final ResultCategory name;
	
	// RANGES FOR EACH CRITERIA
	public Range a1;
	public Range a2;
	public Range a3;
	public Range a4;
	public Range b1;
	public Range b2;
	public Range c;
	public Range d;
	public Range d1;
	public Range d2;
	public Range e;
	
	// A1, A2, A3, or A4 must be true in order for A to be true
	protected int aPopulationReductionPast1;
	protected int aPopulationReductionPast2;
	protected int aPopulationReductionFuture3;
	protected int aPopulationReductionEither;
	
	// B1 or B2 has to be true for B to be true
	protected int bExtent;
	protected int bArea;
	protected int bLocations;
	
	// C -- populationSize and C1 or C2
	protected int cPopulationSize;
	protected int cPopulationDeclineGenerations1;
	protected int cMaxSubpopulationSize;
	protected double cAlotInSubpopulation;
	
	protected int dPopulationSize;
	
	protected int eExtinctionGenerations;
	
	public Classification(ResultCategory name) {
		this.name = name;
	}
	
	public CriteriaResult a1(HashMap<String, Range> factors, String populationReductionPastBasis) {
		CriteriaResult returnResult = new CriteriaResult(name, "a1");
		Range result = null;
		String[] csv = populationReductionPastBasis.split(",");
		if (!(csv.length == 1 && csv[0] == "0")) {
			Range ppr = Range.greaterthanequal(factors.get(Factors.populationReductionPast),
					(float) aPopulationReductionPast1);
			Range prpr = factors.get(Factors.populationReductionPastReversible);
			Range prpu = factors.get(Factors.populationReductionPastUnderstood);
			Range prpc = factors.get(Factors.populationReductionPastCeased);
			result = Range.independentAND(ppr, prpr);
			result = Range.independentAND(result, prpu);
			result = Range.independentAND(result, prpc);

		}

		a1 = result;
		returnResult.range = result;
		returnResult.setCriteriaSet(createAString(result, csv, "1"));
		if (returnResult.getResultString().equals(""))
			returnResult.range = null;
		
		returnResult.printRange();
		
		return returnResult;
	}	
	
	public CriteriaResult a2(HashMap<String, Range> factors, String populationReductionPastBasis) {
		CriteriaResult returnResult = new CriteriaResult(name, "a2");
		Range result = null;
		String[] csv = populationReductionPastBasis.split(",");

		if (!(csv.length == 1 && csv[0] == "0")) {
			Range ppr = Range.greaterthanequal(factors.get(Factors.populationReductionPast),
					(float) aPopulationReductionPast2);
			Range prpr = factors.get(Factors.populationReductionPastReversible);
			Range prpu = factors.get(Factors.populationReductionPastUnderstood);
			Range prpc = factors.get(Factors.populationReductionPastCeased);

			prpc = Range.isFalse(prpc);
			prpu = Range.isFalse(prpu);
			prpr = Range.isFalse(prpr);

			result = Range.independentOR(prpc, prpu);
			result = Range.independentOR(result, prpr);
			result = Range.independentAND(result, ppr);

		}
		a2 = result;
		returnResult.range = result;
		returnResult.setCriteriaSet(createAString(result, csv, "2"));
		if (returnResult.getResultString().equals(""))
			returnResult.range = null;
		
		returnResult.printRange();
		
		return returnResult;
	}
	
	public CriteriaResult a3(Range prf, String populationReductionFutureBasis) {
		CriteriaResult returnResult = new CriteriaResult(name, "a3");
		Range result = null;
		String[] csv = populationReductionFutureBasis.split(",");

		if (!(csv.length == 1 && csv[0] == "0")) {
			prf = Range.greaterthanequal(prf, aPopulationReductionFuture3);
			result = prf;
		}
		a3 = result;
		returnResult.range = result;
		returnResult.setCriteriaSet(createA3String(result, csv));
		if (returnResult.getResultString().equals(""))
			returnResult.range = null;
		
		returnResult.printRange();
		
		return returnResult;
	}
	
	public CriteriaResult a4(HashMap<String, Range> factors, String populationReductionEitherBasis) {
		CriteriaResult returnResult = new CriteriaResult(name, "a4");
		Range result = null;
		String[] csv = populationReductionEitherBasis.split(",");

		if (!(csv.length == 1 && csv[0] == "0")) {
			Range pre = (Range) factors.get(Factors.populationReductionEither);
			pre = Range.greaterthanequal(pre, aPopulationReductionEither);
			Range prc = (Range) factors.get(Factors.populationReductionEitherCeased);
			Range pru = (Range) factors.get(Factors.populationReductionEitherUnderstood);
			Range prr = (Range) factors.get(Factors.populationReductionEitherReversible);
			prc = Range.isFalse(prc);
			pru = Range.isFalse(pru);
			prr = Range.isFalse(prr);
			result = Range.independentOR(prc, pru);
			result = Range.independentOR(result, prr);
			result = Range.independentAND(result, pre);

		}
		a4 = result;
		returnResult.range = result;
		returnResult.setCriteriaSet(createAString(result, csv, "4"));
		if (returnResult.getResultString().equals(""))
			returnResult.range = null;
		
		returnResult.printRange();
		
		return returnResult;
	}
	
	public CriteriaResult b1(HashMap<String, Range> factors) {
		Range extent = (Range) factors.get(Factors.extent);
		Range and1 = Range.lessthan(extent, bExtent);

		return b(factors, and1, "1");
	}
	
	public CriteriaResult b2(HashMap<String, Range> factors) {
		Range area = (Range) factors.get(Factors.area);
		Range and1 = Range.lessthan(area, bArea);
		
		return b(factors, and1, "2");
	}
	
	private CriteriaResult b(HashMap<String, Range> factors, Range and1, String number) {
		CriteriaResult returnResult = new CriteriaResult(name, "b"+number);
		
		if (isNonZero(and1)) {
			Range sf = (Range) factors.get(Factors.severeFragmentation);
			Range loc = (Range) factors.get(Factors.locations);
			loc = Range.equals(loc, bLocations);
			Range or1 = Range.independentOR(sf, loc);

			Range ed = (Range) factors.get(Factors.extentDecline);
			Range ad = (Range) factors.get(Factors.areaDecline);
			Range hd = (Range) factors.get(Factors.habitatDecline);
			Range ld = (Range) factors.get(Factors.locationDecline);
			Range sd = (Range) factors.get(Factors.subpopulationDecline);
			Range pd = (Range) factors.get(Factors.populationDecline);
			Range or2 = Range.independentOR(ed, ad);
			or2 = Range.independentOR(or2, hd);
			or2 = Range.independentOR(or2, ld);
			or2 = Range.independentOR(or2, sd);
			or2 = Range.independentOR(or2, pd);

			Range ef = (Range) factors.get(Factors.extentFluctuation);
			Range af = (Range) factors.get(Factors.areaFluctuation);
			Range lf = (Range) factors.get(Factors.locationFluctuation);
			Range sef = (Range) factors.get(Factors.subpopulationFluctuation);
			Range pf = (Range) factors.get(Factors.populationFluctuation);
			Range or3 = Range.independentOR(ef, af);
			or3 = Range.independentOR(or3, lf);
			or3 = Range.independentOR(or3, sef);
			or3 = Range.independentOR(or3, pf);

			if ((or1 != null && !Range.isConstant(or1, 0)) && (or2 != null && !Range.isConstant(or2, 0))
					&& (or3 != null && !Range.isConstant(or3, 0))) {

				Range and = Range.independentAND(and1, or1);
				and = Range.independentAND(and, or2);
				and = Range.independentAND(and, or3);
				
				returnResult.range = and;
				if (isNonZero(returnResult.range))
					returnResult.setCriteriaSet(createBString(number, sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf));

			} else if (or1 != null && !Range.isConstant(or1, 0)) {
				if (or2 != null && !Range.isConstant(or2, 0)) {
					Range and = Range.independentAND(and1, or1);
					and = Range.independentAND(and, or2);
					
					returnResult.range = and;
					if (isNonZero(returnResult.range))
						returnResult.setCriteriaSet(createBString(number, sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf));
					
				} else if (or3 != null && !Range.isConstant(or3, 0)) {
					Range and = Range.independentAND(and1, or1);
					and = Range.independentAND(and, or3);
					
					returnResult.range = and;
					if (isNonZero(returnResult.range))
						returnResult.setCriteriaSet(createBString(number, sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf));
				}
				// NOT ENOUGH DATA
				else {
					returnResult.range = null;
					if (isNonZero(returnResult.range))
						returnResult.setCriteriaSet(createBString(number, sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf));
				}
			}

			else if ((or2 != null && !Range.isConstant(or2, 0)) && (or3 != null && !Range.isConstant(or3, 0))) {
				Range and = Range.independentAND(and1, or2);
				and = Range.independentAND(and, or3);
				returnResult.range = and;
				if (isNonZero(returnResult.range))
					returnResult.setCriteriaSet(createBString(number, sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf));
			}

			// NOT ENOUGH DATA
			else {
				returnResult.range = null;
				if (isNonZero(returnResult.range))
					returnResult.setCriteriaSet(createBString(number, sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf));
			}
		}
		else {
			returnResult.range = null;
		}
		
		if ("1".equals(number))
			b1 = returnResult.range;
		else if ("2".equals(number))
			b2 = returnResult.range;
		
		returnResult.printRange();
		
		return returnResult;
	}
	
	public abstract CriteriaResult c(HashMap<String, Range> factors);
	
	public CriteriaResult c(HashMap<String, Range> factors, String declineGenFactor) {
		CriteriaResult returnResult = new CriteriaResult(name, "c");

		Range ps = factors.get(Factors.populationSize);
		Range ps1 = Range.lessthan(ps, cPopulationSize);

		Range pdg1 = (Range) factors.get(declineGenFactor);
		pdg1 = Range.greaterthanequal(pdg1, cPopulationDeclineGenerations1);

		Range pd = factors.get(Factors.populationDecline);
		Range sps = factors.get(Factors.subpopulationSize);
		Range div = Range.divide(sps, ps);
		sps = Range.lessthanequal(sps, cMaxSubpopulationSize);

		Range pf = (Range) factors.get(Factors.populationFluctuation);

		Range result = Range.independentOR(sps, div);
		result = Range.independentOR(result, pf);
		result = Range.independentAND(result, pd);
		result = Range.independentOR(result, pdg1);
		result = Range.independentAND(result, ps1);

		c = result;
		returnResult.range = result;
		
		if (isNonZero(result))
			returnResult.setCriteriaSet(createCString(pdg1, sps, div, pf));
		
		returnResult.printRange();
		
		return returnResult;

	}
	
	public CriteriaResult d(Range ps) {
		CriteriaResult returnResult = new CriteriaResult(name, "d");
		d = Range.lessthan(ps, dPopulationSize);
		returnResult.range = d;
		
		if (isNonZero(d))
			returnResult.setCriteriaSet(new CriteriaSet(name, "D"));

		returnResult.printRange();
		
		return returnResult;
	}
	
	public CriteriaResult e(Range eg3) {
		CriteriaResult returnResult = new CriteriaResult(name, "e");
		e = Range.greaterthanequal(eg3, eExtinctionGenerations);
		returnResult.range = e;
		
		if (isNonZero(e))
			returnResult.setCriteriaSet(new CriteriaSet(name, "E"));
		
		returnResult.printRange();
		
		return returnResult;
	}
	
	private CriteriaSet createAString(Range result, String[] csv, String number, String legend) {
		List<String> criteriaMet = new ArrayList<String>();
		if (isNonZero(result)) {
			for (String selection : csv) {
				try {
					criteriaMet.add("A" + number + legend.charAt(Integer.valueOf(selection)-1));
				} catch (Exception e) {
					continue;
				}
			}
		}
		return new CriteriaSet(name, criteriaMet);	
	}
	
	protected CriteriaSet createAString(Range result, String[] csv, String number) {
		return createAString(result, csv, number, "abcde");
	}
	
	protected CriteriaSet createA3String(Range result, String[] csv) {
		return createAString(result, csv, "3", "bcde");
	}
	
	protected CriteriaSet createBString(String number, Range sf, Range ed, Range ad, Range hd, Range ld, Range sd, Range pd,
			Range ef, Range af, Range lf, Range sef, Range pf) {
		List<String> criteriaMet = new ArrayList<String>();
		
		// DO A STRING
		if (isNonZero(sf))
			criteriaMet.add("B" + number + "a");
		
		// DO B STRING
		if (isNonZero(ed))
			criteriaMet.add("B" + number + "bi");
		
		if (isNonZero(ad))
			criteriaMet.add("B" + number + "bii");
		
		if (isNonZero(hd))
			criteriaMet.add("B" + number + "biii");
		
		if (isNonZero(ld) || isNonZero(sd))
			criteriaMet.add("B" + number + "biv");
		
		if (isNonZero(pd))
			criteriaMet.add("B" + number + "bv");
		
		// DO C STRING
		if (isNonZero(ef))
			criteriaMet.add("B" + number + "ci");
		
		if (isNonZero(af))
			criteriaMet.add("B" + number + "cii");
		
		if (isNonZero(lf) || isNonZero(sef))
			criteriaMet.add("B" + number + "ciii");
		
		if (isNonZero(pf))
			criteriaMet.add("B" + number + "civ");
		
		return new CriteriaSet(name, criteriaMet);
	}

	protected CriteriaSet createCString(Range pdg1, Range sps, Range div, Range pf) {
		List<String> criteriaMet = new ArrayList<String>();
		if (isNonZero(pdg1))
			criteriaMet.add("C1");

		if (isNonZero(sps))
			criteriaMet.add("C2ai");
		
		if (isNonZero(div))
			criteriaMet.add("C2aii");
		
		if (isNonZero(pf))
			criteriaMet.add("C2b");
		
		return new CriteriaSet(name, criteriaMet);
	}
	
	protected boolean isNonZero(Range range) {
		return range != null && !Range.isConstant(range, 0);
	}
	
}
