package extensions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.extensions.reports.AssessmentHtmlTemplate;
import org.iucn.sis.server.extensions.reports.AssessmentReportTemplate;
import org.iucn.sis.shared.api.models.Assessment;
import org.junit.Test;

import core.BasicHibernateTest;

public class AllReports extends BasicHibernateTest {
	
	@Test
	public void testGenerate() throws PersistentException, IOException {
		Session session = openSession();
		
		Assessment assessment = SISPersistentManager.instance().getObject(session, Assessment.class, 75822);
		
		AssessmentHtmlTemplate template = new AssessmentHtmlTemplate(session, assessment, true, false);
		template.parse();
		
		File file = new File("/var/sis/reports/" + assessment.getId() + ".html");
		file.getParentFile().mkdirs();
		
		BufferedWriter out = new BufferedWriter(new PrintWriter(new FileWriter(file)));
		out.write(template.getHtmlString());
		
		out.close();
	}
	
	@Test
	public void testGenerateNoView() throws PersistentException, IOException {
		Session session = openSession();
		
		Assessment assessment = SISPersistentManager.instance().getObject(session, Assessment.class, 8580201);
		
		AssessmentHtmlTemplate template = new AssessmentHtmlTemplate(session, assessment, true, false);
		template.parseAvailable();
		
		File file = new File("/var/sis/reports/" + assessment.getId() + "NoView.html");
		file.getParentFile().mkdirs();
		
		BufferedWriter out = new BufferedWriter(new PrintWriter(new FileWriter(file)));
		out.write(template.getHtmlString());
		
		out.close();
	}
	
	@Test
	public void testGenerateBlueReport() throws PersistentException, IOException {
		Session session = openSession();
		
		Assessment assessment = SISPersistentManager.instance().getObject(session, Assessment.class, 8580201);
		
		AssessmentReportTemplate template = new AssessmentReportTemplate(session, assessment);
		template.build();
		
		File file = new File("/var/sis/reports/" + assessment.getId() + "Blue.html");
		file.getParentFile().mkdirs();
		
		BufferedWriter out = new BufferedWriter(new PrintWriter(new FileWriter(file)));
		out.write(template.toString());
		
		out.close();
	}

}
