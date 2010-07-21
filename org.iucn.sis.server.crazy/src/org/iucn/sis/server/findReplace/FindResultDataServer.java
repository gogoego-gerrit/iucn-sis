package org.iucn.sis.server.findReplace;

import java.util.ArrayList;
import java.util.HashMap;

import org.iucn.sis.server.utils.XMLUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.util.SysDebugger;
import com.solertium.vfs.VFSPath;

public class FindResultDataServer {

	public static class OldNewStrings {

		public String oldString;
		public String newString;
		public int index;

		public OldNewStrings(String oldString, String newString, int index) {
			this.oldString = oldString;
			this.newString = newString;
			this.index = index;
		}
	}

	public static class SentenceReplacementData {
		String groupIndex;
		String beginIndex;
		HashMap<String, OldNewStrings> replacementString;
		int numberOfIndicies;
		String originalString;
		boolean decided;
		int currentIndex;
		String resultDescription;

		//
		// public SentenceReplacementData(String resultDescription,
		// String index, String beginIndex, String originalString, String text,
		// int indexOfText, int numberOfIndicies ) {
		// this.groupIndex = index;
		// this.originalString = originalString;
		// this.resultDescription = resultDescription;
		// decided = false;
		// currentIndex = 0;
		// this.beginIndex = beginIndex;
		// this.numberOfIndicies = numberOfIndicies;
		// this.replacementString = new HashMap();
		// for (int i = 0; i < numberOfIndicies; i++)
		// {
		// replacementString.put(i+"", new OldNewStrings(text, null,
		// indexOfText));
		// }
		//			
		// }

		// public SentenceReplacementData(String resultDescription, String
		// index,
		// String beginIndex, String originalString) {
		// this.groupIndex = index;
		// this.beginIndex = beginIndex;
		// this.originalString = originalString;
		// this.replacementString = null;
		// this.numberOfIndicies = 0;
		// this.resultDescription = resultDescription;
		// decided = false;
		// currentIndex = 0;
		// }

		// public SentenceReplacementData(String resultDescription, String
		// index,
		// String beginIndex, String originalString, HashMap<String, String>
		// newStuffs) {
		// this.groupIndex = index;
		// this.beginIndex = beginIndex;
		// this.originalString = originalString;
		// this.replacementString = new HashMap<String, OldNewStrings>();
		// Iterator<String> iter = newStuffs.keySet().iterator();
		// int counter = 0;
		// while(iter.hasNext())
		// {
		// String key = iter.next();
		// replacementString.put(counter + "", new OldNewStrings(
		// (String) newStuffs.get(key), null,
		// Integer.parseInt(key) ));
		// SysDebugger.getInstance().println("I am putting " + counter + " " +
		// (String) newStuffs.get(key) + null +
		// Integer.parseInt(key));
		// counter++;
		// }
		// this.numberOfIndicies = replacementString.size();
		// this.resultDescription = resultDescription;
		// decided = false;
		// currentIndex = 0;
		// }

		public SentenceReplacementData(String resultDescription, String index, String beginIndex,
				String originalString, HashMap<String, OldNewStrings> newStuffs) {
			this.groupIndex = index;
			this.beginIndex = beginIndex;
			this.originalString = originalString;
			this.replacementString = newStuffs;
			this.numberOfIndicies = replacementString.size();
			this.resultDescription = resultDescription;
			decided = false;
			currentIndex = 0;
		}

		public int getBeginIndex() {
			try {
				return Integer.parseInt(beginIndex);
			} catch (Exception e) {
				return -1;
			}
		}

		public String getBeginIndexString() {
			return beginIndex;
		}

		public String getIndex() {
			return groupIndex;
		}

		public int getNumberOfIndicies() {
			return numberOfIndicies;
		}

		public String getNumberOfIndiciesString() {
			return numberOfIndicies + "";
		}

		public String getOldWord(int index) {
			try {
				String old = replacementString.get(index + "").oldString;
				if (old == null) {
					return "";
				} else
					return old;
			} catch (Exception e) {
				return "";
			}
		}

		public int getOldWordIndex(int index) {
			try {
				return replacementString.get(index + "").index;
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		}

		public String getOriginalString() {
			return originalString;
		}

		public HashMap getReplacementString() {
			return replacementString;
		}

		public String getResultDescription() {
			return resultDescription;
		}

		public boolean isDecided() {
			return decided;
		}

		public boolean needsReplacing() {
			return getReplacementString() == null;
		}

		public void replaceWith(String replacementString) {
			this.replacementString.get(currentIndex + "").newString = XMLUtils.clean(replacementString);
			setDecided(true);
		}

		public void setDecided(boolean decided) {
			this.decided = decided;
		}

		public void setIndex(String index) {
			this.groupIndex = index;
		}

		public void setOriginalString(String originalString) {
			this.originalString = originalString;
		}

		/**
		 * Only call when using the constructor without the hashmap argument.
		 * 
		 * @param replacementString
		 */
		public void setReplacementString(HashMap<String, OldNewStrings> replacementString) {
			this.numberOfIndicies = replacementString.size();
			this.replacementString = replacementString;
		}

		public void setReplacementString(int index, String replacementString) {
			this.replacementString.get("" + index).newString = replacementString;
		}

		public void skip() {
			this.replacementString = null;
			setDecided(true);
		}

	}

