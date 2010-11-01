package org.iucn.sis.shared.helpers;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;

public class XMLUtils {
	public static final String NO_NODE_ATTRIBUTE_FOUND = "no node attribute found here";
	public static final String NO_NODE_VALUE_FOUND = "no node value found here";

	public static final String FACTOR_COS = "Factor";

	public static final String NARRATIVE_STRUCTURE = "narrative";
	public static final String RICH_TEXT_STRUCTURE = "richText";
	public static final String BOOLEAN_STRUCTURE = "boolean";
	public static final String BOOLEAN_RANGE_STRUCTURE = "booleanRange";
	public static final String BOOLEAN_UNKNOWN_STRUCTURE = "booleanUnknown";
	public static final String DATE_STRUCTURE = "date";
	public static final String NOTE_STRUCTURE = "note";
	public static final String EMPTY_STRUCTURE = "empty";
	public static final String LABEL_STRUCTURE = "label";
	public static final String REFERENCE_STRUCTURE = "reference";
	public static final String OPTIONS_LIST = "optionsList";
	public static final String CLASSIFICATION_SCHEME_STRUCTURE = "classificationScheme";
	public static final String REGIONAL_EXPERT_QUESTIONS_STRUCTURE = "regionalQuestions";
	public static final String REGIONAL_INFORMATION = "regionalInformation";

	public static final String RED_LIST_CATEGORIES_CRITERIA = "categoryAndCriteria";

	public static final String LIVELIHOODS = "livelihoods";
	public static final String USE_TRADE = "useTrade";
	public static final String ONE_TO_MANY = "oneToMany";

	public static final String RANGE_STRUCTURE = "range";
	public static final String RANGE_HIGH_GUESS = "upperBound";
	public static final String RANGE_LOW_GUESS = "lowerBound";
	public static final String RANGE_BEST_GUESS = "bestGuess";

	public static final String NUMBER_STRUCTURE = "number";
	public static final String TEXT_STRUCTURE = "text";
	public static final String MULTIPLE_TEXT_STRUCTURE = "multipleText";
	public static final String MULTI_STRUCTURE = "multiStructure";
	public static final String MULTIPLE_SELECT_STRUCTURE = "multipleSelect";
	public static final String SINGLE_SELECT_STRUCTURE = "singleSelect";
	public static final String QUALIFIER_STRUCTURE = "qualifier";
	public static final String JUSTIFICATION_STRUCTURE = "justification";
	public static final String FILE_STRUCTURE = "file";
	public static final String BUTTON_STRUCTURE = "button";
	public static final String AUTO_COMPLETE_STRUCTURE = "autoComplete";
	public static final String RELATED_STRUCTURE = "relatedStructure";

	// COMPLICATED STRUCTURES
	public static final String IMAGE_STRUCTURE = "image";
	public static final String MAP_STRUCTURE = "map";
	public static final String TREE_STRUCTURE = "treeStructure";
	public static final String STRUCTURE_COLLECTION = "collection";
	public static final String DOMINANT_STRUCTURE_COLLECTION = "dominantcollection";
	// public static final String RELATED_STRUCTURES = "relatedStructures";

	public static final String THREAT_STRUCTURE = "threat";
	public static final String THREAT_TIMING = "timing";
	public static final String THREAT_SCOPE = "scope";
	public static final String THREAT_SEVERITY = "severity";
	public static final String THREAT_IMPACT = "impact";

	// BASIC DATA
	public static final String KINGDOM = "kingdom";
	public static final String PHYLUM = "phylum";
	public static final String CLASS = "class";
	public static final String ORDER = "order";
	public static final String FAMILY = "family";
	public static final String GENUS = "genus";
	public static final String SPECIES = "species";

	public static final String ASSESSMENT_ID = "assessmentID";
	public static final String SPECIES_ID = "speciesID";
	public static final String SPECIES_NAME = "speciesName";
	public static final String COMMON_NAME = "commonName";
	public static final String TAXONOMIC_AUTHORITY = "taxonomicAuthority";
	public static final String ASSESSMENT_TYPE = "assessmentType";
	public static final String REGION = "region";
	public static final String RANK = "rank";
	public static final String INFRARANK = "infrarank";
	public static final String SUBPOPULATION = "subpopulation";
	public static final String PLANT_TYPE = "plantType";

