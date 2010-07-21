package org.iucn.sis.server.api.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.shared.api.assessments.PublishedAssessmentsComparator;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Region;
import org.iucn.sis.shared.api.models.Relationship;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.query.ExperimentalSelectQuery;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.QConstraintGroup;
import com.solertium.db.query.QRelationConstraint;
import com.solertium.db.query.SelectQuery;

public class AssessmentFilterHelper {

	protected final AssessmentFilter filter;

	public AssessmentFilterHelper(AssessmentFilter filter) {
		this.filter = filter;
	}

	
	public List<Assessment> getAssessments( Integer taxaID) {
		ArrayList<Assessment> ret = new ArrayList<Assessment>();

		if (filter.isDraft()) {
			List<Assessment> draftAssessments = SIS.get().getAssessmentIO().readDraftAssessmentsForTaxon(taxaID);
			for (Assessment draft : draftAssessments)
				if (allowAssessment(draft))
					ret.add(draft);			
		}

		if (filter.isRecentPublished() || filter.isAllPublished()) {
			List<Assessment> publishedAssessments  = SIS.get().getAssessmentIO().readPublishedAssessmentsForTaxon(taxaID);
			Collections.sort(publishedAssessments, new PublishedAssessmentsComparator());
			for (Assessment published : publishedAssessments) {
				if (published != null) {
					if (allowAssessment(published)) {
						ret.add(published);
					}
				}
				
			}
		}

		return ret;
	}

	public boolean allowAssessment(Assessment assessment) {
		if (filter.isRecentPublished() && assessment.isPublished() && assessment.getIsHistorical() )
			return false;
		else if( filter.isDraft() && !assessment.isDraft() )
			return false;
		else {
			if (filter.getRelationshipName().equalsIgnoreCase(Relationship.ALL))
				return true;
			else if (filter.getRelationshipName().equalsIgnoreCase(Relationship.OR)) {
				List<Integer> regionIds = assessment.getRegionIDs();
				for (Region region : filter.getRegions())
					if (regionIds.contains(region.getId()))
						return true;
				
				return false; //If it hasn't returned yet, no region matched
			}
			else //AssessmentFilter.REGION_TYPE_AND
				return assessment.getRegionIDs().containsAll(filter.getRegions()) && filter.getRegions().containsAll(assessment.getRegionIDs());
		}
	}
	
	public Collection<Integer> getAssessmentIds(Integer taxaID) {
		
		SelectQuery query = new SelectQuery();
		query.select("assessment", "*");
		query.constrain(new QComparisonConstraint(new CanonicalColumnName("assessment","taxonid"), QConstraint.CT_EQUALS, taxaID));
		query.constrain(new QComparisonConstraint(new CanonicalColumnName("assessment","state"), QConstraint.CT_EQUALS, Assessment.ACTIVE));
		
		if (filter.isAllPublished() || filter.isRecentPublished()) {
			
			QConstraintGroup constraint = new QConstraintGroup();
			constraint.addConstraint(new QComparisonConstraint(new CanonicalColumnName("assessment","assessment_typeid"), QConstraint.CT_EQUALS, AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
			if (filter.isRecentPublished())
				constraint.addConstraint(QConstraint.CG_AND, new QComparisonConstraint(new CanonicalColumnName("assessment","historical"), QConstraint.CT_EQUALS, "true") );
			if (filter.isDraft()) {
				constraint.addConstraint(QConstraint.CG_OR, new QComparisonConstraint(new CanonicalColumnName("assessment","assessment_typeid"), QConstraint.CT_EQUALS, AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
			}
			query.constrain(constraint);
		} else if (filter.isDraft()){
			query.constrain(new QComparisonConstraint(new CanonicalColumnName("assessment","assessment_typeid"), QConstraint.CT_EQUALS, AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
		}
		
		
		
		if (!filter.isAllRegions()) {
			
			query.select("field", "*");
			query.select("primitive_field", "*");
			query.select("fk_list_primitive_values", "*");
			
			query.join("field", new QRelationConstraint(new CanonicalColumnName("assessment","id"), new CanonicalColumnName("field","assessmentid")));
			query.constrain(new QComparisonConstraint(new CanonicalColumnName("field","name"), QConstraint.CT_EQUALS, "RegionInformation"));
			query.join("primitive_field", new QRelationConstraint(new CanonicalColumnName("field", "id"), new CanonicalColumnName("primitive_field", "fieldid")));
			query.constrain(new QComparisonConstraint(new CanonicalColumnName("primitive_field", "name"), QConstraint.CT_EQUALS, "regions"));
			query.join("fk_list_primitive_values", new QRelationConstraint(new CanonicalColumnName("fk_list_primitive_values", "fk_list_primitive_id"), new CanonicalColumnName("primitive_field", "id")));

			
		
		}
			
		final Set<Integer> ids = new HashSet<Integer>();
		final Map<Integer, Set<Integer>> idsToRegions = new HashMap<Integer, Set<Integer>>();
		try {
			SIS.get().getExecutionContext().doQuery(query, new RowProcessor() {
			
				@Override
				public void process(Row row) {
					Integer asmID = row.get(0).getInteger();
					if (filter.isAllRegions())
						ids.add(asmID);
					else {
						if (!idsToRegions.containsKey(asmID))
							idsToRegions.put(asmID, new HashSet<Integer>());
						idsToRegions.get(asmID).add(row.get("value").getInteger());
					}
				}
			});
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		if (!filter.isAllRegions()) {
			if (filter.getRelationshipName().equalsIgnoreCase(Relationship.OR)) {
				for (Entry<Integer, Set<Integer>> entry : idsToRegions.entrySet()) {
					if (entry.getValue().containsAll(filter.getRegionIds()))
						ids.add(entry.getKey());
				}
				
			} else if (filter.getRelationshipName().equalsIgnoreCase(Relationship.AND)) {
				for (Entry<Integer, Set<Integer>> entry : idsToRegions.entrySet()) {
					for (Integer id : filter.getRegionIds()) {
						if (entry.getValue().contains(id)) {
							ids.add(entry.getKey());
							break;
						}
					}
					
				}
			}
		}
		
		return ids;
		
	}
		

}
