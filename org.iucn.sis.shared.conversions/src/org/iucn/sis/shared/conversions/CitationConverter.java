package org.iucn.sis.shared.conversions;

import java.util.List;

import org.iucn.sis.shared.api.citations.ReferenceCitationGeneratorShared;
import org.iucn.sis.shared.api.citations.ReferenceCitationGeneratorShared.ReturnedCitation;
import org.iucn.sis.shared.api.models.Reference;

/**
 * 
 * Generate citations for references that are missing one.
 * 
 * @author carlscott
 *
 */
public class CitationConverter extends Converter {
	
	public CitationConverter() {
		super();
		setClearSessionAfterTransaction(true);
	}
	
	@SuppressWarnings("unchecked")
	protected void run() throws Exception {
		List<Object> results = session.createSQLQuery("SELECT id FROM tmp.missing_citation").list();
		
		int count = 0, batch = 100;
		
		for (Object result : results) {
			final Reference reference;
			try {
				reference = (Reference) session.load(Reference.class, (Integer)result);
			} catch (Exception e) {
				printf("# Failed to load reference %s", result);
				continue;
			}
			
			ReturnedCitation citation = ReferenceCitationGeneratorShared.
				generateNewCitation(reference.toMap(), reference.getType());
			
			reference.setCitationComplete(citation.allFieldsEntered);
			reference.setCitation(citation.citation);
			
			session.update(reference);
			
			if (++count % batch == 0) {
				printf("%s...", count);
				commitAndStartTransaction();
			}
		}
		
		commitAndStartTransaction();
	}

}
