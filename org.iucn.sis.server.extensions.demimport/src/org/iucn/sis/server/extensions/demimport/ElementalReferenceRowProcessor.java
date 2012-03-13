package org.iucn.sis.server.extensions.demimport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.ReferenceCriteria;
import org.iucn.sis.shared.api.models.Reference;

import com.solertium.db.Column;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;

public class ElementalReferenceRowProcessor extends RowProcessor {
	
	private static final Map<String, Reference> cache = new ConcurrentHashMap<String, Reference>();

	private final List<Reference> references;
	private final Session session;

	public ElementalReferenceRowProcessor(Session session) {
		this.references = new ArrayList<Reference>();
		this.session = session;
	}

	@Override
	public void process(final Row r) {
		try {
			//Row r = ri.getStructuredRow(getExecutionContext(), "bibliography");
			
			String hash = get(r, "Bib_hash");
			if ("".equals(hash))
				hash = get(r, "Bib_Hash");
			String type = get(r, "Publication_Type");
			
			Map<String, String> refData = new HashMap<String, String>();
			
			for (final Column c : r.getColumns()) {
				String name = c.getLocalName();
				String value = c.toString();
				
				if (value != null)
					refData.put(name, value);
			}

			Reference reference = Reference.fromMap(refData);
			reference.setHash(hash);
			reference.setType(type);
			String key = reference.generateCitation();
			
			if (cache.containsKey(key)) {
				reference = cache.get(key);
			}
			else {
				//In DB?
				ReferenceCriteria criteria = new ReferenceCriteria(session);
				criteria.citation.eq(key);
				Reference[] existing = criteria.listReference();
				
				if (existing.length > 0)
					reference = existing[0];
				else { //New reference
					session.save(reference);
					cache.put(key, reference);
				}
			}
			references.add(reference);
		} catch (
				
				
				
				
				
				
				
				
				
				
				
				
				Exception dbx) {
			dbx.printStackTrace();
		}
	}
	
	private String get(Row row, String column) {
		Column c = row.get(column);
		if (c == null)
			return "";
		
		return c.toString();
	}
	
	public List<Reference> getReferences() {
		return references;
	}

}
