package org.iucn.sis.shared.api.criteriacalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.RangePrimitiveField;

/**
 * Implements the Fuzzy Expert System
 * 
 * @author liz.schwartz
 * 
 */
public class FuzzyExpImpl {

	static class AnalysisResult {
		public ArrayList results;
		public String criteriaStringCR;
		public String criteriaStringEN;
		public String criteriaStringVU;

		public AnalysisResult() {
			results = new ArrayList();
			criteriaStringCR = "";
			criteriaStringEN = "";
			criteriaStringVU = "";
		}

	}

	boolean INDIVIDUALDT = false;

	boolean TESTING = true;
	private CR critical;
	private VU vulnerable;

	private EN endangered;
	private static final double dt = 0.5;
	private static final double rt = 0.5;
	private static final int primaryField = 0;
	public final int xCR = 100;
	public final int xEN = 200;
	public final int xVU = 300;

	public final int xLR = 400;

	public FuzzyExpImpl() {
		critical = new CR();
		vulnerable = new VU();
		endangered = new EN();
	}

	private String calculateCriteriaString(ExpertResult result, CriteriaResult cr, CriteriaResult en, CriteriaResult vu) {
		String returnString = "";

		// IF CRITICALLY ENDANGERED
		if (result.getResult().startsWith("C")) {
			returnString = cr.resultString;
		}
		// IF ENDANGERED
		else if (result.getResult().startsWith("E")) {
			returnString = en.resultString;
		}
		// IF VULNERABLE
		else if (result.getResult().startsWith("V")) {
			returnString = vu.resultString;
		}

		return returnString;
	}

	private ExpertResult calculateResult(Range cr, Range en, Range vu, Assessment assessment) {
		ExpertResult result = new ExpertResult(assessment);

		// CREATE HIGH, MID, LOW LINES in that order
		ArrayList lines = createLines(cr, en, vu);
		result.setLeft(((Line) lines.get(0)).x(rt));
		result.setRight(((Line) lines.get(2)).x(rt));
		result.setBest(((Line) lines.get(1)).x(rt));
		System.out.println(
				"This is highLine (" + ((Line) lines.get(0)).x1 + "," + ((Line) lines.get(0)).y1 + "), ("
						+ ((Line) lines.get(0)).x2 + "," + ((Line) lines.get(0)).y2 + ")");
		System.out.println(
				"This is midLine (" + ((Line) lines.get(1)).x1 + "," + ((Line) lines.get(1)).y1 + "), ("
						+ ((Line) lines.get(1)).x2 + "," + ((Line) lines.get(1)).y2 + ")");
		System.out.println(
				"This is lowLine (" + ((Line) lines.get(2)).x1 + "," + ((Line) lines.get(2)).y1 + "), ("
						+ ((Line) lines.get(2)).x2 + "," + ((Line) lines.get(2)).y2 + ")");

		if (result.getBest() <= xCR) {
			result.setResult("Critically Endangered");
		} else if (result.getBest() <= xEN) {
			result.setResult("Endangered");
		} else if (result.getBest() <= xVU) {
			result.setResult("Vulnerable");
		} else
			result.setResult("Lower Risk");

		return result;
	}

	private ArrayList createLines(final Range cr, final Range en, final Range vu) {
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
		System.out.println("" + lowcr);
		System.out.println("" + highcr);
		System.out.println("" + midcr);
		System.out.println("" + lowen);
		System.out.println("" + highen);
		System.out.println("" + miden);
		System.out.println("" + lowvu);
		System.out.println("" + highvu);
		System.out.println("" + midvu);

		if (highcr >= rt) {
			lineHigh = new Line(x, xCR, y, highcr);
		} else if (highen >= rt) {
			lineHigh = new Line(xCR, xEN, highcr, highen);
		} else if (highvu >= rt) {
			lineHigh = new Line(xEN, xVU, highen, highvu);
		} else
			lineHigh = new Line(xVU, xLR, highvu, yfinal);

		if (midcr >= rt) {
			lineMid = new Line(x, xCR, y, midcr);
		} else if (miden >= rt) {
			lineMid = new Line(xCR, xEN, midcr, miden);
		} else if (midvu >= rt) {
			lineMid = new Line(xEN, xVU, miden, midvu);
		} else
			lineMid = new Line(xVU, xLR, midvu, yfinal);

		if (lowcr >= rt) {
			lineLow = new Line(x, xCR, y, lowcr);
		} else if (lowen >= rt) {
			lineLow = new Line(xCR, xEN, lowcr, lowen);
		} else if (lowvu >= rt) {
			lineLow = new Line(xEN, xVU, lowen, lowvu);
		} else
			lineLow = new Line(xVU, xLR, lowvu, yfinal);

		ArrayList returnVals = new ArrayList();
		returnVals.add(lineHigh);
		returnVals.add(lineMid);
		returnVals.add(lineLow);
		return returnVals;
	}

