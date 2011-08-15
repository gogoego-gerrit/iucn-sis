package org.iucn.sis.client.panels.references;

import org.iucn.sis.client.api.caches.ReferenceCache;
import org.iucn.sis.shared.api.models.Reference;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class ReferenceModel extends BaseModelData {

	/**
	 * Auto-generated
	 */
	private static final long serialVersionUID = 8259134038222297761L;
	
	private final Reference ref;
	private String count;

	public ReferenceModel(Reference ref) {
		this(ref, "");
	}

	public ReferenceModel(Reference ref, String count) {
		this.ref = ref;
		this.count = count;
		
		rebuild();
	}

	public void rebuild() {
		Reference ref = getModel();
		
		set("author", ref.getAuthor());
		set("title", ref.getTitle());
		set("year", ref.getYear());
		set("field", "N/A");
		set("citation", getVisibleCitation());
		set("count", count);
	}
	
	public void increment() {
		int cInt;
		try {
			cInt = Integer.valueOf(count);
		} catch (Exception e) {
			cInt = 0;
		}
		
		count = Integer.toString(cInt + 1);
	}
	
	public void decrement() {
		int cInt;
		try {
			cInt = Integer.valueOf(count);
		} catch (Exception e) {
			cInt = 0;
		}
		
		if (cInt > 0)
			count = Integer.toString(cInt - 1);
	}
	
	public void setField(String field) {
		set("field", field);
	}
	
	public String getVisibleCitation() {
		Reference ref = getModel();
		
		String citation = ref.generateCitationIfNotAlreadyGenerate();
		if (citation == null || "".equals(citation.trim()))
			return "<i>(Unable to generate citation for reference of type " + ref.getType() + ")</i>";
		else
			return citation;
	}
	
	public Reference getModel() {
		return ReferenceCache.impl.contains(ref.getId()) ? 
				ReferenceCache.impl.get(ref.getId()) : ref;
	}
	
}
