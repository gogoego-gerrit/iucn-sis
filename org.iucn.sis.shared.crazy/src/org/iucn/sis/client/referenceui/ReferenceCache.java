package org.iucn.sis.client.referenceui;

import java.util.ArrayList;
import java.util.HashMap;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.acl.InsufficientRightsException;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.assessments.AssessmentUtilFactory;

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
public class ReferenceCache extends HashMap {

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
	public void addReferences(String assessmentID, ArrayList references) {
		ArrayList current = getReferences(assessmentID);
		for (int i = 0; i < references.size(); i++) {
			if (!current.contains(references.get(i)))
				current.add(references.get(i));
		}
		put(assessmentID, current);
	}

	public void addReferencesToAssessmentAndSave(ArrayList references, String canonicalName,
			final GenericCallback<Object> wayback) {

		if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, AssessmentCache.impl.getCurrentAssessment())) {
			WindowUtils.errorAlert("You cannot add references to an assessment "
					+ "you don't have permissions to edit.");
			return;
		}

		int added = 0;
		final StringBuffer errors = new StringBuffer("");
		for (int i = 0; i < references.size(); i++) {
			ReferenceUI current = (ReferenceUI) references.get(i);
			if (!AssessmentCache.impl.getCurrentAssessment().getReferences(canonicalName).contains(current)) {
				AssessmentCache.impl.getCurrentAssessment().addReference(current, canonicalName);
				added++;
			} else {
				errors.append("<br>The reference " + current.getField("title") + ", " + current.getField("author")
						+ " is already attached.");
			}
		}

		if (added > 0) {
			try {
				AssessmentUtilFactory.getSaveUtils().saveAssessment(AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<Object>() {
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
	public ArrayList getReferences(String assessmentID) {
		ArrayList list = new ArrayList();
		if (hasReferences(assessmentID)) {
			list = (ArrayList) get(assessmentID);
		}
		return list;
	}

	/**
	 * Determines if there are references for this assessment.
	 * 
	 * @param assessmentID
	 *            the assessment id
	 * @return true if there are and the list is not empty, false otherwise
	 */
	public boolean hasReferences(String assessmentID) {
		return containsKey(assessmentID) && !((ArrayList) get(assessmentID)).isEmpty();
	}
}
