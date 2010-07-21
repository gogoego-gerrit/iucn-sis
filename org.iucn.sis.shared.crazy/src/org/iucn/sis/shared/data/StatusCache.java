package org.iucn.sis.shared.data;

import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.data.assessments.AssessmentFetchRequest;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentParser;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Timer;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * Resolves and caches statuses (save-locked, checked-out, needs updating) for
 * assessments and taxa.
 * 
 * @author adam.schwartz
 * 
 */
public class StatusCache {

	private class ReleaseLockTimer extends Timer {

		private String id;

		public ReleaseLockTimer(String id) {
			this.id = id;
		}

		@Override
		public void run() {
			assessmentStatus.remove(id);
		}
	}

	private class Status {

		private Integer type;
		private ReleaseLockTimer timer;

		public Status(Integer type, String id) {
			this.type = type;

			if (type != NEEDS_UPDATE)
				startReleaseTimer(id, 2 * 60 * 1000);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Status)
				return ((Status) obj).type == type;
			else if (obj instanceof Integer)
				return obj == type;

			return super.equals(obj);
		}

		public void startReleaseTimer(String id, int msLength) {
			timer = new ReleaseLockTimer(id);
			timer.schedule(msLength);
		}

	}

	public static final Integer NEEDS_UPDATE = new Integer(0);
	public static final Integer HAS_LOCK = new Integer(1);

	public static final Integer CHECKED_OUT = new Integer(2);

	public static final Integer LOCKED = new Integer(3);

	public static final Integer UNLOCKED = new Integer(20);
	public static StatusCache impl = new StatusCache();

	private Map<String, Status> assessmentStatus;

	private Map<String, Status> taxonStatus;

	private StatusCache() {
		assessmentStatus = new HashMap<String, Status>();
		taxonStatus = new HashMap<String, Status>();
	}

	/**
	 * Will perform a status check on the assessment, displaying the appropriate
	 * message depending on one of the following statuses of the assessment:
	 * <ul>
	 * <li>Locked by another user, by a save</li>
	 * <li>Checked out by a user</li>
	 * <li>Update Needed - the client's cached copy is stale, and an update is
	 * required. In this case the forceUpdate argument will flag whether to
	 * immediately update the stale copy, or to prompt the user, allowing
	 * him/her to choose whether to clobber someone else's stuff.</li>
	 * </ul>
	 * 
	 * If one of the first two statuses are returned, or if the client chooses
	 * to perform an update of its stale copy, the callback will have its
	 * onFailure() function invoked, notifying the caller that the operation
	 * should be withheld. If the forceUpdate flag is true or the user chooses
	 * to continue using his stale copy and the status is "update needed", the
	 * onSuccess() function will be invoked.
	 * 
	 * If the assessment is unfettered or the client is the one who owns the
	 * save lock, the onSuccess() function will be invoked.
	 * 
	 * @param assessment
	 *            - the assessment to lock
	 * @param forceUpdate
	 *            - whether to forceably update a stale, local copy
	 * @param callback
	 */
	public void checkStatus(final AssessmentData assessment, final boolean forceUpdate,
			final GenericCallback<Integer> callback) {

		Status curStatus = assessmentStatus.get(generateID(assessment));

		if (curStatus == null) {
			long date = assessment.getDateModified();

			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.get("/status/assessment/" + assessment.getAssessmentID() + "/" + assessment.getType() + "/" + date,
					new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							// Do nothing. No lock!
							callback.onSuccess(UNLOCKED);
						}

						public void onSuccess(String result) {
							if (ndoc.getDocumentElement().getNodeName().equalsIgnoreCase("save_lock")) {
								sayAssessmentLocked(ndoc, callback);
							} else if (ndoc.getDocumentElement().getNodeName().equalsIgnoreCase("checked_out")) {
								sayAssessmentCheckedOut(ndoc, callback);
							} else if (ndoc.getDocumentElement().getNodeName().equalsIgnoreCase("update")) {
								sayAssessmentNeedsUpdate(assessment, ndoc, forceUpdate, callback);
							} else {
								callback.onSuccess(UNLOCKED);
							}
						}
					});
		} else if (curStatus.type == NEEDS_UPDATE) {
			sayAssessmentNeedsUpdate(assessment, null, forceUpdate, callback);
		} else if (curStatus.type == HAS_LOCK) {
			callback.onSuccess(HAS_LOCK);
		}
	}

	public void clearCache() {
		assessmentStatus.clear();
		taxonStatus.clear();
	}

	private void doUpdate(final AssessmentData assessment, final NativeDocument ndoc,
			final GenericCallback<Integer> callback) {
		if (ndoc != null) {
			AssessmentParser p = new AssessmentParser();
			p.parse(ndoc.getDocumentElement().getElementByTagName("assessment"));
			AssessmentCache.impl.addAssessment(p.getAssessment());
			AssessmentCache.impl.setCurrentAssessment(p.getAssessment());

			Info.display("Updated", "Changes to this assessment were found on the server, "
					+ "and were used to update your copy.");

			// Remove a status, if there was one
			assessmentStatus.remove(generateID(assessment));

			ClientUIContainer.bodyContainer.getTabManager().getPanelManager().DEM.redraw();
			callback.onSuccess(UNLOCKED);
		} else {
			AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(assessment.getUID()), 
					new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							callback.onFailure(caught);
						}

						public void onSuccess(String result) {
							callback.onSuccess(UNLOCKED);
						}
					});
		}
	}

	private String generateID(final AssessmentData assessment) {
		return assessment.getAssessmentID() + assessment.getType();
	}

	public void getStatus(AssessmentData assessment) {
		assessmentStatus.get(assessment.getAssessmentID());
	}

	public void sayAssessmentCheckedOut(final NativeDocument ndoc, final GenericCallback callback) {
		String owner = ndoc.getDocumentElement().getAttribute("owner");
		WindowUtils.errorAlert("Assessment Checked Out", "Oops! This assessment "
				+ "has been checked out, meaning it is currently being worked on by "
				+ "another user, most likely at a workshop. To avoid conflicts, "
				+ "no changes can be committed until the assessments are checked "
				+ "back in and the lock is relinquished, at which time SIS will "
				+ "prompt you to <b>revert your changes and update</b> to the " + "newest version of this assessment."
				+ "<br>**Assessment was checked out by: " + owner);

		callback.onFailure(new Exception("Checked out."));
	}

	public void sayAssessmentLocked(final NativeDocument ndoc, final GenericCallback callback) {
		String owner = ndoc.getDocumentElement().getAttribute("owner");

		if (owner.equalsIgnoreCase(SimpleSISClient.currentUser.username)) {
			callback.onSuccess(HAS_LOCK);
		} else {

			WindowUtils.errorAlert("Assessment Locked", "Oops! This assessment "
					+ "is locked, meaning it is currently being worked on by "
					+ "another user. To avoid conflicts, no changes can be committed "
					+ "until the lock is relinquished, so the suggested action is to "
					+ "stop editing the assessment until you do not receive this message "
					+ "when you select it as your current assessment." + "<br>** Assessment Locked by: " + owner);

			callback.onFailure(new Exception("Locked."));
		}
	}

	public void sayAssessmentNeedsUpdate(final AssessmentData assessment, final NativeDocument ndoc,
			boolean forceUpdate, final GenericCallback<Integer> callback) {
		if (forceUpdate) {
			doUpdate(assessment, ndoc, callback);
		} else {
			String owner = ndoc.getDocumentElement().getAttribute("owner");

			if (owner.equals(SimpleSISClient.currentUser.username)) {
				// If there was a server-side change to an assessment by this
				// user, I'm going
				// to perform the update automagically, as it's possible there
				// was an issue
				// with getting the timestamp back on the client, or a reference
				// was changed
				// without this user having had the lock yet, in which case an
				// update won't
				// clobber any data and is actually necessary.
				doUpdate(assessment, ndoc, callback);
			} else {
				Listener<MessageBoxEvent> listener = new Listener<MessageBoxEvent>() {
					public void handleEvent(MessageBoxEvent we) {
						if (we.getButtonClicked().getText().equalsIgnoreCase("Update")) {
							doUpdate(assessment, ndoc, callback);
							callback.onFailure(new Exception("Stale version updated."));
						} else if (we.getButtonClicked().getText().equalsIgnoreCase("OVerride")) {
							callback.onSuccess(StatusCache.UNLOCKED);
						} else {
							callback.onFailure(new Exception("Nothing doing."));
						}
					}
				};
				MessageBox box = new MessageBox();
				box.setButtons(MessageBox.CANCEL);
				Button override = new Button("Override");
				Button update = new Button("Update");
				box.setIcon(MessageBox.WARNING);
				box.getDialog().getButtonBar().insert(override, 0);
				box.getDialog().getButtonBar().insert(update, 0);
				box.addCallback(listener);
				box.setTitle("Conflict!");
				box.setMessage("Changes have been made to this assessment by another " + "user in the system, " + owner
						+ ", since your last successful "
						+ "save. If you choose Override, their changes will be lost. If "
						+ "you choose Update, you will lose the changes you've made since "
						+ "your last successful save.");
				box.show();
			}
		}
	}

	/**
	 * TODO: Work this out better. It's only storing that I have the lock,
	 * currently...
	 * 
	 * @param assessment
	 * @param status
	 */
	public void setStatus(AssessmentData assessment, Integer status) {
		if (status == HAS_LOCK)
			assessmentStatus.put(assessment.getAssessmentID(), new Status(status, generateID(assessment)));
	}
}
