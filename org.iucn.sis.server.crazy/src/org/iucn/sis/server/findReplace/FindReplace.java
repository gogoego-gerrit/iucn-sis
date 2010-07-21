package org.iucn.sis.server.findReplace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.iucn.sis.server.findReplace.FindResultDataServer.OldNewStrings;
import org.iucn.sis.server.findReplace.FindResultDataServer.SentenceReplacementData;
import org.iucn.sis.server.locking.FileLocker;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentParser;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.utils.VFSUtils;

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
			AssessmentData assessment = new AssessmentParser(ndoc).getAssessment();

			name += assessment.getSpeciesName();
			List<String> region = assessment.getRegionIDs();

			for( String cur : region )
				name += " ${" + cur + "}";
			
			String date = assessment.getDateAssessed();
			if( date != null && !date.equals("") ) {
				if (date.length() > 10)
					date = date.substring(0, 10);
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

	private String calculateReplacement(String group, SentenceReplacementData data, Pattern pattern) {

		HashMap<String, OldNewStrings> replacements = data.getReplacementString();
		boolean startsWith = false;
		boolean endsWith = false;
		System.out.println("comming in calculate replacement with group: \n" + group);
		if (group.startsWith("<structure>")) {
			startsWith = true;
			group = group.substring("<structure>".length());
		}
		
		if (group.endsWith("</structure>")) {
			endsWith = true;
			group = group.substring(0, group.length()-"</structure>".length());
		}
		
		Matcher regexMatcher = pattern.matcher(group);
		int counter = 0;
		StringBuffer replacementString = new StringBuffer();
		Iterator<String> iter = replacements.keySet().iterator();
		while (regexMatcher.find()) {
			if (replacements.containsKey(counter + "")) {
				regexMatcher.appendReplacement(replacementString, Matcher.quoteReplacement(replacements.get(counter
						+ "").newString));
			}
			counter++;
		}
		
		
		regexMatcher.appendTail(replacementString);
		if (startsWith)
			replacementString = new StringBuffer("<structure>" + replacementString.toString());
		if (endsWith)
			replacementString.append("</structure>");
		return replacementString.toString();
	}

	/**
	 * 
	 * @param vfs
	 * @param uri
	 * @param regex
	 * @return
	 */
	public FindResultDataServer findAndReturnLineRegex(VFS vfs, VFSPath uri, String string, String options, String field) {

		FindResultDataServer result = new FindResultDataServer(string, uri);
		int groupWithEntireStructureWithoutTags = 2;
		int groupOfFindings = 3;
		int flag = calculateFlag(options);
		string = searchString(options, string);
		String regex = getRegex(string);

		if (vfs.exists(uri)) {
			String fileText = getFileText(uri, vfs, field);
			if (fileText != null) {
				Pattern compiledRegex = Pattern.compile(regex, flag);
				Pattern compiledInnerRegex = Pattern.compile(string, flag);
				Matcher regexMatcher = compiledRegex.matcher(fileText);
				int counter = 0;
				int previousIndex = 0;
				while (regexMatcher.find()) {
					String resultDescription = getFieldID(fileText, previousIndex, regexMatcher.start());
					previousIndex = regexMatcher.end();
					HashMap<String, OldNewStrings> indexToText = findWithinSentence(compiledInnerRegex, regexMatcher
							.group(groupWithEntireStructureWithoutTags));

					result.addNewFinding(resultDescription, counter, previousIndex, regexMatcher
							.group(groupWithEntireStructureWithoutTags), indexToText);
					counter++;
				}
				if (counter > 0) {
					result.setName(calculateAssessmentName(vfs, uri.toString(), fileText));
				} else {
					result = null;
				}
			} else {
				result = null;
			}
		}
		return result;
	}

	private HashMap<String, OldNewStrings> findWithinSentence(Pattern text, String sentence) {
		HashMap<String, OldNewStrings> indexToText = new HashMap<String, OldNewStrings>();
		Matcher regexMatcher = text.matcher(sentence);
		int counter = 0;
		while (regexMatcher.find()) {
			indexToText.put(counter + "", new OldNewStrings(regexMatcher.group(), null, regexMatcher.start()));
			counter++;
		}
		// System.out.println(
		// "this is the size of the hashmap i am returning "
		// + indexToText.size());
		return indexToText;
	}

	private String getFieldID(String fileText, int previousIndex, int index) {
		String id = "";
		int fileIndex = fileText.substring(0, index).lastIndexOf("field id=\"");
		if (fileIndex > -1) {
			id = fileText.substring(fileIndex + 10, fileIndex + 10 + fileText.substring(fileIndex + 10).indexOf("\""));
		}
		return id;
	}

	private String getFieldSpecificRegex(String field) {

		return "<field\\sid=\"" + field + "\">(.|\\s)*?(</field>){1}";
		// return "<field\\sid=\""+field+"\">(([^\2]*)(</field>)){1}";
		// return
		// "<field\\sid=\""+field+"\">[^(\\Q</field>\\E)]*(\\Q</field>\\E){1}";
		// return "<field\\sid=\""+field+"\">[^(</field>)]*(</field>){1}";
		// return "(\\Q<field\\E\\s+id=\"" + field +
		// "\">){1}\\s*(((<structure[^>/]*>){1}([^\\Q</structure>\\E\\Q</field>\\E]*)(</structure>){1}\\s*)|(\\Q<structure/>\\E\\s*))*([^\\Q</field>\\E]*)(</field>){1}"
		// ;
		// return "(<field\\s*id=\""+ field +
		// "\">)([^\\Q</field>\\E]*)(</field>){1}";
		// return "(<field\\s*id=\"" + field +
		// "\">)([^(<\\/field>)]*)(</field>){1}";
		// return
		// "(<field[^>/]*id=\""+field+"\">){1}([^(<field.*>)]*)(</field>){1}";
		// return "(<field\\s+id=\""+ field + "\">)([^(</field>]*)(</field>)";
		// return "(\\Q<field id=\""+ field +
		// "\">\\E)([^(\\Q</field>\\E)]*)(\\Q</field>\\E)";
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
		// return "(<structure[^>/]*>){1}(([^\\Q</structure>\\E]*)(\\Q" + regex
		// + "\\E)([^\\Q</structure>\\E]*))(</structure>){1}";
		// return "(<structure[^>]*>){1}([^(</structure>)]*(This)(
		// return
		// "(<structure[^>]*>){1}(([^\Q</structure>\E]*)(\QThis\E)([^\Q</structure
		// >\E]*))(</structure>){1}"
		// return "(<structure[^>]*>){1}([^(</structure>)]*(" + regex +
		// ")[^(</structure>)]*)(</structure>){1}";
		// return "(<structure[^>]*>){1}(([^\\Q</structure>\\E]*)(\\Q" + regex +
		// "\\E)([^\\Q</structure>\\E]*))(</structure>){1}";
		// return "(<[a-zA-Z0-9]*.*>){1}([^(<[a-zA-Z0-9]*.*>)]*(\\Q" + regex +
		// "\\E)[^(<[a-zA-Z0-9]*>)]*)(</[a-zA-Z0-9]*>){1}";
		// return "(<structure.*>){1}([^(<structure.*>)]*(\\Q" + regex +
		// "\\E)[^(<structure>)]*)(</structure*>){1}";
		// return "(<structure>){1}([^(<structure>)]*(\\Q"+regex +
		// "\\E)[^(<structure>)]*)(</structure>){1}";
		// return
		// "(<[a-zA-Z0-9]*.*>(\Q<![CDATA[\E)?+)([^(<[a-zA-Z0-9]*.*>)]*(\Q" +
		// string + "\E)[^(<[a-zA-Z0-9]*>)]*)((\Q]]>\E)?+</[a-zA-Z0-9]*>)"
		// (<field\s+id="([^"]*)">){1}\s*((<structure[^>/]*>){1}([^\Q</structure>
		// \E\Q</field>\E]*)
		// (</structure>){1}\s*)*((<structure[^>/]*>){1}([^\Q</structure>\E\Q</
		// field>\E]*)(</structure>){1}\s*)
		// ((<structure[^>/]*>){1}([^\Q</structure>\E\Q</field>\E]*)(</structure>
		// ){1}\s*)*([^\Q</field>\E]*)(</field>){1}
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

	public boolean replace(VFS vfs, FindResultDataServer data, String options, String field) {

		boolean success = false;
		if (field.equalsIgnoreCase(ALL)) {
			success = replaceInAllFields(vfs, data, options);
		} else {
			success = replaceWithinField(vfs, data, options, field);
		}
		return success;
	}

	public boolean replaceInAllFields(VFS vfs, FindResultDataServer data, String options) {
		// System.out.println("I am in replace");
		boolean success = false;
		try {
			if (vfs.exists(data.url)) {

				// System.out.println("The url exists");
				// GET LOCK ON FILE, IF DON'T GET LOCK, FAIL
				if (FileLocker.impl.aquireLock(data.url)) {
					boolean problemsReplacing = false;
					// System.out.println("The lock was gotten");

					// GET TEXT THAT WE NEED TO LOOK THROUGH AND LOOK FOR
					HashMap<String, SentenceReplacementData> indexList = new HashMap<String, SentenceReplacementData>();
					for (int i = 0; i < data.getSentenceDataList().size(); i++) {
						indexList.put(data.getSentenceData(i).groupIndex.trim(), data.getSentenceData(i));
					}

					String fileText = DocumentUtils.getVFSFileAsString(data.url, vfs);
					String searchString = searchString(options, data.getText());
					int flag = calculateFlag(options);
					String regex = getRegex(searchString);

					
					// START THE REPLACING
					Pattern compiledInnerRegex = Pattern.compile(searchString, flag);
					Pattern compiledRegex = Pattern.compile(regex, flag);
					Matcher regexMatcher = compiledRegex.matcher(fileText);
					StringBuffer buffer = new StringBuffer();
					int counter = 0;

					while (regexMatcher.find()) {
						if (indexList.containsKey(counter + "")) {
							SentenceReplacementData senData = indexList.get(counter + "");
							int beginIndex = senData.getBeginIndex();

							if (regexMatcher.end() == beginIndex) {
								String replacement = calculateReplacement(regexMatcher.group(), indexList.get(counter
										+ ""), compiledInnerRegex);


								regexMatcher.appendReplacement(buffer, replacement);
							} else {
								// System.out.println(
								// "They weren't equal");
								problemsReplacing = true;
							}
						}
						counter++;
					}
					regexMatcher.appendTail(buffer);
					if (!problemsReplacing) {
						DocumentUtils.writeVFSFile(data.url, vfs, buffer.toString());
						
					}
					FileLocker.impl.releaseLock(data.url);
					success = !problemsReplacing;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return success;
	}

	public boolean replaceWithinField(VFS vfs, FindResultDataServer data, String options, String field) {
		// System.out.println("I am in replace");
		boolean success = false;
		try {
			if (vfs.exists(data.url)) {

				// System.out.println("The url exists");
				// GET LOCK ON FILE, IF DON'T GET LOCK, FAIL
				if (FileLocker.impl.aquireLock(data.url)) {
					boolean problemsReplacing = false;
					// System.out.println("The lock was gotten");

					// GET TEXT THAT WE NEED TO LOOK THROUGH AND LOOK FOR
					HashMap<String, SentenceReplacementData> indexList = new HashMap<String, SentenceReplacementData>();
					for (int i = 0; i < data.getSentenceDataList().size(); i++) {
						indexList.put(data.getSentenceData(i).groupIndex.trim(), data.getSentenceData(i));
					}

					String originalFileText = DocumentUtils.getVFSFileAsString(data.url, vfs);
					VFSPath path = VFSUtils.parseVFSPath(data.url);
					String fileText = getFileText(path, vfs, field);
					System.out.println("This is text " + data.getText());
					String searchString = searchString(options, data.getText());
					int flag = calculateFlag(options);
					System.out.println("This is searchString " + searchString);
					String regex = getRegex(searchString);

					// START THE REPLACING
					Pattern compiledInnerRegex = Pattern.compile(searchString, flag);
					Pattern compiledRegex = Pattern.compile(regex, flag);
					Matcher regexMatcher = compiledRegex.matcher(fileText);
					StringBuffer buffer = new StringBuffer();
					int counter = 0;

					while (regexMatcher.find()) {
						if (indexList.containsKey(counter + "")) {
							System.out.println("It did contain " + counter);
							SentenceReplacementData senData = indexList.get(counter + "");
							int beginIndex = senData.getBeginIndex();

							if (regexMatcher.end() == beginIndex) {
								System.out.println("They were equal");
								String replacement = calculateReplacement(regexMatcher.group(), indexList.get(counter
										+ ""), compiledInnerRegex);

								System.out.println("This is replacement " + replacement);

								regexMatcher.appendReplacement(buffer, replacement);
							} else {
								// System.out.println(
								// "They weren't equal");
								problemsReplacing = true;
							}
						}
						counter++;
					}
					regexMatcher.appendTail(buffer);

					// START THE REPLACING IN THE REAL TEXT FILE
					Pattern fieldRegex = Pattern.compile(getFieldSpecificRegex(field));
					Matcher matcher = fieldRegex.matcher(originalFileText);
					StringBuffer newFileText = new StringBuffer();
					if (matcher.find()) {
						matcher.appendReplacement(newFileText, buffer.toString());
						matcher.appendTail(newFileText);
					} else {
						problemsReplacing = true;
					}

					// boolean found = false;
					// Element parentElement = doc.getDocumentElement();
					// NodeList list =
					// parentElement.getElementsByTagName("field");
					// Element elementToReplace = null;
					// int i = 0;
					// while(!found && i < list.getLength())
					// {
					// elementToReplace = (Element) list.item(i);
					// if
					// (elementToReplace.getAttribute("id").equalsIgnoreCase(field
					// ))
					// {
					// found = true;
					// }
					// i++;
					// }

					if (!problemsReplacing) {
						System.out.println("I wrote to the file");
						DocumentUtils.writeVFSFile(data.url, vfs, newFileText.toString());
					}
					FileLocker.impl.releaseLock(data.url);
					success = !problemsReplacing;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return success;
	}

	private String searchString(String options, String originalString) {
		if (isRegex(options)) {
			if (originalString.contains("*")) {
				System.out.println("It does contain *");
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

}
