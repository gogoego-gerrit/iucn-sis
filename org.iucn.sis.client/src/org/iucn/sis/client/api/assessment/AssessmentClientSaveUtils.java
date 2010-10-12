package org.iucn.sis.client.api.assessment;

import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.StatusCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.acl.InsufficientRightsException;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.displays.Display;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;

import com.google.gwt.core.client.GWT;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;

public class AssessmentClientSaveUtils {

	public static void saveAssessment(final GenericCallback<Object> callback)
			throws InsufficientRightsException {
		saveAssessment(AssessmentCache.impl.getCurrentAssessment(), callback);
	}
	
	public static void saveAssessment(final Assessment assessmentToSave, final GenericCallback<Object> callback)
			throws InsufficientRightsException {
		saveAssessment(null, assessmentToSave, callback);
	}
	
	/**
	 * Tries to save the assessment passed as an argument. If the assessmentToSave parameter is null
	 * it will try to pull the current assessment to save.
	 * 
	 * @param assessmentToSave
	 * @param callback
	 */
	public static void saveAssessment(final List<Display> fieldWidgets, final Assessment assessmentToSave, final GenericCallback<Object> callback)
		throws InsufficientRightsException {

		if (!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.WRITE, assessmentToSave)) {
			throw new InsufficientRightsException();
		}

		StatusCache.impl.checkStatus(assessmentToSave, false, new GenericCallback<Integer>() {
			public void onFailure(Throwable caught) {
				// Nothing extra to print out here.
			}

			public void onSuccess(Integer result) {
				if (result == StatusCache.UNLOCKED || result == StatusCache.HAS_LOCK) {
					if (fieldWidgets != null)
						saveWidgetDataToAssessment(fieldWidgets, assessmentToSave);

					final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
					ndoc.post(UriBase.getInstance().getSISBase() +"/assessments", assessmentToSave.toXML(), new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							if (ndoc.getStatusText().indexOf("409") > -1) {
								callback.onFailure(new Exception("A draft assessment " +
										"with the specified regions already exists. Please modify " +
								"your choice of regions and try again."));
							} else {
								callback.onFailure(new Exception("Please check your internet " +
								"connection and try again."));
							}
						}
						public void onSuccess(String result) {
							try {
								Assessment ret = Assessment.fromXML(ndoc);
								for ( Field curField : ret.getField() ) {
									Field clientField = assessmentToSave.getField(curField.getName());
									if( clientField == null ) {
										Debug.println("Missing the client field " + curField.getName() + "...????");
										assessmentToSave.getField().add(curField);
									} else {
										if( clientField.getId() == 0 )
											clientField.setId(curField.getId());
	
										for( PrimitiveField curPrim : curField.getPrimitiveField() ) {
											PrimitiveField clientPrim = clientField.getKeyToPrimitiveFields().get(curPrim.getName());
											if( clientPrim.getId() == null || clientPrim.getId().equals(Integer.valueOf(0)))
												clientPrim.setId(curPrim.getId());
										}	
									}
								}
							} catch (Throwable e) {
								Debug.println(e);
							}
								
							StatusCache.impl.setStatus(assessmentToSave, StatusCache.HAS_LOCK);
							callback.onSuccess(result);
						}
					});
				} else {
					callback.onFailure(new Exception("Assessment locked or needs an update."));
				}
			}
		});
	}
	
	/**
	 * Compares the saved assessment to what is currently displayed in the field
	 * widgets. Returns whether or not the data needs to be saved.
	 * 
	 * @return boolean - save needed or not
	 */
	public static boolean shouldSaveCurrentAssessment(List<Display> fieldWidgets) {
		if (!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.WRITE, AssessmentCache.impl.getCurrentAssessment())) {
			return false;
		} else {
			for( Display cur : fieldWidgets ) {
				boolean hasChanged = cur.hasChanged();
				Debug.println("Field {0} has changed? {1}", cur.getCanonicalName(), hasChanged);
				if (hasChanged)
					return true;
			}
			return false;
		}
	}

	protected static void saveWidgetDataToAssessment(final List<Display> fieldWidgets,
			final Assessment assessmentToSave) {
		
		Debug.println("Saving assessment...");
		for (Display cur : fieldWidgets) {
			//Debug.println("Saving data to Field {0}: {1}", cur.getCanonicalName(), cur.getField().getPrimitiveField());
			try {
				cur.save();
				
				Field curField = cur.getField();
				if (curField == null)
					Debug.println("Display {0} yielded a null field after save", cur.getCanonicalName());
				else { 
					if (!curField.hasData()) {
						Debug.println("+ Removing {0} with id {1} because it doesn't have any data", curField.getName(), curField.getId());
						assessmentToSave.getField().remove(curField);
					}
					else {
						Debug.println("+ Adding {0} with id {1}", curField.getName(), curField.getId());
						assessmentToSave.getField().add(curField);
						curField.setAssessment(assessmentToSave);
					}
				}
			} catch (Throwable e) {
				GWT.log("Failed to save display " + cur.getCanonicalName(), e);
				Debug.println(e);
			}
		}
	}
}
