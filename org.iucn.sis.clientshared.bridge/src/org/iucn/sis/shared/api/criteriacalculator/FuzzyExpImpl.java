package org.iucn.sis.shared.api.criteriacalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.BooleanRangePrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.RangePrimitiveField;

/**
 * Implements the Fuzzy Expert System
 * 
 * @author liz.schwartz
 * 
 */
public class FuzzyExpImpl {

	static class AnalysisResult {
		public ArrayList<CriteriaResult> results;
		public CriteriaResult CRResult;
		public CriteriaResult ENResult;
		public CriteriaResult VUResult;
		public String criteriaStringCR;
		public String criteriaStringEN;
		public String criteriaStringVU;

		public AnalysisResult() {
			results = new ArrayList<CriteriaResult>();
			criteriaStringCR = "";
			criteriaStringEN = "";
			criteriaStringVU = "";
		}
		
		public void addResult(CriteriaResult result) {
			results.add(result);
		}
		
		public void addCRResult(CriteriaResult result) {
			results.add(result);
			CRResult = result;
		}
		
		public void addENResult(CriteriaResult result) {
			results.add(result);
			ENResult = result;
		}
		
		public void addVUResult(CriteriaResult result) {
			results.add(result);
			VUResult = result;
		}

	}
	
	public static boolean VERBOSE = false;

	boolean INDIVIDUALDT = false;

	private CR critical;
	private VU vulnerable;
	private EN endangered;
	
	private static final double dt = 0.5;
	private static final double rt = 0.5;
	
	public static final int xDD = 0;
	public static final int xCR = 100;
	public static final int xEN = 200;
	public static final int xVU = 300;
	public static final int xLR = 400;

	public FuzzyExpImpl() {
		critical = new CR();
		vulnerable = new VU();
		endangered = new EN();
	}
	
	/**
	 * Does the analysis of the current assessment. Returns an ExpertResult if
	 * there is enough data, null otherwise.
	 * 
	 * @param assessment
	 *            TODO
	 * 
	 * @return
	 */
	public ExpertResult doAnalysis(Assessment assessment) {
		// GET ALL RANGES FOR SPECIFIC CRITERIA
		AnalysisResult analysisA = doAnalysisA(assessment);
		AnalysisResult analysisB = doAnalysisB(assessment);
		AnalysisResult analysisC = doAnalysisC(assessment);
		AnalysisResult analysisD = doAnalysisD(assessment);
		AnalysisResult analysisE = doAnalysisE(assessment);

		// DO FINAL RANGES FOR EACH CLASSIFICATION
		CriteriaResult finalResultCR = finalResult(ResultCategory.CR, 
				analysisA.CRResult,
				analysisB.CRResult, analysisC.CRResult,
				analysisD.CRResult, analysisE.CRResult);
		CriteriaResult finalResultEN = finalResult(ResultCategory.EN, 
				analysisA.ENResult,
				analysisB.ENResult, analysisC.ENResult,
				analysisD.ENResult, analysisE.ENResult);
		CriteriaResult finalResultVU = finalResult(ResultCategory.VU, 
				analysisA.VUResult,
				analysisB.VUResult, analysisC.VUResult,
				analysisD.VUResult, analysisE.VUResult);

		// DO DT IF DIDN'T DO INDIVIDUALLY
		if (!INDIVIDUALDT) {
			finalResultCR.range = Range.dt(finalResultCR.range, dt);
			finalResultEN.range = Range.dt(finalResultEN.range, dt);
			finalResultVU.range = Range.dt(finalResultVU.range, dt);
		}

		println("Final Results...");
		finalResultCR.printRange();
		finalResultEN.printRange();
		finalResultVU.printRange();
		// GET RESULT WITH SPECIFIC RT

		println("FINAL CR ----- {0}", finalResultCR.getResultString());
		println("FINAL EN ----- {0}", finalResultEN.getResultString());
		println("FINAL VU ----- {0}", finalResultVU.getResultString());

		final ExpertResult result;
		if (finalResultCR.range != null && finalResultEN.range != null && finalResultVU.range != null) {
			result = calculateResult(finalResultCR.range, finalResultEN.range, finalResultVU.range, assessment);
			
			String[] criterias = getCriterias(analysisA, analysisB, analysisC, analysisD, analysisE).split("-");
			result.setCriteriaMet(calculateCriteriaMet(result, finalResultCR, finalResultEN, finalResultVU));
			result.setCriteriaCR(finalResultCR.getCriteriaSet());
			result.setCriteriaEN(finalResultEN.getCriteriaSet());
			result.setCriteriaVU(finalResultVU.getCriteriaSet());
			
			result.setNotEnoughData(criterias[0]);
			// result.setEnoughData(criterias[1]);

		} else if (finalResultEN.range != null && finalResultVU.range != null) {
			result = calculateResult(newPretendRange(), finalResultEN.range, finalResultVU.range, assessment);
			
			String[] criterias = getCriterias(analysisA, analysisB, analysisC, analysisD, analysisE).split("-");
			result.setCriteriaMet(calculateCriteriaMet(result, finalResultCR, finalResultEN, finalResultVU));
			result.setCriteriaCR(new CriteriaSet(ResultCategory.CR));
			result.setCriteriaEN(finalResultEN.getCriteriaSet());
			result.setCriteriaVU(finalResultVU.getCriteriaSet());
			
			result.setNotEnoughData(criterias[0]);
		}

		else if (finalResultVU.range != null) {
			result = calculateResult(newPretendRange(), newPretendRange(), finalResultVU.range, assessment);
			
			String[] criterias = getCriterias(analysisA, analysisB, analysisC, analysisD, analysisE).split("-");
			result.setCriteriaMet(calculateCriteriaMet(result, finalResultCR, finalResultEN, finalResultVU));
			result.setCriteriaCR(new CriteriaSet(ResultCategory.CR));
			result.setCriteriaEN(new CriteriaSet(ResultCategory.EN));
			result.setCriteriaVU(finalResultVU.getCriteriaSet());
			
			result.setNotEnoughData(criterias[0]);
			// result.setEnoughData(criterias[1]);
		}

		else {
			result = new ExpertResult();
			result.setNotEnoughData(null);
			result.setCriteriaMet(new CriteriaSet(ResultCategory.DD));
			result.setResult(ResultCategory.DD);
			result.setLeft(-1);
			result.setRight(-1);
			result.setBest(-1);
		}

		if (result != null)
			println("Not enough data: {0}", result.getNotEnoughData());
		
		return result;
	}

