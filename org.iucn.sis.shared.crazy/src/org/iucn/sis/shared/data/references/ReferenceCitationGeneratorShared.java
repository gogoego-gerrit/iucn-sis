package org.iucn.sis.shared.data.references;

import java.util.HashMap;

public class ReferenceCitationGeneratorShared {

	public static class ReturnedCitation {
		public boolean allFieldsEntered;
		public String citation;

		public ReturnedCitation(boolean allFieldsEntered, String citation) {
			this.allFieldsEntered = allFieldsEntered;
			this.citation = citation;
		}
	}

	/**
	 * Used when the type is submission_type, with published being the default
	 */
	public static final String[] SUBMISSION_TYPES = { "Published", "In press", "Submitted", "In prep" };
	private static final String[] SUBMISSION_TYPES_DISPLAY = { "(in press)" };

	private static String cleanEndPunctuation(String citation) {
		if (citation != null) {
			citation = citation.trim();
			if (citation.endsWith(","))
				citation = citation.substring(0, citation.length() - 1);
			if (!(citation.endsWith(".") || citation.endsWith("!") || citation.endsWith("?")))
				citation = citation + ".";
		}
		return citation;
	}

	private static String createAuthorYearTitle(String author, String year, String title, boolean italize) {
		StringBuffer citation = new StringBuffer();

		if (author != null && !author.equalsIgnoreCase("")) {
			author = author.trim();
			if (!(author.endsWith(".") || author.endsWith("!") || author.endsWith(",") || author.endsWith("?")))
				author = author + ".";
			citation.append(author);
		}

		if (year != null && !year.equalsIgnoreCase(""))
			citation.append(getWhiteSpace(citation) + year + ".");

		if (title != null && !title.equalsIgnoreCase("")) {
			if (title.endsWith("."))
				;
			title.substring(0, title.length() - 1);

			if (italize) {
				title = title.replaceAll("<i>", "!!!!!!!!");
				title = title.replaceAll("</i>", "<i>");
				title = title.replaceAll("!!!!!!!!", "</i>");
				citation.append(getWhiteSpace(citation) + "<i>" + title + "</i>");
			}

			else
				citation.append(getWhiteSpace(citation) + title);

			if (!title.equalsIgnoreCase("")
					&& !(title.endsWith("!") || title.endsWith(".") || title.endsWith("?") || title.endsWith(","))) {
				citation.append(".");
			}
		}

		return citation.toString();
	}

	private static String createEditorJournalName(String editor, String journal, boolean italize, String punctuation) {
		StringBuffer citation = new StringBuffer();

		if (editor != null && !editor.equalsIgnoreCase("")) {
			citation.append(getWhiteSpace(citation) + "In: " + editor);

			if (editor.indexOf(" and ") != -1)
				citation.append(getWhiteSpace(citation) + "(eds)");
			else
				citation.append(getWhiteSpace(citation) + "(ed.)");
			citation.append(", ");
		}

		if (journal != null && !journal.equalsIgnoreCase("")) {
			if (italize) {
				journal = journal.replaceAll("<i>", "!!!!!!!!");
				journal = journal.replaceAll("</i>", "<i>");
				journal = journal.replaceAll("!!!!!!!!", "</i>");

				citation.append(getWhiteSpace(citation) + "<i>" + journal + "</i>" + punctuation);
			}

			else
				citation.append(getWhiteSpace(citation) + journal + punctuation);
		} else {
			citation = new StringBuffer(citation.toString().trim());
		}

		if (!citation.toString().endsWith(punctuation) && citation.toString().endsWith(","))
			citation.replace(citation.length() - 1, citation.length(), punctuation);

		return citation.toString();
	}

