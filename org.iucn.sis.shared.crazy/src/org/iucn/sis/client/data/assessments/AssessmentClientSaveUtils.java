package org.iucn.sis.client.data.assessments;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.expert.ExpertUtils;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.acl.InsufficientRightsException;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.FieldWidgetCache;
import org.iucn.sis.shared.data.StatusCache;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentParser;
import org.iucn.sis.shared.data.assessments.AssessmentUtils;
import org.iucn.sis.shared.data.assessments.CanonicalNames;

import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class AssessmentClientSaveUtils implements AssessmentUtils {
	
	private boolean isSavedNeeded(NativeDocument newXMLDoc, NativeDocument oldXMLDoc) {
		boolean save = false;

		NativeNodeList newList = newXMLDoc.getDocumentElement().getElementsByTagName("field");
		NativeNodeList oldList = oldXMLDoc.getDocumentElement().getElementsByTagName("field");

		for (int i = 0; i < newList.getLength() && !save; i++) {
			NativeElement newFieldElement = newList.elementAt(i);
			NativeNodeList newStructureElements = newFieldElement.getElementsByTagName("structure");

			// IF IT DOESN'T HAVE ANY STRUCTURES, DOESN'T NEED TO CHANGE
			// WE NEED TO CHECK TO MAKE SURE THAT THEY ARE THE SAME AS NEW ONES
			if (newStructureElements.getLength() != 0) {
				boolean notFound = true;
				String newFieldID = newFieldElement.getAttribute("id");

				// Loop through old list of fields, see if you can find it.
				for (int j = 0; j < oldList.getLength() && !save && notFound; j++) {
					NativeElement oldFieldElement = (NativeElement) oldList.item(j);

					// If you've found it...
					String idatt = oldFieldElement.getAttribute("id");
					if (idatt != null && idatt.equals(newFieldID)) {
						notFound = false;
						NativeNodeList oldStructureElements = oldFieldElement.getElementsByTagName("structure");

						// If somehow a structure's data set isn't defined
						// correctly...
						if (oldStructureElements.getLength() != 0
								&& newStructureElements.getLength() != oldStructureElements.getLength())
							return true;

						for (int k = 0; k < newStructureElements.getLength(); k++) {
							if (!oldStructureElements.elementAt(k).getTextContent().equals(
									newStructureElements.elementAt(k).getTextContent())) {
								SysDebugger.getInstance().println(
										"In isSaveNeeded: Structure " + k + " in field " + newFieldID + " mismatch.");
								SysDebugger.getInstance().println(
										oldStructureElements.elementAt(k).getTextContent() + " != "
										+ newStructureElements.elementAt(k).getTextContent());
								save = true;
							}
						}
					}
				}
				// IF OLD DIDN'T HAVE ATTRIBUTE NEEDED TO SAVE
				if (notFound) {
					SysDebugger.getInstance().println("In isSaveNeeded: Field " + newFieldID + " not found.");

					boolean notEmpty = false;
					for (int j = 0; j < newStructureElements.getLength(); j++) {
						String curText = newStructureElements.elementAt(j).getText();

						if (!(curText.equalsIgnoreCase("") || curText.equalsIgnoreCase("0") || curText
								.equalsIgnoreCase("false"))) {
							notEmpty = true;
						}
					}

					if (notEmpty)
						save = true;
				}
			}
		}

		SysDebugger.getInstance().println("Is save needed after checking fields? " + save);
		// NEEDS TO SEE IF CLASSIFICATION SCHEMES HAVE BEEN CHANGED
		if (!save) {
			newList = newXMLDoc.getDocumentElement().getElementsByTagName("classificationScheme");
			oldList = oldXMLDoc.getDocumentElement().getElementsByTagName("classificationScheme");

			for (int i = 0; i < newList.getLength() && !save; i++) {
				NativeElement newClassScheme = newList.elementAt(i);

				boolean newClassSchemeNotFound = true;
				String newClassShemeID = newClassScheme.getAttribute("id");

				// Loop through old list of classification schemes, see if you
				// can find it.
				for (int j = 0; j < oldList.getLength() && !save && newClassSchemeNotFound; j++) {
					NativeElement oldClassScheme = oldList.elementAt(j);

					// If you've found the classification scheme...
					if ((oldClassScheme.getAttribute("id").equals(newClassShemeID))) {
						newClassSchemeNotFound = false;

						NativeNodeList newSelected = newClassScheme.getElementsByTagName("selected");
						NativeNodeList oldSelected = oldClassScheme.getElementsByTagName("selected");

						// IF there's a new amount of selected class scheme
						// options ... gotta save
						if (newSelected.getLength() != oldSelected.getLength()) {
							SysDebugger.getInstance().println(
									"In isSaveNeeded: New amount of selected options " + newSelected.getLength()
									+ " vs. " + oldSelected.getLength());
							save = true;
						} else {
							for (int k = 0; k < newSelected.getLength() && !save; k++) {
								NativeElement curNewSelected = newSelected.elementAt(k);

								boolean newSelectedNotFound = true;
								String curNewSelectedID = curNewSelected.getAttribute("id");

								for (int l = 0; l < oldSelected.getLength() && !save; l++) {
									NativeElement curOldSelected = oldSelected.elementAt(l);

									// If we find a matching "selected" tag in
									// the old set...check its structs
									if (curOldSelected.getAttribute("id").equals(curNewSelectedID)) {
										newSelectedNotFound = false;

										NativeNodeList oldStructureElements = curOldSelected
										.getElementsByTagName("structure");
										NativeNodeList newStructureElements = curNewSelected
										.getElementsByTagName("structure");

										if (oldStructureElements.getLength() != newStructureElements.getLength())
											save = true;
										else
											for (int m = 0; m < newStructureElements.getLength(); m++) {
												if (!oldStructureElements.elementAt(m).getText().equals(
														newStructureElements.elementAt(m).getText())) {
													SysDebugger.getInstance().println(
															"In isSaveNeeded: Differing struct data in scheme "
															+ newClassShemeID + ", selected "
															+ curNewSelectedID);
													SysDebugger.getInstance().println(
															oldStructureElements.elementAt(m).getText() + " vs. "
															+ newStructureElements.elementAt(m).getText());
													save = true;
												}
											}
									}
								}
								if (newSelectedNotFound) {
									SysDebugger.getInstance().println(
											"In isSaveNeeded: Class scheme selected ID" + curNewSelectedID
											+ " in scheme " + newClassShemeID + " not found.");
									save = true;
								}
							}
						}
					}
				}
				// IF OLD DIDN'T HAVE ATTRIBUTE NEEDED TO SAVE
				if (newClassSchemeNotFound) {
					if (newClassScheme.getElementsByTagName("selected").getLength() != 0)
						save = true;
				}
			}
		}

		SysDebugger.getInstance().println("In isSaveNeeded: Is save needed? " + save);
		return save;
	}
	
	/**
	 * Tries to save the assessment passed as an argument. If the assessmentToSave parameter is null
	 * it will try to pull the current assessment to save.
	 * 
	 * @param assessmentToSave
	 * @param callback
	 */
	public void saveAssessment(final AssessmentData assessmentToSave, final GenericCallback<Object> callback)
	throws InsufficientRightsException {

		if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, assessmentToSave)) {
			throw new InsufficientRightsException();
		}

		StatusCache.impl.checkStatus(assessmentToSave, false, new GenericCallback<Integer>() {
			public void onFailure(Throwable caught) {
				// Nothing extra to print out here.
			}

			public void onSuccess(Integer result) {
				if (result == StatusCache.UNLOCKED || result == StatusCache.HAS_LOCK) {
					try {
						final Map<String, Object> oldData = saveDataToCurrentAssessment();
						if( oldData != null ) {
							String xml = assessmentToSave.toXML();

							final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
							ndoc.postAsText("/assessments", xml, new GenericCallback<String>() {
								public void onFailure(Throwable caught) {
									if( ndoc.getStatusText().indexOf("409") > -1 ) {
										revertData(oldData, true);
										callback.onFailure(new Exception("A draft assessment " +
												"with the specified regions already exists. Please modify " +
												"your choice of regions and try again."));
									} else {
										revertData(oldData, false);
										callback.onFailure(new Exception("Please check your internet " +
												"connection and try again."));
									}
								}

								public void onSuccess(String result) {
									assessmentToSave.setDateModified(Long.valueOf(ndoc.getText()));
									StatusCache.impl.setStatus(assessmentToSave, StatusCache.HAS_LOCK);
									callback.onSuccess(result);
								}
							});
						}
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

	private void revertData(Map<String, Object> oldData, boolean updateRegionField) {
		AssessmentData asm = AssessmentCache.impl.getCurrentAssessment();
		for( Entry<String, Object> entry : oldData.entrySet() ) {
			if( entry.getValue() != null ) {
				asm.getDataMap().put(entry.getKey(), entry.getValue());
			} else {
				asm.getDataMap().remove(entry.getKey());
			}
			
			if( updateRegionField && entry.getKey().equals(CanonicalNames.RegionInformation) )
				FieldWidgetCache.impl.addAssessmentDataToDisplay(FieldWidgetCache.impl.get(entry.getKey()));
		}
	}
	
	private Map<String, Object> saveDataToCurrentAssessment() {
		String fieldsXML = FieldWidgetCache.impl.getCurrentFields();
		Map<String, Object> oldData = new HashMap<String, Object>();

		// IE WORKAROUND - IT BARFS WHEN TRYING TO PARSE BARE ASSESSMENT TAGS
		if (!fieldsXML.equalsIgnoreCase("")) {
			String fieldsCurrentXML = "<assessment>\n" + fieldsXML + "</assessment>\n";

			NativeDocument newXMLDoc = SimpleSISClient.getHttpBasicNativeDocument();
			newXMLDoc.parse(fieldsCurrentXML);

			HashMap<String, Object> dataToAddToAssessment = new HashMap<String, Object>();

			AssessmentParser parser = new AssessmentParser();
			dataToAddToAssessment = parser.parseDataOnly(newXMLDoc);

//			if( AssessmentCache.impl.getCurrentAssessment().getType().equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS) && 
//					dataToAddToAssessment.containsKey(CanonicalNames.RegionInformation) ) {
//				String isRegional = ((ArrayList<String>)dataToAddToAssessment.get(CanonicalNames.RegionInformation)).get(0);
//				if( !isRegional.equals(AssessmentCache.impl.getCurrentAssessment().getDataPiece(0, CanonicalNames.RegionInformation, "")) ) {
//					WindowUtils.hideLoadingAlert();
//					WindowUtils.errorAlert("Cannot Change Regionality", "Changing a global draft " +
//						"assessment to be regional or a regional to be global is not currently supported. " +
//						"You can create a new assessment using this as a template as a temporary " +
//						"workaround. Apologies for the inconvenience.");
//					return false;
//				}
//			}
				
			 oldData = AssessmentCache.impl.getCurrentAssessment().addData(dataToAddToAssessment);
		}

		ExpertUtils.processAssessment(AssessmentCache.impl.getCurrentAssessment());

		FieldWidgetCache.impl.addAssessmentDataToDisplay(FieldWidgetCache.impl.get(CanonicalNames.RedListCriteria));

		return oldData;
	}


	/**
	 * Compares the saved assessment to what is currently displayed in the field
	 * widgets. Returns whether or not the data needs to be saved.
	 * 
	 * @return boolean - save needed or not
	 */
	public boolean shouldSaveCurrentAssessment() {

		if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, AssessmentCache.impl.getCurrentAssessment())) {
			return false;
		} else {
			String fieldsXML = FieldWidgetCache.impl.getCurrentFields();

			// IE WORKAROUND - IT BARFS WHEN TRYING TO PARSE BARE ASSESSMENT
			// TAGS
			if (fieldsXML.equalsIgnoreCase(""))
				return false;

			String fieldsCurrentXML = "<assessment>\n" + fieldsXML + "</assessment>\n";
			String fieldsSaved = "<assessment>\n" + AssessmentCache.impl.getCurrentAssessment().buildXml() + "</assessment>\n";

			NativeDocument newXMLDoc = SimpleSISClient.getHttpBasicNativeDocument();
			NativeDocument oldXMLDoc = SimpleSISClient.getHttpBasicNativeDocument();

			newXMLDoc.parse(fieldsCurrentXML);
			oldXMLDoc.parse(fieldsSaved);

			return isSavedNeeded(newXMLDoc, oldXMLDoc);
		}
	}
}
