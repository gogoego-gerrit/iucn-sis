package org.iucn.sis.shared.api.criteriacalculator;

import java.util.HashMap;

import org.iucn.sis.shared.api.debug.Debug;


public abstract class Classification {
	
	protected final String name;
	
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
	
	public Classification(String name) {
		this.name = name;
	}
	
	protected void println(String template, Object... args) {
		if (FuzzyExpImpl.VERBOSE)
			Debug.println(template, args);
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
		returnResult.resultString = createAString(result, csv, "1");
		if (returnResult.resultString.equals(""))
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
		returnResult.resultString = createAString(result, csv, "2");
		if (returnResult.resultString.equals(""))
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
		returnResult.resultString = createA3String(result, csv);
		if (returnResult.resultString.equalsIgnoreCase(""))
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
		returnResult.resultString = createAString(result, csv, "4");
		if (returnResult.resultString.equalsIgnoreCase(""))
			returnResult.range = null;
		
		returnResult.printRange();
		
		return returnResult;
	}
	
	public CriteriaResult b1(HashMap<String, Range> factors) {
		CriteriaResult returnResult = new CriteriaResult(name, "b1");
		Range result = null;
		returnResult.resultString = "";

		Range extent = (Range) factors.get(Factors.extent);
		Range and1 = Range.lessthan(extent, bExtent);

		// EXTENT < 100
		if (and1 != null && !Range.isConstant(and1, 0)) {

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
				b1 = and;
				returnResult.range = and;
				if (returnResult.range != null && !Range.isConstant(returnResult.range, 0))
					returnResult.resultString = createBString("1", sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf);

			} else if (or1 != null && !Range.isConstant(or1, 0)) {

				if (or2 != null && !Range.isConstant(or2, 0)) {
					Range and = Range.independentAND(and1, or1);
					and = Range.independentAND(and, or2);
					b1 = and;
					returnResult.range = and;
					if (returnResult.range != null && !Range.isConstant(returnResult.range, 0))
						returnResult.resultString = createBString("1", sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf);
				} else if (or3 != null && !Range.isConstant(or3, 0)) {
					Range and = Range.independentAND(and1, or1);
					and = Range.independentAND(and, or3);
					b1 = and;
					returnResult.range = and;
					if (returnResult.range != null && !Range.isConstant(returnResult.range, 0))
						returnResult.resultString = createBString("1", sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf);
				}

				// NOT ENOUGH DATA
				else {
					b1 = result;
					returnResult.range = result;
					if (returnResult.range != null && !Range.isConstant(returnResult.range, 0))
						returnResult.resultString = createBString("1", sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf);
				}
			}

			else if ((or2 != null && !Range.isConstant(or2, 0)) && (or3 != null && !Range.isConstant(or3, 0))) {
				Range and = Range.independentAND(and1, or2);
				and = Range.independentAND(and, or3);
				b1 = and;
				returnResult.range = and;
				if (returnResult.range != null && !Range.isConstant(returnResult.range, 0))
					returnResult.resultString = createBString("1", sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf);
			}

			// NOT ENOUGH DATA
			else {
				b1 = result;
				returnResult.range = result;
				if (returnResult.range != null && !Range.isConstant(returnResult.range, 0))
					returnResult.resultString = createBString("1", sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf);
			}

		}
		// EXTENT >= 100
		else {
			b1 = result;
			returnResult.range = result;
		}
		
		returnResult.printRange();
		
