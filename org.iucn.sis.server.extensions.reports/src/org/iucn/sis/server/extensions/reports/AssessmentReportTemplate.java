package org.iucn.sis.server.extensions.reports;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.iucn.sis.server.api.utils.LookupLoader;
import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.fields.ProxyField;
import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.solertium.util.portable.PortableAlphanumericComparator;

/**
 * 
 * @author rasanka.jayawardana
 * 
 */

public class AssessmentReportTemplate{
	
	protected boolean showSpecial;
	protected StringBuilder html = new StringBuilder();
	
	public AssessmentReportTemplate(boolean showSpecial) {
		this.showSpecial = showSpecial;
	}
	
	public void readHTMLTemplate() throws IOException{		
		HTMLReader reader = new HTMLReader("AssessmentReport.html");
		Scanner scan = new Scanner(reader.getHTML()); 
		while (scan.hasNextLine())
			html.append(scan.nextLine());
		
	}	
	
	public void setReportValue(String find,String replace){
		if(html.indexOf(find) > -1)
			html.replace(html.indexOf(find), html.indexOf(find)+find.length(), replace);
	}
	
	public void buildTaxonomy(Taxon taxa,Assessment assessment){
		
		setReportValue("#REPORT_TITLE",taxa.getFullName());
		setReportValue("#SCIENTIFIC_NAME",taxa.getFullName());
		setReportValue("#KINGDOM",fetchTaxaFootPrints(taxa.getFootprint(),0));
		setReportValue("#PHYLUM",fetchTaxaFootPrints(taxa.getFootprint(),1));
		setReportValue("#CLASS",fetchTaxaFootPrints(taxa.getFootprint(),2));
		setReportValue("#ORDER",fetchTaxaFootPrints(taxa.getFootprint(),3));
		setReportValue("#FAMILY",fetchTaxaFootPrints(taxa.getFootprint(),4));
		setReportValue("#SPECIES_AUTHORITY",taxa.getTaxonomicAuthority());
		setReportValue("#COMMON_NAMES",fetchCommonNames(taxa));
		setReportValue("#SYNONYMS",fetchSynonyms(taxa));
		setReportValue("#TAXA_NOTES",fetchTextPrimitiveField(assessment.getField(CanonicalNames.TaxonomicNotes), "value"));
		
	}
	
	public void buildAssessmentInfo(Assessment assessment){
		
		setReportValue("#REDLIST_CAT_CRIT",fetchCategoryAndCrieteria(assessment.getField(CanonicalNames.RedListCriteria)));
		setReportValue("#YEAR_ASSESSED",fetchDatePrimitiveField(assessment.getField(CanonicalNames.RedListAssessmentDate),"value"));
		setReportValue("#ASSESSORS",fetchStringPrimitiveField(assessment.getField(CanonicalNames.RedListAssessmentAuthors),"value"));
		setReportValue("#REVIEWERS",fetchStringPrimitiveField(assessment.getField(CanonicalNames.RedListEvaluators),"text"));
		setReportValue("#JUSTIFICATION",fetchTextPrimitiveField(assessment.getField(CanonicalNames.RedListRationale),"value"));
		setReportValue("#REDLIST_HISTORY",fetchTextPrimitiveField(assessment.getField(CanonicalNames.RedListHistory),"narrative"));
		
	}
	
	public void buildGeographicRange(Assessment assessment){
		
		setReportValue("#RANGE_DESC",fetchTextPrimitiveField(assessment.getField(CanonicalNames.RangeDocumentation),"narrative"));
		setReportValue("#COUNTRIES",fetchCountrySubFieldValues(assessment.getField(CanonicalNames.CountryOccurrence), CanonicalNames.CountryOccurrence));	
			
	}	
	
	public void buildPopulation(Assessment assessment){
		
		setReportValue("#POPULATION",fetchTextPrimitiveField(assessment.getField(CanonicalNames.PopulationDocumentation),"narrative"));
		setReportValue("#POPULATION_TREND",fetchForeignPrimitiveField(assessment.getField(CanonicalNames.PopulationTrend)));	

	}	
	
	public void buildHabitatAndEcology(Assessment assessment){
		
		setReportValue("#HABITAT_ECOLOGY",fetchTextPrimitiveField(assessment.getField(CanonicalNames.HabitatDocumentation),"narrative"));
		setReportValue("#LIST_OF_HABITATS",fetchSubFieldValues(assessment.getField(CanonicalNames.GeneralHabitats), CanonicalNames.GeneralHabitats));	
		
	}	
	
