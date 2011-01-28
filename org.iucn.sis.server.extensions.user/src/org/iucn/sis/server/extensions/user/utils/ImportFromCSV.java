package org.iucn.sis.server.extensions.user.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.shared.api.models.User;

import com.solertium.util.CSVTokenizer;
import com.solertium.util.DynamicWriter;

public class ImportFromCSV extends DynamicWriter {
	
	private static final int EMAIL = 0;
	private static final int TYPE = 1;
	private static final int FIRST = 2;
	private static final int LAST = 3;
	private static final int MID_INITIAL = 4;
	private static final int NICKNAME = 5;
	private static final int AFFILIATION = 6;
	
	private UserEvent addProfileListener;
	private UserEvent addUserListener;
	
	public void setAddProfileListener(UserEvent addProfileListener) {
		this.addProfileListener = addProfileListener;
	}
	
	public void setAddUserListener(UserEvent addUserListener) {
		this.addUserListener = addUserListener;
	}
	
	public void importUsers(Reader csv) throws IOException {
		final BufferedReader reader = new BufferedReader(csv);
		
		int success = 0;
		
		final List<String> missingInfo = new ArrayList<String>();
		final List<String> conflict = new ArrayList<String>();
		
		String line;
		while ((line = reader.readLine()) != null) {
			if ("".equals(line))
				continue;
			
			final CSVTokenizer tokenizer = new CSVTokenizer(line);
			tokenizer.setNullOnEnd(true);
			
			int index = 0;
			String mode = null, token;
			final User user = new User();
			user.setState(User.ACTIVE);
			user.setRapidlistUser(false);
			user.setSisUser(false);
			
			while ((token = tokenizer.nextToken()) != null) {
				switch(index++) {
				case EMAIL:
					user.setUsername(token);
					user.setEmail(token);
					break;
				case TYPE:
					mode = "u".equalsIgnoreCase(token) || "p".equalsIgnoreCase(token) ? token : null;
					break;
				case FIRST:
					user.setFirstName(token);
					break;
				case LAST:
					user.setLastName(token);
					break;
				case MID_INITIAL:
					//SIS doens't take the middle initial...let's add it to the initial tho
					user.setInitials(token);
					break;
				case NICKNAME:
					user.setNickname(token);
					break;
				case AFFILIATION:
					user.setAffiliation(token);
					break;
				default:
					break;
				}
			}
			
			if (isBlank(user.getUsername()) || isBlank(mode)) {
				/*
				 * Blank username, skip and don't even report it!
				 */
				continue;
			}
			
			if (isBlank(user.getLastName())) {
				missingInfo.add(user.getUsername());
				
				continue;
			}
			
			if (!isBlank(user.getInitials())) {
				String middle = user.getInitials();
				String initials = "";
				if (!isBlank(user.getFirstName()))
					initials = user.getFirstName().charAt(0) + middle + user.getLastName().charAt(0);
				user.setInitials(initials);
			}
			else if (!isBlank(user.getFirstName()))
				user.setInitials(user.getFirstName().charAt(0) + "" + user.getLastName().charAt(0));
			
			if ("u".equalsIgnoreCase(mode)) {
				user.setSisUser(true);
				//Going to assume a password gets set by the handler.
				
				boolean isSuccess = addUserListener.addUser(user);
				if (isSuccess)
					success++;
				else
					conflict.add(user.getUsername());
			}
			else {
				//This means they will not be able to login...
				user.setPassword("");
				
				boolean isSuccess = addProfileListener.addUser(user);
				if (isSuccess)
					success++;
				else
					conflict.add(user.getUsername());
			}
		}
		
		write("Import successful.  " + success + " new identities created in SIS.");
		if (!missingInfo.isEmpty()) {
			write("");
			write("The following identities were not created due to missing required information: ");
			write("");
			for (String name : missingInfo)
				write(name);
		}
		if (!conflict.isEmpty()) {
			write("");
			write("The following identities were not created due to existence in SIS prior to import: ");
			write("");
			for (String name : conflict)
				write(name);
		}
		write("");
		write("[end of report]");
		
	}
	
	private boolean isBlank(String value) {
		return value == null || "".equals(value);
	}
	
	public static interface UserEvent {
		
		/**
		 * A handler that adds a user to the database.
		 * Returns true if the add was successful, or 
		 * false if the user account already exists.
		 * 
		 * @param user
		 * @return
		 */
		public boolean addUser(User user);
		
	}

}
