package org.iucn.sis.shared.api.structures;

public interface CreatesWidget {

	@SuppressWarnings("unchecked")
	public Structure generate(String theStructure, String description, String structID, Object data);
	
}