	public void buildThreats(Assessment assessment){
		
		setReportValue("#MAJOR_THREATS",fetchTextPrimitiveField(assessment.getField(CanonicalNames.ThreatsDocumentation),"value"));
		setReportValue("#LIST_OF_THREATS",fetchSubFieldValues(assessment.getField(CanonicalNames.Threats), CanonicalNames.Threats));	
				
	}	
	
	public void buildConservationActions(Assessment assessment){
		
		setReportValue("#CONSERVATION_ACTIONS",fetchTextPrimitiveField(assessment.getField(CanonicalNames.ConservationActionsDocumentation),"narrative"));
		setReportValue("#LIST_OF_CON_ACTIONS",fetchSubFieldValues(assessment.getField(CanonicalNames.ConservationActions), CanonicalNames.ConservationActions));	
			
	}		
	
	 
	public void buildBibliography(Assessment assessment){
		
		setReportValue("#BIBLIOGRAPHY",fetchReferences(assessment));

	}	
	
	public void buildCitation(Assessment assessment){
		
		setReportValue("#CITATION",fetchCitation(assessment));

	}		
	
	@SuppressWarnings("unchecked")
	private String fetchSubFieldValues(Field field, String canonicalName){
		String returnStr = "";		
	
		if (field != null && field.getFields() != null) {
			StringBuilder builder = new StringBuilder();
			List<String> list = new ArrayList<String>();
			String value = "";
			String topValue = "";
			int dotCount = 0;
			for (Field subfield : field.getFields()) {
				value = "";
				dotCount = 0;
				PrimitiveField lookup = subfield.getPrimitiveField(canonicalName+"Lookup"); 
				value = LookupLoader.get(subfield.getName(), lookup.getName(), Integer.valueOf(lookup.getRawValue()).intValue(),true);
				dotCount = value.replaceAll("[^.]", "").length();
				if(dotCount > 0){
					topValue = "";
					topValue = LookupLoader.getByRef(value.substring(0,1), lookup.getName());
					if(!list.contains(topValue))
						list.add(topValue);
					int j = 3;
					for(int i =1;i < dotCount; i++){
						topValue = "";
						topValue = LookupLoader.getByRef(value.substring(0, j), lookup.getName());
						if(!list.contains(topValue))
							list.add(topValue);
						j += 2;
					}
				}				
				list.add(value);
			}
			Collections.sort(list,new PortableAlphanumericComparator());
			for (String row : list)
				builder.append(row + "<br/>");
			
			returnStr = builder.toString();
		}else
			returnStr = "-";
		return returnStr;	
	}
	
	@SuppressWarnings("unchecked")
	private String fetchCountrySubFieldValues(Field field, String canonicalName){
		String returnStr = "";
	
		if (field != null && field.getFields() != null) {
			Map<String, List<String>> dataMap = new HashMap<String, List<String>>();
			
			for (Field subfield : field.getFields()) {
				PrimitiveField lookup = subfield.getPrimitiveField(canonicalName+"Lookup"); 
				PrimitiveField originlookup = subfield.getPrimitiveField("origin"); 
				
				String origin = LookupLoader.get(canonicalName, "origin", Integer.valueOf(originlookup.getRawValue()).intValue(),false);
				String country = LookupLoader.get(subfield.getName(), lookup.getName(), Integer.valueOf(lookup.getRawValue()).intValue(),false);
					
				if(dataMap.containsKey(origin)){
					List<String> tempList = new ArrayList<String>();
					tempList.addAll(dataMap.get(origin));
					tempList.add(country); 							
					dataMap.put(origin, tempList);
				}else{
					List<String> tempList = new ArrayList<String>();
					tempList.add(country);
					dataMap.put(origin, tempList);
				}	
			}
				
			String countryStr = "";
			Set<String> keys = dataMap.keySet();
			StringBuilder builder = new StringBuilder();
			for (String key : keys) {
				builder.append("<b>"+key+"</b>: <br>");
				countryStr = "";
				Collections.sort(dataMap.get(key),new PortableAlphanumericComparator());
				for(String country : dataMap.get(key))
					countryStr += country+"; ";
				
				builder.append(countryStr+"<br>");
			}
			returnStr = builder.toString();
		}else
			returnStr = "-";

		return returnStr;		
	}	
		
	private String fetchTaxaFootPrints(String[] footprints, int index) {
		if(footprints[index] != null && footprints[index] != "")
			return footprints[index];
		else
			return "-";
	}	
	
