package org.iucn.sis.server.extensions.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.naming.NamingException;

import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.data.DisplayDataProcessor;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonFactory;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.structures.DisplayData;
import org.iucn.sis.shared.api.structures.FieldData;
import org.iucn.sis.shared.api.structures.SISCategoryAndCriteria;
import org.iucn.sis.shared.api.structures.Structure;
import org.iucn.sis.shared.api.structures.TreeData;
import org.iucn.sis.shared.api.structures.TreeDataRow;
import org.iucn.sis.shared.api.structures.UseTrade;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.iucn.sis.shared.api.utils.FieldParser;
import org.iucn.sis.shared.api.views.Organization;
import org.iucn.sis.shared.api.views.Page;
import org.iucn.sis.shared.api.views.View;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.AlphanumericComparator;
import com.solertium.util.ArrayUtils;
import com.solertium.vfs.VFS;
import com.sun.rowset.internal.Row;

public class AssessmentHtmlTemplate {

	public final static String CSS_VFS_LOCATION = "/reports/speciesReportStyle.css";
	public final static String CSS_LOCATION = "/raw/reports/speciesReportStyle.css";
	public final static String LOCAL_CSS_LOCATION = "speciesReportStyle.css";
	protected Taxon taxon;
	protected StringBuilder theHtml;
	protected boolean showEmptyFields;
	protected Organization curOrg;
	
	protected boolean limitedSet;
	protected List<String> exclude;

	public AssessmentHtmlTemplate(boolean showEmptyFields, boolean limitedSet) {
		theHtml = new StringBuilder();
		theHtml.append("<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n<link rel=\"stylesheet\" type=\"text/css\" href=\"" + CSS_LOCATION + "\">");
		this.showEmptyFields = showEmptyFields;
		this.limitedSet = limitedSet;
		System.out.println("Showing empty fields? " + showEmptyFields + " and limited set? " + limitedSet);
		curOrg = null;
		exclude = Arrays.asList(CanonicalNames.limitedSetExcludes);
	}

	private StringBuilder appendInlineTable(String heading, String data) {
		StringBuilder retHtml = new StringBuilder();
		retHtml.append("<table style=\"display: inline-table;\"><tr><th>");
		retHtml.append(heading + "</th></tr>\n");
		retHtml.append("<tr><td>" + data + "</td></tr></table>\n");
		return retHtml;
	}

	private void appendSectionHeader(String title) {
		theHtml.append("<h1>");
		theHtml.append(title);
		theHtml.append("</h1>\n");
	}

	private void buildAssessmentInfo(Assessment assessment, VFS vfs) {
		Object[] entries = assessment.getDataMap().entrySet().toArray();
		Arrays.sort(entries, new Comparator() {

			private AlphanumericComparator comparator = new AlphanumericComparator();

			public int compare(Object lhs, Object rhs) {
				Entry le = (Entry) lhs;
				Entry re = (Entry) rhs;

				return comparator.compare((String) le.getKey(), (String) re.getKey());
			}
		});

		for (int i = 0; i < entries.length; i++) {
			Entry curEntry = (Entry) entries[i];
			String canonicalName = curEntry.getKey().toString();

			if( !limitedSet || !exclude.contains(canonicalName) ) {
				DisplayData currentDisplayData = fetchDisplayData(canonicalName, vfs);
				try {
					Structure defaultStruct = null;

					if (currentDisplayData instanceof FieldData)
						defaultStruct = DisplayDataProcessor.processDisplayStructure(currentDisplayData);

					if (curEntry.getValue() instanceof ArrayList) {
						parseField((ArrayList<String>) curEntry.getValue(), canonicalName, 8, defaultStruct, false, false,
								false);
					} else {
						parseClassificationScheme((HashMap<String, ArrayList<String>>) curEntry.getValue(), canonicalName,
								vfs, currentDisplayData);
					}

				} catch (Exception e) {
					System.out.println("DIED TRYING TO BUILD " + canonicalName);
					e.printStackTrace();
				}
			}
		}
	}

	// Conservation
	private void buildBibliography(Assessment assessment, VFS vfs) {
		appendSectionHeader("Bibliography");
		StringBuffer orgHtml = new StringBuffer("<div id=\"bibliography\">");
		
		if( assessment.getReferences().size() == 0 ) {
			if( showEmptyFields )
				orgHtml.append("<p>No references used in this assessment.</p>");
		} else {
			ArrayList<Reference> seen = new ArrayList<Reference>();
			ArrayList<Reference> allRefs = assessment.getReferencesAsList();
			final AlphanumericComparator comparer = new AlphanumericComparator();
			
			ArrayUtils.quicksort(allRefs, new Comparator<Reference>() {
				public int compare(Reference o1, Reference o2) {
					o1.generateCitationIfNotAlreadyGenerate();
					o2.generateCitationIfNotAlreadyGenerate();
					return comparer.compare(o1.getCitation(), o2.getCitation());
				}
			});
			
			for( Reference curRef : allRefs ) {
				if( !seen.contains(curRef) ) {
					orgHtml.append("<p>" + curRef.getCitation() + "</p>");
					seen.add(curRef);
				}
			}
		}
		
		theHtml.append(orgHtml.toString());
	}
	
