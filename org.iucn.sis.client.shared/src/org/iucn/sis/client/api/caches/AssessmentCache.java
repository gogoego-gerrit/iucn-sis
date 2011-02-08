package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.RecentlyAccessed;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
import org.iucn.sis.shared.api.models.fields.RegionField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.core.client.GWT;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

public class AssessmentCache {

	public static final AssessmentCache impl = new AssessmentCache();

	private Map<Integer, Assessment> cache;
	private Map<Integer, List<Assessment>> taxonToAssessmentCache;
	private Map<Integer, Boolean> taxaFetched;
	
	private AssessmentCache() {
		cache = new HashMap<Integer, Assessment>();
		taxaFetched = new HashMap<Integer, Boolean>();
		taxonToAssessmentCache = new HashMap<Integer, List<Assessment>>();
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
	public void createNewAssessment(final Taxon taxon, final String type, final String schema, Assessment template, 
			List<Integer> regions, boolean endemic, final GenericCallback<String> wayback) {
		Assessment newAssessment = null;
		
		if (template != null)
			newAssessment = template.deepCopy(new AssessmentCopyFilter());
		else
			newAssessment = new Assessment();
		
		newAssessment.setTaxon(taxon);
		newAssessment.setType(type);
		newAssessment.setSchema(schema);
		
		Field regionField = new Field(CanonicalNames.RegionInformation, newAssessment);
		
		RegionField proxy = new RegionField(regionField);
		proxy.setRegions(regions);
		proxy.setEndemic(endemic);
		
		newAssessment.setField(regionField);
		
		if (taxaFetched.containsKey(Integer.valueOf(taxon.getId())))
			taxaFetched.remove(Integer.valueOf(taxon.getId()));
		
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.putAsText(UriBase.getInstance().getSISBase() + "/assessments", newAssessment.toXML(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final String newID = ndoc.getText();
				fetchAssessments(new AssessmentFetchRequest(Integer.valueOf(newID)), new GenericCallback<String>() {
					public void onSuccess(String result) {
						//AssessmentCache.impl.getAssessment(Integer.valueOf(newID), true);
						StateManager.impl.setAssessment(impl.getAssessment(Integer.valueOf(newID)));
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
		clear();
	}

	public void evictAssessments(String ids) {
		if (ids != null) {
			String[] array = ids.indexOf(",") > -1 ? ids.split(",") : new String[] { ids } ;
			for (String id : array)
				remove(id);
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
			if( AssessmentCache.impl.getAssessment(uid) != null )
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
							Assessment current = Assessment.fromXML(el);
							Taxon t = TaxonomyCache.impl.getTaxon(current.getSpeciesID());
							if (t != null)
								current.setTaxon(t);

							addAssessment(current);
							taxaFetched.put(Integer.valueOf(current.getSpeciesID()), Boolean.TRUE);
							
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

	public Assessment getCurrentAssessment() {
		return StateManager.impl.getAssessment();
	}
	
	public Assessment getAssessment(Integer id) {
		return cache.get(id);
	}
	
	public Assessment getUserAssessment(int id) {
		return getUserAssessment(Integer.valueOf(id));
	}
		
	public Assessment getUserAssessment(Integer id) {
		return getAssessment(id);
	}

	public Assessment getDraftAssessment(Integer id) {
		return getAssessment(id);
	}
	
	public Assessment getPublishedAssessment(Integer id) {
		return getAssessment(id);
	}
	
	public Set<Assessment> getDraftAssessmentsForTaxon(Integer taxonID) {
		return getDraftAssessmentsForTaxon(taxonID, SchemaCache.impl.getDefaultSchema());
	}
	
	public Set<Assessment> getDraftAssessmentsForTaxon(Integer taxonID, String schema) {
		return getAssessmentsForTaxon(taxonID, AssessmentType.DRAFT_ASSESSMENT_STATUS_ID, schema);
	}
	
	public Set<Assessment> getPublishedAssessmentsForTaxon(Integer taxonID) {
		return getPublishedAssessmentsForTaxon(taxonID, SchemaCache.impl.getDefaultSchema());
	}
	
	public Set<Assessment> getPublishedAssessmentsForTaxon(Integer taxonID, String schema) {
		return getAssessmentsForTaxon(taxonID, AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID, schema);
	}
	
	public Set<Assessment> getAssessmentsForTaxon(Integer taxonID, int assessmentType, String schema) {
		if ( taxonToAssessmentCache.containsKey(taxonID)) {
			Set<Assessment> assessments = new HashSet<Assessment>();
			for (Assessment cur : taxonToAssessmentCache.get(taxonID)) {
				String curSchema = cur.getSchema(SchemaCache.impl.getDefaultSchema());
				if ((schema == null || schema.equals(curSchema)) && 
						cur.getAssessmentType().getId() == assessmentType)
					assessments.add(cur);
			}
			return assessments;
		} else
			return new HashSet<Assessment>();
	}
	
	public List<RecentlyAccessedCache.RecentAssessment> getRecentAssessments() {
		return RecentlyAccessedCache.impl.list(RecentlyAccessed.ASSESSMENT);
	}

	/**
	 * 
	 * @deprecated use RecentlyAccessedCache directly.
	 */
	public void loadRecentAssessments(final GenericCallback<Object> wayBacks) {
		RecentlyAccessedCache.impl.load(RecentlyAccessed.ASSESSMENT, wayBacks);
	}
	
	public void uncache(Integer id) {
		cache.remove(id);
	}
	
	public void uncache(Collection<Integer> ids) {
		for (Integer id : ids)
			uncache(id);
	}

	/*public void resetCurrentAssessment() {
		setCurrentAssessment(null);
	}

	
	public void setCurrentAssessment(final Assessment assessment) {
		setCurrentAssessment(assessment, true);
	}
	
	public void setCurrentAssessment(final Assessment assessment, boolean saveIfNecessary) {
		if (assessment != null) {
			//Used to set the current taxon, no longer needed...
			TaxonomyCache.impl.fetchTaxon(assessment.getSpeciesID(), false, saveIfNecessary, new GenericCallback<Taxon>() {
				public void onSuccess(Taxon result) {
					doSetCurrentAssessment(result, assessment);
				}
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Unable to fetch taxon information, please try again later.");
					//doSetCurrentAssessment(assessment);
				}
			});
		} else
			doSetCurrentAssessment(null, null);
	}*/
	
	private void doSetCurrentAssessment(final Taxon parent, final Assessment assessment) {
		if( assessment != null && assessment.getType().equals(AssessmentType.DRAFT_ASSESSMENT_TYPE) && 
				!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.READ, assessment) ) {
			WindowUtils.errorAlert("Insufficient Rights", "Sorry, you don't have rights to select this Draft assessment.");
			//StateManager.impl.setState(null, null, null);
		} else {
			//StateManager.impl.setAssessment(parent, assessment);

			if (assessment != null) {
				try {
					updateRecentAssessments();
				} catch (Throwable e) {
					GWT.log("Failed to update recent assessments", e);
				}
				/*StatusCache.impl.checkStatus(assessment, true, new GenericCallback<Integer>() {
					public void onFailure(Throwable caught) {
						// Nothing to do, really.
					}

					public void onSuccess(Integer result) {
						// Nothing to do, really.
					}
				});*/
			}
		}

		FieldWidgetCache.impl.resetWidgetContents();
		SISClientBase.getInstance().onAssessmentChanged();
	}

	public void updateRecentAssessments() {
		RecentlyAccessedCache.impl.add(RecentlyAccessed.ASSESSMENT, 
			new RecentlyAccessedCache.RecentAssessment(getCurrentAssessment())
		);
	}
	
	private static class AssessmentCopyFilter implements Assessment.DeepCopyFilter {
		
		private final List<String> excluded;
		
		public AssessmentCopyFilter() {
			excluded = new ArrayList<String>();
			excluded.add("RedListAssessmentDate");
			excluded.add("RedListEvaluators");
			excluded.add("RedListAssessmentAuthors");
			excluded.add("RedListReasonsForChange");
			excluded.add("RedListPetition");
			excluded.add("RedListEvaluated");
			excluded.add("RedListConsistencyCheck");
		}
		
		@Override
		public Field copy(Assessment assessment, Field field) {
			if (excluded.contains(field.getName())) {
				/*
				 * First, exclude certain fields.
				 */
				return null;
			}
			else if (CanonicalNames.RedListCriteria.equals(field.getName())) {
				RedListCriteriaField proxy = new RedListCriteriaField(field);
				Integer version = proxy.getCriteriaVersion();
				if (0 == version.intValue()) {
					/*
					 * Will be 0 if there is no data or if 
					 * the most current version is selected. 
					 * Either way, we want to remove the data.
					 */
					return null;
				}
				else {
					/*
					 * Return the field, but remove the history text.
					 */
					Field copy = field.deepCopy(false, true);
					PrimitiveField<?> historyText = 
						copy.getPrimitiveField(RedListCriteriaField.RLHISTORY_TEXT_KEY);
					if (historyText != null)
						copy.getPrimitiveField().remove(historyText);
					
					return copy;
				}
			}
			else {
				/*
				 * Else, return a copy of the field
				 */
				return field.deepCopy(false, true);
			}
		}
		
	}
}
