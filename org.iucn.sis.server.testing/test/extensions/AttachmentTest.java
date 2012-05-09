package extensions;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.extensions.attachments.AttachmentIO;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.FieldAttachment;
import org.junit.Test;

import core.BasicHibernateTest;

public class AttachmentTest extends BasicHibernateTest {
	
	@Test
	public void testAttach() throws PersistentException {
		Session session = openTransaction();
		
		List<Integer> fields = new ArrayList<Integer>();
		fields.add(1924402);
		
		int attachmentID = 16527363;
		
		AttachmentIO io = new AttachmentIO(session);
		io.attach(attachmentID, fields);
		
		closeTransaction(session);
		
		testFetch();
	}
	
	@Test
	public void testFetch() {
		Session session = openSession();
		
		AttachmentIO io = new AttachmentIO(session);
		FieldAttachment[] list = io.getAttachments((Assessment)session.load(Assessment.class, 1924391));
		
		Assert.assertNotSame(0, list.length);
		
		closeSession(session);
	}

}
