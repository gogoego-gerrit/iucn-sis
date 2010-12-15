package org.iucn.sis.client.api.models;

import java.util.ArrayList;
import java.util.Set;

import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Taxon;

import com.solertium.lwxml.shared.GenericCallback;

public class ClientTaxon extends Taxon implements Referenceable {
	@Override
	public void addReferences(ArrayList<Reference> references,
			GenericCallback<Object> callback) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Set<Reference> getReferencesAsList() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onReferenceChanged(GenericCallback<Object> callback) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeReferences(ArrayList<Reference> references,
			GenericCallback<Object> callback) {
		// TODO Auto-generated method stub
		
	}
}
