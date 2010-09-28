package org.iucn.sis.server.extensions.findreplace;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
import org.iucn.sis.server.api.locking.LockType;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.shared.api.findreplace.FindReplaceData;
import org.iucn.sis.shared.api.findreplace.FindReplaceData.FieldReplacementInfo;
import org.iucn.sis.shared.api.findreplace.FindReplaceData.OldNewStrings;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.primitivefields.TextPrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * 
 * @author liz.schwartz
 * 
 */
public class FindReplace {

	/**
	 * Used for which field to check, otherwise it is a canonical name
	 */
	public final static String ALL = "all";

	private String calculateAssessmentName(VFS vfs, String url, String fileText) {
		String name = "";
		try {
			NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
			ndoc.parse(DocumentUtils.getVFSFileAsString(url, vfs));
			Assessment assessment = Assessment.fromXML(ndoc);

			name += assessment.getSpeciesName();
			List<Integer> region = assessment.getRegionIDs();

			for (Integer cur : region)
				name += " ${" + cur + "}";

			Date date = assessment.getDateAssessed();
			if (date != null) {
				name += " " + date;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	private int calculateFlag(String options) {
		int flag = Pattern.CANON_EQ;
		if (isCaseInsensitive(options)) {
			flag = flag | Pattern.CASE_INSENSITIVE;
		}
		return flag;
	}

	protected TextPrimitiveField getPrimitiveFieldToSearch(Assessment assessment, String field)
			throws FindReplaceException {
		if (assessment.getField(field) != null) {
			Map<String, PrimitiveField> pfs = assessment.getField(field).getKeyToPrimitiveFields();
			if (pfs.size() > 1)
				throw new FindReplaceException("Unable to search field " + field
						+ " because it has more than 1 primitive fields");
			for (Entry<String, PrimitiveField> entry : pfs.entrySet()) {
				try {
					return ((TextPrimitiveField) entry.getValue());
				} catch (ClassCastException e) {
					throw new FindReplaceException("In order to do find/replace the primitive field for field " + field
							+ " must be a text primitive field");
				}

			}
		}
		return null;

	}

	/**
	 * Gets the list of fields to search for the string
	 * 
	 * @param field
	 * @return
	 */
	protected List<String> getFieldsToSearch(String field) {
		List<String> fields = new ArrayList<String>();
		if (field.equalsIgnoreCase("all")) {
			fields.add(CanonicalNames.RedListRationale);
			fields.add(CanonicalNames.ConservationActionsDocumentation);
			fields.add(CanonicalNames.HabitatDocumentation);
			fields.add(CanonicalNames.PopulationDocumentation);
			fields.add(CanonicalNames.RangeDocumentation);
			fields.add(CanonicalNames.ThreatsDocumentation);
			fields.add(CanonicalNames.UseTradeDocumentation);
			fields.add(CanonicalNames.TaxonomicNotes);
		} else {
			fields.add(field);
		}
		return fields;
	}

	/**
	 * 
	 * @param assessment
	 * @param searchString
	 * @param options
	 * @param field
	 * @return
	 * @throws PersistentException
	 * @throws FindReplaceException
	 */
	public FindReplaceData find(Assessment assessment, String searchString, String options, String field)
			throws PersistentException, FindReplaceException {

		FindReplaceData result = new FindReplaceData(assessment.getId(), assessment.getSpeciesName() + " "
				+ assessment.getDateAssessed(), assessment.getAssessmentType().getDisplayName(), searchString);
		String javaString = javaSearchString(options, searchString);
		int flag = calculateFlag(options);
		List<String> fields = getFieldsToSearch(field);

		for (String curField : fields) {
			TextPrimitiveField pf = getPrimitiveFieldToSearch(assessment, curField);
			if (pf != null && pf.getValue() != null) {
				Pattern compiledRegex = Pattern.compile(javaString, flag);
				Matcher regexMatcher = compiledRegex.matcher(pf.getValue());
				int counter = 0;
				List<OldNewStrings> indexToText = new ArrayList<OldNewStrings>();
				while (regexMatcher.find()) {
					indexToText.add(new OldNewStrings(regexMatcher.group(), null, regexMatcher.start()));
					counter++;
				}
				if (!indexToText.isEmpty()) {
					result.addNewFinding(pf.getValue(), pf.getField().getId(), curField, indexToText);
				}
			}
		}

		if (result.getSentenceDataList().isEmpty())
			return null;

		return result;

		// DO POSTGRES SEARCH
		// String fieldIDsql =
		// "SELECT id as f_id, name as f_name FROM field WHERE assessmentid=" +
		// assessment.getId()
		// + " AND " + narrativeFieldSQLClause(field);
		// String primitiveFieldSQL = "(("
		// + fieldIDsql
		// +
		// ") fieldIDs JOIN primitive_field on fieldIDs.f_id = primitive_field.fieldid) primitive_field_ids JOIN text_primitive_field on primitive_field_ids.id=text_primitive_field.primitive_fieldid";
		// String sql = "SELECT index,value,f_name from (" + primitiveFieldSQL +
		// ") WHERE value " + posgresSearchString
		// + ";";

		// System.out.println("sql is " + sql);

		// Session session = SISPersistentManager.instance().getSession();
		// SQLQuery query = session.createSQLQuery(sql).addScalar("index",
		// Hibernate.INTEGER).addScalar("value",
		// Hibernate.TEXT).addScalar("f_name", Hibernate.STRING);
		// List<Object[]> results = query.list();

		// if (results.size() > 0) {
		// int flag = calculateFlag(options);
		// Pattern compiledRegex = Pattern.compile(javaString, flag);
		//
		// for (Object[] sqlResult : results) {
		// String value = (String) sqlResult[2];
		// Integer id = (Integer) sqlResult[0];
		// String name = (String) sqlResult[1];
		//
		// List<OldNewStrings> foundStrings = findWithinSentence(compiledRegex,
		// value);
		// if (foundStrings.size() > 0)
		// result.addNewFinding(value, id, name, foundStrings);
		// }
		// }
		//
		// if (result.getSentenceDataList().size() > 0)
		// return result;
		// return null;

		// String regex = getRegex(searchString);
		//
		// if (vfs.exists(uri)) {
		// String fileText = getFileText(uri, vfs, field);
		// if (fileText != null) {
		// Pattern compiledRegex = Pattern.compile(regex, flag);
		// Pattern compiledInnerRegex = Pattern.compile(searchString, flag);
		// Matcher regexMatcher = compiledRegex.matcher(fileText);
		// int counter = 0;
		// int previousIndex = 0;
		// while (regexMatcher.find()) {
		// String resultDescription = getFieldID(fileText, previousIndex,
		// regexMatcher.start());
		// previousIndex = regexMatcher.end();
		// HashMap<String, OldNewStrings> indexToText =
		// findWithinSentence(compiledInnerRegex, regexMatcher
		// .group(groupWithEntireStructureWithoutTags));
		//
		// result.addNewFinding(resultDescription, counter, previousIndex,
		// regexMatcher
		// .group(groupWithEntireStructureWithoutTags), indexToText);
		// counter++;
		// }
		// if (counter > 0) {
		// result.setName(calculateAssessmentName(vfs, uri.toString(),
		// fileText));
		// } else {
		// result = null;
		// }
		// } else {
		// result = null;
		// }
		// // }
		// return result;
	}

	private List<OldNewStrings> findWithinSentence(Pattern text, String sentence) {
		List<OldNewStrings> indexToText = new ArrayList<OldNewStrings>();
		Matcher regexMatcher = text.matcher(sentence);
		int counter = 0;
		while (regexMatcher.find()) {
			indexToText.add(new OldNewStrings(regexMatcher.group(), null, regexMatcher.start()));
			counter++;
		}
		// System.out.println(
		// "this is the size of the hashmap i am returning "
		// + indexToText.size());
		return indexToText;
	}

	// private String getFieldID(String fileText, int previousIndex, int index)
	// {
	// String id = "";
	// int fileIndex = fileText.substring(0, index).lastIndexOf("field id=\"");
	// if (fileIndex > -1) {
	// id = fileText.substring(fileIndex + 10, fileIndex + 10 +
	// fileText.substring(fileIndex + 10).indexOf("\""));
	// }
	// return id;
	// }

	private String getFieldSpecificRegex(String field) {

		return "<field\\sid=\"" + field + "\">(.|\\s)*?(</field>){1}";

	}

	private String getFileText(VFSPath uri, VFS vfs, String field) {
		String returnString = DocumentUtils.getVFSFileAsString(uri.toString(), vfs);
		if (!field.equalsIgnoreCase(ALL)) {
			Pattern pattern = Pattern.compile(getFieldSpecificRegex(field));
			// System.out.println(returnString);
			Matcher matcher = pattern.matcher(returnString);
			if (matcher.find()) {
				returnString = matcher.group();
			} else {
				returnString = null;
			}
		}
		return returnString;
	}

	private String getRegex(String regex) {
		return "(<structure[^>/]*>){1}([^<]*(" + regex + ")[^<]*)(</structure>){1}";

	}

	private boolean isCaseInsensitive(String options) {
		try {
			return options.split(",")[0].equals("1");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean isEntireWord(String options) {
		try {
			return options.split(",")[1].equals("1");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean isRegex(String options) {
		try {
			return options.split(",")[2].equals("1");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean replace(User user, FindReplaceData data, String options, String field) {

		boolean success = false;
		try {

			success = replaceInFields(user, data, options, field);

		} catch (FindReplaceException e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	public boolean replaceInFields(User user, FindReplaceData data, String options, String field)
			throws FindReplaceException {
		boolean success = false;
		Assessment assessment = SIS.get().getAssessmentIO().getAssessment(data.getAssessmentID());

		if (assessment != null) {

			Map<Integer, String> fieldsToSave = new HashMap<Integer, String>();

			List<FieldReplacementInfo> replaceData = data.getSentenceDataList();
			Pattern compiledRegex = Pattern.compile(javaSearchString(options, data.getText()));

			for (FieldReplacementInfo replacement : replaceData) {
				String oldValue = replacement.value;
				Matcher regexMatcher = compiledRegex.matcher(oldValue);
				StringBuffer replacementString = new StringBuffer();
				Map<Integer, OldNewStrings> replacements = replacement.getMap();
				while (regexMatcher.find()) {
					if (replacements.containsKey(regexMatcher.start())) {
						regexMatcher.appendReplacement(replacementString, Matcher.quoteReplacement(replacements
								.get(regexMatcher.start()).newString));
					}
				}
				regexMatcher.appendTail(replacementString);
				
				List<String> fields = getFieldsToSearch(replacement.fieldName);
				for (String fieldToReplace : fields) {
					TextPrimitiveField pf = getPrimitiveFieldToSearch(assessment, fieldToReplace);
					pf.setValue(replacementString.toString());
					fieldsToSave.put(pf.getId(), pf.getValue());
				}
			}
			
			success = SIS.get().getPrimitiveFieldIO().updateTextPrimitiveFieldValuesInDatabase(fieldsToSave, user,
					assessment);

		}
		return success;

	}

	private String javaSearchString(String options, String originalString) {
		if (isRegex(options)) {
			if (originalString.contains("*")) {
				originalString = originalString.replaceAll("\\*", "\\\\E\\\\w+\\\\Q");

				if (originalString.startsWith("\\E")) {
					originalString = originalString.substring(2);
				} else {
					originalString = "\\Q" + originalString;
				}

				if (originalString.endsWith("\\Q")) {
					originalString = originalString.substring(0, originalString.length() - 2);
				} else {
					originalString = originalString + "\\E";
				}
			}

			if (isEntireWord(options)) {
				originalString = "\\b" + originalString + "\\b";
			}
		} else if (isEntireWord(options)) {
			originalString = "\\b\\Q" + originalString + "\\E\\b";
		} else {
			originalString = "\\Q" + originalString + "\\E";
		}
		return originalString;
	}

	private String postgresSearchString(String options, String originalString) {

		String searchString = "";
		if (isCaseInsensitive(options)) {
			searchString += "~* ";
		} else {
			searchString += "~ ";
		}

		if (isRegex(options)) {
			if (originalString.contains("*")) {
				originalString = originalString.replaceAll("\\*", "\\w+");
			}

			if (isEntireWord(options)) {
				originalString = "\\m" + originalString + "\\M";
			}
		} else {
			originalString = "(?q)" + originalString;
		}
		return searchString + originalString;
	}

	private String narrativeFieldSQLClause(String field) {
		if (field == null || field.equals("") || field.equalsIgnoreCase("all")) {
			StringBuilder x = new StringBuilder();
			for (String name : CanonicalNames.narrativeFields) {
				x.append("name='" + name + "' OR ");
			}
			return "(" + x.substring(0, x.length() - 2) + ")";
		} else {
			return "name='" + field + "'";
		}

	}

}
