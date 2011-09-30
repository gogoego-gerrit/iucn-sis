package org.iucn.sis.server.extensions.reports;

import java.text.SimpleDateFormat;
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

/**
 * 
 * @author rasanka.jayawardana
 * 
 */

public class AssessmentReportTemplate{
	
	protected boolean showEmptyFields;
	protected boolean limitedSet;	
	
	public AssessmentReportTemplate(boolean showEmptyFields, boolean limitedSet) {
		this.showEmptyFields = showEmptyFields;
		this.limitedSet = limitedSet;
	}	
	
	public String buildReportHeading(){
		String heading = "<table id=\"box-table-b\" width=\"60%\" border=\"0\">" +
						  "<tr>" +
						  "  <td width=\"147\" rowspan=\"2\" align=\"center\"></td>" + //<img src=\"iucnLogo.gif\" width=\"50\" height=\"48\" />
						  "  <td width=\"643\" align=\"center\">IUCN Species Information Service (SIS) Toolkit</td>" +
						  "</tr>" +
						  "<tr>" +
						  "  <td align=\"center\">Assessment Report</td>" +
						  "</tr>" +
						  "<tr>" +
						  "  <td colspan=\"2\"><hr></td>" +
						  "</tr>" +						  
						  "</table>";
		return heading;
	}	
	
	public String buildTaxonomy(Taxon taxa,Assessment assessment){
		String taxonomy = "<table id=\"box-table-a\" width=\"70%\" border=\"1\">" +
						  " <tr><th colspan=\"5\"><b>Taxonomy</b></th></tr>" +		
						  "  <tr>" +
						  "	  <td width=\"20%\"><b>Kingdom</b></td>" +
						  "	  <td width=\"20%\"><b>Phylum</b></td>" +
						  "	  <td width=\"20%\"><b>Class</b></td>" +
						  "	  <td width=\"20%\"><b>Order</b></td>" +
						  "	  <td width=\"20%\"><b>Family</b></td>" +
						  " </tr>" +
						  "	<tr>" +
						  "   <td>"+fetchTaxaFootPrints(taxa.getFootprint(),0)+"</td>" +
						  "	  <td>"+fetchTaxaFootPrints(taxa.getFootprint(),1)+"</td>" +
						  "   <td>"+fetchTaxaFootPrints(taxa.getFootprint(),2)+"</td>" +
						  "   <td>"+fetchTaxaFootPrints(taxa.getFootprint(),3)+"</td>" +
						  "   <td>"+fetchTaxaFootPrints(taxa.getFootprint(),4)+"</td>" +
						  " </tr>" +
						  "</table>" +
						  "<table id=\"box-table-a\" width=\"70%\" border=\"1\">" +
						  "  <tr>" +
						  "	  <td width=\"20%\"><b>Scientific Name:</b></td>" +
						  "	  <td width=\"80%\">"+taxa.getFullName()+"</td>" +
						  " </tr>" +
						  "  <tr>" +
						  "	  <td width=\"20%\"><b>Species Authority:</b></td>" +
						  "	  <td width=\"80%\">"+taxa.getTaxonomicAuthority()+"</td>" +
						  " </tr>" +	
						  "  <tr>" +
						  "	  <td colspan=\"2\"><b>Common Name/s:</b><br>"+fetchCommonNames(taxa)+"</td>" +
						  " </tr>" +	
						  "  <tr>" +
						  "	  <td width=\"20%\" valign=\"top\"><b>Synonym/s:</b></td>" +
						  "	  <td width=\"80%\">"+fetchSynonyms(taxa)+"</td>" +
						  " </tr>" +
						  "  <tr>" +
						  "	  <td width=\"20%\" valign=\"top\"><b>Taxonomic Notes:</b></td>" +
						  "	  <td width=\"80%\">"+fetchTextPrimitiveField(assessment.getField(CanonicalNames.TaxonomicNotes), "value")+"</td>" +
						  " </tr>" +						  
						  "</table>";		
		return taxonomy;
	}
	
