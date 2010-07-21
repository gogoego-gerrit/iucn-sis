package org.iucn.sis.client.expert;

//package org.iucn.sis.expert;
//
///**
// * Expert system for SIS.  Takes an assessment in XML format or as an ExpertAssessment
// * object and does its thing.
// * @author adam.schwartz
// *
// */
//public class ExpertImpl
//{
//
//	public ExpertImpl() 
//	{
//		
//	}
//	
//	public ExpertResult doAnalysisFromExpertAssessment( ExpertAssessment assessment )
//	{
//		return doAnalysis( assessment );
//	}
//
//	/**
//	 * Actually performs the Expert system analysis.  Returns an ExpertResult
//	 * object holding the results with a reference to the ExpertAssessment object
//	 * it was run on.
//	 * @param ExpertAssessment object to be analyzed
//	 * @return ExpertResult object with results
//	 */
//	private ExpertResult doAnalysis( ExpertAssessment assessment )
//	{
//		ExpertResult result = new ExpertResult( assessment );
//
//		try
//		{
//			boolean [] results = new boolean[11];
//			int category = 3;
//			String resultString = "";
//
//			if( isCRa1(assessment) ) { results[0] = true; category = 0; }
//			if( isCRa2(assessment) ) { results[1] = true; category = 0; }
//			if( isCRa3(assessment) ) { results[2] = true; category = 0; }
//			if( isCRa4(assessment) ) { results[3] = true; category = 0; }
//			if( isCRb1(assessment) ) { results[4] = true; category = 0; }
//			if( isCRb2(assessment) ) { results[5] = true; category = 0; }
//			if( isCRc(assessment) ) { results[6] = true; category = 0; }
//			if( isCRd(assessment) ) { results[7] = true; category = 0; }
//			if( isCRe(assessment) ) { results[8] = true; category = 0; }
//
//			if( category == 0 )
//			{
//				resultString = makeABResultString( results );
//
//				if( results[6] ) resultString += "; (c)";
//				if( results[7] ) resultString += "; (d)";
//				if( results[8] ) resultString += "; (e)";
//			}
//
//			if( category == 3 )
//			{
//				if( isENa1(assessment) ) { results[0] = true; category = 1; }
//				if( isENa2(assessment) ) { results[1] = true; category = 1; }
//				if( isENa3(assessment) ) { results[2] = true; category = 1; }
//				if( isENa4(assessment) ) { results[3] = true; category = 1; }
//				if( isENb1(assessment) ) { results[4] = true; category = 1; }
//				if( isENb2(assessment) ) { results[5] = true; category = 1; }
//				if( isENc1(assessment) ) { results[6] = true; category = 1; }
//				if( isENc2(assessment) ) { results[7] = true; category = 1; }
//				if( isENd(assessment) ) { results[8] = true; category = 1; }
//				if( isENe(assessment) ) { results[9] = true; category = 1; }
//
//				if( category == 1 )
//				{
//					resultString = makeABResultString( results );
//
//					if( results[6] || results[7] )
//					{
//						resultString += "; (c)";
//						if( results[6] ) resultString += "1";
//						if( results[7] ) resultString += "2";
//					}
//					if( results[8] ) resultString += "; (d)";
//					if( results[9] ) resultString += "; (e)";
//				}
//			}
//
//
//			if( category == 3 )
//			{
//				if( isVUa1(assessment) ) { results[0] = true; category = 2; }
//				if( isVUa2(assessment) ) { results[1] = true; category = 2; }
//				if( isVUa3(assessment) ) { results[2] = true; category = 2; }
//				if( isVUa4(assessment) ) { results[3] = true; category = 2; }
//				if( isVUb1(assessment) ) { results[4] = true; category = 2; }
//				if( isVUb2(assessment) ) { results[5] = true; category = 2; }
//				if( isVUc1(assessment) ) { results[6] = true; category = 2; }
//				if( isVUc2(assessment) ) { results[7] = true; category = 2; }
//				if( isVUd1(assessment) ) { results[8] = true; category = 2; }
//				if( isVUd2(assessment) ) { results[9] = true; category = 2; }
//				if( isVUe(assessment) ) { results[10] = true; category = 2; }
//
//				resultString = makeABResultString( results );
//
//				if( results[6] || results[7] )
//				{
//					resultString += "; (c)";
//					if( results[6] ) resultString += "1";
//					if( results[7] ) resultString += "2";
//				}
//				if( results[8] || results[9] )
//				{
//					resultString += "; (d)";
//					if( results[8] ) resultString += "1";
//					if( results[9] ) resultString += "2";
//				}
//				if( results[10] ) resultString += "; (e)";
//			}
//
//			//IF IT STARTS WITH A SPACE AND AN ; STRIP IT OFF
//			if( resultString.matches("^ ;.*") )
//				resultString = resultString.substring(2);
//
//			if( category == 0 )
//				result.setResult( "CR" + resultString );
//			else if( category == 1 )
//				result.setResult( "EN" + resultString );
//			else if( category == 2 )
//				result.setResult( "VU" + resultString );
//			else
//				result.setResult( "No Result" );
//		}
//		catch( Exception e )
//		{
//			result.setResult( "Cannot complete analysis. A required factor is not set." );
////			e.printStackTrace();
//		}
//
//		return result;
//	}
//
//	public String makeABResultString( boolean results[] )
//	{
//		String resultString = "";
//
//		if( results[0] || results[1] || results[2] || results[3] )
//		{
//			resultString += "(a)";
//			if( results[0] ) resultString += "1";
//			if( results[1] ) resultString += "2";
//			if( results[2] ) resultString += "3";
//			if( results[3] ) resultString += "4";
//		}
//		if( results[4] || results[5] )
//		{
//			resultString += "; (b)";
//			if( results[4] ) resultString += "1";
//			if( results[5] ) resultString += "2";
//		}
//		
//		return resultString;
//	}
//
//	private boolean isCRa1( ExpertAssessment assessment )
//	{
//		if( assessment.getPopulationReductionPast() >= 90 &&
//				assessment.getPopulationReductionPastReversible().booleanValue() &&
//				assessment.getPopulationReductionPastUnderstood().booleanValue() &&
//				assessment.getPopulationReductionPastCeased().booleanValue() &&
//				( assessment.getPopulationReductionPastBasis().matches(".*0.*") ||
//					assessment.getPopulationReductionPastBasis().matches(".*1.*") ||
//					assessment.getPopulationReductionPastBasis().matches(".*2.*") ||
//					assessment.getPopulationReductionPastBasis().matches(".*3.*") ||
//					assessment.getPopulationReductionPastBasis().matches(".*4.*") ) )
//			return true;
//
//		return false;
//	}
//	
//	private boolean isCRa2( ExpertAssessment assessment )
//	{
//		if( assessment.getPopulationReductionPast() >= 80 &&
//				(!assessment.getPopulationReductionPastReversible().booleanValue() ||
//				 !assessment.getPopulationReductionPastUnderstood().booleanValue() ||
//				 !assessment.getPopulationReductionPastCeased().booleanValue() ) &&
//				(   assessment.getPopulationReductionPastBasis().matches(".*0.*") ||
//					assessment.getPopulationReductionPastBasis().matches(".*1.*") ||
//					assessment.getPopulationReductionPastBasis().matches(".*2.*") ||
//					assessment.getPopulationReductionPastBasis().matches(".*3.*") ||
//					assessment.getPopulationReductionPastBasis().matches(".*4.*") ) )
//			return true;
//		
//		return false;
//	}
//
//	private boolean isCRa3( ExpertAssessment assessment )
//	{
//		if( assessment.getPopulationReductionFuture() >= 80 &&
//			   (assessment.getPopulationReductionFutureBasis().matches(".*1.*") ||
//				assessment.getPopulationReductionFutureBasis().matches(".*2.*") ||
//				assessment.getPopulationReductionFutureBasis().matches(".*3.*") ||
//				assessment.getPopulationReductionFutureBasis().matches(".*4.*") ) )
//			return true;
//		
//		return false;
//	}
//	
//	private boolean isCRa4( ExpertAssessment assessment )
//	{
//		if( assessment.getPopulationReductionEither() >= 80 &&
//				(!assessment.getPopulationReductionEitherReversible().booleanValue() ||
//				 !assessment.getPopulationReductionEitherUnderstood().booleanValue() ||
//				 !assessment.getPopulationReductionEitherCeased().booleanValue() ) &&
//				(   assessment.getPopulationReductionEitherBasis().matches(".*0.*") ||
//					assessment.getPopulationReductionEitherBasis().matches(".*1.*") ||
//					assessment.getPopulationReductionEitherBasis().matches(".*2.*") ||
//					assessment.getPopulationReductionEitherBasis().matches(".*3.*") ||
//					assessment.getPopulationReductionEitherBasis().matches(".*4.*") ) )
//			return true;
//		
//		return false;
//	}
//	
//	private boolean isCRb1( ExpertAssessment assessment )
//	{
//		long locations = assessment.getLocations().getLow();
//		
//		int count = 0;
//		if( assessment.getExtent() < 100 )
//		{
//			if( assessment.isSevereFragmentation() || locations == 1 )
//				count++;
//			if( assessment.isExtentDecline() || assessment.isAreaDecline() || 
//				assessment.isHabitatDecline() || assessment.isLocationDecline() ||
//				assessment.isSubpopulationDecline() || assessment.isPopulationDecline() )
//				count++;
//			if( assessment.isExtentFluctuation() || assessment.isAreaFluctuation() || 
//				assessment.isLocationFluctuation() || 
//				assessment.isSubpopulationFluctuation() || 
//				assessment.isPopulationFluctuation() )
//				count++;
//			
//			if( count >= 2 )
//			
//			return true;
//		}
//		
//		return false;
//	}
//	
//	private boolean isCRb2( ExpertAssessment assessment )
//	{
//		long locations = assessment.getLocations().getLow();
//		
//		int count = 0;
//		if( assessment.getArea() < 10 )
//		{
//			if( assessment.isSevereFragmentation() || locations == 1 )
//				count++;
//			if( assessment.isExtentDecline() || assessment.isAreaDecline() || 
//				assessment.isHabitatDecline() || assessment.isLocationDecline() ||
//				assessment.isSubpopulationDecline() || assessment.isPopulationDecline() )
//				count++;
//			if( assessment.isExtentFluctuation() || assessment.isAreaFluctuation() || 
//				assessment.isLocationFluctuation() || 
//				assessment.isSubpopulationFluctuation() || 
//				assessment.isPopulationFluctuation() )
//				count++;
//			
//			if( count >= 2 )
//			
//			return true;
//		}
//		
//		return false;
//	}
//	
//	private boolean isCRc( ExpertAssessment assessment )
//	{
//		long populationSize = assessment.getPopulationSize().getLow();
//		
//		if( (populationSize < 250 && assessment.getPopulationDeclineGenerations1().getHigh() >= 25) 
//				||
//			(assessment.isPopulationDecline() && assessment.getSubpopulationSize() <= 50) 
//				||
//			( (populationSize != 0 && (assessment.getSubpopulationSize() / populationSize) >= 0.9 )) 
//				||
//			assessment.isPopulationFluctuation() )
//			return true;
//		
//		return false;
//	}
//
//	private boolean isCRd( ExpertAssessment assessment )
//	{
//		return assessment.getPopulationSize().getLow() < 50;
//	}
//
//	private boolean isCRe( ExpertAssessment assessment )
//	{
//		return assessment.getExtinctionGenerations3().getHigh() >= 50;
//	}
//
//	
//	private boolean isENa1( ExpertAssessment assessment )
//	{
//		if( assessment.getPopulationReductionPast() >= 70 &&
//				assessment.getPopulationReductionPastReversible().booleanValue() &&
//				assessment.getPopulationReductionPastUnderstood().booleanValue() &&
//				assessment.getPopulationReductionPastCeased().booleanValue() &&
//				( assessment.getPopulationReductionPastBasis().matches(".*0.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*1.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*2.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*3.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*4.*") ) )
//			return true;
//
//		return false;
//	}
//	
//	private boolean isENa2( ExpertAssessment assessment )
//	{
//		if( assessment.getPopulationReductionPast() >= 50 &&
//				(!assessment.getPopulationReductionPastReversible().booleanValue() ||
//				 !assessment.getPopulationReductionPastUnderstood().booleanValue() ||
//				 !assessment.getPopulationReductionPastCeased().booleanValue() ) &&
//				(   assessment.getPopulationReductionPastBasis().matches(".*0.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*1.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*2.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*3.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*4.*") ) )
//			return true;
//		
//		return false;
//	}
//
//	private boolean isENa3( ExpertAssessment assessment )
//	{
//		if( assessment.getPopulationReductionFuture() >= 50 &&
//				(assessment.getPopulationReductionFutureBasis().matches(".*1.*") ||
//						assessment.getPopulationReductionFutureBasis().matches(".*2.*") ||
//						assessment.getPopulationReductionFutureBasis().matches(".*3.*") ||
//						assessment.getPopulationReductionFutureBasis().matches(".*4.*") ) )
//				return true;
//			
//			return false;
//	}
//	
//	private boolean isENa4( ExpertAssessment assessment )
//	{
//		if( assessment.getPopulationReductionEither() >= 50 &&
//				(!assessment.getPopulationReductionEitherReversible().booleanValue() ||
//				 !assessment.getPopulationReductionEitherUnderstood().booleanValue() ||
//				 !assessment.getPopulationReductionEitherCeased().booleanValue() ) &&
//				(   assessment.getPopulationReductionEitherBasis().matches(".*0.*") ||
//						assessment.getPopulationReductionEitherBasis().matches(".*1.*") ||
//						assessment.getPopulationReductionEitherBasis().matches(".*2.*") ||
//						assessment.getPopulationReductionEitherBasis().matches(".*3.*") ||
//						assessment.getPopulationReductionEitherBasis().matches(".*4.*") ) )
//			return true;
//		
//		return false;
//	}
//	
//	private boolean isENb1( ExpertAssessment assessment )
//	{
//		long locations = assessment.getLocations().getLow();
//		
//		int count = 0;
//		if( assessment.getExtent() < 5000 )
//		{
//			if( assessment.isSevereFragmentation() || locations <= 5 )
//				count++;
//			if( assessment.isExtentDecline() || assessment.isAreaDecline() || 
//				assessment.isHabitatDecline() || assessment.isLocationDecline() ||
//				assessment.isSubpopulationDecline() || assessment.isPopulationDecline() )
//				count++;
//			if( assessment.isExtentFluctuation() || assessment.isAreaFluctuation() || 
//				assessment.isLocationFluctuation() || 
//				assessment.isSubpopulationFluctuation() || 
//				assessment.isPopulationFluctuation() )
//				count++;
//			
//			if( count >= 2 )
//			
//			return true;
//		}
//		
//		return false;
//	}
//	
//	private boolean isENb2( ExpertAssessment assessment )
//	{
//		long locations = assessment.getLocations().getLow();
//		
//		int count = 0;
//		if( assessment.getArea() < 500 )
//		{
//			if( assessment.isSevereFragmentation() || locations <= 5 )
//				count++;
//			if( assessment.isExtentDecline() || assessment.isAreaDecline() || 
//				assessment.isHabitatDecline() || assessment.isLocationDecline() ||
//				assessment.isSubpopulationDecline() || assessment.isPopulationDecline() )
//				count++;
//			if( assessment.isExtentFluctuation() || assessment.isAreaFluctuation() || 
//				assessment.isLocationFluctuation() || 
//				assessment.isSubpopulationFluctuation() || 
//				assessment.isPopulationFluctuation() )
//				count++;
//			
//			if( count >= 2 )
//			
//			return true;
//		}
//		
//		return false;
//	}
//	
//	private boolean isENc1( ExpertAssessment assessment )
//	{
//		long populationSize = assessment.getPopulationSize().getLow();
//		
//		if( populationSize < 2500 && 
//				assessment.getPopulationDeclineGenerations2().getHigh() >= 25 ) 
//			return true;
//		
//		return false;
//	}
//	
//	private boolean isENc2( ExpertAssessment assessment )
//	{
//		long populationSize = assessment.getPopulationSize().getLow();
//		
//		if( (populationSize < 2500 && 
//				( assessment.isPopulationFluctuation() ||
//					(assessment.isPopulationDecline() &&
//						(assessment.getSubpopulationSize() <= 250 || 
//						assessment.getSubpopulationSize() / populationSize >= .95 ) )
//				)
//			) )
//			return true;
//		
//		return false;
//	}
//		
//	private boolean isENd( ExpertAssessment assessment )
//	{
//		return assessment.getPopulationSize().getLow() < 250;
//	}
//
//	private boolean isENe( ExpertAssessment assessment )
//	{
//		return assessment.getExtinctionGenerations5().getHigh() >= 20;
//	}
//	
//	//VU
//	private boolean isVUa1( ExpertAssessment assessment )
//	{
//		if( assessment.getPopulationReductionPast() >= 50 &&
//				assessment.getPopulationReductionPastReversible().booleanValue() &&
//				assessment.getPopulationReductionPastUnderstood().booleanValue() &&
//				assessment.getPopulationReductionPastCeased().booleanValue() &&
//				( assessment.getPopulationReductionPastBasis().matches(".*0.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*1.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*2.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*3.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*4.*") ) )
//			return true;
//
//		return false;
//	}
//	
//	private boolean isVUa2( ExpertAssessment assessment )
//	{
//		if( assessment.getPopulationReductionPast() >= 30 &&
//				(!assessment.getPopulationReductionPastReversible().booleanValue() ||
//				 !assessment.getPopulationReductionPastUnderstood().booleanValue() ||
//				 !assessment.getPopulationReductionPastCeased().booleanValue() ) &&
//				(   assessment.getPopulationReductionPastBasis().matches(".*0.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*1.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*2.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*3.*") ||
//						assessment.getPopulationReductionPastBasis().matches(".*4.*") ) )
//			return true;
//		
//		return false;
//	}
//
//	private boolean isVUa3( ExpertAssessment assessment )
//	{
//		if( assessment.getPopulationReductionFuture() >= 30 &&
//				(assessment.getPopulationReductionFutureBasis().matches(".*1.*") ||
//						assessment.getPopulationReductionFutureBasis().matches(".*2.*") ||
//						assessment.getPopulationReductionFutureBasis().matches(".*3.*") ||
//						assessment.getPopulationReductionFutureBasis().matches(".*4.*") ) )
//				return true;
//			
//			return false;
//	}
//	
//	private boolean isVUa4( ExpertAssessment assessment )
//	{
//		if( assessment.getPopulationReductionEither() >= 30 &&
//				(!assessment.getPopulationReductionEitherReversible().booleanValue() ||
//				 !assessment.getPopulationReductionEitherUnderstood().booleanValue() ||
//				 !assessment.getPopulationReductionEitherCeased().booleanValue() ) &&
//				(   assessment.getPopulationReductionEitherBasis().matches(".*0.*") ||
//						assessment.getPopulationReductionEitherBasis().matches(".*1.*") ||
//						assessment.getPopulationReductionEitherBasis().matches(".*2.*") ||
//						assessment.getPopulationReductionEitherBasis().matches(".*3.*") ||
//						assessment.getPopulationReductionEitherBasis().matches(".*4.*") ) )
//			return true;
//		
//		return false;
//	}
//	
//	private boolean isVUb1( ExpertAssessment assessment )
//	{
//		long locations = assessment.getLocations().getLow();
//		
//		int count = 0;
//		if( assessment.getExtent() < 20000 )
//		{
//			if( assessment.isSevereFragmentation() || locations <= 10 )
//				count++;
//			if( assessment.isExtentDecline() || assessment.isAreaDecline() || 
//				assessment.isHabitatDecline() || assessment.isLocationDecline() ||
//				assessment.isSubpopulationDecline() || assessment.isPopulationDecline() )
//				count++;
//			if( assessment.isExtentFluctuation() || assessment.isAreaFluctuation() || 
//				assessment.isLocationFluctuation() || 
//				assessment.isSubpopulationFluctuation() || 
//				assessment.isPopulationFluctuation() )
//				count++;
//			
//			if( count >= 2 )
//			
//			return true;
//		}
//		
//		return false;
//	}
//	
//	private boolean isVUb2( ExpertAssessment assessment )
//	{
//		long locations = assessment.getLocations().getLow();
//		
//		int count = 0;
//		if( assessment.getArea() < 2000 )
//		{
//			if( assessment.isSevereFragmentation() || locations <= 10 )
//				count++;
//			if( assessment.isExtentDecline() || assessment.isAreaDecline() || 
//				assessment.isHabitatDecline() || assessment.isLocationDecline() ||
//				assessment.isSubpopulationDecline() || assessment.isPopulationDecline() )
//				count++;
//			if( assessment.isExtentFluctuation() || assessment.isAreaFluctuation() || 
//				assessment.isLocationFluctuation() || 
//				assessment.isSubpopulationFluctuation() || 
//				assessment.isPopulationFluctuation() )
//				count++;
//			
//			if( count >= 2 )
//			
//			return true;
//		}
//		
//		return false;
//	}
//	
//	private boolean isVUc1( ExpertAssessment assessment )
//	{
//		long populationSize = assessment.getLocations().getLow();
//
//		if( populationSize < 10000 && 
//				assessment.getPopulationDeclineGenerations3().getHigh() >= 10 ) 
//			return true;
//		
//		return false;
//	}
//	
//	private boolean isVUc2( ExpertAssessment assessment )
//	{
//		long populationSize = assessment.getLocations().getLow();
//
//		if( (populationSize < 10000 && 
//				( assessment.isPopulationFluctuation() ||
//					(assessment.isPopulationDecline() &&
//						(assessment.getSubpopulationSize() <= 250 || 
//						assessment.getSubpopulationSize() / populationSize >= .95 ) )
//				)
//			) )
//			return true;
//		
//		return false;
//	}
//	
//	private boolean isVUd1( ExpertAssessment assessment )
//	{
//		return assessment.getPopulationSize().getLow() < 1000;
//	}
//
//	private boolean isVUd2( ExpertAssessment assessment )
//	{
//		return assessment.isAreaRestricted();
//	}
//
//	private boolean isVUe( ExpertAssessment assessment )
//	{
//		return assessment.getExtinctionGenerations5().getHigh() >= 10;
//	}
//}