	private CriteriaSet calculateCriteriaMet(ExpertResult result, CriteriaResult cr, CriteriaResult en, CriteriaResult vu) {
		CriteriaSet criteriaSet = new CriteriaSet(ResultCategory.DD);

		if (result.getResult() == ResultCategory.CR)
			criteriaSet = cr.getCriteriaSet();
		else if (result.getResult() == ResultCategory.EN)
			criteriaSet = en.getCriteriaSet();
		else if (result.getResult() == ResultCategory.VU)
			criteriaSet = vu.getCriteriaSet();
		
		return criteriaSet;
	}

	private ExpertResult calculateResult(Range cr, Range en, Range vu, Assessment assessment) {
		// CREATE HIGH, MID, LOW LINES in that order
		ArrayList<Line> lines = createLines(cr, en, vu);
		
		ExpertResult result = new ExpertResult();
		result.setLeft(lines.get(0).x(rt));
		result.setRight(lines.get(2).x(rt));
		result.setBest(lines.get(1).x(rt));
		
		for (Line line : lines)
			println("Calculated {0}line ({1},{2}), ({3},{4})", 
					line.name, line.x1, line.y1, line.x2, line.y2);
		
		if (result.getBest() <= xCR) {
			result.setResult(ResultCategory.CR);
		} else if (result.getBest() <= xEN) {
			result.setResult(ResultCategory.EN);
		} else if (result.getBest() <= xVU) {
			result.setResult(ResultCategory.VU);
		} else
			result.setResult(ResultCategory.LR);
		
		println("Calculated result: {0}, {1}, {2}: {3}", result.getLeft(), 
				result.getBest(), result.getRight(),  result.getResult());

		return result;
	}

