package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.utils.CanonicalNames;

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
		public Integer id;

		public AssessmentInfo(String status, Integer id) {
			this.status = status;
			this.id = id;
		}
	}

	public static final AssessmentCache impl = new AssessmentCache();
	private static final int NUMRECENTASSESSMENTS = 10;
	private static final int HOWOFTENTOSAVERECENTASSESSMENTS = 1;

	private Assessment currentAssessment;

	/**
	 * ArrayList that stores NUMRECENTASSESSMENTS number of the most recently
	 * view assessments. (Stores AssessmentInfo objects) null if hasn't been
	 * initialized
	 */
	private ArrayList<AssessmentInfo> recentAssessments;

	private int numSinceSaveRecent;

	private Map<Integer, Assessment> cache;
	private Map<Integer, List<Assessment>> taxonToAssessmentCache;
	private Map<Integer, Boolean> taxaFetched;
	
	private AssessmentCache() {
		cache = new HashMap<Integer, Assessment>();
		taxaFetched = new HashMap<Integer, Boolean>();
		taxonToAssessmentCache = new HashMap<Integer, List<Assessment>>();
		
		numSinceSaveRecent = 0;
	}

	public void addAssessment(Assessment assessment) {
		Assessment old = cache.put(Integer.valueOf(assessment.getId()), assessment);
		
		if( !taxonToAssessmentCache.containsKey(Integer.valueOf(assessment.getSpeciesID()) ) )
			taxonToAssessmentCache.put(Integer.valueOf(assessment.getSpeciesID()), new ArrayList<Assessment>());
		else if( old != null )
			taxonToAssessmentCache.remove(old);
			
		taxonToAssessmentCache.get(Integer.valueOf(assessment.getSpeciesID())).add(assessment);
	}
	
	public void clear() {
		cache.clear();
		taxonToAssessmentCache.clear();
		taxaFetched.clear();
	}
	
	public boolean contains(Integer id) {
		return cache.containsKey(id);
	}
	
	public boolean contains(int id) {
		return contains(Integer.valueOf(id));
	}
	
	public void createGlobalDraftAssessments(List<Integer> taxaIDs, boolean useTemplate,
			AssessmentFilter filter, final GenericCallback<String> wayback) {
		if( taxaIDs.size() == 0 )
			wayback.onSuccess("0");
		else {
			StringBuilder ret = new StringBuilder("<create>");
			ret.append(filter.toXML());
			ret.append("<useTemplate>" + useTemplate + "</useTemplate>");
			for( Integer curID : taxaIDs ) {
				ret.append("<taxon>");
				ret.append(curID);
				ret.append("</taxon>");
			}
			ret.append("</create>");

			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
			ndoc.postAsText(UriBase.getInstance().getSISBase() +"/assessments?action=batch", ret.toString(), new GenericCallback<String>() {
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
	 * @param type - the assessment type, see AssessmentType
	 * @param template - an assessment to be used as a template, or null to create from scratch
	 * @param region - the Locality specification for this new assessment
	 * @param endemic TODO
	 * @param wayback - a GenericCallback
	 */
	public void createNewAssessment(final Taxon taxon, final String type, Assessment template, 
			List<Integer> regions, boolean endemic, final GenericCallback<String> wayback) {
		Assessment newAss = null;
		
		if( template != null )
			newAss = template.deepCopy();
		else
			newAss = new Assessment();
		
		//newAss.setSpeciesID(taxon.getId());
		newAss.setTaxon(taxon);
		newAss.setType(type);
		newAss.setRegions(RegionCache.impl.getRegionsByID(regions), endemic);
		
		if( taxaFetched.containsKey(Integer.valueOf(taxon.getId())) )
			taxaFetched.remove(Integer.valueOf(taxon.getId()));
		
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.putAsText(UriBase.getInstance().getSISBase() + "/assessments", newAss.toXML(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final String newID = ndoc.getText();
				fetchAssessments(new AssessmentFetchRequest(Integer.valueOf(newID)), new GenericCallback<String>() {
					public void onSuccess(String result) {
						AssessmentCache.impl.getAssessment(Integer.valueOf(newID), true);
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

	public void evictAssessments(String ids) {
		if (ids == null)
			return;
		else {
			String[] assIDs = null;

			if (ids.indexOf(",") > -1)
				assIDs = ids.split(",");
			else
				assIDs = new String[] { ids };

			for (int i = 0; i < assIDs.length; i++)
				remove(assIDs[i]);
		}
	}

	public Assessment remove(int id) {
		return cache.remove(Integer.valueOf(id));
	}
	
	public Assessment remove(String id) {
		return cache.remove(Integer.valueOf(id));
	}
	
	public Assessment remove(Integer id) {
		return cache.remove(id);
	}
	
	public void fetchAssessments(AssessmentFetchRequest request, final GenericCallback<String> callback) {
		List<Integer> uidsToRemove = new ArrayList<Integer>();
		List<Integer> taxonIDsToRemove = new ArrayList<Integer>();
		
		for( Integer curTaxon : request.getTaxonIDs() ) {
			if( taxaFetched.containsKey(curTaxon) )
				taxonIDsToRemove.add(curTaxon);
			else
				taxaFetched.put(curTaxon, Boolean.valueOf(true));
		}
		
		for( Integer uid : request.getAssessmentUIDs() ) {
			if( AssessmentCache.impl.getAssessment(uid, false) != null )
				uidsToRemove.add(uid);
		}
		
		request.getTaxonIDs().removeAll(taxonIDsToRemove);
		request.getAssessmentUIDs().removeAll(uidsToRemove);
		
		if( request.getTaxonIDs().size() == 0 && request.getAssessmentUIDs().size() == 0 )
			callback.onSuccess("OK");
		else {
			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
			ndoc.post(UriBase.getInstance().getSISBase() + "/assessments?action=fetch", request.toXML(), new GenericCallback<String>() {
				public void onSuccess(String result) {
					NativeNodeList asses = ndoc.getDocumentElement().getElementsByTagName(Assessment.ROOT_TAG);
					for (int i = 0; i < asses.getLength(); i++) {
						NativeElement el = asses.elementAt(i);
						try {
							Assessment ass = Assessment.fromXML(el);
							Taxon t = TaxonomyCache.impl.getTaxon(ass.getSpeciesID());
							if( t != null )
								ass.setTaxon(t);

							addAssessment(ass);
							taxaFetched.put(Integer.valueOf(ass.getSpeciesID()), Boolean.TRUE);
							
						} catch (Throwable e) {
							Debug.println("Error caching assessment: {0}", e);
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

	public Assessment getCurrentAssessment() {
		return currentAssessment;
	}

	public Assessment getAssessment(int id, boolean setAsCurrent) {
		return getAssessment(Integer.valueOf(id), setAsCurrent);
	}
	
	public Assessment getAssessment(Integer id, boolean setAsCurrent) {
		Assessment ret = cache.get(id);
		
		if (setAsCurrent)
			setCurrentAssessment(ret);
		return ret;
	}
	
	public Assessment getUserAssessment(int id, boolean setAsCurrent) {
		return getUserAssessment(Integer.valueOf(id), setAsCurrent);
	}
		
	public Assessment getUserAssessment(Integer id, boolean setAsCurrent) {
		return getAssessment(id, setAsCurrent);
	}

	public Assessment getDraftAssessment(int id, boolean setAsCurrent) {
		return getDraftAssessment(Integer.valueOf(id), setAsCurrent);
	}
	
	public Assessment getDraftAssessment(Integer id, boolean setAsCurrent) {
		return getAssessment(id, setAsCurrent);
	}
	
	public Assessment getPublishedAssessment(int id, boolean setAsCurrent) {
		return getPublishedAssessment(Integer.valueOf(id), setAsCurrent);
	}
	
	public Assessment getPublishedAssessment(Integer id, boolean setAsCurrent) {
		return getAssessment(id, setAsCurrent);
	}

	public List<Assessment> getDraftAssessmentsForTaxon(int taxonID) {
		return getDraftAssessmentsForTaxon(Integer.valueOf(taxonID));
	}
	
	public List<Assessment> getDraftAssessmentsForTaxon(Taxon taxon) {
		return getDraftAssessmentsForTaxon(taxon.getId());
	}
	
	public List<Assessment> getDraftAssessmentsForTaxon(Integer taxonID) {
		return getAssessmentsForTaxon(taxonID, AssessmentType.DRAFT_ASSESSMENT_STATUS_ID);
	}
	
	public List<Assessment> getPublishedAssessmentsForTaxon(int taxonID) {
		return getPublishedAssessmentsForTaxon(Integer.valueOf(taxonID));
	}
	
	public List<Assessment> getPublishedAssessmentsForTaxon(Integer taxonID) {
		return getAssessmentsForTaxon(taxonID, AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID);
	}
	
	public List<Assessment> getAssessmentsForTaxon(Integer taxonID, int assessmentType) {
		if( taxonToAssessmentCache.containsKey(taxonID) ) {
			List<Assessment> assessments = new ArrayList<Assessment>();
			for( Assessment cur : taxonToAssessmentCache.get(taxonID) ) {
				if( cur.getAssessmentType().getId() == assessmentType ) 
					assessments.add(cur);
			}
			
			return assessments;
		} else
			return new ArrayList<Assessment>();
	}
	
	public ArrayList<AssessmentInfo> getRecentAssessments() {
		return recentAssessments;
	}

	public void loadRecentAssessments(final GenericCallback<String> wayBacks) {
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.get(UriBase.getInstance().getRecentAssessmentsBase() + "/recentAssessments/" + SISClientBase.currentUser.getUsername(), new GenericCallback<String>() {

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
						recentAssessments.add(i, new AssessmentInfo(status, Integer.valueOf(id)));
					}
				}
				wayBacks.onSuccess(arg0);

			}
		});
	}

	public void resetCurrentAssessment() {
		setCurrentAssessment(null);
	}

	
	public void setCurrentAssessment(final Assessment assessment) {
		if (assessment != null) {
			TaxonomyCache.impl.fetchTaxon(assessment.getSpeciesID(), true, new GenericCallback<Taxon>() {
				public void onSuccess(Taxon result) {
					doSetCurrentAssessment(assessment);
				}
				public void onFailure(Throwable caught) {
					doSetCurrentAssessment(assessment);
				}
			});
		} else {
			doSetCurrentAssessment(assessment);
		}
		
	}
	
	private void doSetCurrentAssessment(Assessment assessment) {
		if( assessment != null && assessment.getType().equals(AssessmentType.DRAFT_ASSESSMENT_TYPE) && 
				!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.READ, assessment) ) {
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
		SISClientBase.getInstance().onAssessmentChanged();
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
		NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.post(UriBase.getInstance().getRecentAssessmentsBase() + "/recentAssessments/" + SISClientBase.currentUser.getUsername(), xml.toString(),
				new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(String arg0) {
			}
		});
	}
	
	private void updateRecentAssessments() {
		if (recentAssessments != null) {
			Assessment currentAss = getCurrentAssessment();
			String status = currentAss.getType();

			int index = -1;

			for (int i = 0; i < recentAssessments.size() && index < 0; i++) {
				AssessmentInfo current = recentAssessments.get(i);

				if (status.equals(current.status)) {
					if (current.id.equals(currentAss.getId()))
						index = i;
				}
			}

			// NOT ALREADY IN LIST
			if (index < 0) {
				Integer id = currentAss.getId();

				if (id != null) {
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

			// IF NEED TO SAVE TO SERVER
			if (numSinceSaveRecent == HOWOFTENTOSAVERECENTASSESSMENTS) {
				numSinceSaveRecent = 0;
				saveRecentAssessments();
			}
		}
	}
}
