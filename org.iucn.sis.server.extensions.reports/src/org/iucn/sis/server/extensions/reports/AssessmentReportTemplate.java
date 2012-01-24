package org.iucn.sis.server.extensions.reports;

import java.io.IOException;

import org.hibernate.Session;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.utils.CanonicalNames;

/**
 * 
 * @author rasanka.jayawardana
 * 
 */

public class AssessmentReportTemplate extends ReportTemplate {
	
	public AssessmentReportTemplate(Session session, Assessment assessment) throws IOException {
		super(session, assessment, "AssessmentReport.html");
	}
	
	@Override
	public void build() {
		buildTaxonomy();
		buildAssessmentInfo();
		buildGeographicRange();
		buildPopulation();
		buildHabitatAndEcology();
		buildThreats();
		buildConservationActions();
		buildBibliography();
		buildCitation();
	}
	
	private void buildAssessmentInfo() {
		setReportValue(CanonicalNames.RedListCriteria,fetchCategoryAndCrieteria(assessment.getField(CanonicalNames.RedListCriteria)));
		setReportValue(CanonicalNames.RedListAssessmentDate,fetchDatePrimitiveField(assessment.getField(CanonicalNames.RedListAssessmentDate),"value"));
		setReportValue(CanonicalNames.RedListAssessors,fetchUsers(assessment.getField(CanonicalNames.RedListAssessors)));
		setReportValue(CanonicalNames.RedListEvaluators,fetchUsers(assessment.getField(CanonicalNames.RedListEvaluators)));
		setReportValue(CanonicalNames.RedListContributors,fetchUsers(assessment.getField(CanonicalNames.RedListContributors)));
		setReportValue(CanonicalNames.RedListFacilitators,fetchUsers(assessment.getField(CanonicalNames.RedListFacilitators)));
		setReportValue(CanonicalNames.RedListRationale,fetchTextPrimitiveField(assessment.getField(CanonicalNames.RedListRationale),"value"));
		setReportValue(CanonicalNames.RedListHistory,fetchTextPrimitiveField(assessment.getField(CanonicalNames.RedListHistory),"narrative"));
	}
	
	private void buildGeographicRange() {
		setNarrativeValue(CanonicalNames.RangeDocumentation);
		setReportValue(CanonicalNames.CountryOccurrence,fetchCountrySubFieldValues(assessment.getField(CanonicalNames.CountryOccurrence), CanonicalNames.CountryOccurrence));	
	}	
	
	private void buildPopulation() {
		setNarrativeValue(CanonicalNames.PopulationDocumentation);
		setReportValue(CanonicalNames.PopulationTrend,fetchForeignPrimitiveField(assessment.getField(CanonicalNames.PopulationTrend)));	
	}	
	
	private void buildHabitatAndEcology() {
		setNarrativeValue(CanonicalNames.HabitatDocumentation);
		setClassificationSchemeValue(CanonicalNames.GeneralHabitats);	
	}	
	
	private void buildThreats() {
		setNarrativeValue(CanonicalNames.ThreatsDocumentation, "value");
		setClassificationSchemeValue(CanonicalNames.Threats);	
	}	
	
	private void buildConservationActions() {
		setNarrativeValue(CanonicalNames.ConservationActionsDocumentation);
		setClassificationSchemeValue(CanonicalNames.ConservationActions);	
	}
	
	private void setNarrativeValue(String field) {
		setNarrativeValue(field, "narrative");
	}
	
	private void setNarrativeValue(String field, String prim) {
		setReportValue(field, fetchTextPrimitiveField(assessment.getField(field), prim));
	}
	
	private void setClassificationSchemeValue(String field) {
		setReportValue(field, fetchSubFieldValues(assessment.getField(field), field));
	}
	
}