	private ArrayList<Line> createLines(final Range cr, final Range en, final Range vu) {
		final int x = 0;
		final double y = 0;
		final int yfinal = 1;
		Line lineLow;
		Line lineMid;
		Line lineHigh;

		// GET LOW, MID, HIGH FOR ALL RANGES
		double lowcr = cr.getLow();
		double highcr = cr.getHigh();
		double midcr = (lowcr + highcr) / 2;
		double lowen = en.getLow();
		double highen = en.getHigh();
		double miden = (lowen + highen) / 2;
		double lowvu = vu.getLow();
		double highvu = vu.getHigh();
		double midvu = (lowvu + highvu) / 2;
		
		println("Line for CR: Low {0}, High {1}, Mid {2}", lowcr, highcr, midcr);
		println("Line for EN: Low {0}, High {1}, Mid {2}", lowen, highen, miden);
		println("Line for VU: Low {0}, High {1}, Mid {2}", lowvu, highvu, midvu);

		if (highcr >= rt) {
			lineHigh = new Line("high", x, xCR, y, highcr);
		} else if (highen >= rt) {
			lineHigh = new Line("high", xCR, xEN, highcr, highen);
		} else if (highvu >= rt) {
			lineHigh = new Line("high", xEN, xVU, highen, highvu);
		} else
			lineHigh = new Line("high", xVU, xLR, highvu, yfinal);

		if (midcr >= rt) {
			lineMid = new Line("mid", x, xCR, y, midcr);
		} else if (miden >= rt) {
			lineMid = new Line("mid", xCR, xEN, midcr, miden);
		} else if (midvu >= rt) {
			lineMid = new Line("mid", xEN, xVU, miden, midvu);
		} else
			lineMid = new Line("mid", xVU, xLR, midvu, yfinal);

		if (lowcr >= rt) {
			lineLow = new Line("low", x, xCR, y, lowcr);
		} else if (lowen >= rt) {
			lineLow = new Line("low", xCR, xEN, lowcr, lowen);
		} else if (lowvu >= rt) {
			lineLow = new Line("low", xEN, xVU, lowen, lowvu);
		} else
			lineLow = new Line("low", xVU, xLR, lowvu, yfinal);

		ArrayList<Line> returnVals = new ArrayList<Line>();
		returnVals.add(lineHigh);
		returnVals.add(lineMid);
		returnVals.add(lineLow);
		return returnVals;
	}

	@SuppressWarnings("unchecked")
	private Range createRangeFromAssessment(Assessment assessment, String factor) {
		Field factorField = assessment.getField(factor);
		Range result = null;
		
		if (factorField != null) {
			for (PrimitiveField<?> curPrim : factorField.getPrimitiveField()) {
				if (curPrim instanceof RangePrimitiveField) {
					result = new Range(((PrimitiveField<String>)curPrim).getValue());
					break;
				}
				else if (curPrim instanceof BooleanRangePrimitiveField) {
					String value = ((PrimitiveField<String>)curPrim).getValue();
					//Disregard "unknown" as of ticket #57
					if (!BooleanRangePrimitiveField.UNKNOWN.equals(value))
						result = new Range(value);
					break;
				}
			}
			
			PrimitiveField<?> qualifier = factorField.getPrimitiveField("qualifier");
			if (result != null && qualifier != null && qualifier instanceof ForeignKeyPrimitiveField)
				result.setQualifier(((ForeignKeyPrimitiveField)qualifier).getValue());
			
			PrimitiveField<?> direction = factorField.getPrimitiveField("direction");
			if (direction != null && direction instanceof ForeignKeyPrimitiveField)
				if (((ForeignKeyPrimitiveField)direction).getValue().intValue() != 1)
					return null;
		}
		
		return result;
	}

	private String createStringFromAssessment(Assessment assessment, String factor) {
		Field factorField = assessment.getField(factor);
		StringBuilder result = new StringBuilder("");
		if (factorField != null) {
			for (PrimitiveField<?> curPrim : factorField.getPrimitiveField() ) {
				/*
				 * We don't care about details, only values.
				 * TODO: replace this with a check for the "value" 
				 * field instead, and only use fields named "value".
				 */
				if ("detail".equals(curPrim.getName()))
					continue;
				
				if (curPrim instanceof ForeignKeyListPrimitiveField) {
					List<Integer> fkList = ((ForeignKeyListPrimitiveField)curPrim).getValue();
					for (Integer fk : fkList) {
						result.append(",");
						result.append(fk);
					}
				}
			}
		}
		
		String value = result.toString();
		
		return "".equals(value) ? value : value.substring(1);
	}	
	
	private Range newPretendRange() {
		Range pretend = new Range();
		pretend.setHigh(0);
		pretend.setHighBest(0);
		pretend.setLow(0);
		pretend.setLowBest(0);
		
		return pretend;
	}
	
	private Range analyzeFirstFactor(String[] factors, Assessment assessment) {
		Range primary = createRangeFromAssessment(assessment, factors[0]);
		if (INDIVIDUALDT)
			primary = Range.dt(primary, dt);
		return primary;
	}
	
