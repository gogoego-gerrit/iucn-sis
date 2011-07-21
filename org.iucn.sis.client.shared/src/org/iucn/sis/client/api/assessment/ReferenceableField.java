package org.iucn.sis.client.api.assessment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.shared.api.acl.InsufficientRightsException;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Reference;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class ReferenceableField implements Referenceable {
	
	private final Field field;
	
	public ReferenceableField(Field field) {
		this.field = field;
	}
	
	@Override
	public void addReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
		field.getReference().addAll(references);
		persist(callback);
	}
	
	@Override
	public void onReferenceChanged(GenericCallback<Object> callback) {
		// Don't care
	}
	
	@Override
	public Set<Reference> getReferencesAsList() {
		return new HashSet<Reference>(field.getReference());
	}
	
	@Override
	public void removeReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
		field.getReference().removeAll(references);
		persist(callback);
	}
	
	protected void persist(final GenericCallback<Object> callback) {
		if (!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.WRITE, 
				AssessmentCache.impl.getCurrentAssessment())) {
			WindowUtils.errorAlert("You cannot add references to an assessment "
					+ "you don't have permissions to edit.");
			return;
		}
		
		Assessment assessment = AssessmentCache.impl.getCurrentAssessment();
		if (!assessment.getField().contains(field))
			assessment.getField().add(field);
		
		try {
			AssessmentClientSaveUtils.saveAssessment(null, assessment, new GenericCallback<Object>() {
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				};
				public void onSuccess(Object result) {
					callback.onSuccess("");
				};
			});
		} catch (InsufficientRightsException e) {
			WindowUtils.errorAlert("Insufficient Permissions", "You do not have "
					+ "permission to modify this assessment. The changes you " + "just made will not be saved.");
			callback.onFailure(e);
		}
	}

}
