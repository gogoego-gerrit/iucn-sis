package org.iucn.sis.server.workflow;

import java.util.Collection;

import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.workflow.WorkflowStatus;
import org.iucn.sis.shared.workflow.WorkflowUserInfo;

import com.solertium.mail.GMailer;
import com.solertium.mail.Mailer;
import com.solertium.util.TrivialExceptionHandler;

public class WorkflowStatusMailer {
	
	private final WorkflowUserInfo sender;
	private final WorkflowStatus newStatus;
	private final WorkflowComment comment;
	
	private final WorkingSetData workingSet;
	
	private StringBuilder bodyBuilder;
	
	public WorkflowStatusMailer(WorkflowUserInfo sender, WorkflowStatus newStatus, WorkingSetData workingSet, WorkflowComment comment) {
		this.workingSet = workingSet;
		this.newStatus = newStatus;
		this.sender = sender;
		this.comment = comment;
	}
	
	public void send(final Collection<WorkflowUserInfo> recipients) {
		final Mailer mailer = new GMailer("gogoego.tests@gmail.com", "gg3t3sts");//SISMailer.getGMailer();
		mailer.setSubject("SIS working set submission notification");
		
		for (WorkflowUserInfo recipient : recipients) {
			mailer.setTo(recipient.getEmailForMailer());
			mailer.setTo("carl.scott@solertium.com");
			
			mailer.setBody(buildBody(recipient));
			
			try {
				mailer.background_send();
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
	}
	
	private String buildBody(WorkflowUserInfo recipient) {
		bodyBuilder = new StringBuilder();
		
		append("Dear " + recipient.getName(), 2);
		append("The following working set within SIS has " +
			"been submitted to you for " + newStatus.getEmailFriendlyDesc() + ":", 2);

		append("Working Set: " + workingSet.getWorkingSetName());
		append("Working Set Owner: " + workingSet.getCreator());
		append("Working Set Status: " + workingSet.getWorkflowStatus());
		append("Working Set Description: " + workingSet.getDescription());
		append("Number of species: " + Integer.toString(workingSet.getSpeciesIDs().size()));
		append("Submission notes: " + comment.getComment());
		append("Person submitting: " + sender.getName(), 2);
		append("Please log into SIS and review the assessments; be sure " +
			"to provide appropriate notes and to change the status, if necessary, " +
			"to keep the assessments moving through the submission process.", 2);
		append("Thank you.", 2);
		append("SIS Administration.");
		
		return bodyBuilder.toString();
	}
	
	private void append(String text) {
		append(text, 1);
	}
	
	private void append(String text, int newLines) {
		bodyBuilder.append(text);
		for (int i = 0; i < newLines; i++)
			bodyBuilder.append("\n");
	}

}
