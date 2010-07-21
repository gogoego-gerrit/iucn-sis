package org.iucn.sis.shared.io;

import java.util.ArrayList;

import org.iucn.sis.shared.data.assessments.AssessmentData;

/**
 * When an AssessmentIO operation occurs, an instance of this message Object is
 * returned. The message is, in String format, a readable summary of the outcome
 * of the operation.
 * 
 * TODO: When serialized to XML, the message can be sent from server to client,
 * where the single-argument constructor will recreate the Object.
 * 
 * @author adam.schwartz
 * 
 */
public class AssessmentIOMessage {

	private ArrayList<AssessmentData> successfullySaved;
	private ArrayList<AssessmentData> insufficientPermissions;
	private ArrayList<AssessmentData> locked;

	private String message;

	
	/**
	 * Creates a blank IO message.
	 */
	public AssessmentIOMessage() {
		successfullySaved = new ArrayList<AssessmentData>();
		insufficientPermissions = new ArrayList<AssessmentData>();
		locked = new ArrayList<AssessmentData>();

		message = null;
	}


	public void addInsufficientPermissions(AssessmentData assessment) {
		insufficientPermissions.add(assessment);
	}

	public void addLocked(AssessmentData assessment) {
		locked.add(assessment);
	}

	public void addSuccessfullySaved(AssessmentData assessment) {
		successfullySaved.add(assessment);
	}

	public ArrayList<AssessmentData> getSuccessfullySaved() {
		return successfullySaved;
	}
	
	public String getMessage() {

		if (message != null) {
			StringBuilder ret = new StringBuilder();
			if (successfullySaved.isEmpty()) {
				ret.append("No assessments successfully saved.");
			} else {
				ret.append("Assessments successfully saved:\n");
				for (AssessmentData curAssessment : successfullySaved)
					ret.append(curAssessment.getAssessmentID() + " for species " + curAssessment.getSpeciesName());
			}

			if (insufficientPermissions.isEmpty()) {
				ret.append("No assessments skipped because of insufficient permissions.");
			} else {
				ret.append("Assessments NOT saved because of insufficient permissions:\n");
				for (AssessmentData curAssessment : insufficientPermissions)
					ret.append(curAssessment.getAssessmentID() + " for species " + curAssessment.getSpeciesName());
			}

			if (locked.isEmpty()) {
				ret.append("No assessments skipped because another user has it locked.");
			} else {
				ret.append("Assessments NOT saved because another user has it locked:\n");
				for (AssessmentData curAssessment : locked)
					ret.append(curAssessment.getAssessmentID() + " for species " + curAssessment.getSpeciesName());
			}

			message = ret.toString();
		}

		return message;
	}

	public String regenerateMessage() {
		message = null;
		return getMessage();
	}

	public String toHTML() {
		StringBuilder ret = new StringBuilder();
		if (successfullySaved.isEmpty()) {
			ret.append("No assessments successfully saved.<br><br>");
		} else {
			ret.append("Assessments successfully saved:<br><ul>");
			for (AssessmentData curAssessment : successfullySaved)
				ret.append("<li>" + curAssessment.getType().replace("_status", "") + " "
						+ curAssessment.getAssessmentID() + " for species " + curAssessment.getSpeciesName() + "</li>");
			ret.append("</ul><br>");
		}

		if (insufficientPermissions.isEmpty()) {
			ret.append("No assessments skipped because of insufficient permissions.<br><br>");
		} else {
			ret.append("Assessments NOT saved because of insufficient permissions:<br><ul>");
			for (AssessmentData curAssessment : insufficientPermissions)
				ret.append("<li>" + curAssessment.getType().replace("_status", "") + " "
						+ curAssessment.getAssessmentID() + " for species " + curAssessment.getSpeciesName() + "</li>");
			ret.append("</ul><br>");
		}

		if (locked.isEmpty()) {
			ret.append("No assessments skipped because another user has it locked.<br><br>");
		} else {
			ret.append("Assessments NOT saved because another user has it locked:<br><ul>");
			for (AssessmentData curAssessment : locked)
				ret.append("<li>" + curAssessment.getType().replace("_status", "") + " "
						+ curAssessment.getAssessmentID() + " for species " + curAssessment.getSpeciesName() + "</li>");
			ret.append("</ul><br>");
		}

		return ret.toString();
	}

	@Override
	public String toString() {
		return getMessage();
	}

	public String toXML() {
		StringBuilder ret = new StringBuilder();
		if (successfullySaved.isEmpty()) {
			ret.append("No assessments successfully saved.");
		} else {
			ret.append("Assessments successfully saved:\n");
			for (AssessmentData curAssessment : successfullySaved)
				ret.append(curAssessment.getAssessmentID() + " for species " + curAssessment.getSpeciesName());
		}

		if (insufficientPermissions.isEmpty()) {
			ret.append("No assessments skipped because of insufficient permissions.");
		} else {
			ret.append("Assessments NOT saved because of insufficient permissions:\n");
			for (AssessmentData curAssessment : insufficientPermissions)
				ret.append(curAssessment.getAssessmentID() + " for species " + curAssessment.getSpeciesName());
		}

		if (locked.isEmpty()) {
			ret.append("No assessments skipped because another user has it locked.");
		} else {
			ret.append("Assessments NOT saved because another user has it locked:\n");
			for (AssessmentData curAssessment : locked)
				ret.append(curAssessment.getAssessmentID() + " for species " + curAssessment.getSpeciesName());
		}

		return ret.toString();
	}
}