	public String buildAssessmentInfo(Assessment assessment){
		
		String assessmentInfo =   "<table id=\"box-table-a\" width=\"70%\" border=\"1\">" +	
								  " <tr><th colspan=\"2\"><b>Assessment Information</b></th></tr>" +		
								  "  <tr>" +
								  "	  <td width=\"20%\"><b>Red List Category & Criteria:</b></td>" +
								  "	  <td width=\"80%\">"+fetchCategoryAndCrieteria(assessment.getField(CanonicalNames.RedListCriteria))+"</td>" +
								  " </tr>" +
								  "  <tr>" +
								  "	  <td width=\"20%\"><b>Year Assessed:</b></td>" +
								  "	  <td width=\"80%\">"+fetchDatePrimitiveField(assessment.getField(CanonicalNames.RedListAssessmentDate),"value")+"</td>" +
								  " </tr>" +	
								  "  <tr>" +
								  "	  <td width=\"20%\"><b>Assessor/s:</b></td>" +
								  "	  <td width=\"80%\">"+fetchStringPrimitiveField(assessment.getField(CanonicalNames.RedListAssessmentAuthors),"value")+"</td>" +
								  " </tr>" +			
								  "  <tr>" +
								  "	  <td width=\"20%\"><b>Reviewer/s:</b></td>" +
								  "	  <td width=\"80%\">"+fetchStringPrimitiveField(assessment.getField(CanonicalNames.RedListEvaluators),"text")+"</td>" +
								  " </tr>" +	
								  "  <tr>" +
								  "	  <td colspan=\"2\"><b>Justification:</b><br>"+fetchTextPrimitiveField(assessment.getField(CanonicalNames.RedListRationale),"value")+"</td>" +
								  " </tr>" +
								  "  <tr>" +
								  "	  <td width=\"20%\" valign=\"top\"><b>History:</b></td>" +
								  "	  <td width=\"80%\">"+fetchTextPrimitiveField(assessment.getField(CanonicalNames.RedListHistory),"narrative")+"</td>" +
								  " </tr>" +										  
								  "</table>";	
		
		return assessmentInfo;
	}
	
	public String buildGeographicRange(Assessment assessment){
		
		String geographicRange =  "<table id=\"box-table-a\" width=\"70%\" border=\"1\">" +	
								  " <tr><th colspan=\"2\"><b>Geographic Range</b></th></tr>" +
								  "  <tr>" +
								  "	  <td width=\"20%\" valign=\"top\"><b>Range Description:</b></td>" +
								  "	  <td width=\"80%\">"+fetchTextPrimitiveField(assessment.getField(CanonicalNames.RangeDocumentation),"narrative")+"</td>" +
								  " </tr>" +
								  "  <tr>" +
								  "	  <td width=\"20%\" valign=\"top\"><b>Countries:</b></td>" +
								  "	  <td width=\"80%\"><b>Native:</b><br/> "+fetchSubFieldValues(assessment.getField(CanonicalNames.CountryOccurrence), CanonicalNames.CountryOccurrence, "H")+"</td>" +
								  " </tr>" +												  
								  "</table>";	
		
		return geographicRange;
	}	
	
	public String buildPopulation(Assessment assessment){
		
		String population =   "<table id=\"box-table-a\" width=\"70%\" border=\"1\">" +	
							  " <tr><th colspan=\"2\"><b>Population</b></th></tr>" +
							  "  <tr>" +
							  "	  <td width=\"20%\" valign=\"top\"><b>Population:</b></td>" +
							  "	  <td width=\"80%\">"+fetchTextPrimitiveField(assessment.getField(CanonicalNames.PopulationDocumentation),"narrative")+"</td>" +
							  " </tr>" +
							  "  <tr>" +
							  "	  <td width=\"20%\"><b>Population Trend:</b></td>" +
							  "	  <td width=\"80%\">"+fetchForeignPrimitiveField(assessment.getField(CanonicalNames.PopulationTrend))+"</td>" +
							  " </tr>" +												  
							  "</table>";	
		
		return population;
	}	
	
