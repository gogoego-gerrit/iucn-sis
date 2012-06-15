package org.iucn.sis.shared.api.criteriacalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;


public abstract class Classification {
	
	public static final String A_UNKNOWN = "0-1";
	
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
		Range extent = factors.get(Factors.extent);
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
			Range bxa, bxb, bxc;
			
			Range sf = factors.get(Factors.severeFragmentation);
			Range loc = factors.get(Factors.locations);
			loc = Range.lessthanequal(loc, bLocations);
			
			bxa = Range.independentOR(sf, loc);

			Range ed = factors.get(Factors.extentDecline);
			Range ad = factors.get(Factors.areaDecline);
			Range hd = factors.get(Factors.habitatDecline);
			Range ld = factors.get(Factors.locationDecline);
			Range sd = factors.get(Factors.subpopulationDecline);
			Range pd = factors.get(Factors.populationDecline);
			
			bxb = Range.independentOR(ed, ad);
			bxb = Range.independentOR(bxb, hd);
			bxb = Range.independentOR(bxb, ld);
			bxb = Range.independentOR(bxb, sd);
			bxb = Range.independentOR(bxb, pd);

			Range ef = factors.get(Factors.extentFluctuation);
			Range af = factors.get(Factors.areaFluctuation);
			Range lf = factors.get(Factors.locationFluctuation);
			Range sef = factors.get(Factors.subpopulationFluctuation);
			Range pf = factors.get(Factors.populationFluctuation);
			
			bxc = Range.independentOR(ef, af);
			bxc = Range.independentOR(bxc, lf);
			bxc = Range.independentOR(bxc, sef);
			bxc = Range.independentOR(bxc, pf);

			if (isNonZero(bxa) && isNonZero(bxb) && isNonZero(bxc)) {
				Range and = Range.independentAND(and1, bxa);
				and = Range.independentAND(and, bxb);
				and = Range.independentAND(and, bxc);
				
				returnResult.range = and;
				if (isNonZero(returnResult.range))
					returnResult.setCriteriaSet(createBString(number, bxa, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf));

			}
			else if (isNonZero(bxa) && isNonZero(bxb)) {
				Range and = Range.independentAND(and1, bxa);
				and = Range.independentAND(and, bxb);
				
				returnResult.range = and;
				if (isNonZero(returnResult.range))
					returnResult.setCriteriaSet(createBString(number, bxa, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf));
			}
			else if (isNonZero(bxa) && isNonZero(bxc)) {
				Range and = Range.independentAND(and1, bxa);
				and = Range.independentAND(and, bxc);
				
				returnResult.range = and;
				if (isNonZero(returnResult.range))
					returnResult.setCriteriaSet(createBString(number, bxa, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf));
			}
			else if (isNonZero(bxb) && isNonZero(bxc)) {
				Range and = Range.independentAND(and1, bxb);
				and = Range.independentAND(and, bxc);
				
				returnResult.range = and;
				if (isNonZero(returnResult.range))
					returnResult.setCriteriaSet(createBString(number, bxa, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf));
			}
			// NOT ENOUGH DATA
			else {
				returnResult.range = null;
				if (isNonZero(returnResult.range))
					returnResult.setCriteriaSet(createBString(number, bxa, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf));
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
		Range sps = factors.get(Factors.subpopulationSize);
		
		Range C = Range.lessthan(ps, cPopulationSize);

		//Population decline gen X for C1, but must be Observed or Estimated
		Range C1 = factors.get(declineGenFactor);
		C1 = Range.qualify(C1, Range.OBSERVED, Range.ESTIMATED);
		C1 = Range.greaterthanequal(C1, cPopulationDeclineGenerations1);

		//Population decline for C2, but must be Observed, Estimated, Projected or Inferred...
		Range C2 = factors.get(Factors.populationDecline);
		C2 = Range.qualify(C2, Range.OBSERVED, Range.ESTIMATED, Range.PROJECTED, Range.INFERRED);
		
		Range C2ai = Range.lessthanequal(sps, cMaxSubpopulationSize);
		
		Range C2aii = Range.divide(sps, ps);
		C2aii = Range.greaterthanequal(C2aii, cAlotInSubpopulation);

		Range C2b = factors.get(Factors.populationFluctuation);

		Range result = Range.independentOR(C2ai, C2aii); //C2a
		result = Range.independentOR(result, C2b); //C2a or C2b
		result = Range.independentAND(result, C2); //C2 and C2a or C2b
		result = Range.independentOR(result, C1); //C2 or C1
		
		//Must meet the first criteria of C
		result = Range.independentAND(result, C); //C1 or C2 and C

		c = result;
		returnResult.range = result;
		
		if (isNonZero(result))
			returnResult.setCriteriaSet(createCString(C1, C2ai, C2aii, C2b));
		
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
	
	protected CriteriaSet createBString(String number, Range bxa, Range ed, Range ad, Range hd, Range ld, Range sd, Range pd,
			Range ef, Range af, Range lf, Range sef, Range pf) {
		List<String> criteriaMet = new ArrayList<String>();
		
		// DO A STRING
		if (isNonZero(bxa))
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

	protected CriteriaSet createCString(Range C1, Range C2ai, Range C2aii, Range C2b) {
		List<String> criteriaMet = new ArrayList<String>();
		if (isNonZero(C1))
			criteriaMet.add("C1");

		if (isNonZero(C2ai))
			criteriaMet.add("C2ai");
		
		if (isNonZero(C2aii))
			criteriaMet.add("C2aii");
		
		if (isNonZero(C2b))
			criteriaMet.add("C2b");
		
		return new CriteriaSet(name, criteriaMet);
	}
	
	protected boolean isNonZero(Range range) {
		return range != null && !Range.isConstant(range, 0);
	}
	
}
