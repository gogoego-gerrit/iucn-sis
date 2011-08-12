package org.iucn.sis.shared.api.models.interfaces;

import java.util.Set;

import org.iucn.sis.shared.api.models.Reference;

public interface HasReferences {
	
	public Set<Reference> getReference();

}
