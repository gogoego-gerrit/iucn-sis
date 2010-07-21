package org.iucn.sis.server.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;

public class MostRecentFlagger {

	/**
	 * Given a list of assessments for a single taxon, this method will peruse the list
	 * and find the most recent assessment for each permutation of regions, flagging it
	 * appropriately using the isHistorical flag. Returns a list of assessments it had to
	 * change to be written back.
	 *  
	 * @param List of assessments for a single taxon
	 */
	public static List<AssessmentData> flagMostRecentInList(List<AssessmentData> assessments) {
		List<AssessmentData> changed = new ArrayList<AssessmentData>();
		HashMap<String, AssessmentData> mostRecent = new HashMap<String, AssessmentData>();
		
		Collections.sort(assessments, new Comparator<AssessmentData>() {
			public int compare(AssessmentData o1, AssessmentData o2) {
				Date date2 = parseDate(o2.getDateAssessed());
				Date date1 = parseDate(o1.getDateAssessed());
				
				if( date2 == null )
					return -1;
				else if( date1 == null )
					return 1;
				
				int ret = date2.compareTo(date1);
				
				if( ret == 0 ) {
					if( o2.isHistorical() )
						ret = -1;
					else
						ret = 1;
				}
				
				return ret;
			}
		});
		
		for( AssessmentData curAss : assessments ) {
			boolean isGlobal = curAss.isGlobal() || curAss.isEndemic();
			boolean isRegional = curAss.isRegional();

			if( isGlobal ) {
				if( mostRecent.containsKey("Global")) {
					AssessmentData latest = mostRecent.get("Global");
					if( !curAss.getDateAssessed().equals(latest.getDateAssessed()) ) {
						if( !curAss.isHistorical() ) {
							System.out.println("Assessment " + curAss.getAssessmentID() + " should be historical global.");
							curAss.setHistorical(true);
							changed.add(curAss);
						}
					}
				} else if( curAss.getDataPiece(1, CanonicalNames.RedListCriteria, "").equals("2") ) {
					if( !curAss.isHistorical() ) {
						System.out.println("Assessment " + curAss.getAssessmentID() + " should be historical because of crit version.");
						curAss.setHistorical(true);
						changed.add(curAss);
					}
				} else {
					if( curAss.isHistorical() ) {
						System.out.println("Assessment " + curAss.getAssessmentID() + " should be most recent global.");
						curAss.setHistorical(false);
						changed.add(curAss);
					}

					mostRecent.put("Global", curAss);
				}
			}
			
			if( isRegional ) {
				if( mostRecent.containsKey(curAss.getRegionIDs())) {
					AssessmentData latest = mostRecent.get(curAss.getRegionIDs());
					if( !curAss.getDateAssessed().equals(latest.getDateAssessed()) ) {
						if( !curAss.isHistorical() ) {
							System.out.println("Assessment " + curAss.getAssessmentID() + " should be historical for region " + curAss.getRegionIDs() + ".");
							curAss.setHistorical(true);
							changed.add(curAss);
						}
					}
				} else if( curAss.getDataPiece(1, CanonicalNames.RedListCriteria, "").equals("2") ) {
					if( !curAss.isHistorical() ) {
						System.out.println("Assessment " + curAss.getAssessmentID() + " should be historical because of crit version.");
						curAss.setHistorical(true);
						changed.add(curAss);
					}
				} else {
					if( curAss.isHistorical() ) {
						System.out.println("Assessment " + curAss.getAssessmentID() + " should be most recent for region " + curAss.getRegionIDs() + ".");
						curAss.setHistorical(false);
						changed.add(curAss);
					}

					mostRecent.put(Arrays.toString(curAss.getRegionIDs().toArray()), curAss);
				}
			}
		}
		
		return changed;
	}
	
	private static Date parseDate(String dateString) {
		Date date = null;
		
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString.substring(0, 10));
		} catch (ParseException e) {
			try {
				date = new SimpleDateFormat("yyyy/MM/dd").parse(dateString.substring(0, 10));
			} catch (ParseException e1) {
				System.out.println("Could not parse date string " + dateString);
			}
		} catch (Exception e) {
			System.out.println("Could not parse date string " + dateString);
		}
		
		return date;
	}
}
