package org.iucn.sis.shared.conversions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.extensions.attachments.AttachmentIO;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.FieldAttachment;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class MissingAttachments950Converter extends Converter {

	public MissingAttachments950Converter() {
		super();
		setClearSessionAfterTransaction(true);
	}

	@SuppressWarnings("unchecked")
	protected void run() throws Exception {
		Map<Integer, String> fieldNameMap = new HashMap<Integer, String>();
		fieldNameMap.put(2, CanonicalNames.HabitatDocumentation);
		fieldNameMap.put(3, CanonicalNames.PopulationDocumentation);
		fieldNameMap.put(4, CanonicalNames.RangeDocumentation);
		fieldNameMap.put(5, CanonicalNames.ThreatsDocumentation);
		fieldNameMap.put(6, CanonicalNames.ConservationActionsDocumentation);
		fieldNameMap.put(7, CanonicalNames.UseTradeDocumentation);
		fieldNameMap.put(8, CanonicalNames.RedListRationale);
		
		Integer T = Integer.valueOf(1);
		
		String query = "SELECT assessmentid, taxonid, " +
			"habitats, population, range, threats, conservation, usetrade, " +
			"rationale, attach FROM tmp.attach_950_asm";
		
		boolean test = "true".equals(parameters.getFirstValue("test"));
		int testid = 0;
		
		List<Object[]> results = session.createSQLQuery(query).list();
		
		Map<String, Integer> attachmentCache = new HashMap<String, Integer>();

		AttachmentIO io = new AttachmentIO(session);
		
		for (Object[] result : results) {
			Integer assessmentid = (Integer) result[0];
			String attachment = "/attachments/scripted/" + result[9];
			
			List<String> fieldNames = new ArrayList<String>();
			
			for (Map.Entry<Integer, String> entry : fieldNameMap.entrySet())
				if (T.equals(result[entry.getKey()]))
					fieldNames.add(entry.getValue());
		
			Integer attachmentID = attachmentCache.get(attachment);
			if (attachmentID == null) {
				if (test) {
					attachmentCache.put(attachment, testid);
					printf("Created new test attachment %s at %s", attachment, testid);
					testid++;
				}
				else {
					FieldAttachment fa = io.createAttachment((String)result[9], 
						attachment, true, new UserIO(session).getUserFromUsername("admin"));
					attachmentCache.put(attachment, fa.getId());
					printf("Created new attachment %s at %s", attachment, attachmentID = fa.getId());
				}
			}
			
			Assessment assessment = (Assessment)session.load(Assessment.class, assessmentid);
			
			List<Integer> fieldIDs = new ArrayList<Integer>();
			
			for (String fieldName : fieldNames) {
				Field field = assessment.getField(fieldName);
				if (field != null)
					fieldIDs.add(field.getId());
			}
			
			if (test)
				printf("Found %s/%s fields to add attachment to", fieldIDs.size(), fieldNames.size());
			else {
				io.attach(attachmentID, fieldIDs);
				
				commitAndStartTransaction();
			}
		}
	}

}
