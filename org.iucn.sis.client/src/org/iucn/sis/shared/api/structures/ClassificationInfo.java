package org.iucn.sis.shared.api.structures;

/**
 * This is all the data needed by 
 * classification schemes to render.
 * 
 * @author call.scott
 *
 */
public class ClassificationInfo {
	
	private String description;
	private String data;
	
	public ClassificationInfo(String description, String data) {
		this.description = description;
		this.data = data;
	}
	
	public String getData() {
		return data;
	}
	
	public String getDescription() {
		return description;
	}

}
