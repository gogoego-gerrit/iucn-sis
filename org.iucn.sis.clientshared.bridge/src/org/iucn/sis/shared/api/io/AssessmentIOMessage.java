package org.iucn.sis.shared.api.io;

import java.util.ArrayList;

import org.iucn.sis.shared.api.models.Assessment;

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

	private ArrayList<Assessment> successfullySaved;
	private ArrayList<Assessment> insufficientPermissions;
	private ArrayList<Assessment> locked;
	private ArrayList<Assessment> failed;

	private String message;

	
	/**
	 * Creates a blank IO message.
	 */
	public AssessmentIOMessage() {
		successfullySaved = new ArrayList<Assessment>();
		insufficientPermissions = new ArrayList<Assessment>();
		locked = new ArrayList<Assessment>();
		failed = new ArrayList<Assessment>();

		message = null;
	}

	

	public void addInsufficientPermissions(Assessment assessment) {
		insufficientPermissions.add(assessment);
	}

	public void addLocked(Assessment assessment) {
		locked.add(assessment);
	}
	
	public void addFailed(Assessment assessment) {
		locked.add(assessment);
	}

	public void addSuccessfullySaved(Assessment assessment) {
		successfullySaved.add(assessment);
	}

	public ArrayList<Assessment> getSuccessfullySaved() {
		return successfullySaved;
	}
	
	public ArrayList<Assessment> getFailed() {
		return failed;
	}
	
	public String getMessage() {

		if (message != null) {
			StringBuilder ret = new StringBuilder();
			if (successfullySaved.isEmpty()) {
				ret.append("No assessments successfully saved.");
			} else {
				ret.append("Assessments successfully saved:\n");
				for (Assessment curAssessment : successfullySaved)
					ret.append(curAssessment.getId() + " for species " + curAssessment.getSpeciesName());
			}

			if (insufficientPermissions.isEmpty()) {
				ret.append("No assessments skipped because of insufficient permissions.");
			} else {
				ret.append("Assessments NOT saved because of insufficient permissions:\n");
				for (Assessment curAssessment : insufficientPermissions)
					ret.append(curAssessment.getId() + " for species " + curAssessment.getSpeciesName());
			}

			if (locked.isEmpty()) {
				ret.append("No assessments skipped because another user has it locked.");
			} else {
				ret.append("Assessments NOT saved because another user has it locked:\n");
				for (Assessment curAssessment : locked)
					ret.append(curAssessment.getId() + " for species " + curAssessment.getSpeciesName());
			}
			
			if (failed.isEmpty()) {
				ret.append("No assessments failed to save because of error.");
			} else {
				ret.append("Assessments NOT saved because of server error: \n");
				for (Assessment curAssessment : locked)
					ret.append(curAssessment.getId() + " for species " + curAssessment.getSpeciesName());
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
			for (Assessment curAssessment : successfullySaved)
				ret.append("<li>" + curAssessment.getType().replace("_status", "") + " "
						+ curAssessment.getId() + " for species " + curAssessment.getSpeciesName() + "</li>");
			ret.append("</ul><br>");
		}

		if (insufficientPermissions.isEmpty()) {
			ret.append("No assessments skipped because of insufficient permissions.<br><br>");
		} else {
			ret.append("Assessments NOT saved because of insufficient permissions:<br><ul>");
			for (Assessment curAssessment : insufficientPermissions)
				ret.append("<li>" + curAssessment.getType().replace("_status", "") + " "
						+ curAssessment.getId() + " for species " + curAssessment.getSpeciesName() + "</li>");
			ret.append("</ul><br>");
		}

		if (locked.isEmpty()) {
			ret.append("No assessments skipped because another user has it locked.<br><br>");
		} else {
			ret.append("Assessments NOT saved because another user has it locked:<br><ul>");
			for (Assessment curAssessment : locked)
				ret.append("<li>" + curAssessment.getType().replace("_status", "") + " "
						+ curAssessment.getId() + " for species " + curAssessment.getSpeciesName() + "</li>");
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
			for (Assessment curAssessment : successfullySaved)
				ret.append(curAssessment.getId() + " for species " + curAssessment.getSpeciesName());
		}

		if (insufficientPermissions.isEmpty()) {
			ret.append("No assessments skipped because of insufficient permissions.");
		} else {
			ret.append("Assessments NOT saved because of insufficient permissions:\n");
			for (Assessment curAssessment : insufficientPermissions)
				ret.append(curAssessment.getId() + " for species " + curAssessment.getSpeciesName());
		}

		if (locked.isEmpty()) {
			ret.append("No assessments skipped because another user has it locked.");
		} else {
			ret.append("Assessments NOT saved because another user has it locked:\n");
			for (Assessment curAssessment : locked)
				ret.append(curAssessment.getId() + " for species " + curAssessment.getSpeciesName());
		}

		return ret.toString();
	}
}
