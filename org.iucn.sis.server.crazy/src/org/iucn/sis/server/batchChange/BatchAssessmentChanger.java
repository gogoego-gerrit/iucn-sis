package org.iucn.sis.server.batchChange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.utils.XMLUtils;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;

import com.solertium.util.SysDebugger;
import com.solertium.vfs.VFS;

public class BatchAssessmentChanger {
	public static boolean changeAssessment(VFS vfs, AssessmentData assessment, HashMap newData, boolean overwrite,
			boolean append, String type, String username) {
		SysDebugger.getNamedInstance("info").println(
				"Changing assessment " + assessment.getAssessmentID() + ":" + assessment.getType());
		HashMap curData = assessment.getDataMap();
		boolean changed = false;

		for (Iterator<String> iter = newData.keySet().iterator(); iter.hasNext();) {
			String curField = iter.next();

			if (curData.containsKey(curField)) {
				if (append
						&& (curField.endsWith("Documentation") || curField.equalsIgnoreCase("TaxonomicNotes") || curField
								.equalsIgnoreCase("RedListRationale"))) {
					String cur = ((ArrayList) curData.get(curField)).get(0).toString();
					cur += "<br>" + ((ArrayList) newData.get(curField)).get(0).toString();
					((ArrayList) curData.get(curField)).remove(0);
					((ArrayList) curData.get(curField)).add(0, cur);

					changed = true;
				} else if (overwrite) {
					if (newData.get(curField) != null)
						curData.put(curField, newData.get(curField));
					else
						curData.remove(curField);

					changed = true;
				} else if (newData.get(curField) != null) {
					Object object = curData.get(curField);
					if (object instanceof ArrayList) {
						ArrayList cur = (ArrayList) object;
						for (int i = 0; i < cur.size(); i++) {
							if (((ArrayList) newData.get(curField)).size() > i
									&& cur.get(i).toString().equalsIgnoreCase("")
									&& !cur.get(i).equals(((ArrayList) newData.get(curField)).get(i))) {
								cur.set(i, ((ArrayList) newData.get(curField)).get(i));
								changed = true;
							}
						}
					} else {
						HashMap cur = (HashMap) object;
						if (cur.keySet().size() == 0) {
							curData.put(curField, newData.get(curField));
							changed = true;
						}
					}
				}
			} else if (newData.get(curField) != null) {
				curData.put(curField, newData.get(curField));
				changed = true;
			}
		}

		return changed;
	}

	/**
	 * Changes the draft assessment to have the same data that is in the newData hashmap, will append for narative fields if
	 * selected and may overwrite data in other fields if selected
	 * 
	 * @param vfs
	 * @param assessments
	 * @param regions
	 * @param username
	 * @param newData
	 * @param append
	 * @param overwrite
	 * @return
	 */
	public synchronized static String changeDraftAssessments(VFS vfs, final List<AssessmentData> assessments, final List<String> regions, 
			final String username, final HashMap newData, final boolean append, final boolean overwrite) {
		if (assessments == null || assessments.isEmpty())
			return "";

		StringBuffer changedIDs = new StringBuffer("");
		StringBuffer changedSummary = new StringBuffer("");
		int numChanged = 0;

		try {

			List<AssessmentData> toSave = new ArrayList<AssessmentData>();
			
			for (AssessmentData cur : assessments) {
				try {
					if (changeAssessment(vfs, cur, newData, overwrite, append, BaseAssessment.DRAFT_ASSESSMENT_STATUS,
							username)) {
						toSave.add(cur);
						
						changedIDs.append(cur.getAssessmentID());
						changedIDs.append(",");

						changedSummary.append("Changed draft assessment for <i>" + cur.getSpeciesName() + "</i><br>");
						numChanged++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (changedIDs.length() > 0 && changedIDs.charAt(changedIDs.length() - 1) == ',')
				changedIDs.deleteCharAt(changedIDs.length() - 1);

			if (numChanged > 0) {
				AssessmentIO.writeAssessments(toSave, username, vfs, true);
				changedSummary.insert(0, "<b>Changed " + numChanged + " draft assessments.</b><br><br>");
			} else
				changedSummary.append("<br><i>No changes were needed.</i><br>");
		} catch (Exception e) {
			e.printStackTrace();
			changedSummary.append("<br/><i>Error fetching draft assessments to batch change. Please report "
					+ "this issue and the timestamp " + new java.util.Date().toGMTString()
					+ " to an SIS administrator.</i><br/>");
		}

		StringBuffer ret = new StringBuffer();

		ret.append("<draftIDs>");
		ret.append(XMLUtils.clean(changedIDs.toString()));
		ret.append("</draftIDs>\n");

		ret.append("<draftSummary>");
		ret.append(XMLUtils.clean(changedSummary.toString()));
		ret.append("</draftSummary>\n");

		return ret.toString();
	}

	public synchronized static String changePublishedAssessments(VFS vfs, final List<AssessmentData> assessments, String username,
			final HashMap newData, final boolean overwrite, final boolean append) {
		if (assessments == null || assessments.isEmpty())
			return "";

		StringBuffer changedIDs = new StringBuffer("");
		StringBuffer changedSummary = new StringBuffer("");
		int numChanged = 0;

		try {
			List<AssessmentData> toSave = new ArrayList<AssessmentData>();

			for (AssessmentData cur : assessments) {
				if (cur.isHistorical())
					continue;

				try {
					if (changeAssessment(vfs, cur, newData, overwrite, append,
							BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, username)) {
						toSave.add(cur);
						
						changedIDs.append(cur.getAssessmentID());
						changedIDs.append(",");

						changedSummary.append("Changed published assessment for <i>" + cur.getSpeciesName()
								+ "</i><br>");
						numChanged++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (changedIDs.length() > 0 && changedIDs.charAt(changedIDs.length() - 1) == ',')
				changedIDs.deleteCharAt(changedIDs.length() - 1);

			if (numChanged > 0) {
				AssessmentIO.writeAssessments(toSave, username, vfs, true);
				changedSummary.insert(0, "<br><i>Changed " + numChanged + " draft assessments.</i><br><br>");
			} else
				changedSummary.append("<br><i>No changes were needed.</i><br>");
		} catch (Exception e) {
			e.printStackTrace();
			changedSummary.append("<br><i>Error fetching published assessments for batch change. Please report "
					+ "this issue and the timestamp " + new java.util.Date().toGMTString()
					+ " to an SIS administrator.</i><br>");
		}

		StringBuffer ret = new StringBuffer();

		ret.append("<publishedIDs>");
		ret.append(XMLUtils.clean(changedIDs.toString()));
		ret.append("</publishedIDs>\n");

		ret.append("<publishedSummary>");
		ret.append(XMLUtils.clean(changedSummary.toString()));
		ret.append("</publishedSummary>\n");

		return ret.toString();
	}
}
