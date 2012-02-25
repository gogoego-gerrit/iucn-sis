package core;

import org.iucn.sis.server.utils.WorkingSetExporter;
import org.junit.Test;

public class Offline extends BasicHibernateTest {
	
	@Test
	public void run() {
		//Needs to be an existing WS in your database
		int workingSet = 85286;
		
		//Local and will be generated
		String location = "/var/sis/replication";
		String fileName = "test";
		
		WorkingSetExporter exporter = 
			new WorkingSetExporter(workingSet, "admin", false, location, fileName);
		exporter.run();
	}

}