	private HashMap<String, Range> analyzeFactors(String[] factors, Assessment assessment) {
		final HashMap<String, Range> map = new HashMap<String, Range>();
		for (String factor : factors) {
			Range primary = createRangeFromAssessment(assessment, factor);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			map.put(factor, primary);
		}
		return map;
	}

	/**
	 * Computes the A criteria for the assessment object for critically
	 * endangered, endangered, and vulnerable.
	 * 
	 * @param assessment
	 * @return arraylist of Ranges -- first all CR, then EN, then VU, then the
	 *         final result of all three
	 */
	private AnalysisResult doAnalysisA(final Assessment assessment) {
		AnalysisResult analysis = new AnalysisResult();

		// GET ALL ENTERED INFORMATION FOR A1
		String populationReductionPastBasis = 
			createStringFromAssessment(assessment, Factors.populationReductionPastBasis);
		
		CriteriaResult resultCR1 = critical.a1(
			analyzeFactors(critical.factorsA1, assessment), populationReductionPastBasis);

		CriteriaResult resultEN1 = endangered.a1(
			analyzeFactors(endangered.factorsA1, assessment), populationReductionPastBasis);

		CriteriaResult resultVU1 = vulnerable.a1(
			analyzeFactors(vulnerable.factorsA1, assessment), populationReductionPastBasis);

		// GET ALL ENTERED INFORMATION FOR A2
		CriteriaResult resultCR2 = critical.a2(
			analyzeFactors(critical.factorsA2, assessment), 
			populationReductionPastBasis
		);

		CriteriaResult resultEN2 = endangered.a2(
			analyzeFactors(endangered.factorsA2, assessment), 
			populationReductionPastBasis
		);

		CriteriaResult resultVU2 = vulnerable.a2(
			analyzeFactors(vulnerable.factorsA2, assessment), 
			populationReductionPastBasis
		);

		// GET ALL ENTERED INFORMATION FOR A3
		String populationReductionFutureBasis = 
			createStringFromAssessment(assessment, Factors.populationReductionFutureBasis);
		CriteriaResult resultCR3 = critical.a3(
			analyzeFirstFactor(critical.factorsA3, assessment), 
			populationReductionFutureBasis
		);

		CriteriaResult resultEN3 = endangered.a3(
			analyzeFirstFactor(endangered.factorsA3, assessment), 
			populationReductionFutureBasis);

		CriteriaResult resultVU3 = vulnerable.a3(
			analyzeFirstFactor(vulnerable.factorsA3, assessment), 
			populationReductionFutureBasis);

		// GET ALL ENTERED INFORMATION FOR A4
		String populationReductionEitherBasis = 
			createStringFromAssessment(assessment, Factors.populationReductionEitherBasis);
		CriteriaResult resultCR4 = critical.a4(
			analyzeFactors(critical.factorsA4, assessment), 
			populationReductionEitherBasis);

		CriteriaResult resultEN4 = endangered.a4(
			analyzeFactors(endangered.factorsA4, assessment), 
			populationReductionEitherBasis
		);

		CriteriaResult resultVU4 = vulnerable.a4(
			analyzeFactors(vulnerable.factorsA4, assessment), 
			populationReductionEitherBasis
		);

		// GET FINAL INFO FOR A
		CriteriaResult crA = getFinalA(ResultCategory.CR, resultCR1, resultCR2, resultCR3, resultCR4);
		CriteriaResult enA = getFinalA(ResultCategory.EN, resultEN1, resultEN2, resultEN3, resultEN4);
		CriteriaResult vuA = getFinalA(ResultCategory.VU, resultVU1, resultVU2, resultVU3, resultVU4);

		analysis.addResult(resultCR1);
		analysis.addResult(resultCR2);
		analysis.addResult(resultCR3);
		analysis.addResult(resultCR4);

		analysis.addResult(resultEN1);
		analysis.addResult(resultEN2);
		analysis.addResult(resultEN3);
		analysis.addResult(resultEN4);

		analysis.addResult(resultVU1);
		analysis.addResult(resultVU2);
		analysis.addResult(resultVU3);
		analysis.addResult(resultVU4);

		analysis.addCRResult(crA);
		analysis.addENResult(enA);
		analysis.addVUResult(vuA);
		return analysis;
	}

