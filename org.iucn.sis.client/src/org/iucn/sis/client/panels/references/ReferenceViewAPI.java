package org.iucn.sis.client.panels.references;

import java.util.ArrayList;

import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.Reference;

import com.solertium.lwxml.shared.GenericCallback;

public interface ReferenceViewAPI {
	
	public Referenceable getReferenceable();
	
	public void onAddSelected(ArrayList<Reference> selectedValues);
	
	public void onRemoveSelected(ArrayList<Reference> selectedValues);
	
	public void setReferences(Referenceable referenceable, GenericCallback<Object> addCallback, GenericCallback<Object> removeCallback);
	
	public void showSearchTab();

}
