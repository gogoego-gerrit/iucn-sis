package org.iucn.sis.client.api.models;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class NameValueModelData extends BaseModelData {
	
	private static final long serialVersionUID = 1L;
		
	public NameValueModelData(String name, String value) {
		super();
		set("text", name);
		set("value", value);
	}
	
	public String getName() {
		return get("text");
	}
		
	public String getValue() {
		return get("value");
	}

}