	private AnalysisResult doAnalysisB(final Assessment assessment) {
		AnalysisResult analysis = new AnalysisResult();

		// GET ALL ENTERED INFORMATION FOR B1
		CriteriaResult resultCR1 = critical.b1(analyzeFactors(critical.factorsB1, assessment));
		CriteriaResult resultEN1 = endangered.b1(analyzeFactors(endangered.factorsB1, assessment));
		CriteriaResult resultVU1 = vulnerable.b1(analyzeFactors(vulnerable.factorsB1, assessment));

		// GET ALL ENTERED INFORMATION FOR B2
		CriteriaResult resultCR2 = critical.b2(analyzeFactors(critical.factorsB2, assessment));
		CriteriaResult resultEN2 = endangered.b2(analyzeFactors(endangered.factorsB2, assessment));
		CriteriaResult resultVU2 = vulnerable.b2(analyzeFactors(vulnerable.factorsB2, assessment));

		CriteriaResult finalCRb = getFinalSimple(ResultCategory.CR, "B", resultCR1, resultCR2);
		CriteriaResult finalENb = getFinalSimple(ResultCategory.EN, "B", resultEN1, resultEN2);
		CriteriaResult finalVUb = getFinalSimple(ResultCategory.VU, "B", resultVU1, resultVU2);

		analysis.addResult(resultCR1);
		analysis.addResult(resultCR2);

		analysis.addResult(resultEN1);
		analysis.addResult(resultEN2);

		analysis.addResult(resultVU1);
		analysis.addResult(resultVU2);

		analysis.addCRResult(finalCRb);
		analysis.addENResult(finalENb);
		analysis.addVUResult(finalVUb);
		return analysis;
	}

	private AnalysisResult doAnalysisC(final Assessment assessment) {
		AnalysisResult analysis = new AnalysisResult();

		// GET ALL ENTERED INFORMATION FOR C1
		CriteriaResult resultCR = critical.c(analyzeFactors(critical.factorsC, assessment));
		CriteriaResult resultEN = endangered.c(analyzeFactors(endangered.factorsC, assessment));
		CriteriaResult resultVU = vulnerable.c(analyzeFactors(vulnerable.factorsC, assessment));

		// GET ALL ENTERED INFORMATION FOR C2
		//CriteriaResult resultEN2 = endangered.c2(analyzeFactors(endangered.factorsC2, assessment));
		//CriteriaResult resultVU2 = vulnerable.c2(analyzeFactors(vulnerable.factorsC2, assessment));
		
		CriteriaResult finalCR = resultCR;
		CriteriaResult finalEN = resultEN;//getFinalC("EN", resultEN1, resultEN2);
		CriteriaResult finalVU = resultVU;//getFinalC("VU", resultVU1, resultVU2);

		analysis.addResult(resultCR);

		analysis.addResult(resultEN);
		//analysis.addResult(resultEN2);

		analysis.addResult(resultVU);
		//analysis.addResult(resultVU2);

		analysis.addCRResult(finalCR);
		analysis.addENResult(finalEN);
		analysis.addVUResult(finalVU);
		return analysis;
	}

	private AnalysisResult doAnalysisD(final Assessment assessment) {
		AnalysisResult analysis = new AnalysisResult();

		// GET ALL ENTERED INFORMATION FOR D1
		CriteriaResult resultCR = critical.d(analyzeFirstFactor(critical.factorsD, assessment));
		CriteriaResult resultEN = endangered.d(analyzeFirstFactor(endangered.factorsD, assessment));
		CriteriaResult resultVU1 = vulnerable.d1(analyzeFirstFactor(vulnerable.factorsD1, assessment));

		// GET ALL ENTERED INFORMATION FOR D2
		CriteriaResult resultVU2 = vulnerable.d2(analyzeFactors(vulnerable.factorsD2, assessment));

		CriteriaResult finalVU = getFinalSimple(ResultCategory.VU, "D", resultVU1, resultVU2);

		analysis.addResult(resultCR);
		analysis.addResult(resultEN);
		analysis.addResult(resultVU1);
		analysis.addResult(resultVU2);
		analysis.addCRResult(resultCR);
		analysis.addENResult(resultEN);
		analysis.addVUResult(finalVU);

		return analysis;
	}

	private AnalysisResult doAnalysisE(final Assessment assessment) {
		AnalysisResult analysis = new AnalysisResult();

		// GET ALL ENTERED INFORMATION FOR D1
		analysis.addCRResult(critical.e(analyzeFirstFactor(critical.factorsE, assessment)));
		analysis.addENResult(endangered.e(analyzeFirstFactor(endangered.factorsE, assessment)));
		analysis.addVUResult(vulnerable.e(analyzeFirstFactor(vulnerable.factorsE, assessment)));

		return analysis;
	}