	/**
	 * Takes a node with one FindResultData object represented in xml format
	 * 
	 * @param doc
	 */
	public static FindResultDataServer fromXML(Element element) {
		FindResultDataServer returnData = new FindResultDataServer();
		returnData.text = element.getElementsByTagName("text").item(0).getTextContent();
		returnData.url = XMLUtils.cleanFromXML(element.getElementsByTagName("url").item(0).getTextContent());
		returnData.name = XMLUtils.cleanFromXML(element.getElementsByTagName("name").item(0).getTextContent());

		returnData.sentenceDataList = new ArrayList();
		NodeList list = element.getElementsByTagName("line");
		for (int i = 0; i < list.getLength(); i++) {
			try {
				Element lineElement = (Element) list.item(i);
				NodeList indexList = ((Element) lineElement.getElementsByTagName("replacement").item(0))
						.getElementsByTagName("index");
				HashMap<String, OldNewStrings> indexArray = new HashMap(indexList.getLength());
				for (int j = 0; j < indexList.getLength(); j++) {
					Element innerElement = (Element) indexList.item(j);
					indexArray.put(innerElement.getAttribute("count"), new OldNewStrings(null, innerElement
							.getTextContent(), 0));
				}
				// SentenceReplacementData data = new SentenceReplacementData(
				// lineElement.getElementsByTagName("description").item(0).
				// getTextContent(),
				// lineElement.getElementsByTagName("groupIndex").item(0).
				// getTextContent(),
				// lineElement.getElementsByTagName("beginIndex").item(0).
				// getTextContent(),
				// lineElement.getElementsByTagName("sentence").item(0).
				// getTextContent());
				// data.setReplacementString(indexArray);
				SentenceReplacementData data = new SentenceReplacementData(lineElement.getElementsByTagName(
						"description").item(0).getTextContent(), lineElement.getElementsByTagName("groupIndex").item(0)
						.getTextContent(), lineElement.getElementsByTagName("beginIndex").item(0).getTextContent(),
						lineElement.getElementsByTagName("sentence").item(0).getTextContent(), indexArray);
				returnData.sentenceDataList.add(data);
			} catch (Exception e) {
				e.printStackTrace();
				// GOES IN HERE IF NOTHING TO REPLACE, DON'T WANT IT ADDED TO
				// LIST ANYWAYS
			}
		}

		return returnData;
	}

	String url;
	String text;
	String regex;

	String type;

	String name;

	boolean isFinished;

	ArrayList sentenceDataList;

	public FindResultDataServer() {
		url = null;
		text = null;
		regex = null;
		name = null;
		sentenceDataList = new ArrayList();
		isFinished = false;
	}

	public FindResultDataServer(String text, String regex, String url, String name) {
		this.url = url;
		this.text = text;
		this.regex = regex;
		this.name = name;
		sentenceDataList = new ArrayList();
		isFinished = false;
	}

	public FindResultDataServer(String text, VFSPath url) {
		this.url = url.toString();
		this.text = text;
		this.regex = null;
		this.name = null;
		sentenceDataList = new ArrayList();
		isFinished = false;
	}

	public void addNewFinding(String resultDescription, int groupIndex, int beginIndex, String sentence,
			HashMap<String, OldNewStrings> indexToText) {

		sentenceDataList.add(new SentenceReplacementData(resultDescription, groupIndex + "", beginIndex + "", sentence,
				indexToText));
	}

	public String getName() {
		return name;
	}

	public String getRegex() {
		return regex;
	}

	public SentenceReplacementData getSentenceData(int index) {
		return (SentenceReplacementData) sentenceDataList.get(index);
	}

	public ArrayList getSentenceDataList() {
		return sentenceDataList;
	}

	public String getText() {
		return text;
	}

	public String getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toXML() {
		String xml = "<result>\r\n";
		xml += "<text>" + text + "</text>\r\n";

		for (int i = 0; i < sentenceDataList.size(); i++) {
			SentenceReplacementData data = (SentenceReplacementData) sentenceDataList.get(i);
			String groupIndex = data.getIndex();
			String beginIndex = data.getBeginIndexString();
			String sentence = data.getOriginalString();
			String numberOfIndicies = data.getNumberOfIndiciesString();
			String description = data.getResultDescription();
			String text = "<texts>\r\n";
			for (int j = 0; j < data.getNumberOfIndicies(); j++) {
				text += "<text index=\"" + data.getOldWordIndex(j) + "\">" + data.getOldWord(j) + "</text>\r\n";
			}
			text += "</texts>\r\n";
			xml += "<line>\r\n <groupIndex>" + groupIndex + "</groupIndex>\r\n" + "<beginIndex>" + beginIndex
					+ "</beginIndex>\r\n<sentence>" + sentence + "</sentence>\r\n<indexCount>" + numberOfIndicies
					+ "</indexCount>\r\n<description>" + description + "</description>\r\n" + text;
			xml += "</line>\r\n";
		}
		xml += "<url>" + url + "</url>\r\n";
		xml += "<name>" + name + "</name>\r\n";
		xml += "</result>\r\n";

		SysDebugger.getInstance().println("I will return " + xml);
		return xml;
	}

}
