package org.iucn.sis.shared.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class AssessmentData extends BaseAssessment implements Referenceable, AuthorizableObject {

	/**
	 * HashMap<String(CanonicalName), Object data>
	 * 
	 * data is ArrayList for Fields, a HashMap of selected values for
	 * ClassificationSchemes
	 */
	private HashMap<String, Object> data;

	/**
	 * HashMap<String(CanonicalName), ArrayList<ReferenceUI> references>
	 */
	private HashMap<String, ArrayList<ReferenceUI>> references;

	public AssessmentData() {
		super();
		data = new HashMap<String, Object>();
		createData();
		references = new HashMap<String, ArrayList<ReferenceUI>>();
	}

	public String getFullURI() {
		return "resource/assessment/" + type;
	}
	
	public String getProperty(String key) {
		if( "region".equalsIgnoreCase(key) )
			return isGlobal() ? "global" : serializeRegionIDs(getRegionIDs());
		else
			return "";
	}
	

	public Map<String, Object> addData(Map<String, Object> newData) {
		Map<String, Object> oldData = new HashMap<String, Object>();
		for( Entry<String, Object> cur : newData.entrySet() )
			oldData.put( cur.getKey(), data.put(cur.getKey(), cur.getValue()) );
		
		return oldData;
	}

	public void addReference(ReferenceUI ref, String canonicalName) {

		if (references.get(canonicalName) == null)
			references.put(canonicalName, new ArrayList<ReferenceUI>());

		ref.setAssociatedField(canonicalName);
		if (!references.get(canonicalName).contains(ref)) // Deduping
															// purposes...
			references.get(canonicalName).add(ref);

		if (canonicalName.equals(CanonicalNames.RedListPublication) && !isDone)
			setDone(true);
	}

	public void addReferences(ArrayList<ReferenceUI> references, GenericCallback<Object> callback) {
		/*ReferenceCache.getInstance().addReferences(AssessmentCache.impl.getCurrentAssessment().getAssessmentID(),
				references);

		ReferenceCache.getInstance().addReferencesToAssessmentAndSave(references, "Global", callback);*/
	}

	public String buildXml() {
		String xml = "";

		xml += "<globalReferences>\r\n";
		if (references.get("Global") != null)
			for (ReferenceUI curRef : references.get("Global")) {
				xml += curRef.toXML();
			}
		xml += "</globalReferences>\r\n";

		// FOR EACH FIELD
		for (Iterator iter = data.keySet().iterator(); iter.hasNext();) {
			String cur = (String) iter.next();

			if (data.get(cur) instanceof ArrayList) {
				xml += "<field id=\"" + cur + "\">\r\n";

				// FOR EACH STRUCTURE
				for (Iterator innerIter = ((ArrayList) data.get(cur)).listIterator(); innerIter.hasNext();) {
					xml += "<structure>" + XMLUtils.clean((String) innerIter.next()) + "</structure>\r\n";
				}

				if (references.get(cur) != null)
					for (Iterator innerIter = ((ArrayList) references.get(cur)).listIterator(); innerIter.hasNext();) {
						xml += ((ReferenceUI) innerIter.next()).toXML();
					}

				xml += "</field>\r\n";
			} else {
				HashMap selected = (HashMap) data.get(cur);

				if (selected != null) {
					xml += "<classificationScheme id=\"" + cur + "\">\r\n";
					for (Iterator selectedIter = selected.keySet().iterator(); selectedIter.hasNext();) {
						String curSelected = (String) selectedIter.next();
						String selectedRefID = cur + "." + curSelected;
						
						xml += "<selected id=\"" + curSelected + "\">\r\n";

						ArrayList data = (ArrayList) selected.get(curSelected);
						for (Iterator dataIter = data.listIterator(); dataIter.hasNext();) {
							xml += "<structure>" + XMLUtils.clean((String) dataIter.next()) + "</structure>\r\n";
						}
						
						if (references.get(selectedRefID) != null)
							for (Iterator innerIter = ((ArrayList) references.get(selectedRefID)).listIterator(); innerIter.hasNext();) {
								xml += ((ReferenceUI) innerIter.next()).toXML();
							}
						xml += "</selected>\r\n";
					}

					if (references.get(cur) != null)
						for (Iterator innerIter = ((ArrayList) references.get(cur)).listIterator(); innerIter.hasNext();) {
							xml += ((ReferenceUI) innerIter.next()).toXML();
						}
					xml += "</classificationScheme>\r\n";
				}
			}
		}

		return xml;
	}

	private void createData() {
		if (!this.data.containsKey(CanonicalNames.RegionInformation)) {
			ArrayList data = new ArrayList(2);
			data.add("-1");
			data.add("true");
			this.data.put(CanonicalNames.RegionInformation, data);
		}

		if (!this.data.containsKey(CanonicalNames.RedListCriteria)) {
//			this.data.put(CanonicalNames.RedListCriteria, SISCategoryAndCriteria.generateDefaultDataList());
		}
	}

	public AssessmentData deepCopy() {
		String xml = toXML();
		AssessmentParser parser = new AssessmentParser();
		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.parse(xml);
		parser.parse(ndoc);
		AssessmentData ret = parser.getAssessment();
		ret.createData();

		ret.getDataMap().remove(CanonicalNames.RedListEvaluated);
		ret.getDataMap().remove(CanonicalNames.RedListEvaluators);
		ret.getDataMap().remove(CanonicalNames.RedListEvaluationDate);
		ret.getDataMap().remove(CanonicalNames.RedListConsistencyCheck);
		ret.getDataMap().remove(CanonicalNames.RedListCaveat);
		ret.getDataMap().remove(CanonicalNames.RedListPetition);
		ret.getDataMap().remove(CanonicalNames.RedListPublication);
		ret.getDataMap().remove(CanonicalNames.OtherPublication);
		ret.getDataMap().remove(CanonicalNames.RedListReasonsForChange);

		return ret;
	}

	public String getAssessors() {
		return getFirstDataPiece(CanonicalNames.RedListAssessors, "");
	}

	public String getCategoryAbbreviation() {
		return null;
		//return getDataPiece(SISCategoryAndCriteria.GENERATED_CATEGORY_INDEX, CanonicalNames.RedListCriteria, "");
	}

	public String getCategoryCriteria() {
		return null;
		//return getDataPiece(SISCategoryAndCriteria.GENERATED_CRITERIA_INDEX, CanonicalNames.RedListCriteria, "");
	}

	//
	// public void setRegion(String region) {
	// this.region = region;
	// }
	//
	public String getCategoryFuzzyResult() {
		return categoryFuzzyResult;
	}

	public String getCritVersion() {
		return null;
		//return getDataPiece(SISCategoryAndCriteria.CRIT_VERSION_INDEX, CanonicalNames.RedListCriteria, "");
	}

	public String getCurrentPopulationTrend() {
		return getFirstDataPiece(CanonicalNames.PopulationTrend, "");
	}

	public HashMap<String, Object> getDataMap() {
		return data;
	}

	/**
	 * Gets a piece out of the data array at specified index, returning it if
	 * found.
	 * 
	 * @param index
	 *            - index of the data in the array
	 * @param canonicalName
	 * @param failureData
	 * @return data if found, failureData if not.
	 */
	public String getDataPiece(int index, String canonicalName, String failureData) {
		try {
			if (data.containsKey(canonicalName))
				return ((ArrayList) data.get(canonicalName)).get(index).toString();
			else
				return failureData;
		} catch (Exception e) {
			return failureData;
		}
	}

	public String getDateAssessed() {
		if (getFirstDataPiece(CanonicalNames.RedListAssessmentDate, "").equals(""))
			return getDateFinalized();
		else
			return getFirstDataPiece(CanonicalNames.RedListAssessmentDate, null);
	}

	public String getEvaluators() {
		return getFirstDataPiece(CanonicalNames.RedListEvaluators, "");
	}

	/**
	 * Returns and ArrayList if normal field, or a HashMap if a
	 * classificationScheme
	 * 
	 * @param canonicalName
	 * @return ArrayList or HashMap
	 */
	public Object getFieldData(String canonicalName) {
		return data.get(canonicalName);
	}

	/**
	 * Gets first piece out of the data array, returning it if found.
	 * 
	 * @param canonicalName
	 * @param failureData
	 * @return data if found, failureData if not.
	 */
	public String getFirstDataPiece(String canonicalName, String failureData) {
		try {
			if (data.containsKey(canonicalName))
				return ((ArrayList) data.get(canonicalName)).get(0).toString();
			else
				return failureData;
		} catch (Exception e) {
			return failureData;
		}
	}

	//
	// public String getChangeReason() {
	// return changeReason;
	// }
	//
	// public void setChangeReason(String changeReason) {
	// this.changeReason = changeReason;
	// }
	//
	public String getLastSeen() {
		return getFirstDataPiece(CanonicalNames.DateLastSeen, "");
	}

	public String getManualCategoryAbbreviation() {
		return null;
		//return getDataPiece(SISCategoryAndCriteria.MANUAL_CATEGORY_INDEX, CanonicalNames.RedListCriteria, "");
	}

	public String getManualCategoryCriteria() {
		return null;
		//return getDataPiece(SISCategoryAndCriteria.MANUAL_CRITERIA_INDEX, CanonicalNames.RedListCriteria, "");
	}

	// public boolean isEndemicToRegion() {
	// return endemicToRegion;
	// }
	//	
	// public void setEndemicToRegion(boolean endemicToRegion) {
	// this.endemicToRegion = endemicToRegion;
	// }
	//	
	// public boolean isPossiblyExtinctCandidate() {
	// return possiblyExtinctCandidate;
	// }
	//
	// public void setPossiblyExtinctCandidate(boolean possiblyExtinctCandidate)
	// {
	// this.possiblyExtinctCandidate = possiblyExtinctCandidate;
	// }
	//
	// public boolean isGenuineChange() {
	// return genuineChange;
	// }
	//
	// public void setGenuineChange(boolean genuineChange) {
	// this.genuineChange = genuineChange;
	// }
	//
	// public boolean isGenuineRecent() {
	// return genuineRecent;
	// }
	//
	// public void setGenuineRecent(boolean genuineRecent) {
	// this.genuineRecent = genuineRecent;
	// }
	//
	// public boolean isGenuineSinceFirst() {
	// return genuineSinceFirst;
	// }
	//
	// public void setGenuineSinceFirst(boolean genuineSinceFirst) {
	// this.genuineSinceFirst = genuineSinceFirst;
	// }
	//
	// public boolean isNonGenuineChange() {
	// return nonGenuineChange;
	// }
	//
	// public void setNonGenuineChange(boolean nonGenuineChange) {
	// this.nonGenuineChange = nonGenuineChange;
	// }
	//
	// public boolean isCriteriaRevision() {
	// return criteriaRevision;
	// }
	//
	// public void setCriteriaRevision(boolean criteriaRevision) {
	// this.criteriaRevision = criteriaRevision;
	// }
	//
	// public boolean isCriteriaChange() {
	// return criteriaChange;
	// }
	//
	// public void setCriteriaChange(boolean criteriaChange) {
	// this.criteriaChange = criteriaChange;
	// }
	//
	// public boolean isKnowledgeNew() {
	// return knowledgeNew;
	// }
	//
	// public void setKnowledgeNew(boolean knowledgeNew) {
	// this.knowledgeNew = knowledgeNew;
	// }
	//
	// public boolean isKnowledgeCorrection() {
	// return knowledgeCorrection;
	// }
	//
	// public void setKnowledgeCorrection(boolean knowledgeCorrection) {
	// this.knowledgeCorrection = knowledgeCorrection;
	// }
	//
	// public boolean isKnowledgeCriteria() {
	// return knowledgeCriteria;
	// }
	//
	// public void setKnowledgeCriteria(boolean knowledgeCriteria) {
	// this.knowledgeCriteria = knowledgeCriteria;
	// }
	//
	// public boolean isTaxonomy() {
	// return taxonomy;
	// }
	//
	// public void setTaxonomy(boolean taxonomy) {
	// this.taxonomy = taxonomy;
	// }
	//
	// public boolean isOther() {
	// return other;
	// }
	//
	// public void setOther(boolean other) {
	// this.other = other;
	// }
	//
	// public boolean isNoChange() {
	// return noChange;
	// }
	//
	// public void setNoChange(boolean noChange) {
	// this.noChange = noChange;
	// }
	//

	//
	// public void setPossiblyExtinct(boolean possiblyExtinct) {
	// this.possiblyExtinct = possiblyExtinct;
	// }
	//
	// public String getCategoryText() {
	// return categoryText;
	// }
	//
	// public void setCategoryText(String categoryText) {
	// this.categoryText = categoryText;
	// }
	//
	// public void setDateAssessed(String dateAssessed) {
	// this.dateAssessed = dateAssessed;
	// }
	public String getNote() {
		return getFirstDataPiece(CanonicalNames.RedListNotes, "");
	}

	/**
	 * If manual category is chosen, returns as such. If not, returns expert
	 * system's.
	 */
	public String getProperCategoryAbbreviation() {
//		if (!isDone)
//			return "(Not Done)";

		String cat = "";

		if (isManual())
			cat = getManualCategoryAbbreviation();
		else
			cat = getCategoryAbbreviation();

		if (cat.equals(""))
			cat = "N/A";

		return cat;
	}

	/**
	 * If manual category is chosen, returns as such. If not, returns expert
	 * system's.
	 */
	public String getProperCriteriaString() {
		if (!isDone)
			return "(Not Done)";

		String crit = "";

		if (isManual())
			crit = getManualCategoryCriteria();
		else
			crit = getCategoryCriteria();

		if (crit.equals(""))
			crit = "N/A";

		return crit;
	}

	public String getRationale() {
		return getFirstDataPiece(CanonicalNames.RedListRationale, "");
	}

	public HashMap<String, ArrayList<ReferenceUI>> getReferences() {
		return references;
	}

	public ArrayList<ReferenceUI> getReferences(String fieldName) {
		if ((ArrayList<ReferenceUI>) references.get(fieldName) == null)
			return new ArrayList<ReferenceUI>();
		return references.get(fieldName);
	}

	/**
	 * This will return the reference objects existing in the references HashMap
	 * as a list, pulling them using HashMap.Entry.getValue(), so be careful -
	 * changes to these references will result in changes to the assessment's
	 * references.
	 * 
	 * @return ArrayList of ReferenceUI objects
	 */
	public ArrayList<ReferenceUI> getReferencesAsList() {
		ArrayList<ReferenceUI> refs = new ArrayList<ReferenceUI>();
		for (Entry<String, ArrayList<ReferenceUI>> curEntry : references.entrySet())
			refs.addAll(curEntry.getValue());

		return refs;
	}

	public String getRegionIDsCSV() {
		return getDataPiece(0, CanonicalNames.RegionInformation, "");
	}
	
	public List<String> getRegionIDs() {
		String ret = getDataPiece(0, CanonicalNames.RegionInformation, "");
		if( ret.equals("") )
			return new ArrayList<String>();
		else if( !ret.contains(",") )
			return Arrays.asList(new String [] { ret });
		else
			return Arrays.asList(ret.split(","));
	}
	
	@Override
	public int hashCode() {
		return getUID().hashCode();
	}
	
	private String serializeRegionIDs(List<String> regionIDs) {
		ArrayUtils.insertionSort(regionIDs, new PortableAlphanumericComparator());
		StringBuilder str = new StringBuilder();
		for( String curID : regionIDs )
			str.append("," + curID);
		
		return str.substring(1);
	}
	
	public boolean isDraft() {
		return getType().equals(DRAFT_ASSESSMENT_STATUS);
	}
	
	public boolean isPublished() {
		return getType().equals(PUBLISHED_ASSESSMENT_STATUS);
	}
	
	public boolean isEndemic() {
		boolean isEndemic = getDataPiece(1, CanonicalNames.RegionInformation, "false").equalsIgnoreCase("true");
		return isEndemic;
	}

	public boolean isGlobal() {
		List<String> regions = getRegionIDs();
		return regions.contains(GLOBAL_ID);
	}

	//
	// public void setNote(String note) {
	// this.note = note;
	// }
	//

	public boolean isManual() {
		return false;
		//return getDataPiece(SISCategoryAndCriteria.IS_MANUAL_INDEX, CanonicalNames.RedListCriteria, "")
			//	.equalsIgnoreCase("true");
	}

	//
	// public void setLastSeen(String lastSeen) {
	// this.lastSeen = lastSeen;
	// }
	//
	public boolean isPossiblyExtinct() {
		return getDataPiece(7, CanonicalNames.RedListCriteria, "").equalsIgnoreCase("true");
	}

	public boolean isRegional() {
		return !isGlobal();
	}

	// public void setRationale(String rationale) {
	// this.rationale = rationale;
	// }

	public void onReferenceChanged(final GenericCallback<Object> callback) {
		/*try {
			AssessmentUtilFactory.getSaveUtils().saveAssessment(this, new GenericCallback<Object>() {
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				public void onSuccess(Object result) {
					callback.onSuccess((String)result);
				}
			});
		} catch (InsufficientRightsException e) {
//			WindowUtils.errorAlert("Insufficient Permissions", "You do not have "
//					+ "permission to modify this assessment. The changes you " + "just made will not be saved.");
//			callback.onFailure(e);
		}*/
	}

	public boolean removeReference(ReferenceUI ref, String canonicalName) {
		return references.get(canonicalName).remove(ref);
	}

	public void removeReferences(ArrayList<ReferenceUI> references, final GenericCallback<Object> callback) {
		for (int i = 0; i < references.size(); i++)
			removeReference(references.get(i), (references.get(i)).getAssociatedField());

		/*try {
			AssessmentUtilFactory.getSaveUtils().saveAssessment(this, new GenericCallback<Object>() {
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				public void onSuccess(Object result) {
					callback.onSuccess((String)result);
				}
			});
		} catch (InsufficientRightsException e) {
//			WindowUtils.errorAlert("Insufficient Permissions", "You do not have "
//					+ "permission to modify this assessment. The changes you " + "just made will not be saved.");
//			callback.onFailure(e);
		}*/
	}

	public void setCategoryAbbreviation(String criteria) {
		//setDataPiece(SISCategoryAndCriteria.GENERATED_CATEGORY_INDEX, CanonicalNames.RedListCriteria, criteria);
	}

	public void setCategoryCriteria(String criteria) {
		//setDataPiece(SISCategoryAndCriteria.GENERATED_CRITERIA_INDEX, CanonicalNames.RedListCriteria, criteria);
	}

	public void setCategoryFuzzyResult(String result) {
		categoryFuzzyResult = result;
	}

	public void setCritVersion(String critVersion) {
		//setDataPiece(SISCategoryAndCriteria.CRIT_VERSION_INDEX, CanonicalNames.RedListCriteria, critVersion);
	}

	public void setData(HashMap data) {
		this.data = data;
	}

	/**
	 * Sets a piece in the data array. If something goes wrong, i.e. the array
	 * is not long enough, it will return false.
	 * 
	 * @param index
	 * @param canonicalName
	 * @param data
	 */
	public boolean setDataPiece(int index, String canonicalName, String dataToAdd) {
		try {
			if (data.containsKey(canonicalName)) {
				((ArrayList) data.get(canonicalName)).ensureCapacity(index);
				((ArrayList) data.get(canonicalName)).set(index, dataToAdd);
				return true;
			} else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void setDateAssessed(String date) {
		setFirstDataPiece(CanonicalNames.RedListAssessmentDate, date);
		// setDateFinalized(date);
	}

	public void setEndemic(boolean isEndemic) {
		setDataPiece(1, CanonicalNames.RegionInformation, Boolean.toString(isEndemic));
	}

	/**
	 * Sets first piece in the data array, creating it if necessary.
	 * 
	 * @param canonicalName
	 * @param data
	 */
	public boolean setFirstDataPiece(String canonicalName, String dataToAdd) {
		try {
			if (data.containsKey(canonicalName))
				((ArrayList) data.get(canonicalName)).set(0, dataToAdd);
			else {
				ArrayList dataList = new ArrayList();
				dataList.add(dataToAdd);
				data.put(canonicalName, dataList);
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void setIsManual(boolean isManual) {
		//setDataPiece(SISCategoryAndCriteria.IS_MANUAL_INDEX, CanonicalNames.RedListCriteria, "" + isManual);
	}

	public void setManualCategoryAbbreviation(String criteria) {
		//setDataPiece(SISCategoryAndCriteria.MANUAL_CATEGORY_INDEX, CanonicalNames.RedListCriteria, criteria);
	}

	public void setManualCategoryCriteria(String criteria) {
		//setDataPiece(SISCategoryAndCriteria.MANUAL_CRITERIA_INDEX, CanonicalNames.RedListCriteria, criteria);
	}

	public void setReferences(HashMap refs) {
		references = refs;
	}

	public void setRegionIDs(List<String> regionIDs) {
		if (!setDataPiece(0, CanonicalNames.RegionInformation, serializeRegionIDs(regionIDs)))
			System.out.println(" --- FAILED TO SET IS REGION NAME TO " + regionIDs + " --- ");
	}

	public void setRegionID(String regionID) {
		if (!setDataPiece(0, CanonicalNames.RegionInformation, regionID))
			System.out.println(" --- FAILED TO SET IS REGION NAME TO " + regionID + " --- ");
	}
	
	@Override
	public String toString() {
		return getUID();
	}
	
	@Override
	public String toXML() {
		String xml = super.toXML();
		xml += buildXml();
		xml += "</assessment>";

		return xml;
	}
}