	private CriteriaResult finalResult(ResultCategory category, CriteriaResult a, CriteriaResult b, CriteriaResult c, CriteriaResult d,
			CriteriaResult e) {
		return getFinalA(category, a, b, c, d, e);
	}
	
	private void parseCriteria(StringBuilder noData, StringBuilder data, int sizeCR, int sizeEN, int sizeVU, String append, ArrayList<CriteriaResult> list) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == null) {

				if (i < sizeCR) {
					int j = i + 1;
					noData.append("CR" + append + j + ",");
				} else if (i < sizeCR + sizeEN) {
					int j = i + 1 - sizeCR;
					noData.append("EN" + append + j + ",");
				} else if (i < sizeCR + sizeEN + sizeVU) {
					int j = i + 1 - sizeCR - sizeEN;
					noData.append("VU" + append + j + ",");
				}
			} else {
				/*
				 * Note: previous code always used CriteriaA's sizing 
				 * method here, but I think that was just an error due 
				 * to copy-paste code, not intent.
				 * 
				 * I note this in case I am wrong, so it can be reverted.
				 */
				if (i < sizeCR) {
					int j = i + 1;
					data.append("CR" + append + j + ",");
				} else if (i < sizeCR + sizeEN) {
					int j = i + 1 - sizeCR;
					data.append("EN" + append + j + ",");
				} else if (i < sizeCR + sizeEN + sizeVU) {
					int j = i + 1 - sizeCR - sizeEN;
					data.append("VU" + append + j + ",");
				}
			}

		}
	}

	private String getCriterias(AnalysisResult resultsA, AnalysisResult resultsB, AnalysisResult resultsC, AnalysisResult resultsD,
			AnalysisResult resultsE) {
		// set result with all criteria that couldn't be calculated
		StringBuilder noData = new StringBuilder();
		StringBuilder data = new StringBuilder();

		parseCriteria(noData, data, critical.criteriaA, endangered.criteriaA, vulnerable.criteriaA, "a", resultsA.results);
		parseCriteria(noData, data, critical.criteriaB, endangered.criteriaB, vulnerable.criteriaB, "b", resultsB.results);
		parseCriteria(noData, data, critical.criteriaC, endangered.criteriaC, vulnerable.criteriaC, "c", resultsC.results);
		parseCriteria(noData, data, critical.criteriaD, endangered.criteriaD, vulnerable.criteriaD, "d", resultsD.results);
		parseCriteria(noData, data, critical.criteriaE, endangered.criteriaE, vulnerable.criteriaE, "e", resultsE.results);

		return noData.toString() + "-" + data.toString();
	}

	/**
	 * Accepts 4 different ranges, and does a dependent or between a and b, and
	 * then independent or on the rest of the ranges. If a range is null it
	 * ignores it.
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @return
	 */
	private CriteriaResult getFinalA(ResultCategory category, CriteriaResult a, CriteriaResult b, CriteriaResult... more) {
		//Do combining of criteria
		List<CriteriaSet> sets = new ArrayList<CriteriaSet>();
		sets.add(a.getCriteriaSet());
		sets.add(b.getCriteriaSet());
		for (CriteriaResult result : more)
			sets.add(result.getCriteriaSet());
		
		// DO COMBINING OF RANGES		
		Range result = Range.dependentOR(a.range, b.range);
		for (CriteriaResult c : more)
			result = Range.independentOR(result, c.range);
		
		CriteriaResult analysis = new CriteriaResult(category, "A");
		analysis.range = result;
		analysis.getCriteriaSet().merge(sets);
		
		analysis.printRange();
		
		return analysis;
	}

	private CriteriaResult getFinalSimple(ResultCategory category, String criterion, CriteriaResult a, CriteriaResult b) {
		Range result = Range.independentOR(a.range, b.range);

		CriteriaResult analysis = new CriteriaResult(category, criterion);
		analysis.getCriteriaSet().merge(a.getCriteriaSet(), b.getCriteriaSet());
		analysis.range = result;
		
		analysis.printRange();
		
		return analysis;
	}
	
	private static void println(String template, Object... obj) {
		if (VERBOSE)
			Debug.println(template, obj);
	}

}
