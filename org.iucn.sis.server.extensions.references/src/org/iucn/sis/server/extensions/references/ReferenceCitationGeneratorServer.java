package org.iucn.sis.server.extensions.references;

import java.util.HashMap;

import org.iucn.sis.shared.api.citations.ReferenceCitationGeneratorShared;
import org.iucn.sis.shared.api.citations.ReferenceCitationGeneratorShared.ReturnedCitation;
import org.iucn.sis.shared.api.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ReferenceCitationGeneratorServer {

	@SuppressWarnings("deprecation")
	private static void generateCitation(Element referenceElement, Document doc, boolean checkFirst) {
		try {

			String type = referenceElement.getAttribute("type");
			HashMap<String, String> fields = getFields(referenceElement);

			if (type == null || type.equalsIgnoreCase("") || type.equalsIgnoreCase("null")) {
				type = "rldb";
				referenceElement.setAttribute("type", type);
			}

			if (!checkFirst || shouldGenerateCitation(fields)) {
				ReturnedCitation citation = null;
				if (matches(type, "book"))
					citation = ReferenceCitationGeneratorShared.generateBookChapterCitation(fields);
				else if (matches(type, "book_section", "book section"))
					citation = ReferenceCitationGeneratorShared.generateBookChapterCitation(fields);
				else if (matches(type, "computer_program", "computer program"))
					citation = ReferenceCitationGeneratorShared.generateComputerProgramCitation(fields);
				else if (matches(type, "conference_proceedings"))
					citation = ReferenceCitationGeneratorShared.generateConferenceProceedingsCitation(fields);
				else if (matches(type, "edited_book", "edited book"))
					citation = ReferenceCitationGeneratorShared.generateEditedBookCitation(fields);
				else if (matches(type, "electronic_source", "electronic source"))
					citation = ReferenceCitationGeneratorShared.generateElectronicSourceCitation(fields);
				else if (matches(type, "journal_article", "journal article"))
					citation = ReferenceCitationGeneratorShared.generateJournalArticleCitation(fields);
				else if (matches(type, "magazine_article", "magazine article"))
					citation = ReferenceCitationGeneratorShared.generateMagazineCitation(fields);
				else if (matches(type, "manuscript"))
					citation = ReferenceCitationGeneratorShared.generateManuscriptCitation(fields);
				else if (matches(type, "newspaper_article", "newspaper article"))
					citation = ReferenceCitationGeneratorShared.generateNewspaperCitation(fields);
				else if (matches(type, "personal_communication", "personal communication"))
					citation = ReferenceCitationGeneratorShared.generatePersonalCommunicationCitation(fields);
				else if (matches(type, "report"))
					citation = ReferenceCitationGeneratorShared.generateReportCitation(fields);
				else if (matches(type, "rldb"))
					citation = ReferenceCitationGeneratorShared.generateRLDBCitation(fields);
				else if (matches("thesis"))
					citation = ReferenceCitationGeneratorShared.generateThesisCitation(fields);
				else if (matches(type, "generic", "other"))
					citation = generateGenericCitation(referenceElement);

				if (citation == null)
					citation = new ReturnedCitation(false, "");

				setCitation(referenceElement, doc, citation);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private static boolean matches(String type, String... potentialTypes) {
		for (String current : potentialTypes)
			if (type.equalsIgnoreCase(current))
				return true;
		return false;
	}

	/**
	 * given an element which represents the reference, it checks to see if
	 * there is already a secondary title -- if there is, it exits without
	 * adding citation, if there isn't, adds a label with the name citation with
	 * the generated citation
	 * 
	 * @param referenceElement
	 */
	public static void generateCitationIfNotAlreadyGenerated(Element referenceElement, Document doc) {
		generateCitation(referenceElement, doc, true);
	}

	protected static ReturnedCitation generateGenericCitation(Element referenceElement) {
		referenceElement.setAttribute("type", "Other");
		return ReferenceCitationGeneratorShared.generateOtherCitation(getFields(referenceElement));
	}

	/**
	 * Generates a new citation and places it in the citation field.
	 * 
	 * @param referenceElement
	 */
	public static void generateNewCitation(Element referenceElement, Document doc) {
		generateCitation(referenceElement, doc, false);
	}

	private static HashMap<String, String> getFields(Element referenceElement) {
		HashMap<String, String> fields = new HashMap<String, String>();
		NodeList list = referenceElement.getElementsByTagName("field");

		for (int i = 0; i < list.getLength(); i++) {
			Element temp = (Element) list.item(i);
			String name = temp.getAttribute("name");
			String textContent = temp.getTextContent();

			fields.put(name, textContent);
		}

		return fields;

	}

	private static void makeElement(String name, String label, String textContent, Element parentElement, Document doc) {
		Element newElement = doc.createElement("field");
		newElement.setAttribute("name", name);
		// newElement.setAttribute("label", label);
		newElement.setTextContent(textContent);
		parentElement.appendChild(newElement);
	}

	private static boolean setCitation(Element referenceElement, Document doc, ReturnedCitation citation) {
		boolean success = true;
		try {
			NodeList fields = referenceElement.getElementsByTagName("field");
			Element citationElement = null;
			Element valid = null;

			for (int i = 0; i < fields.getLength() && (citationElement == null || valid == null); i++) {
				Element temp = (Element) fields.item(i);
				if (temp.getAttribute("name").equalsIgnoreCase("citation"))
					citationElement = temp;
				else if (temp.getAttribute("name").equalsIgnoreCase("citation"))
					valid = temp;
			}

			String citationString = (citation.citation == null) ? "" : citation.citation;
			if (citationElement != null) {
				citationElement.setTextContent(XMLUtils.clean(citationString));
			} else {
				makeElement("citation", "Citation", citationString, referenceElement, doc);
			}

			String bool = "Y";
			if (!citation.allFieldsEntered)
				bool = "N";
			if (valid != null) {
				valid.setTextContent(bool);
			} else {
				makeElement("citation_complete", "Citation_Complete", bool, referenceElement, doc);
			}

		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	private static boolean shouldGenerateCitation(HashMap<String, String> fields) {
		boolean generate = false;

		try {
			String citation = ReferenceCitationGeneratorShared.getLabel(fields, "citation");
			if (citation != null && !citation.trim().equalsIgnoreCase(""))
				generate = true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return generate;
	}

}
