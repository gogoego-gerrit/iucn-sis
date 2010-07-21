package org.iucn.sis.server.extensions.batchchanges;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.utils.XMLUtils;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.User;

import com.solertium.util.SysDebugger;

public class BatchAssessmentChanger {
	
	
	public static boolean changeAssessment(Assessment assessment, Assessment newData, boolean overwrite, boolean append, String type, User username) {
		SysDebugger.getNamedInstance("info").println(
				"Changing assessment " + assessment.getAssessmentID() + ":" + assessment.getType());
		boolean changed = false;

		for (Field field : newData.getField()) {
			Field oldField = assessment.getField(field.getName());
			if (oldField == null) {
				oldField = new Field();
			}
			
			changed |= field.copyInto(oldField, append, overwrite);
		}

		return changed;
	}

	/**
	 * Changes the draft assessment to have the same data that is in the newData hashmap, will append for narative fields if
	 * selected and may overwrite data in other fields if selected
	 * @param assessments
	 * @param newData
	 * @param append
	 * @param overwrite
	 * @param username
	 * @param vfs
	 * @param regions
	 * 
	 * @return
	 */
	public synchronized static String changeDraftAssessments(final List<Assessment> assessments, //final List<String> regions, 
			final Assessment newData, final boolean append, final boolean overwrite, final User username) {
		if (assessments == null || assessments.isEmpty())
			return "";

		StringBuffer changedIDs = new StringBuffer("");
		StringBuffer changedSummary = new StringBuffer("");
		int numChanged = 0;

		try {

			List<Assessment> toSave = new ArrayList<Assessment>();
			
			for (Assessment cur : assessments) {
				try {
					if (changeAssessment(cur, newData, overwrite, append, AssessmentType.DRAFT_ASSESSMENT_TYPE,
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
				SIS.get().getAssessmentIO().writeAssessments(toSave, username, true);
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

	public synchronized static String changePublishedAssessments(final List<Assessment> assessments, Assessment newDataAssessment,
			final boolean append, final boolean overwrite, User username) {
		if (assessments == null || assessments.isEmpty())
			return "";

		StringBuffer changedIDs = new StringBuffer("");
		StringBuffer changedSummary = new StringBuffer("");
		int numChanged = 0;

		try {
			List<Assessment> toSave = new ArrayList<Assessment>();

			for (Assessment cur : assessments) {
				if (cur.getIsHistorical())
					continue;

				try {
					if (changeAssessment(cur, newDataAssessment, overwrite, append,
							AssessmentType.PUBLISHED_ASSESSMENT_TYPE, username)) {
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
				SIS.get().getAssessmentIO().writeAssessments(toSave, username, true);
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