	public String buildHabitatAndEcology(Assessment assessment){
		
		String habitatAndEcology =    "<table id=\"box-table-a\" width=\"70%\" border=\"1\">" +		
									  " <tr><th colspan=\"2\"><b>Habitat and Ecology</b></th></tr>" +
									  "  <tr>" +
									  "	  <td width=\"20%\" valign=\"top\"><b>Habitat and Ecology:</b></td>" +
									  "	  <td width=\"80%\">"+fetchTextPrimitiveField(assessment.getField(CanonicalNames.HabitatDocumentation),"narrative")+"</td>" +
									  " </tr>" +
									  "  <tr>" +
									  "	  <td width=\"20%\" valign=\"top\"><b>List of Habitats:</b></td>" +
									  "	  <td width=\"80%\">"+fetchSubFieldValues(assessment.getField(CanonicalNames.GeneralHabitats), CanonicalNames.GeneralHabitats, "V")+"</td>" +
									  " </tr>" +									  
									  "</table>";	
		
		return habitatAndEcology;
	}	
	
	public String buildThreats(Assessment assessment){
		
		String threats =    "<table id=\"box-table-a\" width=\"70%\" border=\"1\">" +	
							" <tr><th colspan=\"2\"><b>Threats</b></th></tr>" +
						    "  <tr>" +
							"	  <td width=\"20%\" valign=\"top\"><b>Major Threat(s):</b></td>" +
							"	  <td width=\"80%\">"+fetchTextPrimitiveField(assessment.getField(CanonicalNames.ThreatsDocumentation),"value")+"</td>" +
							" </tr>" +
							"  <tr>" +
							"	  <td width=\"20%\" valign=\"top\"><b>List of Threats:</b></td>" +
							"	  <td width=\"80%\">"+fetchSubFieldValues(assessment.getField(CanonicalNames.Threats), CanonicalNames.Threats, "V")+"</td>" +
							" </tr>" +									  
							"</table>";	
		
		return threats;
	}	
	
	public String buildConservationActions(Assessment assessment){
		
		String conActions =   "<table id=\"box-table-a\" width=\"70%\" border=\"1\">" +	
							  " <tr><th colspan=\"2\"><b>Conservation Actions</b></th></tr>" +
							  "  <tr>" +
							  "	  <td width=\"20%\" valign=\"top\"><b>Conservation Actions:</b></td>" +
							  "	  <td width=\"80%\">"+fetchTextPrimitiveField(assessment.getField(CanonicalNames.ConservationActionsDocumentation),"narrative")+"</td>" +
							  " </tr>" +
							  "  <tr>" +
							  "	  <td width=\"20%\" valign=\"top\"><b>List of Conservation Actions:</b></td>" +
							  "	  <td width=\"80%\">"+fetchSubFieldValues(assessment.getField(CanonicalNames.ConservationActions), CanonicalNames.ConservationActions, "V")+"</td>" +
							  " </tr>" +									  
							  "</table>";	
		
		return conActions;
	}		
	
	 
	public String buildBibliography(Assessment assessment){
		
		String bibiliography =    "<table id=\"box-table-a\" width=\"70%\" border=\"1\">" +	
								  " <tr><th colspan=\"2\"><b>Bibliography</b></th></tr>" +
								  "  <tr>" +
								  "	  <td width=\"20%\">&nbsp;</td>" +
								  "	  <td width=\"80%\">"+fetchReferences(assessment)+"</td>" +
								  " </tr>" +									  
								  "</table>";	
		
		return bibiliography;
	}	
	
