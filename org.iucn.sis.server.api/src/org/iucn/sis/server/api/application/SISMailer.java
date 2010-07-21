package org.iucn.sis.server.api.application;

import com.solertium.mail.GMailer;
import com.solertium.mail.Mailer;

public class SISMailer {

	public static Mailer getGMailer() {
		Mailer mailer = new GMailer("network@iucnsis.org", "sist3l3port");
		
		return mailer;
	}
}