	public static ReturnedCitation generateBookChapterCitation(HashMap<String, String> fields) {
		ReturnedCitation returnedCitation = null;
		StringBuffer citation = new StringBuffer();

		try {
			boolean valid = true;
			String author = getLabel(fields, "author");
			String year = getLabel(fields, "year");
			String title = getLabel(fields, "title");
			String bookTitle = getLabel(fields, "secondary_title");
			String editors = getLabel(fields, "secondary_author");
			String pageNumbers = getLabel(fields, "pages");
			String publisher = getLabel(fields, "publisher");
			String placePublished = getLabel(fields, "place_published");
			String submissionType = getLabel(fields, "submission_type");

			year = getYear(year, submissionType);
			submissionType = getSubmissionType(getLabel(fields, "year"), submissionType);

			if (isBlank(author) || isBlank(year) || isBlank(title) || isBlank(publisher) || isBlank(placePublished)
					|| isBlank(pageNumbers) || isBlank(bookTitle) || isBlank(editors)) {
				valid = false;
			}

			citation.append(createAuthorYearTitle(author, year, title, false));
			citation.append(createEditorJournalName(editors, bookTitle, true, ","));

			if (!isBlank(pageNumbers)) {
				if (!pageNumbers.startsWith("pp"))
					pageNumbers = "pp. " + pageNumbers;
				citation.append(getWhiteSpace(citation) + pageNumbers + ".");
			}

			if (publisher != null && !publisher.equalsIgnoreCase("")) {
				citation.append(getWhiteSpace(citation) + publisher);
				if (!isBlank(placePublished)) {
					citation.append(", ");
				}
			}

			if (!isBlank(placePublished))
				citation.append(getWhiteSpace(citation) + placePublished);

			if (!isBlank(submissionType))
				citation.append(getWhiteSpace(citation) + submissionType);

			returnedCitation = new ReturnedCitation(valid, cleanEndPunctuation(citation.toString()));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnedCitation;
	}

	public static ReturnedCitation generateBookCitation(HashMap<String, String> fields) {

		ReturnedCitation returnedCitation = null;
		StringBuffer citation = new StringBuffer();
		try {
			boolean valid = true;
			String author = getLabel(fields, "author");
			String year = getLabel(fields, "year");
			String title = getLabel(fields, "title");
			String publisher = getLabel(fields, "publisher");
			String placePublished = getLabel(fields, "place_published");
			String submissionType = getLabel(fields, "submission_type");

			year = getYear(year, submissionType);
			submissionType = getSubmissionType(getLabel(fields, "year"), submissionType);

			if (isBlank(author) || isBlank(year) || isBlank(title) || isBlank(publisher) || isBlank(placePublished)) {
				valid = false;
			}

			citation.append(createAuthorYearTitle(author, year, title, true));
			if (publisher != null && !publisher.equalsIgnoreCase("")) {
				citation.append(getWhiteSpace(citation) + publisher);
				if (!isBlank(placePublished)) {
					citation.append(",");
				}
			}

			if (!isBlank(placePublished))
				citation.append(getWhiteSpace(citation) + placePublished);

			if (!isBlank(submissionType))
				citation.append(getWhiteSpace(citation) + submissionType);

			returnedCitation = new ReturnedCitation(valid, cleanEndPunctuation(citation.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnedCitation;

	}

	private static ReturnedCitation generateCitation(HashMap<String, String> fields, String type) {
		ReturnedCitation citation = null;
		if (type.equalsIgnoreCase("book"))
			citation = ReferenceCitationGeneratorShared.generateBookCitation(fields);
		else if (type.equalsIgnoreCase("book section"))
			citation = ReferenceCitationGeneratorShared.generateBookChapterCitation(fields);
		else if (type.equalsIgnoreCase("edited book"))
			citation = ReferenceCitationGeneratorShared.generateEditedBookCitation(fields);
		else if (type.equalsIgnoreCase("journal article"))
			citation = ReferenceCitationGeneratorShared.generateJournalArticleCitation(fields);
		else if (type.equalsIgnoreCase("conference proceedings"))
			citation = ReferenceCitationGeneratorShared.generateConferenceProceedingsCitation(fields);
		else if (type.equalsIgnoreCase("computer program"))
			citation = ReferenceCitationGeneratorShared.generateComputerProgramCitation(fields);
		else if (type.equalsIgnoreCase("electronic source"))
			citation = ReferenceCitationGeneratorShared.generateElectronicSourceCitation(fields);
		else if ((type.equalsIgnoreCase("generic")) || (type.equalsIgnoreCase("other")))
			citation = generateOtherCitation(fields);
		else if (type.equalsIgnoreCase("manuscript"))
			citation = ReferenceCitationGeneratorShared.generateManuscriptCitation(fields);
		else if (type.equalsIgnoreCase("magazine article"))
			citation = ReferenceCitationGeneratorShared.generateMagazineCitation(fields);
		else if (type.equalsIgnoreCase("newspaper article"))
			citation = ReferenceCitationGeneratorShared.generateNewspaperCitation(fields);
		else if (type.equalsIgnoreCase("personal communication"))
			citation = ReferenceCitationGeneratorShared.generatePersonalCommunicationCitation(fields);
		else if (type.equalsIgnoreCase("report"))
			citation = ReferenceCitationGeneratorShared.generateReportCitation(fields);
		else if (type.equalsIgnoreCase("rldb"))
			citation = ReferenceCitationGeneratorShared.generateRLDBCitation(fields);
		else if (type.equalsIgnoreCase("thesis"))
			citation = ReferenceCitationGeneratorShared.generateThesisCitation(fields);
		else if (type.equalsIgnoreCase("conference paper"))
			citation = ReferenceCitationGeneratorShared.generateJournalArticleCitation(fields);
		else //If you don't recognize the type, use book
			citation = ReferenceCitationGeneratorShared.generateBookCitation(fields);
		
		if (citation == null) {
			citation = new ReturnedCitation(false, "");
		}

		return citation;

	}

	public static ReturnedCitation generateComputerProgramCitation(HashMap<String, String> fields) {
		ReturnedCitation returnedCitation = null;
		StringBuffer citation = new StringBuffer();

		try {
			boolean valid = true;
			String author = getLabel(fields, "author");
			String year = getLabel(fields, "year");
			String title = getLabel(fields, "title");
			String description = getLabel(fields, "pages");
			String publisher = getLabel(fields, "publisher");
			String city = getLabel(fields, "place_published");

			if (isBlank(author) || isBlank(year) || isBlank(title) || isBlank(publisher) || isBlank(city))
				valid = false;

			citation.append(createAuthorYearTitle(author, year, title, false));
			if (!isBlank(description)) {
				citation.append(getWhiteSpace(citation) + "[" + description + "].");

			}

			if (!isBlank(publisher)) {
				citation.append(getWhiteSpace(citation) + publisher);
				if (!isBlank(city)) {
					citation.append(", " + city);
				}

			} else {
				citation.append(getWhiteSpace(citation) + city);
			}
			citation.append(".");

			returnedCitation = new ReturnedCitation(valid, citation.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnedCitation;
	}

	public static ReturnedCitation generateConferenceProceedingsCitation(HashMap<String, String> fields) {
		ReturnedCitation returnedCitation = null;
		StringBuffer citation = new StringBuffer();

		try {
			boolean valid = true;
			String author = getLabel(fields, "author");
			String year = getLabel(fields, "year");
			String title = getLabel(fields, "title");
			String conferenceName = getLabel(fields, "secondary_title");
			String editor = getLabel(fields, "secondary_author");
			String conferenceLocation = getLabel(fields, "place_published");
			String volume = getLabel(fields, "volume");
			String pageNumbers = getLabel(fields, "pages");
			String issue = getLabel(fields, "number_of_volumes");
			String sponsor = getLabel(fields, "subsidiary_author");
			String submissionType = getLabel(fields, "submission_type");

			year = getYear(year, submissionType);
			submissionType = getSubmissionType(getLabel(fields, "year"), submissionType);

			if (isBlank(author) || isBlank(year) || isBlank(title) || isBlank(conferenceLocation)
					|| isBlank(conferenceName) || isBlank(pageNumbers) || isBlank(volume)) {
				valid = false;
			}

			citation.append(createAuthorYearTitle(author, year, title, false));
			citation.append(createEditorJournalName(editor, conferenceName, false, ""));

			if (!isBlank(volume)) {
				citation.append(getWhiteSpace(citation) + volume);
				if (!isBlank(issue))
					citation.append(getWhiteSpace(citation) + issue);

				if (!isBlank(pageNumbers))
					citation.append(": " + pageNumbers);
			}

			else if (isBlank(editor)) {
				if (!isBlank(pageNumbers)) {
					citation.append(": " + pageNumbers);
				}
			} else {
				if (!isBlank(pageNumbers)) {
					if (!pageNumbers.startsWith("pp"))
						pageNumbers = "pp. " + pageNumbers;
					citation.append(", " + pageNumbers);
				}
			}

			citation.append(".");

			if (!isBlank(sponsor)) {
				citation.append(getWhiteSpace(citation) + sponsor + ".");
			}

			if (!isBlank(conferenceLocation)) {
				citation.append(getWhiteSpace(citation) + conferenceLocation + ".");
			}

			if (!isBlank(submissionType))
				citation.append(getWhiteSpace(citation) + submissionType);

			returnedCitation = new ReturnedCitation(valid, cleanEndPunctuation(citation.toString()));
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return returnedCitation;
	}

	public static ReturnedCitation generateEditedBookCitation(HashMap<String, String> fields) {
		ReturnedCitation returnedCitation = null;
		StringBuffer citation = new StringBuffer();

		try {
			boolean valid = true;
			String editor = getLabel(fields, "author");
			String year = getLabel(fields, "year");
			String title = getLabel(fields, "title");
			String bookTitle = getLabel(fields, "secondary_title");
			String editors = getLabel(fields, "secondary_author");
			String pageNumbers = getLabel(fields, "pages");
			String publisher = getLabel(fields, "publisher");
			String placePublished = getLabel(fields, "place_published");
			String submissionType = getLabel(fields, "submission_type");

			year = getYear(year, submissionType);
			submissionType = getSubmissionType(getLabel(fields, "year"), submissionType);

			if (isBlank(editor) || isBlank(year) || isBlank(title) || isBlank(publisher) || isBlank(placePublished)) {
				valid = false;
			}

			if (editor.indexOf(" and ") > -1)
				editor = editor + " (eds)";
			else
				editor = editor + " (ed.)";

			citation.append(createAuthorYearTitle(editor, year, title, isBlank(bookTitle)));
			citation.append(createEditorJournalName(editors, bookTitle, true, ","));

			if (!isBlank(pageNumbers)) {
				if (!pageNumbers.startsWith("pp"))
					pageNumbers = "pp. " + pageNumbers;
				citation.append(getWhiteSpace(citation) + pageNumbers + ".");
			}

			if (publisher != null && !publisher.equalsIgnoreCase("")) {
				citation.append(getWhiteSpace(citation) + publisher);
				if (!isBlank(placePublished)) {
					citation.append(", ");
				}
			}

			if (!isBlank(placePublished))
				citation.append(getWhiteSpace(citation) + placePublished);
			if (!isBlank(submissionType))
				citation.append(getWhiteSpace(citation) + submissionType);

			returnedCitation = new ReturnedCitation(valid, cleanEndPunctuation(citation.toString()));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnedCitation;
	}

	public static ReturnedCitation generateElectronicSourceCitation(HashMap<String, String> fields) {
		ReturnedCitation returnedCitation = null;
		StringBuffer citation = new StringBuffer();

		try {
			boolean valid = true;
			String author = getLabel(fields, "author");
			String year = getLabel(fields, "year");
			String title = getLabel(fields, "title");
			String city = getLabel(fields, "place_published");
			String accessDate = getLabel(fields, "number");
			String url = getLabel(fields, "url");
			String description = getLabel(fields, "pages");

			if (isBlank(accessDate) || isBlank(url))
				valid = false;

			citation.append(createAuthorYearTitle(author, year, title, false));
			if (!isBlank(city)) {
				citation.append(getWhiteSpace(citation) + city);
				if (!isBlank(description)) {
					citation.append(": " + description);
				}
			} else if (!isBlank(description)) {
				citation.append(getWhiteSpace(citation) + description + ".");
			}
			if (!isBlank(url)) {
				if (url.indexOf("<a href") == -1)
					url = "<a href=\"" + url + "\">" + url + "</a>";
				citation.append(getWhiteSpace(citation) + "Available at: " + url + ". ");
			}
			if (!isBlank(accessDate))
				citation.append(getWhiteSpace(citation) + "(Accessed: " + accessDate + ").");

			returnedCitation = new ReturnedCitation(valid, citation.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnedCitation;
	}

	public static ReturnedCitation generateJournalArticleCitation(HashMap<String, String> fields) {

		ReturnedCitation returnedCitation = null;
		StringBuffer citation = new StringBuffer();

		try {
			boolean valid = true;
			String author = getLabel(fields, "author");
			String year = getLabel(fields, "year");
			String title = getLabel(fields, "title");
			String journal = getLabel(fields, "secondary_title");
			String volume = getLabel(fields, "volume");
			String pageNumbers = getLabel(fields, "pages");
			String issue = getLabel(fields, "number");
			String submissionType = getLabel(fields, "submission_type");

			year = getYear(year, submissionType);
			submissionType = getSubmissionType(getLabel(fields, "year"), submissionType);

			if (isBlank(author) || isBlank(year) || isBlank(title) || isBlank(journal) || isBlank(pageNumbers)
					|| isBlank(volume)) {
				valid = false;
			}

			citation.append(createAuthorYearTitle(author, year, title, false));
			citation.append(createEditorJournalName("", journal, true, ""));

			if (!isBlank(volume)) {
				citation.append(getWhiteSpace(citation) + volume);
				if (!isBlank(issue))
					citation.append("(" + issue + ")");
			}

			if (!isBlank(pageNumbers)) {
				if (pageNumbers.startsWith("pp"))
					pageNumbers = pageNumbers.substring(2).trim();
				if (pageNumbers.startsWith("."))
					pageNumbers = pageNumbers.substring(1).trim();
				citation.append(": " + pageNumbers);
			}

			if (!isBlank(submissionType))
				citation.append(getWhiteSpace(citation) + submissionType);

			returnedCitation = new ReturnedCitation(valid, cleanEndPunctuation(citation.toString()));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnedCitation;
	}

	public static ReturnedCitation generateMagazineCitation(HashMap<String, String> fields) {
		return generateJournalArticleCitation(fields);
	}

	public static ReturnedCitation generateManuscriptCitation(HashMap<String, String> fields) {
		return generateBookCitation(fields);
	}

	/**
	 * Generates a new citation and places it in the citation field.
	 * 
	 * @param referenceElement
	 */
	public static ReturnedCitation generateNewCitation(HashMap<String, String> fields, String type) {
		return generateCitation(fields, type);
	}

	public static ReturnedCitation generateNewspaperCitation(HashMap<String, String> fields) {
		return generateJournalArticleCitation(fields);
	}

	public static ReturnedCitation generateOtherCitation(HashMap<String, String> fields) {
		return generateBookCitation(fields);
	}

	public static ReturnedCitation generatePersonalCommunicationCitation(HashMap<String, String> fields) {
		return null;
	}

	public static ReturnedCitation generateReportCitation(HashMap<String, String> fields) {
		ReturnedCitation returnedCitation = null;
		StringBuffer citation = new StringBuffer();

		try {
			boolean valid = true;
			String author = getLabel(fields, "author");
			String year = getLabel(fields, "year");
			String title = getLabel(fields, "title");
			String editor = getLabel(fields, "secondary_author");
			String journal = getLabel(fields, "secondary_title");
			String city = getLabel(fields, "place_published");
			String institution = getLabel(fields, "publisher");
			String submissionType = getLabel(fields, "submission_type");

			year = getYear(year, submissionType);
			submissionType = getSubmissionType(getLabel(fields, "year"), submissionType);
			if (isBlank(author) || isBlank(year) || isBlank(title) || isBlank(journal) || isBlank(city)) {
				valid = false;
			}

			citation.append(createAuthorYearTitle(author, year, title, false));
			citation.append(getWhiteSpace(citation) + createEditorJournalName(editor, journal, false, "."));
			if (!isBlank(institution))
				citation.append(getWhiteSpace(citation) + institution + ",");
			if (!isBlank(city))
				citation.append(getWhiteSpace(citation) + city);

			citation = new StringBuffer(cleanEndPunctuation(citation.toString()));

			if (!isBlank(submissionType))
				citation.append(getWhiteSpace(citation) + submissionType);

			returnedCitation = new ReturnedCitation(valid, cleanEndPunctuation(citation.toString()));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnedCitation;
	}

	public static ReturnedCitation generateRLDBCitation(HashMap<String, String> fields) {
		ReturnedCitation returnedCitation = null;

		try {
			returnedCitation = new ReturnedCitation(true, getLabel(fields, "citation"));
			if (isBlank(returnedCitation.citation))
				returnedCitation.citation = getLabel(fields, "secondary_title");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnedCitation;
	}

	public static ReturnedCitation generateThesisCitation(HashMap<String, String> fields) {
		ReturnedCitation returnedCitation = null;
		StringBuffer citation = new StringBuffer();

		try {
			boolean valid = true;
			String author = getLabel(fields, "author");
			String year = getLabel(fields, "year");
			String title = getLabel(fields, "title");
			String dept = getLabel(fields, "secondary_title");
			String institution = getLabel(fields, "publisher");
			String submissionType = getLabel(fields, "submission_type");

			year = getYear(year, submissionType);
			submissionType = getSubmissionType(getLabel(fields, "year"), submissionType);

			if (isBlank(author) || isBlank(year) || isBlank(title) || isBlank(dept) || isBlank(institution))
				valid = false;

			citation.append(createAuthorYearTitle(author, year, title, false));
			if (!isBlank(dept)) {
				citation.append(getWhiteSpace(citation) + dept);
				if (!isBlank(institution)) {
					citation.append(", " + institution + ".");
				}

			} else if (!isBlank(institution)) {
				citation.append(getWhiteSpace(citation) + institution + ".");
			}

			if (!isBlank(submissionType))
				citation.append(getWhiteSpace(citation) + submissionType);

			returnedCitation = new ReturnedCitation(valid, cleanEndPunctuation(citation.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnedCitation;
	}

	public static String getLabel(HashMap<String, String> fields, String labelName) {
		String ret = (String) fields.get(labelName);
		if (ret == null)
			return "";

		return ret;
	}

	private static String getSubmissionType(String year, String submissionType) {
		String returnValue = "";
		if (submissionType != null && submissionType.equalsIgnoreCase(SUBMISSION_TYPES[1]) && year != null
				&& !year.trim().equalsIgnoreCase("")) {
			returnValue = SUBMISSION_TYPES_DISPLAY[0];
		}
		return returnValue;
	}

	private static String getWhiteSpace(StringBuffer citation) {
		if (citation.toString().endsWith(" "))
			return "";
		else
			return " ";
	}

	private static String getYear(String year, String submissionType) {
		String returnValue = year;

		if (submissionType == null)
			return year;
		else if (submissionType.equalsIgnoreCase(SUBMISSION_TYPES[2])
				|| submissionType.equalsIgnoreCase(SUBMISSION_TYPES[3])) {
			returnValue = submissionType;
		} else if (submissionType.equalsIgnoreCase(SUBMISSION_TYPES[1]) && (year == null || year.equalsIgnoreCase(""))) {
			returnValue = submissionType;
		}
		return returnValue;
	}

	private static boolean isBlank(String text) {
		return (text == null || text.equalsIgnoreCase(""));
	}

}
