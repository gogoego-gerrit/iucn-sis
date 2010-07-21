package org.iucn.sis.server.api.application;

import java.util.Date;

import javax.security.auth.login.LoginContext;

import org.iucn.sis.shared.api.models.User;

import com.solertium.db.ExecutionContext;
import com.solertium.mail.Mailer;
import com.solertium.util.MD5Hash;
import com.solertium.util.restlet.authentication.DBAuthenticator;
import com.solertium.util.restlet.authentication.Authenticator.AccountExistsException;

public class SISDBAuthenticator extends DBAuthenticator {

	public SISDBAuthenticator(ExecutionContext ec) {
		super(ec, "user", "username", "password");
		
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public String putNewAccount(String login, String password) throws AccountExistsException {
		if (doesAccountExist(login))
			throw new AccountExistsException();
		System.out.println("in putNewAccount");
		User user = SIS.get().getUserIO().getUserFromUsername(login);
		if (user == null) {
			user = new User();
			user.setUsername(login);
			user.setEmail(login);
			user.setFirstName("");
			user.setLastName("");
		} else if (user.state == User.DELETED) {
			user.state = User.ACTIVE;
		} else {
			throw new AccountExistsException("User already exists");
		}
		user.setPassword(translatePassword(login, password));
		user.setSisUser(true);
		user.setRapidlistUser(false);
		
		System.out.println("before trying to save");
		try{
		SIS.get().getUserIO().saveUser(user);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AccountExistsException(e.getLocalizedMessage());
		}
		System.out.println("after trying to save with id " + user.getId());
		return Integer.toString(user.getId());
		
	}
	
	@Override
	public boolean validateAccount(String login, String password) {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//PASSWORD COULD BE EMPTY IF NOT YET ASSIGNED.  DON"T WANT THEM TO BE ABLE TO LOG IN
		if (password != null && !password.trim().equals("")) {
			return super.validateAccount(login, password);
		}
		return false;
		
		
	}
	
	@Override
	public boolean deleteUser(String username) throws AccountNotFoundException {
		User user = SIS.get().getUserIO().getUserFromUsername(username);
		if (user == null)
			throw new AccountNotFoundException("User " + username + " was not found.");
		user.state = User.DELETED;
		return SIS.get().getUserIO().saveUser(user);		
	}
	
	
	@Override
	public boolean resetPassword(String username) {
		if (!username.equalsIgnoreCase("admin") || username.equals("")) {
			String newPassword = new MD5Hash(username + new Date().toString()).toString().substring(0, 8);
			if (changePassword(username, newPassword)) {
				System.out.println("Changed password to " + newPassword);
				
				String body = "Hello " + username + ", \r\n \r\n Your password has been "
				+ "reset for your IUCN application.  Please log in using the new credentials:\r\n"
				+ "  Username: " + username + "\r\n  Password: " + newPassword
				+ "\r\n \r\n We strongly recommend that you change your password on the " +
						"login page.";
				Mailer mailer = SISMailer.getGMailer();
				mailer.setTo(username);
				mailer.setBody(body);

				try {
					mailer.background_send();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return true;
			} else {
				System.out.println("Error changing password for user " + username);
				return false;
			}
		} else {
			System.out.println("Username requested reset is either admin or empty, so it failed: " + username);
			return false;
		}
	}
	
	@Override
	public String emailConfirmationCode(String email) {
		String confirmationCode = getSHA1Hash(Long.toString(new Date().getTime()) + "some_more_salt");
		System.out.println("Your confirmation code is " + confirmationCode);
		String body = "Your confirmation code for the Species Information " + "System is " + confirmationCode
		+ "\r\n\r\n" + "Please visit http://iucnsis.org/authn/confirm/" + confirmationCode
		+ " to activate your account.";

		String subject = "Species Information System Account Signup Confirmation";
		
		Mailer mailer = SISMailer.getGMailer();
		mailer.setTo(email);
		mailer.setSubject(subject);
		mailer.setBody(body);

		try {
			mailer.background_send();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return confirmationCode;
	}
	
	
	
	
	

}
