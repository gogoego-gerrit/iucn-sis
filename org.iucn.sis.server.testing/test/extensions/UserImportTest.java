package extensions;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.iucn.sis.server.extensions.user.utils.ImportFromCSV;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.User;
import org.junit.Before;
import org.junit.Test;

import core.BasicTest;

public class UserImportTest extends BasicTest implements ImportFromCSV.UserEvent {
	
	ImportFromCSV worker;
	
	Set<String> seen = new HashSet<String>();
	
	@Before
	public void setup() {
		worker = new ImportFromCSV();
		worker.setAddProfileListener(this);
		worker.setAddUserListener(this);
		
		seen.clear();
	}
	
	@Test
	public void testMissing() throws IOException {
		StringBuilder lines = new StringBuilder();
		lines.append("email,type,first_name,last_name,mid_initial,nickname,affiliation\n");
		lines.append("carl.scott@solertium.com,u,Carl,,u,SIS,Solertium\n");
		lines.append("carl.scott.2@solertium.com,p,Carl,Scott\n");
		lines.append("carl.scott.2@solertium.com,u,Carl,Scott,u,Test,Me\n");
		
		StringWriter writer = new StringWriter();
		
		worker.setOutputStream(writer, "\r\n");
		worker.importUsers(new StringReader(lines.toString()));
		
		String result = writer.toString();
		Debug.println(result);
		
		Assert.assertTrue(result.contains("1 new identities"));
		Assert.assertTrue(result.contains("were not created due to missing"));
		Assert.assertTrue(result.contains("were not created due to existence"));
	}
	
	
	@Test
	public void testDuplicate() throws IOException {
		StringBuilder lines = new StringBuilder();
		lines.append("email,type,first_name,last_name,mid_initial,nickname,affiliation\n");
		lines.append("carl.scott@solertium.com,u,Carl,Scott,u,SIS,Solertium\n");
		lines.append("carl.scott.2@solertium.com,p,Carl,Scott\n");
		lines.append("carl.scott.2@solertium.com,u,Carl,Scott,u,Test,Me\n");
		
		StringWriter writer = new StringWriter();
		
		worker.setOutputStream(writer, "\r\n");
		worker.importUsers(new StringReader(lines.toString()));
		
		String result = writer.toString();
		Debug.println(result);
		
		Assert.assertTrue(result.contains("2 new identities"));
		Assert.assertTrue(result.contains("were not created"));
	}
	
	@Test
	public void testValid() throws IOException {
		StringBuilder lines = new StringBuilder();
		lines.append("email,type,first_name,last_name,mid_initial,nickname,affiliation\n");
		lines.append("carl.scott@solertium.com,u,Carl,Scott,u,SIS,Solertium\n");
		lines.append("carl.scott.2@solertium.com,p,Carl,Scott\n");
		lines.append("carl.scott.3@solertium.com,u,Carl,Scott,u,Test,Me\n");
		
		StringWriter writer = new StringWriter();
		
		worker.setOutputStream(writer, "\r\n");
		worker.importUsers(new StringReader(lines.toString()));
		
		String result = writer.toString();
		Debug.println(result);
		
		Assert.assertTrue(result.contains("3 new identities"));
		Assert.assertTrue(!result.contains("were not created"));
	}

	@Override
	public boolean addUser(User user) {
		Debug.println("{0} - {1} {2} ({3}) initials: {4}, affil: {5}; user? {6}", 
			user.getUsername(), user.getFirstName(), user.getLastName(), 
			user.getNickname(), user.getInitials(), user.getAffiliation(), 
			user.isSISUser()
		);
		
		return seen.add(user.getUsername());
	} 
	
}
