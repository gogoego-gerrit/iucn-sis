/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */

package com.solertium.mail;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Mailer.java
 * 
 * Utility class for sending e-mail.  Must be configured with credentials for
 * a valid e-mail server.  This can simply send via a public account like gmail,
 * hotmail, etc., use a local private SMTP server, or a hosted relay service
 * like AuthSMTP.
 * 
 * @author carl.scott and rob.heittman
 */
public class Mailer implements Runnable {

	private Collection<? extends DataSource> attachments;

	private String bcc = null;

	private String body = "";
	private String cc = null;

	private String contentType = "text/plain; charset=UTF-8";
	private String from;
	private final Authenticator authenticator;

	private Properties props = new Properties();
	private String replyTo = null;

	private String subject = "Message from Web Site";
	private String to = null;

	/**
	 * A Mailer must now be explicitly constructed with the server, port,
	 * SSL settings, and account/password to be used for authentication.
	 * 
	 * There is no default at the level of this utility class.
	 * 
	 * @param server Server name, e.g. smtp.gmail.com
	 * @param port Submission port, e.g. 587
	 * @param ssl True or false -- true for gmail
	 * @param account Account to use for mail server
	 * @param password Password to use for mail server
	 */
	public Mailer(String server, int port, boolean ssl, final String account, final String password) {
		props.setProperty("mail.smtp.host", server);
		props.setProperty("mail.smtp.port", ""+port);
		from = account;
		if(password!=null){
			props.setProperty("mail.smtp.user", account);
			props.setProperty("mail.smtp.submitter", account);
			props.setProperty("mail.smtp.auth", "true");
			props.setProperty("mail.smtp.user", password);
			authenticator = new Authenticator(){
				@Override
				public PasswordAuthentication getPasswordAuthentication(){
					return new PasswordAuthentication(account, password);
				}
			};
		} else {
			authenticator = null;
		}
		if(ssl==true){
			props.setProperty("mail.smtp.ssl.enable", "true");			
			props.setProperty("mail.smtp.starttls.enable", "true");			
		}
	}
	
	public void setAttachments(Collection<? extends DataSource> attachments) {
		this.attachments = attachments;
	}

	public void addBodyLine(final String body) {
		this.body = this.body + body + "\n";
	}

	public void background_send() throws Exception {
		new Thread(this).start();
	}

	private Message getMessage(final Session msession) throws Exception {
		final MimeMessage message = new MimeMessage(msession);
		
		message.setFrom(new InternetAddress(from));
		if (replyTo != null) {
			final InternetAddress[] iarray = new InternetAddress[1];
			iarray[0] = new InternetAddress(replyTo);
			message.setReplyTo(iarray);
		}
		if (to.indexOf(";") > -1) {
			final StringTokenizer tok = new StringTokenizer(to, ";");
			while (tok.hasMoreTokens()) {
				final String nextRecipient = tok.nextToken();
				message.addRecipient(Message.RecipientType.TO,
						new InternetAddress(nextRecipient));
			}
		} else
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					to));
		if (cc != null)
			if (cc.indexOf(";") > -1) {
				final StringTokenizer tok = new StringTokenizer(cc, ";");
				while (tok.hasMoreTokens()) {
					final String nextRecipient = tok.nextToken();
					message.addRecipient(Message.RecipientType.CC,
							new InternetAddress(nextRecipient));
				}
			} else
				message.addRecipient(Message.RecipientType.CC,
						new InternetAddress(cc));
		if (bcc != null)
			if (bcc.indexOf(";") > -1) {
				final StringTokenizer tok = new StringTokenizer(bcc, ";");
				while (tok.hasMoreTokens()) {
					final String nextRecipient = tok.nextToken();
					message.addRecipient(Message.RecipientType.BCC,
							new InternetAddress(nextRecipient));
				}
			} else
				message.addRecipient(Message.RecipientType.BCC,
						new InternetAddress(bcc));
		message.setSubject(subject);
		message.setSentDate(new Date());
		if (attachments != null && !attachments.isEmpty()) {
			final Multipart multipart = new MimeMultipart();
			final BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(body, contentType);
			multipart.addBodyPart(messageBodyPart);
			for (final DataSource dataSource : attachments) {
				final BodyPart dataBodyPart = new MimeBodyPart();
				dataBodyPart.setDataHandler(new DataHandler(dataSource));
				dataBodyPart.setFileName(dataSource.getName());
				multipart.addBodyPart(dataBodyPart);
			}
			message.setContent(multipart);
		} else
			message.setContent(body, contentType);
		return message;
	}

	public MimeMessage parse(final String messageSource) throws Exception {
		final Session msession = Session.getInstance(props);
		return new MimeMessage(msession, new ByteArrayInputStream(messageSource
				.getBytes()));
	}

	public void run() {
		try {
			send();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	public void send() throws Exception {
		final Session msession;
		if(authenticator!=null){
			msession = Session.getInstance(props, authenticator);
		} else {
			msession = Session.getInstance(props);
		}
		final Message message = getMessage(msession);
		Transport.send(message);
	}

	/**
	 * @param from -
	 *            email address of the sender
	 * @param replyto -
	 *            reply address of the sender
	 * @param to -
	 *            email address(es) of the recipient - semicolon delimited if
	 *            need be
	 * @param subject -
	 *            subject line
	 * @param body -
	 *            the plaintext body of the email
	 */
	public void send(final String ifrom, final String ireplyTo,
			final String ito, final String icc, final String isubject,
			final String ibody) throws Exception {
		setFrom(ifrom);
		setReplyTo(ireplyTo);
		setTo(ito);
		setCC(icc);
		setSubject(isubject);
		setBody(ibody);
		send();
	}

	public void setBCC(final String bcc) {
		this.bcc = bcc;
	}

	public void setBody(final String body) {
		this.body = body;
	}

	public void setCC(final String cc) {
		this.cc = cc;
	}

	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	public void setFrom(final String from) {
		this.from = from;
	}

	public void setReplyTo(final String replyTo) {
		this.replyTo = replyTo;
	}

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	public void setTo(final String to) {
		this.to = to;
	}

}