	private Range createRangeFromAssessment(Assessment assessment, String factor) {
		Field factorField = assessment.getField(factor);
		Range result = null;
		if (factorField != null) {
			for( PrimitiveField curPrim : factorField.getPrimitiveField() )
				if( curPrim instanceof RangePrimitiveField )
					result = new Range( ((RangePrimitiveField)curPrim).getValue() );
		}
		return result;
	}

	private String createStringFromAssessment(Assessment assessment, String factor) {
		Field factorField = assessment.getField(factor);
		StringBuilder result = new StringBuilder("");
		if (factorField != null) {
			for( PrimitiveField curPrim : factorField.getPrimitiveField() ) {
				if( curPrim instanceof ForeignKeyListPrimitiveField ) {
					List<Integer> fkList = ((ForeignKeyListPrimitiveField)curPrim).getValue();
					for( Integer fk : fkList ) {
						result.append(",");
						result.append(fk);
					}
				}
			}
		}
		return result.substring(1).toString();
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
		ExpertResult result = null;

		// GET ALL RANGES FOR SPECIFIC CRITERIA
		AnalysisResult analysisA = doAnalysisA(assessment);
		ArrayList resultsA = analysisA.results;
		AnalysisResult analysisB = doAnalysisB(assessment);
		ArrayList resultsB = analysisB.results;
		AnalysisResult analysisC = doAnalysisC(assessment);
		ArrayList resultsC = analysisC.results;
		AnalysisResult analysisD = doAnalysisD(assessment);
		ArrayList resultsD = analysisD.results;
		AnalysisResult analysisE = doAnalysisE(assessment);
		ArrayList resultsE = analysisE.results;
		System.out.println("finished doing all specific ranges");

		// DO FINAL RANGES FOR EACH CLASSIFICATION
		CriteriaResult finalResultCR = finalResult((CriteriaResult) resultsA.get(resultsA.size() - 3),
				(CriteriaResult) resultsB.get(resultsB.size() - 3), (CriteriaResult) resultsC.get(resultsC.size() - 3),
				(CriteriaResult) resultsD.get(resultsD.size() - 3), (CriteriaResult) resultsE.get(resultsE.size() - 3));
		CriteriaResult finalResultEN = finalResult((CriteriaResult) resultsA.get(resultsA.size() - 2),
				(CriteriaResult) resultsB.get(resultsB.size() - 2), (CriteriaResult) resultsC.get(resultsC.size() - 2),
				(CriteriaResult) resultsD.get(resultsD.size() - 2), (CriteriaResult) resultsE.get(resultsE.size() - 2));
		CriteriaResult finalResultVU = finalResult((CriteriaResult) resultsA.get(resultsA.size() - 1),
				(CriteriaResult) resultsB.get(resultsB.size() - 1), (CriteriaResult) resultsC.get(resultsC.size() - 1),
				(CriteriaResult) resultsD.get(resultsD.size() - 1), (CriteriaResult) resultsE.get(resultsE.size() - 1));
		System.out.println("Finished doing all final thingies");

		// DO DT IF DIDN'T DO INDIVIDUALLY
		if (!INDIVIDUALDT) {
			finalResultCR.range = Range.dt(finalResultCR.range, dt);
			finalResultEN.range = Range.dt(finalResultEN.range, dt);
			finalResultVU.range = Range.dt(finalResultVU.range, dt);
		}

		printRange("FINAL CR ----- ", finalResultCR.range);
		printRange("FINAL EN ----- ", finalResultEN.range);
		printRange("FINAL VU ----- ", finalResultVU.range);
		// GET RESULT WITH SPECIFIC RT

		// System.out.println("FINAL CR ----- " + finalResultCR.resultString);
		// System.out.println("FINAL EN ----- " + finalResultEN.resultString);
		// System.out.println("FINAL VU ----- " + finalResultVU.resultString);

		if (finalResultCR.range != null && finalResultEN.range != null && finalResultVU.range != null) {
			result = calculateResult(finalResultCR.range, finalResultEN.range, finalResultVU.range, assessment);
			System.out.println(
					"THIS IS THE FINAL RESULT " + result.getLeft() + "," + result.getBest() + "," + result.getRight()
							+ "   " + result.getResult());
			String[] criterias = getCriterias(resultsA, resultsB, resultsC, resultsD, resultsE).split("-");
			result.setCriteriaString(calculateCriteriaString(result, finalResultCR, finalResultEN, finalResultVU));
			result.setCriteriaStringCR(finalResultCR.resultString);
			result.setCriteriaStringEN(finalResultEN.resultString);
			result.setCriteriaStringVU(finalResultVU.resultString);
			
			result.setNotEnoughData(criterias[0]);
			// result.setEnoughData(criterias[1]);

		} else if (finalResultEN.range != null && finalResultVU.range != null) {
			Range pretendCR = new Range();
			pretendCR.setHigh(0);
			pretendCR.setHighBest(0);
			pretendCR.setLow(0);
			pretendCR.setLowBest(0);
			result = calculateResult(pretendCR, finalResultEN.range, finalResultVU.range, assessment);
			String[] criterias = getCriterias(resultsA, resultsB, resultsC, resultsD, resultsE).split("-");
			result.setCriteriaString(calculateCriteriaString(result, finalResultCR, finalResultEN, finalResultVU));
			result.setNotEnoughData(criterias[0]);
		}

		else if (finalResultVU.range != null) {
			Range pretendCR = new Range();
			pretendCR.setHigh(0);
			pretendCR.setHighBest(0);
			pretendCR.setLow(0);
			pretendCR.setLowBest(0);

			Range pretendEN = new Range();
			pretendEN.setHigh(0);
			pretendEN.setHighBest(0);
			pretendEN.setLow(0);
			pretendEN.setLowBest(0);

			result = calculateResult(pretendCR, pretendEN, finalResultVU.range, assessment);
			System.out.println(
					"THIS IS THE FINAL RESULT " + result.getLeft() + "," + result.getBest() + "," + result.getRight()
							+ "   " + result.getResult());
			String[] criterias = getCriterias(resultsA, resultsB, resultsC, resultsD, resultsE).split("-");
			result.setCriteriaString(calculateCriteriaString(result, finalResultCR, finalResultEN, finalResultVU));
			result.setNotEnoughData(criterias[0]);
			// result.setEnoughData(criterias[1]);
		}

		else {
			result = new ExpertResult(assessment);
			result.setNotEnoughData(null);
			result.setCriteriaString(null);
			result.setResult(null);
			result.setLeft(-1);
			result.setRight(-1);
			result.setBest(-1);
		}

		if (result != null)
			System.out.println(result.getNotEnoughData());
		else
			System.out.println("woot");
		// return result;
		return result;

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
		HashMap criticalA1Map = new HashMap();
		for (int i = 0; i < critical.factorsA1.length; i++) {
			Range primary = createRangeFromAssessment(assessment, critical.factorsA1[i]);

			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			criticalA1Map.put(critical.factorsA1[i], primary);
		}

		CriteriaResult resultCR1 = critical.a1(criticalA1Map, createStringFromAssessment(assessment,
				Factors.populationReductionPastBasis));
		printRange(" crA1 ", resultCR1.range);

		HashMap endangeredA1Map = new HashMap();
		for (int i = 0; i < endangered.factorsA1.length; i++) {
			Range primary = createRangeFromAssessment(assessment, endangered.factorsA1[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			endangeredA1Map.put(endangered.factorsA1[i], primary);
		}

		CriteriaResult resultEN1 = endangered.a1(endangeredA1Map, createStringFromAssessment(assessment,
				Factors.populationReductionPastBasis));
		printRange("enA1 ", resultEN1.range);

		HashMap vulnerableA1Map = new HashMap();
		for (int i = 0; i < vulnerable.factorsA1.length; i++) {
			Range primary = createRangeFromAssessment(assessment, vulnerable.factorsA1[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			vulnerableA1Map.put(vulnerable.factorsA1[i], primary);
		}

		CriteriaResult resultVU1 = vulnerable.a1(vulnerableA1Map, createStringFromAssessment(assessment,
				Factors.populationReductionPastBasis));
		printRange("vuA1 ", resultVU1.range);

		// GET ALL ENTERED INFORMATION FOR A2
		HashMap criticalA2Map = new HashMap();
		for (int i = 0; i < critical.factorsA2.length; i++) {
			Range primary = createRangeFromAssessment(assessment, critical.factorsA2[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			criticalA2Map.put(critical.factorsA2[i], primary);
		}

		CriteriaResult resultCR2 = critical.a2(criticalA2Map, createStringFromAssessment(assessment,
				Factors.populationReductionPastBasis));
		printRange("CRA2 ", resultCR2.range);

		HashMap endangeredA2Map = new HashMap();
		for (int i = 0; i < endangered.factorsA2.length; i++) {
			Range primary = createRangeFromAssessment(assessment, endangered.factorsA2[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			endangeredA2Map.put(endangered.factorsA2[i], primary);
		}

		CriteriaResult resultEN2 = endangered.a2(endangeredA2Map, createStringFromAssessment(assessment,
				Factors.populationReductionPastBasis));
		printRange("ENA2 ", resultEN2.range);

		HashMap vulnerableA2Map = new HashMap();
		for (int i = 0; i < vulnerable.factorsA2.length; i++) {
			Range primary = createRangeFromAssessment(assessment, vulnerable.factorsA2[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			vulnerableA2Map.put(vulnerable.factorsA2[i], primary);
		}

		CriteriaResult resultVU2 = vulnerable.a2(vulnerableA2Map, createStringFromAssessment(assessment,
				Factors.populationReductionPastBasis));
		printRange(" VUA2 ", resultVU2.range);

		// GET ALL ENTERED INFORMATION FOR A3
		Range prf1 = createRangeFromAssessment(assessment, critical.factorsA3[0]);
		if (INDIVIDUALDT)
			prf1 = Range.dt(prf1, dt);

		CriteriaResult resultCR3 = critical.a3(prf1, createStringFromAssessment(assessment,
				Factors.populationReductionFutureBasis));
		printRange(" CRA3 ", resultCR3.range);

		Range prf2 = createRangeFromAssessment(assessment, endangered.factorsA3[0]);
		if (INDIVIDUALDT)
			prf2 = Range.dt(prf2, dt);
		CriteriaResult resultEN3 = endangered.a3(prf2, createStringFromAssessment(assessment,
				Factors.populationReductionFutureBasis));
		printRange("ENA3 ", resultEN3.range);

		Range prf3 = createRangeFromAssessment(assessment, vulnerable.factorsA3[0]);
		if (INDIVIDUALDT)
			prf3 = Range.dt(prf3, dt);
		CriteriaResult resultVU3 = vulnerable.a3(prf3, createStringFromAssessment(assessment,
				Factors.populationReductionFutureBasis));
		printRange("VUA3 ", resultVU3.range);

		// GET ALL ENTERED INFORMATION FOR A4
		HashMap criticalA4Map = new HashMap();
		for (int i = 0; i < critical.factorsA4.length; i++) {
			Range primary = createRangeFromAssessment(assessment, critical.factorsA4[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			criticalA4Map.put(critical.factorsA4[i], primary);
		}

		CriteriaResult resultCR4 = critical.a4(criticalA4Map, createStringFromAssessment(assessment,
				Factors.populationReductionEitherBasis));
		printRange("CRA4 ", resultCR4.range);

		HashMap endangeredA4Map = new HashMap();
		for (int i = 0; i < endangered.factorsA4.length; i++) {
			Range primary = createRangeFromAssessment(assessment, endangered.factorsA4[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			endangeredA4Map.put(endangered.factorsA4[i], primary);
		}
		CriteriaResult resultEN4 = endangered.a4(endangeredA4Map, createStringFromAssessment(assessment,
				Factors.populationReductionEitherBasis));
		printRange("ENA4 ", resultEN4.range);

		HashMap vulnerableA4Map = new HashMap();
		for (int i = 0; i < vulnerable.factorsA4.length; i++) {
			Range primary = createRangeFromAssessment(assessment, vulnerable.factorsA4[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			vulnerableA4Map.put(vulnerable.factorsA4[i], primary);
		}
		CriteriaResult resultVU4 = vulnerable.a4(vulnerableA4Map, createStringFromAssessment(assessment,
				Factors.populationReductionEitherBasis));
		printRange("VUA4 ", resultVU4.range);

		// GET FINAL INFO FOR A
		CriteriaResult crA = getFinalA(resultCR1, resultCR2, resultCR3, resultCR4);
		printRange("CRA ", crA.range);
		CriteriaResult enA = getFinalA(resultEN1, resultEN2, resultEN3, resultEN4);
		printRange(" ENA ", enA.range);
		CriteriaResult vuA = getFinalA(resultVU1, resultVU2, resultVU3, resultVU4);
		printRange("VUA ", vuA.range);

		analysis.results.add(resultCR1);
		analysis.results.add(resultCR2);
		analysis.results.add(resultCR3);
		analysis.results.add(resultCR4);

		analysis.results.add(resultEN1);
		analysis.results.add(resultEN2);
		analysis.results.add(resultEN3);
		analysis.results.add(resultEN4);

		analysis.results.add(resultVU1);
		analysis.results.add(resultVU2);
		analysis.results.add(resultVU3);
		analysis.results.add(resultVU4);

		analysis.results.add(crA);
		analysis.results.add(enA);
		analysis.results.add(vuA);
		return analysis;
	}

	private AnalysisResult doAnalysisB(final Assessment assessment) {
		AnalysisResult analysis = new AnalysisResult();

		// GET ALL ENTERED INFORMATION FOR B1
		HashMap criticalB1Map = new HashMap();
		for (int i = 0; i < critical.factorsB1.length; i++) {
			Range primary = createRangeFromAssessment(assessment, critical.factorsB1[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			criticalB1Map.put(critical.factorsB1[i], primary);
		}
		CriteriaResult resultCR1 = critical.b1(criticalB1Map);
		printRange("CRB1", resultCR1.range);

		HashMap endangeredB1Map = new HashMap();
		for (int i = 0; i < endangered.factorsB1.length; i++) {
			Range primary = createRangeFromAssessment(assessment, endangered.factorsB1[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			endangeredB1Map.put(endangered.factorsB1[i], primary);
		}
		CriteriaResult resultEN1 = endangered.b1(endangeredB1Map);
		printRange("ENB1 ", resultEN1.range);

		HashMap vulnerableB1Map = new HashMap();
		for (int i = 0; i < vulnerable.factorsB1.length; i++) {
			Range primary = createRangeFromAssessment(assessment, vulnerable.factorsB1[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			vulnerableB1Map.put(vulnerable.factorsB1[i], primary);
		}
		CriteriaResult resultVU1 = vulnerable.b1(vulnerableB1Map);
		printRange("VUB1", resultVU1.range);

		// GET ALL ENTERED INFORMATION FOR B2
		HashMap criticalB2Map = new HashMap();
		for (int i = 0; i < critical.factorsB2.length; i++) {
			Range primary = createRangeFromAssessment(assessment, critical.factorsB2[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			criticalB2Map.put(critical.factorsB2[i], primary);
		}
		CriteriaResult resultCR2 = critical.b2(criticalB2Map);
		printRange("CRB2 ", resultCR2.range);

		HashMap endangeredB2Map = new HashMap();
		for (int i = 0; i < endangered.factorsB2.length; i++) {
			Range primary = createRangeFromAssessment(assessment, endangered.factorsB2[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			endangeredB2Map.put(endangered.factorsB2[i], primary);
		}
		CriteriaResult resultEN2 = endangered.b2(endangeredB2Map);
		printRange("ENB2", resultEN2.range);

		HashMap vulnerableB2Map = new HashMap();
		for (int i = 0; i < vulnerable.factorsB2.length; i++) {
			Range primary = createRangeFromAssessment(assessment, vulnerable.factorsB2[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			vulnerableB2Map.put(vulnerable.factorsB2[i], primary);
		}
		CriteriaResult resultVU2 = vulnerable.b2(vulnerableB2Map);
		printRange("VUB2", resultVU2.range);

		CriteriaResult finalCRb = getFinalB(resultCR1, resultCR2);
		printRange("finalCRB ", finalCRb.range);
		CriteriaResult finalENb = getFinalB(resultEN1, resultEN2);
		printRange("finalENB ", finalENb.range);
		CriteriaResult finalVUb = getFinalB(resultVU1, resultVU2);
		printRange("finalVUb", finalVUb.range);

		analysis.results.add(resultCR1);
		analysis.results.add(resultCR2);

		analysis.results.add(resultEN1);
		analysis.results.add(resultEN2);

		analysis.results.add(resultVU1);
		analysis.results.add(resultVU2);

		analysis.results.add(finalCRb);
		analysis.results.add(finalENb);
		analysis.results.add(finalVUb);
		return analysis;
	}

	private AnalysisResult doAnalysisC(final Assessment assessment) {
		AnalysisResult analysis = new AnalysisResult();

		// GET ALL ENTERED INFORMATION FOR C1
		HashMap criticalC1Map = new HashMap();
		for (int i = 0; i < critical.factorsC.length; i++) {
			Range primary = createRangeFromAssessment(assessment, critical.factorsC[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			criticalC1Map.put(critical.factorsC[i], primary);
		}
		CriteriaResult resultCR = critical.c(criticalC1Map);
		printRange("CCR ", resultCR.range);

		HashMap endangeredC1Map = new HashMap();
		for (int i = 0; i < endangered.factorsC1.length; i++) {
			Range primary = createRangeFromAssessment(assessment, endangered.factorsC1[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			endangeredC1Map.put(endangered.factorsC1[i], primary);
		}
		CriteriaResult resultEN1 = endangered.c1(endangeredC1Map);
		printRange("ENC1 ", resultEN1.range);

		HashMap vulnerableC1Map = new HashMap();
		for (int i = 0; i < vulnerable.factorsC1.length; i++) {
			Range primary = createRangeFromAssessment(assessment, vulnerable.factorsC1[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			vulnerableC1Map.put(vulnerable.factorsC1[i], primary);
		}
		CriteriaResult resultVU1 = vulnerable.c1(vulnerableC1Map);
		printRange("VUC1", resultVU1.range);

		// GET ALL ENTERED INFORMATION FOR C2
		HashMap endangeredC2Map = new HashMap();
		for (int i = 0; i < endangered.factorsC2.length; i++) {
			Range primary = createRangeFromAssessment(assessment, endangered.factorsC2[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			endangeredC2Map.put(endangered.factorsC2[i], primary);
		}
		CriteriaResult resultEN2 = endangered.c2(endangeredC2Map);
		printRange("ENC2", resultEN2.range);

		HashMap vulnerableC2Map = new HashMap();
		for (int i = 0; i < vulnerable.factorsC2.length; i++) {
			Range primary = createRangeFromAssessment(assessment, vulnerable.factorsC2[i]);
			if (INDIVIDUALDT)
				primary = Range.dt(primary, dt);
			vulnerableC2Map.put(vulnerable.factorsC2[i], primary);
		}
		CriteriaResult resultVU2 = vulnerable.c2(vulnerableC2Map);
		printRange("VUC2", resultVU2.range);

		CriteriaResult finalCR = resultCR;
		printRange("CRC ", finalCR.range);
		CriteriaResult finalEN = getFinalC(resultEN1, resultEN2);
		printRange("ENC ", finalEN.range);
		CriteriaResult finalVU = getFinalC(resultVU1, resultVU2);
		printRange("VUC", finalVU.range);

		analysis.results.add(resultCR);

		analysis.results.add(resultEN1);
		analysis.results.add(resultEN2);

		analysis.results.add(resultVU1);
		analysis.results.add(resultVU2);

		analysis.results.add(finalCR);
		analysis.results.add(finalEN);
		analysis.results.add(finalVU);
		return analysis;
	}

	private AnalysisResult doAnalysisD(final Assessment assessment) {
		AnalysisResult analysis = new AnalysisResult();

		// GET ALL ENTERED INFORMATION FOR D1
		Range primary = createRangeFromAssessment(assessment, critical.factorsD[0]);
		if (INDIVIDUALDT)
			primary = Range.dt(primary, dt);
		CriteriaResult resultCR = critical.d(primary);
		printRange("CRD ", resultCR.range);

		primary = createRangeFromAssessment(assessment, endangered.factorsD[0]);
		if (INDIVIDUALDT)
			primary = Range.dt(primary, dt);
		CriteriaResult resultEN = endangered.d(primary);
		printRange("END ", resultEN.range);

		primary = createRangeFromAssessment(assessment, vulnerable.factorsD1[0]);
		if (INDIVIDUALDT)
			primary = Range.dt(primary, dt);
		CriteriaResult resultVU1 = vulnerable.d1(primary);
		printRange("VUD1 ", resultVU1.range);

		// GET ALL ENTERED INFORMATION FOR D2
		primary = createRangeFromAssessment(assessment, vulnerable.factorsD2[0]);
		if (INDIVIDUALDT)
			primary = Range.dt(primary, dt);
		CriteriaResult resultVU2 = vulnerable.d2(primary);
		printRange("VUD2 ", resultVU2.range);

		CriteriaResult finalVU = getFinalD(resultVU1, resultVU2);

		analysis.results.add(resultCR);
		analysis.results.add(resultEN);
		analysis.results.add(resultVU1);
		analysis.results.add(resultVU2);
		analysis.results.add(resultCR);
		analysis.results.add(resultEN);
		analysis.results.add(finalVU);
		printRange("FINAL CRD ", resultCR.range);
		printRange("Final END ", resultEN.range);
		printRange("Final VUD ", finalVU.range);

		return analysis;
	}

	private AnalysisResult doAnalysisE(final Assessment assessment) {
		AnalysisResult analysis = new AnalysisResult();
		CriteriaResult result;

		// GET ALL ENTERED INFORMATION FOR D1
		Range primary = createRangeFromAssessment(assessment, critical.factorsE[0]);
		if (INDIVIDUALDT)
			primary = Range.dt(primary, dt);
		result = critical.e(primary);
		analysis.results.add(result);
		printRange("CRE", result.range);

		primary = createRangeFromAssessment(assessment, endangered.factorsE[0]);
		if (INDIVIDUALDT)
			primary = Range.dt(primary, dt);
		result = endangered.e(primary);
		analysis.results.add(result);
		printRange("ENE", result.range);

		primary = createRangeFromAssessment(assessment, vulnerable.factorsE[0]);
		if (INDIVIDUALDT)
			primary = Range.dt(primary, dt);
		result = vulnerable.e(primary);
		analysis.results.add(result);
		printRange("VUE", result.range);

		return analysis;
	}

	private CriteriaResult finalResult(CriteriaResult a, CriteriaResult b, CriteriaResult c, CriteriaResult d,
			CriteriaResult e) {
		Range aRange = null;
		Range bRange = null;
		Range cRange = null;
		Range dRange = null;
		Range eRange = null;
		if (a != null)
			aRange = a.range;
		if (b != null)
			bRange = b.range;
		if (c != null)
			cRange = c.range;
		if (d != null)
			dRange = d.range;
		if (e != null)
			eRange = e.range;
		Range result = Range.dependentOR(aRange, bRange);
		result = Range.independentOR(result, cRange);
		result = Range.independentOR(result, dRange);
		result = Range.independentOR(result, eRange);

		// DO COMBINING OF CRITERIA STRINGS
		boolean startedString = false;
		String returnString = "";
		if (!a.resultString.equals("")) {
			returnString = a.resultString;
			startedString = true;
		}
		if (!b.resultString.equals("")) {
			if (startedString) {
				returnString += "; " + b.resultString;
			} else {
				returnString = b.resultString;
			}
			startedString = true;
		}
		if (!c.resultString.equals("")) {
			if (startedString) {
				returnString += "; " + c.resultString;
			} else {
				returnString = c.resultString;
			}
			startedString = true;
		}
		if (!d.resultString.equals("")) {
			if (startedString) {
				returnString += "; " + d.resultString;
			} else {
				returnString = d.resultString;
			}
			startedString = true;
		}
		if (!e.resultString.equals("")) {
			if (startedString) {
				returnString += "; " + e.resultString;
			} else {
				returnString = e.resultString;
			}
			startedString = true;
		}

		CriteriaResult analysis = new CriteriaResult();
		analysis.range = result;
		analysis.resultString = returnString;
		return analysis;

	}

	private String getCriterias(ArrayList resultsA, ArrayList resultsB, ArrayList resultsC, ArrayList resultsD,
			ArrayList resultsE) {
		// set result with all criteria that couldn't be calculated
		StringBuffer noData = new StringBuffer();
		StringBuffer data = new StringBuffer();

		for (int i = 0; i < resultsA.size(); i++) {
			if (resultsA.get(i) == null) {

				if (i < critical.criteriaA) {
					int j = i + 1;
					noData.append("CRa" + j + ",");
				} else if (i < critical.criteriaA + endangered.criteriaA) {
					int j = i + 1 - critical.criteriaA;
					noData.append("ENa" + j + ",");
				} else if (i < critical.criteriaA + endangered.criteriaA + vulnerable.criteriaA) {
					int j = i + 1 - critical.criteriaA - endangered.criteriaA;
					noData.append("VUa" + j + ",");
				}
			} else {
				if (i < critical.criteriaA) {
					int j = i + 1;
					data.append("CRa" + j + ",");
				} else if (i < critical.criteriaA + endangered.criteriaA) {
					int j = i + 1 - critical.criteriaA;
					data.append("ENa" + j + ",");
				} else if (i < critical.criteriaA + endangered.criteriaA + vulnerable.criteriaA) {
					int j = i + 1 - critical.criteriaA - endangered.criteriaA;
					data.append("VUa" + j + ",");
				}
			}

		}
		for (int i = 0; i < resultsB.size(); i++) {
			if (resultsB.get(i) == null) {

				if (i < critical.criteriaB) {
					int j = i + 1;
					noData.append("CRb" + j + ",");
				} else if (i < critical.criteriaB + endangered.criteriaB) {
					int j = i + 1 - critical.criteriaB;
					noData.append("ENb" + j + ",");
				} else if (i < critical.criteriaB + endangered.criteriaB + vulnerable.criteriaB) {
					int j = i + 1 - critical.criteriaB - endangered.criteriaB;
					noData.append("VUb" + j + ",");
				}

			}

			else {
				if (i < critical.criteriaA) {
					int j = i + 1;
					data.append("CRb" + j + ",");
				} else if (i < critical.criteriaA + endangered.criteriaA) {
					int j = i + 1 - critical.criteriaA;
					data.append("ENb" + j + ",");
				} else if (i < critical.criteriaA + endangered.criteriaA + vulnerable.criteriaA) {
					int j = i + 1 - critical.criteriaA - endangered.criteriaA;
					data.append("VUb" + j + ",");
				}
			}
		}
		for (int i = 0; i < resultsC.size(); i++) {
			if (resultsC.get(i) == null) {

				if (i < critical.criteriaC) {
					int j = i + 1;
					noData.append("CRc" + j + ",");
				} else if (i < critical.criteriaC + endangered.criteriaC) {
					int j = i + 1 - critical.criteriaC;
					noData.append("ENc" + j + ",");
				} else if (i < critical.criteriaC + endangered.criteriaC + vulnerable.criteriaC) {
					int j = i + 1 - critical.criteriaC - endangered.criteriaC;
					noData.append("VUc" + j + ",");
				}
			} else {
				if (i < critical.criteriaA) {
					int j = i + 1;
					data.append("CRc" + j + ",");
				} else if (i < critical.criteriaA + endangered.criteriaA) {
					int j = i + 1 - critical.criteriaA;
					data.append("ENc" + j + ",");
				} else if (i < critical.criteriaA + endangered.criteriaA + vulnerable.criteriaA) {
					int j = i + 1 - critical.criteriaA - endangered.criteriaA;
					data.append("VUc" + j + ",");
				}
			}
		}
		for (int i = 0; i < resultsD.size(); i++) {
			if (resultsD.get(i) == null) {

				if (i < critical.criteriaD) {
					int j = i + 1;
					noData.append("CRd" + j + ",");
				} else if (i < critical.criteriaD + endangered.criteriaD) {
					int j = i + 1 - critical.criteriaD;
					noData.append("ENd" + j + ",");
				} else if (i < critical.criteriaD + endangered.criteriaD + vulnerable.criteriaD) {
					int j = i + 1 - critical.criteriaD - endangered.criteriaD;
					noData.append("VUd" + j + ",");

				}

			} else {
				if (i < critical.criteriaA) {
					int j = i + 1;
					data.append("CRd" + j + ",");
				} else if (i < critical.criteriaA + endangered.criteriaA) {
					int j = i + 1 - critical.criteriaA;
					data.append("ENd" + j + ",");
				} else if (i < critical.criteriaA + endangered.criteriaA + vulnerable.criteriaA) {
					int j = i + 1 - critical.criteriaA - endangered.criteriaA;
					data.append("VUd" + j + ",");
				}
			}
		}
		for (int i = 0; i < resultsE.size(); i++) {
			if (resultsE.get(i) == null) {

				if (i < critical.criteriaE) {
					int j = i + 1;
					noData.append("CRe" + j + ",");
				} else if (i < critical.criteriaE + endangered.criteriaE) {
					int j = i + 1 - critical.criteriaE;
					noData.append("ENe" + j + ",");
				} else if (i < critical.criteriaE + endangered.criteriaE + vulnerable.criteriaE) {
					int j = i + 1 - critical.criteriaE - endangered.criteriaE;
					noData.append("VUb" + j + ",");
				}
			} else {
				if (i < critical.criteriaA) {
					int j = i + 1;
					data.append("CRe" + j + ",");
				} else if (i < critical.criteriaA + endangered.criteriaA) {
					int j = i + 1 - critical.criteriaA;
					data.append("ENe" + j + ",");
				} else if (i < critical.criteriaA + endangered.criteriaA + vulnerable.criteriaA) {
					int j = i + 1 - critical.criteriaA - endangered.criteriaA;
					data.append("VUe" + j + ",");
				}
			}
		}

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
	private CriteriaResult getFinalA(CriteriaResult a, CriteriaResult b, CriteriaResult c, CriteriaResult d) {

		// DO COMBINING OF RANGES
		CriteriaResult analysis = new CriteriaResult();
		Range result = Range.dependentOR(a.range, b.range);
		result = Range.independentOR(result, c.range);
		result = Range.independentOR(result, d.range);

		// DO COMBINING OF CRITERIA STRINGS
		boolean startedString = false;
		String returnString = "";
		if (!a.resultString.equals("")) {
			returnString = a.resultString;
			startedString = true;
		}
		if (!b.resultString.equals("")) {
			if (startedString) {
				returnString += "+" + b.resultString.substring(1);
			} else {
				returnString = b.resultString;
			}
			startedString = true;
		}
		if (!c.resultString.equals("")) {
			if (startedString) {
				returnString += "+" + c.resultString.substring(1);
			} else {
				returnString = c.resultString;
			}
			startedString = true;
		}
		if (!d.resultString.equals("")) {
			if (startedString) {
				returnString += "+" + d.resultString.substring(1);
			} else {
				returnString = d.resultString;
			}
			startedString = true;
		}
		analysis.resultString = returnString;
		analysis.range = result;
		return analysis;
	}

	private CriteriaResult getFinalB(CriteriaResult a, CriteriaResult b) {
		CriteriaResult analysis = new CriteriaResult();
		Range result = Range.independentOR(a.range, b.range);

		// DO COMBINING OF CRITERIA STRINGS
		boolean startedString = false;
		String returnString = "";
		if (!a.resultString.equals("")) {
			returnString = a.resultString;
			startedString = true;
		}
		if (!b.resultString.equals("")) {
			if (startedString) {
				returnString += "+" + b.resultString.substring(1);
			} else {
				returnString = b.resultString;
			}
			startedString = true;
		}

		analysis.range = result;
		analysis.resultString = returnString;
		return analysis;

	}

	private CriteriaResult getFinalC(CriteriaResult a, CriteriaResult b) {
		CriteriaResult analysis = new CriteriaResult();
		Range result = Range.independentOR(a.range, b.range);

		// DO COMBINING OF CRITERIA STRINGS
		boolean startedString = false;
		String returnString = "";
		if (!a.resultString.equals("")) {
			returnString = a.resultString;
			startedString = true;
		}
		if (!b.resultString.equals("")) {
			if (startedString) {
				returnString += "+" + b.resultString.substring(1);
			} else {
				returnString = b.resultString;
			}
			startedString = true;
		}

		analysis.range = result;
		analysis.resultString = returnString;
		return analysis;

	}

	private CriteriaResult getFinalD(CriteriaResult a, CriteriaResult b) {
		CriteriaResult analysis = new CriteriaResult();
		Range result = Range.independentOR(a.range, b.range);

		// DO COMBINING OF CRITERIA STRINGS
		boolean startedString = false;
		String returnString = "";
		if (!a.resultString.equals("")) {
			returnString = a.resultString;
			startedString = true;
		}
		if (!b.resultString.equals("")) {
			if (startedString) {
				returnString += "+" + b.resultString.substring(1);
			} else {
				returnString = b.resultString;
			}
			startedString = true;
		}

		analysis.range = result;
		analysis.resultString = returnString;
		return analysis;
	}

	private void printRange(String descrip, Range range) {
		if (TESTING) {
			if (range != null) {

				System.out.println(
						"This is the results from " + descrip + " " + range.getLow() + "," + range.getLowBest() + ","
								+ range.getHighBest() + "," + range.getHigh());
			} else {
				System.out.println(" " + descrip + " == null");

			}
		}
	}

}
