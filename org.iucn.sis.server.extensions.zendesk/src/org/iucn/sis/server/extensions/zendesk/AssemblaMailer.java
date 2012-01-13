package org.iucn.sis.server.extensions.zendesk;

import java.util.Properties;

import org.gogoego.api.mail.InstanceMailer;
import org.iucn.sis.shared.api.models.User;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.solertium.mail.Mailer;

public class AssemblaMailer {
	
	private final User user;
	private final Properties settings;
	private final Mailer mailer;
	
	private String subject;
	private String body;
	private String reporter;
	
	public AssemblaMailer(User user, Properties settings) {
		this(user, settings, InstanceMailer.getInstance().getMailer());
	}
	
	public AssemblaMailer(User user, Properties settings, Mailer mailer) {
		this.user = user;
		this.settings = settings;
		this.mailer = mailer;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public void setReporter(String reporter) {
		this.reporter = reporter;
	}
	
	public void send() throws ResourceException {
		if (subject == null || body == null || reporter == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a subject and a body.");
		
		final StringBuilder out = new StringBuilder();
		out.append("Reporter Name: " + reporter + "\n");
		out.append("Reporter Username: " + user.getUsername() + "\n");
		out.append("Affiliation: " + user.getAffiliation() + "\n");
		out.append("Component: Support\n");
		out.append("Description:\n" + body + "\n.");
		
		final String assignedTo = settings.getProperty(Settings.ASSEMBLA_ASSIGNED);
		if (assignedTo != null)
			out.append("Assigned-to: " + assignedTo + "\n");
		
		mailer.setTo(settings.getProperty(Settings.ASSEMBLA_EMAIL, "sis@support.assembla.com"));
		mailer.setSubject(subject);
		mailer.setBody(out.toString());
		
		try {
			mailer.background_send();
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

}
