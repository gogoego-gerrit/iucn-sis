package org.iucn.sis.client.panels.references;

import org.iucn.sis.shared.api.models.Reference;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class ReferenceModel extends BaseModelData {

	/**
	 * Auto-generated
	 */
	private static final long serialVersionUID = 8259134038222297761L;
	protected Reference ref;

	public ReferenceModel(Reference ref) {
		set("author", ref.getAuthor());
		set("title", ref.getTitle());
		set("year", ref.getYear());
		set("count", "");
		set("field", ref.getField());
		set("citation", ref.getCitation());

		this.ref = ref;
	}

	public ReferenceModel(Reference ref, String count) {
		set("author", ref.getAuthor());
		set("title", ref.getTitle());
		set("year", ref.getYear());
		set("field", ref.getField());
		set("citation", ref.getCitation());
		set("count", count);

		this.ref = ref;
	}

	public void rebuild() {
		set("author", ref.getAuthor());
		set("title", ref.getTitle());
		set("year", ref.getYear());
		set("field", ref.getField());
		set("citation", ref.getCitation());
	}
	
}