	@SuppressWarnings("unchecked")
	private String fetchSubFieldValues(Field field, String canonicalName, String order){
		String returnStr = "";
		try{		
			if (field != null && field.getFields() != null) {
	
				for (Field subfield : field.getFields()) {
					PrimitiveField lookup = subfield.getPrimitiveField(canonicalName+"Lookup"); 

					if(order == "V")
						returnStr += LookupLoader.get(subfield.getName(), lookup.getName(), Integer.valueOf(lookup.getRawValue()).intValue()) + "<br/>";
					else
						returnStr += LookupLoader.get(subfield.getName(), lookup.getName(), Integer.valueOf(lookup.getRawValue()).intValue()) + "; ";

				}
			}else
				returnStr = "-";
		}catch(Exception e){
			e.printStackTrace();
		}
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
		try{
			if (taxon.getCommonNames().isEmpty()) {
				commonNames = "-";
			} else {
				
				Set<CommonName> temp = taxon.getCommonNames();
				for (CommonName cur : temp) {
					if(cur != null){
						if(!cur.getLanguage().isEmpty() && !cur.getName().isEmpty())
							commonNames += "&nbsp;&nbsp;&nbsp;"+cur.getLanguage()+" - "+cur.getName()+"<br/>";
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return commonNames;
	}
	
	private String fetchSynonyms(Taxon taxon) {
		String synonyms = "";
		try{
			if (taxon.getSynonyms().isEmpty()) {
				synonyms = "-";
			} else {
				
				Set<Synonym> temp = taxon.getSynonyms();
				for (Synonym cur : temp) {
					if(cur != null){
						synonyms += cur.getFriendlyName()+"<br/>";
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return synonyms;
	}	
	
	private String fetchReferences(Assessment assessment){
		String reference = "";
		try{
			if (assessment.getReference().isEmpty()) {
				reference = "-";
			} else {
				
				Set<Reference> temp = assessment.getReference();
				for (Reference cur : temp) {
					if(cur != null){
						if(!cur.getCitation().isEmpty())
							reference += cur.getCitation()+"<br/>";
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return reference;
	}
	
	private String fetchTextPrimitiveField(Field field, String type){

		String returnStr = "";
		try{
			if(field != null){		
				ProxyField proxy = new ProxyField(field);
				returnStr = proxy.getTextPrimitiveField(type);			
			}else
				returnStr = "-";
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return returnStr;
	}	
	
	private String fetchStringPrimitiveField(Field field, String type){

		String returnStr = "";
		try{
			if(field != null){		
				ProxyField proxy = new ProxyField(field);
				returnStr = proxy.getStringPrimitiveField(type);			
			}else
				returnStr = "-";
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return returnStr;
	}	
	
	private String fetchDatePrimitiveField(Field field, String type){

		String returnStr = "";
		try{
			if(field != null){		
				ProxyField proxy = new ProxyField(field);
				returnStr = new SimpleDateFormat("yyyy").format(proxy.getDatePrimitiveField(type));				
			}else
				returnStr = "-";
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return returnStr;
	}
	
	private String fetchCategoryAndCrieteria(Field field){
		String catAndCrit = "";
		try{
			if(field != null){
				
				RedListCriteriaField proxy = new RedListCriteriaField(field);
				String category = proxy.isManual() ? proxy.getManualCategory() : proxy.getGeneratedCategory();
				
				catAndCrit = "".equals(category) ? "-" : ResultCategory.fromString(category).getName();
								
			}else
				catAndCrit = "-";
		}catch(Exception e){
			e.printStackTrace();
		}
		return catAndCrit;
	}
	
	private String fetchForeignPrimitiveField(Field field){

		String returnVal = "";
		try{
			if(field != null){		
				ProxyField proxy = new ProxyField(field);
				returnVal = LookupLoader.get(field.getName(), "value", proxy.getForeignKeyPrimitiveField("value"));
			}	
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return returnVal;
	}	
}