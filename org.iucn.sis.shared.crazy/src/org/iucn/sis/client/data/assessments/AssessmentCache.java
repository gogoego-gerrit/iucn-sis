package org.iucn.sis.client.data.assessments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.FieldWidgetCache;
import org.iucn.sis.shared.data.StatusCache;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentFilter;
import org.iucn.sis.shared.data.assessments.AssessmentParser;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Html;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

public class AssessmentCache {

	/**
	 * 
	 * status is either published_status, draft_status, or user_status id is the
	 * filename minus .xml
	 * 
	 */
	public class AssessmentInfo {
		public String status;
		public String id;

		public AssessmentInfo(String status, String id) {
			this.status = status;
			this.id = id;
		}
	}

	public static final AssessmentCache impl = new AssessmentCache();
	private static final int NUMRECENTASSESSMENTS = 10;
	private static final int HOWOFTENTOSAVERECENTASSESSMENTS = 1;

	private AssessmentData currentAssessment;
	private AssessmentParser parser;

	/**
	 * ArrayList that stores NUMRECENTASSESSMENTS number of the most recently
	 * view assessments. (Stores AssessmentInfo objects) null if hasn't been
	 * initialized
	 */
	private ArrayList<AssessmentInfo> recentAssessments;

	private int numSinceSaveRecent;

	private HashMap<String, AssessmentData> publishedCache;
	private HashMap<String, AssessmentData> draftCache;
	private HashMap<String, AssessmentData> userCache;
	
	//Two easy-access caches
	private HashMap<String, HashMap<String, AssessmentData>> assessmentTypeToCache;
	private HashMap<String, List<AssessmentData>> taxonToDraftCache;
	private HashMap<String, Boolean> taxaFetched;
	
	private AssessmentCache() {
		parser = new AssessmentParser();
		publishedCache = new HashMap<String, AssessmentData>();
		draftCache = new HashMap<String, AssessmentData>();
		userCache = new HashMap<String, AssessmentData>();
		
		taxaFetched = new HashMap<String, Boolean>();
		
		assessmentTypeToCache = new HashMap<String, HashMap<String,AssessmentData>>();
		assessmentTypeToCache.put(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, publishedCache);
		assessmentTypeToCache.put(BaseAssessment.DRAFT_ASSESSMENT_STATUS, draftCache);
		assessmentTypeToCache.put(BaseAssessment.USER_ASSESSMENT_STATUS, userCache);
		
		taxonToDraftCache = new HashMap<String, List<AssessmentData>>();
		
		numSinceSaveRecent = 0;
	}

