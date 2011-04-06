package org.iucn.sis.client.panels.references;

import org.iucn.sis.shared.api.models.Reference;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class ReferenceModel extends BaseModelData {

	/**
	 * Auto-generated
	 */
	private static final long serialVersionUID = 8259134038222297761L;
	
	private final Reference ref;
	private final String count;

	public ReferenceModel(Reference ref) {
		this(ref, "");
	}

	public ReferenceModel(Reference ref, String count) {
		this.ref = ref;
		this.count = count;
		
		rebuild();
	}

	public void rebuild() {
		set("author", ref.getAuthor());
		set("title", ref.getTitle());
		set("year", ref.getYear());
		set("field", "N/A");
		set("citation", getVisibleCitation());
		set("count", count);
	}
	
	public void setField(String field) {
		set("field", field);
	}
	
	private String getVisibleCitation() {
		String citation = ref.generateCitationIfNotAlreadyGenerate();
		if (citation == null || "".equals(citation.trim()))
			return "<i>(Unable to generate citation for reference of type " + ref.getType() + ")</i>";
		else
			return citation;
	}
	
	public Reference getModel() {
		return ref;
	}
}
