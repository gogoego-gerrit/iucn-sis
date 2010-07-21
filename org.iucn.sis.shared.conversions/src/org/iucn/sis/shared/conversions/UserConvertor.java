package org.iucn.sis.shared.conversions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.hibernate.HibernateException;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.User;

import com.solertium.db.DBException;
import com.solertium.db.DBSession;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.SystemExecutionContext;
import com.solertium.util.SHA1Hash;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;

public class UserConvertor {

	public static void convertAllUsers() throws NamingException, DBException, Exception {
		final Map<String, String> userHashToHashes = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(UserConvertor.class.getResourceAsStream("hamsterfish"))); 
		
		try {
			String cur = "";
			while ((cur = reader.readLine()) != null) {
				String [] split = cur.split(":");
				userHashToHashes.put(split[0].replace("|", ""), split[1].replace("|", ""));
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		DBSession db = DBSessionFactory.getDBSession("sisusers");
		ExecutionContext ec = new SystemExecutionContext(db);
		ec.setExecutionLevel(ExecutionContext.SQL_ALLOWED);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);

		ec.doQuery("SELECT * FROM USER JOIN PROFILE ON PROFILE.USERID=USER.ID;", new RowProcessor() {

			@Override
			public void process(Row row){
				User user = new User();
				user.setUsername(row.get("USERNAME").getString());
				user.setId(row.get("ID").getInteger());
				user.setFirstName(row.get("FIRSTNAME").getString());
				user.setLastName(row.get("LASTNAME").getString());
				user.setEmail(getEmailAddresses(row));
				user.setAffiliation(row.get("AFFILIATION").getString());
				user.setSisUser(row.get("SIS").getString() != null && row.get("SIS").getString().equals("true"));
				user.setRapidlistUser(row.get("RAPIDLIST").getString() != null && row.get("RAPIDLIST").getString().equals("true"));
				user.setInitials(row.get("INITIALS").getString());
				user.setPassword(userHashToHashes.get(getSHA1Hash(user.getUsername())));
				
				String quickgroup = row.get("QUICKGROUP").getString();
				if (quickgroup != null) {
					String[] quickgroups = quickgroup.split(",");
					for (String group : quickgroups){
						try {
							PermissionGroup permGroup = SIS.get().getPermissionIO().getPermissionGroup(group);
							if (permGroup == null)
								continue;
							user.getPermissionGroups().add(permGroup);
						} catch (PersistentException e) {							
							throw new RuntimeException(e);
						}
					}
					if (user.getUsername().equalsIgnoreCase("admin")) {
						try {
						user.getPermissionGroups().add(SIS.get().getPermissionIO().getPermissionGroup("sysAdmin"));
						} catch (PersistentException e) {							
							throw new RuntimeException(e);
						}
					}
				}
				
				try {
					SIS.get().getManager().getSession().save(user);
					SIS.get().getUserIO().afterSave(user);
				} catch (HibernateException e) {
					// TODO Auto-generated catch block
					throw new RuntimeException(e);
				} catch (PersistentException e) {
					// TODO Auto-generated catch block
					throw new RuntimeException(e);
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					throw new RuntimeException(e);
				} catch (ConflictException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		});

	}
	
	public static String getSHA1Hash(String in) {
		SHA1Hash hash = new SHA1Hash(in);
		return hash.toString();
	}

	protected static String getEmailAddresses(Row row) {
		String email = row.get("USERNAME").getString();
		if (!email.contains("@")) {
			if (email.equalsIgnoreCase("craig.ht"))
				return "craig.hilton-taylor@iucn.org";
			else if (email.equalsIgnoreCase("caroline.p"))
				return "caroline.pollock@iucn.org";
			else if (email.equalsIgnoreCase("leah.c"))
				return "leah.collett@iucn.org";
			else if (email.equalsIgnoreCase("jim.ragle"))
				return "james.ragle@iucn.org";
			else if	(email.equalsIgnoreCase("melanie.b"))
				return "melanie.bilz@iucn.org";
			else if (email.equalsIgnoreCase("admin") || email.equalsIgnoreCase("adam"))
				return "adam.schwartz@solertium.com";
			else 
				return "bademail@noemail.com";
					
		}
		return email;
	}
}