	public void addAssessment(AssessmentData assessment) {
		AssessmentData old = assessmentTypeToCache.get(assessment.getType()).put(assessment.getAssessmentID(), assessment);
		
		if( assessment.getType().equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS)) {
			if( !taxonToDraftCache.containsKey(assessment.getSpeciesID() ) )
				taxonToDraftCache.put(assessment.getSpeciesID(), new ArrayList<AssessmentData>());
			else if( old != null )
				taxonToDraftCache.remove(old);
			
			taxonToDraftCache.get(assessment.getSpeciesID()).add(assessment);
		} else
			assessmentTypeToCache.get(assessment.getType()).put(assessment.getAssessmentID(), assessment);
	}
	
	public void clear() {
		publishedCache.clear();
		draftCache.clear();
		userCache.clear();
		taxonToDraftCache.clear();
		taxaFetched.clear();
	}
	
	public void clearDrafts() {
		draftCache.clear();
		taxonToDraftCache.clear();
		taxaFetched.clear();
	}

	public boolean contains(String type, String id) {
		return assessmentTypeToCache.get(type).containsKey(id);
	}
	
	public void createGlobalDraftAssessments(List<String> taxaIDs, boolean useTemplate,
			AssessmentFilter filter, final GenericCallback<String> wayback) {
		if( taxaIDs.size() == 0 )
			wayback.onSuccess("0");
		else {
			StringBuilder ret = new StringBuilder("<create>");
			ret.append(filter.toXML());
			ret.append("<useTemplate>" + useTemplate + "</useTemplate>");
			for( String curID : taxaIDs ) {
				ret.append("<taxon>");
				ret.append(curID);
				ret.append("</taxon>");
			}
			ret.append("</create>");

			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.postAsText("/assessments?action=batch", ret.toString(), new GenericCallback<String>() {
				public void onSuccess(String result) {
					AssessmentCache.impl.clear();
					
					String message = ndoc.getText();
					com.extjs.gxt.ui.client.widget.Window w = WindowUtils.getWindow(true, false, "Batch Create Results");
					w.setScrollMode(Scroll.AUTOY);
					w.add(new Html(message));
					w.setClosable(true);
					w.show();
					w.center();
					w.setSize(400, 500);
				}
				public void onFailure(Throwable caught) {

				}
			});	
		}
	}
	
	/**
	 * Creates a new assessment.
	 * 
	 * @param taxon - the Taxon that is being assessed
	 * @param type - the assessment type, see BaseAssessment
	 * @param template - an assessment to be used as a template, or null to create from scratch
	 * @param region - the Locality specification for this new assessment
	 * @param endemic TODO
	 * @param wayback - a GenericCallback
	 */
	public void createNewAssessment(final TaxonNode taxon, final String type, AssessmentData template, 
			List<String> regions, boolean endemic, final GenericCallback<String> wayback) {
		AssessmentData newAss = null;
		
		if( template != null )
			newAss = template.deepCopy();
		else
			newAss = new AssessmentData();
		
		newAss.setSpeciesID(taxon.getId()+"");
		newAss.setSpeciesName(taxon.getFullName());
		newAss.setType(type);
		newAss.setRegionIDs(regions);
		newAss.setEndemic(endemic);
		newAss.setAssessmentID("new");
		
		if( taxaFetched.containsKey(taxon.getId()+"") )
			taxaFetched.remove(taxon.getId()+"");
		
		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.putAsText("/assessments", newAss.toXML(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final String newID = ndoc.getText();
				fetchAssessments(new AssessmentFetchRequest(newID+"_"+type), new GenericCallback<String>() {
					public void onSuccess(String result) {
						AssessmentCache.impl.getAssessment(type, newID, true);
						if( type.equals(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS) )
							taxon.getAssessments().add(0, AssessmentCache.impl.getCurrentAssessment().getAssessmentID());
					};
					public void onFailure(Throwable caught) {
						wayback.onFailure(caught);
					};
				});
				
				wayback.onSuccess(ndoc.getText());
			}
			public void onFailure(Throwable caught) {
				wayback.onFailure(caught);
			}
		});
	}
	
	public void doLogout() {
		currentAssessment = null;
		clear();
	}

	public void evictAssessments(String ids, String type) {
		if (ids == null)
			return;
		else {
			String[] assIDs = null;

			if (ids.indexOf(",") > -1)
				assIDs = ids.split(",");
			else
				assIDs = new String[] { ids };

			for (int i = 0; i < assIDs.length; i++)
				assessmentTypeToCache.get(type).remove(assIDs[i]);
		}
	}

	public void fetchAssessments(AssessmentFetchRequest request, final GenericCallback<String> callback) {
		List<String> uidsToRemove = new ArrayList<String>();
		List<String> taxonIDsToRemove = new ArrayList<String>();
		
		for( String curTaxon : request.getTaxonIDs() ) {
			if( taxaFetched.containsKey(curTaxon) )
				taxonIDsToRemove.add(curTaxon);
			else
				taxaFetched.put(curTaxon, Boolean.valueOf(true));
		}
		
		for( String uid : request.getAssessmentUIDs() ) {
			String id = uid.replaceFirst("_[a-zA-Z]+_status", "");
			String type = (uid.contains("draft") ? BaseAssessment.DRAFT_ASSESSMENT_STATUS : 
				uid.contains("pub") ? BaseAssessment.PUBLISHED_ASSESSMENT_STATUS : BaseAssessment.USER_ASSESSMENT_STATUS);
			
			if( AssessmentCache.impl.getAssessment(type, id, false) != null )
				uidsToRemove.add(uid);
		}
		
		request.getTaxonIDs().removeAll(taxonIDsToRemove);
		request.getAssessmentUIDs().removeAll(uidsToRemove);
		
		if( request.getTaxonIDs().size() == 0 && request.getAssessmentUIDs().size() == 0 )
			callback.onSuccess("OK");
		else {
			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.post("/assessments?action=fetch", request.toXML(), new GenericCallback<String>() {
				public void onSuccess(String result) {
					NativeNodeList asses = ndoc.getDocumentElement().getElementsByTagName("assessment");
					for (int i = 0; i < asses.getLength(); i++) {
						NativeElement el = asses.elementAt(i);
						String type = el.getElementByTagName("validationStatus").getTextContent();
						String id = el.getElementByTagName("assessmentID").getTextContent();

						if( !contains(type, id) ) {
							parser.parse(el);
							addAssessment(parser.getAssessment());
						}
					}
					callback.onSuccess(ndoc.getStatusText());
				}

				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
	}
	
	public String fetchRecentAssessments() {
		StringBuffer csv = new StringBuffer();
		if (recentAssessments != null) {
			for (int i = 0; i < recentAssessments.size(); i++) {
				AssessmentInfo temp = (AssessmentInfo) recentAssessments.get(i);
				csv.append(temp.id + ":" + temp.status + ",");
			}
			if (csv.length() > 0)
				return csv.toString().substring(0, csv.length() - 1);
			else
				return csv.toString();
		} else {
			return null;
		}
	}

	public AssessmentData getCurrentAssessment() {
		return currentAssessment;
	}

	public AssessmentData getAssessment(String type, String id, boolean setAsCurrent) {
		AssessmentData ret = assessmentTypeToCache.get(type).get(id);
		if (setAsCurrent)
			setCurrentAssessment(ret);
		return ret;
	}
	
	public List<AssessmentData> getAllAssessmentsForTaxon(TaxonNode taxon) {
		List<AssessmentData> ret = new ArrayList<AssessmentData>();
		
		if( taxonToDraftCache.containsKey(taxon.getId()+"") )
			ret.addAll(taxonToDraftCache.get(taxon.getId()+""));
		
		for( String pubID : taxon.getAssessments() )
			ret.add(getPublishedAssessment(pubID, false));
		
		ret.add(userCache.get(taxon.getId()+""));
		
		return ret;
	}
	
	public List<AssessmentData> getDraftAssessmentsForTaxon(TaxonNode taxon) {
		return getDraftAssessmentsForTaxon(taxon.getId()+"");
	}
	
	public AssessmentData getUserAssessment(String id, boolean setAsCurrent) {
		return getAssessment(BaseAssessment.USER_ASSESSMENT_STATUS, id, setAsCurrent);
	}
	
	public AssessmentData getDraftAssessment(String id, boolean setAsCurrent) {
		return getAssessment(BaseAssessment.DRAFT_ASSESSMENT_STATUS, id, setAsCurrent);
	}
	
	public AssessmentData getPublishedAssessment(String id, boolean setAsCurrent) {
		return getAssessment(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, id, setAsCurrent);
	}

	public List<AssessmentData> getDraftAssessmentsForTaxon(String taxonID) {
		if( taxonToDraftCache.containsKey(taxonID) )
			return taxonToDraftCache.get(taxonID);
		else
			return new ArrayList<AssessmentData>();
	}
	
	public ArrayList<AssessmentInfo> getRecentAssessments() {
		return recentAssessments;
	}

	public void loadRecentAssessments(final GenericCallback<String> wayBacks) {
		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.get("/recentAssessments/" + SimpleSISClient.currentUser.getUsername(), new GenericCallback<String>() {

			public void onFailure(Throwable caught) {
				recentAssessments = new ArrayList<AssessmentInfo>();
				wayBacks.onFailure(caught);
			}

			public void onSuccess(String arg0) {
				recentAssessments = new ArrayList<AssessmentInfo>();
			
				if( !ndoc.getStatusText().equals("204") ) {	
					NativeNodeList assessments = ndoc.getDocumentElement().getElementsByTagName("assessment");

					for (int i = 0; i < assessments.getLength(); i++) {
						NativeElement assessment = assessments.elementAt(i);
						String status = assessment.getAttribute("status");
						String id = assessment.getTextContent();
						recentAssessments.add(i, new AssessmentInfo(status, id));
					}
				}
				wayBacks.onSuccess(arg0);

			}
		});
	}

	public void resetCurrentAssessment() {
		setCurrentAssessment(null);
		ClientUIContainer.bodyContainer.tabManager.panelManager.DEM.clearDEM();
	}

	
	public void setCurrentAssessment(AssessmentData assessment) {
		if( assessment != null && assessment.getType().equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS) && 
				!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, assessment) ) {
			WindowUtils.errorAlert("Insufficient Rights", "Sorry, you don't have rights to select this Draft assessment.");
			currentAssessment = null;
		} else {
			currentAssessment = assessment;

			if (currentAssessment != null) {
				updateRecentAssessments();
				StatusCache.impl.checkStatus(currentAssessment, true, new GenericCallback<Integer>() {
					public void onFailure(Throwable caught) {
						// Nothing to do, really.
					}

					public void onSuccess(Integer result) {
						// Nothing to do, really.
					}
				});
			}
		}

		FieldWidgetCache.impl.resetWidgetContents();
		ClientUIContainer.bodyContainer.tabManager.panelManager.DEM.updateWorkflowStatus();
		ClientUIContainer.headerContainer.assessmentChanged();
	}

	/**
	 * saves a list of recent assessments to the server, silently succeeds and
	 * fails.
	 */
	private void saveRecentAssessments() {
		StringBuffer xml = new StringBuffer("<recent>\r\n");
		for (int i = 0; i < recentAssessments.size(); i++) {
			AssessmentInfo temp = (AssessmentInfo) recentAssessments.get(i);
			xml.append("<assessment status=\"" + temp.status + "\">" + temp.id + "</assessment>\r\n");
		}
		xml.append("</recent>");
		NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.post("/recentAssessments/" + SimpleSISClient.currentUser.getUsername(), xml.toString(),
				new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(String arg0) {
			}
		});
	}
	
	private void updateRecentAssessments() {
		if (recentAssessments != null) {
			AssessmentData currentAss = getCurrentAssessment();
			String status = currentAss.getType();

			int index = -1;

			for (int i = 0; i < recentAssessments.size() && index < 0; i++) {
				AssessmentInfo current = recentAssessments.get(i);

				if (status.equals(current.status)) {
					if (current.id.equalsIgnoreCase(currentAss.getAssessmentID()))
						index = i;
				}
			}

			// NOT ALREADY IN LIST
			if (index < 0) {
				String id = currentAss.getAssessmentID();

				if (id != null && !id.equals("")) {
					recentAssessments.add(0, new AssessmentInfo(status, id));
					if (recentAssessments.size() > NUMRECENTASSESSMENTS) {
						recentAssessments.remove(NUMRECENTASSESSMENTS);
					}
				}
				numSinceSaveRecent++;
			} else {
				recentAssessments.add(0, recentAssessments.remove(index));
				numSinceSaveRecent++;
			}

			ClientUIContainer.bodyContainer.tabManager.panelManager.recentAssessmentsPanel.refresh();

			// IF NEED TO SAVE TO SERVER
			if (numSinceSaveRecent == HOWOFTENTOSAVERECENTASSESSMENTS) {
				numSinceSaveRecent = 0;
				saveRecentAssessments();
			}

		}
	}
}