	private String fetchCommonNames(Taxon taxon) {
		String commonNames = "";
		
		if (taxon.getCommonNames().isEmpty()) {
			commonNames = "-";
		} else {
			StringBuilder builder = new StringBuilder();
			Set<CommonName> temp = taxon.getCommonNames();
			for (CommonName cur : temp) {
				if(cur != null && cur.getIso() != null)
					builder.append("&nbsp;&nbsp;&nbsp;"+cur.getLanguage()+" - "+cur.getName()+"<br/>");
				
			}
			commonNames = builder.toString();
		}
		return commonNames;
	}
	
	private String fetchSynonyms(Taxon taxon) {
		String synonyms = "";

		if (taxon.getSynonyms().isEmpty()) {
			synonyms = "-";
		} else {
			StringBuilder builder = new StringBuilder();
			Set<Synonym> temp = taxon.getSynonyms();
			for (Synonym cur : temp) {
				if(cur != null && cur.getFriendlyName() != null)
					builder.append(cur.getFriendlyName()+"<br/>");
			}
			synonyms = builder.toString();
		}

		return synonyms;
	}
	
	private String fetchCitation(Assessment assessment){
		String referenceStr = "";
		StringBuilder builder = new StringBuilder();
		List<String> list = new ArrayList<String>();
		Field field = assessment.getField(CanonicalNames.RedListSource);
		if(field != null && field.getReference() != null){			
			Set<Reference> ref = field.getReference();
			if(!ref.isEmpty()){
				for (Reference cur : ref) {
					if(cur != null){
						if(cur.getCitation() != null && !cur.getCitation().isEmpty())
							list.add(cur.getCitation());
					}
				}
			}
		}
		if(!list.isEmpty()){
			Collections.sort(list, new PortableAlphanumericComparator());
			for (String row : list)
				builder.append(row + "<br/>");
			
			referenceStr = builder.toString();
		}else
			referenceStr = "-";
		return referenceStr;
	}
	
	private String fetchReferences(Assessment assessment){
		String referenceStr = "";

		StringBuilder builder = new StringBuilder();
		List<String> list = new ArrayList<String>();
		Set<Reference> temp = assessment.getReference();
		if(!temp.isEmpty()){
			for (Reference cur : temp) {
				if(cur != null){
					if(!cur.getCitation().isEmpty() && cur.getCitation() != null)
						list.add(cur.getCitation());
				}
			}
		}
		if(!assessment.getField().isEmpty()){
			for (Field field : assessment.getField()) {
				if (field.getReference() != null && !field.getReference().isEmpty()) {
					for (Reference reference : field.getReference()){
						if(reference.getCitation() != null && !list.contains(reference.getCitation()))
							list.add(reference.getCitation());
					}
				}
			}
		}
		if(!list.isEmpty()){
			Collections.sort(list, new PortableAlphanumericComparator());
			for (String row : list)
				builder.append(row + "<br/>");
			
			referenceStr = builder.toString();
		}else
			referenceStr = "-";

		return referenceStr;
	}
		
	private String fetchTextPrimitiveField(Field field, String type){

		String returnStr = "";

		if(field != null){		
			ProxyField proxy = new ProxyField(field);
			returnStr = proxy.getTextPrimitiveField(type);			
		}else
			returnStr = "-";

		return returnStr;
	}	
	
	private String fetchStringPrimitiveField(Field field, String type){

		String returnStr = "";
		
		if(field != null){		
			ProxyField proxy = new ProxyField(field);
			returnStr = proxy.getStringPrimitiveField(type);			
		}else
			returnStr = "None";
		
		return returnStr;
	}	
	
	private String fetchDatePrimitiveField(Field field, String type){

		String returnStr = "";

		if(field != null){		
			ProxyField proxy = new ProxyField(field);
			returnStr = new SimpleDateFormat("yyyy").format(proxy.getDatePrimitiveField(type));				
		}else
			returnStr = "-";
		
		return returnStr;
	}
	
	private String fetchCategoryAndCrieteria(Field field){
		String catAndCrit = "";

		if(field != null){
			
			RedListCriteriaField proxy = new RedListCriteriaField(field);
			String category = proxy.isManual() ? proxy.getManualCategory() : proxy.getGeneratedCategory();
				
			catAndCrit = "".equals(category) ? "-" : ResultCategory.fromString(category).getName();
								
		}else
			catAndCrit = "-";

		return catAndCrit;
	}
	
	private String fetchForeignPrimitiveField(Field field){

		String returnVal = "";
		
		if(field != null){		
			ProxyField proxy = new ProxyField(field);
			returnVal = LookupLoader.get(field.getName(), "value", proxy.getForeignKeyPrimitiveField("value"),false);
		}	

		return returnVal;
	}	
	
	public String getHTMLString(){
		return this.html.toString();
	}
}