		return returnResult;
	}
	
	public CriteriaResult b2(HashMap<String, Range> factors) {
		CriteriaResult returnResult = new CriteriaResult(name, "b2");
		Range result = null;
		returnResult.resultString = "";

		Range area = (Range) factors.get(Factors.area);
		Range and1 = Range.lessthan(area, bArea);

		// EXTENT < 100
		if (and1 != null && !Range.isConstant(and1, 0)) {

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
				b2 = and;
				returnResult.range = and;
				if (returnResult.range != null && !Range.isConstant(returnResult.range, 0))
					returnResult.resultString = createBString("2", sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf);

			} else if (or1 != null && !Range.isConstant(or1, 0)) {

				if (or2 != null && !Range.isConstant(or2, 0)) {
					Range and = Range.independentAND(and1, or1);
					and = Range.independentAND(and, or2);
					b2 = and;
					returnResult.range = and;
					if (returnResult.range != null && !Range.isConstant(returnResult.range, 0))
						returnResult.resultString = createBString("2", sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf);
					
				} else if (or3 != null && !Range.isConstant(or3, 0)) {
					Range and = Range.independentAND(and1, or1);
					and = Range.independentAND(and, or3);
					b2 = and;
					returnResult.range = and;
					if (returnResult.range != null && !Range.isConstant(returnResult.range, 0))
						returnResult.resultString = createBString("2", sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf);
					
				}

				// NOT ENOUGH DATA
				else {
					b2 = result;
					if (returnResult.range != null && !Range.isConstant(returnResult.range, 0))
						returnResult.resultString = createBString("2", sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf);
				
				}
			}

			else if ((or2 != null && !Range.isConstant(or2, 0)) && (or3 != null && !Range.isConstant(or3, 0))) {
				Range and = Range.independentAND(and1, or2);
				and = Range.independentAND(and, or3);
				b2 = and;
				returnResult.range = and;
				if (returnResult.range != null && !Range.isConstant(returnResult.range, 0))
					returnResult.resultString = createBString("2", sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf);
			}

			// NOT ENOUGH DATA
			else {
				b2 = result;
				returnResult.range = result;
				if (returnResult.range != null && !Range.isConstant(returnResult.range, 0))
					returnResult.resultString = createBString("2", sf, ed, ad, hd, ld, sd, pd, ef, af, lf, sef, pf);
			}

		}
		// EXTENT >= 100
		else {
			b2 = result;
			returnResult.range = result;
		}
		
		returnResult.printRange();
		
		return returnResult;
	}
	
	public abstract CriteriaResult c(HashMap<String, Range> factors);
	
	public CriteriaResult c(HashMap<String, Range> factors, String declineGenFactor) {
		CriteriaResult returnResult = new CriteriaResult(name, "c");
		returnResult.resultString = "";

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
		if (result != null && (!Range.isConstant(result, 0))) {
			returnResult.resultString = createCString(pdg1, sps, div, pf);
		}
		
		returnResult.printRange();
		
		return returnResult;

	}
	
	public CriteriaResult d(Range ps) {
		CriteriaResult returnResult = new CriteriaResult(name, "d");
		d = Range.lessthan(ps, dPopulationSize);
		returnResult.range = d;

		if ((d != null) && (!(Range.isConstant(d, 0)))) {
			returnResult.resultString = "D";
		}
		
		returnResult.printRange();
		
		return returnResult;
	}
	
	public CriteriaResult e(Range eg3) {
		CriteriaResult returnResult = new CriteriaResult(name, "e");
		e = Range.greaterthanequal(eg3, eExtinctionGenerations);
		returnResult.range = e;
		if ((e != null) && (!(Range.isConstant(e, 0)))) {
			returnResult.resultString = "E";
		}
		
		returnResult.printRange();
		
		return returnResult;
	}
	
	protected String createAString(Range result, String[] csv, String number) {
		boolean stringSet = false;
		String resultString = "";
		if ((result != null) && (!(Range.isConstant(result, 0)))) {
			for (int i = 0; i < csv.length; i++) {
				if (csv[i].equals("1")) {
					if (stringSet)
						resultString += "a";
					else
						resultString = "A" + number + "a";
					stringSet = true;
				} else if (csv[i].equals("2")) {
					if (stringSet)
						resultString += "b";
					else
						resultString = "A" + number + "b";
					stringSet = true;
				} else if (csv[i].equals("3")) {
					if (stringSet)
						resultString += "c";
					else
						resultString = "A" + number + "c";
					stringSet = true;
				} else if (csv[i].equals("4")) {
					if (stringSet)
						resultString += "d";
					else
						resultString = "A" + number + "d";
					stringSet = true;
				} else if (csv[i].equals("5")) {
					if (stringSet)
						resultString += "e";
					else
						resultString = "A" + number + "e";
					stringSet = true;
				}
			}
		}

		return resultString;
	}
	
	protected String createA3String(Range result, String[] csv) {
		boolean stringSet = false;
		String resultString = "";
		if ((result != null) && (!(Range.isConstant(result, 0)))) {
			for (int i = 0; i < csv.length; i++) {
				if (csv[i].equals("1")) {
					if (stringSet)
						resultString += "b";
					else
						resultString = "A" + 3 + "b";
				} else if (csv[i].equals("2")) {
					if (stringSet)
						resultString += "c";
					else
						resultString = "A" + 3 + "c";
					stringSet = true;
				} else if (csv[i].equals("3")) {
					if (stringSet)
						resultString += "d";
					else
						resultString = "A" + 3 + "d";
					stringSet = true;
				} else if (csv[i].equals("4")) {
					if (stringSet)
						resultString += "e";
					else
						resultString = "A" + 3 + "e";
					stringSet = true;
				}
			}
		}

		return resultString;
	}
	
	protected String createBString(String number, Range sf, Range ed, Range ad, Range hd, Range ld, Range sd, Range pd,
			Range ef, Range af, Range lf, Range sef, Range pf) {
		String returnString = "";
		String aString = "";
		String bString = "";
		String cString = "";
		boolean stringStarted = false;
		boolean bStarted = false;
		boolean cStarted = false;

		// DO C STRING
		if ((ef != null) && (!(Range.isConstant(ef, 0)))) {
			if (cStarted) {
				cString += ",i";
			} else
				cString += "i";
			cStarted = true;
		}
		if ((af != null) && (!(Range.isConstant(af, 0)))) {
			if (cStarted) {
				cString += ",ii";
			} else
				cString += "ii";
			cStarted = true;
		}
		if ((lf != null) && (!(Range.isConstant(lf, 0))) || ((sef != null) && (!(Range.isConstant(sef, 0))))) {
			if (cStarted) {
				cString += ",iii";
			} else
				cString += "iii";
			cStarted = true;
		}
		if ((pf != null) && (!(Range.isConstant(pf, 0)))) {
			if (cStarted) {
				cString += ",iv";
			} else
				cString += "iv";
			cStarted = true;
		}
		if (cStarted) {
			cString = "c(" + cString + ")";
		}

		// DO B STRING
		if ((ed != null) && (!(Range.isConstant(ed, 0)))) {
			if (bStarted) {
				bString += ",i";
			} else
				bString += "i";
			bStarted = true;
		}
		if ((ad != null) && (!(Range.isConstant(ad, 0)))) {
			if (bStarted) {
				bString += ",ii";
			} else
				bString += "ii";
			bStarted = true;
		}
		if ((hd != null) && (!(Range.isConstant(hd, 0)))) {
			if (bStarted) {
				bString += ",iii";
			} else
				bString += "iii";
			bStarted = true;
		}
		if ((ld != null) && (!(Range.isConstant(ld, 0))) || ((sd != null) && (!(Range.isConstant(sd, 0))))) {
			if (bStarted) {
				bString += ",iv";
			} else
				bString += "iv";
			bStarted = true;
		}
		if ((pd != null) && (!(Range.isConstant(pd, 0)))) {
			if (bStarted) {
				bString += ",v";
			} else
				bString += "v";
			bStarted = true;
		}
		if (bStarted) {
			bString = "b(" + bString + ")";
		}

		// DO A STRING
		if ((sf != null) || (!(Range.isConstant(sf, 0)))) {
			aString = "a";
			stringStarted = true;
		}

		if (stringStarted || bStarted || cStarted) {
			returnString = "B" + number + aString + bString + cString;
		}

		return returnString;
	}

	protected String createCString(Range pdg1, Range sps, Range div, Range pf) {
		String returnString = "";
		String string1 = "";
		String stringa = "";
		String stringb = "";

		if (pdg1 != null && (!Range.isConstant(pdg1, 0))) {
			string1 = "1";
		}
		if (sps != null && (!Range.isConstant(sps, 0))) {
			stringa = "i";
		}
		if (div != null && (!Range.isConstant(div, 0))) {
			if (stringa.equals(""))
				stringa = "ii";
			else
				stringa += ",ii";
		}
		if (!stringa.equals(""))
			stringa = "a(" + stringa + ")";
		if (pf != null && (!Range.isConstant(pf, 0))) {
			stringb = "b";
		}

		if (!(stringa.equals("") && stringb.equals(""))) {
			stringa = "2" + stringa + stringb;

			if (string1.equals("")) {
				returnString = "C" + stringa;
			} else
				returnString = "C" + string1 + "+" + stringa;
		} else if (!(string1.equals("")))
			returnString = "C" + string1;

		return returnString;
	}
	
}
