package org.iucn.sis.client.api.assessment;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.FieldWidgetCache;
import org.iucn.sis.client.api.caches.StatusCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.acl.InsufficientRightsException;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.assessments.AssessmentUtils;
import org.iucn.sis.shared.api.displays.Display;
import org.iucn.sis.shared.api.displays.FieldDisplay;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;

public class AssessmentClientSaveUtils {

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
					try {
						if( fieldWidgets != null )
							saveWidgetDataToAssessment(fieldWidgets, assessmentToSave);

						final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
						ndoc.post(UriBase.getInstance().getSISBase() +"/assessments", assessmentToSave.toXML(), new GenericCallback<String>() {
							public void onFailure(Throwable caught) {
								if( ndoc.getStatusText().indexOf("409") > -1 ) {
									callback.onFailure(new Exception("A draft assessment " +
											"with the specified regions already exists. Please modify " +
									"your choice of regions and try again."));
								} else {
									callback.onFailure(new Exception("Please check your internet " +
									"connection and try again."));
								}
							}

							public void onSuccess(String result) {
								//assessmentToSave.setDateModified(Long.valueOf(ndoc.getText()));
								try {
								Assessment ret = Assessment.fromXML(ndoc);
								for( Field curField : ret.getField() ) {
									Field clientField = assessmentToSave.getField(curField.getName());
									if( clientField == null ) {
										System.out.print("Missing the client field " + curField.getName() + "...????");
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
									e.printStackTrace();
								}
								
								StatusCache.impl.setStatus(assessmentToSave, StatusCache.HAS_LOCK);
								callback.onSuccess(result);
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
						callback.onFailure(e);
						return;
					}
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
				System.out.println("Field " + cur.getCanonicalName() + " has changed? " + cur.hasChanged());
				if( cur.hasChanged() )
					return true;
			}
			
			return false;
		}
	}

	protected static void saveWidgetDataToAssessment(final List<Display> fieldWidgets,
			final Assessment assessmentToSave) {
		for( Display cur : fieldWidgets ) {
			System.out.println("Saving data to Field " + cur.getCanonicalName());
			cur.save();
			Field curField = cur.getField();
			if( curField.getPrimitiveField().size() == 0 && curField.getFields().size() == 0 )
				assessmentToSave.getField().remove(curField);
			else {
				assessmentToSave.getField().add(curField);
				curField.setAssessment(assessmentToSave);
			}
		}
	}
}