	private void buildCommonNames(Taxon taxon) {
		theHtml.append("<p><strong>Common Names: </strong>");
		if (taxon.getCommonNames().isEmpty()) {
			theHtml.append("No Common Names");
		} else {
			HashMap<String, String> dupes = new HashMap<String, String>();

			for (int i = 0; i < taxon.getCommonNames().size(); i++) {
				CommonNameData curCN = (CommonNameData) taxon.getCommonNames().get(i);

				if (!dupes.containsKey(curCN.getName()))
					dupes.put(curCN.getName(), curCN.getLanguage());
				else if (dupes.get(curCN.getName()) == null || dupes.get(curCN.getName()).equals(""))
					if (curCN.getLanguage() != null || curCN.getLanguage().length() > 0)
						dupes.put(curCN.getName(), curCN.getLanguage());
			}

			String commonNames = "";
			for (Entry<String, String> curEntry : dupes.entrySet()) {
				commonNames += curEntry.getKey() + " (" + curEntry.getValue() + "), ";
			}
			// trim off the comma from the last entry
			commonNames = commonNames.substring(0, commonNames.length() - 2);
			theHtml.append(commonNames);
		}
		theHtml.append("<br/>\n");
	}

	// Conservation
	private void buildConservation(Page conservationPage, Assessment assessment, VFS vfs) {
		StringBuffer orgHtml = buildPageOrganizations(conservationPage.getOrganizations(), assessment, vfs);
		if (orgHtml.length() > 0) {
			theHtml.append("<div id=\"conservation\">\n");
			appendSectionHeader(conservationPage.getTitle());
			theHtml.append(orgHtml);
			theHtml.append("</div>\n");
		}
	}

	// Habitats
	private void buildHabitats(Page habitatsPage, Assessment assessment, VFS vfs) {
		StringBuffer orgHtml = buildPageOrganizations(habitatsPage.getOrganizations(), assessment, vfs);
		if (orgHtml.length() > 0) {
			theHtml.append("<div id=\"habitats\">\n");
			appendSectionHeader(habitatsPage.getTitle());
			theHtml.append(orgHtml);
			theHtml.append("</div>\n");
		}
	}

