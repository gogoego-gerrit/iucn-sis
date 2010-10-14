package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.shared.api.acl.InsufficientRightsException;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Reference;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * ReferenceCache.java
 * 
 * A static instance of a glorified HashMap that stores lists of recently used
 * references for assessments.
 * 
 * Caches an ArrayList
 * 
 * @author carl.scott
 * 
 */
public class ReferenceCache extends HashMap<Integer, Set<Reference>> {

	private static final long serialVersionUID = 1L;
	private static ReferenceCache instance;

	/**
	 * Gets the static instance of this mapping
	 * 
	 * @return the reference cache
	 */
	public static ReferenceCache getInstance() {
		if (instance == null)
			instance = new ReferenceCache();
		return instance;
	}

	/**
	 * Default Constructor
	 * 
	 */
	private ReferenceCache() {
		super();
	}

	/**
	 * Adds a list of reference to the cache given an assessment ID. Does not
	 * override current references.
	 * 
	 * @param assessmentID
	 *            the assessment ID
	 * @param references
	 *            the list of references to add
	 */
	public void addReferences(Collection<Reference> references) {
		addReferences(AssessmentCache.impl.getCurrentAssessment().getId(), references);
	}
	
	public void addReferences(Integer assessmentID, Collection<Reference> references) {
		Set<Reference> current = getReferences(assessmentID);
		current.addAll(references);
		put(assessmentID, current);
	}
	
	public void addReferencesToAssessmentAndSave(Collection<Reference> references, Field field, final GenericCallback<Object> wayback) {
		if (!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.WRITE, 
				AssessmentCache.impl.getCurrentAssessment())) {
			WindowUtils.errorAlert("You cannot add references to an assessment "
					+ "you don't have permissions to edit.");
			return;
		}
		
		if (field == null) {
			Debug.println("Failed to add references to null field!");
			try {
				throw new NullPointerException();
			} catch (NullPointerException e) {
				Debug.println(e);
			}
			return;
		}

		int added = 0;
		final StringBuffer errors = new StringBuffer("");
		
		if (field.getReference() == null)
			field.setReference(new HashSet<Reference>());
		
		for (Reference current : references) {
			if (!field.getReference().contains(current)) {
				field.getReference().add(current);
				added++;
			} else {
				errors.append("<br/>The reference " + current.getTitle() + ", " + current.getAuthor()
						+ " is already attached.");
			}
		}

		if (added > 0) {
			Assessment assessment = AssessmentCache.impl.getCurrentAssessment();
			if (!assessment.getField().contains(field))
				assessment.getField().add(field);
			
			try {
				AssessmentClientSaveUtils.saveAssessment(null, assessment, new GenericCallback<Object>() {
					public void onFailure(Throwable caught) {
						wayback.onFailure(caught);
					};
					public void onSuccess(Object result) {
						wayback.onSuccess("");
					};
				});
			} catch (InsufficientRightsException e) {
				WindowUtils.errorAlert("Insufficient Permissions", "You do not have "
						+ "permission to modify this assessment. The changes you " + "just made will not be saved.");
				wayback.onFailure(e);
			}
		} else {
			final Dialog report = new Dialog();
			report.setClosable(true);
			report.add(new HTML("Not saved - no changes were made.<br>" + errors));
			report.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					report.hide();
				}
			}));
			report.show();

			wayback.onFailure(null);
		}
	}

	/**
	 * Gets references for this particular assessment, or an empty list if there
	 * are none
	 * 
	 * @param assessmentID
	 *            the assessment id
	 * @return the list
	 */
	public Set<Reference> getReferences(Integer assessmentID) {
		Set<Reference> list = new HashSet<Reference>();
		if (hasReferences(assessmentID))
			list = get(assessmentID);
		return list;
	}

	/**
	 * Determines if there are references for this assessment.
	 * 
	 * @param assessmentID
	 *            the assessment id
	 * @return true if there are and the list is not empty, false otherwise
	 */
	public boolean hasReferences(Integer assessmentID) {
		return containsKey(assessmentID) && !get(assessmentID).isEmpty();
	}
}