	// RULES
	public static final String BOOLEAN_RULE = "booleanRule";
	public static final String SELECT_RULE = "selectRule";
	public static final String CONTENT_RULE = "contentRule";

	// BIBLIOGRAPHY CONSTANTS
	public static final String BIB_CREATED = "Created";
	public static final String BIB_REF_TYPE = "RefType";
	public static final String BIB_AUTHOR = "AuthorPrimary";
	public static final String BIB_TITLE = "TitlePrimary";
	public static final String BIB_PUB_YEAR = "PubYear";
	public static final String BIB_PUB_DATE = "PubDateFreeForm";
	public static final String BIB_PERIODICAL_FULL = "PeriodicalFull";
	public static final String BIB_PERIODICAL_ABBREV = "PeriodicalAbbrev";
	public static final String BIB_VOLUME = "Volume";
	public static final String BIB_ISSUE = "Issue";
	public static final String BIB_START_PAGE = "StartPage";
	public static final String BIB_OTHER_PAGES = "OtherPages";
	public static final String BIB_EDITION = "Edition";
	public static final String BIB_PUBLISHER = "Publisher";
	public static final String BIB_PLACE_OF_PUBLICATION = "PlaceOfPublication";
	public static final String BIB_ISSN_ISBN = "ISSN_ISBN";
	public static final String BIB_LANGUAGE = "Language";
	public static final String BIB_FOREIGN_TITLE = "OriginalForeignTitle";
	public static final String BIB_LINKS = "Links";
	public static final String BIB_ABSTRACT = "Abstract";
	public static final String BIB_NOTES = "Notes";
	public static final String BIB_RETRIEVED_DATE = "RetrievedDate";
	public static final String BIB_URL = "URL";
	public static final String BIB_WEBSITE_TITLE = "WebsiteTitle";
	public static final String BIB_WEBSITE_EDITOR = "WebsiteEditor";
	public static final String BIB_COMMENTS = "Comments";

	public static String clean(Object cleanMe) {
		if (cleanMe instanceof String)
			return clean((String) cleanMe);

		return null;
	}

	public static String clean(String cleanMe) {
		if (cleanMe != null) {
			cleanMe = cleanMe.replaceAll("(?!&amp;)(?!&lt;)(?!&gt;)(?!&quot;)&", "&amp;");
			cleanMe = cleanMe.replaceAll("<", "&lt;");
			cleanMe = cleanMe.replaceAll(">", "&gt;");
			cleanMe = cleanMe.replaceAll("\"", "&quot;");

			return cleanMe;
		}

		return "";
	}

	public static String cleanFromXML(String cleanMe) {
		if (cleanMe != null) {
			cleanMe = cleanMe.replaceAll("&amp;", "&");
			cleanMe = cleanMe.replaceAll("&lt;", "<");
			cleanMe = cleanMe.replaceAll("&gt;", ">");
			cleanMe = cleanMe.replaceAll("&quot;", "\"");

			return cleanMe;
		}

		return "";
	}

	public static String getXMLAttribute(final NativeNode node, final String name) {
		return getXMLAttribute(node, name, NO_NODE_ATTRIBUTE_FOUND);
	}

	/**
	 * Helper function that returns an attribute from a given node. This version
	 * operates on NativeNodes.
	 * 
	 * @param node
	 *            the node to extract the attribute from
	 * @param name
	 *            the name of the attribute
	 * @return a String containing what the attribute's value is
	 */
	public static String getXMLAttribute(final NativeNode node, final String name, final String onFail) {
		if (!(node instanceof NativeElement))
			return onFail; // only works on Elements
		try {
			if (((NativeElement) node).getAttribute(name) != null)
				return ((NativeElement) node).getAttribute(name).trim();
			else
				return onFail;
		} catch (Exception e) {
			return onFail;
		}
	}

	public static String getXMLValue(final NativeNode node) {
		return getXMLValue(node, "");
	}

	/**
	 * Helper function that returns the first child's nodevalue of a given node,
	 * or a given default return string.
	 * 
	 * @param node
	 *            the node to read
	 * @param defaultRet
	 *            string to return on fail
	 * @return a String containing the contents or defaultRet
	 */
	public static String getXMLValue(final NativeNode node, final String onFail) {
		String ret = node.getTextContent();
		if (ret != null)
			return ret.trim();

		return ret;
	}

}
