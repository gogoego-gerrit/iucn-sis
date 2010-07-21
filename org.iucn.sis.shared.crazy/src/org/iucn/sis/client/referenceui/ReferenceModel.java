package org.iucn.sis.client.referenceui;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class ReferenceModel extends BaseModelData {

	/**
	 * Auto-generated
	 */
	private static final long serialVersionUID = 8259134038222297761L;
	protected ReferenceUI ref;

	public ReferenceModel(ReferenceUI ref) {
		set("author", ref.getField("author"));
		set("title", ref.getField("title"));
		set("year", ref.getField("year"));
		set("count", "");
		set("field", ref.getAssociatedField());
		set("citation", ref.getCitation());

		this.ref = ref;
	}

	public ReferenceModel(ReferenceUI ref, String count) {
		set("author", ref.getField("author"));
		set("title", ref.getField("title"));
		set("year", ref.getField("year"));
		set("count", count);
		set("field", ref.getAssociatedField());
		set("citation", ref.getCitation());

		this.ref = ref;
	}

	public void rebuild() {
		set("author", ref.getField("author"));
		set("title", ref.getField("title"));
		set("year", ref.getField("year"));
		set("field", ref.getAssociatedField());
		set("citation", ref.getCitation());
	}
}