	/**** Header ****/
	private void buildHeadingTable(Assessment assessment) {
		theHtml.append("<title>");
		theHtml.append(taxon.getFullName());
		theHtml.append("</title></head><body>");
		theHtml.append("<div id=\"header\">\n");
		String type = assessment.getType();
		if (type.equals(BaseAssessment.USER_ASSESSMENT_STATUS)) {
			theHtml.append("<div id=\"draftType\">USER</div>\n");
		} else if (type.equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS)) {
			theHtml.append("<div id=\"draftType\">DRAFT</div>\n");
		}
		theHtml.append("<img src=\"/raw/reports/redListLogo.jpg\" alt=\"IUCN Red List\">\n");
		theHtml.append("<div id=\"speciesInfo\">");
		theHtml.append("<h1><em>");
		theHtml.append(taxon.getFullName() + " - " + taxon.getTaxonomicAuthority());
		theHtml.append("</em></h1>");
		buildHierarchyList(taxon);
		theHtml.append("</div>\n</div>\n");
		buildTaxonomyInformation(assessment);
	}

	private void buildHierarchyList(Taxon taxon) {

		for (int i = 0; i < taxon.getFootprint().length; i++) {
			// Taxon.displayableLevel[i]
			theHtml.append(taxon.getFootprint()[i]);
			theHtml.append(" - ");
		}
		theHtml.append(taxon.getName());
	}

	private HashMap<String, String> buildMap(TreeData treeData) {
		HashMap<String, String> codeToDesc = new HashMap<String, String>();
		// buildMaps(codeToDesc, curRow, parentDesc);
		for (TreeDataRow cur : treeData.getTreeRoots())
			buildMaps(codeToDesc, cur, "", 0);
		
		return codeToDesc;
	}

	private void buildMaps(HashMap<String, String> codeToDesc, TreeDataRow curRow, String parentDesc, int depth) {
		String code = curRow.getDisplayId();
		String description = curRow.getDescription();
		String id = curRow.getRowNumber();
		boolean skipMe = id.equals("0");
		
		if( !skipMe )
			codeToDesc.put(code, depth == 0 ? description : parentDesc + description);
		
		for (TreeDataRow cur : curRow.getChildren() )
			buildMaps(codeToDesc, cur, depth == 0 && !skipMe ? description + " -> " : parentDesc, skipMe ? depth : depth+1);
	}

	// Occurrence
	private void buildOccurrence(Page occurrencePage, Assessment assessment, VFS vfs) {
		StringBuffer orgHtml = buildPageOrganizations(occurrencePage.getOrganizations(), assessment, vfs);
		if (orgHtml.length() > 0) {
			theHtml.append("<div id=\"occurrence\">\n");
			appendSectionHeader(occurrencePage.getTitle());
			theHtml.append(orgHtml);
			theHtml.append("</div>\n");
		}
	}

	// Habitats
	private void buildPage(Page page, String pageName, Assessment assessment, VFS vfs) {
		StringBuffer orgHtml = buildPageOrganizations(page.getOrganizations(), assessment, vfs);
		if (orgHtml.length() > 0) {
			theHtml.append("<div id=\"" + pageName + "\">\n");
			appendSectionHeader(page.getTitle());
			theHtml.append(orgHtml);
			theHtml.append("</div>\n");
		}
	}

	private StringBuffer buildPageOrganizations(ArrayList<Organization> organizations, Assessment assessment,
			VFS vfs) {
		StringBuffer retHtml = new StringBuffer();
		for (int i = 0; i < organizations.size(); i++) {
			curOrg = organizations.get(i);

			// Sections to ignore
			if (curOrg.getTitle().equals("Publication Information")) {
				continue;
			}

			StringBuilder orgHtml = new StringBuilder();

			for (int j = 0; j < curOrg.getComposites().size(); j++) {
				for (int k = 0; k < curOrg.getComposites().get(j).getFields().size(); k++) {
					String fieldId = curOrg.getComposites().get(j).getFields().get(k);
					orgHtml.append(parsePageField(fieldId, assessment.getDataMap().get(fieldId), vfs));
				}
			}
			if (orgHtml.length() > 0) {
				if (!(curOrg.getTitle().contains("Documentation"))) {
					retHtml.append("<h2>" + curOrg.getTitle().replaceAll("[A-Z][a-z]", " $0") + "</h2>\n");
				}
				retHtml.append(orgHtml);
			}
		}
		return retHtml;
	}

	// Population
	private void buildPopulation(Page populationPage, Assessment assessment, VFS vfs) {
		StringBuffer orgHtml = buildPageOrganizations(populationPage.getOrganizations(), assessment, vfs);
		if (orgHtml.length() > 0) {
			theHtml.append("<div id=\"population\">\n");
			appendSectionHeader(populationPage.getTitle());
			theHtml.append(orgHtml);
			theHtml.append("</div>\n");
		}
	}

	// Red List - assuming this should always have content
	private void buildRedListAssessment(Page redListPage, Assessment assessment, VFS vfs) {
		theHtml.append("<div id=\"redList\">\n");
		appendSectionHeader(redListPage.getTitle());
		theHtml.append(buildPageOrganizations(redListPage.getOrganizations(), assessment, vfs));
		theHtml.append("</div>\n");
	}

	// Services
	private void buildServices(Page servicesPage, Assessment assessment, VFS vfs) {
		StringBuffer orgHtml = buildPageOrganizations(servicesPage.getOrganizations(), assessment, vfs);
		if (orgHtml.length() > 0) {
			theHtml.append("<div id=\"services\">\n");
			appendSectionHeader(servicesPage.getTitle());
			theHtml.append(orgHtml);
			theHtml.append("</div>\n");
		}
	}

	private void buildSynonyms(Taxon taxon) {
		theHtml.append("<strong>Synonyms: </strong>");
		if (taxon.getSynonyms().isEmpty()) {
			theHtml.append("No Synonyms");
		} else {
			HashMap<String, String> dupes = new HashMap<String, String>();

			for (int i = 0; i < taxon.getSynonyms().size(); i++) {
				SynonymData curSyn = (SynonymData) taxon.getSynonyms().get(i);

				if (!dupes.containsKey(curSyn.getName()))
					dupes.put(curSyn.getName(), curSyn.getAuthorityString());
				else if (dupes.get(curSyn.getName()) == null || dupes.get(curSyn.getName()).equals(""))
					if (curSyn.getAuthorityString() != null)
						dupes.put(curSyn.getName(), curSyn.getAuthorityString());
			}

			for (Entry<String, String> curEntry : dupes.entrySet()) {
				theHtml.append(curEntry.getKey());
				theHtml.append(" ");
				theHtml.append(curEntry.getValue());
				theHtml.append("; ");
			}
		}
		theHtml.append("</p>\n");
	}

	private void buildTaxonomyInformation(Assessment assessment) {
		theHtml.append("<div id=\"taxonomyInfo\">\n");
		buildCommonNames(taxon);
		buildSynonyms(taxon);
		theHtml.append("<p>");
		for (int i = 0; i < taxon.getNotes().size(); i++) {
			theHtml.append(taxon.getNotes().get(i));
			theHtml.append(" ");
		}
		theHtml.append("</p>");
		if (assessment.getDataMap().get("TaxonomicNotes") instanceof ArrayList
				&& ((ArrayList) assessment.getDataMap().get("TaxonomicNotes")).size() > 0) {
			theHtml.append("<p>" + createDataLabel("Taxonomic Note") + "<br/>");
			theHtml.append(((ArrayList) assessment.getDataMap().get("TaxonomicNotes")).get(0) + "</p>");
		}
		theHtml.append("</div>\n");
	}

	// Threats
	private void buildThreats(Page threatsPage, Assessment assessment, VFS vfs) {
		StringBuffer orgHtml = buildPageOrganizations(threatsPage.getOrganizations(), assessment, vfs);
		if (orgHtml.length() > 0) {
			theHtml.append("<div id=\"threats\">\n");
			appendSectionHeader(threatsPage.getTitle());
			theHtml.append(orgHtml);
			theHtml.append("</div>\n");
		}
	}

	// Use and Trade
	private void buildUseTrade(Page useTradePage, Assessment assessment, VFS vfs) {
		StringBuffer orgHtml = buildPageOrganizations(useTradePage.getOrganizations(), assessment, vfs);
		if (orgHtml.length() > 0) {
			theHtml.append("<div id=\"useTrade\">\n");
			appendSectionHeader(useTradePage.getTitle());
			theHtml.append(orgHtml);
			theHtml.append("</div>\n");
		}
	}

	private String createDataLabel(String label) {
		String ret = "<span class=\"dataLabel\">" + label;
//		System.out.println("add label formatting to '" + label + "'");
		if (!(label.endsWith(":")) && !(label.endsWith("?")) && !(label.endsWith("."))) {
			ret += ":";
		}
		ret += " </span>\n";
		return ret;
	}

	protected DisplayData fetchDisplayData(String canonicalName, VFS vfs) {
		try {
			NativeDocument jnd = NativeDocumentFactory.newNativeDocument();
			jnd.fromXML(DocumentUtils.serializeNodeToString(DocumentUtils.getVFSFileAsDocument("/browse/docs/fields/"
					+ canonicalName + ".xml", vfs)));

			FieldParser parser = new FieldParser();
			DisplayData currentDisplayData = parser.parseFieldData(jnd);
			return currentDisplayData;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("DIED FETCHING DISPLAY DATA FOR " + canonicalName);
			return null;
		}
	}

	public byte[] getHtmlBytes() {
		return theHtml.toString().getBytes();
	}

	public String getHtmlString() {
		return theHtml.toString();
	}

	public void parse(Assessment assessment) {
		this.parse(assessment, null, null);
	}

	public void parse(Assessment assessment, Taxon node, View curView) {

		VFS vfs = null;
		vfs = ServerApplication.getStaticVFS();

		if (node == null) {
			String specieID = assessment.getSpeciesID();

			String sInfo = DocumentUtils.getVFSFileAsString("/browse/nodes/"
					+ FilenameStriper.getIDAsStripedPath(specieID) + ".xml", vfs);
			NativeDocument taxaDoc = NativeDocumentFactory.newNativeDocument();
			taxaDoc.parse(sInfo);

			taxon = TaxonFactory.createNode(taxaDoc);
		} else
			taxon = node;

		// Add IUCN logo and species Name/Authority and Taxonomy Info
		buildHeadingTable(assessment);

		// if we're not provided with a view just display everything in
		// alphabetical order
		if (curView == null) {
			// Add assessment info
			buildAssessmentInfo(assessment, vfs);
			buildBibliography(assessment, vfs);
		} else {
			// Red List
			buildRedListAssessment(curView.getPages().get("RedListing"), assessment, vfs);

			// Distribution
			buildPage(curView.getPages().get("Distribution"), "Distribution", assessment, vfs);

			// Occurrence
			buildOccurrence(curView.getPages().get("Occurrence"), assessment, vfs);

			// Population
			buildPopulation(curView.getPages().get("Population"), assessment, vfs);

			// Habitats
			buildHabitats(curView.getPages().get("Habitats"), assessment, vfs);

			// Use and Trade
			buildUseTrade(curView.getPages().get("UseTrade"), assessment, vfs);

			// Threats
			buildThreats(curView.getPages().get("Threats"), assessment, vfs);

			// Conservation
			buildConservation(curView.getPages().get("Conservation"), assessment, vfs);

			// Services
			buildServices(curView.getPages().get("EcosystemServices"), assessment, vfs);
			
			// Bibliography
			buildBibliography(assessment, vfs);
		}

		theHtml.append("</body>\r\n</html>");
	}

	private StringBuilder parseClassificationScheme(HashMap<String, ArrayList<String>> selected, String canonicalName,
			VFS vfs, DisplayData currentDisplayData) {
		StringBuilder retHtml = new StringBuilder();
		if (selected == null || selected.keySet() == null || selected.keySet().size() == 0) {
			if (showEmptyFields) {
				retHtml.append("<p>" + createDataLabel(canonicalName));
				retHtml.append("(Not specified)</p>\n");
			}
			return retHtml;
		}
		
		TreeData currentTreeData = (TreeData) currentDisplayData;
		final HashMap<String, String> structMap = buildMap(currentTreeData);
		Structure defaultStruct = DisplayDataProcessor.processDisplayStructure(currentTreeData.getDefaultStructure());
		ArrayList<Entry<String, ArrayList<String>>> entries = new ArrayList<Entry<String, ArrayList<String>>>(selected.entrySet());
		ArrayUtils.quicksort(entries, new Comparator<Entry<String, ArrayList<String>>>() {
			private PortableAlphanumericComparator comparator = new PortableAlphanumericComparator();

			public int compare(Entry<String, ArrayList<String>> lhs, Entry<String, ArrayList<String>> rhs) {
				try {
					return comparator.compare(structMap.get(lhs.getKey()), structMap.get(rhs.getKey()));
				} catch (NullPointerException e) {
					return -1;
				}
			}
		});
		retHtml.append("<table>");
		boolean showHeader = true;
		boolean occurrenceHeadingReorder = false;
		for (Entry<String, ArrayList<String>> curEntry : entries) {
			if (canonicalName.equals("CountryOccurrence") || canonicalName.equals("LargeMarineEcosystems")
					|| canonicalName.equals("FAOOccurrence")) {
				occurrenceHeadingReorder = true;
			}
			retHtml.append(parseField(curEntry.getValue(), structMap.get(curEntry.getKey()), 16, defaultStruct,
					true, showHeader, occurrenceHeadingReorder));
			showHeader = false;
			occurrenceHeadingReorder = false;

		}
		retHtml.append("</table>");
		return retHtml;
	}
	
	private String generateTextFromUsers(List<User> userList) {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < userList.size(); i++) {
			text.append(userList.get(i).getCitationName());
			
			if (i + 1 < userList.size() - 1)
				text.append(", ");

			else if (i + 1 == userList.size() - 1)
				text.append(" & ");
		}
		
		return text.toString();
	}
	private HashMap<String, User> getUserInfo(List<String> userIDs) throws NamingException {
		HashMap<String, User> users = new HashMap<String, User>();
		
		SystemExecutionContext ec2 = new SystemExecutionContext("users");
		ec2.setExecutionLevel(ExecutionContext.ADMIN);
		ec2.setAPILevel(ExecutionContext.SQL_ALLOWED);
		
		final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
		query.select("user", "*");
		query.select("profile", "firstname");
		query.select("profile", "lastname");
		query.select("profile", "initials");
		query.select("profile", "affiliation");
		
		QConstraintGroup constraints = new QConstraintGroup();
		for( String id : userIDs )
			constraints.addConstraint(QConstraint.CG_OR, new QComparisonConstraint(new CanonicalColumnName("user", "id"), QConstraint.CT_EQUALS, id));
	
		query.constrain(constraints);
		
		final Row.Set rs = new Row.Set();
		try {
			ec2.doQuery(query, rs);
			
			for( Row curRow : rs.getSet() ) {
				String f = curRow.get("firstname").getString();
				String l = curRow.get("lastname").getString();
				String i = curRow.get("initials").getString();
				String id = curRow.get("id").getString();
				
				User user = new User();
				user.setFirstName(f);
				user.setLastName(l);
				user.setInitials(i);
				
				users.put(id, user);
			}
			
			return users;
		} catch (DBException e) {
			e.printStackTrace();
			return null;
		}
	}

	private StringBuilder parseField(ArrayList<String> data, String canonicalName, int dataIndent,
			Structure defaultStructure, boolean forceShowCanonicalName, boolean showTableHeader,
			boolean occurrenceHeadingReorder) {
		StringBuilder retHtml = new StringBuilder();
		if (data.size() == 0)
			return retHtml;

		ArrayList<String> prettyData = new ArrayList<String>();
		defaultStructure.getDisplayableData(data, prettyData, 0);
		
		if( "RedListEvaluators".equals(canonicalName) || "RedListContributors".equals(canonicalName) || 
				"RedListAssessors".equals(canonicalName)) {
			if( prettyData.get(0).equals("") && data.size() > 1 ) {
				//Sigh. Need to build the user text.
				List<String> userIDs = new ArrayList<String>();
				for (int i = 2; i < data.size(); i++) { 
					//START AT 2 - index 1 is now just the total number of users...
					String curID = data.get(i);
					if( !curID.equals("0") )
						userIDs.add(curID);
				}
				
				if( userIDs.size() > 0 ) {
					try {
						HashMap<String, User> users = getUserInfo(userIDs);
						prettyData.set(0, generateTextFromUsers(new ArrayList<User>(users.values())));
					} catch (NamingException e) {
						e.printStackTrace();
					}
				}
			}
		}
		// don't show empty fields if the preference is set
		if (!showEmptyFields) {
			boolean allEmptyFields = true;
			for (int i = 0; i < prettyData.size(); i++) {
//				System.out.print(prettyData.get(i) + ", ");
				if (!(prettyData.get(i).equals("")) && !(prettyData.get(i).equals("(Not Specified)"))) {
					allEmptyFields = false;
					break;
				}
			}
//			System.out.println(" - is empty? " + allEmptyFields);
			if (allEmptyFields) {
				return retHtml;
			}
		}

		ArrayList headers = defaultStructure.extractDescriptions();
		if (headers == null) {
			headers = new ArrayList();
		}

		boolean useTable = (headers.size() > 1);

		// Special cases for parsing
		if (canonicalName.equals("UseTradeDetails")) {
			retHtml = parseUseTrade(prettyData, headers);
			return retHtml;
		} else if (canonicalName.equals("RedListReasonsForChange")) {
			retHtml = parseReasonsForChange(prettyData, headers);
			return retHtml;
		} else if (canonicalName.equals("RedListCriteria")) {
			// Red List status sets it's own header, don't need to do it
			// automatically
			parseRedListStatus(prettyData, headers);
			return retHtml;
		} else if (canonicalName.contains("PopulationReduction")) {
			// Print the main percentage in normal table, print basis and causes
			// in
			// specially formatted table
			if (!(canonicalName.endsWith("Past") || canonicalName.endsWith("Future") || canonicalName
					.endsWith("Ongoing"))) {
				retHtml = parsePopulationReduction(canonicalName, prettyData);
				return retHtml;
			}
		} else if (curOrg.getTitle().equals("Life History") && !canonicalName.equals("GenerationLength")) {
			String header = headers.get(0).toString();
			String tableData = prettyData.get(0).toString();
			if (tableData.length() == 0) {
				tableData = "-";
			}
			if (headers.size() == 2 && headers.get(1).equals("Units:")) {
				tableData += " " + prettyData.get(1);
			}
			if (canonicalName.equals("EggLaying")) {
				retHtml.append("<h2>Breeding Strategy</h2>\n");
			}
			retHtml.append(appendInlineTable(header, tableData));
			if (canonicalName.equals("MaleMaturitySize") || canonicalName.equals("GestationTime")
					|| canonicalName.equals("NaturalMortality") || canonicalName.equals("Parthenogenesis")) {
				retHtml.append("<br/>");
			}
			return retHtml;
		}

		// if we don't have all the data for a field, just print headers for the
		// data we have
		int numHeadersToUse = headers.size();
//		System.out.println(canonicalName + " " + headers.size() + " " + prettyData.size());
		if (prettyData.size() < numHeadersToUse) {
			numHeadersToUse = prettyData.size();
		}

		if (useTable && !forceShowCanonicalName) {
			retHtml.append("<table>");
		}
		if (forceShowCanonicalName) {
			if (showTableHeader) {
				retHtml.append("<tr><th>");
				if (curOrg.getTitle().equals("Countries of Occurrence")) {
					retHtml.append("Country");
				} else if (curOrg.getTitle().equals("Threats Classification Scheme")) {
					retHtml.append("Threat");
				} else if (curOrg.getTitle().equals("IUCN Habitats Classification Scheme")) {
					retHtml.append("Habitat");
					retHtml.append("</th><th>Suitability</th><th>Major Importance?");
					numHeadersToUse = 0;
				}
				retHtml.append("</th>");
				for (int i = 0; i < numHeadersToUse; i++) {
					if (occurrenceHeadingReorder && (i == 1)) {
						retHtml.append("<th>" + headers.get(i + 1) + "</th>");
						retHtml.append("<th>" + headers.get(i) + "</th>");
						i++;
					} else if (curOrg.getTitle().equals("Threats Classification Scheme")
							&& (headers.get(i).toString().equals("Total Selected"))) {
						retHtml.append(""); // ignore the stresses column, will
						// be its own row
					} else {
						retHtml.append("<th>" + headers.get(i) + "</th>");
					}
				}
				retHtml.append("</tr>\n");
			}
		} else if (useTable) {
			retHtml.append("<tr>");
			for (int i = 0; i < numHeadersToUse; i++)
				retHtml.append("<th>" + headers.get(i) + "</th>");
			retHtml.append("</tr>\n");
		} else {
			for (int i = 0; i < numHeadersToUse; i++) {
				if (!canonicalName.endsWith("Documentation") && !canonicalName.equalsIgnoreCase("RedListRationale")) {
					if (!(canonicalName.equals("RedListCaveat") || canonicalName.equals("RedListPetition"))) {
						retHtml.append("<p>");
					}
					retHtml.append(createDataLabel(headers.get(i).toString()));
				}
			}
		}
		if (forceShowCanonicalName) {
			retHtml.append("<tr><td ");
			if (!curOrg.getTitle().equals("Threats Classification Scheme")) {
				retHtml.append("class=\"dataBorder\"");
			}
			retHtml.append(">" + canonicalName + "</td>");
		} else if (useTable) {
			retHtml.append("<tr>");
		}

		boolean actuallyData = false;
		for (int i = 0; i < prettyData.size(); i++) {
			String tempText = prettyData.get(i).toString();
			if (tempText.equals("")) {
				tempText = "(Not specified)";
			} else if (!tempText.equals("(Not Specified)"))
				actuallyData = true;

			if (canonicalName.endsWith("Documentation") || canonicalName.equalsIgnoreCase("RedListRationale")) {
				if (!actuallyData && showEmptyFields) {
					retHtml.append("<p>" + createDataLabel(canonicalName));
					retHtml.append("<br/>" + tempText + "</p>");
				} else {
					retHtml.append("<p>" + tempText + "</p>");
				}
			} else {
				if (canonicalName.contains("Population") && headers.get(i).toString().contains("Percent")
						&& !tempText.contains("%")) {
					tempText += "%";
				}
				// remove hh:mm:ss from "mm-dd-yyyy hh:mm:ss" formatted date
				else if (canonicalName.contains("Date") && tempText.length() == 19) {
					tempText = tempText.substring(0, 10);
				}
				if (forceShowCanonicalName || useTable) {
					if (tempText.equalsIgnoreCase("(Not specified)")) {
						tempText = "-";
					}
					if (occurrenceHeadingReorder && (i == 1)) {
						retHtml.append("<td class=\"dataBorder\">" + prettyData.get(i + 1) + "</td>");
						retHtml.append("<td class=\"dataBorder\">" + tempText + "</td>");
						i++;
					} else if (curOrg.getTitle().equals("Threats Classification Scheme")) {
						if (i <= 4) {
							retHtml.append("<td>" + tempText + "</td>");
						} else if (i == 5) {
							retHtml
									.append("</tr>\n<tr><td colspan=\"6\" class=\"dataBorder\"><em>Total Stresses</em>: "
											+ tempText + " ");
						} else if (i == (prettyData.size() - 1)) {
							retHtml.append(tempText + "</td>");
						} else {
							retHtml.append(tempText + ", ");
						}
					} else {
						retHtml.append("<td ");
						if (!useTable || forceShowCanonicalName) {
							retHtml.append("class=\"dataBorder\"");
						}
						retHtml.append(">" + tempText + "</td>");
					}
				} else {
					retHtml.append(tempText);
					if (!canonicalName.equals("RedListCaveat")) {
						retHtml.append(" ");
					} else {
						retHtml.append("</p>");
					}
				}
			}
		}
		if (useTable && !forceShowCanonicalName)
			retHtml.append("</tr></table>\n");
		else if (forceShowCanonicalName)
			retHtml.append("</tr>\n");

		return retHtml;
	}

	private StringBuilder parsePageField(String canonicalName, Object data, VFS vfs) {
//		System.out.println("---" + canonicalName + ": ");
		StringBuilder retHtml = new StringBuilder();
		
		if( limitedSet && exclude.contains(canonicalName) ) {
			return retHtml;
		}
		
		if (data == null) {
			if (showEmptyFields) {
				retHtml.append("<p>" + createDataLabel(canonicalName));
				retHtml.append("(Not specified)</p>\n");
			}
			return retHtml;
		}
		// fields that shouldn't be displayed in the report
		else if (canonicalName.equals("RedListConsistencyCheck") || canonicalName.equals("RedListNotes")
				|| canonicalName.equals("OldDEMPastDecline") || canonicalName.equals("OldDEMPeriodPastDecline")
				|| canonicalName.equals("OldDEMFutureDecline") || canonicalName.equals("OldDEMPeriodFutureDecline")
				|| canonicalName.equals("LandCover")) {
			return retHtml;
		}
		DisplayData currentDisplayData = fetchDisplayData(canonicalName, vfs);
		try {
			Structure defaultStruct = null;
			if (currentDisplayData instanceof FieldData)
				defaultStruct = DisplayDataProcessor.processDisplayStructure(currentDisplayData);
			if (data instanceof ArrayList) {
				retHtml = parseField((ArrayList<String>) data, canonicalName, 8, defaultStruct, false, false, false);
			} else {
				retHtml = parseClassificationScheme((HashMap<String, ArrayList<String>>) data, canonicalName, vfs,
						currentDisplayData);
			}

		} catch (Exception e) {
			System.out.println("DIED TRYING TO BUILD " + canonicalName);
			e.printStackTrace();
		}
		return retHtml;
	}

	private StringBuilder parsePopulationReduction(String canonicalName, ArrayList prettyData) {
		String heading = "";
		if (canonicalName.endsWith("Basis")) {
			heading = "Basis?";
		} else if (canonicalName.endsWith("Reversible")) {
			heading = "Reversible?";
		} else if (canonicalName.endsWith("Understood")) {
			heading = "Understood?";
		} else if (canonicalName.endsWith("Ceased")) {
			heading = "Ceased?";
		}
		return appendInlineTable(heading, prettyData.get(0).toString());
	}

	private StringBuilder parseReasonsForChange(ArrayList prettyData, ArrayList headers) {
		StringBuilder retHtml = new StringBuilder();
		if (prettyData.size() != 5) {
			return retHtml;
		}
		retHtml.append(prettyData.get(0) + ": ");
		if (prettyData.get(0).equals("Genuine Change")) {
			retHtml.append(prettyData.get(1));
		} else if (prettyData.get(0).equals("Nongenuine Change")) {
			retHtml.append(prettyData.get(2));
			if (prettyData.get(2) == "Other") {
				retHtml.append(" - " + prettyData.get(3));
			}
		} else if (prettyData.get(0).equals("No change")) {
			retHtml.append(prettyData.get(4));
		}
		return retHtml;
	}

	private void parseRedListStatus(ArrayList prettyData, ArrayList headers) {
		StringBuilder retHtml = new StringBuilder();
		boolean isManual = prettyData.get(SISCategoryAndCriteria.IS_MANUAL_INDEX).equals("True");
		String version = prettyData.get(SISCategoryAndCriteria.CRIT_VERSION_INDEX).toString();
		String manualCategory = prettyData.get(SISCategoryAndCriteria.MANUAL_CATEGORY_INDEX).toString();
		String autoCategory = prettyData.get(SISCategoryAndCriteria.GENERATED_CATEGORY_INDEX).toString();
		retHtml.append("<center><table id=\"redListStatus\"><tr><th colspan=\"2\">Red List Status</th></tr>\n");
		if ((isManual && (manualCategory.equals("None")) || manualCategory.equals("")) || 
				(!isManual && (autoCategory.equals("None") || autoCategory.equals("")))) {
			retHtml.append("<tr><td colspan=\"2\">Red List category not determined</td></tr>\n");
			retHtml.append("</table></center>");
		} else {
			retHtml.append("<tr><td id=\"redListStatusInfo\" colspan=\"2\">");
			if (isManual) {
				retHtml.append(manualCategory);
				retHtml.append(", ");
				retHtml.append(prettyData.get(SISCategoryAndCriteria.MANUAL_CRITERIA_INDEX));
			} else {
				retHtml.append(autoCategory);
				retHtml.append(", ");
				retHtml.append(prettyData.get(SISCategoryAndCriteria.GENERATED_CRITERIA_INDEX));
			}
			if (version.length() > 0 && !version.equals("(Not Specified)")) {
				retHtml.append(" (IUCN version " + prettyData.get(1) + ")");
				retHtml.append("</td></tr>\n");
			}
			// if Manual or Auto Category is CR add the possibly extinct fields
			if ((isManual && manualCategory.startsWith("CR")) || (!isManual && autoCategory.startsWith("CR"))) {
				retHtml.append("<tr><td colspan=\"2\">&nbsp</td></tr><tr><td>");
				retHtml.append(headers.get(SISCategoryAndCriteria.POSSIBLY_EXTINCT_INDEX) + ": ");
				retHtml.append("</td><td>");
				retHtml.append(prettyData.get(SISCategoryAndCriteria.POSSIBLY_EXTINCT_INDEX));
				retHtml.append("</td></tr>\n<tr><td>");
				retHtml.append(headers.get(SISCategoryAndCriteria.POSSIBLY_EXTINCT_CANDIDATE_INDEX) + ": ");
				retHtml.append("</td><td>");
				retHtml.append(prettyData.get(SISCategoryAndCriteria.POSSIBLY_EXTINCT_CANDIDATE_INDEX));
				retHtml.append("</td></tr>\n<tr><td>");
				retHtml.append(headers.get(SISCategoryAndCriteria.DATE_LAST_SEEN_INDEX) + ": ");
				retHtml.append("</td><td>");
				retHtml.append(prettyData.get(SISCategoryAndCriteria.DATE_LAST_SEEN_INDEX));
				retHtml.append("</td></tr>\n");
			} else if( manualCategory.startsWith("DD") ) {
				retHtml.append("<tr><td colspan=\"2\">&nbsp</td></tr><tr><td>");
				retHtml.append(headers.get(SISCategoryAndCriteria.DATA_DEFICIENT_INDEX) + ": ");
				retHtml.append("</td><td>");
				retHtml.append(prettyData.get(SISCategoryAndCriteria.DATA_DEFICIENT_INDEX));
				retHtml.append("</td></tr>\n");
			}
			retHtml.append("</table></center>");
		}
		theHtml.append(retHtml);
	}

	private StringBuilder parseUseTrade(ArrayList prettyData, ArrayList headers) {
		StringBuilder retHtml = new StringBuilder();
		UseTrade utTemp = new UseTrade("", "");
		retHtml.append("<table><tr>");
		int colCount = 0;
		for (Iterator iter = headers.listIterator(); iter.hasNext();) {
			retHtml.append("<th>" + iter.next().toString() + "</th>");
		}
		retHtml.append("</tr>");
		for (int i = 1; i < prettyData.size(); i++) {
			if (colCount == headers.size()) {
				retHtml.append("</tr>\n<tr>");
				colCount = 0;
			}
			String tempText = prettyData.get(i).toString();

			if (tempText.equals(""))
				tempText = "(Not specified)";

			retHtml.append("<td class=\"dataBorder\">" + tempText + "</td>");
			colCount++;
		}
		retHtml.append("</tr></table>");
		return retHtml;
	}
}
